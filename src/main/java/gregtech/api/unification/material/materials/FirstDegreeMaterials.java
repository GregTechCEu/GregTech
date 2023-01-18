package gregtech.api.unification.material.materials;

import gregtech.api.GTValues;
import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.BlastProperty.GasTier;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import net.minecraft.init.Enchantments;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;

public class FirstDegreeMaterials {

    public static void register() {

        // ID 250 RESERVED: Almandine

        Andradite = new Material.Builder(251, "andradite")
                .gem(1)
                .color(0x967800).iconSet(RUBY)
                .components(Calcium, 3, Iron, 2, Silicon, 3, Oxygen, 12)
                .build();

        AnnealedCopper = new Material.Builder(252, "annealed_copper")
                .ingot().fluid()
                .color(0xFF8D3B).iconSet(BRIGHT)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_FINE_WIRE)
                .components(Copper, 1)
                .cableProperties(GTValues.V[2], 1, 1)
                .fluidTemp(1358)
                .build();
        Copper.getProperty(PropertyKey.INGOT).setArcSmeltingInto(AnnealedCopper);

        // ID 253 RESERVED: Asbestos

        Ash = new Material.Builder(254, "ash")
                .dust(1)
                .color(0x969696)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Hematite = new Material.Builder(255, "hematite")
                .dust().ore()
                .color(0x915A5A)
                .flags(MAGNETIC_ORE)
                .components(Iron, 2, Oxygen, 3)
                .build();

        BatteryAlloy = new Material.Builder(256, "battery_alloy")
                .ingot(1).fluid()
                .color(0x9C7CA0)
                .flags(EXT_METAL)
                .components(Lead, 4, Antimony, 1)
                .fluidTemp(660)
                .build();

        // ID 257 RESERVED: Blue Topaz

        Bone = new Material.Builder(258, "bone")
                .dust(1)
                .color(0xFAFAFA)
                .flags(MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Calcium, 1)
                .build();

        Brass = new Material.Builder(259, "brass")
                .ingot(1).fluid()
                .color(0xFFB400).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .components(Zinc, 1, Copper, 3)
                .rotorStats(8.0f, 3.0f, 152)
                .itemPipeProperties(2048, 1)
                .fluidTemp(1160)
                .build();

