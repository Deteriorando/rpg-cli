package dete.org.view.terminal.presenters;

import dete.org.core.entity.entities.Player;
import dete.org.core.inventory.Item;
import dete.org.core.presenters.InventoryPresenter;
import dete.org.view.terminal.InventoryUI;
import dete.org.view.terminal.TerminalUI;

/**
 * Implementação terminal do {@link InventoryPresenter}.
 *
 * <p>Responsável por apresentar informações e mensagens relacionadas
 * ao inventário utilizando a interface de terminal.</p>
 */
public class TerminalInventoryPresenter implements InventoryPresenter {

    @Override
    public void presenterAskItemNumber() {
        InventoryUI.askItemNumberMessage();
    }

    @Override
    public void presenterInventoryScreen(Player player) {
        InventoryUI.inventoryScreen(player);
    }

    @Override
    public void presenterInvalidItem() {
        InventoryUI.invalidItemMessage();
    }

    @Override
    public void presenterItemUsed(Item item) {
        InventoryUI.itemUsedMessage(item);
    }

    @Override
    public void presenterNoItemsToUse() {
        InventoryUI.noItemsToUseMessage();
    }

    @Override
    public void presenterNotCanDropItem() {
        InventoryUI.noCanDropItemMessage();
    }
}
