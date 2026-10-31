package dete.org.core.input;

import java.util.Scanner;

public class InputScanner {
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Retorna apenas um número.
     * */
    public int getCodeInt() {
        int code = scanner.nextInt();

        // limpa o ‘buffer’
        scanner.nextLine();
        return code;
    }

    public Scanner getScanner() {
        return scanner;
    }
}
