package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

public class BlockGregStairs extends BlockStairs {

    public BlockGregStairs(IBlockState state) {
        super(state);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_DECORATIONS);
        this.useNeighborBrightness = true;
        this.setHarvestLevel(ToolClasses.AXE, 0);
    }

    @Override
    public boolean doesSideBlockChestOpening(@Nonnull IBlockState blockState, @Nonnull IBlockAccess world,
                                             @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
        return false;
    }
}
