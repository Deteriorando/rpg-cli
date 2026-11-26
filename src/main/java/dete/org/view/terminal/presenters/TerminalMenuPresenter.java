package dete.org.view.terminal.presenters;

import dete.org.audio.SoundEffects;
import dete.org.core.entity.Classes;
import dete.org.core.entity.Entity;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.PlayerInput;
import dete.org.core.presenters.MenuPresenter;
import dete.org.view.terminal.MenuIU;
import dete.org.view.terminal.TerminalUI;
import dete.org.view.terminal.tui.CombatTuiController;

/**
 * Implementação terminal do {@link MenuPresenter}.
 *
 * <p>Converte as solicitações de apresentação do núcleo do jogo em TUI ASCII
 * durante o combate e mantém as telas de menu/perfil/classes para fora do combate.</p>
 */
public class TerminalMenuPresenter implements MenuPresenter {

    private final PlayerInput input;

    public TerminalMenuPresenter(PlayerInput input) {
        this.input = input;
    }

    @Override
    public void presenterStart(Player player, Enemy enemy) {
        CombatTuiController.startCombat(player, enemy);
    }

    @Override
    public void presenterRoundHeader(int rounds, Player player, Enemy enemy) {
        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            tui.onRoundStart(rounds);
        } else {
            MenuIU.roundHeaderMessage(rounds, player, enemy);
        }
    }

    @Override
    public void presenterRoundEnd() {
        if (!CombatTuiController.isActive()) {
            MenuIU.combateDividerMessage();
            TerminalUI.pause(input);
        }
    }

    @Override
    public void presenterWin(Enemy enemy, int rounds) {
        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            tui.onVictory(rounds);
        } else {
            MenuIU.winMessage(enemy, rounds);
        }
    }

    @Override
    public void presenterDefeat(Enemy enemy) {
        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            tui.onDefeat();
        } else {
            MenuIU.defeatMessage(enemy);
        }
    }

    @Override
    public void presenterHelpAction(Player player) {
        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            tui.onPlayerDecisionPrompt();
        } else {
            MenuIU.helpActionPlayer(player);
        }
    }

    @Override
    public void presenterActionConfirmed() {
        SoundEffects.play(SoundEffects.Cue.CONFIRM);
    }

    @Override
    public void presenterEnemyTurnHeader() {
        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            tui.onEnemyTurnStart();
        } else {
            MenuIU.enemyTurnHeaderMessage();
        }
    }

    @Override
    public void presenterCombatEnd() {
        CombatTuiController.endCombat();
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
        // Só é chamado quando ConfigurePlayer validou a escolha.
        SoundEffects.play(SoundEffects.Cue.CONFIRM);
        MenuIU.classSelected(classes);
    }
}
