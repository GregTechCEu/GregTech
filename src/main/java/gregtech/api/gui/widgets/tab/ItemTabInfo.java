package gregtech.api.gui.widgets.tab;

import com.google.common.collect.Lists;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiUtils;

import static gregtech.api.gui.impl.ModularUIGui.*;

public class ItemTabInfo implements ITabInfo {

    private final String nameLocale;
    private final ItemStack iconStack;

    public ItemTabInfo(String nameLocale, ItemStack iconStack) {
        this.nameLocale = nameLocale;
        this.iconStack = iconStack;
    }

    @Override
    public void renderTab(IGuiTexture tabTexture, int posX, int posY, int xSize, int ySize, boolean isSelected) {
        tabTexture.draw(posX, posY, xSize, ySize);
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(iconStack, posX + xSize / 2 - 8, posY + ySize / 2 - 8);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.color(rColorForOverlay, gColorForOverlay, bColorForOverlay, 1.0F);
    }

    @Override
    public void renderHoverText(int posX, int posY, int xSize, int ySize, int guiWidth, int guiHeight, boolean isSelected, int mouseX, int mouseY) {
        if (nameLocale != null) {
            String localizedText = LocalizationUtils.format(nameLocale);
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution resolution = new ScaledResolution(mc);
            GuiUtils.drawHoveringText(Lists.newArrayList(localizedText), mouseX, mouseY,
                    resolution.getScaledWidth(), resolution.getScaledHeight(), -1, mc.fontRenderer);
        }
    }
}
