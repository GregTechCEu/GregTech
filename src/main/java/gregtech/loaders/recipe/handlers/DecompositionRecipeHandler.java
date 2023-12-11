package gregtech.loaders.recipe.handlers;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.unification.material.info.MaterialFlags.*;

public class DecompositionRecipeHandler {

    public static void runRecipeGeneration() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            OrePrefix prefix = material.hasProperty(PropertyKey.DUST) ? OrePrefix.dust : null;
            processDecomposition(prefix, material);
        }
    }

    private static void processDecomposition(OrePrefix decomposePrefix, Material material) {
        if (material.getMaterialComponents().isEmpty() ||
                (!material.hasFlag(DECOMPOSITION_BY_ELECTROLYZING) &&
                        !material.hasFlag(DECOMPOSITION_BY_CENTRIFUGING)) ||
                // disable decomposition if explicitly disabled for this material or for one of it's components
                material.hasFlag(DISABLE_DECOMPOSITION) ||
                material.getMaterialComponents().size() > 6)
            return;

        List<ItemStack> outputs = new ArrayList<>();
        List<FluidStack> fluidOutputs = new ArrayList<>();
        int totalInputAmount = 0;

        // compute outputs
        for (MaterialStack component : material.getMaterialComponents()) {
            totalInputAmount += component.amount;
            if (component.material.hasProperty(PropertyKey.DUST)) {
                outputs.add(OreDictUnifier.get(OrePrefix.dust, component.material, (int) component.amount));
            } else if (component.material.hasProperty(PropertyKey.FLUID)) {
                fluidOutputs.add(component.material.getFluid((int) (1000 * component.amount)));
            }
        }

        // only reduce items
        if (decomposePrefix != null) {
            // calculate lowest common denominator
            List<Integer> materialAmounts = new ArrayList<>();
            materialAmounts.add(totalInputAmount);
            outputs.forEach(itemStack -> materialAmounts.add(itemStack.getCount()));
            fluidOutputs.forEach(fluidStack -> materialAmounts.add(fluidStack.amount / 1000));

            int highestDivisor = 1;

            int smallestMaterialAmount = getSmallestMaterialAmount(materialAmounts);
            for (int i = 2; i <= smallestMaterialAmount; i++) {
                if (isEveryMaterialReducible(i, materialAmounts))
                    highestDivisor = i;
            }

            // divide components
            if (highestDivisor != 1) {
                List<ItemStack> reducedOutputs = new ArrayList<>();

                for (ItemStack itemStack : outputs) {
                    ItemStack reducedStack = itemStack.copy();
                    reducedStack.setCount(reducedStack.getCount() / highestDivisor);
                    reducedOutputs.add(reducedStack);
                }

                List<FluidStack> reducedFluidOutputs = new ArrayList<>();

                for (FluidStack fluidStack : fluidOutputs) {
                    FluidStack reducedFluidStack = fluidStack.copy();
                    reducedFluidStack.amount /= highestDivisor;
                    reducedFluidOutputs.add(reducedFluidStack);
                }

                outputs = reducedOutputs;
                fluidOutputs = reducedFluidOutputs;
                totalInputAmount /= highestDivisor;
            }
        }

        // generate builder
        RecipeBuilder<?> builder;
        if (material.hasFlag(DECOMPOSITION_BY_ELECTROLYZING)) {
            builder = RecipeMaps.ELECTROLYZER_RECIPES.recipeBuilder()
                    .duration(((int) material.getProtons() * totalInputAmount * 2))
                    .EUt(material.getMaterialComponents().size() <= 2 ? VA[LV] : 2 * VA[LV]);
        } else {
            builder = RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                    .duration((int) Math.ceil(material.getMass() * totalInputAmount * 1.5))
                    .EUt(VA[LV]);
        }
        builder.outputs(outputs);
        builder.fluidOutputs(fluidOutputs);

        // finish builder
        if (decomposePrefix != null) {
            builder.input(decomposePrefix, material, totalInputAmount);
        } else {
            builder.fluidInputs(material.getFluid(1000));
        }

        // register recipe
        builder.buildAndRegister();
    }

    private static boolean isEveryMaterialReducible(int divisor, List<Integer> materialAmounts) {
        for (int amount : materialAmounts) {
            if (amount % divisor != 0)
                return false;
        }
        return true;
    }

    private static int getSmallestMaterialAmount(List<Integer> materialAmounts) {
        return materialAmounts.stream().min(Integer::compare).orElse(0);
    }
}
