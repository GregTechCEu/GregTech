package gregtech.api.pipenet.nodenet;

import gregtech.api.pipenet.PipeNetWalker;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.util.GTLog;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NodeNetBuilder<NodeDataType> extends PipeNetWalker {

    public static void build(NodeNet<?> net, World world, BlockPos sourcePipe) {
        NodeNetBuilder<?> builder = new NodeNetBuilder(net, world, sourcePipe, 1);
        GTLog.logger.info("Building Pipe Net");
        builder.traversePipeNet();
    }

    private final NodeNet nodeNet;

    protected NodeNetBuilder(NodeNet<NodeDataType> nodeNet, World world, BlockPos sourcePipe, int walkedBlocks) {
        super(world, sourcePipe, walkedBlocks);
        this.nodeNet = nodeNet;
    }

    @Override
    protected PipeNetWalker createSubWalker(World world, BlockPos nextPos, int walkedBlocks) {
        NodeNetBuilder<NodeDataType> builder = new NodeNetBuilder(nodeNet, world, nextPos, walkedBlocks);
        return builder;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void checkPipe(IPipeTile<?, ?> pipeTile, BlockPos pos) {
        TileEntityPipeBase<?, ?> pipe = (TileEntityPipeBase<?, ?>) pipeTile;
        List<Node> nodes = new ArrayList<>();
        int pipes = 0;
        BlockPos.PooledMutableBlockPos offsetPos = BlockPos.PooledMutableBlockPos.retain();
        for(EnumFacing facing : EnumFacing.values()) {
            offsetPos.setPos(pos).move(facing);
            TileEntity tile = getWorld().getTileEntity(offsetPos);
            if(!(tile instanceof IPipeTile)) continue;
            IPipeTile<?, ?> otherPipe = (IPipeTile<?, ?>) tile;
            if(isValidPipe(pipeTile, otherPipe, pos, facing)) {
                if(++pipes > 2) {
                    pipe.createAndSetNode(nodeNet, true);
                    offsetPos.release();
                    return;
                }
                //pipes.add(pipe);
                if(otherPipe.getNodeData().equals(pipeTile.getNodeData()) && otherPipe.hasNode() && !otherPipe.getNode().isJunction() && isWalked(pos.offset(facing))) {
                    nodes.add(otherPipe.getNode());
                }
            }
        }
        offsetPos.release();
        if(nodes.size() == 0) {
            pipe.createAndSetNode(nodeNet, false);
            return;
        }
        if(nodes.size() == 1) {
            pipe.setNode(nodes.get(0));
        } else if(nodes.size() == 2) {
            if(nodes.get(0) != nodes.get(1)) {
                nodes.get(0).mergeNode(nodes.get(1));
            }
            pipe.setNode(nodes.get(0));
        }
    }

    @Override
    protected void checkNeighbour(IPipeTile<?, ?> pipeTile, BlockPos pipePos, EnumFacing faceToNeighbour, @Nullable TileEntity neighbourTile) {
    }

    @Override
    protected boolean isValidPipe(IPipeTile<?, ?> currentPipe, IPipeTile<?, ?> neighbourPipe, BlockPos pipePos, EnumFacing faceToNeighbour) {
        return ((TileEntityPipeBase<?, ?>)currentPipe).getPipeTypeClass().isAssignableFrom(((TileEntityPipeBase<?, ?>)neighbourPipe).getPipeTypeClass());
    }
}