        Bronze = new Material.Builder(260, "bronze")
                .ingot().fluid()
                .color(0xFF8000).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_FRAME, GENERATE_SMALL_GEAR, GENERATE_FOIL, GENERATE_GEAR)
                .components(Tin, 1, Copper, 3)
                .toolStats(ToolProperty.Builder.of(3.0F, 2.0F, 192, 2)
                        .enchantability(18).build())
                .rotorStats(6.0f, 2.5f, 192)
                .fluidPipeProperties(1696, 20, true)
                .fluidTemp(1357)
                .build();

        BrownLimonite = new Material.Builder(261, "brown_limonite")
                .dust(1).ore()
                .color(0xC86400).iconSet(METALLIC)
                .flags(MAGNETIC_ORE)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .build();

        Calcite = new Material.Builder(262, "calcite")
                .dust(1).ore()
                .color(0xFAE6DC)
                .components(Calcium, 1, Carbon, 1, Oxygen, 3)
                .build();

        Cassiterite = new Material.Builder(263, "cassiterite")
                .dust(1).ore(2)
                .color(0xDCDCDC).iconSet(METALLIC)
                .components(Tin, 1, Oxygen, 2)
                .build();

        CassiteriteSand = new Material.Builder(264, "cassiterite_sand")
                .dust(1).ore(2)
                .color(0xDCDCDC).iconSet(SAND)
                .components(Tin, 1, Oxygen, 2)
                .build();

        Chalcopyrite = new Material.Builder(265, "chalcopyrite")
                .dust(1).ore()
                .color(0xA07828)
                .flags(DISABLE_DECOMPOSITION)
                .components(Copper, 1, Iron, 1, Sulfur, 2)
                .build();

        Charcoal = new Material.Builder(266, "charcoal")
                .gem(1, 1600) //default charcoal burn time in vanilla
                .color(0x644646).iconSet(FINE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE)
                .components(Carbon, 1)
                .build();

        Chromite = new Material.Builder(267, "chromite")
                .dust(1).ore()
                .color(0x23140F).iconSet(METALLIC)
                .components(Iron, 1, Chrome, 2, Oxygen, 4)
                .build();

        Cinnabar = new Material.Builder(268, "cinnabar")
                .gem(1).ore()
                .color(0x960000).iconSet(EMERALD)
                .flags(CRYSTALLIZABLE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Mercury, 1, Sulfur, 1)
                .build();

        Water = new Material.Builder(269, "water")
                .fluid()
                .color(0x0000FF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .fluidTemp(300)
                .build();

        LiquidOxygen = new Material.Builder(270, "liquid_oxygen")
                .fluid()
                .color(0x6688DD)
                .flags(DISABLE_DECOMPOSITION)
                .components(Oxygen, 1)
                .fluidTemp(85)
                .build();

        Coal = new Material.Builder(271, "coal")
                .gem(0, 1600).ore(2) //default coal burn time in vanilla
                .color(0x464646).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Cobaltite = new Material.Builder(272, "cobaltite")
                .dust(1).ore()
                .color(0x5050FA).iconSet(METALLIC)
                .flags(WASHING_PERSULFATE, DISABLE_DECOMPOSITION)
                .components(Cobalt, 1, Arsenic, 1, Sulfur, 1)
                .build();

        Cooperite = new Material.Builder(273, "cooperite")
                .dust(1).ore()
                .color(0xFFFFC8).iconSet(METALLIC)
                .flags(WASHING_MERCURY, DISABLE_DECOMPOSITION)
                .components(Platinum, 3, Nickel, 1, Sulfur, 1, Palladium, 1)
                .build();

        Cupronickel = new Material.Builder(274, "cupronickel")
                .ingot(1).fluid()
                .color(0xE39680).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING, GENERATE_FINE_WIRE)
                .components(Copper, 1, Nickel, 1)
                .itemPipeProperties(2048, 1)
                .cableProperties(GTValues.V[2], 1, 1)
                .fluidTemp(1542)
                .build();

        DarkAsh = new Material.Builder(275, "dark_ash")
                .dust(1)
                .color(0x323232)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Diamond = new Material.Builder(276, "diamond")
                .gem(3).ore()
                .color(0xC8FFFF).iconSet(DIAMOND)
                .flags(GENERATE_BOLT_SCREW, GENERATE_LENS, GENERATE_GEAR, NO_SMASHING, NO_SMELTING,
                        HIGH_SIFTER_OUTPUT, DISABLE_DECOMPOSITION, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Carbon, 1)
                .toolStats(ToolProperty.Builder.of(6.0F, 7.0F, 768, 3)
                        .attackSpeed(0.1F).enchantability(18).build())
                .build();

        Electrum = new Material.Builder(277, "electrum")
                .ingot().fluid()
                .color(0xFFFF64).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_FINE_WIRE, GENERATE_RING)
                .components(Silver, 1, Gold, 1)
                .itemPipeProperties(1024, 2)
                .cableProperties(GTValues.V[3], 2, 2)
                .fluidTemp(1285)
                .build();

        // ID 278 RESERVED: Emerald

        Galena = new Material.Builder(279, "galena")
                .dust(3).ore()
                .color(0x643C64)
                .flags(NO_SMELTING, DISABLE_DECOMPOSITION)
                .components(Lead, 1, Sulfur, 1)
                .build();

        Garnierite = new Material.Builder(280, "garnierite")
                .dust(3).ore()
                .color(0x32C846).iconSet(METALLIC)
                .flags(WASHING_PERSULFATE)
                .components(Nickel, 1, Oxygen, 1)
                .build();

        GreenSapphire = new Material.Builder(281, "green_sapphire")
                .gem().ore()
                .color(0x64C882).iconSet(GEM_HORIZONTAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Oxygen, 3)
                .build();

        // ID 282 RESERVED: Grossular

        Ice = new Material.Builder(283, "ice")
                .dust(0).fluid()
                .color(0xC8C8FF, false).iconSet(SHINY)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .fluidTemp(273)
                .build();

        Ilmenite = new Material.Builder(284, "ilmenite")
                .dust(3).ore()
                .color(0x463732).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION, MAGNETIC_ORE)
                .components(Iron, 1, Titanium, 1, Oxygen, 3)
                .build();

        Rutile = new Material.Builder(285, "rutile")
                .gem()
                .color(0xD40D5C).iconSet(GEM_HORIZONTAL)
                .flags(DISABLE_DECOMPOSITION, MAGNETIC_ORE)
                .components(Titanium, 1, Oxygen, 2)
                .build();

        Bauxite = new Material.Builder(286, "bauxite")
                .dust(1).ore()
                .color(0xC86400)
                .flags(DISABLE_DECOMPOSITION)
                .components(Aluminium, 2, Oxygen, 3)
                .build();

        Invar = new Material.Builder(287, "invar")
                .ingot().fluid()
                .color(0xB4B478).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_FRAME, GENERATE_GEAR)
                .components(Iron, 2, Nickel, 1)
                .toolStats(ToolProperty.Builder.of(4.0F, 3.0F, 384, 2)
                        .enchantability(18)
                        .enchantment(Enchantments.BANE_OF_ARTHROPODS, 3)
                        .enchantment(Enchantments.EFFICIENCY, 1).build())
                .rotorStats(7.0f, 3.0f, 512)
                .fluidTemp(1916)
                .build();

        Kanthal = new Material.Builder(288, "kanthal")
                .ingot().fluid()
                .color(0xC2D2DF).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Iron, 1, Aluminium, 1, Chrome, 1)
                .cableProperties(GTValues.V[3], 4, 3)
                .blastTemp(1800, GasTier.LOW, VA[MV], 1000)
                .fluidTemp(1708)
                .build();

        Lazurite = new Material.Builder(289, "lazurite")
                .gem(1).ore(5)
                .color(0x6478FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, GENERATE_ROD, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 6, Silicon, 6, Calcium, 8, Sodium, 8)
                .build();

        Magnalium = new Material.Builder(290, "magnalium")
                .ingot().fluid()
                .color(0xC8BEFF)
                .flags(EXT2_METAL, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnesium, 1, Aluminium, 2)
                .rotorStats(6.0f, 2.0f, 256)
                .itemPipeProperties(1024, 2)
                .fluidTemp(929)
                .build();

        Magnesite = new Material.Builder(291, "magnesite")
                .dust().ore()
                .color(0xFAFAB4).iconSet(METALLIC)
                .components(Magnesium, 1, Carbon, 1, Oxygen, 3)
                .build();

        Magnetite = new Material.Builder(292, "magnetite")
                .dust().ore()
                .color(0x1E1E1E).iconSet(METALLIC)
                .flags(MAGNETIC_ORE)
                .components(Iron, 3, Oxygen, 4)
                .build();

        Molybdenite = new Material.Builder(293, "molybdenite")
                .dust().ore()
                .color(0x191919).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Molybdenum, 1, Sulfur, 2)
                .build();

        Nichrome = new Material.Builder(294, "nichrome")
                .ingot().fluid()
                .color(0xCDCEF6).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Nickel, 4, Chrome, 1)
                .cableProperties(GTValues.V[4], 4, 4)
                .blastTemp(2700, GasTier.LOW, VA[HV], 1300)
                .fluidTemp(1818)
                .build();

        NiobiumNitride = new Material.Builder(295, "niobium_nitride")
                .ingot().fluid()
                .color(0x1D291D)
                .flags(EXT_METAL, GENERATE_FOIL)
                .components(Niobium, 1, Nitrogen, 1)
                .cableProperties(GTValues.V[6], 1, 1)
                .blastTemp(2846, GasTier.MID)
                .build();

        NiobiumTitanium = new Material.Builder(296, "niobium_titanium")
                .ingot().fluid()
                .color(0x1D1D29)
                .flags(EXT2_METAL, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .components(Niobium, 1, Titanium, 1)
                .fluidPipeProperties(5900, 175, true)
                .cableProperties(GTValues.V[6], 4, 2)
                .blastTemp(4500, GasTier.HIGH, VA[HV], 1500)
                .fluidTemp(2345)
                .build();

        Obsidian = new Material.Builder(297, "obsidian")
                .dust(3)
                .color(0x503264)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_RECIPES, GENERATE_PLATE)
                .components(Magnesium, 1, Iron, 1, Silicon, 2, Oxygen, 4)
                .build();

        Phosphate = new Material.Builder(298, "phosphate")
                .dust(1)
                .color(0xFFFF00)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE, EXPLOSIVE)
                .components(Phosphorus, 1, Oxygen, 4)
                .build();

        PlatinumRaw = new Material.Builder(299, "platinum_raw")
                .dust()
                .color(0xFFFFC8).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Platinum, 1, Chlorine, 2)
                .build();

        SterlingSilver = new Material.Builder(300, "sterling_silver")
                .ingot().fluid()
                .color(0xFADCE1).iconSet(SHINY)
                .flags(EXT2_METAL)
                .components(Copper, 1, Silver, 4)
                .toolStats(ToolProperty.Builder.of(3.0F, 8.0F, 768, 2)
                        .attackSpeed(0.3F).enchantability(33)
                        .enchantment(Enchantments.SMITE, 3).build())
                .rotorStats(13.0f, 2.0f, 196)
                .itemPipeProperties(1024, 2)
                .blastTemp(1700, GasTier.LOW, VA[MV], 1000)
                .fluidTemp(1258)
                .build();

        RoseGold = new Material.Builder(301, "rose_gold")
                .ingot().fluid()
                .color(0xFFE61E).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_RING)
                .components(Copper, 1, Gold, 4)
                .toolStats(ToolProperty.Builder.of(12.0F, 2.0F, 768, 2)
                        .enchantability(33)
                        .enchantment(Enchantments.FORTUNE, 2).build())
                .rotorStats(14.0f, 2.0f, 152)
                .itemPipeProperties(1024, 2)
                .blastTemp(1600, GasTier.LOW, VA[MV], 1000)
                .fluidTemp(1341)
                .build();

        BlackBronze = new Material.Builder(302, "black_bronze")
                .ingot().fluid()
                .color(0x64327D)
                .flags(EXT2_METAL, GENERATE_GEAR)
                .components(Gold, 1, Silver, 1, Copper, 3)
                .rotorStats(12.0f, 2.0f, 256)
                .itemPipeProperties(1024, 2)
                .blastTemp(2000, GasTier.LOW, VA[MV], 1000)
                .fluidTemp(1328)
                .build();

        BismuthBronze = new Material.Builder(303, "bismuth_bronze")
                .ingot().fluid()
                .color(0x647D7D)
                .flags(EXT2_METAL)
                .components(Bismuth, 1, Zinc, 1, Copper, 3)
                .rotorStats(8.0f, 3.0f, 256)
                .blastTemp(1100, GasTier.LOW, VA[MV], 1000)
                .fluidTemp(1036)
                .build();

        Biotite = new Material.Builder(304, "biotite")
                .dust(1)
                .color(0x141E14).iconSet(METALLIC)
                .components(Potassium, 1, Magnesium, 3, Aluminium, 3, Fluorine, 2, Silicon, 3, Oxygen, 10)
                .build();

        Powellite = new Material.Builder(305, "powellite")
                .dust().ore()
                .color(0xFFFF00)
                .components(Calcium, 1, Molybdenum, 1, Oxygen, 4)
                .build();

        Pyrite = new Material.Builder(306, "pyrite")
                .dust(1).ore()
                .color(0x967828).iconSet(ROUGH)
                .flags(DISABLE_DECOMPOSITION, MAGNETIC_ORE)
                .components(Iron, 1, Sulfur, 2)
                .build();

        Pyrolusite = new Material.Builder(307, "pyrolusite")
                .dust().ore()
                .color(0x9696AA)
                .components(Manganese, 1, Oxygen, 2)
                .build();

        // ID 308 RESERVED: Pyrope

        RockSalt = new Material.Builder(309, "rock_salt")
                .gem(1).ore()
                .color(0xF0C8C8).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Potassium, 1, Chlorine, 1)
                .build();

        Ruridit = new Material.Builder(310, "ruridit")
                .ingot(3)
                .colorAverage().iconSet(BRIGHT)
                .flags(GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_LONG_ROD)
                .components(Ruthenium, 2, Iridium, 1)
                .blastTemp(4500, GasTier.HIGH, VA[EV], 1600)
                .build();

        // ID 311 RESERVED: Ruby

        Salt = new Material.Builder(312, "salt")
                .gem(1).ore()
                .color(0xFAFAFA).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Sodium, 1, Chlorine, 1)
                .build();

        Saltpeter = new Material.Builder(313, "saltpeter")
                .dust(1).ore()
                .color(0xE6E6E6).iconSet(FINE)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE)
                .components(Potassium, 1, Nitrogen, 1, Oxygen, 3)
                .build();

        Sapphire = new Material.Builder(314, "sapphire")
                .gem().ore()
                .color(0x6464C8).iconSet(GEM_VERTICAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(Aluminium, 2, Oxygen, 3)
                .build();

        Scheelite = new Material.Builder(315, "scheelite")
                .dust(3).ore()
                .color(0xC88C14)
                .flags(DISABLE_DECOMPOSITION, MAGNETIC_ORE)
                .components(Calcium, 1, Tungsten, 1, Oxygen, 4)
                .build()
                .setFormula("Ca(WO3)O", true);

        Sodalite = new Material.Builder(316, "sodalite")
                .gem(1).ore(5)
                .color(0x1414FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, GENERATE_ROD, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 3, Silicon, 3, Sodium, 4, Chlorine, 1)
                .build();

        AluminiumSulfite = new Material.Builder(317, "aluminium_sulfite")
                .dust()
                .color(0xCC4BBB).iconSet(DULL)
                .components(Aluminium, 2, Sulfur, 3, Oxygen, 9)
                .build().setFormula("Al2(SO3)3", true);

        Tantalite = new Material.Builder(318, "tantalite")
                .dust(3).ore()
                .color(0x915028).iconSet(METALLIC)
                .components(Manganese, 1, Tantalum, 2, Oxygen, 6)
                .build();

        Coke = new Material.Builder(319, "coke")
                .gem(2, 3200) // 2x burn time of coal
                .color(0x666666).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE)
                .components(Carbon, 1)
                .build();

        SolderingAlloy = new Material.Builder(320, "soldering_alloy")
                .ingot(1).fluid()
                .color(0x9696A0)
                .components(Tin, 6, Lead, 3, Antimony, 1)
                .fluidTemp(544)
                .build();

        // ID 321 RESERVED: Spessartine

        Sphalerite = new Material.Builder(322, "sphalerite")
                .dust(1).ore()
                .color(0xFFFFFF)
                .flags(DISABLE_DECOMPOSITION, WASHING_PERSULFATE)
                .components(Zinc, 1, Sulfur, 1)
                .build();

        StainlessSteel = new Material.Builder(323, "stainless_steel")
                .ingot(3).fluid()
                .color(0xC8C8DC).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_FRAME, GENERATE_LONG_ROD, GENERATE_FOIL, GENERATE_GEAR)
                .components(Iron, 6, Chrome, 1, Manganese, 1, Nickel, 1)
                .toolStats(ToolProperty.Builder.of(7.0F, 5.0F, 1024, 3)
                        .enchantability(14).build())
                .rotorStats(7.0f, 4.0f, 480)
                .fluidPipeProperties(2428, 75, true, true, true, false)
                .blastTemp(1700, GasTier.LOW, VA[HV], 1100)
                .fluidTemp(2011)
                .build();

        Steel = new Material.Builder(324, "steel")
                .ingot(3).fluid()
                .color(0x808080).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_SPRING,
                        GENERATE_SPRING_SMALL, GENERATE_FRAME, DISABLE_DECOMPOSITION, GENERATE_FINE_WIRE, GENERATE_GEAR)
                .components(Iron, 1)
                .toolStats(ToolProperty.Builder.of(5.0F, 3.0F, 512, 3)
                        .enchantability(14).build())
                .rotorStats(6.0f, 3.0f, 512)
                .fluidPipeProperties(1855, 75, true)
                .cableProperties(GTValues.V[4], 2, 2)
                .blastTemp(1000, null, VA[MV], 800) // no gas tier for steel
                .fluidTemp(2046)
                .build();

        Stibnite = new Material.Builder(325, "stibnite")
                .dust().ore()
                .color(0x464646).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Antimony, 2, Sulfur, 3)
                .build();

        // Free ID 326

        Tetrahedrite = new Material.Builder(327, "tetrahedrite")
                .dust().ore()
                .color(0xC82000)
                .flags(WASHING_PERSULFATE, DISABLE_DECOMPOSITION)
                .components(Copper, 3, Antimony, 1, Iron, 1, Sulfur, 3)
                .build();

        TinAlloy = new Material.Builder(328, "tin_alloy")
                .ingot().fluid()
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Tin, 1, Iron, 1)
                .fluidPipeProperties(1572, 20, true)
                .fluidTemp(1258)
                .build();

        // ID 329 RESERVED: Topaz

        Tungstate = new Material.Builder(330, "tungstate")
                .dust(3).ore()
                .color(0x373223)
                .flags(DISABLE_DECOMPOSITION, MAGNETIC_ORE)
                .components(Tungsten, 1, Lithium, 2, Oxygen, 4)
                .build()
                .setFormula("Li2(WO3)O", true);

        Ultimet = new Material.Builder(331, "ultimet")
                .ingot(4).fluid()
                .color(0xB4B4E6).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_GEAR)
                .components(Cobalt, 5, Chrome, 2, Nickel, 1, Molybdenum, 1)
                .toolStats(ToolProperty.Builder.of(10.0F, 7.0F, 2048, 4)
                        .attackSpeed(0.1F).enchantability(21).build())
                .rotorStats(9.0f, 4.0f, 2048)
                .itemPipeProperties(128, 16)
                .blastTemp(2700, GasTier.MID, VA[HV], 1300)
                .fluidTemp(1980)
                .build();

        Uraninite = new Material.Builder(332, "uraninite")
                .dust(3).ore(true)
                .color(0x232323).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium238, 1, Oxygen, 2)
                .build()
                .setFormula("UO2", true);

        Uvarovite = new Material.Builder(333, "uvarovite")
                .gem()
                .color(0xB4ffB4).iconSet(RUBY)
                .components(Calcium, 3, Chrome, 2, Silicon, 3, Oxygen, 12)
                .build();

        VanadiumGallium = new Material.Builder(334, "vanadium_gallium")
                .ingot().fluid()
                .color(0x80808C).iconSet(SHINY)
                .flags(STD_METAL, GENERATE_FOIL, GENERATE_SPRING, GENERATE_SPRING_SMALL)
                .components(Vanadium, 3, Gallium, 1)
                .cableProperties(GTValues.V[7], 4, 2)
                .blastTemp(4500, GasTier.HIGH, VA[EV], 1200)
                .fluidTemp(1712)
                .build();

        WroughtIron = new Material.Builder(335, "wrought_iron")
                .ingot().fluid()
                .color(0xC8B4B4).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_GEAR, GENERATE_FOIL, MORTAR_GRINDABLE, GENERATE_RING, GENERATE_LONG_ROD, GENERATE_BOLT_SCREW, DISABLE_DECOMPOSITION)
                .components(Iron, 1)
                .toolStats(ToolProperty.Builder.of(2.0F, 2.0F, 384, 2)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(6.0f, 3.5f, 384)
                .fluidTemp(2011)
                .build();
        Iron.getProperty(PropertyKey.INGOT).setSmeltingInto(WroughtIron);
        Iron.getProperty(PropertyKey.INGOT).setArcSmeltingInto(WroughtIron);

        Wulfenite = new Material.Builder(336, "wulfenite")
                .dust(3).ore()
                .color(0xFF8000)
                .components(Lead, 1, Molybdenum, 1, Oxygen, 4)
                .build();

        YellowLimonite = new Material.Builder(337, "yellow_limonite")
                .dust().ore()
                .color(0xC8C800).iconSet(METALLIC)
                .flags(MAGNETIC_ORE)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .build();

        YttriumBariumCuprate = new Material.Builder(338, "yttrium_barium_cuprate")
                .ingot().fluid()
                .color(0x504046).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FINE_WIRE, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .components(Yttrium, 1, Barium, 2, Copper, 3, Oxygen, 7)
                .cableProperties(GTValues.V[8], 4, 4)
                .blastTemp(4500, GasTier.HIGH) // todo redo this EBF process
                .fluidTemp(1799)
                .build();

        NetherQuartz = new Material.Builder(339, "nether_quartz")
                .gem(1).ore(2)
                .color(0xE6D2D2).iconSet(QUARTZ)
                .flags(GENERATE_PLATE, NO_SMELTING, CRYSTALLIZABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        CertusQuartz = new Material.Builder(214, "certus_quartz")
                .gem(1).ore(2)
                .color(0xD2D2E6).iconSet(CERTUS)
                .flags(GENERATE_PLATE, NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        Quartzite = new Material.Builder(340, "quartzite")
                .gem(1).ore()
                .color(0xD2E6D2).iconSet(QUARTZ)
                .flags(NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION, GENERATE_PLATE)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        Graphite = new Material.Builder(341, "graphite")
                .ore()
                .color(0x808080)
                .flags(NO_SMELTING, FLAMMABLE, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Graphene = new Material.Builder(342, "graphene")
                .dust()
                .color(0x808080).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .cableProperties(GTValues.V[5], 1, 1)
                .build();

        TungsticAcid = new Material.Builder(343, "tungstic_acid")
                .dust()
                .color(0xBCC800).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Tungsten, 1, Oxygen, 4)
                .build();

        Osmiridium = new Material.Builder(344, "osmiridium")
                .ingot(3).fluid()
                .color(0x6464FF).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_ROTOR, GENERATE_ROUND, GENERATE_FINE_WIRE, GENERATE_GEAR)
                .components(Iridium, 3, Osmium, 1)
                .rotorStats(9.0f, 3.0f, 3152)
                .itemPipeProperties(64, 32)
                .blastTemp(4500, GasTier.HIGH, VA[LuV], 900)
                .fluidTemp(3012)
                .build();

        LithiumChloride = new Material.Builder(345, "lithium_chloride")
                .dust()
                .color(0xDEDEFA).iconSet(FINE)
                .components(Lithium, 1, Chlorine, 1)
                .build();

        CalciumChloride = new Material.Builder(346, "calcium_chloride")
                .dust()
                .color(0xEBEBFA).iconSet(FINE)
                .components(Calcium, 1, Chlorine, 2)
                .build();

        Bornite = new Material.Builder(347, "bornite")
                .dust(1).ore()
                .color(0x97662B).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Copper, 5, Iron, 1, Sulfur, 4)
                .build();

        Chalcocite = new Material.Builder(348, "chalcocite")
                .dust().ore()
                .color(0x353535).iconSet(GEM_VERTICAL)
                .flags(DISABLE_DECOMPOSITION)
                .components(Copper, 2, Sulfur, 1)
                .build();

        // Free ID 349

        // Free ID 350

        GalliumArsenide = new Material.Builder(351, "gallium_arsenide")
                .ingot(1).fluid()
                .color(0xA0A0A0)
                .flags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Arsenic, 1, Gallium, 1)
                .blastTemp(1200, GasTier.LOW, VA[MV], 1200)
                .fluidTemp(1511)
                .build();

        Potash = new Material.Builder(352, "potash")
                .dust(1)
                .color(0x784137)
                .components(Potassium, 2, Oxygen, 1)
                .build();

        SodaAsh = new Material.Builder(353, "soda_ash")
                .dust(1)
                .color(0xDCDCFF)
                .components(Sodium, 2, Carbon, 1, Oxygen, 3)
                .build();

        IndiumGalliumPhosphide = new Material.Builder(354, "indium_gallium_phosphide")
                .ingot(1).fluid()
                .color(0xA08CBE)
                .flags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Indium, 1, Gallium, 1, Phosphorus, 1)
                .fluidTemp(350)
                .build();

        NickelZincFerrite = new Material.Builder(355, "nickel_zinc_ferrite")
                .ingot(0).fluid()
                .color(0x3C3C3C).iconSet(METALLIC)
                .flags(GENERATE_RING)
                .components(Nickel, 1, Zinc, 1, Iron, 4, Oxygen, 8)
                .fluidTemp(1410)
                .build();

        SiliconDioxide = new Material.Builder(356, "silicon_dioxide")
                .dust(1)
                .color(0xC8C8C8).iconSet(QUARTZ)
                .flags(NO_SMASHING, NO_SMELTING)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        MagnesiumChloride = new Material.Builder(357, "magnesium_chloride")
                .dust(1)
                .color(0xD40D5C)
                .components(Magnesium, 1, Chlorine, 2)
                .build();

        SodiumSulfide = new Material.Builder(358, "sodium_sulfide")
                .dust(1)
                .color(0xFFE680)
                .components(Sodium, 2, Sulfur, 1)
                .build();

        PhosphorusPentoxide = new Material.Builder(359, "phosphorus_pentoxide")
                .dust(1)
                .color(0xDCDC00)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Phosphorus, 4, Oxygen, 10)
                .build();

        Quicklime = new Material.Builder(360, "quicklime")
                .dust(1)
                .color(0xF0F0F0)
                .components(Calcium, 1, Oxygen, 1)
                .build();

        SodiumBisulfate = new Material.Builder(361, "sodium_bisulfate")
                .dust(1)
                .color(0x004455)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Hydrogen, 1, Sulfur, 1, Oxygen, 4)
                .build();

        FerriteMixture = new Material.Builder(362, "ferrite_mixture")
                .dust(1)
                .color(0xB4B4B4).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Nickel, 1, Zinc, 1, Iron, 4)
                .build();

        Magnesia = new Material.Builder(363, "magnesia")
                .dust(1)
                .color(0x887878)
                .components(Magnesium, 1, Oxygen, 1)
                .build();

        PlatinumGroupSludge = new Material.Builder(364, "platinum_group_sludge")
                .dust(1)
                .color(0x001E00).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .build();

        Realgar = new Material.Builder(365, "realgar")
                .gem().ore()
                .color(0x9D2123).iconSet(EMERALD)
                .flags(DISABLE_DECOMPOSITION)
                .components(Arsenic, 4, Sulfur, 4)
                .build();

        SodiumBicarbonate = new Material.Builder(366, "sodium_bicarbonate")
                .dust(1)
                .color(0x565b96).iconSet(ROUGH)
                .components(Sodium, 1, Hydrogen, 1, Carbon, 1, Oxygen, 3)
                .build();

        PotassiumDichromate = new Material.Builder(367, "potassium_dichromate")
                .dust(1)
                .color(0xFF084E)
                .components(Potassium, 2, Chrome, 2, Oxygen, 7)
                .build();

        ChromiumTrioxide = new Material.Builder(368, "chromium_trioxide")
                .dust(1)
                .color(0xFFE4E1)
                .components(Chrome, 1, Oxygen, 3)
                .build();

        AntimonyTrioxide = new Material.Builder(369, "antimony_trioxide")
                .dust(1)
                .color(0xE6E6F0)
                .components(Antimony, 2, Oxygen, 3)
                .build();

        // FREE IDs 370-375

        MetalMixture = new Material.Builder(376, "metal_mixture")
                .dust(1)
                .color(0x502d16).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .build();

        SodiumHydroxide = new Material.Builder(377, "sodium_hydroxide")
                .dust(1)
                .color(0x003380)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Oxygen, 1, Hydrogen, 1)
                .build();

        SodiumPersulfate = new Material.Builder(378, "sodium_persulfate")
                .fluid()
                .components(Sodium, 2, Sulfur, 2, Oxygen, 8)
                .build();

        Bastnasite = new Material.Builder(379, "bastnasite")
                .dust().ore()
                .color(0xC86E2D).iconSet(FINE)
                .flags(MAGNETIC_ORE)
                .components(Cerium, 1, Carbon, 1, Fluorine, 1, Oxygen, 3)
                .build();

        Pentlandite = new Material.Builder(380, "pentlandite")
                .dust().ore()
                .color(0xA59605)
                .flags(WASHING_PERSULFATE, DISABLE_DECOMPOSITION)
                .components(Nickel, 9, Sulfur, 8)
                .build();

        // ID 381 RESERVED: Spodumene

        // ID 382 RESERVED: Lepidolite

        Alumina = new Material.Builder(383, "alumina")
                .dust()
                .color(0x78C3EB).iconSet(METALLIC)
                .flags()
                .components(Aluminium, 2, Oxygen, 3)
                .build();

        GlauconiteSand = new Material.Builder(384, "glauconite_sand")
                .dust().ore() // used to be 3
                .color(0x82B43C).iconSet(SAND)
                .components(Potassium, 1, Magnesium, 2, Alumina, 10, Hydrogen, 2, Oxygen, 6)
                .build()
                .setFormula("KMg2(Al2O3)2H2O6", true);

        Malachite = new Material.Builder(385, "malachite")
                .gem().ore()
                .color(0x055F05).iconSet(LAPIS)
                .flags(WASHING_PERSULFATE)
                .components(Copper, 2, Carbon, 1, Hydrogen, 2, Oxygen, 5)
                .build();

        Mica = new Material.Builder(386, "mica")
                .dust().ore()
                .color(0xC3C3CD).iconSet(FINE)
                .components(Potassium, 1, Alumina, 5, SiliconDioxide, 3, Fluorine, 2, Oxygen, 3)
                .build()
                .setFormula("KAl2O3(SiO2)F2O3", true);

        Barite = new Material.Builder(387, "barite")
                .dust().ore()
                .color(0xE6EBEB)
                .components(Barium, 1, Sulfur, 1, Oxygen, 4)
                .build();

        Alunite = new Material.Builder(388, "alunite")
                .dust().ore()
                .color(0xE1B441).iconSet(METALLIC)
                .components(Potassium, 1, Alumina, 10, Sulfur, 2, Oxygen, 2)
                .build()
                .setFormula("KAl4(SO4)2", true);

        PotassiumBisulfate = new Material.Builder(389, "potassium_bisulfate")
                .dust()
                .color(0xFDBD68).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Potassium, 1, Hydrogen, 1, Sulfur, 1, Oxygen, 4)
                .build();

        PotassiumPersulfate = new Material.Builder(390, "potassium_persulfate")
                .fluid()
                .color(0xFAB482)
                .components(Potassium, 2, Sulfur, 2, Oxygen, 8)
                .build();

        // Free ID 391

        // ID 392 RESERVED

        // ID 393 RESERVED

        // ID 394 RESERVED

        IronMagnetic = new Material.Builder(395, "iron_magnetic")
                .ingot()
                .color(0xC8C8C8).iconSet(MAGNETIC)
                .flags(GENERATE_BOLT_SCREW, IS_MAGNETIC)
                .components(Iron, 1)
                .ingotSmeltInto(Iron)
                .arcSmeltInto(WroughtIron)
                .macerateInto(Iron)
                .build();
        Iron.getProperty(PropertyKey.INGOT).setMagneticMaterial(IronMagnetic);

        TungstenCarbide = new Material.Builder(396, "tungsten_carbide")
                .ingot(4).fluid()
                .color(0x330066).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_FOIL, GENERATE_GEAR, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Tungsten, 1, Carbon, 1)
                .toolStats(ToolProperty.Builder.of(60.0F, 2.0F, 1024, 4)
                        .enchantability(21).build())
                .rotorStats(12.0f, 4.0f, 1280)
                .fluidPipeProperties(3837, 200, true)
                .blastTemp(3058, GasTier.MID, VA[HV], 1500)
                .build();

        CarbonDioxide = new Material.Builder(397, "carbon_dioxide")
                .fluid(FluidTypes.GAS)
                .color(0xA9D0F5)
                .components(Carbon, 1, Oxygen, 2)
                .build();

        TitaniumTetrachloride = new Material.Builder(398, "titanium_tetrachloride")
                .fluid()
                .color(0xD40D5C)
                .flags(DISABLE_DECOMPOSITION)
                .components(Titanium, 1, Chlorine, 4)
                .build();

        NitrogenDioxide = new Material.Builder(399, "nitrogen_dioxide")
                .fluid(FluidTypes.GAS)
                .color(0x85FCFF).iconSet(GAS)
                .components(Nitrogen, 1, Oxygen, 2)
                .build();

        HydrogenSulfide = new Material.Builder(400, "hydrogen_sulfide")
                .fluid(FluidTypes.GAS)
                .components(Hydrogen, 2, Sulfur, 1)
                .build();

        NitricAcid = new Material.Builder(401, "nitric_acid")
                .fluid(FluidTypes.ACID)
                .color(0xCCCC00)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 1, Nitrogen, 1, Oxygen, 3)
                .build();

        SulfuricAcid = new Material.Builder(402, "sulfuric_acid")
                .fluid(FluidTypes.ACID)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Sulfur, 1, Oxygen, 4)
                .build();

        PhosphoricAcid = new Material.Builder(403, "phosphoric_acid")
                .fluid(FluidTypes.ACID)
                .color(0xDCDC01)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 3, Phosphorus, 1, Oxygen, 4)
                .build();

        SulfurTrioxide = new Material.Builder(404, "sulfur_trioxide")
                .fluid(FluidTypes.GAS)
                .color(0xA0A014)
                .components(Sulfur, 1, Oxygen, 3)
                .build();

        SulfurDioxide = new Material.Builder(405, "sulfur_dioxide")
                .fluid(FluidTypes.GAS)
                .color(0xC8C819)
                .components(Sulfur, 1, Oxygen, 2)
                .build();

        CarbonMonoxide = new Material.Builder(406, "carbon_monoxide")
                .fluid(FluidTypes.GAS)
                .color(0x0E4880)
                .components(Carbon, 1, Oxygen, 1)
                .build();

        HypochlorousAcid = new Material.Builder(407, "hypochlorous_acid")
                .fluid(FluidTypes.ACID)
                .color(0x6F8A91)
                .components(Hydrogen, 1, Chlorine, 1, Oxygen, 1)
                .build();

        Ammonia = new Material.Builder(408, "ammonia")
                .fluid(FluidTypes.GAS)
                .color(0x3F3480)
                .components(Nitrogen, 1, Hydrogen, 3)
                .build();

        HydrofluoricAcid = new Material.Builder(409, "hydrofluoric_acid")
                .fluid(FluidTypes.ACID)
                .color(0x0088AA)
                .components(Hydrogen, 1, Fluorine, 1)
                .build();

        NitricOxide = new Material.Builder(410, "nitric_oxide")
                .fluid(FluidTypes.GAS)
                .color(0x7DC8F0)
                .components(Nitrogen, 1, Oxygen, 1)
                .build();

        Iron3Chloride = new Material.Builder(411, "iron_iii_chloride")
                .fluid()
                .color(0x060B0B)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Iron, 1, Chlorine, 3)
                .build();

        UraniumHexafluoride = new Material.Builder(412, "uranium_hexafluoride")
                .fluid(FluidTypes.GAS)
                .color(0x42D126)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium238, 1, Fluorine, 6)
                .build()
                .setFormula("UF6", true);

        EnrichedUraniumHexafluoride = new Material.Builder(413, "enriched_uranium_hexafluoride")
                .fluid(FluidTypes.GAS)
                .color(0x4BF52A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium235, 1, Fluorine, 6)
                .build()
                .setFormula("UF6", true);

        DepletedUraniumHexafluoride = new Material.Builder(414, "depleted_uranium_hexafluoride")
                .fluid(FluidTypes.GAS)
                .color(0x74BA66)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium238, 1, Fluorine, 6)
                .build()
                .setFormula("UF6", true);

        NitrousOxide = new Material.Builder(415, "nitrous_oxide")
                .fluid(FluidTypes.GAS)
                .color(0x7DC8FF)
                .components(Nitrogen, 2, Oxygen, 1)
                .build();

        EnderPearl = new Material.Builder(416, "ender_pearl")
                .gem(1)
                .color(0x6CDCC8)
                .flags(NO_SMASHING, NO_SMELTING, GENERATE_PLATE)
                .components(Beryllium, 1, Potassium, 4, Nitrogen, 5)
                .build();

        PotassiumFeldspar = new Material.Builder(417, "potassium_feldspar")
                .dust(1)
                .color(0x782828).iconSet(FINE)
                .components(Potassium, 1, Aluminium, 1, Silicon, 1, Oxygen, 8)
                .build();

        NeodymiumMagnetic = new Material.Builder(418, "neodymium_magnetic")
                .ingot()
                .color(0x646464).iconSet(MAGNETIC)
                .flags(GENERATE_ROD, IS_MAGNETIC)
                .components(Neodymium, 1)
                .ingotSmeltInto(Neodymium)
                .arcSmeltInto(Neodymium)
                .macerateInto(Neodymium)
                .build();
        Neodymium.getProperty(PropertyKey.INGOT).setMagneticMaterial(NeodymiumMagnetic);

        HydrochloricAcid = new Material.Builder(419, "hydrochloric_acid")
                .fluid(FluidTypes.ACID)
                .components(Hydrogen, 1, Chlorine, 1)
                .build();

        Steam = new Material.Builder(420, "steam")
                .fluid(FluidTypes.GAS, true)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .fluidTemp(373)
                .build();

        DistilledWater = new Material.Builder(421, "distilled_water")
                .fluid()
                .color(0x4A94FF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .build();

        SodiumPotassium = new Material.Builder(422, "sodium_potassium")
                .fluid()
                .color(0x64FCB4)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Sodium, 1, Potassium, 1)
                .build();

        SamariumMagnetic = new Material.Builder(423, "samarium_magnetic")
                .ingot()
                .color(0xFFFFCD).iconSet(MAGNETIC)
                .flags(GENERATE_LONG_ROD, IS_MAGNETIC)
                .components(Samarium, 1)
                .ingotSmeltInto(Samarium)
                .arcSmeltInto(Samarium)
                .macerateInto(Samarium)
                .build();
        Samarium.getProperty(PropertyKey.INGOT).setMagneticMaterial(SamariumMagnetic);

        ManganesePhosphide = new Material.Builder(424, "manganese_phosphide")
                .ingot().fluid()
                .color(0xE1B454).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Manganese, 1, Phosphorus, 1)
                .cableProperties(GTValues.V[GTValues.LV], 2, 0, true, 78)
                .blastTemp(1200, GasTier.LOW)
                .fluidTemp(1368)
                .build();

        MagnesiumDiboride = new Material.Builder(425, "magnesium_diboride")
                .ingot().fluid()
                .color(0x331900).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Magnesium, 1, Boron, 2)
                .cableProperties(GTValues.V[GTValues.MV], 4, 0, true, 78)
                .blastTemp(2500, GasTier.LOW, VA[HV], 1000)
                .fluidTemp(1103)
                .build();

        MercuryBariumCalciumCuprate = new Material.Builder(426, "mercury_barium_calcium_cuprate")
                .ingot().fluid()
                .color(0x555555).iconSet(SHINY)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Mercury, 1, Barium, 2, Calcium, 2, Copper, 3, Oxygen, 8)
                .cableProperties(GTValues.V[GTValues.HV], 4, 0, true, 78)
                .blastTemp(3300, GasTier.LOW, VA[HV], 1500)
                .fluidTemp(1075)
                .build();

        UraniumTriplatinum = new Material.Builder(427, "uranium_triplatinum")
                .ingot().fluid()
                .color(0x008700).iconSet(SHINY)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Uranium238, 1, Platinum, 3)
                .cableProperties(GTValues.V[GTValues.EV], 6, 0, true, 30)
                .blastTemp(4400, GasTier.MID, VA[EV], 1000)
                .fluidTemp(1882)
                .build()
                .setFormula("UPt3", true);

        SamariumIronArsenicOxide = new Material.Builder(428, "samarium_iron_arsenic_oxide")
                .ingot().fluid()
                .color(0x330033).iconSet(SHINY)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Samarium, 1, Iron, 1, Arsenic, 1, Oxygen, 1)
                .cableProperties(GTValues.V[GTValues.IV], 6, 0, true, 30)
                .blastTemp(5200, GasTier.MID, VA[EV], 1500)
                .fluidTemp(1347)
                .build();

        IndiumTinBariumTitaniumCuprate = new Material.Builder(429, "indium_tin_barium_titanium_cuprate")
                .ingot().fluid()
                .color(0x994C00).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_ELECTROLYZING, GENERATE_FINE_WIRE)
                .components(Indium, 4, Tin, 2, Barium, 2, Titanium, 1, Copper, 7, Oxygen, 14)
                .cableProperties(GTValues.V[GTValues.LuV], 8, 0, true, 5)
                .blastTemp(6000, GasTier.HIGH, VA[IV], 1000)
                .fluidTemp(1012)
                .build();

        UraniumRhodiumDinaquadide = new Material.Builder(430, "uranium_rhodium_dinaquadide")
                .ingot().fluid()
                .color(0x0A0A0A)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, GENERATE_FINE_WIRE)
                .components(Uranium238, 1, Rhodium, 1, Naquadah, 2)
                .cableProperties(GTValues.V[GTValues.ZPM], 8, 0, true, 5)
                .blastTemp(9000, GasTier.HIGH, VA[IV], 1500)
                .fluidTemp(3410)
                .build()
                .setFormula("URhNq2", true);

        EnrichedNaquadahTriniumEuropiumDuranide = new Material.Builder(431, "enriched_naquadah_trinium_europium_duranide")
                .ingot().fluid()
                .color(0x7D9673).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, GENERATE_FINE_WIRE)
                .components(NaquadahEnriched, 4, Trinium, 3, Europium, 2, Duranium, 1)
                .cableProperties(GTValues.V[GTValues.UV], 16, 0, true, 3)
                .blastTemp(9900, GasTier.HIGH, VA[LuV], 1000)
                .fluidTemp(5930)
                .build();

        RutheniumTriniumAmericiumNeutronate = new Material.Builder(432, "ruthenium_trinium_americium_neutronate")
                .ingot().fluid()
                .color(0xFFFFFF).iconSet(BRIGHT)
                .flags(DECOMPOSITION_BY_ELECTROLYZING)
                .components(Ruthenium, 1, Trinium, 2, Americium, 1, Neutronium, 2, Oxygen, 8)
                .cableProperties(GTValues.V[GTValues.UHV], 24, 0, true, 3)
                .blastTemp(10800, GasTier.HIGHER)
                .fluidTemp(23691)
                .build();

        InertMetalMixture = new Material.Builder(433, "inert_metal_mixture")
                .dust()
                .color(0xE2AE72).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Rhodium, 1, Ruthenium, 1, Oxygen, 4)
                .build();

        RhodiumSulfate = new Material.Builder(434, "rhodium_sulfate")
                .fluid()
                .color(0xEEAA55)
                .flags(DISABLE_DECOMPOSITION)
                .components(Rhodium, 2, Sulfur, 3, Oxygen, 12)
                .fluidTemp(1128)
                .build().setFormula("Rh2(SO4)3", true);

        RutheniumTetroxide = new Material.Builder(435, "ruthenium_tetroxide")
                .dust()
                .color(0xC7C7C7)
                .flags(DISABLE_DECOMPOSITION)
                .components(Ruthenium, 1, Oxygen, 4)
                .build();

        OsmiumTetroxide = new Material.Builder(436, "osmium_tetroxide")
                .dust()
                .color(0xACAD71).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Osmium, 1, Oxygen, 4)
                .build();

        IridiumChloride = new Material.Builder(437, "iridium_chloride")
                .dust()
                .color(0x013220).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iridium, 1, Chlorine, 3)
                .build();

        FluoroantimonicAcid = new Material.Builder(438, "fluoroantimonic_acid")
                .fluid(FluidTypes.ACID)
                .components(Hydrogen, 2, Antimony, 1, Fluorine, 7)
                .build();

        TitaniumTrifluoride = new Material.Builder(439, "titanium_trifluoride")
                .dust()
                .color(0x8F00FF).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Titanium, 1, Fluorine, 3)
                .build();

        CalciumPhosphide = new Material.Builder(440, "calcium_phosphide")
                .dust()
                .color(0xA52A2A).iconSet(METALLIC)
                .components(Calcium, 1, Phosphorus, 1)
                .build();

        IndiumPhosphide = new Material.Builder(441, "indium_phosphide")
                .dust()
                .color(0x582E5C).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Indium, 1, Phosphorus, 1)
                .build();

        BariumSulfide = new Material.Builder(442, "barium_sulfide")
                .dust()
                .color(0xF0EAD6).iconSet(METALLIC)
                .components(Barium, 1, Sulfur, 1)
                .build();

        TriniumSulfide = new Material.Builder(443, "trinium_sulfide")
                .dust()
                .color(0xE68066).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Trinium, 1, Sulfur, 1)
                .build();

        ZincSulfide = new Material.Builder(444, "zinc_sulfide")
                .dust()
                .color(0xFFFFF6).iconSet(DULL)
                .components(Zinc, 1, Sulfur, 1)
                .build();

        GalliumSulfide = new Material.Builder(445, "gallium_sulfide")
                .dust()
                .color(0xFFF59E).iconSet(SHINY)
                .components(Gallium, 1, Sulfur, 1)
                .build();

        AntimonyTrifluoride = new Material.Builder(446, "antimony_trifluoride")
                .dust()
                .color(0xF7EABC).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Antimony, 1, Fluorine, 3)
                .build();

        EnrichedNaquadahSulfate = new Material.Builder(447, "enriched_naquadah_sulfate")
                .dust()
                .color(0x2E2E1C).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(NaquadahEnriched, 1, Sulfur, 1, Oxygen, 4)
                .build();

        NaquadriaSulfate = new Material.Builder(448, "naquadria_sulfate")
                .dust()
                .color(0x006633).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Naquadria, 1, Sulfur, 1, Oxygen, 4)
                .build();

        Pyrochlore = new Material.Builder(449, "pyrochlore")
                .dust().ore()
                .color(0x2B1100).iconSet(METALLIC)
                .flags()
                .components(Calcium, 2, Niobium, 2, Oxygen, 7)
                .build();

        LiquidHelium = new Material.Builder(450, "liquid_helium")
                .fluid()
                .color(0xFCFF90)
                .flags(DISABLE_DECOMPOSITION)
                .components(Helium, 1)
                .fluidTemp(4)
                .build();

        BlueVitriol = new Material.Builder(451, "blue_vitriol")
                .fluid()
                .color(0x4242DE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Copper, 1, Sulfur, 1, Oxygen, 4)
                .build();

        GreenVitriol = new Material.Builder(452, "green_vitriol")
                .fluid()
                .color(0x42DE42)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iron, 1, Sulfur, 1, Oxygen, 4)
                .build();

        RedVitriol = new Material.Builder(453, "red_vitriol")
                .fluid()
                .color(0xDE4242)
                .flags(DISABLE_DECOMPOSITION)
                .components(Cobalt, 1, Sulfur, 1, Oxygen, 4)
                .build();

        PinkVitriol = new Material.Builder(454, "pink_vitriol")
                .fluid()
                .color(0xDE6F6F)
                .flags(DISABLE_DECOMPOSITION)
                .components(Magnesium, 1, Sulfur, 1, Oxygen, 4)
                .build();

        CyanVitriol = new Material.Builder(455, "cyan_vitriol")
                .fluid()
                .color(0x6FDEDE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Nickel, 1, Sulfur, 1, Oxygen, 4)
                .build();

        WhiteVitriol = new Material.Builder(456, "white_vitriol")
                .fluid()
                .color(0xDEDEDE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zinc, 1, Sulfur, 1, Oxygen, 4)
                .build();

        GrayVitriol = new Material.Builder(457, "gray_vitriol")
                .fluid()
                .color(0x6F6F6F)
                .flags(DISABLE_DECOMPOSITION)
                .components(Manganese, 1, Sulfur, 1, Oxygen, 4)
                .build();

        ClayVitriol = new Material.Builder(458, "clay_vitriol")
                .fluid()
                .color(0x42DEDE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Aluminium, 2, Sulfur, 3, Oxygen, 12)
                .build()
                .setFormula("Al2(SO4)3", true);

        ChloroauricAcid = new Material.Builder(459, "chloroauric_acid")
                .fluid(FluidTypes.ACID)
                .color(0xFFC846)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 1, Gold, 1, Chlorine, 4)
                .build();

        ChloroplatinicAcid = new Material.Builder(460, "chloroplatinic_acid")
                .fluid(FluidTypes.ACID)
                .color(0xFF4646)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Platinum, 1, Chlorine, 6)
                .build();

        CarbonDisulfide = new Material.Builder(461, "carbon_disulfide")
                .fluid(FluidTypes.GAS)
                .color(0x302C01)
                .components(Carbon, 1, Sulfur, 2)
                .build();

        SodiumEthylXanthate = new Material.Builder(462, "sodium_ethyl_xanthate")
                .fluid()
                .color(0xEAF514)
                .components(Carbon, 3, Hydrogen, 5, Oxygen, 1, Sulfur, 2, Sodium, 1)
                .build();

        GraniticMineralSand = new Material.Builder(2513, "granitic_mineral_sand")
                .dust(1).ore()
                .color(0x283C3C).iconSet(SAND)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iron, 3, Oxygen, 4)
                .build();

        BasalticMineralSand = new Material.Builder(2518, "basaltic_mineral_sand")
                .dust(1).ore()
                .color(0x283228).iconSet(SAND)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iron, 3, Oxygen, 4)
                .build();
    }
}
