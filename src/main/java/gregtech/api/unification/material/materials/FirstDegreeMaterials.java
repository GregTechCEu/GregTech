package gregtech.api.unification.material.materials;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraft.init.Enchantments;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;

public class FirstDegreeMaterials {
    /**
     * ID RANGE: 124-253 (incl.)
     */
    public static void register() {
        Almandine = new Material.Builder(250, "almandine")
                .gem(1).ore(6, 1)
                .color(0xFF0000)
                .flags(STD_GEM, GENERATE_LENS)
                .components(Aluminium, 2, Iron, 3, Silicon, 3, Oxygen, 12)
                .build();

        Andradite = new Material.Builder(251, "andradite")
                .gem(1)
                .color(0x967800).iconSet(RUBY)
                .components(Calcium, 3, Iron, 2, Silicon, 3, Oxygen, 12)
                .build();

        AnnealedCopper = new Material.Builder(252, "annealed_copper")
                .ingot().fluid()
                .color(0xFF8D3B).iconSet(BRIGHT)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .components(Copper, 1)
                .cableProperties(GTValues.V[2], 1, 1)
                .build();
        Copper.getProperty(PropertyKey.INGOT).setArcSmeltingInto(AnnealedCopper);

        Asbestos = new Material.Builder(253, "asbestos")
                .dust(1).ore()
                .color(0xE6E6E6)
                .components(Magnesium, 3, Silicon, 2, Hydrogen, 4, Oxygen, 9)
                .build();

        Ash = new Material.Builder(254, "ash")
                .dust(1)
                .color(0x969696)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        BandedIron = new Material.Builder(255, "banded_iron")
                .dust().ore()
                .color(0x915A5A)
                .components(Iron, 2, Oxygen, 3)
                .build();

        BatteryAlloy = new Material.Builder(256, "battery_alloy")
                .ingot(1).fluid()
                .color(0x9C7CA0)
                .flags(EXT_METAL)
                .components(Lead, 4, Antimony, 1)
                .build();

        BlueTopaz = new Material.Builder(257, "blue_topaz")
                .gem(3).ore()
                .color(0x7B96DC).iconSet(GEM_HORIZONTAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Silicon, 1, Fluorine, 2, Hydrogen, 2, Oxygen, 6)
                .toolStats(7.0f, 3.0f, 256, 15)
                .build();

        Bone = new Material.Builder(258, "bone")
                .dust(1)
                .color(0xFAFAFA)
                .flags(MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Calcium, 1)
                .build();

        Brass = new Material.Builder(259, "brass")
                .ingot(1).fluid()
                .color(0xFFB400).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_RING)
                .components(Zinc, 1, Copper, 3)
                .toolStats(8.0f, 3.0f, 152, 21)
                .itemPipeProperties(2048, 1)
                .build();

