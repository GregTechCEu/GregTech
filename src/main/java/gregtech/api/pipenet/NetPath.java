package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.util.FacingPos;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.GraphPath;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class NetPath<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends INodeData<NodeDataType>> {

    private final List<NodeG<PipeType, NodeDataType>> nodeList;
    private final List<NetEdge> edgeList;

    private final NodeG<PipeType, NodeDataType> sourceNode;

    private final NodeG<PipeType, NodeDataType> targetNode;

    private final double weight;

    private NodeDataType data = null;

    /**
     * Generates a loop NetPath for a node
     * 
     * @param node the node to
     */
    public NetPath(NodeG<PipeType, NodeDataType> node) {
        this.sourceNode = node;
        this.targetNode = node;
        this.nodeList = new ObjectArrayList<>(1);
        this.nodeList.add(node);
        this.weight = node.getData().getWeightFactor();
        this.edgeList = new ObjectArrayList<>(0);
    }

    /**
     * Generates a NetPath from an ordered list of nodes, edges, and a weight.
     * Used exclusively for single path generation.
     * 
     * @param nodes  List of nodes.
     * @param edges  List of edges.
     * @param weight Sum weight of the path.
     */
    public NetPath(List<NodeG<PipeType, NodeDataType>> nodes, List<NetEdge> edges, double weight) {
        this.sourceNode = nodes.get(0);
        this.targetNode = nodes.get(nodes.size() - 1);
        this.nodeList = nodes;
        this.weight = weight;
        this.edgeList = edges;
    }

    /**
     * Generates a NetPath from a GraphPath
     * 
     * @param path the GraphPath
     */
    public NetPath(GraphPath<NodeG<PipeType, NodeDataType>, NetEdge> path) {
        this.sourceNode = path.getStartVertex();
        this.targetNode = path.getEndVertex();
        this.nodeList = path.getVertexList();
        // convert weight to the true value of the involved nodes
        this.weight = (path.getWeight() + sourceNode.getData().getWeightFactor() +
                targetNode.getData().getWeightFactor()) / 2;
        this.edgeList = path.getEdgeList();
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

    public Iterator<EnumFacing> getFacingIterator() {
        return this.getTargetTEs().keySet().iterator();
    }

    public FacedNetPath<PipeType, NodeDataType> firstFacing() {
        return this.withFacing(this.getFacingIterator().next());
    }

    public FacedNetPath<PipeType, NodeDataType> withFacing(EnumFacing facing) {
        return new FacedNetPath<>(this, facing);
    }

    public Map<EnumFacing, TileEntity> getTargetTEs() {
        return targetNode.getConnecteds();
    }

    public double getWeight() {
        return weight;
    }

    public NodeDataType getMinData() {
        // generate min data on-demand and cache it, rather than generating for every path always
        if (this.data == null) {
            this.data = sourceNode.getData()
                    .getMinData(this.nodeList.stream().map(NodeG::getData).collect(Collectors.toSet()));
        }
        return data;
    }

    public boolean checkPredicate(Object o) {
        for (NetEdge edge : this.edgeList) {
            if (edge.getPredicate() == null) continue;
            if (!edge.getPredicate().test(o)) return false;
        }
        return true;
    }

    public static class FacedNetPath<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>> {

        public NetPath<PT, NDT> path;

        public EnumFacing facing;

        public FacedNetPath(NetPath<PT, NDT> path, EnumFacing facing) {
            this.path = path;
            this.facing = facing;
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
            return path.getMinData();
        }

        public boolean checkPredicate(Object o) {
            return path.checkPredicate(o);
        }

        public FacingPos toFacingPos() {
            return new FacingPos(path.getTargetNode().getNodePos(), this.facing);
        }
    }
}
