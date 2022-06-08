package gregtech.api.block;

import gregtech.api.items.toolitem.ToolClasses;
import net.minecraft.block.state.IBlockState;

public interface IStateHarvestLevel {

    int getHarvestLevel(IBlockState state);

    default String getHarvestTool(IBlockState state) {
        return ToolClasses.PICKAXE;
    }
}
