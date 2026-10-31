package dete.org.core.input;

public enum UserAction {
    ATTACK(1, "Atacar"),
    DEFENCE(2, "Defender"),
    INVENTORY(3, "Inventário"),
    INVALID(-1, "Invalido");

    private final String name;
    private final int code;

    UserAction(int code, String name) {
        this.name = name;
        this.code = code;
    }

    public String getName() { return this.name; }
    public int getCode() { return this.code; }

    /**
     * Retorna a ação correspondente ao código informado.
     * <br>
     * Retorna {@link #INVALID} caso nenhuma ação corresponda ao código.
     *
     * @param code código que representa a ação
     * @return ação correspondente ao código ou {@link #INVALID} caso não seja encontrada
     */
    public static UserAction getByCode(int code) {
        for (UserAction userAction : UserAction.values()) {
            if (userAction.getCode() == code) {
                return userAction;
            }
        }

        return INVALID;
    }
}
