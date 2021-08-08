package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static gregtech.api.gui.impl.ModularUIGui.*;

public class LabelWidget extends Widget {

    protected boolean xCentered = false;

    protected final String text;
    protected final Object[] formatting;
    private final int color;

    public LabelWidget(int xPosition, int yPosition, String text, Object... formatting) {
        this(xPosition, yPosition, text, 0x404040, formatting);
    }

    public LabelWidget(int xPosition, int yPosition, String text, int color) {
        this(xPosition, yPosition, text, color, new Object[0]);
    }

    public LabelWidget(int xPosition, int yPosition, String text, int color, Object[] formatting) {
        super(new Position(xPosition, yPosition), Size.ZERO);
        this.text = text;
        this.color = color;
        this.formatting = formatting;
        recomputeSize();
    }

    private String getResultText() {
        return I18n.format(text, formatting);
    }

    private void recomputeSize() {
        if (isClientSide()) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            String resultText = getResultText();
            setSize(new Size(fontRenderer.getStringWidth(resultText), fontRenderer.FONT_HEIGHT));
            if (uiAccess != null) {
                uiAccess.notifySizeChange();
            }
        }
    }

    public LabelWidget setXCentered(boolean xCentered) {
        this.xCentered = xCentered;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, IRenderContext context) {
        String resultText = getResultText();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Position pos = getPosition();
        if (!xCentered) {
            fontRenderer.drawString(resultText, pos.x, pos.y, color);
        } else {
            fontRenderer.drawString(resultText,
                    pos.x - fontRenderer.getStringWidth(resultText) / 2, pos.y, color);
        }
        GlStateManager.color(rColorForOverlay, gColorForOverlay, bColorForOverlay, 1.0F);
    }

}
