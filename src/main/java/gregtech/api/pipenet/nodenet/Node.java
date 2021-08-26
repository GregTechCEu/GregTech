package gregtech.api.pipenet.nodenet;

import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.Pos;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * A node is a line of pipes (can have turns) where each have the exact same properties
 * If a pipe has more than 2 connections, it is a 'junction' node. They are always a separate node
 */
public class Node<NodeDataType> {

    private PipeNet<NodeDataType> pipeNet;
    private NodeDataType data;
    private final Set<Long> pipes = new HashSet<>();
    private boolean junction = false;

    public Node(PipeNet<NodeDataType> pipeNet) {
        this.pipeNet = Objects.requireNonNull(pipeNet);
        this.pipeNet.addNode(this);
    }

    public World getWorld() {
        return pipeNet.getWorldData();
    }

    public Set<Long> getPipePositions() {
        return Collections.unmodifiableSet(pipes);
    }

    /**
     * When a pipe gets assigned a new node and there already is a node, then this method is called
     *
     * @param oldNode the old node of the pipe
     */
    public void transferNodeData(Node<NodeDataType> oldNode) {
    }

    public PipeNet<NodeDataType> getPipeNet() {
        return pipeNet;
    }

    public void setJunction(boolean junction) {
        this.junction = junction;
    }

    /**
     * The weight is simply the amount of pipes in the node.
     * Very useful when calculating f.e. total loss per block
     *
     * @return weight
     */
    public int getWeight() {
        return pipes.size();
    }

    public final boolean addPipe(IPipeTile<?, NodeDataType> pipe) {
        if (junction) return false;
        Objects.requireNonNull(pipe);
        if (data == null && pipes.add(Pos.asLong(pipe.getPos()))) {
            data = pipe.getNodeData();
            pipeNet.checkAddedInChunk(pipe.getPos());
            return true;
        } else if (!pipe.getNodeData().equals(this.data))
            return false;

        if (pipes.add(Pos.asLong(pipe.getPos()))) {
            pipeNet.checkAddedInChunk(pipe.getPos());
            return true;
        }
        return false;
    }

    public final boolean removePipe(IPipeTile<?, NodeDataType> pipe) {
        return pipes.remove(pipe);
    }

    public NodeDataType getNodeData() {
        return data;
    }

    public List<IPipeTile<?, NodeDataType>> getPipes(World world) {
        List<IPipeTile<?, NodeDataType>> pipes = new ArrayList<>();
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
        for (Long rawPos : this.pipes) {
            Pos.setPos(pos, rawPos);
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IPipeTile)
                pipes.add((IPipeTile<?, NodeDataType>) tile);
        }
        return pipes;
    }

    public final void destroy() {
        getPipes(getWorld()).forEach(pipe -> {
            ((TileEntityPipeBase<?, ?>) pipe).setNode(null);
            pipeNet.ensureRemovedFromChunk(pipe.getPos());
        });
        pipes.clear();
    }

    public final void mergeNode(Node<NodeDataType> other) {
        if (data.equals(other.data)) {
            for (IPipeTile<?, NodeDataType> pipe : getPipes(getWorld())) {
                ((TileEntityPipeBase<?, NodeDataType>) pipe).setNode(this);
            }
            pipes.addAll(other.pipes);
            other.destroy();
        }
    }

    public void onSetWorld(World world) {
        BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
        for (Long rawPos : pipes) {
            Pos.setPos(pos, rawPos);
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityPipeBase) {
                ((TileEntityPipeBase<?, NodeDataType>) tile).setNode(this);
            }
        }
        pos.release();
    }

    public final boolean isJunction() {
        return junction;
    }

    public boolean isAlone() {
        return pipes.size() == 1;
    }

    public boolean containsNode(BlockPos blockPos) {
        return pipes.contains(Pos.asLong(blockPos));
    }

    public void update() {
    }

    public NBTTagCompound writeNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        pipeNet.writeNodeData(data, nbt);
        NBTTagList pipes = new NBTTagList();
        this.pipes.forEach(pipe -> {
            pipes.appendTag(new NBTTagLong(pipe));
        });
        nbt.setTag("Pipes", pipes);
        nbt.setBoolean("Junction", junction);
        return nbt;
    }

    public void readNbt(NBTTagCompound nbt) {
        NBTTagList pipes = nbt.getTagList("Pipes", 4);
        for (int i = 0; i < pipes.tagCount(); i++) {
            this.pipes.add(((NBTTagLong) pipes.get(i)).getLong());
        }
        this.data = pipeNet.readNodeData(nbt);
        setJunction(nbt.getBoolean("Junction"));
    }
}
