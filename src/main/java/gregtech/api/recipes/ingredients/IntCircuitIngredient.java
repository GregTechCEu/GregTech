package gregtech.api.recipes.ingredients;

import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.common.items.MetaItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

public class IntCircuitIngredient extends GTRecipeItemInput {

    public static final int CIRCUIT_MAX = 32;
    private final int matchingConfigurations;

    public static IntCircuitIngredient getOrCreate(IntCircuitIngredient ri) {
        return (IntCircuitIngredient) getFromCache(new IntCircuitIngredient(ri.matchingConfigurations));
    }

    @Override
    protected IntCircuitIngredient copy() {
        IntCircuitIngredient copy = new IntCircuitIngredient(this.matchingConfigurations);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    public IntCircuitIngredient(int matchingConfigurations) {
        super(getIntegratedCircuit(matchingConfigurations));
        this.matchingConfigurations = matchingConfigurations;
    }

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
        if (!isIntegratedCircuit(itemStack)) return 0;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        return tagCompound.getInteger("Configuration");
    }

    public static boolean isIntegratedCircuit(ItemStack itemStack) {
        boolean isCircuit = MetaItems.INTEGRATED_CIRCUIT.isItemEqual(itemStack);
        if (isCircuit && !itemStack.hasTagCompound()) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("Configuration", 0);
            itemStack.setTagCompound(compound);
        }
        return isCircuit;
    }

    public static void adjustConfiguration(PlayerInventoryHolder holder, int amount) {
        adjustConfiguration(holder.getCurrentItem(), amount);
        holder.markAsDirty();
    }

    public static void adjustConfiguration(ItemStack stack, int amount) {
        if (!IntCircuitIngredient.isIntegratedCircuit(stack)) return;
        int configuration = IntCircuitIngredient.getCircuitConfiguration(stack);
        configuration += amount;
        configuration = MathHelper.clamp(configuration, 0, IntCircuitIngredient.CIRCUIT_MAX);
        IntCircuitIngredient.setCircuitConfiguration(stack, configuration);
    }

    @Override
    public boolean acceptsStack(@Nullable ItemStack itemStack) {
        return itemStack != null && MetaItems.INTEGRATED_CIRCUIT.isItemEqual(itemStack) &&
                matchingConfigurations == getCircuitConfiguration(itemStack);
    }

}
