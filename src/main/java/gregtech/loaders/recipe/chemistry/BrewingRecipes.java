package gregtech.loaders.recipe.chemistry;

import gregtech.api.unification.material.type.DustMaterial;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import static gregtech.api.recipes.RecipeMaps.BREWING_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.common.items.MetaItems.PLANT_BALL;

public class BrewingRecipes {

    public static void init() {
        BREWING_RECIPES.recipeBuilder()
                .input(PLANT_BALL)
                .fluidInputs(Honey.getFluid(180))
                .fluidOutputs(Biomass.getFluid(270))
                .duration(1440).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input("treeSapling", 1)
                .fluidInputs(Honey.getFluid(100))
                .fluidOutputs(Biomass.getFluid(150))
                .duration(600).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Items.POTATO)
                .fluidInputs(Honey.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Items.CARROT)
                .fluidInputs(Honey.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Blocks.CACTUS)
                .fluidInputs(Honey.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Items.REEDS)
                .fluidInputs(Honey.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Blocks.BROWN_MUSHROOM)
                .fluidInputs(Honey.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Blocks.RED_MUSHROOM)
                .fluidInputs(Honey.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Items.BEETROOT)
                .fluidInputs(Honey.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .inputs(PLANT_BALL.getStackForm())
                .fluidInputs(Juice.getFluid(180))
                .fluidOutputs(Biomass.getFluid(270))
                .duration(1440).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input("treeSapling", 1)
                .fluidInputs(Juice.getFluid(100))
                .fluidOutputs(Biomass.getFluid(150))
                .duration(600).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Items.POTATO)
                .fluidInputs(Juice.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Items.CARROT)
                .fluidInputs(Juice.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Blocks.CACTUS)
                .fluidInputs(Juice.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Items.REEDS)
                .fluidInputs(Juice.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Blocks.BROWN_MUSHROOM)
                .fluidInputs(Juice.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Blocks.RED_MUSHROOM)
                .fluidInputs(Juice.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        BREWING_RECIPES.recipeBuilder()
                .input(Items.BEETROOT)
                .fluidInputs(Juice.getFluid(20))
                .fluidOutputs(Biomass.getFluid(30))
                .duration(160).EUt(3).buildAndRegister();

        for (DustMaterial dustMaterial : new DustMaterial[]{Talc, Soapstone, Redstone}) {
            BREWING_RECIPES.recipeBuilder()
                    .input(dust, dustMaterial)
                    .fluidInputs(Oil.getFluid(750))
                    .fluidOutputs(Lubricant.getFluid(750))
                    .duration(128).EUt(4).buildAndRegister();

            BREWING_RECIPES.recipeBuilder()
                    .input(dust, dustMaterial)
                    .fluidInputs(Creosote.getFluid(750))
                    .fluidOutputs(Lubricant.getFluid(750))
                    .duration(128).EUt(4).buildAndRegister();

            BREWING_RECIPES.recipeBuilder()
                    .input(dust, dustMaterial)
                    .fluidInputs(SeedOil.getFluid(750))
                    .fluidOutputs(Lubricant.getFluid(750))
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
    }
}
