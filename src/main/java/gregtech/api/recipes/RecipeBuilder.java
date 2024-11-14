package gregtech.api.recipes;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.buildaction.RecipeBuildAction;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.recipes.ingredients.GTFluidIngredient;
import gregtech.api.recipes.ingredients.GTItemIngredient;
import gregtech.api.recipes.ingredients.OreItemIngredient;
import gregtech.api.recipes.ingredients.StandardFluidIngredient;
import gregtech.api.recipes.ingredients.StandardItemIngredient;
import gregtech.api.recipes.ingredients.nbt.NBTMatcher;
import gregtech.api.recipes.lookup.flag.FluidStackMatchingContext;
import gregtech.api.recipes.lookup.flag.ItemStackMatchingContext;
import gregtech.api.recipes.output.FluidOutputProvider;
import gregtech.api.recipes.output.ItemOutputProvider;
import gregtech.api.recipes.output.StandardFluidOutput;
import gregtech.api.recipes.output.StandardItemOutput;
import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.recipes.properties.RecipePropertyStorage;
import gregtech.api.recipes.properties.RecipePropertyStorageImpl;
import gregtech.api.recipes.properties.impl.CircuitProperty;
import gregtech.api.recipes.properties.impl.CleanroomProperty;
import gregtech.api.recipes.properties.impl.DimensionProperty;
import gregtech.api.recipes.properties.impl.PowerGenerationProperty;
import gregtech.api.recipes.properties.impl.PowerPropertyData;
import gregtech.api.recipes.properties.impl.PowerUsageProperty;
import gregtech.api.recipes.roll.ListWithRollInformation;
import gregtech.api.recipes.roll.RollInformation;
import gregtech.api.recipes.roll.RollInterpreter;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.EnumValidationResult;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.IHasStackForm;
import gregtech.api.util.Mods;
import gregtech.api.util.ValidationResult;
import gregtech.common.ConfigHolder;
import gregtech.integration.groovy.GroovyScriptModule;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.helper.ingredient.OreDictIngredient;
import crafttweaker.CraftTweakerAPI;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @see Recipe
 */

@SuppressWarnings("unchecked")
public class RecipeBuilder<R extends RecipeBuilder<R>> {

    protected RecipeMap<R> recipeMap;

    protected final @NotNull List<GTItemIngredient> itemInputs;
    protected final @NotNull List<RollInformation<GTItemIngredient>> rolledItemInputs;
    protected @NotNull RollInterpreter itemInputInterpreter = RollInterpreter.DEFAULT;
    protected final @NotNull List<ItemStack> itemOutputs;
    protected final @NotNull List<RollInformation<ItemStack>> rolledItemOutputs;
    protected @NotNull RollInterpreter itemOutputInterpreter = RollInterpreter.DEFAULT;
    protected @Nullable ItemOutputProvider itemOutputOverride;

    protected final @NotNull List<GTFluidIngredient> fluidInputs;
    protected final @NotNull List<RollInformation<GTFluidIngredient>> rolledFluidInputs;
    protected @NotNull RollInterpreter fluidInputInterpreter = RollInterpreter.DEFAULT;
    protected final @NotNull List<FluidStack> fluidOutputs;
    protected final @NotNull List<RollInformation<FluidStack>> rolledFluidOutputs;
    protected @NotNull RollInterpreter fluidOutputInterpreter = RollInterpreter.DEFAULT;
    protected @Nullable FluidOutputProvider fluidOutputOverride;

    protected RecipePropertyStorage recipePropertyStorage = RecipePropertyStorage.EMPTY;
    protected int duration;

    protected boolean hidden = false;
    protected GTRecipeCategory category;
    protected boolean isCTRecipe = false;

    protected EnumValidationResult recipeStatus = EnumValidationResult.VALID;

    protected boolean ignoreAllBuildActions = false;
    protected Object2ObjectOpenHashMap<ResourceLocation, RecipeBuildAction<R>> ignoredBuildActions;

    protected boolean ignoreAllBuildActions = false;
    protected Map<ResourceLocation, RecipeBuildAction<R>> ignoredBuildActions;

    protected RecipeBuilder() {
        this.itemInputs = new ArrayList<>();
        this.rolledItemInputs = new ArrayList<>();
        this.itemOutputs = new ArrayList<>();
        this.rolledItemOutputs = new ArrayList<>();
        this.fluidInputs = new ArrayList<>();
        this.rolledFluidInputs = new ArrayList<>();
        this.fluidOutputs = new ArrayList<>();
        this.rolledFluidOutputs = new ArrayList<>();
    }

    public RecipeBuilder(Recipe recipe, RecipeMap<R> recipeMap) {
        this.recipeMap = recipeMap;
        ListWithRollInformation<GTItemIngredient> itemIn = recipe.getItemIngredients();
        this.itemInputs = new ObjectArrayList<>(itemIn.getUnrolled());
        this.rolledItemInputs = itemIn.recomposeRolled();
        this.itemInputInterpreter = itemIn.getInterpreter();
        ListWithRollInformation<GTFluidIngredient> fluidIn = recipe.getFluidIngredients();
        this.fluidInputs = new ObjectArrayList<>(fluidIn.getUnrolled());
        this.rolledFluidInputs = fluidIn.recomposeRolled();
        this.fluidInputInterpreter = fluidIn.getInterpreter();
        ItemOutputProvider itemProvider = recipe.getItemOutputProvider();
        // we can unfold a standard output, but otherwise we have to set the override
        if (itemProvider instanceof StandardItemOutput standard) {
            ListWithRollInformation<ItemStack> itemOut = standard.getOutputs();
            this.itemOutputs = Arrays.stream(itemOut.getUnrolled()).map(GTUtility::copy).collect(Collectors.toList());
            this.rolledItemOutputs = itemOut.recomposeRolled();
            this.itemOutputInterpreter = itemOut.getInterpreter();
        } else {
            this.itemOutputs = new ObjectArrayList<>();
            this.rolledItemOutputs = new ObjectArrayList<>();
            this.itemOutputOverride = itemProvider;
        }
        FluidOutputProvider fluidProvider = recipe.getFluidOutputProvider();
        // we can unfold a standard output, but otherwise we have to set the override
        if (fluidProvider instanceof StandardFluidOutput standard) {
            ListWithRollInformation<FluidStack> fluidOut = standard.getOutputs();
            this.fluidOutputs = Arrays.stream(fluidOut.getUnrolled()).map(FluidStack::copy)
                    .collect(Collectors.toList());
            this.rolledFluidOutputs = fluidOut.recomposeRolled();
            this.fluidOutputInterpreter = fluidOut.getInterpreter();
        } else {
            this.fluidOutputs = new ObjectArrayList<>();
            this.rolledFluidOutputs = new ObjectArrayList<>();
            this.fluidOutputOverride = fluidProvider;
        }
        this.duration = recipe.getDuration();
        this.hidden = recipe.isHidden();
        this.category = recipe.getRecipeCategory();
        this.recipePropertyStorage = recipe.propertyStorage().copy();
    }

    protected RecipeBuilder(@NotNull RecipeBuilder<R> recipeBuilder) {
        recipeMap = recipeBuilder.recipeMap;

        itemInputs = new ObjectArrayList<>(recipeBuilder.itemInputs);
        rolledItemInputs = new ObjectArrayList<>(recipeBuilder.rolledItemInputs);
        itemInputInterpreter = recipeBuilder.itemInputInterpreter;
        itemOutputs = GTUtility.copyStackList(recipeBuilder.itemOutputs);
        rolledItemOutputs = new ObjectArrayList<>(recipeBuilder.rolledItemOutputs);
        itemOutputInterpreter = recipeBuilder.itemOutputInterpreter;
        itemOutputOverride = recipeBuilder.itemOutputOverride;

        fluidInputs = new ObjectArrayList<>(recipeBuilder.fluidInputs);
        rolledFluidInputs = new ObjectArrayList<>(recipeBuilder.rolledFluidInputs);
        fluidInputInterpreter = recipeBuilder.fluidInputInterpreter;
        fluidOutputs = GTUtility.copyFluidList(recipeBuilder.fluidOutputs);
        rolledFluidOutputs = new ObjectArrayList<>(recipeBuilder.rolledFluidOutputs);
        fluidOutputInterpreter = recipeBuilder.fluidOutputInterpreter;
        fluidOutputOverride = recipeBuilder.fluidOutputOverride;

        recipePropertyStorage = recipeBuilder.recipePropertyStorage.copy();
        duration = recipeBuilder.duration;
        hidden = recipeBuilder.hidden;
        category = recipeBuilder.category;
        isCTRecipe = recipeBuilder.isCTRecipe;
        recipeStatus = recipeBuilder.recipeStatus;
        this.ignoreAllBuildActions = recipeBuilder.ignoreAllBuildActions;
        if (recipeBuilder.ignoredBuildActions != null) {
            this.ignoredBuildActions = new Object2ObjectOpenHashMap<>(recipeBuilder.ignoredBuildActions);
        }
    }

