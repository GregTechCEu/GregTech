package gregtech.client.renderer.texture;

import codechicken.lib.render.BlockRenderer.BlockFace;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils.IIconRegister;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.TransformationList;
import codechicken.lib.vec.uv.IconTransformation;
import codechicken.lib.vec.uv.UVTransformationList;
import gregtech.api.GTValues;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.cclop.GTBlockOperation;
import gregtech.client.renderer.cclop.UVMirror;
import gregtech.client.renderer.texture.cube.*;
import gregtech.client.renderer.texture.custom.ClipboardRenderer;
import gregtech.client.renderer.texture.custom.LargeTurbineRenderer;
import gregtech.client.renderer.texture.custom.SafeRenderer;
import gregtech.client.renderer.texture.custom.TankRenderer;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.texture.custom.CrateRenderer;
import gregtech.client.renderer.texture.custom.DrumRenderer;
import gregtech.client.utils.BloomEffectUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gregtech.client.renderer.texture.cube.OrientedOverlayRenderer.OverlayFace.*;

public class Textures {

    public static final Map<String, ICubeRenderer> CUBE_RENDERER_REGISTRY = new HashMap<>();

    private static final ThreadLocal<BlockFace> blockFaces = ThreadLocal.withInitial(BlockFace::new);
    public static final List<IIconRegister> iconRegisters = new ArrayList<>();

    // Custom Renderers
    public static final ClipboardRenderer CLIPBOARD_RENDERER = new ClipboardRenderer();
    public static final CrateRenderer WOODEN_CRATE = new CrateRenderer("storage/crates/wooden_crate");
    public static final CrateRenderer METAL_CRATE = new CrateRenderer("storage/crates/metal_crate");
    public static final DrumRenderer WOODEN_DRUM = new DrumRenderer("storage/drums/wooden_drum");
    public static final DrumRenderer DRUM = new DrumRenderer("storage/drums/drum");
    public static final SafeRenderer SAFE = new SafeRenderer("storage/safe");
    public static final TankRenderer WOODEN_TANK = new TankRenderer("storage/tank/wooden");
    public static final TankRenderer METAL_TANK = new TankRenderer("storage/tank/metal");
    public static final LargeTurbineRenderer LARGE_TURBINE_ROTOR_RENDERER = new LargeTurbineRenderer();

    // Simple Cube Renderers
    public static final SimpleCubeRenderer BRONZE_PLATED_BRICKS = new SimpleCubeRenderer("casings/solid/machine_bronze_plated_bricks");
    public static final SimpleCubeRenderer PRIMITIVE_BRICKS = new SimpleCubeRenderer("casings/solid/machine_primitive_bricks");
    public static final SimpleCubeRenderer COKE_BRICKS = new SimpleCubeRenderer("casings/solid/machine_coke_bricks");
    public static final SimpleCubeRenderer HEAT_PROOF_CASING = new SimpleCubeRenderer("casings/solid/machine_casing_heatproof");
    public static final SimpleCubeRenderer FROST_PROOF_CASING = new SimpleCubeRenderer("casings/solid/machine_casing_frost_proof");
    public static final SimpleCubeRenderer SOLID_STEEL_CASING = new SimpleCubeRenderer("casings/solid/machine_casing_solid_steel");
    public static final SimpleCubeRenderer CLEAN_STAINLESS_STEEL_CASING = new SimpleCubeRenderer("casings/solid/machine_casing_clean_stainless_steel");
    public static final SimpleCubeRenderer STABLE_TITANIUM_CASING = new SimpleCubeRenderer("casings/solid/machine_casing_stable_titanium");
    public static final SimpleCubeRenderer ROBUST_TUNGSTENSTEEL_CASING = new SimpleCubeRenderer("casings/solid/machine_casing_robust_tungstensteel");
    public static final SimpleCubeRenderer INERT_PTFE_CASING = new SimpleCubeRenderer("casings/solid/machine_casing_inert_ptfe");
    public static final SimpleCubeRenderer BRONZE_FIREBOX = new SimpleCubeRenderer("casings/firebox/machine_casing_firebox_bronze");
    public static final SimpleCubeRenderer BRONZE_FIREBOX_ACTIVE = new FullBrightnessSimpleCubeRenderer("casings/firebox/machine_casing_firebox_bronze_active");
    public static final SimpleCubeRenderer STEEL_FIREBOX = new SimpleCubeRenderer("casings/firebox/machine_casing_firebox_steel");
    public static final SimpleCubeRenderer STEEL_FIREBOX_ACTIVE = new FullBrightnessSimpleCubeRenderer("casings/firebox/machine_casing_firebox_steel_active");
    public static final SimpleCubeRenderer TITANIUM_FIREBOX = new SimpleCubeRenderer("casings/firebox/machine_casing_firebox_titanium");
    public static final SimpleCubeRenderer TITANIUM_FIREBOX_ACTIVE = new FullBrightnessSimpleCubeRenderer("casings/firebox/machine_casing_firebox_titanium_active");
    public static final SimpleCubeRenderer TUNGSTENSTEEL_FIREBOX = new SimpleCubeRenderer("casings/firebox/machine_casing_firebox_tungstensteel");
    public static final SimpleCubeRenderer TUNGSTENSTEEL_FIREBOX_ACTIVE = new FullBrightnessSimpleCubeRenderer("casings/firebox/machine_casing_firebox_tungstensteel_active");
    public static final SimpleCubeRenderer FUSION_TEXTURE = new SimpleCubeRenderer("casings/fusion/machine_casing_fusion_hatch");
    public static final SimpleCubeRenderer ACTIVE_FUSION_TEXTURE = new SimpleCubeRenderer("casings/fusion/machine_casing_fusion_hatch_yellow");

