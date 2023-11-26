package gregtech.api.pipenet.block;

import net.minecraft.util.IStringSerializable;

public interface IPipeType<NodeDataType> extends IStringSerializable {

    float getThickness();

    NodeDataType modifyProperties(NodeDataType baseProperties);

    boolean isPaintable();
}
