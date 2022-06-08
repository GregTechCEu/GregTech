package gregtech.api.pipenet.tile;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.metatileentity.SyncedTileEntityBase;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static gregtech.api.capability.GregtechDataCodes.*;

public abstract class TileEntityPipeBase<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> extends SyncedTileEntityBase implements IPipeTile<PipeType, NodeDataType> {

    protected final PipeCoverableImplementation coverableImplementation = new PipeCoverableImplementation(this);
    protected int paintingColor = -1;
    private int connections = 0;
    private int blockedConnections = 0;
    private NodeDataType cachedNodeData;
    private BlockPipe<PipeType, NodeDataType, ?> pipeBlock;
    private PipeType pipeType = getPipeTypeClass().getEnumConstants()[0];
    @Nullable
    private Material frameMaterial;

    public TileEntityPipeBase() {
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
        this.connections = tileEntity.getConnections();
        if (tileEntity instanceof TileEntityPipeBase) {
            this.updates.putAll(((TileEntityPipeBase<?, ?>) tileEntity).updates);
        }
        tileEntity.getCoverableImplementation().transferDataTo(coverableImplementation);
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
            writeCustomData(UPDATE_FRAME_MATERIAL, buf -> buf.writeVarInt(frameMaterial == null ? -1 : GregTechAPI.MATERIAL_REGISTRY.getIDForObject(frameMaterial)));
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
    public int getConnections() {
        return connections;
    }

    @Override
    public int getBlockedConnections() {
        return canHaveBlockedFaces() ? blockedConnections : 0;
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
        return (connections & 1 << side.getIndex()) > 0;
    }

    @Override
    public void setConnection(EnumFacing side, boolean connected, boolean fromNeighbor) {
        // fix desync between two connections. Can happen if a pipe side is blocked, and a new pipe is placed next to it.
        if (!getWorld().isRemote) {
            if (isConnected(side) == connected) {
                return;
            }
            TileEntity tile = getWorld().getTileEntity(getPos().offset(side));
            // block connections if Pipe Types do not match
            if (connected && tile instanceof IPipeTile && ((IPipeTile<?, ?>) tile).getPipeType().getClass() != this.getPipeType().getClass()) {
                return;
            }
            connections = withSideConnection(connections, side, connected);

            updateNetworkConnection(side, connected);
            writeCustomData(UPDATE_CONNECTIONS, buffer -> {
                buffer.writeVarInt(connections);
            });
            markDirty();

            if (!fromNeighbor && tile instanceof IPipeTile) {
                syncPipeConnections(side, (IPipeTile<?, ?>) tile);
            }
        }
    }

    private void syncPipeConnections(EnumFacing side, IPipeTile<?, ?> pipe) {
        EnumFacing oppositeSide = side.getOpposite();
        boolean neighbourOpen = pipe.isConnected(oppositeSide);
        if (isConnected(side) == neighbourOpen) {
            return;
        }
        if (!neighbourOpen || pipe.getCoverableImplementation().getCoverAtSide(oppositeSide) == null) {
            pipe.setConnection(oppositeSide, !neighbourOpen, true);
        }
    }

    private void updateNetworkConnection(EnumFacing side, boolean connected) {
        WorldPipeNet<?, ?> worldPipeNet = getPipeBlock().getWorldPipeNet(getWorld());
        worldPipeNet.updateBlockedConnections(getPos(), side, !connected);
    }

    protected int withSideConnection(int blockedConnections, EnumFacing side, boolean connected) {
        int index = 1 << side.getIndex();
        if (connected) {
            return blockedConnections | index;
        } else {
            return blockedConnections & ~index;
        }
    }

    @Override
    public void setFaceBlocked(EnumFacing side, boolean blocked) {
        if (!world.isRemote && canHaveBlockedFaces()) {
            blockedConnections = withSideConnection(blockedConnections, side, blocked);
            writeCustomData(UPDATE_BLOCKED_CONNECTIONS, buf -> {
                buf.writeVarInt(blockedConnections);
            });
            markDirty();
            WorldPipeNet<?, ?> worldPipeNet = getPipeBlock().getWorldPipeNet(getWorld());
            PipeNet<?> net = worldPipeNet.getNetFromPos(pos);
            if (net != null) {
                net.onPipeConnectionsUpdate();
            }
        }
    }

    @Override
    public boolean isFaceBlocked(EnumFacing side) {
        return (blockedConnections & (1 << side.getIndex())) > 0;
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
                TileEntity neighbourTile = world.getTileEntity(pos.offset(facing));
                if (neighbourTile instanceof IPipeTile) {
                    IPipeTile<?, ?> pipeTile = (IPipeTile<?, ?>) neighbourTile;
                    if (pipeTile.isConnected(facing.getOpposite()) && pipeTile.getPipeType().getThickness() < selfThickness) {
                        connections |= 1 << (facing.getIndex() + 6);
                    }
                }
                if (getCoverableImplementation().getCoverAtSide(facing) != null) {
                    connections |= 1 << (facing.getIndex() + 12);
                }
            }
        }
        return connections;
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
        T defaultValue;
        if (getPipeBlock() == null)
            defaultValue = null;
        else
            defaultValue = getCapabilityInternal(capability, facing);

