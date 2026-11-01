package dete.org.core.presenters;

import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.InputScanner;

public interface MenuPresenter {
    void presenterStart(Player player, Enemy enemy);
    void presenterRoundHeader(int rounds, Player player, Enemy enemy);
    void presenterRoundEnd(InputScanner scanner);
    void presenterWin(Enemy enemy, int rounds);
    void presenterDefeat(Enemy enemy);
    void presenterHelpAction(Player player);
    void presenterEnemyTurnHeader();
}
