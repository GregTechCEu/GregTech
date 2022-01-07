package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.metatileentity.multiblock.IMultipleRecipeMaps;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTLog;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class MultiRecipeMapInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return String.format("%s:multi_recipemap_provider", GTValues.MODID);
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, @Nonnull IBlockState blockState, IProbeHitData data) {
        if (blockState.getBlock().hasTileEntity(blockState)) {
            EnumFacing sideHit = data.getSideHit();
            TileEntity tileEntity = world.getTileEntity(data.getPos());
            if (tileEntity == null) return;
            try {
                IMultipleRecipeMaps resultCapability = tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_MULTIPLE_RECIPEMAPS, null);
                if (resultCapability != null) {
                    addProbeInfo(resultCapability, probeInfo, tileEntity, sideHit);
                }
            } catch (ClassCastException ignored) {

            } catch (Throwable e) {
                GTLog.logger.error("Bad One probe Implem: {} {} {}", e.getClass().toGenericString(), e.getMessage(), e.getStackTrace()[0].getClassName());
            }
        }
    }


    protected void addProbeInfo(@Nonnull IMultipleRecipeMaps iMultipleRecipeMaps, IProbeInfo iProbeInfo, TileEntity tileEntity, EnumFacing enumFacing) {
        if (iMultipleRecipeMaps.hasMultipleRecipeMaps()) {
            iProbeInfo.text(TextStyleClass.INFO + I18n.format("gregtech.multiblock.multiple_recipemaps.header"));
            for (RecipeMap<?> recipeMap : iMultipleRecipeMaps.getAvailableRecipeMaps()) {
                if (recipeMap.equals(iMultipleRecipeMaps.getCurrentRecipeMap()))
                    iProbeInfo.text("   " + TextStyleClass.INFOIMP + "{*recipemap." + recipeMap.getUnlocalizedName() + ".name*} {*<*}");
                else
                    iProbeInfo.text( "   " + TextStyleClass.LABEL + recipeMap.getLocalizedName());
            }
        }
    }
}
