package gregtech.api.items.metaitem;

import gregtech.api.capability.impl.SimpleThermalFluidHandlerItemStack;
import gregtech.api.capability.impl.ThermalFluidHandlerItemStack;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ThermalFluidStats implements IItemComponent, IItemCapabilityProvider {

    public final int capacity;
    public final int maxFluidTemperature;
    private final boolean gasProof;
    private final boolean acidProof;
    private final boolean cryoProof;
    private final boolean plasmaProof;
    public final boolean allowPartialFill;

    public ThermalFluidStats(int capacity, int maxFluidTemperature, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof, boolean allowPartialFill) {
        this.capacity = capacity;
        this.maxFluidTemperature = maxFluidTemperature;
        this.gasProof = gasProof;
        this.acidProof = acidProof;
        this.cryoProof = cryoProof;
        this.plasmaProof = plasmaProof;
        this.allowPartialFill = allowPartialFill;
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        if (allowPartialFill) {
            return new ThermalFluidHandlerItemStack(itemStack, capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof);
        }
        return new SimpleThermalFluidHandlerItemStack(itemStack, capacity, maxFluidTemperature, gasProof, acidProof, cryoProof, plasmaProof);
    }
}
