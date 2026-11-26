package dete.org.view.terminal.presenters;

import dete.org.audio.SoundEffects;
import dete.org.core.entity.entities.Player;
import dete.org.core.inventory.Item;
import dete.org.core.inventory.ItemType;
import dete.org.core.presenters.InventoryPresenter;
import dete.org.view.terminal.InventoryUI;
import dete.org.view.terminal.tui.CombatTuiController;

/**
 * Implementação terminal do {@link InventoryPresenter}.
 *
 * <p>Integra a seleção e uso de itens na TUI de combate quando ativa,
 * preservando a exibição clássica em outros contextos.</p>
 */
public class TerminalInventoryPresenter implements InventoryPresenter {

    @Override
    public void presenterAskItemNumber() {
        if (!CombatTuiController.isActive()) {
            InventoryUI.askItemNumberMessage();
        }
    }

    @Override
    public void presenterInventoryScreen(Player player) {
        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            tui.onInventoryPrompt(player);
        } else {
            InventoryUI.inventoryScreen(player);
        }
    }

    @Override
    public void presenterInvalidItem() {
        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            tui.onInvalidItem();
        } else {
            InventoryUI.invalidItemMessage();
        }
    }

    @Override
    public void presenterItemSelectionCanceled() {
        SoundEffects.play(SoundEffects.Cue.CANCEL);
    }

    @Override
    public void presenterItemUsed(Item item) {
        SoundEffects.play(SoundEffects.Cue.CONFIRM);
        if (item.getType() == ItemType.POTION) {
            SoundEffects.play(SoundEffects.Cue.DRINK_POTION);
        }

        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            tui.onItemUsed(item);
        } else {
            InventoryUI.itemUsedMessage(item);
        }
    }

    @Override
    public void presenterNoItemsToUse() {
        CombatTuiController tui = CombatTuiController.getActive();
        if (tui != null) {
            tui.onNoItemsToUse();
        } else {
            InventoryUI.noItemsToUseMessage();
        }
    }

    @Override
    public void presenterNotCanDropItem() {
        InventoryUI.noCanDropItemMessage();
    }
}
