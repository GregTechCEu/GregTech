package gregtech.common.items.armor;

import gregtech.api.capability.impl.GTFluidHandlerItemStack;
import gregtech.api.items.armoritem.jetpack.IJetpackStats;
import gregtech.api.items.armoritem.jetpack.JetpackBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FueledJetpackBehavior extends JetpackBehavior {

    //private final int maxCapacity;

    public FueledJetpackBehavior(IJetpackStats jetpackStats) {

        super(jetpackStats);
    }

    @Override
    protected int getFuelPerUse() {

        return 0;
    }

    @Override
    protected boolean hasEnoughFuel(@NotNull ItemStack stack, int amount) {

        return false;
    }

    @Override
    protected void drainFuel(@NotNull ItemStack stack, int amount) {

    }

    @Override
    protected boolean hasFuel(@NotNull ItemStack stack) {

        return false;
    }

    @Override
    public ICapabilityProvider createProvider(@NotNull ItemStack stack, @Nullable NBTTagCompound tag) {
        //return new GTFluidHandlerItemStack(stack, maxCapacity)
        //        .setFilter()
        //        .setCanDrain(false);
        return null;
    }
}
