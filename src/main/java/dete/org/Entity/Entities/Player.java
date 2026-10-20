package dete.org.Entity.Entities;

import dete.org.Entity.Classes;
import dete.org.Entity.Entity;
import dete.org.Systems.Inventory.Inventory;
import dete.org.Systems.Inventory.Item;

public class Player extends Entity {

    protected Inventory inventory;

    public Player(double life, double damage, Classes type, double multiplied) {
        super(life, damage, type, multiplied);
        this.inventory = inventory;
    }

    @Override
    public void takeDamage(Enemy target) {
        life.setValue(life.getValue() - target.getDamage().getValue());
    }

    @Override
    public String toString() {
        return "- Atributos do Player -\n" + life + damage + classes + multiplier;
    }

    public void addItemToInventory(Item item) {
    }
}
