package gregtech.api.pipenet.block;

import gregtech.api.pipenet.INodeData;

import net.minecraft.util.IStringSerializable;

public interface IPipeType<NodeDataType extends INodeData<NodeDataType>> extends IStringSerializable {

    float getThickness();

    NodeDataType modifyProperties(NodeDataType baseProperties);

    boolean isPaintable();
}
