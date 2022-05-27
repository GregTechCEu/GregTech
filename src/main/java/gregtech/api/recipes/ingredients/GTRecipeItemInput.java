package gregtech.api.recipes.ingredients;

import gregtech.api.GTValues;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Objects;

public class GTRecipeItemInput extends GTRecipeInput {
    ItemStack[] inputStacks;
    //TODO: figure out how to make this or something like it more efficient in memory. currently
    //eating 10MB on nomi.
    Object2ObjectOpenHashMap<Item, Int2ObjectOpenHashMap<Object2ObjectOpenHashMap<NBTTagCompound, ItemStack>>> itemMap;

    protected GTRecipeItemInput(ItemStack stack, int amount) {
        this(new ItemStack[]{stack}, amount);
    }

    protected GTRecipeItemInput(ItemStack[] stack, int amount) {
        this.amount = amount;

        NonNullList<ItemStack> lst = NonNullList.create();
        for (ItemStack is : stack) {
            if (is.getMetadata() == GTValues.W ) {
                is.getItem().getSubItems(net.minecraft.creativetab.CreativeTabs.SEARCH, lst);
            } else{
                lst.add(is);
            }
        }

        for (ItemStack is : lst) {
            if (!is.isEmpty()) {
                if (itemMap == null) {
                    itemMap = new Object2ObjectOpenHashMap<>(2,1F);
                }
                itemMap.compute(is.getItem(), (k, v) -> {
                    if (v == null) {
                        v = new Int2ObjectOpenHashMap<>(2,1F);
                    }
                    v.compute(is.getMetadata(), (k2, v2) -> {
                        if (v2 == null) {
                            v2 = new Object2ObjectOpenHashMap<>(2,1F);
                        }
                        v2.put(is.getTagCompound(), is);
                        return v2;
                    });
                    return v;
                });
            }
        }
        this.inputStacks = lst.stream().peek(is -> is.setCount(this.amount)).toArray(ItemStack[]::new);
    }

    protected GTRecipeItemInput(ItemStack... stack) {
        this(stack, stack[0].getCount());
    }

    public static GTRecipeItemInput getOrCreate(ItemStack stack, int amount) {
        return getFromCache(new GTRecipeItemInput(stack, amount));
    }

    private static GTRecipeItemInput getFromCache(GTRecipeItemInput realIngredient) {
        if (INSTANCES.get(realIngredient) == null) {
            INSTANCES.put(realIngredient, new WeakReference<>(realIngredient));
        } else {
            realIngredient = (GTRecipeItemInput) INSTANCES.get(realIngredient).get();
        }
        return realIngredient;
    }

    public static GTRecipeInput getOrCreate(GTRecipeInput ri, int i) {
        return getFromCache(new GTRecipeItemInput(ri.getInputStacks(), i));
    }

    public static GTRecipeInput getOrCreate(GTRecipeInput ri) {
        return getFromCache(new GTRecipeItemInput(ri.getInputStacks()));
    }

    public static GTRecipeInput getOrCreate(ItemStack ri) {
        return getFromCache(new GTRecipeItemInput(ri));
    }

    protected GTRecipeItemInput copy() {
        GTRecipeItemInput copy = new GTRecipeItemInput(this.inputStacks, this.amount);
        copy.isConsumable = this.isConsumable;
        copy.nbtMatcher = this.nbtMatcher;
        copy.nbtCondition = this.nbtCondition;
        return copy;
    }

    @Override
    GTRecipeInput getFromCache(GTRecipeInput recipeInput) {
        return getFromCache((GTRecipeItemInput) recipeInput);
    }

    @Override
    public ItemStack[] getInputStacks() {
        return this.inputStacks;
    }

    @Override
    public FluidStack getInputFluidStack() {
        return null;
    }

    @Override
    public boolean isOreDict() {
        return false;
    }

    @Override
    public boolean acceptsStack(ItemStack input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        Int2ObjectOpenHashMap<Object2ObjectOpenHashMap<NBTTagCompound, ItemStack>> map = itemMap.get(input.getItem());
        if (map != null) {
            Object2ObjectOpenHashMap<NBTTagCompound, ItemStack> map2 = map.get(input.getMetadata());
            if (map2 != null) {
                if (nbtMatcher == null) {
                    ItemStack returned = map2.get(input.getTagCompound());
                    if (returned != null) {
                        return returned.areCapsCompatible(input);
                    }
                }
                return nbtMatcher.evaluate(input, nbtCondition);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (nbtMatcher == null) {
            return Objects.hash(Arrays.stream(inputStacks).map(ItemStack::getItem),
                    Arrays.stream(inputStacks).map(ItemStack::getMetadata),
                    this.amount, this.nbtMatcher, this.nbtCondition, isConsumable,
                    Arrays.stream(inputStacks).map(ItemStack::getTagCompound));
        }
        return Objects.hash(Arrays.stream(inputStacks).map(ItemStack::getItem),
                Arrays.stream(inputStacks).map(ItemStack::getMetadata),
                this.amount, this.nbtMatcher, this.nbtCondition, isConsumable,
                0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GTRecipeItemInput)) return false;
        GTRecipeItemInput other = (GTRecipeItemInput) obj;

        if (this.amount != other.amount) return false;
        if (this.isConsumable != other.isConsumable) return false;
        if (this.nbtMatcher != null && !this.nbtMatcher.equals(other.nbtMatcher)) return false;
        if (this.nbtCondition != null && !this.nbtCondition.equals(other.nbtCondition)) return false;

        if (this.inputStacks.length != other.inputStacks.length) return false;
        for (int i = 0; i < this.inputStacks.length; i++) {
            if (!ItemStack.areItemStacksEqual(this.inputStacks[i], other.inputStacks[i])) return false;
        }
        return true;
    }

    @Override
    public boolean equalIgnoreAmount(GTRecipeInput input) {
        if (this == input) return true;
        if (!(input instanceof GTRecipeItemInput)) return false;
        GTRecipeItemInput other = (GTRecipeItemInput) input;

        if (this.nbtMatcher != null && !this.nbtMatcher.equals(other.nbtMatcher)) return false;
        if (this.nbtCondition != null && !this.nbtCondition.equals(other.nbtCondition)) return false;

        if (this.inputStacks.length != other.inputStacks.length) return false;
        for (int i = 0; i < this.inputStacks.length; i++) {
            if (!ItemStack.areItemStacksEqual(this.inputStacks[i], other.inputStacks[i])) return false;
        }
        return true;    }
}
