package gregtech.common.metatileentities.multiblock.hpca.helper;

import gregtech.api.capability.IHPCAComputationProvider;

public class HPCAComputationProviderTestImpl extends HPCAComponentHatchTestImpl implements IHPCAComputationProvider {

    private final int cwuPerTick;
    private final int coolantPerTick;

    protected HPCAComputationProviderTestImpl(int upkeepEUt, int maxEUt, int cwuPerTick, int coolantPerTick) {
        super(upkeepEUt, maxEUt);
        this.cwuPerTick = cwuPerTick;
        this.coolantPerTick = coolantPerTick;
    }

    @Override
    public int getCWUPerTick() {
        return cwuPerTick;
    }

    @Override
    public int getCoolingPerTick() {
        return coolantPerTick;
    }
}
