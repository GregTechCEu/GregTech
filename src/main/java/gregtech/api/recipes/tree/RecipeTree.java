package gregtech.api.recipes.tree;

import gregtech.api.GTValues;
import gregtech.api.recipes.Recipe;

import gregtech.api.recipes.ingredients.GTRecipeFluidInput;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.recipes.tree.flag.FlagApplicator;
import gregtech.api.recipes.tree.flag.FlagMap;
import gregtech.api.recipes.tree.flag.FluidStackApplicatorMap;
import gregtech.api.recipes.tree.flag.ItemStackApplicatorMap;
import gregtech.api.recipes.tree.flag.SingleFlagApplicator;
import gregtech.api.recipes.tree.property.PropertyFilterMap;

import gregtech.api.recipes.tree.property.PropertySet;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public class RecipeTree {

    private static final Set<RecipeTree> TREES = Collections.newSetFromMap(new WeakHashMap<>());

    private final List<RecipeWrapper> recipes = new ObjectArrayList<>();

    private final PropertyFilterMap filters = new PropertyFilterMap();

    private @Nullable ItemStackApplicatorMap item = null;
    private @Nullable ItemStackApplicatorMap itemDamage = null;
    private @Nullable ItemStackApplicatorMap itemNBT = null;
    private @Nullable ItemStackApplicatorMap itemDamageNBT = null;

    private @Nullable FluidStackApplicatorMap fluid = null;
    private @Nullable FluidStackApplicatorMap fluidNBT = null;

    public RecipeTree() {
        TREES.add(this);
    }

    public void addRecipe(@NotNull Recipe recipe) {
        if (recipes.size() == 65536) throw new IndexOutOfBoundsException("Cannot support more than 65536 recipes per recipe map!");
        RecipeWrapper wrapper = new RecipeWrapper(recipe);
        recipes.add(wrapper);
    }

    public @NotNull RecipeWrapper getRecipeByIndex(int index) {
        return recipes.get(index);
    }

    public @Range(from = 0, to = 65536) int getRecipeCount() {
        return recipes.size();
    }

    public CompactibleIterator<Recipe> findRecipes(List<ItemStack> items, List<FluidStack> fluids, PropertySet properties) {
        BitSet filter = filters.filter(properties);
        if (filter.cardinality() == getRecipeCount()) return CompactibleIterator.empty();
        FlagMap map = new FlagMap(this, filter);
        for (ItemStack stack : items) {
            if (item != null) item.getApplicator(stack).applyFlags(map, stack);
            if (itemDamage != null) itemDamage.getApplicator(stack).applyFlags(map, stack);
            if (itemNBT != null) itemNBT.getApplicator(stack).applyFlags(map, stack);
            if (itemDamageNBT != null) itemDamageNBT.getApplicator(stack).applyFlags(map, stack);
        }
        for (FluidStack stack : fluids) {
            if (fluid != null) fluid.getApplicator(stack).applyFlags(map, stack);
            if (fluidNBT != null) fluidNBT.getApplicator(stack).applyFlags(map, stack);
        }
        return map.matchedIterator();
    }

    private @NotNull ItemStackApplicatorMap getItem() {
        if (item == null) item = ItemStackApplicatorMap.item();
        return item;
    }

    private @NotNull ItemStackApplicatorMap getItemDamage() {
        if (itemDamage == null) itemDamage = ItemStackApplicatorMap.itemDamage();
        return itemDamage;
    }

    private @NotNull ItemStackApplicatorMap getItemNBT() {
        if (itemNBT == null) itemNBT = ItemStackApplicatorMap.itemDamageNBT();
        return itemNBT;
    }

    private @NotNull ItemStackApplicatorMap getItemDamageNBT() {
        if (itemDamageNBT == null) itemDamageNBT = ItemStackApplicatorMap.itemDamageNBT();
        return itemDamageNBT;
    }

    private @NotNull FluidStackApplicatorMap getFluid() {
        if (fluid == null) fluid = FluidStackApplicatorMap.fluid();
        return fluid;
    }

    private @NotNull FluidStackApplicatorMap getFluidNBT() {
        if (fluidNBT == null) fluidNBT = FluidStackApplicatorMap.fluidNBT();
        return fluidNBT;
    }

    public static void rebuildRecipeTrees() {
        for (RecipeTree tree : TREES) {
            tree.rebuild();
        }
    }

    public void rebuild() {
        filters.clear();
        item = null;
        itemDamage = null;
        itemNBT = null;
        itemDamageNBT = null;
        fluid = null;
        fluidNBT = null;
        for (int i = 0; i < recipes.size(); i++) {
            RecipeWrapper recipe = recipes.get(i);
            filters.addFilters(i, recipe.getRecipe().propertyStorage());
            long flags = 0;
            byte flag = 0;
            for (GTRecipeInput input : recipe.getRecipe().getInputs()) {
                if (input instanceof GTRecipeItemInput || input instanceof GTRecipeOreInput) {
                    boolean nbtMap;
                    FlagApplicator<ItemStack> applicator = new SingleFlagApplicator<>(flag);
                    NBTMatcher matcher = input.getNBTMatcher();
                    if (matcher != null) {
                        if (matcher != NBTMatcher.ANY) {
                            NBTCondition condition = input.getNBTMatchingCondition();
                            final byte finalFlag = flag;
                            applicator = (c, e) -> e | (matcher.evaluate(c, condition) ? (1L << finalFlag) : 0);
                        }
                        nbtMap = false;
                    } else nbtMap = true;

                    flags += (1L << flag);
                    flag++;
                    ItemStack[] stacks;
                    if (input instanceof GTRecipeItemInput s) stacks = s.getOriginalStacks();
                    else stacks = input.getInputStacks();
                    for (ItemStack stack : stacks) {
                        ItemStackApplicatorMap map;
                        if (stack.getMetadata() == GTValues.W) {
                            if (nbtMap) map = getItemNBT();
                            else map = getItem();
                        } else {
                            if (nbtMap) map = getItemDamageNBT();
                            else map = getItemDamage();
                        }
                        map.getOrCreate(stack).insertApplicator(i, applicator);
                    }
                }
            }
            for (GTRecipeInput input : recipe.getRecipe().getFluidInputs()) {
                if (input instanceof GTRecipeFluidInput) {
                    boolean nbtMap;
                    FlagApplicator<FluidStack> applicator = new SingleFlagApplicator<>(flag);
                    NBTMatcher matcher = input.getNBTMatcher();
                    if (matcher != null) {
                        if (matcher != NBTMatcher.ANY) {
                            NBTCondition condition = input.getNBTMatchingCondition();
                            final byte finalFlag = flag;
                            applicator = (c, e) -> e | (matcher.evaluate(c, condition) ? (1L << finalFlag) : 0);
                        }
                        nbtMap = false;
                    } else nbtMap = true;

                    flags += (1L << flag);
                    flag++;
                    FluidStackApplicatorMap map = nbtMap ? getFluidNBT() : getFluid();
                    map.getOrCreate(input.getInputFluidStack()).insertApplicator(i, applicator);
                }
            }
            recipe.setFlagsThreshold(flags);
        }
        filters.trim(2);

    }
}
