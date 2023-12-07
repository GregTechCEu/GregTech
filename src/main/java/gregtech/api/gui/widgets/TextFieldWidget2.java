package gregtech.api.gui.widgets;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * @author brachy84
 */
public class TextFieldWidget2 extends Widget {

    // all positive whole numbers
    public static final Pattern NATURAL_NUMS = Pattern.compile("[0-9]*");
    // all positive and negative numbers
    public static final Pattern WHOLE_NUMS = Pattern.compile("-?[0-9]*");
    public static final Pattern DECIMALS = Pattern.compile("[0-9]*(\\.[0-9]*)?");
    public static final Pattern LETTERS = Pattern.compile("[a-zA-Z]*");

    private String text;
    private String localisedPostFix;
    private final Supplier<String> supplier;
    private final Consumer<String> setter;
    private Consumer<TextFieldWidget2> onFocus;
    private Pattern regex;
    private Function<String, String> validator = s -> s;
    private boolean initialised = false;
    private boolean centered;
    private int textColor = 0xFFFFFF;
    private int markedColor = 0x2F72A8;
    private boolean postFixRight = false;
    private int maxLength = 32;
    private float scale = 1;

    private boolean focused;
    private int cursorPos;
    private int cursorPos2;

    private int clickTime = 20;
    private int cursorTime = 0;
    private boolean drawCursor = true;

    @Nullable
    private Boolean unicodeMode;

    public TextFieldWidget2(int x, int y, int width, int height, Supplier<String> supplier, Consumer<String> setter) {
        super(x, y, width, height);
        this.supplier = supplier;
        this.setter = setter;
        this.setText(supplier.get());
    }

    @Override
    public void initWidget() {
        if (isRemote()) {
            this.localisedPostFix = I18n.hasKey(localisedPostFix) ? I18n.format(localisedPostFix) : localisedPostFix;
        }
    }

    @Override
    public void updateScreen() {
        clickTime++;
        if (++cursorTime == 10) {
            cursorTime = 0;
            drawCursor = !drawCursor;
        }
    }

    @Override
    public void detectAndSendChanges() {
        String t = supplier.get();
        if (!initialised || (!focused && !getText().equals(t))) {
            setText(t);
            writeUpdateInfo(-2, buf -> buf.writeString(t));
            initialised = true;
        }
    }

    protected int getTextX() {
        if (centered) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            boolean prevUnicode = fontRenderer.getUnicodeFlag();
            if (this.unicodeMode != null) {
                fontRenderer.setUnicodeFlag(this.unicodeMode);
            }

            float textW = fontRenderer.getStringWidth(getRenderText()) * scale;
            if (localisedPostFix != null && !localisedPostFix.isEmpty())
                textW += 3 + fontRenderer.getStringWidth(localisedPostFix) * scale;
            int x = (int) (getSize().width / 2f - textW / 2f + getPosition().x);
            if (this.unicodeMode != null) {
                fontRenderer.setUnicodeFlag(prevUnicode);
            }
            return x;
        }
        return getPosition().x + 1;
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int y = getPosition().y;
        int textX = getTextX();

