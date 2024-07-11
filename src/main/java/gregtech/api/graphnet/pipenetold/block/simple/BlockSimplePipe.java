package gregtech.api.graphnet.pipenetold.block.simple;

import gregtech.api.graphnet.pipenetold.IPipeNetData;
import gregtech.api.graphnet.pipenetold.WorldPipeNetBase;
import gregtech.api.graphnet.pipenetold.block.BlockPipe;
import gregtech.api.graphnet.pipenetold.block.IPipeType;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.pipenetold.tile.IPipeTile;
import gregtech.api.graphnet.pipenetold.tile.TileEntityPipeBase;

import net.minecraft.item.ItemStack;

public abstract class BlockSimplePipe<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends IPipeNetData<NodeDataType>, Edge extends NetEdge,
        WorldPipeNetType extends WorldPipeNetBase<NodeDataType, PipeType, Edge>>
                                     extends BlockPipe<PipeType, NodeDataType, Edge, WorldPipeNetType> {

    @Override
    public NodeDataType createProperties(IPipeTile<PipeType, NodeDataType, Edge> pipeTile) {
        return createProperties(pipeTile.getPipeType());
    }

    @Override
    public NodeDataType createItemProperties(ItemStack itemStack) {
        return createProperties(getItemPipeType(itemStack));
    }

    protected abstract NodeDataType createProperties(PipeType pipeType);

    @Override
    public ItemStack getDropItem(IPipeTile<PipeType, NodeDataType, Edge> pipeTile) {
        return new ItemStack(this, 1, pipeTile.getPipeType().ordinal());
    }

    @Override
    public PipeType getItemPipeType(ItemStack itemStack) {
        return getPipeTypeClass().getEnumConstants()[itemStack.getMetadata()];
    }

    @Override
    public void setTileEntityData(TileEntityPipeBase<PipeType, NodeDataType, Edge> pipeTile, ItemStack itemStack) {
        pipeTile.setPipeData(this, getItemPipeType(itemStack));
    }
}
