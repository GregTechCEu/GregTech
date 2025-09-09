package gregtech.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.match.Matcher;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.util.GTLog;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Definition of ItemStacks, Ore dicts, of ingredients for
 * use on RecipeMaps Recipes go here.
 * <p>
 * Forge uses are nor Hashable neither implement equals for these cases,
 * as they use a list of ItemStacks internally.
 * <p>
 * The behavior of the ingredient is determined by the GTRecipeInput used.
 * <p>
 * Each GTRecipeInput is cached by an internal hashtable, and any duplicative
 * instances will be replaced by identical object previously created. This
 * caching strategy is turned off after recipe registration is over.
 */
public abstract class GTRecipeInput implements Matcher<Object> {

    /**
     * Sorting order of standard recipe inputs.
     */
    public static final int SORTING_ORDER_COMMON = 0;
    /**
     * Sorting order of non-consumable recipe inputs.
     */
    public static final int SORTING_ORDER_NC = 5;
    /**
     * Sorting order of non-consumable {@link IntCircuitIngredient}s.
     */
    public static final int SORTING_ORDER_INT_CIRCUIT = 10;

    public static final Comparator<GTRecipeInput> RECIPE_INPUT_COMPARATOR = Comparator
            .comparingInt(GTRecipeInput::getSortingOrder);

    /**
     * All items will initially match the with is NBT (OreDicts have a null tag?)
     * but this behavior can be changed by using a NBTMatcher and an appropriate NBTCondition.
     */

    protected int amount;
    protected boolean isConsumable = true;
    protected NBTMatcher nbtMatcher;
    protected NBTCondition nbtCondition;

    private boolean cached;

    private int hash;
    protected boolean hashCached;

    /**
     * @deprecated Calling this function is unnecessary. Use the ingredient directly.
     */
    @Deprecated
    public static GTRecipeInput getOrCreate(GTRecipeInput gtRecipeIngredient) {
        return gtRecipeIngredient;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached() {
        this.cached = true;
    }

    protected abstract GTRecipeInput copy();

    /**
     * Returns a copy of the ingredient with the given amount.
     * Used by the parallel logic to multiply the amount of the ingredients.
     * If you're not using the parallel logic, you can ignore this.
     *
     * @return returns a copy of the GTRecipeInput with the given amount.
     */
    public abstract GTRecipeInput copyWithAmount(int amount);

    /**
     * Returns either this instance with {@link GTRecipeInput#amount} field modified (for non-cached recipe inputs)
     * or new copy with given amount (for cached recipe inputs).
     */
    public GTRecipeInput withAmount(int amount) {
        if (getAmount() == amount) {
            return this;
        } else if (isCached()) {
            return copyWithAmount(amount);
        } else {
            this.amount = amount;
            this.hashCached = false;
            return this;
        }
    }

    public GTRecipeInput setNonConsumable() {
        if (!isConsumable) return this;
        GTRecipeInput recipeInput = cached ? copy() : this;
        recipeInput.isConsumable = false;
        recipeInput.hashCached = false;
        return recipeInput;
    }

    public GTRecipeInput setNBTMatchingCondition(NBTMatcher nbtMatcher, NBTCondition nbtCondition) {
        GTRecipeInput recipeInput = cached ? copy() : this;
        recipeInput.nbtMatcher = nbtMatcher;
        recipeInput.nbtCondition = nbtCondition;
        recipeInput.hashCached = false;
        return recipeInput;
    }

    public boolean hasNBTMatchingCondition() {
        return nbtMatcher != null;
    }

    public NBTMatcher getNBTMatcher() {
        return nbtMatcher;
    }

    public NBTCondition getNBTMatchingCondition() {
        return nbtCondition;
    }

    public boolean isNonConsumable() {
        return !isConsumable;
    }

    public ItemStack[] getInputStacks() {
        return null;
    }

    public FluidStack getInputFluidStack() {
        return null;
    }

    public boolean isOreDict() {
        return false;
    }

    public int getOreDict() {
        return -1;
    }

    public boolean acceptsStack(@Nullable ItemStack input) {
        return false;
    }

    public boolean acceptsFluid(@Nullable FluidStack input) {
        return false;
    }

    @Override
    public boolean matches(Object o) {
        if (o instanceof ItemStack s) {
            return acceptsStack(s);
        } else if (o instanceof FluidStack s) {
            return acceptsFluid(s);
        }
        return false;
    }

    @Override
    public @Range(from = 1, to = Long.MAX_VALUE) long getRequiredCount() {
        return getAmount();
    }

    @Override
    public int hashCode() {
        if (!this.hashCached) {
            this.hash = computeHash();
            this.hashCached = true;
        }
        return this.hash;
    }

    protected abstract int computeHash();

    @Override
    public abstract boolean equals(Object obj);

    /**
     * @return true if the input matches another input, while ignoring its amount field and
     *         non-consumable status.
     *         <p>
     *         used for unique input matching in RecipeMap
     * @see gregtech.api.recipes.RecipeMap#uniqueIngredientsList(Collection) RecipeMap#uniqueIngredientsList(Collection)
     */
    public abstract boolean equalIgnoreAmount(GTRecipeInput input);

    /**
     * Get sorting order of this recipe input instance. Recipe inputs are sorted with ascending order.
     *
     * @return sorting order of this recipe input instance
     * @see #SORTING_ORDER_COMMON
     * @see #SORTING_ORDER_NC
     * @see #SORTING_ORDER_INT_CIRCUIT
     */
    public int getSortingOrder() {
        return this.isNonConsumable() ? SORTING_ORDER_NC : SORTING_ORDER_COMMON;
    }

    public static NBTTagCompound writeToNBT(GTRecipeInput input) {
        NBTTagCompound tag = new NBTTagCompound();
        if (input instanceof GTRecipeItemInput) {
            NBTTagList stackList = new NBTTagList();
            for (ItemStack stack : input.getInputStacks()) {
                stackList.appendTag(stack.serializeNBT());
            }
            tag.setTag("stacks", stackList);
        } else if (input instanceof GTRecipeOreInput) {
            tag.setInteger("ore", input.getOreDict());
        } else if (input instanceof GTRecipeFluidInput) {
            tag.setTag("fluid", input.getInputFluidStack().writeToNBT(new NBTTagCompound()));
        }
        tag.setInteger("amount", input.getAmount());
        return tag;
    }

    public static GTRecipeInput readFromNBT(NBTTagCompound tag) {
        int amount = tag.getInteger("amount");
        if (tag.hasKey("stacks")) {
            NBTTagList list = tag.getTagList("stacks", Constants.NBT.TAG_COMPOUND);
            ItemStack[] stacks = new ItemStack[list.tagCount()];
            Arrays.setAll(stacks, i -> new ItemStack(list.getCompoundTagAt(i)));
            return new GTRecipeItemInput(stacks, amount);

        } else if (tag.hasKey("ore")) {
            return new GTRecipeOreInput(tag.getInteger("ore"), amount);
        } else if (tag.hasKey("fluid")) {
            FluidStack stack = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("fluid"));
            return new GTRecipeFluidInput(Objects.requireNonNull(stack), amount);
        }
        GTLog.logger.warn("unable to read tag!: " + tag);
        return null;
    }

