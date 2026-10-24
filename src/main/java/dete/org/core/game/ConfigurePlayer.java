package dete.org.core.game;

import dete.org.core.entity.Classes;
import dete.org.core.entity.entities.Player;
import dete.org.core.ui.MenuInterface;

import java.util.Scanner;

import static java.lang.IO.print;
import static java.lang.IO.println;

/**
 *  <p>
 *      Está classe ela muda a configuração da classe do personagem.
 *  </p>
 * */

public record ConfigurePlayer(Scanner scan) {

    public void configure(Player player) {
        println("---- Escolha sua Classe ----");
        Classes[] classes = Classes.values();

        for (int i = 0; i < classes.length; i++) {
            MenuInterface.delay(800 - (i * 100L));
            println((i + 1) + " - " + classes[i].getName());
        }

        MenuInterface.delay(MenuInterface.timerGeneral);
        print("Opção: ");

        if (!scan.hasNextInt()) {
            println("Entrada inválida!");
            scan.next(); // Limpa o texto inválido do buffer
            return;
        }

        int chose = scan.nextInt();
        if (chose < 1 || chose > classes.length) {
            println("Opção fora do intervalo!");
            return;
        }

        Classes selectedClass = classes[chose - 1];

        player.changeClass(selectedClass);

        MenuInterface.clearConsole();
        println("Classe escolhida: " + selectedClass.getName());
        print(player);
        MenuInterface.delay(MenuInterface.timerGeneral);
    }

}