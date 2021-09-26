package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;

public class ComponentRecipesHighTier {

    public static void register() {

        // Field Generators Start --------------------------------------------------------------------------------------
        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.HSSE)
                .input(OrePrefix.plate, Materials.HSSE, 4)
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Master)
                .input(OrePrefix.wireFine, Materials.Osmium, 32)
                .input(OrePrefix.wireFine, Materials.Osmium, 32)
                .input(OrePrefix.wireFine, Materials.Osmium, 32)
                .input(OrePrefix.wireFine, Materials.Osmium, 32)
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.FIELD_GENERATOR_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.HSSS)
                .input(OrePrefix.plate, Materials.HSSS, 4)
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.wireFine, Materials.Duranium, 32)
                .input(OrePrefix.wireFine, Materials.Duranium, 32)
                .input(OrePrefix.wireFine, Materials.Duranium, 32)
                .input(OrePrefix.wireFine, Materials.Duranium, 32)
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.FIELD_GENERATOR_ZPM.getStackForm())
                .duration(600).EUt(23040).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.Tritanium)
                .input(OrePrefix.plate, Materials.Tritanium, 4)
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Superconductor)
                .input(OrePrefix.wireFine, Materials.Naquadria, 32)
                .input(OrePrefix.wireFine, Materials.Naquadria, 32)
                .input(OrePrefix.wireFine, Materials.Naquadria, 32)
                .input(OrePrefix.wireFine, Materials.Naquadria, 32)
                .input(OrePrefix.cableGtSingle, Materials.NaquadahAlloy)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.FIELD_GENERATOR_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();

        // Robot Arms Start --------------------------------------------------------------------------------------------
        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.HSSE, 2)
                .input(OrePrefix.gearSmall, Materials.HSSE)
                .input(OrePrefix.spring, Materials.HSSE)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Master)
                .inputs(MetaItems.ELECTRIC_MOTOR_LUV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_LUV.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 4)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(250))
                .outputs(MetaItems.ROBOT_ARM_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.HSSS, 2)
                .input(OrePrefix.gearSmall, Materials.HSSS)
                .input(OrePrefix.spring, Materials.HSSS)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .inputs(MetaItems.ELECTRIC_MOTOR_ZPM.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_ZPM.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 4)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(500))
                .outputs(MetaItems.ROBOT_ARM_ZPM.getStackForm())
                .duration(600).EUt(23040).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.Tritanium, 2)
                .input(OrePrefix.gearSmall, Materials.Tritanium)
                .input(OrePrefix.spring, Materials.Tritanium)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Superconductor)
                .inputs(MetaItems.ELECTRIC_MOTOR_UV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_UV.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.NaquadahAlloy, 4)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(1000))
                .outputs(MetaItems.ROBOT_ARM_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();


        // Motors Start ------------------------------------------------------------------------------------------------
        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.SamariumMagnetic)
                .input(OrePrefix.stickLong, Materials.HSSE)
                .input(OrePrefix.ring, Materials.HSSE, 4)
                .input(OrePrefix.round, Materials.HSSE, 8)
                .input(OrePrefix.wireFine, Materials.HSSG, 16)
                .input(OrePrefix.wireFine, Materials.HSSG, 16)
                .input(OrePrefix.wireFine, Materials.HSSG, 16)
                .input(OrePrefix.wireFine, Materials.HSSG, 16)
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(250))
                .outputs(MetaItems.ELECTRIC_MOTOR_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.SamariumMagnetic)
                .input(OrePrefix.stickLong, Materials.HSSS)
                .input(OrePrefix.ring, Materials.HSSS, 4)
                .input(OrePrefix.round, Materials.HSSS, 8)
                .input(OrePrefix.wireFine, Materials.Naquadah, 16)
                .input(OrePrefix.wireFine, Materials.Naquadah, 16)
                .input(OrePrefix.wireFine, Materials.Naquadah, 16)
                .input(OrePrefix.wireFine, Materials.Naquadah, 16)
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(500))
                .outputs(MetaItems.ELECTRIC_MOTOR_ZPM.getStackForm())
                .duration(600).EUt(23040).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.SamariumMagnetic)
                .input(OrePrefix.stickLong, Materials.Tritanium)
                .input(OrePrefix.ring, Materials.Tritanium, 4)
                .input(OrePrefix.round, Materials.Tritanium, 8)
                .input(OrePrefix.wireFine, Materials.YttriumBariumCuprate, 16)
                .input(OrePrefix.wireFine, Materials.YttriumBariumCuprate, 16)
                .input(OrePrefix.wireFine, Materials.YttriumBariumCuprate, 16)
                .input(OrePrefix.wireFine, Materials.YttriumBariumCuprate, 16)
                .input(OrePrefix.cableGtSingle, Materials.NaquadahAlloy, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(1000))
                .outputs(MetaItems.ELECTRIC_MOTOR_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();

        // Sensors Start------------------------------------------------------------------------------------------------

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.HSSE)
                .input(OrePrefix.stickLong, Materials.Rhodium)
                .inputs(MetaItems.QUANTUM_EYE.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Master)
                .input(OrePrefix.foil, Materials.Palladium, 8)
                .input(OrePrefix.foil, Materials.Palladium, 8)
                .input(OrePrefix.foil, Materials.Palladium, 8)
                .input(OrePrefix.foil, Materials.Palladium, 8)
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.SENSOR_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.HSSS)
                .input(OrePrefix.stickLong, Materials.Ruthenium)
                .inputs(MetaItems.QUANTUM_EYE.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.SENSOR_ZPM.getStackForm())
                .duration(600).EUt(23040).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.Tritanium)
                .input(OrePrefix.stickLong, Materials.Naquadria)
                .inputs(MetaItems.QUANTUM_EYE.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Superconductor)
                .input(OrePrefix.foil, Materials.Americium, 8)
                .input(OrePrefix.foil, Materials.Americium, 8)
                .input(OrePrefix.foil, Materials.Americium, 8)
                .input(OrePrefix.foil, Materials.Americium, 8)
                .input(OrePrefix.cableGtSingle, Materials.NaquadahAlloy, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.SENSOR_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();

        // Emitters Start-----------------------------------------------------------------------------------------------

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.HSSE)
                .input(OrePrefix.plate, Materials.Rhodium)
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Master, 2)
                .input(OrePrefix.foil, Materials.Palladium, 8)
                .input(OrePrefix.foil, Materials.Palladium, 8)
                .input(OrePrefix.foil, Materials.Palladium, 8)
                .input(OrePrefix.foil, Materials.Palladium, 8)
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.EMITTER_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.HSSS)
                .input(OrePrefix.plate, Materials.Ruthenium)
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate, 2)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.EMITTER_ZPM.getStackForm())
                .duration(600).EUt(23040).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.Tritanium)
                .input(OrePrefix.plate, Materials.Naquadria)
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Superconductor, 2)
                .input(OrePrefix.foil, Materials.Americium, 8)
                .input(OrePrefix.foil, Materials.Americium, 8)
                .input(OrePrefix.foil, Materials.Americium, 8)
                .input(OrePrefix.foil, Materials.Americium, 8)
                .input(OrePrefix.cableGtSingle, Materials.NaquadahAlloy, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.EMITTER_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();

        // Pistons Start------------------------------------------------------------------------------------------------

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.HSSE, 2)
                .input(OrePrefix.stickLong, Materials.HSSE)
                .input(OrePrefix.gear, Materials.HSSE)
                .input(OrePrefix.spring, Materials.HSSE)
                .inputs(MetaItems.ELECTRIC_MOTOR_LUV.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(250))
                .outputs(MetaItems.ELECTRIC_PISTON_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.HSSS, 2)
                .input(OrePrefix.stickLong, Materials.HSSS)
                .input(OrePrefix.gear, Materials.HSSS)
                .input(OrePrefix.spring, Materials.HSSS)
                .inputs(MetaItems.ELECTRIC_MOTOR_ZPM.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(500))
                .outputs(MetaItems.ELECTRIC_PISTON_ZPM.getStackForm())
                .duration(600).EUt(23040).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Tritanium, 2)
                .input(OrePrefix.stickLong, Materials.Tritanium)
                .input(OrePrefix.gear, Materials.Tritanium)
                .input(OrePrefix.spring, Materials.Tritanium)
                .inputs(MetaItems.ELECTRIC_MOTOR_UV.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.NaquadahAlloy, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(1000))
                .outputs(MetaItems.ELECTRIC_PISTON_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();

        // Conveyors Start----------------------------------------------------------------------------------------------

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.gearSmall, Materials.HSSE, 2)
                .input(OrePrefix.ring, Materials.HSSE, 2)
                .input(OrePrefix.screw, Materials.HSSE, 4)
                .input(OrePrefix.round, Materials.HSSE, 16)
                .inputs(MetaItems.ELECTRIC_MOTOR_LUV.getStackForm(2))
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                .fluidInputs(Materials.StyreneButadieneRubber.getFluid(GTValues.L * 8))
                .fluidInputs(Materials.Lubricant.getFluid(250))
                .outputs(MetaItems.CONVEYOR_MODULE_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.gearSmall, Materials.HSSS, 2)
                .input(OrePrefix.ring, Materials.HSSS, 2)
                .input(OrePrefix.screw, Materials.HSSS, 4)
                .input(OrePrefix.round, Materials.HSSS, 16)
                .inputs(MetaItems.ELECTRIC_MOTOR_ZPM.getStackForm(2))
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2)
                .fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L * 8))
                .fluidInputs(Materials.Lubricant.getFluid(500))
                .outputs(MetaItems.CONVEYOR_MODULE_ZPM.getStackForm())
                .duration(600).EUt(23040).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.gearSmall, Materials.Tritanium, 2)
                .input(OrePrefix.ring, Materials.Tritanium, 2)
                .input(OrePrefix.screw, Materials.Tritanium, 4)
                .input(OrePrefix.round, Materials.Tritanium, 16)
                .inputs(MetaItems.ELECTRIC_MOTOR_UV.getStackForm(2))
                .input(OrePrefix.cableGtSingle, Materials.NaquadahAlloy, 2)
                .fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L * 8))
                .fluidInputs(Materials.Lubricant.getFluid(1000))
                .outputs(MetaItems.CONVEYOR_MODULE_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();

        //Pumps Start --------------------------------------------------------------------------------------------------

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.HSSE, 2)
                .input(OrePrefix.pipeNormalFluid, Materials.NiobiumTitanium)
                .input(OrePrefix.rotor, Materials.HSSE)
                .input(OrePrefix.screw, Materials.HSSE, 4)
                .inputs(MetaItems.ELECTRIC_MOTOR_LUV.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                .fluidInputs(Materials.SiliconeRubber.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(250))
                .outputs(MetaItems.ELECTRIC_PUMP_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.HSSS, 2)
                .input(OrePrefix.pipeNormalFluid, Materials.Iridium)
                .input(OrePrefix.rotor, Materials.HSSS)
                .input(OrePrefix.screw, Materials.HSSS, 4)
                .inputs(MetaItems.ELECTRIC_MOTOR_ZPM.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2)
                .fluidInputs(Materials.SiliconeRubber.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(500))
                .outputs(MetaItems.ELECTRIC_PUMP_ZPM.getStackForm())
                .duration(600).EUt(23040).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.Tritanium, 2)
                .input(OrePrefix.pipeNormalFluid, Materials.Europium)
                .input(OrePrefix.rotor, Materials.Tritanium)
                .input(OrePrefix.screw, Materials.Tritanium, 4)
                .inputs(MetaItems.ELECTRIC_MOTOR_UV.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.NaquadahAlloy, 2)
                .fluidInputs(Materials.SiliconeRubber.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(1000))
                .outputs(MetaItems.ELECTRIC_PUMP_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();
    }
}
