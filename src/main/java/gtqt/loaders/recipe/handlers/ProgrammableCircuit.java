package gtqt.loaders.recipe.handlers;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;

import static gregtech.api.GTValues.*;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.FORMING_PRESS_RECIPES;
import static gregtech.api.unification.material.Materials.Stone;
import static gregtech.api.unification.ore.OrePrefix.plate;
import static gregtech.common.items.MetaItems.*;
import static gtqt.common.items.GTQTMetaItems.*;

public class ProgrammableCircuit {
    public static void init() {
        ProgrameCircuit(0, PROGRAMMABLE_CIRCUIT_0);
        ProgrameCircuit(1, PROGRAMMABLE_CIRCUIT_1);
        ProgrameCircuit(2, PROGRAMMABLE_CIRCUIT_2);
        ProgrameCircuit(3, PROGRAMMABLE_CIRCUIT_3);
        ProgrameCircuit(4, PROGRAMMABLE_CIRCUIT_4);
        ProgrameCircuit(5, PROGRAMMABLE_CIRCUIT_5);
        ProgrameCircuit(6, PROGRAMMABLE_CIRCUIT_6);
        ProgrameCircuit(7, PROGRAMMABLE_CIRCUIT_7);
        ProgrameCircuit(8, PROGRAMMABLE_CIRCUIT_8);
        ProgrameCircuit(9, PROGRAMMABLE_CIRCUIT_9);
        ProgrameCircuit(10, PROGRAMMABLE_CIRCUIT_10);
        ProgrameCircuit(11, PROGRAMMABLE_CIRCUIT_11);
        ProgrameCircuit(12, PROGRAMMABLE_CIRCUIT_12);
        ProgrameCircuit(13, PROGRAMMABLE_CIRCUIT_13);
        ProgrameCircuit(14, PROGRAMMABLE_CIRCUIT_14);
        ProgrameCircuit(15, PROGRAMMABLE_CIRCUIT_15);
        ProgrameCircuit(16, PROGRAMMABLE_CIRCUIT_16);
        ProgrameCircuit(17, PROGRAMMABLE_CIRCUIT_17);
        ProgrameCircuit(18, PROGRAMMABLE_CIRCUIT_18);
        ProgrameCircuit(19, PROGRAMMABLE_CIRCUIT_19);
        ProgrameCircuit(20, PROGRAMMABLE_CIRCUIT_20);
        ProgrameCircuit(21, PROGRAMMABLE_CIRCUIT_21);
        ProgrameCircuit(22, PROGRAMMABLE_CIRCUIT_22);
        ProgrameCircuit(23, PROGRAMMABLE_CIRCUIT_23);
        ProgrameCircuit(24, PROGRAMMABLE_CIRCUIT_24);
        ProgrameCircuit(25, PROGRAMMABLE_CIRCUIT_25);
        ProgrameCircuit(26, PROGRAMMABLE_CIRCUIT_26);
        ProgrameCircuit(27, PROGRAMMABLE_CIRCUIT_27);
        ProgrameCircuit(28, PROGRAMMABLE_CIRCUIT_28);
        ProgrameCircuit(29, PROGRAMMABLE_CIRCUIT_29);
        ProgrameCircuit(30, PROGRAMMABLE_CIRCUIT_30);
        ProgrameCircuit(31, PROGRAMMABLE_CIRCUIT_31);
        ProgrameCircuit(32, PROGRAMMABLE_CIRCUIT_32);

        ASSEMBLER_RECIPES.recipeBuilder()
                .input(ROBOT_ARM_HV)
                .input(CONVEYOR_MODULE_HV)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.HV)
                .circuitMeta(7)
                .fluidInputs(Materials.Tin.getFluid(L))
                .output(COVER_PROGRAMMABLE_CIRCUIT)
                .EUt(VA[HV]).duration(20)
                .buildAndRegister();
    }
    public static void ProgrameCircuit(int id, MetaItem<?>.MetaValueItem circuit)
    {
        FORMING_PRESS_RECIPES.recipeBuilder()
                .input(plate,Stone)
                .circuitMeta(id)
                .output(circuit)
                .EUt(VA[0])
                .duration(1)
                .buildAndRegister();
    }
}
