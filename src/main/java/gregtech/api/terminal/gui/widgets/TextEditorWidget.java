package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditorWidget extends WidgetGroup {
    private static final TextureArea PALETTE = TextureArea.fullImage("textures/gui/widget/palette.png");
    private static final TextureArea STYLE = TextureArea.fullImage("textures/gui/widget/formatting.png");
    private final TextPanelWidget textPanelWidget;

    private static final Pattern COMMENT = Pattern.compile("(//.*|/\\*[\\s\\S]*?\\*/)|(#.*)");
    private static final Pattern STRING = Pattern.compile("(\"(?:[^\"\\\\]|\\\\[\\s\\S])*\"|'(?:[^'\\\\]|\\\\[\\s\\S])*')");
    private static final Pattern BOOL = Pattern.compile("\\b(true|false|null|undefined|NaN)\\b");
    private static final Pattern KEYWORD = Pattern.compile("\\b(import|var|for|if|else|return|this|while|new|function|switch|case|typeof|do|in|throw|try|catch|finally|with|instance|delete|void|break|continue)\\b");
    private static final Pattern KEYWORD_2 = Pattern.compile("\\b(String|int|long|boolean|float|double|byte|short|document|Date|Math|window|Object|location|navigator|Array|Number|Boolean|Function|RegExp)\\b");
    private static final Pattern VARIABLE = Pattern.compile("(?:[^\\W\\d]|\\$)[\\$\\w]*");
    private static final Pattern NUMBER = Pattern.compile("(0[xX][0-9a-fA-F]+|\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?|\\.\\d+(?:[eE][+-]?\\d+)?)");
    private static final Pattern ANY = Pattern.compile("[\\s\\S]");

    public TextEditorWidget(int x, int y, int width, int height,Consumer<String> stringUpdate, boolean allowToolBox) {
        super(new Position(x, y), new Size(Math.max(width, allowToolBox ? 80 : width), Math.max(height, allowToolBox ? 32 : height)));
        textPanelWidget = new TextPanelWidget(0, 32, Math.max(width, allowToolBox ? 80 : width), Math.max(height, allowToolBox ? 32 : height) - 32, stringUpdate);
        this.addWidget(textPanelWidget);
        if (allowToolBox) {
            initToolBox();
        }
    }

    public TextEditorWidget setBackground(IGuiTexture background) {
        textPanelWidget.setBackground(background);
        return this;
    }

    public TextEditorWidget setContent(String content) {
        textPanelWidget.pageSetCurrent(content);
        return this;
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        textPanelWidget.setActive(active);
    }

    private void initToolBox() {
        TextFormatting[] formatting = TextFormatting.values();
        // palette
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                TextFormatting colorFormatting = formatting[y * 4 + x];
                this.addWidget(new RectButtonWidget(x * 8, y * 8, 8, 8, 1)
                        .setToggleButton(PALETTE.getSubArea(0.5 + x * 0.125, y * 0.25, 0.125, 0.25),
                                (cd, pressed) -> {
                                    if (pressed) {
                                        textPanelWidget.addFormatting(colorFormatting);
                                    } else {
                                        textPanelWidget.removeFormatting(colorFormatting);
                                    }
                                })
                        .setValueSupplier(true, () -> colorFormatting == textPanelWidget.getFrontColorFormatting())
                        .setIcon(PALETTE.getSubArea(x * 0.125, y * 0.25, 0.125, 0.25))
                        .setColors(0, -1, 0)
                        .setHoverText(colorFormatting.getFriendlyName()));
            }
        }
        // style
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++) {
                TextFormatting styleFormatting = formatting[16 + y * 3 + x];
                if (styleFormatting == TextFormatting.RESET) break;
                this.addWidget(new RectButtonWidget(x * 16 + 32, y * 16, 16, 16, 1)
                        .setToggleButton(STYLE.getSubArea(0.5 + x * 1.0 / 6, y * 0.5, 1.0 / 6, 0.5),
                                (cd, pressed)-> {
                                    if (pressed) {
                                        textPanelWidget.addFormatting(styleFormatting);
                                    } else {
                                        textPanelWidget.removeFormatting(styleFormatting);
                                    }
                                })
                        .setValueSupplier(true, ()-> textPanelWidget.getFrontStyleFormatting().contains(styleFormatting))
                        .setIcon(STYLE.getSubArea(x * 1.0 / 6, y * 0.5, 1.0 / 6, 0.5))
                        .setColors(0, -1, 0)
                        .setHoverText(styleFormatting.getFriendlyName()));
            }
        }
        this.addWidget(new RectButtonWidget(3 * 16 + 32, 0, 16, 16, 3)
                .setToggleButton(new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor()), (c, p)-> textPanelWidget.allowMarkdown = !p)
                .setValueSupplier(true, () -> !textPanelWidget.allowMarkdown)
                .setColors(TerminalTheme.COLOR_B_3.getColor(), TerminalTheme.COLOR_1.getColor(), TerminalTheme.COLOR_B_3.getColor())
                .setIcon(new ColorRectTexture(TerminalTheme.COLOR_7.getColor()))
                .setHoverText("Check Markdown when Ctrl+V"));
        this.addWidget(new RectButtonWidget(3 * 16 + 32, 16, 16, 16, 3)
                .setClickListener(clickData -> textPanelWidget.pageSetCurrent(""))
                .setColors(TerminalTheme.COLOR_B_3.getColor(), TerminalTheme.COLOR_1.getColor(), TerminalTheme.COLOR_B_3.getColor())
                .setIcon(new ColorRectTexture(TerminalTheme.COLOR_7.getColor()))
                .setHoverText("Clean Up"));
    }

    private static class TextPanelWidget extends DraggableScrollableWidgetGroup {
        public final static int SPACE = 0;
        public int updateCount;
        public String content;
        public int textHeight;
        public final Consumer<String> stringUpdate;
        public TextFormatting frontColor;
        public List<TextFormatting> frontStyle;
        public boolean allowMarkdown;

        private static final char SECTION_SIGN = '\u00A7';

        @SideOnly(Side.CLIENT)
        public FontRenderer fontRenderer;
        @SideOnly(Side.CLIENT)
        private static final Pattern R_CODE_PATTERN = Pattern.compile("(?i)" + SECTION_SIGN + "[R]");
        @SideOnly(Side.CLIENT)
        private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(?i)" + SECTION_SIGN + "[0-9A-F]");


        public TextPanelWidget(int x, int y, int width, int height, Consumer<String> stringUpdate) {
            super(x, y, width, height);
            this.stringUpdate = stringUpdate;
            this.content = "";
            this.allowMarkdown = true;
            if (isClientSide()) {
                fontRenderer = Minecraft.getMinecraft().fontRenderer;
                textHeight = fontRenderer.getWordWrappedHeight(content, width - yBarWidth);
                frontColor = null;
                frontStyle = new ArrayList<>();
            }
        }

        @Override
        public int getMaxHeight() {
            return textHeight + SPACE + xBarHeight;
        }

        public void updateScreen() {
            super.updateScreen();
            ++this.updateCount;
        }

        @Override
        public boolean keyTyped(char typedChar, int keyCode) {
            if(!focus || !isActive()) return false;
            if (GuiScreen.isKeyComboCtrlV(keyCode)) {
                this.pageInsertIntoCurrent(allowMarkdown ? formatFromMarkdown(GuiScreen.getClipboardString()) : GuiScreen.getClipboardString());
                findFrontFormatting();
            } else {
                switch(keyCode) {
                    case 14:
                        if (!content.isEmpty()) {
                            this.pageSetCurrent(content.substring(0, content.length() - 1));
                        }
                        break;
                    case 28:
                    case 156:
                        this.pageInsertIntoCurrent("\n");
                        break;
                    default:
                        if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                            this.pageInsertIntoCurrent(Character.toString(typedChar));
                        }
                }
            }
            if (getMaxHeight() > getSize().height) {
                setScrollYOffset(getMaxHeight() - getSize().height);
            } else {
                setScrollYOffset(0);
            }
            return true;
        }

        private String formatFromMarkdown(String markdown) {
            StringBuilder builder = new StringBuilder();
            Stack<TextFormatting> stack = new Stack<>();
            int[] chars = markdown.chars().toArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '\\' && i + 1 < chars.length){
                    if (chars[i + 1] == '*' || chars[i + 1] == '_' || chars[i + 1] == '~' || chars[i + 1] == '`') {
                        builder.append(chars[i + 1]);
                        i++;
                    } else {
                        builder.append('\\');
                    }
                } else if (chars[i] == '*' && i + 1 < chars.length && chars[i + 1] == ' ') { // SUBLINE
                    builder.append(' ').append(TextFormatting.BOLD).append('*').append(TextFormatting.RESET).append(' ');
                    i++;
                } else if (chars[i] == '*' && i + 1 < chars.length && chars[i + 1] == '*') { // BOLD
                    checkTextFormatting(builder, TextFormatting.BOLD, stack);
                    i++;
                } else if (chars[i] == '_'){
                    if (i - 1 == -1 || !Character.isLetterOrDigit(chars[i-1])) { // ITALIC
                        checkTextFormatting(builder, TextFormatting.ITALIC, stack);
                    } else if (i + 1 == chars.length || !Character.isLetterOrDigit(chars[i+1])) {
                        checkTextFormatting(builder, TextFormatting.ITALIC, stack);
                    } else {
                        builder.append('_');
                    }
                } else if (chars[i] == '~') { // STRIKETHROUGH
                    checkTextFormatting(builder, TextFormatting.STRIKETHROUGH, stack);
                } else if (chars[i] == '`' && i + 1 < chars.length && chars[i + 1] == '`' && i + 2 < chars.length && chars[i + 2] == '`') { // code
                    boolean find = false;
                    for (int j = i + 3; j < chars.length - 2; j++) {
                        if (chars[j] == '`' && chars[j + 1] == '`' && chars[j + 2] == '`') {
                            find = true;
                            builder.append(checkCode(markdown.substring(i + 3, j)));
                            i += j - i;
                        }
                    }
                    if (!find) {
                        builder.append("```");
                    }
                    i += 2;
                } else if (chars[i] == '`') {
                    checkTextFormatting(builder, TextFormatting.UNDERLINE, stack);
                } else {
                    builder.append((char) chars[i]);
                }
            }
            return builder.toString();
        }

        private String checkCode(String code) {
            Pattern[] patterns = new Pattern[]{COMMENT, STRING, BOOL, KEYWORD, KEYWORD_2, VARIABLE, NUMBER, ANY};
            TextFormatting[] colors = new TextFormatting[]{
                    TextFormatting.DARK_GRAY, // comment
                    TextFormatting.DARK_GREEN,  //string
                    TextFormatting.RED,  // value
                    TextFormatting.BLUE,  // keyword
                    TextFormatting.LIGHT_PURPLE, // keyword2
                    TextFormatting.BLACK,  // variable
                    TextFormatting.RED,  // variable
                    TextFormatting.DARK_PURPLE}; // else
            StringBuilder builder = new StringBuilder();
            while (code.length() > 0) {
                boolean find = false;
                for (int i = 0; i < patterns.length; i++) {
                    Matcher matcher = patterns[i].matcher(code);
                    if (matcher.find() && matcher.start() == 0) {
                        builder.append(colors[i].toString()).append(code, 0, matcher.end()).append(TextFormatting.RESET.toString());
                        find = true;
                        code = code.substring(matcher.end());
                        break;
                    }
                }
                if (!find) {
                    builder.append(code.charAt(0));
                    code = code.substring(1);
                }
            }
            return builder.toString();
        }

        private void checkTextFormatting(StringBuilder builder, TextFormatting formatting,  Stack<TextFormatting> stack) {
            if (!stack.isEmpty() && stack.peek() == formatting) {
                builder.append(TextFormatting.RESET.toString());
                stack.pop();
                for (TextFormatting pre : stack) {
                    builder.append(pre.toString());
                }
            } else {
                stack.push(formatting);
                builder.append(formatting.toString());
            }
        }

        private static TextFormatting lookAheadChars(final String content, int index) {
            if (index > 1 && content.charAt(index - 2) == SECTION_SIGN) {
                int t = content.charAt(index - 1);
                if ('0' <= t && t <= '9'){
                    return TextFormatting.values()[t - '0'];
                } else if ('a' <= t && t <= 'f'){
                    return TextFormatting.values()[t - 'a' + 10];
                } else if ('k' <= t && t <= 'o') {
                    return TextFormatting.values()[t - 'k' + 16];
                } else if (t == 'r') {
                    return TextFormatting.values()[21];
                }
            }
            return null;
        }

        public static String cleanUpFormatting(final String content) {
            Set<Integer> removed = new HashSet<>();
		    Matcher marcher = R_CODE_PATTERN.matcher(content);
		    while (marcher.find()) {
                int index = marcher.start();
                while (index > 1) {
                    TextFormatting ahead = lookAheadChars(content, index);
                    if (ahead != null) {
                        removed.add(index - 2);
                    } else {
                        break;
                    }
                    index -= 2;
                }
            }
            marcher = COLOR_CODE_PATTERN.matcher(content);
            while (marcher.find()) {
                int index = marcher.start();
                while (index > 1) {
                    TextFormatting ahead = lookAheadChars(content, index);
                    if (ahead == null) {
                        break;
                    } else if (TextFormatting.RESET != ahead){
                        if (!removed.add(index - 2)) {
                            break;
                        }
                    } else {
                        break;
                    }
                    index -= 2;
                }
            }
            StringBuilder builder = new StringBuilder();
            AtomicInteger start = new AtomicInteger();
            removed.stream().sorted().forEach(remove->{
                builder.append(content, start.get(), remove);
                start.set(remove + 2);
            });
            builder.append(content, start.get(), content.length());
            return builder.toString();
        }

        private void findFrontFormatting() {
            int lastReset = content.lastIndexOf(SECTION_SIGN + "r");
            int lastColor = -1;
            frontColor = null;
            frontStyle.clear();
            for (TextFormatting value : TextFormatting.values()) {
                int index = content.lastIndexOf(value.toString());
                if (index > lastReset) {
                    if (value.isColor()) {
                        if (index > lastColor) {
                            lastColor = index;
                            frontColor = value;
                        }
                    } else if (value.isFancyStyling() && index > lastColor) {
                        frontStyle.add(value);
                    }
                }
            }
        }

        public void addFormatting(TextFormatting formatting) {
            if (formatting.isColor()) {
                frontColor = formatting;
                pageInsertIntoCurrent(formatting.toString());
                for (TextFormatting style : frontStyle) {
                    pageInsertIntoCurrent(style.toString());
                }
            } else if (formatting.isFancyStyling()){
                if (frontStyle.contains(formatting)) {
                    return;
                }
                frontStyle.add(formatting);
                pageInsertIntoCurrent(formatting.toString());
            }
        }

        public void removeFormatting(TextFormatting formatting) {
            if (formatting.isColor()) {
                frontColor = null;
                pageInsertIntoCurrent(TextFormatting.RESET.toString());
                frontStyle.forEach(style->pageInsertIntoCurrent(style.toString()));
            } else if (formatting.isFancyStyling()) {
                pageInsertIntoCurrent(TextFormatting.RESET.toString());
                if (frontColor != null) {
                    pageInsertIntoCurrent(frontColor.toString());
                }
                frontStyle.remove(formatting);
                frontStyle.forEach(style->pageInsertIntoCurrent(style.toString()));
            }
        }

        public TextFormatting getFrontColorFormatting() {
            return frontColor;
        }

        public List<TextFormatting> getFrontStyleFormatting() {
            return frontStyle;
        }

        public void pageSetCurrent(String string) {
            if (!content.equals(string)) {
                content = cleanUpFormatting(string);
                findFrontFormatting();
                if(stringUpdate != null) {
                    stringUpdate.accept(content);
                }
                textHeight = this.fontRenderer.getWordWrappedHeight(content + "" + TextFormatting.BLACK + "_", this.getSize().width - yBarWidth);
            }
        }

        public void pageInsertIntoCurrent(String string) {
            content = cleanUpFormatting(content + string);
            if(stringUpdate != null) {
                stringUpdate.accept(content);
            }
            textHeight = this.fontRenderer.getWordWrappedHeight(content + "" + TextFormatting.BLACK + "_", this.getSize().width - yBarWidth);
        }

        @Override
        public boolean hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
            String contentString = content;
            if (focus && isActive()) {
                if (this.fontRenderer.getBidiFlag()) {
                    contentString += "_";
                } else if (this.updateCount / 6 % 2 == 0) {
                    contentString += TextFormatting.BLACK + "_";
                } else {
                    contentString += TextFormatting.GRAY + "_";
                }
            }
            int x = getPosition().x - scrollXOffset;
            int y = getPosition().y + SPACE - scrollYOffset;
            for (String textLine : this.fontRenderer.listFormattedStringToWidth(contentString, getSize().width - yBarWidth)) {
                fontRenderer.drawString(textLine, x, y, 0xff000000, false);
                y += fontRenderer.FONT_HEIGHT;
            }
            return true;
        }
    }
}


