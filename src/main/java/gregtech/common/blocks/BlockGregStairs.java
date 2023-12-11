package gregtech.common.blocks;

import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ToolClasses;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.jetbrains.annotations.NotNull;

public class BlockGregStairs extends BlockStairs {

    public BlockGregStairs(IBlockState state) {
        super(state);
        setCreativeTab(GregTechAPI.TAB_GREGTECH_DECORATIONS);
        this.useNeighborBrightness = true;
        this.setHarvestLevel(ToolClasses.AXE, 0);
    }

    @Override
    public boolean doesSideBlockChestOpening(@NotNull IBlockState blockState, @NotNull IBlockAccess world,
                                             @NotNull BlockPos pos, @NotNull EnumFacing side) {
        return false;
    }
}
