package dete.org.core.Inventory.Exeptions;

public class InventoryFullException extends RuntimeException {
    public InventoryFullException(String message) {
        super(message);
    }
}
