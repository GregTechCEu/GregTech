package gregtech.api.fission.component.impl.data;

import gregtech.api.fission.component.FissionComponentData;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class FuelData implements FissionComponentData {

    public float emission;
    public float heatPerFission;
    public int durability;
    public @NotNull ItemStack result = ItemStack.EMPTY;
}
