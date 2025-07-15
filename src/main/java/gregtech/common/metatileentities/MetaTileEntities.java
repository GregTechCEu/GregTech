package gregtech.common.metatileentities;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.FeCompat;
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
import gregtech.api.util.Mods;
import gregtech.client.particle.VanillaParticleEffects;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.converter.MetaTileEntityConverter;
import gregtech.common.metatileentities.electric.MetaTileEntityAlarm;
import gregtech.common.metatileentities.electric.MetaTileEntityBatteryBuffer;
import gregtech.common.metatileentities.electric.MetaTileEntityBlockBreaker;
import gregtech.common.metatileentities.electric.MetaTileEntityCharger;
import gregtech.common.metatileentities.electric.MetaTileEntityDiode;
import gregtech.common.metatileentities.electric.MetaTileEntityFisher;
import gregtech.common.metatileentities.electric.MetaTileEntityGasCollector;
import gregtech.common.metatileentities.electric.MetaTileEntityHull;
import gregtech.common.metatileentities.electric.MetaTileEntityItemCollector;
import gregtech.common.metatileentities.electric.MetaTileEntityMagicEnergyAbsorber;
import gregtech.common.metatileentities.electric.MetaTileEntityMiner;
import gregtech.common.metatileentities.electric.MetaTileEntityPump;
import gregtech.common.metatileentities.electric.MetaTileEntityRockBreaker;
import gregtech.common.metatileentities.electric.MetaTileEntitySingleCombustion;
import gregtech.common.metatileentities.electric.MetaTileEntitySingleTurbine;
import gregtech.common.metatileentities.electric.MetaTileEntityTransformer;
import gregtech.common.metatileentities.electric.MetaTileEntityWorldAccelerator;
import gregtech.common.metatileentities.electric.SimpleMachineMetaTileEntityResizable;
import gregtech.common.metatileentities.multi.BoilerType;
import gregtech.common.metatileentities.multi.MetaTileEntityCokeOven;
import gregtech.common.metatileentities.multi.MetaTileEntityCokeOvenHatch;
import gregtech.common.metatileentities.multi.MetaTileEntityLargeBoiler;
import gregtech.common.metatileentities.multi.MetaTileEntityMultiblockTank;
import gregtech.common.metatileentities.multi.MetaTileEntityPrimitiveBlastFurnace;
import gregtech.common.metatileentities.multi.MetaTileEntityPrimitiveWaterPump;
import gregtech.common.metatileentities.multi.MetaTileEntityPumpHatch;
import gregtech.common.metatileentities.multi.MetaTileEntityTankValve;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityActiveTransformer;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityAssemblyLine;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityCleanroom;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityCrackingUnit;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDataBank;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDistillationTower;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityElectricBlastFurnace;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityFluidDrill;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityFusionReactor;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityHPCA;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityImplosionCompressor;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityLargeChemicalReactor;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityLargeMiner;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityMultiSmelter;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityNetworkSwitch;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityPowerSubstation;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityProcessingArray;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityPyrolyseOven;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityResearchStation;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityVacuumFreezer;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityCentralMonitor;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeCombustionEngine;
import gregtech.common.metatileentities.multi.electric.generator.MetaTileEntityLargeTurbine;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityAutoMaintenanceHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityCleaningMaintenanceHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityComputationHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityDataAccessHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityEnergyHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityLaserHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMachineHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMaintenanceHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMufflerHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityObjectHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityOpticalDataHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityPassthroughHatchComputation;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityPassthroughHatchFluid;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityPassthroughHatchItem;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityPassthroughHatchLaser;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityReservoirHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityRotorHolder;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntitySterileCleaningMaintenanceHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntitySubstationEnergyHatch;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEInputBus;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEInputHatch;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEOutputBus;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEOutputHatch;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEStockingBus;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.MetaTileEntityMEStockingHatch;
import gregtech.common.metatileentities.multi.multiblockpart.hpca.MetaTileEntityHPCAAdvancedComputation;
import gregtech.common.metatileentities.multi.multiblockpart.hpca.MetaTileEntityHPCAAdvancedCooler;
import gregtech.common.metatileentities.multi.multiblockpart.hpca.MetaTileEntityHPCABridge;
import gregtech.common.metatileentities.multi.multiblockpart.hpca.MetaTileEntityHPCAComputation;
import gregtech.common.metatileentities.multi.multiblockpart.hpca.MetaTileEntityHPCACooler;
import gregtech.common.metatileentities.multi.multiblockpart.hpca.MetaTileEntityHPCAEmpty;
import gregtech.common.metatileentities.multi.steam.MetaTileEntitySteamGrinder;
import gregtech.common.metatileentities.multi.steam.MetaTileEntitySteamOven;
import gregtech.common.metatileentities.primitive.MetaTileEntityCharcoalPileIgniter;
import gregtech.common.metatileentities.steam.SteamAlloySmelter;
import gregtech.common.metatileentities.steam.SteamCompressor;
import gregtech.common.metatileentities.steam.SteamExtractor;
import gregtech.common.metatileentities.steam.SteamFurnace;
import gregtech.common.metatileentities.steam.SteamHammer;
import gregtech.common.metatileentities.steam.SteamMacerator;
import gregtech.common.metatileentities.steam.SteamMiner;
import gregtech.common.metatileentities.steam.SteamRockBreaker;
import gregtech.common.metatileentities.steam.boiler.SteamCoalBoiler;
import gregtech.common.metatileentities.steam.boiler.SteamLavaBoiler;
import gregtech.common.metatileentities.steam.boiler.SteamSolarBoiler;
import gregtech.common.metatileentities.steam.multiblockpart.MetaTileEntitySteamHatch;
import gregtech.common.metatileentities.steam.multiblockpart.MetaTileEntitySteamItemBus;
import gregtech.common.metatileentities.storage.MetaTileEntityBuffer;
import gregtech.common.metatileentities.storage.MetaTileEntityCrate;
import gregtech.common.metatileentities.storage.MetaTileEntityCreativeChest;
import gregtech.common.metatileentities.storage.MetaTileEntityCreativeEnergy;
import gregtech.common.metatileentities.storage.MetaTileEntityCreativeTank;
import gregtech.common.metatileentities.storage.MetaTileEntityDrum;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumChest;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumExtender;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumProxy;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumStorageController;
import gregtech.common.metatileentities.storage.MetaTileEntityQuantumTank;
import gregtech.common.metatileentities.storage.MetaTileEntityWorkbench;
import gregtech.common.pipelike.fluidpipe.longdistance.MetaTileEntityLDFluidEndpoint;
import gregtech.common.pipelike.itempipe.longdistance.MetaTileEntityLDItemEndpoint;
import gregtech.integration.jei.multiblock.MultiblockInfoCategory;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static gregtech.api.util.GTUtility.gregtechId;

public class MetaTileEntities {

    // spotless:off

    // HULLS
    public static final MetaTileEntityHull[] HULL = new MetaTileEntityHull[GTValues.V.length];
    public static final MetaTileEntityTransformer[] TRANSFORMER = new MetaTileEntityTransformer[GTValues.V.length -
            1]; // no MAX
    public static final MetaTileEntityTransformer[] HI_AMP_TRANSFORMER = new MetaTileEntityTransformer[
            GTValues.V.length - 1]; /// no MAX
    public static final MetaTileEntityTransformer[] POWER_TRANSFORMER = new MetaTileEntityTransformer[
            GTValues.V.length - 1]; // no MAX
    public static final MetaTileEntityDiode[] DIODES = new MetaTileEntityDiode[GTValues.V.length];
    public static final MetaTileEntityBatteryBuffer[][] BATTERY_BUFFER = new MetaTileEntityBatteryBuffer[3][GTValues.V.length];
    public static final MetaTileEntityCharger[] CHARGER = new MetaTileEntityCharger[GTValues.V.length];

