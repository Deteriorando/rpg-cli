package dete.org.view.terminal.tui;

import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.inventory.Item;
import dete.org.view.terminal.tui.AnsiStyle.LineBuilder;
import dete.org.view.terminal.tui.CombatTuiState.HistoryEntry;
import java.util.ArrayList;
import java.util.List;

public final class CombatTuiRenderer {
    private CombatTuiRenderer() {}

    private static final int MAX_FRAME_WIDTH = 118;

    public static String render(CombatTuiState state, int termCols, int termRows, boolean isTty) {
        int safeCols = Math.max(1, termCols);
        int safeRows = Math.max(1, termRows);

        if (safeCols < 64 || safeRows < 18) {
            return renderUltraCompact(state, safeCols, safeRows, isTty);
        }

        int frameWidth;
        int leftMargin;
        if (safeCols >= 80) {
            frameWidth = Math.min(safeCols - 2, MAX_FRAME_WIDTH);
            leftMargin = (safeCols - frameWidth) / 2;
        } else {
            frameWidth = safeCols;
            leftMargin = 0;
        }
        int rightMargin = Math.max(0, safeCols - frameWidth - leftMargin);

        int innerWidth = frameWidth - 4; // "│ " + inner + " │"
        boolean sideBySideMiddle = innerWidth >= 70;
        boolean showPortrait = (innerWidth - 4) >= 82 && safeRows >= 29;
        boolean looseSpacing = safeRows >= 34;
        boolean compactStats = safeRows < 26;

        int midPanelInnerRows;
        if (!sideBySideMiddle) {
            midPanelInnerRows = safeRows >= 32 ? 5 : safeRows >= 26 ? 4 : 3;
        } else if (safeRows >= 34) {
            midPanelInnerRows = 7;
        } else if (safeRows >= 26) {
            midPanelInnerRows = 5;
        } else {
            midPanelInnerRows = 4;
        }

        int decisionInnerRows = safeRows >= 26 ? 5 : 3;

        List<String> frameLines = new ArrayList<>(38);
        frameLines.add(CombatPanelFrame.buildOuterTopBorder(frameWidth));
        frameLines.add(buildHeaderRow(innerWidth, state.getRound()));

        if (looseSpacing) {
            frameLines.add(CombatPanelFrame.buildOuterBlankRow(frameWidth));
        }

        // Painel INIMIGO
        CombatEnemyPanel.buildEnemyPanel(frameLines, innerWidth, state.getEnemy(), showPortrait, compactStats);

        if (looseSpacing) {
            frameLines.add(CombatPanelFrame.buildOuterBlankRow(frameWidth));
        }

        // Painéis HISTORICO + JOGADOR
        if (sideBySideMiddle) {
            CombatStatusPanels.buildMiddleRowSideBySide(frameLines, innerWidth, state, midPanelInnerRows);
        } else {
            CombatStatusPanels.buildMiddleStacked(frameLines, innerWidth, state, midPanelInnerRows);
        }

        if (looseSpacing) {
            frameLines.add(CombatPanelFrame.buildOuterBlankRow(frameWidth));
        }

        // Painel DECISAO
        CombatDecisionPanel.buildDecisionPanel(frameLines, innerWidth, state, decisionInnerRows);

        frameLines.add(CombatPanelFrame.buildOuterBottomBorder(frameWidth));

        if (frameLines.size() > safeRows) {
            return renderUltraCompact(state, safeCols, safeRows, isTty);
        }
        return assembleScreen(frameLines, safeCols, safeRows, leftMargin, rightMargin, isTty);
    }

