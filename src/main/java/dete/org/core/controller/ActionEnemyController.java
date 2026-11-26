package dete.org.core.controller;

import dete.org.core.presenters.CombatPresenter;
import dete.org.core.dto.AttackResult;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.EnemyInput;

/**
 * Controla as ações executadas pelo inimigo durante o combate.
 *
 * <p>Seleciona e executa ações como ataque, defesa e regeneração.</p>
 */
public class ActionEnemyController {

    /** Valor base da regeneração do inimigo, antes do multiplicador. */
    private static final double REGEN_AMOUNT = 80;

    private final CombatPresenter actionPresenter;

    public ActionEnemyController(CombatPresenter combatPresenter) {
        this.actionPresenter = combatPresenter;
    }

    public void execute(EnemyInput.Action action, Enemy enemy, Player player) {
        switch (action) {
            case ATTACK -> {
                AttackResult result = enemy.attack(player);
                actionPresenter.presenterAttack(result, enemy);
            }
            case DEFENSE -> {
                enemy.applyEffect(new Effect("Postura Defensiva!", 0.85));
                actionPresenter.presenterDefense(enemy);
            }
            case REGENERATION -> {
                if (enemy.canRegenerate()) {
                    double previousLife = enemy.getLife().getValue();
                    double rolledHeal = enemy.regeneration(REGEN_AMOUNT);
                    enemy.triggerRegenCooldown();
                    double restoredLife = enemy.getLife().getValue() - previousLife;
                    actionPresenter.presenterEnemyRegeneration(enemy, restoredLife, rolledHeal > REGEN_AMOUNT);
                } else {
                    // Se estiver em cooldown, a IA ataca como fallback
                    AttackResult result = enemy.attack(player);
                    actionPresenter.presenterAttack(result, enemy);
                }
            }
        }
    }
}
