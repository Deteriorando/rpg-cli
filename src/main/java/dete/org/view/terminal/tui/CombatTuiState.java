package dete.org.view.terminal.tui;

import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Estado vivo da interface TUI durante uma partida de combate.
 */
public final class CombatTuiState {

    public enum Mode {
        ACTION_SELECT,
        INVENTORY_SELECT,
        ENEMY_TURN,
        VICTORY,
        DEFEAT
    }

    public enum EventKind {
        CRITICAL_ENEMY,
        ENEMY_NORMAL,
        PLAYER_CRITICAL,
        PLAYER_NORMAL,
        DEFENSE,
        HEAL,
        INFO
    }

    public record HistoryEntry(int round, String message, EventKind kind) {}

    private static final int MAX_HISTORY = 50;

    private final Player player;
    private final Enemy enemy;
    private final List<HistoryEntry> history = new ArrayList<>();

    private int round = 1;
    private Mode mode = Mode.ACTION_SELECT;
    private int selectedAction = 0; // 0=Atacar, 1=Defender, 2=Usar item
    private int selectedItem = 0;   // 0..slots.size()-1 = itens; slots.size() = Voltar
    private String statusNotice;
    private boolean noticeAlert;
    private int finalRounds = 1;

    public CombatTuiState(Player player, Enemy enemy) {
        this.player = player;
        this.enemy = enemy;
    }

    public synchronized Player getPlayer() {
        return player;
    }

    public synchronized Enemy getEnemy() {
        return enemy;
    }

    public synchronized int getRound() {
        return round;
    }

    public synchronized void setRound(int round) {
        this.round = Math.max(1, round);
    }

    public synchronized Mode getMode() {
        return mode;
    }

    public synchronized void setMode(Mode mode) {
        this.mode = mode;
    }

    public synchronized int getSelectedAction() {
        return selectedAction;
    }

    public synchronized void setSelectedAction(int index) {
        this.selectedAction = Math.floorMod(index, 3);
    }

    public synchronized void moveAction(int delta) {
        setSelectedAction(selectedAction + delta);
    }

    public synchronized int getSelectedItem() {
        return selectedItem;
    }

    public synchronized void resetItemSelection(int slotCount) {
        this.selectedItem = 0;
        clampItemSelection(slotCount);
    }

    public synchronized void moveItem(int delta, int slotCount) {
        int totalOptions = slotCount + 1; // itens + [0] Voltar
        this.selectedItem = Math.floorMod(this.selectedItem + delta, totalOptions);
    }

    public synchronized void clampItemSelection(int slotCount) {
        int totalOptions = slotCount + 1;
        if (selectedItem < 0 || selectedItem >= totalOptions) {
            selectedItem = 0;
        }
    }

    public synchronized String getStatusNotice() {
        return statusNotice;
    }

    public synchronized boolean isNoticeAlert() {
        return noticeAlert;
    }

    public synchronized void setStatusNotice(String message, boolean alert) {
        this.statusNotice = message;
        this.noticeAlert = alert;
    }

    public synchronized void clearStatusNotice() {
        this.statusNotice = null;
        this.noticeAlert = false;
    }

    public synchronized int getFinalRounds() {
        return finalRounds;
    }

    public synchronized void setFinalRounds(int finalRounds) {
        this.finalRounds = Math.max(1, finalRounds);
    }

    public synchronized void addHistory(int eventRound, String message, EventKind kind) {
        history.add(0, new HistoryEntry(eventRound, message, kind));
        if (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }
    }

    public synchronized List<HistoryEntry> getHistorySnapshot() {
        return List.copyOf(history);
    }
}
