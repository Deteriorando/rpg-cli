package dete.org.view.terminal.tui;

import dete.org.core.entity.entities.Enemy;
import dete.org.view.terminal.tui.AnsiStyle.LineBuilder;
import java.util.List;

final class CombatEnemyPanel {
    private CombatEnemyPanel() {}

    static void buildEnemyPanel(List<String> out,
                                        int innerWidth,
                                        Enemy enemy,
                                        boolean showPortrait,
                                        boolean compactStats) {
        out.add(CombatPanelFrame.fullPanelTop("INIMIGO", AnsiStyle.FG_GOLD, innerWidth));

        int contentWidth = innerWidth - 4; // "| " + content + " |"
        String enemyName = CombatStatusPanels.safeName(enemy.getName(), "Inimigo");
        String enemyClass = enemy.getClasses().getName();
        int curHp = CombatStatusPanels.displayLife(enemy);
        int maxHp = (int) Math.round(enemy.getLife().getMaxValue());
        String hpText = curHp + " / " + maxHp;
        String dmgText = CombatStatusPanels.formatNumber(enemy.getDamage().getValue());
        String multText = "x" + CombatStatusPanels.formatNumber(enemy.getMultiplied().getValue());
        String effText = CombatStatusPanels.formatEffect(enemy);
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
                        CombatStatusPanels.appendHpBar(left, enemy.getLife().getValue(), enemy.getLife().getMaxValue(),
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
                midAndRight.append(" " + CombatPanelFrame.boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);

                LineBuilder prefix = new LineBuilder();
                prefix.append(CombatPanelFrame.boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
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
                            CombatStatusPanels.appendHpBar(body, enemy.getLife().getValue(), enemy.getLife().getMaxValue(),
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
                            CombatStatusPanels.appendHpBar(body, enemy.getLife().getValue(), enemy.getLife().getMaxValue(),
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
                out.add(CombatPanelFrame.wrapFullPanelContent(body.finish()));
            }
        }

        out.add(CombatPanelFrame.fullPanelBottom(innerWidth));
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
}
