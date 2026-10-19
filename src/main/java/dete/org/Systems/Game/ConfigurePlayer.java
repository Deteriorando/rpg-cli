package dete.org.Systems.Game;

import dete.org.Entity.Classes;
import dete.org.Entity.Entities.Player;
import dete.org.Systems.Combat.Effect;
import dete.org.Systems.Interface.MenuInterface;

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

        applyClassAttributes(player, selectedClass);

        MenuInterface.clearConsole();
        println("Classe escolhida: " + selectedClass.getName());
        print(player);
        MenuInterface.delay(MenuInterface.timerGeneral);
    }

    public static void reset(Player player){
        player.clearEffect();
        applyClassAttributes(player, player.getClasses());
    }

    private static void applyClassAttributes(Player player, Classes selectedClass) {
        player.setClasses(selectedClass);

        switch (selectedClass) {
            case WIZARD   -> setAttributes(player, 2220, 35, 2);
            case WITCH    -> setAttributes(player, 2220, 35, 4);
            case WARRIOR  -> setAttributes(player, 1820, 45, 3);
            case BERSEKER -> setAttributes(player, 1720, 27.5, 6);
            default       -> setAttributes(player, 1500, 33.4, 5); // HUMAN
        }
    }

    private static void setAttributes(Player player, double life, double damage, double multiplied) {
        player.setLife(life);
        player.setDamage(damage);
        player.setMultiplied(multiplied);
    }


}