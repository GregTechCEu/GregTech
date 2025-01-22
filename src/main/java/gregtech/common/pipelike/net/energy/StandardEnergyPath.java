package gregtech.common.pipelike.net.energy;

import gregtech.api.graphnet.group.NetGroup;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.logic.WeightFactorLogic;
import gregtech.api.graphnet.net.NetEdge;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.path.PathBuilder;
import gregtech.api.graphnet.path.SingletonNetPath;
import gregtech.api.graphnet.path.StandardNetPath;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraftforge.fml.common.FMLCommonHandler;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class StandardEnergyPath extends StandardNetPath implements EnergyPath {

    // reverse of natural order so that larger longs come first in iteration order.
    public static final LongComparator voltageLimitComparator = new LongComparator() {

        @Override
        public int compare(long k1, long k2) {
            return Long.compare(k2, k1);
        }

        @Override
        public int compare(Long o1, Long o2) {
            return Long.compare(o2, o1);
        }
    };

    protected final @NotNull Long2ObjectAVLTreeMap<Set<NetNode>> voltageLimitInfo;

    protected final long loss;

    public StandardEnergyPath(@NotNull ImmutableCollection<NetNode> nodes, @NotNull ImmutableCollection<NetEdge> edges,
                              double weight, @NotNull Long2ObjectAVLTreeMap<Set<NetNode>> voltageLimitInfo, long loss) {
        super(nodes, edges, weight);
        this.voltageLimitInfo = voltageLimitInfo;
        this.loss = loss;
    }

    public StandardEnergyPath(@NotNull StandardEnergyPath reverse) {
        super(reverse);
        this.voltageLimitInfo = reverse.voltageLimitInfo;
        this.loss = reverse.loss;
    }

    @NotNull
    @Override
    public PathFlowReport traverse(final long voltage, final long amperage) {
        long resultVoltage = voltage - loss;
        if (resultVoltage <= 0) return EMPTY;
        for (NetEdge edge : getOrderedEdges()) {
            if (!edge.test(IPredicateTestObject.INSTANCE)) return EMPTY;
        }

        var set = voltageLimitInfo.tailMap(resultVoltage).long2ObjectEntrySet();
        for (var entry : set) {
            long key = entry.getLongKey();
            if (key >= resultVoltage) continue;
            // move 90% of the way towards the limiting voltage for every node with this limit
            int count = entry.getValue().size();
            resultVoltage = (long) (key + (resultVoltage - key) * Math.pow(0.1, count));
        }
        long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        long resultAmperage = amperage;
        List<Runnable> postActions = new ObjectArrayList<>();
        for (NetNode node : getOrderedNodes()) {
            NetLogicData data = node.getData();
            EnergyFlowLogic energyFlow = data.getLogicEntryNullable(EnergyFlowLogic.TYPE);
            if (energyFlow == null) {
                energyFlow = new EnergyFlowLogic();
                data.setLogicEntry(energyFlow);
            }
            long sum = 0L;
            for (EnergyFlowData energyFlowData : energyFlow.getFlow(tick)) {
                long amperaged = energyFlowData.amperage();
                sum += amperaged;
            }
            long correctedAmperage = Math.min(data.getLogicEntryDefaultable(AmperageLimitLogic.TYPE).getValue() -
                    sum, resultAmperage);

            EnergyFlowLogic finalEnergyFlow = energyFlow;
            long finalResultVoltage = resultVoltage;
            long finalResultAmperage = resultAmperage;
            postActions.add(() -> {
                TemperatureLogic tempLogic = data.getLogicEntryNullable(TemperatureLogic.TYPE);
                if (tempLogic != null) {
                    long endVoltage = Math.min(voltage,
                            data.getLogicEntryDefaultable(VoltageLimitLogic.TYPE).getValue());
                    float heat = (float) computeHeat(voltage, endVoltage, finalResultAmperage, correctedAmperage);
                    if (heat > 0) tempLogic.applyThermalEnergy(heat, tick);
                    if (node instanceof WorldPipeNode n) {
                        tempLogic.defaultHandleTemperature(n.getNet().getWorld(), n.getEquivalencyData());
                    }
                }
                finalEnergyFlow.recordFlow(tick, new EnergyFlowData(correctedAmperage, finalResultVoltage));
            });
            resultAmperage = correctedAmperage;
        }
        long finalResultVoltage = resultVoltage;
        long finalResultAmperage = resultAmperage;
        postActions.add(() -> {
            NetGroup group = getSourceNode().getGroupUnsafe();
            if (group != null && group.getData() instanceof EnergyGroupData data) {
                data.addEnergyInPerSec(voltage * amperage, tick);
                data.addEnergyOutPerSec(finalResultVoltage * finalResultAmperage, tick);
            }
        });
        return new StandardReport(resultAmperage, resultVoltage, postActions);
    }

    public static double computeHeat(long startVoltage, long endVoltage, long startAmperage, long endAmperage) {
        return Math.pow((double) startVoltage * startAmperage - (double) endVoltage * endAmperage, 0.6);
    }

    @Override
    public @NotNull StandardEnergyPath reversed() {
        if (reversed == null) {
            reversed = new StandardEnergyPath(this);
        }
        return (StandardEnergyPath) reversed;
    }

    public static final class Builder implements PathBuilder {

        public final List<NetNode> nodes = new ObjectArrayList<>();
        public final List<NetEdge> edges = new ObjectArrayList<>();
        public final Long2ObjectAVLTreeMap<Set<NetNode>> voltageLimitInfo = new Long2ObjectAVLTreeMap<>(
                voltageLimitComparator);
        public double loss = 0;

        public Builder(@NotNull NetNode startingNode) {
            nodes.add(startingNode);
            handleAdditionalInfo(startingNode);
        }

        private void handleAdditionalInfo(@NotNull NetNode node) {
            long value = node.getData().getLogicEntryDefaultable(VoltageLimitLogic.TYPE).getValue();
            Set<NetNode> set = voltageLimitInfo.get(value);
            if (set == null) {
                set = new ObjectOpenHashSet<>();
                voltageLimitInfo.put(value, set);
            }
            set.add(node);
            loss += node.getData().getLogicEntryDefaultable(VoltageLossLogic.TYPE).getValue();
        }

        @Override
        @Contract("_, _ -> this")
        public Builder addToEnd(@NotNull NetNode node, @NotNull NetEdge edge) {
            NetNode end = nodes.get(nodes.size() - 1);
            if (edge.getOppositeNode(node) != end)
                throw new IllegalArgumentException("Edge does not link last node and new node!");
            nodes.add(node);
            handleAdditionalInfo(node);
            edges.add(edge);
            return this;
        }

        @Override
        @Contract("_, _ -> this")
        public Builder addToStart(@NotNull NetNode node, @NotNull NetEdge edge) {
            NetNode end = nodes.get(0);
            if (edge.getOppositeNode(node) != end)
                throw new IllegalArgumentException("Edge does not link last node and new node!");
            nodes.add(0, node);
            handleAdditionalInfo(node);
            edges.add(0, edge);
            return this;
        }

        @Override
        @Contract("-> this")
        public Builder reverse() {
            Collections.reverse(nodes);
            Collections.reverse(edges);
            return this;
        }

        @Override
        public StandardEnergyPath build() {
            double sum = 0.0;
            for (NetEdge edge : edges) {
                double edgeWeight = edge.getWeight();
                sum += edgeWeight;
            }
            return new StandardEnergyPath(ImmutableSet.copyOf(nodes), ImmutableSet.copyOf(edges),
                    sum, voltageLimitInfo, (long) Math.ceil(loss));
        }
    }

    public static class SingletonEnergyPath extends SingletonNetPath implements EnergyPath {

        protected final long voltageLimit;

        protected final long loss;

        protected final long amperageLimit;

        public SingletonEnergyPath(NetNode node) {
            this(node, node.getData().getLogicEntryDefaultable(WeightFactorLogic.TYPE).getValue());
        }

        public SingletonEnergyPath(NetNode node, double weight) {
            super(node, weight);
            NetLogicData data = node.getData();
            this.voltageLimit = data.getLogicEntryDefaultable(VoltageLimitLogic.TYPE).getValue();
            this.loss = (long) Math.ceil(data.getLogicEntryDefaultable(VoltageLossLogic.TYPE).getValue());
            this.amperageLimit = data.getLogicEntryDefaultable(AmperageLimitLogic.TYPE).getValue();
        }

        @Override
        public @NotNull EnergyPath.PathFlowReport traverse(long voltage, long amperage) {
            long resultVoltage = voltage - loss;
            if (resultVoltage <= 0) return EMPTY;
            else if (resultVoltage > voltageLimit) {
                resultVoltage = (long) (voltageLimit + (resultVoltage - voltageLimit) * 0.1);
            }

            NetLogicData data = node.getData();
            EnergyFlowLogic energyFlow = data.getLogicEntryNullable(EnergyFlowLogic.TYPE);
            if (energyFlow == null) {
                energyFlow = new EnergyFlowLogic();
                data.setLogicEntry(energyFlow);
            }
            long tick = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
            long sum = 0L;
            for (EnergyFlowData energyFlowData : energyFlow.getFlow(tick)) {
                long amperaged = energyFlowData.amperage();
                sum += amperaged;
            }
            long resultAmperage = Math.min(amperageLimit - sum, amperage);

            EnergyFlowLogic finalEnergyFlow = energyFlow;
            long finalResultVoltage = resultVoltage;
            return new StandardReport(resultAmperage, resultVoltage, () -> {
                TemperatureLogic tempLogic = data.getLogicEntryNullable(TemperatureLogic.TYPE);
                if (tempLogic != null) {
                    long endVoltage = Math.min(voltage,
                            data.getLogicEntryDefaultable(VoltageLimitLogic.TYPE).getValue());
                    float heat = (float) computeHeat(voltage, endVoltage, resultAmperage, resultAmperage);
                    if (heat > 0) tempLogic.applyThermalEnergy(heat, tick);
                    if (node instanceof WorldPipeNode n) {
                        tempLogic.defaultHandleTemperature(n.getNet().getWorld(), n.getEquivalencyData());
                    }
                }
                finalEnergyFlow.recordFlow(tick, new EnergyFlowData(resultAmperage, finalResultVoltage));

                NetGroup group = getSourceNode().getGroupUnsafe();
                if (group != null && group.getData() instanceof EnergyGroupData gData) {
                    gData.addEnergyInPerSec(voltage * amperage, tick);
                    gData.addEnergyOutPerSec(finalResultVoltage * resultAmperage, tick);
                }
            });
        }
    }

    protected static final EnergyPath.PathFlowReport EMPTY = new PathFlowReport() {

        @Override
        public long voltageOut() {
            return 0;
        }

        @Override
        public long amperageOut() {
            return 0;
        }

        @Override
        public void report() {}
    };

    public static final class StandardReport implements PathFlowReport {

        private final long amperage;
        private final long voltage;
        private final Runnable[] report;

        public StandardReport(long amperage, long voltage, @NotNull Runnable @NotNull... report) {
            this.amperage = amperage;
            this.voltage = voltage;
            this.report = report;
        }

        public StandardReport(long amperage, long voltage, @NotNull List<@NotNull Runnable> report) {
            this.amperage = amperage;
            this.voltage = voltage;
            this.report = report.toArray(new Runnable[0]);
        }

        @Override
        public long voltageOut() {
            return voltage;
        }

        @Override
        public long amperageOut() {
            return amperage;
        }

        @Override
        public void report() {
            for (Runnable runnable : report) {
                runnable.run();
            }
        }
    }
}
