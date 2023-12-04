package gregtech.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;

import org.jetbrains.annotations.ApiStatus;

/**
 * @deprecated use {@link gregtech.api.util.BlockUtility#setWalkingSpeedBonus(IBlockState, double)}
 */
@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "2.9")
public interface IWalkingSpeedBonus {

    default double getWalkingSpeedBonus() {
        return 1.0D;
    }

    default boolean checkApplicableBlocks(IBlockState state) {
        return false;
    }

    default boolean bonusSpeedCondition(Entity walkingEntity) {
        return !walkingEntity.isInWater() && !walkingEntity.isSneaking();
    }
}
