package dete.org.view.terminal;

import dete.org.core.input.PlayerInput;

public class TerminalUI {
    public static void pause(PlayerInput scan) {
        println("\nPróxima rodada...");
        println("- Pressione ENTER para continuar -");
        scan.next();
        clearConsole();
    }

    public static void delay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void clearConsole() {
        try {
            // 1. Sequência ANSI para limpar terminal real / emulado
            print("\033[H\033[2J");
            System.out.flush();

            // 2. Form Feed ('\f')
            print("\014");
            System.out.flush();

            // 3. Comando de sistema caso seja Windows
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) {
                println("");
            }
        }
    }

    //Compatibilidade
    public static void println(String message) {
        System.out.println(message);
    }

    public static void print(String message) {
        System.out.print(message);
    }
}
