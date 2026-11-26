package dete.org.view.terminal.tui;

import dete.org.core.controller.Effect;
import dete.org.core.entity.Entity;
import dete.org.core.entity.entities.Player;
import dete.org.view.terminal.tui.AnsiStyle.LineBuilder;
import dete.org.view.terminal.tui.AnsiStyle.Rgb;
import dete.org.view.terminal.tui.CombatTuiState.EventKind;
import dete.org.view.terminal.tui.CombatTuiState.HistoryEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class CombatStatusPanels {
    private CombatStatusPanels() {}

    static void buildMiddleRowSideBySide(List<String> out,
                                                 int innerWidth,
                                                 CombatTuiState state,
                                                 int rows) {
        int gap = 2;
        int histOuter = Math.min((int) Math.round((innerWidth - gap) * 0.59), innerWidth - gap - 36);
        int playerOuter = innerWidth - gap - histOuter;

        LineBuilder top = new LineBuilder();
        top.append(CombatPanelFrame.boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        CombatPanelFrame.appendPanelTop(top, "HISTORICO", AnsiStyle.FG_MUTED, histOuter);
        top.appendSpaces(gap, AnsiStyle.BG_OUTER);
        CombatPanelFrame.appendPanelTop(top, "JOGADOR", AnsiStyle.FG_CYAN, playerOuter);
        top.append(" " + CombatPanelFrame.boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        out.add(top.finish());

        int histContent = histOuter - 4;
        int playerContent = playerOuter - 4;

        List<String> histRows = renderHistoryRows(state, histContent, rows);
        List<String> playerRows = renderPlayerRows(state.getPlayer(), playerContent, rows);

        for (int r = 0; r < rows; r++) {
            LineBuilder prefix = new LineBuilder();
            prefix.append(CombatPanelFrame.boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
            prefix.append("|", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
            prefix.appendSpaces(1, AnsiStyle.BG_OUTER);

            LineBuilder mid = new LineBuilder();
            mid.appendSpaces(1, AnsiStyle.BG_OUTER);
            mid.append("|", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
            mid.appendSpaces(gap, AnsiStyle.BG_OUTER);
            mid.append("|", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
            mid.appendSpaces(1, AnsiStyle.BG_OUTER);

            LineBuilder suffix = new LineBuilder();
            suffix.appendSpaces(1, AnsiStyle.BG_OUTER);
            suffix.append("|", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
            suffix.append(" " + CombatPanelFrame.boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);

            out.add(prefix.finish() + histRows.get(r) + mid.finish() + playerRows.get(r) + suffix.finish());
        }

        LineBuilder bot = new LineBuilder();
        bot.append(CombatPanelFrame.boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        CombatPanelFrame.appendPanelBottom(bot, histOuter);
        bot.appendSpaces(gap, AnsiStyle.BG_OUTER);
        CombatPanelFrame.appendPanelBottom(bot, playerOuter);
        bot.append(" " + CombatPanelFrame.boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        out.add(bot.finish());
    }

    static void buildMiddleStacked(List<String> out,
                                           int innerWidth,
                                           CombatTuiState state,
                                           int rows) {
        int contentWidth = innerWidth - 4;
        CombatPanelFrame.appendFullPanel(out, "JOGADOR", AnsiStyle.FG_CYAN, innerWidth,
                renderPlayerRows(state.getPlayer(), contentWidth, 3));
        CombatPanelFrame.appendFullPanel(out, "HISTORICO", AnsiStyle.FG_MUTED, innerWidth,
                renderHistoryRows(state, contentWidth, rows));
    }

    private static List<String> renderHistoryRows(CombatTuiState state, int width, int totalRows) {
        List<String> lines = new ArrayList<>(totalRows);
        List<HistoryEntry> entries = state.getHistorySnapshot();

        // Linha 0: cabeçalho "ROD.  EVENTO"
        LineBuilder hdr = new LineBuilder();
        hdr.append("  ROD.  EVENTO", AnsiStyle.FG_MUTED, AnsiStyle.BG_PANEL);
        hdr.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(hdr.finish());

        int startEventRow = 1;
        if (totalRows >= 6) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PANEL));
            startEventRow = 2;
        }

        int maxVisibleEvents = Math.max(1, totalRows - startEventRow - (totalRows >= 7 ? 1 : 0));
        if (entries.isEmpty()) {
            LineBuilder emptyMsg = new LineBuilder();
            String msg = AnsiStyle.truncatePlain("Combate iniciado contra "
                    + safeName(state.getEnemy().getName(), "Inimigo") + ".", Math.max(8, width - 9));
            emptyMsg.append("  --    ", AnsiStyle.FG_DIM, AnsiStyle.BG_PANEL);
            emptyMsg.append(msg, AnsiStyle.FG_MUTED, AnsiStyle.BG_PANEL);
            emptyMsg.padTo(width, AnsiStyle.BG_PANEL);
            lines.add(emptyMsg.finish());
        } else {
            int count = Math.min(maxVisibleEvents, entries.size());
            for (int i = 0; i < count; i++) {
                HistoryEntry e = entries.get(i);
                boolean isLatest = (i == 0);
                boolean isAlertRow = isLatest && (e.kind() == EventKind.CRITICAL_ENEMY);
                Rgb rowBg = isAlertRow ? AnsiStyle.BG_HIST_ALERT : AnsiStyle.BG_PANEL;

                Rgb roundFg = isAlertRow ? AnsiStyle.FG_MUTED : AnsiStyle.FG_DIM;
                Rgb msgFg = switch (e.kind()) {
                    case CRITICAL_ENEMY -> AnsiStyle.FG_SALMON;
                    case PLAYER_CRITICAL -> AnsiStyle.FG_GOLD;
                    case DEFENSE -> AnsiStyle.FG_CYAN;
                    case HEAL -> AnsiStyle.FG_GREEN;
                    default -> AnsiStyle.FG_WHITE;
                };

                LineBuilder row = new LineBuilder();
                if (isLatest) {
                    row.append(" ", null, AnsiStyle.BG_PANEL);
                    row.append(" " + String.format("%02d", e.round()) + "    ", roundFg, rowBg);
                    int msgBudget = width - row.visibleLength() - 1;
                    row.append(AnsiStyle.truncatePlain(e.message(), msgBudget), msgFg, rowBg);
                    row.padTo(Math.max(1, width - 1), rowBg);
                    row.padTo(width, AnsiStyle.BG_PANEL);
                } else {
                    row.append("  " + String.format("%02d", e.round()) + "    ", roundFg, AnsiStyle.BG_PANEL);
                    int msgBudget = width - row.visibleLength();
                    row.append(AnsiStyle.truncatePlain(e.message(), msgBudget), msgFg, AnsiStyle.BG_PANEL);
                    row.padTo(width, AnsiStyle.BG_PANEL);
                }
                lines.add(row.finish());
            }
        }

        while (lines.size() < totalRows) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PANEL));
        }
        return lines;
    }

    private static List<String> renderPlayerRows(Player player, int width, int totalRows) {
        List<String> lines = new ArrayList<>(totalRows);
        String pName = safeName(player.getName(), "Jogador");
        String pClass = player.getClasses().getName();
        String title = pName + " / " + pClass;
        String hpText = displayLife(player) + " / " + (int) Math.round(player.getLife().getMaxValue());
        String dmgText = formatNumber(player.getDamage().getValue());
        String multText = "x" + formatNumber(player.getMultiplied().getValue());
        String effText = formatEffect(player);
        boolean hasEff = player.hasEffect();

        if (totalRows >= 6) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PLAYER));
        }

        // Nome / Classe
        LineBuilder nameRow = new LineBuilder();
        nameRow.append("  " + AnsiStyle.truncatePlain(title, Math.max(6, width - 3)),
                AnsiStyle.FG_CYAN, AnsiStyle.BG_PLAYER);
        nameRow.padTo(width, AnsiStyle.BG_PLAYER);
        lines.add(nameRow.finish());

        if (totalRows >= 5) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PLAYER));
        }

        // VIDA bar
        LineBuilder lifeRow = new LineBuilder();
        lifeRow.append("  VIDA  ", AnsiStyle.FG_MUTED, AnsiStyle.BG_PLAYER);
        int fixedOverhead = 2 + 6 + 2 + 1 + hpText.length() + 2; // "  VIDA  " + "[]" + " " + hpText + margin
        int barInner = Math.max(1, Math.min(20, width - fixedOverhead));
        appendHpBar(lifeRow, player.getLife().getValue(), player.getLife().getMaxValue(),
                barInner, AnsiStyle.FG_GREEN, AnsiStyle.BG_PLAYER);
        lifeRow.append(" " + hpText, AnsiStyle.FG_WHITE, AnsiStyle.BG_PLAYER);
        lifeRow.padTo(width, AnsiStyle.BG_PLAYER);
        lines.add(lifeRow.finish());

        if (totalRows >= 6) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PLAYER));
        }

        // DANO / MULT / EFEITO
        LineBuilder statsRow = new LineBuilder();
        statsRow.append("  DANO ", AnsiStyle.FG_MUTED, AnsiStyle.BG_PLAYER);
        statsRow.append(dmgText, AnsiStyle.FG_WHITE, AnsiStyle.BG_PLAYER);
        statsRow.append(width < 37 ? " MULT " : "   MULT ", AnsiStyle.FG_MUTED, AnsiStyle.BG_PLAYER);
        statsRow.append(multText, AnsiStyle.FG_PURPLE, AnsiStyle.BG_PLAYER);
        statsRow.append(width < 37 ? " EFEITO " : "   EFEITO ", AnsiStyle.FG_MUTED, AnsiStyle.BG_PLAYER);
        int rem = width - statsRow.visibleLength();
        statsRow.append(AnsiStyle.truncatePlain(effText, rem),
                hasEff ? AnsiStyle.FG_CYAN : AnsiStyle.FG_MUTED, AnsiStyle.BG_PLAYER);
        statsRow.padTo(width, AnsiStyle.BG_PLAYER);
        lines.add(statsRow.finish());

        while (lines.size() < totalRows) {
            lines.add(CombatPanelFrame.blankLine(width, AnsiStyle.BG_PLAYER));
        }
        return lines;
    }

    static void appendHpBar(LineBuilder lb,
                                    double curValue,
                                    double maxValue,
                                    int barWidth,
                                    Rgb fillColor,
                                    Rgb bg) {
        double safeMax = Math.max(1.0, maxValue);
        double ratio = Math.max(0.0, Math.min(1.0, curValue / safeMax));
        int filled = (int) Math.round(ratio * barWidth);
        if (curValue > 0 && filled == 0) {
            filled = 1;
        }
        int empty = Math.max(0, barWidth - filled);

        lb.append("[", AnsiStyle.FG_BORDER, bg);
        lb.append(AnsiStyle.repeat('#', filled), fillColor, bg);
        lb.append(AnsiStyle.repeat('-', empty), AnsiStyle.FG_DIM, bg);
        lb.append("]", AnsiStyle.FG_BORDER, bg);
    }

    static int displayLife(Entity entity) {
        double v = entity.getLife().getValue();
        if (v > 0 && v < 1) {
            return 1;
        }
        return (int) Math.round(v);
    }

    static String safeName(String name, String fallback) {
        return (name == null || name.isBlank()) ? fallback : name;
    }

    static String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-6) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.US, "%.1f", value);
    }

    static String formatEffect(Entity entity) {
        if (!entity.hasEffect()) {
            return "Nenhum";
        }
        Effect eff = entity.getEffect();
        return eff.name() != null ? eff.name() : "Ativo";
    }
}
