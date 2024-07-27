package gregtech.api.capability.data;

import gregtech.api.capability.data.query.DataAccessFormat;

public interface IStandardDataAccess extends IHatchDataAccess {

    @Override
    default DataAccessFormat getFormat() {
        return DataAccessFormat.STANDARD;
    }
}
