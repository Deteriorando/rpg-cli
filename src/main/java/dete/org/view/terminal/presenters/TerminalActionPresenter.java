package dete.org.view.terminal.presenters;

import dete.org.core.presenters.CombatPresenter;
import dete.org.core.dto.AttackResult;
import dete.org.core.entity.Entity;
import dete.org.core.entity.entities.Enemy;
import dete.org.view.terminal.CombatUI;
import dete.org.view.terminal.MenuIU;
import dete.org.view.terminal.TerminalUI;

public class TerminalActionPresenter implements CombatPresenter {

    @Override
    public void presenterAttack(AttackResult result, Entity entity) {
        String name = entity.getName();

        if (result.critical()) {
            CombatUI.multiplierActivatedMessage(name);
        }

        TerminalUI.clearConsole();

        CombatUI.damageDealtMessage(name, result.damage());
    }

    @Override
    public void presenterDefense(Entity entity) {
        if (entity.hasEffect()) {
            CombatUI.entityDefenseMessage(entity);
            CombatUI.defenseAbsorbedMessage(entity.getEffect());
        }
    }

    @Override
    public void presenterEnemyRegenaration(Enemy enemy) {
        CombatUI.enemyRegenMessage(enemy);
    }

    @Override
    public void presenterInvalidMessage() {
        MenuIU.invalidChoiceMessage();
    }

}
