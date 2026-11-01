package dete.org.core.presenters;

import dete.org.core.dto.AttackResult;
import dete.org.core.entity.Entity;
import dete.org.core.entity.entities.Enemy;

public interface CombatPresenter {
    void presenterAttack(AttackResult result, Entity entity);

    void presenterDefense(Entity entity);

    void presenterEnemyRegenaration(Enemy enemy);

    void presenterInvalidMessage();
}
