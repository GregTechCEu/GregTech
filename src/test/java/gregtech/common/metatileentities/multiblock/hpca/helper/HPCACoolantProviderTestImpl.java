package gregtech.common.metatileentities.multiblock.hpca.helper;

import gregtech.api.capability.IHPCACoolantProvider;

public class HPCACoolantProviderTestImpl extends HPCAComponentHatchTestImpl implements IHPCACoolantProvider {

    private final int coolingAmount;

    protected HPCACoolantProviderTestImpl(int upkeepEUt, int maxEUt, int coolingAmount) {
        super(upkeepEUt, maxEUt);
        this.coolingAmount = coolingAmount;
    }

    @Override
    public int getCoolingAmount() {
        return coolingAmount;
    }

    // not tested
    @Override
    public boolean isActiveCooler() {
        return false;
    }

    // not tested
    @Override
    public int getMaxCoolantPerTick() {
        return 0;
    }
}
