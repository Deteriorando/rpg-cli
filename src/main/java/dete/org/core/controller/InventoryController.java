package dete.org.core.controller;

import dete.org.core.entity.entities.Player;
import dete.org.core.input.InputScanner;
import dete.org.core.presenters.InventoryPresenter;
import dete.org.core.inventory.Inventory.InventorySlot;
import dete.org.core.inventory.Item;

import java.util.List;

/**
 * Controla o uso de itens do inventário durante o combate.
 *
 * <p>Responsável por receber a escolha do jogador, validar o item
 * selecionado e solicitar ao jogador a utilização do item.</p>
 */
public class InventoryController {

    private final InputScanner scanner;
    private final InventoryPresenter inventoryPresenter;

    public InventoryController(InputScanner scanner, InventoryPresenter inventoryPresenter) {
        this.scanner = scanner;
        this.inventoryPresenter = inventoryPresenter;
    }

    /**
     * Devolve true se um item foi usado (o turno é gasto) e false se o jogador
     * desistiu ou escolheu algo inválido (o turno não é gasto).
     */
    public boolean useItemInCombat(Player player) {
        List<InventorySlot> slots = player.getInventorySlots();

        if (slots.isEmpty()) {
            inventoryPresenter.presenterNoItemsToUse();
            return false;
        }

        inventoryPresenter.presenterInventoryScreen(player);
        inventoryPresenter.presenterAskItemNumber();
        int number = scanner.getCodeInt();

        if (number == 0) {
            return false;
        }
        if (number < 1 || number > slots.size()) {
            inventoryPresenter.presenterInvalidItem();
            return false;
        }

        Item item = slots.get(number - 1).getItem();
        if (!player.useItem(item)) {
            inventoryPresenter.presenterInvalidItem();
            return false;
        }

        inventoryPresenter.presenterItemUsed(item);
        return true;
    }
}