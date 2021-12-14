package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.*;
import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.api.items.metaitem.stats.IItemContainerItemProvider;
import gregtech.api.sound.GTSounds;
import gregtech.api.terminal.hardware.HardwareProvider;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterial;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.MarkerMaterials.Component;
import gregtech.api.unification.material.MarkerMaterials.Tier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.RandomPotionEffect;
import gregtech.common.ConfigHolder;
import gregtech.common.items.behaviors.*;
import gregtech.common.items.behaviors.monitorplugin.AdvancedMonitorPluginBehavior;
import gregtech.common.items.behaviors.monitorplugin.FakeGuiPluginBehavior;
import gregtech.common.items.behaviors.monitorplugin.OnlinePicPluginBehavior;
import gregtech.common.items.behaviors.monitorplugin.TextPluginBehavior;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

import static gregtech.api.util.DyeUtil.getOredictColorName;
import static gregtech.common.items.MetaItems.*;

public class MetaItem1 extends StandardMetaItem {

    public MetaItem1() {
        super();
    }

    @Override
    public void registerSubItems() {
        // Credits: ID 0-10
        CREDIT_COPPER = addItem(0, "credit.copper");
        CREDIT_CUPRONICKEL = addItem(1, "credit.cupronickel");
        CREDIT_SILVER = addItem(2, "credit.silver").setRarity(EnumRarity.UNCOMMON);
        CREDIT_GOLD = addItem(3, "credit.gold").setRarity(EnumRarity.UNCOMMON);
        CREDIT_PLATINUM = addItem(4, "credit.platinum").setRarity(EnumRarity.RARE);
        CREDIT_OSMIUM = addItem(5, "credit.osmium").setRarity(EnumRarity.RARE);
        CREDIT_NAQUADAH = addItem(6, "credit.naquadah").setRarity(EnumRarity.EPIC);
        CREDIT_NEUTRONIUM = addItem(7, "credit.neutronium") .setRarity(EnumRarity.EPIC);

        COIN_GOLD_ANCIENT = addItem(8, "coin.gold.ancient")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Gold, GTValues.M / 4))).setRarity(EnumRarity.RARE);
        COIN_DOGE = addItem(9, "coin.doge")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Brass, GTValues.M / 4))).setRarity(EnumRarity.EPIC);
        COIN_CHOCOLATE = addItem(10, "coin.chocolate")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Gold, GTValues.M / 4)))
                .addComponents(new FoodStats(1, 0.1F, false, true, OreDictUnifier.get(OrePrefix.foil, Materials.Gold), new RandomPotionEffect(MobEffects.SPEED, 200, 1, 10)));

        // Solidifier Shapes: ID 11-30
        SHAPE_EMPTY = addItem(11, "shape.empty").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));

        SHAPE_MOLDS[0] = SHAPE_MOLD_PLATE = addItem(12, "shape.mold.plate").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_MOLDS[1] = SHAPE_MOLD_GEAR = addItem(13, "shape.mold.gear").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_MOLDS[2] = SHAPE_MOLD_CREDIT = addItem(14, "shape.mold.credit").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_MOLDS[3] = SHAPE_MOLD_BOTTLE = addItem(15, "shape.mold.bottle").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_MOLDS[4] = SHAPE_MOLD_INGOT = addItem(16, "shape.mold.ingot").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_MOLDS[5] = SHAPE_MOLD_BALL = addItem(17, "shape.mold.ball").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_MOLDS[6] = SHAPE_MOLD_BLOCK = addItem(18, "shape.mold.block").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_MOLDS[7] = SHAPE_MOLD_NUGGET = addItem(19, "shape.mold.nugget").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_MOLDS[8] = SHAPE_MOLD_CYLINDER = addItem(20, "shape.mold.cylinder").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_MOLDS[9] = SHAPE_MOLD_ANVIL = addItem(21, "shape.mold.anvil").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_MOLDS[10] = SHAPE_MOLD_NAME = addItem(22, "shape.mold.name").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_MOLDS[11] = SHAPE_MOLD_GEAR_SMALL = addItem(23, "shape.mold.gear.small").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_MOLDS[12] = SHAPE_MOLD_ROTOR = addItem(24, "shape.mold.rotor").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));

        // Extruder Shapes: ID 31-60
        SHAPE_EXTRUDERS[0] = SHAPE_EXTRUDER_PLATE = addItem(31, "shape.extruder.plate").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[1] = SHAPE_EXTRUDER_ROD = addItem(32, "shape.extruder.rod").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[2] = SHAPE_EXTRUDER_BOLT = addItem(33, "shape.extruder.bolt").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[3] = SHAPE_EXTRUDER_RING = addItem(34, "shape.extruder.ring").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[4] = SHAPE_EXTRUDER_CELL = addItem(35, "shape.extruder.cell").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[5] = SHAPE_EXTRUDER_INGOT = addItem(36, "shape.extruder.ingot").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[6] = SHAPE_EXTRUDER_WIRE = addItem(37, "shape.extruder.wire").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[7] = SHAPE_EXTRUDER_PIPE_TINY = addItem(38, "shape.extruder.pipe.tiny").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[8] = SHAPE_EXTRUDER_PIPE_SMALL = addItem(39, "shape.extruder.pipe.small").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[9] = SHAPE_EXTRUDER_PIPE_MEDIUM = addItem(40, "shape.extruder.pipe.medium").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[10] = SHAPE_EXTRUDER_PIPE_LARGE = addItem(41, "shape.extruder.pipe.large").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[11] = SHAPE_EXTRUDER_PIPE_HUGE = addItem(42, "shape.extruder.pipe.huge").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[12] = SHAPE_EXTRUDER_BLOCK = addItem(43, "shape.extruder.block").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[13] = SHAPE_EXTRUDER_SWORD = addItem(44, "shape.extruder.sword").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[14] = SHAPE_EXTRUDER_PICKAXE = addItem(45, "shape.extruder.pickaxe").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[15] = SHAPE_EXTRUDER_SHOVEL = addItem(46, "shape.extruder.shovel").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[16] = SHAPE_EXTRUDER_AXE = addItem(47, "shape.extruder.axe").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[17] = SHAPE_EXTRUDER_HOE = addItem(48, "shape.extruder.hoe").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[18] = SHAPE_EXTRUDER_HAMMER = addItem(49, "shape.extruder.hammer").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[19] = SHAPE_EXTRUDER_FILE = addItem(50, "shape.extruder.file").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[20] = SHAPE_EXTRUDER_SAW = addItem(51, "shape.extruder.saw").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[21] = SHAPE_EXTRUDER_GEAR = addItem(52, "shape.extruder.gear").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[22] = SHAPE_EXTRUDER_BOTTLE = addItem(53, "shape.extruder.bottle").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[23] = SHAPE_EXTRUDER_FOIL = addItem(54, "shape.extruder.foil").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[24] = SHAPE_EXTRUDER_GEAR_SMALL = addItem(55, "shape.extruder.gear_small").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[25] = SHAPE_EXTRUDER_ROD_LONG = addItem(56, "shape.extruder.rod_long").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));
        SHAPE_EXTRUDERS[26] = SHAPE_EXTRUDER_ROTOR = addItem(57, "shape.extruder.rotor").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 4)));

        // Spray Cans: ID 61-77
        SPRAY_EMPTY = addItem(61, "spray.empty")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Tin, GTValues.M * 2), new MaterialStack(Materials.Redstone, GTValues.M)));

        for (int i = 0; i < EnumDyeColor.values().length; i++) {
            SPRAY_CAN_DYES[i] = addItem(62 + i, "spray.can.dyes." + EnumDyeColor.values()[i].getName()).setMaxStackSize(1)
                    .addComponents(new ColorSprayBehaviour(SPRAY_EMPTY.getStackForm(), 512, i));
        }

        // Fluid Cells: ID 78-88
        FLUID_CELL = addItem(78, "fluid_cell").addComponents(new FluidStats(1000, Integer.MIN_VALUE, Integer.MAX_VALUE, false));

        FLUID_CELL_UNIVERSAL = addItem(79, "fluid_cell.universal").addComponents(new FluidStats(1000, Integer.MIN_VALUE, Integer.MAX_VALUE, true));

        FLUID_CELL_LARGE_STEEL = addItem(80, "large_fluid_cell.steel")
                .addComponents(new FluidStats(8000, Integer.MIN_VALUE, Integer.MAX_VALUE, true))
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, OrePrefix.ingot.materialAmount * 4L)));

        FLUID_CELL_LARGE_ALUMINIUM = addItem(81, "large_fluid_cell.aluminium")
                .addComponents(new FluidStats(32000, Integer.MIN_VALUE, Integer.MAX_VALUE, true))
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Aluminium, OrePrefix.ingot.materialAmount * 4L)));

        FLUID_CELL_LARGE_STAINLESS_STEEL = addItem(82, "large_fluid_cell.stainless_steel")
                .addComponents(new FluidStats(64000, Integer.MIN_VALUE, Integer.MAX_VALUE, true))
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.StainlessSteel, OrePrefix.ingot.materialAmount * 6L)));

        FLUID_CELL_LARGE_TITANIUM = addItem(83, "large_fluid_cell.titanium")
                .addComponents(new FluidStats(128000, Integer.MIN_VALUE, Integer.MAX_VALUE, true))
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Titanium, OrePrefix.ingot.materialAmount * 6L)));

        FLUID_CELL_LARGE_TUNGSTEN_STEEL = addItem(84, "large_fluid_cell.tungstensteel")
                .addComponents(new FluidStats(512000, Integer.MIN_VALUE, Integer.MAX_VALUE, true))
                .setMaxStackSize(32)
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.TungstenSteel, OrePrefix.ingot.materialAmount * 8L)));

        // Limited-Use Items: ID 89-95

        TOOL_MATCHES = addItem(89, "tool.matches")
                .addComponents(new LighterBehaviour(1));
        TOOL_MATCHBOX = addItem(90, "tool.matchbox")
                .addComponents(new LighterBehaviour(16)).setMaxStackSize(1);
        TOOL_LIGHTER_INVAR = addItem(91, "tool.lighter.invar")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Invar, GTValues.M * 2)))
                .addComponents(new LighterBehaviour(100)).setMaxStackSize(1);
        TOOL_LIGHTER_PLATINUM = addItem(92, "tool.lighter.platinum")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Platinum, GTValues.M * 2)))
                .addComponents(new LighterBehaviour(1000)).setMaxStackSize(1).setRarity(EnumRarity.UNCOMMON);

        BOTTLE_PURPLE_DRINK = addItem(93, "bottle.purple.drink").addComponents(new FoodStats(8, 0.2F, true, true, new ItemStack(Items.GLASS_BOTTLE), new RandomPotionEffect(MobEffects.HASTE, 800, 1, 90)));

        // Voltage Coils: ID 96-110
        VOLTAGE_COIL_ULV = addItem(96, "voltage_coil.ulv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Lead, GTValues.M * 2), new MaterialStack(Materials.IronMagnetic, GTValues.M / 2)));
        VOLTAGE_COIL_LV = addItem(97, "voltage_coil.lv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 2), new MaterialStack(Materials.IronMagnetic, GTValues.M / 2)));
        VOLTAGE_COIL_MV = addItem(98, "voltage_coil.mv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Aluminium, GTValues.M * 2), new MaterialStack(Materials.SteelMagnetic, GTValues.M / 2)));
        VOLTAGE_COIL_HV = addItem(99, "voltage_coil.hv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.BlackSteel, GTValues.M * 2), new MaterialStack(Materials.SteelMagnetic, GTValues.M / 2)));
        VOLTAGE_COIL_EV = addItem(100, "voltage_coil.ev").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.TungstenSteel, GTValues.M * 2), new MaterialStack(Materials.NeodymiumMagnetic, GTValues.M / 2)));
        VOLTAGE_COIL_IV = addItem(101, "voltage_coil.iv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Iridium, GTValues.M * 2), new MaterialStack(Materials.NeodymiumMagnetic, GTValues.M / 2)));
        VOLTAGE_COIL_LUV = addItem(102, "voltage_coil.luv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Osmiridium, GTValues.M * 2), new MaterialStack(Materials.SamariumMagnetic, GTValues.M / 2)));
        VOLTAGE_COIL_ZPM = addItem(103, "voltage_coil.zpm").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Europium, GTValues.M * 2), new MaterialStack(Materials.SamariumMagnetic, GTValues.M / 2)));
        VOLTAGE_COIL_UV = addItem(104, "voltage_coil.uv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.FluxedElectrum, GTValues.M * 2), new MaterialStack(Materials.SamariumMagnetic, GTValues.M / 2)));

        // ???: ID 111-125

        // Motors: ID 126-140
        ELECTRIC_MOTOR_LV = addItem(127, "electric.motor.lv");
        ELECTRIC_MOTOR_MV = addItem(128, "electric.motor.mv");
        ELECTRIC_MOTOR_HV = addItem(129, "electric.motor.hv");
        ELECTRIC_MOTOR_EV = addItem(130, "electric.motor.ev");
        ELECTRIC_MOTOR_IV = addItem(131, "electric.motor.iv");
        ELECTRIC_MOTOR_LUV = addItem(132, "electric.motor.luv");
        ELECTRIC_MOTOR_ZPM = addItem(133, "electric.motor.zpm");
        ELECTRIC_MOTOR_UV = addItem(134, "electric.motor.uv");

        // Pumps: ID 141-155
        PUMPS[0] = ELECTRIC_PUMP_LV = addItem(142, "electric.pump.lv");
        PUMPS[1] = ELECTRIC_PUMP_MV = addItem(143, "electric.pump.mv");
        PUMPS[2] = ELECTRIC_PUMP_HV = addItem(144, "electric.pump.hv");
        PUMPS[3] = ELECTRIC_PUMP_EV = addItem(145, "electric.pump.ev");
        PUMPS[4] = ELECTRIC_PUMP_IV = addItem(146, "electric.pump.iv");
        PUMPS[5] = ELECTRIC_PUMP_LUV = addItem(147, "electric.pump.luv");
        PUMPS[6] = ELECTRIC_PUMP_ZPM = addItem(148, "electric.pump.zpm");
        PUMPS[7] = ELECTRIC_PUMP_UV = addItem(149, "electric.pump.uv");

        // Conveyors: ID 156-170
        CONVEYOR_MODULE_LV = addItem(157, "conveyor.module.lv");
        CONVEYOR_MODULE_MV = addItem(158, "conveyor.module.mv");
        CONVEYOR_MODULE_HV = addItem(159, "conveyor.module.hv");
        CONVEYOR_MODULE_EV = addItem(160, "conveyor.module.ev");
        CONVEYOR_MODULE_IV = addItem(161, "conveyor.module.iv");
        CONVEYOR_MODULE_LUV = addItem(162, "conveyor.module.luv");
        CONVEYOR_MODULE_ZPM = addItem(163, "conveyor.module.zpm");
        CONVEYOR_MODULE_UV = addItem(164, "conveyor.module.uv");

        // Pistons: ID 171-185
        ELECTRIC_PISTON_LV = addItem(172, "electric.piston.lv");
        ELECTRIC_PISTON_MV = addItem(173, "electric.piston.mv");
        ELECTRIC_PISTON_HV = addItem(174, "electric.piston.hv");
        ELECTRIC_PISTON_EV = addItem(175, "electric.piston.ev");
        ELECTRIC_PISTON_IV = addItem(176, "electric.piston.iv");
        ELECTRIC_PISTON_LUV = addItem(177, "electric.piston.luv");
        ELECTRIC_PISTON_ZPM = addItem(178, "electric.piston.zpm");
        ELECTRIC_PISTON_UV = addItem(179, "electric.piston.uv");

        // Robot Arms: ID 186-200
        ROBOT_ARM_LV = addItem(187, "robot.arm.lv");
        ROBOT_ARM_MV = addItem(188, "robot.arm.mv");
        ROBOT_ARM_HV = addItem(189, "robot.arm.hv");
        ROBOT_ARM_EV = addItem(190, "robot.arm.ev");
        ROBOT_ARM_IV = addItem(191, "robot.arm.iv");
        ROBOT_ARM_LUV = addItem(192, "robot.arm.luv");
        ROBOT_ARM_ZPM = addItem(193, "robot.arm.zpm");
        ROBOT_ARM_UV = addItem(194, "robot.arm.uv");

        // Field Generators: ID 201-215
        FIELD_GENERATOR_LV = addItem(202, "field.generator.lv");
        FIELD_GENERATOR_MV = addItem(203, "field.generator.mv");
        FIELD_GENERATOR_HV = addItem(204, "field.generator.hv");
        FIELD_GENERATOR_EV = addItem(205, "field.generator.ev");
        FIELD_GENERATOR_IV = addItem(206, "field.generator.iv");
        FIELD_GENERATOR_LUV = addItem(207, "field.generator.luv");
        FIELD_GENERATOR_ZPM = addItem(208, "field.generator.zpm");
        FIELD_GENERATOR_UV = addItem(209, "field.generator.uv");

        // Emitters: ID 216-230
        EMITTER_LV = addItem(217, "emitter.lv");
        EMITTER_MV = addItem(218, "emitter.mv");
        EMITTER_HV = addItem(219, "emitter.hv");
        EMITTER_EV = addItem(220, "emitter.ev");
        EMITTER_IV = addItem(221, "emitter.iv");
        EMITTER_LUV = addItem(222, "emitter.luv");
        EMITTER_ZPM = addItem(223, "emitter.zpm");
        EMITTER_UV = addItem(224, "emitter.uv");

        // Sensors: ID 231-245
        SENSOR_LV = addItem(232, "sensor.lv");
        SENSOR_MV = addItem(233, "sensor.mv");
        SENSOR_HV = addItem(234, "sensor.hv");
        SENSOR_EV = addItem(235, "sensor.ev");
        SENSOR_IV = addItem(236, "sensor.iv");
        SENSOR_LUV = addItem(237, "sensor.luv");
        SENSOR_ZPM = addItem(238, "sensor.zpm");
        SENSOR_UV = addItem(239, "sensor.uv");

        // Fluid Regulators: ID 246-260
        FLUID_REGULATORS[0] = FLUID_REGULATOR_LV = addItem(247, "fluid.regulator.lv");
        FLUID_REGULATORS[1] = FLUID_REGULATOR_MV = addItem(248, "fluid.regulator.mv");
        FLUID_REGULATORS[2] = FLUID_REGULATOR_HV = addItem(249, "fluid.regulator.hv");
        FLUID_REGULATORS[3] = FLUID_REGULATOR_EV = addItem(250, "fluid.regulator.ev");
        FLUID_REGULATORS[4] = FLUID_REGULATOR_IV = addItem(251, "fluid.regulator.iv");
        FLUID_REGULATORS[5] = FLUID_REGULATOR_LUV = addItem(252, "fluid.regulator.luv");
        FLUID_REGULATORS[6] = FLUID_REGULATOR_ZPM = addItem(253, "fluid.regulator.zpm");
        FLUID_REGULATORS[7] = FLUID_REGULATOR_UV = addItem(254, "fluid.regulator.uv");

        // Data Items: ID 261-265
        TOOL_DATA_STICK = addItem(261, "tool.datastick");
        TOOL_DATA_ORB = addItem(262, "tool.dataorb");

        // Special Machine Components: ID 266-280
        COMPONENT_GRINDER_DIAMOND = addItem(266, "component.grinder.diamond")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Steel, GTValues.M * 8), new MaterialStack(Materials.Diamond, GTValues.M * 5)));
        COMPONENT_GRINDER_TUNGSTEN = addItem(267, "component.grinder.tungsten")
                .setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Tungsten, GTValues.M * 4), new MaterialStack(Materials.VanadiumSteel, GTValues.M * 8), new MaterialStack(Materials.Diamond, GTValues.M)));

        // Special Eyes/Stars: ID 281-289
        QUANTUM_EYE = addItem(281, "quantumeye");
        QUANTUM_STAR = addItem(282, "quantumstar");
        GRAVI_STAR = addItem(283, "gravistar");

        // Filters: ID 290-300
        FLUID_FILTER = addItem(290, "fluid_filter");
        ITEM_FILTER = addItem(291, "item_filter");
        ORE_DICTIONARY_FILTER = addItem(292, "ore_dictionary_filter");
        SMART_FILTER = addItem(293, "smart_item_filter");

        // Functional Covers: ID 301-330
        COVER_MACHINE_CONTROLLER = addItem(301, "cover.controller");
        COVER_ACTIVITY_DETECTOR = addItem(302, "cover.activity.detector");
        COVER_FLUID_DETECTOR = addItem(303, "cover.fluid.detector");
        COVER_ITEM_DETECTOR = addItem(304, "cover.item.detector");
        COVER_ENERGY_DETECTOR = addItem(305, "cover.energy.detector");
        COVER_SCREEN = addItem(306, "cover.screen");
        COVER_CRAFTING = addItem(307, "cover.crafting");
        COVER_DRAIN = addItem(308, "cover.drain");
        COVER_SHUTTER = addItem(309, "cover.shutter");
        COVER_INFINITE_WATER = addItem(310, "cover.infinite_water");
        COVER_ENDER_FLUID_LINK = addItem(311, "cover.ender_fluid_link");
        COVER_DIGITAL_INTERFACE = addItem(312, "cover.digital");
        COVER_DIGITAL_INTERFACE_WIRELESS = addItem(313, "cover.digital.wireless");

        COVER_FACADE = addItem(330, "cover.facade").addComponents(new FacadeItem()).disableModelLoading();

        // Solar Panels: ID 331-346
        COVER_SOLAR_PANEL = addItem(331, "cover.solar.panel");
        COVER_SOLAR_PANEL_ULV = addItem(332, "cover.solar.panel.ulv");
        COVER_SOLAR_PANEL_LV = addItem(333, "cover.solar.panel.lv");
        COVER_SOLAR_PANEL_MV = addItem(334, "cover.solar.panel.mv");
        COVER_SOLAR_PANEL_HV = addItem(335, "cover.solar.panel.hv");
        COVER_SOLAR_PANEL_EV = addItem(336, "cover.solar.panel.ev");
        COVER_SOLAR_PANEL_IV = addItem(337, "cover.solar.panel.iv");
        COVER_SOLAR_PANEL_LUV = addItem(338, "cover.solar.panel.luv");
        COVER_SOLAR_PANEL_ZPM = addItem(339, "cover.solar.panel.zpm");
        COVER_SOLAR_PANEL_UV = addItem(340, "cover.solar.panel.uv");
        // MAX-tier solar panel?

        if (!ConfigHolder.machines.enableHighTierSolars) {
            COVER_SOLAR_PANEL_IV.setInvisible();
            COVER_SOLAR_PANEL_LUV.setInvisible();
            COVER_SOLAR_PANEL_ZPM.setInvisible();
            COVER_SOLAR_PANEL_UV.setInvisible();
        }

        // Early Game Brick Related: ID 347-360
        IItemContainerItemProvider selfContainerItemProvider = itemStack -> itemStack;
        WOODEN_FORM_EMPTY = addItem(347, "wooden_form.empty");
        WOODEN_FORM_BRICK = addItem(348, "wooden_form.brick").addComponents(selfContainerItemProvider);
        COMPRESSED_CLAY = addItem(349, "compressed.clay");
        COMPRESSED_COKE_CLAY = addItem(350, "compressed.coke_clay");
        COMPRESSED_FIRECLAY = addItem(351, "compressed.fireclay");
        FIRECLAY_BRICK = addItem(352, "brick.fireclay");
        COKE_OVEN_BRICK = addItem(353, "brick.coke");

        if (!ConfigHolder.recipes.hardMiscRecipes)
            COMPRESSED_CLAY.setInvisible();

        // Boules: ID 361-370
        SILICON_BOULE = addItem(361, "boule.silicon");
        GLOWSTONE_BOULE = addItem(362, "boule.glowstone");
        NAQUADAH_BOULE = addItem(363, "boule.naquadah");
        NEUTRONIUM_BOULE = addItem(364, "boule.neutronium");

        // Boule-Direct Wafers: ID 371-380
        SILICON_WAFER = addItem(371, "wafer.silicon");
        GLOWSTONE_WAFER = addItem(372, "wafer.glowstone");
        NAQUADAH_WAFER = addItem(373, "wafer.naquadah");
        NEUTRONIUM_WAFER = addItem(374, "wafer.neutronium");

        // Unfinished Circuit Boards: ID 381-400
        COATED_BOARD = addItem(381, "board.coated");
        PHENOLIC_BOARD = addItem(382, "board.phenolic");
        PLASTIC_BOARD = addItem(383, "board.plastic");
        EPOXY_BOARD = addItem(384, "board.epoxy");
        FIBER_BOARD = addItem(385, "board.fiber_reinforced");
        MULTILAYER_FIBER_BOARD = addItem(386, "board.multilayer.fiber_reinforced");
        WETWARE_BOARD = addItem(387, "board.wetware");

        // Finished Circuit Boards: ID 401-420
        BASIC_CIRCUIT_BOARD = addItem(401, "circuit_board.basic");
        GOOD_CIRCUIT_BOARD = addItem(402, "circuit_board.good");
        PLASTIC_CIRCUIT_BOARD = addItem(403, "circuit_board.plastic");
        ADVANCED_CIRCUIT_BOARD = addItem(404, "circuit_board.advanced");
        EXTREME_CIRCUIT_BOARD = addItem(405, "circuit_board.extreme");
        ELITE_CIRCUIT_BOARD = addItem(406, "circuit_board.elite");
        WETWARE_CIRCUIT_BOARD = addItem(407, "circuit_board.wetware");

        // Dyes: ID 421-436
        for (int i = 0; i < EnumDyeColor.values().length; i++) {
            EnumDyeColor dyeColor = EnumDyeColor.values()[i];
            DYE_ONLY_ITEMS[i] = addItem(421 + i, "dye." + dyeColor.getName()).addOreDict(getOredictColorName(dyeColor));
        }

        // Plant/Rubber Related: ID 438-445
        RUBBER_DROP = addItem(438, "rubber_drop").setBurnValue(200);
        PLANT_BALL = addItem(439, "plant_ball").setBurnValue(75);
        BIO_CHAFF = addItem(440, "bio_chaff").setBurnValue(200);

        // Power Units: ID 446-459
        POWER_UNIT_LV = addItem(446, "power_unit.lv").addComponents(ElectricStats.createElectricItem(100000L, GTValues.LV)).setMaxStackSize(8);
        POWER_UNIT_MV = addItem(447, "power_unit.mv").addComponents(ElectricStats.createElectricItem(400000L, GTValues.MV)).setMaxStackSize(8);
        POWER_UNIT_HV = addItem(448, "power_unit.hv").addComponents(ElectricStats.createElectricItem(1600000L, GTValues.HV)).setMaxStackSize(8);
        POWER_UNIT_EV = addItem(449, "power_unit.ev").addComponents(ElectricStats.createElectricItem(6400000L, GTValues.EV)).setMaxStackSize(8);
        POWER_UNIT_IV = addItem(450, "power_unit.iv").addComponents(ElectricStats.createElectricItem(25600000L, GTValues.IV)).setMaxStackSize(8);

        // Usable Items: ID 460-490
        DYNAMITE = addItem(460, "dynamite").addComponents(new DynamiteBehaviour()).setMaxStackSize(16);
        INTEGRATED_CIRCUIT = addItem(461, "circuit.integrated").addComponents(new IntCircuitBehaviour()).setModelAmount(33);
        FOAM_SPRAYER = addItem(462, "foam_sprayer").addComponents(new FoamSprayerBehavior()).setMaxStackSize(1);
        NANO_SABER = addItem(463, "nano_saber").addComponents(ElectricStats.createElectricItem(4_000_000L, GTValues.HV)).addComponents(new NanoSaberBehavior()).setMaxStackSize(1);
        CLIPBOARD = addItem(464, "clipboard").addComponents(new ClipboardBehavior()).setMaxStackSize(1);
        TERMINAL = addItem(465, "terminal").addComponents(new HardwareProvider(), new TerminalBehaviour()).setMaxStackSize(1);
        PROSPECTOR_LV = addItem(466, "prospector.lv").addComponents(ElectricStats.createElectricItem(100_000L, GTValues.LV), new ProspectorScannerBehavior(2, GTValues.LV));
        PROSPECTOR_HV = addItem(467, "prospector.hv").addComponents(ElectricStats.createElectricItem(1_600_000L, GTValues.HV), new ProspectorScannerBehavior(3, GTValues.HV));
        PROSPECTOR_LUV = addItem(468, "prospector.luv").addComponents(ElectricStats.createElectricItem(1_000_000_000L, GTValues.LuV), new ProspectorScannerBehavior(5, GTValues.LuV));

        // Misc Crafting Items: ID 491-515
        ENERGIUM_DUST = addItem(491, "energium_dust");
        ENGRAVED_LAPOTRON_CHIP = addItem(492, "engraved.lapotron_chip");
        //Free ID: 493, 494, 495, 496
        NEUTRON_REFLECTOR = addItem(497, "neutron_reflector");
        GELLED_TOLUENE = addItem(498, "gelled_toluene");
        CARBON_FIBERS = addItem(499, "carbon.fibers");
        CARBON_MESH = addItem(500, "carbon.mesh");
        CARBON_PLATE = addItem(501, "carbon.plate");
        DUCT_TAPE = addItem(502, "duct_tape");
        WIRELESS = addItem(503, "wireless");
        CAMERA = addItem(504, "camera");

        // Circuit Components: ID 516-565
        VACUUM_TUBE = addItem(516, "circuit.vacuum_tube").setUnificationData(OrePrefix.circuit, Tier.Primitive);
        GLASS_TUBE = addItem(517, "component.glass.tube");
        SMALL_COIL = addItem(518, "component.small_coil");
        TRANSISTOR = addItem(519, "component.transistor").setUnificationData(OrePrefix.component, Component.Transistor);
        RESISTOR = addItem(520, "component.resistor").setUnificationData(OrePrefix.component, Component.Resistor);
        CAPACITOR = addItem(521, "component.capacitor").setUnificationData(OrePrefix.component, Component.Capacitor);
        DIODE = addItem(522, "component.diode").setUnificationData(OrePrefix.component, Component.Diode);
        SMD_TRANSISTOR = addItem(523, "component.smd.transistor").setUnificationData(OrePrefix.component, Component.Transistor);
        SMD_RESISTOR = addItem(524, "component.smd.resistor").setUnificationData(OrePrefix.component, Component.Resistor);
        SMD_CAPACITOR = addItem(525, "component.smd.capacitor").setUnificationData(OrePrefix.component, Component.Capacitor);
        SMD_DIODE = addItem(526, "component.smd.diode").setUnificationData(OrePrefix.component, Component.Diode);
        ADVANCED_SMD_TRANSISTOR = addItem(527, "component.advanced_smd.transistor");
        ADVANCED_SMD_RESISTOR = addItem(528, "component.advanced_smd.resistor");
        ADVANCED_SMD_CAPACITOR = addItem(529, "component.advanced_smd.capacitor");
        ADVANCED_SMD_DIODE = addItem(530, "component.advanced_smd.diode");

        // Engraved and Complex Wafers: ID 566-590
        CENTRAL_PROCESSING_UNIT_WAFER = addItem(566, "wafer.central_processing_unit");
        RANDOM_ACCESS_MEMORY_WAFER = addItem(567, "wafer.random_access_memory");
        INTEGRATED_LOGIC_CIRCUIT_WAFER = addItem(568, "wafer.integrated_logic_circuit");
        NANO_CENTRAL_PROCESSING_UNIT_WAFER = addItem(569, "wafer.nano_central_processing_unit");
        QUBIT_CENTRAL_PROCESSING_UNIT_WAFER = addItem(570, "wafer.qbit_central_processing_unit");
        SIMPLE_SYSTEM_ON_CHIP_WAFER = addItem(571, "wafer.simple_system_on_chip");
        SYSTEM_ON_CHIP_WAFER = addItem(572, "wafer.system_on_chip");
        ADVANCED_SYSTEM_ON_CHIP_WAFER = addItem(573, "wafer.advanced_system_on_chip");
        HIGHLY_ADVANCED_SOC_WAFER = addItem(574, "wafer.highly_advanced_system_on_chip");
        ULTRA_LOW_POWER_INTEGRATED_CIRCUIT_WAFER = addItem(575, "wafer.ultra_low_power_integrated_circuit");
        LOW_POWER_INTEGRATED_CIRCUIT_WAFER = addItem(576, "wafer.low_power_integrated_circuit");
        POWER_INTEGRATED_CIRCUIT_WAFER = addItem(577, "wafer.power_integrated_circuit");
        HIGH_POWER_INTEGRATED_CIRCUIT_WAFER = addItem(578, "wafer.high_power_integrated_circuit");
        NAND_MEMORY_CHIP_WAFER = addItem(579, "wafer.nand_memory_chip");
        NOR_MEMORY_CHIP_WAFER = addItem(580, "wafer.nor_memory_chip");

        // Engraved and Complex Cut Wafers: ID 591-615
        CENTRAL_PROCESSING_UNIT = addItem(591, "plate.central_processing_unit");
        RANDOM_ACCESS_MEMORY = addItem(592, "plate.random_access_memory");
        INTEGRATED_LOGIC_CIRCUIT = addItem(593, "plate.integrated_logic_circuit");
        NANO_CENTRAL_PROCESSING_UNIT = addItem(594, "plate.nano_central_processing_unit");
        QUBIT_CENTRAL_PROCESSING_UNIT = addItem(595, "plate.qbit_central_processing_unit");
        SIMPLE_SYSTEM_ON_CHIP = addItem(596, "plate.simple_system_on_chip");
        SYSTEM_ON_CHIP = addItem(597, "plate.system_on_chip");
        ADVANCED_SYSTEM_ON_CHIP = addItem(598, "plate.advanced_system_on_chip");
        HIGHLY_ADVANCED_SOC = addItem(599, "plate.highly_advanced_system_on_chip");
        ULTRA_LOW_POWER_INTEGRATED_CIRCUIT = addItem(600, "plate.ultra_low_power_integrated_circuit");
        LOW_POWER_INTEGRATED_CIRCUIT = addItem(601, "plate.low_power_integrated_circuit");
        POWER_INTEGRATED_CIRCUIT = addItem(602, "plate.power_integrated_circuit");
        HIGH_POWER_INTEGRATED_CIRCUIT = addItem(603, "plate.high_power_integrated_circuit");
        NAND_MEMORY_CHIP = addItem(604, "plate.nand_memory_chip");
        NOR_MEMORY_CHIP = addItem(605, "plate.nor_memory_chip");

        // ???: ID 616-620

        // Circuits: ID 621-700

        // T1: Electronic
        ELECTRONIC_CIRCUIT_LV = addItem(621, "circuit.electronic").setUnificationData(OrePrefix.circuit, Tier.Basic);
        ELECTRONIC_CIRCUIT_MV = addItem(622, "circuit.good_electronic").setUnificationData(OrePrefix.circuit, Tier.Good);

        // T2: Integrated
        INTEGRATED_CIRCUIT_LV = addItem(623, "circuit.basic_integrated").setUnificationData(OrePrefix.circuit, Tier.Basic);
        INTEGRATED_CIRCUIT_MV = addItem(624, "circuit.good_integrated").setUnificationData(OrePrefix.circuit, Tier.Good);
        INTEGRATED_CIRCUIT_HV = addItem(625, "circuit.advanced_integrated").setUnificationData(OrePrefix.circuit, Tier.Advanced);

        // Misc Unlocks
        NAND_CHIP_ULV = addItem(626, "circuit.nand_chip").setUnificationData(OrePrefix.circuit, Tier.Primitive);
        MICROPROCESSOR_LV = addItem(627, "circuit.microprocessor").setUnificationData(OrePrefix.circuit, Tier.Basic);

        // T3: Processor
        PROCESSOR_MV = addItem(628, "circuit.processor").setUnificationData(OrePrefix.circuit, Tier.Good);
        PROCESSOR_ASSEMBLY_HV = addItem(629, "circuit.assembly").setUnificationData(OrePrefix.circuit, Tier.Advanced);
        WORKSTATION_EV = addItem(630, "circuit.workstation").setUnificationData(OrePrefix.circuit, Tier.Extreme);
        MAINFRAME_IV = addItem(631, "circuit.mainframe").setUnificationData(OrePrefix.circuit, Tier.Elite);

        // T4: Nano
        NANO_PROCESSOR_HV = addItem(632, "circuit.nano_processor").setUnificationData(OrePrefix.circuit, Tier.Advanced);
        NANO_PROCESSOR_ASSEMBLY_EV = addItem(633, "circuit.nano_assembly").setUnificationData(OrePrefix.circuit, Tier.Extreme);
        NANO_COMPUTER_IV = addItem(634, "circuit.nano_computer").setUnificationData(OrePrefix.circuit, Tier.Elite);
        NANO_MAINFRAME_LUV = addItem(635, "circuit.nano_mainframe").setUnificationData(OrePrefix.circuit, Tier.Master);

        // T5: Quantum
        QUANTUM_PROCESSOR_EV = addItem(636, "circuit.quantum_processor").setUnificationData(OrePrefix.circuit, Tier.Extreme);
        QUANTUM_ASSEMBLY_IV = addItem(637, "circuit.quantum_assembly").setUnificationData(OrePrefix.circuit, Tier.Elite);
        QUANTUM_COMPUTER_LUV = addItem(638, "circuit.quantum_computer").setUnificationData(OrePrefix.circuit, Tier.Master);
        QUANTUM_MAINFRAME_ZPM = addItem(639, "circuit.quantum_mainframe").setUnificationData(OrePrefix.circuit, Tier.Ultimate);

        // T6: Crystal
        CRYSTAL_PROCESSOR_IV = addItem(640, "circuit.crystal_processor").setUnificationData(OrePrefix.circuit, Tier.Elite);
        CRYSTAL_ASSEMBLY_LUV = addItem(641, "circuit.crystal_assembly").setUnificationData(OrePrefix.circuit, Tier.Master);
        CRYSTAL_COMPUTER_ZPM = addItem(642, "circuit.crystal_computer").setUnificationData(OrePrefix.circuit, Tier.Ultimate);
        CRYSTAL_MAINFRAME_UV = addItem(643, "circuit.crystal_mainframe").setUnificationData(OrePrefix.circuit, Tier.Super);

        // T7: Wetware
        WETWARE_PROCESSOR_LUV = addItem(644, "circuit.wetware_processor").setUnificationData(OrePrefix.circuit, Tier.Master);
        WETWARE_PROCESSOR_ASSEMBLY_ZPM = addItem(645, "circuit.wetware_assembly").setUnificationData(OrePrefix.circuit, Tier.Ultimate);
        WETWARE_SUPER_COMPUTER_UV = addItem(646, "circuit.wetware_computer").setUnificationData(OrePrefix.circuit, Tier.Super);
        WETWARE_MAINFRAME_UHV = addItem(647, "circuit.wetware_mainframe").setUnificationData(OrePrefix.circuit, Tier.Infinite);

        // T8: Bioware

        // T9: Optical

        // T10: Exotic

        // T11: Cosmic

        // T12: Supra-Causal

        // T13: ???

        // Crystal Circuit Components: ID 701-705
        RAW_CRYSTAL_CHIP = addItem(701, "crystal.raw");
        RAW_CRYSTAL_CHIP_PART = addItem(702, "crystal.raw_chip");
        ENGRAVED_CRYSTAL_CHIP = addItem(703, "engraved.crystal_chip");
        CRYSTAL_CENTRAL_PROCESSING_UNIT = addItem(704, "crystal.central_processing_unit");
        CRYSTAL_SYSTEM_ON_CHIP = addItem(705, "crystal.system_on_chip");

        // Wetware Circuit Components: ID 706-710
        NEURO_PROCESSOR = addItem(708, "processor.neuro");
        STEM_CELLS = addItem(709, "stem_cells");
        PETRI_DISH = addItem(710, "petri_dish");

        // Turbine Rotors: ID 711-715
        TURBINE_ROTOR = addItem(711, "turbine_rotor").addComponents(new TurbineRotorBehavior());

        // Battery Hulls: ID 716-730
        BATTERY_HULL_LV = addItem(717, "battery.hull.lv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.BatteryAlloy, OrePrefix.plate.materialAmount)));
        BATTERY_HULL_MV = addItem(718, "battery.hull.mv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.BatteryAlloy, OrePrefix.plate.materialAmount * 3L)));
        BATTERY_HULL_HV = addItem(719, "battery.hull.hv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.BatteryAlloy, OrePrefix.plate.materialAmount * 9L)));
        BATTERY_HULL_SMALL_VANADIUM = addItem(720, "battery.hull.ev").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.BlueSteel, GTValues.M * 2)));
        BATTERY_HULL_MEDIUM_VANADIUM = addItem(721, "battery.hull.iv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.RoseGold, GTValues.M * 6)));
        BATTERY_HULL_LARGE_VANADIUM = addItem(722, "battery.hull.luv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.RedSteel, GTValues.M * 18)));
        BATTERY_HULL_MEDIUM_NAQUADRIA = addItem(723, "battery.hull.zpm").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Europium, GTValues.M * 6)));
        BATTERY_HULL_LARGE_NAQUADRIA = addItem(724, "battery.hull.uv").setMaterialInfo(new ItemMaterialInfo(new MaterialStack(Materials.Americium, GTValues.M * 18)));

        // Batteries: 731-775
        BATTERY_ULV_TANTALUM = addItem(731, "battery.re.ulv.tantalum").addComponents(ElectricStats.createRechargeableBattery(1000, GTValues.ULV)).setUnificationData(OrePrefix.battery, Tier.Primitive);

        BATTERY_LV_SODIUM = addItem(732, "battery.re.lv.sodium").addComponents(ElectricStats.createRechargeableBattery(80000, GTValues.LV)).setUnificationData(OrePrefix.battery, Tier.Basic).setModelAmount(8);
        BATTERY_MV_SODIUM = addItem(733, "battery.re.mv.sodium").addComponents(ElectricStats.createRechargeableBattery(360000, GTValues.MV)).setUnificationData(OrePrefix.battery, Tier.Good).setModelAmount(8);
        BATTERY_HV_SODIUM = addItem(734, "battery.re.hv.sodium").addComponents(ElectricStats.createRechargeableBattery(1200000, GTValues.HV)).setUnificationData(OrePrefix.battery, Tier.Advanced).setModelAmount(8);

        BATTERY_LV_LITHIUM = addItem(735, "battery.re.lv.lithium").addComponents(ElectricStats.createRechargeableBattery(100000, GTValues.LV)).setUnificationData(OrePrefix.battery, Tier.Basic).setModelAmount(8);
        BATTERY_MV_LITHIUM = addItem(736, "battery.re.mv.lithium").addComponents(ElectricStats.createRechargeableBattery(400000, GTValues.MV)).setUnificationData(OrePrefix.battery, Tier.Good).setModelAmount(8);
        BATTERY_HV_LITHIUM = addItem(737, "battery.re.hv.lithium").addComponents(ElectricStats.createRechargeableBattery(1600000, GTValues.HV)).setUnificationData(OrePrefix.battery, Tier.Advanced).setModelAmount(8);

        BATTERY_LV_CADMIUM = addItem(738, "battery.re.lv.cadmium").addComponents(ElectricStats.createRechargeableBattery(120000, GTValues.LV)).setUnificationData(OrePrefix.battery, Tier.Basic).setModelAmount(8);
        BATTERY_MV_CADMIUM = addItem(739, "battery.re.mv.cadmium").addComponents(ElectricStats.createRechargeableBattery(420000, GTValues.MV)).setUnificationData(OrePrefix.battery, Tier.Good).setModelAmount(8);
        BATTERY_HV_CADMIUM = addItem(740, "battery.re.hv.cadmium").addComponents(ElectricStats.createRechargeableBattery(1800000, GTValues.HV)).setUnificationData(OrePrefix.battery, Tier.Advanced).setModelAmount(8);

        ENERGIUM_CRYSTAL = addItem(741, "energy_crystal").addComponents(ElectricStats.createRechargeableBattery(6400000L, GTValues.HV)).setUnificationData(OrePrefix.battery, Tier.Advanced).setModelAmount(8).setMaxStackSize(1);
        LAPOTRON_CRYSTAL = addItem(742, "lapotron_crystal").addComponents(ElectricStats.createRechargeableBattery(16000000L, GTValues.EV)).setUnificationData(OrePrefix.battery, Tier.Extreme).setModelAmount(8).setMaxStackSize(1);

        BATTERY_EV_VANADIUM = addItem(743, "battery.ev.vanadium").addComponents(ElectricStats.createRechargeableBattery(10240000L, GTValues.EV)).setUnificationData(OrePrefix.battery, Tier.Extreme).setModelAmount(8);
        BATTERY_IV_VANADIUM = addItem(744, "battery.iv.vanadium").addComponents(ElectricStats.createRechargeableBattery(40960000L, GTValues.IV)).setUnificationData(OrePrefix.battery, Tier.Elite).setModelAmount(8);
        BATTERY_LUV_VANADIUM = addItem(745, "battery.luv.vanadium").addComponents(ElectricStats.createRechargeableBattery(163840000L, GTValues.LuV)).setUnificationData(OrePrefix.battery, Tier.Master).setModelAmount(8);

        BATTERY_ZPM_NAQUADRIA = addItem(746, "battery.zpm.naquadria").addComponents(ElectricStats.createRechargeableBattery(655360000L, GTValues.ZPM)).setUnificationData(OrePrefix.battery, Tier.Ultimate).setModelAmount(8);
        BATTERY_UV_NAQUADRIA = addItem(747, "battery.uv.naquadria").addComponents(ElectricStats.createRechargeableBattery(2621440000L, GTValues.UV)).setUnificationData(OrePrefix.battery, Tier.Super).setModelAmount(8);

        ENERGY_LAPOTRONIC_ORB = addItem(748, "energy.lapotronic_orb").addComponents(ElectricStats.createRechargeableBattery(100000000L, GTValues.IV)).setUnificationData(OrePrefix.battery, Tier.Elite).setModelAmount(8);
        ENERGY_LAPOTRONIC_ORB_CLUSTER = addItem(749, "energy.lapotronic_orb_cluster").addComponents(ElectricStats.createRechargeableBattery(1000000000L, GTValues.LuV)).setUnificationData(OrePrefix.battery, Tier.Master).setModelAmount(8);

        ENERGY_LAPOTRONIC_MODULE = addItem(750, "energy.module").addComponents(new IItemComponent[]{ElectricStats.createRechargeableBattery(10000000000L, GTValues.ZPM)}).setUnificationData(OrePrefix.battery, Tier.Ultimate).setModelAmount(8);
        ENERGY_LAPOTRONIC_CLUSTER = addItem(751, "energy.cluster").addComponents(new IItemComponent[]{ElectricStats.createRechargeableBattery(100000000000L, GTValues.UV)}).setUnificationData(OrePrefix.battery, Tier.Super).setModelAmount(8);

        ZERO_POINT_MODULE = addItem(752, "zpm").addComponents(ElectricStats.createBattery(2000000000000L, GTValues.ZPM, true)).setModelAmount(8);
        ULTIMATE_BATTERY = addItem(753, "max.battery").addComponents(ElectricStats.createRechargeableBattery(Long.MAX_VALUE, GTValues.MAX)).setUnificationData(OrePrefix.battery, Tier.Maximum).setModelAmount(8);

        IMPELLER_MV = addItem(776, "impeller.mv").setRarity(EnumRarity.UNCOMMON);
        IMPELLER_HV = addItem(777, "impeller.hv").setRarity(EnumRarity.RARE);
        GRAVITATION_ENGINE = addItem(778, "gravitation_engine").setRarity(EnumRarity.EPIC);

        // Plugins: 780-799
        PLUGIN_ADVANCED_MONITOR = addItem(780, "plugin.advanced_monitor").addComponents(new AdvancedMonitorPluginBehavior());
        PLUGIN_FAKE_GUI = addItem(781, "plugin.fake_gui").addComponents(new FakeGuiPluginBehavior());
        PLUGIN_ONLINE_PIC = addItem(782, "plugin.online_pic").addComponents(new OnlinePicPluginBehavior());
        PLUGIN_TEXT = addItem(783, "plugin.text").addComponents(new TextPluginBehavior());

        COLOURED_LEDS = addItem(798, "coloured.leds");
        DISPLAY = addItem(799, "display");

        // Records: 800-819
        SUS_RECORD = addItem(800, "record.sus").addComponents(new MusicDiscStats(GTSounds.RECORD_SOUND)).setRarity(EnumRarity.RARE).setMaxStackSize(1).setInvisible();

        // Dyed Glass Lenses: 820-840
        for (int i = 0; i < MarkerMaterials.Color.VALUES.length; i++) {
            MarkerMaterial color = MarkerMaterials.Color.VALUES[i];
            if (color != MarkerMaterials.Color.White) {
                GLASS_LENSES.put(color, addItem(820 + i, String.format("glass_lens.%s", color.toString())));
            }
        }
    }
}
