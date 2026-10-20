package dete.org.Systems.Inventory;

import dete.org.Systems.Inventory.Exeptions.InventoryFullException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Inventory {

    public static class InventorySlot {
        private final Item item;
        private int quantity;

        public InventorySlot(Item item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }

        public Item getItem() { return item; }
        public int getQuantity() { return quantity; }
        public void addQuantity(int amount) { this.quantity += amount; }

        public boolean isFull() {
            return this.quantity >= item.getMaxStackSize();
        }

        public int getRemainingSpace() {
            return item.getMaxStackSize() - this.quantity;
        }
    }

    private final int slotLimit;
    private final List<InventorySlot> slots;

    public Inventory(int slotLimit) {
        this.slotLimit = slotLimit;
        this.slots = new ArrayList<>();
    }

    public void addItem(Item item, int quantity) {
        validateItem(item, quantity);

        int remainingQuantity = quantity;

        if (item.hasProperty(ItemProperties.STACKABLE)) {
            remainingQuantity = fillExistingSlots(item, remainingQuantity);
            if (remainingQuantity == 0) return;
        }

        fillNewSlots(item, remainingQuantity);
    }

    private void validateItem(Item item, int quantity) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add a null item to the inventory.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
    }

    private int fillExistingSlots(Item item, int quantity) {
        for (InventorySlot slot : slots) {
            if (isSameItem(slot, item) && !slot.isFull()) {
                int spaceInSlot = slot.getRemainingSpace();

                long total = (long) slot.getQuantity() + quantity;
                if (total > Integer.MAX_VALUE) {
                    throw new ArithmeticException("Stack quantity overflow!");
                }

                if (quantity <= spaceInSlot) {
                    slot.addQuantity(quantity);
                    return 0;
                } else {
                    slot.addQuantity(spaceInSlot);
                    quantity -= spaceInSlot;
                }
            }
        }
        return quantity;
    }

    private void fillNewSlots(Item item, int quantity) {
        while (quantity > 0) {
            if (hasReachedSlotLimit()) {
                throw new InventoryFullException("Inventory is full! Could not add all items.");
            }

            int amountToPut = calculateAmountForNewSlot(item, quantity);
            slots.add(new InventorySlot(item, amountToPut));
            quantity -= amountToPut;
        }
    }

    private boolean isSameItem(InventorySlot slot, Item item) {
        return slot.getItem().getId().equals(item.getId());
    }

    private boolean hasReachedSlotLimit() {
        return slots.size() >= slotLimit;
    }

    private int calculateAmountForNewSlot(Item item, int quantity) {
        if (item.hasProperty(ItemProperties.STACKABLE)) {
            return Math.min(quantity, item.getMaxStackSize());
        }
        return 1;
    }

    public void listInventory() {
        System.out.println("=== INVENTORY ===");
        if (slots.isEmpty()) {
            System.out.println("Your inventory is empty.");
            return;
        }

        int index = 1;
        for (InventorySlot slot : slots) {
            Item item = slot.getItem();
            int qty = slot.getQuantity();

            if (item.hasProperty(ItemProperties.STACKABLE)) {
                System.out.println(index + ". " + item.getName() + " (x" + qty + "/" + item.getMaxStackSize() + ") [" + item.getType().name() + "]");
            } else {
                System.out.println(index + ". " + item.getName() + " [" + item.getType().name() + "]");
            }
            index++;
        }
    }

    public List<InventorySlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }
}