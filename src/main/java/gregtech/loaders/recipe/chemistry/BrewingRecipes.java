package gregtech.loaders.recipe.chemistry;

import gregtech.api.unification.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.recipes.RecipeMaps.BREWING_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.common.items.MetaItems.BIO_CHAFF;

public class BrewingRecipes {

    public static void init() {

        for (Material material : new Material[]{Talc, Soapstone, Redstone}) {
            BREWING_RECIPES.recipeBuilder()
                    .input(dust, material)
                    .fluidInputs(Oil.getFluid(1000))
                    .fluidOutputs(Lubricant.getFluid(1000))
                    .duration(128).EUt(4).buildAndRegister();

            BREWING_RECIPES.recipeBuilder()
                    .input(dust, material)
                    .fluidInputs(Creosote.getFluid(1000))
                    .fluidOutputs(Lubricant.getFluid(1000))
                    .duration(128).EUt(4).buildAndRegister();

            BREWING_RECIPES.recipeBuilder()
                    .input(dust, material)
                    .fluidInputs(SeedOil.getFluid(1000))
                    .fluidOutputs(Lubricant.getFluid(1000))
                    .duration(128).EUt(4).buildAndRegister();
        }

        // Biomass
        BREWING_RECIPES.recipeBuilder().duration(800).EUt(3).input("treeSapling", 1).fluidInputs(Water.getFluid(100)).fluidOutputs(Biomass.getFluid(100)).buildAndRegister();
        BREWING_RECIPES.recipeBuilder().duration(160).EUt(3).inputs(new ItemStack(Items.POTATO)).fluidInputs(Water.getFluid(20)).fluidOutputs(Biomass.getFluid(20)).buildAndRegister();
        BREWING_RECIPES.recipeBuilder().duration(160).EUt(3).inputs(new ItemStack(Items.CARROT)).fluidInputs(Water.getFluid(20)).fluidOutputs(Biomass.getFluid(20)).buildAndRegister();
        BREWING_RECIPES.recipeBuilder().duration(160).EUt(3).inputs(new ItemStack(Blocks.CACTUS)).fluidInputs(Water.getFluid(20)).fluidOutputs(Biomass.getFluid(20)).buildAndRegister();
        BREWING_RECIPES.recipeBuilder().duration(160).EUt(3).inputs(new ItemStack(Items.REEDS)).fluidInputs(Water.getFluid(20)).fluidOutputs(Biomass.getFluid(20)).buildAndRegister();
        BREWING_RECIPES.recipeBuilder().duration(160).EUt(3).inputs(new ItemStack(Blocks.BROWN_MUSHROOM)).fluidInputs(Water.getFluid(20)).fluidOutputs(Biomass.getFluid(20)).buildAndRegister();
        BREWING_RECIPES.recipeBuilder().duration(160).EUt(3).inputs(new ItemStack(Blocks.RED_MUSHROOM)).fluidInputs(Water.getFluid(20)).fluidOutputs(Biomass.getFluid(20)).buildAndRegister();
        BREWING_RECIPES.recipeBuilder().duration(160).EUt(3).inputs(new ItemStack(Items.BEETROOT)).fluidInputs(Water.getFluid(20)).fluidOutputs(Biomass.getFluid(20)).buildAndRegister();
        BREWING_RECIPES.recipeBuilder().EUt(4).duration(128).input(BIO_CHAFF).fluidInputs(Water.getFluid(750)).fluidOutputs(Biomass.getFluid(750)).buildAndRegister();
    }
}
