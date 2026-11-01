package dete.org.core.entity.configEntities;

import dete.org.core.entity.Classes;
import dete.org.core.entity.entities.Player;
import dete.org.core.input.PlayerInput;
import dete.org.core.presenters.MenuPresenter;

/**
 * Responsável por apresentar as opções de classe ao jogador
 * e aplicar a classe escolhida ao personagem.
 */
public record ConfigurePlayer(PlayerInput input, MenuPresenter menuPresenter) {

    public void configure(Player player) {
        menuPresenter.presenterBarDividerClasses();
        Classes[] classes = Classes.values();

        menuPresenter.presenterShowClasses(classes);
        menuPresenter.presenterAskClassNumber();

        int choice = input.getCodeInt();
        if (choice < 1 || choice > classes.length) {
            menuPresenter.presenterOptionNumberInvalidClasses();
            return;
        }

        Classes selectedClass = classes[choice - 1];

        player.changeClass(selectedClass);

        menuPresenter.presenterShowClassSelected(selectedClass);
        menuPresenter.presenterShowProfile(player);
    }

}