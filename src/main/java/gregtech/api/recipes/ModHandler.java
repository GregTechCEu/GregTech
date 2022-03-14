package gregtech.api.recipes;

import crafttweaker.mc1120.actions.ActionAddFurnaceRecipe;
import crafttweaker.mc1120.furnace.MCFurnaceManager;
import gregtech.api.GTValues;
import gregtech.api.items.ToolDictNames;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.recipes.recipes.DummyRecipe;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
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
import gregtech.api.util.ShapedOreEnergyTransferRecipe;
import gregtech.api.util.world.DummyWorld;
import gregtech.common.ConfigHolder;
import gregtech.common.crafting.GTShapedOreRecipe;
import gregtech.common.crafting.GTShapelessOreRecipe;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ModHandler {

    /**
     * Returns if that Liquid is Water or Distilled Water, or a valid Boiler Fluid.
     */
    public static boolean isWater(@Nullable FluidStack fluid) {
        if (fluid == null) return false;
        if (fluid.isFluidEqual(new FluidStack(FluidRegistry.WATER, 1))) return true;
        if (fluid.isFluidEqual(Materials.DistilledWater.getFluid(1))) return true;

        for (String fluidName : ConfigHolder.machines.boilerFluids) {
            Fluid f = FluidRegistry.getFluid(fluidName);
            if (f != null && fluid.isFluidEqual(new FluidStack(f, 1))) return true;
        }
        return false;
    }

    public static FluidStack getBoilerFluidFromContainer(@Nonnull IFluidHandler fluidHandler, boolean doDrain) {
        return getBoilerFluidFromContainer(fluidHandler, 1, doDrain);
    }

    public static FluidStack getBoilerFluidFromContainer(@Nonnull IFluidHandler fluidHandler, int amount, boolean doDrain) {
        if (amount == 0) return null;
        FluidStack drainedWater = fluidHandler.drain(Materials.Water.getFluid(amount), doDrain);
        if (drainedWater == null || drainedWater.amount == 0) {
            drainedWater = fluidHandler.drain(Materials.DistilledWater.getFluid(amount), doDrain);
        }
        if (drainedWater == null || drainedWater.amount == 0) {
            for (String fluidName : ConfigHolder.machines.boilerFluids) {
                Fluid f = FluidRegistry.getFluid(fluidName);
                if (f != null) {
                    drainedWater = fluidHandler.drain(new FluidStack(f, amount), doDrain);
                    if (drainedWater != null && drainedWater.amount > 0) {
                        break;
                    }
                }
            }
        }
        return drainedWater;
    }

    /**
     * Returns if that Liquid is Lava
     */
    public static boolean isLava(FluidStack fluid) {
        return new FluidStack(FluidRegistry.LAVA, 0).isFluidEqual(fluid);
    }

    /**
     * Returns a Liquid Stack with given amount of Lava.
     */
    public static FluidStack getLava(int amount) {
        return new FluidStack(FluidRegistry.LAVA, amount);
    }

    /**
     * Returns if that Liquid is Steam
     */
    public static boolean isSteam(FluidStack fluid) {
        return getSteam(1).isFluidEqual(fluid);
    }

    /**
     * Returns a Liquid Stack with given amount of Steam.
     */
    public static FluidStack getSteam(int amount) {
        return Objects.requireNonNull(Materials.Steam.getFluid(amount));
    }

    public static boolean isMaterialWood(Material material) {
        return material == Materials.Wood || material == Materials.TreatedWood;
    }

    public static int getFuelValue(ItemStack stack) {
        return TileEntityFurnace.getItemBurnTime(stack);
    }

    public static ItemStack getBurningFuelRemainder(ItemStack fuelStack) {
        float remainderChance;
        ItemStack remainder;
        if (OreDictUnifier.getOreDictionaryNames(fuelStack).contains("fuelCoke")) {
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

    ///////////////////////////////////////////////////
    //        Furnace Smelting Recipe Helpers        //
    ///////////////////////////////////////////////////

    public static void addSmeltingRecipe(UnificationEntry input, ItemStack output) {
        List<ItemStack> allStacks = OreDictUnifier.getAll(input);
        for (ItemStack inputStack : allStacks) {
            addSmeltingRecipe(inputStack, output, 0.0f);
        }
    }

    public static void addSmeltingRecipe(UnificationEntry input, ItemStack output, float experience) {
        List<ItemStack> allStacks = OreDictUnifier.getAll(input);
        for (ItemStack inputStack : allStacks) {
            addSmeltingRecipe(inputStack, output, experience);
        }
    }

    /**
     * Just simple Furnace smelting
     */
    public static void addSmeltingRecipe(ItemStack input, ItemStack output, float experience) {
        boolean skip = false;
        if (input.isEmpty()) {
            GTLog.logger.error("Input cannot be an empty ItemStack", new IllegalArgumentException());
            skip = true;
            RecipeMap.setFoundInvalidRecipe(true);
        }
        if (output.isEmpty()) {
            GTLog.logger.error("Output cannot be an empty ItemStack", new IllegalArgumentException());
            skip = true;
            RecipeMap.setFoundInvalidRecipe(true);
        }
        if (skip) return;
        FurnaceRecipes recipes = FurnaceRecipes.instance();

        if (recipes.getSmeltingResult(input).isEmpty()) {
            //register only if there is no recipe with duplicate input
            recipes.addSmeltingRecipe(input, output, experience);
        }
    }

    ///////////////////////////////////////////////////
    //              Crafting Recipe Helpers          //
    ///////////////////////////////////////////////////

    /**
     * Adds Shaped Crafting Recipes.
     * <p/>
     * MetaValueItem's are converted to ItemStack via {@link MetaItem.MetaValueItem#getStackForm()} method.
     * <p/>
     * For Enums - {@link Enum#name()} is called.
     * <p/>
     * For UnificationEntry - {@link UnificationEntry#toString()} is called.
     * <p/>
     * Lowercase Letters are reserved for Tools. They are as follows:
     * <p/>
     * <ul>
     * <li>'b' -  ToolDictNames.craftingToolBlade</li>
     * <li>'c' -  ToolDictNames.craftingToolCrowbar</li>
     * <li>'d' -  ToolDictNames.craftingToolScrewdriver</li>
     * <li>'f' -  ToolDictNames.craftingToolFile</li>
     * <li>'h' -  ToolDictNames.craftingToolHardHammer</li>
     * <li>'i' -  ToolDictNames.craftingToolSolderingIron</li>
     * <li>'j' -  ToolDictNames.craftingToolSolderingMetal</li>
     * <li>'k' -  ToolDictNames.craftingToolKnife</li>
     * <li>'m' -  ToolDictNames.craftingToolMortar</li>
     * <li>'p' -  ToolDictNames.craftingToolDrawplate</li>
     * <li>'r' -  ToolDictNames.craftingToolSoftHammer</li>
     * <li>'s' -  ToolDictNames.craftingToolSaw</li>
     * <li>'w' -  ToolDictNames.craftingToolWrench</li>
     * <li>'x' -  ToolDictNames.craftingToolWireCutter</li>
     * </ul>
     */

    public static void addMirroredShapedRecipe(String regName, ItemStack result, Object... recipe) {
        addMirroredShapedRecipe(false, regName, result, recipe);
    }

    public static void addMirroredShapedRecipe(boolean withUnificationData, String regName, ItemStack result, Object... recipe) {
        result = OreDictUnifier.getUnificated(result);
        boolean skip = false;
        if (result.isEmpty()) {
            GTLog.logger.error("Result cannot be an empty ItemStack. Recipe: {}", regName);
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            skip = true;
        }
        skip |= validateRecipe(regName, recipe);
        if (skip) {
            RecipeMap.setFoundInvalidRecipe(true);
            return;
        }

        IRecipe shapedOreRecipe = new GTShapedOreRecipe(false, new ResourceLocation(GTValues.MODID, "general"), result.copy(), finalizeShapedRecipeInput(recipe))
                .setMirrored(true)
                .setRegistryName(regName);
        ForgeRegistries.RECIPES.register(shapedOreRecipe);

        if (withUnificationData) OreDictUnifier.registerOre(result, getRecyclingIngredients(result.getCount(), recipe));

    }

    /**
     * Adds Shaped Crafting Recipes.
     * <p/>
     * MetaValueItem's are converted to ItemStack via {@link MetaItem.MetaValueItem#getStackForm()} method.
     * <p/>
     * For Enums - {@link Enum#name()} is called.
     * <p/>
     * For UnificationEntry - {@link UnificationEntry#toString()} is called.
     * <p/>
     * Lowercase Letters are reserved for Tools. They are as follows:
     * <p/>
     * <ul>
     * <li>'b' -  ToolDictNames.craftingToolBlade</li>
     * <li>'c' -  ToolDictNames.craftingToolCrowbar</li>
     * <li>'d' -  ToolDictNames.craftingToolScrewdriver</li>
     * <li>'f' -  ToolDictNames.craftingToolFile</li>
     * <li>'h' -  ToolDictNames.craftingToolHardHammer</li>
     * <li>'i' -  ToolDictNames.craftingToolSolderingIron</li>
     * <li>'j' -  ToolDictNames.craftingToolSolderingMetal</li>
     * <li>'k' -  ToolDictNames.craftingToolKnife</li>
     * <li>'m' -  ToolDictNames.craftingToolMortar</li>
     * <li>'p' -  ToolDictNames.craftingToolDrawplate</li>
     * <li>'r' -  ToolDictNames.craftingToolSoftHammer</li>
     * <li>'s' -  ToolDictNames.craftingToolSaw</li>
     * <li>'w' -  ToolDictNames.craftingToolWrench</li>
     * <li>'x' -  ToolDictNames.craftingToolWireCutter</li>
     * </ul>
     */
    public static void addShapedRecipe(String regName, ItemStack result, Object... recipe) {
        addShapedRecipe(false, regName, result, false, recipe);
    }

    public static void addShapedNBTClearingRecipe(String regName, ItemStack result, Object... recipe) {
        addShapedRecipe(false, regName, result, true, recipe);
    }

    public static void addShapedRecipe(boolean withUnificationData, String regName, ItemStack result, Object... recipe) {
        addShapedRecipe(withUnificationData, regName, result, false, recipe);
    }

    public static void addShapedRecipe(boolean withUnificationData, String regName, ItemStack result, boolean isNBTClearing, Object... recipe) {
        boolean skip = false;
        if (result.isEmpty()) {
            GTLog.logger.error("Result cannot be an empty ItemStack. Recipe: {}", regName);
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            skip = true;
        }
        skip |= validateRecipe(regName, recipe);
        if (skip) {
            RecipeMap.setFoundInvalidRecipe(true);
            return;
        }

        IRecipe shapedOreRecipe;
        shapedOreRecipe = new GTShapedOreRecipe(isNBTClearing, null, result.copy(), finalizeShapedRecipeInput(recipe))
                .setMirrored(false) //make all recipes not mirrored by default
                .setRegistryName(regName);

        ForgeRegistries.RECIPES.register(shapedOreRecipe);

        if (withUnificationData)
            OreDictUnifier.registerOre(result, getRecyclingIngredients(result.getCount(), recipe));
    }

    public static void addShapedEnergyTransferRecipe(String regName, ItemStack result, Predicate<ItemStack> chargePredicate, boolean overrideCharge, boolean transferMaxCharge, Object... recipe) {
        boolean skip = false;
        if (result.isEmpty()) {
            GTLog.logger.error("Result cannot be an empty ItemStack. Recipe: {}", regName);
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            skip = true;
        }
        skip |= validateRecipe(regName, recipe);
        if (skip) {
            RecipeMap.setFoundInvalidRecipe(true);
            return;
        }

        IRecipe shapedOreRecipe = new ShapedOreEnergyTransferRecipe(null, result.copy(), chargePredicate, overrideCharge, transferMaxCharge, finalizeShapedRecipeInput(recipe))
                .setMirrored(false) //make all recipes not mirrored by default
                .setRegistryName(regName);
        ForgeRegistries.RECIPES.register(shapedOreRecipe);
    }

    private static boolean validateRecipe(String regName, Object... recipe) {
        boolean skip = false;
        if (recipe == null) {
            GTLog.logger.error("Recipe cannot be null", new IllegalArgumentException());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            skip = true;
        } else if (recipe.length == 0) {
            GTLog.logger.error("Recipe cannot be empty", new IllegalArgumentException());
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            skip = true;
        } else if (Arrays.asList(recipe).contains(null) || Arrays.asList(recipe).contains(ItemStack.EMPTY)) {
            GTLog.logger.error("Recipe cannot contain null elements or Empty ItemStacks. Recipe: {}",
                    Arrays.stream(recipe)
                            .map(o -> o == null ? "NULL" : o)
                            .map(o -> o == ItemStack.EMPTY ? "EMPTY STACK" : o)
                            .map(Object::toString)
                            .map(s -> "\"" + s + "\"")
                            .collect(Collectors.joining(", ")));
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            skip = true;
        } else {
            ModContainer container = Loader.instance().activeModContainer();
            if (ForgeRegistries.RECIPES.containsKey(new ResourceLocation(container == null ? GTValues.MODID : container.getModId().toLowerCase(), regName))) {
                GTLog.logger.error("Tried to register recipe, {}, with duplicate key. Recipe: {}", regName,
                        Arrays.stream(recipe)
                                .map(Object::toString)
                                .map(s -> "\"" + s + "\"")
                                .collect(Collectors.joining(", ")));
                GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
                skip = true;
            }
        }
        return skip;
    }

    public static Object[] finalizeShapedRecipeInput(Object... recipe) {
        for (byte i = 0; i < recipe.length; i++) {
            recipe[i] = finalizeIngredient(recipe[i]);
        }
        int idx = 0;
        ArrayList<Object> recipeList = new ArrayList<>(Arrays.asList(recipe));

        while (recipe[idx] instanceof String) {
            StringBuilder s = new StringBuilder((String) recipe[idx++]);
            while (s.length() < 3) s.append(" ");
            if (s.length() > 3) throw new IllegalArgumentException();
            for (char c : s.toString().toCharArray()) {
                IGTTool tool = ToolHelper.getToolFromSymbol(c);
                if (tool != null) {
                    if (!tool.getOreDictNames().isEmpty()) {
                        recipeList.add(c);
                        recipeList.add(tool.getOreDictNames().get(0));
                    }
                }
            }
        }
        return recipeList.toArray();
    }

    public static Object finalizeIngredient(Object ingredient) {
        if (ingredient instanceof MetaItem.MetaValueItem) {
            ingredient = ((MetaItem<?>.MetaValueItem) ingredient).getStackForm();
        } else if (ingredient instanceof Enum) {
            ingredient = ((Enum<?>) ingredient).name();
        } else if (ingredient instanceof OrePrefix) {
            ingredient = ((OrePrefix) ingredient).name();
        } else if (ingredient instanceof UnificationEntry) {
            UnificationEntry entry = (UnificationEntry) ingredient;
            if(ConfigHolder.misc.debug && entry.material != null && !entry.orePrefix.isIgnored(entry.material) && !entry.orePrefix.doGenerateItem(entry.material)) {
                GTLog.logger.error("Attempted to create recipe for invalid/missing Unification Entry {}", ingredient.toString(), new IllegalArgumentException());
            }
            ingredient = ingredient.toString();
        } else if (!(ingredient instanceof ItemStack
                || ingredient instanceof Item
                || ingredient instanceof Block
                || ingredient instanceof String
                || ingredient instanceof Character
                || ingredient instanceof Boolean
                || ingredient instanceof Ingredient)) {
            throw new IllegalArgumentException(ingredient.getClass().getSimpleName() + " type is not suitable for crafting input.");
        }
        return ingredient;
    }

    public static ItemMaterialInfo getRecyclingIngredients(int outputCount, Object... recipe) {
        Map<Character, Integer> inputCountMap = new HashMap<>();
        Map<Material, Long> materialStacksExploded = new HashMap<>();

        int itr = 0;
        while (recipe[itr] instanceof String) {
            String s = (String) recipe[itr];
            for (char c : s.toCharArray()) {
                if (ToolHelper.getToolFromSymbol(c) != null) continue; // skip tools
                int count = inputCountMap.getOrDefault(c, 0);
                inputCountMap.put(c, count + 1);
            }
            itr++;
        }

        char lastChar = ' ';
        for (int i = itr; i < recipe.length; i++) {
            Object ingredient = recipe[i];

            // Track the current working ingredient symbol
            if (ingredient instanceof Character) {
                lastChar = (char) ingredient;
                continue;
            }

            // Should never happen if recipe is formatted correctly
            // In the case that it isn't, this error should be handled
            // by an earlier method call parsing the recipe.
            if (lastChar == ' ') return null;

            ItemStack stack;
            if (ingredient instanceof MetaItem.MetaValueItem) {
                stack = ((MetaItem<?>.MetaValueItem) ingredient).getStackForm();
            } else if (ingredient instanceof UnificationEntry) {
                stack = OreDictUnifier.get((UnificationEntry) ingredient);
            } else if (ingredient instanceof ItemStack) {
                stack = (ItemStack) ingredient;
            } else if (ingredient instanceof Item) {
                stack = new ItemStack((Item) ingredient, 1);
            } else if (ingredient instanceof Block) {
                stack = new ItemStack((Block) ingredient, 1);
            } else if (ingredient instanceof String) {
                stack = OreDictUnifier.get((String) ingredient);
            } else continue; // throw out bad entries

            BiConsumer<MaterialStack, Character> func = (ms, c) -> {
                long amount = materialStacksExploded.getOrDefault(ms.material, 0L);
                materialStacksExploded.put(ms.material, (ms.amount * inputCountMap.get(c)) + amount);
            };

            // First try to get ItemMaterialInfo
            ItemMaterialInfo info = OreDictUnifier.getMaterialInfo(stack);
            if (info != null) {
                for (MaterialStack ms : info.getMaterials()) {
                    if (!(ms.material instanceof MarkerMaterial)) func.accept(ms, lastChar);
                }
                continue;
            }

            // Then try to get a single Material (UnificationEntry needs this, for example)
            MaterialStack materialStack = OreDictUnifier.getMaterial(stack);
            if (materialStack != null && !(materialStack.material instanceof MarkerMaterial)) func.accept(materialStack, lastChar);

            // Gather any secondary materials if this item has an OrePrefix
            OrePrefix prefix = OreDictUnifier.getPrefix(stack);
            if (prefix != null && !prefix.secondaryMaterials.isEmpty()) {
                for (MaterialStack ms : prefix.secondaryMaterials) {
                    func.accept(ms, lastChar);
                }
            }
        }

        return new ItemMaterialInfo(materialStacksExploded.entrySet().stream()
                .map(e -> new MaterialStack(e.getKey(), e.getValue() / outputCount))
                .sorted(Comparator.comparingLong(m -> -m.amount))
                .collect(Collectors.toList())
        );
    }

    /**
     * Add Shapeless Crafting Recipes
     */

    public static void addShapelessRecipe(String regName, ItemStack result, Object... recipe) {
        addShapelessRecipe(regName, result, false, recipe);
    }

    public static void addShapelessNBTClearingRecipe(String regName, ItemStack result, Object... recipe) {
        addShapelessRecipe(regName, result, true, recipe);
    }

    public static void addShapelessRecipe(String regName, ItemStack result, boolean isNBTClearing, Object... recipe) {
        boolean skip = false;
        if (result.isEmpty()) {
            GTLog.logger.error("Result cannot be an empty ItemStack. Recipe: {}", regName);
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            skip = true;
        }
        skip |= validateRecipe(regName, recipe);
        if (skip) {
            RecipeMap.setFoundInvalidRecipe(true);
            return;
        }

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
                if (tool == null || tool.getOreDictNames().isEmpty()) {
                    throw new IllegalArgumentException("Tool name is not found for char " + recipe[i]);
                }
                recipe[i] = tool.getOreDictNames().get(0);
            } else if (!(recipe[i] instanceof ItemStack
                    || recipe[i] instanceof Item
                    || recipe[i] instanceof Block
                    || recipe[i] instanceof String)) {
                throw new IllegalArgumentException(recipe.getClass().getSimpleName() + " type is not suitable for crafting input.");
            }
        }
        IRecipe shapelessRecipe;
            shapelessRecipe = new GTShapelessOreRecipe(isNBTClearing, null, result.copy(), recipe)
                    .setRegistryName(regName);

        try {
            //workaround for MC bug that makes all shaped recipe inputs that have enchanted items
            //or renamed ones on input fail, even if all ingredients match it
            Field field = ShapelessOreRecipe.class.getDeclaredField("isSimple");
            field.setAccessible(true);
            field.setBoolean(shapelessRecipe, false);
        } catch (ReflectiveOperationException exception) {
            GTLog.logger.error("Failed to mark shapeless recipe as complex", exception);
        }

        ForgeRegistries.RECIPES.register(shapelessRecipe);
    }

    public static Collection<ItemStack> getAllSubItems(ItemStack item) {
        //match subtypes only on wildcard damage value items
        if (item.getItemDamage() != GTValues.W)
            return Collections.singleton(item);
        NonNullList<ItemStack> stackList = NonNullList.create();
        CreativeTabs[] visibleTags = item.getItem().getCreativeTabs();
        for (CreativeTabs creativeTab : visibleTags) {
            NonNullList<ItemStack> thisList = NonNullList.create();
            item.getItem().getSubItems(creativeTab, thisList);
            loop:
            for (ItemStack newStack : thisList) {
                for (ItemStack alreadyExists : stackList) {
                    if (ItemStack.areItemStacksEqual(alreadyExists, newStack))
                        continue loop; //do not add equal item stacks
                }
                stackList.add(newStack);
            }
        }
        return stackList;
    }

    ///////////////////////////////////////////////////
    //              Recipe Remove Helpers            //
    ///////////////////////////////////////////////////

    public static boolean removeFurnaceSmelting(UnificationEntry input) {
        boolean result = false;
        List<ItemStack> allStacks = OreDictUnifier.getAll(input);
        for (ItemStack inputStack : allStacks) {
            result |= removeFurnaceSmelting(inputStack);
        }
        return result;
    }

    /**
     * Removes a Smelting Recipe
     */
    public static boolean removeFurnaceSmelting(ItemStack input) {
        if (input.isEmpty()) {
            GTLog.logger.error("Cannot remove furnace recipe with empty input.");
            GTLog.logger.error("Stacktrace:", new IllegalArgumentException());
            RecipeMap.setFoundInvalidRecipe(true);
            return false;
        }

        boolean wasRemoved = FurnaceRecipes.instance().getSmeltingList().keySet().removeIf(currentStack -> currentStack.getItem() == input.getItem() && (currentStack.getMetadata() == GTValues.W || currentStack.getMetadata() == input.getMetadata()));

        if (ConfigHolder.misc.debug) {
            if (wasRemoved)
                GTLog.logger.info("Removed Smelting Recipe for Input: {}", input.getDisplayName());
            else GTLog.logger.error("Failed to Remove Smelting Recipe for Input: {}", input.getDisplayName());
        }

        return wasRemoved;
    }

    public static int removeRecipes(ItemStack output) {
        int recipesRemoved = removeRecipes(recipe -> ItemStack.areItemStacksEqual(recipe.getRecipeOutput(), output));

        if (ConfigHolder.misc.debug) {
            if (recipesRemoved != 0)
                GTLog.logger.info("Removed {} Recipe(s) with Output: {}", recipesRemoved, output.getDisplayName());
            else GTLog.logger.error("Failed to Remove Recipe with Output: {}", output.getDisplayName());
        }
        return recipesRemoved;
    }

    public static int removeRecipes(Predicate<IRecipe> predicate) {
        int recipesRemoved = 0;

        IForgeRegistry<IRecipe> registry = ForgeRegistries.RECIPES;
        List<IRecipe> toRemove = new ArrayList<>();

        for (IRecipe recipe : registry) {
            if (predicate.test(recipe)) {
                toRemove.add(recipe);
                recipesRemoved++;
            }
        }

        toRemove.forEach(recipe -> registry.register(new DummyRecipe().setRegistryName(recipe.getRegistryName())));

        return recipesRemoved;
    }

    /**
     * Removes a Crafting Table Recipe with the given name.
     *
     * @param location The ResourceLocation of the Recipe.
     *                 Can also accept a String.
     */
    public static void removeRecipeByName(ResourceLocation location) {
        if (ConfigHolder.misc.debug) {
            String recipeName = location.toString();
            if (ForgeRegistries.RECIPES.containsKey(location))
                GTLog.logger.info("Removed Recipe with Name: {}", recipeName);
            else GTLog.logger.error("Failed to Remove Recipe with Name: {}", recipeName);
        }
        ForgeRegistries.RECIPES.register(new DummyRecipe().setRegistryName(location));
    }

    public static void removeRecipeByName(String recipeName) {
        removeRecipeByName(new ResourceLocation(recipeName));
    }

    /**
     * Removes Crafting Table Recipes with a range of names, being {@link GTValues} voltage names.
     * An example of how to use it:
     *
     * <cr>
     *     removeTieredRecipeByName("gregtech:transformer_", EV, UV);
     * </cr>
     *
     * This will remove recipes with names:
     *
     * <cr>
     *     gregtech:transformer_ev
     *     gregtech:transformer_iv
     *     gregtech:transformer_luv
     *     gregtech:transformer_zpm
     *     gregtech:transformer_uv
     * </cr>
     *
     * @param recipeName The base name of the Recipes to remove.
     * @param startTier  The starting tier index, inclusive.
     * @param endTier    The ending tier index, inclusive.
     */
    public static void removeTieredRecipeByName(String recipeName, int startTier, int endTier) {
        for (int i = startTier; i <= endTier; i++)
            removeRecipeByName(String.format("%s%s", recipeName, GTValues.VN[i].toLowerCase()));
    }

    ///////////////////////////////////////////////////
    //            Get Recipe Output Helpers          //
    ///////////////////////////////////////////////////

    public static Pair<IRecipe, ItemStack> getRecipeOutput(World world, ItemStack... recipe) {
        if (recipe == null || recipe.length == 0)
            return ImmutablePair.of(null, ItemStack.EMPTY);

        if (world == null) world = DummyWorld.INSTANCE;

        InventoryCrafting craftingGrid = new InventoryCrafting(new DummyContainer(), 3, 3);

        for (int i = 0; i < 9 && i < recipe.length; i++) {
            ItemStack recipeStack = recipe[i];
            if (recipeStack != null && !recipeStack.isEmpty()) {
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

    public static ItemStack getSmeltingOutput(ItemStack input) {
        if (input.isEmpty()) return ItemStack.EMPTY;
        return OreDictUnifier.getUnificated(FurnaceRecipes.instance().getSmeltingResult(input));
    }

    /* Note: If a Furnace recipe is added through CT that is the exact same as one of the recipes that will be removed
       then this recipe will not be added. Forge will prevent the duplicate smelting recipe from being added before we
       remove the recipe added by another mod, therefore the CT recipe will fail. At this point, disable the config and
       remove the recipes manually
     */
    public static void removeSmeltingEBFMetals() {

        boolean isCTLoaded = Loader.isModLoaded(GTValues.MODID_CT);

        Field actionAddFurnaceRecipe$output = null;

        Map<ItemStack, ItemStack> furnaceList = FurnaceRecipes.instance().getSmeltingList();

        Iterator<Map.Entry<ItemStack, ItemStack>> recipeIterator = furnaceList.entrySet().iterator();

        outer: while(recipeIterator.hasNext()) {
            Map.Entry<ItemStack, ItemStack> recipe = recipeIterator.next();

            ItemStack output = recipe.getValue();
            ItemStack input = recipe.getKey();
            MaterialStack ms = OreDictUnifier.getMaterial(output);

            if(ms != null) {
                Material material = ms.material;
                if(material.hasProperty(PropertyKey.BLAST)) {
                    ItemStack dust = OreDictUnifier.get(OrePrefix.dust, material);
                    ItemStack ingot = OreDictUnifier.get(OrePrefix.ingot, material);
                    //Check if the inputs are actually dust -> ingot
                    if(ingot.isItemEqual(output) && dust.isItemEqual(input)) {
                        if(isCTLoaded) {
                            if(actionAddFurnaceRecipe$output == null) {
                                try {
                                    actionAddFurnaceRecipe$output = ActionAddFurnaceRecipe.class.getDeclaredField("output");
                                    actionAddFurnaceRecipe$output.setAccessible(true);
                                } catch (NoSuchFieldException e) {
                                    e.printStackTrace();
                                }
                            }
                            for(ActionAddFurnaceRecipe aafr : MCFurnaceManager.recipesToAdd) {
                                try {
                                    // Check for equality, if the stack added into FurnaceManager..
                                    // ..was a cached stack in an existing ActionAddFurnaceRecipe as well
                                    if(actionAddFurnaceRecipe$output.get(aafr) == output) {
                                        if(ConfigHolder.misc.debug) {
                                            GTLog.logger.info("Not removing Smelting Recipe for EBF material {} as it is added via CT", LocalizationUtils.format(material.getUnlocalizedName()));
                                        }
                                        continue outer;
                                    }
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        recipeIterator.remove();
                        if(ConfigHolder.misc.debug) {
                            GTLog.logger.info("Removing Smelting Recipe for EBF material {}", LocalizationUtils.format(material.getUnlocalizedName()));
                        }
                    }
                }
            }
        }
    }
}
