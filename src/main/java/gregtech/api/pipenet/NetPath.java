package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.edge.NetEdge;
import gregtech.api.util.FacingPos;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jgrapht.GraphPath;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NetPath<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends INodeData<NodeDataType>, E extends NetEdge> {

    private List<NetNode<PipeType, NodeDataType, E>> nodeList;
    private List<E> edgeList;

    private NetNode<PipeType, NodeDataType, E> sourceNode;

    private NetNode<PipeType, NodeDataType, E> targetNode;

    private double weight;

    private NodeDataType data = null;

    /**
     * Generates a loop NetPath for a node
     * 
     * @param node the node to
     */
    public NetPath(NetNode<PipeType, NodeDataType, E> node) {
        this.sourceNode = node;
        this.targetNode = node;
        this.nodeList = Collections.singletonList(node);
        this.weight = node.getData().getWeightFactor();
        this.edgeList = new ObjectArrayList<>(0);
        assert this.nodeList.size() == this.edgeList.size() + 1;
    }

    /**
     * Generates a NetPath from an ordered list of nodes, edges, and a weight.
     * Used exclusively for single path generation.
     *
     * @param nodes  List of nodes.
     * @param edges  List of edges.
     * @param weight Sum weight of the path.
     */
    public NetPath(List<NetNode<PipeType, NodeDataType, E>> nodes, List<E> edges, double weight) {
        this.sourceNode = nodes.get(0);
        this.targetNode = nodes.get(nodes.size() - 1);
        this.nodeList = nodes;
        this.weight = weight;
        this.edgeList = edges;
        assert this.nodeList.size() == this.edgeList.size() + 1;
    }

    /**
     * Generates a NetPath from a GraphPath
     * 
     * @param path the GraphPath
     */
    public NetPath(GraphPath<NetNode<PipeType, NodeDataType, E>, E> path) {
        this.sourceNode = path.getStartVertex();
        this.targetNode = path.getEndVertex();
        this.nodeList = path.getVertexList();
        // convert weight to the true value of the involved nodes
        this.weight = (path.getWeight() + sourceNode.getData().getWeightFactor() +
                targetNode.getData().getWeightFactor()) / 2;
        this.edgeList = path.getEdgeList();
        assert this.nodeList.size() == this.edgeList.size() + 1;
    }

    protected NetPath() {}

    public List<NetNode<PipeType, NodeDataType, E>> getNodeList() {
        return nodeList;
    }

    public List<E> getEdgeList() {
        return edgeList;
    }

    public NetNode<PipeType, NodeDataType, E> getSourceNode() {
        return sourceNode;
    }

    public NetNode<PipeType, NodeDataType, E> getTargetNode() {
        return targetNode;
    }

    public Iterator<EnumFacing> getFacingIterator() {
        return this.getTargetTEs().keySet().iterator();
    }

    public FacedNetPath<PipeType, NodeDataType, E> firstFacing() {
        return this.withFacing(this.getFacingIterator().next());
    }

    public FacedNetPath<PipeType, NodeDataType, E> withFacing(EnumFacing facing) {
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
                    .getMinData(this.nodeList.stream().map(NetNode::getData).collect(Collectors.toSet()));
        }
        return data;
    }

    public boolean checkPredicate(Object o) {
        for (NetEdge edge : this.edgeList) {
            if (!edge.getPredicate().test(o)) return false;
        }
        return true;
    }

    public static class FacedNetPath<PT extends Enum<PT> & IPipeType<NDT>, NDT extends INodeData<NDT>,
            E extends NetEdge> {

        public NetPath<PT, NDT, E> path;

        public EnumFacing facing;

        public FacedNetPath(NetPath<PT, NDT, E> path, EnumFacing facing) {
            this.path = path;
            this.facing = facing;
        }

        public TileEntity getTargetTE() {
            return path.getTargetTEs().get(facing);
        }

        public List<NetNode<PT, NDT, E>> getNodeList() {
            return path.getNodeList();
        }

        public List<E> getEdgeList() {
            return path.getEdgeList();
        }

        public NetNode<PT, NDT, E> getSourceNode() {
            return path.getSourceNode();
        }

        public NetNode<PT, NDT, E> getTargetNode() {
            return path.getTargetNode();
        }

        public Map<EnumFacing, TileEntity> getTargetTEs() {
            return path.getTargetTEs();
        }

        public double getWeight() {
            return path.getWeight();
        }

        public NDT getMinData() {
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
