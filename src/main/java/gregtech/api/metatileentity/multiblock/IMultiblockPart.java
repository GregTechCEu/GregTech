package gregtech.api.metatileentity.multiblock;

public interface IMultiblockPart {

    boolean isAttachedToMultiBlock();

    void addToMultiBlock(MultiblockControllerBase controllerBase);

    void removeFromMultiBlock(MultiblockControllerBase controllerBase);

    default MultiblockControllerBase getAttachedMultiblock() {return null;}

    /**
     * Gets how many multiblocks are currently using the part.
     */
    default int getWallsharedCount() { return 1;}

    default boolean canPartShare(MultiblockControllerBase target, String substructureName) {
        return canPartShare();
    }

    default boolean canPartShare() {
        return true;
    }

    /** Called when distinct mode is toggled on the controller that this part is attached to */
    default void onDistinctChange(boolean newValue) {}
}
