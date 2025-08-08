package gregtech.api.color.containers;

import gregtech.api.color.ColoredBlockContainer;
import gregtech.api.pipenet.tile.IPipeTile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTPipeColorContainer extends ColoredBlockContainer {

    @Override
    public boolean setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                            @NotNull EntityPlayer player, @Nullable EnumDyeColor newColor) {
        if (newColor == null) {
            return removeColor(world, pos, facing, player);
        }

        if (getColorInt(world, pos, facing, player) == newColor.colorValue) {
            return false;
        }

        if (world.getTileEntity(pos) instanceof IPipeTile<?, ?>pipeTile) {
            pipeTile.setPaintingColor(newColor.colorValue);
            return true;
        }

        return false;
    }

    @Override
    public boolean setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                            @NotNull EntityPlayer player, int newColor) {
        if (newColor == -1) {
            return removeColor(world, pos, facing, player);
        }

        if (world.getTileEntity(pos) instanceof IPipeTile<?, ?>pipeTile) {
            if (pipeTile.isPainted() && getColorInt(world, pos, facing, player) == newColor) {
                return false;
            } else {
                pipeTile.setPaintingColor(newColor);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean removeColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                               @NotNull EntityPlayer player) {
        if (world.getTileEntity(pos) instanceof IPipeTile<?, ?>pipeTile && pipeTile.isPainted()) {
            pipeTile.setPaintingColor(-1);
            return true;
        }

        return false;
    }

    @Override
    public @Nullable EnumDyeColor getColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                           @NotNull EntityPlayer player) {
        int mteColor = getColorInt(world, pos, facing, player);
        if (mteColor == -1) return null;

        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            if (mteColor == dyeColor.colorValue) {
                return dyeColor;
            }
        }

        return null;
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
    public boolean supportsARGB() {
        return true;
    }

    @Override
    public boolean isValid(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                           @NotNull EntityPlayer player) {
        return world.getTileEntity(pos) instanceof IPipeTile<?, ?>;
    }
}
