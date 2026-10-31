package dete.org.core.controller;

/**
 * @param valueEffect Ex: 0.85 para 15% de redução
 */
public record Effect(String name, double valueEffect) {

    public double apply(double baseValue) {
        return baseValue * valueEffect;
    }

    @Override
    public String toString() {
        return name;
    }
}