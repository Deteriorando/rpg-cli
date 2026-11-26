package dete.org.view.terminal.tui;

import dete.org.core.controller.Effect;
import dete.org.core.entity.Entity;
import dete.org.core.entity.entities.Enemy;
import dete.org.core.entity.entities.Player;
import dete.org.core.inventory.Inventory.InventorySlot;
import dete.org.core.inventory.Item;
import dete.org.core.inventory.ItemProperties;
import dete.org.view.terminal.tui.AnsiStyle.LineBuilder;
import dete.org.view.terminal.tui.AnsiStyle.Rgb;
import dete.org.view.terminal.tui.CombatTuiState.EventKind;
import dete.org.view.terminal.tui.CombatTuiState.HistoryEntry;
import dete.org.view.terminal.tui.CombatTuiState.Mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Renderizador ASCII puro da tela de combate fiel ao layout de referência,
 * com adaptação responsiva para terminais menores.
 */
public final class CombatTuiRenderer {

    private static final int MAX_FRAME_WIDTH = 118;
    private static final boolean UTF8_OUTPUT = isUtf8Output();

    private CombatTuiRenderer() {}

    private static boolean isUtf8Output() {
        return java.nio.charset.StandardCharsets.UTF_8.equals(System.out.charset());
    }

    private static char boxTL() { return UTF8_OUTPUT ? '┌' : '+'; }
    private static char boxTR() { return UTF8_OUTPUT ? '┐' : '+'; }
    private static char boxBL() { return UTF8_OUTPUT ? '└' : '+'; }
    private static char boxBR() { return UTF8_OUTPUT ? '┘' : '+'; }
    private static char boxH()  { return UTF8_OUTPUT ? '─' : '-'; }
    private static char boxV()  { return UTF8_OUTPUT ? '│' : '|'; }

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
        frameLines.add(buildOuterTopBorder(frameWidth));
        frameLines.add(buildHeaderRow(frameWidth, innerWidth, state.getRound()));

        if (looseSpacing) {
            frameLines.add(buildOuterBlankRow(frameWidth));
        }

        // Painel INIMIGO
        buildEnemyPanel(frameLines, frameWidth, innerWidth, state.getEnemy(), showPortrait, compactStats);

        if (looseSpacing) {
            frameLines.add(buildOuterBlankRow(frameWidth));
        }

        // Painéis HISTORICO + JOGADOR
        if (sideBySideMiddle) {
            buildMiddleRowSideBySide(frameLines, frameWidth, innerWidth, state, midPanelInnerRows);
        } else {
            buildMiddleStacked(frameLines, frameWidth, innerWidth, state, midPanelInnerRows);
        }

        if (looseSpacing) {
            frameLines.add(buildOuterBlankRow(frameWidth));
        }

        // Painel DECISAO
        buildDecisionPanel(frameLines, frameWidth, innerWidth, state, decisionInnerRows);

        frameLines.add(buildOuterBottomBorder(frameWidth));

