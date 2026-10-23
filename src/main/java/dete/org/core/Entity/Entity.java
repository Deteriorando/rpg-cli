package dete.org.core.Entity;

import dete.org.core.Entity.Atributes.Damage;
import dete.org.core.Entity.Atributes.Life;
import dete.org.core.Entity.Atributes.Multiplier;
import dete.org.core.Entity.Entities.Enemy;
import dete.org.core.Controller.Effect;
import dete.org.core.Interface.MenuInterface;

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

    @Override
    public void attack(Entity target) {
        double damageToDeal = calculateDamageValue();
        boolean hadEffect = target.hasEffect();
        if (hadEffect) {
            damageToDeal = target.getEffect().apply(damageToDeal);
            target.clearEffect();
        }

        int currentLife = (int) Math.round(target.getLife().getValue());
        target.getLife().setValue(currentLife - damageToDeal);

        String attackerName = (this instanceof Enemy) ? ((Enemy) this).getName() : "Player";
        MenuInterface.damageDealtMessage(attackerName, damageToDeal);
    }

    @Override
    public void regeneration(double value) {
        boolean canMultiplyRegen = java.util.concurrent.ThreadLocalRandom.current().nextInt(100) < 30;
        double regenValue = life.getValue() + (value * multiplier.getValue());


        if (canMultiplyRegen) {
            MenuInterface.multiplierActivatedMessage(this.getClass().getSimpleName());
            life.setValue(regenValue);
            return;
        }
        life.setValue(regenValue);
    }

    private double calculateDamageValue() {
        boolean isCritical = java.util.concurrent.ThreadLocalRandom.current().nextInt(100) < 30;

        if (isCritical) {
            MenuInterface.multiplierActivatedMessage(this.getClass().getSimpleName());
            return this.damage.getValue() * this.multiplier.getValue();
        }

        return this.damage.getValue();
    }

    public void updateCooldowns() {
        if (regenCooldown > 0) {
            regenCooldown--;
        }
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

    public abstract void takeDamage(Enemy target);

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