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

    @NotNull
    private final World world;
    @NotNull
    private final BlockPos pos;

    private GTPipeColorContainer(@NotNull World world, @NotNull BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    @Override
    public boolean setColor(@Nullable EnumDyeColor newColor) {
        if (newColor == null) {
            return removeColor();
        }

        if (getColorInt() == newColor.colorValue) {
            return false;
        }

        if (world.getTileEntity(pos) instanceof IPipeTile<?, ?>pipeTile) {
            pipeTile.setPaintingColor(newColor.colorValue);
            return true;
        }

        return false;
    }

    @Override
    public boolean setColor(int newColor) {
        if (newColor == -1) {
            return removeColor();
        }

        if (world.getTileEntity(pos) instanceof IPipeTile<?, ?>pipeTile) {
            if (pipeTile.isPainted() && getColorInt() == newColor) {
                return false;
            } else {
                pipeTile.setPaintingColor(newColor);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean supportsARGB() {
        return true;
    }

    @Override
    public boolean removeColor() {
        if (world.getTileEntity(pos) instanceof IPipeTile<?, ?>pipeTile && pipeTile.isPainted()) {
            pipeTile.setPaintingColor(-1);
            return true;
        }

        return false;
    }

    @Override
    public @Nullable EnumDyeColor getColor() {
        int mteColor = getColorInt();
        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            if (mteColor == dyeColor.colorValue) {
                return dyeColor;
            }
        }

        return null;
    }

    public @Nullable EnumDyeColor getPaintingColor() {
        int mteColor = getPaintingColorInt();
        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            if (mteColor == dyeColor.colorValue) {
                return dyeColor;
            }
        }

        return null;
    }

    @Override
    public int getColorInt() {
        if (world.getTileEntity(pos) instanceof IPipeTile<?, ?>pipeTile) {
            return pipeTile.getPaintingColor();
        }

        return -1;
    }

    public int getPaintingColorInt() {
        if (world.getTileEntity(pos) instanceof IPipeTile<?, ?>pipeTile && pipeTile.isPainted()) {
            return pipeTile.getPaintingColor();
        }

        return -1;
    }

    public static class GTPipeColorManager extends ContainerManager {

        @Override
        protected @NotNull ColoredBlockContainer createInstance(@NotNull World world, @NotNull BlockPos pos,
                                                                @NotNull EnumFacing facing,
                                                                @NotNull EntityPlayer player) {
            return new GTPipeColorContainer(world, pos);
        }

        @Override
        protected boolean blockMatches(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                       @NotNull EntityPlayer player) {
            return world.getTileEntity(pos) instanceof IPipeTile<?, ?>;
        }
    }
}
