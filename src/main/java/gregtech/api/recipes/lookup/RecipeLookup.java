package gregtech.api.recipes.lookup;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.ingredients.GTFluidIngredient;
import gregtech.api.recipes.ingredients.GTItemIngredient;
import gregtech.api.recipes.lookup.flag.FlagApplicator;
import gregtech.api.recipes.lookup.flag.FlagMap;
import gregtech.api.recipes.lookup.flag.FluidStackApplicatorMap;
import gregtech.api.recipes.lookup.flag.FluidStackMatchingContext;
import gregtech.api.recipes.lookup.flag.ItemStackApplicatorMap;
import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;
import gregtech.api.recipes.lookup.flag.SingleFlagApplicator;
import gregtech.api.recipes.lookup.property.PropertyFilterMap;
import gregtech.api.recipes.lookup.property.PropertySet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class RecipeLookup extends IndexedRecipeLookup {

    protected final ObjectArrayList<Recipe> recipes = new ObjectArrayList<>();

    private final PropertyFilterMap filters = new PropertyFilterMap();

    private @Nullable ItemStackApplicatorMap item = null;
    private @Nullable ItemStackApplicatorMap itemDamage = null;
    private @Nullable ItemStackApplicatorMap itemNBT = null;
    private @Nullable ItemStackApplicatorMap itemDamageNBT = null;

    private @Nullable FluidStackApplicatorMap fluid = null;
    private @Nullable FluidStackApplicatorMap fluidNBT = null;

    private boolean valid = false;

    @Override
    public boolean addRecipe(@NotNull Recipe recipe) {
        if (recipes.size() == 65536) return false;
        recipes.add(recipe);
        return true;
    }

    @Override
    public boolean removeRecipe(@NotNull Recipe recipe) {
        return recipes.remove(recipe);
    }

    @Override
    public @NotNull @UnmodifiableView List<Recipe> getAllRecipes() {
        return recipes;
    }

    @Override
    public void clear() {
        invalidate();
        recipes.clear();
    }

    @Override
    @NotNull
    public CompactibleIterator<Recipe> findRecipes(@NotNull List<ItemStack> items, @NotNull List<FluidStack> fluids,
                                                   @Nullable PropertySet properties) {
        if (!valid) {
            if (recipes.isEmpty()) return CompactibleIterator.empty();
            rebuild();
        }
        BitSet filter = properties == null ? new BitSet() : filters.filter(properties);
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

    protected @NotNull ItemStackApplicatorMap getItem() {
        if (item == null) item = ItemStackApplicatorMap.item();
        return item;
    }

    protected @NotNull ItemStackApplicatorMap getItemDamage() {
        if (itemDamage == null) itemDamage = ItemStackApplicatorMap.itemDamage();
        return itemDamage;
    }

    protected @NotNull ItemStackApplicatorMap getItemNBT() {
        if (itemNBT == null) itemNBT = ItemStackApplicatorMap.itemDamageNBT();
        return itemNBT;
    }

    protected @NotNull ItemStackApplicatorMap getItemDamageNBT() {
        if (itemDamageNBT == null) itemDamageNBT = ItemStackApplicatorMap.itemDamageNBT();
        return itemDamageNBT;
    }

    protected @NotNull FluidStackApplicatorMap getFluid() {
        if (fluid == null) fluid = FluidStackApplicatorMap.fluid();
        return fluid;
    }

    protected @NotNull FluidStackApplicatorMap getFluidNBT() {
        if (fluidNBT == null) fluidNBT = FluidStackApplicatorMap.fluidNBT();
        return fluidNBT;
    }

    @MustBeInvokedByOverriders
    protected void invalidate() {
        valid = false;
        filters.clear();
        item = null;
        itemDamage = null;
        itemNBT = null;
        itemDamageNBT = null;
        fluid = null;
        fluidNBT = null;
    }

    @Override
    public void rebuild() {
        invalidate();
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            filters.addFilters(i, recipe.propertyStorage());

            List<GTItemIngredient> itemIngredients = recipe.getItemIngredients();
            List<GTFluidIngredient> fluidIngredients = recipe.getFluidIngredients();

            if (itemIngredients.size() + fluidIngredients.size() > 64)
                throw new IllegalStateException("Found a recipe with more than 64 inputs in it!");

            for (byte j = 0; j < itemIngredients.size(); j++) {
                GTItemIngredient ingredient = itemIngredients.get(j);
                FlagApplicator<ItemStack> applicator;
                if (ingredient.getMatcher() == null) applicator = new SingleFlagApplicator<>(j);
                else {
                    byte finalJ = j;
                    applicator = (context, flags) -> ingredient.getMatcher().matches(context) ? flags | (1L << finalJ) :
                            flags;
                }
                for (ItemStackMatchingContext context : ItemStackMatchingContext.VALUES) {
                    Collection<ItemStack> stacks = ingredient.getMatchingStacksWithinContext(context);
                    if (!stacks.isEmpty()) {
                        ItemStackApplicatorMap map = switch (context) {
                            case ITEM -> getItem();
                            case ITEM_DAMAGE -> getItemDamage();
                            case ITEM_NBT -> getItemNBT();
                            case ITEM_DAMAGE_NBT -> getItemDamageNBT();
                        };
                        for (ItemStack stack : stacks) {
                            map.getOrCreate(stack).insertApplicator(i, applicator);
                        }
                    }
                }
            }
            int offset = itemIngredients.size();

            for (byte j = 0; j < fluidIngredients.size(); j++) {
                GTFluidIngredient ingredient = fluidIngredients.get(j);
                FlagApplicator<FluidStack> applicator;
                if (ingredient.getMatcher() == null) applicator = new SingleFlagApplicator<>((byte) (offset + j));
                else {
                    byte finalJ = j;
                    applicator = (context, flags) -> ingredient.getMatcher().matches(context) ? flags | (1L << finalJ) :
                            flags;
                }
                for (FluidStackMatchingContext context : FluidStackMatchingContext.VALUES) {
                    Collection<FluidStack> stacks = ingredient.getMatchingStacksWithinContext(context);
                    if (!stacks.isEmpty()) {
                        FluidStackApplicatorMap map = switch (context) {
                            case FLUID -> getFluid();
                            case FLUID_NBT -> getFluidNBT();
                        };
                        for (FluidStack stack : stacks) {
                            map.getOrCreate(stack).insertApplicator(i, applicator);
                        }
                    }
                }
            }
        }
        filters.trim(2);
        valid = true;
    }
}
