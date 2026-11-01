package dete.org.view.terminal;

import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;

import static dete.org.view.terminal.config.ConfigUI.TIMER_GENERAL;
import static java.lang.IO.print;
import static java.lang.IO.println;

public class MenuIU {

    public static void menu() {
        long timer = 500;

        TerminalUI.delay(timer);
        println("\n---- Jogo ----\n");
        TerminalUI.delay(timer);
        println("1 - Start");
        TerminalUI.delay(timer);
        println("2 - Perfil");
        TerminalUI.delay(timer);
        println("3 - Escolha uma Classe");
        TerminalUI.delay(timer);
        println("4 - Sair");
    }

    public static void profile(Player player) {
        print(player);
    }

    public static void startCombateMessage(Player player, Enemy enemy) {
        TerminalUI.clearConsole();
        println("\n⚔️ INÍCIO DO COMBATE: "+ "(" + player.getClasses().getName() + ") - " + player.getName()
                + " VS "
                + enemy.getName() + " - (" + enemy.getClasses().getName() + ")" + " ⚔️");
        TerminalUI.delay(1500);
    }

    public static void roundHeaderMessage(int rounds, Player player, Enemy enemy) {
        println("\n================ RODADA " + rounds + " ================");
        println("Vida do Player: " + (int) player.getLife().getValue() + " | Vida de " + enemy.getName() + ": " + (int) enemy.getLife().getValue());
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void helpActionPlayer(Player player) {
        long timer = 300;

        println("\n--- Seu Turno ---");
        TerminalUI.delay(timer);
        println("1 - Atacar");
        TerminalUI.delay(timer);
        println("2 - Defender (-15% de dano do próximo ataque)");
        TerminalUI.delay(timer);
        println("3 - Usar item");
        TerminalUI.delay(timer);
        print("Escolha sua ação: ");
    }

    public static void enemyTurnHeaderMessage() {
        long timer = 300;
        println("\n--- Turno do Inimigo ---");
        TerminalUI.delay(timer);
    }

    public static void invalidChoiceMessage() {
        println("Opção inválida! Você hesitou e perdeu sua ação.");
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void combateDividerMessage() {
        println("\n==========================================");
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void winMessage(Enemy loser, int rounds) {
        println("🎉 VITÓRIA! Você derrotou " + loser.getName() + " em " + rounds + " rodadas!");
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void defeatMessage(Enemy enemy) {
        println("💀 DERROTA! Você foi derrotado por " + enemy.getName() + "...");
        TerminalUI.delay(TIMER_GENERAL);
    }

}