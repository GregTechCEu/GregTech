package gregtech.integration.theoneprobe.provider.debug;

import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.ConfigHolder;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DebugPipeNetInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return "gregtech:debug_pipe_net_provider";
    }

    @Override
    public void addProbeInfo(@NotNull ProbeMode mode, @NotNull IProbeInfo probeInfo, @NotNull EntityPlayer player,
                             @NotNull World world, @NotNull IBlockState blockState, @NotNull IProbeHitData data) {
        if (ConfigHolder.misc.debug) {
            TileEntity tileEntity = world.getTileEntity(data.getPos());
            if (tileEntity instanceof IGregTechTileEntity) {
                MetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                if (metaTileEntity != null) {
                    List<String> list = new ArrayList<>();
                    list.add("MetaTileEntity Id: " + metaTileEntity.metaTileEntityId);
                    metaTileEntity.addDebugInfo(list);
                    list.forEach(probeInfo::text);
                }
            }
            if (tileEntity instanceof PipeTileEntity pipeTile) {
                String builder = "{" +
                        ", mark: " + pipeTile.getVisualColor() +
                        ", open: " + pipeTile.getConnectionMask() +
                        ", blocked: " + pipeTile.getBlockedMask() +
                        "}";
                probeInfo.text(builder);
            }
        }
    }
}
