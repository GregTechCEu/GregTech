package gregtech.common.pipelike.fluidpipe.net;

import gregtech.api.pipenet.AbstractEdgePredicate;
import gregtech.api.pipenet.IShutteredEdgePredicate;
import gregtech.api.pipenet.StandardEdgePredicate;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.BaseFilterContainer;
import gregtech.common.covers.filter.FluidFilterContainer;
import gregtech.common.covers.filter.ItemFilterContainer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

public class FluidEdgePredicate extends StandardEdgePredicate<FluidEdgePredicate> {

    private final static String KEY = "Fluid";

    static {
        PREDICATES.put(KEY, new FluidEdgePredicate());
    }

    @Override
    public boolean test(Object o) {
        if (shutteredSource || shutteredTarget) return false;
        if (!(o instanceof FluidStack stack)) return false;
        return sourceFilter.test(stack) && targetFilter.test(stack);
    }

    @Override
    public @NotNull FluidEdgePredicate createPredicate() {
        return new FluidEdgePredicate();
    }

    @Override
    protected String predicateName() {
        return KEY;
    }

    @Override
    protected BaseFilterContainer getDefaultFilterContainer() {
        return new FluidFilterContainer(DECOY);
    }
}
