package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armoritem.*;
import gregtech.common.ConfigHolder;
import gregtech.common.items.armor.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ArmorItems {

    private static final List<IGTArmor> ARMORS = new ArrayList<>();

    public static ItemGTElectricArmor NIGHTVISION_GOGGLES;

    private ArmorItems() {/**/}

    public static List<IGTArmor> getAllArmors() {
        return ARMORS;
    }

    public static void init() {
        // todo clean up this config... "stuff"
        NIGHTVISION_GOGGLES = register(ItemGTElectricArmor.Builder.of(GTValues.MODID, "nightvision_goggles", EntityEquipmentSlot.HEAD)
                .electric(ConfigHolder.tools.voltageTierNightVision, 80_000L * (long) Math.max(1, Math.pow(1, ConfigHolder.tools.voltageTierNightVision - 1)))
                .electricCost(2)
                .behaviors(NightvisionBehavior.INSTANCE)
                .texture("gregtech:textures/armor/nightvision_goggles.png"));
    }

    public static <T extends IGTArmor, U extends ArmorBuilder<T, U>> T register(@NotNull ArmorBuilder<T, U> builder) {
        T armor = builder.build();
        ARMORS.add(armor);
        return armor;
    }

    public static ItemGTArmor register(@NotNull ItemGTArmor armor) {
        ARMORS.add(armor);
        return armor;
    }

    public static void registerModels() {
    }

    // TODO Remove in v...
    @Deprecated
    public static void initLegacyArmor() {
        ArmorMetaItem<ArmorMetaItem<?>.ArmorMetaValueItem> legacyArmor = new ArmorMetaItem<>() {
            @Override
            public void registerSubItems() {
                // todo strip all of this of as much functionality as possible before merge
                // todo hide these items from jei as well
                MetaItems.NIGHTVISION_GOGGLES = addItem(1, "nightvision_goggles").setArmorLogic(new NightvisionGoggles(2, 80_000L * (long) Math.max(1, Math.pow(1, ConfigHolder.tools.voltageTierNightVision - 1)), ConfigHolder.tools.voltageTierNightVision, EntityEquipmentSlot.HEAD));

                MetaItems.SEMIFLUID_JETPACK = addItem(2, "liquid_fuel_jetpack").setArmorLogic(new PowerlessJetpack());
                MetaItems.ELECTRIC_JETPACK = addItem(3, "electric_jetpack").setArmorLogic(new Jetpack(30, 1_000_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.tools.voltageTierImpeller - 2)), ConfigHolder.tools.voltageTierImpeller)).setModelAmount(8).setRarity(EnumRarity.UNCOMMON);
                MetaItems.ELECTRIC_JETPACK_ADVANCED = addItem(4, "advanced_electric_jetpack").setArmorLogic(new AdvancedJetpack(512, 6_400_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.tools.voltageTierAdvImpeller - 4)), ConfigHolder.tools.voltageTierAdvImpeller)).setRarity(EnumRarity.RARE);

                int energyPerUse = 512;
                int tier = ConfigHolder.tools.voltageTierNanoSuit;
                long maxCapacity = 6_400_000L * (long) Math.max(1, Math.pow(4, tier - 3));
                MetaItems.NANO_HELMET = addItem(20, "nms.helmet").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.HEAD, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.UNCOMMON);
                MetaItems.NANO_CHESTPLATE = addItem(21, "nms.chestplate").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.CHEST, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.UNCOMMON);
                MetaItems.NANO_LEGGINGS = addItem(22, "nms.leggings").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.LEGS, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.UNCOMMON);
                MetaItems.NANO_BOOTS = addItem(23, "nms.boots").setArmorLogic(new NanoMuscleSuite(EntityEquipmentSlot.FEET, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.UNCOMMON);
                MetaItems.NANO_CHESTPLATE_ADVANCED = addItem(30, "nms.advanced_chestplate").setArmorLogic(new AdvancedNanoMuscleSuite(energyPerUse, 12_800_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.tools.voltageTierAdvNanoSuit - 3)), ConfigHolder.tools.voltageTierAdvNanoSuit)).setRarity(EnumRarity.RARE);

                energyPerUse = 8192;
                tier = ConfigHolder.tools.voltageTierQuarkTech;
                maxCapacity = 100_000_000L * (long) Math.max(1, Math.pow(4, tier - 5));
                MetaItems.QUANTUM_HELMET = addItem(40, "qts.helmet").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.HEAD, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.RARE);
                MetaItems.QUANTUM_CHESTPLATE = addItem(41, "qts.chestplate").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.CHEST, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.RARE);
                MetaItems.QUANTUM_LEGGINGS = addItem(42, "qts.leggings").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.LEGS, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.RARE);
                MetaItems.QUANTUM_BOOTS = addItem(43, "qts.boots").setArmorLogic(new QuarkTechSuite(EntityEquipmentSlot.FEET, energyPerUse, maxCapacity, tier)).setRarity(EnumRarity.RARE);
                MetaItems.QUANTUM_CHESTPLATE_ADVANCED = addItem(50, "qts.advanced_chestplate").setArmorLogic(new AdvancedQuarkTechSuite(energyPerUse, 1_000_000_000L * (long) Math.max(1, Math.pow(4, ConfigHolder.tools.voltageTierAdvQuarkTech - 6)), ConfigHolder.tools.voltageTierAdvQuarkTech)).setRarity(EnumRarity.EPIC);
            }
        };
        legacyArmor.setRegistryName("gt_armor");
    }

    // TODO Remove in v...
    @Deprecated
    public static void registerLegacyConversionRecipes() {
        // todo
    }
}
