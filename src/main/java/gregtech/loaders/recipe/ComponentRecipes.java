package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.type.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import net.minecraftforge.fluids.FluidStack;

import static gregtech.api.GTValues.L;
import static gregtech.common.items.MetaItems.FLUID_REGULATORS;
import static gregtech.common.items.MetaItems.PUMPS;

public class ComponentRecipes {

    private static final FluidStack[] pumpFluids = {Materials.Rubber.getFluid(L), Materials.StyreneButadieneRubber.getFluid((L * 3)/4), Materials.SiliconeRubber.getFluid(L/2) };

    public static void register() {

        //Field Generators Start ---------------------------------------------------------------------------------------
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.EnderPearl, 1)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Basic, 4)
                .fluidInputs(Materials.Osmium.getFluid(L * 2))
                .outputs(MetaItems.FIELD_GENERATOR_LV.getStackForm())
                .duration(100).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.EnderEye, 1)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Good, 4)
                .fluidInputs(Materials.Osmium.getFluid(L * 4))
                .outputs(MetaItems.FIELD_GENERATOR_MV.getStackForm())
                .duration(100).EUt(120).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(MetaItems.QUANTUM_EYE.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Advanced, 4)
                .fluidInputs(Materials.Osmium.getFluid(L * 8))
                .outputs(MetaItems.FIELD_GENERATOR_HV.getStackForm())
                .duration(100).EUt(480).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.dust, Materials.NetherStar, 1)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Extreme, 4)
                .fluidInputs(Materials.Osmium.getFluid(L * 16))
                .outputs(MetaItems.FIELD_GENERATOR_EV.getStackForm())
                .duration(100).EUt(1920).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Elite, 4)
                .fluidInputs(Materials.Osmium.getFluid(L * 32))
                .outputs(MetaItems.FIELD_GENERATOR_IV.getStackForm())
                .duration(100).EUt(7680).buildAndRegister();


        //Robot Arms Start ---------------------------------------------------------------------------------------------
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Tin, 3)
                .input(OrePrefix.stick, Materials.Steel, 2)
                .inputs(MetaItems.ELECTRIC_MOTOR_LV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_LV.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Basic)
                .outputs(MetaItems.ROBOT_ARM_LV.getStackForm())
                .duration(100).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Copper, 3)
                .input(OrePrefix.stick, Materials.Aluminium, 2)
                .inputs(MetaItems.ELECTRIC_MOTOR_MV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_MV.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Good)
                .outputs(MetaItems.ROBOT_ARM_MV.getStackForm())
                .duration(100).EUt(120).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Gold, 3)
                .input(OrePrefix.stick, Materials.StainlessSteel, 2)
                .inputs(MetaItems.ELECTRIC_MOTOR_HV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_HV.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Advanced)
                .outputs(MetaItems.ROBOT_ARM_HV.getStackForm())
                .duration(100).EUt(480).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Aluminium, 3)
                .input(OrePrefix.stick, Materials.Titanium, 2)
                .inputs(MetaItems.ELECTRIC_MOTOR_EV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_EV.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Extreme)
                .outputs(MetaItems.ROBOT_ARM_EV.getStackForm())
                .duration(100).EUt(1920).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Tungsten, 3)
                .input(OrePrefix.stick, Materials.TungstenSteel, 2)
                .inputs(MetaItems.ELECTRIC_MOTOR_IV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_IV.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Elite)
                .outputs(MetaItems.ROBOT_ARM_IV.getStackForm())
                .duration(100).EUt(7680).buildAndRegister();


        //Motors Start--------------------------------------------------------------------------------------------------
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Tin, 2)
                .input(OrePrefix.stick, Materials.Iron, 2)
                .input(OrePrefix.stick, Materials.IronMagnetic)
                .input(OrePrefix.wireGtSingle, Materials.Copper, 4)
                .outputs(MetaItems.ELECTRIC_MOTOR_LV.getStackForm())
                .duration(100).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Tin, 2)
                .input(OrePrefix.stick, Materials.Steel, 2)
                .input(OrePrefix.stick, Materials.SteelMagnetic)
                .input(OrePrefix.wireGtSingle, Materials.Copper, 4)
                .outputs(MetaItems.ELECTRIC_MOTOR_LV.getStackForm())
                .duration(100).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Copper, 2)
                .input(OrePrefix.stick, Materials.Aluminium, 2)
                .input(OrePrefix.stick, Materials.SteelMagnetic)
                .input(OrePrefix.wireGtDouble, Materials.Copper, 4)
                .outputs(MetaItems.ELECTRIC_MOTOR_MV.getStackForm())
                .duration(100).EUt(120).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Gold, 2)
                .input(OrePrefix.stick, Materials.StainlessSteel, 2)
                .input(OrePrefix.stick, Materials.SteelMagnetic)
                .input(OrePrefix.wireGtQuadruple, Materials.Copper, 4)
                .outputs(MetaItems.ELECTRIC_MOTOR_HV.getStackForm())
                .duration(100).EUt(480).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Aluminium, 2)
                .input(OrePrefix.stick, Materials.Titanium, 2)
                .input(OrePrefix.stick, Materials.NeodymiumMagnetic)
                .input(OrePrefix.wireGtOctal, Materials.AnnealedCopper, 4)
                .outputs(MetaItems.ELECTRIC_MOTOR_EV.getStackForm())
                .duration(100).EUt(1920).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Tungsten, 2)
                .input(OrePrefix.stick, Materials.TungstenSteel, 2)
                .input(OrePrefix.stick, Materials.NeodymiumMagnetic)
                .input(OrePrefix.wireGtHex, Materials.AnnealedCopper, 4)
                .outputs(MetaItems.ELECTRIC_MOTOR_IV.getStackForm())
                .duration(100).EUt(7680).buildAndRegister();


        //Sensors Start-------------------------------------------------------------------------------------------------
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Brass)
                .input(OrePrefix.plate, Materials.Steel)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Basic)
                .input(OrePrefix.gem, Materials.Quartzite)
                .outputs(MetaItems.SENSOR_LV.getStackForm())
                .duration(100).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Electrum)
                .input(OrePrefix.plate, Materials.Aluminium)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Good)
                .input(OrePrefix.gem, Materials.NetherQuartz)
                .outputs(MetaItems.SENSOR_MV.getStackForm())
                .duration(100).EUt(120).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Chrome)
                .input(OrePrefix.plate, Materials.StainlessSteel)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Advanced)
                .input(OrePrefix.gem, Materials.Emerald)
                .outputs(MetaItems.SENSOR_HV.getStackForm())
                .duration(100).EUt(480).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Platinum)
                .input(OrePrefix.plate, Materials.Titanium)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Extreme)
                .input(OrePrefix.gem, Materials.EnderPearl)
                .outputs(MetaItems.SENSOR_EV.getStackForm())
                .duration(100).EUt(1920).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Osmium)
                .input(OrePrefix.plate, Materials.TungstenSteel)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Elite)
                .input(OrePrefix.gem, Materials.EnderEye)
                .outputs(MetaItems.SENSOR_IV.getStackForm())
                .duration(100).EUt(7680).buildAndRegister();


        //Emitters Start------------------------------------------------------------------------------------------------
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Brass, 4)
                .input(OrePrefix.cableGtSingle, Materials.Tin)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Basic, 2)
                .input(OrePrefix.gem, Materials.Quartzite)
                .circuitMeta(1)
                .outputs(MetaItems.EMITTER_LV.getStackForm())
                .duration(100).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Electrum, 4)
                .input(OrePrefix.cableGtSingle, Materials.Copper)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Good, 2)
                .input(OrePrefix.gem, Materials.NetherQuartz)
                .circuitMeta(1)
                .outputs(MetaItems.EMITTER_MV.getStackForm())
                .duration(100).EUt(120).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Chrome, 4)
                .input(OrePrefix.cableGtSingle, Materials.Gold)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Advanced, 2)
                .input(OrePrefix.gem, Materials.Emerald)
                .circuitMeta(1)
                .outputs(MetaItems.EMITTER_HV.getStackForm())
                .duration(100).EUt(480).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Platinum, 4)
                .input(OrePrefix.cableGtSingle, Materials.Aluminium)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Extreme, 2)
                .input(OrePrefix.gem, Materials.EnderPearl)
                .circuitMeta(1)
                .outputs(MetaItems.EMITTER_EV.getStackForm())
                .duration(100).EUt(1920).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Osmium, 4)
                .input(OrePrefix.cableGtSingle, Materials.Tungsten)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Elite, 2)
                .input(OrePrefix.gem, Materials.EnderEye)
                .circuitMeta(1)
                .outputs(MetaItems.EMITTER_IV.getStackForm())
                .duration(100).EUt(7680).buildAndRegister();


        //Pistons Start-------------------------------------------------------------------------------------------------
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Steel, 2)
                .input(OrePrefix.cableGtSingle, Materials.Tin, 2)
                .input(OrePrefix.plate, Materials.Steel, 3)
                .input(OrePrefix.gearSmall, Materials.Steel)
                .inputs(MetaItems.ELECTRIC_MOTOR_LV.getStackForm())
                .outputs(MetaItems.ELECTRIC_PISTON_LV.getStackForm())
                .duration(100).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Aluminium, 2)
                .input(OrePrefix.cableGtSingle, Materials.Copper, 2)
                .input(OrePrefix.plate, Materials.Aluminium, 3)
                .input(OrePrefix.gearSmall, Materials.Aluminium)
                .inputs(MetaItems.ELECTRIC_MOTOR_MV.getStackForm())
                .outputs(MetaItems.ELECTRIC_PISTON_MV.getStackForm())
                .duration(100).EUt(120).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.StainlessSteel, 2)
                .input(OrePrefix.cableGtSingle, Materials.Gold, 2)
                .input(OrePrefix.plate, Materials.StainlessSteel, 3)
                .input(OrePrefix.gearSmall, Materials.StainlessSteel)
                .inputs(MetaItems.ELECTRIC_MOTOR_HV.getStackForm())
                .outputs(MetaItems.ELECTRIC_PISTON_HV.getStackForm())
                .duration(100).EUt(480).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Titanium, 2)
                .input(OrePrefix.cableGtSingle, Materials.Aluminium, 2)
                .input(OrePrefix.plate, Materials.Titanium, 3)
                .input(OrePrefix.gearSmall, Materials.Titanium)
                .inputs(MetaItems.ELECTRIC_MOTOR_EV.getStackForm())
                .outputs(MetaItems.ELECTRIC_PISTON_EV.getStackForm())
                .duration(100).EUt(1920).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.TungstenSteel, 2)
                .input(OrePrefix.cableGtSingle, Materials.Tungsten, 2)
                .input(OrePrefix.plate, Materials.TungstenSteel, 3)
                .input(OrePrefix.gearSmall, Materials.TungstenSteel)
                .inputs(MetaItems.ELECTRIC_MOTOR_IV.getStackForm())
                .outputs(MetaItems.ELECTRIC_PISTON_IV.getStackForm())
                .duration(100).EUt(7680).buildAndRegister();


        //Conveyors Start-----------------------------------------------------------------------------------------------
        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Tin)
                .inputs(MetaItems.ELECTRIC_MOTOR_LV.getStackForm(2))
                .circuitMeta(1)
                .fluidInputs(Materials.Rubber.getFluid(L * 6))
                .outputs(MetaItems.CONVEYOR_MODULE_LV.getStackForm())
                .duration(100).EUt(30).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Copper)
                .inputs(MetaItems.ELECTRIC_MOTOR_MV.getStackForm(2))
                .circuitMeta(1)
                .fluidInputs(Materials.Rubber.getFluid(L * 6))
                .outputs(MetaItems.CONVEYOR_MODULE_MV.getStackForm())
                .duration(100).EUt(120).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Gold)
                .inputs(MetaItems.ELECTRIC_MOTOR_HV.getStackForm(2))
                .circuitMeta(1)
                .fluidInputs(Materials.Rubber.getFluid(L * 6))
                .outputs(MetaItems.CONVEYOR_MODULE_HV.getStackForm())
                .duration(100).EUt(480).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Aluminium)
                .inputs(MetaItems.ELECTRIC_MOTOR_EV.getStackForm(2))
                .circuitMeta(1)
                .fluidInputs(Materials.Rubber.getFluid(L * 6))
                .outputs(MetaItems.CONVEYOR_MODULE_EV.getStackForm())
                .duration(100).EUt(1920).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Tungsten)
                .inputs(MetaItems.ELECTRIC_MOTOR_IV.getStackForm(2))
                .circuitMeta(1)
                .fluidInputs(Materials.Rubber.getFluid(L * 6))
                .outputs(MetaItems.CONVEYOR_MODULE_IV.getStackForm())
                .duration(100).EUt(7680).buildAndRegister();


        //Pumps Start---------------------------------------------------------------------------------------------------
        for(FluidStack fluid : pumpFluids) {
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Tin)
                    .input(OrePrefix.plate, Materials.Tin, 2)
                    .input(OrePrefix.screw, Materials.Tin)
                    .input(OrePrefix.rotor, Materials.Tin)
                    .inputs(MetaItems.ELECTRIC_MOTOR_LV.getStackForm())
                    .fluidInputs(fluid)
                    .outputs(MetaItems.ELECTRIC_PUMP_LV.getStackForm())
                    .duration(100).EUt(30).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Copper)
                    .input(OrePrefix.plate, Materials.Bronze, 2)
                    .input(OrePrefix.screw, Materials.Bronze)
                    .input(OrePrefix.rotor, Materials.Bronze)
                    .inputs(MetaItems.ELECTRIC_MOTOR_MV.getStackForm())
                    .fluidInputs(fluid)
                    .outputs(MetaItems.ELECTRIC_PUMP_MV.getStackForm())
                    .duration(100).EUt(120).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Copper)
                    .input(OrePrefix.plate, Materials.Steel, 2)
                    .input(OrePrefix.screw, Materials.Steel)
                    .input(OrePrefix.rotor, Materials.Steel)
                    .inputs(MetaItems.ELECTRIC_MOTOR_HV.getStackForm())
                    .fluidInputs(fluid)
                    .outputs(MetaItems.ELECTRIC_PUMP_HV.getStackForm())
                    .duration(100).EUt(480).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Aluminium)
                    .input(OrePrefix.plate, Materials.StainlessSteel, 2)
                    .input(OrePrefix.screw, Materials.StainlessSteel)
                    .input(OrePrefix.rotor, Materials.StainlessSteel)
                    .inputs(MetaItems.ELECTRIC_MOTOR_EV.getStackForm())
                    .fluidInputs(fluid)
                    .outputs(MetaItems.ELECTRIC_PUMP_EV.getStackForm())
                    .duration(100).EUt(1920).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Tungsten)
                    .input(OrePrefix.plate, Materials.TungstenSteel, 2)
                    .input(OrePrefix.screw, Materials.TungstenSteel)
                    .input(OrePrefix.rotor, Materials.TungstenSteel)
                    .inputs(MetaItems.ELECTRIC_MOTOR_IV.getStackForm())
                    .fluidInputs(fluid)
                    .outputs(MetaItems.ELECTRIC_PUMP_IV.getStackForm())
                    .duration(100).EUt(7680).buildAndRegister();
        }

        Material[] circuitTiers = new Material[] {MarkerMaterials.Tier.Basic, MarkerMaterials.Tier.Good, MarkerMaterials.Tier.Advanced, MarkerMaterials.Tier.Extreme, MarkerMaterials.Tier.Elite};

        for (int i = 0; i < circuitTiers.length; i++) {
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .inputs(PUMPS[i].getStackForm())
                    .input(OrePrefix.circuit, circuitTiers[i], 2)
                    .outputs(FLUID_REGULATORS[i].getStackForm())
                    .EUt((int) (GTValues.V[i + 1] * 30 / 32))
                    .duration(100)
                    .buildAndRegister();
        }
    }
}
