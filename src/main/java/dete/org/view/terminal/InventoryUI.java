package dete.org.view.terminal;

import dete.org.core.entity.entities.Player;
import dete.org.core.inventory.Inventory;
import dete.org.core.inventory.Item;
import dete.org.core.inventory.ItemProperties;

import java.util.List;

import static dete.org.view.terminal.config.ConfigUI.TIMER_GENERAL;
import static java.lang.IO.print;
import static java.lang.IO.println;

public class InventoryUI {


    public static void inventoryScreen(Player player) {
        println("\n=== INVENTÁRIO ===");

        List<Inventory.InventorySlot> slots = player.getInventorySlots();
        if (slots.isEmpty()) {
            println("Seu inventário está vazio.");
            return;
        }

        for (int i = 0; i < slots.size(); i++) {
            Inventory.InventorySlot slot = slots.get(i);
            Item item = slot.getItem();

            String stack = item.hasProperty(ItemProperties.STACKABLE)
                    ? " (x" + slot.getQuantity() + "/" + item.getMaxStackSize() + ")"
                    : "";

            println((i + 1) + " - " + item.getName() + stack + " [" + item.getType().name() + "]");
        }
    }

    public static void askItemNumberMessage() {
        print("Número do item (0 para voltar): ");
    }

    public static void invalidItemMessage() {
        println("Esse item não existe!");
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void noItemsToUseMessage() {
        println("Você não tem itens para usar.");
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void itemUsedMessage(Item item) {
        println("🧪 Você usou " + item.getName() + "!");
        TerminalUI.delay(TIMER_GENERAL);
    }
}
