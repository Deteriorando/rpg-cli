package dete.org.Systems.Inventory;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
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

    public Item(String id, String name, String description, ItemType type, int maxStackSize, ItemProperties... property) {
        this.id = id.toUpperCase();
        this.name = name;
        this.description = description;
        this.type = type;
        this.properties = EnumSet.noneOf(ItemProperties.class);

        Objects.requireNonNull(property, "Não pode haver propriedades nulas!");
        this.properties.addAll(Arrays.asList(property));

        validateProperties();
        validateMaxStackSize(maxStackSize);

        this.maxStackSize = maxStackSize;
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

    @Override
    public String toString() {
        return "id: " + id
                + ", name: " + name
                + ", description: " + description
                + ", type: " + type;
    }

    private void validateProperties() {
        if (hasProperty(ItemProperties.CONSUMABLE)
                && hasProperty(ItemProperties.EQUIPPABLE)
                && hasProperty(ItemProperties.KEY_ITEM)) {
            throw new IllegalArgumentException("Você não pode colocar "
                    + ItemProperties.CONSUMABLE + ", "
                    + ItemProperties.EQUIPPABLE + " e "
                    + ItemProperties.KEY_ITEM + " juntas!");
        }

        if (hasProperty(ItemProperties.NON_DROPPABLE) && hasProperty(ItemProperties.TRADABLE)) {
            throw new IllegalArgumentException("A propriedade "
                    + ItemProperties.NON_DROPPABLE + " não pode estar junta com a "
                    + ItemProperties.TRADABLE + ".");
        }
    }

    private void validateMaxStackSize(int maxStackSize) {
        if (hasProperty(ItemProperties.STACKABLE) && maxStackSize < 1) {
            throw new IllegalArgumentException("Um item empilhável não pode ter o limite menor que 1.");
        }
        if (maxStackSize < 0) {
            throw new IllegalArgumentException("O limite de pilha não pode ser negativo.");
        }
    }
}