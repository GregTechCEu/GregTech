package gregtech.common.items.Armor;

import gregtech.api.GTValues;
import gregtech.api.items.armor.*;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;

public class MetaArmor extends ArmorMetaItem<ArmorMetaItem<?>.ArmorMetaValueItem> {

    @Override
    public void registerSubItems() {
        ConfigHolder.UnofficialOptions.Equipment e = ConfigHolder.U.equipment;

        int energyPerUse = 5000;
        int tier = e.voltageTierNanoSuit;
        long maxCapacity = (long) Math.max(1, Math.pow(4, tier));
        MetaItems.NANO_MUSCLE_SUITE_CHESTPLATE = addItem(0, "nms.chestplate").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.CHEST, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.UNCOMMON);
        MetaItems.NANO_MUSCLE_SUITE_LEGGINGS = addItem(1, "nms.leggings").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.LEGS, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.UNCOMMON);
        MetaItems.NANO_MUSCLE_SUITE_HELMET = addItem(2, "nms.helmet").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.HEAD, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.UNCOMMON);
        MetaItems.NANO_MUSCLE_SUITE_BOOTS = addItem(3, "nms.boots").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.FEET, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.UNCOMMON);

        energyPerUse = 10000;
        tier = e.voltageTierQuarkTech;
        maxCapacity = (long) Math.max(1, Math.pow(4, tier));
        MetaItems.QUARK_TECH_SUITE_CHESTPLATE = addItem(4, "qts.chestplate").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.CHEST, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.RARE);
        MetaItems.QUARK_TECH_SUITE_LEGGINGS = addItem(5, "qts.leggings").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.LEGS, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.RARE);
        MetaItems.QUARK_TECH_SUITE_HELMET = addItem(6, "qts.helmet").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.HEAD, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.RARE);
        MetaItems.QUARK_TECH_SUITE_BOOTS = addItem(7, "qts.boots").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.FEET, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.RARE);

        MetaItems.SEMIFLUID_JETPACK = addItem(8, "liquid_fuel_jetpack").setArmorLogic(new PowerlessJetpack()).setRarity(EnumRarity.UNCOMMON);
        MetaItems.IMPELLER_JETPACK = addItem(9, "impeller_jetpack").setArmorLogic(new Jetpack(125, 2520000L * (long) Math.max(1, Math.pow(4, e.voltageTierImpeller - 2)), e.voltageTierImpeller)).setModelAmount(8).setRarity(EnumRarity.UNCOMMON);

        MetaItems.BATPACK_LV = addItem(10, "battery_pack.lv").setArmorLogic(new BatteryPack(0, e.batpack.capacityLV, GTValues.LV)).setModelAmount(8);
        MetaItems.BATPACK_MV = addItem(11, "battery_pack.mv").setArmorLogic(new BatteryPack(0, e.batpack.capacityMV, GTValues.MV)).setModelAmount(8);
        MetaItems.BATPACK_HV = addItem(12, "battery_pack.hv").setArmorLogic(new BatteryPack(0, e.batpack.capacityHV, GTValues.HV)).setModelAmount(8);

        MetaItems.ADVANCED_QUARK_TECH_SUITE_CHESTPLATE = addItem(13, "qts.advanced_chestplate").setArmorLogic(new AdvancedQuarkTechSuite(10000, 100000000L * (long) Math.max(1, Math.pow(4, e.voltageTierAdvQuarkTech)), e.voltageTierAdvQuarkTech)).setRarity(EnumRarity.EPIC);
        MetaItems.ADVANCED_NANO_MUSCLE_CHESTPLATE = addItem(14, "nms.advanced_chestplate").setArmorLogic(new AdvancedNanoMuscleSuite(5000, 11400000L * (long) Math.max(1, Math.pow(4, e.voltageTierAdvNanoSuit)), e.voltageTierAdvNanoSuit)).setRarity(EnumRarity.RARE);
        MetaItems.ADVANCED_IMPELLER_JETPACK = addItem(15, "advanced_impeller_jetpack").setArmorLogic(new AdvancedJetpack(512, 11400000L * (long) Math.max(1, Math.pow(4, e.voltageTierAdvImpeller - 3)), e.voltageTierAdvImpeller)).setRarity(EnumRarity.RARE);

        MetaItems.NIGHTVISION_GOGGLES = addItem(16, "nightvision_goggles").setArmorLogic(new NightvisionGoggles()).setRarity(EnumRarity.UNCOMMON);
    }
}
