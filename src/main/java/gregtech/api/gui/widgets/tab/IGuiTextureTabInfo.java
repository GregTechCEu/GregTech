package gregtech.api.gui.widgets.tab;

import com.google.common.collect.Lists;
import gregtech.api.gui.resources.IGuiTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiUtils;

public class IGuiTextureTabInfo implements ITabInfo {
    public final IGuiTexture texture;
    public final String nameLocale;

    public IGuiTextureTabInfo(IGuiTexture texture, String nameLocale) {
        this.texture = texture;
        this.nameLocale = nameLocale;
    }

    @Override
    public void renderTab(IGuiTexture tabTexture, int posX, int posY, int xSize, int ySize, boolean isSelected) {
        tabTexture.draw(posX, posY, xSize, ySize);
        texture.draw(posX, posY, xSize, ySize);
    }

    @Override
    public void renderHoverText(int posX, int posY, int xSize, int ySize, int guiWidth, int guiHeight, boolean isSelected, int mouseX, int mouseY) {
        String localizedText = I18n.format(nameLocale);
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc);
        GuiUtils.drawHoveringText(Lists.newArrayList(localizedText), mouseX, mouseY, resolution.getScaledWidth(), resolution.getScaledHeight(), -1, mc.fontRenderer);
    }
}
