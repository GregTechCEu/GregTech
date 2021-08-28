package gregtech.api.terminal.hardware;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.common.items.behaviors.TerminalBehaviour;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/28
 * @Description:
 */
public class HardwareProvider implements ICapabilityProvider, IItemCapabilityProvider {
    private Map<String, Hardware> providers;
    private ItemStack itemStack;


    public HardwareProvider() {

    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<Hardware> getHardware() {
        if (TerminalBehaviour.isCreative(itemStack)) {
            return new ArrayList<>(providers.values());
        }
        return itemStack.getOrCreateSubCompound("terminal").getCompoundTag("_hw").getKeySet().stream().map(providers::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public boolean hasHardware(String name) {
        return itemStack.getOrCreateSubCompound("terminal").getCompoundTag("_hw").hasKey(name);
    }

    public NBTTagCompound getHardwareNBT(String name) {
        return itemStack.getOrCreateSubCompound("terminal").getCompoundTag("_hw").getCompoundTag(name);
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        HardwareProvider provider = new HardwareProvider();
        provider.providers = new HashMap<>();
        provider.itemStack = itemStack;
        for (Hardware hardware : TerminalRegistry.getAllHardware()) {
            Hardware instance = hardware.createHardware(itemStack);
            if (instance != null) {
                instance.provider = provider;
                provider.providers.put(hardware.getRegistryName(), instance);
            }
        }
        return provider;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (providers != null) {
            for (ICapabilityProvider provider : providers.values()) {
                if (provider.hasCapability(capability, facing)) {
                    return true;
                }
            }
        }
        return capability == GregtechCapabilities.CAPABILITY_HARDWARE_PROVIDER;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (providers != null) {
            for (ICapabilityProvider provider : providers.values()) {
                T cap = provider.getCapability(capability, facing);
                if (cap != null) {
                    return cap;
                }
            }
        }
        return capability == GregtechCapabilities.CAPABILITY_HARDWARE_PROVIDER ? (T) this : null;
    }
}
