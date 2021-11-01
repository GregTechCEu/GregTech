package gregtech.api.terminal.hardware;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.common.items.behaviors.TerminalBehaviour;
import net.minecraft.item.Item;
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
    private Map<String, ItemStack> itemCache;
    private Boolean isCreative;
    private ItemStack itemStack;
    private NBTTagCompound tag;


    public HardwareProvider() {

    }

    public void cleanCache(String name) {
        itemCache.remove(name);
    }

    public boolean isCreative() {
        if (isCreative == null) {
            isCreative = TerminalBehaviour.isCreative(getItemStack());
        }
        return isCreative;
    }

    public Map<String, Hardware> getProviders() {
        return providers;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public NBTTagCompound getOrCreateHardwareCompound() {
        if (tag == null) {
            NBTTagCompound terminal = itemStack.getOrCreateSubCompound("terminal");
            if (!terminal.hasKey("_hw")) {
                terminal.setTag("_hw", new NBTTagCompound());
            }
            tag = terminal.getCompoundTag("_hw");
        }
        return tag;
    }

    public List<Hardware> getHardware() {
        if (TerminalBehaviour.isCreative(itemStack)) {
            return new ArrayList<>(providers.values());
        }
        return getOrCreateHardwareCompound().getKeySet().stream().map(providers::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public boolean hasHardware(String name) {
        return itemStack != null && (TerminalBehaviour.isCreative(getItemStack()) || getOrCreateHardwareCompound().hasKey(name));
    }

    public NBTTagCompound getHardwareNBT(String name) {
        return getOrCreateHardwareCompound().getCompoundTag(name);
    }

    public ItemStack getHardwareItem(String name) {
        if (!itemCache.containsKey(name)) {
            NBTTagCompound tag = getHardwareNBT(name);
            if (tag.hasKey("item")) {
                itemCache.put(name, new ItemStack(tag.getCompoundTag("item")));
            } else {
                itemCache.put(name, ItemStack.EMPTY);
            }
        }
        return itemCache.get(name);
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        HardwareProvider provider = new HardwareProvider();
        provider.providers = new LinkedHashMap<>();
        provider.itemCache = new HashMap<>();
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
            for (Map.Entry<String, Hardware> entry : providers.entrySet()) {
                Hardware provider = entry.getValue();
                if (provider instanceof IHardwareCapability &&
                        hasHardware(entry.getKey()) &&
                        ((IHardwareCapability) provider).hasCapability(capability)) {
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
            for (Map.Entry<String, Hardware> entry : providers.entrySet()) {
                Hardware provider = entry.getValue();
                if (provider instanceof IHardwareCapability &&
                        hasHardware(entry.getKey()) &&
                        ((IHardwareCapability) provider).hasCapability(capability)) {
                    return ((IHardwareCapability) provider).getCapability(capability);
                }
            }
        }
        return capability == GregtechCapabilities.CAPABILITY_HARDWARE_PROVIDER ? GregtechCapabilities.CAPABILITY_HARDWARE_PROVIDER.cast(this) : null;
    }
}
