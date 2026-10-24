package dete.org.core.controller;

import dete.org.core.entity.entities.Player;
import dete.org.core.ui.MenuInterface;
import dete.org.core.inventory.Inventory.InventorySlot;
import dete.org.core.inventory.Item;

import java.util.List;
import java.util.Scanner;

/**
 * <p>
 *     Controla o uso de itens do inventário durante o combate (entrada do jogador
 *     e chamadas ao Player). Os textos ficam todos no MenuInterface.
 * </p>
 */
public class InventoryController {

    /**
     * Devolve true se um item foi usado (o turno é gasto) e false se o jogador
     * desistiu ou escolheu algo inválido (o turno não é gasto).
     */
    public static boolean useItemInCombat(Player player, Scanner scan) {
        List<InventorySlot> slots = player.getInventorySlots();

        if (slots.isEmpty()) {
            MenuInterface.noItemsToUseMessage();
            return false;
        }

        MenuInterface.inventoryScreen(player);
        MenuInterface.askItemNumberMessage();
        int number = MenuInterface.readChoice(scan);

        if (number == 0) {
            return false;
        }
        if (number < 1 || number > slots.size()) {
            MenuInterface.invalidItemMessage();
            return false;
        }

        Item item = slots.get(number - 1).getItem();
        if (!player.useItem(item)) {
            MenuInterface.invalidItemMessage();
            return false;
        }

        MenuInterface.itemUsedMessage(item);
        return true;
    }
}