    // Simple Sided Cube Renderers
    public static final SimpleSidedCubeRenderer STEAM_CASING_BRONZE = new SimpleSidedCubeRenderer("casings/steam/bronze");
    public static final SimpleSidedCubeRenderer STEAM_CASING_STEEL = new SimpleSidedCubeRenderer("casings/steam/steel");
    public static final SimpleSidedCubeRenderer STEAM_BRICKED_CASING_BRONZE = new SimpleSidedCubeRenderer("casings/steam/bricked_bronze");
    public static final SimpleSidedCubeRenderer STEAM_BRICKED_CASING_STEEL = new SimpleSidedCubeRenderer("casings/steam/bricked_steel");
    public static final SimpleSidedCubeRenderer[] VOLTAGE_CASINGS = new SimpleSidedCubeRenderer[GTValues.V.length];
    public static final SimpleSidedCubeRenderer PRIMITIVE_PUMP = new SimpleSidedCubeRenderer("casings/pump_deck");
    public static final SimpleSidedCubeRenderer MAGIC_ENERGY_ABSORBER = new SimpleSidedCubeRenderer("casings/magic/absorber/normal");
    public static final SimpleSidedCubeRenderer MAGIC_ENERGY_ABSORBER_ACTIVE = new SimpleSidedCubeRenderer("casings/magic/absorber/active");

    // Simple Oriented Cube Renderers
    public static final SimpleOrientedCubeRenderer CRAFTING_TABLE = new SimpleOrientedCubeRenderer("casings/crafting_table");

