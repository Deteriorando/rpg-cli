package dete.org.Entity;

public enum Classes {
    HUMAN(1,"Human"),
    WIZARD(2,"Wizard"),
    WITCH(3,"Witch"),
    WARRIOR(4,"Warrior"),
    BERSEKER(5, "Berserk");

    final String name;
    final int id;

    Classes(int id, String name) {
        if (id < 0) throw new IllegalArgumentException("Você não pode colocar valores menores que 0");
        if (name == null) throw new NullPointerException("O Nome não pode está vazio!");

        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "\n- Classe: " + name.toUpperCase();
    }
}
