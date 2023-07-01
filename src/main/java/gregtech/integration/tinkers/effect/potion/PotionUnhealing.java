package gregtech.integration.tinkers.effect.potion;

import gregtech.api.util.GTPotion;

public class PotionUnhealing extends GTPotion {

    public PotionUnhealing() {
        super("gt_unhealing", true, 0xFAFAFA, 0);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    protected boolean canRender() {
        return true;
    }
}
