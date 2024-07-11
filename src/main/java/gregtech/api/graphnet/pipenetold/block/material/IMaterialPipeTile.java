package gregtech.api.graphnet.pipenetold.block.material;

import gregtech.api.graphnet.pipenetold.IPipeNetData;
import gregtech.api.graphnet.pipenetold.block.IPipeType;
import gregtech.api.graphnet.edge.NetEdge;
import gregtech.api.graphnet.pipenetold.tile.IPipeTile;
import gregtech.api.unification.material.Material;

public interface IMaterialPipeTile<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends IPipeNetData<NodeDataType>, Edge extends NetEdge>
                                  extends IPipeTile<PipeType, NodeDataType, Edge> {

    Material getPipeMaterial();
}
