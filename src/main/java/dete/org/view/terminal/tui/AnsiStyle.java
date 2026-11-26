package dete.org.view.terminal.tui;

/**
 * Paleta de cores ANSI TrueColor e utilitários de composição de linhas
 * com largura visual exata para a TUI de combate.
 */
public final class AnsiStyle {

    public record Rgb(int r, int g, int b) {
        public String fg() {
            return "\033[38;2;" + r + ";" + g + ";" + b + "m";
        }

        public String bg() {
            return "\033[48;2;" + r + ";" + g + ";" + b + "m";
        }
    }

    // Fundos (fidelidade ao PNG)
    public static final Rgb BG_OUTER = new Rgb(11, 16, 24);
    public static final Rgb BG_ENEMY = new Rgb(27, 25, 32);
    public static final Rgb BG_PANEL = new Rgb(16, 25, 36);
    public static final Rgb BG_PLAYER = new Rgb(16, 29, 39);
    public static final Rgb BG_HIST_ALERT = new Rgb(45, 32, 40);
    public static final Rgb BG_HIST_FOCUS = new Rgb(24, 41, 56);

    // Botões de decisão
    public static final Rgb BG_BTN_ATTACK_ACTIVE = new Rgb(80, 48, 57);
    public static final Rgb BG_BTN_ATTACK_INACTIVE = new Rgb(43, 29, 36);
    public static final Rgb BG_BTN_DEFEND_ACTIVE = new Rgb(41, 64, 96);
    public static final Rgb BG_BTN_DEFEND_INACTIVE = new Rgb(32, 44, 64);
    public static final Rgb BG_BTN_ITEM_ACTIVE = new Rgb(60, 49, 94);
    public static final Rgb BG_BTN_ITEM_INACTIVE = new Rgb(42, 37, 65);
    public static final Rgb BG_BTN_BACK_ACTIVE = new Rgb(55, 68, 84);
    public static final Rgb BG_BTN_BACK_INACTIVE = new Rgb(26, 38, 50);

    // Cores de texto e bordas
    public static final Rgb FG_BORDER = new Rgb(87, 116, 138);
    public static final Rgb FG_CYAN = new Rgb(128, 232, 240);
    public static final Rgb FG_WHITE = new Rgb(231, 238, 245);
    public static final Rgb FG_MUTED = new Rgb(164, 182, 200);
    public static final Rgb FG_DIM = new Rgb(110, 128, 146);
    public static final Rgb FG_GOLD = new Rgb(255, 210, 138);
    public static final Rgb FG_CORAL = new Rgb(220, 145, 106);
    public static final Rgb FG_SALMON = new Rgb(255, 157, 153);
    public static final Rgb FG_PURPLE = new Rgb(198, 172, 255);
    public static final Rgb FG_GREEN = new Rgb(172, 233, 161);
    public static final Rgb FG_ARMOR = new Rgb(159, 196, 227);

    public static final String RESET = "\033[0m";

    private static final boolean COLOR_ENABLED = detectColorEnabled();

    private AnsiStyle() {}

    private static boolean detectColorEnabled() {
        String noColor = System.getenv("NO_COLOR");
        if (noColor != null && !noColor.isEmpty()) {
            return false;
        }
        String term = System.getenv("TERM");
        var console = System.console();
        return console != null && console.isTerminal() && !"dumb".equalsIgnoreCase(term);
    }

    public static String truncatePlain(String text, int maxCols) {
        if (text == null || maxCols <= 0) {
            return "";
        }
        if (text.length() <= maxCols) {
            return text;
        }
        if (maxCols <= 3) {
            return text.substring(0, maxCols);
        }
        return text.substring(0, maxCols - 3) + "...";
    }

    public static String repeat(char ch, int count) {
        if (count <= 0) {
            return "";
        }
        return String.valueOf(ch).repeat(count);
    }

    /**
     * Construtor de linha com controle estrito de largura visível para evitar
     * desalinhamento de bordas independentemente de códigos ANSI.
     */
    public static final class LineBuilder {
        private final StringBuilder sb = new StringBuilder(256);
        private int visibleLength = 0;

        public LineBuilder append(String text, Rgb fg, Rgb bg) {
            return append(text, fg, bg, false);
        }

        public LineBuilder append(String text, Rgb fg, Rgb bg, boolean bold) {
            if (text == null || text.isEmpty()) {
                return this;
            }
            if (COLOR_ENABLED) {
                if (bg != null) sb.append(bg.bg());
                if (fg != null) sb.append(fg.fg());
                if (bold) sb.append("\033[1m");
            }
            sb.append(text);
            if (COLOR_ENABLED && bold) {
                sb.append("\033[22m");
            }
            visibleLength += text.length();
            return this;
        }

        public LineBuilder appendSpaces(int count, Rgb bg) {
            if (count <= 0) {
                return this;
            }
            return append(repeat(' ', count), null, bg, false);
        }

        public LineBuilder padTo(int targetWidth, Rgb bg) {
            if (visibleLength < targetWidth) {
                appendSpaces(targetWidth - visibleLength, bg);
            }
            return this;
        }

        public int visibleLength() {
            return visibleLength;
        }

        public String finish() {
            if (COLOR_ENABLED) {
                sb.append(RESET);
            }
            return sb.toString();
        }
    }
}
