package dete.org.core.presenters;

import dete.org.core.dto.AttackResult;
import dete.org.core.entity.Entity;
import dete.org.core.entity.entities.Enemy;

/**
 * Define as operações de apresentação relacionadas às ações de combate.
 *
 * <p>Recebe os resultados produzidos pela lógica de combate e permite
 * que diferentes interfaces apresentem essas informações.</p>
 */
public interface CombatPresenter {
    void presenterAttack(AttackResult result, Entity entity);

    void presenterDefense(Entity entity);

    void presenterEnemyRegeneration(Enemy enemy);

    void presenterInvalidMessage();
}
