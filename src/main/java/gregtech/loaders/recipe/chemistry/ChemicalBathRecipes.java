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
                .input(dust, Wood)
                .fluidInputs(Water.getFluid(100))
                .output(Items.PAPER)
                .duration(200).EUt(4).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(dust, Paper)
                .fluidInputs(Water.getFluid(100))
                .output(Items.PAPER)
                .duration(100).EUt(4).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(Items.REEDS, 1, true)
                .fluidInputs(Water.getFluid(100))
                .output(Items.PAPER)
                .duration(100).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(dust, Wood)
                .fluidInputs(DistilledWater.getFluid(100))
                .output(Items.PAPER)
                .duration(200).EUt(4).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(dust, Paper)
                .fluidInputs(DistilledWater.getFluid(100))
                .output(Items.PAPER)
                .duration(100).EUt(4).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(Items.REEDS, 1, true)
                .fluidInputs(DistilledWater.getFluid(100))
                .output(Items.PAPER)
                .duration(100).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input("plankWood", 1)
                .fluidInputs(Creosote.getFluid(100))
                .outputs(MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.TREATED_PLANK))
                .duration(100).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .inputs(MetaBlocks.STONE_BLOCKS.get(StoneVariant.SMOOTH)
                        .getItemVariant(StoneVariantBlock.StoneType.CONCRETE_LIGHT))
                .fluidInputs(Water.getFluid(100))
                .outputs(MetaBlocks.STONE_BLOCKS.get(StoneVariant.SMOOTH)
                        .getItemVariant(StoneVariantBlock.StoneType.CONCRETE_DARK))
                .duration(100).EUt(VA[ULV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(dust, Scheelite, 6)
                .fluidInputs(HydrochloricAcid.getFluid(2000))
                .output(dust, TungsticAcid, 7)
                .output(dust, CalciumChloride, 3)
                .duration(210).EUt(960).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(dust, Tungstate, 7)
                .fluidInputs(HydrochloricAcid.getFluid(2000))
                .output(dust, TungsticAcid, 7)
                .output(dust, LithiumChloride, 4)
                .duration(210).EUt(960).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(ingotHot, Kanthal)
                .fluidInputs(Water.getFluid(100))
                .output(ingot, Kanthal)
                .duration(400).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(ingotHot, Kanthal)
                .fluidInputs(DistilledWater.getFluid(100))
                .output(ingot, Kanthal)
                .duration(250).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(ingotHot, Silicon)
                .fluidInputs(Water.getFluid(100))
                .output(ingot, Silicon)
                .duration(200).EUt(VA[MV]).buildAndRegister();

        CHEMICAL_BATH_RECIPES.recipeBuilder()
                .input(ingotHot, Silicon)
                .fluidInputs(DistilledWater.getFluid(100))
                .output(ingot, Silicon)
                .duration(125).EUt(VA[MV]).buildAndRegister();
    }
}
