package dete.org.core.presenters;

import dete.org.core.entity.entities.Player;
import dete.org.core.inventory.Item;

public interface InventoryPresenter {
    void presenterAskItemNumber();
    void presenterInventoryScreen(Player player);
    void presenterInvalidItem();
    void presenterItemUsed(Item item);
    void presenterNoItemsToUse();
}