    /**
     * Mantém a escolha de combate visível quando não há espaço para os painéis.
     */
    private static String renderUltraCompact(CombatTuiState state, int cols, int rows, boolean isTty) {
        List<String> lines = new ArrayList<>();
        Enemy enemy = state.getEnemy();
        Player player = state.getPlayer();

        LineBuilder hdr = new LineBuilder();
        hdr.append(AnsiStyle.truncatePlain(String.format("RPG CLI | RODADA %02d", state.getRound()), cols),
                AnsiStyle.FG_CYAN, AnsiStyle.BG_OUTER);
        hdr.padTo(cols, AnsiStyle.BG_OUTER);
        lines.add(hdr.finish());

        LineBuilder en = new LineBuilder();
        en.append(AnsiStyle.truncatePlain(
                String.format("INI: %s %d/%d", CombatStatusPanels.safeName(enemy.getName(), "Inimigo"),
                        CombatStatusPanels.displayLife(enemy), (int) Math.round(enemy.getLife().getMaxValue())), cols),
                AnsiStyle.FG_GOLD, AnsiStyle.BG_ENEMY);
        en.padTo(cols, AnsiStyle.BG_ENEMY);
        lines.add(en.finish());

        LineBuilder pl = new LineBuilder();
        pl.append(AnsiStyle.truncatePlain(
                String.format("JOG: %s %d/%d", CombatStatusPanels.safeName(player.getName(), "Jogador"),
                        CombatStatusPanels.displayLife(player), (int) Math.round(player.getLife().getMaxValue())), cols),
                AnsiStyle.FG_GREEN, AnsiStyle.BG_PANEL);
        pl.padTo(cols, AnsiStyle.BG_PANEL);
        lines.add(pl.finish());

        List<HistoryEntry> hist = state.getHistorySnapshot();
        if (!hist.isEmpty()) {
            LineBuilder h = new LineBuilder();
            HistoryEntry top = hist.get(0);
            h.append(AnsiStyle.truncatePlain(String.format("%02d %s", top.round(), top.message()), cols),
                    AnsiStyle.FG_WHITE, AnsiStyle.BG_HIST_FOCUS);
            h.padTo(cols, AnsiStyle.BG_HIST_FOCUS);
            lines.add(h.finish());
        }

        LineBuilder act = new LineBuilder();
        String prompt = switch (state.getMode()) {
            case ACTION_SELECT -> (state.getSelectedAction() + 1) + "> 1 Atacar 2 Defender 3 Item";
            case INVENTORY_SELECT -> {
                var slots = player.getInventorySlots();
                state.clampItemSelection(slots.size());
                yield "> " + CombatDecisionPanel.inventoryLabels(slots).get(state.getSelectedItem());
            }
            case ENEMY_TURN -> "Turno inimigo...";
            case VICTORY -> "VITORIA! [Enter]";
            case DEFEAT -> "DERROTA! [Enter]";
        };
        act.append(AnsiStyle.truncatePlain(prompt, cols), AnsiStyle.FG_CYAN, AnsiStyle.BG_PANEL);
        act.padTo(cols, AnsiStyle.BG_PANEL);
        int details = Math.min(lines.size(), rows - 1);
        lines.subList(details, lines.size()).clear();
        lines.add(act.finish());

        return assembleScreen(lines, cols, rows, 0, 0, isTty);
    }

    private static String assembleScreen(List<String> frameLines,
                                         int termCols,
                                         int termRows,
                                         int leftMargin,
                                         int rightMargin,
                                         boolean isTty) {
        StringBuilder out = new StringBuilder(frameLines.size() * (termCols + 32));
        String lineBreak = isTty ? "\r\n" : System.lineSeparator();

        int topPad = 0;
        if (isTty && termRows > frameLines.size()) {
            topPad = Math.min(2, (termRows - frameLines.size()) / 2);
        }
        int bottomPad = isTty ? Math.max(0, termRows - topPad - frameLines.size()) : 0;

        for (int i = 0; i < topPad; i++) {
            out.append(CombatPanelFrame.blankLine(termCols, AnsiStyle.BG_OUTER)).append(lineBreak);
        }

        for (int i = 0; i < frameLines.size(); i++) {
            if (isTty || leftMargin > 0 || rightMargin > 0) {
                LineBuilder row = new LineBuilder();
                row.appendSpaces(leftMargin, AnsiStyle.BG_OUTER);
                out.append(row.finish());
                out.append(frameLines.get(i));
                if (isTty && rightMargin > 0) {
                    LineBuilder tail = new LineBuilder();
                    tail.appendSpaces(rightMargin, AnsiStyle.BG_OUTER);
                    out.append(tail.finish());
                }
            } else {
                out.append(frameLines.get(i));
            }
            if (i < frameLines.size() - 1 || bottomPad > 0) {
                out.append(lineBreak);
            }
        }

        if (isTty) {
            for (int i = 0; i < bottomPad; i++) {
                out.append(CombatPanelFrame.blankLine(termCols, AnsiStyle.BG_OUTER));
                if (i < bottomPad - 1) {
                    out.append(lineBreak);
                }
            }
        }

        return out.toString();
    }

    private static String buildHeaderRow(int innerWidth, int round) {
        String leftTitle = "RPG CLI";
        String rightTitle = String.format("COMBATE / RODADA %02d", round);
        if (leftTitle.length() + 2 + rightTitle.length() > innerWidth) {
            rightTitle = String.format("RODADA %02d", round);
        }
        if (leftTitle.length() + 1 + rightTitle.length() > innerWidth) {
            rightTitle = String.format("R%02d", round);
        }
        int gap = Math.max(1, innerWidth - leftTitle.length() - rightTitle.length());

        LineBuilder lb = new LineBuilder();
        lb.append(CombatPanelFrame.boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        lb.append(leftTitle, AnsiStyle.FG_CYAN, AnsiStyle.BG_OUTER);
        lb.appendSpaces(gap, AnsiStyle.BG_OUTER);
        lb.append(rightTitle, AnsiStyle.FG_WHITE, AnsiStyle.BG_OUTER);
        lb.append(" " + CombatPanelFrame.boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        return lb.finish();
    }
}
