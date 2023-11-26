package gregtech.api.metatileentity.interfaces;

import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IHasWorldObjectAndCoords extends IDirtyNotifiable {

    World world();

    BlockPos pos();

    default boolean isServerSide() {
        return world() != null && !world().isRemote;
    }

    default boolean isClientSide() {
        return world() != null && world().isRemote;
    }

    void notifyBlockUpdate();

    default void scheduleRenderUpdate() {
        BlockPos pos = pos();
        world().markBlockRangeForRenderUpdate(
                pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }
}
