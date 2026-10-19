package dete.org.Entity.Entities;

import dete.org.Entity.Classes;
import dete.org.Entity.Entity;

public class Enemy extends Entity {
    protected String name;

    public Enemy(double life, double damage, Classes type, double multiplied, String name) {
        super(life, damage, type, multiplied);
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
