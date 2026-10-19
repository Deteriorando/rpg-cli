package dete.org.Systems.Combat;

import dete.org.Entity.Entity;

/**
 * @param multiplier Ex: 0.85 para 15% de redução
 */
public record Effect(String name, double multiplier) {

    public double apply(double baseValue) {
        return baseValue * multiplier;
    }

    @Override
    public String toString() {
        return name;
    }
}