    // Oriented Overlay Renderers
    public static final OrientedOverlayRenderer COAL_BOILER_OVERLAY = new OrientedOverlayRenderer("generators/boiler/coal", FRONT);
    public static final OrientedOverlayRenderer LAVA_BOILER_OVERLAY = new OrientedOverlayRenderer("generators/boiler/lava", FRONT);
    public static final OrientedOverlayRenderer SOLAR_BOILER_OVERLAY = new OrientedOverlayRenderer("generators/boiler/solar", TOP);
    public static final OrientedOverlayRenderer PRIMITIVE_PUMP_OVERLAY = new OrientedOverlayRenderer("multiblock/primitive_pump", FRONT);
    public static final OrientedOverlayRenderer PRIMITIVE_BLAST_FURNACE_OVERLAY = new OrientedOverlayRenderer("machines/primitive_blast_furnace", FRONT);
    public static final OrientedOverlayRenderer COKE_OVEN_OVERLAY = new OrientedOverlayRenderer("machines/coke_oven", FRONT);
    public static final OrientedOverlayRenderer MULTIBLOCK_WORKABLE_OVERLAY = new OrientedOverlayRenderer("machines/multiblock_workable", FRONT);
    public static final OrientedOverlayRenderer BLAST_FURNACE_OVERLAY = new OrientedOverlayRenderer("multiblock/blast_furnace", FRONT);
    public static final OrientedOverlayRenderer IMPLOSION_COMPRESSOR_OVERLAY = new OrientedOverlayRenderer("multiblock/implosion_compressor", FRONT);
    public static final OrientedOverlayRenderer MULTI_FURNACE_OVERLAY = new OrientedOverlayRenderer("multiblock/multi_furnace", FRONT);
    public static final OrientedOverlayRenderer PYROLYSE_OVEN_OVERLAY = new OrientedOverlayRenderer("multiblock/pyrolyse_oven", FRONT);
    public static final OrientedOverlayRenderer VACUUM_FREEZER_OVERLAY = new OrientedOverlayRenderer("multiblock/vacuum_freezer", FRONT);
    public static final OrientedOverlayRenderer DISTILLATION_TOWER_OVERLAY = new OrientedOverlayRenderer("multiblock/distillation_tower", FRONT);
    public static final OrientedOverlayRenderer CRACKING_UNIT_OVERLAY = new OrientedOverlayRenderer("multiblock/cracking_unit", FRONT);
    public static final OrientedOverlayRenderer LARGE_CHEMICAL_REACTOR_OVERLAY = new OrientedOverlayRenderer("multiblock/large_chemical_reactor", FRONT);
    public static final OrientedOverlayRenderer DIESEL_ENGINE_OVERLAY = new OrientedOverlayRenderer("multiblock/generator/large_combustion_engine", FRONT);
    public static final OrientedOverlayRenderer LARGE_STEAM_TURBINE_OVERLAY = new OrientedOverlayRenderer("multiblock/generator/large_steam_turbine", FRONT);
    public static final OrientedOverlayRenderer LARGE_GAS_TURBINE_OVERLAY = new OrientedOverlayRenderer("multiblock/generator/large_gas_turbine", FRONT);
    public static final OrientedOverlayRenderer LARGE_PLASMA_TURBINE_OVERLAY = new OrientedOverlayRenderer("multiblock/generator/large_plasma_turbine", FRONT);
    public static final OrientedOverlayRenderer LARGE_BRONZE_BOILER = new OrientedOverlayRenderer("multiblock/generator/large_bronze_boiler", FRONT);
    public static final OrientedOverlayRenderer LARGE_STEEL_BOILER = new OrientedOverlayRenderer("multiblock/generator/large_steel_boiler", FRONT);
    public static final OrientedOverlayRenderer LARGE_TITANIUM_BOILER = new OrientedOverlayRenderer("multiblock/generator/large_titanium_boiler", FRONT);
    public static final OrientedOverlayRenderer LARGE_TUNGSTENSTEEL_BOILER = new OrientedOverlayRenderer("multiblock/generator/large_tungstensteel_boiler", FRONT);
    public static final OrientedOverlayRenderer FUSION_REACTOR_OVERLAY = new OrientedOverlayRenderer("machines/fusion_reactor", FRONT);
    public static final OrientedOverlayRenderer LARGE_MINER_OVERLAY_BASIC = new OrientedOverlayRenderer("multiblock/large_miner_basic", FRONT);
    public static final OrientedOverlayRenderer LARGE_MINER_OVERLAY_ADVANCED = new OrientedOverlayRenderer("multiblock/large_miner_advanced", FRONT);
    public static final OrientedOverlayRenderer LARGE_MINER_OVERLAY_ADVANCED_2 = new OrientedOverlayRenderer("multiblock/large_miner_advanced_2", FRONT);
    public static final OrientedOverlayRenderer ALLOY_SMELTER_OVERLAY = new OrientedOverlayRenderer("machines/alloy_smelter", FRONT);
    public static final OrientedOverlayRenderer FURNACE_OVERLAY = new OrientedOverlayRenderer("machines/furnace", FRONT);
    public static final OrientedOverlayRenderer ELECTRIC_FURNACE_OVERLAY = new OrientedOverlayRenderer("machines/electric_furnace", FRONT);
    public static final OrientedOverlayRenderer EXTRACTOR_OVERLAY = new OrientedOverlayRenderer("machines/extractor", FRONT, TOP, SIDE);
    public static final OrientedOverlayRenderer COMPRESSOR_OVERLAY = new OrientedOverlayRenderer("machines/compressor", FRONT, TOP, SIDE);
    public static final OrientedOverlayRenderer MACERATOR_OVERLAY = new OrientedOverlayRenderer("machines/macerator", FRONT, TOP);
    public static final OrientedOverlayRenderer PULVERIZER_OVERLAY = new OrientedOverlayRenderer("machines/pulverizer", FRONT, TOP);
    public static final OrientedOverlayRenderer ARC_FURNACE_OVERLAY = new OrientedOverlayRenderer("machines/arc_furnace", FRONT, BOTTOM, SIDE);
    public static final OrientedOverlayRenderer ASSEMBLER_OVERLAY = new OrientedOverlayRenderer("machines/assembler", FRONT, TOP);
    public static final OrientedOverlayRenderer AUTOCLAVE_OVERLAY = new OrientedOverlayRenderer("machines/autoclave", FRONT, SIDE, TOP);
    public static final OrientedOverlayRenderer BENDER_OVERLAY = new OrientedOverlayRenderer("machines/bender", FRONT);
    public static final OrientedOverlayRenderer BREWERY_OVERLAY = new OrientedOverlayRenderer("machines/brewery", FRONT, SIDE);
    public static final OrientedOverlayRenderer CANNER_OVERLAY = new OrientedOverlayRenderer("machines/canner", FRONT, SIDE);
    public static final OrientedOverlayRenderer CENTRIFUGE_OVERLAY = new OrientedOverlayRenderer("machines/centrifuge", FRONT, SIDE, TOP);
    public static final OrientedOverlayRenderer CHEMICAL_BATH_OVERLAY = new OrientedOverlayRenderer("machines/chemical_bath", FRONT, SIDE);
    public static final OrientedOverlayRenderer CHEMICAL_REACTOR_OVERLAY = new OrientedOverlayRenderer("machines/chemical_reactor", FRONT);
    public static final OrientedOverlayRenderer CUTTER_OVERLAY = new OrientedOverlayRenderer("machines/cutter", FRONT);
    public static final OrientedOverlayRenderer DISTILLERY_OVERLAY = new OrientedOverlayRenderer("machines/distillery", FRONT, SIDE);
    public static final OrientedOverlayRenderer ELECTROLYZER_OVERLAY = new OrientedOverlayRenderer("machines/electrolyzer", FRONT, SIDE);
    public static final OrientedOverlayRenderer ELECTROMAGNETIC_SEPARATOR_OVERLAY = new OrientedOverlayRenderer("machines/electromagnetic_separator", FRONT, TOP);
    public static final OrientedOverlayRenderer EXTRUDER_OVERLAY = new OrientedOverlayRenderer("machines/extruder", FRONT, TOP);
    public static final OrientedOverlayRenderer FERMENTER_OVERLAY = new OrientedOverlayRenderer("machines/fermenter", FRONT, SIDE);
    public static final OrientedOverlayRenderer FLUID_HEATER_OVERLAY = new OrientedOverlayRenderer("machines/fluid_heater", FRONT, SIDE, TOP);
    public static final OrientedOverlayRenderer FLUID_SOLIDIFIER_OVERLAY = new OrientedOverlayRenderer("machines/fluid_solidifier", FRONT);
    public static final OrientedOverlayRenderer FORGE_HAMMER_OVERLAY = new OrientedOverlayRenderer("machines/forge_hammer", FRONT);
    public static final OrientedOverlayRenderer FORMING_PRESS_OVERLAY = new OrientedOverlayRenderer("machines/press", FRONT, SIDE, TOP);
    public static final OrientedOverlayRenderer GAS_COLLECTOR_OVERLAY = new OrientedOverlayRenderer("machines/gas_collector", FRONT, SIDE, TOP, BOTTOM, BACK);
    public static final OrientedOverlayRenderer LATHE_OVERLAY = new OrientedOverlayRenderer("machines/lathe", FRONT);
    public static final OrientedOverlayRenderer MIXER_OVERLAY = new OrientedOverlayRenderer("machines/mixer", FRONT, SIDE, TOP);
    public static final OrientedOverlayRenderer ORE_WASHER_OVERLAY = new OrientedOverlayRenderer("machines/ore_washer", FRONT, SIDE);
    public static final OrientedOverlayRenderer PACKER_OVERLAY = new OrientedOverlayRenderer("machines/packer", FRONT);
    public static final OrientedOverlayRenderer UNPACKER_OVERLAY = new OrientedOverlayRenderer("machines/unpacker", FRONT);
    public static final OrientedOverlayRenderer POLARIZER_OVERLAY = new OrientedOverlayRenderer("machines/polarizer", FRONT, TOP);
    public static final OrientedOverlayRenderer LASER_ENGRAVER_OVERLAY = new OrientedOverlayRenderer("machines/laser_engraver", FRONT);
    public static final OrientedOverlayRenderer ROCK_BREAKER_OVERLAY = new OrientedOverlayRenderer("machines/rock_crusher", FRONT);
    public static final OrientedOverlayRenderer SIFTER_OVERLAY = new OrientedOverlayRenderer("machines/sifter", FRONT, TOP);
    public static final OrientedOverlayRenderer THERMAL_CENTRIFUGE_OVERLAY = new OrientedOverlayRenderer("machines/thermal_centrifuge", FRONT);
    public static final OrientedOverlayRenderer WIREMILL_OVERLAY = new OrientedOverlayRenderer("machines/wiremill", FRONT, TOP);
    public static final OrientedOverlayRenderer MASS_FABRICATOR_OVERLAY = new OrientedOverlayRenderer("machines/mass_fabricator", FRONT);
    public static final OrientedOverlayRenderer REPLICATOR_OVERLAY = new OrientedOverlayRenderer("machines/replicator", FRONT);
    public static final OrientedOverlayRenderer SCANNER_OVERLAY = new OrientedOverlayRenderer("machines/scanner", FRONT);
    public static final OrientedOverlayRenderer COMBUSTION_GENERATOR_OVERLAY = new OrientedOverlayRenderer("generators/combustion", TOP);
    public static final OrientedOverlayRenderer GAS_TURBINE_OVERLAY = new OrientedOverlayRenderer("generators/gas_turbine", SIDE);
    public static final OrientedOverlayRenderer STEAM_TURBINE_OVERLAY = new OrientedOverlayRenderer("generators/steam_turbine", SIDE);
    public static final OrientedOverlayRenderer WORLD_ACCELERATOR_OVERLAY = new OrientedOverlayRenderer("machines/world_accelerator", FRONT, BACK, SIDE);
    public static final OrientedOverlayRenderer WORLD_ACCELERATOR_TE_OVERLAY = new OrientedOverlayRenderer("machines/world_accelerator_te", FRONT, BACK, SIDE);

