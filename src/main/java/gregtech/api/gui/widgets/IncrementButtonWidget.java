package gregtech.api.gui.widgets;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.SizedTextureArea;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.IntConsumer;

public class IncrementButtonWidget extends Widget {

    private TextureArea buttonTexture = GuiTextures.VANILLA_BUTTON.getSubArea(0.0, 0.0, 1.0, 0.5);
    private final int increment;
    private final int incrementShift;
    private final int incrementCtrl;
    private final int incrementShiftCtrl;
    private final IntConsumer updater;
    private int clickValue;
    private boolean shouldClientCallback;

    public IncrementButtonWidget(int x, int y, int width, int height, int increment, int incrementShift, int incrementCtrl, int incrementShiftCtrl, IntConsumer updater) {
        super(x, y, width, height);
        this.increment = increment;
        this.incrementShift = incrementShift;
        this.incrementCtrl = incrementCtrl;
        this.incrementShiftCtrl = incrementShiftCtrl;
        this.updater = updater;
        this.clickValue = increment;
    }

    public IncrementButtonWidget setButtonTexture(TextureArea buttonTexture) {
        this.buttonTexture = buttonTexture;
        return this;
    }

    public IncrementButtonWidget setShouldClientCallback(boolean shouldClientCallback) {
        this.shouldClientCallback = shouldClientCallback;
        return this;
    }

    @Override
    public void updateScreen() {
        this.clickValue = getClickValue();
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        Position position = getPosition();
        Size size = getSize();
        if (buttonTexture instanceof SizedTextureArea) {
            ((SizedTextureArea) buttonTexture).drawHorizontalCutSubArea(position.x, position.y, size.width, size.height, 0.0, 1.0);
        } else {
            buttonTexture.drawSubArea(position.x, position.y, size.width, size.height, 0.0, 0.0, 1.0, 1.0);
        }
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String text = clickValue + "";
        fontRenderer.drawString(text,
                position.x + size.width / 2 - fontRenderer.getStringWidth(text) / 2,
                position.y + size.height / 2 - fontRenderer.FONT_HEIGHT / 2, 0xFFFFFF);
    }

    @Override
    public void detectAndSendChanges() {
    }

    @SideOnly(Side.CLIENT)
    private int getClickValue() {
        if (isShiftDown()) {
            if (isCtrlDown())
                return incrementShiftCtrl;
            else
                return incrementShift;
        } else if (isCtrlDown())
            return incrementCtrl;
        return increment;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            updater.accept(clickValue);
            if (shouldClientCallback)
                writeClientAction(-1, buf -> buf.writeInt(clickValue));
            playButtonClickSound();
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == -1) {
            updater.accept(buffer.readInt());
        }
    }
}
