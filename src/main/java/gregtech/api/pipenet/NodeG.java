package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import org.jetbrains.annotations.Nullable;
import org.jgrapht.GraphPath;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NodeG<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType extends INodeData<NodeDataType>>
                  implements INBTSerializable<NBTTagCompound> {

    public static final int DEFAULT_MARK = 0;

    public NodeDataType data;
    /**
     * Specifies bitmask of blocked connections
     * Node will not connect in blocked direction in any case,
     * even if neighbour node mark matches
     */
    public int openConnections;
    /**
     * Specifies mark of this node
     * Nodes can connect only if their marks are equal, or if
     * one of marks is default one
     */
    public int mark;
    public boolean isActive;

    // TODO make weak reference
    public IPipeTile<PipeType, NodeDataType> heldMTE;
    /**
     * Connected tile entities
     */
    private Map<EnumFacing, WeakReference<TileEntity>> connecteds = new Object2ObjectOpenHashMap<>(6);

    /**
     * CANNOT BE CHANGED DURING THE LIFETIME OF A NODE OR THE GRAPH WILL BREAK (or so I've been told)
     */
    private BlockPos nodePos;

    private NetGroup<PipeType, NodeDataType> group = null;

    private List<NetPath<PipeType, NodeDataType>> pathCache = null;

    public NodeG(NodeDataType data, int openConnections, int mark, boolean isActive,
                 IPipeTile<PipeType, NodeDataType> heldMTE, BlockPos pos) {
        this.data = data;
        this.openConnections = openConnections;
        this.mark = mark;
        this.isActive = isActive;
        this.heldMTE = heldMTE;
        this.nodePos = pos;
    }

    /**
     * For construction during NBT reading only
     */
    public NodeG(NBTTagCompound tag, WorldPipeNetG<NodeDataType, PipeType> net) {
        deserializeNBT(tag);
        this.data = net.readNodeData(tag.getCompoundTag("Data"));
    }

    @Nullable
    public NetGroup<PipeType, NodeDataType> getGroup() {
        return group;
    }

    void setGroup(NetGroup<PipeType, NodeDataType> group) {
        this.group = group;
    }

    void clearGroup() {
        this.group = null;
    }

    public void addConnected(EnumFacing facing, TileEntity te) {
        this.connecteds.put(facing, new WeakReference<>(te));
    }

    public boolean hasConnecteds() {
        Optional<Boolean> o = this.connecteds.values().stream().map(a -> a.get() != null).reduce((a, b) -> a || b);
        return o.isPresent() && o.get();
    }

    public Map<EnumFacing, TileEntity> getConnecteds() {
        //noinspection DataFlowIssue
        return this.connecteds.entrySet().stream().filter(a -> a.getValue().get() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, a -> a.getValue().get()));
    }

    public TileEntity getConnnected(EnumFacing facing) {
        return this.connecteds.get(facing).get();
    }

    public boolean isBlocked(EnumFacing facing) {
        return (openConnections & 1 << facing.getIndex()) == 0;
    }

    void setBlocked(EnumFacing facing, boolean isBlocked) {
        if (!isBlocked) {
            this.openConnections |= 1 << facing.getIndex();
        } else {
            this.openConnections &= ~(1 << facing.getIndex());
        }
    }

    public BlockPos getNodePos() {
        return nodePos;
    }

    public long getLongPos() {
        return nodePos.toLong();
    }

    void sync(NodeG<PipeType, NodeDataType> node) {
        this.data = node.data;
        this.mark = node.mark;
        this.isActive = node.isActive;
        this.openConnections = node.openConnections;
        // if heldMTE is not null, then it is more up to date than the graph's version.
        if (this.heldMTE == null) {
            this.heldMTE = node.heldMTE;
        } else {
            node.heldMTE = this.heldMTE;
        }
    }

    public NodeDataType getData() {
        return data;
    }

    @Nullable
    public List<NetPath<PipeType, NodeDataType>> getPathCache() {
        return pathCache;
    }

    /**
     * Sets the path cache to the provided cache. Returns the provided cache for convenience.
     * 
     * @param pathCache The new cache.
     * @return The new cache.
     */
    public List<NetPath<PipeType, NodeDataType>> setPathCache(List<NetPath<PipeType, NodeDataType>> pathCache) {
        this.pathCache = pathCache;
        return pathCache;
    }

    public void clearPathCache() {
        this.pathCache = null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("Pos", this.nodePos.toLong());
        tag.setInteger("Mark", this.mark);
        tag.setInteger("OpenConnections", this.openConnections);
        tag.setBoolean("IsActive", this.isActive);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.nodePos = BlockPos.fromLong(nbt.getLong("Pos"));
        this.mark = nbt.getInteger("Mark");
        this.openConnections = nbt.getInteger("OpenConnections");
        this.isActive = nbt.getBoolean("IsActive");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeG<?, ?> nodeG = (NodeG<?, ?>) o;
        return Objects.equals(nodePos, nodeG.nodePos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodePos);
    }
}
