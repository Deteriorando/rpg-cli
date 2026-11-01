package dete.org.core.input;

import java.util.Scanner;

public class InputScanner {
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Retorna apenas um número.
     * */
    public int getCodeInt() {
        if (scanner.hasNextInt()) {
            return scanner.nextInt();
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
