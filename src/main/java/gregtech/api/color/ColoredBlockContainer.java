package gregtech.api.color;

import gregtech.api.color.containers.AE2ColorContainer;
import gregtech.api.color.containers.BedColorContainer;
import gregtech.api.color.containers.GTPipeColorContainer;
import gregtech.api.color.containers.MTEColorContainer;
import gregtech.api.color.containers.VanillaColorContainer;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Used to provide a consistent interface for dealing with colored blocks, whether vanilla or modded. <br/>
 * Inspired by GT5u's <a href=
 * "https://github.com/GTNewHorizons/GT5-Unofficial/blob/7ba0fc903e5d14928d2b894b00a7b7dfc65eee18/src/main/java/gregtech/api/util/ColoredBlockContainer.java">ColoredBlockContainer</a>
 */
public abstract class ColoredBlockContainer {

    @NotNull
    private static final Map<ResourceLocation, ColoredBlockContainer> CONTAINERS = new Object2ObjectOpenHashMap<>(5);

    public static void registerContainer(@NotNull ColoredBlockContainer container) {
        Objects.requireNonNull(container, "A null ColoredBlockContainer cannot be registered!");
        ResourceLocation id = container.id;
        Objects.requireNonNull(id, "A ColoredBlockContainer cannot have a null ID!");
        if (CONTAINERS.containsKey(id)) {
            throw new IllegalArgumentException(
                    String.format("A ColoredBlockContainer with an ID of %s already exists!", id));
        }

        CONTAINERS.put(id, container);
    }

    /**
     * Get the color container for the block or tile entity at the provided position. <br/>
     * Will return {@code null} if no container was valid.
     */
    public static @Nullable ColoredBlockContainer getContainer(@NotNull World world, @NotNull BlockPos pos,
                                                               @NotNull EnumFacing facing,
                                                               @NotNull EntityPlayer player) {
        for (ColoredBlockContainer container : CONTAINERS.values()) {
            if (container.isBlockValid(world, pos, facing, player)) {
                return container;
            }
        }

        return null;
    }

    @ApiStatus.Internal
    public static void registerCEuContainers() {
        registerContainer(new GTPipeColorContainer(GTUtility.gregtechId("pipe")));
        registerContainer(new MTEColorContainer(GTUtility.gregtechId("mte")));
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            registerContainer(new AE2ColorContainer(GTUtility.gregtechId("ae2")));
        }
        registerContainer(new VanillaColorContainer(GTUtility.gregtechId("vanilla")));
        registerContainer(new BedColorContainer(GTUtility.gregtechId("bed")));
    }

    @NotNull
    protected final ResourceLocation id;

    public ColoredBlockContainer(@NotNull ResourceLocation id) {
        this.id = id;
    }

    public abstract boolean isBlockValid(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                         @NotNull EntityPlayer player);

    public @NotNull EnumActionResult setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                              @NotNull EntityPlayer player, @Nullable EnumDyeColor newColor) {
        return EnumActionResult.PASS;
    }

    public @NotNull EnumActionResult setColor(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                              @NotNull EntityPlayer player, int newColor) {
        return EnumActionResult.PASS;
    }

    public @NotNull EnumActionResult removeColor(@NotNull World world, @NotNull BlockPos pos,
                                                 @NotNull EnumFacing facing,
                                                 @NotNull EntityPlayer player) {
        return EnumActionResult.PASS;
    }

    public @Nullable EnumDyeColor getColor(@NotNull World world, @NotNull BlockPos pos,
                                           @NotNull EnumFacing facing, @NotNull EntityPlayer player) {
        return null;
    }

    public int getColorInt(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                           @NotNull EntityPlayer player) {
        EnumDyeColor dyeColor = getColor(world, pos, facing, player);
        return dyeColor == null ? -1 : dyeColor.colorValue;
    }

    public boolean colorMatches(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                @NotNull EntityPlayer player, @Nullable EnumDyeColor color) {
        return getColor(world, pos, facing, player) == color;
    }

    public boolean colorMatches(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
                                @NotNull EntityPlayer player, int color) {
        return getColorInt(world, pos, facing, player) == color;
    }

    public abstract @NotNull ColorModeSupport getSupportedColorMode();
}