    // Simple Overlay Renderers
    public static final SimpleOverlayRenderer SCREEN = new SimpleOverlayRenderer("overlay/machine/overlay_screen");
    public static final SimpleOverlayRenderer DISPLAY = new SimpleOverlayRenderer("cover/overlay_display");
    public static final SimpleOverlayRenderer SHUTTER = new SimpleOverlayRenderer("cover/overlay_shutter");
    public static final SimpleOverlayRenderer DETECTOR_ENERGY = new SimpleOverlayRenderer("cover/overlay_energy_detector");
    public static final SimpleOverlayRenderer DETECTOR_FLUID = new SimpleOverlayRenderer("cover/overlay_fluid_detector");
    public static final SimpleOverlayRenderer DETECTOR_ITEM = new SimpleOverlayRenderer("cover/overlay_item_detector");
    public static final SimpleOverlayRenderer CRAFTING = new SimpleOverlayRenderer("cover/overlay_crafting");
    public static final SimpleOverlayRenderer SOLAR_PANEL = new SimpleOverlayRenderer("cover/overlay_solar_panel");
    public static final SimpleOverlayRenderer INFINITE_WATER = new SimpleOverlayRenderer("cover/overlay_infinite_water");
    public static final SimpleOverlayRenderer ENDER_FLUID_LINK = new SimpleOverlayRenderer("cover/overlay_ender_fluid_link");
    public static final SimpleOverlayRenderer PIPE_OUT_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_pipe_out");
    public static final SimpleOverlayRenderer PIPE_IN_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_pipe_in");
    public static final SimpleOverlayRenderer PIPE_4X_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_pipe_4x");
    public static final SimpleOverlayRenderer PIPE_9X_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_pipe_9x");

