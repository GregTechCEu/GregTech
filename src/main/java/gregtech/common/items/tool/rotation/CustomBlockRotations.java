package gregtech.common.items.tool.rotation;

import gregtech.api.cover.CoverRayTracer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

public class CustomBlockRotations {

    private static final Map<Block, ICustomRotationBehavior> CUSTOM_BEHAVIOR_MAP = new Object2ObjectOpenHashMap<>();

    @ApiStatus.Internal
    public static void init() {
        // nice little way to initialize an inner-class enum
        CustomRotations.init();
    }

    public static void registerCustomRotation(Block block, ICustomRotationBehavior behavior) {
        CUSTOM_BEHAVIOR_MAP.put(block, behavior);
    }

    public static ICustomRotationBehavior getCustomRotation(Block block) {
        return CUSTOM_BEHAVIOR_MAP.get(block);
    }

    public static final ICustomRotationBehavior BLOCK_HORIZONTAL_BEHAVIOR = new ICustomRotationBehavior() {

        @Override
        public boolean customRotate(IBlockState state, World world, BlockPos pos, RayTraceResult hitResult) {
            EnumFacing gridSide = CoverRayTracer.determineGridSideHit(hitResult);
            if (gridSide == null) return false;
            if (gridSide.getAxis() == EnumFacing.Axis.Y) return false;

            if (gridSide != state.getValue(BlockHorizontal.FACING)) {
                state = state.withProperty(BlockHorizontal.FACING, gridSide);
                world.setBlockState(pos, state);
                return true;
            }
            return false;
        }

        @Override
        public boolean showXOnSide(IBlockState state, EnumFacing facing) {
            return state.getValue(BlockHorizontal.FACING) == facing;
        }
    };

    public static final ICustomRotationBehavior BLOCK_DIRECTIONAL_BEHAVIOR = new ICustomRotationBehavior() {

        @Override
        public boolean customRotate(IBlockState state, World world, BlockPos pos, RayTraceResult hitResult) {
            EnumFacing gridSide = CoverRayTracer.determineGridSideHit(hitResult);
            if (gridSide == null) return false;

            if (gridSide != state.getValue(BlockDirectional.FACING)) {
                state = state.withProperty(BlockDirectional.FACING, gridSide);
                world.setBlockState(pos, state);
                return true;
            }
            return false;
        }

        @Override
        public boolean showXOnSide(IBlockState state, EnumFacing facing) {
            return state.getValue(BlockDirectional.FACING) == facing;
        }
    };

    private enum CustomRotations {

        // BlockDirectional
        PISTON(Blocks.PISTON, BLOCK_DIRECTIONAL_BEHAVIOR),
        STICKY_PISTON(Blocks.STICKY_PISTON, BLOCK_DIRECTIONAL_BEHAVIOR),
        DROPPER(Blocks.DROPPER, BLOCK_DIRECTIONAL_BEHAVIOR),
        DISPENSER(Blocks.DISPENSER, BLOCK_DIRECTIONAL_BEHAVIOR),
        OBSERVER(Blocks.OBSERVER, BLOCK_DIRECTIONAL_BEHAVIOR),

        // BlockHorizontal
        FURNACE(Blocks.FURNACE, BLOCK_HORIZONTAL_BEHAVIOR),
        LIT_FURNACE(Blocks.LIT_FURNACE, BLOCK_HORIZONTAL_BEHAVIOR),
        PUMPKIN(Blocks.PUMPKIN, BLOCK_HORIZONTAL_BEHAVIOR),
        LIT_PUMPKIN(Blocks.LIT_PUMPKIN, BLOCK_HORIZONTAL_BEHAVIOR),
        CHEST(Blocks.CHEST, BLOCK_HORIZONTAL_BEHAVIOR),
        TRAPPED_CHEST(Blocks.TRAPPED_CHEST, BLOCK_HORIZONTAL_BEHAVIOR),
        ENDER_CHEST(Blocks.ENDER_CHEST, BLOCK_HORIZONTAL_BEHAVIOR),

        // Custom facings

        // Cannot face up, and uses a custom BlockState property key
        HOPPER(Blocks.HOPPER, new ICustomRotationBehavior() {

            @Override
            public boolean customRotate(IBlockState state, World world, BlockPos pos, RayTraceResult hitResult) {
                EnumFacing gridSide = CoverRayTracer.determineGridSideHit(hitResult);
                if (gridSide == null || gridSide == EnumFacing.UP) return false;

                if (gridSide != state.getValue(BlockHopper.FACING)) {
                    state = state.withProperty(BlockHopper.FACING, gridSide);
                    world.setBlockState(pos, state);
                    return true;
                }
                return false;
            }

            @Override
            public boolean showXOnSide(IBlockState state, EnumFacing facing) {
                return state.getValue(BlockHopper.FACING) == facing;
            }
        }),

        ;

        CustomRotations(Block block, ICustomRotationBehavior behavior) {
            registerCustomRotation(block, behavior);
        }

        private static void init() {}
    }
}
