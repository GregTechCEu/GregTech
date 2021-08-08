package gregtech.api.recipes.ingredients;

import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;

public class IntCircuitIngredient extends Ingredient {

    public static final int CIRCUIT_MAX = 32;

    public static ItemStack getIntegratedCircuit(int configuration) {
        ItemStack stack = MetaItems.INTEGRATED_CIRCUIT.getStackForm();
        setCircuitConfiguration(stack, configuration);
        return stack;
    }

    public static void setCircuitConfiguration(ItemStack itemStack, int configuration) {
        if (!MetaItems.INTEGRATED_CIRCUIT.isItemEqual(itemStack))
            throw new IllegalArgumentException("Given item stack is not an integrated circuit!");
        if (configuration < 0 || configuration > CIRCUIT_MAX)
            throw new IllegalArgumentException("Given configuration number is out of range!");
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            itemStack.setTagCompound(tagCompound);
        }
        tagCompound.setInteger("Configuration", configuration);
    }

    public static int getCircuitConfiguration(ItemStack itemStack) {
        if (!MetaItems.INTEGRATED_CIRCUIT.isItemEqual(itemStack) ||
                !itemStack.hasTagCompound()) return 0;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        return tagCompound.getInteger("Configuration");
    }

    private static ItemStack[] gatherMatchingCircuits(int... matchingConfigurations) {
        ItemStack[] resultItems = new ItemStack[matchingConfigurations.length];
        for (int i = 0; i < resultItems.length; i++) {
            resultItems[i] = getIntegratedCircuit(matchingConfigurations[i]);
        }
        return resultItems;
    }

    private final int[] matchingConfigurations;

    public IntCircuitIngredient(int... matchingConfigurations) {
        super(gatherMatchingCircuits(matchingConfigurations));
        this.matchingConfigurations = matchingConfigurations;
    }

    @Override
    public boolean apply(@Nullable ItemStack itemStack) {
        return itemStack != null && MetaItems.INTEGRATED_CIRCUIT.isItemEqual(itemStack) &&
                ArrayUtils.contains(matchingConfigurations, getCircuitConfiguration(itemStack));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

}
