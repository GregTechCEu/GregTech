package gregtech.common.items.armor;

import gregtech.api.GTValues;
import gregtech.api.items.armor.*;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;

public class MetaArmor extends ArmorMetaItem<ArmorMetaItem<?>.ArmorMetaValueItem> {

    @Override
    public void registerSubItems() {
        ConfigHolder.ToolOptions e = ConfigHolder.tools;

        MetaItems.NIGHTVISION_GOGGLES = addItem(1, "nightvision_goggles").setArmorLogic(new NightvisionGoggles()).setRarity(EnumRarity.UNCOMMON);

        MetaItems.SEMIFLUID_JETPACK = addItem(2, "liquid_fuel_jetpack").setArmorLogic(new PowerlessJetpack()).setRarity(EnumRarity.UNCOMMON);
        MetaItems.IMPELLER_JETPACK = addItem(3, "impeller_jetpack").setArmorLogic(new Jetpack(125, 2520000L * (long) Math.max(1, Math.pow(4, e.voltageTierImpeller - 2)), e.voltageTierImpeller)).setModelAmount(8).setRarity(EnumRarity.UNCOMMON);
        MetaItems.ADVANCED_IMPELLER_JETPACK = addItem(4, "advanced_impeller_jetpack").setArmorLogic(new AdvancedJetpack(512, 11400000L * (long) Math.max(1, Math.pow(4, e.voltageTierAdvImpeller - 3)), e.voltageTierAdvImpeller)).setRarity(EnumRarity.RARE);

        int energyPerUse = 5000;
        int tier = e.voltageTierNanoSuit;
        long maxCapacity = 1600000L * (long) Math.max(1, Math.pow(4, tier - 3));
        MetaItems.NANO_MUSCLE_SUITE_HELMET = addItem(20, "nms.helmet").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.HEAD, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.UNCOMMON);
        MetaItems.NANO_MUSCLE_SUITE_CHESTPLATE = addItem(21, "nms.chestplate").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.CHEST, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.UNCOMMON);
        MetaItems.NANO_MUSCLE_SUITE_LEGGINGS = addItem(22, "nms.leggings").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.LEGS, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.UNCOMMON);
        MetaItems.NANO_MUSCLE_SUITE_BOOTS = addItem(23, "nms.boots").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.FEET, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.UNCOMMON);
        MetaItems.ADVANCED_NANO_MUSCLE_CHESTPLATE = addItem(30, "nms.advanced_chestplate").setArmorLogic(new AdvancedNanoMuscleSuite(5000, 13000000L * (long) Math.max(1, Math.pow(4, e.voltageTierAdvNanoSuit - 4)), e.voltageTierAdvNanoSuit)).setRarity(EnumRarity.RARE);

        energyPerUse = 10000;
        tier = e.voltageTierQuarkTech;
        maxCapacity = 8000000L * (long) Math.max(1, Math.pow(4, tier - 5));
        MetaItems.QUARK_TECH_SUITE_HELMET = addItem(40, "qts.helmet").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.HEAD, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.RARE);
        MetaItems.QUARK_TECH_SUITE_CHESTPLATE = addItem(41, "qts.chestplate").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.CHEST, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.RARE);
        MetaItems.QUARK_TECH_SUITE_LEGGINGS = addItem(42, "qts.leggings").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.LEGS, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.RARE);
        MetaItems.QUARK_TECH_SUITE_BOOTS = addItem(43, "qts.boots").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.FEET, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.RARE);
        MetaItems.ADVANCED_QUARK_TECH_SUITE_CHESTPLATE = addItem(50, "qts.advanced_chestplate").setArmorLogic(new AdvancedQuarkTechSuite(10000, 100000000L * (long) Math.max(1, Math.pow(4, e.voltageTierAdvQuarkTech - 5)), e.voltageTierAdvQuarkTech)).setRarity(EnumRarity.EPIC);

        MetaItems.BATPACK_LV = addItem(70, "battery_pack.lv").setArmorLogic(new BatteryPack(0, e.batpack.capacityLV, GTValues.LV)).setModelAmount(8);
        MetaItems.BATPACK_MV = addItem(71, "battery_pack.mv").setArmorLogic(new BatteryPack(0, e.batpack.capacityMV, GTValues.MV)).setModelAmount(8);
        MetaItems.BATPACK_HV = addItem(72, "battery_pack.hv").setArmorLogic(new BatteryPack(0, e.batpack.capacityHV, GTValues.HV)).setModelAmount(8);
    }
}
