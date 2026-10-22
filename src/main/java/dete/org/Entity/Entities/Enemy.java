package dete.org.Entity.Entities;

import dete.org.Entity.ClassStats;
import dete.org.Entity.Classes;
import dete.org.Entity.Entity;

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