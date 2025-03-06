package gregtech.loaders.recipe.chemistry;

import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import gregtech.common.blocks.StoneVariantBlock.StoneVariant;
import gregtech.common.blocks.wood.BlockGregPlanks;

import net.minecraft.init.Items;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.CHEMICAL_BATH_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;

public class ChemicalBathRecipes {

    public static void init() {
        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(dust, Wood)
                .fluidInputs(Water.getFluid(100))
                .outputItem(Items.PAPER)
                .duration(200).volts(4).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(dust, Paper)
                .fluidInputs(Water.getFluid(100))
                .outputItem(Items.PAPER)
                .duration(100).volts(4).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(Items.REEDS, 1)
                .fluidInputs(Water.getFluid(100))
                .outputItem(Items.PAPER)
                .duration(100).volts(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(dust, Wood)
                .fluidInputs(DistilledWater.getFluid(100))
                .outputItem(Items.PAPER)
                .duration(200).volts(4).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(dust, Paper)
                .fluidInputs(DistilledWater.getFluid(100))
                .outputItem(Items.PAPER)
                .duration(100).volts(4).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(Items.REEDS, 1)
                .fluidInputs(DistilledWater.getFluid(100))
                .outputItem(Items.PAPER)
                .duration(100).volts(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem("plankWood", 1)
                .fluidInputs(Creosote.getFluid(100))
                .outputs(MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK))
                .duration(100).volts(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.STONE_BLOCKS.get(StoneVariant.SMOOTH)
                        .getItemVariant(StoneVariantBlock.StoneType.CONCRETE_LIGHT))
                .fluidInputs(Water.getFluid(100))
                .outputs(MetaBlocks.STONE_BLOCKS.get(StoneVariant.SMOOTH)
                        .getItemVariant(StoneVariantBlock.StoneType.CONCRETE_DARK))
                .duration(100).volts(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(dust, Scheelite, 6)
                .fluidInputs(HydrochloricAcid.getFluid(2000))
                .outputItem(dust, TungsticAcid, 7)
                .outputItem(dust, CalciumChloride, 3)
                .duration(210).volts(960).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(dust, Tungstate, 7)
                .fluidInputs(HydrochloricAcid.getFluid(2000))
                .outputItem(dust, TungsticAcid, 7)
                .outputItem(dust, LithiumChloride, 4)
                .duration(210).volts(960).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(ingotHot, Kanthal)
                .fluidInputs(Water.getFluid(100))
                .outputItem(ingot, Kanthal)
                .duration(400).volts(VA[MV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(ingotHot, Kanthal)
                .fluidInputs(DistilledWater.getFluid(100))
                .outputItem(ingot, Kanthal)
                .duration(250).volts(VA[MV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(ingotHot, Silicon)
                .fluidInputs(Water.getFluid(100))
                .outputItem(ingot, Silicon)
                .duration(200).volts(VA[MV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(ingotHot, Silicon)
                .fluidInputs(DistilledWater.getFluid(100))
                .outputItem(ingot, Silicon)
                .duration(125).volts(VA[MV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(ingotHot, BlackSteel)
                .fluidInputs(Water.getFluid(100))
                .outputItem(ingot, BlackSteel)
                .duration(200).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(ingotHot, BlackSteel)
                .fluidInputs(DistilledWater.getFluid(100))
                .outputItem(ingot, BlackSteel)
                .duration(125).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(ingotHot, RedSteel)
                .fluidInputs(Water.getFluid(100))
                .outputItem(ingot, RedSteel)
                .duration(400).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(ingotHot, RedSteel)
                .fluidInputs(DistilledWater.getFluid(100))
                .outputItem(ingot, RedSteel)
                .duration(250).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(ingotHot, BlueSteel)
                .fluidInputs(Water.getFluid(100))
                .outputItem(ingot, BlueSteel)
                .duration(400).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputItem(ingotHot, BlueSteel)
                .fluidInputs(DistilledWater.getFluid(100))
                .outputItem(ingot, BlueSteel)
                .duration(250).EUt(VA[MV]).buildAndRegister();
    }
}
