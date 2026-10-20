package dete.org.Systems.Inventory.Exeptions;

public class InventoryFullException extends RuntimeException {
    public InventoryFullException(String message) {
        super(message);
    }
}
