package gregtech.api.color.containers;

import gregtech.api.color.ColoredBlockContainer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class VanillaColorContainer extends ColoredBlockContainer {

    private static final BiMap<Block, Block> TRANSFORMATIONS = ImmutableBiMap.of(
            Blocks.GLASS, Blocks.STAINED_GLASS,
            Blocks.GLASS_PANE, Blocks.STAINED_GLASS_PANE,
            Blocks.HARDENED_CLAY, Blocks.STAINED_HARDENED_CLAY);

    private static final Map<Block, IProperty<EnumDyeColor>> PROPERTY_MAP = ImmutableMap.of(
            Blocks.GLASS, BlockStainedGlass.COLOR,
            Blocks.GLASS_PANE, BlockStainedGlassPane.COLOR,
            Blocks.HARDENED_CLAY, BlockColored.COLOR);

    @Override
    public boolean setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                            @NotNull EntityPlayer player, @Nullable EnumDyeColor newColor) {
        if (newColor == null) {
            return removeColor(world, pos, facing, player);
        }

        if (getColor(world, pos, facing, player) == newColor) {
            return false;
        }

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (TRANSFORMATIONS.containsKey(block)) {
            IBlockState newBlockState = TRANSFORMATIONS.get(block)
                    .getDefaultState()
                    .withProperty(PROPERTY_MAP.get(block), newColor);
            return world.setBlockState(pos, newBlockState);
        }

        return block.recolorBlock(world, pos, facing, newColor);
    }

    @Override
    public boolean removeColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                               @NotNull EntityPlayer player) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (TRANSFORMATIONS.containsValue(block)) {
            IBlockState newBlockState = TRANSFORMATIONS.inverse()
                    .get(block)
                    .getDefaultState();
            return world.setBlockState(pos, newBlockState);
        } else {
            for (IProperty<?> prop : state.getPropertyKeys()) {
                if (prop.getName().equals("color") && prop.getValueClass() == EnumDyeColor.class) {
                    IBlockState defaultState = block.getDefaultState();
                    EnumDyeColor defaultColor = EnumDyeColor.WHITE;
                    try {
                        // try to read the default color value from the default state instead of just
                        // blindly setting it to default state, and potentially resetting other values
                        defaultColor = (EnumDyeColor) defaultState.getValue(prop);
                    } catch (IllegalArgumentException ignored) {
                        // no default color, we may have to fallback to WHITE here
                        // other mods that have custom behavior can be done as
                        // special cases above on a case-by-case basis
                    }

                    return block.recolorBlock(world, pos, facing, defaultColor);
                }
            }
        }

        return false;
    }

    @Override
    public @Nullable EnumDyeColor getColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                           @NotNull EntityPlayer player) {
        IBlockState state = world.getBlockState(pos);
        for (IProperty<?> prop : state.getPropertyKeys()) {
            if (prop.getValueClass() == EnumDyeColor.class) {
                // noinspection unchecked <- grr, shakes fist
                return state.getValue((IProperty<EnumDyeColor>) prop);
            }
        }

        return null;
    }

    @Override
    public boolean isBlockValid(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                @NotNull EntityPlayer player) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        if (TRANSFORMATIONS.containsKey(block) || TRANSFORMATIONS.containsValue(block)) {
            return true;
        }

        for (IProperty<?> prop : state.getPropertyKeys()) {
            if (prop.getValueClass() == EnumDyeColor.class) {
                return !world.isAirBlock(pos);
            }
        }

        return false;
    }
}