    public R read(@NotNull RecipeBuilder<R> recipeBuilder) {
        itemInputs.addAll(recipeBuilder.itemInputs);
        rolledItemInputs.addAll(recipeBuilder.rolledItemInputs);
        itemInputInterpreter = recipeBuilder.itemInputInterpreter;
        itemOutputs.addAll(recipeBuilder.itemOutputs);
        rolledItemOutputs.addAll(recipeBuilder.rolledItemOutputs);
        itemOutputInterpreter = recipeBuilder.itemOutputInterpreter;
        itemOutputOverride = recipeBuilder.itemOutputOverride;

        fluidInputs.addAll(recipeBuilder.fluidInputs);
        rolledFluidInputs.addAll(recipeBuilder.rolledFluidInputs);
        fluidInputInterpreter = recipeBuilder.fluidInputInterpreter;
        fluidOutputs.addAll(recipeBuilder.fluidOutputs);
        rolledFluidOutputs.addAll(recipeBuilder.rolledFluidOutputs);
        fluidOutputInterpreter = recipeBuilder.fluidOutputInterpreter;
        fluidOutputOverride = recipeBuilder.fluidOutputOverride;

        recipePropertyStorage = recipeBuilder.recipePropertyStorage.copy();
        duration = recipeBuilder.duration;
        hidden = recipeBuilder.hidden;
        category = recipeBuilder.category;
        isCTRecipe = recipeBuilder.isCTRecipe;
        recipeStatus = recipeBuilder.recipeStatus;
        return (R) this;
        this.ignoreAllBuildActions = recipeBuilder.ignoreAllBuildActions;
        if (recipeBuilder.ignoredBuildActions != null) {
            this.ignoredBuildActions = new Object2ObjectOpenHashMap<>(recipeBuilder.ignoredBuildActions);
        }
    }

    public R cleanroom(@Nullable CleanroomType cleanroom) {
        if (!ConfigHolder.machines.enableCleanroom) {
            return (R) this;
        }
        if (cleanroom == null) this.removeProperty(CleanroomProperty.getInstance());
        else this.applyProperty(CleanroomProperty.getInstance(), cleanroom);
        return (R) this;
    }

    public R dimension(int dimensionID) {
        return dimension(dimensionID, false);
    }

    public R dimension(int dimensionID, boolean toBlackList) {
        DimensionProperty.DimensionPropertyList dimensionIDs = getCompleteDimensionIDs();
        if (dimensionIDs == null) {
            dimensionIDs = new DimensionProperty.DimensionPropertyList();
            this.applyProperty(DimensionProperty.getInstance(), dimensionIDs);
        }
        dimensionIDs.add(dimensionID, toBlackList);
        return (R) this;
    }

    public @Nullable DimensionProperty.DimensionPropertyList getCompleteDimensionIDs() {
        return this.recipePropertyStorage.get(DimensionProperty.getInstance(), null);
    }

    public @NotNull IntList getDimensionIDs() {
        return this.recipePropertyStorage.get(DimensionProperty.getInstance(),
                DimensionProperty.DimensionPropertyList.EMPTY_LIST).whiteListDimensions;
    }

    public @NotNull IntList getBlockedDimensionIDs() {
        return this.recipePropertyStorage.get(DimensionProperty.getInstance(),
                DimensionProperty.DimensionPropertyList.EMPTY_LIST).blackListDimensions;
    }

    public final <T> boolean applyProperty(@NotNull RecipeProperty<T> property, @NotNull T value) {
        if (this.recipePropertyStorage == RecipePropertyStorage.EMPTY) {
            this.recipePropertyStorage = new RecipePropertyStorageImpl();
        }

        boolean stored = this.recipePropertyStorage.store(property, value);
        if (!stored) {
            this.recipePropertyStorage = RecipePropertyStorage.ERRORED;
        }
        return stored;
    }

    @Nullable
    public final <T> T removeProperty(@NotNull RecipeProperty<T> property) {
        if (this.recipePropertyStorage == RecipePropertyStorage.EMPTY) return null;

        return this.recipePropertyStorage.remove(property);
    }

    @Contract("_, !null -> !null")
    public final <T> @Nullable T getProperty(@NotNull RecipeProperty<T> property, @Nullable T defaultValue) {
        if (this.recipePropertyStorage == RecipePropertyStorage.EMPTY) return defaultValue;
        return this.recipePropertyStorage.get(property, defaultValue);
    }

    public R ingredient(GTItemIngredient input) {
        this.itemInputs.add(input);
        return (R) this;
    }

    public R ingredient(GTFluidIngredient input) {
        this.fluidInputs.add(input);
        return (R) this;
    }

    public R ingredient(GTItemIngredient input, long rollValue, long rollBoost) {
        this.rolledItemInputs.add(RollInformation.of(input, rollValue, rollBoost));
        return (R) this;
    }

    public R ingredient(GTFluidIngredient input, long rollValue, long rollBoost) {
        this.rolledFluidInputs.add(RollInformation.of(input, rollValue, rollBoost));
        return (R) this;
    }

    //////////////////////
    // ITEM INPUT START //
    //////////////////////

    public R inputItem(@NotNull String oredict) {
        return inputItem(oredict, 1);
    }

    public R inputItemRoll(@NotNull String oredict, long rollValue, long rollBoost) {
        return inputItemRoll(oredict, 1, rollValue, rollBoost);
    }

    public R inputItem(@NotNull String oredict, int count) {
        return inputItem(oredict, count, null);
    }

    public R inputItemRoll(@NotNull String oredict, int count, long rollValue, long rollBoost) {
        return inputItemRoll(oredict, count, rollValue, rollBoost, null);
    }

    public R inputItem(@NotNull String oredict, int count, @Nullable NBTMatcher nbtMatcher) {
        return ingredient(OreItemIngredient.of(oredict, count, nbtMatcher));
    }

    public R inputItemRoll(@NotNull String oredict, int count, long rollValue, long rollBoost,
                           @Nullable NBTMatcher nbtMatcher) {
        return ingredient(OreItemIngredient.of(oredict, count, nbtMatcher), rollValue, rollBoost);
    }

    public R inputItem(OrePrefix orePrefix, Material material) {
        return inputItem(orePrefix, material, 1);
    }

    public R inputItemRoll(OrePrefix orePrefix, Material material, long rollValue, long rollBoost) {
        return inputItemRoll(orePrefix, material, 1, rollValue, rollBoost);
    }

    public R inputItem(OrePrefix orePrefix, Material material, int count) {
        return inputItem(orePrefix, material, count, null);
    }

    public R inputItemRoll(OrePrefix orePrefix, Material material, int count, long rollValue, long rollBoost) {
        return inputItemRoll(orePrefix, material, count, rollValue, rollBoost, null);
    }

    public R inputItem(OrePrefix orePrefix, Material material, int count, @Nullable NBTMatcher nbtMatcher) {
        return inputItem(new UnificationEntry(orePrefix, material).toString(), count, nbtMatcher);
    }

    public R inputItemRoll(OrePrefix orePrefix, Material material, int count, long rollValue, long rollBoost,
                           @Nullable NBTMatcher nbtMatcher) {
        return inputItemRoll(new UnificationEntry(orePrefix, material).toString(), count, rollValue, rollBoost,
                nbtMatcher);
    }

    public R inputItem(Item item) {
        return ingredient(StandardItemIngredient.builder()
                .addItem(item).clearToContextAndBuild(ItemStackMatchingContext.ITEM));
    }

    public R inputItemRoll(Item item, long rollValue, long rollBoost) {
        return ingredient(StandardItemIngredient.builder()
                .addItem(item).clearToContextAndBuild(ItemStackMatchingContext.ITEM), rollValue, rollBoost);
    }

    public R inputItem(Item item, NBTMatcher matcher) {
        return ingredient(StandardItemIngredient.builder().setMatcher(matcher)
                .addItem(item).clearToContextAndBuild(ItemStackMatchingContext.ITEM));
    }

