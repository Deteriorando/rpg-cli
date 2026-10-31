package dete.org.core.init;

import dete.org.core.entity.Classes;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.InputScanner;
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
        InputScanner input = new InputScanner();

        for (int i = 0; i < classes.length; i++) {
            MenuInterface.delay(400);
            println((i + 1) + " - " + classes[i].getName());
        }

        MenuInterface.delay(MenuInterface.timerGeneral);
        print("Opção: ");

        int chose = input.getCodeInt();
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