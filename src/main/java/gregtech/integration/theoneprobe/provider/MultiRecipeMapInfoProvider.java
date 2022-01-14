package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMultipleRecipeMaps;
import gregtech.api.recipes.RecipeMap;
import mcjty.theoneprobe.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

public class MultiRecipeMapInfoProvider extends CapabilityInfoProvider<IMultipleRecipeMaps> {

    @Override
    public String getID() {
        return String.format("%s:multi_recipemap_provider", GTValues.MODID);
    }

    @Override
    protected Capability<IMultipleRecipeMaps> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_MULTIPLE_RECIPEMAPS;
    }

    @Override
    protected void addProbeInfo(@Nonnull IMultipleRecipeMaps iMultipleRecipeMaps, IProbeInfo iProbeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        if (!(tileEntity instanceof IMultipleRecipeMaps)) return;
        iProbeInfo.text(TextStyleClass.INFO + I18n.format("gregtech.multiblock.multiple_recipemaps.header"));
        for (RecipeMap<?> recipeMap : iMultipleRecipeMaps.getAvailableRecipeMaps()) {
            if (recipeMap.equals(iMultipleRecipeMaps.getCurrentRecipeMap())) {
                iProbeInfo.text("   " + TextStyleClass.INFOIMP + "{*recipemap." + recipeMap.getUnlocalizedName() + ".name*} {*<*}");
            } else {
                iProbeInfo.text("   " + TextStyleClass.LABEL + recipeMap.getLocalizedName());
            }
        }
    }
}
