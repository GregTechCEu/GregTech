package gregtech.common.pipelike.handlers.properties;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PipeNetProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.common.pipelike.block.pipe.PipeStructure;
import gregtech.common.pipelike.net.item.WorldItemNet;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

public final class MaterialItemProperties implements PipeNetProperties.IPipeNetMaterialProperty {

    public static final MaterialPropertyKey<MaterialItemProperties> KEY = new MaterialPropertyKey<>();

    private final long baseItemsPer5Ticks;
    private final float priority;

    public MaterialItemProperties(long baseItemsPer5Ticks, float priority) {
        this.baseItemsPer5Ticks = baseItemsPer5Ticks;
        this.priority = priority;
    }

    public static MaterialItemProperties create(long baseThroughput) {
        return new MaterialItemProperties(baseThroughput, 2048f / baseThroughput);
    }

    @Override
    public MaterialPropertyKey<?> getKey() {
        return KEY;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.WOOD)) {
            properties.ensureSet(PropertyKey.INGOT, true);
        }
    }

    @Override
    public void addToNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof PipeStructure) {
            WorldPipeNetNode node = WorldItemNet.getWorldNet(world).getOrCreateNode(pos);
            mutateData(node.getData(), structure);
        }
    }

    @Override
    public void mutateData(NetLogicData data, IPipeStructure structure) {
        if (structure instanceof PipeStructure pipe) {
            long throughput = baseItemsPer5Ticks * pipe.material();
            double weight = priority * (pipe.restrictive() ? 100d : 1d) * pipe.channelCount() / pipe.material();
            data.setLogicEntry(WeightFactorLogic.INSTANCE.getWith(weight))
                    .setLogicEntry(ThroughputLogic.INSTANCE.getWith(throughput));
        }
    }

    @Override
    public @Nullable WorldPipeNetNode getFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof PipeStructure)
            return WorldItemNet.getWorldNet(world).getNode(pos);
        else return null;
    }

    @Override
    public void removeFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof PipeStructure) {
            WorldItemNet net = WorldItemNet.getWorldNet(world);
            NetNode node = net.getNode(pos);
            if (node != null) net.removeNode(node);
        }
    }

    @Override
    public boolean generatesStructure(IPipeStructure structure) {
        return structure.getClass() == PipeStructure.class;
    }
}
