package gregtech.api.metatileentity.interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHasWorldObjectAndCoords {

    World getWorld();

    BlockPos getPos();

    default boolean isServerSide() {
        return getWorld() != null && !getWorld().isRemote;
    }

    default boolean isClientSide() {
        return getWorld() != null && getWorld().isRemote;
    }

    void markDirty();

    void notifyBlockUpdate();

    default void scheduleRenderUpdate() {
        BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }
}
