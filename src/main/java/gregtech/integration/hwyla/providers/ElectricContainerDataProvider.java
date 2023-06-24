package gregtech.integration.hwyla.providers;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.integration.hwyla.CapabilityDataProvider;
import gregtech.integration.hwyla.HWYLAConfigKeys;
import mcp.mobius.waila.api.ITaggedList;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ElectricContainerDataProvider extends CapabilityDataProvider<IEnergyContainer> {

    public static final IWailaDataProvider INSTANCE = new ElectricContainerDataProvider();

    @Override
    protected @NotNull Capability<IEnergyContainer> getCapability() {
        return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
    }

    @Override
    protected boolean allowDisplaying(@NotNull IEnergyContainer capability) {
        return !capability.isOneProbeHidden();
    }

    @Override
    protected NBTTagCompound getNBTData(IEnergyContainer capability, NBTTagCompound tag) {
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setLong("Capacity", capability.getEnergyCapacity());
        subTag.setLong("Stored", capability.getEnergyStored());
        tag.setTag("EnergyContainerEU", subTag);
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        if (!config.getConfig(HWYLAConfigKeys.ENERGY_CONTAINER) || accessor.getTileEntity() == null) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("EnergyContainerEU")) {
            NBTTagCompound energyTag = accessor.getNBTData().getCompoundTag("EnergyContainerEU");
            long stored = energyTag.getLong("Stored");
            long capacity = energyTag.getLong("Capacity");

            ((ITaggedList<String, String>) tooltip).add(String.format("%d / %d EU", stored, capacity), "IEnergyContainer");
            return tooltip;
        }
        return tooltip;
    }
}
