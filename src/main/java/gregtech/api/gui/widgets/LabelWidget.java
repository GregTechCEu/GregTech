package gregtech.api.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class LabelWidget extends Widget {

    protected boolean xCentered;
    protected boolean yCentered;
    protected int width;

    protected final String text;
    protected final Object[] formatting;
    private final int color;
    private boolean dropShadow;
    @SideOnly(Side.CLIENT)
    private List<String> texts;

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
        if (isClientSide()) {
            texts = Collections.singletonList(getResultText());
        }
        recomputeSize();
    }

    public LabelWidget setShadow(boolean dropShadow){
        this.dropShadow = dropShadow;
        return this;
    }

    public LabelWidget setWidth(int width) {
        this.width = width;
        if (isClientSide()) {
            if (this.width > 0) {
                texts = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(getResultText(), width);
            } else {
                texts = Collections.singletonList(getResultText());
            }
        }
        return this;
    }

    public LabelWidget setYCentered(boolean yCentered) {
        this.yCentered = yCentered;
        return this;
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
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        Position pos = getPosition();
        int height = fontRenderer.FONT_HEIGHT * texts.size();
        for (int i = 0; i < texts.size(); i++) {
            String resultText = texts.get(i);
            int width = fontRenderer.getStringWidth(resultText);
            float x = pos.x - (xCentered ? width / 2f : 0);
            float y = pos.y - (yCentered ? height / 2f : 0) + i * fontRenderer.FONT_HEIGHT;
            fontRenderer.drawString(resultText, x, y, color, dropShadow);
        }
    }

}
