package gregtech.api.graphnet.traverse;

import gregtech.api.graphnet.NetNode;
import gregtech.api.graphnet.edge.AbstractNetFlowEdge;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.edge.util.FlowConsumerList;
import gregtech.api.graphnet.path.INetPath;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraftforge.fml.common.FMLCommonHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class TraverseHelpers {

    private TraverseHelpers() {}

    /**
     * Provides logic for traversing a flow net in a 'flood' manner;
     * specifically, find the lowest weight path, fill it to capacity, find the next lowest weight path, etc.
     * Requires dynamic weights to function properly.
     *
     * @param data the traversal data.
     * @param paths the paths to traverse.
     * @param flowIn the flow to traverse with.
     * @return the consumed flow.
     */
    public static <N extends NetNode, E extends AbstractNetFlowEdge, P extends INetPath<N, E>, D extends ITraverseData<N, P>> long traverseFlood(
            @NotNull D data,
            @NotNull Iterator<P> paths,
            long flowIn) {
        boolean simulate = data.getSimulatorKey() != null;
        long availableFlow = flowIn;
        pathloop:
        while (paths.hasNext()) {
            List<Runnable> pathTraverseCalls = simulate ? null : new ObjectArrayList<>();
            long pathFlowIn = availableFlow;
            long pathFlow = availableFlow;
            P path = paths.next();
            if (data.prepareForPathWalk(path)) continue;

            List<N> nodes = path.getOrderedNodes();
            List<E> edges = path.getOrderedEdges();
            FlowConsumerList flowConsumers = new FlowConsumerList();
            assert nodes.size() == edges.size() + 1;

            N targetNode = nodes.get(0);
            pathFlow = data.traverseToNode(targetNode, pathFlow);

            for (int i = 0; i < edges.size(); i++) {
                E edge = edges.get(i);
                targetNode = nodes.get(i + 1);

                if (targetNode.traverse(data.getQueryTick(), true)) {
                    if (!simulate) {
                        N finalTargetNode = targetNode;
                        pathTraverseCalls.add(() -> finalTargetNode.traverse(data.getQueryTick(), false));
                    }
                } else continue pathloop;

                long flowLimit = edge.getFlowLimit(data.getTestObject(), data.getGraphNet(), data.getQueryTick(), data.getSimulatorKey());
                if (flowLimit < pathFlow) {
                    double ratio = (double) flowLimit / pathFlow;
                    flowConsumers.modifyRatios(ratio);
                    pathFlowIn *= ratio;
                    pathFlow = flowLimit;
                }
                flowConsumers.add(edge, data.getTestObject(), data.getGraphNet(), pathFlow, data.getQueryTick(), data.getSimulatorKey());
                pathFlow = data.traverseToNode(targetNode, pathFlow);


                if (pathFlow <= 0) continue pathloop;
            }
            long accepted = data.finalizeAtDestination(targetNode, pathFlow);
            double ratio = (double) accepted / pathFlow;
            flowConsumers.doConsumption(ratio);
            if (!simulate) pathTraverseCalls.forEach(Runnable::run);
            availableFlow -= pathFlowIn * ratio;

            if (availableFlow <= 0) break;
        }

        return flowIn - availableFlow;
    }

    /**
     * Provides logic for traversing a net that simply finds the lowest weight path that it can traverse,
     * and then traverses it. Optionally supports flow, in which case overflows will be reported
     * and paths will be iterated over until flow or paths are exhausted.
     *
     * @param data the traversal data.
     * @param paths the paths to traverse.
     * @param overflowListener will be provided with a node and incoming overflow once a path is walked
     *                         and the final overflows are calculated. If null, no overflow logic will be calculated.
     * @param flowIn the flow to traverse with.
     * @return the consumed flow.
     */
    public static <N extends NetNode, E extends NetEdge, P extends INetPath<N, E>, D extends ITraverseData<N, P>> long traverseDumb(
            @NotNull D data,
            @NotNull Iterator<P> paths,
            @Nullable BiConsumer<N, Long> overflowListener,
            long flowIn) {
        boolean simulate = data.getSimulatorKey() != null;
        boolean flow = overflowListener != null;
        long availableFlow = flowIn;
        pathloop:
        while (paths.hasNext()) {
            List<Runnable> pathTraverseCalls = simulate ? null : new ObjectArrayList<>();
            long pathFlowIn = availableFlow;
            long pathFlow = availableFlow;
            P path = paths.next();
            if (data.prepareForPathWalk(path)) continue;

            List<N> nodes = path.getOrderedNodes();
            List<E> edges = path.getOrderedEdges();

            FlowConsumerList flowConsumers = flow ? new FlowConsumerList() : null;
            List<Consumer<Long>> overflowReporters = flow ? new ObjectArrayList<>() : null;
            assert nodes.size() == edges.size() + 1;

            N targetNode = nodes.get(0);
            pathFlow = data.traverseToNode(targetNode, pathFlow);

            for (int i = 0; i < edges.size(); i++) {
                E edge = edges.get(i);
                targetNode = nodes.get(i + 1);

                if (targetNode.traverse(data.getQueryTick(), true)) {
                    if (!simulate) {
                        N finalTargetNode = targetNode;
                        pathTraverseCalls.add(() -> finalTargetNode.traverse(data.getQueryTick(), false));
                    }
                } else continue pathloop;

                if (flow && edge instanceof AbstractNetFlowEdge flowEdge) {
                    long flowLimit = flowEdge.getFlowLimit(data.getTestObject(), data.getGraphNet(), data.getQueryTick(), data.getSimulatorKey());
                    if (flowLimit < pathFlow) {
                        long overflow = pathFlow - flowLimit;
                        N finalTargetNode = targetNode;
                        overflowReporters.add(reduction -> {
                            long finalOverflow = overflow - reduction;
                            if (finalOverflow > 0) overflowListener.accept(finalTargetNode, finalOverflow);
                        });
                        pathFlow = flowLimit;
                    }
                    flowConsumers.add(flowEdge, data.getTestObject(), data.getGraphNet(), pathFlow, data.getQueryTick(), data.getSimulatorKey());
                }
                pathFlow = data.traverseToNode(targetNode, pathFlow);

                if (pathFlow <= 0) continue pathloop;
            }
            long accepted = data.finalizeAtDestination(targetNode, pathFlow);
            long unaccepted = pathFlow - accepted;
            if (flow) {
                flowConsumers.doConsumption(unaccepted);
                overflowReporters.forEach((c) -> c.accept(unaccepted));
            }
            if (!simulate) pathTraverseCalls.forEach(Runnable::run);
            availableFlow -= pathFlowIn - unaccepted;

            if (availableFlow <= 0) break;
        }

        return flowIn - availableFlow;
    }
}
