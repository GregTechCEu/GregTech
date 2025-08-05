package gregtech.api.color.containers;

import gregtech.api.color.ColoredBlockContainer;
import gregtech.api.util.Mods;

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

    @NotNull
    private final World world;
    @NotNull
    private final BlockPos pos;
    @NotNull
    private final EnumFacing facing;
    @NotNull
    private final EntityPlayer player;

    private AE2ColorContainer(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
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

        if (getColor() == newColor) {
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
    public boolean removeColor() {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IColorableTile colorableTile && colorableTile.getColor() != AEColor.TRANSPARENT) {
            colorableTile.recolourBlock(facing, AEColor.TRANSPARENT, player);
            return true;
        }

        return false;
    }

    @Override
    public @Nullable EnumDyeColor getColor() {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IColorableTile colorableTile) {
            return colorableTile.getColor().dye;
        }

        return null;
    }

    public static class AE2BlockManager extends ColoredBlockContainer.ContainerManager {

        @Override
        protected @NotNull ColoredBlockContainer createInstance(@NotNull World world, @NotNull BlockPos pos,
                                                                @NotNull EnumFacing facing,
                                                                @NotNull EntityPlayer player) {
            return new AE2ColorContainer(world, pos, facing, player);
        }

        @Override
        protected boolean blockMatches(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                       @NotNull EntityPlayer player) {
            if (!Mods.AppliedEnergistics2.isModLoaded()) return false;
            TileEntity te = world.getTileEntity(pos);
            return te instanceof IColorableTile;
        }
    }
}
