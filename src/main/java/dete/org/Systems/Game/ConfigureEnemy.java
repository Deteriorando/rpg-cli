package dete.org.Systems.Game;

import dete.org.Entity.Classes;
import dete.org.Entity.Entities.Enemy;
import java.util.concurrent.ThreadLocalRandom;

public record ConfigureEnemy() {

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

    public static void enemyInitializer(Enemy enemy) {
        Classes[] classes = Classes.values();

        int randomClassIndex = ThreadLocalRandom.current().nextInt(0, classes.length);
        Classes selectedClass = classes[randomClassIndex];

        enemy.clearEffect();
        enemy.setClasses(selectedClass);

        switch (selectedClass) {
            case WIZARD   -> setAttributes(enemy, 2220, 40, 2.5);
            case WITCH    -> setAttributes(enemy, 2220, 40, 4);
            case WARRIOR  -> setAttributes(enemy, 1820, 55, 3);
            case BERSEKER -> setAttributes(enemy, 1720, 36.7, 6);
            default       -> setAttributes(enemy, 1500, 33.4, 5); // HUMAN
        }
    }

    private static void setAttributes(Enemy enemy, double life, double damage, double multiplier) {
        enemy.setLife(life);
        enemy.setDamage(damage);
        enemy.setMultiplied(multiplier);
    }

}