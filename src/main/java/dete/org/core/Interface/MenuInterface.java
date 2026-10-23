package dete.org.core.Interface;

import dete.org.core.Entity.Entities.Enemy;
import dete.org.core.Entity.Entities.Player;
import dete.org.core.Controller.Effect;
import dete.org.core.Inventory.Inventory;
import dete.org.core.Inventory.Item;
import dete.org.core.Inventory.ItemProperties;

import java.util.List;
import java.util.Scanner;

import static java.lang.IO.print;
import static java.lang.IO.println;

public class MenuInterface {
    public final static long timerGeneral = 450;

    public static void menu() {
        long timer = 500;

        delay(timer);
        println("\n---- Jogo ----\n");
        delay(timer);
        println("1 - Start");
        delay(timer);
        println("2 - Perfil");
        delay(timer);
        println("3 - Escolha uma Classe");
        delay(timer);
        println("4 - Sair");
    }

    public static void profile(Player player) {
        print(player);
    }

    public static void startCombateMessage(Player player, Enemy enemy) {
        clearConsole();
        println("\n⚔️ INÍCIO DO COMBATE: " + player.getClasses().getName()
                + " VS " + enemy.getName() + "(" + enemy.getClasses().getName() + ")" + " ⚔️");
        delay(1500);
    }

    public static void roundHeaderMessage(int rounds, Player player, Enemy enemy) {
        println("\n================ RODADA " + rounds + " ================");
        println("Vida do Player: " + (int) player.getLife().getValue() + " | Vida de " + enemy.getName() + ": " + (int) enemy.getLife().getValue());
        delay(timerGeneral);
    }

    public static void helpActionPlayer(Player player) {
        long timer = 300;

        println("\n--- Seu Turno ---");
        delay(timer);
        println("1 - Atacar");
        delay(timer);
        println("2 - Defender (-15% de dano do próximo ataque)");
        delay(timer);
        println("3 - Usar item");
        delay(timer);
        print("Escolha sua ação: ");
    }

    public static void enemyTurnHeaderMessage() {
        println("\n--- Turno do Inimigo ---");
    }

    public static void playerDefenseMessage(Player player) {
        println("🛡️ Você assumiu uma " + player.getEffect());
        delay(timerGeneral);
    }

    public static void enemyDefenseMessage(Enemy enemy) {
        println("🛡️ O " + enemy.getName() + " assumiu uma " + enemy.getEffect());
        delay(timerGeneral);
    }

    public static void entityRegenMessage(String entity) {
        println("✨ " + entity + " regenerou vida!");
        delay(timerGeneral);
    }

    public static void enemyRegenMessage(Enemy enemy) {
        println("✨ O " + enemy.getName() + " regenerou vida!");
        delay(timerGeneral);
    }

    public static void invalidChoiceMessage() {
        println("Opção inválida! Você hesitou e perdeu sua ação.");
        delay(timerGeneral);
    }

    public static void combateDividerMessage() {
        println("\n==========================================");
        delay(timerGeneral);
    }

    public static void winMessage(Enemy loser, int rounds) {
        println("🎉 VITÓRIA! Você derrotou " + loser.getName() + " em " + rounds + " rodadas!");
        delay(timerGeneral);
    }

    public static void defeatMessage(Enemy enemy) {
        println("💀 DERROTA! Você foi derrotado por " + enemy.getName() + "...");
        delay(timerGeneral);
    }

    // --- Mensagens de Combate ---

    public static void defenseAbsorbedMessage(Effect effect) {
        println("🛡️ A " + effect.name() + " absorveu parte do impacto!");
        delay(timerGeneral);
    }

    public static void damageDealtMessage(String attackerName, double damage) {
        println(attackerName + " causou " + (int) damage + " de dano!");
        delay(timerGeneral);
    }

    public static void multiplierActivatedMessage(String entityName) {
        println("🔥 MULTIPLICADOR ATIVADO PARA " + entityName.toUpperCase() + "!");
        delay(timerGeneral);
    }

    public static void regenOnCooldownMessage(int turnsLeft) {
        println("⏳ Regeneração em tempo de recarga! Aguarde mais " + turnsLeft + " rodada(s).");
        delay(timerGeneral);
    }

    public static void regenValueMessage(double value) {
        println("- Quantidade de vida recuperada: " + (int) value);
        delay(timerGeneral);
    }

    // --- Inventário ---

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
        delay(timerGeneral);
    }

    public static void noItemsToUseMessage() {
        println("Você não tem itens para usar.");
        delay(timerGeneral);
    }

    public static void itemUsedMessage(Item item) {
        println("🧪 Você usou " + item.getName() + "!");
        delay(timerGeneral);
    }

    /**
     * Lê uma opção numérica. Texto inválido devolve -1, sem deixar lixo no buffer do Scanner.
     */
    public static int readChoice(Scanner scan) {
        int choice = -1;
        if (scan.hasNextInt()) {
            choice = scan.nextInt();
        } else {
            scan.next(); // descarta o texto inválido
        }
        scan.nextLine(); // consome o resto da linha
        return choice;
    }

    public static void clearConsole() {
        try {
            // 1. Sequência ANSI para limpar terminal real / emulado
            print("\033[H\033[2J");
            System.out.flush();

            // 2. Form Feed ('\f')
            print("\014");
            System.out.flush();

            // 3. Comando de sistema caso seja Windows
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) {
                println("");
            }
        }
    }


    public static void pause(Scanner scan) {
        println("\nPróxima rodada...");
        println("- Pressione ENTER para continuar -");
        scan.nextLine();
    }

    public static void delay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}