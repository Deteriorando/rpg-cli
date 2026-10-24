package dete.org.core.inventory;

import dete.org.core.inventory.exceptions.InventoryFullException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.IO.print;

public class Inventory {

    public static class InventorySlot {
        private final Item item;
        private int quantity;

        private InventorySlot(Item item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }

        public Item getItem() { return item; }
        public int getQuantity() { return quantity; }
    }

    private final int slotLimit;
    private final List<InventorySlot> slots = new ArrayList<>();

    public Inventory(int slotLimit) {
        this.slotLimit = slotLimit;
    }

    public void addItem(Item item, int quantity) {
        if (!canAdd(item, quantity)) { // canAdd também valida os argumentos
            throw new InventoryFullException("Inventory is full! Could not add all items.");
        }

        int remaining = quantity;

        for (InventorySlot slot : slots) {
            if (remaining > 0 && isSameItem(slot, item)) {
                int toAdd = Math.min(remaining, spaceLeft(slot));
                slot.quantity += toAdd;
                remaining -= toAdd;
            }
        }

        while (remaining > 0) {
            int amount = Math.min(remaining, stackLimit(item));
            slots.add(new InventorySlot(item, amount));
            remaining -= amount;
        }
    }

    public boolean canAdd(Item item, int quantity) {
        validateItem(item, quantity);

        long remaining = quantity;
        for (InventorySlot slot : slots) {
            if (isSameItem(slot, item)) {
                remaining -= spaceLeft(slot);
            }
        }

        long limit = stackLimit(item);
        long newSlots = remaining <= 0 ? 0 : (remaining + limit - 1) / limit;
        return slots.size() + newSlots <= slotLimit;
    }

    public void removeItem(Item item, int quantity) {
        validateItem(item, quantity);

        if (countOf(item) < quantity) {
            throw new IllegalStateException("Not enough items to remove.");
        }

        if (item.hasProperty(ItemProperties.NON_DROPPABLE)) {
            print("Você não pode remover este item.");
            return;
        }

        int remaining = quantity;

        for (int i = slots.size() - 1; i >= 0 && remaining > 0; i--) {
            InventorySlot slot = slots.get(i);
            if (!isSameItem(slot, item)) continue;

            int toRemove = Math.min(remaining, slot.quantity);
            slot.quantity -= toRemove;
            remaining -= toRemove;

            if (slot.quantity == 0) {
                slots.remove(i);
            }
        }
    }

    public long countOf(Item item) {
        long total = 0;
        for (InventorySlot slot : slots) {
            if (isSameItem(slot, item)) {
                total += slot.quantity;
            }
        }
        return total;
    }

    private void validateItem(Item item, int quantity) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
    }

    public void listInventory() {
        System.out.println("=== INVENTORY ===");
        if (slots.isEmpty()) {
            System.out.println("Your inventory is empty.");
            return;
        }

        for (int i = 0; i < slots.size(); i++) {
            InventorySlot slot = slots.get(i);
            Item item = slot.getItem();
            System.out.println((i + 1) + ". " + item.getName() + " [" + item.getType().name() + "]");
        }
    }

    public boolean contains(Item item, int quantity) {
        return countOf(item) >= quantity;
    }

    private boolean isStackable(Item item) {
        return item.hasProperty(ItemProperties.STACKABLE);
    }

    private int stackLimit(Item item) {
        return isStackable(item) ? item.getMaxStackSize() : 1;
    }

    private int spaceLeft(InventorySlot slot) {
        return stackLimit(slot.item) - slot.quantity;
    }

    private boolean isSameItem(InventorySlot slot, Item item) {
        return slot.item.getId().equals(item.getId());
    }

    public List<InventorySlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }
}