package gregtech.api.recipes;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.recipes.recipes.DummyRecipe;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.DummyContainer;
import gregtech.api.util.GTLog;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.world.DummyWorld;
import gregtech.common.ConfigHolder;
import gregtech.common.crafting.FluidReplaceRecipe;
import gregtech.common.crafting.GTShapedOreRecipe;
import gregtech.common.crafting.GTShapelessOreRecipe;
import gregtech.common.crafting.ShapedOreEnergyTransferRecipe;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import crafttweaker.mc1120.actions.ActionAddFurnaceRecipe;
import crafttweaker.mc1120.furnace.MCFurnaceManager;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ModHandler {

    public static final boolean ERROR_ON_INVALID_RECIPE = GTValues.isDeobfEnvironment() ||
            !ConfigHolder.misc.ignoreErrorOrInvalidRecipes;
    public static boolean hasInvalidRecipe = false;

    private ModHandler() {}

    public static void postInit() {
        if (ERROR_ON_INVALID_RECIPE && hasInvalidRecipe) {
            throw new IllegalStateException("Invalid Recipes Found. See earlier log entries for details.");
        }
    }

    /**
     * @param material the material to check
     * @return if the material is a wood
     */
    public static boolean isMaterialWood(@Nullable Material material) {
        return material != null && material.hasProperty(PropertyKey.WOOD);
    }

    // Furnace Smelting

    /**
     * @param fuelStack the stack to check
     * @return the remainder item for a burnt fuel in a boiler
     */
    public static ItemStack getBurningFuelRemainder(ItemStack fuelStack) {
        float remainderChance;
        ItemStack remainder;
        if (OreDictUnifier.hasOreDictionary(fuelStack, "fuelCoke")) {
            remainder = OreDictUnifier.get(OrePrefix.dust, Materials.Ash);
            remainderChance = 0.5f;
        } else {
            MaterialStack materialStack = OreDictUnifier.getMaterial(fuelStack);
            if (materialStack == null)
                return ItemStack.EMPTY;
            else if (materialStack.material == Materials.Charcoal) {
                remainder = OreDictUnifier.get(OrePrefix.dust, Materials.Ash);
                remainderChance = 0.3f;
            } else if (materialStack.material == Materials.Coal) {
                remainder = OreDictUnifier.get(OrePrefix.dust, Materials.DarkAsh);
                remainderChance = 0.35f;
            } else if (materialStack.material == Materials.Coke) {
                remainder = OreDictUnifier.get(OrePrefix.dust, Materials.Ash);
                remainderChance = 0.5f;
            } else return ItemStack.EMPTY;
        }
        return GTValues.RNG.nextFloat() <= remainderChance ? remainder : ItemStack.EMPTY;
    }

    /**
     * Add a smelting recipe for all valid items in a unification entry
     *
     * @param input  the unification entry to input
     * @param output the output of the recipe
     */
    public static void addSmeltingRecipe(@NotNull UnificationEntry input, @NotNull ItemStack output) {
        addSmeltingRecipe(input, output, 0.0F);
    }

    /**
     * @param input  the input of the recipe
     * @param output the output of the recipe
     */
    public static void addSmeltingRecipe(@NotNull ItemStack input, @NotNull ItemStack output) {
        addSmeltingRecipe(input, output, 0.0F);
    }

    /**
     * Add a smelting recipe for all valid items in a unification entry
     *
     * @param input      the unification entry to input
     * @param output     the output of the recipe
     * @param experience the experience of the recipe
     */
    public static void addSmeltingRecipe(@NotNull UnificationEntry input, @NotNull ItemStack output, float experience) {
        for (ItemStack inputStack : OreDictUnifier.getAll(input)) {
            addSmeltingRecipe(inputStack, output, experience);
        }
    }

    /**
     * Add a furnace smelting recipe
     *
     * @param input      the input of the recipe
     * @param output     the output of the recipe
     * @param experience the experience of the recipe
     */
    public static void addSmeltingRecipe(@NotNull ItemStack input, @NotNull ItemStack output, float experience) {
        if (input.isEmpty() && setErroredInvalidRecipe("Furnace Recipe Input cannot be an empty ItemStack")) {
            return;
        }
        if (output.isEmpty() && setErroredInvalidRecipe("Furnace Recipe Output cannot be an empty ItemStack")) {
            return;
        }

        FurnaceRecipes recipes = FurnaceRecipes.instance();
        if (recipes.getSmeltingResult(input).isEmpty()) {
            // register only if there is no recipe with duplicate input
            recipes.addSmeltingRecipe(input, output, experience);
        } else {
            logInvalidRecipe(String.format("Tried to register duplicate Furnace Recipe: %sx %s:%s -> %sx %s:%s, %sexp",
                    input.getCount(), Objects.requireNonNull(input.getItem().getRegistryName()).getNamespace(),
                    input.getDisplayName(),
                    output.getCount(), Objects.requireNonNull(output.getItem().getRegistryName()).getNamespace(),
                    output.getDisplayName(), experience));
        }
    }

    /**
     * @param input the input for the recipe
     * @return the output of the recipe
     */
    @NotNull
    public static ItemStack getSmeltingOutput(@NotNull ItemStack input) {
        if (input.isEmpty()) return ItemStack.EMPTY;
        return OreDictUnifier.getUnificated(FurnaceRecipes.instance().getSmeltingResult(input));
    }

    // Crafting Recipes

    /**
     * Adds Shaped Crafting Recipes.
     * <p/>
     * {@link MetaItem.MetaValueItem}'s are converted to ItemStack via {@link MetaItem.MetaValueItem#getStackForm()}
     * method.
     * <p/>
     * For Enums - {@link Enum#name()} is called.
     * <p/>
     * For {@link UnificationEntry} - {@link UnificationEntry#toString()} is called.
     * <p/>
     * For Lowercase Characters - gets IGTool from {@link ToolHelper#getToolFromSymbol(Character)}, and calls
     * {@link IGTTool#getOreDictName()}
     * <p/>
     * Base tool names are as follows:
     * <ul>
     * <li>{@code 'c'} - {@code craftingToolCrowbar}</li>
     * <li>{@code 'd'} - {@code craftingToolScrewdriver}</li>
     * <li>{@code 'f'} - {@code craftingToolFile}</li>
     * <li>{@code 'h'} - {@code craftingToolHardHammer}</li>
     * <li>{@code 'k'} - {@code craftingToolKnife}</li>
     * <li>{@code 'm'} - {@code craftingToolMortar}</li>
     * <li>{@code 'r'} - {@code craftingToolSoftHammer}</li>
     * <li>{@code 's'} - {@code craftingToolSaw}</li>
     * <li>{@code 'w'} - {@code craftingToolWrench}</li>
     * <li>{@code 'x'} - {@code craftingToolWireCutter}</li>
     * </ul>
     *
     * @param regName the registry name for the recipe
     * @param result  the output for the recipe
     * @param recipe  the contents of the recipe
     */
    public static void addShapedRecipe(@NotNull String regName, @NotNull ItemStack result, @NotNull Object... recipe) {
        addShapedRecipe(false, regName, result, false, false, recipe);
    }

    /**
     * Adds a shaped recipe with a single fluid container, which gets consumed as input and is output with different
     * contents
     *
     * @see ModHandler#addShapedRecipe(String, ItemStack, Object...)
     */
    public static void addFluidReplaceRecipe(String regName, ItemStack result, Object... recipe) {
        addFluidReplaceRecipe(regName, result, false, recipe);
    }

    /**
     * Adds a shaped recipe which clears the nbt of the outputs
     *
     * @see ModHandler#addShapedRecipe(String, ItemStack, Object...)
     */
    public static void addShapedNBTClearingRecipe(String regName, ItemStack result, Object... recipe) {
        addShapedRecipe(false, regName, result, true, false, recipe);
    }

    /**
     * Adds a shaped recipe which sets the {@link UnificationEntry} for the output according to the crafting inputs
     *
     * @param withUnificationData whether to use unification data
     * @see ModHandler#addShapedRecipe(String, ItemStack, Object...)
     */
    public static void addShapedRecipe(boolean withUnificationData, String regName, ItemStack result,
                                       Object... recipe) {
        addShapedRecipe(withUnificationData, regName, result, false, false, recipe);
    }

    /**
     * Adds a mirrored shaped recipe
     *
     * @see ModHandler#addShapedRecipe(String, ItemStack, Object...)
     */
    public static void addMirroredShapedRecipe(String regName, ItemStack result, Object... recipe) {
        addShapedRecipe(false, regName, result, false, true, recipe);
    }

    /**
     * @param withUnificationData whether to use unification data
     * @param isNBTClearing       whether to clear output nbt
     * @param isMirrored          whether the recipe should be mirrored
     * @see ModHandler#addShapedRecipe(String, ItemStack, Object...)
     */
    public static void addShapedRecipe(boolean withUnificationData, @NotNull String regName, @NotNull ItemStack result,
                                       boolean isNBTClearing, boolean isMirrored, @NotNull Object... recipe) {
        if (!validateRecipeWithOutput(regName, result, recipe)) return;

        addRecipe(regName, result, isNBTClearing, isMirrored, recipe);

        if (withUnificationData) {
            OreDictUnifier.registerOre(result, RecyclingHandler.getRecyclingIngredients(result.getCount(), recipe));
        }
    }

    /**
     * @see ModHandler#addFluidReplaceRecipe(String, ItemStack, Object...)
     */
    public static void addFluidReplaceRecipe(String regName, ItemStack result, boolean isNBTClearing,
                                             Object... recipe) {
        if (!validateRecipeWithOutput(regName, result, recipe)) return;

        IRecipe shapedOreRecipe = new FluidReplaceRecipe(isNBTClearing, null, result.copy(),
                finalizeShapedRecipeInput(recipe))
                        .setMirrored(false) // make all recipes not mirrored by default
                        .setRegistryName(regName);

        registerRecipe(shapedOreRecipe);
    }

    /**
     * @param chargePredicate   the predicate for charging the output
     * @param overrideCharge    whether to override the energy amount
     * @param transferMaxCharge whether to transfer all the potential charge
     * @see ModHandler#addShapedRecipe(String, ItemStack, Object...)
     */
    public static void addShapedEnergyTransferRecipe(String regName, ItemStack result,
                                                     Predicate<ItemStack> chargePredicate, boolean overrideCharge,
                                                     boolean transferMaxCharge, Object... recipe) {
        if (!validateRecipeWithOutput(regName, result, recipe)) return;

        IRecipe shapedOreRecipe = new ShapedOreEnergyTransferRecipe(null, result.copy(), chargePredicate,
                overrideCharge, transferMaxCharge, finalizeShapedRecipeInput(recipe))
                        .setMirrored(false)
                        .setRegistryName(regName);

        registerRecipe(shapedOreRecipe);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean validateRecipeWithOutput(@NotNull String regName, @NotNull ItemStack result,
                                                    @NotNull Object... recipe) {
        if (result.isEmpty()) {
            if (setErroredInvalidRecipe("Recipe output cannot be an empty ItemStack. Recipe: " + regName)) return false;
        }
        return validateRecipe(regName, recipe);
    }

    private static void addRecipe(@NotNull String regName, @NotNull ItemStack result, boolean isNBTClearing,
                                  boolean isMirrored, @NotNull Object... recipe) {
        IRecipe shapedOreRecipe = new GTShapedOreRecipe(isNBTClearing, null, result.copy(),
                finalizeShapedRecipeInput(recipe))
                        .setMirrored(isMirrored)
                        .setRegistryName(regName);

        registerRecipe(shapedOreRecipe);
    }

    private static void registerRecipe(@NotNull IRecipe recipe) {
        ForgeRegistries.RECIPES.register(recipe);
    }

    /**
     * @param regName the name of the recipe
     * @param recipe  the recipe to validate
     * @return if the recipe should be skipped
     */
    private static boolean validateRecipe(String regName, Object... recipe) {
        if (recipe == null) {
            return !setErroredInvalidRecipe("Recipe cannot be null");
        } else if (recipe.length == 0) {
            return !setErroredInvalidRecipe("Recipe cannot be empty");
        } else if (Arrays.asList(recipe).contains(null) || Arrays.asList(recipe).contains(ItemStack.EMPTY)) {
            String recipeMessage = Arrays.stream(recipe)
                    .map(o -> o == null ? "NULL" : o)
                    .map(o -> o == ItemStack.EMPTY ? "EMPTY STACK" : o)
                    .map(Object::toString)
                    .map(s -> "\"" + s + "\"")
                    .collect(Collectors.joining(", "));
            return !setErroredInvalidRecipe(
                    "Recipe cannot contain null elements or Empty ItemStacks. Recipe: " + recipeMessage);
        } else {
            ModContainer container = Loader.instance().activeModContainer();
            if (ForgeRegistries.RECIPES.containsKey(new ResourceLocation(
                    container == null ? GTValues.MODID : container.getModId().toLowerCase(), regName))) {
                String recipeMessage = Arrays.stream(recipe)
                        .map(Object::toString)
                        .map(s -> "\"" + s + "\"")
                        .collect(Collectors.joining(", "));
                logInvalidRecipe(
                        "Tried to register recipe, " + regName + ", with duplicate key. Recipe: " + recipeMessage);
                return false;
            }
        }
        return true;
    }

    /**
     * @param recipe the recipe to finalize
     * @return the finalized recipe
     */
    public static Object @NotNull [] finalizeShapedRecipeInput(Object... recipe) {
        for (byte i = 0; i < recipe.length; i++) {
            recipe[i] = finalizeIngredient(recipe[i]);
        }
        int idx = 0;
        Collection<Object> recipeList = new ArrayList<>(Arrays.asList(recipe));

        while (recipe[idx] instanceof String) {
            StringBuilder s = new StringBuilder((String) recipe[idx++]);
            while (s.length() < 3) s.append(" ");
            if (s.length() > 3) throw new IllegalArgumentException("Recipe row cannot be larger than 3. Index: " + idx);
            for (char c : s.toString().toCharArray()) {
                IGTTool tool = ToolHelper.getToolFromSymbol(c);
                if (tool != null && tool.getOreDictName() != null) {
                    recipeList.add(c);
                    recipeList.add(tool.getOreDictName());
                }
            }
        }
        return recipeList.toArray();
    }

    /**
     * @param ingredient the ingredient to finalize
     * @return the finalized ingredient
     */
    @NotNull
    public static Object finalizeIngredient(@NotNull Object ingredient) {
        if (ingredient instanceof MetaItem.MetaValueItem metaValueItem) {
            ingredient = metaValueItem.getStackForm();
        } else if (ingredient instanceof Enum anEnum) {
            ingredient = anEnum.name();
        } else if (ingredient instanceof OrePrefix orePrefix) {
            ingredient = orePrefix.name();
        } else if (ingredient instanceof UnificationEntry entry) {
            if (ConfigHolder.misc.debug && entry.material != null && !entry.orePrefix.isIgnored(entry.material) &&
                    !entry.orePrefix.doGenerateItem(entry.material)) {
                logInvalidRecipe("Attempted to create recipe for invalid/missing Unification Entry " + ingredient);
            }
            ingredient = ingredient.toString();
        } else if (!(ingredient instanceof ItemStack || ingredient instanceof Item || ingredient instanceof Block ||
                ingredient instanceof String || ingredient instanceof Character || ingredient instanceof Boolean ||
                ingredient instanceof Ingredient)) {
                    throw new IllegalArgumentException(
                            ingredient.getClass().getSimpleName() + " type is not suitable for crafting input.");
                }
        return ingredient;
    }

    /**
     * @param outputCount the amount of outputs the recipe has
     * @param recipe      the recipe to retrieve from
     * @return the recycling ingredients for a recipe
     * @deprecated Use {@link RecyclingHandler#getRecyclingIngredients(int, Object...)}. Will be removed in 2.9
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    @Nullable
    public static ItemMaterialInfo getRecyclingIngredients(int outputCount, @NotNull Object... recipe) {
        return RecyclingHandler.getRecyclingIngredients(outputCount, recipe);
    }

    /**
     * Add a shapeless recipe
     *
     * @param regName the registry name of the recipe
     * @param result  the output of the recipe
     * @param recipe  the recipe to add
     */
    public static void addShapelessRecipe(@NotNull String regName, @NotNull ItemStack result,
                                          @NotNull Object... recipe) {
        addShapelessRecipe(regName, result, false, recipe);
    }

    /**
     * Adds a shapeless recipe which clears the nbt of the outputs
     *
     * @see ModHandler#addShapelessRecipe(String, ItemStack, boolean, Object...)
     */
    public static void addShapelessNBTClearingRecipe(@NotNull String regName, @NotNull ItemStack result,
                                                     @NotNull Object... recipe) {
        addShapelessRecipe(regName, result, true, recipe);
    }

    /**
     * Add a shapeless recipe
     *
     * @param isNBTClearing if the recipe should clear the nbt of the outputs
     * @see ModHandler#addShapelessRecipe(String, ItemStack, Object...)
     */
    public static void addShapelessRecipe(String regName, ItemStack result, boolean isNBTClearing, Object... recipe) {
        if (!validateRecipeWithOutput(regName, result, recipe)) return;

        for (byte i = 0; i < recipe.length; i++) {
            if (recipe[i] instanceof MetaItem.MetaValueItem) {
                recipe[i] = ((MetaItem<?>.MetaValueItem) recipe[i]).getStackForm();
            } else if (recipe[i] instanceof Enum) {
                recipe[i] = ((Enum<?>) recipe[i]).name();
            } else if (recipe[i] instanceof OrePrefix) {
                recipe[i] = ((OrePrefix) recipe[i]).name();
            } else if (recipe[i] instanceof UnificationEntry) {
                recipe[i] = recipe[i].toString();
            } else if (recipe[i] instanceof Character) {
                IGTTool tool = ToolHelper.getToolFromSymbol((char) recipe[i]);
                if (tool == null || tool.getOreDictName() == null) {
                    throw new IllegalArgumentException("Tool name is not found for char " + recipe[i]);
                }
                recipe[i] = tool.getOreDictName();
            } else if (!(recipe[i] instanceof ItemStack || recipe[i] instanceof Item || recipe[i] instanceof Block ||
                    recipe[i] instanceof String)) {
                        throw new IllegalArgumentException(
                                recipe.getClass().getSimpleName() + " type is not suitable for crafting input.");
                    }
        }
        IRecipe shapelessRecipe = new GTShapelessOreRecipe(isNBTClearing, null, result.copy(), recipe)
                .setRegistryName(regName);

        try {
            // workaround for MC bug that makes all shaped recipe inputs that have enchanted items
            // or renamed ones on input fail, even if all ingredients match it
            Field field = ShapelessOreRecipe.class.getDeclaredField("isSimple");
            field.setAccessible(true);
            field.setBoolean(shapelessRecipe, false);
        } catch (ReflectiveOperationException exception) {
            GTLog.logger.error("Failed to mark shapeless recipe as complex", exception);
        }

        registerRecipe(shapelessRecipe);
    }

    // recipe removal

    /**
     * Remove all recipes matching a unification entry as input
     *
     * @param input the input to match
     * @return if a recipe was removed
     */
    @SuppressWarnings("unused")
    public static boolean removeFurnaceSmelting(@NotNull UnificationEntry input) {
        boolean result = false;
        for (ItemStack inputStack : OreDictUnifier.getAll(input)) {
            result = result || removeFurnaceSmelting(inputStack);
        }
        return result;
    }

    /**
     * Remove a Smelting Recipe by input
     *
     * @param input the input to remove by
     * @return if the recipe was removed
     */
    public static boolean removeFurnaceSmelting(@NotNull ItemStack input) {
        if (input.isEmpty()) {
            if (setErroredInvalidRecipe("Cannot remove furnace recipe with empty input.")) return false;
        }

        boolean wasRemoved = FurnaceRecipes.instance().getSmeltingList().keySet()
                .removeIf(currentStack -> currentStack.getItem() == input.getItem() &&
                        (currentStack.getMetadata() == GTValues.W ||
                                currentStack.getMetadata() == input.getMetadata()));

        if (ConfigHolder.misc.debug) {
            if (wasRemoved) {
                GTLog.logger.info("Removed Smelting Recipe for Input: {}", input.getDisplayName());
            } else {
                GTLog.logger.error("Failed to Remove Smelting Recipe for Input: {}", input.getDisplayName());
            }
        }

        return wasRemoved;
    }

    /**
     * Remove all recipes matching an output
     *
     * @param output the output to match
     * @return the amount of recipes removed
     */
    @SuppressWarnings("UnusedReturnValue")
    public static int removeRecipeByOutput(@NotNull ItemStack output) {
        int recipesRemoved = removeRecipeByOutput(
                recipe -> ItemStack.areItemStacksEqual(recipe.getRecipeOutput(), output));

        if (ConfigHolder.misc.debug) {
            if (recipesRemoved != 0) {
                GTLog.logger.info("Removed {} Recipe(s) with Output: {}", recipesRemoved, output.getDisplayName());
            } else {
                GTLog.logger.error("Failed to Remove Recipe with Output: {}", output.getDisplayName());
            }
        }
        return recipesRemoved;
    }

    /**
     * Remove all recipes matching a predicate
     *
     * @param predicate the matcher
     * @return the amount of recipes removed
     */
    public static int removeRecipeByOutput(Predicate<IRecipe> predicate) {
        int recipesRemoved = 0;

        final IForgeRegistry<IRecipe> registry = ForgeRegistries.RECIPES;

        Collection<IRecipe> toRemove = new ArrayList<>();

        for (IRecipe recipe : registry) {
            if (predicate.test(recipe)) {
                toRemove.add(recipe);
                recipesRemoved++;
            }
        }

        toRemove.forEach(recipe -> {
            if (recipe.getRegistryName() != null) {
                registry.register(new DummyRecipe().setRegistryName(recipe.getRegistryName()));
            }
        });

        return recipesRemoved;
    }

    /**
     * Removes a Crafting Table Recipe with the given name.
     *
     * @param location the ResourceLocation of the Recipe.
     */
    public static void removeRecipeByName(@NotNull ResourceLocation location) {
        if (ConfigHolder.misc.debug) {
            String recipeName = location.toString();
            if (ForgeRegistries.RECIPES.containsKey(location)) {
                GTLog.logger.info("Removed Recipe with Name: {}", recipeName);
            } else {
                GTLog.logger.error("Failed to Remove Recipe with Name: {}", recipeName);
            }
        }
        ForgeRegistries.RECIPES.register(new DummyRecipe().setRegistryName(location));
    }

    /**
     * Removes a Crafting Table Recipe with the given name.
     *
     * @param recipeName the name of the Recipe.
     */
    public static void removeRecipeByName(String recipeName) {
        removeRecipeByName(new ResourceLocation(recipeName));
    }

    /**
     * Removes Crafting Table Recipes with a range of names, being {@link GTValues} voltage names.
     *
     * <p>
     * An example of how to use it: {@code removeTieredRecipeByName("gregtech:transformer_", EV, UV);}
     * <p>
     * This will remove recipes with names:
     *
     * <ul>
     * <li>gregtech:transformer_ev</li>
     * <li>gregtech:transformer_iv</li>
     * <li>gregtech:transformer_luv</li>
     * <li>gregtech:transformer_zpm</li>
     * <li>gregtech:transformer_uv</li>
     * </ul>
     *
     * @param recipeName The base name of the Recipes to remove.
     * @param startTier  The starting tier index, inclusive.
     * @param endTier    The ending tier index, inclusive.
     */
    @SuppressWarnings("unused")
    public static void removeTieredRecipeByName(@NotNull String recipeName, int startTier, int endTier) {
        for (int i = startTier; i <= endTier; i++) {
            removeRecipeByName(String.format("%s%s", recipeName, GTValues.VN[i].toLowerCase()));
        }
    }

    ///////////////////////////////////////////////////
    // Get Recipe Output Helpers //
    ///////////////////////////////////////////////////

    /**
     * @param world  the world to check the output from
     * @param recipe the recipe to retrieve from. Must not contain null values.
     * @return a Pair of the recipe, and the output
     */
    @NotNull
    public static Pair<IRecipe, ItemStack> getRecipeOutput(@Nullable World world, @Nullable ItemStack... recipe) {
        if (recipe == null || recipe.length == 0) return ImmutablePair.of(null, ItemStack.EMPTY);
        if (world == null) world = DummyWorld.INSTANCE;

        InventoryCrafting craftingGrid = new InventoryCrafting(new DummyContainer(), 3, 3);

        for (int i = 0; i < 9 && i < recipe.length; i++) {
            ItemStack recipeStack = recipe[i];
            if (!recipeStack.isEmpty()) {
                craftingGrid.setInventorySlotContents(i, recipeStack);
            }
        }

        for (IRecipe tmpRecipe : CraftingManager.REGISTRY) {
            if (tmpRecipe.matches(craftingGrid, world)) {
                ItemStack itemStack = tmpRecipe.getCraftingResult(craftingGrid);
                return ImmutablePair.of(tmpRecipe, itemStack);
            }
        }

        return ImmutablePair.of(null, ItemStack.EMPTY);
    }

    /**
     * Note: If a Furnace recipe is added through CT that is the exact same as one of the recipes that will be removed
     * then this recipe will not be added. Forge will prevent the duplicate smelting recipe from being added
     * before we remove the recipe added by another mod, therefore the CT recipe will fail. At this point,
     * disable the config and remove the recipes manually
     */
    public static void removeSmeltingEBFMetals() {
        boolean isCTLoaded = Loader.isModLoaded(GTValues.MODID_CT);

        Field actionAddFurnaceRecipe$output = null;

        Map<ItemStack, ItemStack> furnaceList = FurnaceRecipes.instance().getSmeltingList();

        Iterator<Map.Entry<ItemStack, ItemStack>> recipeIterator = furnaceList.entrySet().iterator();

        outer:
        while (recipeIterator.hasNext()) {
            Map.Entry<ItemStack, ItemStack> recipe = recipeIterator.next();

            ItemStack output = recipe.getValue();
            ItemStack input = recipe.getKey();
            MaterialStack ms = OreDictUnifier.getMaterial(output);

            if (ms != null) {
                Material material = ms.material;
                if (material.hasProperty(PropertyKey.BLAST)) {
                    ItemStack dust = OreDictUnifier.get(OrePrefix.dust, material);
                    ItemStack ingot = OreDictUnifier.get(OrePrefix.ingot, material);
                    // Check if the inputs are actually dust -> ingot
                    if (ingot.isItemEqual(output) && dust.isItemEqual(input)) {
                        if (isCTLoaded) {
                            if (actionAddFurnaceRecipe$output == null) {
                                try {
                                    actionAddFurnaceRecipe$output = ActionAddFurnaceRecipe.class
                                            .getDeclaredField("output");
                                    actionAddFurnaceRecipe$output.setAccessible(true);
                                } catch (NoSuchFieldException e) {
                                    GTLog.logger.error("Could not reflect Furnace output field", e);
                                    return;
                                }
                            }
                            for (ActionAddFurnaceRecipe aafr : MCFurnaceManager.recipesToAdd) {
                                try {
                                    // Check for equality, if the stack added into FurnaceManager..
                                    // ..was a cached stack in an existing ActionAddFurnaceRecipe as well
                                    if (actionAddFurnaceRecipe$output.get(aafr) == output) {
                                        if (ConfigHolder.misc.debug) {
                                            GTLog.logger.info(
                                                    "Not removing Smelting Recipe for EBF material {} as it is added via CT",
                                                    LocalizationUtils.format(material.getUnlocalizedName()));
                                        }
                                        continue outer;
                                    }
                                } catch (IllegalAccessException e) {
                                    GTLog.logger.error("Could not get Furnace recipe output from field", e);
                                }
                            }
                        }
                        recipeIterator.remove();
                        if (ConfigHolder.misc.debug) {
                            GTLog.logger.info("Removing Smelting Recipe for EBF material {}",
                                    LocalizationUtils.format(material.getUnlocalizedName()));
                        }
                    }
                }
            }
        }
    }

    /**
     * @param message the message for the exception
     * @return if recipe registration should continue
     * @throws IllegalArgumentException if a recipe was invalid and invalid recipes are not ignored
     */
    public static boolean setErroredInvalidRecipe(@NotNull String message) throws IllegalArgumentException {
        hasInvalidRecipe = true;
        logInvalidRecipe(message);
        return ERROR_ON_INVALID_RECIPE;
    }

    public static void logInvalidRecipe(@NotNull String message) {
        GTLog.logger.warn("Invalid Recipe Found", new IllegalArgumentException(message));
    }
}
