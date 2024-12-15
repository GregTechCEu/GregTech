package gregtech.api.fission.component;

import gregtech.api.util.ItemStackHashStrategy;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FissionComponentRegistry {

    private final Map<ItemStack, FissionComponentData> itemData = new Object2ObjectOpenCustomHashMap<>(ItemStackHashStrategy.comparingItemDamageCount());
    private final Map<FluidStack, FissionComponentData> fluidData = new Object2ObjectOpenHashMap<>();

    public void add(@NotNull ItemStack stack, @NotNull FissionComponentData data) {
        this.itemData.put(stack, data);
    }

    public void add(@NotNull FluidStack stack, @NotNull FissionComponentData data) {
        this.fluidData.put(stack, data);
    }

    public <T extends FissionComponentData> @Nullable T getData(@NotNull Class<? extends T> clazz, @NotNull ItemStack stack) {
        FissionComponentData o = itemData.get(stack);
        if (o == null) {
            return null;
        }
        return clazz.cast(o);
    }

    public <T extends FissionComponentData> @Nullable T getData(@NotNull Class<? extends T> clazz, @NotNull FluidStack stack) {
        FissionComponentData o = fluidData.get(stack);
        if (o == null) {
            return null;
        }
        return clazz.cast(o);
    }
}
