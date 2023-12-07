package gregtech.integration.theoneprobe.provider.debug;

import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;

public class DebugTickTimeProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return "gregtech:debug_tick_time_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world,
                             IBlockState blockState, IProbeHitData data) {
        if (ConfigHolder.misc.debug) {
            TileEntity tile = world.getTileEntity(data.getPos());
            if (tile instanceof MetaTileEntityHolder holder) {
                double[] timeStatistics = holder.getTimeStatistics();
                if (timeStatistics != null) {
                    double averageTickTime = timeStatistics[0];
                    double worstTickTime = timeStatistics[1];

                    // this is for dev environment debug, so don't worry about translating
                    probeInfo.text("Average: " +
                            TextFormattingUtil.formatNumbers(averageTickTime / MetaTileEntityHolder.TRACKED_TICKS) +
                            "ns");
                    probeInfo.text("Worst: " + TextFormattingUtil.formatNumbers(worstTickTime) + "ns");
                }
            }
        }
    }
}
