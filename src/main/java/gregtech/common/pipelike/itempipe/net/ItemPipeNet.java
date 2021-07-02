package gregtech.common.pipelike.itempipe.net;

import com.google.common.base.Preconditions;
import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.block.simple.EmptyNodeData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public class ItemPipeNet extends PipeNet<EmptyNodeData> {

    //private ItemStorageNetwork storageNetwork;
    private ItemTransferNetwork transferNetwork;

    public ItemPipeNet(WorldPipeNet<EmptyNodeData, ? extends PipeNet> world) {
        super(world);
    }

    @Override
    protected void updateBlockedConnections(BlockPos nodePos, EnumFacing facing, boolean isBlocked) {
        super.updateBlockedConnections(nodePos, facing, isBlocked);
        //getStorageNetwork().handleBlockedConnectionChange(nodePos, facing, isBlocked);
        getTransferNetwork().handleBlockedConnectionChange(nodePos, facing, isBlocked);
    }

    public void nodeNeighbourChanged(BlockPos nodePos) {
        if (containsNode(nodePos)) {
            int blockedConnections = getNodeAt(nodePos).blockedConnections;
            //getStorageNetwork().checkForItemHandlers(nodePos, blockedConnections);
            getTransferNetwork().checkForItemHandlers(nodePos, blockedConnections);
        }
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<EmptyNodeData>> transferredNodes, PipeNet<EmptyNodeData> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        ItemPipeNet parentInventoryNet = (ItemPipeNet) parentNet;
        if (parentInventoryNet.transferNetwork != null) {
            parentInventoryNet.transferNetwork.transferItemHandlers(transferredNodes.keySet(), getTransferNetwork());
        }
    }

    public ItemTransferNetwork getTransferNetwork() {
        if(transferNetwork == null) {
            Preconditions.checkNotNull(getWorldData(), "World is null at the time getStorageNetwork is called!");
            this.transferNetwork = new ItemTransferNetwork(getWorldData());
        }
        return transferNetwork;
    }

    /*public ItemStorageNetwork getStorageNetwork() {
        if (storageNetwork == null) {
            Preconditions.checkNotNull(getWorldData(), "World is null at the time getStorageNetwork is called!");
            this.storageNetwork = new ItemStorageNetwork(getWorldData());
        }
        return storageNetwork;
    }*/

    @Override
    protected void writeNodeData(EmptyNodeData nodeData, NBTTagCompound tagCompound) {
    }

    @Override
    protected EmptyNodeData readNodeData(NBTTagCompound tagCompound) {
        return EmptyNodeData.INSTANCE;
    }
}
