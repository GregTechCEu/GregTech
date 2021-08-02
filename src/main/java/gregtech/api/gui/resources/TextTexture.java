package gregtech.api.gui.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TextTexture implements IGuiTexture{
    private final String text;
    private final int color;
    private final int textWidth;

    public TextTexture(String text) {
        this.text = text;
        this.color = 0xff000000;
        textWidth = FMLCommonHandler.instance().getSide().isClient()? Minecraft.getMinecraft().fontRenderer.getStringWidth(text):0;
    }

    public TextTexture(String text, int color) {
        this.text = text;
        this.color = color;
        textWidth = FMLCommonHandler.instance().getSide().isClient()? Minecraft.getMinecraft().fontRenderer.getStringWidth(text):0;
    }


    @Override
    public void draw(double x, double y, int width, int height) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawString(I18n.format(text), (float) (x + (width - textWidth) / 2), (float) (y + (height - fontRenderer.FONT_HEIGHT) / 2 + 2), color, false);
    }
}
