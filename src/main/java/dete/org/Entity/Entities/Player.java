package dete.org.Entity.Entities;

import dete.org.Entity.Classes;
import dete.org.Entity.Entity;
import dete.org.Systems.Inventory.Inventory;
import dete.org.Systems.Inventory.Item;
import dete.org.Systems.Inventory.ItemProperties;

import java.util.List;

public class Player extends Entity {

    private static final int INVENTORY_SLOTS = 20;
    private final Inventory inventory = new Inventory(INVENTORY_SLOTS);

    public Player(double life, double damage, Classes type, double multiplied) {
        super(life, damage, type, multiplied);
    }

    @Override
    public void takeDamage(Enemy target) {
        life.setValue(life.getValue() - target.getDamage().getValue());
    }

    @Override
    public String toString() {
        return "- Atributos do Player -\n" + life + damage + classes + multiplier;
    }

    public boolean addItemToInventory(Item item) {
        return addItemToInventory(item, 1);
    }

    /** Pega o item. Retorna false, sem alterar nada, se não couber tudo. */
    public boolean addItemToInventory(Item item, int quantity) {
        if (!inventory.canAdd(item, quantity)) {
            return false;
        }
        inventory.addItem(item, quantity);
        return true;
    }

    /** Larga o item. Retorna false se ele não puder ser largado ou o jogador não tiver a quantidade. */
    public boolean dropItem(Item item, int quantity) {
        if (item.hasProperty(ItemProperties.NON_DROPPABLE)) {
            return false;
        }
        if (!inventory.contains(item, quantity)) {
            return false;
        }
        inventory.removeItem(item, quantity);
        return true;
    }

    /** Usa o item. Itens CONSUMABLE perdem uma unidade depois do uso. */
    public boolean useItem(Item item) {
        if (!inventory.contains(item, 1)) {
            return false;
        }
        item.use(this);
        if (item.hasProperty(ItemProperties.CONSUMABLE)) {
            inventory.removeItem(item, 1);
        }
        return true;
    }

    public boolean hasItem(Item item, int quantity) {
        return inventory.contains(item, quantity);
    }

    public void listInventory() {
        inventory.listInventory();
    }

    /** Visão somente leitura dos slots, para o menu numerar e escolher itens. */
    public List<Inventory.InventorySlot> getInventorySlots() {
        return inventory.getSlots();
    }
}