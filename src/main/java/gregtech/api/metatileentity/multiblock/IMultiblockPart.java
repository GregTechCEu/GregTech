package gregtech.api.metatileentity.multiblock;

import gregtech.api.metatileentity.MetaTileEntity;

public interface IMultiblockPart {

    boolean isAttachedToMultiBlock();

    void addToMultiBlock(MultiblockControllerBase controllerBase);

    void removeFromMultiBlock(MultiblockControllerBase controllerBase);

    default void setupNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
    }

}
