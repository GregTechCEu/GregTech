package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.util.MCGuiUtil;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TextFieldWidget extends Widget {

    @SideOnly(Side.CLIENT)
    protected GuiTextField textField;

    protected int maxStringLength = 32;
    protected Predicate<String> textValidator;
    protected Supplier<String> textSupplier;
    protected Consumer<String> textResponder;
    protected String currentString;
    private IGuiTexture background;
    private boolean enableBackground;
    private boolean isClient;

    public TextFieldWidget(int xPosition, int yPosition, int width, int height, boolean enableBackground, Supplier<String> textSupplier, Consumer<String> textResponder) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        if (isClientSide()) {
            this.enableBackground = enableBackground;
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            if (enableBackground) {
                this.textField = new GuiTextField(0, fontRenderer, xPosition, yPosition, width, height);
            } else {
                this.textField = new GuiTextField(0, fontRenderer, xPosition + 1, yPosition + (height - fontRenderer.FONT_HEIGHT) / 2 + 1, width - 2, height);
            }
            this.textField.setCanLoseFocus(true);
            this.textField.setEnableBackgroundDrawing(enableBackground);
            this.textField.setMaxStringLength(this.maxStringLength);
            this.textField.setGuiResponder(MCGuiUtil.createTextFieldResponder(this::onTextChanged));
        }
        this.textSupplier = textSupplier;
        this.textResponder = textResponder;
    }

    public TextFieldWidget(int xPosition, int yPosition, int width, int height, boolean enableBackground, Supplier<String> textSupplier, Consumer<String> textResponder, int maxStringLength) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        if (isClientSide()) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            if (enableBackground) {
                this.textField = new GuiTextField(0, fontRenderer, xPosition, yPosition, width, height);
            } else {
                this.textField = new GuiTextField(0, fontRenderer, xPosition + 1, yPosition + (height - fontRenderer.FONT_HEIGHT) / 2 + 1, width - 2, height);
            }
            this.textField.setCanLoseFocus(true);
            this.textField.setEnableBackgroundDrawing(enableBackground);
            this.textField.setMaxStringLength(maxStringLength);
            this.maxStringLength = maxStringLength;
            this.textField.setGuiResponder(MCGuiUtil.createTextFieldResponder(this::onTextChanged));
        }
        this.textSupplier = textSupplier;
        this.textResponder = textResponder;
    }

    public TextFieldWidget(int xPosition, int yPosition, int width, int height, IGuiTexture background, Supplier<String> textSupplier, Consumer<String> textResponder) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        if (isClientSide()) {
            this.enableBackground = false;
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            this.textField = new GuiTextField(0, fontRenderer, xPosition + 1, yPosition + (height - fontRenderer.FONT_HEIGHT) / 2 + 1, width - 2, height);
            this.textField.setCanLoseFocus(true);
            this.textField.setEnableBackgroundDrawing(false);
            this.textField.setMaxStringLength(maxStringLength);
            this.textField.setGuiResponder(MCGuiUtil.createTextFieldResponder(this::onTextChanged));
        }
        this.background = background;
        this.textSupplier = textSupplier;
        this.textResponder = textResponder;
    }

    public TextFieldWidget doesClientCallback(boolean isClient) {
        this.isClient = isClient;
        return this;
    }

    public TextFieldWidget setTextSupplier(Supplier<String> textSupplier, boolean isClient) {
        this.isClient = isClient;
        this.textSupplier = textSupplier;
        return this;
    }

    public TextFieldWidget setTextResponder(Consumer<String> textResponder, boolean isClient) {
        this.isClient = isClient;
        this.textResponder = textResponder;
        return this;
    }

    public TextFieldWidget setCurrentString(String currentString) {
        this.currentString = currentString;
        this.textField.setText(currentString);
        return this;
    }

    public String getCurrentString() {
        if (isRemote()) {
            return this.textField.getText();
        }
        return this.currentString;
    }


    @Override
    protected void onPositionUpdate() {
        if (isClientSide() && textField != null) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            Position position = getPosition();
            Size size = getSize();
            GuiTextField textField = this.textField;
            textField.x = enableBackground ? position.x : position.x + 1;
            textField.y = enableBackground ? position.y : position.y + (size.height - fontRenderer.FONT_HEIGHT) / 2 + 1;
        }
    }

    @Override
    protected void onSizeUpdate() {
        if (isClientSide() && textField != null) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            Position position = getPosition();
            Size size = getSize();
            GuiTextField textField = this.textField;
            textField.width = enableBackground ? size.width : size.width - 2;
            textField.height = size.height;
            textField.y = enableBackground ? position.y : position.y + (getSize().height - fontRenderer.FONT_HEIGHT) / 2 + 1;

        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        if (background != null) {
            Position position = getPosition();
            Size size = getSize();
            background.draw(position.x, position.y, size.width, size.height);
        }
        this.textField.drawTextBox();
        GlStateManager.enableBlend();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return this.textField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyTyped(char charTyped, int keyCode) {
        return this.textField.textboxKeyTyped(charTyped, keyCode);
    }

    @Override
    public void updateScreen() {
        if (textSupplier != null && isClient) {
            this.currentString = textSupplier.get();
            this.textField.setText(currentString);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (textSupplier != null && !textSupplier.get().equals(currentString)) {
            this.currentString = textSupplier.get();
            writeUpdateInfo(1, buffer -> buffer.writeString(currentString));
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 1) {
            this.currentString = buffer.readString(Short.MAX_VALUE);
            this.textField.setText(currentString);
        }
    }

    protected void onTextChanged(String newTextString) {
        if (textValidator.test(newTextString)) {
            if (isClient && textResponder != null) {
                textResponder.accept(newTextString);
            }
            writeClientAction(1, buffer -> buffer.writeString(newTextString));
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            String clientText = buffer.readString(Short.MAX_VALUE);
            clientText = clientText.substring(0, Math.min(clientText.length(), maxStringLength));
            if (textValidator.test(clientText)) {
                this.currentString = clientText;
                if (textResponder != null) {
                    this.textResponder.accept(clientText);
                }
            }
        }
    }

    public TextFieldWidget setTextColor(int textColor) {
        if (isClientSide()) {
            this.textField.setTextColor(textColor);
        }
        return this;
    }

    public TextFieldWidget setMaxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
        if (isClientSide()) {
            this.textField.setMaxStringLength(maxStringLength);
        }
        return this;
    }

    public TextFieldWidget setValidator(Predicate<String> validator) {
        this.textValidator = validator;
        if (isClientSide()) {
            this.textField.setValidator(validator::test);
        }
        return this;
    }
}
