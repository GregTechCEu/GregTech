package gregtech.worldgen;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import org.jetbrains.annotations.NotNull;

import static gregtech.worldgen.WorldgenUtil.RockPlacementResult.*;

public final class WorldgenUtil {

    /**
     * Checks if a surface rock can replace a block
     *
     * @param world the world containing the block
     * @param pos   the position of the block
     * @param state the block to check for replacement
     * @return the result
     */
    public static @NotNull WorldgenUtil.RockPlacementResult canSurfaceRockReplace(@NotNull World world, @NotNull BlockPos pos,
                                                                                  @NotNull IBlockState state) {
        Block block = state.getBlock();
        if (block.isAir(state, world, pos) || block.isReplaceable(world, pos) || state.isOpaqueCube() ||
                block instanceof BlockBush || block instanceof BlockFire || block.canBeReplacedByLeaves(state, world, pos)) {
            if (state.isOpaqueCube()) return SKIP_LAYER;

            var material = state.getMaterial();
            if (material == Material.PLANTS || material == Material.VINE || material == Material.CORAL
                    || material == Material.CACTUS || material == Material.GOURD) {
                return SKIP_LAYER;
            }

            if (block instanceof IFluidBlock || block instanceof BlockLiquid) {
                return SKIP_COLUMN;
            }
            return SUCCESS;
        }
        return SKIP_LAYER;
    }

    /**
     * Checks if a surface rock can be placed on top of another block
     *
     * @param world the world containing the block
     * @param pos   the position of the block to place on top of
     * @param state the block to check
     * @return the result
     */
    public static @NotNull WorldgenUtil.RockPlacementResult canSurfaceRockStay(@NotNull World world, @NotNull BlockPos pos,
                                                                               @NotNull IBlockState state) {
        Block block = state.getBlock();
        var material = state.getMaterial();

        if (block.isAir(state, world, pos)) return SKIP_LAYER;
        if (block instanceof BlockBush || block instanceof BlockFire) return SKIP_LAYER;
        if (block == Blocks.FARMLAND) return SKIP_COLUMN;
        if (block instanceof IFluidBlock || block instanceof BlockLiquid) return SKIP_COLUMN;
        if (block.isWood(world, pos)) return SKIP_LAYER;
        if (block.isLeaves(state, world, pos)) return SKIP_LAYER;
        if (block.isFoliage(world, pos)) return SKIP_LAYER;
        if (material == Material.PLANTS || material == Material.VINE || material == Material.CORAL
                || material == Material.CACTUS || material == Material.GOURD) return SKIP_LAYER;
        return SUCCESS;
    }

    public enum RockPlacementResult {
        SKIP_COLUMN,
        SKIP_LAYER,
        SUCCESS
    }

    private WorldgenUtil() {}
}
