package gregtech.api.block;

import net.minecraft.block.state.IBlockState;

public interface IWalkingSpeedBonus {

    default double getWalkingSpeedBonus() {
        return 1.0D;
    }

    default boolean checkApplicableBlocks(IBlockState state) {
        return false;
    }
}
