package dete.org.core.Inventory.Items;

import dete.org.core.Entity.Entity;
import dete.org.core.Inventory.Item;
import dete.org.core.Inventory.ItemProperties;
import dete.org.core.Inventory.ItemType;

public class HealthPotion extends Item {

    private final double healAmount;

    public HealthPotion(double healAmount) {
        super("HEALTH_POTION",
                "Poção de Vida",
                "Recupera " + healAmount + " de vida.",
                ItemType.POTION,
                10, // até 10 poções por slot
                ItemProperties.STACKABLE, ItemProperties.CONSUMABLE);
        this.healAmount = healAmount;
    }

    @Override
    public void use(Entity user) {
        user.regeneration(healAmount);
    }
}
