package gregtech.api.color.containers;

import gregtech.api.color.ColoredBlockContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static gregtech.api.util.GTUtility.getMetaTileEntity;

public class MTEColorContainer extends ColoredBlockContainer {

    @Override
    public boolean setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                            @NotNull EntityPlayer player, @Nullable EnumDyeColor newColor) {
        if (newColor == null) {
            return removeColor(world, pos, facing, player);
        }

        if (getColorInt(world, pos, facing, player) == newColor.colorValue) {
            return false;
        }

        MetaTileEntity mte = getMetaTileEntity(world, pos);
        if (mte != null && mte.canBeModifiedBy(player)) {
            mte.setPaintingColor(newColor, facing);
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

        if (getColorInt(world, pos, facing, player) == newColor) {
            return false;
        }

        MetaTileEntity mte = getMetaTileEntity(world, pos);
        if (mte != null && mte.canBeModifiedBy(player)) {
            mte.setPaintingColor(newColor, facing);
            return true;
        }

        return false;
    }

    @Override
    public boolean removeColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                               @NotNull EntityPlayer player) {
        MetaTileEntity mte = getMetaTileEntity(world, pos);
        if (mte != null && mte.isPainted() && mte.canBeModifiedBy(player)) {
            mte.setPaintingColor(-1, facing);
            return true;
        }

        return false;
    }

    @Override
    public @Nullable EnumDyeColor getColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                           @NotNull EntityPlayer player) {
        return GTUtility.getDyeColorFromARGB(getColorInt(world, pos, facing, player));
    }

    @Override
    public int getColorInt(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                           @NotNull EntityPlayer player) {
        MetaTileEntity mte = getMetaTileEntity(world, pos);
        if (mte != null) {
            return mte.getPaintingColor();
        }

        return -1;
    }

    @Override
    public boolean isValid(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                           @NotNull EntityPlayer player) {
        MetaTileEntity mte = getMetaTileEntity(world, pos);
        return mte != null && mte.isValid();
    }

    @Override
    public boolean supportsARGB() {
        return true;
    }
}
