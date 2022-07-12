package gregtech.api.recipes.ingredients;

import gregtech.api.recipes.ingredients.NBTMatching.NBTCondition;
import gregtech.api.recipes.ingredients.NBTMatching.NBTMatcher;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Definition of ItemStacks, Ore dicts, of ingredients for
 * use on RecipeMaps Recipes go here.
 * <p>
 * Forge uses are nor Hashable neither implement equals for these cases,
 * as they use a list of ItemStacks internally.
 * <p>
 * The behavior of the ingredient is determined by the GTingredient used.
 */
public abstract class GTRecipeInput {
    public static final WeakHashMap<GTRecipeInput, WeakReference<GTRecipeInput>> INSTANCES = new WeakHashMap<>();
    /**
     * All items will initially match the with is NBT (OreDicts have a null tag?)
     * but this behavior can be changed by using a NBTMatcher and an appropriate NBTCondition.
     */

    protected int amount;
    protected boolean isConsumable = true;
    protected NBTMatcher nbtMatcher;
    protected NBTCondition nbtCondition;

    static GTRecipeInput getFromCache(GTRecipeInput realIngredient) {
        if (INSTANCES.get(realIngredient) == null) {
            INSTANCES.put(realIngredient, new WeakReference<>(realIngredient));
        } else {
            realIngredient = INSTANCES.get(realIngredient).get();
        }
        return realIngredient;
    }

    public static GTRecipeInput getOrCreate(GTRecipeInput gtRecipeIngredient) {
        return getFromCache(gtRecipeIngredient);
    }

    public int getAmount() {
        return amount;
    }

    abstract GTRecipeInput copy();

    public GTRecipeInput setNonConsumable() {
        GTRecipeInput copy = copy();
        copy.isConsumable = false;
        return getFromCache(copy);
    }

    public GTRecipeInput setNBTMatchingCondition(NBTMatcher nbtMatcher, NBTCondition nbtCondition) {
        GTRecipeInput copy = copy();
        copy.nbtMatcher = nbtMatcher;
        copy.nbtCondition = nbtCondition;
        return getFromCache(copy);
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

    /**
     * @return true if the input matches another input, while ignoring its amount field and
     * non-consumable status.
     * <p>
     * used for unique input matching in RecipeMap
     * @see gregtech.api.recipes.RecipeMap#uniqueIngredientsList(List) (GTRecipeInput)
     */
    public abstract boolean equalIgnoreAmount(GTRecipeInput input);

    protected static class ItemToMetaList implements Map.Entry<Item, List<MetaToTAGList>> {
        protected Item item;
        protected List<MetaToTAGList> metaToTAGList;

        public ItemToMetaList(ItemStack stack) {
            this.item = stack.getItem();
            this.metaToTAGList = new ObjectArrayList<>();
            this.metaToTAGList.add(new MetaToTAGList(stack));
        }

        public Item getItem() {
            return item;
        }

        public List<MetaToTAGList> getMetaToTAGList() {
            return metaToTAGList;
        }

        void addStackToLists(ItemStack stack) {
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

    protected static class MetaToTAGList implements Map.Entry<Integer, List<TagToStack>> {
        protected int meta;
        protected List<TagToStack> tagToStack;

        public MetaToTAGList(ItemStack stack) {
            this.meta = stack.getMetadata();
            this.tagToStack = new ObjectArrayList<>(1);
            this.tagToStack.add(new TagToStack(stack));
        }

        public int getMeta() {
            return meta;
        }

        public List<TagToStack> getTagToStack() {
            return tagToStack;
        }

        void addStackToList(ItemStack stack) {
            this.tagToStack.add(new TagToStack(stack.getTagCompound(), stack));
        }

        @Override
        public Integer getKey() {
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

    protected static class TagToStack implements Map.Entry<NBTTagCompound, ItemStack> {
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

        public ItemStack getStack() {
            return stack;
        }

        public NBTTagCompound getTag() {
            return tag;
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
