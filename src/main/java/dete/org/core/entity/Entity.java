package dete.org.core.entity;

import dete.org.core.dto.AttackResult;
import dete.org.core.entity.atributes.Damage;
import dete.org.core.entity.atributes.Life;
import dete.org.core.entity.atributes.Multiplier;
import dete.org.core.controller.Effect;

import java.util.concurrent.ThreadLocalRandom;

public abstract class Entity implements EntityBehaviour {
    protected Life life;
    protected Damage damage;
    protected Classes classes;
    protected Multiplier multiplier;
    protected Effect activeEffect;
    protected int regenCooldown = 0; // Inicia em 0 para a habilidade estar disponível no início do combate

    public Entity(double life, double damage, Classes type, double multiplied) {
        if (type == null)
            throw new NullPointerException("O tipo da Classe não pode estar vazio!");

        this.life = new Life(life);
        this.damage = new Damage(damage);
        this.multiplier = new Multiplier(multiplied);
        this.classes = type;
    }

    /**
     * Realiza um ataque contra uma entidade.
     *
     * <p>
     * O dano pode ser reduzido por um efeito defensivo presente no alvo.
     * Após o cálculo, o dano é aplicado à vida do alvo. <br> <br>
     *
     * Pode haver uma chance de multiplicar o dano.
     * </p>
     *
     * @param target entidade que receberá o dano
     * @return valor final do dano causado ao alvo
     */
    @Override
    public AttackResult attack(Entity target) {
        double damageToDeal = this.damage.getValue();
        boolean isCritical = rollChanceMultiplier(30);

        if (isCritical) {
            damageToDeal *= multiplier.getValue();
        }

        if (target.hasEffect()) {
            damageToDeal = target.getEffect().apply(damageToDeal);
            target.clearEffect();
        }

        target.getLife().takeDamage(damageToDeal);

        return new AttackResult(damageToDeal, isCritical);
    }

    /**
     * Regenera a vida da entidade.
     *
     * <p>Existe uma chance de 30% de aplicar o multiplicador de regeneração.
     * O valor calculado é então aplicado à vida da entidade.</p>
     *
     * @param value valor base da regeneração
     * @return valor total da cura calculada, independentemente de quanto foi
     *         efetivamente acrescentado à vida
     */
    @Override
    public double regeneration(double value) {
        boolean canMultiplyRegen = rollChanceMultiplier(30);

        if (canMultiplyRegen) {
            value *= multiplier.getValue();
        }

        life.regenerate(value);

        return value;
    }

    public void updateCooldowns() {
        if (regenCooldown > 0) {
            regenCooldown--;
        }
    }

    public boolean rollChanceMultiplier(int chance) {
        if (chance < 0 || chance > 100) {
            throw new IllegalArgumentException("Chance deve estar entre 0 e 100.");
        }

        return ThreadLocalRandom.current().nextInt(100) < chance;
    }

    public boolean isAlive() { return this.life.getValue() > 0; }

    public void applyEffect(Effect effect) { this.activeEffect = effect; }
    public void clearEffect() { this.activeEffect = null; }
    public boolean hasEffect() { return this.activeEffect != null; }
    public Effect getActiveEffect() { return this.activeEffect; }

    public boolean canRegenerate() { return regenCooldown <= 0; }
    public int getRegenCooldown() { return regenCooldown; }
    public void triggerRegenCooldown() { this.regenCooldown = 4; }
    public void resetCooldowns() { this.regenCooldown = 0; }

    public void setLife(double life) { this.life.setValue(life); }
    public void setDamage(double damage) { this.damage.setValue(damage); }
    public void setMultiplied(double multiplied) { this.multiplier.setValue(multiplied); }
    public void setClasses(Classes type) {
        if (type == null) throw new NullPointerException("O tipo da Classe não pode estar vazio!");
        this.classes = type;
    }

    public Classes getClasses() { return classes; }
    public Damage getDamage() { return damage; }
    public Multiplier getMultiplied() { return multiplier; }
    public Life getLife() { return life; }
    public Effect getEffect() { return activeEffect; }
}