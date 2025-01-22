package gregtech.api.capability.data.query;

import gregtech.api.capability.data.IComputationProvider;

public interface IComputationQuery extends IBridgeable {

    void registerProvider(IComputationProvider provider);
}
