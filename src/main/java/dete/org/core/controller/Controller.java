package dete.org.core.controller;

import dete.org.core.input.PlayerInput;
import dete.org.core.presenters.CombatPresenter;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.EnemyInput;
import dete.org.core.input.UserAction;
import dete.org.core.presenters.InventoryPresenter;
import dete.org.core.presenters.MenuPresenter;

/**
 * Coordena o fluxo principal de um combate entre um jogador e um inimigo.
 *
 * <p>Responsável por controlar as rodadas, solicitar ações do jogador,
 * executar as ações do inimigo e comunicar os eventos de apresentação
 * através dos presenters.</p>
 */
public class Controller {
    protected int rounds;

    private final PlayerInput            input;
    private final ActionPlayerController actionPlayerController;
    private final ActionEnemyController  actionEnemyController;
    private final MenuPresenter menuPresenter;

    public Controller(PlayerInput input,
                      CombatPresenter combatPresenter,
                      MenuPresenter menuPresenter,
                      InventoryPresenter inventoryPresenter
                      ) {
        this.input = input;
        actionPlayerController = new ActionPlayerController(
                input,
                combatPresenter,
                inventoryPresenter
        );
        actionEnemyController = new ActionEnemyController(combatPresenter);
        this.menuPresenter = menuPresenter;
    }

    public void start(Player player, Enemy enemy) {
        rounds = 1;
        try {
            menuPresenter.presenterStart(player, enemy);
            while (player.isAlive() && enemy.isAlive() && !input.isEndOfInput()) {
                menuPresenter.presenterRoundHeader(rounds, player, enemy);

                executePlayerAction(player, enemy);
                if (input.isEndOfInput()) {
                    break;
                }

                if (!enemy.isAlive()) {
                    break;
                }

                executeEnemyAction(enemy, player);

                // Atualiza os cooldowns no fim do turno completo
                if (!enemy.canRegenerate()) enemy.updateCooldowns();

                if (!player.isAlive()) {
                    break;
                }
                rounds++;
                menuPresenter.presenterRoundEnd();
            }

            if (!input.isEndOfInput()) {
                if (player.isAlive()) {
                    menuPresenter.presenterWin(enemy, rounds);
                } else {
                    menuPresenter.presenterDefeat(enemy);
                }
            }
        } finally {
            try {
                menuPresenter.presenterCombatEnd();
            } finally {
                player.resetToClassStats();
            }
        }
    }

    private void executePlayerAction(Player player, Enemy enemy) {
        boolean validAction;

        do {
            menuPresenter.presenterHelpAction(player);

            int choice = input.getCodeInt();
            if (input.isEndOfInput()) {
                return;
            }
            UserAction action = UserAction.getByCode(choice);

            if (action != UserAction.INVALID) {
                menuPresenter.presenterActionConfirmed();
            }

            validAction = actionPlayerController.execute(action, player, enemy);

        } while (!validAction && !input.isEndOfInput());
    }

    private void executeEnemyAction(Enemy enemy, Player player) {
        EnemyInput enemyAction = new EnemyInput();

        int randomAction = enemyAction.getRandomCode(); // 1 a 5
        EnemyInput.Action action = EnemyInput.Action.getByCode(randomAction);

        menuPresenter.presenterEnemyTurnHeader();
        actionEnemyController.execute(action, enemy, player);
    }
}
