package gregtech.api.color.containers;

import gregtech.api.color.ColoredBlockContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.util.AEColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AE2ColorContainer extends ColoredBlockContainer {

    @Override
    public boolean setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                            @NotNull EntityPlayer player, @Nullable EnumDyeColor newColor) {
        if (newColor == null) {
            return removeColor(world, pos, facing, player);
        }

        if (getColor(world, pos, facing, player) == newColor) {
            return false;
        }

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IColorableTile colorableTile) {
            if (colorableTile.getColor().dye != newColor) {
                colorableTile.recolourBlock(facing, AEColor.values()[newColor.ordinal()], player);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean removeColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                               @NotNull EntityPlayer player) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IColorableTile colorableTile && colorableTile.getColor() != AEColor.TRANSPARENT) {
            colorableTile.recolourBlock(facing, AEColor.TRANSPARENT, player);
            return true;
        }

        return false;
    }

    @Override
    public @Nullable EnumDyeColor getColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                           @NotNull EntityPlayer player) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IColorableTile colorableTile) {
            return colorableTile.getColor().dye;
        }

        return null;
    }

    @Override
    public boolean isBlockValid(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                @NotNull EntityPlayer player) {
        return world.getTileEntity(pos) instanceof IColorableTile;
    }
}
