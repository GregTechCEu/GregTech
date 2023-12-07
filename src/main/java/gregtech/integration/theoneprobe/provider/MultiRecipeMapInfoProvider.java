package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMultipleRecipeMaps;
import gregtech.api.recipes.RecipeMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import org.jetbrains.annotations.NotNull;

public class MultiRecipeMapInfoProvider extends CapabilityInfoProvider<IMultipleRecipeMaps> {

    @Override
    public String getID() {
        return GTValues.MODID + ":multi_recipemap_provider";
    }

    @NotNull
    @Override
    protected Capability<IMultipleRecipeMaps> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_MULTIPLE_RECIPEMAPS;
    }

    @Override
    protected void addProbeInfo(@NotNull IMultipleRecipeMaps iMultipleRecipeMaps, @NotNull IProbeInfo iProbeInfo,
                                @NotNull EntityPlayer player, @NotNull TileEntity tileEntity,
                                @NotNull IProbeHitData data) {
        if (iMultipleRecipeMaps.getAvailableRecipeMaps().length == 1) return;

        iProbeInfo.text(TextStyleClass.INFO + IProbeInfo.STARTLOC + "gregtech.multiblock.multiple_recipemaps.header" +
                IProbeInfo.ENDLOC);
        for (RecipeMap<?> recipeMap : iMultipleRecipeMaps.getAvailableRecipeMaps()) {
            if (recipeMap.equals(iMultipleRecipeMaps.getCurrentRecipeMap())) {
                iProbeInfo.text("   " + TextStyleClass.INFOIMP + "{*" + recipeMap.getTranslationKey() + "*} {*<*}");
            } else {
                iProbeInfo.text("   " + TextStyleClass.LABEL + "{*" + recipeMap.getTranslationKey() + "*}");
            }
        }
    }
}
