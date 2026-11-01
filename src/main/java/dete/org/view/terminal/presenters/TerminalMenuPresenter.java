package dete.org.view.terminal.presenters;

import dete.org.core.entity.Classes;
import dete.org.core.entity.Entity;
import dete.org.core.input.PlayerInput;
import dete.org.core.presenters.MenuPresenter;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.view.terminal.MenuIU;
import dete.org.view.terminal.TerminalUI;


/**
 * Implementação terminal do {@link MenuPresenter}.
 *
 * <p>Converte as solicitações de apresentação do núcleo do jogo
 * em mensagens exibidas através da interface de terminal.</p>
 */
public class TerminalMenuPresenter implements MenuPresenter {

    private final PlayerInput input;
    public TerminalMenuPresenter(PlayerInput input) {
        this.input = input;
    }

    @Override
    public void presenterStart(Player player, Enemy enemy) {
        MenuIU.startCombateMessage(player, enemy);
    }

    @Override
    public void presenterRoundHeader(int rounds, Player player, Enemy enemy) {
        MenuIU.roundHeaderMessage(rounds, player, enemy);
    }

    @Override
    public void presenterRoundEnd() {
        MenuIU.combateDividerMessage();
        TerminalUI.pause(input);
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

    @Override
    public void presenterShowProfile(Entity entity) {
        MenuIU.profile(entity);
    }

    @Override
    public void presenterShowClasses(Classes[] classes) {
        MenuIU.showClasses(classes);
    }

    @Override
    public void presenterBarDividerClasses() {
        MenuIU.barDividerClasses();
    }

    @Override
    public void presenterAskClassNumber() {
        MenuIU.askClassNumber();
    }

    @Override
    public void presenterOptionNumberInvalidClasses() {
        MenuIU.optionNumberInvalidClass();
    }

    @Override
    public void presenterShowClassSelected(Classes classes) {
        MenuIU.classSelected(classes);
    }
}
