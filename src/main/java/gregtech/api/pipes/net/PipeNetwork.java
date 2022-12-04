package gregtech.api.pipes.net;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;

public class PipeNetwork implements INBTSerializable<NBTTagCompound> {
    private final HashMap<BlockPos, Node> nodes = new HashMap<BlockPos, Node>();

    public PipeNetwork() {
        NetworkController.INSTANCE.register(this);
    }

    public void removeNode(BlockPos position) {
        this.nodes.remove(position);

        if (nodes.size() == 0) {
            NetworkController.INSTANCE.deregister(this);
        }
    }

    public void addNode(BlockPos position, Node node) {
        this.nodes.put(position, node);
        node.setParent(this);
    }

    public int size() {
        return this.nodes.size();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbtTagCompound) {

    }
}
