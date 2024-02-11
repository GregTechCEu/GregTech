package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.flow.FlowChannel;
import gregtech.api.pipenet.tile.IPipeTile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NodeG<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends INodeData<NodeDataType>>
                  implements INBTSerializable<NBTTagCompound> {

    public static final int DEFAULT_MARK = 0;

    private final WorldPipeNetG<NodeDataType, PipeType> net;

    private NodeDataType data;
    /**
     * Specifies bitmask of open connections.
     * Open connections determine visual connections and graph edges.
     * An open connection does not always mean an edge is present.
     */
    private int openConnections;
    /**
     * Specifies bitmask of blocked connections.
     * Blocked connections allow flow out, but not flow in.
     * Only allowed on directed graphs.
     */
    private int blockedConnections;
    /**
     * Specifies mark of this node
     * Nodes can connect only if their marks are equal, or if
     * one of marks is default one
     */
    public int mark;
    public boolean isActive;

    private WeakReference<IPipeTile<PipeType, NodeDataType>> heldMTE;

    private final BlockPos nodePos;

    private NetGroup<PipeType, NodeDataType> group = null;

    /**
     * Stores the channels that this node is involved with. Used exclusively for flow graphs.
     */
    private final Set<FlowChannel<PipeType, NodeDataType>> channels = new ObjectOpenHashSet<>();

    private List<NetPath<PipeType, NodeDataType>> pathCache = null;

    public NodeG(NodeDataType data, IPipeTile<PipeType, NodeDataType> heldMTE,
                 WorldPipeNetG<NodeDataType, PipeType> net) {
        this.data = data;
        this.openConnections = 0;
        this.blockedConnections = 0;
        this.mark = 0;
        this.isActive = false;
        this.heldMTE = new WeakReference<>(heldMTE);
        this.nodePos = heldMTE.getPipePos();
        this.net = net;
    }

    /**
     * Creates a dummy node for client-side information handling.
     * Should never be required to reference its net, data, or position.
     */
    @SideOnly(Side.CLIENT)
    public NodeG(IPipeTile<PipeType, NodeDataType> heldMTE) {
        this.nodePos = null;
        this.net = null;
        this.data = null;
        this.heldMTE = new WeakReference<>(heldMTE);
        this.openConnections = 0;
        this.blockedConnections = 0;
    }

    /**
     * Creates a dummy node for flow network calculations.
     * Should never be required to reference its net, data, mte, or position.
     */
    public NodeG() {
        this.nodePos = null;
        this.net = null;
        this.data = null;
        this.heldMTE = new WeakReference<>(null);
        this.openConnections = 0;
        this.blockedConnections = 0;
    }

    /**
     * For construction during NBT reading only
     */
    public NodeG(NBTTagCompound tag, WorldPipeNetG<NodeDataType, PipeType> net) {
        this.nodePos = BlockPos.fromLong(tag.getLong("Pos"));
        deserializeNBT(tag);
        this.data = net.readNodeData(tag.getCompoundTag("Data"));
        this.net = net;
        this.heldMTE = new WeakReference<>(null);
    }

    public NetGroup<PipeType, NodeDataType> getGroupSafe() {
        if (this.group == null) {
            new NetGroup<>(this.net.pipeGraph, this.net).addNodes(this);
            // addNodes automatically sets our group to the new group
        }
        return this.group;
    }

    @Nullable
    public NetGroup<PipeType, NodeDataType> getGroupUnsafe() {
        return this.group;
    }

    NetGroup<PipeType, NodeDataType> setGroup(NetGroup<PipeType, NodeDataType> group) {
        this.group = group;
        return group;
    }

    void clearGroup() {
        this.group = null;
    }

    public boolean hasConnecteds() {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (!isConnected(facing)) continue;
            if (getHeldMTE().getNonPipeNeighbour(facing) != null) return true;
        }
        return false;
    }

    /**
     * Returns null if we cannot get a valid mte for our location; this happens during world load.
     */
    public Map<EnumFacing, TileEntity> getConnecteds() {
        try {
            getHeldMTE();
        } catch (Exception e) {
            return null;
        }
        Map<EnumFacing, TileEntity> map = new Object2ObjectOpenHashMap<>(6);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (!isConnected(facing)) continue;
            if (getHeldMTE().getNonPipeNeighbour(facing) != null)
                map.put(facing, getHeldMTE().getNonPipeNeighbour(facing));
        }
        return map;
    }

    public TileEntity getConnnected(EnumFacing facing) {
        return this.getHeldMTE().getNonPipeNeighbour(facing);
    }

    public boolean isConnected(EnumFacing facing) {
        return (openConnections & 1 << facing.getIndex()) != 0;
    }

    /**
     * Should only be used with dummy nodes, otherwise go through the net.
     */
    public void setOpenConnections(int openConnections) {
        this.openConnections = openConnections;
    }

    void setConnected(EnumFacing facing, boolean connect) {
        if (connect) {
            this.openConnections |= 1 << facing.getIndex();
        } else {
            this.openConnections &= ~(1 << facing.getIndex());
        }
        this.getHeldMTESafe().onConnectionChange();
        this.getGroupSafe().connectionChange(this);
    }

    public int getOpenConnections() {
        return openConnections;
    }

    public boolean isBlocked(EnumFacing facing) {
        return (blockedConnections & 1 << facing.getIndex()) != 0;
    }

    /**
     * Should only be used with dummy nodes, otherwise go through the net.
     */
    public void setBlockedConnections(int blockedConnections) {
        this.blockedConnections = blockedConnections;
    }

    void setBlocked(EnumFacing facing, boolean block) {
        if (block) {
            this.blockedConnections |= 1 << facing.getIndex();
        } else {
            this.blockedConnections &= ~(1 << facing.getIndex());
        }
        this.getHeldMTESafe().onBlockedChange();
    }

    public int getBlockedConnections() {
        return blockedConnections;
    }

    public BlockPos getNodePos() {
        return nodePos;
    }

    public long getLongPos() {
        return nodePos.toLong();
    }

    public void setHeldMTE(IPipeTile<PipeType, NodeDataType> heldMTE) {
        if (this.heldMTE.get() != heldMTE)
            this.heldMTE = new WeakReference<>(heldMTE);
    }

    /**
     * Performs no safety checks.
     */
    @Nullable
    public IPipeTile<PipeType, NodeDataType> getHeldMTEUnsafe() {
        return heldMTE.get();
    }

    /**
     * Ensures that the returned tile is not null.
     */
    public IPipeTile<PipeType, NodeDataType> getHeldMTE() {
        IPipeTile<PipeType, NodeDataType> te = getHeldMTEUnsafe();
        if (te == null) {
            te = net.castTE(net.getWorld().getTileEntity(this.nodePos));
            setHeldMTE(te);
        }
        return te;
    }

    /**
     * Ensures that the returned tile is the correct one for this position.
     */
    public IPipeTile<PipeType, NodeDataType> getHeldMTESafe() {
        IPipeTile<PipeType, NodeDataType> te = getHeldMTEUnsafe();
        IPipeTile<PipeType, NodeDataType> properTE = net.castTE(net.getWorld().getTileEntity(this.nodePos));
        if (te != properTE) {
            setHeldMTE(properTE);
        }
        return properTE;
    }

    public void setData(NodeDataType data) {
        this.data = data;
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

    /**
     * @param channel The channel to test. Can be null to check only if there is space for another channel.
     * @return {@code true} if the provided channel can be supported by the node.
     */
    public boolean canSupportChannel(FlowChannel<PipeType, NodeDataType> channel) {
        if (this.data == null) return true;
        return this.channels.size() < this.data.getChannelMaxCount() || this.channels.contains(channel);
    }

    /**
     * Adds a channel to a node's collection. Cannot go over the node's channel limit.
     * 
     * @param channel the channel to add.
     * @return {@code true} if the channel was added or was already present.
     */
    public boolean addChannel(FlowChannel<PipeType, NodeDataType> channel) {
        if (this.channels.size() < this.data.getChannelMaxCount()) {
            this.channels.add(channel);
            return true;
        }
        return this.channels.contains(channel);
    }

    /**
     * Removes a channel from a node's collection.
     * 
     * @param channel the channel to remove.
     * @return {@code true} if the channel was in the node's collection.
     */
    public boolean removeChannel(FlowChannel<PipeType, NodeDataType> channel) {
        return this.channels.remove(channel);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("Pos", this.nodePos.toLong());
        tag.setInteger("Mark", this.mark);
        tag.setInteger("OpenConnections", this.openConnections);
        tag.setInteger("BlockedConnections", this.blockedConnections);
        tag.setBoolean("IsActive", this.isActive);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.mark = nbt.getInteger("Mark");
        this.openConnections = nbt.getInteger("OpenConnections");
        this.blockedConnections = nbt.getInteger("BlockedConnections");
        this.isActive = nbt.getBoolean("IsActive");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeG<?, ?> nodeG = (NodeG<?, ?>) o;
        return nodePos != null && Objects.equals(nodePos, nodeG.nodePos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodePos);
    }
}
