package gregtech.api.capability.impl;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Simple, standardized way to represent multiple fluid handlers as a single handler.
 */
public class FluidHandlerList implements IFluidHandler {

    private final Set<IFluidHandler> list;

    public FluidHandlerList(Collection<? extends IFluidHandler> handlers) {
        list = new ObjectOpenHashSet<>(handlers);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        List<IFluidTankProperties> list = new ObjectArrayList<>();
        for (IFluidHandler handler : this.list) {
            Collections.addAll(list, handler.getTankProperties());
        }
        return list.toArray(new IFluidTankProperties[0]);
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        int filled = 0;
        for (IFluidHandler handler : list) {
            filled += handler.fill(resource, doFill);
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        resource = resource.copy();
        int drain = 0;
        for (IFluidHandler handler : list) {
            FluidStack d = handler.drain(resource, doDrain);
            if (d != null) {
                drain += d.amount;
                resource.amount -= d.amount;
            }
        }
        if (drain == 0) return null;
        resource.amount = drain;
        return resource;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        FluidStack drain = null;
        FluidStack helper = null;
        for (IFluidHandler handler : list) {
            if (drain == null) {
                drain = handler.drain(maxDrain, doDrain);
                helper = drain;
            } else {
                helper.amount = maxDrain - drain.amount;
                FluidStack d = handler.drain(helper, doDrain);
                if (d != null) drain.amount += d.amount;
            }
        }
        return drain;
    }

    @NotNull
    @UnmodifiableView
    public Collection<IFluidHandler> getBackingHandlers() {
        return list;
    }
}
