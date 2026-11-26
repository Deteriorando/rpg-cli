package dete.org.view.terminal.tui;

import dete.org.core.inventory.Inventory.InventorySlot;
import dete.org.core.inventory.Item;
import dete.org.core.inventory.ItemProperties;
import dete.org.view.terminal.tui.AnsiStyle.LineBuilder;
import dete.org.view.terminal.tui.AnsiStyle.Rgb;
import dete.org.view.terminal.tui.CombatTuiState.Mode;
import java.util.ArrayList;
import java.util.List;

final class CombatDecisionPanel {
    private CombatDecisionPanel() {}

    static void buildDecisionPanel(List<String> out,
                                           int innerWidth,
                                           CombatTuiState state,
                                           int rows) {
        int contentWidth = innerWidth - 4;
        Mode mode = state.getMode();
        List<String> bodyRows = new ArrayList<>(rows);

        switch (mode) {
            case ACTION_SELECT -> renderDecisionActions(bodyRows, state, contentWidth, rows);
            case INVENTORY_SELECT -> renderDecisionInventory(bodyRows, state, contentWidth, rows);
            case ENEMY_TURN -> renderDecisionEnemyTurn(bodyRows, state, contentWidth, rows);
            case VICTORY, DEFEAT -> renderDecisionEnd(bodyRows, state, contentWidth, rows);
        }

        CombatPanelFrame.appendFullPanel(out, "DECISAO", AnsiStyle.FG_CYAN, innerWidth, bodyRows);
    }

