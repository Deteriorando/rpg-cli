package dete.org.Systems.Inventory.Items;

import dete.org.Entity.Entity;
import dete.org.Systems.Inventory.Item;
import dete.org.Systems.Inventory.ItemProperties;
import dete.org.Systems.Inventory.ItemType;

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
        user.setLife(user.getLife().getValue() + healAmount);
    }
}
