package dete.org.core.Game;

import dete.org.core.Entity.Classes;
import dete.org.core.Entity.Entities.Enemy;
import dete.org.core.Entity.Entities.Player;
import dete.org.core.Controller.Controller;
import dete.org.core.Interface.MenuInterface;
import dete.org.core.Inventory.Items.HealthPotion;

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