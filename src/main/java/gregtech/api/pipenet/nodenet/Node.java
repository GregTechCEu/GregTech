package gregtech.api.pipenet.nodenet;

import gregtech.api.pipenet.Pos;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.util.GTLog;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Node<NodeDataType> {

    private final NodeNet<NodeDataType> nodeNet;
    private final NodeDataType data;
    private final Set<IPipeTile<?, NodeDataType>> PIPES = new HashSet<>();
    private boolean junction;
    private final World world;

    public Node(NodeNet<NodeDataType> nodeNet, IPipeTile<?, NodeDataType> pipe) {
        this.nodeNet = Objects.requireNonNull(nodeNet);
        PIPES.add(Objects.requireNonNull(pipe));
        data = pipe.getNodeData();
        nodeNet.addNode(this);
        this.world = pipe.getWorld();
        this.junction = false;
    }

    public World getWorld() {
        return world;
    }

    public Set<IPipeTile<?, NodeDataType>> getPipes() {
        return PIPES;
    }

    public List<Long> getPipePositions() {
        return PIPES.stream().map(pipe -> Pos.asLong(pipe.getPos())).collect(Collectors.toList());
    }

    public final void replace(IPipeTile<?, NodeDataType> oldPipe, IPipeTile<?, NodeDataType> newPipe) {
        if (PIPES.remove(Objects.requireNonNull(oldPipe))) {
            PIPES.add(Objects.requireNonNull(newPipe));
        } else {
            GTLog.logger.warn("Tried to replace a pipe from a node, but the pipe was not in the node");
        }
    }

    public NodeNet<NodeDataType> getNodeNet() {
        return nodeNet;
    }

    public void setJunction(boolean junction) {
        this.junction = junction;
    }

    public int getWeight() {
        return PIPES.size();
    }

    public final boolean addPipe(IPipeTile<?, NodeDataType> pipe) {
        if (junction) return false;
        Objects.requireNonNull(pipe);
        if (!pipe.getNodeData().equals(this.data))
            return false;

        return PIPES.add(pipe);
    }

    public final boolean removePipe(IPipeTile<?, NodeDataType> pipe) {
        return PIPES.remove(pipe);
    }

    public NodeDataType getNodeData() {
        return data;
    }

    public final void destroy() {
        PIPES.forEach(pipe -> {
            ((TileEntityPipeBase<?, ?>) pipe).setNode(null);
        });
        PIPES.clear();
    }

    public final void mergeNode(Node<NodeDataType> other) {
        if (data.equals(other.data)) {
            for (IPipeTile<?, NodeDataType> pipe : other.PIPES) {
                ((TileEntityPipeBase<?, NodeDataType>) pipe).setNode(this);
            }
            PIPES.addAll(other.PIPES);
            other.destroy();
        }
    }

    public final boolean isJunction() {
        return junction;
    }

    public boolean isAlone() {
        return PIPES.size() == 1;
    }

    public void update() {
    }
}
