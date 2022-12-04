package gregtech.api.pipes;

import gregtech.api.pipes.net.Node;
import gregtech.api.pipes.net.PipeNetwork;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class PipeTE extends TileEntity implements ITickable {
    private Node node = null;

    PipeTE() {}

    @Override
    public void update() {
        // Don't tick if node is non-existent.
        if (this.node == null) {
            return;
        }

        // Updates pipe to give it a network if not assigned one
        // through connections.
        if (this.node.getParent() == null) {
            PipeNetwork newNet = new PipeNetwork();
            newNet.addNode(pos, this.node);
        }
    }

    public Node getNode() {
        return this.node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public boolean hasNode() {
        return this.node != null;
    }
}
