package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.category.RecipeCategories;
import gregtech.api.recipes.ingredients.nbtmatch.NBTCondition;
import gregtech.api.recipes.ingredients.nbtmatch.NBTMatcher;
import gregtech.api.recipes.ingredients.nbtmatch.NBTTagType;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;

import com.google.common.collect.ImmutableList;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gregtech.api.GTValues.*;
import static gregtech.api.GTValues.MV;
import static gregtech.api.unification.material.info.MaterialFlags.*;

public class RecyclingRecipes {

    private static final NBTCondition RENAMED_NBT = NBTCondition.create(NBTTagType.COMPOUND, "display", "");
    private static final NBTCondition REPAIRCOST_NBT = NBTCondition.create(NBTTagType.INT, "RepairCost", "");

    // TODO - Fix recipe order with some things (noticed Hermetic Casings)
    // TODO - Figure out solution to LuV+ components
    // TODO - (to remember) Do NOT calculate any material component lists for circuits, they are simply totally lost
    // TODO - Work on durations and EUt's

    public static void init() {
        for (Entry<ItemStack, ItemMaterialInfo> entry : OreDictUnifier.getAllItemInfos()) {
            ItemStack itemStack = entry.getKey();
            ItemMaterialInfo materialInfo = entry.getValue();
            List<MaterialStack> materialStacks = new ArrayList<>(materialInfo.getMaterials());
            registerRecyclingRecipes(itemStack, materialStacks, false, null);
        }
    }

    public static void registerRecyclingRecipes(ItemStack input, List<MaterialStack> components,
                                                boolean ignoreArcSmelting, @Nullable OrePrefix prefix) {
        // Gather the valid Materials for use in recycling recipes.
        // - Filter out Materials that cannot create a Dust
        // - Filter out Materials that do not equate to at least 1 Nugget worth of Material.
        // - Sort Materials on a descending material amount
        List<MaterialStack> materials = components.stream()
                .filter(stack -> stack.material.hasProperty(PropertyKey.DUST))
                .filter(stack -> stack.amount >= M / 9)
                .sorted(Comparator.comparingLong(ms -> -ms.amount))
                .collect(Collectors.toList());

        // Exit if no Materials matching the above requirements exist.
        if (materials.isEmpty()) return;

        // Calculate the voltage multiplier based on if a Material has a Blast Property
        int voltageMultiplier = calculateVoltageMultiplier(components);

        if (prefix != OrePrefix.dust) {
            registerMaceratorRecycling(input, components, voltageMultiplier);
        }

        registerSmeltingRecycling(input, components, voltageMultiplier, prefix);
    }

    private static void registerMaceratorRecycling(ItemStack input, List<MaterialStack> materials, int multiplier) {
        // Finalize the output list.
        List<ItemStack> outputs = finalizeOutputs(
                materials,
                RecipeMaps.MACERATOR_RECIPES.getMaxOutputs(),
                OreDictUnifier::getDust);

        // Exit if no valid Materials exist for this recycling Recipe.
        if (outputs.size() == 0) return;

        // Build the final Recipe.
        RecipeBuilder<SimpleRecipeBuilder> recipe = RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(input.copy())
                .outputs(outputs)
                .duration(calculateDuration(materials))
                .EUt(2 * multiplier)
                .category(RecipeCategories.MACERATOR_RECYCLING);

        cleanInputNBT(input, recipe);

        recipe.buildAndRegister();
    }