    public R inputItemRoll(Item item, long rollValue, long rollBoost, NBTMatcher matcher) {
        return ingredient(StandardItemIngredient.builder().setMatcher(matcher)
                .addItem(item).clearToContextAndBuild(ItemStackMatchingContext.ITEM), rollValue, rollBoost);
    }

    public R inputItem(Item item, NBTTagCompound tag) {
        return ingredient(StandardItemIngredient.builder()
                .addItem(item, 0, tag).clearToContextAndBuild(ItemStackMatchingContext.ITEM_NBT));
    }

    public R inputItemRoll(Item item, long rollValue, long rollBoost, NBTTagCompound tag) {
        return ingredient(StandardItemIngredient.builder()
                .addItem(item, 0, tag).clearToContextAndBuild(ItemStackMatchingContext.ITEM_NBT), rollValue, rollBoost);
    }

    public R inputItem(Item item, int count) {
        return ingredient(StandardItemIngredient.builder().setCount(count)
                .addItem(item).clearToContextAndBuild(ItemStackMatchingContext.ITEM));
    }

    public R inputItemRoll(Item item, int count, long rollValue, long rollBoost) {
        return ingredient(StandardItemIngredient.builder().setCount(count)
                .addItem(item).clearToContextAndBuild(ItemStackMatchingContext.ITEM), rollValue, rollBoost);
    }

    public R inputItem(Item item, int count, NBTMatcher matcher) {
        return ingredient(StandardItemIngredient.builder().setCount(count).setMatcher(matcher)
                .addItem(item).clearToContextAndBuild(ItemStackMatchingContext.ITEM));
    }

    public R inputItemRoll(Item item, int count, long rollValue, long rollBoost, NBTMatcher matcher) {
        return ingredient(StandardItemIngredient.builder().setCount(count).setMatcher(matcher)
                .addItem(item).clearToContextAndBuild(ItemStackMatchingContext.ITEM), rollValue, rollBoost);
    }

    public R inputItem(Item item, int count, NBTTagCompound tag) {
        return ingredient(StandardItemIngredient.builder().setCount(count)
                .addItem(item, 0, tag).clearToContextAndBuild(ItemStackMatchingContext.ITEM_NBT));
    }

    public R inputItemRoll(Item item, int count, long rollValue, long rollBoost, NBTTagCompound tag) {
        return ingredient(StandardItemIngredient.builder().setCount(count)
                .addItem(item, 0, tag).clearToContextAndBuild(ItemStackMatchingContext.ITEM_NBT), rollValue, rollBoost);
    }

