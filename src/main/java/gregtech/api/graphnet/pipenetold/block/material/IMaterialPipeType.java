package gregtech.api.graphnet.pipenetold.block.material;

import gregtech.api.graphnet.pipenetold.IPipeNetData;
import gregtech.api.graphnet.pipenetold.block.IPipeType;
import gregtech.api.unification.ore.OrePrefix;

public interface IMaterialPipeType<NodeDataType extends IPipeNetData<NodeDataType>> extends IPipeType<NodeDataType> {

    /**
     * Determines ore prefix used for this pipe type, which gives pipe ore dictionary key
     * when combined with pipe's material
     *
     * @return ore prefix used for this pipe type
     */
    OrePrefix getOrePrefix();
}
