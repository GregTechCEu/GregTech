package gregtech.common.metatileentities;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.render.OrientedOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTLog;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.electric.*;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityEnergyHatch;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityFluidHatch;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityItemBus;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import gregtech.common.metatileentities.multi.MetaTileEntityCokeOven;
import gregtech.common.metatileentities.multi.MetaTileEntityCokeOvenHatch;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler.BoilerType;
import gregtech.common.metatileentities.multi.MetaTileEntityPrimitiveBlastFurnace;
import gregtech.common.metatileentities.multi.electric.*;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeCombustionEngine;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine.TurbineType;
import gregtech.common.metatileentities.steam.*;
import gregtech.common.metatileentities.steam.boiler.SteamCoalBoiler;
import gregtech.common.metatileentities.steam.boiler.SteamLavaBoiler;
import gregtech.common.metatileentities.steam.boiler.SteamSolarBoiler;
import gregtech.common.metatileentities.storage.*;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntities {

    //HULLS
    public static MetaTileEntityHull[] HULL = new MetaTileEntityHull[GTValues.V.length];
    public static MetaTileEntityTransformer[] TRANSFORMER = new MetaTileEntityTransformer[GTValues.V.length - 2]; // no ULV, no MAX
    public static MetaTileEntityBatteryBuffer[][] BATTERY_BUFFER = new MetaTileEntityBatteryBuffer[GTValues.V.length][];
    public static MetaTileEntityCharger[] CHARGER = new MetaTileEntityCharger[GTValues.V.length];

    //STEAM AGE SECTION
    public static SteamCoalBoiler STEAM_BOILER_COAL_BRONZE;
    public static SteamCoalBoiler STEAM_BOILER_COAL_STEEL;
    public static SteamSolarBoiler STEAM_BOILER_SOLAR_BRONZE;
    public static SteamLavaBoiler STEAM_BOILER_LAVA_BRONZE;
    public static SteamLavaBoiler STEAM_BOILER_LAVA_STEEL;
    public static SteamExtractor STEAM_EXTRACTOR_BRONZE;
    public static SteamExtractor STEAM_EXTRACTOR_STEEL;
    public static SteamMacerator STEAM_MACERATOR_BRONZE;
    public static SteamMacerator STEAM_MACERATOR_STEEL;
    public static SteamCompressor STEAM_COMPRESSOR_BRONZE;
    public static SteamCompressor STEAM_COMPRESSOR_STEEL;
    public static SteamHammer STEAM_HAMMER_BRONZE;
    public static SteamHammer STEAM_HAMMER_STEEL;
    public static SteamFurnace STEAM_FURNACE_BRONZE;
    public static SteamFurnace STEAM_FURNACE_STEEL;
    public static SteamAlloySmelter STEAM_ALLOY_SMELTER_BRONZE;
    public static SteamAlloySmelter STEAM_ALLOY_SMELTER_STEEL;

    //public static MetaTileEntityPumpHatch PUMP_OUTPUT_HATCH;
    //public static MetaTileEntityWaterPump PRIMITIVE_WATER_PUMP;

    //SIMPLE MACHINES SECTION
    public static SimpleMachineMetaTileEntity[] ELECTRIC_FURNACE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static MetaTileEntityMacerator[] MACERATOR = new MetaTileEntityMacerator[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] ALLOY_SMELTER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] AMPLIFABRICATOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] ARC_FURNACE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] ASSEMBLER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] AUTOCLAVE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] BENDER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] BREWERY = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] CANNER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] CENTRIFUGE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] CHEMICAL_BATH = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] CHEMICAL_REACTOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] COMPRESSOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] CUTTER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] DISTILLERY = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] ELECTROLYZER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] ELECTROMAGNETIC_SEPARATOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] EXTRACTOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] EXTRUDER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] FERMENTER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] FLUID_CANNER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] FLUID_EXTRACTOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] FLUID_HEATER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] FLUID_SOLIDIFIER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] FORGE_HAMMER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] FORMING_PRESS = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] LATHE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] MICROWAVE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] MIXER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] ORE_WASHER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] PACKER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] UNPACKER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] PLASMA_ARC_FURNACE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] POLARIZER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] LASER_ENGRAVER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] SIFTER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] THERMAL_CENTRIFUGE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] WIREMILL = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static SimpleMachineMetaTileEntity[] CLUSTER_MILL = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];

    //GENERATORS SECTION
    public static SimpleGeneratorMetaTileEntity[] COMBUSTION_GENERATOR = new SimpleGeneratorMetaTileEntity[4];
    public static SimpleGeneratorMetaTileEntity[] STEAM_TURBINE = new SimpleGeneratorMetaTileEntity[4];
    public static SimpleGeneratorMetaTileEntity[] GAS_TURBINE = new SimpleGeneratorMetaTileEntity[4];
    public static MetaTileEntityMagicEnergyAbsorber MAGIC_ENERGY_ABSORBER;

    //MULTIBLOCK PARTS SECTION
    public static MetaTileEntityItemBus[] ITEM_IMPORT_BUS = new MetaTileEntityItemBus[GTValues.UHV + 1]; // ULV-UHV
    public static MetaTileEntityItemBus[] ITEM_EXPORT_BUS = new MetaTileEntityItemBus[GTValues.UHV + 1];
    public static MetaTileEntityFluidHatch[] FLUID_IMPORT_HATCH = new MetaTileEntityFluidHatch[GTValues.UHV + 1];
    public static MetaTileEntityFluidHatch[] FLUID_EXPORT_HATCH = new MetaTileEntityFluidHatch[GTValues.UHV + 1];
    public static MetaTileEntityEnergyHatch[] ENERGY_INPUT_HATCH = new MetaTileEntityEnergyHatch[GTValues.V.length];
    public static MetaTileEntityEnergyHatch[] ENERGY_OUTPUT_HATCH = new MetaTileEntityEnergyHatch[GTValues.V.length];
    public static MetaTileEntityRotorHolder[] ROTOR_HOLDER = new MetaTileEntityRotorHolder[3]; //HV, LuV, MAX
    public static MetaTileEntityCokeOvenHatch COKE_OVEN_HATCH;

    //MULTIBLOCKS SECTION
    public static MetaTileEntityPrimitiveBlastFurnace PRIMITIVE_BLAST_FURNACE;
    public static MetaTileEntityCokeOven COKE_OVEN;
    public static MetaTileEntityElectricBlastFurnace ELECTRIC_BLAST_FURNACE;
    public static MetaTileEntityVacuumFreezer VACUUM_FREEZER;
    public static MetaTileEntityImplosionCompressor IMPLOSION_COMPRESSOR;
    public static MetaTileEntityPyrolyseOven PYROLYSE_OVEN;
    public static MetaTileEntityDistillationTower DISTILLATION_TOWER;
    public static MetaTileEntityCrackingUnit CRACKER;
    public static MetaTileEntityMultiFurnace MULTI_FURNACE;
    public static MetaTileEntityLargeCombustionEngine LARGE_COMBUSTION_ENGINE;

    public static MetaTileEntityLargeTurbine LARGE_STEAM_TURBINE;
    public static MetaTileEntityLargeTurbine LARGE_GAS_TURBINE;
    public static MetaTileEntityLargeTurbine LARGE_PLASMA_TURBINE;

    public static MetaTileEntityLargeBoiler LARGE_BRONZE_BOILER;
    public static MetaTileEntityLargeBoiler LARGE_STEEL_BOILER;
    public static MetaTileEntityLargeBoiler LARGE_TITANIUM_BOILER;
    public static MetaTileEntityLargeBoiler LARGE_TUNGSTENSTEEL_BOILER;

    public static MetaTileEntityAssemblyLine ASSEMBLY_LINE;
    public static MetaTileEntityFusionReactor[] FUSION_REACTOR = new MetaTileEntityFusionReactor[3];

    public static MetaTileEntityLargeChemicalReactor LARGE_CHEMICAL_REACTOR;

    //STORAGE SECTION
    public static MetaTileEntityChest SMALL_WOODEN_CHEST;
    public static MetaTileEntityChest WOODEN_CHEST;
    public static MetaTileEntityChest BRONZE_CHEST;
    public static MetaTileEntityChest STEEL_CHEST;
    public static MetaTileEntityChest STAINLESS_STEEL_CHEST;
    public static MetaTileEntityChest TITANIUM_CHEST;
    public static MetaTileEntityChest TUNGSTENSTEEL_CHEST;
    public static MetaTileEntityLockedSafe LOCKED_SAFE;

    public static MetaTileEntityTank WOODEN_TANK;
    public static MetaTileEntityTank BRONZE_TANK;
    public static MetaTileEntityTank STEEL_TANK;
    public static MetaTileEntityTank STAINLESS_STEEL_TANK;
    public static MetaTileEntityTank TITANIUM_TANK;
    public static MetaTileEntityTank TUNGSTENSTEEL_TANK;

    public static MetaTileEntityDrum WOODEN_DRUM;
    public static MetaTileEntityDrum BRONZE_DRUM;
    public static MetaTileEntityDrum STEEL_DRUM;
    public static MetaTileEntityDrum STAINLESS_STEEL_DRUM;
    public static MetaTileEntityDrum TITANIUM_DRUM;
    public static MetaTileEntityDrum TUNGSTENSTEEL_DRUM;

    public static MetaTileEntityCrate WOODEN_CRATE;
    public static MetaTileEntityCrate BRONZE_CRATE;
    public static MetaTileEntityCrate STEEL_CRATE;
    public static MetaTileEntityCrate STAINLESS_STEEL_CRATE;
    public static MetaTileEntityCrate TITANIUM_CRATE;
    public static MetaTileEntityCrate TUNGSTENSTEEL_CRATE;


    //MISC MACHINES SECTION
    public static MetaTileEntityWorkbench WORKBENCH;
    public static MetaTileEntityPump[] PUMP = new MetaTileEntityPump[8];
    public static MetaTileEntityBlockBreaker[] BLOCK_BREAKER = new MetaTileEntityBlockBreaker[4];
    public static MetaTileEntityAirCollector[] AIR_COLLECTOR = new MetaTileEntityAirCollector[6];
    public static MetaTileEntityItemCollector[] ITEM_COLLECTOR = new MetaTileEntityItemCollector[4];
    public static MetaTileEntityTeslaCoil TESLA_COIL;
    public static MetaTileEntityQuantumChest[] QUANTUM_CHEST = new MetaTileEntityQuantumChest[4];
    public static MetaTileEntityQuantumTank[] QUANTUM_TANK = new MetaTileEntityQuantumTank[4];
    public static MetaTileEntityFisher[] FISHER = new MetaTileEntityFisher[4];

    public static void init() {
        GTLog.logger.info("Registering MetaTileEntities");

        STEAM_BOILER_COAL_BRONZE = GregTechAPI.registerMetaTileEntity(1, new SteamCoalBoiler(gregtechId("steam_boiler_coal_bronze"), false));
        STEAM_BOILER_COAL_STEEL = GregTechAPI.registerMetaTileEntity(2, new SteamCoalBoiler(gregtechId("steam_boiler_coal_steel"), true));

        STEAM_BOILER_SOLAR_BRONZE = GregTechAPI.registerMetaTileEntity(3, new SteamSolarBoiler(gregtechId("steam_boiler_solar_bronze"), false));

        STEAM_BOILER_LAVA_BRONZE = GregTechAPI.registerMetaTileEntity(5, new SteamLavaBoiler(gregtechId("steam_boiler_lava_bronze"), false));
        STEAM_BOILER_LAVA_STEEL = GregTechAPI.registerMetaTileEntity(6, new SteamLavaBoiler(gregtechId("steam_boiler_lava_steel"), true));

        STEAM_EXTRACTOR_BRONZE = GregTechAPI.registerMetaTileEntity(7, new SteamExtractor(gregtechId("steam_extractor_bronze"), false));
        STEAM_EXTRACTOR_STEEL = GregTechAPI.registerMetaTileEntity(8, new SteamExtractor(gregtechId("steam_extractor_steel"), true));

        STEAM_MACERATOR_BRONZE = GregTechAPI.registerMetaTileEntity(9, new SteamMacerator(gregtechId("steam_macerator_bronze"), false));
        STEAM_MACERATOR_STEEL = GregTechAPI.registerMetaTileEntity(10, new SteamMacerator(gregtechId("steam_macerator_steel"), true));

        STEAM_COMPRESSOR_BRONZE = GregTechAPI.registerMetaTileEntity(11, new SteamCompressor(gregtechId("steam_compressor_bronze"), false));
        STEAM_COMPRESSOR_STEEL = GregTechAPI.registerMetaTileEntity(12, new SteamCompressor(gregtechId("steam_compressor_steel"), true));

        STEAM_HAMMER_BRONZE = GregTechAPI.registerMetaTileEntity(13, new SteamHammer(gregtechId("steam_hammer_bronze"), false));
        STEAM_HAMMER_STEEL = GregTechAPI.registerMetaTileEntity(14, new SteamHammer(gregtechId("steam_hammer_steel"), true));

        STEAM_FURNACE_BRONZE = GregTechAPI.registerMetaTileEntity(15, new SteamFurnace(gregtechId("steam_furnace_bronze"), false));
        STEAM_FURNACE_STEEL = GregTechAPI.registerMetaTileEntity(16, new SteamFurnace(gregtechId("steam_furnace_steel"), true));

        STEAM_ALLOY_SMELTER_BRONZE = GregTechAPI.registerMetaTileEntity(17, new SteamAlloySmelter(gregtechId("steam_alloy_smelter_bronze"), false));
        STEAM_ALLOY_SMELTER_STEEL = GregTechAPI.registerMetaTileEntity(18, new SteamAlloySmelter(gregtechId("steam_alloy_smelter_steel"), true));

        // Electric Furnace, IDs 50-64
        registerSimpleMetaTileEntity(ELECTRIC_FURNACE, 50, "electric_furnace", RecipeMaps.FURNACE_RECIPES, Textures.ELECTRIC_FURNACE_OVERLAY,
                ConfigHolder.U.machines.midTierElectricFurnace, ConfigHolder.U.machines.highTierElectricFurnace);

        // Macerator, IDs 65-79
        MACERATOR[0] = GregTechAPI.registerMetaTileEntity(65, new MetaTileEntityMacerator(gregtechId("macerator.lv"), RecipeMaps.MACERATOR_RECIPES, 1, Textures.MACERATOR_OVERLAY, 1));
        MACERATOR[1] = GregTechAPI.registerMetaTileEntity(66, new MetaTileEntityMacerator(gregtechId("macerator.mv"), RecipeMaps.MACERATOR_RECIPES, 1, Textures.MACERATOR_OVERLAY, 2));
        MACERATOR[2] = GregTechAPI.registerMetaTileEntity(67, new MetaTileEntityMacerator(gregtechId("macerator.hv"), RecipeMaps.MACERATOR_RECIPES, 3, Textures.MACERATOR_OVERLAY, 3));
        MACERATOR[3] = GregTechAPI.registerMetaTileEntity(68, new MetaTileEntityMacerator(gregtechId("macerator.ev"), RecipeMaps.MACERATOR_RECIPES, 3, Textures.MACERATOR_OVERLAY, 4));
        MACERATOR[4] = GregTechAPI.registerMetaTileEntity(69, new MetaTileEntityMacerator(gregtechId("macerator.iv"), RecipeMaps.MACERATOR_RECIPES, 3, Textures.MACERATOR_OVERLAY, 5));
        if (ConfigHolder.U.machines.midTierMachines || ConfigHolder.U.machines.midTierMacerators) {
            MACERATOR[5] = GregTechAPI.registerMetaTileEntity(70, new MetaTileEntityMacerator(gregtechId("macerator.luv"), RecipeMaps.MACERATOR_RECIPES, 3, Textures.MACERATOR_OVERLAY, 6));
            MACERATOR[6] = GregTechAPI.registerMetaTileEntity(71, new MetaTileEntityMacerator(gregtechId("macerator.zpm"), RecipeMaps.MACERATOR_RECIPES, 3, Textures.MACERATOR_OVERLAY, 7));
            MACERATOR[7] = GregTechAPI.registerMetaTileEntity(72, new MetaTileEntityMacerator(gregtechId("macerator.uv"), RecipeMaps.MACERATOR_RECIPES, 3, Textures.MACERATOR_OVERLAY, 8));
        }
        if (ConfigHolder.U.machines.highTierMachines || ConfigHolder.U.machines.highTierMacerators) {
            MACERATOR[8] = GregTechAPI.registerMetaTileEntity(73, new MetaTileEntityMacerator(gregtechId("macerator.uhv"), RecipeMaps.MACERATOR_RECIPES, 3, Textures.MACERATOR_OVERLAY, 9));
            MACERATOR[9] = GregTechAPI.registerMetaTileEntity(74, new MetaTileEntityMacerator(gregtechId("macerator.uev"), RecipeMaps.MACERATOR_RECIPES, 3, Textures.MACERATOR_OVERLAY, 10));
            MACERATOR[10] = GregTechAPI.registerMetaTileEntity(75, new MetaTileEntityMacerator(gregtechId("macerator.uiv"), RecipeMaps.MACERATOR_RECIPES, 3, Textures.MACERATOR_OVERLAY, 11));
            MACERATOR[11] = GregTechAPI.registerMetaTileEntity(76, new MetaTileEntityMacerator(gregtechId("macerator.umv"), RecipeMaps.MACERATOR_RECIPES, 3, Textures.MACERATOR_OVERLAY, 12));
            MACERATOR[12] = GregTechAPI.registerMetaTileEntity(77, new MetaTileEntityMacerator(gregtechId("macerator.uxv"), RecipeMaps.MACERATOR_RECIPES, 3, Textures.MACERATOR_OVERLAY, 13));
        }

        // Alloy Smelter, IDs 80-94
        registerSimpleMetaTileEntity(ALLOY_SMELTER, 80, "alloy_smelter", RecipeMaps.ALLOY_SMELTER_RECIPES, Textures.ALLOY_SMELTER_OVERLAY,
                ConfigHolder.U.machines.midTierAlloySmelter, ConfigHolder.U.machines.highTierAlloySmelter);

        // Amplifabricator, IDs 95-109
        // Can add higher tier machines if desired, space is left for it
        registerSimpleMetaTileEntity(AMPLIFABRICATOR, 95, "amplifab", RecipeMaps.AMPLIFIERS, Textures.AMPLIFAB_OVERLAY, false, false);

        // Arc Furnace, IDs 110-124
        registerSimpleMetaTileEntity(ARC_FURNACE, 110, "arc_furnace", RecipeMaps.ARC_FURNACE_RECIPES, Textures.ARC_FURNACE_OVERLAY,
                ConfigHolder.U.machines.midTierArcFurnaces, ConfigHolder.U.machines.highTierArcFurnaces, false);

        // Assembler, IDs 125-139
        registerSimpleMetaTileEntity(ASSEMBLER, 125, "assembler", RecipeMaps.ASSEMBLER_RECIPES, Textures.ASSEMBLER_OVERLAY,
                ConfigHolder.U.machines.midTierAssemblers, ConfigHolder.U.machines.highTierAssemblers);

        // Autoclave, IDs 140-154
        registerSimpleMetaTileEntity(AUTOCLAVE, 140, "autoclave", RecipeMaps.AUTOCLAVE_RECIPES, Textures.AUTOCLAVE_OVERLAY,
                ConfigHolder.U.machines.midTierAutoclaves, ConfigHolder.U.machines.highTierAutoclaves, false);

        // Bender, IDs 155-169
        registerSimpleMetaTileEntity(BENDER, 155, "bender", RecipeMaps.BENDER_RECIPES, Textures.BENDER_OVERLAY,
                ConfigHolder.U.machines.midTierBenders, ConfigHolder.U.machines.highTierBenders);

        // Brewery, IDs 170-184
        registerSimpleMetaTileEntity(BREWERY, 170, "brewery", RecipeMaps.BREWING_RECIPES, Textures.BREWERY_OVERLAY,
                ConfigHolder.U.machines.midTierBreweries, ConfigHolder.U.machines.highTierBreweries);

        // Canner, IDs 185-199
        registerSimpleMetaTileEntity(CANNER, 185, "canner", RecipeMaps.CANNER_RECIPES, Textures.CANNER_OVERLAY,
                ConfigHolder.U.machines.midTierCanners, ConfigHolder.U.machines.highTierCanners);

        // Centrifuge, IDs 200-214
        registerSimpleMetaTileEntity(CENTRIFUGE, 200, "centrifuge", RecipeMaps.CENTRIFUGE_RECIPES, Textures.CENTRIFUGE_OVERLAY,
                ConfigHolder.U.machines.midTierCentrifuges, ConfigHolder.U.machines.highTierCentrifuges, false);

        // Chemical Bath, IDs 215-229
        registerSimpleMetaTileEntity(CHEMICAL_BATH, 215, "chemical_bath", RecipeMaps.CHEMICAL_BATH_RECIPES, Textures.CHEMICAL_BATH_OVERLAY,
                ConfigHolder.U.machines.midTierChemicalBaths, ConfigHolder.U.machines.highTierChemicalBaths);

        // Chemical Reactor, IDs 230-244
        registerSimpleMetaTileEntity(CHEMICAL_REACTOR, 230, "chemical_reactor", RecipeMaps.CHEMICAL_RECIPES, Textures.CHEMICAL_REACTOR_OVERLAY,
                ConfigHolder.U.machines.midTierChemicalReactors, ConfigHolder.U.machines.highTierChemicalReactors);

        // Compressor, IDs 245-259
        registerSimpleMetaTileEntity(COMPRESSOR, 245, "compressor", RecipeMaps.COMPRESSOR_RECIPES, Textures.COMPRESSOR_OVERLAY,
                ConfigHolder.U.machines.midTierCompressors, ConfigHolder.U.machines.highTierCompressors);

        // Cutter, IDs 260-274
        registerSimpleMetaTileEntity(CUTTER, 260, "cutter", RecipeMaps.CUTTER_RECIPES, Textures.CUTTER_OVERLAY,
                ConfigHolder.U.machines.midTierCutters, ConfigHolder.U.machines.highTierCutters);

        // Distillery, IDs 275-289
        registerSimpleMetaTileEntity(DISTILLERY, 275, "distillery", RecipeMaps.DISTILLERY_RECIPES, Textures.DISTILLERY_OVERLAY,
                ConfigHolder.U.machines.midTierDistilleries, ConfigHolder.U.machines.highTierDistilleries);

        // Electrolyzer, IDs 290-304
        registerSimpleMetaTileEntity(ELECTROLYZER, 290, "electrolyzer", RecipeMaps.ELECTROLYZER_RECIPES, Textures.ELECTROLYZER_OVERLAY,
                ConfigHolder.U.machines.midTierElectrolyzers, ConfigHolder.U.machines.highTierElectrolyzers, false);

        // Electromagnetic Separator, IDs 305-319
        registerSimpleMetaTileEntity(ELECTROMAGNETIC_SEPARATOR, 305, "electromagnetic_separator", RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES, Textures.ELECTROMAGNETIC_SEPARATOR_OVERLAY,
                ConfigHolder.U.machines.midTierElectromagneticSeparators, ConfigHolder.U.machines.highTierElectromagneticSeparators);

        // Extractor, IDs 320-334
        registerSimpleMetaTileEntity(EXTRACTOR, 320, "extractor", RecipeMaps.EXTRACTOR_RECIPES, Textures.EXTRACTOR_OVERLAY,
                ConfigHolder.U.machines.midTierExtractors, ConfigHolder.U.machines.highTierExtractors);

        // Extruder, IDs 335-349
        registerSimpleMetaTileEntity(EXTRUDER, 335, "extruder", RecipeMaps.EXTRUDER_RECIPES, Textures.EXTRUDER_OVERLAY,
                ConfigHolder.U.machines.midTierExtruders, ConfigHolder.U.machines.highTierExtruders);

        // Fermenter, IDs 350-364
        registerSimpleMetaTileEntity(FERMENTER, 350, "fermenter", RecipeMaps.FERMENTING_RECIPES, Textures.FERMENTER_OVERLAY,
                ConfigHolder.U.machines.midTierFermenters, ConfigHolder.U.machines.highTierFermenters);

        // Fluid Canner, IDs 365-379
        registerSimpleMetaTileEntity(FLUID_CANNER, 365, "fluid_canner", RecipeMaps.FLUID_CANNER_RECIPES, Textures.FLUID_CANNER_OVERLAY,
                ConfigHolder.U.machines.midTierFluidCanners, ConfigHolder.U.machines.highTierFluidCanners);

        // Fluid Extractor, IDs 380-394
        registerSimpleMetaTileEntity(FLUID_EXTRACTOR, 380, "fluid_extractor", RecipeMaps.FLUID_EXTRACTION_RECIPES, Textures.FLUID_EXTRACTOR_OVERLAY,
                ConfigHolder.U.machines.midTierFluidExtractors, ConfigHolder.U.machines.highTierFluidExtractors);

        // Fluid Heater, IDs 395-409
        registerSimpleMetaTileEntity(FLUID_HEATER, 395, "fluid_heater", RecipeMaps.FLUID_HEATER_RECIPES, Textures.FLUID_HEATER_OVERLAY,
                ConfigHolder.U.machines.midTierFluidHeaters, ConfigHolder.U.machines.highTierFluidHeaters);

        // Fluid Solidifier, IDs 410-424
        registerSimpleMetaTileEntity(FLUID_SOLIDIFIER, 410, "fluid_solidifier", RecipeMaps.FLUID_SOLIDFICATION_RECIPES, Textures.FLUID_SOLIDIFIER_OVERLAY,
                ConfigHolder.U.machines.midTierFluidSolidifiers, ConfigHolder.U.machines.highTierFluidSolidifiers);

        // Forge Hammer, IDs 425-439
        registerSimpleMetaTileEntity(FORGE_HAMMER, 425, "forge_hammer", RecipeMaps.FORGE_HAMMER_RECIPES, Textures.FORGE_HAMMER_OVERLAY,
                ConfigHolder.U.machines.midTierForgeHammers, ConfigHolder.U.machines.highTierForgeHammers);

        // Forming Press, IDs 440-454
        registerSimpleMetaTileEntity(FORMING_PRESS, 440, "forming_press", RecipeMaps.FORMING_PRESS_RECIPES, Textures.FORMING_PRESS_OVERLAY,
                ConfigHolder.U.machines.midTierFormingPresses, ConfigHolder.U.machines.highTierFormingPresses);

        // Lathe, IDs 455-469
        registerSimpleMetaTileEntity(LATHE, 455, "lathe", RecipeMaps.LATHE_RECIPES, Textures.LATHE_OVERLAY,
                ConfigHolder.U.machines.midTierLathes, ConfigHolder.U.machines.highTierLathes);

        // Microwave, IDs 470-484
        registerSimpleMetaTileEntity(MICROWAVE, 470, "microwave", RecipeMaps.MICROWAVE_RECIPES, Textures.MICROWAVE_OVERLAY,
                ConfigHolder.U.machines.midTierMicrowaves, ConfigHolder.U.machines.highTierMicrowaves);

        // Mixer, IDs 485-499
        registerSimpleMetaTileEntity(MIXER, 485, "mixer", RecipeMaps.MIXER_RECIPES, Textures.MIXER_OVERLAY,
                ConfigHolder.U.machines.midTierMixers, ConfigHolder.U.machines.highTierMixers, false);

        // Ore Washer, IDs 500-514
        registerSimpleMetaTileEntity(ORE_WASHER, 500, "ore_washer", RecipeMaps.ORE_WASHER_RECIPES, Textures.ORE_WASHER_OVERLAY,
                ConfigHolder.U.machines.midTierOreWashers, ConfigHolder.U.machines.highTierOreWashers);

        // Packer, IDs 515-529
        registerSimpleMetaTileEntity(PACKER, 515, "packer", RecipeMaps.PACKER_RECIPES, Textures.PACKER_OVERLAY,
                ConfigHolder.U.machines.midTierPackers, ConfigHolder.U.machines.highTierPackers);

        // Unpacker, IDs 530-544
        registerSimpleMetaTileEntity(UNPACKER, 530, "unpacker", RecipeMaps.UNPACKER_RECIPES, Textures.UNPACKER_OVERLAY,
                ConfigHolder.U.machines.midTierUnpackers, ConfigHolder.U.machines.highTierUnpackers);

        // Plasma Arc Furnace, IDs 545-559
        registerSimpleMetaTileEntity(PLASMA_ARC_FURNACE, 545, "plasma_arc_furnace", RecipeMaps.PLASMA_ARC_FURNACE_RECIPES, Textures.PLASMA_ARC_FURNACE_OVERLAY,
                ConfigHolder.U.machines.midTierPlasmaArcFurnaces, ConfigHolder.U.machines.highTierPlasmaArcFurnaces, false);

        // Polarizer, IDs 560-574
        registerSimpleMetaTileEntity(POLARIZER, 560, "polarizer", RecipeMaps.POLARIZER_RECIPES, Textures.POLARIZER_OVERLAY,
                ConfigHolder.U.machines.midTierPolarizers, ConfigHolder.U.machines.highTierPolarizers);

        // Laser Engraver, IDs 575-589
        registerSimpleMetaTileEntity(LASER_ENGRAVER, 575, "laser_engraver", RecipeMaps.LASER_ENGRAVER_RECIPES, Textures.LASER_ENGRAVER_OVERLAY,
                ConfigHolder.U.machines.midTierLaserEngravers, ConfigHolder.U.machines.highTierLaserEngravers);

        // Sifter, IDs 590-604
        registerSimpleMetaTileEntity(SIFTER, 590, "sifter", RecipeMaps.SIFTER_RECIPES, Textures.SIFTER_OVERLAY,
                ConfigHolder.U.machines.midTierSifters, ConfigHolder.U.machines.highTierSifters);

        // Thermal Centrifuge, IDs 605-619
        registerSimpleMetaTileEntity(THERMAL_CENTRIFUGE, 605, "thermal_centrifuge", RecipeMaps.THERMAL_CENTRIFUGE_RECIPES, Textures.THERMAL_CENTRIFUGE_OVERLAY,
                ConfigHolder.U.machines.midTierThermalCentrifuges, ConfigHolder.U.machines.highTierThermalCentrifuges);

        // Wire Mill, IDs 620-634
        registerSimpleMetaTileEntity(WIREMILL, 620, "wiremill", RecipeMaps.WIREMILL_RECIPES, Textures.WIREMILL_OVERLAY,
                ConfigHolder.U.machines.midTierWiremills, ConfigHolder.U.machines.highTierWiremills);

        // Cluster Mill, IDs 635-650
        if (ConfigHolder.U.machines.enableClusterMill) {
            registerSimpleMetaTileEntity(CLUSTER_MILL, 635, "cluster_mill", RecipeMaps.CLUSTER_MILL_RECIPES, Textures.WIREMILL_OVERLAY,
                    ConfigHolder.U.machines.midTierClusterMills, ConfigHolder.U.machines.highTierClusterMills);
        }

        // Some space here for more SimpleMachines

        // Space left for these just in case
        // Diesel Generator, IDs 935-949
        COMBUSTION_GENERATOR[0] = GregTechAPI.registerMetaTileEntity(935, new SimpleGeneratorMetaTileEntity(gregtechId("combustion_generator.lv"), RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 1));
        COMBUSTION_GENERATOR[1] = GregTechAPI.registerMetaTileEntity(936, new SimpleGeneratorMetaTileEntity(gregtechId("combustion_generator.mv"), RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 2));
        COMBUSTION_GENERATOR[2] = GregTechAPI.registerMetaTileEntity(937, new SimpleGeneratorMetaTileEntity(gregtechId("combustion_generator.hv"), RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 3));

        // Steam Turbine, IDs 950-964
        STEAM_TURBINE[0] = GregTechAPI.registerMetaTileEntity(950, new SimpleGeneratorMetaTileEntity(gregtechId("steam_turbine.lv"), RecipeMaps.STEAM_TURBINE_FUELS, Textures.STEAM_TURBINE_OVERLAY, 1));
        STEAM_TURBINE[1] = GregTechAPI.registerMetaTileEntity(951, new SimpleGeneratorMetaTileEntity(gregtechId("steam_turbine.mv"), RecipeMaps.STEAM_TURBINE_FUELS, Textures.STEAM_TURBINE_OVERLAY, 2));
        STEAM_TURBINE[2] = GregTechAPI.registerMetaTileEntity(952, new SimpleGeneratorMetaTileEntity(gregtechId("steam_turbine.hv"), RecipeMaps.STEAM_TURBINE_FUELS, Textures.STEAM_TURBINE_OVERLAY, 3));

        // Gas Turbine, IDs 965-979
        GAS_TURBINE[0] = GregTechAPI.registerMetaTileEntity(965, new SimpleGeneratorMetaTileEntity(gregtechId("gas_turbine.lv"), RecipeMaps.GAS_TURBINE_FUELS, Textures.GAS_TURBINE_OVERLAY, 1));
        GAS_TURBINE[1] = GregTechAPI.registerMetaTileEntity(966, new SimpleGeneratorMetaTileEntity(gregtechId("gas_turbine.mv"), RecipeMaps.GAS_TURBINE_FUELS, Textures.GAS_TURBINE_OVERLAY, 2));
        GAS_TURBINE[2] = GregTechAPI.registerMetaTileEntity(967, new SimpleGeneratorMetaTileEntity(gregtechId("gas_turbine.hv"), RecipeMaps.GAS_TURBINE_FUELS, Textures.GAS_TURBINE_OVERLAY, 3));

        // Item Collector, IDs 980-983
        ITEM_COLLECTOR[0] = GregTechAPI.registerMetaTileEntity(980, new MetaTileEntityItemCollector(gregtechId("item_collector.lv"), 1, 8));
        ITEM_COLLECTOR[1] = GregTechAPI.registerMetaTileEntity(981, new MetaTileEntityItemCollector(gregtechId("item_collector.mv"), 2, 16));
        ITEM_COLLECTOR[2] = GregTechAPI.registerMetaTileEntity(982, new MetaTileEntityItemCollector(gregtechId("item_collector.hv"), 3, 32));
        ITEM_COLLECTOR[3] = GregTechAPI.registerMetaTileEntity(983, new MetaTileEntityItemCollector(gregtechId("item_collector.ev"), 4, 64));

        MAGIC_ENERGY_ABSORBER = GregTechAPI.registerMetaTileEntity(984, new MetaTileEntityMagicEnergyAbsorber(gregtechId("magic_energy_absorber")));

        // Hulls, IDs 985-999
        int endPos = GTValues.HT ? HULL.length - 1 : Math.min(HULL.length - 1, GTValues.UV + 1);
        for (int i = 0; i < endPos; i++) {
            HULL[i] = new MetaTileEntityHull(gregtechId("hull." + GTValues.VN[i].toLowerCase()), i);
            GregTechAPI.registerMetaTileEntity(985 + i, HULL[i]);
        }
        // MAX Hull
        HULL[HULL.length - 1] = new MetaTileEntityHull(gregtechId("hull.max"), HULL.length - 1);
        GregTechAPI.registerMetaTileEntity(985 + HULL.length - 1, HULL[HULL.length - 1]);

        // MULTIBLOCK START: IDs 1000-1299. Space left for addons to register Multiblocks grouped with the rest in JEI
        PRIMITIVE_BLAST_FURNACE = GregTechAPI.registerMetaTileEntity(1000, new MetaTileEntityPrimitiveBlastFurnace(gregtechId("primitive_blast_furnace.bronze")));
        ELECTRIC_BLAST_FURNACE = GregTechAPI.registerMetaTileEntity(1001, new MetaTileEntityElectricBlastFurnace(gregtechId("electric_blast_furnace")));
        VACUUM_FREEZER = GregTechAPI.registerMetaTileEntity(1002, new MetaTileEntityVacuumFreezer(gregtechId("vacuum_freezer")));
        IMPLOSION_COMPRESSOR = GregTechAPI.registerMetaTileEntity(1003, new MetaTileEntityImplosionCompressor(gregtechId("implosion_compressor")));
        PYROLYSE_OVEN = GregTechAPI.registerMetaTileEntity(1004, new MetaTileEntityPyrolyseOven(gregtechId("pyrolyse_oven")));
        DISTILLATION_TOWER = GregTechAPI.registerMetaTileEntity(1005, new MetaTileEntityDistillationTower(gregtechId("distillation_tower")));
        MULTI_FURNACE = GregTechAPI.registerMetaTileEntity(1006, new MetaTileEntityMultiFurnace(gregtechId("multi_furnace")));
        LARGE_COMBUSTION_ENGINE = GregTechAPI.registerMetaTileEntity(1007, new MetaTileEntityLargeCombustionEngine(gregtechId("large_combustion_engine")));
        CRACKER = GregTechAPI.registerMetaTileEntity(1008, new MetaTileEntityCrackingUnit(gregtechId("cracker")));

        LARGE_STEAM_TURBINE = GregTechAPI.registerMetaTileEntity(1009, new MetaTileEntityLargeTurbine(gregtechId("large_turbine.steam"), TurbineType.STEAM));
        LARGE_GAS_TURBINE = GregTechAPI.registerMetaTileEntity(1010, new MetaTileEntityLargeTurbine(gregtechId("large_turbine.gas"), TurbineType.GAS));
        LARGE_PLASMA_TURBINE = GregTechAPI.registerMetaTileEntity(1011, new MetaTileEntityLargeTurbine(gregtechId("large_turbine.plasma"), TurbineType.PLASMA));

        LARGE_BRONZE_BOILER = GregTechAPI.registerMetaTileEntity(1012, new MetaTileEntityLargeBoiler(gregtechId("large_boiler.bronze"), BoilerType.BRONZE));
        LARGE_STEEL_BOILER = GregTechAPI.registerMetaTileEntity(1013, new MetaTileEntityLargeBoiler(gregtechId("large_boiler.steel"), BoilerType.STEEL));
        LARGE_TITANIUM_BOILER = GregTechAPI.registerMetaTileEntity(1014, new MetaTileEntityLargeBoiler(gregtechId("large_boiler.titanium"), BoilerType.TITANIUM));
        LARGE_TUNGSTENSTEEL_BOILER = GregTechAPI.registerMetaTileEntity(1015, new MetaTileEntityLargeBoiler(gregtechId("large_boiler.tungstensteel"), BoilerType.TUNGSTENSTEEL));

        COKE_OVEN = GregTechAPI.registerMetaTileEntity(1016, new MetaTileEntityCokeOven(gregtechId("coke_oven")));
        COKE_OVEN_HATCH = GregTechAPI.registerMetaTileEntity(1017, new MetaTileEntityCokeOvenHatch(gregtechId("coke_oven_hatch")));

        ASSEMBLY_LINE = GregTechAPI.registerMetaTileEntity(1018, new MetaTileEntityAssemblyLine(gregtechId("assembly_line")));
        FUSION_REACTOR[0] = GregTechAPI.registerMetaTileEntity(1019, new MetaTileEntityFusionReactor(gregtechId("fusion_reactor.luv"), 6));
        FUSION_REACTOR[1] = GregTechAPI.registerMetaTileEntity(1020, new MetaTileEntityFusionReactor(gregtechId("fusion_reactor.zpm"), 7));
        FUSION_REACTOR[2] = GregTechAPI.registerMetaTileEntity(1021, new MetaTileEntityFusionReactor(gregtechId("fusion_reactor.uv"), 8));

        LARGE_CHEMICAL_REACTOR = GregTechAPI.registerMetaTileEntity(1022, new MetaTileEntityLargeChemicalReactor(gregtechId("large_chemical_reactor")));

        // MISC MTE's START: IDs 1300-2000

        // Transformer, IDs 1300-1314
        endPos = GTValues.HT ? TRANSFORMER.length : Math.min(TRANSFORMER.length, GTValues.UV);
        for (int i = 1; i <= endPos; i++) {
            MetaTileEntityTransformer transformer = new MetaTileEntityTransformer(gregtechId("transformer." + GTValues.VN[i].toLowerCase()), i);
            TRANSFORMER[i - 1] = GregTechAPI.registerMetaTileEntity(1300 + (i - 1), transformer);
        }

        // Battery Buffer, IDs 1315-1374
        endPos = GTValues.HT ? BATTERY_BUFFER.length - 1 : Math.min(BATTERY_BUFFER.length - 1, GTValues.UV + 1);
        int[] batteryBufferSlots = new int[]{1, 4, 9, 16};
        for (int i = 0; i < endPos; i++) {
            BATTERY_BUFFER[i] = new MetaTileEntityBatteryBuffer[batteryBufferSlots.length];
            for (int slot = 0; slot < batteryBufferSlots.length; slot++) {
                String bufferId = "battery_buffer." + GTValues.VN[i].toLowerCase() + "." + batteryBufferSlots[slot];
                MetaTileEntityBatteryBuffer batteryBuffer = new MetaTileEntityBatteryBuffer(gregtechId(bufferId), i, batteryBufferSlots[slot]);
                BATTERY_BUFFER[i][slot] = GregTechAPI.registerMetaTileEntity(1315 + batteryBufferSlots.length * i + slot, batteryBuffer);
            }
        }
        // MAX Battery Buffer
        BATTERY_BUFFER[BATTERY_BUFFER.length - 1] = new MetaTileEntityBatteryBuffer[batteryBufferSlots.length];
        for (int slot = 0; slot < batteryBufferSlots.length; slot++) {
            MetaTileEntityBatteryBuffer batteryBuffer = new MetaTileEntityBatteryBuffer(gregtechId("battery_buffer.max." + batteryBufferSlots[slot]), BATTERY_BUFFER.length - 1, batteryBufferSlots[slot]);
            BATTERY_BUFFER[BATTERY_BUFFER.length - 1][slot] = GregTechAPI.registerMetaTileEntity(1315 + batteryBufferSlots.length * (BATTERY_BUFFER.length - 1) + slot, batteryBuffer);
        }

        // Charger, IDs 1375-1389
        endPos = GTValues.HT ? CHARGER.length - 1 : Math.min(CHARGER.length - 1, GTValues.UV + 1);
        for (int i = 0; i < endPos; i++) {
            String chargerId = "charger." + GTValues.VN[i].toLowerCase();
            MetaTileEntityCharger charger = new MetaTileEntityCharger(gregtechId(chargerId), i, 4);
            CHARGER[i] = GregTechAPI.registerMetaTileEntity(1375 + i, charger);
        }
        // MAX Charger
        MetaTileEntityCharger charger = new MetaTileEntityCharger(gregtechId("charger.max"), CHARGER.length - 1, 4);
        CHARGER[CHARGER.length - 1] = GregTechAPI.registerMetaTileEntity(1375 + CHARGER.length - 1, charger);

        // Import/Export Buses/Hatches, IDs 1390-1449
        for (int i = 0; i < ITEM_IMPORT_BUS.length - 1; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            ITEM_IMPORT_BUS[i] = new MetaTileEntityItemBus(gregtechId("item_bus.import." + voltageName), i, false);
            ITEM_EXPORT_BUS[i] = new MetaTileEntityItemBus(gregtechId("item_bus.export." + voltageName), i, true);
            FLUID_IMPORT_HATCH[i] = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import." + voltageName), i, false);
            FLUID_EXPORT_HATCH[i] = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.export." + voltageName), i, true);

            GregTechAPI.registerMetaTileEntity(1390 + i, ITEM_IMPORT_BUS[i]);
            GregTechAPI.registerMetaTileEntity(1405 + i, ITEM_EXPORT_BUS[i]);
            GregTechAPI.registerMetaTileEntity(1420 + i, FLUID_IMPORT_HATCH[i]);
            GregTechAPI.registerMetaTileEntity(1435 + i, FLUID_EXPORT_HATCH[i]);
        }
        // Max Hatches/Buses
        ITEM_IMPORT_BUS[9] = new MetaTileEntityItemBus(gregtechId("item_bus.import.max"), 14, false);
        ITEM_EXPORT_BUS[9] = new MetaTileEntityItemBus(gregtechId("item_bus.export.max"), 14, true);
        FLUID_IMPORT_HATCH[9] = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.max"), 14, false);
        FLUID_EXPORT_HATCH[9] = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.export.max"), 14, true);

        GregTechAPI.registerMetaTileEntity(1390 + 9, ITEM_IMPORT_BUS[9]);
        GregTechAPI.registerMetaTileEntity(1405 + 9, ITEM_EXPORT_BUS[9]);
        GregTechAPI.registerMetaTileEntity(1420 + 9, FLUID_IMPORT_HATCH[9]);
        GregTechAPI.registerMetaTileEntity(1435 + 9, FLUID_EXPORT_HATCH[9]);

        // Energy Input/Output Hatches, IDs 1450-1479
        endPos = GTValues.HT ? ENERGY_INPUT_HATCH.length - 1 : Math.min(ENERGY_INPUT_HATCH.length - 1, GTValues.UV + 1);
        for (int i = 0; i < endPos; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            ENERGY_INPUT_HATCH[i] = new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.input." + voltageName), i, false);
            ENERGY_OUTPUT_HATCH[i] = new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.output." + voltageName), i, true);

            GregTechAPI.registerMetaTileEntity(1450 + i, ENERGY_INPUT_HATCH[i]);
            GregTechAPI.registerMetaTileEntity(1465 + i, ENERGY_OUTPUT_HATCH[i]);
        }
        // MAX Hatches
        ENERGY_INPUT_HATCH[ENERGY_INPUT_HATCH.length - 1] = new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.input.max"), ENERGY_INPUT_HATCH.length - 1, false);
        ENERGY_OUTPUT_HATCH[ENERGY_OUTPUT_HATCH.length - 1] = new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.output.max"), ENERGY_OUTPUT_HATCH.length - 1, true);

        GregTechAPI.registerMetaTileEntity(1450 + ENERGY_INPUT_HATCH.length - 1, ENERGY_INPUT_HATCH[ENERGY_INPUT_HATCH.length - 1]);
        GregTechAPI.registerMetaTileEntity(1465 + ENERGY_OUTPUT_HATCH.length - 1, ENERGY_OUTPUT_HATCH[ENERGY_OUTPUT_HATCH.length - 1]);

        // Rotor Holder, IDs 1480-1484
        ROTOR_HOLDER[0] = GregTechAPI.registerMetaTileEntity(1480, new MetaTileEntityRotorHolder(gregtechId("rotor_holder.hv"), GTValues.HV, 1.0f));
        ROTOR_HOLDER[1] = GregTechAPI.registerMetaTileEntity(1481, new MetaTileEntityRotorHolder(gregtechId("rotor_holder.luv"), GTValues.LuV, 1.15f));
        ROTOR_HOLDER[2] = GregTechAPI.registerMetaTileEntity(1482, new MetaTileEntityRotorHolder(gregtechId("rotor_holder.uhv"), GTValues.UHV, 1.25f));

        // Chests, IDs 1485-1499
        SMALL_WOODEN_CHEST = GregTechAPI.registerMetaTileEntity(1485, new MetaTileEntityChest(gregtechId("small_wooden_chest"), Materials.Wood, 1, 1));
        WOODEN_CHEST = GregTechAPI.registerMetaTileEntity(1486, new MetaTileEntityChest(gregtechId("wooden_chest"), Materials.Wood, 9, 3));
        BRONZE_CHEST = GregTechAPI.registerMetaTileEntity(1487, new MetaTileEntityChest(gregtechId("bronze_chest"), Materials.Bronze, 9, 6));
        STEEL_CHEST = GregTechAPI.registerMetaTileEntity(1488, new MetaTileEntityChest(gregtechId("steel_chest"), Materials.Steel, 9, 8));
        STAINLESS_STEEL_CHEST = GregTechAPI.registerMetaTileEntity(1489, new MetaTileEntityChest(gregtechId("stainless_steel_chest"), Materials.StainlessSteel, 9, 10));
        TITANIUM_CHEST = GregTechAPI.registerMetaTileEntity(1490, new MetaTileEntityChest(gregtechId("titanium_chest"), Materials.Titanium, 12, 10));
        TUNGSTENSTEEL_CHEST = GregTechAPI.registerMetaTileEntity(1491, new MetaTileEntityChest(gregtechId("tungstensteel_chest"), Materials.TungstenSteel, 12, 14));

        // Tanks, IDs 1500-1514
        WOODEN_TANK = GregTechAPI.registerMetaTileEntity(1500, new MetaTileEntityTank(gregtechId("wooden_tank"), Materials.Wood, 4000, 1, 3));
        BRONZE_TANK = GregTechAPI.registerMetaTileEntity(1501, new MetaTileEntityTank(gregtechId("bronze_tank"), Materials.Bronze, 8000, 4, 3));
        STEEL_TANK = GregTechAPI.registerMetaTileEntity(1502, new MetaTileEntityTank(gregtechId("steel_tank"), Materials.Steel, 16000, 7, 5));
        STAINLESS_STEEL_TANK = GregTechAPI.registerMetaTileEntity(1503, new MetaTileEntityTank(gregtechId("stainless_steel_tank"), Materials.StainlessSteel, 32000, 9, 7));
        TITANIUM_TANK = GregTechAPI.registerMetaTileEntity(1504, new MetaTileEntityTank(gregtechId("titanium_tank"), Materials.Titanium, 48000, 12, 9));
        TUNGSTENSTEEL_TANK = GregTechAPI.registerMetaTileEntity(1505, new MetaTileEntityTank(gregtechId("tungstensteel_tank"), Materials.TungstenSteel, 64000, 16, 9));

        // Fishers, IDs 1515-1529
        FISHER[0] = GregTechAPI.registerMetaTileEntity(1515, new MetaTileEntityFisher(gregtechId("fisher.lv"), 1));
        FISHER[1] = GregTechAPI.registerMetaTileEntity(1516, new MetaTileEntityFisher(gregtechId("fisher.mv"), 2));
        FISHER[2] = GregTechAPI.registerMetaTileEntity(1517, new MetaTileEntityFisher(gregtechId("fisher.hv"), 3));
        FISHER[3] = GregTechAPI.registerMetaTileEntity(1518, new MetaTileEntityFisher(gregtechId("fisher.ev"), 4));

        // Pumps, IDs 1530-1544
        PUMP[0] = GregTechAPI.registerMetaTileEntity(1530, new MetaTileEntityPump(gregtechId("pump.lv"), 1));
        PUMP[1] = GregTechAPI.registerMetaTileEntity(1531, new MetaTileEntityPump(gregtechId("pump.mv"), 2));
        PUMP[2] = GregTechAPI.registerMetaTileEntity(1532, new MetaTileEntityPump(gregtechId("pump.hv"), 3));
        PUMP[3] = GregTechAPI.registerMetaTileEntity(1533, new MetaTileEntityPump(gregtechId("pump.ev"), 4));
        if (ConfigHolder.U.machines.highTierPumps) {
            PUMP[4] = GregTechAPI.registerMetaTileEntity(1534, new MetaTileEntityPump(gregtechId("pump.iv"), 5));
            PUMP[5] = GregTechAPI.registerMetaTileEntity(1535, new MetaTileEntityPump(gregtechId("pump.luv"), 6));
            PUMP[6] = GregTechAPI.registerMetaTileEntity(1536, new MetaTileEntityPump(gregtechId("pump.zpm"), 7));
            PUMP[7] = GregTechAPI.registerMetaTileEntity(1537, new MetaTileEntityPump(gregtechId("pump.uv"), 8));
        }

        // Air Collectors, IDs 1545-1559
        AIR_COLLECTOR[0] = GregTechAPI.registerMetaTileEntity(1545, new MetaTileEntityAirCollector(gregtechId("air_collector.lv"), 1));
        AIR_COLLECTOR[1] = GregTechAPI.registerMetaTileEntity(1546, new MetaTileEntityAirCollector(gregtechId("air_collector.mv"), 2));
        AIR_COLLECTOR[2] = GregTechAPI.registerMetaTileEntity(1547, new MetaTileEntityAirCollector(gregtechId("air_collector.hv"), 3));
        AIR_COLLECTOR[3] = GregTechAPI.registerMetaTileEntity(1548, new MetaTileEntityAirCollector(gregtechId("air_collector.ev"), 4));
        if (ConfigHolder.U.machines.highTierAirCollectors) {
            AIR_COLLECTOR[4] = GregTechAPI.registerMetaTileEntity(1549, new MetaTileEntityAirCollector(gregtechId("air_collector.iv"), 5));
            AIR_COLLECTOR[5] = GregTechAPI.registerMetaTileEntity(1550, new MetaTileEntityAirCollector(gregtechId("air_collector.luv"), 6));
        }

        // Quantum Chests, IDs 1560-1574
        for (int i = 0; i < QUANTUM_CHEST.length; i++) {
            String voltageName = GTValues.VN[i + 2].toLowerCase();
            QUANTUM_CHEST[i] = new MetaTileEntityQuantumChest(gregtechId("quantum_chest." + voltageName), i + 2, 64L * 64000 * (i + 1));
            GregTechAPI.registerMetaTileEntity(1560 + i, QUANTUM_CHEST[i]);
        }

        // Quantum Tanks, IDs 1575-1589
        for (int i = 0; i < QUANTUM_TANK.length; i++) {
            String voltageName = GTValues.VN[i + 2].toLowerCase();
            QUANTUM_TANK[i] = new MetaTileEntityQuantumTank(gregtechId("quantum_tank." + voltageName), i + 2, 1000 * 64000 * (i + 1));
            GregTechAPI.registerMetaTileEntity(1575 + i, QUANTUM_TANK[i]);
        }

        // Block Breakers, IDs 1590-1594
        for (int i = 0; i < BLOCK_BREAKER.length; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            BLOCK_BREAKER[i] = new MetaTileEntityBlockBreaker(gregtechId("block_breaker." + voltageName), i + 1);
            GregTechAPI.registerMetaTileEntity(1590 + i, BLOCK_BREAKER[i]);
        }

        // Drums, IDs 1595-1609
        if (ConfigHolder.U.registerDrums) {
            WOODEN_DRUM = GregTechAPI.registerMetaTileEntity(1595, new MetaTileEntityDrum(gregtechId("drum.wood"), Materials.Wood, 16000));
            BRONZE_DRUM = GregTechAPI.registerMetaTileEntity(1596, new MetaTileEntityDrum(gregtechId("drum.bronze"), Materials.Bronze, 32000));
            STEEL_DRUM = GregTechAPI.registerMetaTileEntity(1597, new MetaTileEntityDrum(gregtechId("drum.steel"), Materials.Steel, 64000));
            STAINLESS_STEEL_DRUM = GregTechAPI.registerMetaTileEntity(1598, new MetaTileEntityDrum(gregtechId("drum.stainless_steel"), Materials.StainlessSteel, 128000));
            TITANIUM_DRUM = GregTechAPI.registerMetaTileEntity(1599, new MetaTileEntityDrum(gregtechId("drum.titanium"), Materials.Titanium, 192000));
            TUNGSTENSTEEL_DRUM = GregTechAPI.registerMetaTileEntity(1600, new MetaTileEntityDrum(gregtechId("drum.tungstensteel"), Materials.TungstenSteel, 256000));
        }

        // Crates, IDs 1610-1624
        if (ConfigHolder.U.registerCrates) {
            WOODEN_CRATE = GregTechAPI.registerMetaTileEntity(1610, new MetaTileEntityCrate(gregtechId("crate.wood"), Materials.Wood, 36));
            BRONZE_CRATE = GregTechAPI.registerMetaTileEntity(1611, new MetaTileEntityCrate(gregtechId("crate.bronze"), Materials.Bronze, 54));
            STEEL_CRATE = GregTechAPI.registerMetaTileEntity(1612, new MetaTileEntityCrate(gregtechId("crate.steel"), Materials.Steel, 72));
            STAINLESS_STEEL_CRATE = GregTechAPI.registerMetaTileEntity(1613, new MetaTileEntityCrate(gregtechId("crate.stainless_steel"), Materials.StainlessSteel, 90));
            TITANIUM_CRATE = GregTechAPI.registerMetaTileEntity(1614, new MetaTileEntityCrate(gregtechId("crate.titanium"), Materials.Titanium, 108));
            TUNGSTENSTEEL_CRATE = GregTechAPI.registerMetaTileEntity(1615, new MetaTileEntityCrate(gregtechId("crate.tungstensteel"), Materials.TungstenSteel, 126));
        }

        // Misc, IDs 1625-1999
        TESLA_COIL = new MetaTileEntityTeslaCoil(gregtechId("tesla_coil"));
        GregTechAPI.registerMetaTileEntity(1625, TESLA_COIL);
        LOCKED_SAFE = GregTechAPI.registerMetaTileEntity(1626, new MetaTileEntityLockedSafe(gregtechId("locked_safe")));
        WORKBENCH = GregTechAPI.registerMetaTileEntity(1627, new MetaTileEntityWorkbench(gregtechId("workbench")));
        //PRIMITIVE_WATER_PUMP = GregTechAPI.registerMetaTileEntity(1628, new MetaTileEntityWaterPump(gregtechId("primitive_pump")));
        //PUMP_OUTPUT_HATCH = GregTechAPI.registerMetaTileEntity(1629, new MetaTileEntityPumpHatch(gregtechId("pump_hatch")));



        /*
         * FOR ADDON DEVELOPERS:
         *
         * GTCEu will not take more than 2000 IDs. Anything past ID 1999
         * is considered FAIR GAME, take whatever you like.
         *
         * If you would like to reserve IDs, feel free to reach out to the
         * development team and claim a range of IDs! We will mark any
         * claimed ranges below this comment. Max value is 32767.
         *
         * - Gregicality / Shadows of Greg: 2000-3999
         * - Gregification: 4000-4499
         * - GregTech Food Option: 8500-8999
         */
    }

    private static void registerSimpleMetaTileEntity(SimpleMachineMetaTileEntity[] machines,
                                                     int startId,
                                                     String name,
                                                     RecipeMap<?> map,
                                                     OrientedOverlayRenderer texture,
                                                     boolean midTier,
                                                     boolean highTier,
                                                     boolean hasFrontFacing) {
        for (int i = 0; i < machines.length - 1; i++) {
            if (i > 4 && !(ConfigHolder.U.machines.midTierMachines || midTier)) continue;
            if (i > 7 && !(ConfigHolder.U.machines.highTierMachines || highTier)) break;

            String voltageName = GTValues.VN[i + 1].toLowerCase();
            machines[i] = GregTechAPI.registerMetaTileEntity(startId + i,
                    new SimpleMachineMetaTileEntity(gregtechId(String.format("%s.%s", name, voltageName)), map, texture, i + 1, hasFrontFacing));
        }
    }

    private static void registerSimpleMetaTileEntity(SimpleMachineMetaTileEntity[] machines,
                                                     int startId,
                                                     String name,
                                                     RecipeMap<?> map,
                                                     OrientedOverlayRenderer texture,
                                                     boolean midTier,
                                                     boolean highTier) {
        registerSimpleMetaTileEntity(machines, startId, name, map, texture, midTier, highTier, true);
    }

    private static ResourceLocation gregtechId(String name) {
        return new ResourceLocation(GTValues.MODID, name);
    }
}