    /**
     * Use {@link #inputItem(Item)} or {@link #inputItem(Item, int)} if all possible meta values should be matched
     * instead.
     */
    public R inputItem(Item item, int count, int... meta) {
        if (meta.length == 0) return (R) this;
        StandardItemIngredient.ItemIngredientBuilder builder = StandardItemIngredient.builder().setCount(count);
        for (int m : meta) {
            if (m == GTValues.W) throw new IllegalArgumentException(
                    "A wild meta value matcher should not be explicitly specified! Do not specify meta if you want to match all meta values.");
            builder.addItem(item, m);
        }
        return ingredient(builder.clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE));
    }

    /**
     * Use {@link #inputItemRoll(Item, long, long)} or {@link #inputItemRoll(Item, int, long, long)} if all possible
     * meta values should be matched instead.
     */
    public R inputItemRoll(Item item, int count, long rollValue, long rollBoost, int... meta) {
        if (meta.length == 0) return (R) this;
        StandardItemIngredient.ItemIngredientBuilder builder = StandardItemIngredient.builder().setCount(count);
        for (int m : meta) {
            if (m == GTValues.W) throw new IllegalArgumentException(
                    "A wild meta value matcher should not be explicitly specified! Do not specify meta if you want to match all meta values.");
            builder.addItem(item, m);
        }
        return ingredient(builder.clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE), rollValue, rollBoost);
    }

    /**
     * Use {@link #inputItem(Item, NBTMatcher)} or {@link #inputItem(Item, int, NBTMatcher)} if all possible meta values
     * should be matched instead.
     */
    public R inputItem(Item item, int count, NBTMatcher matcher, int... meta) {
        if (meta.length == 0) return (R) this;
        StandardItemIngredient.ItemIngredientBuilder builder = StandardItemIngredient.builder().setCount(count)
                .setMatcher(matcher);
        for (int m : meta) {
            if (m == GTValues.W) throw new IllegalArgumentException(
                    "A wild meta value matcher should not be explicitly specified! Do not specify meta if you want to match all meta values.");
            builder.addItem(item, m);
        }
        return ingredient(builder.clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE));
    }

    /**
     * Use {@link #inputItemRoll(Item, long, long, NBTMatcher)} or
     * {@link #inputItemRoll(Item, int, long, long, NBTMatcher)} if all possible meta values should be matched instead.
     */
    public R inputItemRoll(Item item, int count, long rollValue, long rollBoost, NBTMatcher matcher, int... meta) {
        if (meta.length == 0) return (R) this;
        StandardItemIngredient.ItemIngredientBuilder builder = StandardItemIngredient.builder().setCount(count)
                .setMatcher(matcher);
        for (int m : meta) {
            if (m == GTValues.W) throw new IllegalArgumentException(
                    "A wild meta value matcher should not be explicitly specified! Do not specify meta if you want to match all meta values.");
            builder.addItem(item, m);
        }
        return ingredient(builder.clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE), rollValue, rollBoost);
    }

    /**
     * Use {@link #inputItem(Item, NBTTagCompound)} or {@link #inputItem(Item, int, NBTTagCompound)} if all possible
     * meta values should be matched instead.
     */
    public R inputItem(Item item, int count, NBTTagCompound tag, int... meta) {
        if (meta.length == 0) return (R) this;
        StandardItemIngredient.ItemIngredientBuilder builder = StandardItemIngredient.builder().setCount(count);
        for (int m : meta) {
            if (m == GTValues.W) throw new IllegalArgumentException(
                    "A wild meta value matcher should not be explicitly specified! Do not specify meta if you want to match all meta values.");
            builder.addItem(item, m, tag);
        }
        return ingredient(builder.clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT));
    }

    /**
     * Use {@link #inputItemRoll(Item, long, long, NBTTagCompound)} or
     * {@link #inputItemRoll(Item, int, long, long, NBTTagCompound)} if all possible meta values should be matched
     * instead.
     */
    public R inputItemRoll(Item item, int count, long rollValue, long rollBoost, NBTTagCompound tag, int... meta) {
        if (meta.length == 0) return (R) this;
        StandardItemIngredient.ItemIngredientBuilder builder = StandardItemIngredient.builder().setCount(count);
        for (int m : meta) {
            if (m == GTValues.W) throw new IllegalArgumentException(
                    "A wild meta value matcher should not be explicitly specified! Do not specify meta if you want to match all meta values.");
            builder.addItem(item, m, tag);
        }
        return ingredient(builder.clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT), rollValue,
                rollBoost);
    }

    public R inputItem(Block block) {
        return ingredient(StandardItemIngredient.builder()
                .addBlock(block).clearToContextAndBuild(ItemStackMatchingContext.ITEM));
    }

    public R inputItemRoll(Block block, long rollValue, long rollBoost) {
        return ingredient(StandardItemIngredient.builder()
                .addBlock(block).clearToContextAndBuild(ItemStackMatchingContext.ITEM), rollValue, rollBoost);
    }

    public R inputItem(Block block, NBTMatcher matcher) {
        return ingredient(StandardItemIngredient.builder().setMatcher(matcher)
                .addBlock(block).clearToContextAndBuild(ItemStackMatchingContext.ITEM));
    }

    public R inputItemRoll(Block block, long rollValue, long rollBoost, NBTMatcher matcher) {
        return ingredient(StandardItemIngredient.builder().setMatcher(matcher)
                .addBlock(block).clearToContextAndBuild(ItemStackMatchingContext.ITEM), rollValue, rollBoost);
    }

    public R inputItem(Block block, NBTTagCompound tag) {
        return ingredient(StandardItemIngredient.builder()
                .addBlock(block, 0, tag).clearToContextAndBuild(ItemStackMatchingContext.ITEM_NBT));
    }

    public R inputItemRoll(Block block, long rollValue, long rollBoost, NBTTagCompound tag) {
        return ingredient(StandardItemIngredient.builder()
                .addBlock(block, 0, tag).clearToContextAndBuild(ItemStackMatchingContext.ITEM_NBT), rollValue,
                rollBoost);
    }

    public R inputItem(Block block, int count) {
        return ingredient(StandardItemIngredient.builder().setCount(count)
                .addBlock(block).clearToContextAndBuild(ItemStackMatchingContext.ITEM));
    }

    public R inputItemRoll(Block block, int count, long rollValue, long rollBoost) {
        return ingredient(StandardItemIngredient.builder().setCount(count)
                .addBlock(block).clearToContextAndBuild(ItemStackMatchingContext.ITEM), rollValue, rollBoost);
    }

    public R inputItem(Block block, int count, NBTMatcher matcher) {
        return ingredient(StandardItemIngredient.builder().setCount(count).setMatcher(matcher)
                .addBlock(block).clearToContextAndBuild(ItemStackMatchingContext.ITEM));
    }

    public R inputItemRoll(Block block, int count, long rollValue, long rollBoost, NBTMatcher matcher) {
        return ingredient(StandardItemIngredient.builder().setCount(count).setMatcher(matcher)
                .addBlock(block).clearToContextAndBuild(ItemStackMatchingContext.ITEM), rollValue, rollBoost);
    }

    public R inputItem(Block block, int count, NBTTagCompound tag) {
        return ingredient(StandardItemIngredient.builder().setCount(count)
                .addBlock(block, 0, tag).clearToContextAndBuild(ItemStackMatchingContext.ITEM_NBT));
    }

    public R inputItemRoll(Block block, int count, long rollValue, long rollBoost, NBTTagCompound tag) {
        return ingredient(StandardItemIngredient.builder().setCount(count)
                .addBlock(block, 0, tag).clearToContextAndBuild(ItemStackMatchingContext.ITEM_NBT), rollValue,
                rollBoost);
    }

    /**
     * Use {@link #inputItem(Block)} or {@link #inputItem(Block, int)} if all possible meta values should be matched
     * instead.
     */
    public R inputItem(Block block, int count, int... meta) {
        if (meta.length == 0) return (R) this;
        StandardItemIngredient.ItemIngredientBuilder builder = StandardItemIngredient.builder().setCount(count);
        for (int m : meta) {
            if (m == GTValues.W) throw new IllegalArgumentException(
                    "A wild meta value matcher should not be explicitly specified! Do not specify meta if you want to match all meta values.");
            builder.addBlock(block, m);
        }
        return ingredient(builder.clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE));
    }

    /**
     * Use {@link #inputItemRoll(Block, long, long)} or {@link #inputItemRoll(Block, int, long, long)} if all possible
     * meta values should be matched instead.
     */
    public R inputItemRoll(Block block, int count, long rollValue, long rollBoost, int... meta) {
        if (meta.length == 0) return (R) this;
        StandardItemIngredient.ItemIngredientBuilder builder = StandardItemIngredient.builder().setCount(count);
        for (int m : meta) {
            if (m == GTValues.W) throw new IllegalArgumentException(
                    "A wild meta value matcher should not be explicitly specified! Do not specify meta if you want to match all meta values.");
            builder.addBlock(block, m);
        }
        return ingredient(builder.clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE), rollValue, rollBoost);
    }

    /**
     * Use {@link #inputItem(Block, NBTMatcher)} or {@link #inputItem(Block, int, NBTMatcher)} if all possible meta
     * values should be matched instead.
     */
    public R inputItem(Block block, int count, NBTMatcher matcher, int... meta) {
        if (meta.length == 0) return (R) this;
        StandardItemIngredient.ItemIngredientBuilder builder = StandardItemIngredient.builder().setCount(count)
                .setMatcher(matcher);
        for (int m : meta) {
            if (m == GTValues.W) throw new IllegalArgumentException(
                    "A wild meta value matcher should not be explicitly specified! Do not specify meta if you want to match all meta values.");
            builder.addBlock(block, m);
        }
        return ingredient(builder.clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE));
    }

    /**
     * Use {@link #inputItemRoll(Block, long, long, NBTMatcher)} or
     * {@link #inputItemRoll(Block, int, long, long, NBTMatcher)} if all possible meta values should be matched instead.
     */
    public R inputItemRoll(Block block, int count, long rollValue, long rollBoost, NBTMatcher matcher, int... meta) {
        if (meta.length == 0) return (R) this;
        StandardItemIngredient.ItemIngredientBuilder builder = StandardItemIngredient.builder().setCount(count)
                .setMatcher(matcher);
        for (int m : meta) {
            if (m == GTValues.W) throw new IllegalArgumentException(
                    "A wild meta value matcher should not be explicitly specified! Do not specify meta if you want to match all meta values.");
            builder.addBlock(block, m);
        }
        return ingredient(builder.clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE), rollValue, rollBoost);
    }

    /**
     * Use {@link #inputItem(Block, NBTTagCompound)} or {@link #inputItem(Block, int, NBTTagCompound)} if all possible
     * meta values should be matched instead.
     */
    public R inputItem(Block block, int count, NBTTagCompound tag, int... meta) {
        if (meta.length == 0) return (R) this;
        StandardItemIngredient.ItemIngredientBuilder builder = StandardItemIngredient.builder().setCount(count);
        for (int m : meta) {
            if (m == GTValues.W) throw new IllegalArgumentException(
                    "A wild meta value matcher should not be explicitly specified! Do not specify meta if you want to match all meta values.");
            builder.addBlock(block, m, tag);
        }
        return ingredient(builder.clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT));
    }

    /**
     * Use {@link #inputItemRoll(Block, long, long, NBTTagCompound)} or
     * {@link #inputItemRoll(Block, int, long, long, NBTTagCompound)} if all possible meta values should be matched
     * instead.
     */
    public R inputItemRoll(Block block, int count, long rollValue, long rollBoost, NBTTagCompound tag, int... meta) {
        if (meta.length == 0) return (R) this;
        StandardItemIngredient.ItemIngredientBuilder builder = StandardItemIngredient.builder().setCount(count);
        for (int m : meta) {
            if (m == GTValues.W) throw new IllegalArgumentException(
                    "A wild meta value matcher should not be explicitly specified! Do not specify meta if you want to match all meta values.");
            builder.addBlock(block, m, tag);
        }
        return ingredient(builder.clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT), rollValue,
                rollBoost);
    }

    public R inputItem(IHasStackForm stackForm) {
        return inputItem(stackForm, 1);
    }

    public R inputItemRoll(IHasStackForm stackForm, long rollValue, long rollBoost) {
        return inputItemRoll(stackForm, 1, rollValue, rollBoost);
    }

    public R inputItem(IHasStackForm stackForm, NBTMatcher matcher) {
        return inputItem(stackForm, 1, matcher);
    }

    public R inputItemRoll(IHasStackForm stackForm, long rollValue, long rollBoost, NBTMatcher matcher) {
        return inputItemRoll(stackForm, 1, rollValue, rollBoost, matcher);
    }

    public R inputItem(IHasStackForm stackForm, NBTTagCompound tag) {
        return inputItem(stackForm, 1, tag);
    }

    public R inputItemRoll(IHasStackForm stackForm, long rollValue, long rollBoost, NBTTagCompound tag) {
        return inputItemRoll(stackForm, 1, rollValue, rollBoost, tag);
    }

    public R inputItem(IHasStackForm stackForm, int count) {
        return ingredient(StandardItemIngredient.builder().setCount(count).addStack(stackForm.getStackForm(count))
                .clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE));
    }

    public R inputItemRoll(IHasStackForm stackForm, int count, long rollValue, long rollBoost) {
        return ingredient(StandardItemIngredient.builder().setCount(count).addStack(stackForm.getStackForm(count))
                .clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE), rollValue, rollBoost);
    }

    public R inputItem(IHasStackForm stackForm, int count, NBTMatcher matcher) {
        return ingredient(StandardItemIngredient.builder().setCount(count).setMatcher(matcher)
                .addStack(stackForm.getStackForm(count)).clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE));
    }

    public R inputItemRoll(IHasStackForm stackForm, int count, long rollValue, long rollBoost, NBTMatcher matcher) {
        return ingredient(StandardItemIngredient.builder().setCount(count).setMatcher(matcher)
                .addStack(stackForm.getStackForm(count)).clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE),
                rollValue, rollBoost);
    }

    public R inputItem(IHasStackForm stackForm, int count, NBTTagCompound tag) {
        ItemStack stack = stackForm.getStackForm(count);
        stack.setTagCompound(tag);
        return ingredient(StandardItemIngredient.builder().setCount(count).addStack(stack)
                .clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT));
    }

    public R inputItemRoll(IHasStackForm stackForm, int count, long rollValue, long rollBoost, NBTTagCompound tag) {
        ItemStack stack = stackForm.getStackForm(count);
        stack.setTagCompound(tag);
        return ingredient(StandardItemIngredient.builder().setCount(count).addStack(stack)
                .clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT), rollValue, rollBoost);
    }

    public R inputs(ItemStack input) {
        if (input == null || input.isEmpty()) {
            GTLog.logger.error("Input cannot be null or empty. Input: {}", input, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        } else {
            ingredient(StandardItemIngredient.builder().setCount(input.getCount()).addStack(input)
                    .clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT));
        }
        return (R) this;
    }

    public R inputsRolled(long rollValue, long rollBoost, ItemStack input) {
        if (input == null || input.isEmpty()) {
            GTLog.logger.error("Input cannot be null or empty. Input: {}", input, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
        } else {
            ingredient(StandardItemIngredient.builder().setCount(input.getCount()).addStack(input)
                    .clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT), rollValue, rollBoost);
        }
        return (R) this;
    }

    public R inputs(ItemStack... inputs) {
        for (ItemStack input : inputs) {
            if (input == null || input.isEmpty()) {
                GTLog.logger.error("Inputs cannot contain null or empty ItemStacks. Inputs: {}", input,
                        new Throwable());
                recipeStatus = EnumValidationResult.INVALID;
                continue;
            }
            ingredient(StandardItemIngredient.builder().setCount(input.getCount()).addStack(input)
                    .clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT));
        }
        return (R) this;
    }

    public R inputsRolled(long rollValue, long rollBoost, ItemStack... inputs) {
        for (ItemStack input : inputs) {
            if (input == null || input.isEmpty()) {
                GTLog.logger.error("Inputs cannot contain null or empty ItemStacks. Inputs: {}", input,
                        new Throwable());
                recipeStatus = EnumValidationResult.INVALID;
                continue;
            }
            ingredient(StandardItemIngredient.builder().setCount(input.getCount()).addStack(input)
                    .clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT), rollValue, rollBoost);
        }
        return (R) this;
    }

    public R inputStacks(Collection<ItemStack> inputs) {
        for (ItemStack input : inputs) {
            if (input == null || input.isEmpty()) {
                GTLog.logger.error("Input cannot contain null or empty ItemStacks. Inputs: {}", input, new Throwable());
                recipeStatus = EnumValidationResult.INVALID;
                continue;
            }
            ingredient(StandardItemIngredient.builder().setCount(input.getCount()).addStack(input)
                    .clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT));
        }
        return (R) this;
    }

    public R inputStacksRolled(Collection<ItemStack> inputs, long rollValue, long rollBoost) {
        for (ItemStack input : inputs) {
            if (input == null || input.isEmpty()) {
                GTLog.logger.error("Input cannot contain null or empty ItemStacks. Inputs: {}", input, new Throwable());
                recipeStatus = EnumValidationResult.INVALID;
                continue;
            }
            ingredient(StandardItemIngredient.builder().setCount(input.getCount()).addStack(input)
                    .clearToContextAndBuild(ItemStackMatchingContext.ITEM_DAMAGE_NBT), rollValue, rollBoost);
        }
        return (R) this;
    }

    public R setItemInputInterpreter(@NotNull RollInterpreter itemInputInterpreter) {
        this.itemInputInterpreter = itemInputInterpreter;
        return (R) this;
    }

    public R clearItemInputs() {
        this.itemInputs.clear();
        this.rolledItemInputs.clear();
        return (R) this;
    }

    ////////////////////
    // ITEM INPUT END //
    ////////////////////

    // Special case inputs //

    public R notConsumable(GTItemIngredient gtRecipeIngredient) {
        return ingredient(gtRecipeIngredient, Long.MIN_VALUE, 0);
    }

    public R notConsumable(ItemStack itemStack) {
        return inputsRolled(Long.MIN_VALUE, 0, itemStack);
    }

    public R notConsumable(OrePrefix prefix, Material material, int amount) {
        return inputItemRoll(prefix, material, amount, Long.MIN_VALUE, 0);
    }

    public R notConsumable(OrePrefix prefix, Material material) {
        return notConsumable(prefix, material, 1);
    }

    public R notConsumable(MetaItem<?>.MetaValueItem item) {
        return inputItemRoll(item, Long.MIN_VALUE, 0);
    }

    public R notConsumable(Fluid fluid, int amount) {
        return inputFluidRoll(fluid, amount, Long.MIN_VALUE, 0);
    }

    public R notConsumable(Fluid fluid) {
        return inputFluidRoll(fluid, 1, Long.MIN_VALUE, 0);
    }

    public R notConsumable(FluidStack fluidStack) {
        return fluidInputsRolled(Long.MIN_VALUE, 0, fluidStack);
    }

    public R circuitMeta(int circuitNumber) {
        if (CircuitProperty.CIRCUIT_MIN > circuitNumber || circuitNumber > CircuitProperty.CIRCUIT_MAX) {
            GTLog.logger.error("Integrated Circuit Number cannot be less than {} or more than {}",
                    CircuitProperty.CIRCUIT_MIN, CircuitProperty.CIRCUIT_MAX, new Throwable());
            recipeStatus = EnumValidationResult.INVALID;
            return (R) this;
        }
        applyProperty(CircuitProperty.getInstance(), (byte) circuitNumber);
        return (R) this;
    }

    // ------------------- //

    ///////////////////////
    // FLUID INPUT START //
    ///////////////////////

    public R inputFluid(Fluid fluid, int amount) {
        return ingredient(StandardFluidIngredient.builder().addFluid(fluid).setCount(amount)
                .clearToContextAndBuild(FluidStackMatchingContext.FLUID));
    }

    public R inputFluidRoll(Fluid fluid, int amount, long rollValue, long rollBoost) {
        return ingredient(StandardFluidIngredient.builder().addFluid(fluid).setCount(amount)
                .clearToContextAndBuild(FluidStackMatchingContext.FLUID), rollValue, rollBoost);
    }

    public R inputFluid(Fluid fluid, int amount, NBTMatcher matcher) {
        return ingredient(StandardFluidIngredient.builder().addFluid(fluid).setCount(amount).setMatcher(matcher)
                .clearToContextAndBuild(FluidStackMatchingContext.FLUID));
    }

    public R inputFluidRoll(Fluid fluid, int amount, long rollValue, long rollBoost, NBTMatcher matcher) {
        return ingredient(StandardFluidIngredient.builder().addFluid(fluid).setCount(amount).setMatcher(matcher)
                .clearToContextAndBuild(FluidStackMatchingContext.FLUID), rollValue, rollBoost);
    }

    public R inputFluid(Fluid fluid, int amount, NBTTagCompound tag) {
        return ingredient(StandardFluidIngredient.builder().addFluid(fluid, tag).setCount(amount)
                .clearToContextAndBuild(FluidStackMatchingContext.FLUID_NBT));
    }

    public R inputFluidRoll(Fluid fluid, int amount, long rollValue, long rollBoost, NBTTagCompound tag) {
        return ingredient(StandardFluidIngredient.builder().addFluid(fluid, tag).setCount(amount)
                .clearToContextAndBuild(FluidStackMatchingContext.FLUID_NBT), rollValue, rollBoost);
    }

    public R fluidInputs(FluidStack input) {
        if (input != null && input.amount > 0) {
            ingredient(StandardFluidIngredient.builder().addStack(input).setCount(input.amount)
                    .clearToContextAndBuild(FluidStackMatchingContext.FLUID_NBT));
        } else if (input != null) {
            GTLog.logger.error("Fluid Input count cannot be less than 0. Actual: {}.", input.amount, new Throwable());
        } else {
            GTLog.logger.error("FluidStack cannot be null.");
        }
        return (R) this;
    }

    public R fluidInputsRolled(long rollValue, long rollBoost, FluidStack input) {
        if (input != null && input.amount > 0) {
            ingredient(StandardFluidIngredient.builder().addStack(input).setCount(input.amount)
                    .clearToContextAndBuild(FluidStackMatchingContext.FLUID_NBT), rollValue, rollBoost);
        } else if (input != null) {
            GTLog.logger.error("Fluid Input count cannot be less than 0. Actual: {}.", input.amount, new Throwable());
        } else {
            GTLog.logger.error("FluidStack cannot be null.");
        }
        return (R) this;
    }

    public R fluidInputs(FluidStack... fluidStacks) {
        for (FluidStack fluidStack : fluidStacks) {
            if (fluidStack != null && fluidStack.amount > 0) {
                ingredient(StandardFluidIngredient.builder().addStack(fluidStack).setCount(fluidStack.amount)
                        .clearToContextAndBuild(FluidStackMatchingContext.FLUID_NBT));
            } else if (fluidStack != null) {
                GTLog.logger.error("Fluid Input count cannot be less than 0. Actual: {}.", fluidStack.amount,
                        new Throwable());
            } else {
                GTLog.logger.error("FluidStack cannot be null.");
            }
        }
        return (R) this;
    }

    public R fluidInputsRolled(long rollValue, long rollBoost, FluidStack... fluidStacks) {
        for (FluidStack fluidStack : fluidStacks) {
            if (fluidStack != null && fluidStack.amount > 0) {
                ingredient(StandardFluidIngredient.builder().addStack(fluidStack).setCount(fluidStack.amount)
                        .clearToContextAndBuild(FluidStackMatchingContext.FLUID_NBT), rollValue, rollBoost);
            } else if (fluidStack != null) {
                GTLog.logger.error("Fluid Input count cannot be less than 0. Actual: {}.", fluidStack.amount,
                        new Throwable());
            } else {
                GTLog.logger.error("FluidStack cannot be null.");
            }
        }
        return (R) this;
    }

    public R setFluidInputInterpreter(@NotNull RollInterpreter fluidInputInterpreter) {
        this.fluidInputInterpreter = fluidInputInterpreter;
        return (R) this;
    }

    public R clearFluidInputs() {
        this.fluidInputs.clear();
        this.rolledFluidInputs.clear();
        return (R) this;
    }

    /////////////////////
    // FLUID INPUT END //
    /////////////////////

    ///////////////////////
    // ITEM OUTPUT START //
    ///////////////////////

    public R outputItem(OrePrefix orePrefix, Material material) {
        return outputs(OreDictUnifier.get(orePrefix, material, 1));
    }

    public R outputItemRoll(OrePrefix orePrefix, Material material, long rollValue, long rollBoost) {
        return outputsRolled(rollValue, rollBoost, OreDictUnifier.get(orePrefix, material, 1));
    }

    public R outputItem(OrePrefix orePrefix, Material material, int count) {
        return outputs(OreDictUnifier.get(orePrefix, material, count));
    }

    public R outputItemRoll(OrePrefix orePrefix, Material material, int count, long rollValue, long rollBoost) {
        return outputsRolled(rollValue, rollBoost, OreDictUnifier.get(orePrefix, material, count));
    }

    public R outputItem(Item item) {
        return outputItem(item, 1);
    }

    public R outputItemRoll(Item item, long rollValue, long rollBoost) {
        return outputItemRoll(item, 1, rollValue, rollBoost);
    }

    public R outputItem(Item item, int count) {
        return outputs(new ItemStack(item, count));
    }

    public R outputItemRoll(Item item, int count, long rollValue, long rollBoost) {
        return outputsRolled(rollValue, rollBoost, new ItemStack(item, count));
    }

    public R outputItem(Item item, int count, int meta) {
        return outputs(new ItemStack(item, count, meta));
    }

    public R outputItemRoll(Item item, int count, int meta, long rollValue, long rollBoost) {
        return outputsRolled(rollValue, rollBoost, new ItemStack(item, count, meta));
    }

    public R outputItem(Block item) {
        return outputItem(item, 1);
    }

    public R outputItemRoll(Block item, long rollValue, long rollBoost) {
        return outputItemRoll(item, 1, rollValue, rollBoost);
    }

    public R outputItem(Block item, int count) {
        return outputs(new ItemStack(item, count));
    }

    public R outputItemRoll(Block item, int count, long rollValue, long rollBoost) {
        return outputsRolled(rollValue, rollBoost, new ItemStack(item, count));
    }

    public R outputItem(IHasStackForm item, int count) {
        return outputs(item.getStackForm(count));
    }

    public R outputItemRoll(IHasStackForm item, int count, long rollValue, long rollBoost) {
        return outputsRolled(rollValue, rollBoost, item.getStackForm(count));
    }

    public R outputItem(IHasStackForm item) {
        return outputItem(item, 1);
    }

    public R outputItemRoll(IHasStackForm item, long rollValue, long rollBoost) {
        return outputItemRoll(item, 1, rollValue, rollBoost);
    }

    public R outputs(ItemStack output) {
        if (output != null && !output.isEmpty()) {
            this.itemOutputs.add(output);
        }
        return (R) this;
    }

    public R outputsRolled(long rollValue, long rollBoost, ItemStack output) {
        if (output != null && !output.isEmpty()) {
            this.rolledItemOutputs.add(RollInformation.of(output, rollValue, rollBoost));
        }
        return (R) this;
    }

    public R outputs(ItemStack... outputs) {
        return outputs(Arrays.asList(outputs));
    }

    public R outputsRolled(long rollValue, long rollBoost, ItemStack... outputs) {
        return outputsRolled(Arrays.asList(outputs), rollValue, rollBoost);
    }

    public R outputs(Collection<ItemStack> outputs) {
        outputs = new ArrayList<>(outputs);
        outputs.removeIf(stack -> stack == null || stack.isEmpty());
        this.itemOutputs.addAll(outputs);
        return (R) this;
    }

    public R outputsRolled(Collection<ItemStack> outputs, long rollValue, long rollBoost) {
        for (ItemStack stack : outputs) {
            if (stack == null || stack.isEmpty()) continue;
            this.rolledItemOutputs.add(RollInformation.of(stack, rollValue, rollBoost));
        }
        return (R) this;
    }

    public R setItemOutputInterpreter(@NotNull RollInterpreter itemOutputInterpreter) {
        this.itemOutputInterpreter = itemOutputInterpreter;
        return (R) this;
    }

    public R setItemOutputOverride(@Nullable ItemOutputProvider itemOutputOverride) {
        this.itemOutputOverride = itemOutputOverride;
        return (R) this;
    }

    public R clearOutputs() {
        this.itemOutputs.clear();
        this.rolledItemOutputs.clear();
        return (R) this;
    }

    /////////////////////
    // ITEM OUTPUT END //
    /////////////////////

    ////////////////////////
    // FLUID OUTPUT START //
    ////////////////////////

    public R outputFluid(Fluid fluid, int amount) {
        return fluidOutputs(new FluidStack(fluid, amount));
    }

    public R outputFluidRoll(Fluid fluid, int amount, long rollValue, long rollBoost) {
        return fluidOutputsRolled(rollValue, rollBoost, new FluidStack(fluid, amount));
    }

    public R outputFluid(Fluid fluid, int amount, NBTTagCompound tag) {
        FluidStack stack = new FluidStack(fluid, amount);
        stack.tag = tag;
        return fluidOutputs(stack);
    }

    public R outputFluidRoll(Fluid fluid, int amount, long rollValue, long rollBoost, NBTTagCompound tag) {
        FluidStack stack = new FluidStack(fluid, amount);
        stack.tag = tag;
        return fluidOutputsRolled(rollValue, rollBoost, stack);
    }

    public R fluidOutputs(FluidStack output) {
        if (output != null && output.amount > 0) {
            this.fluidOutputs.add(output);
        }
        return (R) this;
    }

    public R fluidOutputsRolled(long rollValue, long rollBoost, FluidStack output) {
        if (output != null && output.amount > 0) {
            this.rolledFluidOutputs.add(RollInformation.of(output, rollValue, rollBoost));
        }
        return (R) this;
    }

    public R fluidOutputs(FluidStack... outputs) {
        return fluidOutputs(Arrays.asList(outputs));
    }

    public R fluidOutputsRolled(long rollValue, long rollBoost, FluidStack... outputs) {
        return fluidOutputsRolled(Arrays.asList(outputs), rollValue, rollBoost);
    }

    public R fluidOutputs(Collection<FluidStack> outputs) {
        outputs = new ArrayList<>(outputs);
        outputs.removeIf(o -> o == null || o.amount <= 0);
        this.fluidOutputs.addAll(outputs);
        return (R) this;
    }

    public R fluidOutputsRolled(Collection<FluidStack> outputs, long rollValue, long rollBoost) {
        for (FluidStack stack : outputs) {
            if (stack == null || stack.amount <= 0) continue;
            this.rolledFluidOutputs.add(RollInformation.of(stack, rollValue, rollBoost));
        }
        return (R) this;
    }

    public R setFluidOutputInterpreter(@NotNull RollInterpreter fluidOutputInterpreter) {
        this.fluidOutputInterpreter = fluidOutputInterpreter;
        return (R) this;
    }

    public R setFluidOutputOverride(@Nullable FluidOutputProvider fluidOutputOverride) {
        this.fluidOutputOverride = fluidOutputOverride;
        return (R) this;
    }

    public R clearFluidOutputs() {
        this.fluidOutputs.clear();
        this.rolledFluidOutputs.clear();
        return (R) this;
    }

    //////////////////////
    // FLUID OUTPUT END //
    //////////////////////

    // Deprecated chance methods //

    /**
     * @deprecated use {@link #outputsRolled(long, long, ItemStack)} instead, and refer to changes in how chance works.
     */
    @Deprecated
    public R chancedOutput(ItemStack stack, int chance, int tierChanceBoost) {
        return outputsRolled(chance, tierChanceBoost, stack);
    }

    /**
     * @deprecated use {@link #outputItemRoll(OrePrefix, Material, int, long, long)} instead, and refer to changes in
     *             how chance works.
     */
    @Deprecated
    public R chancedOutput(OrePrefix prefix, Material material, int count, int chance, int tierChanceBoost) {
        return outputItemRoll(prefix, material, count, chance, tierChanceBoost);
    }

    /**
     * @deprecated use {@link #outputItemRoll(OrePrefix, Material, long, long)} instead, and refer to changes in how
     *             chance works.
     */
    @Deprecated
    public R chancedOutput(OrePrefix prefix, Material material, int chance, int tierChanceBoost) {
        return outputItemRoll(prefix, material, chance, tierChanceBoost);
    }

    /**
     * @deprecated use {@link #outputItemRoll(IHasStackForm, int, long, long)} instead, and refer to changes in how
     *             chance works.
     */
    @Deprecated
    public R chancedOutput(MetaItem<?>.MetaValueItem item, int count, int chance, int tierChanceBoost) {
        return outputItemRoll(item, count, chance, tierChanceBoost);
    }

    /**
     * @deprecated use {@link #outputItemRoll(IHasStackForm, long, long)} instead, and refer to changes in how chance
     *             works.
     */
    @Deprecated
    public R chancedOutput(MetaItem<?>.MetaValueItem item, int chance, int tierChanceBoost) {
        return outputItemRoll(item, chance, tierChanceBoost);
    }

    /**
     * @deprecated use {@link #fluidOutputsRolled(long, long, FluidStack)} instead, and refer to changes in how chance
     *             works.
     */
    @Deprecated
    public R chancedFluidOutput(FluidStack stack, int chance, int tierChanceBoost) {
        return fluidOutputsRolled(chance, tierChanceBoost, stack);
    }

    // ------------------------- //

    @Optional.Method(modid = Mods.Names.GROOVY_SCRIPT)
    public R inputs(IIngredient ingredient) {
        if (ingredient instanceof OreDictIngredient dict) {
            return inputItem(dict.getOreDict(), dict.getAmount());
        }
        // noinspection ConstantValue
        if ((Object) ingredient instanceof ItemStack s) {
            return inputs(s);
        } else if (ingredient instanceof FluidStack s) {
            return fluidInputs(s);
        }
        throw new IllegalArgumentException("Could not add groovy ingredient " + ingredient + " to recipe!");
    }

    @Optional.Method(modid = Mods.Names.GROOVY_SCRIPT)
    public R inputs(IIngredient... ingredients) {
        for (IIngredient ingredient : ingredients) {
            inputs(ingredient);
        }
        return (R) this;
    }

    @Optional.Method(modid = Mods.Names.GROOVY_SCRIPT)
    public R inputs(Collection<IIngredient> ingredients) {
        for (IIngredient ingredient : ingredients) {
            inputs(ingredient);
        }
        return (R) this;
    }

    @Optional.Method(modid = Mods.Names.GROOVY_SCRIPT)
    public R notConsumable(IIngredient ingredient) {
        if (ingredient instanceof OreDictIngredient dict) {
            return inputItemRoll(dict.getOreDict(), dict.getAmount(), Long.MIN_VALUE, 0);
        }
        // noinspection ConstantValue
        if ((Object) ingredient instanceof ItemStack s) {
            return inputsRolled(Long.MIN_VALUE, 0, s);
        } else if (ingredient instanceof FluidStack s) {
            return fluidInputsRolled(Long.MIN_VALUE, 0, s);
        }
        throw new IllegalArgumentException("Could not add groovy ingredient " + ingredient + " to recipe!");
    }

    public R duration(int duration) {
        this.duration = duration;
        return (R) this;
    }

    /**
     * @deprecated use {@link #volts(long)} instead.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public R EUt(long EUt) {
        return volts(EUt);
    }

    public R volts(long voltage) {
        PowerPropertyData generation = this.getProperty(PowerGenerationProperty.getInstance(), null);
        if (generation != null) {
            generation.setVoltage(voltage);
            return (R) this;
        }
        PowerPropertyData existing = this.getProperty(PowerUsageProperty.getInstance(), null);
        if (existing == null) {
            this.applyProperty(PowerUsageProperty.getInstance(), new PowerPropertyData(voltage));
        } else {
            existing.setVoltage(voltage);
        }
        return (R) this;
    }

    public R amps(long amperage) {
        PowerPropertyData generation = this.getProperty(PowerGenerationProperty.getInstance(), null);
        if (generation != null) {
            generation.setAmperage(amperage);
            return (R) this;
        }
        PowerPropertyData existing = this.getProperty(PowerUsageProperty.getInstance(), null);
        if (existing == null) {
            this.applyProperty(PowerUsageProperty.getInstance(), new PowerPropertyData(1, amperage));
        } else {
            existing.setAmperage(amperage);
        }
        return (R) this;
    }

    public R setGenerating() {
        PowerPropertyData existing = this.getProperty(PowerUsageProperty.getInstance(), null);
        if (existing == null) {
            this.applyProperty(PowerGenerationProperty.getInstance(), new PowerPropertyData(1, 1));
        } else {
            this.applyProperty(PowerGenerationProperty.getInstance(), existing);
            this.recipePropertyStorage.remove(PowerUsageProperty.getInstance());
        }
        return (R) this;
    }

    /**
     * Only use if you absolutely don't want the recipe to be run through any build actions.
     * Instead, you should blacklist specific actions with {@link #ignoreBuildAction(ResourceLocation)}
     */
    public R ignoreAllBuildActions() {
        this.ignoreAllBuildActions = true;
        return (R) this;
    }

    public R ignoreBuildAction(ResourceLocation buildActionName) {
        if (ignoredBuildActions == null) {
            ignoredBuildActions = new Object2ObjectOpenHashMap<>();
        } else if (!recipeMap.getBuildActions().containsKey(buildActionName)) {
            GTLog.logger.error("Recipe map {} does not contain build action {}!", recipeMap, buildActionName,
                    new Throwable());
            return (R) this;
        } else if (ignoredBuildActions.containsKey(buildActionName)) {
            return (R) this;
        }

        ignoredBuildActions.put(buildActionName, recipeMap.getBuildActions().get(buildActionName));

        return (R) this;
    }

    public boolean ignoresAllBuildActions() {
        return ignoreAllBuildActions;
    }

    /**
     * Get all ignored build actions for the recipe map.
     *
     * @return A map of ignored build actions.
     */
    public @NotNull Map<ResourceLocation, RecipeBuildAction<R>> getIgnoredBuildActions() {
        if (ignoreAllBuildActions) {
            return recipeMap.getBuildActions();
        }

        if (ignoredBuildActions == null) {
            return Collections.emptyMap();
        }

        return ignoredBuildActions;
    }

    public R hidden() {
        this.hidden = true;
        return (R) this;
    }

    public R category(@NotNull GTRecipeCategory category) {
        this.category = category;
        return (R) this;
    }

    public R isCTRecipe() {
        this.isCTRecipe = true;
        return (R) this;
    }

    public R setRecipeMap(RecipeMap<R> recipeMap) {
        this.recipeMap = recipeMap;
        return (R) this;
    }

    public R copy() {
        return (R) new RecipeBuilder<>(this);
    }

    /**
     * Only use if you absolutely don't want the recipe to be run through any build actions.
     * Instead, you should blacklist specific actions with {@link #ignoreBuildAction(ResourceLocation)}
     */
    public R ignoreAllBuildActions() {
        this.ignoreAllBuildActions = true;
        return (R) this;
    }

    public R ignoreBuildAction(ResourceLocation buildActionName) {
        if (ignoredBuildActions == null) {
            ignoredBuildActions = new Object2ObjectOpenHashMap<>();
        } else if (!recipeMap.getBuildActions().containsKey(buildActionName)) {
            GTLog.logger.error("Recipe map {} does not contain build action {}!", recipeMap, buildActionName,
                    new Throwable());
            return (R) this;
        } else if (ignoredBuildActions.containsKey(buildActionName)) {
            return (R) this;
        }

        ignoredBuildActions.put(buildActionName, recipeMap.getBuildActions().get(buildActionName));

        return (R) this;
    }

    public ValidationResult<Recipe> build() {
        EnumValidationResult result = recipePropertyStorage == RecipePropertyStorage.EMPTY ?
                EnumValidationResult.INVALID : validate();
        return ValidationResult.newResult(result, new Recipe(
                new ListWithRollInformation<>(GTItemIngredient::getRequiredCount, itemInputs, rolledItemInputs,
                        itemInputInterpreter),
                new ListWithRollInformation<>(GTFluidIngredient::getRequiredCount, fluidInputs, rolledFluidInputs,
                        fluidInputInterpreter),
                resolveItemOutputProvider(),
                resolveFluidOutputProvider(),
                recipePropertyStorage, duration, hidden, isCTRecipe, category));
    }

    protected ItemOutputProvider resolveItemOutputProvider() {
        return itemOutputOverride != null ? itemOutputOverride :
                new StandardItemOutput(new ListWithRollInformation<>(ItemStack::getCount, itemOutputs,
                        rolledItemOutputs, itemOutputInterpreter));
    }

    protected FluidOutputProvider resolveFluidOutputProvider() {
        return fluidOutputOverride != null ? fluidOutputOverride : new StandardFluidOutput(
                new ListWithRollInformation<>(f -> f.amount, fluidOutputs, rolledFluidOutputs, fluidOutputInterpreter));
    }

    protected EnumValidationResult validate() {
        if (GroovyScriptModule.isCurrentlyRunning()) {
            GroovyLog.Msg msg = GroovyLog.msg("Error adding GregTech " + recipeMap.unlocalizedName + " recipe").error();
            validateGroovy(msg);
            return msg.postIfNotEmpty() ? EnumValidationResult.SKIP : EnumValidationResult.VALID;
        }
        if (duration <= 0) {
            GTLog.logger.error("Duration cannot be less or equal to 0", new Throwable());
            if (isCTRecipe) {
                CraftTweakerAPI.logError("Duration cannot be less or equal to 0", new Throwable());
            }
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (recipeMap != null) { // recipeMap can be null in tests
            if (category == null) {
                GTLog.logger.error("Recipes must have a category", new Throwable());
                if (isCTRecipe) {
                    CraftTweakerAPI.logError("Recipes must have a category", new Throwable());
                }
                recipeStatus = EnumValidationResult.INVALID;
            } else if (category.getRecipeMap() != this.recipeMap) {
                GTLog.logger.error("Cannot apply Category with incompatible RecipeMap", new Throwable());
                if (isCTRecipe) {
                    CraftTweakerAPI.logError("Cannot apply Category with incompatible RecipeMap",
                            new Throwable());
                }
                recipeStatus = EnumValidationResult.INVALID;
            }
        }
        if (recipeStatus == EnumValidationResult.INVALID) {
            GTLog.logger.error("Invalid recipe, read the errors above: {}", this);
        }
        return recipeStatus;
    }

    @Optional.Method(modid = Mods.Names.GROOVY_SCRIPT)
    protected void validateGroovy(GroovyLog.Msg errorMsg) {
        errorMsg.add(duration <= 0, () -> "Duration must not be less or equal to 0");
        int maxInput = recipeMap.getMaxInputs();
        int maxOutput = recipeMap.getMaxOutputs();
        int maxFluidInput = recipeMap.getMaxFluidInputs();
        int maxFluidOutput = recipeMap.getMaxFluidOutputs();
        errorMsg.add(itemInputs.size() > maxInput, () -> getRequiredString(maxInput, itemInputs.size(), "item input"));
        errorMsg.add(
                itemOutputs.size() > maxOutput, () -> getRequiredString(maxOutput, itemOutputs.size(), "item output"));
        errorMsg.add(fluidInputs.size() > maxFluidInput,
                () -> getRequiredString(maxFluidInput, fluidInputs.size(), "fluid input"));
        errorMsg.add(fluidOutputs.size() > maxFluidOutput,
                () -> getRequiredString(maxFluidOutput, fluidOutputs.size(), "fluid output"));
    }

    @NotNull
    protected static String getRequiredString(int max, int actual, @NotNull String type) {
        if (max <= 0) {
            return "No " + type + "s allowed, but found " + actual;
        }
        String out = "Must have at most " + max + " " + type;
        if (max != 1) {
            out += "s";
        }
        out += ", but found " + actual;
        return out;
    }

    /**
     * Build and register the recipe, if valid.
     * <strong>Do not call outside of the
     * {@link net.minecraftforge.event.RegistryEvent.Register<net.minecraft.item.crafting.IRecipe>} event for recipes.
     * </strong>
     */
    @MustBeInvokedByOverriders
    public void buildAndRegister() {
        if (!ignoreAllBuildActions) {
            for (Map.Entry<ResourceLocation, RecipeBuildAction<R>> buildAction : recipeMap.getBuildActions()
                    .entrySet()) {
                if (ignoredBuildActions != null && ignoredBuildActions.containsKey(buildAction.getKey())) {
                    continue;
                }
                buildAction.getValue().accept((R) this);
            }
        }
        ValidationResult<Recipe> validationResult = build();
        recipeMap.addRecipe(validationResult);
    }

    /////////////
    // Getters //
    /////////////

    public @NotNull List<GTItemIngredient> getItemInputs() {
        return itemInputs;
    }

    public @NotNull List<RollInformation<GTItemIngredient>> getRolledItemInputs() {
        return rolledItemInputs;
    }

    public @NotNull List<ItemStack> getItemOutputs() {
        return itemOutputs;
    }

    public @NotNull List<RollInformation<ItemStack>> getRolledItemOutputs() {
        return rolledItemOutputs;
    }

    public @NotNull List<GTFluidIngredient> getFluidInputs() {
        return fluidInputs;
    }

    public @NotNull List<RollInformation<GTFluidIngredient>> getRolledFluidInputs() {
        return rolledFluidInputs;
    }

    public @NotNull List<FluidStack> getFluidOutputs() {
        return fluidOutputs;
    }

    public @NotNull List<RollInformation<FluidStack>> getRolledFluidOutputs() {
        return rolledFluidOutputs;
    }

    /**
     * @deprecated use {@link #getVoltage()} instead.
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public long getEUt() {
        return getVoltage();
    }

    public long getVoltage() {
        PowerPropertyData generation = this.getProperty(PowerGenerationProperty.getInstance(), null);
        if (generation != null) return generation.getVoltage();
        else return this.getProperty(PowerUsageProperty.getInstance(), PowerPropertyData.EMPTY).getVoltage();
    }

    public long getAmperage() {
        PowerPropertyData generation = this.getProperty(PowerGenerationProperty.getInstance(), null);
        if (generation != null) return generation.getVoltage();
        else return this.getProperty(PowerUsageProperty.getInstance(), PowerPropertyData.EMPTY).getVoltage();
    }

    public int getDuration() {
        return duration;
    }

    public @Nullable CleanroomType getCleanroom() {
        return this.recipePropertyStorage.get(CleanroomProperty.getInstance(), null);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("recipeMap", recipeMap)
                .append("inputs", itemInputs)
                .append("outputs", itemOutputs)
                .append("chancedOutputs", rolledItemOutputs)
                .append("chancedFluidOutputs", rolledFluidOutputs)
                .append("fluidInputs", fluidInputs)
                .append("fluidOutputs", fluidOutputs)
                .append("duration", duration)
                .append("hidden", hidden)
                .append("cleanroom", getCleanroom())
                .append("dimensions", getDimensionIDs().toString())
                .append("dimensions_blocked", getBlockedDimensionIDs().toString())
                .append("recipeStatus", recipeStatus)
                .append("ignoresBuildActions", ignoresAllBuildActions())
                .append("ignoredBuildActions", getIgnoredBuildActions())
                .toString();
    }
}
