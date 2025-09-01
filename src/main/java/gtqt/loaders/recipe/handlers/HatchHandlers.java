package gtqt.loaders.recipe.handlers;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.util.Mods;

import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.ore.OrePrefix.circuit;
import static gregtech.common.metatileentities.MetaTileEntities.*;
import static gtqt.api.util.MaterialHelper.Plastic;
import static gtqt.common.metatileentities.GTQTMetaTileEntities.*;

public class HatchHandlers {

    static ItemStack normalInterface = Mods.AppliedEnergistics2.getItem("interface");
    static ItemStack fluidInterface = Mods.AppliedEnergistics2.getItem("fluid_interface");

    public static void init() {
        for (int i = 0; i < DUAL_IMPORT_HATCH.length; i++) {
            if (DUAL_IMPORT_HATCH[i] != null && DUAL_EXPORT_HATCH[i] != null) {

                ModHandler.addShapedRecipe("item_dual_output_to_input_" + DUAL_IMPORT_HATCH[i].getTier(),
                        DUAL_IMPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', DUAL_EXPORT_HATCH[i].getStackForm());
                ModHandler.addShapedRecipe("item_dual_input_to_output_" + DUAL_EXPORT_HATCH[i].getTier(),
                        DUAL_EXPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', DUAL_IMPORT_HATCH[i].getStackForm());

                ModHandler.addShapedRecipe("huge_item_dual_output_to_input_" + HUGE_DUAL_IMPORT_HATCH[i].getTier(),
                        HUGE_DUAL_IMPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', HUGE_DUAL_EXPORT_HATCH[i].getStackForm());
                ModHandler.addShapedRecipe("huge_item_dual_input_to_output_" + HUGE_DUAL_EXPORT_HATCH[i].getTier(),
                        HUGE_DUAL_EXPORT_HATCH[i].getStackForm(),
                        "d", "B", 'B', HUGE_DUAL_IMPORT_HATCH[i].getStackForm());
            }
        }
        //Dual Hatch
        for (int i = 0; i < 9; i++) {
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(ITEM_IMPORT_BUS[i + 1])
                    .input(FLUID_IMPORT_HATCH[i + 1])
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i + 1].toLowerCase()), 4)
                    .fluidInputs(Plastic.get(i).getFluid(L * 4))
                    .output(DUAL_IMPORT_HATCH[i])
                    .duration(100).EUt(VA[ULV + i]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(ITEM_EXPORT_BUS[i + 1])
                    .input(FLUID_EXPORT_HATCH[i + 1])
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i + 1].toLowerCase()), 4)
                    .fluidInputs(Plastic.get(i).getFluid(L * 4))
                    .output(DUAL_EXPORT_HATCH[i])
                    .duration(100).EUt(VA[ULV + i]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(DUAL_IMPORT_HATCH[i])
                    .inputs(normalInterface)
                    .inputs(fluidInterface)
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i + 1].toLowerCase()), 8)
                    .fluidInputs(Plastic.get(i).getFluid(L * 4))
                    .output(ME_PATTERN_PROVIDER[i])
                    .duration(100).EUt(VA[ULV + i]).buildAndRegister();

            //巨型 普通仓+一堆超级缸超级箱
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(DUAL_IMPORT_HATCH[i])
                    .input(QUANTUM_CHEST[9], (int) Math.pow(i + 1, 2))
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i + 1].toLowerCase()), 16)
                    .fluidInputs(Plastic.get(i).getFluid(L * 4))
                    .output(HUGE_DUAL_IMPORT_HATCH[i])
                    .duration(400).EUt(VA[ULV + i]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(DUAL_EXPORT_HATCH[i + 1])
                    .input(QUANTUM_TANK[9], (int) Math.pow(i + 1, 2))
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i + 1].toLowerCase()), 16)
                    .fluidInputs(Plastic.get(i).getFluid(L * 4))
                    .output(HUGE_DUAL_EXPORT_HATCH[i])
                    .duration(400).EUt(VA[ULV + i]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(ME_PATTERN_PROVIDER[i])
                    .input(QUANTUM_CHEST[9], (int) Math.pow(i + 1, 2))
                    .input(QUANTUM_TANK[9], (int) Math.pow(i + 1, 2))
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i + 1].toLowerCase()), 32)
                    .fluidInputs(Plastic.get(i).getFluid(L * 4))
                    .output(HUGE_ME_PATTERN_PROVIDER[i])
                    .duration(400).EUt(VA[ULV + i]).buildAndRegister();

            //巨型
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(ITEM_IMPORT_BUS[i])
                    .input(QUANTUM_CHEST[9], (int) Math.pow(i + 1, 2))
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i + 1].toLowerCase()), 8)
                    .fluidInputs(Plastic.get(i).getFluid(L * 4))
                    .output(HUGE_ITEM_IMPORT_BUS[i])
                    .duration(400).EUt(VA[ULV + i]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(ITEM_EXPORT_BUS[i])
                    .input(QUANTUM_TANK[9], (int) Math.pow(i + 1, 2))
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i + 1].toLowerCase()), 8)
                    .fluidInputs(Plastic.get(i).getFluid(L * 4))
                    .output(HUGE_ITEM_EXPORT_BUS[i])
                    .duration(400).EUt(VA[ULV + i]).buildAndRegister();
        }

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(STOCKING_BUS_ME)
                .input(STOCKING_HATCH_ME)
                .input(DUAL_IMPORT_HATCH[IV])
                .input(circuit, MarkerMaterial.create(GTValues.VN[IV].toLowerCase()), 4)
                .output(ME_DUAL_IMPORT_HATCH)
                .duration(100).EUt(VA[IV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(STOCKING_BUS_ME)
                .input(STOCKING_HATCH_ME)
                .input(DUAL_EXPORT_HATCH[IV])
                .input(circuit, MarkerMaterial.create(GTValues.VN[IV].toLowerCase()), 4)
                .output(ME_DUAL_EXPORT_HATCH)
                .duration(100).EUt(VA[IV]).buildAndRegister();
    }
}
