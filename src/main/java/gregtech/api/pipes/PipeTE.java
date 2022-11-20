package gregtech.api.pipes;

import gregtech.api.pipes.net.Node;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class PipeTE extends TileEntity implements ITickable {
    private Node node = null;

    PipeTE() {}

    @Override
    public void update() {

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
