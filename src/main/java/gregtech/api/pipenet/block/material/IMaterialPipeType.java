package gregtech.api.pipenet.block.material;

import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.unification.ore.OrePrefix;

public interface IMaterialPipeType<NodeDataType> extends IPipeType<NodeDataType> {

    /**
     * Determines ore prefix used for this pipe type, which gives pipe ore dictionary key
     * when combined with pipe's material
     *
     * @return ore prefix used for this pipe type
     */
    OrePrefix getOrePrefix();
}
