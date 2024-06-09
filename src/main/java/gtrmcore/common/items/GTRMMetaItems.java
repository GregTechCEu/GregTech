package gtrmcore.common.items;

import gregtech.api.items.metaitem.MetaItem;

public class GTRMMetaItems {

    private GTRMMetaItems() {}

    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_ULV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_ULV;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_ULV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_ULV;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_ULV;
    public static MetaItem<?>.MetaValueItem FLUID_REGULATOR_ULV;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_ULV;
    public static MetaItem<?>.MetaValueItem EMITTER_ULV;
    public static MetaItem<?>.MetaValueItem SENSOR_ULV;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_LOW;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_LOW;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_LOW;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_LOW;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_LOW;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_LOW;
    public static MetaItem<?>.MetaValueItem EMITTER_LOW;
    public static MetaItem<?>.MetaValueItem SENSOR_LOW;
    public static MetaItem<?>.MetaValueItem ELECTRIC_MOTOR_HIGH;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PUMP_HIGH;
    public static MetaItem<?>.MetaValueItem CONVEYOR_MODULE_HIGH;
    public static MetaItem<?>.MetaValueItem ELECTRIC_PISTON_HIGH;
    public static MetaItem<?>.MetaValueItem ROBOT_ARM_HIGH;
    public static MetaItem<?>.MetaValueItem FIELD_GENERATOR_HIGH;
    public static MetaItem<?>.MetaValueItem EMITTER_HIGH;
    public static MetaItem<?>.MetaValueItem SENSOR_HIGH;
    public static MetaItem<?>.MetaValueItem COBBLESTONE_SAW_HEAD;
    public static MetaItem<?>.MetaValueItem WOODEN_HARD_HAMMER_HEAD;
    public static MetaItem<?>.MetaValueItem WOODEN_PICKAXE_HEAD;
    public static MetaItem<?>.MetaValueItem WOOD_FIBER;
    public static MetaItem<?>.MetaValueItem STEAM_VALVE;
    public static MetaItem<?>.MetaValueItem ELECTRIC_STEAM_VALVE;
    public static MetaItem<?>.MetaValueItem HIGH_STEAM_VALVE;
    public static MetaItem<?>.MetaValueItem ELECTRIC_HIGH_STEAM_VALVE;

    public static void init() {
        GTRMMetaItem1 gtrmMeta1 = new GTRMMetaItem1();
        gtrmMeta1.setRegistryName("gtrm_meta_1");
    }
}
