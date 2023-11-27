package gregtech.common.pipelike.optical.net;

import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.common.pipelike.optical.OpticalPipeProperties;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class OpticalPipeNet extends PipeNet<OpticalPipeProperties> {

    private final Map<BlockPos, OpticalRoutePath> NET_DATA = new Object2ObjectOpenHashMap<>();

    public OpticalPipeNet(WorldPipeNet<OpticalPipeProperties, ? extends PipeNet<OpticalPipeProperties>> world) {
        super(world);
    }

    @Nullable
    public OpticalRoutePath getNetData(BlockPos pipePos, EnumFacing facing) {
        if (NET_DATA.containsKey(pipePos)) {
            return NET_DATA.get(pipePos);
        }
        OpticalRoutePath data = OpticalNetWalker.createNetData(getWorldData(), pipePos, facing);
        if (data == OpticalNetWalker.FAILED_MARKER) {
            // walker failed, don't cache, so it tries again on next insertion
            return null;
        }

        NET_DATA.put(pipePos, data);
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        NET_DATA.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        NET_DATA.clear();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<OpticalPipeProperties>> transferredNodes,
                                    PipeNet<OpticalPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((OpticalPipeNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(OpticalPipeProperties nodeData, NBTTagCompound tagCompound) {}

    @Override
    protected OpticalPipeProperties readNodeData(NBTTagCompound tagCompound) {
        return OpticalPipeProperties.INSTANCE;
    }
}
