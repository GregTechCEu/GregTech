package gregtech.common.pipelike.laser;

import gregtech.api.pipenet.INodeData;

import java.util.List;

public class LaserPipeProperties implements INodeData<LaserPipeProperties> {

    public static final LaserPipeProperties INSTANCE = new LaserPipeProperties();

    @Override
    public LaserPipeProperties getSumData(List<LaserPipeProperties> datas) {
        return this;
    }
}
