package gregtech.loaders.recipe;

import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.stack.UnificationEntry;

import net.minecraft.init.Items;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLY_LINE_RECIPES;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.items.MetaItems.*;

public class ComponentRecipes {

    public static void register() {
        // Motors
        // Start--------------------------------------------------------------------------------------------------
        ModHandler.addShapedRecipe("electric_motor_lv_steel", ELECTRIC_MOTOR_LV.getStackForm(), "CWR", "WMW", "RWC",
                'C', new UnificationEntry(cableGtSingle, Tin), 'W', new UnificationEntry(wireGtSingle, Copper), 'R',
                new UnificationEntry(stick, Steel), 'M', new UnificationEntry(stick, SteelMagnetic));
        ModHandler.addShapedRecipe(true, "electric_motor_lv_iron", ELECTRIC_MOTOR_LV.getStackForm(), "CWR", "WMW",
                "RWC", 'C', new UnificationEntry(cableGtSingle, Tin), 'W', new UnificationEntry(wireGtSingle, Copper),
                'R', new UnificationEntry(stick, Iron), 'M', new UnificationEntry(stick, IronMagnetic));
        ModHandler.addShapedRecipe(true, "electric_motor_mv", ELECTRIC_MOTOR_MV.getStackForm(), "CWR", "WMW", "RWC",
                'C', new UnificationEntry(cableGtSingle, Copper), 'W', new UnificationEntry(wireGtDouble, Cupronickel),
                'R', new UnificationEntry(stick, Aluminium), 'M', new UnificationEntry(stick, SteelMagnetic));
        ModHandler.addShapedRecipe(true, "electric_motor_hv", ELECTRIC_MOTOR_HV.getStackForm(), "CWR", "WMW", "RWC",
                'C', new UnificationEntry(cableGtDouble, Silver), 'W', new UnificationEntry(wireGtDouble, Electrum),
                'R', new UnificationEntry(stick, StainlessSteel), 'M', new UnificationEntry(stick, SteelMagnetic));
        ModHandler.addShapedRecipe(true, "electric_motor_ev", ELECTRIC_MOTOR_EV.getStackForm(), "CWR", "WMW", "RWC",
                'C', new UnificationEntry(cableGtDouble, Aluminium), 'W', new UnificationEntry(wireGtDouble, Kanthal),
                'R', new UnificationEntry(stick, Titanium), 'M', new UnificationEntry(stick, NeodymiumMagnetic));
        ModHandler.addShapedRecipe(true, "electric_motor_iv", ELECTRIC_MOTOR_IV.getStackForm(), "CWR", "WMW", "RWC",
                'C', new UnificationEntry(cableGtDouble, Tungsten), 'W', new UnificationEntry(wireGtDouble, Graphene),
                'R', new UnificationEntry(stick, TungstenSteel), 'M', new UnificationEntry(stick, NeodymiumMagnetic));

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtSingle, Tin, 2)
                .inputItem(stick, Iron, 2)
                .inputItem(stick, IronMagnetic)
                .inputItem(wireGtSingle, Copper, 4)
                .outputs(ELECTRIC_MOTOR_LV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtSingle, Tin, 2)
                .inputItem(stick, Steel, 2)
                .inputItem(stick, SteelMagnetic)
                .inputItem(wireGtSingle, Copper, 4)
                .outputs(ELECTRIC_MOTOR_LV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtSingle, Copper, 2)
                .inputItem(stick, Aluminium, 2)
                .inputItem(stick, SteelMagnetic)
                .inputItem(wireGtDouble, Cupronickel, 4)
                .outputs(ELECTRIC_MOTOR_MV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtDouble, Silver, 2)
                .inputItem(stick, StainlessSteel, 2)
                .inputItem(stick, SteelMagnetic)
                .inputItem(wireGtDouble, Electrum, 4)
                .outputs(ELECTRIC_MOTOR_HV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtDouble, Aluminium, 2)
                .inputItem(stick, Titanium, 2)
                .inputItem(stick, NeodymiumMagnetic)
                .inputItem(wireGtDouble, Kanthal, 4)
                .outputs(ELECTRIC_MOTOR_EV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtDouble, Tungsten, 2)
                .inputItem(stick, TungstenSteel, 2)
                .inputItem(stick, NeodymiumMagnetic)
                .inputItem(wireGtDouble, Graphene, 4)
                .outputs(ELECTRIC_MOTOR_IV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(stickLong, SamariumMagnetic)
                .inputItem(stickLong, HSSS, 2)
                .inputItem(ring, HSSS, 2)
                .inputItem(round, HSSS, 4)
                .inputItem(wireFine, Ruridit, 64)
                .inputItem(cableGtSingle, NiobiumTitanium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L))
                .fluidInputs(Lubricant.getFluid(250))
                .outputItem(ELECTRIC_MOTOR_LuV)
                .scannerResearch(ELECTRIC_MOTOR_IV.getStackForm())
                .duration(600).volts(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(stickLong, SamariumMagnetic)
                .inputItem(stickLong, Osmiridium, 4)
                .inputItem(ring, Osmiridium, 4)
                .inputItem(round, Osmiridium, 8)
                .inputItem(wireFine, Europium, 64)
                .inputItem(wireFine, Europium, 32)
                .inputItem(cableGtSingle, VanadiumGallium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .fluidInputs(Lubricant.getFluid(500))
                .outputItem(ELECTRIC_MOTOR_ZPM)
                .scannerResearch(b -> b
                        .researchStack(ELECTRIC_MOTOR_LuV.getStackForm())
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(600).volts(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(stickLong, SamariumMagnetic)
                .inputItem(stickLong, Tritanium, 4)
                .inputItem(ring, Tritanium, 4)
                .inputItem(round, Tritanium, 8)
                .inputItem(wireFine, Americium, 64)
                .inputItem(wireFine, Americium, 64)
                .inputItem(cableGtSingle, YttriumBariumCuprate, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Lubricant.getFluid(1000))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputItem(ELECTRIC_MOTOR_UV)
                .stationResearch(b -> b
                        .researchStack(ELECTRIC_MOTOR_ZPM.getStackForm())
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(600).volts(100000).buildAndRegister();

        // Conveyors
        // Start-----------------------------------------------------------------------------------------------
        final Map<String, Material> rubberMaterials = new Object2ObjectOpenHashMap<>();
        rubberMaterials.put("rubber", Rubber);
        rubberMaterials.put("silicone_rubber", SiliconeRubber);
        rubberMaterials.put("styrene_butadiene_rubber", StyreneButadieneRubber);

        for (Map.Entry<String, Material> materialEntry : rubberMaterials.entrySet()) {
            Material material = materialEntry.getValue();
            String name = materialEntry.getKey();

            ModHandler.addShapedRecipe(material.equals(Rubber), String.format("conveyor_module_lv_%s", name),
                    CONVEYOR_MODULE_LV.getStackForm(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material),
                    'C', new UnificationEntry(cableGtSingle, Tin), 'M', ELECTRIC_MOTOR_LV.getStackForm());
            ModHandler.addShapedRecipe(material.equals(Rubber), String.format("conveyor_module_mv_%s", name),
                    CONVEYOR_MODULE_MV.getStackForm(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material),
                    'C', new UnificationEntry(cableGtSingle, Copper), 'M', ELECTRIC_MOTOR_MV.getStackForm());
            ModHandler.addShapedRecipe(material.equals(Rubber), String.format("conveyor_module_hv_%s", name),
                    CONVEYOR_MODULE_HV.getStackForm(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material),
                    'C', new UnificationEntry(cableGtSingle, Gold), 'M', ELECTRIC_MOTOR_HV.getStackForm());
            ModHandler.addShapedRecipe(material.equals(Rubber), String.format("conveyor_module_ev_%s", name),
                    CONVEYOR_MODULE_EV.getStackForm(), "RRR", "MCM", "RRR", 'R', new UnificationEntry(plate, material),
                    'C', new UnificationEntry(cableGtSingle, Aluminium), 'M', ELECTRIC_MOTOR_EV.getStackForm());
            if (!materialEntry.getValue().equals(Rubber))
                ModHandler.addShapedRecipe(material.equals(SiliconeRubber),
                        String.format("conveyor_module_iv_%s", materialEntry.getKey()),
                        CONVEYOR_MODULE_IV.getStackForm(), "RRR", "MCM", "RRR", 'R',
                        new UnificationEntry(plate, material), 'C', new UnificationEntry(cableGtSingle, Tungsten), 'M',
                        ELECTRIC_MOTOR_IV.getStackForm());

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(cableGtSingle, Tin)
                    .inputs(ELECTRIC_MOTOR_LV.getStackForm(2))
                    .fluidInputs(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputs(CONVEYOR_MODULE_LV.getStackForm())
                    .duration(100).volts(VA[LV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(cableGtSingle, Copper)
                    .inputs(ELECTRIC_MOTOR_MV.getStackForm(2))
                    .fluidInputs(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputs(CONVEYOR_MODULE_MV.getStackForm())
                    .duration(100).volts(VA[LV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(cableGtSingle, Gold)
                    .inputs(ELECTRIC_MOTOR_HV.getStackForm(2))
                    .fluidInputs(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputs(CONVEYOR_MODULE_HV.getStackForm())
                    .duration(100).volts(VA[LV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(cableGtSingle, Aluminium)
                    .inputs(ELECTRIC_MOTOR_EV.getStackForm(2))
                    .fluidInputs(materialEntry.getValue().getFluid(L * 6))
                    .circuitMeta(1)
                    .outputs(CONVEYOR_MODULE_EV.getStackForm())
                    .duration(100).volts(VA[LV]).buildAndRegister();

            if (!materialEntry.getValue().equals(Rubber))
                ASSEMBLER_RECIPES.recipeBuilder()
                        .inputItem(cableGtSingle, Tungsten)
                        .inputs(ELECTRIC_MOTOR_IV.getStackForm(2))
                        .fluidInputs(materialEntry.getValue().getFluid(L * 6))
                        .circuitMeta(1)
                        .outputs(CONVEYOR_MODULE_IV.getStackForm())
                        .duration(100).volts(VA[LV]).buildAndRegister();

            // Pumps
            // Start---------------------------------------------------------------------------------------------------
            ModHandler.addShapedRecipe(material.equals(Rubber), String.format("electric_pump_lv_%s", name),
                    ELECTRIC_PUMP_LV.getStackForm(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, Tin), 'X',
                    new UnificationEntry(rotor, Tin), 'P', new UnificationEntry(pipeNormalFluid, Bronze), 'R',
                    new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, Tin), 'M',
                    ELECTRIC_MOTOR_LV.getStackForm());
            ModHandler.addShapedRecipe(material.equals(Rubber), String.format("electric_pump_mv_%s", name),
                    ELECTRIC_PUMP_MV.getStackForm(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, Bronze), 'X',
                    new UnificationEntry(rotor, Bronze), 'P', new UnificationEntry(pipeNormalFluid, Steel), 'R',
                    new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, Copper), 'M',
                    ELECTRIC_MOTOR_MV.getStackForm());
            ModHandler.addShapedRecipe(material.equals(Rubber), String.format("electric_pump_hv_%s", name),
                    ELECTRIC_PUMP_HV.getStackForm(), "SXR", "dPw", "RMC", 'S', new UnificationEntry(screw, Steel), 'X',
                    new UnificationEntry(rotor, Steel), 'P', new UnificationEntry(pipeNormalFluid, StainlessSteel), 'R',
                    new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, Gold), 'M',
                    ELECTRIC_MOTOR_HV.getStackForm());
            ModHandler.addShapedRecipe(material.equals(Rubber), String.format("electric_pump_ev_%s", name),
                    ELECTRIC_PUMP_EV.getStackForm(), "SXR", "dPw", "RMC", 'S',
                    new UnificationEntry(screw, StainlessSteel), 'X', new UnificationEntry(rotor, StainlessSteel), 'P',
                    new UnificationEntry(pipeNormalFluid, Titanium), 'R', new UnificationEntry(ring, material), 'C',
                    new UnificationEntry(cableGtSingle, Aluminium), 'M', ELECTRIC_MOTOR_EV.getStackForm());
            if (!material.equals(Rubber))
                ModHandler.addShapedRecipe(material.equals(SiliconeRubber), String.format("electric_pump_iv_%s", name),
                        ELECTRIC_PUMP_IV.getStackForm(), "SXR", "dPw", "RMC", 'S',
                        new UnificationEntry(screw, TungstenSteel), 'X', new UnificationEntry(rotor, TungstenSteel),
                        'P', new UnificationEntry(pipeNormalFluid, TungstenSteel), 'R',
                        new UnificationEntry(ring, material), 'C', new UnificationEntry(cableGtSingle, Tungsten), 'M',
                        ELECTRIC_MOTOR_IV.getStackForm());

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(cableGtSingle, Tin)
                    .inputItem(pipeNormalFluid, Bronze)
                    .inputItem(screw, Tin)
                    .inputItem(rotor, Tin)
                    .inputItem(ring, materialEntry.getValue(), 2)
                    .inputs(ELECTRIC_MOTOR_LV.getStackForm())
                    .outputs(ELECTRIC_PUMP_LV.getStackForm())
                    .duration(100).volts(VA[LV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(cableGtSingle, Copper)
                    .inputItem(pipeNormalFluid, Steel)
                    .inputItem(screw, Bronze)
                    .inputItem(rotor, Bronze)
                    .inputItem(ring, materialEntry.getValue(), 2)
                    .inputs(ELECTRIC_MOTOR_MV.getStackForm())
                    .outputs(ELECTRIC_PUMP_MV.getStackForm())
                    .duration(100).volts(VA[LV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(cableGtSingle, Gold)
                    .inputItem(pipeNormalFluid, StainlessSteel)
                    .inputItem(screw, Steel)
                    .inputItem(rotor, Steel)
                    .inputItem(ring, materialEntry.getValue(), 2)
                    .inputs(ELECTRIC_MOTOR_HV.getStackForm())
                    .outputs(ELECTRIC_PUMP_HV.getStackForm())
                    .duration(100).volts(VA[LV]).buildAndRegister();

            ASSEMBLER_RECIPES.recipeBuilder()
                    .inputItem(cableGtSingle, Aluminium)
                    .inputItem(pipeNormalFluid, Titanium)
                    .inputItem(screw, StainlessSteel)
                    .inputItem(rotor, StainlessSteel)
                    .inputItem(ring, materialEntry.getValue(), 2)
                    .inputs(ELECTRIC_MOTOR_EV.getStackForm())
                    .outputs(ELECTRIC_PUMP_EV.getStackForm())
                    .duration(100).volts(VA[LV]).buildAndRegister();

            if (!materialEntry.getValue().equals(Rubber))
                ASSEMBLER_RECIPES.recipeBuilder()
                        .inputItem(cableGtSingle, Tungsten)
                        .inputItem(pipeNormalFluid, TungstenSteel)
                        .inputItem(screw, TungstenSteel)
                        .inputItem(rotor, TungstenSteel)
                        .inputItem(ring, materialEntry.getValue(), 2)
                        .inputs(ELECTRIC_MOTOR_IV.getStackForm())
                        .outputs(ELECTRIC_PUMP_IV.getStackForm())
                        .duration(100).volts(VA[LV]).buildAndRegister();
        }

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(ELECTRIC_MOTOR_LuV, 2)
                .inputItem(plate, HSSS, 2)
                .inputItem(ring, HSSS, 4)
                .inputItem(round, HSSS, 16)
                .inputItem(screw, HSSS, 4)
                .inputItem(cableGtSingle, NiobiumTitanium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L))
                .fluidInputs(Lubricant.getFluid(250))
                .fluidInputs(StyreneButadieneRubber.getFluid(L * 8))
                .outputItem(CONVEYOR_MODULE_LuV)
                .scannerResearch(CONVEYOR_MODULE_IV.getStackForm())
                .duration(600).volts(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(ELECTRIC_MOTOR_ZPM, 2)
                .inputItem(plate, Osmiridium, 2)
                .inputItem(ring, Osmiridium, 4)
                .inputItem(round, Osmiridium, 16)
                .inputItem(screw, Osmiridium, 4)
                .inputItem(cableGtSingle, VanadiumGallium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .fluidInputs(Lubricant.getFluid(500))
                .fluidInputs(StyreneButadieneRubber.getFluid(L * 16))
                .outputItem(CONVEYOR_MODULE_ZPM)
                .scannerResearch(b -> b
                        .researchStack(CONVEYOR_MODULE_LuV.getStackForm())
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(600).volts(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(ELECTRIC_MOTOR_UV, 2)
                .inputItem(plate, Tritanium, 2)
                .inputItem(ring, Tritanium, 4)
                .inputItem(round, Tritanium, 16)
                .inputItem(screw, Tritanium, 4)
                .inputItem(cableGtSingle, YttriumBariumCuprate, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Lubricant.getFluid(1000))
                .fluidInputs(StyreneButadieneRubber.getFluid(L * 24))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputItem(CONVEYOR_MODULE_UV)
                .stationResearch(b -> b
                        .researchStack(CONVEYOR_MODULE_ZPM.getStackForm())
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(600).volts(100000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(ELECTRIC_MOTOR_LuV)
                .inputItem(pipeSmallFluid, NiobiumTitanium)
                .inputItem(plate, HSSS, 2)
                .inputItem(screw, HSSS, 8)
                .inputItem(ring, SiliconeRubber, 4)
                .inputItem(rotor, HSSS)
                .inputItem(cableGtSingle, NiobiumTitanium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L))
                .fluidInputs(Lubricant.getFluid(250))
                .outputItem(ELECTRIC_PUMP_LuV)
                .scannerResearch(ELECTRIC_PUMP_IV.getStackForm())
                .duration(600).volts(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(ELECTRIC_MOTOR_ZPM)
                .inputItem(pipeNormalFluid, Polybenzimidazole)
                .inputItem(plate, Osmiridium, 2)
                .inputItem(screw, Osmiridium, 8)
                .inputItem(ring, SiliconeRubber, 8)
                .inputItem(rotor, Osmiridium)
                .inputItem(cableGtSingle, VanadiumGallium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .fluidInputs(Lubricant.getFluid(500))
                .outputItem(ELECTRIC_PUMP_ZPM)
                .scannerResearch(b -> b
                        .researchStack(ELECTRIC_PUMP_LuV.getStackForm())
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(600).volts(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(ELECTRIC_MOTOR_UV)
                .inputItem(pipeLargeFluid, Naquadah)
                .inputItem(plate, Tritanium, 2)
                .inputItem(screw, Tritanium, 8)
                .inputItem(ring, SiliconeRubber, 16)
                .inputItem(rotor, NaquadahAlloy)
                .inputItem(cableGtSingle, YttriumBariumCuprate, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Lubricant.getFluid(1000))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputItem(ELECTRIC_PUMP_UV)
                .stationResearch(b -> b
                        .researchStack(ELECTRIC_PUMP_ZPM.getStackForm())
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(600).volts(100000).buildAndRegister();

        // Fluid
        // Regulators----------------------------------------------------------------------------------------------

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(ELECTRIC_PUMP_LV.getStackForm())
                .inputItem(circuit, Tier.LV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_LV.getStackForm()).volts(VA[LV])
                .duration(400)
                .withRecycling()
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(ELECTRIC_PUMP_MV.getStackForm())
                .inputItem(circuit, Tier.MV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_MV.getStackForm()).volts(VA[MV])
                .duration(350)
                .withRecycling()
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(ELECTRIC_PUMP_HV.getStackForm())
                .inputItem(circuit, Tier.HV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_HV.getStackForm()).volts(VA[HV])
                .duration(300)
                .withRecycling()
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(ELECTRIC_PUMP_EV.getStackForm())
                .inputItem(circuit, Tier.EV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_EV.getStackForm()).volts(VA[EV])
                .duration(250)
                .withRecycling()
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(ELECTRIC_PUMP_IV.getStackForm())
                .inputItem(circuit, Tier.IV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_IV.getStackForm()).volts(VA[IV])
                .duration(200)
                .withRecycling()
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(ELECTRIC_PUMP_LuV.getStackForm())
                .inputItem(circuit, Tier.LuV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_LUV.getStackForm()).volts(VA[LuV])
                .duration(150)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(ELECTRIC_PUMP_ZPM.getStackForm())
                .inputItem(circuit, Tier.ZPM, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_ZPM.getStackForm()).volts(VA[ZPM])
                .duration(100)
                .buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputs(ELECTRIC_PUMP_UV.getStackForm())
                .inputItem(circuit, Tier.UV, 2)
                .circuitMeta(1)
                .outputs(FLUID_REGULATOR_UV.getStackForm()).volts(VA[UV])
                .duration(50)
                .buildAndRegister();

        // Voiding Covers Start-----------------------------------------------------------------------------------------

        ModHandler.addShapedRecipe(true, "cover_item_voiding", COVER_ITEM_VOIDING.getStackForm(), "SDS", "dPw", " E ",
                'S', new UnificationEntry(screw, Steel), 'D', COVER_ITEM_DETECTOR.getStackForm(), 'P',
                new UnificationEntry(pipeNormalItem, Brass), 'E', Items.ENDER_PEARL);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(screw, Steel, 2)
                .inputs(COVER_ITEM_DETECTOR.getStackForm())
                .inputItem(pipeNormalItem, Brass)
                .inputItem(Items.ENDER_PEARL)
                .outputs(COVER_ITEM_VOIDING.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(COVER_ITEM_VOIDING)
                .inputItem(circuit, Tier.MV, 1)
                .outputs(COVER_ITEM_VOIDING_ADVANCED.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ModHandler.addShapedRecipe(true, "cover_fluid_voiding", COVER_FLUID_VOIDING.getStackForm(), "SDS", "dPw", " E ",
                'S', new UnificationEntry(screw, Steel), 'D', COVER_FLUID_DETECTOR.getStackForm(), 'P',
                new UnificationEntry(pipeNormalFluid, Bronze), 'E', Items.ENDER_PEARL);

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(screw, Steel, 2)
                .inputs(COVER_FLUID_DETECTOR.getStackForm())
                .inputItem(pipeNormalFluid, Bronze)
                .inputItem(Items.ENDER_PEARL)
                .outputs(COVER_FLUID_VOIDING.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(COVER_FLUID_VOIDING)
                .inputItem(circuit, Tier.MV, 1)
                .outputs(COVER_FLUID_VOIDING_ADVANCED.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        // Pistons
        // Start-------------------------------------------------------------------------------------------------
        ModHandler.addShapedRecipe(true, "electric_piston_lv", ELECTRIC_PISTON_LV.getStackForm(), "PPP", "CRR", "CMG",
                'P', new UnificationEntry(plate, Steel), 'C', new UnificationEntry(cableGtSingle, Tin), 'R',
                new UnificationEntry(stick, Steel), 'G', new UnificationEntry(gearSmall, Steel), 'M',
                ELECTRIC_MOTOR_LV.getStackForm());
        ModHandler.addShapedRecipe(true, "electric_piston_mv", ELECTRIC_PISTON_MV.getStackForm(), "PPP", "CRR", "CMG",
                'P', new UnificationEntry(plate, Aluminium), 'C', new UnificationEntry(cableGtSingle, Copper), 'R',
                new UnificationEntry(stick, Aluminium), 'G', new UnificationEntry(gearSmall, Aluminium), 'M',
                ELECTRIC_MOTOR_MV.getStackForm());
        ModHandler.addShapedRecipe(true, "electric_piston_hv", ELECTRIC_PISTON_HV.getStackForm(), "PPP", "CRR", "CMG",
                'P', new UnificationEntry(plate, StainlessSteel), 'C', new UnificationEntry(cableGtSingle, Gold), 'R',
                new UnificationEntry(stick, StainlessSteel), 'G', new UnificationEntry(gearSmall, StainlessSteel), 'M',
                ELECTRIC_MOTOR_HV.getStackForm());
        ModHandler.addShapedRecipe(true, "electric_piston_ev", ELECTRIC_PISTON_EV.getStackForm(), "PPP", "CRR", "CMG",
                'P', new UnificationEntry(plate, Titanium), 'C', new UnificationEntry(cableGtSingle, Aluminium), 'R',
                new UnificationEntry(stick, Titanium), 'G', new UnificationEntry(gearSmall, Titanium), 'M',
                ELECTRIC_MOTOR_EV.getStackForm());
        ModHandler.addShapedRecipe(true, "electric_piston_iv", ELECTRIC_PISTON_IV.getStackForm(), "PPP", "CRR", "CMG",
                'P', new UnificationEntry(plate, TungstenSteel), 'C', new UnificationEntry(cableGtSingle, Tungsten),
                'R', new UnificationEntry(stick, TungstenSteel), 'G', new UnificationEntry(gearSmall, TungstenSteel),
                'M', ELECTRIC_MOTOR_IV.getStackForm());

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Steel, 2)
                .inputItem(cableGtSingle, Tin, 2)
                .inputItem(plate, Steel, 3)
                .inputItem(gearSmall, Steel)
                .inputs(ELECTRIC_MOTOR_LV.getStackForm())
                .outputs(ELECTRIC_PISTON_LV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Aluminium, 2)
                .inputItem(cableGtSingle, Copper, 2)
                .inputItem(plate, Aluminium, 3)
                .inputItem(gearSmall, Aluminium)
                .inputs(ELECTRIC_MOTOR_MV.getStackForm())
                .outputs(ELECTRIC_PISTON_MV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, StainlessSteel, 2)
                .inputItem(cableGtSingle, Gold, 2)
                .inputItem(plate, StainlessSteel, 3)
                .inputItem(gearSmall, StainlessSteel)
                .inputs(ELECTRIC_MOTOR_HV.getStackForm())
                .outputs(ELECTRIC_PISTON_HV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Titanium, 2)
                .inputItem(cableGtSingle, Aluminium, 2)
                .inputItem(plate, Titanium, 3)
                .inputItem(gearSmall, Titanium)
                .inputs(ELECTRIC_MOTOR_EV.getStackForm())
                .outputs(ELECTRIC_PISTON_EV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, TungstenSteel, 2)
                .inputItem(cableGtSingle, Tungsten, 2)
                .inputItem(plate, TungstenSteel, 3)
                .inputItem(gearSmall, TungstenSteel)
                .inputs(ELECTRIC_MOTOR_IV.getStackForm())
                .outputs(ELECTRIC_PISTON_IV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(ELECTRIC_MOTOR_LuV)
                .inputItem(plate, HSSS, 4)
                .inputItem(ring, HSSS, 4)
                .inputItem(round, HSSS, 16)
                .inputItem(stick, HSSS, 4)
                .inputItem(gear, HSSS)
                .inputItem(gearSmall, HSSS, 2)
                .inputItem(cableGtSingle, NiobiumTitanium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L))
                .fluidInputs(Lubricant.getFluid(250))
                .outputItem(ELECTRIC_PISTON_LUV)
                .scannerResearch(ELECTRIC_PISTON_IV.getStackForm())
                .duration(600).volts(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(ELECTRIC_MOTOR_ZPM)
                .inputItem(plate, Osmiridium, 4)
                .inputItem(ring, Osmiridium, 4)
                .inputItem(round, Osmiridium, 16)
                .inputItem(stick, Osmiridium, 4)
                .inputItem(gear, Osmiridium)
                .inputItem(gearSmall, Osmiridium, 2)
                .inputItem(cableGtSingle, VanadiumGallium, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .fluidInputs(Lubricant.getFluid(500))
                .outputItem(ELECTRIC_PISTON_ZPM)
                .scannerResearch(b -> b
                        .researchStack(ELECTRIC_PISTON_LUV.getStackForm())
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(600).volts(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(ELECTRIC_MOTOR_UV)
                .inputItem(plate, Tritanium, 4)
                .inputItem(ring, Tritanium, 4)
                .inputItem(round, Tritanium, 16)
                .inputItem(stick, Tritanium, 4)
                .inputItem(gear, NaquadahAlloy)
                .inputItem(gearSmall, NaquadahAlloy, 2)
                .inputItem(cableGtSingle, YttriumBariumCuprate, 2)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Lubricant.getFluid(1000))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputItem(ELECTRIC_PISTON_UV)
                .stationResearch(b -> b
                        .researchStack(ELECTRIC_PISTON_ZPM.getStackForm())
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(600).volts(100000).buildAndRegister();

        // Robot Arms Start
        // ---------------------------------------------------------------------------------------------
        ModHandler.addShapedRecipe(true, "robot_arm_lv", ROBOT_ARM_LV.getStackForm(), "CCC", "MRM", "PXR", 'C',
                new UnificationEntry(cableGtSingle, Tin), 'R', new UnificationEntry(stick, Steel), 'M',
                ELECTRIC_MOTOR_LV.getStackForm(), 'P', ELECTRIC_PISTON_LV.getStackForm(), 'X',
                new UnificationEntry(circuit, Tier.LV));
        ModHandler.addShapedRecipe(true, "robot_arm_mv", ROBOT_ARM_MV.getStackForm(), "CCC", "MRM", "PXR", 'C',
                new UnificationEntry(cableGtSingle, Copper), 'R', new UnificationEntry(stick, Aluminium), 'M',
                ELECTRIC_MOTOR_MV.getStackForm(), 'P', ELECTRIC_PISTON_MV.getStackForm(), 'X',
                new UnificationEntry(circuit, Tier.MV));
        ModHandler.addShapedRecipe(true, "robot_arm_hv", ROBOT_ARM_HV.getStackForm(), "CCC", "MRM", "PXR", 'C',
                new UnificationEntry(cableGtSingle, Gold), 'R', new UnificationEntry(stick, StainlessSteel), 'M',
                ELECTRIC_MOTOR_HV.getStackForm(), 'P', ELECTRIC_PISTON_HV.getStackForm(), 'X',
                new UnificationEntry(circuit, Tier.HV));
        ModHandler.addShapedRecipe(true, "robot_arm_ev", ROBOT_ARM_EV.getStackForm(), "CCC", "MRM", "PXR", 'C',
                new UnificationEntry(cableGtSingle, Aluminium), 'R', new UnificationEntry(stick, Titanium), 'M',
                ELECTRIC_MOTOR_EV.getStackForm(), 'P', ELECTRIC_PISTON_EV.getStackForm(), 'X',
                new UnificationEntry(circuit, Tier.EV));
        ModHandler.addShapedRecipe(true, "robot_arm_iv", ROBOT_ARM_IV.getStackForm(), "CCC", "MRM", "PXR", 'C',
                new UnificationEntry(cableGtSingle, Tungsten), 'R', new UnificationEntry(stick, TungstenSteel), 'M',
                ELECTRIC_MOTOR_IV.getStackForm(), 'P', ELECTRIC_PISTON_IV.getStackForm(), 'X',
                new UnificationEntry(circuit, Tier.IV));

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtSingle, Tin, 3)
                .inputItem(stick, Steel, 2)
                .inputs(ELECTRIC_MOTOR_LV.getStackForm(2))
                .inputs(ELECTRIC_PISTON_LV.getStackForm())
                .inputItem(circuit, Tier.LV)
                .outputs(ROBOT_ARM_LV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtSingle, Copper, 3)
                .inputItem(stick, Aluminium, 2)
                .inputs(ELECTRIC_MOTOR_MV.getStackForm(2))
                .inputs(ELECTRIC_PISTON_MV.getStackForm())
                .inputItem(circuit, Tier.MV)
                .outputs(ROBOT_ARM_MV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtSingle, Gold, 3)
                .inputItem(stick, StainlessSteel, 2)
                .inputs(ELECTRIC_MOTOR_HV.getStackForm(2))
                .inputs(ELECTRIC_PISTON_HV.getStackForm())
                .inputItem(circuit, Tier.HV)
                .outputs(ROBOT_ARM_HV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtSingle, Aluminium, 3)
                .inputItem(stick, Titanium, 2)
                .inputs(ELECTRIC_MOTOR_EV.getStackForm(2))
                .inputs(ELECTRIC_PISTON_EV.getStackForm())
                .inputItem(circuit, Tier.EV)
                .outputs(ROBOT_ARM_EV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(cableGtSingle, Tungsten, 3)
                .inputItem(stick, TungstenSteel, 2)
                .inputs(ELECTRIC_MOTOR_IV.getStackForm(2))
                .inputs(ELECTRIC_PISTON_IV.getStackForm())
                .inputItem(circuit, Tier.IV)
                .outputs(ROBOT_ARM_IV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(stickLong, HSSS, 4)
                .inputItem(gear, HSSS)
                .inputItem(gearSmall, HSSS, 3)
                .inputItem(ELECTRIC_MOTOR_LuV, 2)
                .inputItem(ELECTRIC_PISTON_LUV)
                .inputItem(circuit, Tier.LuV)
                .inputItem(circuit, Tier.IV, 2)
                .inputItem(circuit, Tier.EV, 4)
                .inputItem(cableGtSingle, NiobiumTitanium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .fluidInputs(Lubricant.getFluid(250))
                .outputItem(ROBOT_ARM_LuV)
                .scannerResearch(ROBOT_ARM_IV.getStackForm())
                .duration(600).volts(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(stickLong, Osmiridium, 4)
                .inputItem(gear, Osmiridium)
                .inputItem(gearSmall, Osmiridium, 3)
                .inputItem(ELECTRIC_MOTOR_ZPM, 2)
                .inputItem(ELECTRIC_PISTON_ZPM)
                .inputItem(circuit, Tier.ZPM)
                .inputItem(circuit, Tier.LuV, 2)
                .inputItem(circuit, Tier.IV, 4)
                .inputItem(cableGtSingle, VanadiumGallium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(Lubricant.getFluid(500))
                .outputItem(ROBOT_ARM_ZPM)
                .scannerResearch(b -> b
                        .researchStack(ROBOT_ARM_LuV.getStackForm())
                        .duration(1200)
                        .EUt(VA[IV]))
                .duration(600).volts(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(stickLong, Tritanium, 4)
                .inputItem(gear, Tritanium)
                .inputItem(gearSmall, Tritanium, 3)
                .inputItem(ELECTRIC_MOTOR_UV, 2)
                .inputItem(ELECTRIC_PISTON_UV)
                .inputItem(circuit, Tier.UV)
                .inputItem(circuit, Tier.ZPM, 2)
                .inputItem(circuit, Tier.LuV, 4)
                .inputItem(cableGtSingle, YttriumBariumCuprate, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 12))
                .fluidInputs(Lubricant.getFluid(1000))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputItem(ROBOT_ARM_UV)
                .stationResearch(b -> b
                        .researchStack(ROBOT_ARM_ZPM.getStackForm())
                        .CWUt(32)
                        .EUt(VA[ZPM]))
                .duration(600).volts(100000).buildAndRegister();

        // Field Generators Start
        // ---------------------------------------------------------------------------------------
        ModHandler.addShapedRecipe(true, "field_generator_lv", FIELD_GENERATOR_LV.getStackForm(), "WPW", "XGX", "WPW",
                'W', new UnificationEntry(wireGtQuadruple, ManganesePhosphide), 'P', new UnificationEntry(plate, Steel),
                'G', new UnificationEntry(gem, EnderPearl), 'X', new UnificationEntry(circuit, Tier.LV));
        ModHandler.addShapedRecipe(true, "field_generator_mv", FIELD_GENERATOR_MV.getStackForm(), "WPW", "XGX", "WPW",
                'W', new UnificationEntry(wireGtQuadruple, MagnesiumDiboride), 'P',
                new UnificationEntry(plate, Aluminium), 'G', new UnificationEntry(gem, EnderEye), 'X',
                new UnificationEntry(circuit, Tier.MV));
        ModHandler.addShapedRecipe(true, "field_generator_hv", FIELD_GENERATOR_HV.getStackForm(), "WPW", "XGX", "WPW",
                'W', new UnificationEntry(wireGtQuadruple, MercuryBariumCalciumCuprate), 'P',
                new UnificationEntry(plate, StainlessSteel), 'G', QUANTUM_EYE.getStackForm(), 'X',
                new UnificationEntry(circuit, Tier.HV));
        ModHandler.addShapedRecipe(true, "field_generator_ev", FIELD_GENERATOR_EV.getStackForm(), "WPW", "XGX", "WPW",
                'W', new UnificationEntry(wireGtQuadruple, UraniumTriplatinum), 'P',
                new UnificationEntry(plateDouble, Titanium), 'G', new UnificationEntry(gem, NetherStar), 'X',
                new UnificationEntry(circuit, Tier.EV));
        ModHandler.addShapedRecipe(true, "field_generator_iv", FIELD_GENERATOR_IV.getStackForm(), "WPW", "XGX", "WPW",
                'W', new UnificationEntry(wireGtQuadruple, SamariumIronArsenicOxide), 'P',
                new UnificationEntry(plateDouble, TungstenSteel), 'G', QUANTUM_STAR.getStackForm(), 'X',
                new UnificationEntry(circuit, Tier.IV));

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(gem, EnderPearl)
                .inputItem(plate, Steel, 2)
                .inputItem(circuit, Tier.LV, 2)
                .inputItem(wireGtQuadruple, ManganesePhosphide, 4)
                .outputs(FIELD_GENERATOR_LV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(gem, EnderEye)
                .inputItem(plate, Aluminium, 2)
                .inputItem(circuit, Tier.MV, 2)
                .inputItem(wireGtQuadruple, MagnesiumDiboride, 4)
                .outputs(FIELD_GENERATOR_MV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(QUANTUM_EYE)
                .inputItem(plate, StainlessSteel, 2)
                .inputItem(circuit, Tier.HV, 2)
                .inputItem(wireGtQuadruple, MercuryBariumCalciumCuprate, 4)
                .outputs(FIELD_GENERATOR_HV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(gem, NetherStar)
                .inputItem(plateDouble, Titanium, 2)
                .inputItem(circuit, Tier.EV, 2)
                .inputItem(wireGtQuadruple, UraniumTriplatinum, 4)
                .outputs(FIELD_GENERATOR_EV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(QUANTUM_STAR)
                .inputItem(plateDouble, TungstenSteel, 2)
                .inputItem(circuit, Tier.IV, 2)
                .inputItem(wireGtQuadruple, SamariumIronArsenicOxide, 4)
                .outputs(FIELD_GENERATOR_IV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(frameGt, HSSS)
                .inputItem(plate, HSSS, 6)
                .inputItem(QUANTUM_STAR)
                .inputItem(EMITTER_LuV, 2)
                .inputItem(circuit, Tier.LuV, 2)
                .inputItem(wireFine, IndiumTinBariumTitaniumCuprate, 64)
                .inputItem(wireFine, IndiumTinBariumTitaniumCuprate, 64)
                .inputItem(cableGtSingle, NiobiumTitanium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .outputItem(FIELD_GENERATOR_LuV)
                .scannerResearch(b -> b
                        .researchStack(FIELD_GENERATOR_IV.getStackForm())
                        .duration(2400))
                .duration(600).volts(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(frameGt, NaquadahAlloy)
                .inputItem(plate, NaquadahAlloy, 6)
                .inputItem(QUANTUM_STAR)
                .inputItem(EMITTER_ZPM, 2)
                .inputItem(circuit, Tier.ZPM, 2)
                .inputItem(wireFine, UraniumRhodiumDinaquadide, 64)
                .inputItem(wireFine, UraniumRhodiumDinaquadide, 64)
                .inputItem(cableGtSingle, VanadiumGallium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .outputItem(FIELD_GENERATOR_ZPM)
                .stationResearch(b -> b
                        .researchStack(FIELD_GENERATOR_LuV.getStackForm())
                        .CWUt(4))
                .duration(600).volts(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(frameGt, Tritanium)
                .inputItem(plate, Tritanium, 6)
                .inputItem(GRAVI_STAR)
                .inputItem(EMITTER_UV, 2)
                .inputItem(circuit, Tier.UV, 2)
                .inputItem(wireFine, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItem(wireFine, EnrichedNaquadahTriniumEuropiumDuranide, 64)
                .inputItem(cableGtSingle, YttriumBariumCuprate, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 12))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputItem(FIELD_GENERATOR_UV)
                .stationResearch(b -> b
                        .researchStack(FIELD_GENERATOR_ZPM.getStackForm())
                        .CWUt(48)
                        .EUt(VA[ZPM]))
                .duration(600).volts(100000).buildAndRegister();

        // Sensors
        // Start-------------------------------------------------------------------------------------------------
        ModHandler.addShapedRecipe(true, "sensor_lv", SENSOR_LV.getStackForm(), "P G", "PR ", "XPP", 'P',
                new UnificationEntry(plate, Steel), 'R', new UnificationEntry(stick, Brass), 'G',
                new UnificationEntry(gem, Quartzite), 'X', new UnificationEntry(circuit, Tier.LV));
        ModHandler.addShapedRecipe(true, "sensor_mv", SENSOR_MV.getStackForm(), "P G", "PR ", "XPP", 'P',
                new UnificationEntry(plate, Aluminium), 'R', new UnificationEntry(stick, Electrum), 'G',
                new UnificationEntry(gemFlawless, Emerald), 'X', new UnificationEntry(circuit, Tier.MV));
        ModHandler.addShapedRecipe(true, "sensor_hv", SENSOR_HV.getStackForm(), "P G", "PR ", "XPP", 'P',
                new UnificationEntry(plate, StainlessSteel), 'R', new UnificationEntry(stick, Chrome), 'G',
                new UnificationEntry(gem, EnderEye), 'X', new UnificationEntry(circuit, Tier.HV));
        ModHandler.addShapedRecipe(true, "sensor_ev", SENSOR_EV.getStackForm(), "P G", "PR ", "XPP", 'P',
                new UnificationEntry(plate, Titanium), 'R', new UnificationEntry(stick, Platinum), 'G',
                QUANTUM_EYE.getStackForm(), 'X', new UnificationEntry(circuit, Tier.EV));
        ModHandler.addShapedRecipe(true, "sensor_iv", SENSOR_IV.getStackForm(), "P G", "PR ", "XPP", 'P',
                new UnificationEntry(plate, TungstenSteel), 'R', new UnificationEntry(stick, Iridium), 'G',
                QUANTUM_STAR.getStackForm(), 'X', new UnificationEntry(circuit, Tier.IV));

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Brass)
                .inputItem(plate, Steel, 4)
                .inputItem(circuit, Tier.LV)
                .inputItem(gem, Quartzite)
                .outputs(SENSOR_LV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Electrum)
                .inputItem(plate, Aluminium, 4)
                .inputItem(circuit, Tier.MV)
                .inputItem(gemFlawless, Emerald)
                .outputs(SENSOR_MV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Chrome)
                .inputItem(plate, StainlessSteel, 4)
                .inputItem(circuit, Tier.HV)
                .inputItem(gem, EnderEye)
                .outputs(SENSOR_HV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Platinum)
                .inputItem(plate, Titanium, 4)
                .inputItem(circuit, Tier.EV)
                .inputItem(QUANTUM_EYE)
                .outputs(SENSOR_EV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Iridium)
                .inputItem(plate, TungstenSteel, 4)
                .inputItem(circuit, Tier.IV)
                .inputItem(QUANTUM_STAR)
                .outputs(SENSOR_IV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(frameGt, HSSS)
                .inputItem(ELECTRIC_MOTOR_LuV)
                .inputItem(plate, Ruridit, 4)
                .inputItem(QUANTUM_STAR)
                .inputItem(circuit, Tier.LuV, 2)
                .inputItem(foil, Palladium, 64)
                .inputItem(foil, Palladium, 32)
                .inputItem(cableGtSingle, NiobiumTitanium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .outputItem(SENSOR_LuV)
                .scannerResearch(b -> b
                        .researchStack(SENSOR_IV.getStackForm())
                        .duration(2400))
                .duration(600).volts(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(frameGt, NaquadahAlloy)
                .inputItem(ELECTRIC_MOTOR_ZPM)
                .inputItem(plate, Osmiridium, 4)
                .inputItem(QUANTUM_STAR, 2)
                .inputItem(circuit, Tier.ZPM, 2)
                .inputItem(foil, Trinium, 64)
                .inputItem(foil, Trinium, 32)
                .inputItem(cableGtSingle, VanadiumGallium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .outputItem(SENSOR_ZPM)
                .stationResearch(b -> b
                        .researchStack(SENSOR_LuV.getStackForm())
                        .CWUt(4))
                .duration(600).volts(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(frameGt, Tritanium)
                .inputItem(ELECTRIC_MOTOR_UV)
                .inputItem(plate, Tritanium, 4)
                .inputItem(GRAVI_STAR)
                .inputItem(circuit, Tier.UV, 2)
                .inputItem(foil, Naquadria, 64)
                .inputItem(foil, Naquadria, 32)
                .inputItem(cableGtSingle, YttriumBariumCuprate, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputItem(SENSOR_UV)
                .stationResearch(b -> b
                        .researchStack(SENSOR_ZPM.getStackForm())
                        .CWUt(48)
                        .EUt(VA[ZPM]))
                .duration(600).volts(100000).buildAndRegister();

        // Emitters
        // Start------------------------------------------------------------------------------------------------
        ModHandler.addShapedRecipe(true, "emitter_lv", EMITTER_LV.getStackForm(), "CRX", "RGR", "XRC", 'R',
                new UnificationEntry(stick, Brass), 'C', new UnificationEntry(cableGtSingle, Tin), 'G',
                new UnificationEntry(gem, Quartzite), 'X', new UnificationEntry(circuit, Tier.LV));
        ModHandler.addShapedRecipe(true, "emitter_mv", EMITTER_MV.getStackForm(), "CRX", "RGR", "XRC", 'R',
                new UnificationEntry(stick, Electrum), 'C', new UnificationEntry(cableGtSingle, Copper), 'G',
                new UnificationEntry(gemFlawless, Emerald), 'X', new UnificationEntry(circuit, Tier.MV));
        ModHandler.addShapedRecipe(true, "emitter_hv", EMITTER_HV.getStackForm(), "CRX", "RGR", "XRC", 'R',
                new UnificationEntry(stick, Chrome), 'C', new UnificationEntry(cableGtSingle, Gold), 'G',
                new UnificationEntry(gem, EnderEye), 'X', new UnificationEntry(circuit, Tier.HV));
        ModHandler.addShapedRecipe(true, "emitter_ev", EMITTER_EV.getStackForm(), "CRX", "RGR", "XRC", 'R',
                new UnificationEntry(stick, Platinum), 'C', new UnificationEntry(cableGtSingle, Aluminium), 'G',
                QUANTUM_EYE.getStackForm(), 'X', new UnificationEntry(circuit, Tier.EV));
        ModHandler.addShapedRecipe(true, "emitter_iv", EMITTER_IV.getStackForm(), "CRX", "RGR", "XRC", 'R',
                new UnificationEntry(stick, Iridium), 'C', new UnificationEntry(cableGtSingle, Tungsten), 'G',
                QUANTUM_STAR.getStackForm(), 'X', new UnificationEntry(circuit, Tier.IV));

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Brass, 4)
                .inputItem(cableGtSingle, Tin, 2)
                .inputItem(circuit, Tier.LV, 2)
                .inputItem(gem, Quartzite)
                .circuitMeta(1)
                .outputs(EMITTER_LV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Electrum, 4)
                .inputItem(cableGtSingle, Copper, 2)
                .inputItem(circuit, Tier.MV, 2)
                .inputItem(gemFlawless, Emerald)
                .circuitMeta(1)
                .outputs(EMITTER_MV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Chrome, 4)
                .inputItem(cableGtSingle, Gold, 2)
                .inputItem(circuit, Tier.HV, 2)
                .inputItem(gem, EnderEye)
                .circuitMeta(1)
                .outputs(EMITTER_HV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Platinum, 4)
                .inputItem(cableGtSingle, Aluminium, 2)
                .inputItem(circuit, Tier.EV, 2)
                .inputItem(QUANTUM_EYE)
                .circuitMeta(1)
                .outputs(EMITTER_EV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLER_RECIPES.recipeBuilder()
                .inputItem(stick, Iridium, 4)
                .inputItem(cableGtSingle, Tungsten, 2)
                .inputItem(circuit, Tier.IV, 2)
                .inputItem(QUANTUM_STAR)
                .circuitMeta(1)
                .outputs(EMITTER_IV.getStackForm())
                .duration(100).volts(VA[LV]).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(frameGt, HSSS)
                .inputItem(ELECTRIC_MOTOR_LuV)
                .inputItem(stickLong, Ruridit, 4)
                .inputItem(QUANTUM_STAR)
                .inputItem(circuit, Tier.LuV, 2)
                .inputItem(foil, Palladium, 64)
                .inputItem(foil, Palladium, 32)
                .inputItem(cableGtSingle, NiobiumTitanium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 2))
                .outputItem(EMITTER_LuV)
                .scannerResearch(b -> b
                        .researchStack(EMITTER_IV.getStackForm())
                        .duration(2400))
                .duration(600).volts(6000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(frameGt, NaquadahAlloy)
                .inputItem(ELECTRIC_MOTOR_ZPM)
                .inputItem(stickLong, Osmiridium, 4)
                .inputItem(QUANTUM_STAR, 2)
                .inputItem(circuit, Tier.ZPM, 2)
                .inputItem(foil, Trinium, 64)
                .inputItem(foil, Trinium, 32)
                .inputItem(cableGtSingle, VanadiumGallium, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 4))
                .outputItem(EMITTER_ZPM)
                .stationResearch(b -> b
                        .researchStack(EMITTER_LuV.getStackForm())
                        .CWUt(8))
                .duration(600).volts(24000).buildAndRegister();

        ASSEMBLY_LINE_RECIPES.recipeBuilder()
                .inputItem(frameGt, Tritanium)
                .inputItem(ELECTRIC_MOTOR_UV)
                .inputItem(stickLong, Tritanium, 4)
                .inputItem(GRAVI_STAR)
                .inputItem(circuit, Tier.UV, 2)
                .inputItem(foil, Naquadria, 64)
                .inputItem(foil, Naquadria, 32)
                .inputItem(cableGtSingle, YttriumBariumCuprate, 4)
                .fluidInputs(SolderingAlloy.getFluid(L * 8))
                .fluidInputs(Naquadria.getFluid(L * 4))
                .outputItem(EMITTER_UV)
                .stationResearch(b -> b
                        .researchStack(EMITTER_ZPM.getStackForm())
                        .CWUt(48)
                        .EUt(VA[ZPM]))
                .duration(600).volts(100000).buildAndRegister();
    }
}
