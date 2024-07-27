package gregtech.integration.jei.basic;

import gregtech.api.nuclear.fission.FissionFuelRegistry;
import gregtech.api.nuclear.fission.IFissionFuelStats;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class FissionFuelInfo implements IRecipeWrapper {

    public ItemStack rod;
    public ItemStack depletedRod;

    private String duration;
    private String maxTemp;
    private String crossSectionFast;
    private String crossSectionSlow;
    private String neutronGenerationTime;

    public FissionFuelInfo(ItemStack rod, ItemStack depletedRod) {
        this.rod = rod;
        this.depletedRod = depletedRod;

        IFissionFuelStats prop = FissionFuelRegistry.getFissionFuel(rod);

        duration = I18n.format("metaitem.nuclear.tooltip.duration", prop.getDuration());
        maxTemp = I18n.format("metaitem.nuclear.tooltip.temperature", prop.getMaxTemperature());
        crossSectionFast = I18n.format("metaitem.nuclear.tooltip.cross_section_fast",
                prop.getFastNeutronFissionCrossSection());
        crossSectionSlow = I18n.format("metaitem.nuclear.tooltip.cross_section_slow",
                prop.getSlowNeutronFissionCrossSection());
        neutronGenerationTime = I18n.format(
                "metaitem.nuclear.tooltip.neutron_time." + prop.getNeutronGenerationTimeCategory(),
                prop.getNeutronGenerationTime());
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.ITEM, rod);
        ingredients.setOutput(VanillaTypes.ITEM, depletedRod);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        int fontHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;

        int start = 40;
        int linesDrawn = 0;
        minecraft.fontRenderer.drawString(duration, 0, start, 0x111111);
        linesDrawn++;
        minecraft.fontRenderer.drawString(maxTemp, 0, fontHeight * linesDrawn + start, 0x111111);
        linesDrawn++;
        minecraft.fontRenderer.drawString(crossSectionFast, 0, fontHeight * linesDrawn + start, 0x111111);
        linesDrawn++;
        minecraft.fontRenderer.drawString(crossSectionSlow, 0, fontHeight * linesDrawn + start, 0x111111);
        linesDrawn++;
        minecraft.fontRenderer.drawString(neutronGenerationTime, 0, fontHeight * linesDrawn + start, 0x111111);
    }
}
