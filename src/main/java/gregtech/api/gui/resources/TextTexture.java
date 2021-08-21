package gregtech.api.gui.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TextTexture implements IGuiTexture{
    public final String text;
    public final int color;

    public TextTexture(String text) {
        this.text = text;
        this.color = 0xff000000;
    }

    public TextTexture(String text, int color) {
        this.text = text;
        this.color = color;
    }


    @Override
    public void draw(double x, double y, int width, int height) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        String resultText = I18n.format(text);
        fontRenderer.drawString(resultText, (float) (x + (width - fontRenderer.getStringWidth(resultText)) / 2), (float) (y + (height - fontRenderer.FONT_HEIGHT) / 2 + 2), color, false);
    }
}