    public static final SimpleOverlayRenderer FLUID_OUTPUT_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_fluid_output");
    public static final SimpleOverlayRenderer ITEM_OUTPUT_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_item_output");

    public static final SimpleOverlayRenderer FLUID_HATCH_OUTPUT_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_fluid_hatch_output");
    public static final SimpleOverlayRenderer FLUID_HATCH_INPUT_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_fluid_hatch_input");
    public static final SimpleOverlayRenderer ITEM_HATCH_OUTPUT_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_item_hatch_output");
    public static final SimpleOverlayRenderer ITEM_HATCH_INPUT_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_item_hatch_input");

    public static final SimpleOverlayRenderer ROTOR_HOLDER_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_rotor_holder");
    public static final SimpleOverlayRenderer ADV_PUMP_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_adv_pump");
    public static final SimpleOverlayRenderer FILTER_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_filter");
    public static final SimpleOverlayRenderer HATCH_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_hatch");
    public static final SimpleOverlayRenderer FLUID_FILTER_OVERLAY = new SimpleOverlayRenderer("cover/overlay_fluid_filter");
    public static final SimpleOverlayRenderer ITEM_FILTER_FILTER_OVERLAY = new SimpleOverlayRenderer("cover/overlay_item_filter");
    public static final SimpleOverlayRenderer ORE_DICTIONARY_FILTER_OVERLAY = new SimpleOverlayRenderer("cover/overlay_ore_dictionary_filter");
    public static final SimpleOverlayRenderer SMART_FILTER_FILTER_OVERLAY = new SimpleOverlayRenderer("cover/overlay_smart_item_filter");
    public static final SimpleOverlayRenderer MACHINE_CONTROLLER_OVERLAY = new SimpleOverlayRenderer("cover/overlay_controller");
    public static final SimpleOverlayRenderer ENERGY_OUT = new SimpleOverlayRenderer("overlay/machine/overlay_energy_out");
    public static final SimpleOverlayRenderer ENERGY_IN = new SimpleOverlayRenderer("overlay/machine/overlay_energy_in");
    public static final SimpleOverlayRenderer ENERGY_OUT_MULTI = new SimpleOverlayRenderer("overlay/machine/overlay_energy_out_multi");
    public static final SimpleOverlayRenderer ENERGY_IN_MULTI = new SimpleOverlayRenderer("overlay/machine/overlay_energy_in_multi");
    public static final SimpleOverlayRenderer ENERGY_OUT_HI = new SimpleOverlayRenderer("overlay/machine/overlay_energy_out_hi");
    public static final SimpleOverlayRenderer ENERGY_IN_HI = new SimpleOverlayRenderer("overlay/machine/overlay_energy_in_hi");
    public static final SimpleOverlayRenderer ENERGY_OUT_ULTRA = new SimpleOverlayRenderer("overlay/machine/overlay_energy_out_ultra");
    public static final SimpleOverlayRenderer ENERGY_IN_ULTRA = new SimpleOverlayRenderer("overlay/machine/overlay_energy_in_ultra");
    public static final SimpleOverlayRenderer CONVEYOR_OVERLAY = new SimpleOverlayRenderer("cover/overlay_conveyor");
    public static final SimpleOverlayRenderer CONVEYOR_OVERLAY_INVERTED = new SimpleOverlayRenderer("cover/overlay_conveyor_inverted");
    public static final SimpleOverlayRenderer ARM_OVERLAY = new SimpleOverlayRenderer("cover/overlay_arm");
    public static final SimpleOverlayRenderer ARM_OVERLAY_INVERTED = new SimpleOverlayRenderer("cover/overlay_arm_inverted");
    public static final SimpleOverlayRenderer PUMP_OVERLAY = new SimpleOverlayRenderer("cover/overlay_pump");
    public static final SimpleOverlayRenderer PUMP_OVERLAY_INVERTED = new SimpleOverlayRenderer("cover/overlay_pump_inverted");
    public static final SimpleOverlayRenderer AIR_VENT_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_air_vent");
    public static final SimpleOverlayRenderer BLOWER_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_blower");
    public static final SimpleOverlayRenderer BLOWER_ACTIVE_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_blower_active");
    public static final SimpleOverlayRenderer INFINITE_EMITTER_FACE = new SimpleOverlayRenderer("overlay/machine/overlay_energy_emitter");
    public static final SimpleOverlayRenderer STEAM_VENT_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_steam_vent");
    public static final SimpleOverlayRenderer QUANTUM_TANK_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_qtank");
    public static final SimpleOverlayRenderer QUANTUM_CHEST_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_qchest");
    public static final SimpleOverlayRenderer BUFFER_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_buffer");
    public static final SimpleOverlayRenderer MAINTENANCE_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_maintenance");
    public static final SimpleOverlayRenderer MAINTENANCE_OVERLAY_TAPED = new SimpleOverlayRenderer("overlay/machine/overlay_maintenance_taped");
    public static final SimpleOverlayRenderer MAINTENANCE_OVERLAY_FULL_AUTO = new SimpleOverlayRenderer("overlay/machine/overlay_maintenance_full_auto");
    public static final SimpleOverlayRenderer MAINTENANCE_OVERLAY_CONFIGURABLE = new SimpleOverlayRenderer("overlay/machine/overlay_maintenance_configurable");
    public static final SimpleOverlayRenderer MUFFLER_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_muffler");
    public static final SimpleOverlayRenderer STEAM_MINER_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_steam_miner");
    public static final SimpleOverlayRenderer CHUNK_MINER_OVERLAY = new SimpleOverlayRenderer("overlay/machine/overlay_chunk_miner");
    public static final SimpleOverlayRenderer BLANK_SCREEN = new SimpleOverlayRenderer("overlay/machine/overlay_blank_screen");

