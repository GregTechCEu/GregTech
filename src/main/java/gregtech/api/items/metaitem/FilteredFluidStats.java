package gregtech.api.items.metaitem;

import gregtech.api.capability.impl.SimpleThermalFluidHandlerItemStack;
import gregtech.api.capability.impl.ThermalFluidHandlerItemStack;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Function;

public class FilteredFluidStats implements IItemComponent, IItemCapabilityProvider {

    public final int maxCapacity;
    public final int minFluidTemperature;
    public final int maxFluidTemperature;
    public final boolean allowPartlyFill;
    private final Function<FluidStack, Boolean> fillPredicate;

    public FilteredFluidStats(int maxCapacity, int minFluidTemperature, int maxFluidTemperature, boolean allowPartlyFill, Function<FluidStack, Boolean> fillPredicate) {
        this.maxCapacity = maxCapacity;
        this.minFluidTemperature = minFluidTemperature;
        this.maxFluidTemperature = maxFluidTemperature;
        this.allowPartlyFill = allowPartlyFill;
        this.fillPredicate = fillPredicate;
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        if (allowPartlyFill) {
            return new ThermalFluidHandlerItemStack(itemStack, maxCapacity, minFluidTemperature, maxFluidTemperature) {
                @Override
                public boolean canFillFluidType(FluidStack fluid) {
                    return super.canFillFluidType(fluid) && fillPredicate.apply(fluid);
                }
            };
        }
        return new SimpleThermalFluidHandlerItemStack(itemStack, maxCapacity, minFluidTemperature, maxFluidTemperature) {
            @Override
            public boolean canFillFluidType(FluidStack fluid) {
                return super.canFillFluidType(fluid) && fillPredicate.apply(fluid);
            }
        };
    }
}
