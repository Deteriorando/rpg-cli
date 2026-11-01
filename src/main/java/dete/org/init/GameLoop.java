package dete.org.init;

import dete.org.core.entity.configEntities.ConfigureEnemy;
import dete.org.core.entity.configEntities.ConfigurePlayer;
import dete.org.core.input.PlayerInput;
import dete.org.core.presenters.CombatPresenter;
import dete.org.core.entity.Classes;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.controller.Controller;
import dete.org.view.terminal.InputScanner;
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

        PlayerInput input = new InputScanner();
        CombatPresenter presenter = new TerminalActionPresenter();
        MenuPresenter menuPresenter = new TerminalMenuPresenter(input);
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
                case 3 -> new ConfigurePlayer(input, menuPresenter).configure(player);
                case 4 -> runnable = false;
            }
        }
    }
}