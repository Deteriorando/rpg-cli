package dete.org.core.Entity;

/**
 * <p>
 *     Cada classe guarda os seus atributos iniciais em um só lugar:
 *     um conjunto para o jogador e outro para o inimigo.
 * </p>
 */
public enum Classes {

    HUMAN      (1, "Human",   new ClassStats(1500, 33.4, 5),   new ClassStats(1500, 33.4, 5)),
    WIZARD     (2, "Wizard",  new ClassStats(2200, 35, 2),     new ClassStats(2200, 40, 2.5)),
    WITCH      (3, "Witch",   new ClassStats(2200, 35, 4),     new ClassStats(2200, 40, 4)),
    WARRIOR    (4, "Warrior", new ClassStats(1820, 45, 3),     new ClassStats(1820, 55, 3)),
    BERSEKER   (5, "Berserk", new ClassStats(1720, 27, 6),     new ClassStats(1720, 36.7, 6));

    final String name;
    final int id;
    private final ClassStats playerStats;
    private final ClassStats enemyStats;

    Classes(int id, String name, ClassStats playerStats, ClassStats enemyStats) {
        if (id < 0) throw new IllegalArgumentException("Você não pode colocar valores menores que 0");
        if (name == null) throw new NullPointerException("O Nome não pode está vazio!");
        if (playerStats == null || enemyStats == null) {
            throw new NullPointerException("Os atributos da classe não podem estar vazios!");
        }

        this.name = name;
        this.id = id;
        this.playerStats = playerStats;
        this.enemyStats = enemyStats;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public ClassStats getPlayerStats() {
        return playerStats;
    }

    public ClassStats getEnemyStats() {
        return enemyStats;
    }

    @Override
    public String toString() {
        return "\n- Classe: " + name.toUpperCase();
    }
}