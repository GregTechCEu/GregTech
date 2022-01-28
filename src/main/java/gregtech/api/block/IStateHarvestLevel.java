package gregtech.api.block;

import net.minecraft.block.state.IBlockState;

public interface IStateHarvestLevel {

    int getHarvestLevel(IBlockState state);

    default String getHarvestTool(IBlockState state) {
        return "pickaxe";
    }
}
