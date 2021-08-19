package gregtech.common.items.Armor;

import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.QuarkTechSuite;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import net.minecraft.inventory.EntityEquipmentSlot;

public class MetaArmor  extends ArmorMetaItem<ArmorMetaItem<?>.ArmorMetaValueItem> {
    @Override
    public void registerSubItems() {
        ConfigHolder.UnofficialOptions.Equipment e = ConfigHolder.UnofficialOptions.equipment;
        MetaItems.NANO_MUSCLE_SUITE_CHESTPLATE = addItem(0, "nms.chestplate").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.CHEST, e.nanoSuit.energyPerUse, e.nanoSuit.capacity));
        MetaItems.NANO_MUSCLE_SUITE_LEGGINS = addItem(1, "nms.leggins").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.LEGS, e.nanoSuit.energyPerUse, e.nanoSuit.capacity));
        MetaItems.NANO_MUSCLE_SUITE_HELMET = addItem(2, "nms.helmet").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.HEAD, e.nanoSuit.energyPerUse, e.nanoSuit.capacity));
        MetaItems.NANO_MUSCLE_SUITE_BOOTS = addItem(3, "nms.boots").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.FEET, e.nanoSuit.energyPerUse, e.nanoSuit.capacity));

        MetaItems.QUARK_TECH_SUITE_CHESTPLATE = addItem(4, "qts.chestplate").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.CHEST, e.quarkTechSuit.energyPerUse, e.quarkTechSuit.capacity, e.quarkTechSuit.voltageTier));
        MetaItems.QUARK_TECH_SUITE_LEGGINS = addItem(5, "qts.leggins").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.LEGS, e.quarkTechSuit.energyPerUse, e.quarkTechSuit.capacity, e.quarkTechSuit.voltageTier));
        MetaItems.QUARK_TECH_SUITE_HELMET = addItem(6, "qts.helmet").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.HEAD, e.quarkTechSuit.energyPerUse, e.quarkTechSuit.capacity, e.quarkTechSuit.voltageTier));
        MetaItems.QUARK_TECH_SUITE_BOOTS = addItem(7, "qts.boots").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.FEET, e.quarkTechSuit.energyPerUse, e.quarkTechSuit.capacity, e.quarkTechSuit.voltageTier));

        MetaItems.SEMIFLUID_JETPACK = addItem(8, "liquid_fuel_jetpack").setArmorLogic(new PowerlessJetpack(e.semiFluidJetpack.capacity, e.semiFluidJetpack.voltageTier));
        MetaItems.IMPELLER_JETPACK = addItem(9, "impeller_jetpack").setArmorLogic(new Jetpack(e.impellerJetpack.energyPerUse, e.impellerJetpack.capacity, e.impellerJetpack.voltageTier));

        MetaItems.BATPACK_LV = addItem(10, "battery_pack.lv").setArmorLogic(new BatteryPack(0, e.batpackLv.capacity, e.batpackLv.voltageTier));
        MetaItems.BATPACK_MV = addItem(11, "battery_pack.mv").setArmorLogic(new BatteryPack(0, e.batpackMv.capacity, e.batpackMv.voltageTier));
        MetaItems.BATPACK_HV = addItem(12, "battery_pack.hv").setArmorLogic(new BatteryPack(0, e.batpackHv.capacity, e.batpackHv.voltageTier));


        MetaItems.ADVANCED_QAURK_TECH_SUITE_CHESTPLATE = addItem(13, "qts.advanced_chestplate").setArmorLogic(new AdvancedQurakTechSuite());
        MetaItems.ADVANCED_NANO_MUSCLE_CHESTPLATE = addItem(14, "nms.advanced_chestplate").setArmorLogic(new AdvancedNanoMuscleSuite());
        MetaItems.ADVANCED_IMPELLER_JETPACK = addItem(15, "advanced_impeller_jetpack").setArmorLogic(new AdvancedJetpack(e.advImpellerJetpack.energyPerUse, e.advImpellerJetpack.capacity, e.advImpellerJetpack.voltageTier));

        MetaItems.NIGHTVISION_GOGGLES = addItem(16, "nightvision_goggles").setArmorLogic(new NightvisionGoggles());

        MetaItems.IMPELLER_JETPACK.setModelAmount(8);
        MetaItems.BATPACK_LV.setModelAmount(8);
        MetaItems.BATPACK_MV.setModelAmount(8);
        MetaItems.BATPACK_HV.setModelAmount(8);

    }
}
