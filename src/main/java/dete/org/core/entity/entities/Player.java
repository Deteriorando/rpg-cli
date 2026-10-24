package dete.org.core.entity.entities;

import dete.org.core.entity.ClassStats;
import dete.org.core.entity.Classes;
import dete.org.core.entity.Entity;
import dete.org.core.inventory.Inventory;
import dete.org.core.inventory.Item;
import dete.org.core.inventory.ItemProperties;

import java.util.List;
import java.util.Objects;

public class Player extends Entity {

    private static final int INVENTORY_SLOTS = 8;

    // Privado e final: ninguém de fora recebe o Inventory, só usa os métodos do Player.
    private final Inventory inventory = new Inventory(INVENTORY_SLOTS);

    public Player(Classes type) {
        this(Objects.requireNonNull(type, "O tipo da Classe não pode estar vazio!").getPlayerStats(), type);
    }

    private Player(ClassStats stats, Classes type) {
        super(stats.life(), stats.damage(), type, stats.multiplier());
    }

    @Override
    public void takeDamage(Enemy target) {
        life.setValue(life.getValue() - target.getDamage().getValue());
    }

    @Override
    public String toString() {
        return "- Atributos do Player -\n" + life + damage + classes + multiplier;
    }

    /** Troca de classe e passa a usar os atributos dela. */
    public void changeClass(Classes type) {
        setClasses(type); // lança NullPointerException se for nula
        applyStats(type.getPlayerStats());
    }

    /** Volta aos atributos da classe atual. Usado no fim de cada combate. */
    public void resetToClassStats() {
        clearEffect();
        resetCooldowns();
        applyStats(getClasses().getPlayerStats());
    }

    private void applyStats(ClassStats stats) {
        setLife(stats.life());
        setDamage(stats.damage());
        setMultiplied(stats.multiplier());
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