package dete.org.core.input;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Responsável por fornecer as ações que podem ser realizadas pelo inimigo
 * durante o combate e por gerar uma escolha de ação aleatória.
 */
public class EnemyInput {

    /**
     * Representa as ações disponíveis para o inimigo durante o combate.
     *
     * <p>Cada ação possui um código utilizado para identificá-la e um nome
     * utilizado na apresentação.</p>
     */
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

        /**
         * Retorna a ação correspondente ao código informado.
         *
         * <p>Caso o código não corresponda a nenhuma ação, retorna
         * {@link #ATTACK} como ação padrão.</p>
         *
         * @param code código que representa a ação
         * @return ação correspondente ao código ou {@link #ATTACK} caso
         *         nenhuma ação seja encontrada
         */
        public static Action getByCode(int code) {
            for (Action enemyAction : Action.values()) {
                if (enemyAction.getCode() == code) {
                    return enemyAction;
                }
            }

            return ATTACK;
        }
    }

    /**
     * Gera aleatoriamente o código de uma ação disponível para o inimigo.
     *
     * @return código aleatório entre 1 e 5
     */
    public int getRandomCode() {
        return ThreadLocalRandom.current().nextInt(1, 5 + 1);
    }
}
