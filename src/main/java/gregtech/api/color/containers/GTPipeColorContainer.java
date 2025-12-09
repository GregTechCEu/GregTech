package gregtech.api.color.containers;

import gregtech.api.color.ColorModeSupport;
import gregtech.api.color.ColoredBlockContainer;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.ColorUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTPipeColorContainer extends ColoredBlockContainer {

    public GTPipeColorContainer(@NotNull ResourceLocation id) {
        super(id);
    }

    @Override
    public @NotNull EnumActionResult setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                              @NotNull EntityPlayer player, @Nullable EnumDyeColor newColor) {
        if (newColor == null) {
            return removeColor(world, pos, facing, player);
        }

        if (colorMatches(world, pos, facing, player, newColor.colorValue)) {
            return EnumActionResult.PASS;
        }

        if (world.getTileEntity(pos) instanceof IPipeTile<?, ?>pipeTile) {
            pipeTile.setPaintingColor(newColor.colorValue);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public @NotNull EnumActionResult setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                              @NotNull EntityPlayer player, int newColor) {
        if (newColor == -1) {
            return removeColor(world, pos, facing, player);
        }

        if (world.getTileEntity(pos) instanceof IPipeTile<?, ?>pipeTile) {
            if (pipeTile.isPainted() && colorMatches(world, pos, facing, player, newColor)) {
                return EnumActionResult.PASS;
            } else {
                pipeTile.setPaintingColor(newColor);
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    @Override
    public @NotNull EnumActionResult removeColor(@NotNull World world, @NotNull BlockPos pos,
                                                 @NotNull EnumFacing facing,
                                                 @NotNull EntityPlayer player) {
        if (world.getTileEntity(pos) instanceof IPipeTile<?, ?>pipeTile && pipeTile.isPainted()) {
            pipeTile.setPaintingColor(-1);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public @Nullable EnumDyeColor getColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                           @NotNull EntityPlayer player) {
        return ColorUtil.getDyeColorFromRGB(getColorInt(world, pos, facing, player));
    }

    @Override
    public int getColorInt(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                           @NotNull EntityPlayer player) {
        if (world.getTileEntity(pos) instanceof IPipeTile<?, ?>pipeTile && pipeTile.isPainted()) {
            return pipeTile.getPaintingColor();
        }

        return -1;
    }

    @Override
    public boolean isBlockValid(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                @NotNull EntityPlayer player) {
        return world.getTileEntity(pos) instanceof IPipeTile<?, ?>;
    }

    @Override
    public @NotNull ColorModeSupport getSupportedColorMode() {
        return ColorModeSupport.EITHER;
    }
}
