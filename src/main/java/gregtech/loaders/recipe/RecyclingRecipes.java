package gregtech.loaders.recipe;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static gregtech.api.GTValues.L;
import static gregtech.api.GTValues.M;
import static gregtech.api.unification.material.info.MaterialFlags.FLAMMABLE;

public class RecyclingRecipes {

    public static void init() {
        initializeArcRecyclingRecipes();
    }

    private static void initializeArcRecyclingRecipes() {
        for (Entry<ItemStack, ItemMaterialInfo> entry : OreDictUnifier.getAllItemInfos()) {
            ItemStack itemStack = entry.getKey();
            ItemMaterialInfo materialInfo = entry.getValue();
            ArrayList<MaterialStack> materialStacks = new ArrayList<>();
            materialStacks.add(materialInfo.material);
            materialStacks.addAll(materialInfo.additionalComponents);
            registerArcRecyclingRecipe(b -> b.inputs(itemStack), materialStacks, false);
        }
    }

    public static void registerArcRecyclingRecipe(Consumer<RecipeBuilder<?>> inputSupplier, List<MaterialStack> components, boolean ignoreArcSmelting) {
        List<MaterialStack> materials = components.stream()
            .filter(stack -> stack.material.hasProperty(PropertyKey.DUST))
            .filter(stack -> stack.amount >= M / 9) //do only materials which have at least one nugget
            .collect(Collectors.toList());
        if (materials.isEmpty()) return;
        MaterialStack firstStack = materials.get(0);
        Material material = firstStack.material;
        int voltageMultiplier = 1;
        if (material.hasProperty(PropertyKey.BLAST)) {
            int blastFurnaceTemperature = material.getProperty(PropertyKey.BLAST).getBlastTemperature();
            voltageMultiplier = blastFurnaceTemperature == 0 ? 1 : blastFurnaceTemperature > 2000 ? 16 : 4;
        } else if (!material.hasProperty(PropertyKey.INGOT)){
            //do not apply arc smelting for gems, solid materials and dust materials
            //only generate recipes for ingot materials
            ignoreArcSmelting = true;
        }

        RecipeBuilder<?> maceratorRecipeBuilder = RecipeMaps.MACERATOR_RECIPES.recipeBuilder()
            .outputs(materials.stream().map(OreDictUnifier::getDust).collect(Collectors.toList()))
            .duration((int) Math.max(1L, firstStack.amount * 30 / M))
            .EUt(8 * voltageMultiplier);
        inputSupplier.accept(maceratorRecipeBuilder);
        maceratorRecipeBuilder.buildAndRegister();

        if (material.hasFluid()) {
            RecipeBuilder<?> fluidExtractorRecipeBuilder = RecipeMaps.EXTRACTOR_RECIPES.recipeBuilder()
                .fluidOutputs(material.getFluid((int) (firstStack.amount * L / M)))
                .duration((int) Math.max(1L, firstStack.amount * 80 / M))
                .EUt(32 * voltageMultiplier);
            inputSupplier.accept(fluidExtractorRecipeBuilder);
            fluidExtractorRecipeBuilder.buildAndRegister();
        }

        if (!ignoreArcSmelting) {
            List<ItemStack> resultList = materials.stream().map(RecyclingRecipes::getArcSmeltingResult).collect(Collectors.toList());
            resultList.removeIf(ItemStack::isEmpty);
            if (resultList.isEmpty()) return;
            RecipeBuilder<?> arcFurnaceRecipeBuilder = RecipeMaps.ARC_FURNACE_RECIPES.recipeBuilder()
                .outputs(resultList)
                .duration((int) Math.max(1L, firstStack.amount * 60 / M))
                .EUt(30 * voltageMultiplier);
            inputSupplier.accept(arcFurnaceRecipeBuilder);
            arcFurnaceRecipeBuilder.buildAndRegister();
        }
    }

    private static ItemStack getArcSmeltingResult(MaterialStack materialStack) {
        Material material = materialStack.material;
        long materialAmount = materialStack.amount;
        if (material.hasFlag(FLAMMABLE)) {
            return OreDictUnifier.getDust(Materials.Ash, materialAmount);
        } else if (material.hasProperty(PropertyKey.GEM)) {
            if (materialStack.material.getMaterialComponents().stream()
                .anyMatch(stack -> stack.material == Materials.Oxygen)) {
                return OreDictUnifier.getDust(Materials.Ash, materialAmount);
            }
            if (materialStack.material.getMaterialComponents().stream()
                .anyMatch(stack -> stack.material == Materials.Carbon)) {
                return OreDictUnifier.getDust(Materials.Carbon, materialAmount);
            }
            return OreDictUnifier.getDust(Materials.DarkAsh, materialAmount);
        } else if (material.hasProperty(PropertyKey.INGOT)) {
            Material arcSmelt = material.getProperty(PropertyKey.INGOT).getArcSmeltInto();
            if (arcSmelt != null)
                return OreDictUnifier.getIngot(arcSmelt, materialAmount);
            return OreDictUnifier.getIngot(material, materialAmount);
        } else {
            return OreDictUnifier.getDust(material, materialAmount);
        }
    }
}
