package dete.org.core.entity.atributes;

/**
 * <p>
 *     Uma Classe para gerir atributo para cada entidade existente, independente
 *     do tipo da entidade. Ela gerencia os seus proprios valores.
 * </p>
 * */

public class Damage {
    protected double value;
    protected final double minValue = 10, maxValue = 120;

    public Damage(double value) {
        if (value < minValue || value > maxValue) {
            throw new IllegalArgumentException(
                    String.format(
                            "\nO Dano deve estar entre %.0f e %.0f.\n- Valor informado: %.0f",
                            minValue, maxValue, value
                    )
            );
        }
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        if (value < minValue) throw new IllegalArgumentException("O valor não pode ser menor que " + minValue);
        this.value = Math.min(value, maxValue);
    }

    @Override
    public String toString() {
        return "\n- Dano: " + value;
    }
}