        if (isCoverable) {
            return defaultValue;
        }
        if (coverBehavior == null && facing != null) {
            return isConnected(facing) ? defaultValue : null;
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
        compound.setInteger("Connections", connections);
        compound.setInteger("BlockedConnections", blockedConnections);
        if (isPainted()) {
            compound.setInteger("InsulationColor", paintingColor);
        }
        compound.setString("FrameMaterial", frameMaterial == null ? "" : frameMaterial.toString());
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

        if (compound.hasKey("Connections")) {
            connections = compound.getInteger("Connections");
        } else if (compound.hasKey("BlockedConnectionsMap")) {
            connections = 0;
            NBTTagCompound blockedConnectionsTag = compound.getCompoundTag("BlockedConnectionsMap");
            for (String attachmentTypeKey : blockedConnectionsTag.getKeySet()) {
                int blockedConnections = blockedConnectionsTag.getInteger(attachmentTypeKey);
                connections |= blockedConnections;
            }
        }
        blockedConnections = compound.getInteger("BlockedConnections");

        if (compound.hasKey("InsulationColor")) {
            this.paintingColor = compound.getInteger("InsulationColor");
        }
        String frameMaterialName = compound.getString("FrameMaterial");
        if (!frameMaterialName.isEmpty()) {
            this.frameMaterial = GregTechAPI.MATERIAL_REGISTRY.getObject(frameMaterialName);
        }
        this.coverableImplementation.readFromNBT(compound);
    }

    @Override
    public void onLoad() {
        super.onLoad();
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
        buf.writeVarInt(connections);
        buf.writeVarInt(blockedConnections);
        buf.writeInt(paintingColor);
        buf.writeVarInt(frameMaterial == null ? -1 : GregTechAPI.MATERIAL_REGISTRY.getIDForObject(frameMaterial));
        this.coverableImplementation.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        readPipeProperties(buf);
        this.connections = buf.readVarInt();
        this.blockedConnections = buf.readVarInt();
        this.paintingColor = buf.readInt();
        int frameMaterialId = buf.readVarInt();
        this.frameMaterial = frameMaterialId < 0 ? null : GregTechAPI.MATERIAL_REGISTRY.getObjectById(frameMaterialId);
        this.coverableImplementation.readInitialSyncData(buf);
    }

    @Override
    public void receiveCustomData(int discriminator, PacketBuffer buf) {
        if (discriminator == UPDATE_INSULATION_COLOR) {
            this.paintingColor = buf.readInt();
            scheduleChunkForRenderUpdate();
        } else if (discriminator == UPDATE_CONNECTIONS) {
            this.connections = buf.readVarInt();
            scheduleChunkForRenderUpdate();
        } else if (discriminator == SYNC_COVER_IMPLEMENTATION) {
            this.coverableImplementation.readCustomData(buf.readVarInt(), buf);
        } else if (discriminator == UPDATE_PIPE_TYPE) {
            readPipeProperties(buf);
            scheduleChunkForRenderUpdate();
        } else if (discriminator == UPDATE_BLOCKED_CONNECTIONS) {
            this.blockedConnections = buf.readVarInt();
            scheduleChunkForRenderUpdate();
        } else if (discriminator == UPDATE_FRAME_MATERIAL) {
            int frameMaterialId = buf.readVarInt();
            this.frameMaterial = frameMaterialId < 0 ? null : GregTechAPI.MATERIAL_REGISTRY.getObjectById(frameMaterialId);
            scheduleChunkForRenderUpdate();
        }
    }

    @Override
    public void writeCoverCustomData(int id, Consumer<PacketBuffer> writer) {
        writeCustomData(SYNC_COVER_IMPLEMENTATION, buffer -> {
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

    public void doExplosion(float explosionPower) {
        getWorld().setBlockToAir(getPos());
        if (!getWorld().isRemote) {
            ((WorldServer) getWorld()).spawnParticle(EnumParticleTypes.SMOKE_LARGE, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.0);
        }
        getWorld().createExplosion(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                explosionPower, false);
    }
}
