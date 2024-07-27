package gregtech.api.capability.data;

import gregtech.api.capability.data.IDataAccess;

public interface IHatchDataAccess extends IDataAccess {

    /**
     * @return if this hatch transmits data through cables
     */
    boolean isTransmitter();
}
