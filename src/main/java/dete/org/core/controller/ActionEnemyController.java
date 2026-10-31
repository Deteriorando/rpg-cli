package dete.org.core.controller;

import dete.org.core.dto.AttackResult;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.EnemyInput;
import dete.org.view.terminal.CombatUI;

public class ActionEnemyController {
    public void execute(EnemyInput.Action action, dete.org.core.entity.entities.Enemy enemy, Player player) {
        switch (action) {
            case ATTACK -> {
                AttackResult result = enemy.attack(player);
                if (result.critical()) CombatUI.multiplierActivatedMessage(enemy.getName());
                CombatUI.damageDealtMessage(enemy.getName(), result.damage());
            }
            case DEFENSE -> {
                enemy.applyEffect(new Effect("Postura Defensiva!", 0.85));
                CombatUI.enemyDefenseMessage(enemy);
            }
            case REGENERATION -> {
                if (enemy.canRegenerate()) {
                    enemy.regeneration(80);
                    enemy.triggerRegenCooldown();
                    CombatUI.enemyRegenMessage(enemy);
                } else {
                    // Se estiver em cooldown, a IA ataca como fallback
                    AttackResult result = enemy.attack(player);
                    if (result.critical()) CombatUI.multiplierActivatedMessage(enemy.getName());
                    CombatUI.damageDealtMessage(enemy.getName(), result.damage());
                }
            }
        }
    }
}