    private static void registerSmeltingRecycling(ItemStack input, List<MaterialStack> materials, int multiplier,
                                                   @Nullable OrePrefix prefix) {
        // Handle simple materials separately
        if (prefix != null && prefix.secondaryMaterials.isEmpty()) {
            MaterialStack ms = OreDictUnifier.getMaterial(input);
            if (ms == null || ms.material == null) {
                return;
            }

            Material m = ms.material;

            /*
            if (m.hasProperty(PropertyKey.INGOT) && m.getProperty(PropertyKey.INGOT).getMacerateInto() != m) {
                m = m.getProperty(PropertyKey.INGOT).getMacerateInto();
            }

             */

            if (!m.hasProperty(PropertyKey.FLUID) || m.getFluid() == null) {
                return;
            }

            if (!m.hasProperty(PropertyKey.BLAST)) {
                RecipeMaps.BLAST_RECIPES.recipeBuilder()
                        .inputs(input.copy())
                        .fluidOutputs(m.getFluid((int) (ms.amount * L / M)))
                        .blastFurnaceTemp(1200)
                        .duration((int) (100 * ms.amount / M))
                        .EUt(GTValues.VA[GTValues.LV] * multiplier)
                        .buildAndRegister();

                RecipeMaps.FURNACE_RECIPES.recipeBuilder()
                        .inputs(input.copy())
                        .fluidOutputs(m.getFluid((int) (ms.amount * L / M)))
                        .duration((int) (150 * ms.amount / M))
                        .EUt(GTValues.VA[GTValues.LV] * multiplier)
                        .buildAndRegister();

                RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                        .inputs(input.copy())
                        .fluidOutputs(m.getFluid((int) (ms.amount * L / M)))
                        .duration((int) (125 * ms.amount / M))
                        .EUt(GTValues.VA[GTValues.LV] * multiplier)
                        .buildAndRegister();

                return;
            }

            var property = m.getProperty(PropertyKey.BLAST);

            int blastTemp = property.getBlastTemperature();
            BlastProperty.GasTier gasTier = property.getGasTier();
            int duration = property.getDurationOverride();
            if (duration <= 0) {
                duration = Math.max(1, (int) (m.getMass() * blastTemp / 50L));
            }
            int EUt = property.getEUtOverride();
            if (EUt <= 0) EUt = VA[MV];

            if (blastTemp < 1500) {
                RecipeMaps.FURNACE_RECIPES.recipeBuilder()
                        .inputs(input.copy())
                        .fluidOutputs(m.getFluid((int) (ms.amount * L / M)))
                        .duration((int) (150 * ms.amount / M))
                        .EUt(GTValues.VA[GTValues.LV] * multiplier)
                        .buildAndRegister();

                RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                        .inputs(input.copy())
                        .fluidOutputs(m.getFluid((int) (ms.amount * L / M)))
                        .duration((int) (125 * ms.amount / M))
                        .EUt(GTValues.VA[GTValues.LV] * multiplier)
                        .buildAndRegister();
            }

            BlastRecipeBuilder blastBuilder = RecipeMaps.BLAST_RECIPES.recipeBuilder()
                    .input(prefix, m)
                    .fluidOutputs(m.getFluid((int) (ms.amount * L / M)))
                    .blastFurnaceTemp(blastTemp)
                    .EUt(EUt);

            if (gasTier != null) {
                FluidStack gas = CraftingComponent.EBF_GASES.get(gasTier).copy();
                gas.amount = (int) (gas.amount * ms.amount / M);

                blastBuilder.copy()
                        .circuitMeta(1)
                        .duration((int) (duration * ms.amount / M))
                        .buildAndRegister();

                blastBuilder.copy()
                        .circuitMeta(2)
                        .fluidInputs(gas)
                        .duration((int) (duration * 0.67 * ms.amount / M))
                        .buildAndRegister();
            } else {
                blastBuilder.duration(duration);
                blastBuilder.buildAndRegister();
            }

            return;
        }

        // Filter down the materials list
        materials = combineStacks(materials.stream()
                .map(RecyclingRecipes::getArcSmeltingResult)
                .filter(Objects::nonNull)
                .filter(m -> m.material.hasFluid())
                .collect(Collectors.toList()));

        // Exit if no valid outputs exist for this recycling Recipe.
        if (materials.size() == 0) return;

        int maxBlastTemp = 0;

        for (var i : materials) {
            if (i.material.hasProperty(PropertyKey.BLAST)) {
                if (i.material.getProperty(PropertyKey.BLAST).getBlastTemperature() > maxBlastTemp) {
                    maxBlastTemp = i.material.getProperty(PropertyKey.BLAST).getBlastTemperature();
                }
            }
        }

        maxBlastTemp = Math.max(1200, maxBlastTemp);

        // Build the final Recipe.
        RecipeBuilder<BlastRecipeBuilder> builder = RecipeMaps.BLAST_RECIPES.recipeBuilder()
                .inputs(input.copy())
                .blastFurnaceTemp(maxBlastTemp)
                .category(RecipeCategories.SMELT_RECYCLING);

        for (int i = 0; i < Math.min(3, materials.size()); i++) {
            builder.fluidOutputs(materials.get(i).material.getFluid((int) (materials.get(i).amount * L / M)));
        }

        builder.duration(calculateDuration(materials))
                .EUt(GTValues.VA[GTValues.LV]);

        cleanInputNBT(input, builder);

        builder.buildAndRegister();
    }

