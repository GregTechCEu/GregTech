package gregtech.loaders.recipe;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static gregtech.api.GTValues.L;
import static gregtech.api.GTValues.M;
import static gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE;

public class RecyclingRecipes {

    // TODO - Durations are still too long
    // TODO - Fix recipe order with some things (noticed Hermetic Casings)
    // TODO - Figure out solution to LuV+ components
    // TODO - (to remember) Do NOT calculate any material component lists for circuits, they are simply totally lost

    public static void init() {
        for (Entry<ItemStack, ItemMaterialInfo> entry : OreDictUnifier.getAllItemInfos()) {
            ItemStack itemStack = entry.getKey();
            ItemMaterialInfo materialInfo = entry.getValue();
            ArrayList<MaterialStack> materialStacks = new ArrayList<>(materialInfo.getMaterials());
            registerRecyclingRecipes(itemStack, materialStacks, false);
        }
    }

    public static void registerRecyclingRecipes(ItemStack input, List<MaterialStack> components, boolean ignoreArcSmelting) {

        // Gather the valid Materials for use in recycling recipes.
        // Filters out any Material that does not have a Dust, and any
        // Materials that do not equate to at least 1 Nugget worth of Material.
        List<MaterialStack> materials = components.stream()
                .filter(stack -> stack.material.hasProperty(PropertyKey.DUST))
                .filter(stack -> stack.amount >= M / 9)
                .collect(Collectors.toList());

        // Exit if no Materials matching the above requirements exist.
        if (materials.isEmpty()) return;

        // Calculate the voltage multiplier based on if a Material has a Blast Property
        int voltageMultiplier = calculateVoltageMultiplier(components);

        // TODO Make sure this is okay
        //do not apply arc smelting for gems, solid materials and dust materials
        //only generate recipes for ingot materials
        if (components.size() == 1 && !components.get(0).material.hasProperty(PropertyKey.INGOT)) {
            ignoreArcSmelting = true;
        }

        // Call to the three recycling methods
        registerMaceratorRecycling(input, components, voltageMultiplier);
        registerExtractorRecycling(input, components, voltageMultiplier);
        if (!ignoreArcSmelting) registerArcRecycling(input, components, voltageMultiplier);
    }

    private static void registerMaceratorRecycling(ItemStack input, List<MaterialStack> materials, int multiplier) {

        // Filter down the materials list.
        // - Filter out Materials that cannot create Dusts
        // - Limit to the maximum number of outputs for the RecipeMap
        materials = materials.stream()
                .filter(ms -> OreDictUnifier.getDust(ms) != ItemStack.EMPTY)
                .limit(RecipeMaps.MACERATOR_RECIPES.getMaxOutputs())
                .collect(Collectors.toList());

        // Exit if no valid Materials exist for this recycling Recipe.
        if (materials.size() == 0) return;

        // Calculate the duration of the Recipe.
        // - Sum the Material amounts together
        // - Multiply by 30, and divide by M
        long duration = 0;
        for (MaterialStack ms : materials) duration += ms.amount;
        duration = Math.max(1L, duration * 30 / M);

        // Build the final Recipe.
        RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
                .inputs(input.copy())
                .outputs(materials.stream().map(OreDictUnifier::getDust).collect(Collectors.toList()))
                .duration((int) duration)
                .EUt(8 * multiplier)
                .buildAndRegister();
    }

    private static void registerExtractorRecycling(ItemStack input, List<MaterialStack> materials, int multiplier) {

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
        // - Multiply by 80 and divide by M
        long duration = fluidMs.amount;
        if (itemMs != null) duration += itemMs.amount;
        duration = Math.max(1L, duration * 80 / M);

        // Build the final Recipe.
        RecipeBuilder<?> extractorBuilder = RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .inputs(input)
                .fluidOutputs(fluidMs.material.getFluid((int) (fluidMs.amount * L / M)))
                .duration((int) duration)
                .EUt(32 * multiplier);

        // Null check the Item before adding it to the Builder
        if (itemMs != null) {
            extractorBuilder.output(OrePrefix.dust, itemMs.material, (int) (itemMs.amount / M));
        }

        extractorBuilder.buildAndRegister();
    }

