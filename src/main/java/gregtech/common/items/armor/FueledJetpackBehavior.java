package gregtech.common.items.armor;

import gregtech.api.capability.IFilter;
import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.items.armoritem.ArmorHelper;
import gregtech.api.items.armoritem.jetpack.IJetpackStats;
import gregtech.api.items.armoritem.jetpack.JetpackBehavior;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Objects;

/**
 * Creates a Jetpack which runs on any Fuel in a provided fuel RecipeMap.
 */
public class FueledJetpackBehavior extends JetpackBehavior {

    private final int maxCapacity;
    private final RecipeMap<?> fuelMap;

    private Recipe recipe;

    public FueledJetpackBehavior(IJetpackStats jetpackStats, int maxCapacity, RecipeMap<?> fuelMap) {
        super(jetpackStats);
        this.maxCapacity = maxCapacity;
        this.fuelMap = fuelMap;
    }

    @Override
    protected int getFuelPerUse() {
        return 1;
    }

    @Override
    protected boolean drainFuel(@NotNull ItemStack stack, int amount, boolean simulate) {
        IFluidHandlerItem fluidHandler = getFluidHandler(stack);
        if (fluidHandler != null) {
            // check that we have some amount of fuel here.
            FluidStack currentFuel = fluidHandler.drain(amount, false);
            if (currentFuel == null) return false;

            // grab the recipe if we haven't already.
            // don't need to worry about resetting as our fluid handler does this for us.
            if (recipe == null) {
                recipe = getRecipe(currentFuel);
            }

            // do the "drain" if we are not simulating.
            // either decrementing our timer, or draining new fuel and restarting the timer
            if (!simulate) {
                NBTTagCompound tag = ArmorHelper.getBehaviorsTag(stack);
                short burnTimer = tag.getShort(ArmorHelper.FUELED_JETPACK_BURN_TIMER);
                if (burnTimer == 0) {
                    // timer is zero, drain some fuel and restart it
                    fluidHandler.drain(amount, true);
                    burnTimer = (short) recipe.getDuration();
                } else {
                    // timer is non-zero, just decrement the timer
                    burnTimer--;
                }
                tag.setShort(ArmorHelper.FUELED_JETPACK_BURN_TIMER, burnTimer);
            }
            return true;
        }
        return false;
    }

    private IFluidHandlerItem getFluidHandler(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    }

    private Recipe getRecipe(@NotNull FluidStack fuel) {
        return fuelMap.find(Collections.emptyList(), Collections.singletonList(fuel), Objects::nonNull);
    }

    @Override
    public ICapabilityProvider createProvider(@NotNull ItemStack stack, @Nullable NBTTagCompound tag) {
        return new FueledJetpackItemStack(stack, maxCapacity)
                .setFilter(new FueledJetpackFilter())
                .setCanDrain(false);
    }

    private final class FueledJetpackFilter implements IFilter<FluidStack> {

        @Override
        public boolean test(@Nonnull FluidStack fluidStack) {
            return getRecipe(fluidStack) != null;
        }

        @Override
        public int getPriority() {
            return IFilter.whitelistLikePriority();
        }
    }

    /* Override class to automatically clear our cached state when the tank is fully emptied. */
    private final class FueledJetpackItemStack extends GTFluidHandlerItemStack {

        public FueledJetpackItemStack(@Nonnull ItemStack container, int capacity) {
            super(container, capacity);
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            FluidStack drained = super.drain(resource, doDrain);
            if (doDrain && this.getFluid() == null) {
                recipe = null;
            }
            return drained;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            FluidStack drained = super.drain(maxDrain, doDrain);
            if (doDrain && this.getFluid() == null) {
                recipe = null;
            }
            return drained;
        }
    }
}
