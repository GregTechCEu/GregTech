package gregtech.integration.theoneprobe.provider;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityPassthroughHatchLaser;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;

public class LaserPassthroughInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return "gregtech:laser_passthrough_info_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, EntityPlayer entityPlayer, World world,
                             IBlockState blockState, IProbeHitData probeHitData) {
        BlockPos pos = probeHitData.getPos();

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity gtte) {
            MetaTileEntity metaTileEntity = gtte.getMetaTileEntity();
            if (metaTileEntity instanceof MetaTileEntityPassthroughHatchLaser passthroughHatch) {
                if (passthroughHatch.getFrontFacing().getOpposite() == probeHitData.getSideHit()) {
                    probeInfo.text(TextStyleClass.INFO + TextFormatting.GOLD.toString() +
                            "{*gregtech.top.passthrough.laser.input*}");
                } else if (passthroughHatch.getFrontFacing() == probeHitData.getSideHit()) {
                    probeInfo.text(TextStyleClass.INFO + TextFormatting.BLUE.toString() +
                            "{*gregtech.top.passthrough.laser.output*}");
                }
            }
        }
    }
}
