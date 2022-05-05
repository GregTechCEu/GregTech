package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IPrimitivePump;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class PrimitivePumpInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return String.format("%s:primitive_pump_provider", GTValues.MODID);
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, @Nonnull IBlockState blockState, IProbeHitData data) {
        if (blockState.getBlock().hasTileEntity(blockState)) {
            TileEntity tileEntity = world.getTileEntity(data.getPos());
            if (!(tileEntity instanceof IGregTechTileEntity)) return;

            MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            if (metaTileEntity instanceof IPrimitivePump) {
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.primitive_pump_production*} " + TextFormatting.AQUA + ((IPrimitivePump) metaTileEntity).getFluidProduction() + TextFormatting.RESET + " L/s");
            }
        }
    }
}
