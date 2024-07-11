package gregtech.common.pipelikeold.optical;

import gregtech.api.graphnet.pipenetold.IPipeNetData;

import java.util.List;

public class OpticalPipeProperties implements IPipeNetData<OpticalPipeProperties> {

    public static final OpticalPipeProperties INSTANCE = new OpticalPipeProperties();

    @Override
    public OpticalPipeProperties getSumData(List<OpticalPipeProperties> datas) {
        return this;
    }
}
