package gregtech.api.capability.data;

public interface IHatchDataAccess extends IDataAccess {

    /**
     * @return if this hatch transmits data through cables
     */
    boolean isTransmitter();
}
