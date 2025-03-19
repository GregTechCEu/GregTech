package gregtech.api.metatileentity.multiblock;

public interface IMultiblockPart {

    boolean isAttachedToMultiBlock();

    void addToMultiBlock(MultiblockControllerBase controllerBase);

    void removeFromMultiBlock(MultiblockControllerBase controllerBase);

    default boolean canPartShare() {
        return true;
    }

    /** Called when distinct mode is toggled on the controller that this part is attached to */
    default void onDistinctChange(boolean newValue) {}
}
