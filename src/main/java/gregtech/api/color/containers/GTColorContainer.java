package gregtech.api.color.containers;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.color.ColoredBlockContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTColorContainer extends ColoredBlockContainer {

    @NotNull
    private final World world;
    @NotNull
    private final BlockPos pos;
    @NotNull
    private final EnumFacing facing;
    @NotNull
    private final EntityPlayer player;

    private GTColorContainer(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                             @NotNull EntityPlayer player) {
        this.world = world;
        this.pos = pos;
        this.facing = facing;
        this.player = player;
    }

    @Override
    public boolean setColor(@Nullable EnumDyeColor newColor) {
        if (newColor == null) {
            return removeColor();
        }

        if (getColorInt() == newColor.colorValue) {
            return false;
        }

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IPipeTile<?, ?>pipeTile) {
            pipeTile.setPaintingColor(newColor.colorValue);
            return true;
        } else {
            MetaTileEntity mte = getMetaTileEntity(te);
            if (mte != null && mte.canBeModifiedBy(player)) {
                mte.setPaintingColor(newColor, facing);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean removeColor() {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IPipeTile<?, ?>pipeTile && pipeTile.isPainted()) {
            pipeTile.setPaintingColor(-1);
            return true;
        } else {
            MetaTileEntity mte = getMetaTileEntity(te);
            if (mte != null && mte.isPainted() && mte.canBeModifiedBy(player)) {
                mte.setPaintingColor(-1, facing);
                return true;
            }
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

    @Override
    public int getColorInt() {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IPipeTile<?, ?>pipeTile) {
            return pipeTile.getPaintingColor();
        } else {
            MetaTileEntity mte = getMetaTileEntity(te);
            if (mte != null) {
                return mte.getPaintingColor();
            }
        }

        return -1;
    }

    public static class GTColorManager extends ColoredBlockContainer.ContainerManager {

        @Override
        protected @NotNull ColoredBlockContainer createInstance(@NotNull World world, @NotNull BlockPos pos,
                                                                @NotNull EnumFacing facing,
                                                                @NotNull EntityPlayer player) {
            return new GTColorContainer(world, pos, facing, player);
        }

        @Override
        protected boolean blockMatches(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                       @NotNull EntityPlayer player) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IPipeTile<?, ?>) {
                return true;
            } else if (te instanceof IGregTechTileEntity gtte) {
                MetaTileEntity mte = gtte.getMetaTileEntity();
                if (mte == null || !mte.isValid()) return false;
                return mte.canBeModifiedBy(player);
            }

            return false;
        }
    }

    private static @Nullable MetaTileEntity getMetaTileEntity(@Nullable TileEntity te) {
        if (te instanceof IGregTechTileEntity gtte) {
            MetaTileEntity mte = gtte.getMetaTileEntity();
            if (mte == null || !mte.isValid()) return null;
            return mte;
        }

        return null;
    }
}
