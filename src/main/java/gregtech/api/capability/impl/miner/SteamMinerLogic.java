package gregtech.api.capability.impl.miner;

import gregtech.api.capability.IVentable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.client.renderer.ICubeRenderer;

public class SteamMinerLogic extends MinerLogic {

    /**
     * Creates the logic for steam miners
     *
     * @param metaTileEntity the {@link MetaTileEntity} this logic belongs to
     * @param fortune        the fortune amount to apply when mining ores
     * @param speed          the speed in ticks per block mined
     * @param maximumRadius  the maximum radius (square shaped) the miner can mine in
     */
    public SteamMinerLogic(MetaTileEntity metaTileEntity, int fortune, int speed, int maximumRadius, ICubeRenderer pipeTexture) {
        super(metaTileEntity, fortune, speed, maximumRadius, pipeTexture);
    }

    @Override
    protected boolean checkCanMine() {
        IVentable machine = (IVentable) metaTileEntity;
        if (machine.isNeedsVenting()) {
            machine.tryDoVenting();
            if (machine.isVentingStuck())
                return false;
        }

        return super.checkCanMine();
    }

    @Override
    protected void onMineOperation() {
        super.onMineOperation();
        ((IVentable) metaTileEntity).setNeedsVenting(true);
    }
}
