package gregtech.api.pipenet;

import gregtech.api.util.GTLog;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class WorldPipeNet<NodeDataType, T extends PipeNet<NodeDataType>> extends WorldSavedData {

    private WeakReference<World> worldRef = new WeakReference<>(null);
    protected List<T> pipeNets = new ArrayList<>();
    protected final Map<ChunkPos, List<T>> pipeNetsByChunk = new HashMap<>();

    public WorldPipeNet(String name) {
        super(name);
    }

    public World getWorld() {
        return this.worldRef.get();
    }

    protected void setWorldAndInit(World world) {
        if (world != this.worldRef.get()) {
            this.worldRef = new WeakReference<>(world);
            onWorldSet();
        }
    }

    public static @NotNull String getDataID(@NotNull final String baseID, @NotNull final World world) {
        // noinspection ConstantValue
        if (world == null || world.isRemote) {
            GTLog.logger.error("WorldPipeNet should only be created on the server!", new Throwable());
            // noinspection ConstantValue
            if (world == null) {
                return baseID;
            }
        }

        int dimension = world.provider.getDimension();
        return dimension == 0 ? baseID : baseID + '.' + dimension;
    }

    protected void onWorldSet() {
        this.pipeNets.forEach(PipeNet::onNodeConnectionsUpdate);
    }

    public void addNode(BlockPos nodePos, NodeDataType nodeData, int mark, int openConnections, boolean isActive) {
        T myPipeNet = null;
        Node<NodeDataType> node = new Node<>(nodeData, openConnections, mark, isActive);
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos offsetPos = nodePos.offset(facing);
            T pipeNet = getNetFromPos(offsetPos);
            Node<NodeDataType> secondNode = pipeNet == null ? null : pipeNet.getAllNodes().get(offsetPos);
            if (pipeNet != null && pipeNet.canAttachNode(nodeData) &&
                    pipeNet.canNodesConnect(secondNode, facing.getOpposite(), node, null)) {
                if (myPipeNet == null) {
                    myPipeNet = pipeNet;
                    myPipeNet.addNode(nodePos, node);
                } else if (myPipeNet != pipeNet) {
                    myPipeNet.uniteNetworks(pipeNet);
                }
            }

        }
        if (myPipeNet == null) {
            myPipeNet = createNetInstance();
            myPipeNet.addNode(nodePos, node);
            addPipeNet(myPipeNet);
            markDirty();
        }
    }

    protected void addPipeNetToChunk(ChunkPos chunkPos, T pipeNet) {
        this.pipeNetsByChunk.computeIfAbsent(chunkPos, any -> new ArrayList<>()).add(pipeNet);
    }

    protected void removePipeNetFromChunk(ChunkPos chunkPos, T pipeNet) {
        List<T> list = this.pipeNetsByChunk.get(chunkPos);
        if (list != null) {
            list.remove(pipeNet);
            if (list.isEmpty()) {
                this.pipeNetsByChunk.remove(chunkPos);
            }
        }
    }

    public void removeNode(BlockPos nodePos) {
        T pipeNet = getNetFromPos(nodePos);
        if (pipeNet != null) {
            pipeNet.removeNode(nodePos);
        }
    }

    public void updateBlockedConnections(BlockPos nodePos, EnumFacing side, boolean isBlocked) {
        T pipeNet = getNetFromPos(nodePos);
        if (pipeNet != null) {
            pipeNet.updateBlockedConnections(nodePos, side, isBlocked);
            pipeNet.onPipeConnectionsUpdate();
        }
    }

    public void updateMark(BlockPos nodePos, int newMark) {
        T pipeNet = getNetFromPos(nodePos);
        if (pipeNet != null) {
            pipeNet.updateMark(nodePos, newMark);
        }
    }

    public T getNetFromPos(BlockPos blockPos) {
        List<T> pipeNetsInChunk = pipeNetsByChunk.getOrDefault(new ChunkPos(blockPos), Collections.emptyList());
        for (T pipeNet : pipeNetsInChunk) {
            if (pipeNet.containsNode(blockPos))
                return pipeNet;
        }
        return null;
    }

    protected void addPipeNet(T pipeNet) {
        addPipeNetSilently(pipeNet);
    }

    protected void addPipeNetSilently(T pipeNet) {
        this.pipeNets.add(pipeNet);
        pipeNet.getContainedChunks().forEach(chunkPos -> addPipeNetToChunk(chunkPos, pipeNet));
        pipeNet.isValid = true;
    }

    protected void removePipeNet(T pipeNet) {
        this.pipeNets.remove(pipeNet);
        pipeNet.getContainedChunks().forEach(chunkPos -> removePipeNetFromChunk(chunkPos, pipeNet));
        pipeNet.isValid = false;
    }

    protected abstract T createNetInstance();

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.pipeNets = new ArrayList<>();
        NBTTagList allEnergyNets = nbt.getTagList("PipeNets", NBT.TAG_COMPOUND);
        for (int i = 0; i < allEnergyNets.tagCount(); i++) {
            NBTTagCompound pNetTag = allEnergyNets.getCompoundTagAt(i);
            T pipeNet = createNetInstance();
            pipeNet.deserializeNBT(pNetTag);
            addPipeNetSilently(pipeNet);
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound) {
        NBTTagList allPipeNets = new NBTTagList();
        for (T pipeNet : pipeNets) {
            NBTTagCompound pNetTag = pipeNet.serializeNBT();
            allPipeNets.appendTag(pNetTag);
        }
        compound.setTag("PipeNets", allPipeNets);
        return compound;
    }
}
