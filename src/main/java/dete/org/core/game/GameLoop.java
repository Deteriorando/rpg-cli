package dete.org.core.game;

import dete.org.core.entity.Classes;
import dete.org.core.entity.Entities.Enemy;
import dete.org.core.entity.Entities.Player;
import dete.org.core.controller.Controller;
import dete.org.core.ui.MenuInterface;
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

        while (runnable){
            MenuInterface.menu();
            int chose = scan.nextInt();

            switch (chose) {
                case 1 -> {
                    Enemy enemy = ConfigureEnemy.createRandomEnemy();
                    Controller.start(player, enemy, scan);
                }
                case 2 -> MenuInterface.profile(player);
                case 3 -> new ConfigurePlayer(scan).configure(player);
                case 4 -> runnable = false;
            }
        }
    }
}