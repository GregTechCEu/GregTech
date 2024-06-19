package gregtech.api.pipenet.alg;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetNode;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;

import java.util.List;

@FunctionalInterface
public interface INetAlgorithm<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>, E extends NetEdge> {

    List<NetPath<PT, NDT, E>> getPathsList(NetNode<PT, NDT, E> source);

    class NetAlgorithmWrapper<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
            NodeDataType extends INodeData<NodeDataType>, E extends NetEdge> {

        INetAlgorithm<PipeType, NodeDataType, E> alg;

        public void setAlg(INetAlgorithm<PipeType, NodeDataType, E> alg) {
            this.alg = alg;
        }

        public INetAlgorithm<PipeType, NodeDataType, E> getAlg() {
            return alg;
        }

        public List<NetPath<PipeType, NodeDataType, E>> getPathsList(NetNode<PipeType, NodeDataType, E> source) {
            if (alg == null) return null;
            return alg.getPathsList(source);
        }
    }
}
