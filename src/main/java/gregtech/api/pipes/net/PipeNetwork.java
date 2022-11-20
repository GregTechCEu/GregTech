package gregtech.api.pipes.net;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;

public class PipeNetwork implements INBTSerializable<NBTTagCompound> {
    private final HashMap<BlockPos, Node> nodes = new HashMap<BlockPos, Node>();

    public void removeNode(BlockPos position) {
        nodes.remove(position);
    }

    public void addNode(BlockPos position, Node node) {
        nodes.put(position, node);
        node.setParent(this);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbtTagCompound) {

    }
}
