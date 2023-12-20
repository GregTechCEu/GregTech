package gregtech.common.pipelike.laser;

import gregtech.api.pipenet.INodeData;

import java.util.List;
import java.util.Set;

public class LaserPipeProperties implements INodeData<LaserPipeProperties> {

    public static final LaserPipeProperties INSTANCE = new LaserPipeProperties();

    @Override
    public LaserPipeProperties getMinData(Set<LaserPipeProperties> datas) {
        return this;
    }
}
