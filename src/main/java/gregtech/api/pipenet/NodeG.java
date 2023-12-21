package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NodeG<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends INodeData<NodeDataType>>
                  implements INBTSerializable<NBTTagCompound> {

    public static final int DEFAULT_MARK = 0;

    private final WorldPipeNetG<NodeDataType, PipeType> net;

    private NodeDataType data;
    /**
     * Specifies bitmask of active connections.
     * Active connections determine visual connections and graph edges.
     * An active connection does not always mean an edge is present.
     */
    private int activeConnections;
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

    private List<NetPath<PipeType, NodeDataType>> pathCache = null;

    public NodeG(NodeDataType data, IPipeTile<PipeType, NodeDataType> heldMTE,
                 WorldPipeNetG<NodeDataType, PipeType> net) {
        this.data = data;
        this.activeConnections = 0;
        this.blockedConnections = 0;
        this.mark = 0;
        this.isActive = false;
        this.heldMTE = new WeakReference<>(heldMTE);
        this.nodePos = heldMTE.getPipePos();
        this.net = net;
    }

    /**
     * Creates a dummy node for client-side information handling.
     * Should never be required to reference its net or position.
     */
    @SideOnly(Side.CLIENT)
    public NodeG(IPipeTile<PipeType, NodeDataType> heldMTE) {
        this.nodePos = null;
        this.net = null;
        this.data = null;
        this.heldMTE = new WeakReference<>(heldMTE);
        this.activeConnections = 0;
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

    @Nullable
    public NetGroup<PipeType, NodeDataType> getGroup() {
        return group;
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

    public Map<EnumFacing, TileEntity> getConnecteds() {
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
        return (activeConnections & 1 << facing.getIndex()) != 0;
    }

    /**
     * Should only be used with dummy nodes, otherwise go through the net.
     */
    public void setActiveConnections(int activeConnections) {
        this.activeConnections = activeConnections;
    }

    void setConnected(EnumFacing facing, boolean connect) {
        if (connect) {
            this.activeConnections |= 1 << facing.getIndex();
        } else {
            this.activeConnections &= ~(1 << facing.getIndex());
        }
        this.getHeldMTE().onConnectionChange();
    }

    public int getActiveConnections() {
        return activeConnections;
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
        this.getHeldMTE().onBlockedChange();
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
     * Ensures that the returned tile is not null nor invalid.
     */
    public IPipeTile<PipeType, NodeDataType> getHeldMTESafe() {
        IPipeTile<PipeType, NodeDataType> te = getHeldMTEUnsafe();
        if (te == null || !te.isValidTile()) {
            te = net.castTE(net.getWorld().getTileEntity(this.nodePos));
            setHeldMTE(te);
        }
        return te;
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

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("Pos", this.nodePos.toLong());
        tag.setInteger("Mark", this.mark);
        tag.setInteger("OpenConnections", this.activeConnections);
        tag.setBoolean("IsActive", this.isActive);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.mark = nbt.getInteger("Mark");
        this.activeConnections = nbt.getInteger("OpenConnections");
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
