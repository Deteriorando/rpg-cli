package dete.org.core.controller;

import dete.org.core.dto.AttackResult;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.UserAction;
import dete.org.core.ui.MenuInterface;

public class ActionPlayerController {
    public boolean execute(UserAction action, Player  player, Enemy enemy) {
        switch (action) {
            case ATTACK -> {
                AttackResult result = player.attack(enemy);
                if (result.critical()) MenuInterface.multiplierActivatedMessage("Player");
                MenuInterface.damageDealtMessage("Player", result.damage());
                return true;
            }
            case DEFENCE -> {
                player.applyEffect(new Effect("Postura Defensiva", 0.85));
                MenuInterface.playerDefenseMessage(player);
                return true;
            }
            case INVENTORY -> {
                if(InventoryController.useItemInCombat(player)){
                    return true;
                }
            }
            default -> {
                MenuInterface.invalidChoiceMessage();
                return true;
            }
        }

        return false;
    }
}
