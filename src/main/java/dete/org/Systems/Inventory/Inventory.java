package dete.org.Systems.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inventory {
    private final int slotLimit;
    private final List<Item> items;
    private final Map<String, Integer> quantities; // Controls stacking by item ID

    public Inventory(int slotLimit) {
        this.slotLimit = slotLimit;
        this.items = new ArrayList<>();
        this.quantities = new HashMap<>();
    }

    public boolean addItem(Item item, int quantity) {
        if (item.hasProperty(ItemProperties.STACKABLE)) {
            if (quantities.containsKey(item.getId())) {
                quantities.put(item.getId(), quantities.get(item.getId()) + quantity);
                return true;
            }
        }

        if (items.size() >= slotLimit) {
            System.out.println("Inventário cheio!");
            return false;
        }

        items.add(item);
        quantities.put(item.getId(), quantity);
        return true;
    }

    public void listInventory() {
        System.out.println("=== INVENTORY ===");
        int index = 1;
        // Laço foreach implementado aqui
        for (Item item : items) {
            int qty = quantities.getOrDefault(item.getId(), 1);
            System.out.println(index + ". " + item.getName() + " (x" + qty + ") [" + item.getType().name() + "]");
            index++;
        }
    }
}