package gregtech.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;

public interface IWalkingSpeedBonus {

    default double getWalkingSpeedBonus() {
        return 1.0D;
    }

    default boolean checkApplicableBlocks(IBlockState state) {
        return false;
    }

    default boolean bonusSpeedCondition(Entity walkingEntity) {
        return !walkingEntity.isInWater();
    }
}
