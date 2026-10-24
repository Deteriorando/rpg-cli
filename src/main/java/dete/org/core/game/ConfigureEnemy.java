package dete.org.core.game;

import dete.org.core.entity.Classes;
import dete.org.core.entity.entities.Enemy;
import java.util.concurrent.ThreadLocalRandom;

public record ConfigureEnemy() {

    private static final String[] NAMES = {"Joe", "Rex", "Bruto", "Mira", "Zeno"};

    /** Cria um inimigo novo, com classe e nome sorteados. Nada de uma luta passa para a próxima. */
    public static Enemy createRandomEnemy() {
        Classes[] classes = Classes.values();
        Classes type = classes[ThreadLocalRandom.current().nextInt(classes.length)];
        String name = NAMES[ThreadLocalRandom.current().nextInt(NAMES.length)];
        return new Enemy(type, name);
    }

    public static void scaleEnemy(Enemy enemy, int level) {
        enemy.clearEffect();
        double newLife = 1500 + (level * 200);
        double newDamage = 20 + (level * 5);

        setAttributes(enemy, newLife, newDamage, enemy.getMultiplied().getValue());
    }

    public static void configure(Enemy enemy, Classes newType, double life, double damage, double multiplier) {
        enemy.clearEffect();
        enemy.setClasses(newType);
        setAttributes(enemy, life, damage, multiplier);
    }

    private static void setAttributes(Enemy enemy, double life, double damage, double multiplier) {
        enemy.setLife(life);
        enemy.setDamage(damage);
        enemy.setMultiplied(multiplier);
    }

}