package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMultipleRecipeMaps;
import gregtech.api.recipes.RecipeMap;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

public class MultiRecipeMapInfoProvider extends CapabilityInfoProvider<IMultipleRecipeMaps> {

    @Override
    public String getID() {
        return GTValues.MODID + ":multi_recipemap_provider";
    }

    @Nonnull
    @Override
    protected Capability<IMultipleRecipeMaps> getCapability() {
        return GregtechTileCapabilities.CAPABILITY_MULTIPLE_RECIPEMAPS;
    }

    @Override
    protected void addProbeInfo(@Nonnull IMultipleRecipeMaps iMultipleRecipeMaps, @Nonnull IProbeInfo iProbeInfo, @Nonnull EntityPlayer player, @Nonnull TileEntity tileEntity, @Nonnull IProbeHitData data) {
        if (iMultipleRecipeMaps.getAvailableRecipeMaps().length == 1) return;

        iProbeInfo.text(TextStyleClass.INFO + IProbeInfo.STARTLOC + "gregtech.multiblock.multiple_recipemaps.header" + IProbeInfo.ENDLOC);
        for (RecipeMap<?> recipeMap : iMultipleRecipeMaps.getAvailableRecipeMaps()) {
            if (recipeMap.equals(iMultipleRecipeMaps.getCurrentRecipeMap())) {
                iProbeInfo.text("   " + TextStyleClass.INFOIMP + "{*recipemap." + recipeMap.getUnlocalizedName() + ".name*} {*<*}");
            } else {
                iProbeInfo.text("   " + TextStyleClass.LABEL + recipeMap.getLocalizedName());
            }
        }
    }
}
