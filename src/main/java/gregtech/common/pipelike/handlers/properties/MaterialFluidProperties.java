package gregtech.common.pipelike.handlers.properties;

import gregtech.api.capability.IPropertyFluidFilter;
import gregtech.api.fluids.FluidConstants;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.logic.ChannelCountLogic;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.ThroughputLogic;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.logic.TemperatureLossFunction;
import gregtech.api.graphnet.pipenet.physical.IPipeMaterialStructure;
import gregtech.api.graphnet.pipenet.physical.IPipeStructure;
import gregtech.api.unification.material.properties.MaterialProperties;
import gregtech.api.unification.material.properties.PipeNetProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.pipelike.block.pipe.MaterialPipeStructure;
import gregtech.common.pipelike.net.fluid.FluidContainmentLogic;
import gregtech.common.pipelike.net.fluid.WorldFluidNet;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class MaterialFluidProperties implements PipeNetProperties.IPipeNetMaterialProperty, IPropertyFluidFilter {

    public static final MaterialPropertyKey<MaterialFluidProperties> KEY = new MaterialPropertyKey<>("FluidProperties");

    private final Set<FluidAttribute> containableAttributes = new ObjectOpenHashSet<>();
    private final EnumSet<FluidState> containableStates = EnumSet.of(FluidState.LIQUID);

    private final int maxFluidTemperature;
    private final int minFluidTemperature;
    private int materialMeltTemperature;

    private final long baseThroughput;
    private final float priority;

    public MaterialFluidProperties(long baseThroughput, int maxFluidTemperature, int minFluidTemperature,
                                   float priority) {
        this.baseThroughput = baseThroughput;
        this.maxFluidTemperature = maxFluidTemperature;
        this.minFluidTemperature = minFluidTemperature;
        this.priority = priority;
    }

    public MaterialFluidProperties(long baseThroughput, int maxFluidTemperature, int minFluidTemperature) {
        this(baseThroughput, maxFluidTemperature, minFluidTemperature, 2048f / baseThroughput);
    }

    public static MaterialFluidProperties createMax(long baseThroughput, int maxFluidTemperature) {
        return createMax(baseThroughput, maxFluidTemperature, 2048f / baseThroughput);
    }

    public static MaterialFluidProperties createMax(long baseThroughput, int maxFluidTemperature, float priority) {
        return new MaterialFluidProperties(baseThroughput, maxFluidTemperature,
                FluidConstants.CRYOGENIC_FLUID_THRESHOLD + 1, priority);
    }

    public static MaterialFluidProperties createMin(long baseThroughput, int minFluidTemperature) {
        return createMin(baseThroughput, minFluidTemperature, 2048f / baseThroughput);
    }

    public static MaterialFluidProperties createMin(long baseThroughput, int minFluidTemperature, float priority) {
        return new MaterialFluidProperties(baseThroughput, 0, minFluidTemperature, priority);
    }

    public static MaterialFluidProperties create(long baseThroughput) {
        return create(baseThroughput, 2048f / baseThroughput);
    }

    public static MaterialFluidProperties create(long baseThroughput, float priority) {
        return new MaterialFluidProperties(baseThroughput, 0, 0, priority);
    }

    public MaterialFluidProperties setContain(FluidState state, boolean canContain) {
        if (canContain) contain(state);
        else notContain(state);
        return this;
    }

    public MaterialFluidProperties setContain(FluidAttribute attribute, boolean canContain) {
        if (canContain) contain(attribute);
        else notContain(attribute);
        return this;
    }

    public MaterialFluidProperties contain(FluidState state) {
        this.containableStates.add(state);
        return this;
    }

    public MaterialFluidProperties contain(FluidAttribute attribute) {
        this.containableAttributes.add(attribute);
        return this;
    }

    public MaterialFluidProperties notContain(FluidState state) {
        this.containableStates.remove(state);
        return this;
    }

    public MaterialFluidProperties notContain(FluidAttribute attribute) {
        this.containableAttributes.remove(attribute);
        return this;
    }

    public boolean canContain(@NotNull FluidState state) {
        return this.containableStates.contains(state);
    }

    public boolean canContain(@NotNull FluidAttribute attribute) {
        return this.containableAttributes.contains(attribute);
    }

    @Override
    public @NotNull @UnmodifiableView Collection<@NotNull FluidAttribute> getContainedAttributes() {
        return containableAttributes;
    }

    public int getMaxFluidTemperature() {
        return maxFluidTemperature;
    }

    public int getMinFluidTemperature() {
        return minFluidTemperature;
    }

    @Override
    public MaterialPropertyKey<?> getKey() {
        return KEY;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flagIn, IPipeMaterialStructure structure) {
        tooltip.add(I18n.format("gregtech.fluid_pipe"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", getThroughput(structure)));
        tooltip.add(I18n.format("gregtech.pipe.priority",
                TextFormattingUtil.formatNumbers(getFlowPriority(structure))));
        appendTooltips(tooltip);
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        if (!properties.hasProperty(PropertyKey.WOOD)) {
            properties.ensureSet(PropertyKey.INGOT, true);
        }
        this.materialMeltTemperature = MaterialEnergyProperties.computeMaterialMeltTemperature(properties);
    }

    @Override
    @Nullable
    public WorldPipeNetNode getOrCreateFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure) {
            WorldPipeNetNode node = WorldFluidNet.getWorldNet(world).getOrCreateNode(pos);
            mutateData(node.getData(), structure);
            return node;
        }
        return null;
    }

    @Override
    public void mutateData(NetLogicData data, IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure pipe) {
            long throughput = getThroughput(structure);
            float coolingFactor = (float) Math.sqrt((double) pipe.material() / (4 + pipe.channelCount()));
            TemperatureLogic existing = data.getLogicEntryNullable(TemperatureLogic.TYPE);
            float energy = existing == null ? 0 : existing.getThermalEnergy();
            data.setLogicEntry(WeightFactorLogic.TYPE.getWith(getFlowPriority(structure)))
                    .setLogicEntry(ThroughputLogic.TYPE.getWith(throughput))
                    .setLogicEntry(FluidContainmentLogic.TYPE.getWith(containableStates, containableAttributes,
                            maxFluidTemperature))
                    .setLogicEntry(TemperatureLogic.TYPE
                            .getWith(TemperatureLossFunction.getOrCreatePipe(coolingFactor), materialMeltTemperature,
                                    minFluidTemperature, 50 * pipe.material(), null)
                            .setInitialThermalEnergy(energy));
            if (pipe.channelCount() > 1) {
                data.setLogicEntry(ChannelCountLogic.TYPE.getWith(pipe.channelCount()));
            }
        }
    }

    private long getThroughput(IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure pipe) {
            return baseThroughput * pipe.material();
        } else return baseThroughput;
    }

    private double getFlowPriority(IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure pipe) {
            return priority * (pipe.restrictive() ? 100d : 1d) * pipe.channelCount() / pipe.material();
        } else return priority;
    }

    @Override
    public @Nullable WorldPipeNetNode getFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure)
            return WorldFluidNet.getWorldNet(world).getNode(pos);
        else return null;
    }

    @Override
    public void removeFromNet(World world, BlockPos pos, IPipeStructure structure) {
        if (structure instanceof MaterialPipeStructure) {
            WorldFluidNet net = WorldFluidNet.getWorldNet(world);
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
