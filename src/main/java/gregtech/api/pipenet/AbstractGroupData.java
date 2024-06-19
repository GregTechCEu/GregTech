package gregtech.api.pipenet;

import gregtech.api.pipenet.block.IPipeType;

public abstract class AbstractGroupData<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>,
        NodeDataType extends INodeData<NodeDataType>> {

    protected NetGroup<PipeType, NodeDataType, ?> group;

    public AbstractGroupData<PipeType, NodeDataType> withGroup(NetGroup<PipeType, NodeDataType, ?> group) {
        this.group = group;
        return this;
    }
}
