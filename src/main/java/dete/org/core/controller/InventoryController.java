package dete.org.core.controller;

import dete.org.core.entity.entities.Player;
import dete.org.core.input.InputScanner;
import dete.org.core.inventory.Inventory;
import dete.org.view.terminal.InventoryUI;
import dete.org.view.terminal.MenuIU;
import dete.org.core.inventory.Inventory.InventorySlot;
import dete.org.core.inventory.Item;

import java.util.List;

/**
 * <p>
 *     Controla o uso de itens do inventário durante o combate (entrada do jogador
 *     e chamadas ao Player). Os textos ficam todos no MenuInterface.
 * </p>
 */
public class InventoryController {

    private final InputScanner scanner;

    public InventoryController(InputScanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Devolve true se um item foi usado (o turno é gasto) e false se o jogador
     * desistiu ou escolheu algo inválido (o turno não é gasto).
     */
    public boolean useItemInCombat(Player player) {
        List<InventorySlot> slots = player.getInventorySlots();

        if (slots.isEmpty()) {
            InventoryUI.noItemsToUseMessage();
            return false;
        }

        InventoryUI.inventoryScreen(player);
        InventoryUI.askItemNumberMessage();
        int number = scanner.getCodeInt();

        if (number == 0) {
            return false;
        }
        if (number < 1 || number > slots.size()) {
            InventoryUI.invalidItemMessage();
            return false;
        }

        Item item = slots.get(number - 1).getItem();
        if (!player.useItem(item)) {
            InventoryUI.invalidItemMessage();
            return false;
        }

        InventoryUI.itemUsedMessage(item);
        return true;
    }
}