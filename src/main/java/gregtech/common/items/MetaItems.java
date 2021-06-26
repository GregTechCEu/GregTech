package gregtech.common.items;

import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.materialitem.MaterialMetaItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.util.GTLog;
import gregtech.common.render.FacadeItemModel;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class MetaItems {

    private MetaItems() {
    }

    public static List<MetaItem<?>> ITEMS = MetaItem.getMetaItems();

    public static MetaItem<?>.MetaValueItem CREDIT_COPPER;
    public static MetaItem<?>.MetaValueItem CREDIT_CUPRONICKEL;
    public static MetaItem<?>.MetaValueItem CREDIT_SILVER;
    public static MetaItem<?>.MetaValueItem CREDIT_GOLD;
    public static MetaItem<?>.MetaValueItem CREDIT_PLATINUM;
    public static MetaItem<?>.MetaValueItem CREDIT_OSMIUM;
    public static MetaItem<?>.MetaValueItem CREDIT_NAQUADAH;
    public static MetaItem<?>.MetaValueItem CREDIT_DARMSTADTIUM;

    public static MetaItem<?>.MetaValueItem COIN_GOLD_ANCIENT;
    public static MetaItem<?>.MetaValueItem COIN_DOGE;
    public static MetaItem<?>.MetaValueItem COIN_CHOCOLATE;

    public static MetaItem<?>.MetaValueItem COMPRESSED_CLAY;
    public static MetaItem<?>.MetaValueItem COMPRESSED_FIRECLAY;
    public static MetaItem<?>.MetaValueItem FIRECLAY_BRICK;
    public static MetaItem<?>.MetaValueItem COKE_OVEN_BRICK;

    public static MetaItem<?>.MetaValueItem WOODEN_FORM_EMPTY;
    public static MetaItem<?>.MetaValueItem WOODEN_FORM_BRICK;

    public static MetaItem<?>.MetaValueItem SHAPE_EMPTY;

    public static MetaItem<?>.MetaValueItem[] SHAPE_MOLDS = new MetaValueItem[13];
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_PLATE;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_GEAR;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_CREDIT;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_BOTTLE;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_INGOT;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_BALL;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_BLOCK;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_NUGGET;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_CYLINDER;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_ANVIL;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_NAME;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_GEAR_SMALL;
    public static MetaItem<?>.MetaValueItem SHAPE_MOLD_ROTOR;

    public static MetaItem<?>.MetaValueItem[] SHAPE_EXTRUDERS = new MetaValueItem[22];
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_PLATE;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_ROD;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_BOLT;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_RING;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_CELL;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_INGOT;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_WIRE;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_PIPE_TINY;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_PIPE_SMALL;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_PIPE_MEDIUM;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_PIPE_LARGE;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_BLOCK;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_SWORD;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_PICKAXE;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_SHOVEL;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_AXE;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_HOE;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_HAMMER;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_FILE;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_SAW;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_GEAR;
    public static MetaItem<?>.MetaValueItem SHAPE_EXTRUDER_BOTTLE;

    public static MetaItem<?>.MetaValueItem SPRAY_EMPTY;

    public static MetaItem<?>.MetaValueItem LARGE_FLUID_CELL_STEEL;
    public static MetaItem<?>.MetaValueItem LARGE_FLUID_CELL_TUNGSTEN_STEEL;

    public static MetaItem<?>.MetaValueItem TOOL_MATCHES;
    public static MetaItem<?>.MetaValueItem TOOL_MATCHBOX;
    public static MetaItem<?>.MetaValueItem TOOL_LIGHTER_INVAR;
    public static MetaItem<?>.MetaValueItem TOOL_LIGHTER_PLATINUM;

    public static MetaItem<?>.MetaValueItem INGOT_MIXED_METAL;
    public static MetaItem<?>.MetaValueItem ADVANCED_ALLOY_PLATE;

    public static MetaItem<?>.MetaValueItem INGOT_IRIDIUM_ALLOY;
    public static MetaItem<?>.MetaValueItem PLATE_IRIDIUM_ALLOY;

    public static MetaItem<?>.MetaValueItem CARBON_FIBERS;
    public static MetaItem<?>.MetaValueItem CARBON_MESH;
    public static MetaItem<?>.MetaValueItem CARBON_PLATE;
    public static MetaItem<?>.MetaValueItem GLASS_FIBER;

    public static MetaItem<?>.MetaValueItem NEUTRON_REFLECTOR;

    public static MetaItem<?>.MetaValueItem BATTERY_HULL_LV;
    public static MetaItem<?>.MetaValueItem BATTERY_HULL_MV;
    public static MetaItem<?>.MetaValueItem BATTERY_HULL_HV;

    public static MetaItem<?>.MetaValueItem BATTERY_RE_ULV_TANTALUM;
    public static MetaItem<?>.MetaValueItem BATTERY_SU_LV_SULFURIC_ACID;
    public static MetaItem<?>.MetaValueItem BATTERY_SU_LV_MERCURY;
    public static MetaItem<?>.MetaValueItem BATTERY_RE_LV_CADMIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_RE_LV_LITHIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_RE_LV_SODIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_SU_MV_SULFURIC_ACID;
    public static MetaItem<?>.MetaValueItem BATTERY_SU_MV_MERCURY;
    public static MetaItem<?>.MetaValueItem BATTERY_RE_MV_CADMIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_RE_MV_LITHIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_RE_MV_SODIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_SU_HV_SULFURIC_ACID;
    public static MetaItem<?>.MetaValueItem BATTERY_SU_HV_MERCURY;
    public static MetaItem<?>.MetaValueItem BATTERY_RE_HV_CADMIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_RE_HV_LITHIUM;
    public static MetaItem<?>.MetaValueItem BATTERY_RE_HV_SODIUM;
    public static MetaItem<?>.MetaValueItem ENERGY_CRYSTAL;
    public static MetaItem<?>.MetaValueItem LAPOTRON_CRYSTAL;

    public static MetaItem<?>.MetaValueItem ENERGY_LAPOTRONIC_ORB;
    public static MetaItem<?>.MetaValueItem ENERGY_LAPOTRONIC_ORB2;
    public static MetaItem<?>.MetaValueItem ZPM;
    public static MetaItem<?>.MetaValueItem ZPM2;

    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_LV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_MV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_HV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_EV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_IV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_LUV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_ZPM;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_UV;

    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_LV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_MV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_HV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_EV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_IV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_LUV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_ZPM;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_UV;

    public static MetaItem<?>.MetaValueItem FLUID_FILTER;

    public static MetaItem<?>.MetaValueItem DYNAMITE;

    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_LV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_MV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_HV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_EV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_IV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_LUV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_ZPM;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_UV;

    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_LV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_MV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_HV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_EV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_IV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_LUV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_ZPM;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_UV;

    public static MetaItem<?>.MetaValueItem ROBOT_ARM_LV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_MV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_HV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_EV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_IV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_LUV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_ZPM;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_UV;

    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_LV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_MV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_HV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_EV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_IV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_LUV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_ZPM;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_UV;

    public static MetaItem<?>.MetaValueItem EMITTER_LV;
    public static MetaItem<?>.MetaValueItem EMITTER_MV;
    public static MetaItem<?>.MetaValueItem EMITTER_HV;
    public static MetaItem<?>.MetaValueItem EMITTER_EV;
    public static MetaItem<?>.MetaValueItem EMITTER_IV;
    public static MetaItem<?>.MetaValueItem EMITTER_LUV;
    public static MetaItem<?>.MetaValueItem EMITTER_ZPM;
    public static MetaItem<?>.MetaValueItem EMITTER_UV;

    public static MetaItem<?>.MetaValueItem SENSOR_LV;
    public static MetaItem<?>.MetaValueItem SENSOR_MV;
    public static MetaItem<?>.MetaValueItem SENSOR_HV;
    public static MetaItem<?>.MetaValueItem SENSOR_EV;
    public static MetaItem<?>.MetaValueItem SENSOR_IV;
    public static MetaItem<?>.MetaValueItem SENSOR_LUV;
    public static MetaItem<?>.MetaValueItem SENSOR_ZPM;
    public static MetaItem<?>.MetaValueItem SENSOR_UV;

    public static MetaItem<?>.MetaValueItem TOOL_DATA_STICK;
    public static MetaItem<?>.MetaValueItem TOOL_DATA_ORB;

    public static MetaItem<?>.MetaValueItem GLOWSTONE_BOULE;
    public static MetaItem<?>.MetaValueItem NAQUADAH_BOULE;
    public static MetaItem<?>.MetaValueItem SILICON_BOULE;
    public static MetaItem<?>.MetaValueItem SILICON_WAFER;
    public static MetaItem<?>.MetaValueItem GLOWSTONE_WAFER;
    public static MetaItem<?>.MetaValueItem NAQUADAH_WAFER;

    public static MetaItem<?>.MetaValueItem ADVANCED_SYSTEM_ON_CHIP_WAFER;
    public static MetaItem<?>.MetaValueItem INTEGRATED_LOGIC_CIRCUIT_WAFER;
    public static MetaItem<?>.MetaValueItem CENTRAL_PROCESSING_UNIT_WAFER;
    public static MetaItem<?>.MetaValueItem HIGH_POWER_INTEGRATED_CIRCUIT_WAFER;
    public static MetaItem<?>.MetaValueItem NAND_MEMORY_CHIP_WAFER;
    public static MetaItem<?>.MetaValueItem NANO_CENTRAL_PROCESSING_UNIT_WAFER;
    public static MetaItem<?>.MetaValueItem NOR_MEMORY_CHIP_WAFER;
    public static MetaItem<?>.MetaValueItem POWER_INTEGRATED_CIRCUIT_WAFER;
    public static MetaItem<?>.MetaValueItem QBIT_CENTRAL_PROCESSING_UNIT_WAFER;
    public static MetaItem<?>.MetaValueItem RANDOM_ACCESS_MEMORY_WAFER;
    public static MetaItem<?>.MetaValueItem SYSTEM_ON_CHIP_WAFER;
    public static MetaItem<?>.MetaValueItem ENGRAVED_CRYSTAL_CHIP;
    public static MetaItem<?>.MetaValueItem ENGRAVED_LAPOTRON_CHIP;

    public static MetaItem<?>.MetaValueItem ADVANCED_SYSTEM_ON_CHIP;
    public static MetaItem<?>.MetaValueItem INTEGRATED_LOGIC_CIRCUIT;
    public static MetaItem<?>.MetaValueItem CENTRAL_PROCESSING_UNIT;
    public static MetaItem<?>.MetaValueItem HIGH_POWER_INTEGRATED_CIRCUIT;
    public static MetaItem<?>.MetaValueItem NAND_MEMORY_CHIP;
    public static MetaItem<?>.MetaValueItem NANO_CENTRAL_PROCESSING_UNIT;
    public static MetaItem<?>.MetaValueItem NOR_MEMORY_CHIP;
    public static MetaItem<?>.MetaValueItem POWER_INTEGRATED_CIRCUIT;
    public static MetaItem<?>.MetaValueItem QBIT_CENTRAL_PROCESSING_UNIT;
    public static MetaItem<?>.MetaValueItem RANDOM_ACCESS_MEMORY;
    public static MetaItem<?>.MetaValueItem SYSTEM_ON_CHIP;
    public static MetaItem<?>.MetaValueItem CRYSTAL_CENTRAL_PROCESSING_UNIT;
    public static MetaItem<?>.MetaValueItem CRYSTAL_SYSTEM_ON_CHIP;

    public static MetaItem<?>.MetaValueItem COATED_BOARD;
    public static MetaItem<?>.MetaValueItem EPOXY_BOARD;
    public static MetaItem<?>.MetaValueItem FIBER_BOARD;
    public static MetaItem<?>.MetaValueItem MULTILAYER_FIBER_BOARD;
    public static MetaItem<?>.MetaValueItem PHENOLIC_BOARD;
    public static MetaItem<?>.MetaValueItem PLASTIC_BOARD;
    public static MetaItem<?>.MetaValueItem WETWARE_BOARD;

    public static MetaItem<?>.MetaValueItem VACUUM_TUBE;
    public static MetaItem<?>.MetaValueItem GLASS_TUBE;
    public static MetaItem<?>.MetaValueItem RESISTOR;
    public static MetaItem<?>.MetaValueItem DIODE;
    public static MetaItem<?>.MetaValueItem CAPACITOR;
    public static MetaItem<?>.MetaValueItem SMALL_COIL;
    public static MetaItem<?>.MetaValueItem SMD_CAPACITOR;
    public static MetaItem<?>.MetaValueItem SMD_DIODE;
    public static MetaItem<?>.MetaValueItem SMD_RESISTOR;
    public static MetaItem<?>.MetaValueItem SMD_TRANSISTOR;
    public static MetaItem<?>.MetaValueItem TRANSISTOR;

    public static MetaItem<?>.MetaValueItem BASIC_CIRCUIT_LV;
    public static MetaItem<?>.MetaValueItem BASIC_ELECTRONIC_CIRCUIT_LV;
    public static MetaItem<?>.MetaValueItem ADVANCED_CIRCUIT_PARTS_LV;
    public static MetaItem<?>.MetaValueItem GOOD_INTEGRATED_CIRCUIT_MV;
    public static MetaItem<?>.MetaValueItem ADVANCED_CIRCUIT_MV;
    public static MetaItem<?>.MetaValueItem PROCESSOR_ASSEMBLY_HV;
    public static MetaItem<?>.MetaValueItem NANO_PROCESSOR_HV;
    public static MetaItem<?>.MetaValueItem NANO_PROCESSOR_ASSEMBLY_EV;
    public static MetaItem<?>.MetaValueItem QUANTUM_PROCESSOR_EV;
    public static MetaItem<?>.MetaValueItem DATA_CONTROL_CIRCUIT_IV;
    public static MetaItem<?>.MetaValueItem CRYSTAL_PROCESSOR_IV;
    public static MetaItem<?>.MetaValueItem ENERGY_FLOW_CIRCUIT_LUV;
    public static MetaItem<?>.MetaValueItem WETWARE_PROCESSOR_LUV;
    public static MetaItem<?>.MetaValueItem WETWARE_PROCESSOR_ASSEMBLY_ZPM;
    public static MetaItem<?>.MetaValueItem WETWARE_SUPER_COMPUTER_UV;
    public static MetaItem<?>.MetaValueItem WETWARE_MAINFRAME_MAX;

    public static MetaItem<?>.MetaValueItem COMPONENT_SAW_BLADE_DIAMOND;
    public static MetaItem<?>.MetaValueItem COMPONENT_GRINDER_DIAMOND;
    public static MetaItem<?>.MetaValueItem COMPONENT_GRINDER_TUNGSTEN;

    public static MetaItem<?>.MetaValueItem QUANTUM_EYE;
    public static MetaItem<?>.MetaValueItem QUANTUM_STAR;
    public static MetaItem<?>.MetaValueItem GRAVI_STAR;

    public static MetaItem<?>.MetaValueItem ITEM_FILTER;
    public static MetaItem<?>.MetaValueItem ORE_DICTIONARY_FILTER;
    public static MetaItem<?>.MetaValueItem SMART_FILTER;

    public static MetaItem<?>.MetaValueItem COVER_SHUTTER;
    public static MetaItem<?>.MetaValueItem COVER_MACHINE_CONTROLLER;
    public static MetaItem<?>.MetaValueItem COVER_FACADE;

    public static MetaItem<?>.MetaValueItem COVER_ACTIVITY_DETECTOR;
    public static MetaItem<?>.MetaValueItem COVER_FLUID_DETECTOR;
    public static MetaItem<?>.MetaValueItem COVER_ITEM_DETECTOR;
    public static MetaItem<?>.MetaValueItem COVER_ENERGY_DETECTOR;

    public static MetaItem<?>.MetaValueItem COVER_SCREEN;
    public static MetaItem<?>.MetaValueItem COVER_CRAFTING;
    public static MetaItem<?>.MetaValueItem COVER_DRAIN;

    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL;
    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL_ULV;
    public static MetaItem<?>.MetaValueItem COVER_SOLAR_PANEL_LV;

    public static MetaItem<?>.MetaValueItem INTEGRATED_CIRCUIT;

    public static MetaItem<?>.MetaValueItem FLUID_CELL;

    public static MetaItem<?>.MetaValueItem FOAM_SPRAYER;

    public static MetaItem<?>.MetaValueItem GELLED_TOLUENE;

    public static MetaItem<?>.MetaValueItem BOTTLE_PURPLE_DRINK;

    public static MetaItem<?>.MetaValueItem DYE_INDIGO;
    public static MetaItem<?>.MetaValueItem PLANT_BALL;
    public static MetaItem<?>.MetaValueItem RUBBER_DROP;
    public static MetaItem<?>.MetaValueItem ENERGIUM_DUST;

    public static MetaItem<?>.MetaValueItem POWER_UNIT_LV;
    public static MetaItem<?>.MetaValueItem POWER_UNIT_MV;
    public static MetaItem<?>.MetaValueItem POWER_UNIT_HV;
    public static MetaItem<?>.MetaValueItem JACKHAMMER_BASE;

    public static MetaItem<?>.MetaValueItem NANO_SABER;
    public static MetaItem<?>.MetaValueItem ENERGY_FIELD_PROJECTOR;
    public static MetaItem<?>.MetaValueItem SCANNER;

    public static MetaItem<?>.MetaValueItem[] DYE_ONLY_ITEMS = new MetaItem.MetaValueItem[EnumDyeColor.values().length];
    public static MetaItem<?>.MetaValueItem[] SPRAY_CAN_DYES = new MetaItem.MetaValueItem[EnumDyeColor.values().length];

    public static MetaItem<?>.MetaValueItem TURBINE_ROTOR;

    public static ArmorMetaItem.ArmorMetaValueItem REBREATHER;

    public static ToolMetaItem<?>.MetaToolValueItem SWORD;
    public static ToolMetaItem<?>.MetaToolValueItem PICKAXE;
    public static ToolMetaItem<?>.MetaToolValueItem SHOVEL;
    public static ToolMetaItem<?>.MetaToolValueItem AXE;
    public static ToolMetaItem<?>.MetaToolValueItem HOE;
    public static ToolMetaItem<?>.MetaToolValueItem SAW;
    public static ToolMetaItem<?>.MetaToolValueItem HARD_HAMMER;
    public static ToolMetaItem<?>.MetaToolValueItem SOFT_HAMMER;
    public static ToolMetaItem<?>.MetaToolValueItem WRENCH;
    public static ToolMetaItem<?>.MetaToolValueItem FILE;
    public static ToolMetaItem<?>.MetaToolValueItem CROWBAR;
    public static ToolMetaItem<?>.MetaToolValueItem SCREWDRIVER;
    public static ToolMetaItem<?>.MetaToolValueItem MORTAR;
    public static ToolMetaItem<?>.MetaToolValueItem WIRE_CUTTER;
    public static ToolMetaItem<?>.MetaToolValueItem SCOOP;
    public static ToolMetaItem<?>.MetaToolValueItem BRANCH_CUTTER;
    public static ToolMetaItem<?>.MetaToolValueItem UNIVERSAL_SPADE;
    public static ToolMetaItem<?>.MetaToolValueItem KNIFE;
    public static ToolMetaItem<?>.MetaToolValueItem BUTCHERY_KNIFE;
    public static ToolMetaItem<?>.MetaToolValueItem SENSE;
    public static ToolMetaItem<?>.MetaToolValueItem PLUNGER;
    public static ToolMetaItem<?>.MetaToolValueItem DRILL_LV;
    public static ToolMetaItem<?>.MetaToolValueItem DRILL_MV;
    public static ToolMetaItem<?>.MetaToolValueItem DRILL_HV;
    public static ToolMetaItem<?>.MetaToolValueItem CHAINSAW_LV;
    public static ToolMetaItem<?>.MetaToolValueItem CHAINSAW_MV;
    public static ToolMetaItem<?>.MetaToolValueItem CHAINSAW_HV;
    public static ToolMetaItem<?>.MetaToolValueItem WRENCH_LV;
    public static ToolMetaItem<?>.MetaToolValueItem WRENCH_MV;
    public static ToolMetaItem<?>.MetaToolValueItem WRENCH_HV;
    public static ToolMetaItem<?>.MetaToolValueItem JACKHAMMER;
    public static ToolMetaItem<?>.MetaToolValueItem BUZZSAW;
    public static ToolMetaItem<?>.MetaToolValueItem SCREWDRIVER_LV;

    public static void init() {
        MetaItem1 first = new MetaItem1();
        first.setRegistryName("meta_item_1");
        MetaItem2 second = new MetaItem2();
        second.setRegistryName("meta_item_2");
        MetaTool tool = new MetaTool();
        tool.setRegistryName("meta_tool");
        MetaArmor armor = new MetaArmor();
        armor.setRegistryName("meta_armor");
    }

    public static void registerOreDict() {
        for (MetaItem<?> item : ITEMS) {
            if (item instanceof MaterialMetaItem) {
                ((MaterialMetaItem) item).registerOreDict();
            }
        }
    }

    public static void registerRecipes() {
        for (MetaItem<?> item : ITEMS) {
            if (item instanceof MetaItem1)
                ((MetaItem1) item).registerRecipes();
            if (item instanceof MetaItem2)
                ((MetaItem2) item).registerRecipes();
            if (item instanceof MetaTool)
                ((MetaTool) item).registerRecipes();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerModels() {
        MinecraftForge.EVENT_BUS.register(MetaItems.class);
        for (MetaItem<?> item : ITEMS) {
            item.registerModels();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void registerColors() {
        for (MetaItem<?> item : ITEMS) {
            item.registerColor();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerBakedModels(ModelBakeEvent event) {
        GTLog.logger.info("Registering special item models");
        registerSpecialItemModel(event, COVER_FACADE, new FacadeItemModel());
    }

    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    private static void registerSpecialItemModel(ModelBakeEvent event, MetaValueItem metaValueItem, IBakedModel bakedModel) {
        //god these casts when intellij says you're fine but compiler complains about shit boundaries
        //noinspection RedundantCast
        ResourceLocation modelPath = ((MetaItem) metaValueItem.getMetaItem()).createItemModelPath(metaValueItem, "");
        ModelResourceLocation modelResourceLocation = new ModelResourceLocation(modelPath, "inventory");
        event.getModelRegistry().putObject(modelResourceLocation, bakedModel);
    }
}
