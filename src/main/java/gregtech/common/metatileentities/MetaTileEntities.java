package gregtech.common.metatileentities;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.converter.MetaTileEntityConverter;
import gregtech.common.metatileentities.electric.*;
import gregtech.common.metatileentities.multi.*;
import gregtech.common.metatileentities.multi.electric.*;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeCombustionEngine;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import gregtech.common.metatileentities.multi.multiblockpart.*;
import gregtech.common.metatileentities.multi.steam.MetaTileEntitySteamGrinder;
import gregtech.common.metatileentities.multi.steam.MetaTileEntitySteamOven;
import gregtech.common.metatileentities.primitive.MetaTileEntityCharcoalPile;
import gregtech.common.metatileentities.steam.*;
import gregtech.common.metatileentities.steam.boiler.SteamCoalBoiler;
import gregtech.common.metatileentities.steam.boiler.SteamLavaBoiler;
import gregtech.common.metatileentities.steam.boiler.SteamSolarBoiler;
import gregtech.common.metatileentities.steam.multiblockpart.MetaTileEntitySteamHatch;
import gregtech.common.metatileentities.steam.multiblockpart.MetaTileEntitySteamItemBus;
import gregtech.common.metatileentities.storage.*;
import gregtech.integration.jei.multiblock.MultiblockInfoCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MetaTileEntities {

    //HULLS
    public static final MetaTileEntityHull[] HULL = new MetaTileEntityHull[GTValues.V.length];
    public static final MetaTileEntityTransformer[] TRANSFORMER = new MetaTileEntityTransformer[GTValues.V.length - 1]; // no ULV, no MAX
    public static final MetaTileEntityAdjustableTransformer[] ADJUSTABLE_TRANSFORMER = new MetaTileEntityAdjustableTransformer[GTValues.V.length - 1]; // no ULV, no MAX
    public static final MetaTileEntityDiode[] DIODES = new MetaTileEntityDiode[GTValues.V.length];
    public static final MetaTileEntityBatteryBuffer[][] BATTERY_BUFFER = new MetaTileEntityBatteryBuffer[3][GTValues.V.length];
    public static final MetaTileEntityCharger[] CHARGER = new MetaTileEntityCharger[GTValues.V.length];
    //SIMPLE MACHINES SECTION
    public static final SimpleMachineMetaTileEntity[] ELECTRIC_FURNACE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final MetaTileEntityMacerator[] MACERATOR = new MetaTileEntityMacerator[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] ALLOY_SMELTER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] ARC_FURNACE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] ASSEMBLER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] AUTOCLAVE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] BENDER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] BREWERY = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] CANNER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] CENTRIFUGE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] CHEMICAL_BATH = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] CHEMICAL_REACTOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] COMPRESSOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] CUTTER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] DISTILLERY = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] ELECTROLYZER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] ELECTROMAGNETIC_SEPARATOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] EXTRACTOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] EXTRUDER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] FERMENTER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] FLUID_HEATER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] FLUID_SOLIDIFIER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] FORGE_HAMMER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] FORMING_PRESS = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] LATHE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] MIXER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] ORE_WASHER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] PACKER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] UNPACKER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] POLARIZER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] LASER_ENGRAVER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] SIFTER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] THERMAL_CENTRIFUGE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] WIREMILL = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] CIRCUIT_ASSEMBLER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    // TODO Replication system
    //public static final SimpleMachineMetaTileEntity[] MASS_FABRICATOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    //public static final SimpleMachineMetaTileEntity[] REPLICATOR = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    // TODO Assembly Line Research system
    //public static final SimpleMachineMetaTileEntity[] SCANNER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] GAS_COLLECTOR = new MetaTileEntityGasCollector[GTValues.V.length - 1];
    public static final MetaTileEntityRockBreaker[] ROCK_BREAKER = new MetaTileEntityRockBreaker[GTValues.V.length - 1];
    public static final MetaTileEntityMiner[] MINER = new MetaTileEntityMiner[GTValues.V.length - 1];
    //GENERATORS SECTION
    public static final SimpleGeneratorMetaTileEntity[] COMBUSTION_GENERATOR = new SimpleGeneratorMetaTileEntity[4];
    public static final SimpleGeneratorMetaTileEntity[] STEAM_TURBINE = new SimpleGeneratorMetaTileEntity[4];
    public static final SimpleGeneratorMetaTileEntity[] GAS_TURBINE = new SimpleGeneratorMetaTileEntity[4];
    //MULTIBLOCK PARTS SECTION
    public static final MetaTileEntityItemBus[] ITEM_IMPORT_BUS = new MetaTileEntityItemBus[GTValues.UHV + 1]; // ULV-UHV
    public static final MetaTileEntityItemBus[] ITEM_EXPORT_BUS = new MetaTileEntityItemBus[GTValues.UHV + 1];
    public static final MetaTileEntityFluidHatch[] FLUID_IMPORT_HATCH = new MetaTileEntityFluidHatch[GTValues.UHV + 1];
    public static final MetaTileEntityFluidHatch[] FLUID_EXPORT_HATCH = new MetaTileEntityFluidHatch[GTValues.UHV + 1];
    public static final MetaTileEntityMultiFluidHatch[] MULTI_FLUID_IMPORT_HATCH = new MetaTileEntityMultiFluidHatch[2];
    public static final MetaTileEntityMultiFluidHatch[] MULTI_FLUID_EXPORT_HATCH = new MetaTileEntityMultiFluidHatch[2];
    public static final MetaTileEntityEnergyHatch[] ENERGY_INPUT_HATCH = new MetaTileEntityEnergyHatch[GTValues.V.length];
    public static final MetaTileEntityEnergyHatch[] ENERGY_INPUT_HATCH_4A = new MetaTileEntityEnergyHatch[6]; // EV, IV, LuV, ZPM, UV, UHV
    public static final MetaTileEntityEnergyHatch[] ENERGY_INPUT_HATCH_16A = new MetaTileEntityEnergyHatch[5]; // IV, LuV, ZPM, UV, UHV
    public static final MetaTileEntityEnergyHatch[] ENERGY_OUTPUT_HATCH = new MetaTileEntityEnergyHatch[GTValues.V.length];
    public static final MetaTileEntityEnergyHatch[] ENERGY_OUTPUT_HATCH_4A = new MetaTileEntityEnergyHatch[6]; // EV, IV, LuV, ZPM, UV, UHV
    public static final MetaTileEntityEnergyHatch[] ENERGY_OUTPUT_HATCH_16A = new MetaTileEntityEnergyHatch[5]; // IV, LuV, ZPM, UV, UHV
    public static final MetaTileEntityRotorHolder[] ROTOR_HOLDER = new MetaTileEntityRotorHolder[6]; //HV, EV, IV, LuV, ZPM, UV
    public static final MetaTileEntityMufflerHatch[] MUFFLER_HATCH = new MetaTileEntityMufflerHatch[GTValues.UV]; // LV-UV
    public static final MetaTileEntityFusionReactor[] FUSION_REACTOR = new MetaTileEntityFusionReactor[3];
    public static final MetaTileEntityQuantumChest[] QUANTUM_CHEST = new MetaTileEntityQuantumChest[10];
    public static final MetaTileEntityQuantumTank[] QUANTUM_TANK = new MetaTileEntityQuantumTank[10];
    public static final MetaTileEntityBuffer[] BUFFER = new MetaTileEntityBuffer[3];
    public static final MetaTileEntityPump[] PUMP = new MetaTileEntityPump[8];
    public static final MetaTileEntityBlockBreaker[] BLOCK_BREAKER = new MetaTileEntityBlockBreaker[4];
    public static final MetaTileEntityItemCollector[] ITEM_COLLECTOR = new MetaTileEntityItemCollector[4];
    public static final MetaTileEntityFisher[] FISHER = new MetaTileEntityFisher[4];
    public static final MetaTileEntityWorldAccelerator[] WORLD_ACCELERATOR = new MetaTileEntityWorldAccelerator[8]; // no ULV, no MAX
    public static MetaTileEntityMachineHatch MACHINE_HATCH;
    public static MetaTileEntityPassthroughHatchItem PASSTHROUGH_HATCH_ITEM;
    public static MetaTileEntityPassthroughHatchFluid PASSTHROUGH_HATCH_FLUID;
    // Used for addons if they wish to disable certain tiers of machines
    private static final Map<String, Boolean> MID_TIER = new HashMap<>();
    private static final Map<String, Boolean> HIGH_TIER = new HashMap<>();
    //STEAM AGE SECTION
    public static SteamCoalBoiler STEAM_BOILER_COAL_BRONZE;
    public static SteamCoalBoiler STEAM_BOILER_COAL_STEEL;
    public static SteamSolarBoiler STEAM_BOILER_SOLAR_BRONZE;
    public static SteamSolarBoiler STEAM_BOILER_SOLAR_STEEL;
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
    public static SteamRockBreaker STEAM_ROCK_BREAKER_BRONZE;
    public static SteamRockBreaker STEAM_ROCK_BREAKER_STEEL;
    public static SteamMiner STEAM_MINER;
    public static MetaTileEntityPumpHatch PUMP_OUTPUT_HATCH;
    public static MetaTileEntityPrimitiveWaterPump PRIMITIVE_WATER_PUMP;
    public static MetaTileEntityMagicEnergyAbsorber MAGIC_ENERGY_ABSORBER;
    public static MetaTileEntityCokeOvenHatch COKE_OVEN_HATCH;
    public static MetaTileEntitySteamItemBus STEAM_EXPORT_BUS;
    public static MetaTileEntitySteamItemBus STEAM_IMPORT_BUS;
    public static MetaTileEntitySteamHatch STEAM_HATCH;
    public static MetaTileEntityMaintenanceHatch MAINTENANCE_HATCH;
    public static MetaTileEntityMaintenanceHatch CONFIGURABLE_MAINTENANCE_HATCH;
    public static MetaTileEntityAutoMaintenanceHatch AUTO_MAINTENANCE_HATCH;
    public static MetaTileEntityCleaningMaintenanceHatch CLEANING_MAINTENANCE_HATCH;
    //MULTIBLOCKS SECTION
    public static MetaTileEntityPrimitiveBlastFurnace PRIMITIVE_BLAST_FURNACE;
    public static MetaTileEntityCokeOven COKE_OVEN;
    public static MetaTileEntityElectricBlastFurnace ELECTRIC_BLAST_FURNACE;
    public static MetaTileEntityVacuumFreezer VACUUM_FREEZER;
    public static MetaTileEntityImplosionCompressor IMPLOSION_COMPRESSOR;
    public static MetaTileEntityPyrolyseOven PYROLYSE_OVEN;
    public static MetaTileEntityDistillationTower DISTILLATION_TOWER;
    public static MetaTileEntityCrackingUnit CRACKER;
    public static MetaTileEntityMultiSmelter MULTI_FURNACE;
    public static MetaTileEntityLargeCombustionEngine LARGE_COMBUSTION_ENGINE;
    public static MetaTileEntityLargeCombustionEngine EXTREME_COMBUSTION_ENGINE;
    public static MetaTileEntityLargeTurbine LARGE_STEAM_TURBINE;
    public static MetaTileEntityLargeTurbine LARGE_GAS_TURBINE;
    public static MetaTileEntityLargeTurbine LARGE_PLASMA_TURBINE;
    public static MetaTileEntityLargeBoiler LARGE_BRONZE_BOILER;
    public static MetaTileEntityLargeBoiler LARGE_STEEL_BOILER;
    public static MetaTileEntityLargeBoiler LARGE_TITANIUM_BOILER;
    public static MetaTileEntityLargeBoiler LARGE_TUNGSTENSTEEL_BOILER;
    public static MetaTileEntityAssemblyLine ASSEMBLY_LINE;
    public static MetaTileEntityLargeChemicalReactor LARGE_CHEMICAL_REACTOR;
    public static MetaTileEntitySteamOven STEAM_OVEN;
    public static MetaTileEntitySteamGrinder STEAM_GRINDER;
    public static MetaTileEntityLargeMiner BASIC_LARGE_MINER;
    public static MetaTileEntityLargeMiner LARGE_MINER;
    public static MetaTileEntityLargeMiner ADVANCED_LARGE_MINER;
    public static MetaTileEntityProcessingArray PROCESSING_ARRAY;
    public static MetaTileEntityProcessingArray ADVANCED_PROCESSING_ARRAY;
    public static MetaTileEntityFluidDrill BASIC_FLUID_DRILLING_RIG;
    public static MetaTileEntityFluidDrill FLUID_DRILLING_RIG;
    public static MetaTileEntityFluidDrill ADVANCED_FLUID_DRILLING_RIG;
    public static MetaTileEntityCleanroom CLEANROOM;
    public static MetaTileEntityCharcoalPile CHARCOAL_PILE;
    //STORAGE SECTION
    public static MetaTileEntityLockedSafe LOCKED_SAFE;
    public static MetaTileEntityTankValve WOODEN_TANK_VALVE;
    public static MetaTileEntityTankValve STEEL_TANK_VALVE;
    public static MetaTileEntityMultiblockTank WOODEN_TANK;
    public static MetaTileEntityMultiblockTank STEEL_TANK;
    public static MetaTileEntityDrum WOODEN_DRUM;
    public static MetaTileEntityDrum BRONZE_DRUM;
    public static MetaTileEntityDrum ALUMINIUM_DRUM;
    public static MetaTileEntityDrum STEEL_DRUM;
    public static MetaTileEntityDrum STAINLESS_STEEL_DRUM;
    public static MetaTileEntityCrate WOODEN_CRATE;
    public static MetaTileEntityCrate BRONZE_CRATE;
    public static MetaTileEntityCrate ALUMINIUM_CRATE;
    public static MetaTileEntityCrate STEEL_CRATE;
    public static MetaTileEntityCrate STAINLESS_STEEL_CRATE;
    public static MetaTileEntityCrate TITANIUM_CRATE;
    public static MetaTileEntityCrate TUNGSTENSTEEL_CRATE;
    //MISC MACHINES SECTION
    public static MetaTileEntityWorkbench WORKBENCH;
    public static MetaTileEntityCreativeEnergy CREATIVE_ENERGY;
    public static MetaTileEntityCreativeTank CREATIVE_TANK;
    public static MetaTileEntityCreativeChest CREATIVE_CHEST;
    public static MetaTileEntityClipboard CLIPBOARD_TILE;
    public static MetaTileEntityMonitorScreen MONITOR_SCREEN;
    public static MetaTileEntityCentralMonitor CENTRAL_MONITOR;

    public static MetaTileEntityConverter[][] ENERGY_CONVERTER = new MetaTileEntityConverter[4][GTValues.V.length];

    public static void init() {
        GTLog.logger.info("Registering MetaTileEntities");

        STEAM_BOILER_COAL_BRONZE = registerMetaTileEntity(1, new SteamCoalBoiler(gregtechId("steam_boiler_coal_bronze"), false));
        STEAM_BOILER_COAL_STEEL = registerMetaTileEntity(2, new SteamCoalBoiler(gregtechId("steam_boiler_coal_steel"), true));

        STEAM_BOILER_SOLAR_BRONZE = registerMetaTileEntity(3, new SteamSolarBoiler(gregtechId("steam_boiler_solar_bronze"), false));
        STEAM_BOILER_SOLAR_STEEL = registerMetaTileEntity(4, new SteamSolarBoiler(gregtechId("steam_boiler_solar_steel"), true));

        STEAM_BOILER_LAVA_BRONZE = registerMetaTileEntity(5, new SteamLavaBoiler(gregtechId("steam_boiler_lava_bronze"), false));
        STEAM_BOILER_LAVA_STEEL = registerMetaTileEntity(6, new SteamLavaBoiler(gregtechId("steam_boiler_lava_steel"), true));

        STEAM_EXTRACTOR_BRONZE = registerMetaTileEntity(7, new SteamExtractor(gregtechId("steam_extractor_bronze"), false));
        STEAM_EXTRACTOR_STEEL = registerMetaTileEntity(8, new SteamExtractor(gregtechId("steam_extractor_steel"), true));

        STEAM_MACERATOR_BRONZE = registerMetaTileEntity(9, new SteamMacerator(gregtechId("steam_macerator_bronze"), false));
        STEAM_MACERATOR_STEEL = registerMetaTileEntity(10, new SteamMacerator(gregtechId("steam_macerator_steel"), true));

        STEAM_COMPRESSOR_BRONZE = registerMetaTileEntity(11, new SteamCompressor(gregtechId("steam_compressor_bronze"), false));
        STEAM_COMPRESSOR_STEEL = registerMetaTileEntity(12, new SteamCompressor(gregtechId("steam_compressor_steel"), true));

        STEAM_HAMMER_BRONZE = registerMetaTileEntity(13, new SteamHammer(gregtechId("steam_hammer_bronze"), false));
        STEAM_HAMMER_STEEL = registerMetaTileEntity(14, new SteamHammer(gregtechId("steam_hammer_steel"), true));

        STEAM_FURNACE_BRONZE = registerMetaTileEntity(15, new SteamFurnace(gregtechId("steam_furnace_bronze"), false));
        STEAM_FURNACE_STEEL = registerMetaTileEntity(16, new SteamFurnace(gregtechId("steam_furnace_steel"), true));

        STEAM_ALLOY_SMELTER_BRONZE = registerMetaTileEntity(17, new SteamAlloySmelter(gregtechId("steam_alloy_smelter_bronze"), false));
        STEAM_ALLOY_SMELTER_STEEL = registerMetaTileEntity(18, new SteamAlloySmelter(gregtechId("steam_alloy_smelter_steel"), true));

        STEAM_ROCK_BREAKER_BRONZE = registerMetaTileEntity(19, new SteamRockBreaker(gregtechId("steam_rock_breaker_bronze"), false));
        STEAM_ROCK_BREAKER_STEEL = registerMetaTileEntity(20, new SteamRockBreaker(gregtechId("steam_rock_breaker_steel"), true));

        STEAM_MINER = registerMetaTileEntity(21, new SteamMiner(gregtechId("steam_miner"), 320, 4, 0));

        // Electric Furnace, IDs 50-64
        registerSimpleMetaTileEntity(ELECTRIC_FURNACE, 50, "electric_furnace", RecipeMaps.FURNACE_RECIPES, Textures.ELECTRIC_FURNACE_OVERLAY, true);

        // Macerator, IDs 65-79
        MACERATOR[1] = registerMetaTileEntity(65, new MetaTileEntityMacerator(gregtechId("macerator.lv"), RecipeMaps.MACERATOR_RECIPES, 1, Textures.MACERATOR_OVERLAY, 1));
        MACERATOR[2] = registerMetaTileEntity(66, new MetaTileEntityMacerator(gregtechId("macerator.mv"), RecipeMaps.MACERATOR_RECIPES, 1, Textures.MACERATOR_OVERLAY, 2));
        MACERATOR[3] = registerMetaTileEntity(67, new MetaTileEntityMacerator(gregtechId("macerator.hv"), RecipeMaps.MACERATOR_RECIPES, 3, Textures.PULVERIZER_OVERLAY, 3));
        MACERATOR[4] = registerMetaTileEntity(68, new MetaTileEntityMacerator(gregtechId("macerator.ev"), RecipeMaps.MACERATOR_RECIPES, 4, Textures.PULVERIZER_OVERLAY, 4));
        MACERATOR[5] = registerMetaTileEntity(69, new MetaTileEntityMacerator(gregtechId("macerator.iv"), RecipeMaps.MACERATOR_RECIPES, 4, Textures.PULVERIZER_OVERLAY, 5));
        if (getMidTier("macerator")) {
            MACERATOR[6] = registerMetaTileEntity(70, new MetaTileEntityMacerator(gregtechId("macerator.luv"), RecipeMaps.MACERATOR_RECIPES, 4, Textures.PULVERIZER_OVERLAY, 6));
            MACERATOR[7] = registerMetaTileEntity(71, new MetaTileEntityMacerator(gregtechId("macerator.zpm"), RecipeMaps.MACERATOR_RECIPES, 4, Textures.PULVERIZER_OVERLAY, 7));
            MACERATOR[8] = registerMetaTileEntity(72, new MetaTileEntityMacerator(gregtechId("macerator.uv"), RecipeMaps.MACERATOR_RECIPES, 4, Textures.PULVERIZER_OVERLAY, 8));
        }
        if (getHighTier("macerator")) {
            MACERATOR[9] = registerMetaTileEntity(73, new MetaTileEntityMacerator(gregtechId("macerator.uhv"), RecipeMaps.MACERATOR_RECIPES, 4, Textures.PULVERIZER_OVERLAY, 9));
            MACERATOR[10] = registerMetaTileEntity(74, new MetaTileEntityMacerator(gregtechId("macerator.uev"), RecipeMaps.MACERATOR_RECIPES, 4, Textures.PULVERIZER_OVERLAY, 10));
            MACERATOR[11] = registerMetaTileEntity(75, new MetaTileEntityMacerator(gregtechId("macerator.uiv"), RecipeMaps.MACERATOR_RECIPES, 4, Textures.PULVERIZER_OVERLAY, 11));
            MACERATOR[12] = registerMetaTileEntity(76, new MetaTileEntityMacerator(gregtechId("macerator.uxv"), RecipeMaps.MACERATOR_RECIPES, 4, Textures.PULVERIZER_OVERLAY, 12));
            MACERATOR[13] = registerMetaTileEntity(77, new MetaTileEntityMacerator(gregtechId("macerator.opv"), RecipeMaps.MACERATOR_RECIPES, 4, Textures.PULVERIZER_OVERLAY, 13));
        }

        // Alloy Smelter, IDs 80-94
        registerSimpleMetaTileEntity(ALLOY_SMELTER, 80, "alloy_smelter", RecipeMaps.ALLOY_SMELTER_RECIPES, Textures.ALLOY_SMELTER_OVERLAY, true);

        // Arc Furnace, IDs 95-109
        registerSimpleMetaTileEntity(ARC_FURNACE, 95, "arc_furnace", RecipeMaps.ARC_FURNACE_RECIPES, Textures.ARC_FURNACE_OVERLAY, false, GTUtility.hvCappedTankSizeFunction);

        // Assembler, IDs 110-124
        registerSimpleMetaTileEntity(ASSEMBLER, 110, "assembler", RecipeMaps.ASSEMBLER_RECIPES, Textures.ASSEMBLER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Autoclave, IDs 125-139
        registerSimpleMetaTileEntity(AUTOCLAVE, 125, "autoclave", RecipeMaps.AUTOCLAVE_RECIPES, Textures.AUTOCLAVE_OVERLAY, false, GTUtility.hvCappedTankSizeFunction);

        // Bender, IDs 140-154
        registerSimpleMetaTileEntity(BENDER, 140, "bender", RecipeMaps.BENDER_RECIPES, Textures.BENDER_OVERLAY, true);

        // Brewery, IDs 155-169
        registerSimpleMetaTileEntity(BREWERY, 155, "brewery", RecipeMaps.BREWING_RECIPES, Textures.BREWERY_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Canner, IDs 170-184
        registerSimpleMetaTileEntity(CANNER, 170, "canner", RecipeMaps.CANNER_RECIPES, Textures.CANNER_OVERLAY, true);

        // Centrifuge, IDs 185-199
        registerSimpleMetaTileEntity(CENTRIFUGE, 185, "centrifuge", RecipeMaps.CENTRIFUGE_RECIPES, Textures.CENTRIFUGE_OVERLAY, false, GTUtility.largeTankSizeFunction);

        // Chemical Bath, IDs 200-214
        registerSimpleMetaTileEntity(CHEMICAL_BATH, 200, "chemical_bath", RecipeMaps.CHEMICAL_BATH_RECIPES, Textures.CHEMICAL_BATH_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Chemical Reactor, IDs 215-229
        registerSimpleMetaTileEntity(CHEMICAL_REACTOR, 215, "chemical_reactor", RecipeMaps.CHEMICAL_RECIPES, Textures.CHEMICAL_REACTOR_OVERLAY, true, tier -> 16000);

        // Compressor, IDs 230-244
        registerSimpleMetaTileEntity(COMPRESSOR, 230, "compressor", RecipeMaps.COMPRESSOR_RECIPES, Textures.COMPRESSOR_OVERLAY, true);

        // Cutter, IDs 245-259
        registerSimpleMetaTileEntity(CUTTER, 245, "cutter", RecipeMaps.CUTTER_RECIPES, Textures.CUTTER_OVERLAY, true);

        // Distillery, IDs 260-274
        registerSimpleMetaTileEntity(DISTILLERY, 260, "distillery", RecipeMaps.DISTILLERY_RECIPES, Textures.DISTILLERY_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Electrolyzer, IDs 275-289
        registerSimpleMetaTileEntity(ELECTROLYZER, 275, "electrolyzer", RecipeMaps.ELECTROLYZER_RECIPES, Textures.ELECTROLYZER_OVERLAY, false, GTUtility.largeTankSizeFunction);

        // Electromagnetic Separator, IDs 290-304
        registerSimpleMetaTileEntity(ELECTROMAGNETIC_SEPARATOR, 290, "electromagnetic_separator", RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES, Textures.ELECTROMAGNETIC_SEPARATOR_OVERLAY, true);

        // Extractor, IDs 305-319
        registerSimpleMetaTileEntity(EXTRACTOR, 305, "extractor", RecipeMaps.EXTRACTOR_RECIPES, Textures.EXTRACTOR_OVERLAY, true);

        // Extruder, IDs 320-334
        registerSimpleMetaTileEntity(EXTRUDER, 320, "extruder", RecipeMaps.EXTRUDER_RECIPES, Textures.EXTRUDER_OVERLAY, true);

        // Fermenter, IDs 335-349
        registerSimpleMetaTileEntity(FERMENTER, 335, "fermenter", RecipeMaps.FERMENTING_RECIPES, Textures.FERMENTER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // TODO Replication system
        // Mass Fabricator, IDs 350-364
        //registerSimpleMetaTileEntity(MASS_FABRICATOR, 350, "mass_fabricator", RecipeMaps.MASS_FABRICATOR_RECIPES, Textures.MASS_FABRICATOR_OVERLAY, true);

        // TODO Should anonymously override SimpleMachineMetaTileEntity#getCircuitSlotOverlay() to display the data stick overlay
        // Replicator, IDs 365-379
        //registerSimpleMetaTileEntity(REPLICATOR, 365, "replicator", RecipeMaps.REPLICATOR_RECIPES, Textures.REPLICATOR_OVERLAY, true);

        // Fluid Heater, IDs 380-394
        registerSimpleMetaTileEntity(FLUID_HEATER, 380, "fluid_heater", RecipeMaps.FLUID_HEATER_RECIPES, Textures.FLUID_HEATER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Fluid Solidifier, IDs 395-409
        registerSimpleMetaTileEntity(FLUID_SOLIDIFIER, 395, "fluid_solidifier", RecipeMaps.FLUID_SOLIDFICATION_RECIPES, Textures.FLUID_SOLIDIFIER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Forge Hammer, IDs 410-424
        registerSimpleMetaTileEntity(FORGE_HAMMER, 410, "forge_hammer", RecipeMaps.FORGE_HAMMER_RECIPES, Textures.FORGE_HAMMER_OVERLAY, true);

        // Forming Press, IDs 425-439
        registerSimpleMetaTileEntity(FORMING_PRESS, 425, "forming_press", RecipeMaps.FORMING_PRESS_RECIPES, Textures.FORMING_PRESS_OVERLAY, true);

        // Lathe, IDs 440-454
        registerSimpleMetaTileEntity(LATHE, 440, "lathe", RecipeMaps.LATHE_RECIPES, Textures.LATHE_OVERLAY, true);

        // TODO Assembly Line Research system
        // TODO Should anonymously override SimpleMachineMetaTileEntity#getCircuitSlotOverlay() to display the data stick overlay
        // Scanner, IDs 455-469
        //registerSimpleMetaTileEntity(SCANNER, 455, "scanner", RecipeMaps.SCANNER_RECIPES, Textures.SCANNER_OVERLAY, true);

        // Mixer, IDs 470-484
        registerSimpleMetaTileEntity(MIXER, 470, "mixer", RecipeMaps.MIXER_RECIPES, Textures.MIXER_OVERLAY, false, GTUtility.hvCappedTankSizeFunction);

        // Ore Washer, IDs 485-499
        registerSimpleMetaTileEntity(ORE_WASHER, 485, "ore_washer", RecipeMaps.ORE_WASHER_RECIPES, Textures.ORE_WASHER_OVERLAY, true);

        // Packer, IDs 500-514
        registerSimpleMetaTileEntity(PACKER, 500, "packer", RecipeMaps.PACKER_RECIPES, Textures.PACKER_OVERLAY, true);

        // FREE, IDs 515-529

        // Gas Collectors, IDs 530-544
        for (int i = 0; i < GAS_COLLECTOR.length - 1; i++) {
            if (i > 4 && !getMidTier("gas_collector")) continue;
            if (i > 7 && !getHighTier("gas_collector")) break;

            String voltageName = GTValues.VN[i + 1].toLowerCase();
            GAS_COLLECTOR[i] = registerMetaTileEntity(530 + i,
                    new MetaTileEntityGasCollector(gregtechId(String.format("%s.%s", "gas_collector", voltageName)), RecipeMaps.GAS_COLLECTOR_RECIPES, Textures.GAS_COLLECTOR_OVERLAY, i + 1, false, GTUtility.largeTankSizeFunction));
        }
        // Polarizer, IDs 545-559
        registerSimpleMetaTileEntity(POLARIZER, 545, "polarizer", RecipeMaps.POLARIZER_RECIPES, Textures.POLARIZER_OVERLAY, true);

        // Laser Engraver, IDs 560-574
        registerSimpleMetaTileEntity(LASER_ENGRAVER, 560, "laser_engraver", RecipeMaps.LASER_ENGRAVER_RECIPES, Textures.LASER_ENGRAVER_OVERLAY, true);

        // Sifter, IDs 575-589
        registerSimpleMetaTileEntity(SIFTER, 575, "sifter", RecipeMaps.SIFTER_RECIPES, Textures.SIFTER_OVERLAY, true);

        // FREE, IDs 590-604

        // Thermal Centrifuge, IDs 605-619
        registerSimpleMetaTileEntity(THERMAL_CENTRIFUGE, 605, "thermal_centrifuge", RecipeMaps.THERMAL_CENTRIFUGE_RECIPES, Textures.THERMAL_CENTRIFUGE_OVERLAY, true);

        // Wire Mill, IDs 620-634
        registerSimpleMetaTileEntity(WIREMILL, 620, "wiremill", RecipeMaps.WIREMILL_RECIPES, Textures.WIREMILL_OVERLAY, true);

        // Circuit Assembler, IDs 650-664
        registerSimpleMetaTileEntity(CIRCUIT_ASSEMBLER, 635, "circuit_assembler", RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES, Textures.ASSEMBLER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Rock Breaker, IDs 665-679
        ROCK_BREAKER[0] = registerMetaTileEntity(665, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.lv"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 1));
        ROCK_BREAKER[1] = registerMetaTileEntity(666, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.mv"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 2));
        ROCK_BREAKER[2] = registerMetaTileEntity(667, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.hv"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 3));
        ROCK_BREAKER[3] = registerMetaTileEntity(668, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.ev"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 4));
        ROCK_BREAKER[4] = registerMetaTileEntity(669, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.iv"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 5));
        if (getMidTier("rock_breaker")) {
            ROCK_BREAKER[5] = registerMetaTileEntity(670, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.luv"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 6));
            ROCK_BREAKER[6] = registerMetaTileEntity(671, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.zpm"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 7));
            ROCK_BREAKER[7] = registerMetaTileEntity(672, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.uv"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 8));
        }
        if (getHighTier("rock_breaker")) {
            ROCK_BREAKER[8] = registerMetaTileEntity(673, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.uhv"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 9));
            ROCK_BREAKER[9] = registerMetaTileEntity(674, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.uev"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 10));
            ROCK_BREAKER[10] = registerMetaTileEntity(675, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.uiv"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 11));
            ROCK_BREAKER[11] = registerMetaTileEntity(676, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.uxv"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 12));
            ROCK_BREAKER[12] = registerMetaTileEntity(677, new MetaTileEntityRockBreaker(gregtechId("rock_breaker.opv"), RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, 13));
        }

        // Some space here for more SimpleMachines

        // Space left for these just in case

        // Chunk Miner, IDs 920-934

        MINER[0] = registerMetaTileEntity(920, new MetaTileEntityMiner(gregtechId("miner.lv"), 1, 160, 8, 1));
        MINER[1] = registerMetaTileEntity(921, new MetaTileEntityMiner(gregtechId("miner.mv"), 2, 80, 16, 2));
        MINER[2] = registerMetaTileEntity(922, new MetaTileEntityMiner(gregtechId("miner.hv"), 3, 40, 24, 3));

        // Diesel Generator, IDs 935-949
        COMBUSTION_GENERATOR[0] = registerMetaTileEntity(935, new SimpleGeneratorMetaTileEntity(gregtechId("combustion_generator.lv"), RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 1, GTUtility.genericGeneratorTankSizeFunction));
        COMBUSTION_GENERATOR[1] = registerMetaTileEntity(936, new SimpleGeneratorMetaTileEntity(gregtechId("combustion_generator.mv"), RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 2, GTUtility.genericGeneratorTankSizeFunction));
        COMBUSTION_GENERATOR[2] = registerMetaTileEntity(937, new SimpleGeneratorMetaTileEntity(gregtechId("combustion_generator.hv"), RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 3, GTUtility.genericGeneratorTankSizeFunction));

        // Steam Turbine, IDs 950-964
        STEAM_TURBINE[0] = registerMetaTileEntity(950, new SimpleGeneratorMetaTileEntity(gregtechId("steam_turbine.lv"), RecipeMaps.STEAM_TURBINE_FUELS, Textures.STEAM_TURBINE_OVERLAY, 1, GTUtility.steamGeneratorTankSizeFunction));
        STEAM_TURBINE[1] = registerMetaTileEntity(951, new SimpleGeneratorMetaTileEntity(gregtechId("steam_turbine.mv"), RecipeMaps.STEAM_TURBINE_FUELS, Textures.STEAM_TURBINE_OVERLAY, 2, GTUtility.steamGeneratorTankSizeFunction));
        STEAM_TURBINE[2] = registerMetaTileEntity(952, new SimpleGeneratorMetaTileEntity(gregtechId("steam_turbine.hv"), RecipeMaps.STEAM_TURBINE_FUELS, Textures.STEAM_TURBINE_OVERLAY, 3, GTUtility.steamGeneratorTankSizeFunction));

        // Gas Turbine, IDs 965-979
        GAS_TURBINE[0] = registerMetaTileEntity(965, new SimpleGeneratorMetaTileEntity(gregtechId("gas_turbine.lv"), RecipeMaps.GAS_TURBINE_FUELS, Textures.GAS_TURBINE_OVERLAY, 1, GTUtility.genericGeneratorTankSizeFunction));
        GAS_TURBINE[1] = registerMetaTileEntity(966, new SimpleGeneratorMetaTileEntity(gregtechId("gas_turbine.mv"), RecipeMaps.GAS_TURBINE_FUELS, Textures.GAS_TURBINE_OVERLAY, 2, GTUtility.genericGeneratorTankSizeFunction));
        GAS_TURBINE[2] = registerMetaTileEntity(967, new SimpleGeneratorMetaTileEntity(gregtechId("gas_turbine.hv"), RecipeMaps.GAS_TURBINE_FUELS, Textures.GAS_TURBINE_OVERLAY, 3, GTUtility.genericGeneratorTankSizeFunction));

        // Item Collector, IDs 980-983
        ITEM_COLLECTOR[0] = registerMetaTileEntity(980, new MetaTileEntityItemCollector(gregtechId("item_collector.lv"), 1, 8));
        ITEM_COLLECTOR[1] = registerMetaTileEntity(981, new MetaTileEntityItemCollector(gregtechId("item_collector.mv"), 2, 16));
        ITEM_COLLECTOR[2] = registerMetaTileEntity(982, new MetaTileEntityItemCollector(gregtechId("item_collector.hv"), 3, 32));
        ITEM_COLLECTOR[3] = registerMetaTileEntity(983, new MetaTileEntityItemCollector(gregtechId("item_collector.ev"), 4, 64));

        MAGIC_ENERGY_ABSORBER = registerMetaTileEntity(984, new MetaTileEntityMagicEnergyAbsorber(gregtechId("magic_energy_absorber")));

        // Hulls, IDs 985-999
        int endPos = GTValues.HT ? HULL.length : Math.min(HULL.length - 1, GTValues.UV + 2);
        for (int i = 0; i < endPos; i++) {
            HULL[i] = new MetaTileEntityHull(gregtechId("hull." + GTValues.VN[i].toLowerCase()), i);
            registerMetaTileEntity(985 + i, HULL[i]);
        }

        // MULTIBLOCK START: IDs 1000-1149. Space left for addons to register Multiblocks grouped with the rest in JEI
        PRIMITIVE_BLAST_FURNACE = registerMetaTileEntity(1000, new MetaTileEntityPrimitiveBlastFurnace(gregtechId("primitive_blast_furnace.bronze")));
        ELECTRIC_BLAST_FURNACE = registerMetaTileEntity(1001, new MetaTileEntityElectricBlastFurnace(gregtechId("electric_blast_furnace")));
        VACUUM_FREEZER = registerMetaTileEntity(1002, new MetaTileEntityVacuumFreezer(gregtechId("vacuum_freezer")));
        IMPLOSION_COMPRESSOR = registerMetaTileEntity(1003, new MetaTileEntityImplosionCompressor(gregtechId("implosion_compressor")));
        PYROLYSE_OVEN = registerMetaTileEntity(1004, new MetaTileEntityPyrolyseOven(gregtechId("pyrolyse_oven")));
        DISTILLATION_TOWER = registerMetaTileEntity(1005, new MetaTileEntityDistillationTower(gregtechId("distillation_tower")));
        MULTI_FURNACE = registerMetaTileEntity(1006, new MetaTileEntityMultiSmelter(gregtechId("multi_furnace")));
        LARGE_COMBUSTION_ENGINE = registerMetaTileEntity(1007, new MetaTileEntityLargeCombustionEngine(gregtechId("large_combustion_engine"), GTValues.EV));
        EXTREME_COMBUSTION_ENGINE = registerMetaTileEntity(1008, new MetaTileEntityLargeCombustionEngine(gregtechId("extreme_combustion_engine"), GTValues.IV));
        CRACKER = registerMetaTileEntity(1009, new MetaTileEntityCrackingUnit(gregtechId("cracker")));

        LARGE_STEAM_TURBINE = registerMetaTileEntity(1010, new MetaTileEntityLargeTurbine(gregtechId("large_turbine.steam"), RecipeMaps.STEAM_TURBINE_FUELS, 3, MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING), MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX), Textures.SOLID_STEEL_CASING, false, Textures.LARGE_STEAM_TURBINE_OVERLAY));
        LARGE_GAS_TURBINE = registerMetaTileEntity(1011, new MetaTileEntityLargeTurbine(gregtechId("large_turbine.gas"), RecipeMaps.GAS_TURBINE_FUELS, 4, MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_TURBINE_CASING), MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX), Textures.CLEAN_STAINLESS_STEEL_CASING, true, Textures.LARGE_GAS_TURBINE_OVERLAY));
        LARGE_PLASMA_TURBINE = registerMetaTileEntity(1012, new MetaTileEntityLargeTurbine(gregtechId("large_turbine.plasma"), RecipeMaps.PLASMA_GENERATOR_FUELS, 5, MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TUNGSTENSTEEL_TURBINE_CASING), MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TUNGSTENSTEEL_GEARBOX), Textures.ROBUST_TUNGSTENSTEEL_CASING, false, Textures.LARGE_PLASMA_TURBINE_OVERLAY));

        LARGE_BRONZE_BOILER = registerMetaTileEntity(1013, new MetaTileEntityLargeBoiler(gregtechId("large_boiler.bronze"), BoilerType.BRONZE));
        LARGE_STEEL_BOILER = registerMetaTileEntity(1014, new MetaTileEntityLargeBoiler(gregtechId("large_boiler.steel"), BoilerType.STEEL));
        LARGE_TITANIUM_BOILER = registerMetaTileEntity(1015, new MetaTileEntityLargeBoiler(gregtechId("large_boiler.titanium"), BoilerType.TITANIUM));
        LARGE_TUNGSTENSTEEL_BOILER = registerMetaTileEntity(1016, new MetaTileEntityLargeBoiler(gregtechId("large_boiler.tungstensteel"), BoilerType.TUNGSTENSTEEL));

        COKE_OVEN = registerMetaTileEntity(1017, new MetaTileEntityCokeOven(gregtechId("coke_oven")));
        COKE_OVEN_HATCH = registerMetaTileEntity(1018, new MetaTileEntityCokeOvenHatch(gregtechId("coke_oven_hatch")));

        ASSEMBLY_LINE = registerMetaTileEntity(1019, new MetaTileEntityAssemblyLine(gregtechId("assembly_line")));
        FUSION_REACTOR[0] = registerMetaTileEntity(1020, new MetaTileEntityFusionReactor(gregtechId("fusion_reactor.luv"), GTValues.LuV));
        FUSION_REACTOR[1] = registerMetaTileEntity(1021, new MetaTileEntityFusionReactor(gregtechId("fusion_reactor.zpm"), GTValues.ZPM));
        FUSION_REACTOR[2] = registerMetaTileEntity(1022, new MetaTileEntityFusionReactor(gregtechId("fusion_reactor.uv"), GTValues.UV));

        LARGE_CHEMICAL_REACTOR = registerMetaTileEntity(1023, new MetaTileEntityLargeChemicalReactor(gregtechId("large_chemical_reactor")));

        STEAM_OVEN = registerMetaTileEntity(1024, new MetaTileEntitySteamOven(gregtechId("steam_oven")));
        STEAM_GRINDER = registerMetaTileEntity(1025, new MetaTileEntitySteamGrinder(gregtechId("steam_grinder")));

        BASIC_LARGE_MINER = registerMetaTileEntity(1026, new MetaTileEntityLargeMiner(gregtechId("large_miner.ev"), GTValues.EV, 16, 3, 4, Materials.Steel, 8));
        LARGE_MINER = registerMetaTileEntity(1027, new MetaTileEntityLargeMiner(gregtechId("large_miner.iv"), GTValues.IV, 4, 5, 5, Materials.Titanium, 16));
        ADVANCED_LARGE_MINER = registerMetaTileEntity(1028, new MetaTileEntityLargeMiner(gregtechId("large_miner.luv"), GTValues.LuV, 1, 7, 6, Materials.TungstenSteel, 32));

        CENTRAL_MONITOR = registerMetaTileEntity(1029, new MetaTileEntityCentralMonitor(gregtechId("central_monitor")));

        PROCESSING_ARRAY = registerMetaTileEntity(1030, new MetaTileEntityProcessingArray(gregtechId("processing_array"), 0));
        ADVANCED_PROCESSING_ARRAY = registerMetaTileEntity(1031, new MetaTileEntityProcessingArray(gregtechId("advanced_processing_array"), 1));

        BASIC_FLUID_DRILLING_RIG = registerMetaTileEntity(1032, new MetaTileEntityFluidDrill(gregtechId("fluid_drilling_rig.mv"), 2));
        FLUID_DRILLING_RIG = registerMetaTileEntity(1033, new MetaTileEntityFluidDrill(gregtechId("fluid_drilling_rig.hv"), 3));
        ADVANCED_FLUID_DRILLING_RIG = registerMetaTileEntity(1034, new MetaTileEntityFluidDrill(gregtechId("fluid_drilling_rig.ev"), 4));

        CLEANROOM = registerMetaTileEntity(1035, new MetaTileEntityCleanroom(gregtechId("cleanroom")));

        CHARCOAL_PILE = registerMetaTileEntity(1036, new MetaTileEntityCharcoalPile(gregtechId("charcoal_pit")));

        // MISC MTE's START: IDs 1150-2000

        // Import/Export Buses/Hatches, IDs 1150-1209
        for (int i = 0; i < ITEM_IMPORT_BUS.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            ITEM_IMPORT_BUS[i] = new MetaTileEntityItemBus(gregtechId("item_bus.import." + voltageName), i, false);
            ITEM_EXPORT_BUS[i] = new MetaTileEntityItemBus(gregtechId("item_bus.export." + voltageName), i, true);
            FLUID_IMPORT_HATCH[i] = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import." + voltageName), i, false);
            FLUID_EXPORT_HATCH[i] = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.export." + voltageName), i, true);

            registerMetaTileEntity(1150 + i, ITEM_IMPORT_BUS[i]);
            registerMetaTileEntity(1165 + i, ITEM_EXPORT_BUS[i]);
            registerMetaTileEntity(1180 + i, FLUID_IMPORT_HATCH[i]);
            registerMetaTileEntity(1195 + i, FLUID_EXPORT_HATCH[i]);
        }

        // Multi-Fluid Hatches
        MULTI_FLUID_IMPORT_HATCH[0] = registerMetaTileEntity(1190, new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.import_4x"), 2, false));
        MULTI_FLUID_IMPORT_HATCH[1] = registerMetaTileEntity(1191, new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.import_9x"), 3, false));
        MULTI_FLUID_EXPORT_HATCH[0] = registerMetaTileEntity(1205, new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.export_4x"), 2, true));
        MULTI_FLUID_EXPORT_HATCH[1] = registerMetaTileEntity(1206, new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.export_9x"), 3, true));

        // Energy Input/Output Hatches, IDs 1210-1269
        endPos = GTValues.HT ? ENERGY_INPUT_HATCH.length - 1 : Math.min(ENERGY_INPUT_HATCH.length - 1, GTValues.UV + 2);
        for (int i = 0; i < endPos; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            ENERGY_INPUT_HATCH[i] = registerMetaTileEntity(1210 + i, new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.input." + voltageName), i, 2, false));
            ENERGY_OUTPUT_HATCH[i] = registerMetaTileEntity(1225 + i, new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.output." + voltageName), i, 2, true));

            if (i >= GTValues.IV && i <= GTValues.UHV) {
                ENERGY_INPUT_HATCH_4A[i + 1 - GTValues.IV]   = registerMetaTileEntity(1240 + i - GTValues.IV, new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.input_4a." + voltageName), i, 4, false));
                ENERGY_INPUT_HATCH_16A[i - GTValues.IV]  = registerMetaTileEntity(1245 + i - GTValues.IV, new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.input_16a." + voltageName), i, 16, false));
                ENERGY_OUTPUT_HATCH_4A[i + 1 - GTValues.IV]  = registerMetaTileEntity(1250 + i - GTValues.IV, new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.output_4a." + voltageName), i, 4, true));
                ENERGY_OUTPUT_HATCH_16A[i - GTValues.IV] = registerMetaTileEntity(1255 + i - GTValues.IV, new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.output_16a." + voltageName), i, 16, true));
            }
        }
        ENERGY_INPUT_HATCH_4A[0] = registerMetaTileEntity(1399, new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.input_4a.ev"), GTValues.EV, 4, false));
        ENERGY_OUTPUT_HATCH_4A[0] = registerMetaTileEntity(1400, new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.output_4a.ev"), GTValues.EV, 4, true));


        // Transformer, IDs 1270-1299
        endPos = GTValues.HT ? TRANSFORMER.length - 1 : Math.min(TRANSFORMER.length - 1, GTValues.UV);
        for (int i = 0; i <= endPos; i++) {
            MetaTileEntityTransformer transformer = new MetaTileEntityTransformer(gregtechId("transformer." + GTValues.VN[i].toLowerCase()), i);
            TRANSFORMER[i] = registerMetaTileEntity(1270 + (i), transformer);
            MetaTileEntityAdjustableTransformer adjustableTransformer = new MetaTileEntityAdjustableTransformer(gregtechId("transformer.adjustable." + GTValues.VN[i].toLowerCase()), i);
            ADJUSTABLE_TRANSFORMER[i] = registerMetaTileEntity(1285 + (i), adjustableTransformer);
        }

        // Diode, IDs 1300-1314
        endPos = GTValues.HT ? DIODES.length - 1 : Math.min(DIODES.length - 1, GTValues.UV + 2);
        for (int i = 0; i < endPos; i++) {
            String diodeId = "diode." + GTValues.VN[i].toLowerCase();
            MetaTileEntityDiode diode = new MetaTileEntityDiode(gregtechId(diodeId), i);
            DIODES[i] = registerMetaTileEntity(1300 + i, diode);
        }

        // Battery Buffer, IDs 1315-1360
        endPos = GTValues.HT ? BATTERY_BUFFER[0].length - 1 : Math.min(BATTERY_BUFFER[0].length - 1, GTValues.UHV + 1);
        int[] batteryBufferSlots = new int[]{4, 8, 16};
        for (int slot = 0; slot < batteryBufferSlots.length; slot++) {
            BATTERY_BUFFER[slot] = new MetaTileEntityBatteryBuffer[endPos];
            for (int i = 0; i < endPos; i++) {
                String bufferId = "battery_buffer." + GTValues.VN[i].toLowerCase() + "." + batteryBufferSlots[slot];
                MetaTileEntityBatteryBuffer batteryBuffer = new MetaTileEntityBatteryBuffer(gregtechId(bufferId), i, batteryBufferSlots[slot]);
                BATTERY_BUFFER[slot][i] = registerMetaTileEntity(1315 + BATTERY_BUFFER[slot].length * slot + i, batteryBuffer);
            }
        }

        // Charger, IDs 1375-1389
        endPos = GTValues.HT ? CHARGER.length - 1 : Math.min(CHARGER.length - 1, GTValues.UHV + 1);
        for (int i = 0; i < endPos; i++) {
            String chargerId = "charger." + GTValues.VN[i].toLowerCase();
            MetaTileEntityCharger charger = new MetaTileEntityCharger(gregtechId(chargerId), i, 4);
            CHARGER[i] = registerMetaTileEntity(1375 + i, charger);
        }

        // World Accelerators, IDs 1390-1404
        if (ConfigHolder.machines.enableWorldAccelerators) {
            WORLD_ACCELERATOR[0] = registerMetaTileEntity(1390, new MetaTileEntityWorldAccelerator(gregtechId("world_accelerator.lv"), 1));
            WORLD_ACCELERATOR[1] = registerMetaTileEntity(1391, new MetaTileEntityWorldAccelerator(gregtechId("world_accelerator.mv"), 2));
            WORLD_ACCELERATOR[2] = registerMetaTileEntity(1392, new MetaTileEntityWorldAccelerator(gregtechId("world_accelerator.hv"), 3));
            WORLD_ACCELERATOR[3] = registerMetaTileEntity(1393, new MetaTileEntityWorldAccelerator(gregtechId("world_accelerator.ev"), 4));
            WORLD_ACCELERATOR[4] = registerMetaTileEntity(1394, new MetaTileEntityWorldAccelerator(gregtechId("world_accelerator.iv"), 5));
            WORLD_ACCELERATOR[5] = registerMetaTileEntity(1395, new MetaTileEntityWorldAccelerator(gregtechId("world_accelerator.luv"), 6));
            WORLD_ACCELERATOR[6] = registerMetaTileEntity(1396, new MetaTileEntityWorldAccelerator(gregtechId("world_accelerator.zpm"), 7));
            WORLD_ACCELERATOR[7] = registerMetaTileEntity(1397, new MetaTileEntityWorldAccelerator(gregtechId("world_accelerator.uv"), 8));
        }

        MACHINE_HATCH = registerMetaTileEntity(1398, new MetaTileEntityMachineHatch(gregtechId("machine_hatch"), 5));

        // 1399 and 1400 are taken by the EV 4A hatches, and are grouped near the other registration rather than here
        // 1401 is taken by the Cleanroom Maintenance hatches, and is grouped with the maintenance hatch registrtation rather than here

        PASSTHROUGH_HATCH_ITEM = registerMetaTileEntity(1402, new MetaTileEntityPassthroughHatchItem(gregtechId("passthrough_hatch_item"), 3));
        PASSTHROUGH_HATCH_FLUID = registerMetaTileEntity(1403, new MetaTileEntityPassthroughHatchFluid(gregtechId("passthrough_hatch_fluid"), 3));
        // Free Range: 1404-1509

        // Buffers, IDs 1510-1512
        BUFFER[0] = registerMetaTileEntity(1510, new MetaTileEntityBuffer(gregtechId("buffer.lv"), 1));
        BUFFER[1] = registerMetaTileEntity(1511, new MetaTileEntityBuffer(gregtechId("buffer.mv"), 2));
        BUFFER[2] = registerMetaTileEntity(1512, new MetaTileEntityBuffer(gregtechId("buffer.hv"), 3));

        // Free Range: 1513-1514

        // Fishers, IDs 1515-1529
        FISHER[0] = registerMetaTileEntity(1515, new MetaTileEntityFisher(gregtechId("fisher.lv"), 1));
        FISHER[1] = registerMetaTileEntity(1516, new MetaTileEntityFisher(gregtechId("fisher.mv"), 2));
        FISHER[2] = registerMetaTileEntity(1517, new MetaTileEntityFisher(gregtechId("fisher.hv"), 3));
        FISHER[3] = registerMetaTileEntity(1518, new MetaTileEntityFisher(gregtechId("fisher.ev"), 4));

        // Pumps, IDs 1530-1544
        PUMP[0] = registerMetaTileEntity(1530, new MetaTileEntityPump(gregtechId("pump.lv"), 1));
        PUMP[1] = registerMetaTileEntity(1531, new MetaTileEntityPump(gregtechId("pump.mv"), 2));
        PUMP[2] = registerMetaTileEntity(1532, new MetaTileEntityPump(gregtechId("pump.hv"), 3));
        PUMP[3] = registerMetaTileEntity(1533, new MetaTileEntityPump(gregtechId("pump.ev"), 4));

        // Super / Quantum Chests, IDs 1560-1574
        for (int i = 0; i < 5; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            QUANTUM_CHEST[i] = new MetaTileEntityQuantumChest(gregtechId("super_chest." + voltageName), i + 1, 4000000L * (int) Math.pow(2, i));
            registerMetaTileEntity(1560 + i, QUANTUM_CHEST[i]);
        }

        for (int i = 5; i < QUANTUM_CHEST.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            long capacity = i == GTValues.UHV ? Integer.MAX_VALUE : 4000000L * (int) Math.pow(2, i);
            QUANTUM_CHEST[i] = new MetaTileEntityQuantumChest(gregtechId("quantum_chest." + voltageName), i, capacity);
            registerMetaTileEntity(1565 + i, QUANTUM_CHEST[i]);
        }

        // Super / Quantum Tanks, IDs 1575-1589
        for (int i = 0; i < 5; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            QUANTUM_TANK[i] = new MetaTileEntityQuantumTank(gregtechId("super_tank." + voltageName), i + 1, 4000000 * (int) Math.pow(2, i));
            registerMetaTileEntity(1575 + i, QUANTUM_TANK[i]);
        }

        for (int i = 5; i < QUANTUM_TANK.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            int capacity = i == GTValues.UHV ? Integer.MAX_VALUE : 4000000 * (int) Math.pow(2, i);
            QUANTUM_TANK[i] = new MetaTileEntityQuantumTank(gregtechId("quantum_tank." + voltageName), i, capacity);
            registerMetaTileEntity(1580 + i, QUANTUM_TANK[i]);
        }

        // Block Breakers, IDs 1590-1594
        for (int i = 0; i < BLOCK_BREAKER.length; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            BLOCK_BREAKER[i] = new MetaTileEntityBlockBreaker(gregtechId("block_breaker." + voltageName), i + 1);
            registerMetaTileEntity(1590 + i, BLOCK_BREAKER[i]);
        }

        // Tanks, IDs 1595-1609
        WOODEN_TANK_VALVE = registerMetaTileEntity(1596, new MetaTileEntityTankValve(gregtechId("tank_valve.wood"), false));
        WOODEN_TANK = registerMetaTileEntity(1597, new MetaTileEntityMultiblockTank(gregtechId("tank.wood"), false, 250 * 1000));

        STEEL_TANK_VALVE = registerMetaTileEntity(1598, new MetaTileEntityTankValve(gregtechId("tank_valve.steel"), true));
        STEEL_TANK = registerMetaTileEntity(1599, new MetaTileEntityMultiblockTank(gregtechId("tank.steel"), true, 1000 * 1000));

        // Drums, IDs 1610-1624
        WOODEN_DRUM = registerMetaTileEntity(1610, new MetaTileEntityDrum(gregtechId("drum.wood"), Materials.Wood, 16000));
        BRONZE_DRUM = registerMetaTileEntity(1611, new MetaTileEntityDrum(gregtechId("drum.bronze"), Materials.Bronze, 32000));
        STEEL_DRUM = registerMetaTileEntity(1612, new MetaTileEntityDrum(gregtechId("drum.steel"), Materials.Steel, 64000));
        ALUMINIUM_DRUM = registerMetaTileEntity(1613, new MetaTileEntityDrum(gregtechId("drum.aluminium"), Materials.Aluminium, 128000));
        STAINLESS_STEEL_DRUM = registerMetaTileEntity(1614, new MetaTileEntityDrum(gregtechId("drum.stainless_steel"), Materials.StainlessSteel, 256000));

        // Crates, IDs 1625-1639
        WOODEN_CRATE = registerMetaTileEntity(1625, new MetaTileEntityCrate(gregtechId("crate.wood"), Materials.Wood, 27));
        BRONZE_CRATE = registerMetaTileEntity(1626, new MetaTileEntityCrate(gregtechId("crate.bronze"), Materials.Bronze, 54));
        STEEL_CRATE = registerMetaTileEntity(1627, new MetaTileEntityCrate(gregtechId("crate.steel"), Materials.Steel, 72));
        ALUMINIUM_CRATE = registerMetaTileEntity(1628, new MetaTileEntityCrate(gregtechId("crate.aluminium"), Materials.Aluminium, 90));
        STAINLESS_STEEL_CRATE = registerMetaTileEntity(1629, new MetaTileEntityCrate(gregtechId("crate.stainless_steel"), Materials.StainlessSteel, 108));
        TITANIUM_CRATE = registerMetaTileEntity(1630, new MetaTileEntityCrate(gregtechId("crate.titanium"), Materials.Titanium, 126));
        TUNGSTENSTEEL_CRATE = registerMetaTileEntity(1631, new MetaTileEntityCrate(gregtechId("crate.tungstensteel"), Materials.TungstenSteel, 144));

        // Rotor Holder, IDs 1640-1645
        ROTOR_HOLDER[0] = registerMetaTileEntity(1640, new MetaTileEntityRotorHolder(gregtechId("rotor_holder.hv"), GTValues.HV));
        ROTOR_HOLDER[1] = registerMetaTileEntity(1641, new MetaTileEntityRotorHolder(gregtechId("rotor_holder.ev"), GTValues.EV));
        ROTOR_HOLDER[2] = registerMetaTileEntity(1642, new MetaTileEntityRotorHolder(gregtechId("rotor_holder.iv"), GTValues.IV));
        ROTOR_HOLDER[3] = registerMetaTileEntity(1643, new MetaTileEntityRotorHolder(gregtechId("rotor_holder.luv"), GTValues.LuV));
        ROTOR_HOLDER[4] = registerMetaTileEntity(1644, new MetaTileEntityRotorHolder(gregtechId("rotor_holder.zpm"), GTValues.ZPM));
        ROTOR_HOLDER[5] = registerMetaTileEntity(1645, new MetaTileEntityRotorHolder(gregtechId("rotor_holder.uv"), GTValues.UV));

        // Misc, IDs 1646-1999
        LOCKED_SAFE = registerMetaTileEntity(1646, new MetaTileEntityLockedSafe(gregtechId("locked_safe")));
        WORKBENCH = registerMetaTileEntity(1647, new MetaTileEntityWorkbench(gregtechId("workbench")));
        PRIMITIVE_WATER_PUMP = registerMetaTileEntity(1648, new MetaTileEntityPrimitiveWaterPump(gregtechId("primitive_water_pump")));
        PUMP_OUTPUT_HATCH = registerMetaTileEntity(1649, new MetaTileEntityPumpHatch(gregtechId("pump_hatch")));

        CREATIVE_ENERGY = registerMetaTileEntity(1650, new MetaTileEntityCreativeEnergy());
        // Steam Hatches/Buses
        STEAM_EXPORT_BUS = registerMetaTileEntity(1651, new MetaTileEntitySteamItemBus(gregtechId("steam_export_bus"), true));
        STEAM_IMPORT_BUS = registerMetaTileEntity(1652, new MetaTileEntitySteamItemBus(gregtechId("steam_import_bus"), false));
        STEAM_HATCH = registerMetaTileEntity(1653, new MetaTileEntitySteamHatch(gregtechId("steam_hatch")));

        // Maintenance Hatches, IDs 1654-1656
        MAINTENANCE_HATCH = registerMetaTileEntity(1654, new MetaTileEntityMaintenanceHatch(gregtechId("maintenance_hatch"), false));
        CONFIGURABLE_MAINTENANCE_HATCH = registerMetaTileEntity(1655, new MetaTileEntityMaintenanceHatch(gregtechId("maintenance_hatch_configurable"), true));
        AUTO_MAINTENANCE_HATCH = registerMetaTileEntity(1656, new MetaTileEntityAutoMaintenanceHatch(gregtechId("maintenance_hatch_full_auto")));
        CLEANING_MAINTENANCE_HATCH = registerMetaTileEntity(1401, new MetaTileEntityCleaningMaintenanceHatch(gregtechId("maintenance_hatch_cleanroom_auto")));

        // Muffler Hatches, IDs 1657-
        for (int i = 0; i < MUFFLER_HATCH.length; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            MUFFLER_HATCH[i] = new MetaTileEntityMufflerHatch(gregtechId("muffler_hatch." + voltageName), i + 1);

            registerMetaTileEntity(1657 + i, MUFFLER_HATCH[i]);
        }

        CLIPBOARD_TILE = registerMetaTileEntity(1666, new MetaTileEntityClipboard(gregtechId("clipboard")));

        MONITOR_SCREEN = registerMetaTileEntity(1667, new MetaTileEntityMonitorScreen(gregtechId("monitor_screen")));

        // Creative Chest and Tank, IDs 1668-1669
        CREATIVE_CHEST = registerMetaTileEntity(1668, new MetaTileEntityCreativeChest(gregtechId("creative_chest")));
        CREATIVE_TANK = registerMetaTileEntity(1669, new MetaTileEntityCreativeTank(gregtechId("creative_tank")));

        // Energy Converter, IDs 1670-1729
        endPos = GTValues.HT ? ENERGY_CONVERTER[0].length - 1 : Math.min(ENERGY_CONVERTER[0].length - 1, GTValues.UHV + 1);
        int[] amps = {1, 4, 8, 16};
        for(int i = 0; i < endPos; i++) {
            for(int j = 0; j < 4; j++) {
                String id = "energy_converter." + GTValues.VN[i].toLowerCase() + "." + amps[j];
                MetaTileEntityConverter converter = new MetaTileEntityConverter(gregtechId(id), i, amps[j]);
                ENERGY_CONVERTER[j][i] = registerMetaTileEntity(1670 + j + i * 4, converter);
            }
        }

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
         * - HtmlTech: 9000-9499
         * - PCM's Ore Addon: 9500-9999
         * - GCM: 10000-10099
         * - MechTech: 10100-10499
         * - MBT 10500 - 10999
         * - CT(MBT) 32000 - ~
         * - FREE RANGE 11000-32767
         */
    }

    private static void registerSimpleMetaTileEntity(SimpleMachineMetaTileEntity[] machines,
                                                    int startId,
                                                    String name,
                                                    RecipeMap<?> map,
                                                     ICubeRenderer texture,
                                                    boolean hasFrontFacing,
                                                    Function<Integer, Integer> tankScalingFunction) {
        registerSimpleMetaTileEntity(machines, startId, name, map, texture, hasFrontFacing, MetaTileEntities::gregtechId, tankScalingFunction);
    }

    private static void registerSimpleMetaTileEntity(SimpleMachineMetaTileEntity[] machines,
                                                    int startId,
                                                    String name,
                                                    RecipeMap<?> map,
                                                     ICubeRenderer texture,
                                                    boolean hasFrontFacing) {
        registerSimpleMetaTileEntity(machines, startId, name, map, texture, hasFrontFacing, GTUtility.defaultTankSizeFunction);
    }

    public static void registerSimpleMetaTileEntity(SimpleMachineMetaTileEntity[] machines,
                                                    int startId,
                                                    String name,
                                                    RecipeMap<?> map,
                                                    ICubeRenderer texture,
                                                    boolean hasFrontFacing,
                                                    Function<String, ResourceLocation> resourceId,
                                                    Function<Integer, Integer> tankScalingFunction) {
        for (int i = 0; i < machines.length - 1; i++) {
            if (i > 4 && !getMidTier(name)) continue;
            if (i > 7 && !getHighTier(name)) break;

            String voltageName = GTValues.VN[i + 1].toLowerCase();
            machines[i + 1] = registerMetaTileEntity(startId + i,
                    new SimpleMachineMetaTileEntity(resourceId.apply(String.format("%s.%s", name, voltageName)), map, texture, i + 1, hasFrontFacing, tankScalingFunction));
        }
    }

    public static <T extends MetaTileEntity> T registerMetaTileEntity(int id, T sampleMetaTileEntity) {
        if (sampleMetaTileEntity instanceof IMultiblockAbilityPart) {
            IMultiblockAbilityPart<?> abilityPart = (IMultiblockAbilityPart<?>) sampleMetaTileEntity;
            MultiblockAbility.registerMultiblockAbility(abilityPart.getAbility(), sampleMetaTileEntity);
        }
        if (sampleMetaTileEntity instanceof MultiblockControllerBase && Loader.isModLoaded(GTValues.MODID_JEI)) {
            if (((MultiblockControllerBase) sampleMetaTileEntity).shouldShowInJei()) {
                MultiblockInfoCategory.registerMultiblock((MultiblockControllerBase) sampleMetaTileEntity);
            }
        }
        GregTechAPI.MTE_REGISTRY.register(id, sampleMetaTileEntity.metaTileEntityId, sampleMetaTileEntity);
        return sampleMetaTileEntity;
    }

    private static ResourceLocation gregtechId(String name) {
        return new ResourceLocation(GTValues.MODID, name);
    }

    @SuppressWarnings("unused")
    public static void setMidTier(String key, boolean enabled) {
        MID_TIER.put(key, enabled);
    }

    @SuppressWarnings("unused")
    public static void setHighTier(String key, boolean enabled) {
        HIGH_TIER.put(key, enabled);
        GTValues.HT = enabled || HIGH_TIER.containsValue(true);
    }

    public static boolean getMidTier(String key) {
        return MID_TIER.getOrDefault(key, true);
    }

    public static boolean getHighTier(String key) {
        return HIGH_TIER.getOrDefault(key, GTValues.HT);
    }
}
