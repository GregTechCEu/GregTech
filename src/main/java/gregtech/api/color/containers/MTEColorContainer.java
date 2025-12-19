package gregtech.api.color.containers;

import gregtech.api.color.ColorModeSupport;
import gregtech.api.color.ColoredBlockContainer;
import gregtech.api.metatileentity.MetaTileEntity;
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

import static gregtech.api.util.GTUtility.getMetaTileEntity;

public class MTEColorContainer extends ColoredBlockContainer {

    public MTEColorContainer(@NotNull ResourceLocation id) {
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

        MetaTileEntity mte = getMetaTileEntity(world, pos);
        if (mte != null && mte.canBeModifiedBy(player)) {
            mte.setPaintingColor(newColor, facing);
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

        if (colorMatches(world, pos, facing, player, newColor)) {
            return EnumActionResult.PASS;
        }

        MetaTileEntity mte = getMetaTileEntity(world, pos);
        if (mte != null && mte.canBeModifiedBy(player)) {
            mte.setPaintingColor(newColor, facing);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public @NotNull EnumActionResult removeColor(@NotNull World world, @NotNull BlockPos pos,
                                                 @NotNull EnumFacing facing,
                                                 @NotNull EntityPlayer player) {
        MetaTileEntity mte = getMetaTileEntity(world, pos);
        if (mte != null && mte.isPainted() && mte.canBeModifiedBy(player)) {
            mte.setPaintingColor(-1, facing);
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
        MetaTileEntity mte = getMetaTileEntity(world, pos);
        if (mte != null) {
            return mte.getPaintingColor();
        }

        return -1;
    }

    @Override
    public boolean isBlockValid(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                @NotNull EntityPlayer player) {
        MetaTileEntity mte = getMetaTileEntity(world, pos);
        return mte != null && mte.isValid();
    }

    @Override
    public @NotNull ColorModeSupport getSupportedColorMode() {
        return ColorModeSupport.EITHER;
    }
}
