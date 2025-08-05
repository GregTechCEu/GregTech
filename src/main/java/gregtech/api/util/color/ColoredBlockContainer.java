package gregtech.api.util.color;

import gregtech.api.util.color.containers.NullColorContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
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

    private static final Set<ContainerManager> MANAGERS = new ObjectArraySet<>(3);

    public static void registerContainerManager(@NotNull ContainerManager manager) {
        Objects.requireNonNull(manager);
        MANAGERS.add(manager);
    }

    public abstract boolean setColor(@Nullable EnumDyeColor newColor);

    public abstract boolean removeColor();

    public abstract @Nullable EnumDyeColor getColor();

    public int getColorInt() {
        EnumDyeColor dyeColor = getColor();
        return dyeColor == null ? -1 : dyeColor.colorValue;
    }

    public boolean isValid() {
        return true;
    }

    public static @NotNull ColoredBlockContainer getInstance(@NotNull World world, @NotNull BlockPos pos,
                                                             @NotNull EnumFacing facing,
                                                             @NotNull EntityPlayer player) {
        for (ContainerManager manager : MANAGERS) {
            if (manager.blockMatches(world, pos, facing, player)) {
                return manager.createInstance(world, pos, facing, player);
            }
        }

        return NullColorContainer.NULL_CONTAINER;
    }

    public static abstract class ContainerManager {

        protected abstract @NotNull ColoredBlockContainer createInstance(@NotNull World world, @NotNull BlockPos pos,
                                                                         @NotNull EnumFacing facing,
                                                                         @NotNull EntityPlayer player);

        protected abstract boolean blockMatches(@NotNull World world, @NotNull BlockPos pos,
                                                @NotNull EnumFacing facing, @NotNull EntityPlayer player);
    }
}
