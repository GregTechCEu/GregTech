package gregtech.api.metatileentity.multiblock;

import org.jetbrains.annotations.NotNull;

public interface IMultiblockPart {

    boolean isAttachedToMultiBlock();

    void addToMultiBlock(@NotNull MultiblockControllerBase controllerBase);

    void removeFromMultiBlock(@NotNull MultiblockControllerBase controllerBase);

    /**
     * Gets how many multiblocks are currently using the part.
     */
    int getWallshareCount();

    boolean canPartShare(MultiblockControllerBase target, String substructureName);

    default boolean canPartShare() {
        return true;
    }

    /** Called when distinct mode is toggled on the controller that this part is attached to */
    default void onDistinctChange(boolean newValue) {}
}
