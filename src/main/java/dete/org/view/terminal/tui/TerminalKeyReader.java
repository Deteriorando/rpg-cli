package dete.org.view.terminal.tui;

import dete.org.view.terminal.tui.TerminalSession.KeyEvent;
import dete.org.view.terminal.tui.TerminalSession.KeyType;

import java.io.IOException;
import java.io.InputStream;

final class TerminalKeyReader {
    private TerminalKeyReader() {}

    static KeyEvent read(InputStream in) throws IOException, InterruptedException {
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
}
