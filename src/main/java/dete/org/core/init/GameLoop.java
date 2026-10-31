package dete.org.core.init;

import dete.org.core.entity.Classes;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.controller.Controller;
import dete.org.core.input.InputScanner;
import dete.org.view.terminal.MenuIU;
import dete.org.core.inventory.items.HealthPotion;

import java.util.Scanner;

/**
 * <p>
 *     Está é a classe que vai rodar o jogo infinitamente.
 * </p>
 * */

public class GameLoop {

    private static boolean runnable = true;

    protected static Scanner scan = new Scanner(System.in);

    public static void run() {
        Player player = new Player(Classes.HUMAN);

        // Itens iniciais, só para testar o inventário
        player.addItemToInventory(new HealthPotion(80), 10);

        InputScanner input = new InputScanner();

        while (runnable){
            MenuIU.menu();
            int chose = input.getCodeInt();

            switch (chose) {
                case 1 -> {
                    Enemy enemy = ConfigureEnemy.createRandomEnemy();
                    Controller.start(player, enemy, scan);
                }
                case 2 -> MenuIU.profile(player);
                case 3 -> new ConfigurePlayer(scan).configure(player);
                case 4 -> runnable = false;
            }
        }
    }
}