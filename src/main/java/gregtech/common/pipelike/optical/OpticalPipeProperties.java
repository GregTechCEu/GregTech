package gregtech.common.pipelike.optical;

import gregtech.api.pipenet.INodeData;

import java.util.List;

public class OpticalPipeProperties implements INodeData<OpticalPipeProperties> {

    public static final OpticalPipeProperties INSTANCE = new OpticalPipeProperties();

    @Override
    public OpticalPipeProperties getSumData(List<OpticalPipeProperties> datas) {
        return this;
    }
}
