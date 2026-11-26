package dete.org.view.terminal.tui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Gerencia detecção de TTY, modo raw via {@code stty}, tela alternativa,
 * leitura de teclas (setas/números/Enter/Esc) e detecção de redimensionamento.
 */
public final class TerminalSession implements AutoCloseable {

    public enum KeyType {
        LEFT, RIGHT, UP, DOWN, TAB,
        ENTER, ESCAPE, BACKSPACE,
        DIGIT, CHAR, EOF
    }

    public record KeyEvent(KeyType type, char ch, int digit) {
        public static KeyEvent of(KeyType type) {
            return new KeyEvent(type, '\0', -1);
        }

        public static KeyEvent digit(int d) {
            return new KeyEvent(KeyType.DIGIT, (char) ('0' + d), d);
        }

        public static KeyEvent ofChar(char c) {
            return new KeyEvent(KeyType.CHAR, c, -1);
        }
    }

    public record Size(int cols, int rows) {}

    private static final int DEFAULT_COLS = 116;
    private static final int DEFAULT_ROWS = 34;

    private String savedSttyState;
    private boolean rawActive = false;
    private boolean altScreenActive = false;
    private Thread shutdownHook;
    private Size lastSize;

    public boolean isInteractiveTty() {
        return rawActive;
    }

    public synchronized void enterTui() {
        var console = System.console();
        if (rawActive || console == null || !console.isTerminal()
                || "dumb".equalsIgnoreCase(System.getenv("TERM"))) {
            return;
        }
        String current = runStty("-g");
        if (current == null || current.isEmpty()) {
            return;
        }
        savedSttyState = current;
        if (runStty("-icanon", "-echo", "-ixon", "min", "1", "time", "0") == null) {
            return;
        }
        rawActive = true;
        shutdownHook = new Thread(this::restoreQuietly, "rpg-cli-tui-restore");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        System.out.print("\033[?1049h\033[?25l\033[2J\033[H");
        System.out.flush();
        altScreenActive = true;
        lastSize = querySize();
    }

    @Override
    public synchronized void close() {
        restoreQuietly();
        if (shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException ignored) {
                // JVM já em desligamento
            }
            shutdownHook = null;
        }
    }

    private synchronized void restoreQuietly() {
        if (altScreenActive) {
            System.out.print(AnsiStyle.RESET + "\033[?25h\033[?1049l");
            System.out.flush();
            altScreenActive = false;
        }
        if (rawActive && savedSttyState != null && !savedSttyState.isEmpty()) {
            boolean interrupted = Thread.interrupted();
            try {
                runStty(savedSttyState);
                rawActive = false;
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public Size getCurrentSize() {
        Size s = querySize();
        this.lastSize = s;
        return s;
    }

    private Size querySize() {
        if (rawActive) {
            String out = runStty("size");
            if (out != null && !out.isEmpty()) {
                String[] parts = out.trim().split("\\s+");
                if (parts.length == 2) {
                    try {
                        int rows = Integer.parseInt(parts[0]);
                        int cols = Integer.parseInt(parts[1]);
                        if (cols > 0 && rows > 0) {
                            return new Size(cols, rows);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        int envCols = parseEnvInt("COLUMNS", DEFAULT_COLS);
        int envRows = parseEnvInt("LINES", DEFAULT_ROWS);
        return new Size(envCols, envRows);
    }

    private static int parseEnvInt(String name, int fallback) {
        String val = System.getenv(name);
        if (val == null || val.isEmpty()) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(val.trim());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public void paintFrame(String frameText) {
        if (altScreenActive) {
            System.out.print("\033[H" + frameText + "\033[J");
            System.out.flush();
        } else {
            System.out.println(frameText);
            System.out.flush();
        }
    }

    public void clearAndPaintFrame(String frameText) {
        if (altScreenActive) {
            System.out.print("\033[2J\033[H" + frameText);
            System.out.flush();
        } else {
            paintFrame(frameText);
        }
    }

    /**
     * Aguarda uma tecla no modo raw TTY enquanto monitora redimensionamentos
     * da janela do terminal; quando redimensiona, invoca {@code onResize}.
     */
    public KeyEvent readKeyWatchingResize(Runnable onResize) {
        if (!rawActive) {
            return KeyEvent.of(KeyType.EOF);
        }

        InputStream in = System.in;
        long lastResizeCheck = System.currentTimeMillis();

        try {
            while (true) {
                if (in.available() > 0) {
                    return decodeNextKey(in);
                }

                long now = System.currentTimeMillis();
                if (now - lastResizeCheck >= 200) {
                    lastResizeCheck = now;
                    Size current = querySize();
                    if (!current.equals(lastSize)) {
                        lastSize = current;
                        if (onResize != null) {
                            onResize.run();
                        }
                    }
                }

                Thread.sleep(25);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return KeyEvent.of(KeyType.EOF);
        } catch (IOException e) {
            return KeyEvent.of(KeyType.EOF);
        }
    }

    private KeyEvent decodeNextKey(InputStream in) throws IOException, InterruptedException {
        int b = in.read();
        if (b == -1 || b == 4) { // EOF ou Ctrl+D
            return KeyEvent.of(KeyType.EOF);
        }
        if (b == '\r' || b == '\n') {
            return KeyEvent.of(KeyType.ENTER);
        }
        if (b == '\t') {
            return KeyEvent.of(KeyType.TAB);
        }
        if (b == 127 || b == 8) {
            return KeyEvent.of(KeyType.BACKSPACE);
        }
        if (b >= '0' && b <= '9') {
            return KeyEvent.digit(b - '0');
        }
        if (b == 27) { // ESC
            // Pequena espera para distinguir ESC isolado de sequências CSI/SS3
            for (int i = 0; i < 4 && in.available() == 0; i++) {
                Thread.sleep(10);
            }
            if (in.available() == 0) {
                return KeyEvent.of(KeyType.ESCAPE);
            }
            int next = in.read();
            if (next == '[' || next == 'O') {
                for (int i = 0; i < 4 && in.available() == 0; i++) {
                    Thread.sleep(5);
                }
                if (in.available() == 0) {
                    return KeyEvent.of(KeyType.ESCAPE);
                }
                int code = in.read();
                // Consome eventuais parâmetros numéricos CSI (ex: \033[1;5D) até a letra final
                while (code >= '0' && code <= '?' && in.available() > 0) {
                    code = in.read();
                }
                return switch (code) {
                    case 'A' -> KeyEvent.of(KeyType.UP);
                    case 'B' -> KeyEvent.of(KeyType.DOWN);
                    case 'C' -> KeyEvent.of(KeyType.RIGHT);
                    case 'D' -> KeyEvent.of(KeyType.LEFT);
                    case 'Z' -> KeyEvent.of(KeyType.LEFT); // Shift+Tab
                    default -> KeyEvent.of(KeyType.ESCAPE);
                };
            }
            return KeyEvent.of(KeyType.ESCAPE);
        }
        return KeyEvent.ofChar((char) b);
    }

    private String runStty(String... args) {
        String[] cmd = new String[args.length + 1];
        cmd[0] = "stty";
        System.arraycopy(args, 0, cmd, 1, args.length);
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            boolean finished = p.waitFor(1000, TimeUnit.MILLISECONDS);
            if (!finished) {
                p.destroyForcibly();
                return null;
            }
            if (p.exitValue() != 0) {
                return null;
            }
            return new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
