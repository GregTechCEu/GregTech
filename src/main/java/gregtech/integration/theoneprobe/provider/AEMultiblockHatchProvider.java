package gregtech.integration.theoneprobe.provider;

import gregtech.api.metatileentity.IAEStatusProvider;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;

public class AEMultiblockHatchProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return ":ae_multiblock_hatch_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, EntityPlayer entityPlayer, World world,
                             IBlockState blockState, IProbeHitData probeHitData) {
        if (blockState.getBlock().hasTileEntity(blockState) &&
                world.getTileEntity(probeHitData.getPos()) instanceof IGregTechTileEntity gtte &&
                gtte.getMetaTileEntity() instanceof IAEStatusProvider aeHostablePart) {
            if (aeHostablePart.isOnline()) {
                probeInfo.text("{*gregtech.gui.me_network.online*}");
            } else {
                probeInfo.text("{*gregtech.gui.me_network.offline*}");
            }
        }
    }
}
