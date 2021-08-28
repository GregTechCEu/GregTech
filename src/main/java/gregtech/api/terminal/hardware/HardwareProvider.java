package gregtech.api.terminal.hardware;

import gregtech.api.capability.impl.CombinedCapabilityProvider;
import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;
import gregtech.api.terminal.TerminalRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/28
 * @Description:
 */
public class HardwareProvider implements IItemCapabilityProvider {

    @Override
    public ICapabilityProvider createProvider(ItemStack itemStack) {
        ArrayList<ICapabilityProvider> providers = new ArrayList<>();
        for (IHardware hardware : TerminalRegistry.getAllHardware()) {
            IHardware instance = hardware.createHardware(itemStack);
            if (instance != null) {
                providers.add(instance);
            }
        }
        return new CombinedCapabilityProvider(providers);
    }
}
