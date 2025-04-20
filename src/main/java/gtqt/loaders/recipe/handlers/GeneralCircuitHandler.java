package gtqt.loaders.recipe.handlers;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.stack.UnificationEntry;

import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.PACKER_RECIPES;
import static gregtech.api.unification.ore.OrePrefix.circuit;
import static gtqt.common.items.GTQTMetaItems.*;

public class GeneralCircuitHandler {
    public static void init()
    {
        //  General Circuits
        //  ULV
        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.ULV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_ULV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  LV
        ModHandler.addShapedRecipe("general_circuit.lv", GENERAL_CIRCUIT_LV.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.LV));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.LV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_LV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  MV
        ModHandler.addShapedRecipe("general_circuit.mv", GENERAL_CIRCUIT_MV.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.MV));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.MV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_MV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  HV
        ModHandler.addShapedRecipe("general_circuit.hv", GENERAL_CIRCUIT_HV.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.HV));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.HV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_HV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  EV
        ModHandler.addShapedRecipe("general_circuit.ev", GENERAL_CIRCUIT_EV.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.EV));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.EV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_EV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  IV
        ModHandler.addShapedRecipe("general_circuit.iv", GENERAL_CIRCUIT_IV.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.IV));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.IV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_IV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  LuV
        ModHandler.addShapedRecipe("general_circuit.luv", GENERAL_CIRCUIT_LuV.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.LuV));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.LuV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_LuV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  ZPM
        ModHandler.addShapedRecipe("general_circuit.zpm", GENERAL_CIRCUIT_ZPM.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.ZPM));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.ZPM)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_ZPM)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  UV
        ModHandler.addShapedRecipe("general_circuit.uv", GENERAL_CIRCUIT_UV.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.UV));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.UV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_UV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  UHV
        ModHandler.addShapedRecipe("general_circuit.uhv", GENERAL_CIRCUIT_UHV.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.UHV));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.UHV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_UHV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  UEV
        ModHandler.addShapedRecipe("general_circuit.uev", GENERAL_CIRCUIT_UEV.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.UEV));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.UEV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_UEV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  UIV
        ModHandler.addShapedRecipe("general_circuit.uiv", GENERAL_CIRCUIT_UIV.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.UIV));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.UIV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_UIV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  UXV
        ModHandler.addShapedRecipe("general_circuit.uxv", GENERAL_CIRCUIT_UXV.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.UXV));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.UXV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_UXV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  OpV
        ModHandler.addShapedRecipe("general_circuit.opv", GENERAL_CIRCUIT_OpV.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.OpV));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.OpV)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_OpV)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();

        //  MAX
        ModHandler.addShapedRecipe("general_circuit.max", GENERAL_CIRCUIT_MAX.getStackForm(),
                " X ",
                'X', new UnificationEntry(circuit, MarkerMaterials.Tier.MAX));

        PACKER_RECIPES.recipeBuilder()
                .input(circuit, MarkerMaterials.Tier.MAX)
                .circuitMeta(5)
                .output(GENERAL_CIRCUIT_MAX)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();
    }

}
