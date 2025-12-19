package gregtech.api.color.containers;

import gregtech.api.color.ColorModeSupport;
import gregtech.api.color.ColoredBlockContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.util.AEColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AE2ColorContainer extends ColoredBlockContainer {

    public AE2ColorContainer(@NotNull ResourceLocation id) {
        super(id);
    }

    @Override
    public @NotNull EnumActionResult setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                              @NotNull EntityPlayer player, @Nullable EnumDyeColor newColor) {
        if (newColor == null) {
            return removeColor(world, pos, facing, player);
        }

        if (colorMatches(world, pos, facing, player, newColor)) {
            return EnumActionResult.PASS;
        }

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IColorableTile colorableTile) {
            if (colorableTile.getColor().dye != newColor) {
                return colorableTile.recolourBlock(facing, AEColor.values()[newColor.ordinal()], player) ?
                        EnumActionResult.SUCCESS : EnumActionResult.FAIL;
            }
        }

        return EnumActionResult.PASS;
    }

    @Override
    public @NotNull EnumActionResult removeColor(@NotNull World world, @NotNull BlockPos pos,
                                                 @NotNull EnumFacing facing,
                                                 @NotNull EntityPlayer player) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IColorableTile colorableTile && colorableTile.getColor() != AEColor.TRANSPARENT) {
            return colorableTile.recolourBlock(facing, AEColor.TRANSPARENT, player) ? EnumActionResult.SUCCESS :
                    EnumActionResult.PASS;
        }

        return EnumActionResult.PASS;
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

    @Override
    public @NotNull ColorModeSupport getSupportedColorMode() {
        return ColorModeSupport.DYE_ONLY;
    }
}
