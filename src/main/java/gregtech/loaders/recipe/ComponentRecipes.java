package gregtech.loaders.recipe;

import gregtech.api.GTValues;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;

import java.util.HashMap;
import java.util.Map;

import static gregtech.api.GTValues.LV;
import static gregtech.api.GTValues.VA;
import static gregtech.common.items.MetaItems.FLUID_REGULATORS;
import static gregtech.common.items.MetaItems.PUMPS;

public class ComponentRecipes {

    public static void register() {

        //Field Generators Start ---------------------------------------------------------------------------------------
        ModHandler.addShapedRecipe(true, "field_generator/field_generator_lv", MetaItems.FIELD_GENERATOR_LV.getStackForm(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.ManganesePhosphide), 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'G', new UnificationEntry(OrePrefix.gemFlawless, Materials.Quartzite), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Basic));
        ModHandler.addShapedRecipe(true, "field_generator/field_generator_mv", MetaItems.FIELD_GENERATOR_MV.getStackForm(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.MagnesiumDiboride), 'P', new UnificationEntry(OrePrefix.plate, Materials.Aluminium), 'G', new UnificationEntry(OrePrefix.gem, Materials.EnderPearl), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Good));
        ModHandler.addShapedRecipe(true, "field_generator/field_generator_hv", MetaItems.FIELD_GENERATOR_HV.getStackForm(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.MercuryBariumCalciumCuprate), 'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel), 'G', new UnificationEntry(OrePrefix.gem, Materials.EnderEye), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Advanced));
        ModHandler.addShapedRecipe(true, "field_generator/field_generator_ev", MetaItems.FIELD_GENERATOR_EV.getStackForm(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.UraniumTriplatinum), 'P', new UnificationEntry(OrePrefix.plateDouble, Materials.Titanium), 'G', MetaItems.QUANTUM_EYE.getStackForm(), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Extreme));
        ModHandler.addShapedRecipe(true, "field_generator/field_generator_iv", MetaItems.FIELD_GENERATOR_IV.getStackForm(), "WPW", "XGX", "WPW", 'W', new UnificationEntry(OrePrefix.wireGtQuadruple, Materials.SamariumIronArsenicOxide), 'P', new UnificationEntry(OrePrefix.plateDouble, Materials.TungstenSteel), 'G', MetaItems.QUANTUM_STAR.getStackForm(), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Elite));

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.gemFlawless, Materials.Quartzite)
                .input(OrePrefix.plate, Materials.Steel, 2)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Basic, 2)
                .input(OrePrefix.wireGtQuadruple, Materials.ManganesePhosphide, 4)
                .outputs(MetaItems.FIELD_GENERATOR_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.gem, Materials.EnderPearl)
                .input(OrePrefix.plate, Materials.Aluminium, 2)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Good, 2)
                .input(OrePrefix.wireGtQuadruple, Materials.MagnesiumDiboride, 4)
                .outputs(MetaItems.FIELD_GENERATOR_MV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.gem, Materials.EnderEye)
                .input(OrePrefix.plate, Materials.StainlessSteel, 2)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Advanced, 2)
                .input(OrePrefix.wireGtQuadruple, Materials.MercuryBariumCalciumCuprate, 4)
                .outputs(MetaItems.FIELD_GENERATOR_HV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(MetaItems.QUANTUM_EYE.getStackForm())
                .input(OrePrefix.plateDouble, Materials.Titanium, 2)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Extreme, 2)
                .input(OrePrefix.wireGtQuadruple, Materials.UraniumTriplatinum, 4)
                .outputs(MetaItems.FIELD_GENERATOR_EV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.plateDouble, Materials.TungstenSteel, 2)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Elite, 2)
                .input(OrePrefix.wireGtQuadruple, Materials.SamariumIronArsenicOxide, 4)
                .outputs(MetaItems.FIELD_GENERATOR_IV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.HSSG)
                .input(OrePrefix.plate, Materials.HSSG, 4)
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Master)
                .input(OrePrefix.wireFine, Materials.IndiumTinBariumTitaniumCuprate, 32)
                .input(OrePrefix.wireFine, Materials.IndiumTinBariumTitaniumCuprate, 32)
                .input(OrePrefix.wireFine, Materials.IndiumTinBariumTitaniumCuprate, 32)
                .input(OrePrefix.wireFine, Materials.IndiumTinBariumTitaniumCuprate, 32)
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.FIELD_GENERATOR_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.NaquadahAlloy)
                .input(OrePrefix.plate, Materials.NaquadahAlloy, 4)
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.wireFine, Materials.UraniumRhodiumDinaquadide, 32)
                .input(OrePrefix.wireFine, Materials.UraniumRhodiumDinaquadide, 32)
                .input(OrePrefix.wireFine, Materials.UraniumRhodiumDinaquadide, 32)
                .input(OrePrefix.wireFine, Materials.UraniumRhodiumDinaquadide, 32)
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.FIELD_GENERATOR_ZPM.getStackForm())
                .duration(600).EUt(23040).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.Tritanium)
                .input(OrePrefix.plate, Materials.Tritanium, 4)
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Super)
                .input(OrePrefix.wireFine, Materials.EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .input(OrePrefix.wireFine, Materials.EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .input(OrePrefix.wireFine, Materials.EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .input(OrePrefix.wireFine, Materials.EnrichedNaquadahTriniumEuropiumDuranide, 32)
                .input(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.FIELD_GENERATOR_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();


        //Robot Arms Start ---------------------------------------------------------------------------------------------
        ModHandler.addShapedRecipe(true, "robot_arm/robot_arm_lv", MetaItems.ROBOT_ARM_LV.getStackForm(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin), 'R', new UnificationEntry(OrePrefix.stick, Materials.Steel), 'M', MetaItems.ELECTRIC_MOTOR_LV.getStackForm(), 'P', MetaItems.ELECTRIC_PISTON_LV.getStackForm(), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Basic));
        ModHandler.addShapedRecipe(true, "robot_arm/robot_arm_mv", MetaItems.ROBOT_ARM_MV.getStackForm(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper), 'R', new UnificationEntry(OrePrefix.stick, Materials.Aluminium), 'M', MetaItems.ELECTRIC_MOTOR_MV.getStackForm(), 'P', MetaItems.ELECTRIC_PISTON_MV.getStackForm(), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Good));
        ModHandler.addShapedRecipe(true, "robot_arm/robot_arm_hv", MetaItems.ROBOT_ARM_HV.getStackForm(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold), 'R', new UnificationEntry(OrePrefix.stick, Materials.StainlessSteel), 'M', MetaItems.ELECTRIC_MOTOR_HV.getStackForm(), 'P', MetaItems.ELECTRIC_PISTON_HV.getStackForm(), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Advanced));
        ModHandler.addShapedRecipe(true, "robot_arm/robot_arm_ev", MetaItems.ROBOT_ARM_EV.getStackForm(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Aluminium), 'R', new UnificationEntry(OrePrefix.stick, Materials.Titanium), 'M', MetaItems.ELECTRIC_MOTOR_EV.getStackForm(), 'P', MetaItems.ELECTRIC_PISTON_EV.getStackForm(), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Extreme));
        ModHandler.addShapedRecipe(true, "robot_arm/robot_arm_iv", MetaItems.ROBOT_ARM_IV.getStackForm(), "CCC", "MRM", "PXR", 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tungsten), 'R', new UnificationEntry(OrePrefix.stick, Materials.TungstenSteel), 'M', MetaItems.ELECTRIC_MOTOR_IV.getStackForm(), 'P', MetaItems.ELECTRIC_PISTON_IV.getStackForm(), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Elite));

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Tin, 3)
                .input(OrePrefix.stick, Materials.Steel, 2)
                .inputs(MetaItems.ELECTRIC_MOTOR_LV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_LV.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Basic)
                .outputs(MetaItems.ROBOT_ARM_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Copper, 3)
                .input(OrePrefix.stick, Materials.Aluminium, 2)
                .inputs(MetaItems.ELECTRIC_MOTOR_MV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_MV.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Good)
                .outputs(MetaItems.ROBOT_ARM_MV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Gold, 3)
                .input(OrePrefix.stick, Materials.StainlessSteel, 2)
                .inputs(MetaItems.ELECTRIC_MOTOR_HV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_HV.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Advanced)
                .outputs(MetaItems.ROBOT_ARM_HV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Aluminium, 3)
                .input(OrePrefix.stick, Materials.Titanium, 2)
                .inputs(MetaItems.ELECTRIC_MOTOR_EV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_EV.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Extreme)
                .outputs(MetaItems.ROBOT_ARM_EV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Tungsten, 3)
                .input(OrePrefix.stick, Materials.TungstenSteel, 2)
                .inputs(MetaItems.ELECTRIC_MOTOR_IV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_IV.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Elite)
                .outputs(MetaItems.ROBOT_ARM_IV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.HSSG, 2)
                .input(OrePrefix.gearSmall, Materials.HSSG)
                .input(OrePrefix.spring, Materials.HSSG)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Master)
                .inputs(MetaItems.ELECTRIC_MOTOR_LUV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_LUV.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 4)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(250))
                .outputs(MetaItems.ROBOT_ARM_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.NaquadahAlloy, 2)
                .input(OrePrefix.gearSmall, Materials.NaquadahAlloy)
                .input(OrePrefix.spring, Materials.NaquadahAlloy)
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
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Super)
                .inputs(MetaItems.ELECTRIC_MOTOR_UV.getStackForm(2))
                .inputs(MetaItems.ELECTRIC_PISTON_UV.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate, 4)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(1000))
                .outputs(MetaItems.ROBOT_ARM_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();


        //Motors Start--------------------------------------------------------------------------------------------------
        ModHandler.addShapedRecipe("electric_motor/electric_motor_lv_steel", MetaItems.ELECTRIC_MOTOR_LV.getStackForm(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin), 'W', new UnificationEntry(OrePrefix.wireGtSingle, Materials.Copper), 'R', new UnificationEntry(OrePrefix.stick, Materials.Steel), 'M', new UnificationEntry(OrePrefix.stick, Materials.SteelMagnetic));
        ModHandler.addShapedRecipe(true, "electric_motor/electric_motor_lv_iron", MetaItems.ELECTRIC_MOTOR_LV.getStackForm(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin), 'W', new UnificationEntry(OrePrefix.wireGtSingle, Materials.Copper), 'R', new UnificationEntry(OrePrefix.stick, Materials.Iron), 'M', new UnificationEntry(OrePrefix.stick, Materials.IronMagnetic));
        ModHandler.addShapedRecipe(true, "electric_motor/electric_motor_mv", MetaItems.ELECTRIC_MOTOR_MV.getStackForm(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper), 'W', new UnificationEntry(OrePrefix.wireGtDouble, Materials.Cupronickel), 'R', new UnificationEntry(OrePrefix.stick, Materials.Aluminium), 'M', new UnificationEntry(OrePrefix.stick, Materials.SteelMagnetic));
        ModHandler.addShapedRecipe(true, "electric_motor/electric_motor_hv", MetaItems.ELECTRIC_MOTOR_HV.getStackForm(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(OrePrefix.cableGtDouble, Materials.Silver), 'W', new UnificationEntry(OrePrefix.wireGtDouble, Materials.Electrum), 'R', new UnificationEntry(OrePrefix.stick, Materials.StainlessSteel), 'M', new UnificationEntry(OrePrefix.stick, Materials.SteelMagnetic));
        ModHandler.addShapedRecipe(true, "electric_motor/electric_motor_ev", MetaItems.ELECTRIC_MOTOR_EV.getStackForm(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(OrePrefix.cableGtDouble, Materials.Aluminium), 'W', new UnificationEntry(OrePrefix.wireGtDouble, Materials.Kanthal), 'R', new UnificationEntry(OrePrefix.stick, Materials.Titanium), 'M', new UnificationEntry(OrePrefix.stick, Materials.NeodymiumMagnetic));
        ModHandler.addShapedRecipe(true, "electric_motor/electric_motor_iv", MetaItems.ELECTRIC_MOTOR_IV.getStackForm(), "CWR", "WMW", "RWC", 'C', new UnificationEntry(OrePrefix.cableGtDouble, Materials.Tungsten), 'W', new UnificationEntry(OrePrefix.wireGtDouble, Materials.Graphene), 'R', new UnificationEntry(OrePrefix.stick, Materials.TungstenSteel), 'M', new UnificationEntry(OrePrefix.stick, Materials.NeodymiumMagnetic));

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Tin, 2)
                .input(OrePrefix.stick, Materials.Iron, 2)
                .input(OrePrefix.stick, Materials.IronMagnetic)
                .input(OrePrefix.wireGtSingle, Materials.Copper, 4)
                .outputs(MetaItems.ELECTRIC_MOTOR_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Tin, 2)
                .input(OrePrefix.stick, Materials.Steel, 2)
                .input(OrePrefix.stick, Materials.SteelMagnetic)
                .input(OrePrefix.wireGtSingle, Materials.Copper, 4)
                .outputs(MetaItems.ELECTRIC_MOTOR_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtSingle, Materials.Copper, 2)
                .input(OrePrefix.stick, Materials.Aluminium, 2)
                .input(OrePrefix.stick, Materials.SteelMagnetic)
                .input(OrePrefix.wireGtDouble, Materials.Cupronickel, 4)
                .outputs(MetaItems.ELECTRIC_MOTOR_MV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtDouble, Materials.Silver, 2)
                .input(OrePrefix.stick, Materials.StainlessSteel, 2)
                .input(OrePrefix.stick, Materials.SteelMagnetic)
                .input(OrePrefix.wireGtDouble, Materials.Electrum, 4)
                .outputs(MetaItems.ELECTRIC_MOTOR_HV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtDouble, Materials.Aluminium, 2)
                .input(OrePrefix.stick, Materials.Titanium, 2)
                .input(OrePrefix.stick, Materials.NeodymiumMagnetic)
                .input(OrePrefix.wireGtDouble, Materials.Kanthal, 4)
                .outputs(MetaItems.ELECTRIC_MOTOR_EV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.cableGtDouble, Materials.Tungsten, 2)
                .input(OrePrefix.stick, Materials.TungstenSteel, 2)
                .input(OrePrefix.stick, Materials.NeodymiumMagnetic)
                .input(OrePrefix.wireGtDouble, Materials.Graphene, 4)
                .outputs(MetaItems.ELECTRIC_MOTOR_IV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.SamariumMagnetic)
                .input(OrePrefix.stickLong, Materials.HSSG)
                .input(OrePrefix.ring, Materials.HSSG, 4)
                .input(OrePrefix.round, Materials.HSSG, 8)
                .input(OrePrefix.wireFine, Materials.Ruridit, 16)
                .input(OrePrefix.wireFine, Materials.Ruridit, 16)
                .input(OrePrefix.wireFine, Materials.Ruridit, 16)
                .input(OrePrefix.wireFine, Materials.Ruridit, 16)
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(250))
                .outputs(MetaItems.ELECTRIC_MOTOR_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.stickLong, Materials.SamariumMagnetic)
                .input(OrePrefix.stickLong, Materials.NaquadahAlloy)
                .input(OrePrefix.ring, Materials.NaquadahAlloy, 4)
                .input(OrePrefix.round, Materials.NaquadahAlloy, 8)
                .input(OrePrefix.wireFine, Materials.Duranium, 16)
                .input(OrePrefix.wireFine, Materials.Duranium, 16)
                .input(OrePrefix.wireFine, Materials.Duranium, 16)
                .input(OrePrefix.wireFine, Materials.Duranium, 16)
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
                .input(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(1000))
                .outputs(MetaItems.ELECTRIC_MOTOR_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();


        //Sensors Start-------------------------------------------------------------------------------------------------
        ModHandler.addShapedRecipe(true, "sensor/sensor_lv", MetaItems.SENSOR_LV.getStackForm(), "P G", "PR ", "XPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'R', new UnificationEntry(OrePrefix.stick, Materials.Brass), 'G', new UnificationEntry(OrePrefix.gem, Materials.Quartzite), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Basic));
        ModHandler.addShapedRecipe(true, "sensor/sensor_mv", MetaItems.SENSOR_MV.getStackForm(), "P G", "PR ", "XPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Aluminium), 'R', new UnificationEntry(OrePrefix.stick, Materials.Electrum), 'G', new UnificationEntry(OrePrefix.gem, Materials.NetherQuartz), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Good));
        ModHandler.addShapedRecipe(true, "sensor/sensor_hv", MetaItems.SENSOR_HV.getStackForm(), "P G", "PR ", "XPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel), 'R', new UnificationEntry(OrePrefix.stick, Materials.Chrome), 'G', new UnificationEntry(OrePrefix.gem, Materials.Emerald), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Advanced));
        ModHandler.addShapedRecipe(true, "sensor/sensor_ev", MetaItems.SENSOR_EV.getStackForm(), "P G", "PR ", "XPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.Titanium), 'R', new UnificationEntry(OrePrefix.stick, Materials.Platinum), 'G', new UnificationEntry(OrePrefix.gem, Materials.EnderPearl), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Extreme));
        ModHandler.addShapedRecipe(true, "sensor/sensor_iv", MetaItems.SENSOR_IV.getStackForm(), "P G", "PR ", "XPP", 'P', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel), 'R', new UnificationEntry(OrePrefix.stick, Materials.Iridium), 'G', new UnificationEntry(OrePrefix.gem, Materials.EnderEye), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Elite));

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Brass)
                .input(OrePrefix.plate, Materials.Steel, 4)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Basic)
                .input(OrePrefix.gem, Materials.Quartzite)
                .outputs(MetaItems.SENSOR_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Electrum)
                .input(OrePrefix.plate, Materials.Aluminium, 4)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Good)
                .input(OrePrefix.gem, Materials.NetherQuartz)
                .outputs(MetaItems.SENSOR_MV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Chrome)
                .input(OrePrefix.plate, Materials.StainlessSteel, 4)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Advanced)
                .input(OrePrefix.gem, Materials.Emerald)
                .outputs(MetaItems.SENSOR_HV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Platinum)
                .input(OrePrefix.plate, Materials.Titanium, 4)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Extreme)
                .input(OrePrefix.gem, Materials.EnderPearl)
                .outputs(MetaItems.SENSOR_EV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Iridium)
                .input(OrePrefix.plate, Materials.TungstenSteel, 4)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Elite)
                .input(OrePrefix.gem, Materials.EnderEye)
                .outputs(MetaItems.SENSOR_IV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.HSSG)
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
                .input(OrePrefix.frameGt, Materials.NaquadahAlloy)
                .input(OrePrefix.stickLong, Materials.Ruthenium)
                .inputs(MetaItems.QUANTUM_EYE.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate)
                .input(OrePrefix.foil, Materials.Trinium, 8)
                .input(OrePrefix.foil, Materials.Trinium, 8)
                .input(OrePrefix.foil, Materials.Trinium, 8)
                .input(OrePrefix.foil, Materials.Trinium, 8)
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.SENSOR_ZPM.getStackForm())
                .duration(600).EUt(23040).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.Tritanium)
                .input(OrePrefix.stickLong, Materials.Naquadria)
                .inputs(MetaItems.QUANTUM_EYE.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Super)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.SENSOR_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();


        //Emitters Start------------------------------------------------------------------------------------------------
        ModHandler.addShapedRecipe(true, "emitter/emitter_lv", MetaItems.EMITTER_LV.getStackForm(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(OrePrefix.stick, Materials.Brass), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin), 'G', new UnificationEntry(OrePrefix.gem, Materials.Quartzite), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Basic));
        ModHandler.addShapedRecipe(true, "emitter/emitter_mv", MetaItems.EMITTER_MV.getStackForm(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(OrePrefix.stick, Materials.Electrum), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper), 'G', new UnificationEntry(OrePrefix.gem, Materials.NetherQuartz), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Good));
        ModHandler.addShapedRecipe(true, "emitter/emitter_hv", MetaItems.EMITTER_HV.getStackForm(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(OrePrefix.stick, Materials.Chrome), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold), 'G', new UnificationEntry(OrePrefix.gem, Materials.Emerald), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Advanced));
        ModHandler.addShapedRecipe(true, "emitter/emitter_ev", MetaItems.EMITTER_EV.getStackForm(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(OrePrefix.stick, Materials.Platinum), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Aluminium), 'G', new UnificationEntry(OrePrefix.gem, Materials.EnderPearl), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Extreme));
        ModHandler.addShapedRecipe(true, "emitter/emitter_iv", MetaItems.EMITTER_IV.getStackForm(), "CRX", "RGR", "XRC", 'R', new UnificationEntry(OrePrefix.stick, Materials.Iridium), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tungsten), 'G', new UnificationEntry(OrePrefix.gem, Materials.EnderEye), 'X', new UnificationEntry(OrePrefix.circuit, MarkerMaterials.Tier.Elite));

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Brass, 4)
                .input(OrePrefix.cableGtSingle, Materials.Tin, 2)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Basic, 2)
                .input(OrePrefix.gem, Materials.Quartzite)
                .circuitMeta(1)
                .outputs(MetaItems.EMITTER_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Electrum, 4)
                .input(OrePrefix.cableGtSingle, Materials.Copper, 2)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Good, 2)
                .input(OrePrefix.gem, Materials.NetherQuartz)
                .circuitMeta(1)
                .outputs(MetaItems.EMITTER_MV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Chrome, 4)
                .input(OrePrefix.cableGtSingle, Materials.Gold, 2)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Advanced, 2)
                .input(OrePrefix.gem, Materials.Emerald)
                .circuitMeta(1)
                .outputs(MetaItems.EMITTER_HV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Platinum, 4)
                .input(OrePrefix.cableGtSingle, Materials.Aluminium, 2)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Extreme, 2)
                .input(OrePrefix.gem, Materials.EnderPearl)
                .circuitMeta(1)
                .outputs(MetaItems.EMITTER_EV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Iridium, 4)
                .input(OrePrefix.cableGtSingle, Materials.Tungsten, 2)
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Elite, 2)
                .input(OrePrefix.gem, Materials.EnderEye)
                .circuitMeta(1)
                .outputs(MetaItems.EMITTER_IV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.HSSG)
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
                .input(OrePrefix.frameGt, Materials.NaquadahAlloy)
                .input(OrePrefix.plate, Materials.Ruthenium)
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Ultimate, 2)
                .input(OrePrefix.foil, Materials.Trinium, 8)
                .input(OrePrefix.foil, Materials.Trinium, 8)
                .input(OrePrefix.foil, Materials.Trinium, 8)
                .input(OrePrefix.foil, Materials.Trinium, 8)
                .input(OrePrefix.cableGtSingle, Materials.VanadiumGallium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.EMITTER_ZPM.getStackForm())
                .duration(600).EUt(23040).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.frameGt, Materials.Tritanium)
                .input(OrePrefix.plate, Materials.Naquadria)
                .inputs(MetaItems.QUANTUM_STAR.getStackForm())
                .input(OrePrefix.circuit, MarkerMaterials.Tier.Super, 2)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.foil, Materials.Europium, 8)
                .input(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .outputs(MetaItems.EMITTER_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();


        //Pistons Start-------------------------------------------------------------------------------------------------
        ModHandler.addShapedRecipe(true, "electric_piston/electric_piston_lv", MetaItems.ELECTRIC_PISTON_LV.getStackForm(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(OrePrefix.plate, Materials.Steel), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin), 'R', new UnificationEntry(OrePrefix.stick, Materials.Steel), 'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Steel), 'M', MetaItems.ELECTRIC_MOTOR_LV.getStackForm());
        ModHandler.addShapedRecipe(true, "electric_piston/electric_piston_mv", MetaItems.ELECTRIC_PISTON_MV.getStackForm(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(OrePrefix.plate, Materials.Aluminium), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper), 'R', new UnificationEntry(OrePrefix.stick, Materials.Aluminium), 'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Aluminium), 'M', MetaItems.ELECTRIC_MOTOR_MV.getStackForm());
        ModHandler.addShapedRecipe(true, "electric_piston/electric_piston_hv", MetaItems.ELECTRIC_PISTON_HV.getStackForm(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(OrePrefix.plate, Materials.StainlessSteel), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold), 'R', new UnificationEntry(OrePrefix.stick, Materials.StainlessSteel), 'G', new UnificationEntry(OrePrefix.gearSmall, Materials.StainlessSteel), 'M', MetaItems.ELECTRIC_MOTOR_HV.getStackForm());
        ModHandler.addShapedRecipe(true, "electric_piston/electric_piston_ev", MetaItems.ELECTRIC_PISTON_EV.getStackForm(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(OrePrefix.plate, Materials.Titanium), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Aluminium), 'R', new UnificationEntry(OrePrefix.stick, Materials.Titanium), 'G', new UnificationEntry(OrePrefix.gearSmall, Materials.Titanium), 'M', MetaItems.ELECTRIC_MOTOR_EV.getStackForm());
        ModHandler.addShapedRecipe(true, "electric_piston/electric_piston_iv", MetaItems.ELECTRIC_PISTON_IV.getStackForm(), "PPP", "CRR", "CMG", 'P', new UnificationEntry(OrePrefix.plate, Materials.TungstenSteel), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tungsten), 'R', new UnificationEntry(OrePrefix.stick, Materials.TungstenSteel), 'G', new UnificationEntry(OrePrefix.gearSmall, Materials.TungstenSteel), 'M', MetaItems.ELECTRIC_MOTOR_IV.getStackForm());

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Steel, 2)
                .input(OrePrefix.cableGtSingle, Materials.Tin, 2)
                .input(OrePrefix.plate, Materials.Steel, 3)
                .input(OrePrefix.gearSmall, Materials.Steel)
                .inputs(MetaItems.ELECTRIC_MOTOR_LV.getStackForm())
                .outputs(MetaItems.ELECTRIC_PISTON_LV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Aluminium, 2)
                .input(OrePrefix.cableGtSingle, Materials.Copper, 2)
                .input(OrePrefix.plate, Materials.Aluminium, 3)
                .input(OrePrefix.gearSmall, Materials.Aluminium)
                .inputs(MetaItems.ELECTRIC_MOTOR_MV.getStackForm())
                .outputs(MetaItems.ELECTRIC_PISTON_MV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.StainlessSteel, 2)
                .input(OrePrefix.cableGtSingle, Materials.Gold, 2)
                .input(OrePrefix.plate, Materials.StainlessSteel, 3)
                .input(OrePrefix.gearSmall, Materials.StainlessSteel)
                .inputs(MetaItems.ELECTRIC_MOTOR_HV.getStackForm())
                .outputs(MetaItems.ELECTRIC_PISTON_HV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.Titanium, 2)
                .input(OrePrefix.cableGtSingle, Materials.Aluminium, 2)
                .input(OrePrefix.plate, Materials.Titanium, 3)
                .input(OrePrefix.gearSmall, Materials.Titanium)
                .inputs(MetaItems.ELECTRIC_MOTOR_EV.getStackForm())
                .outputs(MetaItems.ELECTRIC_PISTON_EV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();

        RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                .input(OrePrefix.stick, Materials.TungstenSteel, 2)
                .input(OrePrefix.cableGtSingle, Materials.Tungsten, 2)
                .input(OrePrefix.plate, Materials.TungstenSteel, 3)
                .input(OrePrefix.gearSmall, Materials.TungstenSteel)
                .inputs(MetaItems.ELECTRIC_MOTOR_IV.getStackForm())
                .outputs(MetaItems.ELECTRIC_PISTON_IV.getStackForm())
                .duration(100).EUt(VA[LV]).buildAndRegister();


        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.HSSG, 2)
                .input(OrePrefix.stickLong, Materials.HSSG)
                .input(OrePrefix.gear, Materials.HSSG)
                .input(OrePrefix.spring, Materials.HSSG)
                .inputs(MetaItems.ELECTRIC_MOTOR_LUV.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(250))
                .outputs(MetaItems.ELECTRIC_PISTON_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.NaquadahAlloy, 2)
                .input(OrePrefix.stickLong, Materials.NaquadahAlloy)
                .input(OrePrefix.gear, Materials.NaquadahAlloy)
                .input(OrePrefix.spring, Materials.NaquadahAlloy)
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
                .input(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate, 2)
                .fluidInputs(Materials.SolderingAlloy.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(1000))
                .outputs(MetaItems.ELECTRIC_PISTON_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();


        //Conveyors Start-----------------------------------------------------------------------------------------------
        final Map<String, Material> rubberMaterials = new HashMap<String, Material>() {{
            put("rubber", Materials.Rubber);
            put("silicone_rubber", Materials.SiliconeRubber);
            put("styrene_butadiene_rubber", Materials.StyreneButadieneRubber);
        }};

        for (Map.Entry<String, Material> materialEntry : rubberMaterials.entrySet()) {
            ModHandler.addShapedRecipe(String.format("conveyor_module/%s/conveyor_module_lv", materialEntry.getKey()), MetaItems.CONVEYOR_MODULE_LV.getStackForm(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(OrePrefix.plate, materialEntry.getValue()), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin), 'M', MetaItems.ELECTRIC_MOTOR_LV.getStackForm());
            ModHandler.addShapedRecipe(String.format("conveyor_module/%s/conveyor_module_mv", materialEntry.getKey()), MetaItems.CONVEYOR_MODULE_MV.getStackForm(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(OrePrefix.plate, materialEntry.getValue()), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper), 'M', MetaItems.ELECTRIC_MOTOR_MV.getStackForm());
            ModHandler.addShapedRecipe(String.format("conveyor_module/%s/conveyor_module_hv", materialEntry.getKey()), MetaItems.CONVEYOR_MODULE_HV.getStackForm(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(OrePrefix.plate, materialEntry.getValue()), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold), 'M', MetaItems.ELECTRIC_MOTOR_HV.getStackForm());
            ModHandler.addShapedRecipe(String.format("conveyor_module/%s/conveyor_module_ev", materialEntry.getKey()), MetaItems.CONVEYOR_MODULE_EV.getStackForm(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(OrePrefix.plate, materialEntry.getValue()), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Aluminium), 'M', MetaItems.ELECTRIC_MOTOR_EV.getStackForm());
            if (!materialEntry.getValue().equals(Materials.Rubber))
                ModHandler.addShapedRecipe(String.format("conveyor_module/%s/conveyor_module_iv", materialEntry.getKey()), MetaItems.CONVEYOR_MODULE_IV.getStackForm(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(OrePrefix.plate, materialEntry.getValue()), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tungsten), 'M', MetaItems.ELECTRIC_MOTOR_IV.getStackForm());

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Tin)
                    .input(OrePrefix.plate, materialEntry.getValue(), 6)
                    .inputs(MetaItems.ELECTRIC_MOTOR_LV.getStackForm(2))
                    .circuitMeta(1)
                    .outputs(MetaItems.CONVEYOR_MODULE_LV.getStackForm())
                    .duration(100).EUt(VA[LV]).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Copper)
                    .input(OrePrefix.plate, materialEntry.getValue(), 6)
                    .inputs(MetaItems.ELECTRIC_MOTOR_MV.getStackForm(2))
                    .circuitMeta(1)
                    .outputs(MetaItems.CONVEYOR_MODULE_MV.getStackForm())
                    .duration(100).EUt(VA[LV]).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Gold)
                    .input(OrePrefix.plate, materialEntry.getValue(), 6)
                    .inputs(MetaItems.ELECTRIC_MOTOR_HV.getStackForm(2))
                    .circuitMeta(1)
                    .outputs(MetaItems.CONVEYOR_MODULE_HV.getStackForm())
                    .duration(100).EUt(VA[LV]).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Aluminium)
                    .input(OrePrefix.plate, materialEntry.getValue(), 6)
                    .inputs(MetaItems.ELECTRIC_MOTOR_EV.getStackForm(2))
                    .circuitMeta(1)
                    .outputs(MetaItems.CONVEYOR_MODULE_EV.getStackForm())
                    .duration(100).EUt(VA[LV]).buildAndRegister();

            if (!materialEntry.getValue().equals(Materials.Rubber))
                RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                        .input(OrePrefix.cableGtSingle, Materials.Tungsten)
                        .input(OrePrefix.plate, materialEntry.getValue(), 6)
                        .inputs(MetaItems.ELECTRIC_MOTOR_IV.getStackForm(2))
                        .circuitMeta(1)
                        .outputs(MetaItems.CONVEYOR_MODULE_IV.getStackForm())
                        .duration(100).EUt(VA[LV]).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(OrePrefix.gearSmall, Materials.HSSG, 2)
                    .input(OrePrefix.ring, Materials.HSSG, 2)
                    .input(OrePrefix.screw, Materials.HSSG, 4)
                    .input(OrePrefix.round, Materials.HSSG, 16)
                    .inputs(MetaItems.ELECTRIC_MOTOR_LUV.getStackForm(2))
                    .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                    .fluidInputs(Materials.StyreneButadieneRubber.getFluid(GTValues.L * 8))
                    .fluidInputs(Materials.Lubricant.getFluid(250))
                    .outputs(MetaItems.CONVEYOR_MODULE_LUV.getStackForm())
                    .duration(600).EUt(5760).buildAndRegister();

            RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                    .input(OrePrefix.gearSmall, Materials.NaquadahAlloy, 2)
                    .input(OrePrefix.ring, Materials.NaquadahAlloy, 2)
                    .input(OrePrefix.screw, Materials.NaquadahAlloy, 4)
                    .input(OrePrefix.round, Materials.NaquadahAlloy, 16)
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
                    .input(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate, 2)
                    .fluidInputs(Materials.Polybenzimidazole.getFluid(GTValues.L * 8))
                    .fluidInputs(Materials.Lubricant.getFluid(1000))
                    .outputs(MetaItems.CONVEYOR_MODULE_UV.getStackForm())
                    .duration(600).EUt(92160).buildAndRegister();


            //Pumps Start---------------------------------------------------------------------------------------------------
            ModHandler.addShapedRecipe(String.format("electric_pump/%s/electric_pump_lv", materialEntry.getKey()), MetaItems.ELECTRIC_PUMP_LV.getStackForm(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(OrePrefix.screw, Materials.Tin), 'X', new UnificationEntry(OrePrefix.rotor, Materials.Tin), 'P', new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Bronze), 'R', new UnificationEntry(OrePrefix.ring, materialEntry.getValue()), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tin), 'M', MetaItems.ELECTRIC_MOTOR_LV.getStackForm());
            ModHandler.addShapedRecipe(String.format("electric_pump/%s/electric_pump_mv", materialEntry.getKey()), MetaItems.ELECTRIC_PUMP_MV.getStackForm(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(OrePrefix.screw, Materials.Bronze), 'X', new UnificationEntry(OrePrefix.rotor, Materials.Bronze), 'P', new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Steel), 'R', new UnificationEntry(OrePrefix.ring, materialEntry.getValue()), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Copper), 'M', MetaItems.ELECTRIC_MOTOR_MV.getStackForm());
            ModHandler.addShapedRecipe(String.format("electric_pump/%s/electric_pump_hv", materialEntry.getKey()), MetaItems.ELECTRIC_PUMP_HV.getStackForm(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(OrePrefix.screw, Materials.Steel), 'X', new UnificationEntry(OrePrefix.rotor, Materials.Steel), 'P', new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.StainlessSteel), 'R', new UnificationEntry(OrePrefix.ring, materialEntry.getValue()), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Gold), 'M', MetaItems.ELECTRIC_MOTOR_HV.getStackForm());
            ModHandler.addShapedRecipe(String.format("electric_pump/%s/electric_pump_ev", materialEntry.getKey()), MetaItems.ELECTRIC_PUMP_EV.getStackForm(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(OrePrefix.screw, Materials.StainlessSteel), 'X', new UnificationEntry(OrePrefix.rotor, Materials.StainlessSteel), 'P', new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.Titanium), 'R', new UnificationEntry(OrePrefix.ring, materialEntry.getValue()), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Aluminium), 'M', MetaItems.ELECTRIC_MOTOR_EV.getStackForm());
            if (!materialEntry.getValue().equals(Materials.Rubber))
                ModHandler.addShapedRecipe(String.format("electric_pump/%s/electric_pump_iv", materialEntry.getKey()), MetaItems.ELECTRIC_PUMP_IV.getStackForm(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(OrePrefix.screw, Materials.TungstenSteel), 'X', new UnificationEntry(OrePrefix.rotor, Materials.TungstenSteel), 'P', new UnificationEntry(OrePrefix.pipeNormalFluid, Materials.TungstenSteel), 'R', new UnificationEntry(OrePrefix.ring, materialEntry.getValue()), 'C', new UnificationEntry(OrePrefix.cableGtSingle, Materials.Tungsten), 'M', MetaItems.ELECTRIC_MOTOR_IV.getStackForm());

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Tin)
                    .input(OrePrefix.pipeNormalFluid, Materials.Bronze)
                    .input(OrePrefix.screw, Materials.Tin)
                    .input(OrePrefix.rotor, Materials.Tin)
                    .input(OrePrefix.ring, materialEntry.getValue(), 2)
                    .inputs(MetaItems.ELECTRIC_MOTOR_LV.getStackForm())
                    .outputs(MetaItems.ELECTRIC_PUMP_LV.getStackForm())
                    .duration(100).EUt(VA[LV]).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Copper)
                    .input(OrePrefix.pipeNormalFluid, Materials.Steel)
                    .input(OrePrefix.screw, Materials.Bronze)
                    .input(OrePrefix.rotor, Materials.Bronze)
                    .input(OrePrefix.ring, materialEntry.getValue(), 2)
                    .inputs(MetaItems.ELECTRIC_MOTOR_MV.getStackForm())
                    .outputs(MetaItems.ELECTRIC_PUMP_MV.getStackForm())
                    .duration(100).EUt(VA[LV]).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Copper)
                    .input(OrePrefix.pipeNormalFluid, Materials.StainlessSteel)
                    .input(OrePrefix.screw, Materials.Steel)
                    .input(OrePrefix.rotor, Materials.Steel)
                    .input(OrePrefix.ring, materialEntry.getValue(), 2)
                    .inputs(MetaItems.ELECTRIC_MOTOR_HV.getStackForm())
                    .outputs(MetaItems.ELECTRIC_PUMP_HV.getStackForm())
                    .duration(100).EUt(VA[LV]).buildAndRegister();

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(OrePrefix.cableGtSingle, Materials.Aluminium)
                    .input(OrePrefix.pipeNormalFluid, Materials.Titanium)
                    .input(OrePrefix.screw, Materials.StainlessSteel)
                    .input(OrePrefix.rotor, Materials.StainlessSteel)
                    .input(OrePrefix.ring, materialEntry.getValue(), 2)
                    .inputs(MetaItems.ELECTRIC_MOTOR_EV.getStackForm())
                    .outputs(MetaItems.ELECTRIC_PUMP_EV.getStackForm())
                    .duration(100).EUt(VA[LV]).buildAndRegister();

            if (!materialEntry.getValue().equals(Materials.Rubber))
                RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                        .input(OrePrefix.cableGtSingle, Materials.Tungsten)
                        .input(OrePrefix.pipeNormalFluid, Materials.TungstenSteel)
                        .input(OrePrefix.screw, Materials.TungstenSteel)
                        .input(OrePrefix.rotor, Materials.TungstenSteel)
                        .input(OrePrefix.ring, materialEntry.getValue(), 2)
                        .inputs(MetaItems.ELECTRIC_MOTOR_IV.getStackForm())
                        .outputs(MetaItems.ELECTRIC_PUMP_IV.getStackForm())
                        .duration(100).EUt(VA[LV]).buildAndRegister();
        }

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.HSSG, 2)
                .input(OrePrefix.pipeNormalFluid, Materials.NiobiumTitanium)
                .input(OrePrefix.rotor, Materials.HSSG)
                .input(OrePrefix.screw, Materials.HSSG, 4)
                .inputs(MetaItems.ELECTRIC_MOTOR_LUV.getStackForm())
                .input(OrePrefix.cableGtSingle, Materials.NiobiumTitanium, 2)
                .fluidInputs(Materials.SiliconeRubber.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(250))
                .outputs(MetaItems.ELECTRIC_PUMP_LUV.getStackForm())
                .duration(600).EUt(5760).buildAndRegister();

        RecipeMaps.ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .input(OrePrefix.plate, Materials.NaquadahAlloy, 2)
                .input(OrePrefix.pipeNormalFluid, Materials.Iridium)
                .input(OrePrefix.rotor, Materials.NaquadahAlloy)
                .input(OrePrefix.screw, Materials.NaquadahAlloy, 4)
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
                .input(OrePrefix.cableGtSingle, Materials.YttriumBariumCuprate, 2)
                .fluidInputs(Materials.SiliconeRubber.getFluid(GTValues.L))
                .fluidInputs(Materials.Lubricant.getFluid(1000))
                .outputs(MetaItems.ELECTRIC_PUMP_UV.getStackForm())
                .duration(600).EUt(92160).buildAndRegister();

        //Fluid Regulators----------------------------------------------------------------------------------------------
        Material[] circuitTiers = new Material[]{MarkerMaterials.Tier.Basic, MarkerMaterials.Tier.Good, MarkerMaterials.Tier.Advanced, MarkerMaterials.Tier.Extreme,
                MarkerMaterials.Tier.Elite, MarkerMaterials.Tier.Master, MarkerMaterials.Tier.Ultimate, MarkerMaterials.Tier.Super};

        for (int i = 0; i < FLUID_REGULATORS.length; i++) {
            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder().circuitMeta(1)
                    .inputs(PUMPS[i].getStackForm())
                    .input(OrePrefix.circuit, circuitTiers[i], 2)
                    .outputs(FLUID_REGULATORS[i].getStackForm())
                    .EUt(GTValues.VA[i + 1])
                    .duration(400 - 50 * i)
                    .buildAndRegister();
        }
    }
}