    private static MaterialStack getArcSmeltingResult(MaterialStack materialStack) {
        Material material = materialStack.material;
        long amount = materialStack.amount;

        if (material.hasFlag(EXPLOSIVE)) {
            return null;
        }

        // If the Material is Flammable, return Ash
        if (material.hasFlag(FLAMMABLE)) {
            return null;
        }

        // Else if the Material is a Gem, process its output (see below)
        if (material.hasProperty(PropertyKey.GEM)) {
            return null;
        }

        // Else if the Material has NO_SMELTING, return nothing
        if (material.hasFlag(NO_SMELTING)) {
            return null;
        }

        // Else if the Material is an Ingot, return the Arc Smelting
        // result if it exists, otherwise return the Material itself.
        if (material.hasProperty(PropertyKey.INGOT)) {
            Material arcSmelt = material.getProperty(PropertyKey.INGOT).getArcSmeltInto();
            if (arcSmelt != null) {
                return new MaterialStack(arcSmelt, amount);
            }
        }
        return materialStack;
    }

    private static int calculateVoltageMultiplier(List<MaterialStack> materials) {
        // Gather the highest blast temperature of any material in the list
        int highestTemp = 0;
        for (MaterialStack ms : materials) {
            Material m = ms.material;
            if (m.hasProperty(PropertyKey.BLAST)) {
                BlastProperty prop = m.getProperty(PropertyKey.BLAST);
                if (prop.getBlastTemperature() > highestTemp) {
                    highestTemp = prop.getBlastTemperature();
                }
            } else if (m.hasFlag(IS_MAGNETIC) && m.hasProperty(PropertyKey.INGOT) &&
                    m.getProperty(PropertyKey.INGOT).getSmeltingInto().hasProperty(PropertyKey.BLAST)) {
                        BlastProperty prop = m.getProperty(PropertyKey.INGOT).getSmeltingInto()
                                .getProperty(PropertyKey.BLAST);
                        if (prop.getBlastTemperature() > highestTemp) {
                            highestTemp = prop.getBlastTemperature();
                        }
                    }
        }

        // No blast temperature in the list means no multiplier
        if (highestTemp == 0) return 1;

        // If less then 2000K, multiplier of 4
        if (highestTemp < 2000) return 4; // todo make this a better value?

        // If above 2000K, multiplier of 16
        return 16;
    }

    /**
     * This method calculates the duration for a recycling method. It:
     * - Sums the amount of material times the mass of the material for the List
     * - Divides that by M
     */
    private static int calculateDuration(List<MaterialStack> materials) {
        long duration = 0;
        for (MaterialStack is : materials) {
            if (is != null) duration += is.amount * is.material.getMass();
        }
        return (int) Math.max(1L, duration / M);
    }

