package dete.org.Systems.Inventory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Properties are using a Set collection so they are not duplicated
 */
public abstract class Item {
    private final String id;
    private final String name;
    private final String description;
    private final ItemType type;
    private final Set<ItemProperties> properties;
    private final int maxStackSize;

    public Item(String id, String name, String description, ItemType type, int maxStackSize) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.properties = EnumSet.noneOf(ItemProperties.class);
        this.maxStackSize = maxStackSize;
    }

    public void addProperty(ItemProperties property) {
        this.properties.add(property);
    }

    public boolean hasProperty(ItemProperties property) {
        return this.properties.contains(property);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ItemType getType() { return type; }

    // Returns an unmodifiable view to protect the internal Set from direct external modifications
    public Set<ItemProperties> getProperties() {
        return Collections.unmodifiableSet(properties);
    }
    public int getMaxStackSize() { return maxStackSize; }

    // Abstract method for each item to define what it does when used in the game
    public abstract void use();
}