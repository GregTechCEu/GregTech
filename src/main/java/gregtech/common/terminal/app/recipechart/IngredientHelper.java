package gregtech.common.terminal.app.recipechart;

import gregtech.api.gui.Widget;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public interface IngredientHelper<T> {

    static <T> IngredientHelper<T> getFor(Object o) {
        Objects.requireNonNull(o);
        if (o.getClass() == ItemStack.class) {
            return (IngredientHelper<T>) ItemStackHelper.INSTANCE;
        }
        if (o.getClass() == FluidStack.class) {
            return (IngredientHelper<T>) FluidStackHelper.INSTANCE;
        }
        throw new IllegalArgumentException();
    }

    static IngredientHelper<?> getForTypeId(int type) {
        return switch (type) {
            case 1 -> ItemStackHelper.INSTANCE;
            case 2 -> FluidStackHelper.INSTANCE;
            default -> throw new IllegalArgumentException();
        };
    }

    byte getTypeId();

    int getAmount(T t);

    void setAmount(T t, int amount);

    boolean areEqual(T t1, T t2);

    boolean isEmpty(T t);

    String getDisplayName(T t);

    Widget createWidget(T t);

    T deserialize(NBTTagCompound nbt);

    NBTTagCompound serialize(T t);
}
