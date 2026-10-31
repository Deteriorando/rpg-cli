package dete.org.view.terminal;

import dete.org.core.controller.Effect;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;

import static dete.org.view.terminal.Config.ConfigUI.TIMER_GENERAL;
import static java.lang.IO.println;

public class CombatUI {

    public static void startCombateMessage(Player player, Enemy enemy) {
        TerminalUI.clearConsole();
        println("\n⚔️ INÍCIO DO COMBATE: " + player.getClasses().getName()
                + " VS " + enemy.getName() + "(" + enemy.getClasses().getName() + ")" + " ⚔️");
        TerminalUI.delay(1500);
    }

    public static void playerDefenseMessage(Player player) {
        println("🛡️ Você assumiu uma " + player.getEffect());
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void enemyDefenseMessage(Enemy enemy) {
        println("🛡️ O " + enemy.getName() + " assumiu uma " + enemy.getEffect());
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void damageDealtMessage(String attackerName, double damage) {
        println(attackerName + " causou " + (int) damage + " de dano!");
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void multiplierActivatedMessage(String entityName) {
        println("🔥 MULTIPLICADOR ATIVADO PARA " + entityName.toUpperCase() + "!");
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void regenOnCooldownMessage(int turnsLeft) {
        println("⏳ Regeneração em tempo de recarga! Aguarde mais " + turnsLeft + " rodada(s).");
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void regenValueMessage(double value) {
        println("- Quantidade de vida recuperada: " + (int) value);
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void defenseAbsorbedMessage(Effect effect) {
        println("🛡️ A " + effect.name() + " absorveu parte do impacto!");
        TerminalUI.delay(TIMER_GENERAL);
    }

    public static void enemyRegenMessage(Enemy enemy) {
        println("✨ O " + enemy.getName() + " regenerou vida!");
        TerminalUI.delay(TIMER_GENERAL);
    }
}
