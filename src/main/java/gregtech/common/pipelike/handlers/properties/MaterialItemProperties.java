package gregtech.common.pipelike.handlers.properties;

import gregtech.api.graphnet.logic.ChannelCountLogic;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PipeNetProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.pipelike.block.pipe.MaterialPipeStructure;
import gregtech.common.pipelike.net.item.WorldItemNet;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MaterialItemProperties implements PipeNetProperties.IPipeNetMaterialProperty {

    public static final MaterialPropertyKey<MaterialItemProperties> KEY = new MaterialPropertyKey<>("ItemProperties");

    private final long baseItemsPer10Ticks;
    private final float priority;

    public MaterialItemProperties(long baseItemsPer10Ticks, float priority) {
        this.baseItemsPer10Ticks = baseItemsPer10Ticks;
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
    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn, IPipeMaterialStructure structure) {
        tooltip.add(I18n.format("gregtech.item_pipe"));
        long items = getThroughput(structure);
        if (items % 32 != 0) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate", items * 2));
        } else {
            tooltip.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate_stacks", items / 32));
        }
        tooltip.add(I18n.format("gregtech.pipe.priority",
                TextFormattingUtil.formatNumbers(getFlowPriority(structure))));
    }

    private long getThroughput(IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure pipe) {
            return baseItemsPer10Ticks * pipe.material();
        } else return baseItemsPer10Ticks;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.WOOD)) {
            properties.ensureSet(PropertyKey.INGOT, true);
        }
    }

    @Override
    @Nullable
    public WorldPipeNode getOrCreateFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure) {
            WorldPipeNode node = WorldItemNet.getWorldNet(world).getOrCreateNode(pos);
            mutateData(node.getData(), structure);
            return node;
        }
        return null;
    }

    @Override
    public void mutateData(NetLogicData data, IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure pipe) {
            long throughput = baseItemsPer10Ticks * pipe.material();
            data.setLogicEntry(WeightFactorLogic.TYPE.getWith(getFlowPriority(structure)))
                    .setLogicEntry(ThroughputLogic.TYPE.getWith(throughput));
            if (pipe.channelCount() > 1) {
                data.setLogicEntry(ChannelCountLogic.TYPE.getWith(pipe.channelCount()));
            }
        }
    }

    private double getFlowPriority(IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure pipe) {
            return priority * (pipe.restrictive() ? 100d : 1d) * pipe.channelCount() / pipe.material();
        } else return priority;
    }

    @Override
    public @Nullable WorldPipeNode getFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure)
            return WorldItemNet.getWorldNet(world).getNode(pos);
        else return null;
    }

    @Override
    public void removeFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure) {
            WorldItemNet net = WorldItemNet.getWorldNet(world);
            NetNode node = net.getNode(pos);
            if (node != null) net.removeNode(node);
        }
    }

    @Override
    public boolean generatesStructure(IPipeStructure structure) {
        return structure.getClass() == MaterialPipeStructure.class;
    }

    @Override
    public boolean supportsStructure(IPipeStructure structure) {
        return structure instanceof MaterialPipeStructure;
    }
}
