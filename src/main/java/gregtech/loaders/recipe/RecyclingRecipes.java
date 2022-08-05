package gregtech.loaders.recipe;

import com.google.common.collect.ImmutableList;
import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gregtech.api.GTValues.L;
import static gregtech.api.GTValues.M;
import static gregtech.api.unification.material.info.MaterialFlags.*;

public class RecyclingRecipes {

    // TODO - Fix recipe order with some things (noticed Hermetic Casings)
    // TODO - Figure out solution to LuV+ components
    // TODO - (to remember) Do NOT calculate any material component lists for circuits, they are simply totally lost
    // TODO - Work on durations and EUt's

    public static void init() {
        for (Entry<ItemStack, ItemMaterialInfo> entry : OreDictUnifier.getAllItemInfos()) {
            ItemStack itemStack = entry.getKey();
            ItemMaterialInfo materialInfo = entry.getValue();
            ArrayList<MaterialStack> materialStacks = new ArrayList<>(materialInfo.getMaterials());
            registerRecyclingRecipes(itemStack, materialStacks, false, null);
        }
    }

    public static void registerRecyclingRecipes(ItemStack input, List<MaterialStack> components, boolean ignoreArcSmelting, @Nullable OrePrefix prefix) {

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
        if (prefix != null) {
            registerExtractorRecycling(input, components, voltageMultiplier, prefix);
        }
        if (ignoreArcSmelting) return;

        if (components.size() == 1) {
            Material m = components.get(0).material;

            // skip non-ingot materials
            if (!m.hasProperty(PropertyKey.INGOT)) {
                return;
            }

            // Skip Ingot -> Ingot Arc Recipes
            if (OreDictUnifier.getPrefix(input) == OrePrefix.ingot && m.getProperty(PropertyKey.INGOT).getArcSmeltInto() == m) {
                return;
            }

            // Prevent Magnetic dust -> Regular Ingot Arc Furnacing, avoiding the EBF recipe
            // "I will rework magnetic materials soon" - DStrand1
            if(prefix == OrePrefix.dust && m.hasFlag(IS_MAGNETIC)) {
                return;
            }
        }
        registerArcRecycling(input, components, prefix);
    }

    private static void registerMaceratorRecycling(ItemStack input, List<MaterialStack> materials, int multiplier) {

        // Finalize the output list.
        List<ItemStack> outputs = finalizeOutputs(
                materials,
                RecipeMaps.MACERATOR_RECIPES.getMaxOutputs(),
                OreDictUnifier::getDust
        );

        // Exit if no valid Materials exist for this recycling Recipe.
        if (outputs.size() == 0) return;

        // Build the final Recipe.
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(input.copy())
                .outputs(outputs)
                .duration(calculateDuration(outputs))
                .EUt(2 * multiplier)
                .buildAndRegister();
    }

    private static void registerExtractorRecycling(ItemStack input, List<MaterialStack> materials, int multiplier, @Nullable OrePrefix prefix) {

        // Handle simple materials separately
        if (prefix != null && prefix.secondaryMaterials.isEmpty()) {
            MaterialStack ms = OreDictUnifier.getMaterial(input);
            if (ms == null || ms.material == null) {
                return;
            }
            Material m = ms.material;
            if (m.hasProperty(PropertyKey.INGOT) && m.getProperty(PropertyKey.INGOT).getMacerateInto() != m) {
                m = m.getProperty(PropertyKey.INGOT).getMacerateInto();
            }
            if (!m.hasProperty(PropertyKey.FLUID) || (prefix == OrePrefix.dust && m.hasProperty(PropertyKey.BLAST))) {
                return;
            }
            RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                    .inputs(input.copy())
                    .fluidOutputs(m.getFluid((int) (ms.amount * L / M)))
                    .duration((int) Math.max(1, ms.amount * ms.material.getMass() / M))
                    .EUt(GTValues.VA[GTValues.LV] * multiplier)
                    .buildAndRegister();

            return;
        }

        // Find the first Material which can create a Fluid.
        // If no Material in the list can create a Fluid, return.
        MaterialStack fluidMs = materials.stream().filter(ms -> ms.material.hasProperty(PropertyKey.FLUID)).findFirst().orElse(null);
        if (fluidMs == null) return;

        // Find the next MaterialStack, which will be the Item output.
        // This can sometimes be before the Fluid output in the list, so we have to
        // assume it can be anywhere in the list.
        MaterialStack itemMs = materials.stream().filter(ms -> !ms.material.equals(fluidMs.material)).findFirst().orElse(null);

        // Calculate the duration based off of those two possible outputs.
        // - Sum the two Material amounts together (if both exist)
        // - Divide the sum by M
        long duration = fluidMs.amount * fluidMs.material.getMass();
        if (itemMs != null) duration += (itemMs.amount * itemMs.material.getMass());
        duration = Math.max(1L, duration / M);

        // Build the final Recipe.
        RecipeBuilder<?> extractorBuilder = RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(input.copy())
                .fluidOutputs(fluidMs.material.getFluid((int) (fluidMs.amount * L / M)))
                .duration((int) duration)
                .EUt(GTValues.VA[GTValues.LV] * multiplier);

        // Null check the Item before adding it to the Builder.
        // - Try to output an Ingot, otherwise output a Dust.
        if (itemMs != null) {
            OrePrefix outputPrefix = itemMs.material.hasProperty(PropertyKey.INGOT) ? OrePrefix.ingot : OrePrefix.dust;
            extractorBuilder.output(outputPrefix, itemMs.material, (int) (itemMs.amount / M));
        }

        extractorBuilder.buildAndRegister();
    }

