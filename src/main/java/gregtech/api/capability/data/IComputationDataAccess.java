package gregtech.api.capability.data;

import gregtech.api.capability.data.query.DataAccessFormat;

public interface IComputationDataAccess extends IHatchDataAccess {

    @Override
    default DataAccessFormat getFormat() {
        return DataAccessFormat.COMPUTATION;
    }
}
