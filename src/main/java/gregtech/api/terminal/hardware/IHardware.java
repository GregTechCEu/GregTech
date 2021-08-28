package gregtech.api.terminal.hardware;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/27
 * @Description:
 */
public interface IHardware extends ICapabilityProvider {
    String getRegistryName();
    default String getUnlocalizedName(){
        return "terminal.hw." + getRegistryName();
    }
    default boolean isHardwareAdequate(IHardware demand) {
        return this.getClass() == demand.getClass() || this.getRegistryName().equals(demand.getRegistryName());
    }

    IHardware createHardware(ItemStack itemStack);

    @Override
    default boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return false;
    }

    @Nullable
    @Override
    default <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return null;
    }
}