    private static void registerArcRecycling(ItemStack input, List<MaterialStack> materials, @Nullable OrePrefix prefix) {
        // Block dusts from being arc'd instead of EBF'd
        MaterialStack ms = OreDictUnifier.getMaterial(input);
        if (prefix == OrePrefix.dust && ms != null && ms.material.hasProperty(PropertyKey.BLAST)) {
            return;
        } else if (prefix == OrePrefix.block) {
            if (ms != null && !ms.material.hasProperty(PropertyKey.GEM)) {
                ItemStack output = OreDictUnifier.get(OrePrefix.ingot, ms.material.getProperty(PropertyKey.INGOT).getArcSmeltInto(), 9);
                RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                        .inputs(input.copy())
                        .outputs(output)
                        .duration(calculateDuration(Collections.singletonList(output)))
                        .EUt(GTValues.VA[GTValues.LV])
                        .buildAndRegister();
            }
            return;
        }

        // Filter down the materials list.
        // - Map to the Arc Smelting result as defined below
        // - Combine any MaterialStacks that have the same Material
        materials = combineStacks(materials.stream()
                .map(RecyclingRecipes::getArcSmeltingResult)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        // Finalize the output List
        List<ItemStack> outputs = finalizeOutputs(
                materials,
                RecipeMaps.ARC_FURNACE_RECIPES.getMaxOutputs(),
                RecyclingRecipes::getArcIngotOrDust
        );

        // Exit if no valid outputs exist for this recycling Recipe.
        if (outputs.size() == 0) return;

        // Build the final Recipe.
        RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                .inputs(input.copy())
                .outputs(outputs)
                .duration(calculateDuration(outputs))
                .EUt(GTValues.VA[GTValues.LV])
                .buildAndRegister();
    }

    private static MaterialStack getArcSmeltingResult(MaterialStack materialStack) {
        Material material = materialStack.material;
        long amount = materialStack.amount;

        if (material.hasFlag(EXPLOSIVE)) {
            return new MaterialStack(Materials.Ash, amount / 16);
        }

        // If the Material is Flammable, return Ash
        if (material.hasFlag(FLAMMABLE)) {
            return new MaterialStack(Materials.Ash, amount / 8);
        }

        // Else if the Material is a Gem, process its output (see below)
        if (material.hasProperty(PropertyKey.GEM)) {
            return getGemArcSmeltResult(materialStack);
        }

        // Else if the Material has NO_SMELTING, return nothing
        if (material.hasFlag(NO_SMELTING)) {
            return null;
        }

        // Else if the Material is an Ingot, return  the Arc Smelting
        // result if it exists, otherwise return the Material itself.
        if (material.hasProperty(PropertyKey.INGOT)) {
            Material arcSmelt = material.getProperty(PropertyKey.INGOT).getArcSmeltInto();
            if (arcSmelt != null) {
                return new MaterialStack(arcSmelt, amount);
            }
        }
        return materialStack;
    }

    private static ItemStack getArcIngotOrDust(@Nonnull MaterialStack stack) {
        if (stack.material == Materials.Carbon) {
            return OreDictUnifier.getDust(stack);
        }
        return OreDictUnifier.getIngotOrDust(stack);
    }

    private static MaterialStack getGemArcSmeltResult(MaterialStack materialStack) {
        Material material = materialStack.material;
        long amount = materialStack.amount;

        // If the Gem Material has Oxygen in it, return Ash
        if (material.getMaterialComponents().stream()
                .anyMatch(stack -> stack.material == Materials.Oxygen)) {
            return new MaterialStack(Materials.Ash, amount / 8);
        }

        // Else if the Gem Material has Carbon in it, return Carbon
        if (material.getMaterialComponents().stream()
                .anyMatch(stack -> stack.material == Materials.Carbon)) {
            return new MaterialStack(Materials.Carbon, amount / 8);
        }

        // Else return Dark Ash
        return new MaterialStack(Materials.DarkAsh, amount / 8);
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
            }
            else if(m.hasFlag(IS_MAGNETIC) && m.hasProperty(PropertyKey.INGOT) && m.getProperty(PropertyKey.INGOT).getSmeltingInto().hasProperty(PropertyKey.BLAST)) {
                BlastProperty prop = m.getProperty(PropertyKey.INGOT).getSmeltingInto().getProperty(PropertyKey.BLAST);
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
    private static int calculateDuration(List<ItemStack> materials) {
        long duration = 0;
        for (ItemStack is : materials) {
            MaterialStack ms = OreDictUnifier.getMaterial(is);
            if (ms != null) duration += ms.amount * ms.material.getMass();
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

    private static List<ItemStack> finalizeOutputs(List<MaterialStack> materials, int maxOutputs, Function<MaterialStack, ItemStack> toItemStackMapper) {

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

    private static void splitStacks(List<Tuple<ItemStack, MaterialStack>> list, ItemStack originalStack, UnificationEntry entry) {
        int amount = originalStack.getCount();
        while (amount > 64) {
            list.add(new Tuple<>(GTUtility.copyAmount(64, originalStack), new MaterialStack(entry.material, entry.orePrefix.getMaterialAmount(entry.material) * 64)));
            amount -= 64;
        }
        list.add(new Tuple<>(GTUtility.copyAmount(amount, originalStack), new MaterialStack(entry.material, entry.orePrefix.getMaterialAmount(entry.material) * amount)));
    }

    private static final List<OrePrefix> DUST_ORDER = ImmutableList.of(OrePrefix.dust, OrePrefix.dustSmall, OrePrefix.dustTiny);
    private static final List<OrePrefix> INGOT_ORDER = ImmutableList.of(OrePrefix.block, OrePrefix.ingot, OrePrefix.nugget);

    private static void shrinkStacks(List<Tuple<ItemStack, MaterialStack>> list, ItemStack originalStack, UnificationEntry entry) {
        Material material = entry.material;
        long materialAmount = originalStack.getCount() * entry.orePrefix.getMaterialAmount(material);

        //noinspection ConstantConditions
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
            splitStacks(list, OreDictUnifier.get(chosenList.get(0), ms.material, (int) (ms.amount / prefix.getMaterialAmount(material))), new UnificationEntry(prefix, material));
        }

        OrePrefix mediumPrefix = chosenList.get(1); // dustSmall or ingot
        OrePrefix smallestPrefix = chosenList.get(2); // dustTiny or nugget
        MaterialStack mediumMS = tempList.get(mediumPrefix); // dustSmall or ingot
        MaterialStack smallestMS = tempList.get(smallestPrefix); // dustTiny or nugget

        // Try to compact the two "lower form" prefixes into one stack, if it doesn't exceed stack size
        if (mediumMS != null && smallestMS != null) {
            long singleStackAmount = mediumMS.amount + smallestMS.amount;
            if (singleStackAmount / smallestPrefix.getMaterialAmount(material) <= 64) {
                list.add(new Tuple<>(OreDictUnifier.get(smallestPrefix, material, (int) (singleStackAmount / smallestPrefix.getMaterialAmount(material))), new MaterialStack(material, singleStackAmount)));
                return;
            }
        }

        // Otherwise simply add the stacks to the List if they exist
        if (mediumMS != null) list.add(new Tuple<>(
                OreDictUnifier.get(mediumPrefix, material, (int) (mediumMS.amount / mediumPrefix.getMaterialAmount(material))),
                new MaterialStack(material, mediumMS.amount)
        ));

        if (smallestMS != null) list.add(new Tuple<>(
                OreDictUnifier.get(smallestPrefix, material, (int) (smallestMS.amount / smallestPrefix.getMaterialAmount(material))),
                new MaterialStack(material, smallestMS.amount)
        ));
    }

    private static boolean isAshMaterial(MaterialStack ms) {
        return ms.material == Materials.Ash || ms.material == Materials.DarkAsh || ms.material == Materials.Carbon;
    }
}
