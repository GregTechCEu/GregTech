package gregtech.api.gui.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class TextTexture implements IGuiTexture{
    public String text;
    public int color;
    public int width;
    public boolean dropShadow;
    public TextType type;
    @SideOnly(Side.CLIENT)
    private List<String> texts;

    public TextTexture(String text, int color) {
        this.color = color;
        this.type = TextType.NORMAL;
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.text = I18n.format(text);
            texts = Collections.singletonList(this.text);
        }
    }

    public TextTexture setColor(int color) {
        this.color = color;
        return this;
    }

    public TextTexture setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    public TextTexture setWidth(int width) {
        this.width = width;
        if (FMLCommonHandler.instance().getSide().isClient()) {
            if (this.width > 0) {
                texts = Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(text, width);
            } else {
                texts = Collections.singletonList(text);
            }
        }
        return this;
    }

    public TextTexture setType(TextType type) {
        this.type = type;
        return this;
    }

    @Override
    public void draw(double x, double y, int width, int height) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int textH = fontRenderer.FONT_HEIGHT;
        if (type == TextType.NORMAL) {
            textH *= texts.size();
            for (int i = 0; i < texts.size(); i++) {
                String resultText = texts.get(i);
                int textW = fontRenderer.getStringWidth(resultText);
                float _x = (float) (x + (width - textW) / 2f);
                float _y = (float) (y + (height - textH) / 2f + i * fontRenderer.FONT_HEIGHT);
                fontRenderer.drawString(resultText, _x, _y, color, dropShadow);
            }
        } else if (type == TextType.HIDE) {
            String resultText = texts.get(0) + (texts.size() > 1 ? ".." : "");
            int textW = fontRenderer.getStringWidth(resultText);
            float _x = (float) (x + (width - textW) / 2f);
            float _y = (float) (y + (height - textH) / 2f);
            fontRenderer.drawString(resultText, _x, _y, color, dropShadow);
        } else if (type == TextType.ROLL) {
            int i = (int) ((Minecraft.getSystemTime() / 1000) % texts.size());
            String resultText = texts.get(i);
            int textW = fontRenderer.getStringWidth(resultText);
            float _x = (float) (x + (width - textW) / 2f);
            float _y = (float) (y + (height - textH) / 2f);
            fontRenderer.drawString(resultText, _x, _y, color, dropShadow);
        }

        GlStateManager.color(1, 1, 1, 1);
    }

    public enum TextType{
        NORMAL,
        HIDE,
        ROLL
    }
}
