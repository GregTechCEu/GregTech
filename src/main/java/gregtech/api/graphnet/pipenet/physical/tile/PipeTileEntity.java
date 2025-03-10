package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.NetLogicEntry;
import gregtech.api.graphnet.logic.NetLogicRegistry;
import gregtech.api.graphnet.logic.NetLogicType;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.WorldPipeNet;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.physical.IInsulatable;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.graphnet.pipenet.physical.block.PipeBlock;
import gregtech.api.graphnet.pipenet.physical.block.RayTraceAABB;
import gregtech.api.metatileentity.NeighborCacheTileEntityBase;
import gregtech.api.network.PacketDataList;
import gregtech.api.unification.material.Material;
import gregtech.client.particle.GTOverheatParticle;
import gregtech.client.particle.GTParticleManager;
import gregtech.client.renderer.pipe.PipeRenderProperties;
import gregtech.client.renderer.pipe.cover.CoverRendererPackage;
import gregtech.client.renderer.pipe.cover.CoverRendererValues;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static gregtech.api.capability.GregtechDataCodes.*;

public class PipeTileEntity extends NeighborCacheTileEntityBase implements ITickable, IWorldPipeNetTile {

    public static final int DEFAULT_COLOR = 0xFFFFFFFF;

    private final Int2ObjectOpenHashMap<NetLogicData> netLogicDatas = new Int2ObjectOpenHashMap<>();
    private final Reference2ReferenceOpenHashMap<NetLogicType<?>, PendingLogicSync> pendingSyncs = new Reference2ReferenceOpenHashMap<>();
    private final ObjectOpenHashSet<NetLogicData.ListenerCallback<?>> listeners = new ObjectOpenHashSet<>();

    // this tile was loaded from datafixed NBT and needs to initialize its connections
    private boolean legacy;
    // used to prevent firing neighbor block updates during chunkload, which will cause a CME
    private boolean suppressUpdates;

    // information that is only required for determining graph topology should be stored on the tile entity level,
    // while information interacted with during graph traversal should be stored on the NetLogicData level.

    private byte connectionMask;
    private byte renderMask;
    private byte blockedMask;
    private int paintingColor = -1;

    private @Nullable Material frameMaterial;

    private final Set<ITickable> tickers = new ObjectOpenHashSet<>();

    protected final PipeCoverHolder covers = new PipeCoverHolder(this);
    private final Reference2ReferenceOpenHashMap<NetNode, PipeCapabilityWrapper> netCapabilities = new Reference2ReferenceOpenHashMap<>();

    @Nullable
    private TemperatureLogic temperatureLogic;
    @SideOnly(Side.CLIENT)
    @Nullable
    private GTOverheatParticle overheatParticle;

    private final int offset = (int) (Math.random() * 20);

    @Nullable
    public PipeTileEntity getPipeNeighbor(EnumFacing facing, boolean allowChunkloading) {
        TileEntity tile = allowChunkloading ? getNeighbor(facing) : getNeighborNoChunkloading(facing);
        if (tile instanceof PipeTileEntity pipe) return pipe;
        else return null;
    }

    public void getDrops(@NotNull NonNullList<ItemStack> drops, @NotNull IBlockState state) {
        drops.add(getMainDrop(state));
        if (getFrameMaterial() != null)
            drops.add(MetaBlocks.FRAMES.get(getFrameMaterial()).getItem(getFrameMaterial()));
    }

