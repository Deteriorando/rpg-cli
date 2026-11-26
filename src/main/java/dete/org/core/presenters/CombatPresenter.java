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

    /**
     * @param enemy    o inimigo que regenerou
     * @param healed   quanto de vida foi recuperado após aplicar o limite de vida
     * @param critical se a regeneração teve o multiplicador aplicado
     */
    void presenterEnemyRegeneration(Enemy enemy, double healed, boolean critical);

    void presenterInvalidMessage();
}
