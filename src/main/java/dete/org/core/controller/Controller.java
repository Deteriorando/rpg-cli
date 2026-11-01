package dete.org.core.controller;

import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.EnemyInput;
import dete.org.core.input.InputScanner;
import dete.org.core.input.UserAction;
import dete.org.view.terminal.MenuIU;
import dete.org.view.terminal.TerminalUI;


public class Controller {
    protected int rounds;

    public Controller() {
        this.rounds = 1;
    }

    public static void start(Player player, Enemy enemy, InputScanner scanner) {
        Controller combat = new Controller();
        MenuIU.startCombateMessage(player, enemy);

        while (player.isAlive() && enemy.isAlive()) {
            MenuIU.roundHeaderMessage(combat.rounds, player, enemy);

            combat.executePlayerAction(player, enemy, scanner);

            if (!enemy.isAlive()) {
                break;
            }

            combat.executeEnemyAction(enemy, player);

            // Atualiza os cooldowns no fim do turno completo
            if (!enemy.canRegenerate()) enemy.updateCooldowns();

            combat.rounds++;
            TerminalUI.pause(scanner);
        }

        MenuIU.combateDividerMessage();
        if (player.isAlive()) {
            MenuIU.winMessage(enemy, combat.rounds);
        } else {
            MenuIU.defeatMessage(enemy);
        }

        player.resetToClassStats();
    }

    private void executePlayerAction(Player player, Enemy enemy, InputScanner inputScanner) {
        boolean validAction;

        do {
            MenuIU.helpActionPlayer(player);

            int choice = inputScanner.getCodeInt();
            UserAction action = UserAction.getByCode(choice);

            TerminalUI.clearConsole();
            TerminalUI.delay(100);
            validAction = new ActionPlayerController(inputScanner).execute(action, player, enemy);

        } while (!validAction);
    }

    private void executeEnemyAction(Enemy enemy, Player player) {
        EnemyInput enemyAction = new EnemyInput();
        long timer = 300;

        int randomAction = enemyAction.getRandomCode(); // 1 a 5
        EnemyInput.Action action = EnemyInput.Action.getByCode(randomAction);

        MenuIU.enemyTurnHeaderMessage();
        TerminalUI.delay(timer);
        new ActionEnemyController().execute(action, enemy, player);
    }
}