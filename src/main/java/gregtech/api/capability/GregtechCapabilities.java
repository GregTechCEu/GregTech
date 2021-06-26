package gregtech.api.capability;

import gregtech.api.capability.tool.IScrewdriverItem;
import gregtech.api.capability.tool.ISoftHammerItem;
import gregtech.api.capability.tool.IWrenchItem;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class GregtechCapabilities {

    @CapabilityInject(IEnergyContainer.class)
    public static Capability<IEnergyContainer> CAPABILITY_ENERGY_CONTAINER = null;

    @CapabilityInject(IElectricItem.class)
    public static Capability<IElectricItem> CAPABILITY_ELECTRIC_ITEM = null;

    @CapabilityInject(IWrenchItem.class)
    public static Capability<IWrenchItem> CAPABILITY_WRENCH = null;

    @CapabilityInject(IScrewdriverItem.class)
    public static Capability<IScrewdriverItem> CAPABILITY_SCREWDRIVER = null;

    @CapabilityInject(ISoftHammerItem.class)
    public static Capability<ISoftHammerItem> CAPABILITY_MALLET = null;

    @CapabilityInject(IFuelable.class)
    public static Capability<IFuelable> CAPABILITY_FUELABLE = null;

}