    // SIMPLE MACHINES SECTION
    public static final SimpleMachineMetaTileEntity[] ELECTRIC_FURNACE = new SimpleMachineMetaTileEntity[
            GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] MACERATOR = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] ALLOY_SMELTER = new SimpleMachineMetaTileEntity[
            GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] ARC_FURNACE = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] ASSEMBLER = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] AUTOCLAVE = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] BENDER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] BREWERY = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] CANNER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] CENTRIFUGE = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] CHEMICAL_BATH = new SimpleMachineMetaTileEntity[
            GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] CHEMICAL_REACTOR = new SimpleMachineMetaTileEntity[
            GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] COMPRESSOR = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] CUTTER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] DISTILLERY = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] ELECTROLYZER = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] ELECTROMAGNETIC_SEPARATOR = new SimpleMachineMetaTileEntity[
            GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] EXTRACTOR = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] EXTRUDER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] FERMENTER = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] FLUID_HEATER = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] FLUID_SOLIDIFIER = new SimpleMachineMetaTileEntity[
            GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] FORGE_HAMMER = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] FORMING_PRESS = new SimpleMachineMetaTileEntity[
            GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] LATHE = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] MIXER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] ORE_WASHER = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] PACKER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] UNPACKER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] POLARIZER = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] LASER_ENGRAVER = new SimpleMachineMetaTileEntity[
            GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] SIFTER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] THERMAL_CENTRIFUGE = new SimpleMachineMetaTileEntity[
            GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] WIREMILL = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] CIRCUIT_ASSEMBLER = new SimpleMachineMetaTileEntity[
            GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] MASS_FABRICATOR = new SimpleMachineMetaTileEntity[
            GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] REPLICATOR = new SimpleMachineMetaTileEntity[GTValues.V.length -
            1];
    public static final SimpleMachineMetaTileEntity[] SCANNER = new SimpleMachineMetaTileEntity[GTValues.V.length - 1];
    public static final SimpleMachineMetaTileEntity[] GAS_COLLECTOR = new MetaTileEntityGasCollector[GTValues.V.length -
            1];
    public static final MetaTileEntityRockBreaker[] ROCK_BREAKER = new MetaTileEntityRockBreaker[GTValues.V.length - 1];
    public static final MetaTileEntityMiner[] MINER = new MetaTileEntityMiner[GTValues.V.length - 1];

    // GENERATORS SECTION
    public static final SimpleGeneratorMetaTileEntity[] COMBUSTION_GENERATOR = new SimpleGeneratorMetaTileEntity[5];
    public static final SimpleGeneratorMetaTileEntity[] STEAM_TURBINE = new SimpleGeneratorMetaTileEntity[5];
    public static final SimpleGeneratorMetaTileEntity[] GAS_TURBINE = new SimpleGeneratorMetaTileEntity[5];

    // MULTIBLOCK PARTS SECTION
    public static final MetaTileEntityItemBus[] ITEM_IMPORT_BUS = new MetaTileEntityItemBus[GTValues.V.length -
            1]; // All tiers but MAX
    public static final MetaTileEntityItemBus[] ITEM_EXPORT_BUS = new MetaTileEntityItemBus[GTValues.V.length - 1];
    public static final MetaTileEntityFluidHatch[] FLUID_IMPORT_HATCH = new MetaTileEntityFluidHatch[GTValues.V.length -
            1];
    public static final MetaTileEntityFluidHatch[] FLUID_EXPORT_HATCH = new MetaTileEntityFluidHatch[GTValues.V.length -
            1];
    public static final MetaTileEntityMultiFluidHatch[] QUADRUPLE_IMPORT_HATCH = new MetaTileEntityMultiFluidHatch[GTValues.V.length]; // EV+
    public static final MetaTileEntityMultiFluidHatch[] NONUPLE_IMPORT_HATCH = new MetaTileEntityMultiFluidHatch[GTValues.V.length]; // EV+
    public static final MetaTileEntityMultiFluidHatch[] SIXTEEN_IMPORT_HATCH = new MetaTileEntityMultiFluidHatch[GTValues.V.length];
    public static final MetaTileEntityMultiFluidHatch[] QUADRUPLE_EXPORT_HATCH = new MetaTileEntityMultiFluidHatch[GTValues.V.length]; // EV+
    public static final MetaTileEntityMultiFluidHatch[] NONUPLE_EXPORT_HATCH = new MetaTileEntityMultiFluidHatch[GTValues.V.length]; // EV+
    public static final MetaTileEntityMultiFluidHatch[] SIXTEEN_EXPORT_HATCH = new MetaTileEntityMultiFluidHatch[GTValues.V.length];
    public static final MetaTileEntityEnergyHatch[] ENERGY_INPUT_HATCH = new MetaTileEntityEnergyHatch[GTValues.V.length];
    public static final MetaTileEntityEnergyHatch[] ENERGY_INPUT_HATCH_4A = new MetaTileEntityEnergyHatch[GTValues.V.length];
    public static final MetaTileEntityEnergyHatch[] ENERGY_INPUT_HATCH_16A = new MetaTileEntityEnergyHatch[GTValues.V.length];
    public static final MetaTileEntityEnergyHatch[] ENERGY_OUTPUT_HATCH = new MetaTileEntityEnergyHatch[GTValues.V.length];
    public static final MetaTileEntityEnergyHatch[] ENERGY_OUTPUT_HATCH_4A = new MetaTileEntityEnergyHatch[GTValues.V.length];
    public static final MetaTileEntityEnergyHatch[] ENERGY_OUTPUT_HATCH_16A = new MetaTileEntityEnergyHatch[GTValues.V.length];
    public static final MetaTileEntitySubstationEnergyHatch[] SUBSTATION_ENERGY_INPUT_HATCH = new MetaTileEntitySubstationEnergyHatch[GTValues.V.length];
    public static final MetaTileEntitySubstationEnergyHatch[] SUBSTATION_ENERGY_OUTPUT_HATCH = new MetaTileEntitySubstationEnergyHatch[GTValues.V.length];
    public static final MetaTileEntityRotorHolder[] ROTOR_HOLDER = new MetaTileEntityRotorHolder[12]; // HV, EV, IV, LuV, ZPM, UV, UHV, UEV, UIV, UXV, OPV,MAX
    public static final MetaTileEntityMufflerHatch[] MUFFLER_HATCH = new MetaTileEntityMufflerHatch[GTValues.UHV +
            1]; // LV-UHV
    public static final MetaTileEntityFusionReactor[] FUSION_REACTOR = new MetaTileEntityFusionReactor[3];
    public static final MetaTileEntityQuantumChest[] QUANTUM_CHEST = new MetaTileEntityQuantumChest[10];
    public static final MetaTileEntityQuantumTank[] QUANTUM_TANK = new MetaTileEntityQuantumTank[10];
    public static final MetaTileEntityBuffer[] BUFFER = new MetaTileEntityBuffer[5];
    public static final MetaTileEntityPump[] PUMP = new MetaTileEntityPump[8];
    public static final MetaTileEntityBlockBreaker[] BLOCK_BREAKER = new MetaTileEntityBlockBreaker[4];
    public static final MetaTileEntityItemCollector[] ITEM_COLLECTOR = new MetaTileEntityItemCollector[4];
    public static final MetaTileEntityFisher[] FISHER = new MetaTileEntityFisher[4];
    public static final MetaTileEntityWorldAccelerator[] WORLD_ACCELERATOR = new MetaTileEntityWorldAccelerator[9]; // LV-UV
    // Used for addons if they wish to disable certain tiers of machines
    private static final Map<String, Boolean> MID_TIER = new HashMap<>();
    private static final Map<String, Boolean> HIGH_TIER = new HashMap<>();
    public static MetaTileEntityQuantumStorageController QUANTUM_STORAGE_CONTROLLER;
    public static MetaTileEntityQuantumProxy QUANTUM_STORAGE_PROXY;
    public static MetaTileEntityQuantumExtender QUANTUM_STORAGE_EXTENDER;
    public static MetaTileEntityMachineHatch MACHINE_HATCH;
    public static MetaTileEntityPassthroughHatchItem PASSTHROUGH_HATCH_ITEM;
    public static MetaTileEntityPassthroughHatchFluid PASSTHROUGH_HATCH_FLUID;
    public static MetaTileEntityReservoirHatch RESERVOIR_HATCH;
    public static MetaTileEntityDataAccessHatch[] DATA_ACCESS_HATCH = new MetaTileEntityDataAccessHatch[5];
    public static MetaTileEntityOpticalDataHatch OPTICAL_DATA_HATCH_RECEIVER;
    public static MetaTileEntityOpticalDataHatch OPTICAL_DATA_HATCH_TRANSMITTER;
    public static MetaTileEntityLaserHatch[] LASER_INPUT_HATCH_256 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_INPUT_HATCH_1024 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_INPUT_HATCH_4096 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_INPUT_HATCH_16384 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_INPUT_HATCH_65536 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_INPUT_HATCH_262144 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_INPUT_HATCH_1048576 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_256 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_1024 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_4096 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_16384 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_65536 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_262144 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_1048576 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityPassthroughHatchComputation PASSTHROUGH_HATCH_COMPUTATION;
    public static MetaTileEntityPassthroughHatchLaser PASSTHROUGH_HATCH_LASER;
    public static MetaTileEntityComputationHatch[] COMPUTATION_HATCH_RECEIVER = new MetaTileEntityComputationHatch[
            GTValues.V.length - 1];
    public static MetaTileEntityComputationHatch[] COMPUTATION_HATCH_TRANSMITTER = new MetaTileEntityComputationHatch[
            GTValues.V.length - 1];
    public static MetaTileEntityObjectHolder OBJECT_HOLDER;
    public static MetaTileEntityHPCAEmpty HPCA_EMPTY_COMPONENT;
    public static MetaTileEntityHPCAComputation HPCA_COMPUTATION_COMPONENT;
    public static MetaTileEntityHPCAComputation HPCA_ADVANCED_COMPUTATION_COMPONENT;
    public static MetaTileEntityHPCACooler HPCA_HEAT_SINK_COMPONENT;
    public static MetaTileEntityHPCACooler HPCA_ACTIVE_COOLER_COMPONENT;
    public static MetaTileEntityHPCABridge HPCA_BRIDGE_COMPONENT;
    public static MetaTileEntityHPCAAdvancedComputation HPCA_SUPER_COMPUTATION_COMPONENT;
    public static MetaTileEntityHPCAAdvancedComputation HPCA_ULTIMATE_COMPUTATION_COMPONENT;
    public static MetaTileEntityHPCAAdvancedCooler HPCA_SUPER_COOLER_COMPONENT;
    public static MetaTileEntityHPCAAdvancedCooler HPCA_ULTIMATE_COOLER_COMPONENT;
    // STEAM AGE SECTION
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
    public static MetaTileEntitySterileCleaningMaintenanceHatch STERILE_CLEANING_MAINTENANCE_HATCH;

    // MULTIBLOCKS SECTION
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
    public static MetaTileEntityCharcoalPileIgniter CHARCOAL_PILE_IGNITER;
    public static MetaTileEntityDataBank DATA_BANK;
    public static MetaTileEntityResearchStation RESEARCH_STATION;
    public static MetaTileEntityHPCA HIGH_PERFORMANCE_COMPUTING_ARRAY;
    public static MetaTileEntityNetworkSwitch NETWORK_SWITCH;
    public static MetaTileEntityPowerSubstation POWER_SUBSTATION;
    public static MetaTileEntityActiveTransformer ACTIVE_TRANSFORMER;

    // STORAGE SECTION
    public static MetaTileEntityTankValve WOODEN_TANK_VALVE;
    public static MetaTileEntityTankValve STEEL_TANK_VALVE;
    public static MetaTileEntityMultiblockTank WOODEN_TANK;
    public static MetaTileEntityMultiblockTank STEEL_TANK;
    public static MetaTileEntityDrum WOODEN_DRUM;
    public static MetaTileEntityDrum BRONZE_DRUM;
    public static MetaTileEntityDrum ALUMINIUM_DRUM;
    public static MetaTileEntityDrum STEEL_DRUM;
    public static MetaTileEntityDrum STAINLESS_STEEL_DRUM;
    public static MetaTileEntityDrum TITANIUM_DRUM;
    public static MetaTileEntityDrum TUNGSTENSTEEL_DRUM;
    public static MetaTileEntityDrum GOLD_DRUM;
    public static MetaTileEntityDrum LEAD_DRUM;
    public static MetaTileEntityDrum IRON_DRUM;
    public static MetaTileEntityDrum COPPER_DRUM;

    public static MetaTileEntityDrum RHODIUM_PLATED_PALLADIUM_DRUM;
    public static MetaTileEntityDrum NAQUADAH_ALLOY_DRUM;
    public static MetaTileEntityDrum DARMSTADTIUM_DRUM;
    public static MetaTileEntityDrum NEUTRONIUM_DRUM;

    public static MetaTileEntityCrate WOODEN_CRATE;
    public static MetaTileEntityCrate BRONZE_CRATE;
    public static MetaTileEntityCrate ALUMINIUM_CRATE;
    public static MetaTileEntityCrate STEEL_CRATE;
    public static MetaTileEntityCrate STAINLESS_STEEL_CRATE;
    public static MetaTileEntityCrate TITANIUM_CRATE;
    public static MetaTileEntityCrate TUNGSTENSTEEL_CRATE;

    public static MetaTileEntityCrate RHODIUM_PLATED_PALLADIUM_CRATE;
    public static MetaTileEntityCrate NAQUADAH_ALLOY_CRATE;
    public static MetaTileEntityCrate DARMSTADTIUM_CRATE;
    public static MetaTileEntityCrate NEUTRONIUM_CRATE;

    // MISC MACHINES SECTION
    public static MetaTileEntityWorkbench WORKBENCH;
    public static MetaTileEntityCreativeEnergy CREATIVE_ENERGY;
    public static MetaTileEntityCreativeTank CREATIVE_TANK;
    public static MetaTileEntityCreativeChest CREATIVE_CHEST;
    public static MetaTileEntityClipboard CLIPBOARD_TILE;
    public static MetaTileEntityMonitorScreen MONITOR_SCREEN;
    public static MetaTileEntityCentralMonitor CENTRAL_MONITOR;
    public static MetaTileEntity FLUID_EXPORT_HATCH_ME;
    public static MetaTileEntity ITEM_EXPORT_BUS_ME;
    public static MetaTileEntity FLUID_IMPORT_HATCH_ME;
    public static MetaTileEntity ITEM_IMPORT_BUS_ME;
    public static MetaTileEntity STOCKING_BUS_ME;
    public static MetaTileEntity STOCKING_HATCH_ME;
    public static MetaTileEntityLDItemEndpoint LONG_DIST_ITEM_ENDPOINT;
    public static MetaTileEntityLDFluidEndpoint LONG_DIST_FLUID_ENDPOINT;
    public static MetaTileEntityAlarm ALARM;

    // todo
    public static MetaTileEntityConverter[][] ENERGY_CONVERTER = new MetaTileEntityConverter[4][GTValues.V.length];

    //spotless:on

    public static void init() {
        GTLog.logger.info("Registering MetaTileEntities");

        //蒸汽单方块
        STEAM_BOILER_COAL_BRONZE = registerMetaTileEntity(1,
                new SteamCoalBoiler(gregtechId("steam_boiler_coal_bronze"), false));
        STEAM_BOILER_COAL_STEEL = registerMetaTileEntity(2,
                new SteamCoalBoiler(gregtechId("steam_boiler_coal_steel"), true));

        STEAM_BOILER_SOLAR_BRONZE = registerMetaTileEntity(3,
                new SteamSolarBoiler(gregtechId("steam_boiler_solar_bronze"), false));
        STEAM_BOILER_SOLAR_STEEL = registerMetaTileEntity(4,
                new SteamSolarBoiler(gregtechId("steam_boiler_solar_steel"), true));

        STEAM_BOILER_LAVA_BRONZE = registerMetaTileEntity(5,
                new SteamLavaBoiler(gregtechId("steam_boiler_lava_bronze"), false));
        STEAM_BOILER_LAVA_STEEL = registerMetaTileEntity(6,
                new SteamLavaBoiler(gregtechId("steam_boiler_lava_steel"), true));

        STEAM_EXTRACTOR_BRONZE = registerMetaTileEntity(7,
                new SteamExtractor(gregtechId("steam_extractor_bronze"), false));
        STEAM_EXTRACTOR_STEEL = registerMetaTileEntity(8,
                new SteamExtractor(gregtechId("steam_extractor_steel"), true));

        STEAM_MACERATOR_BRONZE = registerMetaTileEntity(9,
                new SteamMacerator(gregtechId("steam_macerator_bronze"), false));
        STEAM_MACERATOR_STEEL = registerMetaTileEntity(10,
                new SteamMacerator(gregtechId("steam_macerator_steel"), true));

        STEAM_COMPRESSOR_BRONZE = registerMetaTileEntity(11,
                new SteamCompressor(gregtechId("steam_compressor_bronze"), false));
        STEAM_COMPRESSOR_STEEL = registerMetaTileEntity(12,
                new SteamCompressor(gregtechId("steam_compressor_steel"), true));

        STEAM_HAMMER_BRONZE = registerMetaTileEntity(13, new SteamHammer(gregtechId("steam_hammer_bronze"), false));
        STEAM_HAMMER_STEEL = registerMetaTileEntity(14, new SteamHammer(gregtechId("steam_hammer_steel"), true));

        STEAM_FURNACE_BRONZE = registerMetaTileEntity(15, new SteamFurnace(gregtechId("steam_furnace_bronze"), false));
        STEAM_FURNACE_STEEL = registerMetaTileEntity(16, new SteamFurnace(gregtechId("steam_furnace_steel"), true));

        STEAM_ALLOY_SMELTER_BRONZE = registerMetaTileEntity(17,
                new SteamAlloySmelter(gregtechId("steam_alloy_smelter_bronze"), false));
        STEAM_ALLOY_SMELTER_STEEL = registerMetaTileEntity(18,
                new SteamAlloySmelter(gregtechId("steam_alloy_smelter_steel"), true));

        STEAM_ROCK_BREAKER_BRONZE = registerMetaTileEntity(19,
                new SteamRockBreaker(gregtechId("steam_rock_breaker_bronze"), false));
        STEAM_ROCK_BREAKER_STEEL = registerMetaTileEntity(20,
                new SteamRockBreaker(gregtechId("steam_rock_breaker_steel"), true));

        STEAM_MINER = registerMetaTileEntity(21, new SteamMiner(gregtechId("steam_miner"), 320, 4, 0));

        //50- 电力单方块
        // Electric Furnace, IDs 50-64
        registerSimpleMetaTileEntity(ELECTRIC_FURNACE, 50, "electric_furnace", RecipeMaps.FURNACE_RECIPES,
                Textures.ELECTRIC_FURNACE_OVERLAY, true);

        // Macerator, IDs 65-79
        registerMetaTileEntities(MACERATOR, 65, "macerator",
                (tier, voltageName) -> new SimpleMachineMetaTileEntityResizable(
                        gregtechId(String.format("%s.%s", "macerator", voltageName)),
                        RecipeMaps.MACERATOR_RECIPES,
                        -1,
                        switch (tier) {
                            case 1, 2 -> 1;
                            case 3 -> 3;
                            default -> 4;
                        },
                        tier <= GTValues.MV ? Textures.MACERATOR_OVERLAY : Textures.PULVERIZER_OVERLAY,
                        tier,
                        true,
                        GTUtility.defaultTankSizeFunction,
                        VanillaParticleEffects.TOP_SMOKE_SMALL, null));

        // Alloy Smelter, IDs 80-94
        registerSimpleMetaTileEntity(ALLOY_SMELTER, 80, "alloy_smelter", RecipeMaps.ALLOY_SMELTER_RECIPES,
                Textures.ALLOY_SMELTER_OVERLAY, true);

        // Arc Furnace, IDs 95-109
        registerMetaTileEntities(ARC_FURNACE, 95, "arc_furnace",
                (tier, voltageName) -> new SimpleMachineMetaTileEntityResizable(
                        gregtechId(String.format("%s.%s", "arc_furnace", voltageName)),
                        RecipeMaps.ARC_FURNACE_RECIPES,
                        -1,
                        tier >= GTValues.EV ? 9 : 4,
                        Textures.ARC_FURNACE_OVERLAY,
                        tier,
                        false,
                        GTUtility.hvCappedTankSizeFunction));

        // Assembler, IDs 110-124
        registerSimpleMetaTileEntity(ASSEMBLER, 110, "assembler", RecipeMaps.ASSEMBLER_RECIPES,
                Textures.ASSEMBLER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Autoclave, IDs 125-139
        registerSimpleMetaTileEntity(AUTOCLAVE, 125, "autoclave", RecipeMaps.AUTOCLAVE_RECIPES,
                Textures.AUTOCLAVE_OVERLAY, false, GTUtility.hvCappedTankSizeFunction);

        // Bender, IDs 140-154
        registerSimpleMetaTileEntity(BENDER, 140, "bender", RecipeMaps.BENDER_RECIPES, Textures.BENDER_OVERLAY, true);

        // Brewery, IDs 155-169
        registerSimpleMetaTileEntity(BREWERY, 155, "brewery", RecipeMaps.BREWING_RECIPES, Textures.BREWERY_OVERLAY,
                true, GTUtility.hvCappedTankSizeFunction);

        // Canner, IDs 170-184
        registerSimpleMetaTileEntity(CANNER, 170, "canner", RecipeMaps.CANNER_RECIPES, Textures.CANNER_OVERLAY, true);

        // Centrifuge, IDs 185-199
        registerSimpleMetaTileEntity(CENTRIFUGE, 185, "centrifuge", RecipeMaps.CENTRIFUGE_RECIPES,
                Textures.CENTRIFUGE_OVERLAY, false, GTUtility.largeTankSizeFunction);

        // Chemical Bath, IDs 200-214
        registerSimpleMetaTileEntity(CHEMICAL_BATH, 200, "chemical_bath", RecipeMaps.CHEMICAL_BATH_RECIPES,
                Textures.CHEMICAL_BATH_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Chemical Reactor, IDs 215-229
        registerSimpleMetaTileEntity(CHEMICAL_REACTOR, 215, "chemical_reactor", RecipeMaps.CHEMICAL_RECIPES,
                Textures.CHEMICAL_REACTOR_OVERLAY, true, tier -> 16000);

        // Compressor, IDs 230-244
        registerSimpleMetaTileEntity(COMPRESSOR, 230, "compressor", RecipeMaps.COMPRESSOR_RECIPES,
                Textures.COMPRESSOR_OVERLAY, true);

        // Cutter, IDs 245-259
        registerSimpleMetaTileEntity(CUTTER, 245, "cutter", RecipeMaps.CUTTER_RECIPES, Textures.CUTTER_OVERLAY, true);

        // Distillery, IDs 260-274
        registerSimpleMetaTileEntity(DISTILLERY, 260, "distillery", RecipeMaps.DISTILLERY_RECIPES,
                Textures.DISTILLERY_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Electrolyzer, IDs 275-289
        registerSimpleMetaTileEntity(ELECTROLYZER, 275, "electrolyzer", RecipeMaps.ELECTROLYZER_RECIPES,
                Textures.ELECTROLYZER_OVERLAY, false, GTUtility.largeTankSizeFunction);

        // Electromagnetic Separator, IDs 290-304
        registerSimpleMetaTileEntity(ELECTROMAGNETIC_SEPARATOR, 290, "electromagnetic_separator",
                RecipeMaps.ELECTROMAGNETIC_SEPARATOR_RECIPES, Textures.ELECTROMAGNETIC_SEPARATOR_OVERLAY, true);

        // Extractor, IDs 305-319
        registerSimpleMetaTileEntity(EXTRACTOR, 305, "extractor", RecipeMaps.EXTRACTOR_RECIPES,
                Textures.EXTRACTOR_OVERLAY, true);

        // Extruder, IDs 320-334
        registerSimpleMetaTileEntity(EXTRUDER, 320, "extruder", RecipeMaps.EXTRUDER_RECIPES, Textures.EXTRUDER_OVERLAY,
                true);

        // Fermenter, IDs 335-349
        registerSimpleMetaTileEntity(FERMENTER, 335, "fermenter", RecipeMaps.FERMENTING_RECIPES,
                Textures.FERMENTER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Mass Fabricator, IDs 350-364
        registerSimpleMetaTileEntity(MASS_FABRICATOR, 350, "mass_fabricator", RecipeMaps.MASS_FABRICATOR_RECIPES,
                Textures.MASS_FABRICATOR_OVERLAY, true);

        // Replicator, IDs 365-379
        registerSimpleMetaTileEntity(REPLICATOR, 365, "replicator", RecipeMaps.REPLICATOR_RECIPES,
                Textures.REPLICATOR_OVERLAY, true);

        // Fluid Heater, IDs 380-394
        registerSimpleMetaTileEntity(FLUID_HEATER, 380, "fluid_heater", RecipeMaps.FLUID_HEATER_RECIPES,
                Textures.FLUID_HEATER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Fluid Solidifier, IDs 395-409
        registerSimpleMetaTileEntity(FLUID_SOLIDIFIER, 395, "fluid_solidifier", RecipeMaps.FLUID_SOLIDFICATION_RECIPES,
                Textures.FLUID_SOLIDIFIER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Forge Hammer, IDs 410-424
        registerSimpleMetaTileEntity(FORGE_HAMMER, 410, "forge_hammer", RecipeMaps.FORGE_HAMMER_RECIPES,
                Textures.FORGE_HAMMER_OVERLAY, true);

        // Forming Press, IDs 425-439
        registerSimpleMetaTileEntity(FORMING_PRESS, 425, "forming_press", RecipeMaps.FORMING_PRESS_RECIPES,
                Textures.FORMING_PRESS_OVERLAY, true);

        // Lathe, IDs 440-454
        registerSimpleMetaTileEntity(LATHE, 440, "lathe", RecipeMaps.LATHE_RECIPES, Textures.LATHE_OVERLAY, true);

        // Scanner, IDs 455-469
        registerSimpleMetaTileEntity(SCANNER, 455, "scanner", RecipeMaps.SCANNER_RECIPES, Textures.SCANNER_OVERLAY,
                true);

        // Mixer, IDs 470-484
        registerSimpleMetaTileEntity(MIXER, 470, "mixer", RecipeMaps.MIXER_RECIPES, Textures.MIXER_OVERLAY, false,
                GTUtility.hvCappedTankSizeFunction);

        // Ore Washer, IDs 485-499
        registerSimpleMetaTileEntity(ORE_WASHER, 485, "ore_washer", RecipeMaps.ORE_WASHER_RECIPES,
                Textures.ORE_WASHER_OVERLAY, true);

        // Packer, IDs 500-514
        registerSimpleMetaTileEntity(PACKER, 500, "packer", RecipeMaps.PACKER_RECIPES, Textures.PACKER_OVERLAY, true);

        // FREE, IDs 515-529

        // Gas Collectors, IDs 530-544
        registerMetaTileEntities(GAS_COLLECTOR, 530, "gas_collector",
                (tier, voltageName) -> new MetaTileEntityGasCollector(
                        gregtechId(String.format("%s.%s", "gas_collector", voltageName)),
                        RecipeMaps.GAS_COLLECTOR_RECIPES, Textures.GAS_COLLECTOR_OVERLAY, tier, false,
                        GTUtility.largeTankSizeFunction));
        // Polarizer, IDs 545-559
        registerSimpleMetaTileEntity(POLARIZER, 545, "polarizer", RecipeMaps.POLARIZER_RECIPES,
                Textures.POLARIZER_OVERLAY, true);

        // Laser Engraver, IDs 560-574
        registerSimpleMetaTileEntity(LASER_ENGRAVER, 560, "laser_engraver", RecipeMaps.LASER_ENGRAVER_RECIPES,
                Textures.LASER_ENGRAVER_OVERLAY, true);

        // Sifter, IDs 575-589
        registerSimpleMetaTileEntity(SIFTER, 575, "sifter", RecipeMaps.SIFTER_RECIPES, Textures.SIFTER_OVERLAY, true);

        // FREE, IDs 590-604

        // Thermal Centrifuge, IDs 605-619
        registerSimpleMetaTileEntity(THERMAL_CENTRIFUGE, 605, "thermal_centrifuge",
                RecipeMaps.THERMAL_CENTRIFUGE_RECIPES, Textures.THERMAL_CENTRIFUGE_OVERLAY, true);

        // Wire Mill, IDs 620-634
        registerSimpleMetaTileEntity(WIREMILL, 620, "wiremill", RecipeMaps.WIREMILL_RECIPES, Textures.WIREMILL_OVERLAY,
                true);

        // Circuit Assembler, IDs 650-664
        registerSimpleMetaTileEntity(CIRCUIT_ASSEMBLER, 635, "circuit_assembler", RecipeMaps.CIRCUIT_ASSEMBLER_RECIPES,
                Textures.CIRCUIT_ASSEMBLER_OVERLAY, true, GTUtility.hvCappedTankSizeFunction);

        // Rock Breaker, IDs 665-679
        registerMetaTileEntities(ROCK_BREAKER, 665, "rock_breaker",
                (tier, voltageName) -> new MetaTileEntityRockBreaker(
                        gregtechId(String.format("%s.%s", "rock_breaker", voltageName)),
                        RecipeMaps.ROCK_BREAKER_RECIPES, Textures.ROCK_BREAKER_OVERLAY, tier));

        // World Accelerators, IDs 680-694
        if (ConfigHolder.machines.enableWorldAccelerators) {
            for (int i = 0; i < WORLD_ACCELERATOR.length; i++) {
                WORLD_ACCELERATOR[i] = registerMetaTileEntity(680 + i,
                        new MetaTileEntityWorldAccelerator(
                                gregtechId("world_accelerator." + GTValues.VN[i].toLowerCase()), i + 1));
            }
        }

        //695-709
        for (int i = 0; i < CHARGER.length; i++) {
            String chargerId = "charger." + GTValues.VN[i].toLowerCase();
            MetaTileEntityCharger charger = new MetaTileEntityCharger(gregtechId(chargerId), i, 4);
            CHARGER[i] = registerMetaTileEntity(695 + i, charger);
        }

        // Fishers, IDs 710-724
        for (int i = 0; i < FISHER.length; i++) {
            FISHER[i] = registerMetaTileEntity(710 + i,
                    new MetaTileEntityFisher(gregtechId("fisher." + GTValues.VN[i + 1].toLowerCase()), i + 1));
        }

        // Pumps, IDs 725-739
        for (int i = 0; i < PUMP.length; i++) {
            PUMP[i] = registerMetaTileEntity(725 + i,
                    new MetaTileEntityPump(gregtechId("pump." + GTValues.VN[i + 1].toLowerCase()), i + 1));
        }

        // Block Breakers, IDs 740-754
        for (int i = 0; i < BLOCK_BREAKER.length; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            BLOCK_BREAKER[i] = new MetaTileEntityBlockBreaker(gregtechId("block_breaker." + voltageName), i + 1);
            registerMetaTileEntity(740 + i, BLOCK_BREAKER[i]);
        }

        // Chunk Miner, IDs 920-934
        MINER[0] = registerMetaTileEntity(920, new MetaTileEntityMiner(gregtechId("miner.lv"), 1, 160, 8, 1));
        MINER[1] = registerMetaTileEntity(921, new MetaTileEntityMiner(gregtechId("miner.mv"), 2, 80, 16, 2));
        MINER[2] = registerMetaTileEntity(922, new MetaTileEntityMiner(gregtechId("miner.hv"), 3, 40, 24, 3));

        // Buffers, IDs 1510-1514
        BUFFER[0] = registerMetaTileEntity(930, new MetaTileEntityBuffer(gregtechId("buffer.lv"), 1));
        BUFFER[1] = registerMetaTileEntity(931, new MetaTileEntityBuffer(gregtechId("buffer.mv"), 2));
        BUFFER[2] = registerMetaTileEntity(932, new MetaTileEntityBuffer(gregtechId("buffer.hv"), 3));
        BUFFER[3] = registerMetaTileEntity(933, new MetaTileEntityBuffer(gregtechId("buffer.ev"), 4));
        BUFFER[4] = registerMetaTileEntity(934, new MetaTileEntityBuffer(gregtechId("buffer.iv"), 5));

        //单方块发电机
        // Diesel Generator, IDs 935-949
        COMBUSTION_GENERATOR[0] = registerMetaTileEntity(935,
                new MetaTileEntitySingleCombustion(gregtechId("combustion_generator.lv"),
                        RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 1,
                        GTUtility.genericGeneratorTankSizeFunction));
        COMBUSTION_GENERATOR[1] = registerMetaTileEntity(936,
                new MetaTileEntitySingleCombustion(gregtechId("combustion_generator.mv"),
                        RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 2,
                        GTUtility.genericGeneratorTankSizeFunction));
        COMBUSTION_GENERATOR[2] = registerMetaTileEntity(937,
                new MetaTileEntitySingleCombustion(gregtechId("combustion_generator.hv"),
                        RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 3,
                        GTUtility.genericGeneratorTankSizeFunction));
        COMBUSTION_GENERATOR[3] = registerMetaTileEntity(938,
                new MetaTileEntitySingleCombustion(gregtechId("combustion_generator.ev"),
                        RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 4,
                        GTUtility.genericGeneratorTankSizeFunction));
        COMBUSTION_GENERATOR[4] = registerMetaTileEntity(939,
                new MetaTileEntitySingleCombustion(gregtechId("combustion_generator.iv"),
                        RecipeMaps.COMBUSTION_GENERATOR_FUELS, Textures.COMBUSTION_GENERATOR_OVERLAY, 5,
                        GTUtility.genericGeneratorTankSizeFunction));

        // Steam Turbine, IDs 950-964
        STEAM_TURBINE[0] = registerMetaTileEntity(950,
                new MetaTileEntitySingleTurbine(gregtechId("steam_turbine.lv"), RecipeMaps.STEAM_TURBINE_FUELS,
                        Textures.STEAM_TURBINE_OVERLAY, 1, GTUtility.steamGeneratorTankSizeFunction));
        STEAM_TURBINE[1] = registerMetaTileEntity(951,
                new MetaTileEntitySingleTurbine(gregtechId("steam_turbine.mv"), RecipeMaps.STEAM_TURBINE_FUELS,
                        Textures.STEAM_TURBINE_OVERLAY, 2, GTUtility.steamGeneratorTankSizeFunction));
        STEAM_TURBINE[2] = registerMetaTileEntity(952,
                new MetaTileEntitySingleTurbine(gregtechId("steam_turbine.hv"), RecipeMaps.STEAM_TURBINE_FUELS,
                        Textures.STEAM_TURBINE_OVERLAY, 3, GTUtility.steamGeneratorTankSizeFunction));
        STEAM_TURBINE[3] = registerMetaTileEntity(953,
                new MetaTileEntitySingleTurbine(gregtechId("steam_turbine.ev"), RecipeMaps.STEAM_TURBINE_FUELS,
                        Textures.STEAM_TURBINE_OVERLAY, 4, GTUtility.steamGeneratorTankSizeFunction));
        STEAM_TURBINE[4] = registerMetaTileEntity(954,
                new MetaTileEntitySingleTurbine(gregtechId("steam_turbine.iv"), RecipeMaps.STEAM_TURBINE_FUELS,
                        Textures.STEAM_TURBINE_OVERLAY, 5, GTUtility.steamGeneratorTankSizeFunction));

        // Gas Turbine, IDs 965-979
        GAS_TURBINE[0] = registerMetaTileEntity(965,
                new MetaTileEntitySingleTurbine(gregtechId("gas_turbine.lv"), RecipeMaps.GAS_TURBINE_FUELS,
                        Textures.GAS_TURBINE_OVERLAY, 1, GTUtility.genericGeneratorTankSizeFunction));
        GAS_TURBINE[1] = registerMetaTileEntity(966,
                new MetaTileEntitySingleTurbine(gregtechId("gas_turbine.mv"), RecipeMaps.GAS_TURBINE_FUELS,
                        Textures.GAS_TURBINE_OVERLAY, 2, GTUtility.genericGeneratorTankSizeFunction));
        GAS_TURBINE[2] = registerMetaTileEntity(967,
                new MetaTileEntitySingleTurbine(gregtechId("gas_turbine.hv"), RecipeMaps.GAS_TURBINE_FUELS,
                        Textures.GAS_TURBINE_OVERLAY, 3, GTUtility.genericGeneratorTankSizeFunction));
        GAS_TURBINE[3] = registerMetaTileEntity(968,
                new MetaTileEntitySingleTurbine(gregtechId("gas_turbine.ev"), RecipeMaps.GAS_TURBINE_FUELS,
                        Textures.GAS_TURBINE_OVERLAY, 4, GTUtility.genericGeneratorTankSizeFunction));
        GAS_TURBINE[4] = registerMetaTileEntity(969,
                new MetaTileEntitySingleTurbine(gregtechId("gas_turbine.iv"), RecipeMaps.GAS_TURBINE_FUELS,
                        Textures.GAS_TURBINE_OVERLAY, 5, GTUtility.genericGeneratorTankSizeFunction));

        // Item Collector, IDs 980-983
        ITEM_COLLECTOR[0] = registerMetaTileEntity(980,
                new MetaTileEntityItemCollector(gregtechId("item_collector.lv"), 1, 8));
        ITEM_COLLECTOR[1] = registerMetaTileEntity(981,
                new MetaTileEntityItemCollector(gregtechId("item_collector.mv"), 2, 16));
        ITEM_COLLECTOR[2] = registerMetaTileEntity(982,
                new MetaTileEntityItemCollector(gregtechId("item_collector.hv"), 3, 32));
        ITEM_COLLECTOR[3] = registerMetaTileEntity(983,
                new MetaTileEntityItemCollector(gregtechId("item_collector.ev"), 4, 64));

        MAGIC_ENERGY_ABSORBER = registerMetaTileEntity(984,
                new MetaTileEntityMagicEnergyAbsorber(gregtechId("magic_energy_absorber")));

        // Hulls, IDs 985-999
        int endPos = GregTechAPI.isHighTier() ? HULL.length : Math.min(HULL.length - 1, GTValues.UV + 2);
        for (int i = 0; i < endPos; i++) {
            HULL[i] = new MetaTileEntityHull(gregtechId("hull." + GTValues.VN[i].toLowerCase()), i);
            registerMetaTileEntity(985 + i, HULL[i]);
        }

        //多方块1000-
        // MULTIBLOCK START: IDs 1000-1149. Space left for addons to register Multiblocks grouped with the rest in JEI
        PRIMITIVE_BLAST_FURNACE = registerMetaTileEntity(1000,
                new MetaTileEntityPrimitiveBlastFurnace(gregtechId("primitive_blast_furnace.bronze")));
        ELECTRIC_BLAST_FURNACE = registerMetaTileEntity(1001,
                new MetaTileEntityElectricBlastFurnace(gregtechId("electric_blast_furnace")));
        VACUUM_FREEZER = registerMetaTileEntity(1002, new MetaTileEntityVacuumFreezer(gregtechId("vacuum_freezer")));
        IMPLOSION_COMPRESSOR = registerMetaTileEntity(1003,
                new MetaTileEntityImplosionCompressor(gregtechId("implosion_compressor")));
        PYROLYSE_OVEN = registerMetaTileEntity(1004, new MetaTileEntityPyrolyseOven(gregtechId("pyrolyse_oven")));
        DISTILLATION_TOWER = registerMetaTileEntity(1005,
                new MetaTileEntityDistillationTower(gregtechId("distillation_tower"), true));
        MULTI_FURNACE = registerMetaTileEntity(1006, new MetaTileEntityMultiSmelter(gregtechId("multi_furnace")));
        LARGE_COMBUSTION_ENGINE = registerMetaTileEntity(1007,
                new MetaTileEntityLargeCombustionEngine(gregtechId("large_combustion_engine"), GTValues.EV));
        EXTREME_COMBUSTION_ENGINE = registerMetaTileEntity(1008,
                new MetaTileEntityLargeCombustionEngine(gregtechId("extreme_combustion_engine"), GTValues.IV));
        CRACKER = registerMetaTileEntity(1009, new MetaTileEntityCrackingUnit(gregtechId("cracker")));

        LARGE_STEAM_TURBINE = registerMetaTileEntity(1010,
                new MetaTileEntityLargeTurbine(gregtechId("large_turbine.steam"), RecipeMaps.STEAM_TURBINE_FUELS, 3,
                        MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_TURBINE_CASING),
                        MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX),
                        Textures.SOLID_STEEL_CASING, false, Textures.LARGE_STEAM_TURBINE_OVERLAY));
        LARGE_GAS_TURBINE = registerMetaTileEntity(1011, new MetaTileEntityLargeTurbine(gregtechId("large_turbine.gas"),
                RecipeMaps.GAS_TURBINE_FUELS, 4,
                MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_TURBINE_CASING),
                MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX),
                Textures.CLEAN_STAINLESS_STEEL_CASING, true, Textures.LARGE_GAS_TURBINE_OVERLAY));
        LARGE_PLASMA_TURBINE = registerMetaTileEntity(1012,
                new MetaTileEntityLargeTurbine(gregtechId("large_turbine.plasma"), RecipeMaps.PLASMA_GENERATOR_FUELS, 5,
                        MetaBlocks.TURBINE_CASING
                                .getState(BlockTurbineCasing.TurbineCasingType.TUNGSTENSTEEL_TURBINE_CASING),
                        MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.TUNGSTENSTEEL_GEARBOX),
                        Textures.ROBUST_TUNGSTENSTEEL_CASING, false, Textures.LARGE_PLASMA_TURBINE_OVERLAY));

        LARGE_BRONZE_BOILER = registerMetaTileEntity(1013,
                new MetaTileEntityLargeBoiler(gregtechId("large_boiler.bronze"), BoilerType.BRONZE));
        LARGE_STEEL_BOILER = registerMetaTileEntity(1014,
                new MetaTileEntityLargeBoiler(gregtechId("large_boiler.steel"), BoilerType.STEEL));
        LARGE_TITANIUM_BOILER = registerMetaTileEntity(1015,
                new MetaTileEntityLargeBoiler(gregtechId("large_boiler.titanium"), BoilerType.TITANIUM));
        LARGE_TUNGSTENSTEEL_BOILER = registerMetaTileEntity(1016,
                new MetaTileEntityLargeBoiler(gregtechId("large_boiler.tungstensteel"), BoilerType.TUNGSTENSTEEL));

        COKE_OVEN = registerMetaTileEntity(1017, new MetaTileEntityCokeOven(gregtechId("coke_oven")));
        COKE_OVEN_HATCH = registerMetaTileEntity(1018, new MetaTileEntityCokeOvenHatch(gregtechId("coke_oven_hatch")));

        ASSEMBLY_LINE = registerMetaTileEntity(1019, new MetaTileEntityAssemblyLine(gregtechId("assembly_line")));
        FUSION_REACTOR[0] = registerMetaTileEntity(1020,
                new MetaTileEntityFusionReactor(gregtechId("fusion_reactor.luv"), GTValues.LuV));
        FUSION_REACTOR[1] = registerMetaTileEntity(1021,
                new MetaTileEntityFusionReactor(gregtechId("fusion_reactor.zpm"), GTValues.ZPM));
        FUSION_REACTOR[2] = registerMetaTileEntity(1022,
                new MetaTileEntityFusionReactor(gregtechId("fusion_reactor.uv"), GTValues.UV));

        LARGE_CHEMICAL_REACTOR = registerMetaTileEntity(1023,
                new MetaTileEntityLargeChemicalReactor(gregtechId("large_chemical_reactor")));

        STEAM_OVEN = registerMetaTileEntity(1024, new MetaTileEntitySteamOven(gregtechId("steam_oven")));
        STEAM_GRINDER = registerMetaTileEntity(1025, new MetaTileEntitySteamGrinder(gregtechId("steam_grinder")));

        BASIC_LARGE_MINER = registerMetaTileEntity(1026,
                new MetaTileEntityLargeMiner(gregtechId("large_miner.ev"), GTValues.EV, 16, 3, 4, Materials.Steel, 8));
        LARGE_MINER = registerMetaTileEntity(1027, new MetaTileEntityLargeMiner(gregtechId("large_miner.iv"),
                GTValues.IV, 4, 5, 5, Materials.Titanium, 16));
        ADVANCED_LARGE_MINER = registerMetaTileEntity(1028, new MetaTileEntityLargeMiner(gregtechId("large_miner.luv"),
                GTValues.LuV, 1, 7, 6, Materials.TungstenSteel, 32));

        CENTRAL_MONITOR = registerMetaTileEntity(1029, new MetaTileEntityCentralMonitor(gregtechId("central_monitor")));

        PROCESSING_ARRAY = registerMetaTileEntity(1030,
                new MetaTileEntityProcessingArray(gregtechId("processing_array"), 0));
        ADVANCED_PROCESSING_ARRAY = registerMetaTileEntity(1031,
                new MetaTileEntityProcessingArray(gregtechId("advanced_processing_array"), 1));

        BASIC_FLUID_DRILLING_RIG = registerMetaTileEntity(1032,
                new MetaTileEntityFluidDrill(gregtechId("fluid_drilling_rig.mv"), 2));
        FLUID_DRILLING_RIG = registerMetaTileEntity(1033,
                new MetaTileEntityFluidDrill(gregtechId("fluid_drilling_rig.hv"), 3));
        ADVANCED_FLUID_DRILLING_RIG = registerMetaTileEntity(1034,
                new MetaTileEntityFluidDrill(gregtechId("fluid_drilling_rig.ev"), 4));

        CLEANROOM = registerMetaTileEntity(1035, new MetaTileEntityCleanroom(gregtechId("cleanroom")));

        CHARCOAL_PILE_IGNITER = registerMetaTileEntity(1036,
                new MetaTileEntityCharcoalPileIgniter(gregtechId("charcoal_pile")));

        DATA_BANK = registerMetaTileEntity(1037, new MetaTileEntityDataBank(gregtechId("data_bank")));
        RESEARCH_STATION = registerMetaTileEntity(1038,
                new MetaTileEntityResearchStation(gregtechId("research_station")));
        HIGH_PERFORMANCE_COMPUTING_ARRAY = registerMetaTileEntity(1039,
                new MetaTileEntityHPCA(gregtechId("high_performance_computing_array")));
        NETWORK_SWITCH = registerMetaTileEntity(1040, new MetaTileEntityNetworkSwitch(gregtechId("network_switch")));

        POWER_SUBSTATION = registerMetaTileEntity(1041,
                new MetaTileEntityPowerSubstation(gregtechId("power_substation")));
        ACTIVE_TRANSFORMER = registerMetaTileEntity(1042,
                new MetaTileEntityActiveTransformer(gregtechId("active_transformer")));

        //IO注册
        // Import/Export Buses/Hatches, IDs 1150-1300
        endPos = GregTechAPI.isHighTier() ? ITEM_IMPORT_BUS.length : GTValues.UHV + 1;
        for (int i = 0; i < endPos; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            //总线
            ITEM_IMPORT_BUS[i] = new MetaTileEntityItemBus(gregtechId("item_bus.import." + voltageName), i, false);
            ITEM_EXPORT_BUS[i] = new MetaTileEntityItemBus(gregtechId("item_bus.export." + voltageName), i, true);
            FLUID_IMPORT_HATCH[i] = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import." + voltageName), i,
                    false);
            FLUID_EXPORT_HATCH[i] = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.export." + voltageName), i,
                    true);

            registerMetaTileEntity(1150 + i, ITEM_IMPORT_BUS[i]);
            registerMetaTileEntity(1165 + i, ITEM_EXPORT_BUS[i]);
            registerMetaTileEntity(1180 + i, FLUID_IMPORT_HATCH[i]);
            registerMetaTileEntity(1195 + i, FLUID_EXPORT_HATCH[i]);

            //多重
            QUADRUPLE_IMPORT_HATCH[i] = registerMetaTileEntity(1210 + i,
                    new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.import_4x." + voltageName), i, 4, false));
            NONUPLE_IMPORT_HATCH[i] = registerMetaTileEntity(1225 + i,
                    new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.import_9x." + voltageName), i, 9, false));
            SIXTEEN_IMPORT_HATCH[i] = registerMetaTileEntity(1240 + i,
                    new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.import_16x." + voltageName), i, 16,
                            false));
            QUADRUPLE_EXPORT_HATCH[i] = registerMetaTileEntity(1255 + i,
                    new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.export_4x." + voltageName), i, 4, true));
            NONUPLE_EXPORT_HATCH[i] = registerMetaTileEntity(1270 + i,
                    new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.export_9x." + voltageName), i, 9, true));
            SIXTEEN_EXPORT_HATCH[i] = registerMetaTileEntity(1285 + i,
                    new MetaTileEntityMultiFluidHatch(gregtechId("fluid_hatch.export_16x." + voltageName), i, 16,
                            true));
        }

        //1300-
        // Energy Input/Output Hatches, IDs 1210-1269, 1800-1829
        endPos = GregTechAPI.isHighTier() ? ENERGY_INPUT_HATCH.length - 1 :
                Math.min(ENERGY_INPUT_HATCH.length - 1, GTValues.UV + 2);
        for (int i = 0; i < endPos; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            ENERGY_INPUT_HATCH[i] = registerMetaTileEntity(1300 + i,
                    new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.input." + voltageName), i, 2, false));
            ENERGY_OUTPUT_HATCH[i] = registerMetaTileEntity(1315 + i,
                    new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.output." + voltageName), i, 2, true));
            ENERGY_INPUT_HATCH_4A[i] = registerMetaTileEntity(1330 + i,
                    new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.input_4a." + voltageName), i, 4, false));
            ENERGY_OUTPUT_HATCH_4A[i] = registerMetaTileEntity(1345 + i,
                    new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.output_4a." + voltageName), i, 4, true));
            ENERGY_INPUT_HATCH_16A[i] = registerMetaTileEntity(1360 + i,
                    new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.input_16a." + voltageName), i, 16, false));
            ENERGY_OUTPUT_HATCH_16A[i] = registerMetaTileEntity(1375 + i,
                    new MetaTileEntityEnergyHatch(gregtechId("energy_hatch.output_16a." + voltageName), i, 16, true));
            SUBSTATION_ENERGY_INPUT_HATCH[i] = registerMetaTileEntity(1390 + i,
                    new MetaTileEntitySubstationEnergyHatch(gregtechId("substation_hatch.input_64a." + voltageName), i,
                            64, false));
            SUBSTATION_ENERGY_OUTPUT_HATCH[i] = registerMetaTileEntity(1405 + i,
                    new MetaTileEntitySubstationEnergyHatch(gregtechId("substation_hatch.output_64a." + voltageName), i,
                            64, true));

        }

        // Transformer, IDs 1420-1469
        endPos = GregTechAPI.isHighTier() ? TRANSFORMER.length - 1 : Math.min(TRANSFORMER.length - 1, GTValues.UV);
        for (int i = 0; i <= endPos; i++) {
            // 1A <-> 4A
            MetaTileEntityTransformer transformer = new MetaTileEntityTransformer(
                    gregtechId("transformer." + GTValues.VN[i].toLowerCase()), i);
            TRANSFORMER[i] = registerMetaTileEntity(1420 + (i), transformer);
            // 2A <-> 8A and 4A <-> 16A
            MetaTileEntityTransformer adjustableTransformer = new MetaTileEntityTransformer(
                    gregtechId("transformer.hi_amp." + GTValues.VN[i].toLowerCase()), i, 2, 4);
            HI_AMP_TRANSFORMER[i] = registerMetaTileEntity(1450 + i, adjustableTransformer);
            // 16A <-> 64A (can do other amperages because of legacy compat)
            adjustableTransformer = new MetaTileEntityTransformer(
                    gregtechId("transformer.adjustable." + GTValues.VN[i].toLowerCase()), i, 1, 2, 4, 16);
            POWER_TRANSFORMER[i] = registerMetaTileEntity(1435 + (i), adjustableTransformer);
        }

        // Diode, IDs 1465-1479
        endPos = GregTechAPI.isHighTier() ? DIODES.length - 1 : Math.min(DIODES.length - 1, GTValues.UV + 2);
        for (int i = 0; i < endPos; i++) {
            String diodeId = "diode." + GTValues.VN[i].toLowerCase();
            MetaTileEntityDiode diode = new MetaTileEntityDiode(gregtechId(diodeId), i, 16);
            DIODES[i] = registerMetaTileEntity(1465 + i, diode);
        }

        // Battery Buffer, IDs 1480-1524
        endPos = GregTechAPI.isHighTier() ? BATTERY_BUFFER[0].length : GTValues.UHV + 1;
        int[] batteryBufferSlots = new int[] { 4, 8, 16 };
        for (int slot = 0; slot < batteryBufferSlots.length; slot++) {
            BATTERY_BUFFER[slot] = new MetaTileEntityBatteryBuffer[endPos];
            for (int i = 0; i < endPos; i++) {
                String bufferId = "battery_buffer." + GTValues.VN[i].toLowerCase() + "." + batteryBufferSlots[slot];
                MetaTileEntityBatteryBuffer batteryBuffer = new MetaTileEntityBatteryBuffer(gregtechId(bufferId), i,
                        batteryBufferSlots[slot]);
                BATTERY_BUFFER[slot][i] = registerMetaTileEntity(1480 + BATTERY_BUFFER[slot].length * slot + i,
                        batteryBuffer);
            }
        }

        //激光 1530-
        endPos = GregTechAPI.isHighTier() ? LASER_INPUT_HATCH_256.length - 1 :
                Math.min(LASER_INPUT_HATCH_256.length - 1, GTValues.UHV - GTValues.IV);
        for (int i = 0; i < endPos; i++) {
            int v = i + GTValues.IV;
            String voltageName = GTValues.VN[v].toLowerCase();
            LASER_INPUT_HATCH_256[i] = registerMetaTileEntity(1530 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.target_256a." + voltageName), false, v, 256));
            LASER_OUTPUT_HATCH_256[i] = registerMetaTileEntity(1540 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.source_256a." + voltageName), true, v, 256));
            LASER_INPUT_HATCH_1024[i] = registerMetaTileEntity(1550 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.target_1024a." + voltageName), false, v,
                            1024));
            LASER_OUTPUT_HATCH_1024[i] = registerMetaTileEntity(1560 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.source_1024a." + voltageName), true, v, 1024));
            LASER_INPUT_HATCH_4096[i] = registerMetaTileEntity(1570 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.target_4096a." + voltageName), false, v,
                            4096));
            LASER_OUTPUT_HATCH_4096[i] = registerMetaTileEntity(1580 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.source_4096a." + voltageName), true, v, 4096));
            LASER_INPUT_HATCH_16384[i] = registerMetaTileEntity(1590 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.target_16384a." + voltageName), false, v,
                            16384));
            LASER_OUTPUT_HATCH_16384[i] = registerMetaTileEntity(1600 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.source_16384a." + voltageName), true, v,
                            16384));
            LASER_INPUT_HATCH_65536[i] = registerMetaTileEntity(1610 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.target_65536a." + voltageName), false, v,
                            65536));
            LASER_OUTPUT_HATCH_65536[i] = registerMetaTileEntity(1620 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.source_65536a." + voltageName), true, v,
                            65536));
            LASER_INPUT_HATCH_262144[i] = registerMetaTileEntity(1630 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.target_262144a." + voltageName), false, v,
                            262144));
            LASER_OUTPUT_HATCH_262144[i] = registerMetaTileEntity(1640 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.source_262144a." + voltageName), true, v,
                            262144));
            LASER_INPUT_HATCH_1048576[i] = registerMetaTileEntity(1650 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.target_1048576a." + voltageName), false, v,
                            1048576));
            LASER_OUTPUT_HATCH_1048576[i] = registerMetaTileEntity(1660 + i,
                    new MetaTileEntityLaserHatch(gregtechId("laser_hatch.source_1048576a." + voltageName), true, v,
                            1048576));
        }

        // Maintenance Hatches, IDs 1700-1394
        MAINTENANCE_HATCH = registerMetaTileEntity(1700,
                new MetaTileEntityMaintenanceHatch(gregtechId("maintenance_hatch"), false));
        CONFIGURABLE_MAINTENANCE_HATCH = registerMetaTileEntity(1701,
                new MetaTileEntityMaintenanceHatch(gregtechId("maintenance_hatch_configurable"), true));
        AUTO_MAINTENANCE_HATCH = registerMetaTileEntity(1702,
                new MetaTileEntityAutoMaintenanceHatch(gregtechId("maintenance_hatch_full_auto")));
        CLEANING_MAINTENANCE_HATCH = registerMetaTileEntity(1703,
                new MetaTileEntityCleaningMaintenanceHatch(gregtechId("maintenance_hatch_cleanroom_auto")));
        STERILE_CLEANING_MAINTENANCE_HATCH = registerMetaTileEntity(1704,
                new MetaTileEntitySterileCleaningMaintenanceHatch(gregtechId("maintenance_hatch_sterile_cleanroom_auto")));

        PASSTHROUGH_HATCH_ITEM = registerMetaTileEntity(1710,
                new MetaTileEntityPassthroughHatchItem(gregtechId("passthrough_hatch_item"), 3));
        PASSTHROUGH_HATCH_FLUID = registerMetaTileEntity(1711,
                new MetaTileEntityPassthroughHatchFluid(gregtechId("passthrough_hatch_fluid"), 3));
        PASSTHROUGH_HATCH_LASER = registerMetaTileEntity(1712,
                new MetaTileEntityPassthroughHatchLaser(gregtechId("passthrough_hatch_laser"), 5));
        PASSTHROUGH_HATCH_COMPUTATION = registerMetaTileEntity(1713,
                new MetaTileEntityPassthroughHatchComputation(gregtechId("passthrough_hatch_computation"), 5));

        OPTICAL_DATA_HATCH_RECEIVER = registerMetaTileEntity(1720,
                new MetaTileEntityOpticalDataHatch(gregtechId("data_access_hatch.optical.receiver"), false));
        OPTICAL_DATA_HATCH_TRANSMITTER = registerMetaTileEntity(1721,
                new MetaTileEntityOpticalDataHatch(gregtechId("data_access_hatch.optical.transmitter"), true));
        OBJECT_HOLDER = registerMetaTileEntity(1722,
                new MetaTileEntityObjectHolder(gregtechId("research_station.object_holder")));
        HPCA_EMPTY_COMPONENT = registerMetaTileEntity(1723,
                new MetaTileEntityHPCAEmpty(gregtechId("hpca.empty_component")));
        HPCA_COMPUTATION_COMPONENT = registerMetaTileEntity(1724,
                new MetaTileEntityHPCAComputation(gregtechId("hpca.computation_component"), false));
        HPCA_ADVANCED_COMPUTATION_COMPONENT = registerMetaTileEntity(1725,
                new MetaTileEntityHPCAComputation(gregtechId("hpca.advanced_computation_component"), true));
        HPCA_HEAT_SINK_COMPONENT = registerMetaTileEntity(1726,
                new MetaTileEntityHPCACooler(gregtechId("hpca.heat_sink_component"), false));
        HPCA_ACTIVE_COOLER_COMPONENT = registerMetaTileEntity(1727,
                new MetaTileEntityHPCACooler(gregtechId("hpca.active_cooler_component"), true));
        HPCA_SUPER_COMPUTATION_COMPONENT = registerMetaTileEntity(1728,
                new MetaTileEntityHPCAAdvancedComputation(gregtechId("hpca.super_computation_component"), false));
        HPCA_ULTIMATE_COMPUTATION_COMPONENT = registerMetaTileEntity(1729,
                new MetaTileEntityHPCAAdvancedComputation(gregtechId("hpca.ultimate_computation_component"), true));
        HPCA_SUPER_COOLER_COMPONENT = registerMetaTileEntity(1730,
                new MetaTileEntityHPCAAdvancedCooler(gregtechId("hpca.super_cooler_component"), true, false));
        HPCA_ULTIMATE_COOLER_COMPONENT = registerMetaTileEntity(1731,
                new MetaTileEntityHPCAAdvancedCooler(gregtechId("hpca.ultimate_cooler_component"), false, true));
        HPCA_BRIDGE_COMPONENT = registerMetaTileEntity(1732,
                new MetaTileEntityHPCABridge(gregtechId("hpca.bridge_component")));

        RESERVOIR_HATCH = registerMetaTileEntity(1740, new MetaTileEntityReservoirHatch(gregtechId("reservoir_hatch")));
        MACHINE_HATCH = registerMetaTileEntity(1741, new MetaTileEntityMachineHatch(gregtechId("machine_hatch"), 2));

        // Rotor Holder, IDs 1750-1764
        for (int i = 0; i < ROTOR_HOLDER.length; i++) {
            String voltageName = GTValues.VN[i + 3].toLowerCase();
            ROTOR_HOLDER[i] = registerMetaTileEntity(1750 + i,
                    new MetaTileEntityRotorHolder(gregtechId("rotor_holder." + voltageName), i + 3));
        }

        // Muffler Hatches, IDs 1775-1789
        for (int i = 0; i < MUFFLER_HATCH.length - 1; i++) {
            int tier = i + 1;
            if (!GregTechAPI.isHighTier() && tier == GTValues.UHV) continue; // requires UHV motor to craft, so skip
            String voltageName = GTValues.VN[tier].toLowerCase();
            MUFFLER_HATCH[tier] = new MetaTileEntityMufflerHatch(gregtechId("muffler_hatch." + voltageName), tier);

            registerMetaTileEntity(1775 + i, MUFFLER_HATCH[tier]);
        }

        // Energy Converter, IDs 1790-1849
        endPos = GregTechAPI.isHighTier() ? ENERGY_CONVERTER[0].length : GTValues.UHV + 1;
        int[] amps = { 1, 4, 8, 16 };
        for (int i = 0; i < endPos; i++) {
            for (int j = 0; j < 4; j++) {
                // Check to make sure this is a valid amount of power to be able to convert.
                // Tests if both:
                // - The maximum amount of EU/t of this converter can turn into FE without overflowing
                // - Max int FE/t can convert to the full amount of EU/t without being short
                // This is done because these ratios are configured separately.
                long eu = amps[j] * GTValues.V[i];
                long euToFe = FeCompat.toFeLong(eu, FeCompat.ratio(false));
                long feToEu = FeCompat.toEu(Integer.MAX_VALUE, FeCompat.ratio(true));
                if (euToFe > Integer.MAX_VALUE || feToEu < eu) continue;

                String id = "energy_converter." + GTValues.VN[i].toLowerCase() + "." + amps[j];
                MetaTileEntityConverter converter = new MetaTileEntityConverter(gregtechId(id), i, amps[j]);
                ENERGY_CONVERTER[j][i] = registerMetaTileEntity(1790 + j + i * 4, converter);
            }
        }

        // 1850-1900 free

        // ME Hatches, IDs 1900-
        if (Mods.AppliedEnergistics2.isModLoaded()) {
            FLUID_EXPORT_HATCH_ME = registerMetaTileEntity(1900,
                    new MetaTileEntityMEOutputHatch(gregtechId("me_export_fluid_hatch")));
            ITEM_EXPORT_BUS_ME = registerMetaTileEntity(1901,
                    new MetaTileEntityMEOutputBus(gregtechId("me_export_item_bus")));
            FLUID_IMPORT_HATCH_ME = registerMetaTileEntity(1902,
                    new MetaTileEntityMEInputHatch(gregtechId("me_import_fluid_hatch")));
            ITEM_IMPORT_BUS_ME = registerMetaTileEntity(1903,
                    new MetaTileEntityMEInputBus(gregtechId("me_import_item_bus")));
            STOCKING_BUS_ME = registerMetaTileEntity(1904,
                    new MetaTileEntityMEStockingBus(gregtechId("me_stocking_item_bus")));
            STOCKING_HATCH_ME = registerMetaTileEntity(1905,
                    new MetaTileEntityMEStockingHatch(gregtechId("me_stocking_fluid_hatch")));

        }

        // Data Access Hatches, IDs 1910-
        DATA_ACCESS_HATCH[0] = registerMetaTileEntity(1910,
                new MetaTileEntityDataAccessHatch(gregtechId("data_access_hatch.i"), GTValues.MV, false));
        DATA_ACCESS_HATCH[1] = registerMetaTileEntity(1911,
                new MetaTileEntityDataAccessHatch(gregtechId("data_access_hatch.ii"), GTValues.EV, false));
        DATA_ACCESS_HATCH[2] = registerMetaTileEntity(1912,
                new MetaTileEntityDataAccessHatch(gregtechId("data_access_hatch.iii"), GTValues.LuV, false));
        DATA_ACCESS_HATCH[3] = registerMetaTileEntity(1913,
                new MetaTileEntityDataAccessHatch(gregtechId("data_access_hatch.iv"), GTValues.UV, false));
        DATA_ACCESS_HATCH[4] = registerMetaTileEntity(1914,
                new MetaTileEntityDataAccessHatch(gregtechId("data_access_hatch.creative"), GTValues.MAX, true));

        //算力仓重做 1920-
        for (int i = 0; i < COMPUTATION_HATCH_RECEIVER.length - 1; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            COMPUTATION_HATCH_RECEIVER[i] = registerMetaTileEntity(1920 + i,
                    new MetaTileEntityComputationHatch(gregtechId("computation_hatch.receiver." + voltageName), i + 1,
                            false));
            COMPUTATION_HATCH_TRANSMITTER[i] = registerMetaTileEntity(1935 + i,
                    new MetaTileEntityComputationHatch(gregtechId("computation_hatch.transmitter." + voltageName),
                            i + 1, true));
        }

        //杂项 2000-
        WORKBENCH = registerMetaTileEntity(2000, new MetaTileEntityWorkbench(gregtechId("workbench")));
        PRIMITIVE_WATER_PUMP = registerMetaTileEntity(2001,
                new MetaTileEntityPrimitiveWaterPump(gregtechId("primitive_water_pump")));
        PUMP_OUTPUT_HATCH = registerMetaTileEntity(2002, new MetaTileEntityPumpHatch(gregtechId("pump_hatch")));

        CREATIVE_ENERGY = registerMetaTileEntity(2003, new MetaTileEntityCreativeEnergy());

        // Steam Hatches/Buses

        STEAM_EXPORT_BUS = registerMetaTileEntity(2010,
                new MetaTileEntitySteamItemBus(gregtechId("steam_export_bus"), true));
        STEAM_IMPORT_BUS = registerMetaTileEntity(2011,
                new MetaTileEntitySteamItemBus(gregtechId("steam_import_bus"), false));
        STEAM_HATCH = registerMetaTileEntity(2012, new MetaTileEntitySteamHatch(gregtechId("steam_hatch")));

        CLIPBOARD_TILE = registerMetaTileEntity(2013, new MetaTileEntityClipboard(gregtechId("clipboard")));

        MONITOR_SCREEN = registerMetaTileEntity(2014, new MetaTileEntityMonitorScreen(gregtechId("monitor_screen")));

        // Creative Chest and Tank, IDs 1668-1669
        CREATIVE_CHEST = registerMetaTileEntity(2015, new MetaTileEntityCreativeChest(gregtechId("creative_chest")));
        CREATIVE_TANK = registerMetaTileEntity(2016, new MetaTileEntityCreativeTank(gregtechId("creative_tank")));

        LONG_DIST_ITEM_ENDPOINT = registerMetaTileEntity(2017,
                new MetaTileEntityLDItemEndpoint(gregtechId("ld_item_endpoint")));
        LONG_DIST_FLUID_ENDPOINT = registerMetaTileEntity(2018,
                new MetaTileEntityLDFluidEndpoint(gregtechId("ld_fluid_endpoint")));

        ALARM = registerMetaTileEntity(2019, new MetaTileEntityAlarm(gregtechId("alarm")));
        //存储
        // Quantum Storage Network 2020 -
        QUANTUM_STORAGE_CONTROLLER = registerMetaTileEntity(2020,
                new MetaTileEntityQuantumStorageController(gregtechId("quantum_storage_controller")));
        QUANTUM_STORAGE_PROXY = registerMetaTileEntity(2021,
                new MetaTileEntityQuantumProxy(gregtechId("quantum_storage_proxy")));
        QUANTUM_STORAGE_EXTENDER = registerMetaTileEntity(2022,
                new MetaTileEntityQuantumExtender(gregtechId("quantum_storage_extender")));

        // Super / Quantum Chests, IDs 1560-1574
        for (int i = 0; i < 5; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            QUANTUM_CHEST[i] = new MetaTileEntityQuantumChest(gregtechId("super_chest." + voltageName), i + 1,
                    4000000L * (int) Math.pow(2, i));
            registerMetaTileEntity(2030 + i, QUANTUM_CHEST[i]);
        }

        for (int i = 5; i < QUANTUM_CHEST.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            long capacity = i == GTValues.UHV ? Integer.MAX_VALUE : 4000000L * (int) Math.pow(2, i);
            QUANTUM_CHEST[i] = new MetaTileEntityQuantumChest(gregtechId("quantum_chest." + voltageName), i, capacity);
            registerMetaTileEntity(2030 + i, QUANTUM_CHEST[i]);
        }

        // Super / Quantum Tanks, IDs 1575-1589
        for (int i = 0; i < 5; i++) {
            String voltageName = GTValues.VN[i + 1].toLowerCase();
            QUANTUM_TANK[i] = new MetaTileEntityQuantumTank(gregtechId("super_tank." + voltageName), i + 1,
                    4000000 * (int) Math.pow(2, i));
            registerMetaTileEntity(2040 + i, QUANTUM_TANK[i]);
        }

        for (int i = 5; i < QUANTUM_TANK.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            int capacity = i == GTValues.UHV ? Integer.MAX_VALUE : 4000000 * (int) Math.pow(2, i);
            QUANTUM_TANK[i] = new MetaTileEntityQuantumTank(gregtechId("quantum_tank." + voltageName), i, capacity);
            registerMetaTileEntity(2040 + i, QUANTUM_TANK[i]);
        }
        // Tanks, IDs 1596-1609
        WOODEN_TANK_VALVE = registerMetaTileEntity(2050,
                new MetaTileEntityTankValve(gregtechId("tank_valve.wood"), false));
        WOODEN_TANK = registerMetaTileEntity(2051,
                new MetaTileEntityMultiblockTank(gregtechId("tank.wood"), false, 250 * 1000));

        STEEL_TANK_VALVE = registerMetaTileEntity(2052,
                new MetaTileEntityTankValve(gregtechId("tank_valve.steel"), true));
        STEEL_TANK = registerMetaTileEntity(2053,
                new MetaTileEntityMultiblockTank(gregtechId("tank.steel"), true, 1000 * 1000));

        // Drums, IDs 1610-1624
        WOODEN_DRUM = registerMetaTileEntity(2060,
                new MetaTileEntityDrum(gregtechId("drum.wood"), Materials.Wood, 16000));
        BRONZE_DRUM = registerMetaTileEntity(2061,
                new MetaTileEntityDrum(gregtechId("drum.bronze"), Materials.Bronze, 24000));
        GOLD_DRUM = registerMetaTileEntity(2062,
                new MetaTileEntityDrum(gregtechId("drum.gold"), Materials.Gold, 32000));
        COPPER_DRUM = MetaTileEntities.registerMetaTileEntity(2063,
                new MetaTileEntityDrum(gregtechId("drum.tin"), Materials.Copper, 40000));
        IRON_DRUM = MetaTileEntities.registerMetaTileEntity(2064,
                new MetaTileEntityDrum(gregtechId("drum.iron"), Materials.Iron, 48000));
        LEAD_DRUM = MetaTileEntities.registerMetaTileEntity(2065,
                new MetaTileEntityDrum(gregtechId("drum.lead"), Materials.Lead, 56000));
        STEEL_DRUM = registerMetaTileEntity(2066,
                new MetaTileEntityDrum(gregtechId("drum.steel"), Materials.Steel, 64000));
        ALUMINIUM_DRUM = registerMetaTileEntity(2067,
                new MetaTileEntityDrum(gregtechId("drum.aluminium"), Materials.Aluminium, 128000));
        STAINLESS_STEEL_DRUM = registerMetaTileEntity(2068,
                new MetaTileEntityDrum(gregtechId("drum.stainless_steel"), Materials.StainlessSteel, 256000));
        TITANIUM_DRUM = registerMetaTileEntity(2069,
                new MetaTileEntityDrum(gregtechId("drum.titanium"), Materials.Titanium, 512000));
        TUNGSTENSTEEL_DRUM = registerMetaTileEntity(2070,
                new MetaTileEntityDrum(gregtechId("drum.tungstensteel"), Materials.TungstenSteel, 1024000));
        RHODIUM_PLATED_PALLADIUM_DRUM = MetaTileEntities.registerMetaTileEntity(2071,
                new MetaTileEntityDrum(gregtechId("drum.rhodium_plated_palladium"), Materials.RhodiumPlatedPalladium,
                        2_048_000));
        NAQUADAH_ALLOY_DRUM = MetaTileEntities.registerMetaTileEntity(2072,
                new MetaTileEntityDrum(gregtechId("drum.naquadah_alloy"), Materials.NaquadahAlloy, 4_096_000));
        DARMSTADTIUM_DRUM = MetaTileEntities.registerMetaTileEntity(2073,
                new MetaTileEntityDrum(gregtechId("drum.darmstadtium"), Materials.Darmstadtium, 8_192_000));
        NEUTRONIUM_DRUM = MetaTileEntities.registerMetaTileEntity(2074,
                new MetaTileEntityDrum(gregtechId("drum.neutronium"), Materials.Neutronium, 16_384_000));

        // Crates, IDs 1625-1635
        WOODEN_CRATE = registerMetaTileEntity(2080,
                new MetaTileEntityCrate(gregtechId("crate.wood"), Materials.Wood, 27, 9));
        BRONZE_CRATE = registerMetaTileEntity(2081,
                new MetaTileEntityCrate(gregtechId("crate.bronze"), Materials.Bronze, 54, 9));
        STEEL_CRATE = registerMetaTileEntity(2082,
                new MetaTileEntityCrate(gregtechId("crate.steel"), Materials.Steel, 72, 9));
        ALUMINIUM_CRATE = registerMetaTileEntity(2083,
                new MetaTileEntityCrate(gregtechId("crate.aluminium"), Materials.Aluminium, 90, 10));
        STAINLESS_STEEL_CRATE = registerMetaTileEntity(2084,
                new MetaTileEntityCrate(gregtechId("crate.stainless_steel"), Materials.StainlessSteel, 108, 12));
        TITANIUM_CRATE = registerMetaTileEntity(2085,
                new MetaTileEntityCrate(gregtechId("crate.titanium"), Materials.Titanium, 126, 14));
        TUNGSTENSTEEL_CRATE = registerMetaTileEntity(2086,
                new MetaTileEntityCrate(gregtechId("crate.tungstensteel"), Materials.TungstenSteel, 144, 16));
        RHODIUM_PLATED_PALLADIUM_CRATE = MetaTileEntities.registerMetaTileEntity(2087,
                new MetaTileEntityCrate(gregtechId("crate.rhodium_plated_palladium"), Materials.RhodiumPlatedPalladium,
                        162, 18));
        NAQUADAH_ALLOY_CRATE = MetaTileEntities.registerMetaTileEntity(2088,
                new MetaTileEntityCrate(gregtechId("crate.naquadah_alloy"), Materials.NaquadahAlloy, 180, 20));
        DARMSTADTIUM_CRATE = MetaTileEntities.registerMetaTileEntity(2089,
                new MetaTileEntityCrate(gregtechId("crate.darmstadtium"), Materials.Darmstadtium, 198, 22));
        NEUTRONIUM_CRATE = MetaTileEntities.registerMetaTileEntity(2090,
                new MetaTileEntityCrate(gregtechId("crate.neutronium"), Materials.Neutronium, 216, 24));

    }

    private static void registerSimpleMetaTileEntity(SimpleMachineMetaTileEntity[] machines,
                                                     int startId,
                                                     String name,
                                                     RecipeMap<?> map,
                                                     ICubeRenderer texture,
                                                     boolean hasFrontFacing,
                                                     Function<Integer, Integer> tankScalingFunction) {
        registerSimpleMetaTileEntity(machines, startId, name, map, texture, hasFrontFacing, GTUtility::gregtechId,
                tankScalingFunction);
    }

    private static void registerSimpleMetaTileEntity(SimpleMachineMetaTileEntity[] machines,
                                                     int startId,
                                                     String name,
                                                     RecipeMap<?> map,
                                                     ICubeRenderer texture,
                                                     boolean hasFrontFacing) {
        registerSimpleMetaTileEntity(machines, startId, name, map, texture, hasFrontFacing,
                GTUtility.defaultTankSizeFunction);
    }

    public static void registerSimpleMetaTileEntity(SimpleMachineMetaTileEntity[] machines,
                                                    int startId,
                                                    String name,
                                                    RecipeMap<?> map,
                                                    ICubeRenderer texture,
                                                    boolean hasFrontFacing,
                                                    Function<String, ResourceLocation> resourceId,
                                                    Function<Integer, Integer> tankScalingFunction) {
        registerMetaTileEntities(machines, startId, name,
                (tier, voltageName) -> new SimpleMachineMetaTileEntity(
                        resourceId.apply(String.format("%s.%s", name, voltageName)), map, texture, tier, hasFrontFacing,
                        tankScalingFunction));
    }

    /**
     * @param mteCreator Takes tier and voltage name for the machine, and outputs MTE to register
     */
    public static void registerMetaTileEntities(
            MetaTileEntity[] machines,
            int startId,
            String name,
            BiFunction<Integer, String, MetaTileEntity> mteCreator) {
        for (int i = 0; i < machines.length - 1; i++) {
            if (i > 4 && !getMidTier(name)) continue;
            if (i > 7 && !getHighTier(name)) break;

            String voltageName = GTValues.VN[i + 1].toLowerCase();
            machines[i + 1] = registerMetaTileEntity(startId + i, mteCreator.apply(i + 1, voltageName));
        }
    }

    /**
     * Register a MetaTileEntity
     *
     * @param id  the numeric ID to use as item metadata
     * @param mte the MTE to register
     * @param <T> the MTE class
     * @return the MTE
     */
    public static <T extends MetaTileEntity> @NotNull T registerMetaTileEntity(int id, @NotNull T mte) {
        if (mte instanceof IMultiblockAbilityPart<?> abilityPart) {
            for (var ability : abilityPart.getAbilities())
                MultiblockAbility.registerMultiblockAbility(ability, mte);
        }

        if (Mods.JustEnoughItems.isModLoaded() && mte instanceof MultiblockControllerBase controller &&
                controller.shouldShowInJei()) {
            MultiblockInfoCategory.registerMultiblock(controller);
        }

        mte.getRegistry().register(id, mte.metaTileEntityId, mte);
        return mte;
    }

    @SuppressWarnings("unused")
    public static void setMidTier(String key, boolean enabled) {
        MID_TIER.put(key, enabled);
    }

    @SuppressWarnings("unused")
    public static void setHighTier(String key, boolean enabled) {
        HIGH_TIER.put(key, enabled);
        if (!GregTechAPI.isHighTier()) {
            throw new IllegalArgumentException(
                    "Cannot set High-Tier machine without high tier being enabled in GregTechAPI.");
        }
    }

    public static boolean getMidTier(String key) {
        return MID_TIER.getOrDefault(key, true);
    }

    public static boolean getHighTier(String key) {
        return HIGH_TIER.getOrDefault(key, GregTechAPI.isHighTier());
    }
}
