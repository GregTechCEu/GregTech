package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.WidgetGroup;
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
                                (cd, pressed)-> {
                                    if (pressed) {
                                        textPanelWidget.addFormatting(colorFormatting);
                                    } else {
                                        textPanelWidget.removeFormatting(colorFormatting);
                                    }
                                })
                .setValueSupplier(true, ()-> colorFormatting == textPanelWidget.getFrontColorFormatting())
                .setIcon(PALETTE.getSubArea(x * 0.125, y * 0.25, 0.125, 0.25))
                .setColors(0, -1, 0));
            }
        }
        // style
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++) {
                TextFormatting styleFormatting = formatting[16 + y * 3 + x];
                if (styleFormatting == TextFormatting.RESET) return;
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
                        .setColors(0, -1, 0));
            }
        }
    }

    private static class TextPanelWidget extends DraggableScrollableWidgetGroup {
        public final static int SPACE = 0;
        public int updateCount;
        public String content;
        public int textHeight;
        public final Consumer<String> stringUpdate;
        public TextFormatting frontColor;
        public List<TextFormatting> frontStyle;

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
                this.pageInsertIntoCurrent(GuiScreen.getClipboardString());
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


