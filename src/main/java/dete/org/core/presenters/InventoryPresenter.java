package dete.org.core.presenters;

import dete.org.core.entity.entities.Player;
import dete.org.core.inventory.Item;

/**
 * Define as operações de apresentação relacionadas ao inventário.
 *
 * <p>Representa a interface entre a lógica do jogo e a implementação
 * responsável por exibir informações do inventário.</p>
 */
public interface InventoryPresenter {
    void presenterAskItemNumber();
    void presenterInventoryScreen(Player player);
    void presenterInvalidItem();
    void presenterItemUsed(Item item);
    void presenterNoItemsToUse();
    void presenterNotCanDropItem();
}
