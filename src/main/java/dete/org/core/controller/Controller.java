package dete.org.core.controller;

import dete.org.core.presenters.CombatPresenter;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.EnemyInput;
import dete.org.core.input.InputScanner;
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

    private final InputScanner           scanner;
    private final ActionPlayerController actionPlayerController;
    private final ActionEnemyController  actionEnemyController;
    private final MenuPresenter menuPresenter;

    public Controller(InputScanner scanner,
                      CombatPresenter combatPresenter,
                      MenuPresenter menuPresenter,
                      InventoryPresenter inventoryPresenter
                      ) {
        this.rounds = 1;
        this.scanner = scanner;
        actionPlayerController = new ActionPlayerController(
                scanner,
                combatPresenter,
                inventoryPresenter
        );
        actionEnemyController = new ActionEnemyController(combatPresenter);
        this.menuPresenter = menuPresenter;
    }

    public void start(Player player, Enemy enemy) {
        menuPresenter.presenterStart(player, enemy);

        while (player.isAlive() && enemy.isAlive()) {
            menuPresenter.presenterRoundHeader(rounds, player, enemy);

            executePlayerAction(player, enemy);

            if (!enemy.isAlive()) {
                break;
            }

            executeEnemyAction(enemy, player);

            // Atualiza os cooldowns no fim do turno completo
            if (!enemy.canRegenerate()) enemy.updateCooldowns();

            rounds++;
            menuPresenter.presenterRoundEnd(scanner);
        }

        if (player.isAlive()) {
            menuPresenter.presenterWin(enemy, rounds);
        } else {
            menuPresenter.presenterDefeat(enemy);
        }

        player.resetToClassStats();
    }

    private void executePlayerAction(Player player, Enemy enemy) {
        boolean validAction;

        do {
            menuPresenter.presenterHelpAction(player);

            int choice = scanner.getCodeInt();
            UserAction action = UserAction.getByCode(choice);

            validAction = actionPlayerController.execute(action, player, enemy);

        } while (!validAction);
    }

    private void executeEnemyAction(Enemy enemy, Player player) {
        EnemyInput enemyAction = new EnemyInput();

        int randomAction = enemyAction.getRandomCode(); // 1 a 5
        EnemyInput.Action action = EnemyInput.Action.getByCode(randomAction);

        menuPresenter.presenterEnemyTurnHeader();
        actionEnemyController.execute(action, enemy, player);
    }
}