    private static void renderDecisionActions(List<String> lines,
                                              CombatTuiState state,
                                              int width,
                                              int totalRows) {
        // Linha 0: pergunta + aviso opcional
        LineBuilder promptRow = new LineBuilder();
        String prompt = "  Qual sua acao nesta rodada?";
        String notice = state.getStatusNotice();
        if (notice != null && !notice.isEmpty() && width > prompt.length() + 12) {
            promptRow.append(prompt, AnsiStyle.FG_WHITE, AnsiStyle.BG_PANEL);
            int maxNotice = width - prompt.length() - 4;
            String n = AnsiStyle.truncatePlain(notice, maxNotice);
            promptRow.appendSpaces(Math.max(2, width - prompt.length() - n.length() - 2), AnsiStyle.BG_PANEL);
            promptRow.append(n, state.isNoticeAlert() ? AnsiStyle.FG_SALMON : AnsiStyle.FG_GOLD, AnsiStyle.BG_PANEL);
        } else {
            promptRow.append(AnsiStyle.truncatePlain(prompt, width - 1), AnsiStyle.FG_WHITE, AnsiStyle.BG_PANEL);
        }
        promptRow.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(promptRow.finish());

        if (totalRows >= 5) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PANEL));
        }

        // Botões
        int sel = state.getSelectedAction();
        int pillWidth = Math.max(16, Math.min(22, (width - 8) / 3));
        LineBuilder btnRow = new LineBuilder();
        btnRow.append("  ", null, AnsiStyle.BG_PANEL);
        appendPill(btnRow, sel == 0, "[1] Atacar", pillWidth,
                AnsiStyle.BG_BTN_ATTACK_ACTIVE, AnsiStyle.BG_BTN_ATTACK_INACTIVE, AnsiStyle.FG_SALMON);
        btnRow.appendSpaces(2, AnsiStyle.BG_PANEL);
        appendPill(btnRow, sel == 1, "[2] Defender", pillWidth,
                AnsiStyle.BG_BTN_DEFEND_ACTIVE, AnsiStyle.BG_BTN_DEFEND_INACTIVE, AnsiStyle.FG_ARMOR);
        btnRow.appendSpaces(2, AnsiStyle.BG_PANEL);
        appendPill(btnRow, sel == 2, "[3] Usar item", pillWidth,
                AnsiStyle.BG_BTN_ITEM_ACTIVE, AnsiStyle.BG_BTN_ITEM_INACTIVE, AnsiStyle.FG_PURPLE);
        btnRow.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(btnRow.finish());

        if (totalRows >= 5) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PANEL));
        }

        // Atalhos
        LineBuilder hintRow = new LineBuilder();
        if (width >= 66) {
            hintRow.append("  [<-] [->] ", AnsiStyle.FG_CYAN, AnsiStyle.BG_PANEL);
            hintRow.append("Mover", AnsiStyle.FG_DIM, AnsiStyle.BG_PANEL);
            hintRow.append("       [1-3] ", AnsiStyle.FG_CYAN, AnsiStyle.BG_PANEL);
            hintRow.append("Ir direto", AnsiStyle.FG_DIM, AnsiStyle.BG_PANEL);
            hintRow.append("        [Enter] ", AnsiStyle.FG_CYAN, AnsiStyle.BG_PANEL);
            hintRow.append("Confirmar", AnsiStyle.FG_DIM, AnsiStyle.BG_PANEL);
        } else {
            hintRow.append(AnsiStyle.truncatePlain("  [<-/->] Mover  [1-3] Direto  [Enter] OK", width - 1),
                    AnsiStyle.FG_MUTED, AnsiStyle.BG_PANEL);
        }
        hintRow.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(hintRow.finish());

        padEmptyPanelLines(lines, totalRows, width);
    }

    private static void renderDecisionInventory(List<String> lines,
                                                CombatTuiState state,
                                                int width,
                                                int totalRows) {
        List<InventorySlot> slots = state.getPlayer().getInventorySlots();
        state.clampItemSelection(slots.size());
        int sel = state.getSelectedItem();

        LineBuilder promptRow = new LineBuilder();
        String prompt = "  Inventario: escolha um item para usar";
        String notice = state.getStatusNotice();
        if (notice != null && !notice.isEmpty() && width > prompt.length() + 10) {
            promptRow.append(prompt, AnsiStyle.FG_CYAN, AnsiStyle.BG_PANEL);
            String n = AnsiStyle.truncatePlain(notice, width - prompt.length() - 4);
            promptRow.appendSpaces(Math.max(2, width - prompt.length() - n.length() - 2), AnsiStyle.BG_PANEL);
            promptRow.append(n, AnsiStyle.FG_SALMON, AnsiStyle.BG_PANEL);
        } else {
            promptRow.append(AnsiStyle.truncatePlain(prompt, width - 1), AnsiStyle.FG_CYAN, AnsiStyle.BG_PANEL);
        }
        promptRow.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(promptRow.finish());

        if (totalRows >= 5) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PANEL));
        }

        List<String> labels = inventoryLabels(slots);

        // Janela deslizante horizontal caso haja vários itens
        LineBuilder btnRow = new LineBuilder();
        btnRow.append("  ", null, AnsiStyle.BG_PANEL);
        int avail = width - 4;
        int startIdx = 0;
        int selectedWidth = Math.min(Math.max(labels.get(sel).length() + 5, 14), avail);
        if (sel > 0) {
            int previousWidth = Math.min(Math.max(labels.get(sel - 1).length() + 5, 14), avail);
            startIdx = previousWidth + 2 + selectedWidth <= avail ? sel - 1 : sel;
        }
        int used = 0;
        for (int i = startIdx; i < labels.size(); i++) {
            boolean isBack = (i == labels.size() - 1);
            String lbl = labels.get(i);
            int pillW = Math.min(Math.max(lbl.length() + 5, 14), Math.max(14, avail));
            if (used > 0 && used + 2 + pillW > avail) {
                break;
            }
            if (used > 0) {
                btnRow.appendSpaces(2, AnsiStyle.BG_PANEL);
                used += 2;
            }
            boolean active = (i == sel);
            appendPill(btnRow, active, AnsiStyle.truncatePlain(lbl, pillW - 4), pillW,
                    isBack ? AnsiStyle.BG_BTN_BACK_ACTIVE : AnsiStyle.BG_BTN_ITEM_ACTIVE,
                    isBack ? AnsiStyle.BG_BTN_BACK_INACTIVE : AnsiStyle.BG_BTN_ITEM_INACTIVE,
                    isBack ? AnsiStyle.FG_WHITE : AnsiStyle.FG_PURPLE);
            used += pillW;
        }
        btnRow.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(btnRow.finish());

        if (totalRows >= 5) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PANEL));
        }

        LineBuilder hintRow = new LineBuilder();
        String useKey = slots.size() == 1 ? "[1]" : "[1-" + slots.size() + "]";
        String hint = slots.isEmpty()
                ? "  [0/Esc/Enter] Voltar"
                : "  [<-/->] Mover  " + useKey + " Usar  [0/Esc] Voltar  [Enter] OK";
        hintRow.append(AnsiStyle.truncatePlain(hint, width - 1), AnsiStyle.FG_MUTED, AnsiStyle.BG_PANEL);
        hintRow.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(hintRow.finish());

        padEmptyPanelLines(lines, totalRows, width);
    }

    private static void renderDecisionEnemyTurn(List<String> lines,
                                                CombatTuiState state,
                                                int width,
                                                int totalRows) {
        LineBuilder row0 = new LineBuilder();
        row0.append("  Turno de " + CombatStatusPanels.safeName(state.getEnemy().getName(), "Inimigo") + "...",
                AnsiStyle.FG_GOLD, AnsiStyle.BG_PANEL);
        row0.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(row0.finish());

        if (totalRows >= 5) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PANEL));
        }

        LineBuilder row1 = new LineBuilder();
        row1.append("  Aguarde a acao do inimigo...", AnsiStyle.FG_MUTED, AnsiStyle.BG_PANEL);
        row1.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(row1.finish());

        padEmptyPanelLines(lines, totalRows, width);
    }

    static List<String> inventoryLabels(List<InventorySlot> slots) {
        List<String> labels = new ArrayList<>(slots.size() + 1);
        for (int i = 0; i < slots.size(); i++) {
            InventorySlot slot = slots.get(i);
            Item item = slot.getItem();
            String quantity = item.hasProperty(ItemProperties.STACKABLE)
                    ? " (x" + slot.getQuantity() + "/" + item.getMaxStackSize() + ")"
                    : "";
            labels.add("[" + (i + 1) + "] " + item.getName() + quantity);
        }
        labels.add("[0] Voltar");
        return labels;
    }

    private static void renderDecisionEnd(List<String> lines,
                                          CombatTuiState state,
                                          int width,
                                          int totalRows) {
        boolean victory = (state.getMode() == Mode.VICTORY);
        String enemyName = CombatStatusPanels.safeName(state.getEnemy().getName(), "Inimigo");
        String banner = victory
                ? String.format("  VITORIA! Voce derrotou %s em %02d rodada(s)!", enemyName, state.getFinalRounds())
                : String.format("  DERROTA! Voce foi derrotado por %s na rodada %02d...", enemyName, state.getFinalRounds());

        LineBuilder row0 = new LineBuilder();
        row0.append(AnsiStyle.truncatePlain(banner, width - 1),
                victory ? AnsiStyle.FG_GREEN : AnsiStyle.FG_SALMON, AnsiStyle.BG_PANEL, true);
        row0.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(row0.finish());

        if (totalRows >= 5) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PANEL));
        }

        LineBuilder btn = new LineBuilder();
        btn.append("  ", null, AnsiStyle.BG_PANEL);
        int pillW = Math.min(28, Math.max(18, width - 4));
        appendPill(btn, true, "Voltar ao menu", pillW,
                victory ? AnsiStyle.BG_BTN_DEFEND_ACTIVE : AnsiStyle.BG_BTN_ATTACK_ACTIVE,
                AnsiStyle.BG_BTN_BACK_INACTIVE,
                victory ? AnsiStyle.FG_GREEN : AnsiStyle.FG_GOLD);
        btn.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(btn.finish());

        if (totalRows >= 5) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PANEL));
        }

        LineBuilder hint = new LineBuilder();
        hint.append("  [Enter] ", AnsiStyle.FG_MUTED, AnsiStyle.BG_PANEL);
        hint.append("Continuar", AnsiStyle.FG_DIM, AnsiStyle.BG_PANEL);
        hint.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(hint.finish());

        padEmptyPanelLines(lines, totalRows, width);
    }

    private static void padEmptyPanelLines(List<String> lines, int totalRows, int width) {
        while (lines.size() < totalRows) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PANEL));
        }
    }

    private static void appendPill(LineBuilder lb,
                                   boolean selected,
                                   String label,
                                   int pillWidth,
                                   Rgb activeBg,
                                   Rgb inactiveBg,
                                   Rgb shortcutColor) {
        Rgb bg = selected ? activeBg : inactiveBg;
        String prefix = selected ? " > " : "   ";
        String body = AnsiStyle.truncatePlain(label, pillWidth - prefix.length());
        int shortcutEnd = body.startsWith("[") ? body.indexOf(']') + 1 : 0;
        lb.append(prefix, AnsiStyle.FG_WHITE, bg);
        lb.append(body.substring(0, shortcutEnd), shortcutColor, bg);
        lb.append(body.substring(shortcutEnd), AnsiStyle.FG_WHITE, bg);
        lb.appendSpaces(pillWidth - prefix.length() - body.length(), bg);
    }
}
