package gregtech.api.capability;

import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;

public interface IMultiblockController {

    boolean isStructureFormed(String name);

    default boolean isStructureFormed() {
        return isStructureFormed(MultiblockControllerBase.DEFAULT_STRUCTURE);
    }

    default boolean isStructureObstructed() {
        return false;
    }
}
