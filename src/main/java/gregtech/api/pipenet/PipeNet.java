package gregtech.api.pipenet;

import gregtech.api.pipenet.nodenet.Node;
import gregtech.api.pipenet.nodenet.PipeNetBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;

public abstract class PipeNet<NodeDataType> implements INBTSerializable<NBTTagCompound> {

    protected final WorldPipeNet<NodeDataType, PipeNet<NodeDataType>> worldData;
    private final Map<ChunkPos, Integer> ownedChunks = new HashMap<>();
    private long lastUpdate;
    private boolean isBuilding = false;

    boolean isValid = false;
    private final Set<Node<NodeDataType>> nodes = new HashSet<>();

    public PipeNet(WorldPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>> world) {
        this.worldData = (WorldPipeNet<NodeDataType, PipeNet<NodeDataType>>) world;
        this.worldData.addPipeNet(this);
    }

    public void onSetWorld(World world) {
        nodes.forEach(node -> node.onSetWorld(world));
    }

    public void rebuildNodeNet(BlockPos pos) {
        if (isBuilding) return;
        isBuilding = true;
        PipeNetBuilder.build(this, getWorldData(), pos);
        isBuilding = false;
        isValid = true;
    }

    public final Node<NodeDataType> createNode() {
        return worldData.createNode(this);
    }

    public Set<ChunkPos> getContainedChunks() {
        return Collections.unmodifiableSet(ownedChunks.keySet());
    }

    public World getWorldData() {
        return worldData.getWorld();
    }

    public void addNode(Node<NodeDataType> node) {
        nodes.add(node);
    }

    public void removeNode(Node<NodeDataType> node) {
        nodes.remove(node);
    }

    public void invalidate() {
        this.isValid = false;
    }

    public void destroy() {
        invalidate();
        nodes.forEach(Node::destroy);
        nodes.clear();
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public boolean isValid() {
        return isValid;
    }

    public void onConnectionsUpdate() {
        this.lastUpdate = System.currentTimeMillis();
        if (isValid)
            invalidate();
    }

    public void onNodeNeighbourChange(World world, BlockPos pipePos, EnumFacing facing) {
    }

    public boolean containsNode(BlockPos blockPos) {
        for (Node<NodeDataType> node : nodes) {
            if (node.containsNode(blockPos))
                return true;
        }
        return false;
    }

    public void checkAddedInChunk(BlockPos nodePos) {
        ChunkPos chunkPos = new ChunkPos(nodePos);
        int newValue = this.ownedChunks.compute(chunkPos, (pos, old) -> (old == null ? 0 : old) + 1);
        if (newValue == 1 && isValid()) {
            this.worldData.addPipeNetToChunk(chunkPos, this);
        }
    }

    public void ensureRemovedFromChunk(BlockPos nodePos) {
        ChunkPos chunkPos = new ChunkPos(nodePos);
        int newValue = this.ownedChunks.compute(chunkPos, (pos, old) -> old == null ? 0 : old - 1);
        if (newValue == 0) {
            this.ownedChunks.remove(chunkPos);
            if (isValid()) {
                this.worldData.removePipeNetFromChunk(chunkPos, this);
            }
        }
    }

    /**
     * Called during network split when one net needs to transfer some of it's nodes to another one
     * Use this for diving old net contents according to node amount of new network
     * For example, for fluid pipes it would remove amount of fluid contained in old nodes
     * from parent network and add it to it's own tank, keeping network contents when old network is split
     * Note that it should be called when parent net doesn't have transferredNodes in allNodes already
     */
    protected void transferNodeData(Map<BlockPos, PipeNode<NodeDataType>> transferredNodes, PipeNet<NodeDataType> parentNet) {
        /*transferredNodes.forEach(this::addNodeSilently);
        onConnectionsUpdate();
        worldData.markDirty();*/
    }

    /**
     * Serializes node data into specified tag compound
     * Used for writing persistent node data
     */
    public abstract void writeNodeData(NodeDataType nodeData, NBTTagCompound tagCompound);

    /**
     * Deserializes node data from specified tag compound
     * Used for reading persistent node data
     */
    public abstract NodeDataType readNodeData(NBTTagCompound tagCompound);

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        NBTTagList nodesNBT = new NBTTagList();
        nodes.forEach(node -> nodesNBT.appendTag(node.writeNbt()));
        compound.setTag("Nodes", nodesNBT);
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.ownedChunks.clear();
        NBTTagList nodesNBT = nbt.getTagList("Nodes", 10);
        for (int i = 0; i < nodesNBT.tagCount(); i++) {
            Node<NodeDataType> node = createNode();
            node.readNbt((NBTTagCompound) nodesNBT.get(i));
            addNode(node);
        }
    }
}
