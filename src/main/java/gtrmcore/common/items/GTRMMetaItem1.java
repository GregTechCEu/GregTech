package gtrmcore.common.items;

import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.common.items.behaviors.TooltipBehavior;

import net.minecraft.client.resources.I18n;

import gtrmcore.api.unification.material.GTRMMarkerMaterials;
import gtrmcore.api.unification.ore.GTRMOrePrefix;

import static gtrmcore.common.items.GTRMMetaItems.*;

public class GTRMMetaItem1 extends StandardMetaItem {

    public GTRMMetaItem1() {}

    @Override
    public void registerSubItems() {
        // GTRMparts 1~200

        // First Age parts 1~100
        WOODEN_HARD_HAMMER_HEAD = addItem(1, "wooden.hard.hammer.head");
        WOODEN_PICKAXE_HEAD = addItem(2, "wooden.pickaxe.head");
        COBBLESTONE_SAW_HEAD = addItem(3, "cobblestone.saw.head");
        WOOD_FIBER = addItem(4, "wood.fiber");

        // -----------------------------Gregtech parts 1001~------------------------------

        // Circuits 1001~1100

        // steam valves
        STEAM_VALVE = addItem(1001, "valve.steam").setUnificationData(GTRMOrePrefix.valve,
                GTRMMarkerMaterials.Component.LOW);
        ELECTRIC_STEAM_VALVE = addItem(1002, "valve.electric").setUnificationData(GTRMOrePrefix.valve,
                GTRMMarkerMaterials.Component.LOW);
        // high steam valves
        HIGH_STEAM_VALVE = addItem(1003, "valve.high_steam").setUnificationData(GTRMOrePrefix.valve,
                GTRMMarkerMaterials.Component.HIGH);
        ELECTRIC_HIGH_STEAM_VALVE = addItem(1004, "valve.high_electric").setUnificationData(GTRMOrePrefix.valve,
                GTRMMarkerMaterials.Component.HIGH);

        // Components 1101~1200
        // low steam
        ELECTRIC_MOTOR_LOW = addItem(1101, "electric.motor.low");
        ELECTRIC_PUMP_LOW = addItem(1102, "electric.pump.low");
        CONVEYOR_MODULE_LOW = addItem(1103, "conveyor.module.low");
        ELECTRIC_PISTON_LOW = addItem(1104, "electric.piston.low");
        ROBOT_ARM_LOW = addItem(1105, "robot.arm.low");
        FIELD_GENERATOR_LOW = addItem(1106, "field.generator.low");
        EMITTER_LOW = addItem(1107, "emitter.low");
        SENSOR_LOW = addItem(1108, "sensor.low");

        // high steam
        ELECTRIC_MOTOR_HIGH = addItem(1109, "electric.motor.high");
        ELECTRIC_PUMP_HIGH = addItem(1110, "electric.pump.high");
        CONVEYOR_MODULE_HIGH = addItem(1111, "conveyor.module.high");
        ELECTRIC_PISTON_HIGH = addItem(1112, "electric.piston.high");
        ROBOT_ARM_HIGH = addItem(1113, "robot.arm.high");
        FIELD_GENERATOR_HIGH = addItem(1114, "field.generator.high");
        EMITTER_HIGH = addItem(1115, "emitter.high");
        SENSOR_HIGH = addItem(1116, "sensor.high");

        // ULV
        ELECTRIC_MOTOR_ULV = addItem(1117, "electric.motor.ulv");
        ELECTRIC_PUMP_ULV = addItem(1118, "electric.pump.ulv")
                .addComponents(new TooltipBehavior(lines -> {
                    lines.add(I18n.format("metaitem.electric.pump.tooltip"));
                    lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 1280 / 4));
                }));
        CONVEYOR_MODULE_ULV = addItem(1119, "conveyor.module.ulv")
                .addComponents(new TooltipBehavior(lines -> {
                    lines.add(I18n.format("metaitem.conveyor.module.tooltip"));
                    lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate", 2));
                }));
        ELECTRIC_PISTON_ULV = addItem(1120, "electric.piston.ulv");
        ROBOT_ARM_ULV = addItem(1121, "robot.arm.ulv")
                .addComponents(new TooltipBehavior(lines -> {
                    lines.add(I18n.format("metaitem.robot.arm.tooltip"));
                    lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate", 2));
                }));
        FLUID_REGULATOR_ULV = addItem(1122, "fluid.regulator.ulv")
                .addComponents(new TooltipBehavior(lines -> {
                    lines.add(I18n.format("metaitem.fluid.regulator.tooltip"));
                    lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", 320 / 4));
                }));
        FIELD_GENERATOR_ULV = addItem(1123, "field.generator.ulv");
        EMITTER_ULV = addItem(1124, "emitter.ulv");
        SENSOR_ULV = addItem(1125, "sensor.ulv");
    }
}
