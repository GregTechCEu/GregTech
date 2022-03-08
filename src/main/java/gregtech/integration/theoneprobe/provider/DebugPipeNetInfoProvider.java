package gregtech.integration.theoneprobe.provider;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.common.ConfigHolder;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Map;

public class DebugPipeNetInfoProvider implements IProbeInfoProvider {
    @Override
    public String getID() {
        return "gregtech:debug_pipe_net_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        if (mode == ProbeMode.DEBUG && ConfigHolder.misc.debug) {
            TileEntity tileEntity = world.getTileEntity(data.getPos());
            if (tileEntity instanceof MetaTileEntityHolder) {
                MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
                if (metaTileEntity != null) {
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add("MetaTileEntity Id: " + metaTileEntity.metaTileEntityId);
                    metaTileEntity.addDebugInfo(arrayList);
                    arrayList.forEach(probeInfo::text);
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
                    Map<BlockPos, ? extends Node<?>> nodeMap = pipeNet.getAllNodes();
                    Node<?> node = nodeMap.get(data.getPos());
                    builder.append("{").append("active: ").append(node.isActive)
                            .append(", mark: ").append(node.mark)
                            .append(", open: ").append(node.openConnections).append("}");
                    probeInfo.text(builder.toString());
                }
                probeInfo.text("tile open: " + pipeTile.getConnections());
                /*if (blockPipe instanceof BlockFluidPipe) {
                    if (pipeTile instanceof TileEntityFluidPipeTickable) {
                        probeInfo.text("tile active: " + ((TileEntityFluidPipeTickable) pipeTile).isActive());
                    }
                }*/
            }
        }
    }
}
