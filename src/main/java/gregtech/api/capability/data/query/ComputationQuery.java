package gregtech.api.capability.data.query;

import gregtech.api.capability.data.IComputationProvider;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;

public class ComputationQuery extends DataQueryObject implements IComputationQuery {

    private final Set<IComputationProvider> providers;

    private boolean bridged = false;
    private boolean foundUnbridgeable = false;

    public ComputationQuery() {
        providers = new ObjectOpenHashSet<>();
    }

    @Override
    public DataQueryFormat getFormat() {
        return DataQueryFormat.COMPUTATION;
    }

    @Override
    public void setBridged() {
        this.bridged = true;
    }

    public boolean foundUnbridgeable() {
        return foundUnbridgeable;
    }

    @Override
    public void registerProvider(IComputationProvider provider) {
        if (bridged && !provider.supportsBridging()) {
            foundUnbridgeable = true;
            return;
        }
        providers.add(provider);
    }

    public Set<IComputationProvider> getProviders() {
        return providers;
    }

    public long requestCWU(long amount, boolean simulate) {
        long remaining = amount;
        for (IComputationProvider provider : getProviders()) {
            remaining -= provider.supplyCWU(remaining, simulate);
            if (remaining <= 0) return amount;
        }
        return amount - remaining;
    }

    public long maxCWUt() {
        long amount = 0;
        for (IComputationProvider provider : getProviders()) {
            amount += provider.maxCWUt();
        }
        return amount;
    }
}
