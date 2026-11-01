package dete.org.core.init;

import dete.org.core.presenters.CombatPresenter;
import dete.org.core.entity.Classes;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.controller.Controller;
import dete.org.core.input.InputScanner;
import dete.org.core.presenters.InventoryPresenter;
import dete.org.core.presenters.MenuPresenter;
import dete.org.view.terminal.MenuIU;
import dete.org.core.inventory.items.HealthPotion;
import dete.org.view.terminal.presenters.TerminalActionPresenter;
import dete.org.view.terminal.presenters.TerminalInventoryPresenter;
import dete.org.view.terminal.presenters.TerminalMenuPresenter;

/**
 * <p>
 *     Está é a classe que vai rodar o jogo infinitamente.
 * </p>
 * */

public class GameLoop {

    private static boolean runnable = true;

    public static void run() {
        Player player = new Player(Classes.HUMAN, "Dete");

        // Itens iniciais, só para testar o inventário
        player.addItemToInventory(new HealthPotion(80), 10);

        InputScanner input = new InputScanner();
        CombatPresenter presenter = new TerminalActionPresenter();
        MenuPresenter menuPresenter = new TerminalMenuPresenter();
        InventoryPresenter inventoryPresenter = new TerminalInventoryPresenter();

        while (runnable){
            MenuIU.menu();
            int chose = input.getCodeInt();

            switch (chose) {
                case 1 -> {
                    Enemy enemy = ConfigureEnemy.createRandomEnemy();
                    new Controller(
                            input,
                            presenter,
                            menuPresenter,
                            inventoryPresenter
                    ).start(player, enemy);
                }
                case 2 -> MenuIU.profile(player);
                case 3 -> new ConfigurePlayer(input).configure(player);
                case 4 -> runnable = false;
            }
        }
    }
}