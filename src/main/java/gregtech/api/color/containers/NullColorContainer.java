package gregtech.api.color.containers;

import gregtech.api.color.ColoredBlockContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NullColorContainer extends ColoredBlockContainer {

    public static final NullColorContainer NULL_CONTAINER = new NullColorContainer();

    @Override
    public boolean setColor(@NotNull World world, @NotNull BlockPos pos, @Nullable EnumFacing facing,
                            @NotNull EntityPlayer player, @Nullable EnumDyeColor newColor) {
        return false;
    }

    @Override
    public boolean removeColor(@NotNull World world, @NotNull BlockPos pos, @Nullable EnumFacing facing,
                               @NotNull EntityPlayer player) {
        return false;
    }

    @Override
    public @Nullable EnumDyeColor getColor(@NotNull World world, @NotNull BlockPos pos, @Nullable EnumFacing facing,
                                           @NotNull EntityPlayer player) {
        return null;
    }

    @Override
    public boolean isValid(@NotNull World world, @NotNull BlockPos pos, @Nullable EnumFacing facing,
                           @NotNull EntityPlayer player) {
        return false;
    }

    @Override
    public boolean colorMatches(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                @NotNull EntityPlayer player, @Nullable EnumDyeColor color) {
        return false;
    }
}
