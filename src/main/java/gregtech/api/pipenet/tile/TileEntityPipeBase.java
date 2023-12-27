package gregtech.api.pipenet.tile;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.metatileentity.NeighborCacheTileEntityBase;
import gregtech.api.metatileentity.SyncedTileEntityBase;
import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NodeG;
import gregtech.api.pipenet.WorldPipeNetG;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.unification.material.Material;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;

import static gregtech.api.capability.GregtechDataCodes.*;

public abstract class TileEntityPipeBase<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends INodeData<NodeDataType>> extends NeighborCacheTileEntityBase
                                        implements IPipeTile<PipeType, NodeDataType> {

    protected final PipeCoverableImplementation coverableImplementation = new PipeCoverableImplementation(this);
    /**
     * this - no information
     * null - neighbor is a pipe
     * te - neighbor is the stored value
     */
    private final TileEntity[] nonPipeNeighbors = new TileEntity[6];
    private boolean nonPipeNeighborsInvalidated = false;
    protected int paintingColor = -1;
    /**
     * Our node stores connection data and NodeData data
     */
    protected @Nullable NodeG<PipeType, NodeDataType> netNode;
    private BlockPipe<PipeType, NodeDataType, ?> pipeBlock;
    private PipeType pipeType = getPipeTypeClass().getEnumConstants()[0];
    @Nullable
    private Material frameMaterial;
    // set when this pipe is replaced with a ticking variant to redirect sync packets
    private TileEntityPipeBase<PipeType, NodeDataType> tickingPipe;

    private boolean nbtLoad = false;

    public TileEntityPipeBase() {
        super(false);
        invalidateNeighbors();
    }

    public void setPipeData(BlockPipe<PipeType, NodeDataType, ?> pipeBlock, PipeType pipeType) {
        this.pipeBlock = pipeBlock;
        this.pipeType = pipeType;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_PIPE_TYPE, this::writePipeProperties);
        }
    }

    @Override
    public void transferDataFrom(IPipeTile<PipeType, NodeDataType> tileEntity) {
        this.pipeType = tileEntity.getPipeType();
        this.paintingColor = tileEntity.getPaintingColor();
        this.netNode = tileEntity.getNode();
        if (tileEntity instanceof SyncedTileEntityBase pipeBase) {
            addPacketsFrom(pipeBase);
        }
        coverableImplementation.transferDataTo(tileEntity.getCoverableImplementation());
        setFrameMaterial(tileEntity.getFrameMaterial());
    }

    @Override
    public NodeG<PipeType, NodeDataType> getNode() {
        if (netNode == null) {
            if (this.getPipeWorld().isRemote)
                netNode = new NodeG<>(this);
            else netNode = this.getPipeBlock().getWorldPipeNet(this.getPipeWorld()).getOrCreateNode(this);
        }
        return netNode;
    }

    @Override
    protected void setWorldCreate(World worldIn) {
        this.setWorld(worldIn);
    }

    public abstract Class<PipeType> getPipeTypeClass();

    @Nullable
    @Override
    public Material getFrameMaterial() {
        return frameMaterial;
    }

    public void setFrameMaterial(@Nullable Material frameMaterial) {
        this.frameMaterial = frameMaterial;
        if (world != null && world.isRemote) {
            writeCustomData(UPDATE_FRAME_MATERIAL, buf -> {
                buf.writeVarInt(frameMaterial == null ? -1 : frameMaterial.getRegistry().getNetworkId());
                buf.writeVarInt(frameMaterial == null ? -1 : frameMaterial.getId());
            });
        }
    }

    @Override
    public boolean supportsTicking() {
        return this instanceof ITickable;
    }

    @Override
    public World getPipeWorld() {
        return getWorld();
    }

    @Override
    public BlockPos getPipePos() {
        return getPos();
    }

    @SuppressWarnings("ConstantConditions") // yes this CAN actually be null
    @Override
    public void markDirty() {
        if (getWorld() != null && getPos() != null) {
            getWorld().markChunkDirty(getPos(), this);
        }
    }

    @Override
    protected void invalidateNeighbors() {
        super.invalidateNeighbors();
        if (!this.nonPipeNeighborsInvalidated) {
            Arrays.fill(nonPipeNeighbors, this);
            this.nonPipeNeighborsInvalidated = true;
        }
    }

    public @Nullable TileEntity getNonPipeNeighbour(EnumFacing facing) {
        if (world == null || pos == null) return null;
        int i = facing.getIndex();
        TileEntity neighbor = this.nonPipeNeighbors[i];
        if (neighbor == null) return null;
        if (neighbor == this || (neighbor.isInvalid())) {
            neighbor = getNeighbor(facing);
            if (neighbor instanceof IPipeTile<?, ?>) neighbor = null;
            this.nonPipeNeighbors[i] = neighbor;
            this.nonPipeNeighborsInvalidated = false;
        }
        return neighbor;
    }

    @Override
    public void onNeighborChanged(@NotNull EnumFacing facing) {
        super.onNeighborChanged(facing);
        this.nonPipeNeighbors[facing.getIndex()] = this;
    }

    @Override
    public PipeCoverableImplementation getCoverableImplementation() {
        return coverableImplementation;
    }

    @Override
    public boolean canPlaceCoverOnSide(EnumFacing side) {
        return true;
    }

    @Override
    public IPipeTile<PipeType, NodeDataType> setSupportsTicking() {
        if (supportsTicking()) {
            return this;
        }
        if (this.tickingPipe != null) {
            // pipe was already set to tick before
            // reuse ticking pipe
            return this.tickingPipe;
        }
        // create new tickable tile entity, transfer data, and replace it
        TileEntityPipeBase<PipeType, NodeDataType> newTile = getPipeBlock().createNewTileEntity(true);
        if (!newTile.supportsTicking()) throw new IllegalStateException("Expected pipe to be ticking, but isn't!");
        newTile.transferDataFrom(this);
        getWorld().setTileEntity(getPos(), newTile);
        this.tickingPipe = newTile;
        return newTile;
    }

    @Override
    public BlockPipe<PipeType, NodeDataType, ?> getPipeBlock() {
        if (pipeBlock == null) {
            Block block = getBlockState().getBlock();
            // noinspection unchecked
            this.pipeBlock = block instanceof BlockPipe<?, ?, ?>blockPipe ?
                    (BlockPipe<PipeType, NodeDataType, ?>) blockPipe : null;
        }
        return pipeBlock;
    }

    @Override
    public int getConnections() {
        return this.getNode().getActiveConnections();
    }

    @Override
    public int getNumConnections() {
        int count = 0;
        int connections = getConnections();
        while (connections > 0) {
            count++;
            connections = connections & (connections - 1);
        }
        return count;
    }

    @Override
    public int getBlockedConnections() {
        return this.getNode().getBlockedConnections();
    }

    @Override
    public int getPaintingColor() {
        return isPainted() ? paintingColor : getDefaultPaintingColor();
    }

    @Override
    public void setPaintingColor(int paintingColor) {
        this.paintingColor = paintingColor;
        if (!getWorld().isRemote) {
            getPipeBlock().getWorldPipeNet(getWorld()).updateMark(getPos(), getCableMark());
            writeCustomData(UPDATE_INSULATION_COLOR, buffer -> buffer.writeInt(paintingColor));
            markDirty();
        }
    }

    @Override
    public boolean isPainted() {
        return this.paintingColor != -1;
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    public boolean isConnected(EnumFacing side) {
        return this.getNode().isConnected(side);
    }

    public static boolean isConnected(int connections, EnumFacing side) {
        return (connections & 1 << side.getIndex()) > 0;
    }

    @Override
    public void setConnection(EnumFacing side, boolean connected, boolean fromNeighbor) {
        if (!getWorld().isRemote) {
            if (isConnected(side) == connected) return;
            getPipeBlock().getWorldPipeNet(getWorld()).updateActiveConnections(getPos(), side, connected);
        }
    }

    @Override
    public void onConnectionChange() {
        if (!getWorld().isRemote) {
            this.scheduleRenderUpdate();
            this.markDirty();
            writeCustomData(UPDATE_CONNECTIONS, buffer -> buffer.writeVarInt(this.getConnections()));
        }
    }

    @Override
    public void setFaceBlocked(EnumFacing side, boolean blocked) {
        if (!getWorld().isRemote && canHaveBlockedFaces()) {
            if (isFaceBlocked(side) == blocked) return;
            getPipeBlock().getWorldPipeNet(getWorld()).updateBlockedConnections(getPos(), side, blocked);
        }
    }

    @Override
    public void onBlockedChange() {
        if (!getWorld().isRemote) {
            this.scheduleRenderUpdate();
            this.markDirty();
            writeCustomData(UPDATE_BLOCKED_CONNECTIONS, buf -> buf.writeVarInt(this.getBlockedConnections()));
        }
    }

    @Override
    public boolean isFaceBlocked(EnumFacing side) {
        return this.getNode().isBlocked(side);
    }

    public static boolean isFaceBlocked(int blockedConnections, EnumFacing side) {
        return (blockedConnections & (1 << side.getIndex())) != 0;
    }

    @Override
    public PipeType getPipeType() {
        return pipeType;
    }

    @Override
    public NodeDataType getNodeData() {
        // the only thing standing between this and a stack overflow error
        if (this.netNode == null) return getPipeBlock().createProperties(this);

        if (this.getNode().getData() == null) {
            this.getNode().setData(getPipeBlock().createProperties(this));
        }
        return this.getNode().getData();
    }

    private int getCableMark() {
        return paintingColor == -1 ? 0 : paintingColor;
    }

    /**
     * This returns open connections purely for rendering
     *
     * @return open connections
     */
    public int getVisualConnections() {
        int connections = getConnections();
        float selfThickness = getPipeType().getThickness();
        for (EnumFacing facing : EnumFacing.values()) {
            if (isConnected(facing)) {
                if (world.getTileEntity(pos.offset(facing)) instanceof IPipeTile<?, ?>pipeTile &&
                        pipeTile.isConnected(facing.getOpposite()) &&
                        pipeTile.getPipeType().getThickness() < selfThickness) {
                    connections |= 1 << (facing.getIndex() + 6);
                }
                if (getCoverableImplementation().getCoverAtSide(facing) != null) {
                    connections |= 1 << (facing.getIndex() + 12);
                }
            }
        }
        return connections;
    }

    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_COVER_HOLDER) {
            return GregtechTileCapabilities.CAPABILITY_COVER_HOLDER.cast(getCoverableImplementation());
        }
        return super.getCapability(capability, facing);
    }

    @Nullable
    @Override
    public final <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        boolean isCoverable = capability == GregtechTileCapabilities.CAPABILITY_COVER_HOLDER;
        Cover cover = facing == null ? null : coverableImplementation.getCoverAtSide(facing);
        T defaultValue;
        if (getPipeBlock() == null)
            defaultValue = null;
        else
            defaultValue = getCapabilityInternal(capability, facing);

        if (isCoverable) {
            return defaultValue;
        }
        if (cover == null && facing != null) {
            return isConnected(facing) ? defaultValue : null;
        }
        if (cover != null) {
            return cover.getCapability(capability, defaultValue);
        }
        return defaultValue;
    }

    @Override
    public final boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        BlockPipe<PipeType, NodeDataType, ?> pipeBlock = getPipeBlock();
        if (pipeBlock != null) {
            // noinspection ConstantConditions
            compound.setString("PipeBlock", pipeBlock.getRegistryName().toString());
        }
        compound.setInteger("PipeNetVersion", 2);
        compound.setInteger("PipeType", pipeType.ordinal());
        compound.setInteger("Connections", getNode().getActiveConnections());
        compound.setInteger("BlockedConnections", getNode().getBlockedConnections());
        if (isPainted()) {
            compound.setInteger("InsulationColor", paintingColor);
        }
        compound.setString("FrameMaterial", frameMaterial == null ? "" : frameMaterial.getRegistryName());
        this.coverableImplementation.writeToNBT(compound);
        return compound;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        if (this.tickingPipe != null) {
            this.tickingPipe.readFromNBT(compound);
            return;
        }
        super.readFromNBT(compound);
        if (compound.hasKey("PipeBlock", NBT.TAG_STRING)) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(compound.getString("PipeBlock")));
            // noinspection unchecked
            this.pipeBlock = block instanceof BlockPipe<?, ?, ?>blockPipe ?
                    (BlockPipe<PipeType, NodeDataType, ?>) blockPipe : null;
        }
        this.pipeType = getPipeTypeClass().getEnumConstants()[compound.getInteger("PipeType")];

        this.nbtLoad = true;
        if (compound.hasKey("Connections")) {
            this.getNode().setActiveConnections(compound.getInteger("Connections"));
        } else if (compound.hasKey("BlockedConnectionsMap")) {
            int connections = 0;
            NBTTagCompound blockedConnectionsTag = compound.getCompoundTag("BlockedConnectionsMap");
            for (String attachmentTypeKey : blockedConnectionsTag.getKeySet()) {
                int blockedConnections = blockedConnectionsTag.getInteger(attachmentTypeKey);
                connections |= blockedConnections;
            }
            this.getNode().setActiveConnections(connections);
        }
        this.getNode().setBlockedConnections(compound.getInteger("BlockedConnections"));

        if (compound.hasKey("InsulationColor")) {
            this.paintingColor = compound.getInteger("InsulationColor");
        }
        String frameMaterialName = compound.getString("FrameMaterial");
        if (!frameMaterialName.isEmpty()) {
            this.frameMaterial = GregTechAPI.materialManager.getMaterial(frameMaterialName);
        }

        this.coverableImplementation.readFromNBT(compound);
        if (this.tickingPipe != null && this.coverableImplementation.hasAnyCover()) {
            // one of the covers set the pipe to ticking, and we need to send over the rest of the covers
            this.coverableImplementation.transferDataTo(this.tickingPipe.coverableImplementation);
        }
        this.nbtLoad = false;

        if (!compound.hasKey("PipeNetVersion") && !compound.hasKey("PipeMaterial")) doOldNetSetup();
    }

    protected void doOldNetSetup() {
        for (EnumFacing facing : EnumFacing.VALUES) {
            WorldPipeNetG<NodeDataType, PipeType> net = this.getPipeBlock().getWorldPipeNet(this.getPipeWorld());
            NodeG<PipeType, NodeDataType> nodeOffset = net.getNode(this.getPipePos().offset(facing));
            if (nodeOffset == null) continue;
            if (net.isDirected()) {
                // offset node might've been read before us, so we have to cover for it.
                if (nodeOffset.isConnected(facing.getOpposite())) {
                    net.addEdge(nodeOffset, this.getNode(), null);
                    net.predicateEdge(nodeOffset, this.getNode(), facing.getOpposite());
                }
            }
            if (this.isConnected(facing)) {
                net.addEdge(this.getNode(), nodeOffset, null);
                net.predicateEdge(this.getNode(), nodeOffset, facing);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    protected void writePipeProperties(PacketBuffer buf) {
        buf.writeVarInt(pipeType.ordinal());
    }

    protected void readPipeProperties(PacketBuffer buf) {
        this.pipeType = getPipeTypeClass().getEnumConstants()[buf.readVarInt()];
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        writePipeProperties(buf);
        buf.writeVarInt(this.getNode().getActiveConnections());
        buf.writeVarInt(this.getNode().getBlockedConnections());
        buf.writeInt(paintingColor);
        buf.writeVarInt(frameMaterial == null ? -1 : frameMaterial.getRegistry().getNetworkId());
        buf.writeVarInt(frameMaterial == null ? -1 : frameMaterial.getId());
        this.coverableImplementation.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        if (this.tickingPipe != null) {
            this.tickingPipe.receiveInitialSyncData(buf);
            return;
        }
        readPipeProperties(buf);
        this.getNode().setActiveConnections(buf.readVarInt());
        this.getNode().setBlockedConnections(buf.readVarInt());
        this.paintingColor = buf.readInt();
        int registryId = buf.readVarInt();
        int frameMaterialId = buf.readVarInt();
        if (registryId >= 0 && frameMaterialId >= 0) {
            this.frameMaterial = GregTechAPI.materialManager.getRegistry(registryId).getObjectById(frameMaterialId);
        } else {
            this.frameMaterial = null;
        }
        this.coverableImplementation.readInitialSyncData(buf);
        if (this.tickingPipe != null && this.coverableImplementation.hasAnyCover()) {
            // one of the covers set the pipe to ticking, and we need to send over the rest of the covers
            this.coverableImplementation.transferDataTo(this.tickingPipe.coverableImplementation);
        }
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        if (this.tickingPipe != null) {
            this.tickingPipe.receiveCustomData(discriminator, buf);
            return;
        }
        if (discriminator == UPDATE_INSULATION_COLOR) {
            this.paintingColor = buf.readInt();
        } else if (discriminator == UPDATE_CONNECTIONS) {
            this.getNode().setActiveConnections(buf.readVarInt());
        } else if (discriminator == SYNC_COVER_IMPLEMENTATION) {
            this.coverableImplementation.readCustomData(buf.readVarInt(), buf);
        } else if (discriminator == UPDATE_PIPE_TYPE) {
            readPipeProperties(buf);
        } else if (discriminator == UPDATE_BLOCKED_CONNECTIONS) {
            this.getNode().setBlockedConnections(buf.readVarInt());
        } else if (discriminator == UPDATE_FRAME_MATERIAL) {
            int registryId = buf.readVarInt();
            int frameMaterialId = buf.readVarInt();
            if (registryId >= 0 && frameMaterialId >= 0) {
                this.frameMaterial = GregTechAPI.materialManager.getRegistry(registryId).getObjectById(frameMaterialId);
            } else {
                this.frameMaterial = null;
            }
        } else return;
        scheduleRenderUpdate();
    }

    @Override
    public void writeCoverCustomData(int id, Consumer<PacketBuffer> writer) {
        writeCustomData(SYNC_COVER_IMPLEMENTATION, buffer -> {
            buffer.writeVarInt(id);
            writer.accept(buffer);
        });
    }

    @Override
    public void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
        getPipeBlock().updateActiveNodeStatus(getWorld(), getPos(), this);
    }

    @Override
    public void markAsDirty() {
        markDirty();
        // this most notably gets called when the covers of a pipe get updated, aka the edge predicates need syncing.
        if (getWorld().isRemote || this.nbtLoad) return;
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (!isConnected(facing)) continue;
            this.getPipeBlock().getWorldPipeNet(this.getPipeWorld()).predicateEdge(this.getPipePos(), facing);
        }
    }

    @Override
    public boolean isValidTile() {
        return !isInvalid();
    }

    @Override
    public boolean shouldRefresh(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState oldState,
                                 @NotNull IBlockState newSate) {
        // always return true to ensure that the chunk marks the old MTE as invalid
        return true;
    }

    public void doExplosion(float explosionPower) {
        getWorld().setBlockToAir(getPos());
        if (!getWorld().isRemote) {
            ((WorldServer) getWorld()).spawnParticle(EnumParticleTypes.SMOKE_LARGE, getPos().getX() + 0.5,
                    getPos().getY() + 0.5, getPos().getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.0);
        }
        getWorld().createExplosion(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                explosionPower, false);
    }
}
