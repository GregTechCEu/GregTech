package gregtech.api.graphnet.pipenetold;

import gregtech.api.graphnet.graph.GraphEdge;
import gregtech.api.graphnet.graph.GraphVertex;
import gregtech.api.graphnet.pipenetold.block.IPipeType;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.predicate.test.IPredicateTestObject;
import gregtech.api.util.FacingPos;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.jgrapht.GraphPath;

import java.util.*;
import java.util.stream.Collectors;

public class PipeNetPath<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends IPipeNetData<NodeDataType>, Edge extends NetEdge>
        extends NetPath<PipeNetNode<PipeType, NodeDataType, Edge>, Edge> {

    private NodeDataType data = null;

    public PipeNetPath(GraphVertex node) {
        super(node);
    }

    public PipeNetPath(List<GraphVertex> nodes, List<GraphEdge> graphEdges, double weight) {
        super(nodes, graphEdges, weight);
    }

    public PipeNetPath(GraphPath<GraphVertex, GraphEdge> path) {
        super(path);
    }

    public Iterator<EnumFacing> getFacingIterator() {
        return this.getTargetTEs().keySet().iterator();
    }

    public FacedPipeNetPath<PipeType, NodeDataType, Edge> firstFacing() {
        return this.withFacing(this.getFacingIterator().next());
    }

    public FacedPipeNetPath<PipeType, NodeDataType, Edge> withFacing(EnumFacing facing) {
        return new FacedPipeNetPath<>(this, facing);
    }

    public Map<EnumFacing, TileEntity> getTargetTEs() {
        return getTargetNode().getConnecteds();
    }

    public NodeDataType getSumData() {
        // generate min data on-demand and cache it, rather than generating for every path always
        if (this.data == null) {
            this.data = getSourceNode().getData()
                    .getSumData(this.getNodeList().stream().map(PipeNetNode::getData).collect(Collectors.toList()));
        }
        return data;
    }

    public static class FacedPipeNetPath<PT extends Enum<PT> & IPipeType<NDT>, NDT extends IPipeNetData<NDT>,
            E extends NetEdge> {

        public PipeNetPath<PT, NDT, E> path;

        public EnumFacing facing;

        public FacedPipeNetPath(PipeNetPath<PT, NDT, E> path, EnumFacing facing) {
            this.path = path;
            this.facing = facing;
        }

        public TileEntity getTargetTE() {
            return path.getTargetTEs().get(facing);
        }

        public List<PipeNetNode<PT, NDT, E>> getNodeList() {
            return path.getNodeList();
        }

        public List<E> getEdgeList() {
            return path.getEdgeList();
        }

        public PipeNetNode<PT, NDT, E> getSourceNode() {
            return path.getSourceNode();
        }

        public PipeNetNode<PT, NDT, E> getTargetNode() {
            return path.getTargetNode();
        }

        public Map<EnumFacing, TileEntity> getTargetTEs() {
            return path.getTargetTEs();
        }

        public double getWeight() {
            return path.getWeight();
        }

        public NDT getSumData() {
            return path.getSumData();
        }

        public boolean checkPredicate(IPredicateTestObject testObject) {
            return path.checkPredicate(testObject);
        }

        public FacingPos toFacingPos() {
            return new FacingPos(path.getTargetNode().getNodePos(), this.facing);
        }

        public EnumFacing oppositeFacing() {
            return facing.getOpposite();
        }
    }
}
