package gregtech.common.pipelikeold.laser;

import gregtech.api.graphnet.pipenetold.IPipeNetData;

import java.util.List;

public class LaserPipeProperties implements IPipeNetData<LaserPipeProperties> {

    public static final LaserPipeProperties INSTANCE = new LaserPipeProperties();

    @Override
    public LaserPipeProperties getSumData(List<LaserPipeProperties> datas) {
        return this;
    }
}