        Bronze = new Material.Builder(260, "bronze")
                .ingot().fluid()
                .color(0xFF8000).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_FRAME)
                .components(Tin, 1, Copper, 3)
                .toolStats(6.0f, 2.5f, 192, 21)
                .fluidPipeProperties(1696, 20, true)
                .build();

        BrownLimonite = new Material.Builder(261, "brown_limonite")
                .dust(1).ore()
                .color(0xC86400).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, BLAST_FURNACE_CALCITE_TRIPLE)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .build();

        Calcite = new Material.Builder(262, "calcite")
                .dust(1).ore()
                .color(0xFAE6DC)
                .components(Calcium, 1, Carbon, 1, Oxygen, 3)
                .build();

        Cassiterite = new Material.Builder(263, "cassiterite")
                .dust(1).ore(2, 1)
                .color(0xDCDCDC).iconSet(METALLIC)
                .components(Tin, 1, Oxygen, 2)
                .build();

        CassiteriteSand = new Material.Builder(264, "cassiterite_sand")
                .dust(1).ore(2, 1)
                .color(0xDCDCDC).iconSet(SAND)
                .components(Tin, 1, Oxygen, 2)
                .build();

        Chalcopyrite = new Material.Builder(265, "chalcopyrite")
                .dust(1).ore()
                .color(0xA07828)
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
                .build();

        Clay = new Material.Builder(270, "clay")
                .dust(1)
                .color(0xC8C8DC).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE)
                .components(Sodium, 2, Lithium, 1, Aluminium, 2, Silicon, 2, Water, 6)
                .build();

        Coal = new Material.Builder(271, "coal")
                .gem(1, 1600).ore() //default coal burn time in vanilla
                .color(0x464646).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Cobaltite = new Material.Builder(272, "cobaltite")
                .dust(1).ore()
                .color(0x5050FA).iconSet(METALLIC)
                .components(Cobalt, 1, Arsenic, 1, Sulfur, 1)
                .build();

        Cooperite = new Material.Builder(273, "cooperite")
                .dust(1).ore()
                .color(0xFFFFC8).iconSet(METALLIC)
                .components(Platinum, 3, Nickel, 1, Sulfur, 1, Palladium, 1)
                .build();

        Cupronickel = new Material.Builder(274, "cupronickel")
                .ingot(1).fluid()
                .color(0xE39680).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Copper, 1, Nickel, 1)
                .itemPipeProperties(2048, 1)
                .cableProperties(GTValues.V[1], 1, 1)
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
                .flags(GENERATE_BOLT_SCREW, GENERATE_LENS, GENERATE_GEAR, NO_SMASHING, NO_SMELTING, FLAMMABLE,
                        HIGH_SIFTER_OUTPUT, DISABLE_DECOMPOSITION, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .toolStats(8.0f, 3.0f, 1280, 15)
                .build();

        Electrum = new Material.Builder(277, "electrum")
                .ingot().fluid()
                .color(0xFFFF64).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .components(Silver, 1, Gold, 1)
                .itemPipeProperties(1024, 2)
                .cableProperties(GTValues.V[3], 3, 2)
                .build();

        Emerald = new Material.Builder(278, "emerald")
                .gem().ore()
                .color(0x50FF50).iconSet(EMERALD)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, GENERATE_PLATE)
                .components(Beryllium, 3, Aluminium, 2, Silicon, 6, Oxygen, 18)
                .toolStats(10.0f, 2.0f, 368, 15)
                .build();

        Galena = new Material.Builder(279, "galena")
                .dust(3).ore()
                .color(0x643C64)
                .flags(NO_SMELTING)
                .components(Lead, 3, Silver, 3, Sulfur, 2)
                .build();

        Garnierite = new Material.Builder(280, "garnierite")
                .dust(3).ore()
                .color(0x32C846).iconSet(METALLIC)
                .components(Nickel, 1, Oxygen, 1)
                .build();

        GreenSapphire = new Material.Builder(281, "green_sapphire")
                .gem().ore()
                .color(0x64C882).iconSet(GEM_HORIZONTAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(Aluminium, 2, Oxygen, 3)
                .toolStats(8.0f, 3.0f, 368, 15)
                .build();

        Grossular = new Material.Builder(282, "grossular")
                .gem(1).ore(6, 1)
                .color(0xC86400).iconSet(RUBY)
                .components(Calcium, 3, Aluminium, 2, Silicon, 3, Oxygen, 12)
                .build();

        Ice = new Material.Builder(283, "ice")
                .dust(0).fluid()
                .color(0xC8C8FF).iconSet(SHINY)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .build();

        Ilmenite = new Material.Builder(284, "ilmenite")
                .dust(3).ore(3, 1)
                .color(0x463732).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iron, 1, Titanium, 1, Oxygen, 3)
                .build();

        Rutile = new Material.Builder(285, "rutile")
                .gem().ore(3, 1)
                .color(0xD40D5C).iconSet(GEM_HORIZONTAL)
                .flags(DISABLE_DECOMPOSITION)
                .components(Titanium, 1, Oxygen, 2)
                .build();

        Bauxite = new Material.Builder(286, "bauxite")
                .dust(1).ore(3, 1)
                .color(0xC86400)
                .flags(DISABLE_DECOMPOSITION)
                .components(Rutile, 2, Aluminium, 16, Hydrogen, 10, Oxygen, 11)
                .build();

        Invar = new Material.Builder(287, "invar")
                .ingot().fluid()
                .color(0xB4B478).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_RING, GENERATE_FRAME)
                .components(Iron, 2, Nickel, 1)
                .toolStats(7.0f, 3.0f, 512, 21)
                .addDefaultEnchant(Enchantments.BANE_OF_ARTHROPODS, 3)
                .fluidPipeProperties(2395, 40, true)
                .build();

        Kanthal = new Material.Builder(288, "kanthal")
                .ingot().fluid()
                .color(0xC2D2DF).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Iron, 1, Aluminium, 1, Chrome, 1)
                .cableProperties(GTValues.V[3], 4, 3)
                .blastTemp(1800)
                .build();

        Lazurite = new Material.Builder(289, "lazurite")
                .gem(1).ore(6, 4)
                .color(0x6478FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, GENERATE_ROD, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 6, Silicon, 6, Calcium, 8, Sodium, 8)
                .build();

        Magnalium = new Material.Builder(290, "magnalium")
                .ingot().fluid()
                .color(0xC8BEFF)
                .flags(EXT2_METAL)
                .components(Magnesium, 1, Aluminium, 2)
                .toolStats(6.0f, 2.0f, 256, 21)
                .itemPipeProperties(1024, 2)
                .build();

        Magnesite = new Material.Builder(291, "magnesite")
                .dust().ore()
                .color(0xFAFAB4).iconSet(METALLIC)
                .components(Magnesium, 1, Carbon, 1, Oxygen, 3)
                .build();

        Magnetite = new Material.Builder(292, "magnetite")
                .dust().ore()
                .color(0x1E1E1E).iconSet(METALLIC)
                .components(Iron, 3, Oxygen, 4)
                .build();

        Molybdenite = new Material.Builder(293, "molybdenite")
                .dust().ore()
                .color(0x191919).iconSet(METALLIC)
                .components(Molybdenum, 1, Sulfur, 2)
                .build();

        Nichrome = new Material.Builder(294, "nichrome")
                .ingot().fluid()
                .color(0xCDCEF6).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Nickel, 4, Chrome, 1)
                .cableProperties(GTValues.V[4], 4, 4)
                .blastTemp(2700)
                .build();

        NiobiumNitride = new Material.Builder(295, "niobium_nitride")
                .ingot().fluid()
                .color(0x1D291D)
                .flags(EXT_METAL)
                .components(Niobium, 1, Nitrogen, 1)
                .blastTemp(2573)
                .build();

        NiobiumTitanium = new Material.Builder(296, "niobium_titanium")
                .ingot().fluid()
                .color(0x1D1D29)
                .flags(EXT2_METAL, GENERATE_SPRING, GENERATE_SPRING_SMALL)
                .components(Niobium, 1, Titanium, 1)
                .fluidPipeProperties(2900, 150, true)
                .cableProperties(GTValues.V[6], 4, 2)
                .blastTemp(4500)
                .build();

        Obsidian = new Material.Builder(297, "obsidian")
                .dust(3)
                .color(0x503264)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_RECIPES, GENERATE_PLATE)
                .components(Magnesium, 1, Iron, 1, Silicon, 2, Oxygen, 4)
                .build();

        Phosphate = new Material.Builder(298, "phosphate")
                .dust(1).ore()
                .color(0xFFFF00)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE, EXPLOSIVE)
                .components(Phosphorus, 1, Oxygen, 4)
                .build();

        PigIron = new Material.Builder(299, "pig_iron")
                .ingot().fluid()
                .color(0xC8B4B4).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_RING, BLAST_FURNACE_CALCITE_TRIPLE)
                .components(Iron, 1)
                .toolStats(6.0f, 4.0f, 384, 21)
                .build();

        SterlingSilver = new Material.Builder(300, "sterling_silver")
                .ingot().fluid()
                .color(0xFADCE1).iconSet(SHINY)
                .flags(EXT2_METAL)
                .components(Copper, 1, Silver, 4)
                .toolStats(13.0f, 2.0f, 196, 33)
                .itemPipeProperties(1024, 2)
                .blastTemp(1700)
                .build();

        RoseGold = new Material.Builder(301, "rose_gold")
                .ingot().fluid()
                .color(0xFFE61E).iconSet(SHINY)
                .flags(EXT2_METAL)
                .components(Copper, 1, Gold, 4)
                .toolStats(14.0f, 2.0f, 152, 33)
                .addDefaultEnchant(Enchantments.SMITE, 4)
                .itemPipeProperties(1024, 2)
                .blastTemp(1600)
                .build();

        BlackBronze = new Material.Builder(302, "black_bronze")
                .ingot().fluid()
                .color(0x64327D)
                .flags(EXT2_METAL)
                .components(Gold, 1, Silver, 1, Copper, 3)
                .toolStats(12.0f, 2.0f, 256, 21)
                .addDefaultEnchant(Enchantments.SMITE, 2)
                .itemPipeProperties(1024, 2)
                .blastTemp(2000)
                .build();

        BismuthBronze = new Material.Builder(303, "bismuth_bronze")
                .ingot().fluid()
                .color(0x647D7D)
                .flags(EXT2_METAL)
                .components(Bismuth, 1, Zinc, 1, Copper, 3)
                .toolStats(8.0f, 3.0f, 256, 21)
                .addDefaultEnchant(Enchantments.BANE_OF_ARTHROPODS, 5)
                .blastTemp(1100)
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
                .flags(BLAST_FURNACE_CALCITE_DOUBLE)
                .components(Iron, 1, Sulfur, 2)
                .build();

        Pyrolusite = new Material.Builder(307, "pyrolusite")
                .dust().ore()
                .color(0x9696AA)
                .components(Manganese, 1, Oxygen, 2)
                .build();

        Pyrope = new Material.Builder(308, "pyrope")
                .gem().ore(4, 1)
                .color(0x783264).iconSet(RUBY)
                .flags(GENERATE_LENS)
                .components(Aluminium, 2, Magnesium, 3, Silicon, 3, Oxygen, 12)
                .build();

        RockSalt = new Material.Builder(309, "rock_salt")
                .dust(1).ore(2, 1)
                .color(0xF0C8C8).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Potassium, 1, Chlorine, 1)
                .build();

        // FREE ID 310

        Ruby = new Material.Builder(311, "ruby")
                .gem().ore()
                .color(0xFF6464).iconSet(RUBY)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Chrome, 1, Aluminium, 2, Oxygen, 3)
                .toolStats(8.5f, 3.0f, 256, 33)
                .build();

        Salt = new Material.Builder(312, "salt")
                .dust(1).ore(2, 1)
                .color(0xFAFAFA).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Sodium, 1, Chlorine, 1)
                .build();

        Saltpeter = new Material.Builder(313, "saltpeter")
                .dust(1).ore(4, 1)
                .color(0xE6E6E6).iconSet(FINE)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE)
                .components(Potassium, 1, Nitrogen, 1, Oxygen, 3)
                .build();

        Sapphire = new Material.Builder(314, "sapphire")
                .gem().ore()
                .color(0x6464C8).iconSet(GEM_VERTICAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(Aluminium, 2, Oxygen, 3)
                .toolStats(7.5f, 4.0f, 256, 15)
                .build();

        Scheelite = new Material.Builder(315, "scheelite")
                .dust(3).ore(2, 1)
                .color(0xC88C14)
                .flags(DECOMPOSITION_REQUIRES_HYDROGEN)
                .components(Tungsten, 1, Calcium, 2, Oxygen, 4)
                .build();

        Sodalite = new Material.Builder(316, "sodalite")
                .gem(1).ore(6, 4)
                .color(0x1414FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, GENERATE_ROD, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 3, Silicon, 3, Sodium, 4, Chlorine, 1)
                .build();

        DiamericiumTitanium = new Material.Builder(317, "diamericium_titanium")
                .ingot(4).fluid()
                .color(0x755280).iconSet(METALLIC)
                .components(Americium, 2, Titanium, 1)
                .toolStats(6.0f, 6.0f, 2200, 21)
                .itemPipeProperties(32, 128)
                .cableProperties(GTValues.V[10], 8, 16)
                .blastTemp(10400)
                .build();

        Tantalite = new Material.Builder(318, "tantalite")
                .dust(3).ore(2, 1)
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
                .flags(EXT_METAL, GENERATE_FINE_WIRE)
                .components(Tin, 9, Antimony, 1)
                .cableProperties(GTValues.V[1], 1, 1)
                .build();

        Spessartine = new Material.Builder(321, "spessartine")
                .gem().ore(2, 1)
                .color(0xFF6464).iconSet(RUBY)
                .components(Aluminium, 2, Manganese, 3, Silicon, 3, Oxygen, 12)
                .build();

        Sphalerite = new Material.Builder(322, "sphalerite")
                .dust(1).ore()
                .color(0xFFFFFF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zinc, 1, Sulfur, 1)
                .build();

        StainlessSteel = new Material.Builder(323, "stainless_steel")
                .ingot().fluid()
                .color(0xC8C8DC).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_FRAME, GENERATE_LONG_ROD)
                .components(Iron, 7, Chrome, 1, Manganese, 1, Nickel, 1)
                .toolStats(7.0f, 4.0f, 480, 33)
                .fluidPipeProperties(2428, 60, true)
                .blastTemp(1700)
                .build();

        Steel = new Material.Builder(324, "steel")
                .ingot().fluid()
                .color(0x808080).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_DENSE, GENERATE_SPRING,
                        GENERATE_SPRING_SMALL, GENERATE_FRAME, DISABLE_DECOMPOSITION)
                .components(Iron, 1)
                .toolStats(6.0f, 3.0f, 512, 21)
                .fluidPipeProperties(2557, 40, true)
                .cableProperties(GTValues.V[4], 2, 2)
                .blastTemp(1000)
                .build();

        Stibnite = new Material.Builder(325, "stibnite")
                .dust().ore()
                .color(0x464646).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Antimony, 2, Sulfur, 3)
                .build();

        Tanzanite = new Material.Builder(326, "tanzanite")
                .gem().ore(2, 1)
                .color(0x4000C8).iconSet(GEM_VERTICAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(Calcium, 2, Aluminium, 3, Silicon, 3, Hydrogen, 1)
                .toolStats(7.0f, 2.0f, 256, 15)
                .build();

        Tetrahedrite = new Material.Builder(327, "tetrahedrite")
                .dust().ore()
                .color(0xC82000)
                .components(Copper, 3, Antimony, 1, Sulfur, 3, Iron, 1)
                .build();

        TinAlloy = new Material.Builder(328, "tin_alloy")
                .ingot().fluid()
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Tin, 1, Iron, 1)
                .fluidPipeProperties(1572, 38, true)
                .build();

        Topaz = new Material.Builder(329, "topaz")
                .gem(3).ore(2, 1)
                .color(0xFF8000).iconSet(GEM_HORIZONTAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(Aluminium, 2, Silicon, 1, Fluorine, 1, Hydrogen, 2)
                .toolStats(7.0f, 2.0f, 256, 15)
                .build();

        Tungstate = new Material.Builder(330, "tungstate")
                .dust(3).ore(2, 1)
                .color(0x373223)
                .flags(DECOMPOSITION_REQUIRES_HYDROGEN)
                .components(Tungsten, 1, Lithium, 2, Oxygen, 4)
                .build();

        Ultimet = new Material.Builder(331, "ultimet")
                .ingot(4).fluid()
                .color(0xB4B4E6).iconSet(SHINY)
                .flags(EXT2_METAL)
                .components(Cobalt, 5, Chrome, 2, Nickel, 1, Molybdenum, 1)
                .toolStats(9.0f, 4.0f, 2048, 33)
                .itemPipeProperties(128, 16)
                .blastTemp(2700)
                .build();

        Uraninite = new Material.Builder(332, "uraninite")
                .dust(3).ore()
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
                .cableProperties(GTValues.V[6], 4, 2)
                .blastTemp(4500)
                .build();

        WroughtIron = new Material.Builder(335, "wrought_iron")
                .ingot().fluid()
                .color(0xC8B4B4).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_RING, GENERATE_LONG_ROD, DISABLE_DECOMPOSITION, BLAST_FURNACE_CALCITE_TRIPLE)
                .components(Iron, 1)
                .toolStats(6.0f, 3.5f, 384, 21)
                .fluidPipeProperties(2387, 30, true)
                .build();
        Iron.getProperty(PropertyKey.INGOT).setSmeltingInto(WroughtIron);

        Wulfenite = new Material.Builder(336, "wulfenite")
                .dust(3).ore()
                .color(0xFF8000)
                .components(Lead, 1, Molybdenum, 1, Oxygen, 4)
                .build();

        YellowLimonite = new Material.Builder(337, "yellow_limonite")
                .dust().ore()
                .color(0xC8C800).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, BLAST_FURNACE_CALCITE_DOUBLE)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .build();

        YttriumBariumCuprate = new Material.Builder(338, "yttrium_barium_cuprate")
                .ingot().fluid()
                .color(0x504046).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FINE_WIRE, GENERATE_SPRING, GENERATE_SPRING_SMALL)
                .components(Yttrium, 1, Barium, 2, Copper, 3, Oxygen, 7)
                .cableProperties(GTValues.V[6], 4, 4)
                .build();

        NetherQuartz = new Material.Builder(339, "nether_quartz")
                .gem(1).ore(2, 1)
                .color(0xE6D2D2).iconSet(QUARTZ)
                .flags(STD_SOLID, NO_SMELTING, CRYSTALLIZABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        CertusQuartz = new Material.Builder(214, "certus_quartz")
                .gem(1).ore(2, 1)
                .color(0xD2D2E6).iconSet(QUARTZ)
                .flags(STD_SOLID, NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        Quartzite = new Material.Builder(340, "quartzite")
                .gem(1).ore(2, 1)
                .color(0xD2E6D2).iconSet(QUARTZ)
                .flags(NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION, GENERATE_PLATE)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        Graphite = new Material.Builder(341, "graphite")
                .ingot().ore().fluid()
                .color(0x808080)
                .flags(STD_METAL, NO_SMELTING, FLAMMABLE, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Graphene = new Material.Builder(342, "graphene")
                .ingot().fluid()
                .color(0x808080).iconSet(SHINY)
                .flags(GENERATE_FOIL, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .cableProperties(GTValues.V[5], 1, 1)
                .build();

        Jasper = new Material.Builder(343, "jasper")
                .gem().ore()
                .color(0xC85050).iconSet(EMERALD)
                .flags(NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .build();

        Osmiridium = new Material.Builder(344, "osmiridium")
                .ingot(3).fluid()
                .color(0x6464FF).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Iridium, 3, Osmium, 1)
                .toolStats(9.0f, 3.0f, 3152, 21)
                .itemPipeProperties(64, 32)
                .blastTemp(2500)
                .build();

        Tenorite = new Material.Builder(345, "tenorite")
                .dust(1).ore()
                .color(0x606060)
                .components(Copper, 1, Oxygen, 1)
                .build();

        Cuprite = new Material.Builder(346, "cuprite")
                .dust().ore()
                .color(0x770000).iconSet(RUBY)
                .components(Copper, 2, Oxygen, 1)
                .build();

        Bornite = new Material.Builder(347, "bornite")
                .dust(1).ore()
                .color(0x97662B).iconSet(METALLIC)
                .components(Copper, 5, Iron, 1, Sulfur, 4)
                .build();

        Chalcocite = new Material.Builder(348, "chalcocite")
                .dust().ore()
                .color(0x353535).iconSet(GEM_VERTICAL)
                .components(Copper, 2, Sulfur, 1)
                .build();

        Enargite = new Material.Builder(349, "enargite")
                .dust().ore(2, 1)
                .color(0xBBBBBB).iconSet(METALLIC)
                .components(Copper, 3, Arsenic, 1, Sulfur, 4)
                .build();

        Tennantite = new Material.Builder(350, "tennantite")
                .dust().ore(2, 1)
                .color(0x909090).iconSet(METALLIC)
                .components(Copper, 12, Arsenic, 4, Sulfur, 13)
                .build();

        GalliumArsenide = new Material.Builder(351, "gallium_arsenide")
                .ingot(1).fluid()
                .color(0xA0A0A0)
                .flags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Arsenic, 1, Gallium, 1)
                .blastTemp(1200)
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
                .build();

        NickelZincFerrite = new Material.Builder(355, "nickel_zinc_ferrite")
                .ingot(0).fluid()
                .color(0x3C3C3C).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_RING)
                .components(Nickel, 1, Zinc, 1, Iron, 4)
                .build();

        SiliconDioxide = new Material.Builder(356, "silicon_dioxide")
                .dust(1)
                .color(0xC8C8C8).iconSet(QUARTZ)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE)
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
                .gem()
                .color(0x9D2123).iconSet(EMERALD)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Arsenic, 4, Sulfur, 4)
                .build();

        SodiumBicarbonate = new Material.Builder(366, "sodium_bicarbonate")
                .dust(1)
                .color(0x565b96).iconSet(ROUGH)
                .components(Sodium, 1, Hydrogen, 1, Carbon, 1, Oxygen, 5)
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

        Zincite = new Material.Builder(370, "zincite")
                .dust(1)
                .color(0xFFFFF5)
                .components(Zinc, 1, Oxygen, 1)
                .build();

        CupricOxide = new Material.Builder(371, "cupric_oxide")
                .dust(1)
                .color(0x0F0F0F)
                .components(Copper, 1, Oxygen, 1)
                .build();

        CobaltOxide = new Material.Builder(372, "cobalt_oxide")
                .dust(1)
                .color(0x788000)
                .components(Cobalt, 1, Oxygen, 1)
                .build();

        ArsenicTrioxide = new Material.Builder(373, "arsenic_trioxide")
                .dust(1)
                .iconSet(ROUGH)
                .components(Arsenic, 2, Oxygen, 3)
                .build();

        Massicot = new Material.Builder(374, "massicot")
                .dust(1)
                .color(0xFFDD55)
                .components(Lead, 1, Oxygen, 1)
                .build();

        Ferrosilite = new Material.Builder(375, "ferrosilite")
                .dust(1)
                .color(0x97632A)
                .components(Iron, 1, Silicon, 1, Oxygen, 3)
                .build();

        MetalMixture = new Material.Builder(376, "metal_mixture")
                .dust(1)
                .color(0x502d16).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .build();

        SodiumHydroxide = new Material.Builder(377, "sodium_hydroxide")
                .dust(1)
                .color(0x003380)
                .components(Sodium, 1, Oxygen, 1, Hydrogen, 1)
                .build();

        SodiumPersulfate = new Material.Builder(378, "sodium_persulfate")
                .fluid()
                .components(Sodium, 2, Sulfur, 2, Oxygen, 8)
                .build();

        Bastnasite = new Material.Builder(379, "bastnasite")
                .dust().ore(2, 1)
                .color(0xC86E2D).iconSet(FINE)
                .components(Cerium, 1, Carbon, 1, Fluorine, 1, Oxygen, 3)
                .build();

        Pentlandite = new Material.Builder(380, "pentlandite")
                .dust().ore()
                .color(0xA59605)
                .components(Nickel, 9, Sulfur, 8)
                .build();

        Spodumene = new Material.Builder(381, "spodumene")
                .dust().ore(2, 1)
                .color(0xBEAAAA)
                .components(Lithium, 1, Aluminium, 1, Silicon, 2, Oxygen, 6)
                .build();

        Lepidolite = new Material.Builder(382, "lepidolite")
                .dust().ore(5, 1)
                .color(0xF0328C).iconSet(FINE)
                .components(Potassium, 1, Lithium, 3, Aluminium, 4, Fluorine, 2, Oxygen, 10)
                .build();

        Glauconite = new Material.Builder(383, "glauconite")
                .dust().ore()
                .color(0x82B43C)
                .components(Potassium, 1, Magnesium, 2, Aluminium, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        GlauconiteSand = new Material.Builder(384, "glauconite_sand")
                .dust()
                .color(0x82B43C).iconSet(SAND)
                .components(Potassium, 1, Magnesium, 2, Aluminium, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        Malachite = new Material.Builder(385, "malachite")
                .gem().ore()
                .color(0x055F05).iconSet(LAPIS)
                .components(Copper, 2, Carbon, 1, Hydrogen, 2, Oxygen, 5)
                .build();

        Mica = new Material.Builder(386, "mica")
                .dust()
                .color(0xC3C3CD).iconSet(FINE)
                .components(Potassium, 1, Aluminium, 3, Silicon, 3, Fluorine, 2, Oxygen, 10)
                .build();

        Barite = new Material.Builder(387, "barite")
                .dust().ore()
                .color(0xE6EBEB)
                .components(Barium, 1, Sulfur, 1, Oxygen, 4)
                .build();

        Alunite = new Material.Builder(388, "alunite")
                .dust()
                .color(0xE1B441).iconSet(METALLIC)
                .components(Potassium, 1, Aluminium, 3, Silicon, 2, Hydrogen, 6, Oxygen, 14)
                .build();

        Dolomite = new Material.Builder(389, "dolomite")
                .dust(1)
                .color(0xE1CDCD).iconSet(FLINT)
                .components(Calcium, 1, Magnesium, 1, Carbon, 2, Oxygen, 6)
                .build();

        Wollastonite = new Material.Builder(390, "wollastonite")
                .dust()
                .color(0xF0F0F0)
                .components(Calcium, 1, Silicon, 1, Oxygen, 3)
                .build();

        Kaolinite = new Material.Builder(391, "kaolinite")
                .dust()
                .color(0xf5EBEB).iconSet(METALLIC)
                .components(Aluminium, 2, Silicon, 2, Hydrogen, 4, Oxygen, 9)
                .build();

        Talc = new Material.Builder(392, "talc")
                .dust().ore()
                .color(0x5AB45A).iconSet(FINE)
                .components(Magnesium, 3, Silicon, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        Soapstone = new Material.Builder(393, "soapstone")
                .dust(1).ore(3, 1)
                .color(0x5F915F)
                .components(Magnesium, 3, Silicon, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        Kyanite = new Material.Builder(394, "kyanite")
                .dust()
                .color(0x6E6EFA).iconSet(FLINT)
                .components(Aluminium, 2, Silicon, 1, Oxygen, 5)
                .build();

        IronMagnetic = new Material.Builder(395, "iron_magnetic")
                .ingot()
                .color(0xC8C8C8).iconSet(MAGNETIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .components(Iron, 1)
                .ingotSmeltInto(Iron)
                .arcSmeltInto(Iron)
                .macerateInto(Iron)
                .build();
        Iron.getProperty(PropertyKey.INGOT).setMagneticMaterial(IronMagnetic);

        TungstenCarbide = new Material.Builder(396, "tungsten_carbide")
                .ingot().fluid()
                .color(0x330066).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Tungsten, 1, Carbon, 1)
                .toolStats(12.0f, 4.0f, 1280, 21)
                .fluidPipeProperties(7568, 125, true)
                .blastTemp(2460)
                .build();

        CarbonDioxide = new Material.Builder(397, "carbon_dioxide")
                .fluid(Material.FluidType.GAS)
                .color(0xA9D0F5)
                .components(Carbon, 1, Oxygen, 2)
                .build();

        TitaniumTetrachloride = new Material.Builder(398, "titanium_tetrachloride")
                .fluid().fluidTemp(2220)
                .color(0xD40D5C)
                .flags(DISABLE_DECOMPOSITION)
                .components(Titanium, 1, Chlorine, 4)
                .build();

        NitrogenDioxide = new Material.Builder(399, "nitrogen_dioxide")
                .fluid(Material.FluidType.GAS)
                .color(0x85FCFF).iconSet(GAS)
                .components(Nitrogen, 1, Oxygen, 2)
                .build();

        HydrogenSulfide = new Material.Builder(400, "hydrogen_sulfide")
                .fluid(Material.FluidType.GAS)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Sulfur, 1)
                .build();

        NitricAcid = new Material.Builder(401, "nitric_acid")
                .fluid()
                .color(0xCCCC00)
                .components(Hydrogen, 1, Nitrogen, 1, Oxygen, 3)
                .build();

        SulfuricAcid = new Material.Builder(402, "sulfuric_acid")
                .fluid()
                .components(Hydrogen, 2, Sulfur, 1, Oxygen, 4)
                .build();

        PhosphoricAcid = new Material.Builder(403, "phosphoric_acid")
                .fluid()
                .color(0xDCDC01)
                .components(Hydrogen, 3, Phosphorus, 1, Oxygen, 4)
                .build();

        SulfurTrioxide = new Material.Builder(404, "sulfur_trioxide")
                .fluid(Material.FluidType.GAS)
                .color(0xA0A014)
                .components(Sulfur, 1, Oxygen, 3)
                .build();

        SulfurDioxide = new Material.Builder(405, "sulfur_dioxide")
                .fluid(Material.FluidType.GAS)
                .color(0xC8C819)
                .components(Sulfur, 1, Oxygen, 2)
                .build();

        CarbonMonoxide = new Material.Builder(406, "carbon_monoxide")
                .fluid(Material.FluidType.GAS)
                .color(0x0E4880)
                .components(Carbon, 1, Oxygen, 1)
                .build();

        HypochlorousAcid = new Material.Builder(407, "hypochlorous_acid")
                .fluid()
                .color(0x6F8A91)
                .components(Hydrogen, 1, Chlorine, 1, Oxygen, 1)
                .build();

        Ammonia = new Material.Builder(408, "ammonia")
                .fluid(Material.FluidType.GAS)
                .color(0x3F3480)
                .components(Nitrogen, 1, Hydrogen, 3)
                .build();

        HydrofluoricAcid = new Material.Builder(409, "hydrofluoric_acid")
                .fluid()
                .color(0x0088AA)
                .components(Hydrogen, 1, Fluorine, 1)
                .build();

        NitricOxide = new Material.Builder(410, "nitric_oxide")
                .fluid(Material.FluidType.GAS)
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
                .fluid(Material.FluidType.GAS)
                .color(0x42D126)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium238, 1, Fluorine, 6)
                .build()
                .setFormula("UF6", true);

        EnrichedUraniumHexafluoride = new Material.Builder(413, "enriched_uranium_hexafluoride")
                .fluid(Material.FluidType.GAS)
                .color(0x4BF52A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium235, 1, Fluorine, 6)
                .build()
                .setFormula("UF6", true);

        DepletedUraniumHexafluoride = new Material.Builder(414, "depleted_uranium_hexafluoride")
                .fluid(Material.FluidType.GAS)
                .color(0x74BA66)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium238, 1, Fluorine, 6)
                .build()
                .setFormula("UF6", true);

        NitrousOxide = new Material.Builder(415, "nitrous_oxide")
                .fluid(Material.FluidType.GAS)
                .color(0x7DC8FF)
                .components(Nitrogen, 2, Oxygen, 1)
                .build();

        EnderPearl = new Material.Builder(416, "ender_pearl")
                .gem(1)
                .color(0x6CDCC8)
                .flags(GENERATE_LENS, NO_SMASHING, NO_SMELTING)
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
                .flags(EXT2_METAL, GENERATE_LONG_ROD)
                .components(Neodymium, 1)
                .ingotSmeltInto(Neodymium)
                .arcSmeltInto(Neodymium)
                .macerateInto(Neodymium)
                .blastTemp(1297)
                .build();
        Neodymium.getProperty(PropertyKey.INGOT).setMagneticMaterial(NeodymiumMagnetic);

        HydrochloricAcid = new Material.Builder(419, "hydrochloric_acid")
                .fluid()
                .components(Hydrogen, 1, Chlorine, 1)
                .build();

        Steam = new Material.Builder(420, "steam")
                .fluid(Material.FluidType.GAS, true)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .fluidTemp(380)
                .build();

        DistilledWater = new Material.Builder(421, "distilled_water")
                .fluid()
                .color(0xEEEEFF)
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
                .flags(GENERATE_LONG_ROD)
                .components(Samarium, 1)
                .ingotSmeltInto(Samarium)
                .arcSmeltInto(Samarium)
                .macerateInto(Samarium)
                .blastTemp(1345)
                .build();
        Samarium.getProperty(PropertyKey.INGOT).setMagneticMaterial(SamariumMagnetic);
    }
}