        if (frameLines.size() > safeRows) {
            return renderUltraCompact(state, safeCols, safeRows, isTty);
        }
        return assembleScreen(frameLines, safeCols, safeRows, leftMargin, rightMargin, isTty);
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
            out.append(blankTermLine(termCols)).append(lineBreak);
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
                out.append(blankTermLine(termCols));
                if (i < bottomPad - 1) {
                    out.append(lineBreak);
                }
            }
        }

        return out.toString();
    }

    private static String blankTermLine(int width) {
        LineBuilder lb = new LineBuilder();
        lb.appendSpaces(width, AnsiStyle.BG_OUTER);
        return lb.finish();
    }

    private static String buildOuterTopBorder(int frameWidth) {
        LineBuilder lb = new LineBuilder();
        lb.append(boxTL() + AnsiStyle.repeat(boxH(), frameWidth - 2) + boxTR(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        return lb.finish();
    }

    private static String buildOuterBottomBorder(int frameWidth) {
        LineBuilder lb = new LineBuilder();
        lb.append(boxBL() + AnsiStyle.repeat(boxH(), frameWidth - 2) + boxBR(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        return lb.finish();
    }

    private static String buildOuterBlankRow(int frameWidth) {
        LineBuilder lb = new LineBuilder();
        lb.append(String.valueOf(boxV()), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        lb.appendSpaces(frameWidth - 2, AnsiStyle.BG_OUTER);
        lb.append(String.valueOf(boxV()), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        return lb.finish();
    }

    private static String buildHeaderRow(int frameWidth, int innerWidth, int round) {
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
        lb.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        lb.append(leftTitle, AnsiStyle.FG_CYAN, AnsiStyle.BG_OUTER);
        lb.appendSpaces(gap, AnsiStyle.BG_OUTER);
        lb.append(rightTitle, AnsiStyle.FG_WHITE, AnsiStyle.BG_OUTER);
        lb.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        return lb.finish();
    }

    private static void appendPanelTop(LineBuilder lb, String title, Rgb titleColor, int panelOuterWidth) {
        // "+- TITLE ----+"
        String prefix = "+- ";
        String titlePart = title;
        int fixed = prefix.length() + 1 + 1; // " " after title + "+"
        if (titlePart.length() + fixed > panelOuterWidth) {
            titlePart = AnsiStyle.truncatePlain(titlePart, Math.max(1, panelOuterWidth - fixed));
        }
        int dashes = Math.max(0, panelOuterWidth - prefix.length() - titlePart.length() - 2);
        lb.append("+- ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        lb.append(titlePart, titleColor, AnsiStyle.BG_OUTER);
        lb.append(" " + AnsiStyle.repeat('-', dashes) + "+", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
    }

    private static void appendPanelBottom(LineBuilder lb, int panelOuterWidth) {
        lb.append("+" + AnsiStyle.repeat('-', Math.max(0, panelOuterWidth - 2)) + "+",
                AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
    }

    private static void buildEnemyPanel(List<String> out,
                                        int frameWidth,
                                        int innerWidth,
                                        Enemy enemy,
                                        boolean showPortrait,
                                        boolean compactStats) {
        LineBuilder top = new LineBuilder();
        top.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        appendPanelTop(top, "INIMIGO", AnsiStyle.FG_GOLD, innerWidth);
        top.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        out.add(top.finish());

        int contentWidth = innerWidth - 4; // "| " + content + " |"
        String enemyName = safeName(enemy.getName(), "Inimigo");
        String enemyClass = enemy.getClasses().getName();
        int curHp = displayLife(enemy);
        int maxHp = (int) Math.round(enemy.getLife().getMaxValue());
        String hpText = curHp + " / " + maxHp;
        String dmgText = formatNumber(enemy.getDamage().getValue());
        String multText = "x" + formatNumber(enemy.getMultiplied().getValue());
        String effText = formatEffect(enemy);
        boolean hasEff = enemy.hasEffect();

        if (showPortrait) {
            int portraitArea = Math.max(CombatAsciiArt.WIDTH + 4, contentWidth * 46 / 100);
            int dividerCol = 1;
            int leftArea = contentWidth - dividerCol - portraitArea;
            int barInner = Math.max(12, Math.min(46, leftArea - 6));

            for (int r = 0; r < CombatAsciiArt.HEIGHT; r++) {
                LineBuilder left = new LineBuilder();
                switch (r) {
                    case 1 -> left.append("  " + AnsiStyle.truncatePlain(enemyName, leftArea - 3),
                            AnsiStyle.FG_GOLD, AnsiStyle.BG_ENEMY);
                    case 2 -> left.append("  " + AnsiStyle.truncatePlain(enemyClass, leftArea - 3),
                            AnsiStyle.FG_CORAL, AnsiStyle.BG_ENEMY);
                    case 4 -> {
                        int totalSpan = barInner + 2; // alinhado à largura da barra "[...]"
                        String label = "VIDA";
                        int gap = Math.max(1, totalSpan - label.length() - hpText.length());
                        left.append("  ", null, AnsiStyle.BG_ENEMY);
                        left.append(label, AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
                        left.appendSpaces(gap, AnsiStyle.BG_ENEMY);
                        left.append(hpText, AnsiStyle.FG_GOLD, AnsiStyle.BG_ENEMY);
                    }
                    case 5 -> {
                        left.append("  ", null, AnsiStyle.BG_ENEMY);
                        appendHpBar(left, enemy.getLife().getValue(), enemy.getLife().getMaxValue(),
                                barInner, AnsiStyle.FG_GOLD, AnsiStyle.BG_ENEMY);
                    }
                    case 7 -> appendEnemyStatsLabels(left, leftArea);
                    case 8 -> appendEnemyStatsValues(left, leftArea, dmgText, multText, effText, hasEff);
                    default -> {}
                }
                left.padTo(leftArea, AnsiStyle.BG_ENEMY);
                String leftStr = left.finish();

                LineBuilder midAndRight = new LineBuilder();
                if (r >= 1 && r <= 8) {
                    midAndRight.append("|", AnsiStyle.FG_BORDER, AnsiStyle.BG_ENEMY);
                } else {
                    midAndRight.appendSpaces(1, AnsiStyle.BG_ENEMY);
                }
                CombatAsciiArt.appendCenteredRow(midAndRight, r, portraitArea, AnsiStyle.BG_ENEMY);
                midAndRight.appendSpaces(1, AnsiStyle.BG_OUTER);
                midAndRight.append("|", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
                midAndRight.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);

                LineBuilder prefix = new LineBuilder();
                prefix.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
                prefix.append("|", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
                prefix.appendSpaces(1, AnsiStyle.BG_OUTER);
                out.add(prefix.finish() + leftStr + midAndRight.finish());
            }
        } else {
            int rows = compactStats ? 3 : 5;
            int barInner = Math.max(10, Math.min(44, contentWidth - 11 - hpText.length()));
            for (int r = 0; r < rows; r++) {
                LineBuilder body = new LineBuilder();
                if (compactStats) {
                    switch (r) {
                        case 0 -> {
                            body.append("  " + AnsiStyle.truncatePlain(enemyName, contentWidth - enemyClass.length() - 6),
                                    AnsiStyle.FG_GOLD, AnsiStyle.BG_ENEMY);
                            body.append(" / " + enemyClass, AnsiStyle.FG_CORAL, AnsiStyle.BG_ENEMY);
                        }
                        case 1 -> {
                            body.append("  VIDA  ", AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
                            appendHpBar(body, enemy.getLife().getValue(), enemy.getLife().getMaxValue(),
                                    barInner, AnsiStyle.FG_GOLD, AnsiStyle.BG_ENEMY);
                            body.append(" " + hpText, AnsiStyle.FG_GOLD, AnsiStyle.BG_ENEMY);
                        }
                        case 2 -> {
                            body.append("  DANO ", AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
                            body.append(dmgText, AnsiStyle.FG_SALMON, AnsiStyle.BG_ENEMY);
                            body.append("   MULT ", AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
                            body.append(multText, AnsiStyle.FG_PURPLE, AnsiStyle.BG_ENEMY);
                            body.append("   EFEITO ", AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
                            int rem = Math.max(4, contentWidth - body.visibleLength() - 1);
                            body.append(AnsiStyle.truncatePlain(effText, rem),
                                    hasEff ? AnsiStyle.FG_CYAN : AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
                        }
                        default -> {}
                    }
                } else {
                    switch (r) {
                        case 0 -> {
                            body.append("  " + AnsiStyle.truncatePlain(enemyName, contentWidth - enemyClass.length() - 6),
                                    AnsiStyle.FG_GOLD, AnsiStyle.BG_ENEMY);
                            body.append("  (" + enemyClass + ")", AnsiStyle.FG_CORAL, AnsiStyle.BG_ENEMY);
                        }
                        case 1 -> {
                            int span = Math.max(20, Math.min(54, contentWidth - 4));
                            String lbl = "VIDA";
                            int gap = Math.max(1, span - lbl.length() - hpText.length());
                            body.append("  ", null, AnsiStyle.BG_ENEMY);
                            body.append(lbl, AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
                            body.appendSpaces(gap, AnsiStyle.BG_ENEMY);
                            body.append(hpText, AnsiStyle.FG_GOLD, AnsiStyle.BG_ENEMY);
                        }
                        case 2 -> {
                            int span = Math.max(18, Math.min(52, contentWidth - 6));
                            body.append("  ", null, AnsiStyle.BG_ENEMY);
                            appendHpBar(body, enemy.getLife().getValue(), enemy.getLife().getMaxValue(),
                                    span, AnsiStyle.FG_GOLD, AnsiStyle.BG_ENEMY);
                        }
                        case 4 -> {
                            body.append("  DANO ", AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
                            body.append(dmgText, AnsiStyle.FG_SALMON, AnsiStyle.BG_ENEMY);
                            body.append("    MULTIPLICADOR ", AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
                            body.append(multText, AnsiStyle.FG_PURPLE, AnsiStyle.BG_ENEMY);
                            body.append("    EFEITO ", AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
                            int rem = Math.max(4, contentWidth - body.visibleLength() - 1);
                            body.append(AnsiStyle.truncatePlain(effText, rem),
                                    hasEff ? AnsiStyle.FG_CYAN : AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
                        }
                        default -> {}
                    }
                }
                body.padTo(contentWidth, AnsiStyle.BG_ENEMY);
                out.add(wrapFullPanelContent(body.finish()));
            }
        }

        LineBuilder bot = new LineBuilder();
        bot.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        appendPanelBottom(bot, innerWidth);
        bot.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        out.add(bot.finish());
    }

    private static void appendEnemyStatsLabels(LineBuilder left, int leftArea) {
        int col1 = 2;
        int col2 = Math.max(12, Math.min(18, leftArea / 3));
        int col3 = Math.max(col2 + 16, Math.min(38, (leftArea * 2) / 3));
        left.appendSpaces(col1, AnsiStyle.BG_ENEMY);
        left.append("DANO", AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
        left.appendSpaces(Math.max(1, col2 - left.visibleLength()), AnsiStyle.BG_ENEMY);
        left.append("MULTIPLICADOR", AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
        left.appendSpaces(Math.max(1, col3 - left.visibleLength()), AnsiStyle.BG_ENEMY);
        left.append("EFEITO", AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
    }

    private static void appendEnemyStatsValues(LineBuilder left, int leftArea,
                                               String dmgText, String multText, String effText, boolean hasEff) {
        int col1 = 2;
        int col2 = Math.max(12, Math.min(18, leftArea / 3));
        int col3 = Math.max(col2 + 16, Math.min(38, (leftArea * 2) / 3));
        left.appendSpaces(col1, AnsiStyle.BG_ENEMY);
        left.append(dmgText, AnsiStyle.FG_SALMON, AnsiStyle.BG_ENEMY);
        left.appendSpaces(Math.max(1, col2 - left.visibleLength()), AnsiStyle.BG_ENEMY);
        left.append(multText, AnsiStyle.FG_PURPLE, AnsiStyle.BG_ENEMY);
        left.appendSpaces(Math.max(1, col3 - left.visibleLength()), AnsiStyle.BG_ENEMY);
        int effBudget = Math.max(4, leftArea - col3 - 1);
        left.append(AnsiStyle.truncatePlain(effText, effBudget),
                hasEff ? AnsiStyle.FG_CYAN : AnsiStyle.FG_MUTED, AnsiStyle.BG_ENEMY);
    }

    private static void buildMiddleRowSideBySide(List<String> out,
                                                 int frameWidth,
                                                 int innerWidth,
                                                 CombatTuiState state,
                                                 int rows) {
        int gap = 2;
        int histOuter = Math.min((int) Math.round((innerWidth - gap) * 0.59), innerWidth - gap - 36);
        int playerOuter = innerWidth - gap - histOuter;

        LineBuilder top = new LineBuilder();
        top.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        appendPanelTop(top, "HISTORICO", AnsiStyle.FG_MUTED, histOuter);
        top.appendSpaces(gap, AnsiStyle.BG_OUTER);
        appendPanelTop(top, "JOGADOR", AnsiStyle.FG_CYAN, playerOuter);
        top.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        out.add(top.finish());

        int histContent = histOuter - 4;
        int playerContent = playerOuter - 4;

        List<String> histRows = renderHistoryRows(state, histContent, rows);
        List<String> playerRows = renderPlayerRows(state.getPlayer(), playerContent, rows);

        for (int r = 0; r < rows; r++) {
            LineBuilder prefix = new LineBuilder();
            prefix.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
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
            suffix.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);

            out.add(prefix.finish() + histRows.get(r) + mid.finish() + playerRows.get(r) + suffix.finish());
        }

        LineBuilder bot = new LineBuilder();
        bot.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        appendPanelBottom(bot, histOuter);
        bot.appendSpaces(gap, AnsiStyle.BG_OUTER);
        appendPanelBottom(bot, playerOuter);
        bot.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        out.add(bot.finish());
    }

    private static void buildMiddleStacked(List<String> out,
                                           int frameWidth,
                                           int innerWidth,
                                           CombatTuiState state,
                                           int rows) {
        int contentWidth = innerWidth - 4;
        // JOGADOR primeiro em telas estreitas para status sempre visível
        LineBuilder pTop = new LineBuilder();
        pTop.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        appendPanelTop(pTop, "JOGADOR", AnsiStyle.FG_CYAN, innerWidth);
        pTop.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        out.add(pTop.finish());

        for (String pr : renderPlayerRows(state.getPlayer(), contentWidth, 3)) {
            out.add(wrapFullPanelContent(pr));
        }

        LineBuilder pBot = new LineBuilder();
        pBot.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        appendPanelBottom(pBot, innerWidth);
        pBot.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        out.add(pBot.finish());

        // HISTORICO abaixo
        LineBuilder hTop = new LineBuilder();
        hTop.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        appendPanelTop(hTop, "HISTORICO", AnsiStyle.FG_MUTED, innerWidth);
        hTop.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        out.add(hTop.finish());

        for (String hr : renderHistoryRows(state, contentWidth, rows)) {
            out.add(wrapFullPanelContent(hr));
        }

        LineBuilder hBot = new LineBuilder();
        hBot.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        appendPanelBottom(hBot, innerWidth);
        hBot.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        out.add(hBot.finish());
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
            LineBuilder blank = new LineBuilder();
            blank.padTo(width, AnsiStyle.BG_PANEL);
            lines.add(blank.finish());
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
            LineBuilder pad = new LineBuilder();
            pad.padTo(width, AnsiStyle.BG_PANEL);
            lines.add(pad.finish());
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

        int rowIdx = 0;
        if (totalRows >= 6) {
            LineBuilder b0 = new LineBuilder();
            b0.padTo(width, AnsiStyle.BG_PLAYER);
            lines.add(b0.finish());
            rowIdx++;
        }

        // Nome / Classe
        LineBuilder nameRow = new LineBuilder();
        nameRow.append("  " + AnsiStyle.truncatePlain(title, Math.max(6, width - 3)),
                AnsiStyle.FG_CYAN, AnsiStyle.BG_PLAYER);
        nameRow.padTo(width, AnsiStyle.BG_PLAYER);
        lines.add(nameRow.finish());
        rowIdx++;

        if (totalRows >= 5) {
            LineBuilder b = new LineBuilder();
            b.padTo(width, AnsiStyle.BG_PLAYER);
            lines.add(b.finish());
            rowIdx++;
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
        rowIdx++;

        if (totalRows >= 6) {
            LineBuilder b = new LineBuilder();
            b.padTo(width, AnsiStyle.BG_PLAYER);
            lines.add(b.finish());
            rowIdx++;
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
        rowIdx++;

        while (lines.size() < totalRows) {
            LineBuilder pad = new LineBuilder();
            pad.padTo(width, AnsiStyle.BG_PLAYER);
            lines.add(pad.finish());
        }
        return lines;
    }

    private static void buildDecisionPanel(List<String> out,
                                           int frameWidth,
                                           int innerWidth,
                                           CombatTuiState state,
                                           int rows) {
        LineBuilder top = new LineBuilder();
        top.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        appendPanelTop(top, "DECISAO", AnsiStyle.FG_CYAN, innerWidth);
        top.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        out.add(top.finish());

        int contentWidth = innerWidth - 4;
        Mode mode = state.getMode();
        List<String> bodyRows = new ArrayList<>(rows);

        switch (mode) {
            case ACTION_SELECT -> renderDecisionActions(bodyRows, state, contentWidth, rows);
            case INVENTORY_SELECT -> renderDecisionInventory(bodyRows, state, contentWidth, rows);
            case ENEMY_TURN -> renderDecisionEnemyTurn(bodyRows, state, contentWidth, rows);
            case VICTORY, DEFEAT -> renderDecisionEnd(bodyRows, state, contentWidth, rows);
        }

        for (String r : bodyRows) {
            out.add(wrapFullPanelContent(r));
        }

        LineBuilder bot = new LineBuilder();
        bot.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        appendPanelBottom(bot, innerWidth);
        bot.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        out.add(bot.finish());
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
            LineBuilder blank = new LineBuilder();
            blank.padTo(width, AnsiStyle.BG_PANEL);
            lines.add(blank.finish());
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
            LineBuilder blank = new LineBuilder();
            blank.padTo(width, AnsiStyle.BG_PANEL);
            lines.add(blank.finish());
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
            LineBuilder blank = new LineBuilder();
            blank.padTo(width, AnsiStyle.BG_PANEL);
            lines.add(blank.finish());
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
            LineBuilder blank = new LineBuilder();
            blank.padTo(width, AnsiStyle.BG_PANEL);
            lines.add(blank.finish());
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
        row0.append("  Turno de " + safeName(state.getEnemy().getName(), "Inimigo") + "...",
                AnsiStyle.FG_GOLD, AnsiStyle.BG_PANEL);
        row0.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(row0.finish());

        if (totalRows >= 5) {
            LineBuilder blank = new LineBuilder();
            blank.padTo(width, AnsiStyle.BG_PANEL);
            lines.add(blank.finish());
        }

        LineBuilder row1 = new LineBuilder();
        row1.append("  Aguarde a acao do inimigo...", AnsiStyle.FG_MUTED, AnsiStyle.BG_PANEL);
        row1.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(row1.finish());

        padEmptyPanelLines(lines, totalRows, width);
    }

    private static List<String> inventoryLabels(List<InventorySlot> slots) {
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
        String enemyName = safeName(state.getEnemy().getName(), "Inimigo");
        String banner = victory
                ? String.format("  VITORIA! Voce derrotou %s em %02d rodada(s)!", enemyName, state.getFinalRounds())
                : String.format("  DERROTA! Voce foi derrotado por %s na rodada %02d...", enemyName, state.getFinalRounds());

        LineBuilder row0 = new LineBuilder();
        row0.append(AnsiStyle.truncatePlain(banner, width - 1),
                victory ? AnsiStyle.FG_GREEN : AnsiStyle.FG_SALMON, AnsiStyle.BG_PANEL, true);
        row0.padTo(width, AnsiStyle.BG_PANEL);
        lines.add(row0.finish());

        if (totalRows >= 5) {
            LineBuilder blank = new LineBuilder();
            blank.padTo(width, AnsiStyle.BG_PANEL);
            lines.add(blank.finish());
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
            LineBuilder blank = new LineBuilder();
            blank.padTo(width, AnsiStyle.BG_PANEL);
            lines.add(blank.finish());
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
            LineBuilder pad = new LineBuilder();
            pad.padTo(width, AnsiStyle.BG_PANEL);
            lines.add(pad.finish());
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

    private static void appendHpBar(LineBuilder lb,
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

    private static String wrapFullPanelContent(String content) {
        LineBuilder prefix = new LineBuilder();
        prefix.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        prefix.append("|", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        prefix.appendSpaces(1, AnsiStyle.BG_OUTER);

        LineBuilder suffix = new LineBuilder();
        suffix.appendSpaces(1, AnsiStyle.BG_OUTER);
        suffix.append("|", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        suffix.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);

        return prefix.finish() + content + suffix.finish();
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
                String.format("INI: %s %d/%d", safeName(enemy.getName(), "Inimigo"),
                        displayLife(enemy), (int) Math.round(enemy.getLife().getMaxValue())), cols),
                AnsiStyle.FG_GOLD, AnsiStyle.BG_ENEMY);
        en.padTo(cols, AnsiStyle.BG_ENEMY);
        lines.add(en.finish());

        LineBuilder pl = new LineBuilder();
        pl.append(AnsiStyle.truncatePlain(
                String.format("JOG: %s %d/%d", safeName(player.getName(), "Jogador"),
                        displayLife(player), (int) Math.round(player.getLife().getMaxValue())), cols),
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
                yield "> " + inventoryLabels(slots).get(state.getSelectedItem());
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

    private static int displayLife(Entity entity) {
        double v = entity.getLife().getValue();
        if (v > 0 && v < 1) {
            return 1;
        }
        return (int) Math.round(v);
    }

    private static String safeName(String name, String fallback) {
        return (name == null || name.isBlank()) ? fallback : name;
    }

    private static String formatNumber(double value) {
        if (Math.abs(value - Math.rint(value)) < 1e-6) {
            return String.valueOf((long) Math.rint(value));
        }
        return String.format(Locale.US, "%.1f", value);
    }

    private static String formatEffect(Entity entity) {
        if (!entity.hasEffect()) {
            return "Nenhum";
        }
        Effect eff = entity.getEffect();
        return eff.name() != null ? eff.name() : "Ativo";
    }
}
