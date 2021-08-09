package gregtech.api.pipenet.block.material;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.unification.material.Material;

public interface IMaterialPipeTile<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> extends IPipeTile<PipeType, NodeDataType> {

    Material getPipeMaterial();
}
