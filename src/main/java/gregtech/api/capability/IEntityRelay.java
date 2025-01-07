package gregtech.api.capability;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IEntityRelay {

    /**
     * Receive an entity.
     *
     * @param entity         the entity to receive. Teleportation is left up to the implementer.
     * @param acceptorPos    the position of the block that authored the request. May be null.
     * @param acceptorFacing the side of the block that authored the request. May be null.
     * @return whether the entity was handled by this relay.
     */
    boolean receiveEntity(@NotNull Entity entity, @Nullable BlockPos acceptorPos, @Nullable EnumFacing acceptorFacing);
}
