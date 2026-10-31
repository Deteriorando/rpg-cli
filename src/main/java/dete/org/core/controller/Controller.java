package dete.org.core.controller;

import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.EnemyInput;
import dete.org.core.input.InputScanner;
import dete.org.core.input.UserAction;
import dete.org.core.ui.MenuInterface;

import java.util.Scanner;

public class Controller {
    protected int rounds;

    public Controller() {
        this.rounds = 1;
    }

    public static void start(Player player, dete.org.core.entity.entities.Enemy enemy, Scanner scan) {
        Controller combat = new Controller();
        MenuInterface.startCombateMessage(player, enemy);

        while (player.isAlive() && enemy.isAlive()) {
            MenuInterface.roundHeaderMessage(combat.rounds, player, enemy);

            combat.executePlayerAction(player, enemy);

            if (!enemy.isAlive()) {
                break;
            }

            combat.executeEnemyAction(enemy, player);

            // Atualiza os cooldowns no fim do turno completo
            if (!enemy.canRegenerate()) enemy.updateCooldowns();

            combat.rounds++;
            MenuInterface.pause(scan);
        }

        MenuInterface.combateDividerMessage();
        if (player.isAlive()) {
            MenuInterface.winMessage(enemy, combat.rounds);
        } else {
            MenuInterface.defeatMessage(enemy);
        }

        player.resetToClassStats();
    }

    private void executePlayerAction(Player player, Enemy enemy) {
        boolean validAction;

        do {
            MenuInterface.helpActionPlayer(player);

            InputScanner inputScanner = new InputScanner();

            int choice = inputScanner.getCodeInt();
            UserAction action = UserAction.getByCode(choice);

            MenuInterface.clearConsole();
            MenuInterface.delay(100);
            validAction = new ActionPlayerController().execute(action, player, enemy);

        } while (!validAction);
    }

    private void executeEnemyAction(Enemy enemy, Player player) {
        EnemyInput enemyAction = new EnemyInput();
        long timer = 300;

        int randomAction = enemyAction.getRandomCode(); // 1 a 5
        EnemyInput.Action action = EnemyInput.Action.getByCode(randomAction);

        MenuInterface.enemyTurnHeaderMessage();
        MenuInterface.delay(timer);
        new ActionEnemyController().execute(action, enemy, player);
    }
}