    protected static class ItemToMetaList implements Object2ObjectMap.Entry<Item, List<MetaToTAGList>> {

        protected Item item;
        protected List<MetaToTAGList> metaToTAGList;

        public ItemToMetaList(ItemStack stack) {
            this.item = stack.getItem();
            this.metaToTAGList = ObjectLists.singleton(new MetaToTAGList(stack));
        }

        void addStackToLists(ItemStack stack) {
            if (this.metaToTAGList instanceof ObjectLists.Singleton) {
                this.metaToTAGList = new ObjectArrayList<>(this.metaToTAGList);
            }
            this.metaToTAGList.add(new MetaToTAGList(stack));
        }

        @Override
        public Item getKey() {
            return item;
        }

        @Override
        public List<MetaToTAGList> getValue() {
            return metaToTAGList;
        }

        @Override
        public List<MetaToTAGList> setValue(List<MetaToTAGList> value) {
            return metaToTAGList = value;
        }
    }

    protected static class MetaToTAGList implements Int2ObjectMap.Entry<List<TagToStack>> {

        protected int meta;
        protected List<TagToStack> tagToStack;

        public MetaToTAGList(ItemStack stack) {
            this.meta = stack.getMetadata();
            this.tagToStack = ObjectLists.singleton(new TagToStack(stack));
        }

        void addStackToList(ItemStack stack) {
            if (this.tagToStack instanceof ObjectLists.Singleton) {
                this.tagToStack = new ObjectArrayList<>(this.tagToStack);
            }
            this.tagToStack.add(new TagToStack(stack.getTagCompound(), stack));
        }

        @Override
        public Integer getKey() {
            return meta;
        }

        @Override
        public int getIntKey() {
            return meta;
        }

        @Override
        public List<TagToStack> getValue() {
            return tagToStack;
        }

        @Override
        public List<TagToStack> setValue(List<TagToStack> value) {
            return tagToStack = value;
        }
    }

    protected static class TagToStack implements Object2ObjectMap.Entry<NBTTagCompound, ItemStack> {

        NBTTagCompound tag;
        ItemStack stack;

        TagToStack(NBTTagCompound tag, ItemStack stack) {
            this.tag = tag;
            this.stack = stack;
        }

        TagToStack(ItemStack stack) {
            this.tag = stack.getTagCompound();
            this.stack = stack;
        }

        @Override
        public NBTTagCompound getKey() {
            return tag;
        }

        @Override
        public ItemStack getValue() {
            return stack;
        }

        @Override
        public ItemStack setValue(ItemStack value) {
            return stack = value;
        }
    }
}
