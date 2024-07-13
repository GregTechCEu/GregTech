package gregtech.api.graphnet.pipenet.physical;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.Cover;
import gregtech.api.graphnet.gather.GTGraphGatherables;
import gregtech.api.graphnet.logic.INetLogicEntry;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.metatileentity.NeighborCacheTileEntityBase;

import gregtech.api.unification.material.Material;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_PAINT;

public class PipeTileEntity extends NeighborCacheTileEntityBase implements ITickable {

    private final Object2ObjectOpenHashMap<String, NetLogicData> netLogicDatas = new Object2ObjectOpenHashMap<>();
    private final ObjectOpenHashSet<NetLogicData.LogicDataListener> listeners = new ObjectOpenHashSet<>();

    private final PipeBlock block;

    // information that is only required for determining graph topology should be stored on the tile entity level,
    // while information interacted with during graph traversal should be stored on the NetLogicData level.

    private byte connectionMask;
    private byte blockedMask;
    private int paintingColor;


    private Material frameMaterial;

    private final Set<ITickable> tickers = new ObjectOpenHashSet<>();

    protected final PipeCoverHolder covers = new PipeCoverHolder(this);
    private final Object2ObjectOpenHashMap<Capability<?>, IPipeCapabilityObject> capabilities = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<WorldPipeNetNode, PipeCapabilityWrapper> netCapabilities = new Object2ObjectOpenHashMap<>();

    private final int offset = (int) (Math.random() * 20);

    public PipeTileEntity(PipeBlock block) {
        this.block = block;
    }

    @Nullable
    public PipeTileEntity getPipeNeighbor(EnumFacing facing) {
        TileEntity tile = getNeighbor(facing);
        if (tile instanceof PipeTileEntity pipe) return pipe;
        else return null;
    }

    public ItemStack getDrop() {
        return new ItemStack(getBlockType(), 1, getBlockType().damageDropped(getBlockState()));
    }

    public long getOffsetTimer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() + offset;
    }

    // mask //

    public boolean canConnectTo(EnumFacing facing) {
        return this.getBlockType().getStructure().canConnectTo(facing, connectionMask);
    }

    public void setOpen(EnumFacing facing) {
        this.connectionMask |= 1 << facing.ordinal();
        updateActiveStatus(facing, false);
    }

    public void setClosed(EnumFacing facing) {
        this.connectionMask &= ~(1 << facing.ordinal());
        updateActiveStatus(facing, false);
    }

    public boolean isOpen(EnumFacing facing) {
        return (this.connectionMask & 1 << facing.ordinal()) > 0;
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
        this.tickers.remove(this);
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
        if (!this.isOpen(facing) && !canOpenConnection) {
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
        if (oneActive) this.setOpen(facing);
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
            if (facing == null || isOpen(facing)) {
                return pipeCapability;
            }
            return null;
        }

        T coverCapability = cover.getCapability(capability, pipeCapability);
        if (coverCapability == pipeCapability) {
            if (isOpen(facing)) {
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
    public @NotNull PipeBlock getBlockType() {
        return block;
    }

    @Override
    public void setWorld(@NotNull World worldIn) {
        if (worldIn == this.getWorld()) return;
        super.setWorld(worldIn);
        if (!worldIn.isRemote) {
            this.netLogicDatas.clear();
            this.capabilities.clear();
            this.netCapabilities.clear();
            this.listeners.forEach(NetLogicData.LogicDataListener::invalidate);
            this.listeners.clear();
            for (WorldPipeNetNode node : getBlockType().getNodesForTile(this)) {
                this.addCapabilities(node.getNet().getNewCapabilityObjects(node));
                this.netCapabilities.put(node, new PipeCapabilityWrapper(node.getNet().getTargetCapabilities()));
                String netName = node.getNet().mapName;
                netLogicDatas.put(netName, node.getData());
                var listener = node.getData().new LogicDataListener((e, r, f) ->
                        writeCustomData(GregtechDataCodes.UPDATE_PIPE_LOGIC, buf -> {
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
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<String, NetLogicData> entry : netLogicDatas.entrySet()) {
            tag.setTag(entry.getKey(), entry.getValue().serializeNBT());
        }
        buf.writeCompoundTag(tag);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        if (world.isRemote) {
            netLogicDatas.clear();
            try {
                NBTTagCompound logics = buf.readCompoundTag();
                if (logics == null) return;
                for (String name : logics.getKeySet()) {
                    NetLogicData data = new NetLogicData();
                    data.deserializeNBT((NBTTagList) logics.getTag("LogicData"));
                    netLogicDatas.put(name, data);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void receiveCustomData(int discriminator, @NotNull PacketBuffer buf) {
        if (discriminator == GregtechDataCodes.UPDATE_PIPE_LOGIC) {
            // extra check just to make sure we don't affect actual net data with our writes
            if (world.isRemote) {
                String netName = buf.readString(255);
                String identifier = buf.readString(255);
                boolean removed = buf.readBoolean();
                boolean fullChange = buf.readBoolean();
                if (removed) {
                    this.netLogicDatas.computeIfPresent(netName, (k, v) -> v.removeLogicEntry(identifier));
                } else {
                    INetLogicEntry<?, ?> logic = GTGraphGatherables.getLogicsRegistry()
                            .getOrDefault(identifier, () -> null).get();
                    logic.decode(buf, fullChange);
                    this.netLogicDatas.compute(netName, (k, v) -> {
                        if (v == null) v = new NetLogicData();
                        v.setLogicEntry(logic);
                        return v;
                    });
                }
            }
        } else {
            this.getCoverHolder().readCustomData(discriminator, buf);
        }
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
    }
}