    @Override
    public void validate() {
        super.validate();
        scheduleRenderUpdate();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!getWorld().isRemote) {
            getBlockType().getHandler(this)
                    .removeFromNets(this.getWorld(), this.getPos(), this.getStructure());
            netCapabilities.values().forEach(PipeCapabilityWrapper::invalidate);
        } else killOverheatParticle();
        // TODO I hate this so much can someone please make it so that covers go through getDrops()?
        getCoverHolder().dropAllCovers();
    }

    public ItemStack getMainDrop(@NotNull IBlockState state) {
        return new ItemStack(state.getBlock(), 1);
    }

    public ItemStack getDrop() {
        return new ItemStack(getBlockType(), 1, getBlockType().damageDropped(getBlockState()));
    }

    public long getOffsetTimer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() + offset;
    }

    public void placedBy(ItemStack stack, EntityPlayer player) {}

    public IPipeStructure getStructure() {
        return getBlockType().getStructure();
    }

    // mask //

    public boolean canConnectTo(EnumFacing facing) {
        return this.getStructure().canConnectTo(facing, connectionMask);
    }

    public void setConnected(EnumFacing facing, boolean renderClosed) {
        this.connectionMask |= 1 << facing.ordinal();
        updateActiveStatus(facing, false);
        if (renderClosed) {
            this.renderMask |= 1 << facing.ordinal();
        } else {
            this.renderMask &= ~(1 << facing.ordinal());
        }
        syncConnected();
    }

    public void setDisconnected(EnumFacing facing) {
        this.connectionMask &= ~(1 << facing.ordinal());
        this.renderMask &= ~(1 << facing.ordinal());
        updateActiveStatus(facing, false);
        syncConnected();
    }

    private void syncConnected() {
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_CONNECTIONS, buffer -> {
                buffer.writeByte(connectionMask);
                buffer.writeByte(renderMask);
            });
        } else scheduleRenderUpdate();
        markDirty();
    }

    public boolean isConnected(EnumFacing facing) {
        return (this.connectionMask & 1 << facing.ordinal()) > 0;
    }

    public boolean isConnectedCoverAdjusted(EnumFacing facing) {
        Cover cover;
        return ((this.connectionMask & 1 << facing.ordinal()) > 0) ||
                (cover = getCoverHolder().getCoverAtSide(facing)) != null && cover.forcePipeRenderConnection();
    }

    public void setRenderClosed(EnumFacing facing, boolean closed) {
        if (closed) {
            this.renderMask |= 1 << facing.ordinal();
        } else {
            this.renderMask &= ~(1 << facing.ordinal());
        }
        syncConnected();
    }

    public boolean renderClosed(EnumFacing facing) {
        return (this.renderMask & 1 << facing.ordinal()) > 0;
    }

    public byte getConnectionMask() {
        return connectionMask;
    }

    public byte getCoverAdjustedConnectionMask() {
        byte connectionMask = this.connectionMask;
        for (EnumFacing facing : EnumFacing.VALUES) {
            Cover cover = getCoverHolder().getCoverAtSide(facing);
            if (cover != null) {
                if (cover.forcePipeRenderConnection()) connectionMask |= 1 << facing.ordinal();
            }
        }
        return connectionMask;
    }

    public void setBlocked(EnumFacing facing) {
        this.blockedMask |= 1 << facing.ordinal();
        syncBlocked();
    }

    public void setUnblocked(EnumFacing facing) {
        this.blockedMask &= ~(1 << facing.ordinal());
        syncBlocked();
    }

    private void syncBlocked() {
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_BLOCKED_CONNECTIONS, buffer -> buffer.writeByte(blockedMask));
        } else scheduleRenderUpdate();
        markDirty();
    }

    public boolean isBlocked(EnumFacing facing) {
        return (this.blockedMask & 1 << facing.ordinal()) > 0;
    }

    public byte getBlockedMask() {
        return blockedMask;
    }

    // paint //

    public int getPaintedColor() {
        return paintingColor;
    }

    public int getVisualColor() {
        return isPainted() ? paintingColor : getDefaultPaintingColor();
    }

    public void setPaintingColor(int paintingColor, boolean alphaSensitive) {
        if (!alphaSensitive) {
            paintingColor |= 0xFF000000;
        }
        this.paintingColor = paintingColor;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_PAINT, buffer -> buffer.writeInt(this.paintingColor));
            markDirty();
        } else scheduleRenderUpdate();
    }

    public boolean isPainted() {
        return this.paintingColor != -1;
    }

    public int getDefaultPaintingColor() {
        return DEFAULT_COLOR;
    }

    // frame //

    public void setFrameMaterial(@Nullable Material frameMaterial) {
        this.frameMaterial = frameMaterial;
        syncFrameMaterial();
    }

    private void syncFrameMaterial() {
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_FRAME_MATERIAL, buffer -> {
                if (frameMaterial != null) buffer.writeString(this.frameMaterial.getRegistryName());
                else buffer.writeString("");
            });
        } else scheduleRenderUpdate();
        markDirty();
    }

    public @Nullable Material getFrameMaterial() {
        return frameMaterial;
    }

    // ticking //

    public void addTicker(ITickable ticker) {
        this.tickers.add(ticker);
        // noinspection ConstantValue
        if (getWorld() != null) getWorld().tickableTileEntities.add(this);
    }

    @Override
    public void update() {
        this.tickers.forEach(ITickable::update);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        initialize();
        // since we're an instance of ITickable, we're automatically added to the tickable list just before this exact
        // moment.
        if (!this.isTicking()) {
            // check the last tile first to see if it's us, otherwise fallback to default.
            List<TileEntity> tickables = this.getWorld().tickableTileEntities;
            TileEntity last = tickables.get(tickables.size() - 1);
            if (last == this) tickables.remove(tickables.size() - 1);
            else tickables.remove(this);
        }
    }

    public void removeTicker(ITickable ticker) {
        this.tickers.remove(ticker);
        // noinspection ConstantValue
        if (!this.isTicking() && getWorld() != null) getWorld().tickableTileEntities.remove(this);
    }

    public boolean isTicking() {
        return !tickers.isEmpty();
    }

    // cover //

    @NotNull
    public PipeCoverHolder getCoverHolder() {
        return covers;
    }

    // activeness //

    @Override
    public void onNeighborChanged(@NotNull EnumFacing facing) {
        super.onNeighborChanged(facing);
        updateActiveStatus(facing, false);
    }

    /**
     * Returns a map of facings to tile entities that should have at least one of the required capabilities.
     * 
     * @param node the node for this tile entity. Used to identify the capabilities to match.
     * @return a map of facings to tile entities.
     */
    public @NotNull EnumMap<EnumFacing, TileEntity> getTargetsWithCapabilities(WorldPipeNode node) {
        PipeCapabilityWrapper wrapper = netCapabilities.get(node);
        EnumMap<EnumFacing, TileEntity> caps = new EnumMap<>(EnumFacing.class);
        if (wrapper == null) return caps;

        for (EnumFacing facing : EnumFacing.VALUES) {
            if (wrapper.isActive(facing)) {
                TileEntity tile = getNeighbor(facing);
                if (tile == null) updateActiveStatus(facing, false);
                else caps.put(facing, tile);
            }
        }
        return caps;
    }

    @Override
    public @Nullable TileEntity getTargetWithCapabilities(WorldPipeNode node, EnumFacing facing) {
        PipeCapabilityWrapper wrapper = netCapabilities.get(node);
        if (wrapper == null || !wrapper.isActive(facing)) return null;
        else return getNeighbor(facing);
    }

    @Override
    public PipeCapabilityWrapper getWrapperForNode(WorldPipeNode node) {
        return netCapabilities.get(node);
    }

    /**
     * Updates the pipe's active status based on the tile entity connected to the side.
     * 
     * @param facing            the side to check. Can be null, in which case all sides will be checked.
     * @param canOpenConnection whether the pipe is allowed to open a new connection if it finds a tile it can connect
     *                          to.
     */
    public void updateActiveStatus(@Nullable EnumFacing facing, boolean canOpenConnection) {
        if (facing == null) {
            for (EnumFacing side : EnumFacing.VALUES) {
                updateActiveStatus(side, canOpenConnection);
            }
            return;
        }
        if (!this.isConnectedCoverAdjusted(facing) && !(canOpenConnection && canConnectTo(facing))) {
            setAllIdle(facing);
            return;
        }

        TileEntity tile = getNeighbor(facing);
        if (tile == null || tile instanceof PipeTileEntity) {
            if (tile == null) setRenderClosed(facing, false);
            setAllIdle(facing);
            return;
        }

        boolean oneActive = false;
        for (var netCapability : netCapabilities.entrySet()) {
            for (Capability<?> cap : netCapability.getValue().capabilities.keySet()) {
                if (tile.hasCapability(cap, facing.getOpposite())) {
                    oneActive = true;
                    netCapability.getValue().setActive(facing);
                    break;
                }
            }
        }
        if (canOpenConnection && oneActive) this.setConnected(facing, false);
    }

    private void setAllIdle(EnumFacing facing) {
        for (var netCapability : netCapabilities.entrySet()) {
            netCapability.getValue().setIdle(facing);
        }
    }

    // capability //

    public <T> T getCapabilityCoverQuery(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        for (PipeCapabilityWrapper wrapper : netCapabilities.values()) {
            T cap = wrapper.getCapability(capability, facing);
            if (cap != null) return cap;
        }
        return null;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_COVER_HOLDER) {
            return GregtechTileCapabilities.CAPABILITY_COVER_HOLDER.cast(getCoverHolder());
        }
        T pipeCapability = null;
        for (PipeCapabilityWrapper wrapper : netCapabilities.values()) {
            if ((pipeCapability = wrapper.getCapability(capability, facing)) != null) break;
        }
        if (pipeCapability == null) pipeCapability = super.getCapability(capability, facing);
        Cover cover = facing == null ? null : getCoverHolder().getCoverAtSide(facing);
        if (cover == null) {
            if (facing == null || isConnected(facing)) {
                return pipeCapability;
            }
            return null;
        }

        T coverCapability = cover.getCapability(capability, pipeCapability);
        if (coverCapability == pipeCapability) {
            if (isConnectedCoverAdjusted(facing)) {
                return pipeCapability;
            }
            return null;
        }
        return coverCapability;
    }

    // data sync management //

    public NetLogicData getNetLogicData(int networkID) {
        return netLogicDatas.get(networkID);
    }

    public @UnmodifiableView Int2ObjectOpenHashMap<NetLogicData> getNetLogicDatas() {
        return netLogicDatas;
    }

    @Override
    public @NotNull PipeBlock getBlockType() {
        return (PipeBlock) super.getBlockType();
    }

    @Override
    public void setWorld(@NotNull World worldIn) {
        if (worldIn == this.getWorld()) return;
        super.setWorld(worldIn);
    }

    /**
     * DO NOT CALL UNLESS YOU KNOW WHAT YOU ARE DOING
     */
    @ApiStatus.Internal
    public void initialize() {
        if (!getWorld().isRemote) {
            this.netLogicDatas.clear();
            this.netCapabilities.clear();
            this.listeners.forEach(NetLogicData.ListenerCallback::retire);
            this.listeners.clear();
            for (WorldPipeNode node : PipeBlock.getNodesForTile(this)) {
                WorldPipeNet net = node.getNet();
                this.netCapabilities.put(node, net.buildCapabilityWrapper(this, node));
                int networkID = net.getNetworkID();
                netLogicDatas.put(networkID, node.getData());
                listeners.add(node.getData().addListener((e, r, f) -> markDataForSync(networkID, e, r, f)));
                for (NetLogicEntry<?, ?> entry : node.getData().getEntries()) {
                    markDataForSync(networkID, entry, false, true);
                }
                if (this.temperatureLogic == null) {
                    TemperatureLogic candidate = node.getData().getLogicEntryNullable(TemperatureLogic.TYPE);
                    if (candidate != null)
                        updateTemperatureLogic(candidate);
                }
            }
            if (this.legacy) {
                BlockPos.PooledMutableBlockPos mutablePos = BlockPos.PooledMutableBlockPos.retain();
                for (EnumFacing facing : EnumFacing.VALUES) {
                    if (this.isConnected(facing)) {
                        mutablePos.setPos(this.getPos().offset(facing));
                        TileEntity candidate = getWorld().getChunk(mutablePos)
                                .getTileEntity(mutablePos, Chunk.EnumCreateEntityType.CHECK);
                        if (candidate instanceof PipeTileEntity pipe)
                            PipeBlock.connectTile(this, pipe, facing);
                    }
                }
                mutablePos.release();
                this.legacy = false;
            }
            this.netLogicDatas.trim();
            this.netCapabilities.trim();
            this.listeners.trim();
            this.suppressUpdates = true;
            updateActiveStatus(null, false);
            this.suppressUpdates = false;
        } else {
            getBlockType(); // ensure block is cached on client for later reference
        }
    }

    private void markDataForSync(int networkID, NetLogicEntry<?, ?> entry, boolean removed, boolean fullChange) {
        // attempt to collapse multiple updates to the same data that occur before a sync packet is sent
        PendingLogicSync existing = pendingSyncs.get(entry.getType());
        if (existing != null) {
            if (removed && !existing.isRemoved()) existing.markRemoved();
            else if (!removed && existing.isRemoved()) {
                // if the previous change was a removal and then this change is not a removal,
                // then this is equivalent to a full change.
                existing.markRemoved();
                existing.markFullChange();
            }
            if (fullChange) existing.markFullChange();
        } else {
            pendingSyncs.put(entry.getType(), new PendingLogicSync(networkID, entry, removed, fullChange));
        }
        notifyWorldOfPendingPackets();
    }

    @Override
    protected void beforeUpdatePacket(PacketDataList pendingUpdates) {
        for (PendingLogicSync pendingSync : pendingSyncs.values()) {
            writeCustomData(UPDATE_PIPE_LOGIC, buf -> {
                buf.writeVarInt(pendingSync.networkID());
                buf.writeBoolean(pendingSync.isRemoved());
                if (pendingSync.isRemoved())
                    buf.writeVarInt(NetLogicRegistry.getNetworkID(pendingSync.entry().getType()));
                else NetLogicData.writeEntry(buf, pendingSync.entry(), pendingSync.isFullChange());
            });
        }
        pendingSyncs.clear();
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setByte("ConnectionMask", connectionMask);
        compound.setByte("RenderMask", renderMask);
        compound.setByte("BlockedMask", blockedMask);
        compound.setInteger("Paint", paintingColor);
        if (legacy) compound.setBoolean("Legacy", true);
        if (frameMaterial != null) compound.setString("Frame", frameMaterial.getRegistryName());
        compound.setTag("Covers", getCoverHolder().serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        connectionMask = compound.getByte("ConnectionMask");
        renderMask = compound.getByte("RenderMask");
        blockedMask = compound.getByte("BlockedMask");
        paintingColor = compound.getInteger("Paint");
        legacy = compound.getBoolean("Legacy");
        if (compound.hasKey("Frame"))
            this.frameMaterial = GregTechAPI.materialManager.getMaterial(compound.getString("Frame"));
        else this.frameMaterial = null;
        this.getCoverHolder().deserializeNBT(compound.getCompoundTag("Covers"));
    }

    protected void encodeMaterialToBuffer(@NotNull Material material, @NotNull PacketBuffer buf) {
        buf.writeVarInt(material.getRegistry().getNetworkId());
        buf.writeInt(material.getId());
    }

    protected Material decodeMaterialFromBuffer(@NotNull PacketBuffer buf) {
        return GregTechAPI.materialManager.getRegistry(buf.readVarInt()).getObjectById(buf.readInt());
    }

    public void forceFullSync() {
        writeCustomData(SYNC_EVERYTHING, this::writeInitialSyncData);
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeByte(connectionMask);
        buf.writeByte(renderMask);
        buf.writeByte(blockedMask);
        buf.writeInt(paintingColor);
        buf.writeBoolean(frameMaterial != null);
        if (frameMaterial != null) encodeMaterialToBuffer(frameMaterial, buf);
        buf.writeVarInt(netLogicDatas.size());
        for (var entry : netLogicDatas.entrySet()) {
            buf.writeVarInt(entry.getKey());
            entry.getValue().encode(buf);
        }
        this.getCoverHolder().writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        if (world.isRemote) {
            connectionMask = buf.readByte();
            renderMask = buf.readByte();
            blockedMask = buf.readByte();
            paintingColor = buf.readInt();
            if (buf.readBoolean()) frameMaterial = decodeMaterialFromBuffer(buf);
            else frameMaterial = null;
            netLogicDatas.clear();
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                int networkID = buf.readVarInt();
                NetLogicData data = new NetLogicData();
                data.decode(buf);
                netLogicDatas.put(networkID, data);
            }
            this.getCoverHolder().readInitialSyncData(buf);
        }
        scheduleRenderUpdate();
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull PacketBuffer buf) {
        if (discriminator == SYNC_EVERYTHING) {
            receiveInitialSyncData(buf);
            scheduleRenderUpdate();
        } else if (discriminator == UPDATE_PIPE_LOGIC) {
            // extra check just to make sure we don't affect actual net data with our writes
            if (world.isRemote) {
                int networkID = buf.readVarInt();
                boolean removed = buf.readBoolean();
                if (removed) {
                    NetLogicType<?> type = NetLogicRegistry.getType(buf.readVarInt());
                    NetLogicData data = this.netLogicDatas.get(networkID);
                    if (data != null) data.removeLogicEntry(type);
                } else {
                    NetLogicData data = this.netLogicDatas.computeIfAbsent(networkID, i -> new NetLogicData());
                    NetLogicEntry<?, ?> read = data.readEntry(buf);
                    if (read instanceof TemperatureLogic tempLogic) {
                        updateTemperatureLogic(tempLogic);
                    }
                }
            }
        } else if (discriminator == UPDATE_CONNECTIONS) {
            this.connectionMask = buf.readByte();
            this.renderMask = buf.readByte();
            scheduleRenderUpdate();
        } else if (discriminator == UPDATE_BLOCKED_CONNECTIONS) {
            this.blockedMask = buf.readByte();
            scheduleRenderUpdate();
        } else if (discriminator == UPDATE_FRAME_MATERIAL) {
            String name = buf.readString(255);
            if (name.equals("")) this.frameMaterial = null;
            else this.frameMaterial = GregTechAPI.materialManager.getMaterial(name);
            scheduleRenderUpdate();
        } else if (discriminator == UPDATE_PAINT) {
            this.paintingColor = buf.readInt();
            scheduleRenderUpdate();
        } else {
            this.getCoverHolder().readCustomData(discriminator, buf);
        }
    }

    // particle //

    public void updateTemperatureLogic(@NotNull TemperatureLogic logic) {
        this.temperatureLogic = logic;
        if (getWorld().isRemote) updateTemperatureLogicClient(logic);
    }

    @SideOnly(Side.CLIENT)
    protected void updateTemperatureLogicClient(@NotNull TemperatureLogic logic) {
        if (overheatParticle == null || !overheatParticle.isAlive()) {
            int temp = logic.getTemperature(logic.getLastRestorationTick());
            if (temp > GTOverheatParticle.TEMPERATURE_CUTOFF) {
                IPipeStructure structure = this.getStructure();
                overheatParticle = new GTOverheatParticle(this, logic, structure.getPipeBoxes(this),
                        structure instanceof IInsulatable i && i.isInsulated());
                GTParticleManager.INSTANCE.addEffect(overheatParticle);
            }
        } else {
            overheatParticle.setTemperatureLogic(logic);
        }
    }

    public @Nullable TemperatureLogic getTemperatureLogic() {
        return temperatureLogic;
    }

    @SideOnly(Side.CLIENT)
    public void killOverheatParticle() {
        if (overheatParticle != null) {
            overheatParticle.setExpired();
            overheatParticle = null;
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isOverheatParticleAlive() {
        return overheatParticle != null && overheatParticle.isAlive();
    }

    // misc overrides //

    @Override
    public World world() {
        return getWorld();
    }

    @Override
    public BlockPos pos() {
        return getPos();
    }

    @Override
    public void notifyBlockUpdate() {
        if (!suppressUpdates) getWorld().notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
    }

    @SuppressWarnings("ConstantConditions") // yes this CAN actually be null
    @Override
    public void markDirty() {
        if (getWorld() != null && getPos() != null) {
            getWorld().markChunkDirty(getPos(), this);
        }
    }

    @Override
    public void markAsDirty() {
        markDirty();
        // this most notably gets called when the covers of a pipe get updated, aka the edge predicates need syncing.
        for (var node : this.netCapabilities.keySet()) {
            if (node instanceof WorldPipeNode n) n.getNet().updatePredication(n, this);
        }
    }

    public static @Nullable PipeTileEntity getTileNoLoading(BlockPos pos, int dimension) {
        World world = DimensionManager.getWorld(dimension);
        if (world == null || !world.isBlockLoaded(pos)) return null;

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof PipeTileEntity pipe) return pipe;
        else return null;
    }

    /**
     * Note - the block corresponding to this tile entity must register any new unlisted properties to the default
     * state.
     */
    @SideOnly(Side.CLIENT)
    @MustBeInvokedByOverriders
    public IExtendedBlockState getRenderInformation(IExtendedBlockState state) {
        byte frameMask = 0;
        for (EnumFacing facing : EnumFacing.VALUES) {
            Cover cover = getCoverHolder().getCoverAtSide(facing);
            if (cover != null) {
                frameMask |= 1 << facing.ordinal();
            }
        }
        frameMask = (byte) ~frameMask;
        return state.withProperty(PipeRenderProperties.THICKNESS_PROPERTY, this.getStructure().getRenderThickness())
                .withProperty(PipeRenderProperties.CLOSED_MASK_PROPERTY, renderMask)
                .withProperty(PipeRenderProperties.BLOCKED_MASK_PROPERTY, blockedMask)
                .withProperty(PipeRenderProperties.COLOR_PROPERTY, getVisualColor())
                .withProperty(PipeRenderProperties.FRAME_MATERIAL_PROPERTY, frameMaterial)
                .withProperty(PipeRenderProperties.FRAME_MASK_PROPERTY, frameMask)
                .withProperty(CoverRendererPackage.CRP_PROPERTY, getCoverHolder().createPackage());
    }

    public final ItemStack getPickItem(RayTraceResult target, EntityPlayer player) {
        if (target instanceof RayTraceAABB traceAABB) {
            for (var bb : CoverRendererValues.PLATE_AABBS.entrySet()) {
                if (traceAABB.getBB().equals(bb.getValue())) {
                    // trace hit a cover box
                    Cover cover = getCoverHolder().getCoverAtSide(bb.getKey());
                    if (cover == null) break;
                    return cover.getPickItem();
                }
            }
        }
        return getPickItem(player);
    }

    protected ItemStack getPickItem(EntityPlayer player) {
        return new ItemStack(getBlockType());
    }

    @Override
    public void scheduleRenderUpdate() {
        super.scheduleRenderUpdate();
        if (getWorld().isRemote) scheduleRenderUpdateClient();
    }

    protected void scheduleRenderUpdateClient() {
        if (overheatParticle != null) overheatParticle.updatePipeBoxes(getStructure().getPipeBoxes(this));
    }

    public void getCoverBoxes(Consumer<AxisAlignedBB> consumer) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (getCoverHolder().hasCover(facing)) {
                consumer.accept(CoverRendererValues.PLATE_AABBS.get(facing));
            }
        }
    }
}
