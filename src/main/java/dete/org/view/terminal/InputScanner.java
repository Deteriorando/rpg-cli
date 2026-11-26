package dete.org.view.terminal;

import dete.org.core.input.PlayerInput;
import dete.org.view.terminal.tui.CombatTuiController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Centraliza a leitura de entradas do usuário através do terminal.
 *
 * <p>Lê linhas diretamente de {@code System.in} sem buffer antecipado para
 * coexistir com o modo raw da TUI de combate, tratando EOF com segurança
 * quando executado sem TTY.</p>
 */
public class InputScanner implements PlayerInput {
    private boolean endOfInput = false;
    private Scanner fallbackScanner;

    /**
     * Retorna apenas um número. Quando a TUI de combate está ativa,
     * delega para leitura de teclas (setas/números/Enter) em TTY ou linha em não-TTY.
     */
    @Override
    public int getCodeInt() {
        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            int choice = tui.readChoiceCode(this::readIntFromLine);
            if (tui.isEofReached()) {
                endOfInput = true;
            }
            return choice;
        }
        return readIntFromLine();
    }

    private int readIntFromLine() {
        String line;
        while ((line = readLineUnbuffered()) != null) {
            if (line.isBlank()) {
                continue;
            }
            String token = line.strip().split("\\s+", 2)[0];
            try {
                return Integer.parseInt(token);
            } catch (NumberFormatException ignored) {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public void next() {
        readLineUnbuffered();
    }

    @Override
    public boolean isEndOfInput() {
        return endOfInput;
    }

    public Scanner getScanner() {
        if (fallbackScanner == null) {
            fallbackScanner = new Scanner(System.in);
        }
        return fallbackScanner;
    }

    private String readLineUnbuffered() {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(32);
        try {
            while (true) {
                int b = System.in.read();
                if (b == -1) {
                    if (buf.size() == 0) {
                        endOfInput = true;
                        return null;
                    }
                    break;
                }
                if (b == '\n') {
                    break;
                }
                if (b != '\r') {
                    buf.write(b);
                }
            }
            return buf.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            endOfInput = true;
            return null;
        }
    }
}
