package gregtech.common.items.tool.rotation;

import gregtech.api.GTValues;
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
import net.minecraftforge.fml.common.Loader;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class CustomBlockRotations {

    private static final List<ICustomRotationBehavior> CUSTOM_BEHAVIORS = new ArrayList<>();

    @ApiStatus.Internal
    public static void init() {
        registerCustomRotation(BLOCK_HORIZONTAL_BEHAVIOR);
        registerCustomRotation(BLOCK_DIRECTIONAL_BEHAVIOR);
        registerCustomRotation(HOPPER_BEHAVIOR);

        // Mod-specific custom rotation logic
        if (Loader.isModLoaded(GTValues.MODID_APPENG)) {
            AECustomBlockRotations.init();
        }
    }

    public static void registerCustomRotation(ICustomRotationBehavior behavior) {
        CUSTOM_BEHAVIORS.add(behavior);
    }

    @Nullable
    public static ICustomRotationBehavior getCustomRotation(@NotNull IBlockState state, @NotNull World world,
                                                            @NotNull BlockPos pos) {
        for (ICustomRotationBehavior behavior : CUSTOM_BEHAVIORS) {
            if (behavior.doesApply(state, world, pos)) {
                return behavior;
            }
        }
        return null;
    }

    private static final ICustomRotationBehavior BLOCK_HORIZONTAL_BEHAVIOR = new ICustomRotationBehavior() {

        @Override
        public boolean doesApply(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos) {
            Block block = state.getBlock();
            return block == Blocks.FURNACE || block == Blocks.LIT_FURNACE || block == Blocks.PUMPKIN ||
                    block == Blocks.LIT_PUMPKIN || block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST ||
                    block == Blocks.ENDER_CHEST;
        }

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
        public boolean showXOnSide(IBlockState state, World world, BlockPos pos, EnumFacing facing) {
            return state.getValue(BlockHorizontal.FACING) == facing;
        }
    };

    private static final ICustomRotationBehavior BLOCK_DIRECTIONAL_BEHAVIOR = new ICustomRotationBehavior() {

        @Override
        public boolean doesApply(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos) {
            Block block = state.getBlock();
            return block == Blocks.PISTON || block == Blocks.STICKY_PISTON || block == Blocks.DROPPER ||
                    block == Blocks.DISPENSER || block == Blocks.OBSERVER;
        }

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
        public boolean showXOnSide(IBlockState state, World world, BlockPos pos, EnumFacing facing) {
            return state.getValue(BlockDirectional.FACING) == facing;
        }
    };

    private static final ICustomRotationBehavior HOPPER_BEHAVIOR = new ICustomRotationBehavior() {

        @Override
        public boolean doesApply(@NotNull IBlockState state, @NotNull World world, @NotNull BlockPos pos) {
            return state.getBlock() == Blocks.HOPPER;
        }

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
        public boolean showXOnSide(IBlockState state, World world, BlockPos pos, EnumFacing facing) {
            return state.getValue(BlockHopper.FACING) == facing;
        }
    };
}
