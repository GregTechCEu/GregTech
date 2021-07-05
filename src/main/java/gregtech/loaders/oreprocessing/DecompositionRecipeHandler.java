package gregtech.loaders.oreprocessing;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.IMaterial;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.*;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static gregtech.api.unification.material.type.Material.MatFlags.DECOMPOSITION_REQUIRES_HYDROGEN;
import static gregtech.api.unification.material.type.Material.MatFlags.DISABLE_DECOMPOSITION;

public class DecompositionRecipeHandler {

    public static void runRecipeGeneration() {
        for (Material material : Material.MATERIAL_REGISTRY) {
            if (material instanceof FluidMaterial) {
                OrePrefix prefix = material instanceof DustMaterial ? OrePrefix.dust : null;
                processDecomposition(prefix, material);
            }
        }
        for (SimpleDustMaterial material : SimpleDustMaterial.MATERIAL_REGISTRY) {
            processDecomposition(OrePrefix.dust, material);
        }
        for (SimpleFluidMaterial material : SimpleFluidMaterial.MATERIAL_REGISTRY) {
            processDecomposition(null, material);
        }
    }

    private static void processDecomposition(OrePrefix decomposePrefix, IMaterial<?> material) {
        if (material.getMaterialComponents().isEmpty() ||
            (!material.hasFlag(Material.MatFlags.DECOMPOSITION_BY_ELECTROLYZING) &&
                !material.hasFlag(Material.MatFlags.DECOMPOSITION_BY_CENTRIFUGING)) ||
            //disable decomposition if explicitly disabled for this material or for one of it's components
            material.hasFlag(DISABLE_DECOMPOSITION)) return;

        ArrayList<ItemStack> outputs = new ArrayList<>();
        ArrayList<FluidStack> fluidOutputs = new ArrayList<>();
        int totalInputAmount = 0;

        //compute outputs
        for (MaterialStack component : material.getMaterialComponents()) {
            totalInputAmount += component.amount;
            if (component.material instanceof DustMaterial) {
                outputs.add(OreDictUnifier.get(OrePrefix.dust, component.material, (int) component.amount));
            } else if (component.material instanceof FluidMaterial) {
                FluidMaterial componentMaterial = (FluidMaterial) component.material;
                fluidOutputs.add(componentMaterial.getFluid((int) (1000 * component.amount)));
            }
        }

        //generate builder
        RecipeBuilder builder;
        if (material.hasFlag(Material.MatFlags.DECOMPOSITION_BY_ELECTROLYZING)) {
            builder = RecipeMaps.ELECTROLYZER_RECIPES.recipeBuilder()
                .duration((int) material.getAverageProtons() * totalInputAmount * 8)
                .EUt(getElectrolyzingVoltage(material.getMaterialComponents().stream()
                    .map(s -> s.material).collect(Collectors.toList())));
        } else {
            builder = RecipeMaps.CENTRIFUGE_RECIPES.recipeBuilder()
                .duration((int) Math.ceil(material.getAverageMass() * totalInputAmount * 1.5))
                .EUt(30);
        }
        builder.outputs(outputs);
        builder.fluidOutputs(fluidOutputs);

        //finish builder
        if (decomposePrefix != null) {
            builder.input(decomposePrefix, material, totalInputAmount);
        } else {
            builder.fluidInputs(material.getFluid(1000));
        }
        if (material.hasFlag(DECOMPOSITION_REQUIRES_HYDROGEN)) {
            builder.fluidInputs(Materials.Hydrogen.getFluid(1000 * totalInputAmount));
        }

        //register recipe
        builder.buildAndRegister();
    }

    //todo think something better with this
    private static int getElectrolyzingVoltage(List<IMaterial<?>> components) {
        //tungsten-containing materials electrolyzing requires 1920
        if (components.contains(Materials.Tungsten))
            return 1920; //EV voltage (tungstate and scheelite electrolyzing)
        //Binary compound materials require 30 EU/t
        if (components.size() <= 2) {
            return 30;
        }
        return Math.min(2, components.size()) * 30;
    }

}
