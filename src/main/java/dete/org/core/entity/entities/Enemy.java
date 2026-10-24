package dete.org.core.entity.entities;

import dete.org.core.entity.ClassStats;
import dete.org.core.entity.Classes;
import dete.org.core.entity.Entity;

import java.util.Objects;

public class Enemy extends Entity {
    protected String name;

    public Enemy(Classes type, String name) {
        this(Objects.requireNonNull(type, "O tipo da Classe não pode estar vazio!").getEnemyStats(), type, name);
    }

    private Enemy(ClassStats stats, Classes type, String name) {
        super(stats.life(), stats.damage(), type, stats.multiplier());
        this.name = (name != null && !name.isBlank()) ? name : "No name";
    }

    @Override
    public void takeDamage(Enemy target) {
        setLife(life.getValue() - target.getDamage().getValue());
    }


    @Override
    public String toString() {
        return "- Atributos do Inimigo -\n" + name + life + damage + classes + multiplier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}