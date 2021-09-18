package gregtech.api.gui.widgets;

import com.google.common.collect.Lists;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TextFieldWidget2 extends Widget {

    private String text;
    private String localisedPostFix;
    private final Supplier<String> supplier;
    private final Consumer<String> setter;
    private List<Character> allowedChars;
    private Predicate<String> validator = s -> true;
    private boolean centered;
    private int textColor = 0xFFFFFF;
    private int markedColor = 0x2F72A8;
    private boolean postFixRight = false;

    private boolean focused;
    private int cursorPos;
    private int cursorPos2;

    private int cursorTime = 0;
    private boolean drawCursor = true;

    public TextFieldWidget2(int x, int y, int width, int height, Supplier<String> supplier, Consumer<String> setter) {
        super(x, y, width, height);
        this.supplier = supplier;
        this.setter = setter;
        this.text = supplier.get();
    }

    @Override
    public void initWidget() {
        if (isRemote()) {
            this.localisedPostFix = I18n.hasKey(localisedPostFix) ? I18n.format(localisedPostFix) : localisedPostFix;
        }
    }

    @Override
    public void updateScreen() {
        if (++cursorTime == 10) {
            cursorTime = 0;
            drawCursor = !drawCursor;
        }
        if (!focused && !text.equals(supplier.get())) {
            text = supplier.get();
            writeUpdateInfo(-1, buf -> buf.writeString(text));
        }
    }

    private int getTextX() {
        if (centered) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            int w = getSize().width;
            int textW = fontRenderer.getStringWidth(text);
            if (localisedPostFix != null && !localisedPostFix.isEmpty())
                textW += 3 + fontRenderer.getStringWidth(localisedPostFix);
            return (int) (w / 2f - textW / 2f + getPosition().x);
        }
        return getPosition().x + 1;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int y = getPosition().y;
        int textX = getTextX();
        if (cursorPos != cursorPos2) {
            // render marked text background
            int startX = fontRenderer.getStringWidth(text.substring(0, Math.min(cursorPos, cursorPos2))) + textX;
            String marked = getSelectedText();
            int width = fontRenderer.getStringWidth(marked);
            drawSelectionBox(startX, y, width);
        }
        fontRenderer.drawString(text, textX, y, textColor);
        if (localisedPostFix != null && !localisedPostFix.isEmpty()) {
            // render postfix
            int x = postFixRight && !centered ?
                    getPosition().x + getSize().width - (fontRenderer.getStringWidth(localisedPostFix) + 1) :
                    textX + fontRenderer.getStringWidth(text) + 3;
            fontRenderer.drawString(localisedPostFix, x, y, textColor);
        }
        if (focused && drawCursor) {
            // render cursor
            String sub = text.substring(0, cursorPos);
            int x = fontRenderer.getStringWidth(sub) + textX;
            drawCursor(x, y);
        }
    }

    private void drawCursor(float x, float y) {
        x -= 0.9f;
        y -= 1;
        float endX = x + 0.5f;
        float endY = y + 9;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableTexture2D();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, y, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    private void drawSelectionBox(int x, float y, int width) {
        int endX = x + width;
        y -= 1;
        float endY = y + Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        float red = (float) (markedColor >> 16 & 255) / 255.0F;
        float green = (float) (markedColor >> 8 & 255) / 255.0F;
        float blue = (float) (markedColor & 255) / 255.0F;
        float alpha = (float) (markedColor >> 24 & 255) / 255.0F;
        if (alpha == 0)
            alpha = 1f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(red, green, blue, alpha);
        GlStateManager.disableTexture2D();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, endY, 0.0D).endVertex();
        bufferbuilder.pos(endX, y, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            focused = true;
            int base = mouseX - getTextX();
            int x = 1;
            int i = 0;
            while (x < base) {
                if (i == text.length())
                    break;
                x += Minecraft.getMinecraft().fontRenderer.getCharWidth(text.charAt(i)) + 1;
                i++;
            }
            cursorPos = i;
            cursorPos2 = i;
        } else
            unFocus();
        return focused;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (focused && button == 0) {
            if (mouseX < getPosition().x) {
                cursorPos2 = 0;
                return true;
            }
            int base = mouseX - (getTextX());
            int x = 1;
            int i = 0;
            while (x < base) {
                if (i == text.length())
                    break;
                x += Minecraft.getMinecraft().fontRenderer.getCharWidth(text.charAt(i)) + 1;
                i++;
            }
            cursorPos2 = i;
        }
        return focused;
    }

    public String getSelectedText() {
        return text.substring(Math.min(cursorPos, cursorPos2), Math.max(cursorPos, cursorPos2));
    }

    @Override
    public boolean keyTyped(char charTyped, int keyCode) {
        if (focused) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                unFocus();
                return false;
            }
            if (keyCode == Keyboard.KEY_RETURN) {
                unFocus();
                return true;
            }
            if (GuiScreen.isKeyComboCtrlA(keyCode)) {
                cursorPos = text.length();
                cursorPos2 = 0;
                return true;
            }
            if (GuiScreen.isKeyComboCtrlC(keyCode)) {
                GuiScreen.setClipboardString(this.getSelectedText());
                return true;
            } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
                String clip = GuiScreen.getClipboardString();
                if (allowedChars != null) {
                    for (int i = 0; i < clip.length(); i++) {
                        if (!allowedChars.contains(clip.charAt(i)))
                            return true;
                    }
                }
                int min = Math.min(cursorPos, cursorPos2);
                int max = Math.max(cursorPos, cursorPos2);
                String t1 = text.substring(0, min);
                String t2 = text.substring(max);
                this.text = t1 + clip + t2;
                cursorPos = t1.length() + clip.length();
                cursorPos2 = cursorPos;
                return true;
            } else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
                GuiScreen.setClipboardString(this.getSelectedText());
                int min = Math.min(cursorPos, cursorPos2);
                int max = Math.max(cursorPos, cursorPos2);
                String t1 = text.substring(0, min);
                String t2 = text.substring(max);
                cursorPos = min;
                cursorPos2 = min;
                text = t1 + t2;
                return true;
            }
            if (keyCode == Keyboard.KEY_LEFT && cursorPos > 0) {
                int amount = 1;
                int pos = isShiftDown() ? cursorPos2 : cursorPos;
                if (isCtrlDown()) {
                    for (int i = pos - 1; i >= 0; i--) {
                        if (i == 0 || text.charAt(i) == ' ') {
                            amount = pos - i;
                            break;
                        }
                    }
                }
                cursorPos2 -= amount;
                if (cursorPos2 < 0)
                    cursorPos2 = 0;
                if (!isShiftDown()) {
                    cursorPos -= amount;
                    if (cursorPos < 0)
                        cursorPos = 0;
                }
                return true;
            }
            if (keyCode == Keyboard.KEY_RIGHT && cursorPos < text.length()) {
                int amount = 1;
                int pos = isShiftDown() ? cursorPos2 : cursorPos;
                if (isCtrlDown()) {
                    for (int i = pos + 1; i < text.length(); i++) {
                        if (i == text.length() - 1 || text.charAt(i) == ' ') {
                            amount = i - pos;
                            break;
                        }
                    }
                }
                cursorPos2 += amount;
                if (cursorPos2 > text.length())
                    cursorPos2 = text.length();
                if (!isShiftDown()) {
                    cursorPos += amount;
                    if (cursorPos > text.length())
                        cursorPos = text.length();
                }
                return true;
            }
            if (keyCode == Keyboard.KEY_BACK && text.length() > 0) {
                if (cursorPos != cursorPos2) {
                    int min = Math.min(cursorPos, cursorPos2);
                    int max = Math.max(cursorPos, cursorPos2);
                    String t1 = text.substring(0, min);
                    String t2 = text.substring(max);
                    cursorPos = min;
                    cursorPos2 = min;
                    text = t1 + t2;
                } else if (cursorPos > 0) {
                    String t1 = text.substring(0, cursorPos - 1);
                    String t2 = text.substring(cursorPos);
                    text = t1 + t2;
                    cursorPos--;
                    cursorPos2--;
                }
                return true;
            }
            if (allowedChars == null || allowedChars.contains(charTyped)) {
                String t1 = text.substring(0, cursorPos);
                String t2 = text.substring(cursorPos);
                t1 += charTyped;
                text = t1 + t2;
                cursorPos++;
                cursorPos2 = cursorPos;
                return true;
            }
        }
        return focused;
    }

    public String getText() {
        return text;
    }

    private void unFocus() {
        cursorPos2 = cursorPos;
        if (supplier.get().equals(text) || !validator.test(text)) {
            text = supplier.get();
            focused = false;
            return;
        }
        setter.accept(text);
        focused = false;
        writeUpdateInfo(-1, buf -> buf.writeString(text));
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == -1) {
            text = buffer.readString(32);
            setter.accept(text);
        }
    }

    /**
     * @param textColor text color. Default is 0xFFFFFF (white)
     */
    public TextFieldWidget2 setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    /**
     * If a pressed key is not in this list, it will not be typed. Allows every char by default
     *
     * @param allowedChars chars to allow as string
     */
    public TextFieldWidget2 setAllowedChars(String allowedChars) {
        this.allowedChars = Lists.charactersOf(allowedChars);
        return this;
    }

    /**
     * Called after unfocusing (press enter or click anywhere, but the field) the field
     *
     * @param validator determines whether the entered string is valid. Returns true by default
     */
    public TextFieldWidget2 setValidator(Predicate<String> validator) {
        this.validator = validator;
        return this;
    }

    /**
     * A predefined validator to only accept integer numbers
     *
     * @param min minimum accepted value
     * @param max maximum accepted value
     */
    public TextFieldWidget2 setNumbersOnly(int min, int max) {
        if (this.allowedChars == null) {
            String nums = "0123456789";
            if (min < 0)
                nums += "-";
            setAllowedChars(nums);
        }
        setValidator(val -> {
            if (val.isEmpty()) {
                text = String.valueOf(min);
                return false;
            }
            for (int i = 0; i < val.length(); i++) {
                char c = val.charAt(i);
                if (c == '-' && (min >= 0 || i != 0)) {
                    text = String.valueOf(min);
                    return false;
                }

            }
            int num;
            try {
                num = Integer.parseInt(val);
            } catch (NumberFormatException ignored) {
                text = String.valueOf(max);
                return true;
            }
            if (num < min) {
                text = String.valueOf(min);
                return true;
            }
            if (num > max) {
                text = String.valueOf(max);
                return true;
            }
            return true;
        });
        return this;
    }

    /**
     * @param centered whether to center the text and post fix in the x axis. Default is false
     */
    public TextFieldWidget2 setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }

    /**
     * @param postFix a string that will be rendered after the editable text
     */
    public TextFieldWidget2 setPostFix(String postFix) {
        this.localisedPostFix = postFix;
        if (gui != null && gui.holder != null && isRemote()) {
            this.localisedPostFix = I18n.hasKey(localisedPostFix) ? I18n.format(localisedPostFix) : localisedPostFix;
        }
        return this;
    }

    /**
     * @param markedColor background color of marked text. Default is 0x2F72A8 (lapis lazuli blue)
     */
    public TextFieldWidget2 setMarkedColor(int markedColor) {
        this.markedColor = markedColor;
        return this;
    }

    /**
     * @param postFixRight whether to bind the post fix to the right side. Default is false
     */
    public TextFieldWidget2 bindPostFixToRight(boolean postFixRight) {
        this.postFixRight = postFixRight;
        return this;
    }
}
