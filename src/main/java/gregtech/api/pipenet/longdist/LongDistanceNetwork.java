package gregtech.api.pipenet.longdist;

import gregtech.api.pipenet.WorldPipeNet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class LongDistanceNetwork {

    public static LongDistanceNetwork get(World world, BlockPos pos) {
        return WorldData.get(world).getNetwork(pos);
    }

    private final LongDistancePipeType pipeType;
    private final WorldData world;
    // all pipes and endpoints in this net
    public final ObjectOpenHashSet<BlockPos> longDistancePipeBlocks = new ObjectOpenHashSet<>();
    // stores all connected endpoints, but only the first two are being used
    private final List<MetaTileEntityLongDistanceEndpoint> endpoints = new ArrayList<>();
    // all endpoint positions, for nbt
    private final List<BlockPos> endpointPoss = new ArrayList<>();
    private boolean calculating = false;

    protected LongDistanceNetwork(LongDistancePipeType pipeType, WorldData world) {
        this.pipeType = pipeType;
        this.world = world;
    }

    /**
     * Calculates one or more networks based on the given starting points.
     * For this it will start a new thread to keep the main thread free.
     */
    protected void recalculateNetwork(Collection<BlockPos> starts) {
        this.calculating = true;
        // remove the every pipe from the network
        for (BlockPos pos : this.longDistancePipeBlocks) {
            this.world.removeNetwork(pos);
        }
        for (MetaTileEntityLongDistanceEndpoint endpoint1 : this.endpoints) {
            endpoint1.invalidateLink();
        }
        this.endpoints.clear();
        this.longDistancePipeBlocks.clear();
        // start a new thread where all given starting points are being walked
        Thread thread = new Thread(new NetworkBuildThread(world, this, starts));
        thread.start();
    }

    /**
     * Called from the {@link NetworkBuildThread} to set the gathered data
     */
    protected void setData(Collection<BlockPos> pipes, List<MetaTileEntityLongDistanceEndpoint> endpoints) {
        boolean wasEmpty = this.longDistancePipeBlocks.isEmpty();
        this.calculating = false;
        this.longDistancePipeBlocks.clear();
        this.longDistancePipeBlocks.addAll(pipes);
        this.endpoints.clear();
        this.endpoints.addAll(endpoints);
        if (this.longDistancePipeBlocks.isEmpty()) {
            onDestroy();
            return;
        }
        if (wasEmpty) {
            this.world.networkList.add(this);
        }
        for (BlockPos pos : this.longDistancePipeBlocks) {
            this.world.putNetwork(pos, this);
        }
    }

    /**
     * Removes the pipe at the given position and recalculates all neighbour positions if necessary
     */
    public void onRemovePipe(BlockPos pos) {
        this.longDistancePipeBlocks.remove(pos);
        this.world.removeNetwork(pos);
        if (this.longDistancePipeBlocks.isEmpty()) {
            onDestroy();
            return;
        }
        List<BlockPos> neighbours = new ArrayList<>();
        BlockPos.PooledMutableBlockPos offsetPos = BlockPos.PooledMutableBlockPos.retain();
        for (EnumFacing facing : EnumFacing.VALUES) {
            offsetPos.setPos(pos).move(facing);
            LongDistanceNetwork network = world.getNetwork(offsetPos);
            if (network == this) {
                neighbours.add(offsetPos.toImmutable());
            }
        }
        offsetPos.release();
        if (neighbours.size() > 1) {
            recalculateNetwork(neighbours);
        }
    }

    protected void addEndpoint(MetaTileEntityLongDistanceEndpoint endpoint) {
        if (!this.endpoints.contains(endpoint)) {
            this.endpoints.add(endpoint);
        }
    }

    public void onRemoveEndpoint(MetaTileEntityLongDistanceEndpoint endpoint) {
        // invalidate all linked endpoints
        endpoint.invalidateLink();
        if (this.endpoints.remove(endpoint)) {
            for (MetaTileEntityLongDistanceEndpoint endpoint1 : this.endpoints) {
                endpoint1.invalidateLink();
            }
        }
        onRemovePipe(endpoint.getPos());
    }

    /**
     * Adds a new pipe to the network
     */
    public void onPlacePipe(BlockPos pos) {
        this.longDistancePipeBlocks.add(pos);
        this.world.putNetwork(pos, this);
    }

    /**
     * Adds a new endpoint to the network
     */
    public void onPlaceEndpoint(MetaTileEntityLongDistanceEndpoint endpoint) {
        addEndpoint(endpoint);
        this.longDistancePipeBlocks.add(endpoint.getPos());
        this.world.putNetwork(endpoint.getPos(), this);
    }

    /**
     * Merge a network into this network
     */
    protected void mergePipeNet(LongDistanceNetwork network) {
        if (getPipeType() != network.getPipeType()) {
            throw new IllegalStateException("Can't merge unequal pipe types!");
        }
        for (BlockPos pos : network.longDistancePipeBlocks) {
            this.world.putNetwork(pos, this);
            this.longDistancePipeBlocks.add(pos);
        }
        this.endpoints.addAll(network.endpoints);
        for (MetaTileEntityLongDistanceEndpoint endpoint1 : this.endpoints) {
            endpoint1.invalidateLink();
        }
        network.onDestroy();
    }

    /**
     * invalidate this network
     */
    protected void onDestroy() {
        this.longDistancePipeBlocks.clear();
        this.world.networkList.remove(this);
        for (MetaTileEntityLongDistanceEndpoint endpoint : this.endpoints) {
            endpoint.invalidateLink();
        }
        this.endpoints.clear();
    }

    public MetaTileEntityLongDistanceEndpoint getOtherEndpoint(MetaTileEntityLongDistanceEndpoint endpoint) {
        if (!isValid()) return null;
        if (this.pipeType.getMinLength() > 0) {
            for (int i = 0; i < this.endpoints.size(); i++) {
                MetaTileEntityLongDistanceEndpoint other = this.endpoints.get(i);
                if (endpoint != other && endpoint.getPos().getDistance(other.getPos().getX(), other.getPos().getY(), other.getPos().getZ()) >= this.pipeType.getMinLength()) {
                    if (i > 1) {
                        this.endpoints.remove(i);
                        this.endpoints.add(i, other);
                    }
                    return other;
                }
            }
            return null;
        }
        if (this.endpoints.get(0) == endpoint) {
            return this.endpoints.get(1);
        }
        if (this.endpoints.get(1) == endpoint) {
            return this.endpoints.get(0);
        }
        return null;
    }

    public MetaTileEntityLongDistanceEndpoint getFirstEndpoint() {
        return this.endpoints.isEmpty() ? null : this.endpoints.get(0);
    }

    public MetaTileEntityLongDistanceEndpoint getSecondEndpoint() {
        return this.endpoints.size() > 1 ? this.endpoints.get(1) : null;
    }

    public int getTotalSize() {
        return this.longDistancePipeBlocks.size();
    }

    public int getEndpointAmount() {
        return this.endpoints.size();
    }

    public int getPipeAmount() {
        return getTotalSize() - getEndpointAmount();
    }

    public boolean isValid() {
        return getEndpointAmount() > 1;
    }

    public boolean isCalculating() {
        return calculating;
    }

    public LongDistancePipeType getPipeType() {
        return pipeType;
    }

    /**
     * Stores all pipe data for a world/dimension
     */
    public static class WorldData extends WorldSavedData {

        private static final Object2ObjectOpenHashMap<World, WorldData> WORLD_DATA_MAP = new Object2ObjectOpenHashMap<>();

        public static WorldData get(World world) {
            WorldData worldData = WORLD_DATA_MAP.get(world);
            if (worldData != null) {
                return worldData;
            }
            String DATA_ID = WorldPipeNet.getDataID("long_dist_pipe", world);
            WorldData netWorldData = (WorldData) world.loadData(WorldData.class, DATA_ID);
            if (netWorldData == null) {
                netWorldData = new WorldData(DATA_ID);
                world.setData(DATA_ID, netWorldData);
                WORLD_DATA_MAP.put(world, netWorldData);
            }
            netWorldData.setWorldAndInit(world);
            return netWorldData;
        }

        // all ld pipes in this world to their respective network
        // might change to Map<Chunk, Map<BlockPos, LongDistanceNetwork>>
        private final Object2ObjectOpenHashMap<BlockPos, LongDistanceNetwork> networks = new Object2ObjectOpenHashMap<>();
        private final ObjectOpenHashSet<LongDistanceNetwork> networkList = new ObjectOpenHashSet<>();
        private WeakReference<World> world_ref = new WeakReference<>(null);

        public WorldData(String name) {
            super(name);
        }

        /**
         * set world and load all endpoints
         */
        protected void setWorldAndInit(World world) {
            if (this.world_ref.get() != world) {
                for (LongDistanceNetwork ld : this.networkList) {
                    if (!ld.endpointPoss.isEmpty()) {
                        ld.endpoints.clear();
                        for (BlockPos pos : ld.endpointPoss) {
                            MetaTileEntityLongDistanceEndpoint endpoint = MetaTileEntityLongDistanceEndpoint.tryGet(world, pos);
                            if (endpoint != null) {
                                ld.endpoints.add(endpoint);
                            }
                        }
                    }
                }
            }
            this.world_ref = new WeakReference<>(world);
        }

        public LongDistanceNetwork getNetwork(BlockPos pos) {
            return this.networks.get(pos);
        }

        private void putNetwork(BlockPos pos, LongDistanceNetwork network) {
            this.networks.put(pos, network);
            this.networkList.add(network);
        }

        private void removeNetwork(BlockPos pos) {
            this.networks.remove(pos);
        }

        @Override
        public void readFromNBT(@Nonnull NBTTagCompound nbtTagCompound) {
            this.networks.clear();
            this.networkList.clear();
            NBTTagList list = nbtTagCompound.getTagList("nets", Constants.NBT.TAG_COMPOUND);
            for (NBTBase nbt : list) {
                NBTTagCompound tag = (NBTTagCompound) nbt;
                LongDistancePipeType pipeType = LongDistancePipeType.getPipeType(tag.getString("class"));
                LongDistanceNetwork ld = pipeType.createNetwork(this);
                this.networkList.add(ld);
                NBTTagList posList = tag.getTagList("pipes", Constants.NBT.TAG_LONG);
                for (NBTBase nbtPos : posList) {
                    BlockPos pos = BlockPos.fromLong(((NBTTagLong) nbtPos).getLong());
                    networks.put(pos, ld);
                    ld.longDistancePipeBlocks.add(pos);
                }
                NBTTagList endpoints = tag.getTagList("endpoints", Constants.NBT.TAG_LONG);
                for (NBTBase nbtPos : endpoints) {
                    BlockPos pos = BlockPos.fromLong(((NBTTagLong) nbtPos).getLong());
                    ld.endpointPoss.add(pos);
                }
            }
        }

        @Nonnull
        @Override
        public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbtTagCompound) {
            NBTTagList list = new NBTTagList();
            for (LongDistanceNetwork network : this.networkList) {
                NBTTagCompound tag = new NBTTagCompound();
                list.appendTag(tag);

                String name = network.getPipeType().getName();
                Objects.requireNonNull(name);
                tag.setString("class", name);

                NBTTagList posList = new NBTTagList();
                tag.setTag("pipes", posList);
                for (BlockPos pos : network.longDistancePipeBlocks) {
                    posList.appendTag(new NBTTagLong(pos.toLong()));
                }

                NBTTagList endpoints = new NBTTagList();
                tag.setTag("endpoints", endpoints);
                for (MetaTileEntityLongDistanceEndpoint endpoint : network.endpoints) {
                    endpoints.appendTag(new NBTTagLong(endpoint.getPos().toLong()));
                }
            }
            nbtTagCompound.setTag("nets", list);
            return nbtTagCompound;
        }

        public World getWorld() {
            return this.world_ref.get();
        }
    }
}
