package gregtech.common.pipelike.optical;

import gregtech.api.pipenet.INodeData;

import java.util.List;
import java.util.Set;

public class OpticalPipeProperties implements INodeData<OpticalPipeProperties> {

    public static final OpticalPipeProperties INSTANCE = new OpticalPipeProperties();

    @Override
    public OpticalPipeProperties getMinData(Set<OpticalPipeProperties> datas) {
        return this;
    }
}
