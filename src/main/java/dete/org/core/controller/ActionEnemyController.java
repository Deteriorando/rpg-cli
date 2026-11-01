package dete.org.core.controller;

import dete.org.core.presenters.CombatPresenter;
import dete.org.core.dto.AttackResult;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.EnemyInput;

public class ActionEnemyController {

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
                    enemy.regeneration(80);
                    enemy.triggerRegenCooldown();
                    actionPresenter.presenterEnemyRegenaration(enemy);
                } else {
                    // Se estiver em cooldown, a IA ataca como fallback
                    AttackResult result = enemy.attack(player);
                    actionPresenter.presenterAttack(result, enemy);
                }
            }
        }
    }
}
