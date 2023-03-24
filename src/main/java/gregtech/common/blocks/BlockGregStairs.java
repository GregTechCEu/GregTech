package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;

public class BlockGregStairs extends BlockStairs {

    public BlockGregStairs(IBlockState state) {
        super(state);
        setCreativeTab(GregTechAPI.TAB_GREGTECH);
        this.useNeighborBrightness = true;
    }
}
