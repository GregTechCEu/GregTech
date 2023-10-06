package gregtech.common.metatileentities.miner;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public class SteamMinerLogic extends MinerLogic<SteamMiner> {

    public SteamMinerLogic(@Nonnull SteamMiner steamMiner, int workFrequency, int maximumDiameter) {
        super(steamMiner, workFrequency, maximumDiameter);
    }

    @Override
    protected boolean canOperate() {
        if (mte.isNeedsVenting()) {
            mte.tryDoVenting();
            if (mte.isVentingStuck()) return false;
        }

        return super.canOperate();
    }

    @Override
    protected void onMineOperation(@Nonnull BlockPos pos, boolean isOre, boolean isOrigin) {
        mte.setNeedsVenting(true);
    }
}
