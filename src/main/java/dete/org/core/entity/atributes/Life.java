package dete.org.core.entity.atributes;

/**
 * <p>
 *     É um atributo que gerencia a vida de entidades, ela gerencia os seus
 *     próprios valores.
 * </p>
 */


public class Life {
    protected double value;
    protected final double minValue = 820, maxValue = 2200;

    public Life(double value){
        if (value < minValue || value > maxValue) {
            throw new IllegalArgumentException(
                    String.format(
                            "\nA vida deve estar entre %.0f e %.0f.\n- Valor informado: %.0f",
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
        if (value < 0) {
            this.value = 0;
            return;
        }
        this.value = Math.min(value, maxValue);
    }

    @Override
    public String toString() {
        return "\n- Vida: " + value;
    }
}
