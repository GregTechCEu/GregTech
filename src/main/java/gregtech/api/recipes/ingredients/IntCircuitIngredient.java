package gregtech.api.recipes.ingredients;

import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.common.items.MetaItems;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class IntCircuitIngredient extends GTRecipeInput {

    public static final int CIRCUIT_MIN = 0;
    public static final int CIRCUIT_MAX = 32;

    private static final IntCircuitIngredient[] INGREDIENTS = new IntCircuitIngredient[CIRCUIT_MAX + 1];

    public static IntCircuitIngredient circuitInput(int meta) {
        if (meta < CIRCUIT_MIN || meta > CIRCUIT_MAX) {
            throw new IndexOutOfBoundsException("Circuit meta " + meta + " is out of range");
        }
        IntCircuitIngredient ingredient = INGREDIENTS[meta];
        if (ingredient == null) {
            INGREDIENTS[meta] = ingredient = new IntCircuitIngredient(meta);
            ingredient.isConsumable = false;
            ingredient.setCached();
        }
        return ingredient;
    }

    /**
     * @deprecated Calling this function is unnecessary. Use the ingredient directly.
     */
    @Deprecated
    public static IntCircuitIngredient getOrCreate(IntCircuitIngredient ri) {
        return ri;
    }

    private final int matchingConfigurations;
    private ItemStack[] inputStacks;

    public IntCircuitIngredient(int matchingConfigurations) {
        this.amount = 1;
        this.matchingConfigurations = matchingConfigurations;
    }

    @Override
    protected IntCircuitIngredient copy() {
        IntCircuitIngredient copy = new IntCircuitIngredient(this.matchingConfigurations);
        copy.isConsumable = this.isConsumable;
        return copy;
    }

    @Override
    public GTRecipeInput copyWithAmount(int amount) {
        return copy(); // Amount of IntCircuitIngredient is always 1
    }

    @Override
    public GTRecipeInput setNBTMatchingCondition(NBTMatcher nbtMatcher, NBTCondition nbtCondition) {
        return this; // IntCircuitIngredient ignores nbt conditions
    }

    @Override
    public int getAmount() {
        return 1;
    }

    @Override
    public ItemStack[] getInputStacks() {
        if (this.inputStacks == null) {
            this.inputStacks = new ItemStack[] { getIntegratedCircuit(this.matchingConfigurations) };
        }
        return this.inputStacks;
    }

    @Override
    public boolean acceptsStack(@Nullable ItemStack itemStack) {
        return itemStack != null && MetaItems.INTEGRATED_CIRCUIT.isItemEqual(itemStack) &&
                matchingConfigurations == getCircuitConfiguration(itemStack);
    }

    @Override
    protected int computeHash() {
        return Objects.hash(matchingConfigurations, isConsumable);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof IntCircuitIngredient)) return false;
        IntCircuitIngredient other = (IntCircuitIngredient) obj;
        return this.isConsumable == other.isConsumable && this.matchingConfigurations == other.matchingConfigurations;
    }

    @Override
    public boolean equalIgnoreAmount(GTRecipeInput input) {
        if (this == input) return true;
        if (!(input instanceof IntCircuitIngredient)) return false;
        IntCircuitIngredient other = (IntCircuitIngredient) input;
        return this.matchingConfigurations == other.matchingConfigurations;
    }

    @Override
    public int getSortingOrder() {
        return this.isNonConsumable() ? SORTING_ORDER_INT_CIRCUIT : super.getSortingOrder();
    }

    @Override
    public String toString() {
        return "1xcircuit(" + matchingConfigurations + ")";
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
        if (tagCompound != null) {
            return tagCompound.getInteger("Configuration");
        }
        return 0;
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
}
