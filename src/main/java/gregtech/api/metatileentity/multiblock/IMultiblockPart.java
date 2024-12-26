package gregtech.api.metatileentity.multiblock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMultiblockPart {

    boolean isAttachedToMultiBlock();

    default void addToMultiBlock(@NotNull MultiblockControllerBase controllerBase) {
        addToMultiBlock(controllerBase, "MAIN");
    }

    void addToMultiBlock(@NotNull MultiblockControllerBase controllerBase, @NotNull String substructureName);

    void removeFromMultiBlock(@NotNull MultiblockControllerBase controllerBase);

    /**
     * Gets how many multiblocks are currently using the part.
     */
    int getWallshareCount();

    /**
     * Gets the name of the substructure the part is attached to, or null if it is not attached.
     */
    @Nullable
    String getSubstructureName();

    boolean canPartShare(MultiblockControllerBase target, String substructureName);

    default boolean canPartShare(MultiblockControllerBase target) {
        return canPartShare(target, "MAIN");
    }

    default boolean canPartShare() {
        return true;
    }

    /** Called when distinct mode is toggled on the controller that this part is attached to */
    default void onDistinctChange(boolean newValue) {}
}
