package dete.org.view.terminal.presenters;

import dete.org.audio.SoundEffects;
import dete.org.core.dto.AttackResult;
import dete.org.core.entity.Entity;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.presenters.CombatPresenter;
import dete.org.view.terminal.CombatUI;
import dete.org.view.terminal.MenuIU;
import dete.org.view.terminal.tui.CombatTuiController;

/**
 * Implementação de {@link CombatPresenter} para a interface de terminal.
 *
 * <p>Atualiza a TUI ASCII quando o combate gráfico está ativo ou delega
 * para as mensagens lineares clássicas fora dela.</p>
 */
public class TerminalActionPresenter implements CombatPresenter {

    @Override
    public void presenterAttack(AttackResult result, Entity entity) {
        // Um ataque real gera exatamente um som: crítico ou normal.
        SoundEffects.play(result.critical()
                ? SoundEffects.Cue.CRITICAL_HIT
                : SoundEffects.Cue.HIT);

        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            tui.onAttack(result, entity);
            return;
        }

        String name = entity.getName();
        if (result.critical()) {
            CombatUI.multiplierActivatedMessage(name);
        }
        CombatUI.damageDealtMessage(name, result.damage());
    }

    @Override
    public void presenterDefense(Entity entity) {
        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            if (entity.hasEffect()) {
                tui.onDefense(entity);
            }
            return;
        }

        if (entity.hasEffect()) {
            CombatUI.entityDefenseMessage(entity);
            CombatUI.defenseAbsorbedMessage(entity.getEffect());
        }
    }

    @Override
    public void presenterEnemyRegeneration(Enemy enemy, double healed, boolean critical) {
        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            tui.onEnemyRegeneration(enemy, healed, critical);
            return;
        }

        CombatUI.enemyRegenMessage(enemy, healed, critical);
    }

    @Override
    public void presenterInvalidMessage() {
        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            tui.onInvalidAction();
            return;
        }

        MenuIU.invalidChoiceMessage();
    }
}