    private static void registerArcRecycling(ItemStack input, List<MaterialStack> materials, int multiplier) {

        // Filter down the materials list.
        // - Map to the Arc Smelting result as defined below
        // - Filter out empty ItemStacks
        List<ItemStack> arcItemStacks = materials.stream()
                .map(RecyclingRecipes::getArcSmeltingResult)
                .filter(is -> !is.isEmpty())
                .collect(Collectors.toList());

        // Exit if no valid Materials exist for this recycling Recipe.
        if (materials.size() == 0) return;

        // Combine stacks in the List that have the same Item.
        Map<ItemAndMetadata, Integer> outputsExploded = new HashMap<>();
        for (ItemStack stack : arcItemStacks) {
            ItemAndMetadata item = new ItemAndMetadata(stack.getItem(), stack.getItemDamage());
            int amount = outputsExploded.getOrDefault(item, 0);
            outputsExploded.put(item, stack.getCount() + amount);
        }

        // Sort the outputs List, and calculate duration along the way.
        AtomicLong tempDuration = new AtomicLong(0);
        List<ItemStack> outputs = outputsExploded.entrySet().stream()

                // Map <ItemAndMetadata, Integer> to ItemStack
                .map(e -> e.getKey().toItemStack(e.getValue()))

                // Sort based on MaterialStack.amount, and update duration along the way.
                // Despite what syntax highlighting is saying, we know ms cannot be null.
                .sorted(Comparator.comparingLong(is -> {
                    MaterialStack ms = OreDictUnifier.getMaterialWithCount(is);
                    tempDuration.addAndGet(ms.amount);
                    return -ms.amount;
                }))

                // Limit to the maximum amount of outputs for the Arc Furnace
                .limit(RecipeMaps.ARC_FURNACE_RECIPES.getMaxOutputs())
                .collect(Collectors.toList());

        // Calculate the duration of the Recipe.
        // - Sum the Material amounts together
        // - Multiply by 60, and divide by M
        long duration = Math.max(1L, tempDuration.get() * 60 / M);

        // Build the final Recipe.
        RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                .inputs(input)
                .outputs(outputs)
                .duration((int) duration)
                .EUt(30 * multiplier)
                .buildAndRegister();
    }

    // TODO Mess with material amount conversion for Ash/Dark Ash/Carbon
    private static ItemStack getArcSmeltingResult(MaterialStack materialStack) {
        Material material = materialStack.material;
        long amount = materialStack.amount;

        // If the Material is Flammable, return Ash
        if (material.hasFlag(FLAMMABLE)) {
            return OreDictUnifier.getDust(Materials.Ash, amount);
        }

        // Else if the Material is a Gem, process its output (see below)
        if (material.hasProperty(PropertyKey.GEM)) {
            return getGemArcSmeltResult(materialStack);
        }

        // Else if the Material is an Ingot, return an Ingot of the Arc Smelting
        // result if it exists, otherwise return an Ingot of the Material itself
        if (material.hasProperty(PropertyKey.INGOT)) {
            Material arcSmelt = material.getProperty(PropertyKey.INGOT).getArcSmeltInto();
            if (arcSmelt != null)
                return OreDictUnifier.getIngot(arcSmelt, amount);
            return OreDictUnifier.getIngot(material, amount);
        }

        // Else return a Dust of the Material
        return OreDictUnifier.getDust(material, amount);
    }

    private static ItemStack getGemArcSmeltResult(MaterialStack materialStack) {
        Material material = materialStack.material;
        long amount = materialStack.amount;

        // If the Gem Material has Oxygen in it, return Ash
        if (material.getMaterialComponents().stream()
                .anyMatch(stack -> stack.material == Materials.Oxygen)) {
            return OreDictUnifier.getDust(Materials.Ash, amount);
        }

        // Else if the Gem Material has Carbon in it, return Carbon
        if (material.getMaterialComponents().stream()
                .anyMatch(stack -> stack.material == Materials.Carbon)) {
            return OreDictUnifier.getDust(Materials.Carbon, amount);
        }

        // Else return Dark Ash
        return OreDictUnifier.getDust(Materials.DarkAsh, amount);
    }

    private static int calculateVoltageMultiplier(List<MaterialStack> materials) {
        int highestTemp = 0;
        for (MaterialStack ms : materials) {
            Material m = ms.material;
            if (m.hasProperty(PropertyKey.BLAST)) {
                BlastProperty prop = m.getProperty(PropertyKey.BLAST);
                if (prop.getBlastTemperature() > highestTemp) {
                    highestTemp = prop.getBlastTemperature();
                }
            }
        }
        return highestTemp == 0 ? 1 : highestTemp > 2000 ? 16 : 4;
    }
}
