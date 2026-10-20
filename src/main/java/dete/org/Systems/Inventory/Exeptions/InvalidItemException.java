package dete.org.Systems.Inventory.Exeptions;

public class InvalidItemException extends RuntimeException {
    public InvalidItemException(String message) {
        super(message);
    }
}
