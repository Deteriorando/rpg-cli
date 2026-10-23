package dete.org.core.Entity.Atributes;

/**
 * <p>
 *     Uma Classe que vai multiplicar os valores de outros atributos para
 *     cada entidade existente. Ela gerencia os seus próprios valores.
 * </p>
 * */

public class Multiplier {
    protected double value;
    protected final double minValue = 1, maxValue = 10;

    public Multiplier(double value) {
        if (value < minValue || value > maxValue) {
            throw new IllegalArgumentException(
                    String.format(
                            "\nO Multiplicador deve estar entre %.0f e %.0f.\n- Valor informado: %.0f",
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
        return "\n- Multiplicador: " + value;
    }
}
