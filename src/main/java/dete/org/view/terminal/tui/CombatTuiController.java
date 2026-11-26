package dete.org.view.terminal.tui;

import dete.org.audio.SoundEffects;
import dete.org.core.dto.AttackResult;
import dete.org.core.entity.Entity;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.inventory.Inventory.InventorySlot;
import dete.org.core.inventory.Item;
import dete.org.view.terminal.tui.CombatTuiState.EventKind;
import dete.org.view.terminal.tui.CombatTuiState.Mode;
import dete.org.view.terminal.tui.TerminalSession.KeyEvent;
import dete.org.view.terminal.tui.TerminalSession.KeyType;
import dete.org.view.terminal.tui.TerminalSession.Size;

import java.util.List;
import java.util.function.Supplier;

/**
 * Coordena o ciclo de vida da TUI de combate, conectando presenters e entrada
 * ao estado vivo e ao backend de terminal.
 */
public final class CombatTuiController {

    private static CombatTuiController activeInstance;

    private final TerminalSession terminal;
    private final CombatTuiState state;
    private boolean eofReached = false;

    private CombatTuiController(Player player, Enemy enemy) {
        this.terminal = new TerminalSession();
        this.state = new CombatTuiState(player, enemy);
    }

    public static synchronized void startCombat(Player player, Enemy enemy) {
        endCombat();
        CombatTuiController controller = new CombatTuiController(player, enemy);
        activeInstance = controller;
        controller.terminal.enterTui();
        controller.render();
    }

    public static synchronized void endCombat() {
        if (activeInstance != null) {
            activeInstance.terminal.close();
            activeInstance = null;
        }
    }

    public static synchronized CombatTuiController getActive() {
        return activeInstance;
    }

    public static synchronized boolean isActive() {
        return activeInstance != null;
    }

    public boolean isEofReached() {
        return eofReached;
    }

    public void render() {
        Size size = terminal.getCurrentSize();
        String frame = CombatTuiRenderer.render(state, size.cols(), size.rows(), terminal.isInteractiveTty());
        terminal.paintFrame(frame);
    }

    private void clearAndRender() {
        Size size = terminal.getCurrentSize();
        String frame = CombatTuiRenderer.render(state, size.cols(), size.rows(), terminal.isInteractiveTty());
        terminal.clearAndPaintFrame(frame);
    }

    public void onRoundStart(int round) {
        state.setRound(round);
    }

    public void onPlayerDecisionPrompt() {
        state.setMode(Mode.ACTION_SELECT);
        render();
    }

    public void onInventoryPrompt(Player player) {
        List<InventorySlot> slots = player.getInventorySlots();
        state.resetItemSelection(slots.size());
        state.setMode(Mode.INVENTORY_SELECT);
        render();
    }

    public void onEnemyTurnStart() {
        state.setMode(Mode.ENEMY_TURN);
        render();
        pauseTty(320);
    }

    public void onAttack(AttackResult result, Entity attacker) {
        int dmg = (int) result.damage();
        boolean crit = result.critical();
        String critSuffix = crit ? " (Critico)" : "";
        if (attacker instanceof Player) {
            state.addHistory(state.getRound(),
                    "Voce causou " + dmg + " de dano." + critSuffix,
                    crit ? EventKind.PLAYER_CRITICAL : EventKind.PLAYER_NORMAL);
        } else {
            String name = attacker.getName() != null ? attacker.getName() : "Inimigo";
            state.addHistory(state.getRound(),
                    name + " causou " + dmg + " de dano." + critSuffix,
                    crit ? EventKind.CRITICAL_ENEMY : EventKind.ENEMY_NORMAL);
        }
        state.clearStatusNotice();
        render();
        pauseTty(240);
    }

    public void onDefense(Entity entity) {
        if (entity instanceof Player) {
            state.addHistory(state.getRound(), "Voce entrou em defesa.", EventKind.DEFENSE);
        } else {
            String name = entity.getName() != null ? entity.getName() : "Inimigo";
            state.addHistory(state.getRound(), name + " entrou em defesa.", EventKind.DEFENSE);
        }
        state.clearStatusNotice();
        render();
        pauseTty(220);
    }

    public void onEnemyRegeneration(Enemy enemy, double healed, boolean critical) {
        String name = enemy.getName() != null ? enemy.getName() : "Inimigo";
        String critSuffix = critical ? " (Critico)" : "";
        state.addHistory(state.getRound(),
                name + " regenerou " + (int) healed + " de vida." + critSuffix,
                EventKind.HEAL);
        render();
        pauseTty(240);
    }

    public void onInvalidAction() {
        state.addHistory(state.getRound(), "Opcao invalida! Voce perdeu a acao.", EventKind.INFO);
        state.setStatusNotice("Opcao invalida! Voce perdeu a acao.", true);
        render();
        pauseTty(220);
    }

    public void onNoItemsToUse() {
        state.setStatusNotice("Voce nao tem itens para usar.", true);
        render();
    }

    public void onInvalidItem() {
        state.setStatusNotice("Item invalido!", true);
        render();
    }

    public void onItemUsed(Item item) {
        state.addHistory(state.getRound(), "Voce usou " + item.getName() + ".", EventKind.HEAL);
        state.clearStatusNotice();
    }

