package dete.org.view.terminal.tui;

import dete.org.view.terminal.tui.AnsiStyle.LineBuilder;
import dete.org.view.terminal.tui.AnsiStyle.Rgb;

/**
 * Retrato ASCII colorido do guerreiro com chifres (33 colunas x 10 linhas)
 * alinhado à referência visual do painel INIMIGO.
 */
public final class CombatAsciiArt {

    public static final int WIDTH = 33;
    public static final int HEIGHT = 10;

    private record Segment(String text, Rgb color) {}

    private static final Segment[][] WARRIOR_ROWS = {
            // Linha 0: pontas dos chifres e coroa
            {
                    new Segment("   ", null),
                    new Segment("/\\", AnsiStyle.FG_CORAL),
                    new Segment("       ", null),
                    new Segment("_.-===-._", AnsiStyle.FG_CORAL),
                    new Segment("       ", null),
                    new Segment("/\\", AnsiStyle.FG_CORAL),
                    new Segment("   ", null)
            },
            // Linha 1: base dos chifres e topo do elmo
            {
                    new Segment("  ", null),
                    new Segment("/  \\_____.", AnsiStyle.FG_GOLD),
                    new Segment("'", AnsiStyle.FG_CORAL),
                    new Segment(" ", null),
                    new Segment("_____", AnsiStyle.FG_GOLD),
                    new Segment(" ", null),
                    new Segment("'", AnsiStyle.FG_CORAL),
                    new Segment("._____/  \\", AnsiStyle.FG_GOLD),
                    new Segment("  ", null)
            },
            // Linha 2: curva dos chifres e viseira superior
            {
                    new Segment("  ", null),
                    new Segment("\\", AnsiStyle.FG_GOLD),
                    new Segment("       ", null),
                    new Segment("/- /_____/ \\_\\", AnsiStyle.FG_GOLD),
                    new Segment("      ", null),
                    new Segment("/", AnsiStyle.FG_GOLD),
                    new Segment("  ", null)
            },
            // Linha 3: mandíbula dos chifres e testa
            {
                    new Segment("   ", null),
                    new Segment("`-----", AnsiStyle.FG_GOLD),
                    new Segment("|", AnsiStyle.FG_GOLD),
                    new Segment("   ", null),
                    new Segment("\\_   _/", AnsiStyle.FG_GOLD),
                    new Segment("   ", null),
                    new Segment("|", AnsiStyle.FG_GOLD),
                    new Segment("-----'", AnsiStyle.FG_GOLD),
                    new Segment("   ", null)
            },
            // Linha 4: olhos brilhantes
            {
                    new Segment("         ", null),
                    new Segment("|", AnsiStyle.FG_SALMON),
                    new Segment("  ", null),
                    new Segment("[=] V [=]", AnsiStyle.FG_SALMON),
                    new Segment("  ", null),
                    new Segment("|", AnsiStyle.FG_SALMON),
                    new Segment("         ", null)
            },
            // Linha 5: ombros e nariz
            {
                    new Segment("   ", null),
                    new Segment("___.--\\", AnsiStyle.FG_CORAL),
                    new Segment("     ", null),
                    new Segment("/_\\", AnsiStyle.FG_CORAL),
                    new Segment("     ", null),
                    new Segment("/--.___", AnsiStyle.FG_CORAL),
                    new Segment("   ", null)
            },
            // Linha 6: ombreiras e grade do elmo
            {
                    new Segment(" ", null),
                    new Segment(".'[]_\\", AnsiStyle.FG_ARMOR),
                    new Segment("   ", null),
                    new Segment("|", AnsiStyle.FG_ARMOR),
                    new Segment("  ", null),
                    new Segment("|||||||", AnsiStyle.FG_ARMOR),
                    new Segment("  ", null),
                    new Segment("|", AnsiStyle.FG_ARMOR),
                    new Segment("   ", null),
                    new Segment("/_[]`.", AnsiStyle.FG_ARMOR),
                    new Segment(" ", null)
            },
            // Linha 7: braços e peitoral superior
            {
                    new Segment("/_/", AnsiStyle.FG_ARMOR),
                    new Segment("   ", null),
                    new Segment("|\\_", AnsiStyle.FG_ARMOR),
                    new Segment("  ", null),
                    new Segment("\\__=====__/", AnsiStyle.FG_ARMOR),
                    new Segment("  ", null),
                    new Segment("_/|", AnsiStyle.FG_ARMOR),
                    new Segment("   ", null),
                    new Segment("\\_\\", AnsiStyle.FG_ARMOR)
            },
            // Linha 8: placa central ==X==
            {
                    new Segment("     ", null),
                    new Segment("/", AnsiStyle.FG_ARMOR),
                    new Segment("   ", null),
                    new Segment("\\___/==X==\\___/", AnsiStyle.FG_ARMOR),
                    new Segment("   ", null),
                    new Segment("\\", AnsiStyle.FG_ARMOR),
                    new Segment("     ", null)
            },
            // Linha 9: base da armadura
            {
                    new Segment("    ", null),
                    new Segment("/_____/  \\_____/  \\_____\\", AnsiStyle.FG_ARMOR),
                    new Segment("    ", null)
            }
    };

    private CombatAsciiArt() {}

    public static void appendCenteredRow(LineBuilder line, int rowIndex, int areaWidth, Rgb bg) {
        if (areaWidth <= 0) {
            return;
        }
        if (rowIndex < 0 || rowIndex >= HEIGHT) {
            line.appendSpaces(areaWidth, bg);
            return;
        }
        int rowWidth = plainRow(rowIndex).length();
        if (areaWidth < rowWidth) {
            // Se o painel for ligeiramente menor que 33 cols, recorta com segurança
            String plain = plainRow(rowIndex);
            int start = Math.max(0, (plain.length() - areaWidth) / 2);
            String slice = plain.substring(start, Math.min(plain.length(), start + areaWidth));
            line.append(slice, rowFallbackColor(rowIndex), bg);
            line.appendSpaces(areaWidth - slice.length(), bg);
            return;
        }

        int leftPad = (areaWidth - rowWidth) / 2;
        int rightPad = areaWidth - rowWidth - leftPad;
        line.appendSpaces(leftPad, bg);
        for (Segment seg : WARRIOR_ROWS[rowIndex]) {
            line.append(seg.text(), seg.color(), bg);
        }
        line.appendSpaces(rightPad, bg);
    }

    private static String plainRow(int rowIndex) {
        StringBuilder sb = new StringBuilder(WIDTH);
        for (Segment seg : WARRIOR_ROWS[rowIndex]) {
            sb.append(seg.text());
        }
        return sb.toString();
    }

    private static Rgb rowFallbackColor(int rowIndex) {
        if (rowIndex == 0 || rowIndex == 5) return AnsiStyle.FG_CORAL;
        if (rowIndex >= 1 && rowIndex <= 3) return AnsiStyle.FG_GOLD;
        if (rowIndex == 4) return AnsiStyle.FG_SALMON;
        return AnsiStyle.FG_ARMOR;
    }
}
