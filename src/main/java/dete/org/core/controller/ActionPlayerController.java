package dete.org.core.controller;

import dete.org.core.dto.AttackResult;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.InputScanner;
import dete.org.core.input.UserAction;
import dete.org.view.terminal.CombatUI;
import dete.org.view.terminal.MenuIU;

public class ActionPlayerController {

    private final InventoryController inventoryController;

    public ActionPlayerController(InputScanner scanner) {
        inventoryController = new InventoryController(scanner);
    }

    public boolean execute(UserAction action, Player  player, Enemy enemy) {
        switch (action) {
            case ATTACK -> {
                AttackResult result = player.attack(enemy);
                if (result.critical()) CombatUI.multiplierActivatedMessage("Player");
                CombatUI.damageDealtMessage("Player", result.damage());
                return true;
            }
            case DEFENCE -> {
                player.applyEffect(new Effect("Postura Defensiva", 0.85));
                CombatUI.playerDefenseMessage(player);
                return true;
            }
            case INVENTORY -> {
                if(inventoryController.useItemInCombat(player)){
                    return true;
                }
            }
            default -> {
                MenuIU.invalidChoiceMessage();
                return true;
            }
        }

        return false;
    }
}
