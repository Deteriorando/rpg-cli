package dete.org.view.terminal.tui;

import dete.org.view.terminal.tui.AnsiStyle.LineBuilder;
import dete.org.view.terminal.tui.AnsiStyle.Rgb;
import java.util.List;

final class CombatPanelFrame {
    private CombatPanelFrame() {}

    private static final boolean UTF8_OUTPUT = java.nio.charset.StandardCharsets.UTF_8.equals(System.out.charset());

    private static char boxTL() { return UTF8_OUTPUT ? '┌' : '+'; }

    private static char boxTR() { return UTF8_OUTPUT ? '┐' : '+'; }

    private static char boxBL() { return UTF8_OUTPUT ? '└' : '+'; }

    private static char boxBR() { return UTF8_OUTPUT ? '┘' : '+'; }

    private static char boxH()  { return UTF8_OUTPUT ? '─' : '-'; }

    static char boxV()  { return UTF8_OUTPUT ? '│' : '|'; }

    static String blankLine(int width, Rgb background) {
        LineBuilder lb = new LineBuilder();
        lb.padTo(width, background);
        return lb.finish();
    }

    static String buildOuterTopBorder(int frameWidth) {
        LineBuilder lb = new LineBuilder();
        lb.append(boxTL() + AnsiStyle.repeat(boxH(), frameWidth - 2) + boxTR(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        return lb.finish();
    }

    static String buildOuterBottomBorder(int frameWidth) {
        LineBuilder lb = new LineBuilder();
        lb.append(boxBL() + AnsiStyle.repeat(boxH(), frameWidth - 2) + boxBR(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        return lb.finish();
    }

    static String buildOuterBlankRow(int frameWidth) {
        LineBuilder lb = new LineBuilder();
        lb.append(String.valueOf(boxV()), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        lb.appendSpaces(frameWidth - 2, AnsiStyle.BG_OUTER);
        lb.append(String.valueOf(boxV()), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        return lb.finish();
    }

    static void appendPanelTop(LineBuilder lb, String title, Rgb titleColor, int panelOuterWidth) {
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

    static void appendPanelBottom(LineBuilder lb, int panelOuterWidth) {
        lb.append("+" + AnsiStyle.repeat('-', Math.max(0, panelOuterWidth - 2)) + "+",
                AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
    }

    static void appendFullPanel(List<String> out, String title, Rgb titleColor,
                                        int width, List<String> body) {
        out.add(fullPanelTop(title, titleColor, width));
        for (String row : body) {
            out.add(wrapFullPanelContent(row));
        }
        out.add(fullPanelBottom(width));
    }

    static String fullPanelTop(String title, Rgb titleColor, int width) {
        LineBuilder line = new LineBuilder();
        line.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        appendPanelTop(line, title, titleColor, width);
        line.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        return line.finish();
    }

    static String fullPanelBottom(int width) {
        LineBuilder line = new LineBuilder();
        line.append(boxV() + " ", AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        appendPanelBottom(line, width);
        line.append(" " + boxV(), AnsiStyle.FG_BORDER, AnsiStyle.BG_OUTER);
        return line.finish();
    }

    static String wrapFullPanelContent(String content) {
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
}
