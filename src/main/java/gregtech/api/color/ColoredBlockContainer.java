package gregtech.api.color;

import gregtech.api.color.containers.AE2ColorContainer;
import gregtech.api.color.containers.GTPipeColorContainer;
import gregtech.api.color.containers.MTEColorContainer;
import gregtech.api.color.containers.NullColorContainer;
import gregtech.api.color.containers.VanillaColorContainer;
import gregtech.api.util.Mods;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

/**
 * Used to provide a consistent interface for dealing with colored blocks, whether vanilla or modded. <br/>
 * Inspired by GT5u's <a href=
 * "https://github.com/GTNewHorizons/GT5-Unofficial/blob/7ba0fc903e5d14928d2b894b00a7b7dfc65eee18/src/main/java/gregtech/api/util/ColoredBlockContainer.java">ColoredBlockContainer</a>
 */
public abstract class ColoredBlockContainer {

    @NotNull
    private static final Set<ColoredBlockContainer> CONTAINERS = new ObjectArraySet<>(4);

    public static void registerContainer(@NotNull ColoredBlockContainer container) {
        CONTAINERS.add(Objects.requireNonNull(container, "A null ColoredBlockContainer cannot be registered!"));
    }

    public static @NotNull ColoredBlockContainer getContainer(@NotNull World world, @NotNull BlockPos pos,
                                                              @NotNull EnumFacing facing,
                                                              @NotNull EntityPlayer player) {
        for (ColoredBlockContainer container : CONTAINERS) {
            if (container.isValid(world, pos, facing, player)) {
                return container;
            }
        }

        return NullColorContainer.NULL_CONTAINER;
    }

    @ApiStatus.Internal
    public static void registerCEuContainers() {
        registerContainer(new GTPipeColorContainer());
        registerContainer(new MTEColorContainer());
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            registerContainer(new AE2ColorContainer());
        }
        registerContainer(new VanillaColorContainer());
    }

    public abstract boolean isValid(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                    @NotNull EntityPlayer player);

    public abstract boolean setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                     @NotNull EntityPlayer player, @Nullable EnumDyeColor newColor);

    public boolean setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                            @NotNull EntityPlayer player, int newColor) {
        return false;
    }

    public abstract boolean removeColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                        @NotNull EntityPlayer player);

    public abstract @Nullable EnumDyeColor getColor(@NotNull World world, @NotNull BlockPos pos,
                                                    @NotNull EnumFacing facing, @NotNull EntityPlayer player);

    public int getColorInt(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                           @NotNull EntityPlayer player) {
        return -1;
    }

    public boolean colorMatches(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                @NotNull EntityPlayer player, @Nullable EnumDyeColor color) {
        return getColor(world, pos, facing, player) == color;
    }

    public boolean colorMatches(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                @NotNull EntityPlayer player, int color) {
        return getColorInt(world, pos, facing, player) == color;
    }

    public boolean supportsARGB() {
        return false;
    }
}