    /**
     * Combines any matching Materials in the List into one MaterialStack
     */
    private static List<MaterialStack> combineStacks(List<MaterialStack> rawList) {
        // Combine any stacks in the List that have the same Item.
        Map<Material, Long> materialStacksExploded = new HashMap<>();
        for (MaterialStack ms : rawList) {
            long amount = materialStacksExploded.getOrDefault(ms.material, 0L);
            materialStacksExploded.put(ms.material, ms.amount + amount);
        }
        return materialStacksExploded.entrySet().stream()
                .map(e -> new MaterialStack(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private static List<ItemStack> finalizeOutputs(List<MaterialStack> materials, int maxOutputs,
                                                   Function<MaterialStack, ItemStack> toItemStackMapper) {
        // Map of ItemStack, Long to properly sort by the true material amount for outputs
        List<Tuple<ItemStack, MaterialStack>> outputs = new ArrayList<>();

        for (MaterialStack ms : materials) {
            ItemStack stack = toItemStackMapper.apply(ms);
            if (stack == ItemStack.EMPTY) continue;
            if (stack.getCount() > 64) {
                UnificationEntry entry = OreDictUnifier.getUnificationEntry(stack);
                if (entry != null) { // should always be true
                    OrePrefix prefix = entry.orePrefix;

                    // These are the highest forms that a Material can have (for Ingot and Dust, respectively),
                    // so simply split the stacks and continue.
                    if (prefix == OrePrefix.block || prefix == OrePrefix.dust) {
                        splitStacks(outputs, stack, entry);
                    } else {
                        // Attempt to split and to shrink the stack, and choose the option that creates the
                        // "larger" single stack, in terms of raw material amount.
                        List<Tuple<ItemStack, MaterialStack>> split = new ArrayList<>();
                        List<Tuple<ItemStack, MaterialStack>> shrink = new ArrayList<>();
                        splitStacks(split, stack, entry);
                        shrinkStacks(shrink, stack, entry);

                        if (split.get(0).getSecond().amount > shrink.get(0).getSecond().amount) {
                            outputs.addAll(split);
                        } else outputs.addAll(shrink);
                    }
                }
            } else outputs.add(new Tuple<>(stack, ms));
        }

        // Sort the List by total material amount descending.
        outputs.sort(Comparator.comparingLong(e -> -e.getSecond().amount));

        // Sort "duplicate" outputs to the end.
        // For example, if there are blocks of Steel and nuggets of Steel, and the nuggets
        // are preventing some other output from occupying one of the final slots of the machine,
        // cut the nuggets out to favor the newer item instead of having 2 slots occupied by Steel.
        //
        // There is probably a better way to do this.
        Map<MaterialStack, ItemStack> temp = new HashMap<>();
        for (Tuple<ItemStack, MaterialStack> t : outputs) {
            boolean isInMap = false;
            for (MaterialStack ms : temp.keySet()) {
                if (ms.material == t.getSecond().material) {
                    isInMap = true;
                    break;
                }
            }
            if (!isInMap) temp.put(t.getSecond(), t.getFirst());
        }
        temp.putAll(outputs.stream()
                .filter(t -> !temp.containsKey(t.getSecond()))
                .collect(Collectors.toMap(Tuple::getSecond, Tuple::getFirst)));

        // Filter Ash to the very end of the list, after all others
        List<ItemStack> ashStacks = temp.entrySet().stream()
                .filter(e -> isAshMaterial(e.getKey()))
                .sorted(Comparator.comparingLong(e -> -e.getKey().amount))
                .map(Entry::getValue)
                .collect(Collectors.toList());

        List<ItemStack> returnValues = temp.entrySet().stream()
                .sorted(Comparator.comparingLong(e -> -e.getKey().amount))
                .filter(e -> !isAshMaterial(e.getKey()))
                .limit(maxOutputs)
                .map(Entry::getValue)
                .collect(Collectors.toList());

        for (int i = 0; i < ashStacks.size() && returnValues.size() < maxOutputs; i++) {
            returnValues.add(ashStacks.get(i));
        }
        return returnValues;
    }

    private static void splitStacks(List<Tuple<ItemStack, MaterialStack>> list, ItemStack originalStack,
                                    UnificationEntry entry) {
        int amount = originalStack.getCount();
        while (amount > 64) {
            list.add(new Tuple<>(GTUtility.copy(64, originalStack),
                    new MaterialStack(entry.material, entry.orePrefix.getMaterialAmount(entry.material) * 64)));
            amount -= 64;
        }
        list.add(new Tuple<>(GTUtility.copy(amount, originalStack),
                new MaterialStack(entry.material, entry.orePrefix.getMaterialAmount(entry.material) * amount)));
    }

    private static final List<OrePrefix> DUST_ORDER = ImmutableList.of(OrePrefix.dust);
    private static final List<OrePrefix> INGOT_ORDER = ImmutableList.of(OrePrefix.block, OrePrefix.ingot,
            OrePrefix.nugget);

    private static void shrinkStacks(List<Tuple<ItemStack, MaterialStack>> list, ItemStack originalStack,
                                     UnificationEntry entry) {
        Material material = entry.material;
        long materialAmount = originalStack.getCount() * entry.orePrefix.getMaterialAmount(material);

        // noinspection ConstantConditions
        final List<OrePrefix> chosenList = material.hasProperty(PropertyKey.INGOT) ? INGOT_ORDER : DUST_ORDER;

        // Break materialAmount into a maximal stack
        Map<OrePrefix, MaterialStack> tempList = new HashMap<>();
        for (OrePrefix prefix : chosenList) {

            // Current prefix too large to "compact" into
            if (materialAmount / prefix.getMaterialAmount(material) == 0) continue;

            long newAmount = materialAmount / prefix.getMaterialAmount(material);
            tempList.put(prefix, new MaterialStack(material, newAmount * prefix.getMaterialAmount(material)));
            materialAmount = materialAmount % prefix.getMaterialAmount(material);
        }

        // Split the "highest level" stack (either Blocks or Dusts) if needed, as it is
        // the only stack that could possibly be above 64.
        if (tempList.containsKey(chosenList.get(0))) {
            OrePrefix prefix = chosenList.get(0);
            MaterialStack ms = tempList.get(prefix);
            splitStacks(list,
                    OreDictUnifier.get(chosenList.get(0), ms.material,
                            (int) (ms.amount / prefix.getMaterialAmount(material))),
                    new UnificationEntry(prefix, material));
        }

        OrePrefix mediumPrefix = chosenList.get(1); // dustSmall or ingot
        OrePrefix smallestPrefix = chosenList.get(2); // dustTiny or nugget
        MaterialStack mediumMS = tempList.get(mediumPrefix); // dustSmall or ingot
        MaterialStack smallestMS = tempList.get(smallestPrefix); // dustTiny or nugget

        // Try to compact the two "lower form" prefixes into one stack, if it doesn't exceed stack size
        if (mediumMS != null && smallestMS != null) {
            long singleStackAmount = mediumMS.amount + smallestMS.amount;
            if (singleStackAmount / smallestPrefix.getMaterialAmount(material) <= 64) {
                list.add(new Tuple<>(
                        OreDictUnifier.get(smallestPrefix, material,
                                (int) (singleStackAmount / smallestPrefix.getMaterialAmount(material))),
                        new MaterialStack(material, singleStackAmount)));
                return;
            }
        }

        // Otherwise simply add the stacks to the List if they exist
        if (mediumMS != null) list.add(new Tuple<>(
                OreDictUnifier.get(mediumPrefix, material,
                        (int) (mediumMS.amount / mediumPrefix.getMaterialAmount(material))),
                new MaterialStack(material, mediumMS.amount)));

        if (smallestMS != null) list.add(new Tuple<>(
                OreDictUnifier.get(smallestPrefix, material,
                        (int) (smallestMS.amount / smallestPrefix.getMaterialAmount(material))),
                new MaterialStack(material, smallestMS.amount)));
    }

    private static boolean isAshMaterial(MaterialStack ms) {
        return ms.material == Materials.Ash || ms.material == Materials.DarkAsh || ms.material == Materials.Carbon;
    }

    /**
     * Performs various NBT matching on the provided input and adds the result to the provided RecipeBuilder
     *
     * @param input   The input itemStack
     * @param builder The RecipeBuilder to add the NBT condition to
     */
    private static void cleanInputNBT(ItemStack input, RecipeBuilder<?> builder) {
        // Ignore String tag from naming machines
        MetaTileEntity mte = GTUtility.getMetaTileEntity(input);
        if (mte != null) {
            builder.clearInputs();
            // Don't use ANY to avoid issues with Drums, Super Chests, and other MTEs that hold an inventory
            builder.inputNBT(mte, NBTMatcher.NOT_PRESENT_OR_HAS_KEY, RENAMED_NBT);
        }

        Item inputItem = input.getItem();
        // Check to see if we can recycle tools. Only allow Tools at full durability
        if (inputItem.isDamageable()) {
            if (inputItem.getDamage(input) == 0) {
                builder.clearInputs();
                builder.inputNBT(inputItem, NBTMatcher.NOT_PRESENT_OR_HAS_KEY, REPAIRCOST_NBT);
            }
        }
    }
}
