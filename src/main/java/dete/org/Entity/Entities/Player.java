package dete.org.Entity.Entities;

import dete.org.Entity.Classes;
import dete.org.Entity.Entity;

public class Player extends Entity {

    public Player(double life, double damage, Classes type, double multiplied) {
        super(life, damage, type, multiplied);
    }

    @Override
    public void takeDamage(Enemy target) {
        life.setValue(life.getValue() - target.getDamage().getValue());
    }

    @Override
    public String toString() {
        return "- Atributos do Player -\n" + life + damage + classes + multiplier;
    }


}
