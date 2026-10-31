package dete.org.core.controller;

import dete.org.core.entity.entities.Player;
import dete.org.core.input.EnemyInput;
import dete.org.core.ui.MenuInterface;

public class ActionEnemyController {
    public void execute(EnemyInput.Action action, dete.org.core.entity.entities.Enemy enemy, Player player) {
        switch (action) {
            case ATTACK -> {
                double damage = enemy.attack(player);
                MenuInterface.damageDealtMessage(enemy.getName(), damage);
            }
            case DEFENSE -> {
                enemy.applyEffect(new Effect("Postura Defensiva!", 0.85));
                MenuInterface.enemyDefenseMessage(enemy);
            }
            case REGENERATION -> {
                if (enemy.canRegenerate()) {
                    enemy.regeneration(80);
                    enemy.triggerRegenCooldown();
                    MenuInterface.enemyRegenMessage(enemy);
                } else {
                    // Se estiver em cooldown, a IA ataca como fallback
                    double damage = enemy.attack(player);
                    MenuInterface.damageDealtMessage(enemy.getName(), damage);
                }
            }
        }
    }
}
