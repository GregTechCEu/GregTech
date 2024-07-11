package gregtech.api.graphnet.pipenetold;

import gregtech.api.graphnet.IGraphNet;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.pipenetold.block.IPipeType;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.pipenetold.tile.IPipeTile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;

public final class PipeNetNode<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends IPipeNetData<NodeDataType>, Edge extends NetEdge>
        extends NetNode {

    public static final int DEFAULT_MARK = 0;

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

    private WeakReference<IPipeTile<PipeType, NodeDataType, Edge>> heldMTE;

    private BlockPos nodePos;

    public PipeNetNode(NodeDataType data, IPipeTile<PipeType, NodeDataType, Edge> heldMTE, IGraphNet net) {
        super(net);
        this.setData(data);
        this.openConnections = 0;
        this.blockedConnections = 0;
        this.mark = 0;
        this.heldMTE = new WeakReference<>(heldMTE);
        this.nodePos = heldMTE.getPipePos();
    }

    /**
     * Creates a dummy node for client-side information handling.
     * Should never be required to reference its net, data, or position.
     */
    @SideOnly(Side.CLIENT)
    public PipeNetNode(IPipeTile<PipeType, NodeDataType, Edge> heldMTE) {
        super(null);
        this.nodePos = null;
        this.setData(null);
        this.heldMTE = new WeakReference<>(heldMTE);
        this.openConnections = 0;
        this.blockedConnections = 0;
    }

    /**
     * Creates a dummy node for fake flow edges.
     * Should never be required to reference its net, mte, or position.
     */
    public PipeNetNode(NodeDataType data) {
        super(null);
        this.nodePos = null;
        this.setData(data);
        this.heldMTE = null;
        this.openConnections = 0;
        this.blockedConnections = 0;
    }

    /**
     * For construction during NBT reading only
     */
    @ApiStatus.Internal
    public PipeNetNode(IGraphNet net) {
        super(net);
        this.heldMTE = new WeakReference<>(null);
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

    public WorldPipeNetBase<NodeDataType, PipeType, Edge> getNet() {
        return (WorldPipeNetBase<NodeDataType, PipeType, Edge>) super.getNet();
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
            te = this.getNet().castTE(this.getNet().getWorld().getTileEntity(this.nodePos));
            setHeldMTE(te);
        }
        return te;
    }

    /**
     * Ensures that the returned tile is the correct one for this position.
     */
    public IPipeTile<PipeType, NodeDataType, Edge> getHeldMTESafe() {
        IPipeTile<PipeType, NodeDataType, Edge> te = getHeldMTEUnsafe();
        IPipeTile<PipeType, NodeDataType, Edge> properTE =
                this.getNet().castTE(this.getNet().getWorld().getTileEntity(this.nodePos));
        if (te != properTE) {
            setHeldMTE(properTE);
        }
        return properTE;
    }

    public void setData(NodeDataType data) {
        super.setData(data);
    }

    @Override
    public void setData(NetLogicData data) {
        try {
            NodeDataType cast = (NodeDataType) data;
            this.setData(cast);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Cannot give a pipenet node an arbitrary data type.");
        }
    }

    @Override
    public NodeDataType getData() {
        return (NodeDataType) super.getData();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setLong("Pos", this.nodePos.toLong());
        tag.setInteger("Mark", this.mark);
        tag.setInteger("OpenConnections", this.openConnections);
        tag.setInteger("BlockedConnections", this.blockedConnections);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        this.nodePos = BlockPos.fromLong(nbt.getLong("Pos"));
        this.mark = nbt.getInteger("Mark");
        this.openConnections = nbt.getInteger("OpenConnections");
        this.blockedConnections = nbt.getInteger("BlockedConnections");
    }

    @Override
    protected Object getEquivalencyData() {
        return this.nodePos;
    }
}
