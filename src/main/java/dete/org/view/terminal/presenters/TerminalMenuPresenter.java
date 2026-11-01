package dete.org.view.terminal.presenters;

import dete.org.core.input.InputScanner;
import dete.org.core.presenters.MenuPresenter;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.view.terminal.MenuIU;
import dete.org.view.terminal.TerminalUI;

public class TerminalMenuPresenter implements MenuPresenter {
    @Override
    public void presenterStart(Player player, Enemy enemy) {
        MenuIU.startCombateMessage(player, enemy);
    }

    @Override
    public void presenterRoundHeader(int rounds, Player player, Enemy enemy) {
        MenuIU.roundHeaderMessage(rounds, player, enemy);
    }

    @Override
    public void presenterRoundEnd(InputScanner scanner) {
        MenuIU.combateDividerMessage();
        TerminalUI.pause(scanner);
    }

    @Override
    public void presenterWin(Enemy enemy, int rounds) {
        MenuIU.winMessage(enemy, rounds);
    }

    @Override
    public void presenterDefeat(Enemy enemy) {
        MenuIU.defeatMessage(enemy);
    }

    @Override
    public void presenterHelpAction(Player player) {
        MenuIU.helpActionPlayer(player);
    }

    @Override
    public void presenterEnemyTurnHeader() {
        MenuIU.enemyTurnHeaderMessage();
    }
}
