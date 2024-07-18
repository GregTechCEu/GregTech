package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.graphnet.gather.GTGraphGatherables;
import gregtech.api.graphnet.logic.INetLogicEntry;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.physical.IInsulatable;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.graphnet.pipenet.physical.block.WorldPipeBlock;
import gregtech.api.metatileentity.NeighborCacheTileEntityBase;

import gregtech.api.unification.material.Material;

import gregtech.client.particle.GTOverheatParticle;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_PAINT;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_PIPE_LOGIC;

public class PipeTileEntity extends NeighborCacheTileEntityBase implements ITickable {

    private final Object2ObjectOpenHashMap<String, NetLogicData> netLogicDatas = new Object2ObjectOpenHashMap<>();
    private final ObjectOpenHashSet<NetLogicData.LogicDataListener> listeners = new ObjectOpenHashSet<>();

    private final WorldPipeBlock block;

    // information that is only required for determining graph topology should be stored on the tile entity level,
    // while information interacted with during graph traversal should be stored on the NetLogicData level.

    private byte connectionMask;
    private byte renderMask;
    private byte blockedMask;
    private int paintingColor;


    private Material frameMaterial;

    private final Set<ITickable> tickers = new ObjectOpenHashSet<>();

    protected final PipeCoverHolder covers = new PipeCoverHolder(this);
    private final Object2ObjectOpenHashMap<Capability<?>, IPipeCapabilityObject> capabilities = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<WorldPipeNetNode, PipeCapabilityWrapper> netCapabilities = new Object2ObjectOpenHashMap<>();

    @Nullable
    private TemperatureLogic temperatureLogic;
    @SideOnly(Side.CLIENT)
    @Nullable
    private GTOverheatParticle overheatParticle;

    private final int offset = (int) (Math.random() * 20);

    public PipeTileEntity(WorldPipeBlock block) {
        this.block = block;
    }

    @Nullable
    public PipeTileEntity getPipeNeighbor(EnumFacing facing, boolean allowChunkloading) {
        TileEntity tile = allowChunkloading ? getNeighbor(facing) : getNeighborNoChunkloading(facing);
        if (tile instanceof PipeTileEntity pipe) return pipe;
        else return null;
    }

    public void getDrops(NonNullList<ItemStack> drops, @NotNull IBlockState state) {
        drops.add(this.getBlockType().getDrop(this.getWorld(), this.getPos(), state));
        if (getFrameMaterial() != null)
            drops.add(MetaBlocks.FRAMES.get(getFrameMaterial()).getItem(getFrameMaterial()));
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
    }

    public void setDisconnected(EnumFacing facing) {
        this.connectionMask &= ~(1 << facing.ordinal());
        this.renderMask &= ~(1 << facing.ordinal());
        updateActiveStatus(facing, false);
    }

    public boolean isConnected(EnumFacing facing) {
        return (this.connectionMask & 1 << facing.ordinal()) > 0;
    }

    public boolean renderClosed(EnumFacing facing) {
        return (this.renderMask & 1 << facing.ordinal()) > 0;
    }

    public byte getConnectionMask() {
        return connectionMask;
    }

    public void setBlocked(EnumFacing facing) {
        this.blockedMask |= 1 << facing.ordinal();
    }

    public void setUnblocked(EnumFacing facing) {
        this.blockedMask &= ~(1 << facing.ordinal());
    }

    public boolean isBlocked(EnumFacing facing) {
        return (this.blockedMask & 1 << facing.ordinal()) > 0;
    }

    public byte getBlockedMask() {
        return blockedMask;
    }

    // paint //

    public int getPaintingColor() {
        return isPainted() ? paintingColor : getDefaultPaintingColor();
    }

