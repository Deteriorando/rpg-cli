package dete.org.core.input;

import java.util.concurrent.ThreadLocalRandom;

public class EnemyInput {
    public enum Action {
        ATTACK(1, "Atacar"),
        DEFENSE(2, "Defesa"),
        REGENERATION(3, "Regenerar");

        private final int code;
        private final String name;

        Action(int code, String name) {
            this.code = code;
            this.name = name;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static Action getByCode(int code) {
            for (Action enemyAction : Action.values()) {
                if (enemyAction.getCode() == code) {
                    return enemyAction;
                }
            }

            return ATTACK;
        }
    }


    public int getRandomCode() {
        return ThreadLocalRandom.current().nextInt(1, 5 + 1);
    }
}
