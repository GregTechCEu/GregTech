package gregtech.api.pipenet.alg;

import gregtech.api.pipenet.INodeData;
import gregtech.api.pipenet.NetPath;
import gregtech.api.pipenet.NodeG;
import gregtech.api.pipenet.block.IPipeType;

import java.util.List;

@FunctionalInterface
public interface INetAlgorithm<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> {

    List<NetPath<PT, NDT>> getPathsList(NodeG<PT, NDT> source);

    class NetAlgorithmWrapper<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
            NodeDataType extends INodeData<NodeDataType>> {

        INetAlgorithm<PipeType, NodeDataType> alg;

        public void setAlg(INetAlgorithm<PipeType, NodeDataType> alg) {
            this.alg = alg;
        }

        public INetAlgorithm<PipeType, NodeDataType> getAlg() {
            return alg;
        }

        public List<NetPath<PipeType, NodeDataType>> getPathsList(NodeG<PipeType, NodeDataType> source) {
            if (alg == null) return null;
            return alg.getPathsList(source);
        }
    }
}
