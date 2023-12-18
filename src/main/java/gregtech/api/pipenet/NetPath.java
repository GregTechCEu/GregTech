package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;

import gregtech.api.util.FacingPos;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.jgrapht.GraphPath;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NetPath<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType extends INodeData<NodeDataType>> {

    protected final List<NodeG<PipeType, NodeDataType>> nodeList;
    private final List<NetEdge> edgeList;

    protected final NodeG<PipeType, NodeDataType> sourceNode;

    protected final NodeG<PipeType, NodeDataType> targetNode;

    protected final double weight;

    private NodeDataType data = null;

    private Iterator<EnumFacing> facingIterator;

    public NetPath(GraphPath<NodeG<PipeType, NodeDataType>, NetEdge> path) {
        this.sourceNode = path.getStartVertex();
        this.targetNode = path.getEndVertex();
        this.nodeList = path.getVertexList();
        // convert weight to the true value of the involved nodes
        this.weight = (path.getWeight() + sourceNode.data.getWeightFactor() + targetNode.data.getWeightFactor()) / 2;
        this.edgeList = path.getEdgeList();
        resetFacingIterator();
    }

    public List<NodeG<PipeType, NodeDataType>> getNodeList() {
        return nodeList;
    }

    public NodeG<PipeType, NodeDataType> getSourceNode() {
        return sourceNode;
    }

    public NodeG<PipeType, NodeDataType> getTargetNode() {
        return targetNode;
    }

    public FacedNetPath<PipeType, NodeDataType> withFacing(EnumFacing facing) {
        return new FacedNetPath<>(this, facing);
    }

    public void resetFacingIterator() {
        this.facingIterator = this.getTargetTEs().keySet().iterator();
    }

    public FacedNetPath<PipeType, NodeDataType> nextFacing() {
        return new FacedNetPath<>(this, this.facingIterator.next());
    }

    public boolean hasNextFacing() {
        return this.facingIterator.hasNext();
    }

    public Map<EnumFacing, TileEntity> getTargetTEs() {
        return targetNode.getConnecteds();
    }

    public double getWeight() {
        return weight;
    }

    public NodeDataType getData() {
        // generate min data on-demand and cache it, rather than generating for every path always
        if (this.data == null) {
            this.data = sourceNode.data.getMinData(this.nodeList.stream().map(NodeG::getData).collect(Collectors.toSet()));
        }
        return data;
    }

    public boolean checkPredicate(Object o) {
        for (NetEdge edge : this.edgeList)
            if (!edge.getPredicate().test(o)) return false;
        return true;
    }

    public static class FacedNetPath<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> {

        public NetPath<PT, NDT> path;

        public EnumFacing facing;

        public FacedNetPath(NetPath<PT, NDT> path, EnumFacing facing) {
            new FacedNetPath<>(path, facing);
        }

        public TileEntity getTargetTE() {
            return path.getTargetTEs().get(facing);
        }
        public List<NodeG<PT, NDT>> getNodeList() {
            return path.getNodeList();
        }

        public NodeG<PT, NDT> getSourceNode() {
            return path.getSourceNode();
        }

        public NodeG<PT, NDT> getTargetNode() {
            return path.getTargetNode();
        }

        public Map<EnumFacing, TileEntity> getTargetTEs() {
            return path.getTargetTEs();
        }

        public double getWeight() {
            return path.getWeight();
        }

        public NDT getData() {
            return path.getData();
        }

        public boolean checkPredicate(Object o) {
            return path.checkPredicate(o);
        }

        public FacingPos toFacingPos() {
            return new FacingPos(path.getTargetNode().getNodePos(), this.facing);
        }
    }
}
