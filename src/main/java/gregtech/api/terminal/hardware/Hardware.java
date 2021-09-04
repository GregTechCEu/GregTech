package gregtech.api.terminal.hardware;

import gregtech.common.items.behaviors.TerminalBehaviour;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/27
 * @Description:
 */
public abstract class Hardware implements ICapabilityProvider {
    protected HardwareProvider provider;

    public abstract String getRegistryName();

    public final String getUnlocalizedName() {
        return "terminal.hw." + getRegistryName();
    }

    public boolean isHardwareAdequate(Hardware demand) {
        return this.getClass() == demand.getClass() || this.getRegistryName().equals(demand.getRegistryName());
    }

    public final boolean hasHW() {
        return provider.hasHardware(getRegistryName());
    }

    public final NBTTagCompound getNBT() {
        return provider.getHardwareNBT(getRegistryName());
    }

    public final boolean isCreative(){
        return TerminalBehaviour.isCreative(provider.getItemStack());
    }

    @SideOnly(Side.CLIENT)
    public String addInformation() {
        return null;
    }

    protected abstract Hardware createHardware(ItemStack itemStack);

    @Override
    public final boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (!isCreative() && !hasHW()) {
            return false;
        }
        return hasCapability(capability);
    }

    protected boolean hasCapability(@Nonnull Capability<?> capability) {
        return false;
    }

    @Nullable
    @Override
    public final <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        T result = getCapability(capability);
        if (result == null || !isCreative() && !hasHW()) {
            return null;
        }
        return result;
    }

    protected <T> T getCapability(@Nonnull Capability<T> capability) {
        return null;
    }
}
