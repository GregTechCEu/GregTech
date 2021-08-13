package gregtech.api.pipenet.tile;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.metatileentity.SyncedTileEntityBase;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.util.FirstTickScheduler;
import gregtech.api.util.IFirstTickTask;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public abstract class TileEntityPipeBase<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> extends SyncedTileEntityBase implements IPipeTile<PipeType, NodeDataType>, IFirstTickTask {

    private TIntIntMap openConnectionsMap = new TIntIntHashMap();
    private int openConnections = 0;
    private boolean walked = false;

    protected int insulationColor = DEFAULT_INSULATION_COLOR;
    protected final PipeCoverableImplementation coverableImplementation = new PipeCoverableImplementation(this);
    private NodeDataType cachedNodeData;
    private BlockPipe<PipeType, NodeDataType, ?> pipeBlock;
    private PipeType pipeType = getPipeTypeClass().getEnumConstants()[0];
    private boolean detachedConversionMode;
    private boolean wasInDetachedConversionMode;

    public TileEntityPipeBase() {
        openConnectionsMap.put(AttachmentType.PIPE.ordinal(), 0);
        recomputeBlockedConnections();
    }

    public void setDetachedConversionMode(boolean detachedConversionMode) {
        this.detachedConversionMode = detachedConversionMode;
        this.wasInDetachedConversionMode = true;
    }

    public boolean wasInDetachedConversionMode() {
        return wasInDetachedConversionMode;
    }

    @Override
    public void markWalked() {
        walked = true;
    }

    @Override
    public boolean isWalked() {
        return walked;
    }

    @Override
    public void resetWalk() {
        walked = false;
    }

    public void setPipeData(BlockPipe<PipeType, NodeDataType, ?> pipeBlock, PipeType pipeType) {
        this.pipeBlock = pipeBlock;
        this.pipeType = pipeType;
        if (!getWorld().isRemote) {
            writeCustomData(-4, this::writePipeProperties);
        }
    }

    @Override
    public void transferDataFrom(IPipeTile<PipeType, NodeDataType> tileEntity) {
        this.pipeType = tileEntity.getPipeType();
        this.openConnectionsMap = tileEntity.getOpenConnectionsMap();
        this.insulationColor = tileEntity.getInsulationColor();
        if (tileEntity instanceof TileEntityPipeBase) {
            this.updateEntries.addAll(((TileEntityPipeBase<?, ?>) tileEntity).updateEntries);
        }
        tileEntity.getCoverableImplementation().transferDataTo(coverableImplementation);
        recomputeBlockedConnections();
    }

    public abstract Class<PipeType> getPipeTypeClass();

    @Override
    public abstract boolean supportsTicking();

    @Override
    public World getPipeWorld() {
        return getWorld();
    }

    @Override
    public BlockPos getPipePos() {
        return getPos();
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
        //create new tickable tile entity, transfer data, and replace it
        IPipeTile<PipeType, NodeDataType> newTile = getPipeBlock().createNewTileEntity(true);
        newTile.transferDataFrom(this);
        getWorld().setTileEntity(getPos(), (TileEntity) newTile);
        return newTile;
    }

    @Override
    public BlockPipe<PipeType, NodeDataType, ?> getPipeBlock() {
        if (pipeBlock == null) {
            Block block = getBlockState().getBlock();
            //noinspection unchecked
            this.pipeBlock = block instanceof BlockPipe ? (BlockPipe<PipeType, NodeDataType, ?>) block : null;
        }
        return pipeBlock;
    }

    @Override
    public int getOpenConnections() {
        return openConnections;
    }

    @Override
    public TIntIntMap getOpenConnectionsMap() {
        return new TIntIntHashMap(openConnectionsMap);
    }

    @Override
    public int getInsulationColor() {
        return insulationColor;
    }

    @Override
    public void setInsulationColor(int insulationColor) {
        this.insulationColor = insulationColor;
        if (!getWorld().isRemote) {
            if (!detachedConversionMode) {
                getPipeBlock().getWorldPipeNet(getWorld()).updateMark(getPos(), getCableMark());
            }
            writeCustomData(-1, buffer -> buffer.writeInt(insulationColor));
            markDirty();
        }
    }

    @Override
    public boolean isConnectionOpen(AttachmentType type, EnumFacing side) {
        int blockedConnections = openConnectionsMap.get(type.ordinal());
        return (blockedConnections & 1 << side.getIndex()) > 0;
    }

    @Override
    public void setConnectionBlocked(AttachmentType attachmentType, EnumFacing side, boolean blocked, boolean fromNeighbor) {
        // fix desync between two connections. Can happen if a pipe side is blocked, and a new pipe is placed next to it.
        TileEntity tile = getWorld().getTileEntity(getPos().offset(side));
        IPipeTile<?, ?> neighbourPipe = null;
        if (tile instanceof IPipeTile)
            neighbourPipe = (IPipeTile<?, ?>) tile;
        if (neighbourPipe != null && !fromNeighbor && attachmentType == AttachmentType.PIPE && isConnectionOpenAny(side) != neighbourPipe.isConnectionOpenAny(side.getOpposite())) {
            syncPipeConnections(attachmentType, side);
            return;
        }
        int at = attachmentType.ordinal();
        int openConnections = withSideConnectionBlocked(openConnectionsMap.get(at), side, blocked);
        this.openConnectionsMap.put(at, openConnections);
        recomputeBlockedConnections();
        if (!getWorld().isRemote) {
            if (!detachedConversionMode) {
                updateSideBlockedConnection(side);
            }
            writeCustomData(-2, buffer -> {
                buffer.writeVarInt(at);
                buffer.writeVarInt(openConnections);
            });
            markDirty();
            if (neighbourPipe != null && attachmentType == AttachmentType.PIPE && !fromNeighbor) {
                setNeighborPipeBlocked(attachmentType, side, blocked);
            }
        }
    }

    private void setNeighborPipeBlocked(AttachmentType type, EnumFacing side, boolean blocked) {
        // Block/Unblock neighbor pipe if it exists
        TileEntity te = this.getWorld().getTileEntity(this.getPipePos().offset(side));
        if (te instanceof TileEntityPipeBase) {
            TileEntityPipeBase<?, ?> pipeTe = (TileEntityPipeBase<?, ?>) te;
            pipeTe.setConnectionBlocked(type, side.getOpposite(), blocked, true);
        }
    }

    private boolean isNeighborPipeOpen(AttachmentType type, EnumFacing side) {
        TileEntity te = this.getWorld().getTileEntity(this.getPipePos().offset(side));
        if (te instanceof IPipeTile) {
            IPipeTile<?, ?> pipeTe = (IPipeTile<?, ?>) te;
            return pipeTe.isConnectionOpen(type, side.getOpposite());
        }
        return false;
    }

    private void syncPipeConnections(AttachmentType type, EnumFacing side) {
        if (!isNeighborPipeOpen(type, side) && isConnectionOpen(type, side)) {
            setNeighborPipeBlocked(type, side, false);
        } else {
            setConnectionBlocked(type, side, false, true);
        }
    }

    private void recomputeBlockedConnections() {
        int resultOpenConnections = 0;
        for (int openConnections : openConnectionsMap.values()) {
            resultOpenConnections |= openConnections;
        }
        this.openConnections = resultOpenConnections;
    }

    private void updateSideBlockedConnection(EnumFacing side) {
        if (!detachedConversionMode) {
            WorldPipeNet<?, ?> worldPipeNet = getPipeBlock().getWorldPipeNet(getWorld());
            boolean isSideOpen = false;
            int sideIndex = 1 << side.getIndex();
            for (int blockedConnections : openConnectionsMap.values()) {
                isSideOpen |= (blockedConnections & sideIndex) > 0;
            }
            worldPipeNet.updateBlockedConnections(getPos(), side, !isSideOpen);
        }
    }

    private int withSideConnectionBlocked(int blockedConnections, EnumFacing side, boolean blocked) {
        int index = 1 << side.getIndex();
        if (!blocked) {
            return blockedConnections | index;
        } else {
            return blockedConnections & ~index;
        }
    }

    @Override
    public PipeType getPipeType() {
        return pipeType;
    }

    @Override
    public NodeDataType getNodeData() {
        if (cachedNodeData == null) {
            this.cachedNodeData = getPipeBlock().createProperties(this);
        }
        return cachedNodeData;
    }

    private int getCableMark() {
        return insulationColor == DEFAULT_INSULATION_COLOR ? 0 : insulationColor;
    }

    public <T> T getCapabilityInternal(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_COVERABLE) {
            return GregtechTileCapabilities.CAPABILITY_COVERABLE.cast(getCoverableImplementation());
        }
        return super.getCapability(capability, facing);
    }

    @Nullable
    @Override
    public final <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        boolean isCoverable = capability == GregtechTileCapabilities.CAPABILITY_COVERABLE;
        CoverBehavior coverBehavior = facing == null ? null : coverableImplementation.getCoverAtSide(facing);
        T defaultValue = getCapabilityInternal(capability, facing);
        if (isCoverable) {
            return defaultValue;
        }
        if (coverBehavior == null && facing != null) {
            //boolean isBlocked = (getBlockedConnections() & 1 << facing.getIndex()) > 0;
            return isConnectionOpenAny(facing) ? defaultValue : null;
        }
        if (coverBehavior != null) {
            return coverBehavior.getCapability(capability, defaultValue);
        }
        return defaultValue;
    }

    @Override
    public final boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        super.writeToNBT(compound);
        BlockPipe<PipeType, NodeDataType, ?> pipeBlock = getPipeBlock();
        if (pipeBlock != null) {
            //noinspection ConstantConditions
            compound.setString("PipeBlock", pipeBlock.getRegistryName().toString());
        }
        compound.setInteger("PipeType", pipeType.ordinal());
        NBTTagCompound blockedConnectionsTag = new NBTTagCompound();
        for (int attachmentType : openConnectionsMap.keys()) {
            int blockedConnections = openConnectionsMap.get(attachmentType);
            blockedConnectionsTag.setInteger(Integer.toString(attachmentType), blockedConnections);
        }
        compound.setTag("BlockedConnectionsMap", blockedConnectionsTag);
        compound.setInteger("InsulationColor", insulationColor);
        this.coverableImplementation.writeToNBT(compound);
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("PipeBlock", NBT.TAG_STRING)) {
            Block block = Block.REGISTRY.getObject(new ResourceLocation(compound.getString("PipeBlock")));
            //noinspection unchecked
            this.pipeBlock = block instanceof BlockPipe ? (BlockPipe<PipeType, NodeDataType, ?>) block : null;
        }
        this.pipeType = getPipeTypeClass().getEnumConstants()[compound.getInteger("PipeType")];
        NBTTagCompound blockedConnectionsTag = compound.getCompoundTag("BlockedConnectionsMap");
        this.openConnectionsMap.clear();
        for (String attachmentTypeKey : blockedConnectionsTag.getKeySet()) {
            int attachmentType = Integer.parseInt(attachmentTypeKey);
            int blockedConnections = blockedConnectionsTag.getInteger(attachmentTypeKey);
            this.openConnectionsMap.put(attachmentType, blockedConnections);
        }
        recomputeBlockedConnections();
        this.insulationColor = compound.getInteger("InsulationColor");
        this.coverableImplementation.readFromNBT(compound);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        FirstTickScheduler.addTask(this);
    }

    @Override
    public void handleFirstTick() {
        this.coverableImplementation.onLoad();
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
        buf.writeVarInt(openConnections);
        buf.writeInt(insulationColor);
        this.coverableImplementation.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        readPipeProperties(buf);
        this.openConnections = buf.readVarInt();
        this.insulationColor = buf.readInt();
        this.coverableImplementation.readInitialSyncData(buf);
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        if (discriminator == -1) {
            this.insulationColor = buf.readInt();
            scheduleChunkForRenderUpdate();
        } else if (discriminator == -2) {
            this.openConnectionsMap.put(buf.readVarInt(), buf.readVarInt());
            recomputeBlockedConnections();
            scheduleChunkForRenderUpdate();
        } else if (discriminator == -3) {
            this.coverableImplementation.readCustomData(buf.readVarInt(), buf);
        } else if (discriminator == -4) {
            readPipeProperties(buf);
            scheduleChunkForRenderUpdate();
        }
    }

    @Override
    public void writeCoverCustomData(int id, Consumer<PacketBuffer> writer) {
        writeCustomData(-3, buffer -> {
            buffer.writeVarInt(id);
            writer.accept(buffer);
        });
    }

    @Override
    public void scheduleChunkForRenderUpdate() {
        BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    @Override
    public void notifyBlockUpdate() {
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
        getPipeBlock().updateActiveNodeStatus(getWorld(), getPos(), this);
    }

    @Override
    public void markAsDirty() {
        markDirty();
    }

    @Override
    public boolean isValidTile() {
        return !isInvalid();
    }

    @Override
    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }
}
