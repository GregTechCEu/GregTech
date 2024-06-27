package gregtech.api.pipenet.alg;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;

import java.util.Iterator;
import java.util.List;

public interface INetAlgorithm<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>, E extends NetEdge> {

    Iterator<NetPath<PT, NDT, E>> getPathsIterator(NetNode<PT, NDT, E> source);

    default boolean supportsDynamicWeights() {
        return false;
    }

    class NetAlgorithmWrapper<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
            NodeDataType extends INodeData<NodeDataType>, E extends NetEdge> {

        INetAlgorithm<PipeType, NodeDataType, E> alg;

        public void setAlg(INetAlgorithm<PipeType, NodeDataType, E> alg) {
            this.alg = alg;
        }

        public INetAlgorithm<PipeType, NodeDataType, E> getAlg() {
            return alg;
        }

        public boolean supportsDynamicWeights() {
            if (alg == null) return false;
            return alg.supportsDynamicWeights();
        }

        public Iterator<NetPath<PipeType, NodeDataType, E>> getPathsIterator(NetNode<PipeType, NodeDataType, E> source) {
            if (alg == null) return null;
            return alg.getPathsIterator(source);
        }
    }
}
