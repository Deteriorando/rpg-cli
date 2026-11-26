package dete.org.init;

import dete.org.audio.MusicEngine;
import dete.org.audio.SoundEffects;
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

    public static void run() {
        Player player = new Player(Classes.HUMAN, "Dete");

        // Itens iniciais, só para testar o inventário
        player.addItemToInventory(new HealthPotion(80), 10);

        PlayerInput input = new InputScanner();
        CombatPresenter presenter = new TerminalActionPresenter();
        MenuPresenter menuPresenter = new TerminalMenuPresenter(input);
        InventoryPresenter inventoryPresenter = new TerminalInventoryPresenter();

        try (SoundEffects soundEffects = SoundEffects.open()) {
            while (!input.isEndOfInput()) {
                MenuIU.menu();
                int chose = input.getCodeInt();
                if (input.isEndOfInput()) {
                    return;
                }

                // Opção 4 encerra o jogo e fecha o áudio em seguida; não dispara
                // confirmação para não cortar o som no fechamento.
                if (chose >= 1 && chose <= 3) {
                    SoundEffects.play(SoundEffects.Cue.CONFIRM);
                }

                switch (chose) {
                    case 1 -> {
                        Enemy enemy = ConfigureEnemy.createRandomEnemy();
                        try (var music = MusicEngine.loop("/audio/ordinary-battle-loop.wav")) {
                            SoundEffects.play(SoundEffects.Cue.BATTLE_START);
                            new Controller(
                                    input,
                                    presenter,
                                    menuPresenter,
                                    inventoryPresenter
                            ).start(player, enemy);
                        }
                    }
                    case 2 -> MenuIU.profile(player);
                    case 3 -> new ConfigurePlayer(input, menuPresenter).configure(player);
                    case 4 -> { return; }
                }
            }
        }
    }
}
