package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ElectricContainerDataProvider extends CapabilityDataProvider<IEnergyContainer> {

    public static final ElectricContainerDataProvider INSTANCE = new ElectricContainerDataProvider();

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig(GTValues.MODID, "gregtech.energy");
    }

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
        tag.setTag("gregtech.IEnergyContainer", subTag);
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.energy") || accessor.getTileEntity() == null) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.IEnergyContainer")) {
            NBTTagCompound energyTag = accessor.getNBTData().getCompoundTag("gregtech.IEnergyContainer");
            long stored = energyTag.getLong("Stored");
            long capacity = energyTag.getLong("Capacity");
            tooltip.add(I18n.format("gregtech.waila.energy_stored", stored, capacity));
        }
        return tooltip;
    }
}
