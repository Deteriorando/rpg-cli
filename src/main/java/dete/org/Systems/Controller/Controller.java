package dete.org.Systems.Controller;

import dete.org.Entity.Entities.Player;
import dete.org.Entity.Entities.Enemy;
import dete.org.Systems.Game.ConfigurePlayer;
import dete.org.Systems.Interface.MenuInterface;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Controller {
    protected int rounds;
    protected double regenerationValue = 30;

    public Controller() {
        this.rounds = 1;
    }

    public static void start(Player player, Enemy enemy, Scanner scan) {
        Controller combat = new Controller();
        MenuInterface.startCombateMessage(player, enemy);

        while (player.isAlive() && enemy.isAlive()) {
            MenuInterface.roundHeaderMessage(combat.rounds, player, enemy);

            combat.executePlayerAction(player, enemy, scan);

            if (!enemy.isAlive()) {
                break;
            }

            combat.executeEnemyAction(enemy, player);

            // Atualiza os cooldowns no fim do turno completo
            if (!enemy.canRegenerate()) enemy.updateCooldowns();

            combat.rounds++;
            MenuInterface.pause(scan);
        }

        MenuInterface.combateDividerMessage();
        if (player.isAlive()) {
            MenuInterface.winMessage(enemy, combat.rounds);
        } else {
            MenuInterface.defeatMessage(enemy);
        }

        ConfigurePlayer.reset(player);
    }

    private void executePlayerAction(Player player, Enemy enemy, Scanner scan) {
        boolean validAction = false;

        do {
            MenuInterface.helpActionPlayer(player);

            int choice = 1;
            if (scan.hasNextInt()) {
                choice = scan.nextInt();
            }
            scan.nextLine(); // Limpa o buffer da quebra de linha após o int

            MenuInterface.clearConsole();

            switch (choice) {
                case 1 -> {
                    MenuInterface.delay(100);
                    player.attack(enemy);
                    validAction = true;
                }
                case 2 -> {
                    player.applyEffect(new Effect("Postura Defensiva", 0.85));
                    MenuInterface.delay(100);
                    MenuInterface.playerDefenseMessage(player);
                    validAction = true;
                }
                case 3 -> {
                    // Só gasta o turno se o item foi realmente usado;
                    // se o jogador voltar atrás, escolhe outra ação.
                    if (InventoryController.useItemInCombat(player, scan)) {
                        validAction = true;
                    }
                }
                default -> {
                    MenuInterface.invalidChoiceMessage();
                    validAction = true; // Consome o turno por opção inválida
                }
            }
        } while (!validAction);
    }

    private void executeEnemyAction(Enemy enemy, Player player) {
        int randomAction = ThreadLocalRandom.current().nextInt(0, 5 + 1); // 0 a 5
        long timer = 300;

        MenuInterface.enemyTurnHeaderMessage();
        switch (randomAction) {
            case 2, 5 -> {
                enemy.applyEffect(new Effect("Postura Defensiva!", 0.85));
                MenuInterface.delay(timer);
                MenuInterface.enemyDefenseMessage(enemy);
            }
            case 3 -> {
                if (enemy.canRegenerate()) {
                    enemy.regeneration(regenerationValue);
                    enemy.triggerRegenCooldown();
                    MenuInterface.delay(timer);
                    MenuInterface.enemyRegenMessage(enemy);
                } else {
                    // Se estiver em cooldown, a IA ataca como fallback
                    MenuInterface.delay(timer);
                    enemy.attack(player);
                }
            }
            default -> {
                MenuInterface.delay(timer);
                enemy.attack(player);
            }
        }
    }
}