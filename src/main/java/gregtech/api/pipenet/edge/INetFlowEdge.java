package gregtech.api.pipenet.edge;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.block.IPipeType;

import org.jetbrains.annotations.Nullable;
import org.jgrapht.Graph;

public interface INetFlowEdge<E extends NetEdge & INetFlowEdge<?>> {

    /**
     * Claims a new, unique simulator instance for properly simulating flow edge limits without actually changing them.
     * <br>
     * This simulator must be discarded after use so that the garbage collector can clean up.
     */
    static ChannelSimulatorKey getNewSimulatorInstance() {
        return new ChannelSimulatorKey();
    }

    <PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> void consumeFlowLimit(
                                                                                             Object channel,
                                                                                             Graph<NetNode<PT, NDT, E>, E> graph,
                                                                                             int amount, long queryTick,
                                                                                             @Nullable INetFlowEdge.ChannelSimulatorKey simulator);

    final class ChannelSimulatorKey {

        private static int ID;
        private final int id;

        private ChannelSimulatorKey() {
            this.id = ID++;
        }

        @Override
        public int hashCode() {
            // enforcing hash uniqueness improves weak map performance
            return id;
        }
    }
}