        boolean prevUnicode = fontRenderer.getUnicodeFlag();
        if (this.unicodeMode != null) {
            fontRenderer.setUnicodeFlag(this.unicodeMode);
        }

        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 0);
        float scaleFactor = 1 / scale;
        y *= scaleFactor;
        String text = getRenderText();
        if (cursorPos != cursorPos2) {
            // render marked text background
            float startX = fontRenderer
                    .getStringWidth(text.substring(0, toRenderTextIndex(Math.min(cursorPos, cursorPos2)))) * scale +
                    textX;
            String marked = getSelectedText();
            float width = fontRenderer.getStringWidth(marked);
            drawSelectionBox(startX * scaleFactor, y, width);
        }
        fontRenderer.drawString(text, (int) (textX * scaleFactor), y, textColor);
        if (localisedPostFix != null && !localisedPostFix.isEmpty()) {
            // render postfix
            int x = postFixRight && !centered ?
                    getPosition().x + getSize().width - (fontRenderer.getStringWidth(localisedPostFix) + 1) :
                    textX + fontRenderer.getStringWidth(text) + 3;
            x *= scaleFactor;
            fontRenderer.drawString(localisedPostFix, x, y, textColor);
        }
        if (focused && drawCursor) {
            // render cursor
            String sub = text.substring(0, toRenderTextIndex(cursorPos));
            float x = fontRenderer.getStringWidth(sub) * scale + textX;
            x *= scaleFactor;
            drawCursor(x, y);
        }
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();

        if (this.unicodeMode != null) {
            fontRenderer.setUnicodeFlag(prevUnicode);
        }
    }

    @SideOnly(Side.CLIENT)
    private void drawCursor(float x, float y) {
        x -= 0.9f;
        y -= 1;
        float endX = x + 0.5f * (1 / scale);
        float endY = y + 9;
        float red = (float) (textColor >> 16 & 255) / 255.0F;
        float green = (float) (textColor >> 8 & 255) / 255.0F;
        float blue = (float) (textColor & 255) / 255.0F;
        float alpha = (float) (textColor >> 24 & 255) / 255.0F;
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
    }

    @SideOnly(Side.CLIENT)
    private void drawSelectionBox(float x, float y, float width) {
        float endX = x + width;
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
            gui.getModularUIGui().setFocused(true);
            if (onFocus != null) {
                onFocus.accept(this);
            }
            if (clickTime < 5) {
                cursorPos = getText().length();
                cursorPos2 = 0;
            } else {
                cursorPos = getCursorPosFromMouse(mouseX);
                cursorPos2 = cursorPos;
            }
            clickTime = 0;
        } else
            unFocus();
        return focused;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (focused && button == 0) {
            if (mouseX < getPosition().x) {
                cursorPos = 0;
                return true;
            }
            cursorPos = getCursorPosFromMouse(mouseX);
        }
        return focused;
    }

    private int getCursorPosFromMouse(int mouseX) {
        int base = mouseX - getTextX();
        int i = 0;
        String renderText = getRenderText();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        boolean bold = false;
        // Determine width of string, copied from FontRenderer#getStringWidth
        for (float x = 0; i < renderText.length() && x < base; i++) {
            char c = renderText.charAt(i);
            int cw = fontRenderer.getCharWidth(c);
            // char width of the formatting character (U+167) is -1, for some reason
            if (cw < 0 && i < renderText.length() - 1) {
                cw = 0;
                char c2 = renderText.charAt(++i);
                if (c2 != 'l' && c2 != 'L') {
                    if (c2 == 'r' || c2 == 'R') {
                        bold = false;
                    }
                } else {
                    bold = true;
                }
            }
            x += (bold && cw > 0 ? cw + 1 : cw) * scale;
        }
        return toOriginalTextIndex(i);
    }

    public String getSelectedText() {
        return getText().substring(Math.min(cursorPos, cursorPos2), Math.max(cursorPos, cursorPos2));
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
                cursorPos = getText().length();
                cursorPos2 = 0;
                return true;
            }
            if (GuiScreen.isKeyComboCtrlC(keyCode)) {
                GuiScreen.setClipboardString(this.getSelectedText());
                return true;
            } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
                String clip = GuiScreen.getClipboardString();
                if (getText().length() + clip.length() > maxLength || !isAllowed(clip))
                    return true;
                replaceMarkedText(clip);
                return true;
            } else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
                GuiScreen.setClipboardString(this.getSelectedText());
                replaceMarkedText(null);
                return true;
            }
            if (keyCode == Keyboard.KEY_LEFT) {
                if (cursorPos > 0) {
                    int amount = 1;
                    int pos = cursorPos;
                    if (isCtrlDown()) {
                        for (int i = pos - 1; i >= 0; i--) {
                            if (i == 0 || getText().charAt(i) == ' ') {
                                amount = pos - i;
                                break;
                            }
                        }
                    }
                    cursorPos -= amount;
                    if (cursorPos < 0)
                        cursorPos = 0;
                    if (!isShiftDown()) {
                        cursorPos2 = cursorPos;
                    }
                }
                return true;
            }
            if (keyCode == Keyboard.KEY_RIGHT) {
                if (cursorPos < getText().length()) {
                    int amount = 1;
                    int pos = cursorPos;
                    if (isCtrlDown()) {
                        for (int i = pos + 1; i < getText().length(); i++) {
                            if (i == getText().length() - 1 || getText().charAt(i) == ' ') {
                                amount = i - pos;
                                break;
                            }
                        }
                    }
                    cursorPos = Math.min(cursorPos + amount, getText().length());
                    if (!isShiftDown()) {
                        cursorPos2 = cursorPos;
                    }
                }
                return true;
            }
            if (keyCode == Keyboard.KEY_BACK) {
                if (getText().length() > 0) {
                    if (cursorPos != cursorPos2) {
                        replaceMarkedText(null);
                    } else if (cursorPos > 0) {
                        setText(getText().substring(0, cursorPos - 1) + getText().substring(cursorPos));
                        cursorPos--;
                        cursorPos2 = cursorPos;
                    }
                }
                return true;
            }
            if (keyCode == Keyboard.KEY_DELETE) {
                if (getText().length() > 0) {
                    if (cursorPos != cursorPos2) {
                        replaceMarkedText(null);
                    } else if (cursorPos < getText().length()) {
                        setText(getText().substring(0, cursorPos) + getText().substring(cursorPos + 1));
                    }
                }
                return true;
            }
            if (charTyped != Character.MIN_VALUE && getText().length() < maxLength) {
                int min = Math.min(cursorPos, cursorPos2);
                int max = Math.max(cursorPos, cursorPos2);
                String newText = getText().substring(0, min) + charTyped + getText().substring(max);
                if (isAllowed(newText)) {
                    setText(newText);
                    cursorPos = min + 1;
                    cursorPos2 = cursorPos;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isAllowed(String t) {
        return regex == null || regex.matcher(t).matches();
    }

    private void replaceMarkedText(String replacement) {
        int min = Math.min(cursorPos, cursorPos2);
        int max = Math.max(cursorPos, cursorPos2);
        String t1 = getText().substring(0, min);
        String t2 = getText().substring(max);
        if (replacement != null) {
            if (t1.length() + t2.length() + replacement.length() > maxLength)
                return;
        }
        if (replacement == null) {
            setText(t1 + t2);
            cursorPos = min;
        } else {
            setText(t1 + replacement + t2);
            cursorPos = t1.length() + replacement.length();
        }
        cursorPos2 = cursorPos;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    protected String getRenderText() {
        return this.getText();
    }

    protected int toOriginalTextIndex(int renderTextIndex) {
        return renderTextIndex;
    }

    protected int toRenderTextIndex(int originalTextIndex) {
        return originalTextIndex;
    }

    public boolean isFocused() {
        return focused;
    }

    public void unFocus() {
        if (!focused) return;
        cursorPos2 = cursorPos;
        String t = validator.apply(getText());
        setText(t);
        setter.accept(t);
        focused = false;
        gui.getModularUIGui().setFocused(false);
        writeClientAction(-1, buf -> buf.writeString(t));
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) {
            setText(buffer.readString(maxLength));
            setter.accept(getText());
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == -2) {
            setText(buffer.readString(maxLength));
            setter.accept(getText());
            initialised = true;
            if (cursorPos > getText().length()) {
                cursorPos = getText().length();
            }
            if (cursorPos2 > getText().length()) {
                cursorPos2 = getText().length();
            }
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
     * If a key is pressed, the new string will be matched against this pattern.
     * If it doesn't match, the char will not be typed.
     *
     * @param regex pattern
     */
    public TextFieldWidget2 setAllowedChars(Pattern regex) {
        this.regex = regex;
        return this;
    }

    /**
     * Called after unfocusing (press enter or click anywhere, but the field) the field
     *
     * @param validator determines whether the entered string is valid. Returns true by default
     */
    public TextFieldWidget2 setValidator(Function<String, String> validator) {
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
        if (this.regex == null) {
            regex = min < 0 ? WHOLE_NUMS : NATURAL_NUMS;
        }
        setValidator(val -> {
            if (val.isEmpty()) {
                return String.valueOf(min);
            }
            for (int i = 0; i < val.length(); i++) {
                char c = val.charAt(i);
                if (c == '-' && (min >= 0 || i != 0)) {
                    return String.valueOf(min);
                }

            }
            int num;
            try {
                num = Integer.parseInt(val);
            } catch (NumberFormatException ignored) {
                return String.valueOf(max);
            }
            if (num < min) {
                return String.valueOf(min);
            } else if (num > max) {
                return String.valueOf(max);
            } else {
                return val;
            }
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

    /**
     * Will scale the text, the marked background and the cursor. f.e. 0.5 is half the size
     *
     * @param scale scale factor
     */
    public TextFieldWidget2 setScale(float scale) {
        this.scale = scale;
        return this;
    }

    public TextFieldWidget2 setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    /**
     * Called when the text field gets focused. Only called on client.
     * Use it to un focus other text fields.
     * Optimally this should be done automatically, but that is not really possible with the way Modular UI is made
     */
    public TextFieldWidget2 setOnFocus(Consumer<TextFieldWidget2> onFocus) {
        this.onFocus = onFocus;
        return this;
    }

    /**
     * @param unicodeMode If specified, the text will be rendered with unicode / non-unicode fonts on
     *                    {@code true} / {@code false} respectively.
     */
    public TextFieldWidget2 setUnicodeMode(@Nullable Boolean unicodeMode) {
        this.unicodeMode = unicodeMode;
        return this;
    }
}
