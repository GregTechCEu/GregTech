package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.IGuiTexture;
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

import java.awt.*;
import java.util.function.Consumer;

public class TextEditorWidget extends WidgetGroup {
    private final TextPanelWidget textPanelWidget;
    public TextEditorWidget(int x, int y, int width, int height, String text, Consumer<String> stringUpdate) {
        super(new Position(x, y), new Size(width, height));
        textPanelWidget = new TextPanelWidget(0, 10, width, height-10, text, stringUpdate);
        this.addWidget(new RectButtonWidget(0, 0, 20, 10, 1).setFill(new Color(109, 229, 154, 141).getRGB()).setHoverText("update").setClickListener(d->{
            if (stringUpdate != null) {
                stringUpdate.accept(textPanelWidget.content);
            }
        }));
        this.addWidget(textPanelWidget);
    }

    public TextEditorWidget setBackground(IGuiTexture background) {
        textPanelWidget.setBackground(background);
        return this;
    }

    private static class TextPanelWidget extends DraggableScrollableWidgetGroup {
        private final static int SPACE = 0;
        private int updateCount;
        private String content;
        private int textHeight;
        private final Consumer<String> stringUpdate;
        @SideOnly(Side.CLIENT)
        private FontRenderer fontRenderer;

        public TextPanelWidget(int x, int y, int width, int height, String text, Consumer<String> stringUpdate) {
            super(x, y, width, height);
            this.stringUpdate = stringUpdate;
            this.content = text == null ? "" : text;
            if (isClientSide()) {
                fontRenderer = Minecraft.getMinecraft().fontRenderer;
                textHeight = fontRenderer.getWordWrappedHeight(content, width - yBarWidth);
            }
        }

        @Override
        protected int getMaxHeight() {
            return textHeight + SPACE + xBarHeight;
        }

        public void updateScreen() {
            super.updateScreen();
            ++this.updateCount;
        }

        @Override
        public boolean keyTyped(char typedChar, int keyCode) {
            if(!focus) return false;
            if (GuiScreen.isKeyComboCtrlV(keyCode)) {
                this.pageInsertIntoCurrent(GuiScreen.getClipboardString());
            } else {
                switch(keyCode) {
                    case 14:
                        if (!content.isEmpty()) {
                            this.pageSetCurrent(content.substring(0, content.length() - 1));
                        }
                        return true;
                    case 28:
                    case 156:
                        this.pageInsertIntoCurrent("\n");
                        return true;
                    default:
                        if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                            this.pageInsertIntoCurrent(Character.toString(typedChar));
                        }
                }
            }
            return true;
        }

        private void pageSetCurrent(String string) {
            if (!content.equals(string)) {
                content = string;
                if(stringUpdate != null) {
                    stringUpdate.accept(content);
                }
                textHeight = this.fontRenderer.getWordWrappedHeight(content + "" + TextFormatting.BLACK + "_", this.getSize().width - yBarWidth);
            }
        }

        private void pageInsertIntoCurrent(String string) {
            content += string;
            if(stringUpdate != null) {
                stringUpdate.accept(content);
            }
            textHeight = this.fontRenderer.getWordWrappedHeight(content + "" + TextFormatting.BLACK + "_", this.getSize().width - yBarWidth);
        }

        @Override
        protected boolean hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
            String contentString = content;
            if (focus) {
                if (this.fontRenderer.getBidiFlag()) {
                    contentString += "_";
                } else if (this.updateCount / 6 % 2 == 0) {
                    contentString += TextFormatting.BLACK + "_";
                } else {
                    contentString += TextFormatting.GRAY + "_";
                }
            }
            this.fontRenderer.drawSplitString(contentString, getPosition().x, getPosition().y + SPACE, getSize().width - yBarWidth, 0);
            return true;
        }
    }
}


