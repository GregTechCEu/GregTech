package gregtech.integration.baubles;

import gregtech.api.items.metaitem.stats.IItemCapabilityProvider;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaubleBehavior implements IItemCapabilityProvider, ICapabilityProvider, IBauble {

    private final BaubleType baubleType;

    public BaubleBehavior(BaubleType baubleType) {
        this.baubleType = baubleType;
    }

    @Override
    public BaubleType getBaubleType(ItemStack stack) {
        return baubleType;
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase player) {
        if (stack != null && stack != ItemStack.EMPTY && player != null) {
            stack.getItem().onUpdate(stack, player.getEntityWorld(), player, 0, false);
        }
    }

    @Override
    public ICapabilityProvider createProvider(ItemStack stack) {
        return this;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
    }

    @Override
    public <T> @Nullable T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE) {
            return BaublesCapabilities.CAPABILITY_ITEM_BAUBLE.cast(this);
        }
        return null;
    }
}
