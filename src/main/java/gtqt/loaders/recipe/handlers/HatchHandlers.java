package gtqt.loaders.recipe.handlers;

import gregtech.api.GTValues;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.util.Mods;

import net.minecraft.item.ItemStack;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.Polytetrafluoroethylene;
import static gregtech.api.unification.ore.OrePrefix.circuit;
import static gregtech.common.metatileentities.MetaTileEntities.*;
import static gtqt.common.metatileentities.GTQTMetaTileEntities.*;

public class HatchHandlers {
    static ItemStack normalInterface = Mods.AppliedEnergistics2.getItem("interface");
    static ItemStack fluidInterface = Mods.AppliedEnergistics2.getItem("fluid_interface");
    public static void init() {
        //Dual Hatch
        for (int i = 0; i < DUAL_IMPORT_HATCH.length; i++) {
            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(ITEM_IMPORT_BUS[i+1])
                    .input(FLUID_IMPORT_HATCH[i+1])
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i+1].toLowerCase()), 4)
                    .output(DUAL_IMPORT_HATCH[i])
                    .duration(100).EUt(VA[ULV + i]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(ITEM_EXPORT_BUS[i+1])
                    .input(FLUID_EXPORT_HATCH[i+1])
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i+1].toLowerCase()), 4)
                    .output(DUAL_EXPORT_HATCH[i])
                    .duration(100).EUt(VA[ULV + i]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .input(DUAL_IMPORT_HATCH[i])
                    .inputs(normalInterface)
                    .inputs(fluidInterface)
                    .input(circuit, MarkerMaterial.create(GTValues.VN[i+1].toLowerCase()), 4)
                    .output(ME_PATTERN_PROVIDER[i])
                    .duration(100).EUt(VA[ULV + i]).buildAndRegister();
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
