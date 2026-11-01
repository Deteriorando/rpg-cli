package dete.org.view.terminal;

import dete.org.core.input.PlayerInput;

import java.util.Scanner;

/**
 * Centraliza a leitura de entradas do usuário através do terminal.
 *
 * <p>Encapsula o {@link java.util.Scanner} utilizado para ler entradas
 * do {@code System.in} e fornece operações específicas utilizadas pelo jogo.</p>
 */
public class InputScanner implements PlayerInput {
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Retorna apenas um número.
     * */
    public int getCodeInt() {
        if (scanner.hasNextInt()) {
            int value = scanner.nextInt();
            next();
            return value;
        }

        // descarta a entrada inválida
        next();
        return -1;
    }

    public void next() {
        scanner.nextLine();
    }

    public Scanner getScanner() {
        return scanner;
    }
}