    public static final SimpleOverlayRenderer COVER_INTERFACE_FLUID = new SimpleOverlayRenderer("cover/cover_interface_fluid");
    public static final SimpleOverlayRenderer COVER_INTERFACE_FLUID_GLASS = new SimpleOverlayRenderer("cover/cover_interface_fluid_glass");
    public static final SimpleOverlayRenderer COVER_INTERFACE_ITEM = new SimpleOverlayRenderer("cover/cover_interface_item");
    public static final SimpleOverlayRenderer COVER_INTERFACE_ENERGY = new SimpleOverlayRenderer("cover/cover_interface_energy");
    public static final SimpleOverlayRenderer COVER_INTERFACE_MACHINE_ON = new SimpleOverlayRenderer("cover/cover_interface_machine_on");
    public static final SimpleOverlayRenderer COVER_INTERFACE_MACHINE_OFF = new SimpleOverlayRenderer("cover/cover_interface_machine_off");
    public static final SimpleOverlayRenderer COVER_INTERFACE_PROXY = new SimpleOverlayRenderer("cover/cover_interface_proxy");
    public static final SimpleOverlayRenderer COVER_INTERFACE_WIRELESS = new SimpleOverlayRenderer("cover/cover_interface_wireless");


    static {
        for (int i = 0; i < VOLTAGE_CASINGS.length; i++) {
            String voltageName = GTValues.VN[i].toLowerCase();
            VOLTAGE_CASINGS[i] = new SimpleSidedCubeRenderer("casings/voltage/" + voltageName);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void register(TextureMap textureMap) {
        GTLog.logger.info("Loading meta tile entity texture sprites...");
        for (IIconRegister iconRegister : iconRegisters) {
            iconRegister.registerIcons(textureMap);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void renderFace(CCRenderState renderState, Matrix4 translation, IVertexOperation[] ops, EnumFacing face, Cuboid6 bounds, TextureAtlasSprite sprite) {
        if (ops.length > 0 && ops[0] instanceof GTBlockOperation) {
            GTBlockOperation op = (GTBlockOperation) ops[0];
            if (op.layer == BloomEffectUtil.BLOOM || !op.shouldSideBeRendered(face, bounds)) {
                return;
            }
        }
        renderFaceRaw(renderState, translation, ops, face, bounds, sprite);
    }

    @SideOnly(Side.CLIENT)
    public static void renderFaceRaw(CCRenderState renderState, Matrix4 translation, IVertexOperation[] ops, EnumFacing face, Cuboid6 bounds, TextureAtlasSprite sprite) {
        BlockFace blockFace = blockFaces.get();
        blockFace.loadCuboidFace(bounds, face.getIndex());
        UVTransformationList uvList = new UVTransformationList(new IconTransformation(sprite));
        if (face.getIndex() == 0) {
            uvList.prepend(new UVMirror(0, 0, bounds.min.z, bounds.max.z));
        }
        renderState.setPipeline(blockFace, 0, blockFace.verts.length,
                ArrayUtils.addAll(ops, new TransformationList(translation), uvList));
        renderState.render();
    }

    @SideOnly(Side.CLIENT)
    public static void renderFaceBloom(CCRenderState renderState, Matrix4 translation, IVertexOperation[] ops, EnumFacing face, Cuboid6 bounds, TextureAtlasSprite sprite) {
        if (ops.length > 0 && ops[0] instanceof GTBlockOperation) {
            GTBlockOperation op = (GTBlockOperation) ops[0];
            if (op.layer != BloomEffectUtil.getRealBloomLayer() || !op.shouldSideBeRendered(face, bounds)) {
                return;
            }
        }
        renderFaceRaw(renderState, translation, ops, face, bounds, sprite);
    }
}
