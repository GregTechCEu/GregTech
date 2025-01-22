package gregtech.api.capability.data;

import gregtech.api.capability.data.query.DataAccessFormat;

import org.jetbrains.annotations.NotNull;

public interface IStandardDataAccess extends IHatchDataAccess {

    @Override
    default @NotNull DataAccessFormat getFormat() {
        return DataAccessFormat.STANDARD;
    }
}