    public void onVictory(int rounds) {
        state.setFinalRounds(rounds);
        state.setMode(Mode.VICTORY);
        state.addHistory(rounds, "Vitoria! " + state.getEnemy().getName() + " foi derrotado.", EventKind.HEAL);
        render();
        waitToExitCombat();
    }

    public void onDefeat() {
        state.setFinalRounds(state.getRound());
        state.setMode(Mode.DEFEAT);
        state.addHistory(state.getRound(), "Derrota! Voce caiu em combate.", EventKind.CRITICAL_ENEMY);
        render();
        waitToExitCombat();
    }

    /**
     * Lê a escolha numérica no contexto atual da TUI (decisão de combate ou inventário).
     * Em TTY usa setas/números/Enter; em entrada sem TTY usa {@code nonTtyIntReader}.
     */
    public int readChoiceCode(Supplier<Integer> nonTtyIntReader) {
        if (!terminal.isInteractiveTty()) {
            return nonTtyIntReader.get();
        }
        if (state.getMode() == Mode.INVENTORY_SELECT) {
            return readInventoryKey();
        }
        return readActionKey();
    }

    private int readActionKey() {
        while (true) {
            KeyEvent ev = terminal.readKeyWatchingResize(this::clearAndRender);
            switch (ev.type()) {
                case LEFT, UP -> {
                    moveActionSelection(-1);
                    state.clearStatusNotice();
                    render();
                }
                case RIGHT, DOWN, TAB -> {
                    moveActionSelection(1);
                    state.clearStatusNotice();
                    render();
                }
                case DIGIT -> {
                    if (ev.digit() >= 1 && ev.digit() <= 3) {
                        state.setSelectedAction(ev.digit() - 1);
                        state.clearStatusNotice();
                        render();
                        return ev.digit();
                    }
                }
                case ENTER -> {
                    state.clearStatusNotice();
                    return state.getSelectedAction() + 1;
                }
                case CHAR -> {
                    char lower = Character.toLowerCase(ev.ch());
                    if (lower == 'a' || lower == 'h') {
                        moveActionSelection(-1);
                        state.clearStatusNotice();
                        render();
                    } else if (lower == 'd' || lower == 'l') {
                        moveActionSelection(1);
                        state.clearStatusNotice();
                        render();
                    }
                }
                case EOF -> {
                    eofReached = true;
                    return -1;
                }
                default -> {}
            }
        }
    }

    private int readInventoryKey() {
        int slotCount = state.getPlayer().getInventorySlots().size();
        while (true) {
            KeyEvent ev = terminal.readKeyWatchingResize(this::clearAndRender);
            switch (ev.type()) {
                case LEFT, UP -> {
                    moveItemSelection(-1, slotCount);
                    state.clearStatusNotice();
                    render();
                }
                case RIGHT, DOWN, TAB -> {
                    moveItemSelection(1, slotCount);
                    state.clearStatusNotice();
                    render();
                }
                case DIGIT -> {
                    int d = ev.digit();
                    if (d == 0) {
                        state.clearStatusNotice();
                        return 0;
                    }
                    if (d >= 1 && d <= slotCount) {
                        state.clearStatusNotice();
                        return d;
                    }
                }
                case ESCAPE, BACKSPACE -> {
                    state.clearStatusNotice();
                    return 0;
                }
                case ENTER -> {
                    state.clearStatusNotice();
                    int sel = state.getSelectedItem();
                    return sel >= slotCount ? 0 : (sel + 1);
                }
                case CHAR -> {
                    char lower = Character.toLowerCase(ev.ch());
                    if (lower == 'a' || lower == 'h') {
                        moveItemSelection(-1, slotCount);
                        state.clearStatusNotice();
                        render();
                    } else if (lower == 'd' || lower == 'l') {
                        moveItemSelection(1, slotCount);
                        state.clearStatusNotice();
                        render();
                    } else if (lower == 'q') {
                        state.clearStatusNotice();
                        return 0;
                    }
                }
                case EOF -> {
                    eofReached = true;
                    return 0;
                }
                default -> {}
            }
        }
    }

    /** Move a seleção de ação e só toca quando o índice realmente muda. */
    private void moveActionSelection(int delta) {
        int previous = state.getSelectedAction();
        state.moveAction(delta);
        if (state.getSelectedAction() != previous) {
            SoundEffects.play(SoundEffects.Cue.MOVE_SELECTION);
        }
    }

    /** Move a seleção do inventário e só toca quando o índice realmente muda. */
    private void moveItemSelection(int delta, int slotCount) {
        int previous = state.getSelectedItem();
        state.moveItem(delta, slotCount);
        if (state.getSelectedItem() != previous) {
            SoundEffects.play(SoundEffects.Cue.MOVE_SELECTION);
        }
    }

    private void waitToExitCombat() {
        if (!terminal.isInteractiveTty()) {
            return;
        }
        while (true) {
            KeyEvent ev = terminal.readKeyWatchingResize(this::clearAndRender);
            if (ev.type() == KeyType.ENTER || (ev.type() == KeyType.CHAR && ev.ch() == ' ')) {
                SoundEffects.play(SoundEffects.Cue.CONFIRM);
                return;
            }
            if (ev.type() == KeyType.ESCAPE
                    || ev.type() == KeyType.EOF
                    || (ev.type() == KeyType.CHAR && Character.toLowerCase(ev.ch()) == 'q')) {
                return;
            }
        }
    }

    private void pauseTty(long ms) {
        if (!terminal.isInteractiveTty()) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
