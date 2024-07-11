package gregtech.api.graphnet.pipenetold.block;

import gregtech.api.graphnet.pipenetold.IPipeNetData;

import net.minecraft.util.IStringSerializable;

public interface IPipeType<NodeDataType extends IPipeNetData<NodeDataType>> extends IStringSerializable {

    float getThickness();

    NodeDataType modifyProperties(NodeDataType baseProperties);

    boolean isPaintable();
}
