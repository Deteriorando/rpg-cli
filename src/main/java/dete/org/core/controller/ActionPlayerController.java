package dete.org.core.controller;

import dete.org.core.presenters.CombatPresenter;
import dete.org.core.dto.AttackResult;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.InputScanner;
import dete.org.core.input.UserAction;
import dete.org.core.presenters.InventoryPresenter;

/**
 * Controla as ações disponíveis ao jogador durante o combate.
 *
 * <p>Responsável por interpretar a ação escolhida e delegar sua execução
 * às entidades ou ao sistema de inventário.</p>
 */
public class ActionPlayerController {

    private final InventoryController inventoryController;
    private final CombatPresenter actionPresenter;

    public ActionPlayerController(InputScanner scanner,
                                  CombatPresenter actionPresenter,
                                  InventoryPresenter inventoryPresenter
                                  ) {
        inventoryController = new InventoryController(scanner, inventoryPresenter);
        this.actionPresenter = actionPresenter;
    }

    public boolean execute(UserAction action, Player  player, Enemy enemy) {
        switch (action) {
            case ATTACK -> {
                AttackResult result = player.attack(enemy);
                actionPresenter.presenterAttack(result, player);
                return true;
            }
            case DEFENCE -> {
                player.applyEffect(new Effect("Postura Defensiva", 0.85));
                actionPresenter.presenterDefense(player);
                return true;
            }
            case INVENTORY -> {
                if(inventoryController.useItemInCombat(player)){
                    return true;
                }
            }
            default -> {
                actionPresenter.presenterInvalidMessage();
                return true;
            }
        }

        return false;
    }
}
