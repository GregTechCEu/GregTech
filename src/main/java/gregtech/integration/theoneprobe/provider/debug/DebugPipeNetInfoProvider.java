package gregtech.integration.theoneprobe.provider.debug;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
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
            if (tileEntity instanceof TileEntityPipeBase) {
                IPipeTile<?, ?> pipeTile = (IPipeTile<?, ?>) tileEntity;
                BlockPipe<?, ?, ?> blockPipe = pipeTile.getPipeBlock();
                PipeNet<?> pipeNet = blockPipe.getWorldPipeNet(world).getNetFromPos(data.getPos());
                if (pipeNet != null) {
                    probeInfo.text("Net: " + pipeNet.hashCode());
                    probeInfo.text("Node Info: ");
                    StringBuilder builder = new StringBuilder();
                    Node<?> node = pipeNet.getAllNodes().get(data.getPos());
                    builder.append("{")
                            .append("active: ").append(node.isActive)
                            .append(", mark: ").append(node.mark)
                            .append(", open: ").append(node.openConnections)
                            .append("}");
                    probeInfo.text(builder.toString());
                }
                probeInfo.text("tile open: " + pipeTile.getConnections());
                // if (blockPipe instanceof BlockFluidPipe) {
                // if (pipeTile instanceof TileEntityFluidPipeTickable) {
                // probeInfo.text("tile active: " + ((TileEntityFluidPipeTickable) pipeTile).isActive());
                // }
                // }
            }
        }
    }
}
