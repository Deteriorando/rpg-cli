package dete.org.core.input;

public interface PlayerInput {
    int getCodeInt();
    void next();

    /** Indica que o fluxo de entrada chegou ao fim (EOF em entrada não interativa). */
    default boolean isEndOfInput() {
        return false;
    }
}
