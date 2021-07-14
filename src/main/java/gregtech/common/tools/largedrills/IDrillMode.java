package gregtech.common.tools.largedrills;

import gregtech.common.items.behaviors.ModeSwitchBehavior;

public interface IDrillMode extends ModeSwitchBehavior.ILocalizationKey {
    int getCubeSize();

    float getDigSpeedMultiplier();

    @Override
    String getUnlocalizedName();

    static IDrillMode getSingleBlock() {
        return DrillModes.DrillMode.SINGLE_BLOCK;
    }
}