    public void setPaintingColor(int paintingColor) {
        this.paintingColor = paintingColor;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_PAINT, buffer -> buffer.writeInt(paintingColor));
            markDirty();
        }
    }

    public boolean isPainted() {
        return this.paintingColor != -1;
    }

    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    // frame //

    public void setFrameMaterial(Material frameMaterial) {
        this.frameMaterial = frameMaterial;
    }

    public Material getFrameMaterial() {
        return frameMaterial;
    }

    // ticking //

    public void addTicker(ITickable ticker) {
        this.tickers.add(ticker);
        //noinspection ConstantValue
        if (getWorld() != null) getWorld().tickableTileEntities.add(this);
    }

    @Override
    public void update() {
        this.tickers.forEach(ITickable::update);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // since we're an instance of ITickable, we're automatically added to the tickable list just before this exact moment.
        // it would theoretically be a micro optimization to just pop the last tile from the tickable list, but that's not guaranteed.
        if (!this.isTicking()) this.getWorld().tickableTileEntities.remove(this);
    }

    public void removeTicker(ITickable ticker) {
        this.tickers.remove(ticker);
        //noinspection ConstantValue
        if (!this.isTicking() && getWorld() != null) getWorld().tickableTileEntities.remove(this);
    }

    public boolean isTicking() {
        return !tickers.isEmpty();
    }

    // cover //

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
     * @param node the node for this tile entity. Used to identify the capabilities to match.
     * @return a map of facings to tile entities.
     */
    public EnumMap<EnumFacing, TileEntity> getTargetsWithCapabilities(WorldPipeNetNode node) {
        PipeCapabilityWrapper wrapper = netCapabilities.get(node);
        EnumMap<EnumFacing, TileEntity> caps = new EnumMap<>(EnumFacing.class);
        if (wrapper == null) return caps;

        for (EnumFacing facing : EnumFacing.VALUES) {
            if (wrapper.isActive(facing)) {
                TileEntity tile = getNeighbor(facing);
                if (tile == null || tile instanceof PipeTileEntity) updateActiveStatus(facing, false);
                else caps.put(facing, tile);
            }
        }
        return caps;
    }

    public void updateActiveStatus(@Nullable EnumFacing facing, boolean canOpenConnection) {
        if (facing == null) {
            for (EnumFacing side : EnumFacing.VALUES) {
                updateActiveStatus(side, canOpenConnection);
            }
            return;
        }
        if (!this.isConnected(facing) && !canOpenConnection) {
            setAllIdle(facing);
            return;
        }

        TileEntity tile = getNeighbor(facing);
        if (tile == null || tile instanceof PipeTileEntity) {
            setAllIdle(facing);
            return;
        }

        boolean oneActive = false;
        for (var netCapability : netCapabilities.entrySet()) {
            boolean oneMatch = false;
            for (Capability<?> cap : netCapability.getValue().capabilities) {
                if (tile.hasCapability(cap, facing.getOpposite())) {
                    oneMatch = true;
                    oneActive = true;
                    break;
                }
            }
            netCapability.getKey().setActive(oneMatch);
        }
        if (oneActive) this.setConnected(facing, false);
    }

    private void setAllIdle(EnumFacing facing) {
        for (var netCapability : netCapabilities.entrySet()) {
            netCapability.getValue().setIdle(facing);
            netCapability.getKey().setActive(false);
        }
    }

    // capability //

    private void addCapabilities(IPipeCapabilityObject[] capabilities) {
        for (IPipeCapabilityObject capabilityObject : capabilities) {
            capabilityObject.setTile(this);
            for (Capability<?> capability : capabilityObject.getCapabilities()) {
                this.capabilities.put(capability, capabilityObject);
            }
        }
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
        T pipeCapability = capabilities.get(capability).getCapabilityForSide(capability, facing);
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
            if (isConnected(facing)) {
                return pipeCapability;
            }
            return null;
        }
        return coverCapability;
    }

    // data sync management //

    public NetLogicData getNetLogicData(String netName) {
        return netLogicDatas.get(netName);
    }

    @Override
    public @NotNull WorldPipeBlock getBlockType() {
        return block;
    }

    @Override
    public void setWorld(@NotNull World worldIn) {
        if (worldIn == this.getWorld()) return;
        super.setWorld(worldIn);
        this.initialize(worldIn);
    }

    protected void initialize(World worldIn) {
        if (!worldIn.isRemote) {
            this.netLogicDatas.clear();
            this.capabilities.clear();
            this.netCapabilities.clear();
            this.listeners.forEach(NetLogicData.LogicDataListener::invalidate);
            this.listeners.clear();
            boolean firstNode = true;
            for (WorldPipeNetNode node : getBlockType().getNodesForTile(this)) {
                this.addCapabilities(node.getNet().getNewCapabilityObjects(node));
                this.netCapabilities.put(node, new PipeCapabilityWrapper(node.getNet().getTargetCapabilities()));
                String netName = node.getNet().mapName;
                netLogicDatas.put(netName, node.getData());
                var listener = node.getData().new LogicDataListener((e, r, f) ->
                        writeCustomData(UPDATE_PIPE_LOGIC, buf -> {
                            buf.writeString(netName);
                            buf.writeString(e.getName());
                            buf.writeBoolean(r);
                            buf.writeBoolean(f);
                            if (!r) {
                                e.encode(buf);
                            }
                        }));
                this.listeners.add(listener);
                node.getData().addListener(listener);
                if (firstNode) {
                    firstNode = false;
                    this.temperatureLogic = node.getData().getLogicEntryNullable(TemperatureLogic.INSTANCE);
                }
                node.getNet().updatePredication(node, this);
            }
            this.netLogicDatas.trim();
            this.listeners.trim();
            this.capabilities.trim();
            this.netCapabilities.trim();
        }
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setByte("ConnectionMask", connectionMask);
        compound.setByte("BlockedMask", blockedMask);
        compound.setInteger("Paint", paintingColor);
        compound.setString("Frame", frameMaterial.getRegistryName());
        compound.setTag("Covers", getCoverHolder().serializeNBT());
        return compound;
    }

    @Override
    public void readFromNBT(@NotNull NBTTagCompound compound) {
        super.readFromNBT(compound);
        connectionMask = compound.getByte("ConnectionMask");
        blockedMask = compound.getByte("BlockedMask");
        paintingColor = compound.getInteger("Paint");
        this.frameMaterial = GregTechAPI.materialManager.getMaterial(compound.getString("Frame"));
        this.getCoverHolder().deserializeNBT(compound.getCompoundTag("Covers"));
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeVarInt(netLogicDatas.size());
        for (Map.Entry<String, NetLogicData> entry : netLogicDatas.entrySet()) {
            buf.writeString(entry.getKey());
            entry.getValue().encode(buf);
        }
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        if (world.isRemote) {
            netLogicDatas.clear();
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                String key = buf.readString(255);
                NetLogicData data = new NetLogicData();
                data.decode(buf);
                netLogicDatas.put(key, data);
            }
        }
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull PacketBuffer buf) {
        if (discriminator == UPDATE_PIPE_LOGIC) {
            // extra check just to make sure we don't affect actual net data with our writes
            if (world.isRemote) {
                String netName = buf.readString(255);
                String identifier = buf.readString(255);
                boolean removed = buf.readBoolean();
                boolean fullChange = buf.readBoolean();
                if (removed) {
                    this.netLogicDatas.computeIfPresent(netName, (k, v) -> v.removeLogicEntry(identifier));
                } else {
                    if (fullChange) {
                        INetLogicEntry<?, ?> logic = GTGraphGatherables.getLogicsRegistry()
                                .getOrDefault(identifier, () -> null).get();
                        logic.decode(buf, fullChange);
                        this.netLogicDatas.compute(netName, (k, v) -> {
                            if (v == null) v = new NetLogicData();
                            v.setLogicEntry(logic);
                            return v;
                        });
                    } else {
                        NetLogicData data = this.netLogicDatas.get(netName);
                        if (data != null) {
                            INetLogicEntry<?, ?> entry = data.getLogicEntryNullable(identifier);
                            if (entry != null) entry.decode(buf);
                        } else return;
                    }
                    if (identifier.equals(TemperatureLogic.INSTANCE.getName())) {
                        TemperatureLogic tempLogic = this.netLogicDatas.get(netName)
                                .getLogicEntryNullable(TemperatureLogic.INSTANCE);
                        if (tempLogic != null) updateTemperatureLogic(tempLogic);
                    }
                }
            }
        } else if (discriminator == UPDATE_PAINT) {
            this.paintingColor = buf.readInt();
        } else {
            this.getCoverHolder().readCustomData(discriminator, buf);
        }
    }

    // particle //

    public void updateTemperatureLogic(@NotNull TemperatureLogic logic) {
        if (overheatParticle == null || !overheatParticle.isAlive()) {
            long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
            int temp = logic.getTemperature(tick);
            if (temp > GTOverheatParticle.TEMPERATURE_CUTOFF) {
                IPipeStructure structure = this.getStructure();
                overheatParticle = new GTOverheatParticle(this, logic, structure.getPipeBoxes(this),
                        structure instanceof IInsulatable i && i.isInsulated());
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
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
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
            node.getNet().updatePredication(node, this);
        }
    }

    public static @Nullable PipeTileEntity getTileNoLoading(BlockPos pos, int dimension) {
        World world = DimensionManager.getWorld(dimension);
        if (world == null || !world.isBlockLoaded(pos)) return null;

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof PipeTileEntity pipe) return pipe;
        else return null;
    }
}
