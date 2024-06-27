package gregtech.api.pipenet;

import gregtech.api.pipenet.alg.iter.ICacheableIterator;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;
import gregtech.api.pipenet.tile.IPipeTile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class NetNode<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends INodeData<NodeDataType>, Edge extends NetEdge>
                          implements INBTSerializable<NBTTagCompound> {

    public static final int DEFAULT_MARK = 0;

    public final WorldPipeNetBase<NodeDataType, PipeType, Edge> net;

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

    private WeakReference<IPipeTile<PipeType, NodeDataType, Edge>> heldMTE;

    private final BlockPos nodePos;

    private NetGroup<PipeType, NodeDataType, Edge> group = null;

    @Nullable
    private ICacheableIterator<NetPath<PipeType, NodeDataType, Edge>> pathCache = null;

    public NetNode(NodeDataType data, IPipeTile<PipeType, NodeDataType, Edge> heldMTE,
                   WorldPipeNetBase<NodeDataType, PipeType, Edge> net) {
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
    public NetNode(IPipeTile<PipeType, NodeDataType, Edge> heldMTE) {
        this.nodePos = null;
        this.net = null;
        this.data = null;
        this.heldMTE = new WeakReference<>(heldMTE);
        this.openConnections = 0;
        this.blockedConnections = 0;
    }

    /**
     * Creates a dummy node for fake flow edges.
     * Should never be required to reference its net, mte, or position.
     */
    public NetNode(NodeDataType data) {
        this.nodePos = null;
        this.net = null;
        this.data = data;
        this.heldMTE = null;
        this.openConnections = 0;
        this.blockedConnections = 0;
    }

    /**
     * For construction during NBT reading only
     */
    public NetNode(NBTTagCompound tag, WorldPipeNetBase<NodeDataType, PipeType, Edge> net) {
        this.nodePos = BlockPos.fromLong(tag.getLong("Pos"));
        deserializeNBT(tag);
        this.data = net.readNodeData(tag.getCompoundTag("Data"));
        this.net = net;
        this.heldMTE = new WeakReference<>(null);
    }

    public NetGroup<PipeType, NodeDataType, Edge> getGroupSafe() {
        if (this.group == null) {
            new NetGroup<>(this.net.pipeGraph, this.net).addNode(this);
            // addNodes automatically sets our group to the new group
        }
        return this.group;
    }

    @Nullable
    public NetGroup<PipeType, NodeDataType, Edge> getGroupUnsafe() {
        return this.group;
    }

    NetGroup<PipeType, NodeDataType, Edge> setGroup(NetGroup<PipeType, NodeDataType, Edge> group) {
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

    public void setHeldMTE(IPipeTile<PipeType, NodeDataType, Edge> heldMTE) {
        if (this.heldMTE.get() != heldMTE)
            this.heldMTE = new WeakReference<>(heldMTE);
    }

    /**
     * Performs no safety checks.
     */
    @Nullable
    public IPipeTile<PipeType, NodeDataType, Edge> getHeldMTEUnsafe() {
        return heldMTE.get();
    }

    /**
     * Ensures that the returned tile is not null.
     */
    public IPipeTile<PipeType, NodeDataType, Edge> getHeldMTE() {
        IPipeTile<PipeType, NodeDataType, Edge> te = getHeldMTEUnsafe();
        if (te == null) {
            te = net.castTE(net.getWorld().getTileEntity(this.nodePos));
            setHeldMTE(te);
        }
        return te;
    }

    /**
     * Ensures that the returned tile is the correct one for this position.
     */
    public IPipeTile<PipeType, NodeDataType, Edge> getHeldMTESafe() {
        IPipeTile<PipeType, NodeDataType, Edge> te = getHeldMTEUnsafe();
        IPipeTile<PipeType, NodeDataType, Edge> properTE = net.castTE(net.getWorld().getTileEntity(this.nodePos));
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

    public boolean isActive() {
        return isActive;
    }

    @Nullable
    public Iterator<NetPath<PipeType, NodeDataType, Edge>> getPathCache() {
        if (pathCache == null) return null;
        return pathCache.newIterator();
    }

    /**
     * Sets the path cache to the provided cache. Returns a new iterator from the cache for convenience.
     * 
     * @param pathCache The new cache.
     * @return The new cache.
     */
    public Iterator<NetPath<PipeType, NodeDataType, Edge>> setPathCache(
                                                                        ICacheableIterator<NetPath<PipeType, NodeDataType, Edge>> pathCache) {
        this.pathCache = pathCache;
        return getPathCache();
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
        NetNode<?, ?, ?> node = (NetNode<?, ?, ?>) o;
        return nodePos != null && Objects.equals(nodePos, node.nodePos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodePos);
    }
}
