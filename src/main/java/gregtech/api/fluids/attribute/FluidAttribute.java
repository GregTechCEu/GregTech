package gregtech.api.fluids.attribute;

import gregtech.api.fluids.ContainmentFailureHandler;

import gregtech.api.util.function.TriConsumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class FluidAttribute implements ContainmentFailureHandler {

    private final ResourceLocation resourceLocation;
    private final Consumer<List<String>> fluidTooltip;
    private final Consumer<List<String>> containerTooltip;
    private final TriConsumer<World, BlockPos, FluidStack> blockContainmentFailure;
    private final BiConsumer<EntityPlayer, FluidStack> playerContainmentFailure;
    private final int hashCode;

    public FluidAttribute(@NotNull ResourceLocation resourceLocation,
                          @NotNull Consumer<List<@NotNull String>> fluidTooltip,
                          @NotNull Consumer<List<@NotNull String>> containerTooltip,
                          @NotNull TriConsumer<World, BlockPos, FluidStack> blockContainmentFailure,
                          @NotNull BiConsumer<EntityPlayer, FluidStack> playerContainmentFailure) {
        this.resourceLocation = resourceLocation;
        this.fluidTooltip = fluidTooltip;
        this.containerTooltip = containerTooltip;
        this.hashCode = resourceLocation.hashCode();
        this.blockContainmentFailure = blockContainmentFailure;
        this.playerContainmentFailure = playerContainmentFailure;
    }

    public static Collection<FluidAttribute> inferAttributes(FluidStack stack) {
        if (stack.getFluid() instanceof AttributedFluid fluid) return fluid.getAttributes();
        else return Collections.emptyList();
    }

    public @NotNull ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public void appendFluidTooltips(@NotNull List<@NotNull String> tooltip) {
        fluidTooltip.accept(tooltip);
    }

    public void appendContainerTooltips(@NotNull List<@NotNull String> tooltip) {
        containerTooltip.accept(tooltip);
    }

    @Override
    public void handleFailure(World world, BlockPos failingBlock, FluidStack failingStack) {
        blockContainmentFailure.accept(world, failingBlock, failingStack);
    }

    @Override
    public void handleFailure(EntityPlayer failingPlayer, FluidStack failingStack) {
        playerContainmentFailure.accept(failingPlayer, failingStack);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FluidAttribute that = (FluidAttribute) o;

        return resourceLocation.equals(that.getResourceLocation());
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public @NotNull String toString() {
        return "FluidAttribute{" + resourceLocation + '}';
    }
}
