package gregtech.api.pipenet.nodenet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class NodeNet<NodeDataType> {

    private final Set<Node<NodeDataType>> nodes = new HashSet<>();
    private boolean valid = true;

    public NodeNet(World world, BlockPos pos) {
        NodeNetBuilder.build(this, world, pos);
    }

    public void addNode(Node<NodeDataType> node) {
        nodes.add(node);
    }

    public void removeNode(Node<NodeDataType> node) {
        nodes.remove(node);
    }

    public void invalidate() {
        this.valid = false;
        nodes.forEach(Node::destroy);
        nodes.clear();
    }

    public boolean isValid() {
        return valid;
    }
}
