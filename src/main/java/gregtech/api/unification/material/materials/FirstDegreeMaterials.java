package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.MaterialBuilder;

import static gregtech.api.unification.material.MaterialIconSet.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.type.MaterialFlags.*;

public class FirstDegreeMaterials {
    /**
     * ID RANGE: 124-253 (incl.)
     */
    public static void register() {
        Almandine = new MaterialBuilder(124, "almandine")
                .gem(1).ore()
                .color(0xFF0000).iconSet(GEM_VERTICAL)
                .flags(STD_GEM)
                .components(Aluminium, 2, Iron, 3, Silicon, 3, Oxygen, 12)
                .build();

        Andradite = new MaterialBuilder(125, "andradite")
                .dust(1)
                .color(0x967800).iconSet(ROUGH)
                .components(Calcium, 3, Iron, 2, Silicon, 3, Oxygen, 12)
                .build();

        AnnealedCopper = new MaterialBuilder(126, "annealed_copper")
                .ingot().fluid()
                .color(0xFF7814).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .components(Copper, 1)
                .build();

        Asbestos = new MaterialBuilder(127, "asbestos")
                .dust(1)
                .color(0xE6E6E6)
                .components(Magnesium, 3, Silicon, 2, Hydrogen, 4, Oxygen, 9)
                .build();

        Ash = new MaterialBuilder(128, "ash")
                .dust(1)
                .color(0x969696)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        BandedIron = new MaterialBuilder(129, "banded_iron")
                .dust().ore()
                .color(0x915A5A)
                .components(Iron, 2, Oxygen, 3)
                .build();

        BatteryAlloy = new MaterialBuilder(130, "battery_alloy")
                .ingot(1).fluid()
                .color(0x9C7CA0)
                .flags(EXT_METAL)
                .components(Lead, 4, Antimony, 1)
                .build();

        BlueTopaz = new MaterialBuilder(131, "blue_topaz")
                .gem(3).ore()
                .color(0x0000FF).iconSet(GEM_HORIZONTAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Silicon, 1, Fluorine, 2, Hydrogen, 2, Oxygen, 6)
                .toolStats(7.0f, 3.0f, 256)
                .build();

        Bone = new MaterialBuilder(132, "bone")
                .dust(1)
                .color(0xFAFAFA)
                .flags(MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Calcium, 1)
                .build();

        Brass = new MaterialBuilder(133, "brass")
                .ingot(1).fluid()
                .color(0xFFB400).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_RING)
                .components(Zinc, 1, Copper, 3)
                .toolStats(8.0f, 3.0f, 152)
                .build();

        Bronze = new MaterialBuilder(134, "bronze")
                .ingot().fluid()
                .color(0xFF8000).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_FRAME)
                .components(Tin, 1, Copper, 3)
                .toolStats(6.0f, 2.5f, 192)
                .build();

        BrownLimonite = new MaterialBuilder(135, "brown_limonite")
                .dust(1).ore()
                .color(0xC86400).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .build();

        Calcite = new MaterialBuilder(136, "calcite")
                .dust(1).ore()
                .color(0xFAE6DC)
                .components(Calcium, 1, Carbon, 1, Oxygen, 3)
                .build();

        Cassiterite = new MaterialBuilder(137, "cassiterite")
                .dust(1).ore()
                .color(0xDCDCDC).iconSet(METALLIC)
                .components(Tin, 1, Oxygen, 2)
                .build();

        CassiteriteSand = new MaterialBuilder(138, "cassiterite_sand")
                .dust(1).ore()
                .color(0xDCDCDC).iconSet(SAND)
                .components(Tin, 1, Oxygen, 2)
                .build();

        Chalcopyrite = new MaterialBuilder(139, "chalcopyrite")
                .dust(1).ore()
                .color(0xA07828)
                .components(Copper, 1, Iron, 1, Sulfur, 2)
                .build();

        Charcoal = new MaterialBuilder(140, "charcoal")
                .gem(1)
                .color(0x644646).iconSet(FINE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE)
                .components(Carbon, 1)
                .build();

        Chromite = new MaterialBuilder(141, "chromite")
                .dust(1).ore()
                .color(0x23140F).iconSet(METALLIC)
                .components(Iron, 1, Chrome, 2, Oxygen, 4)
                .build();

        Cinnabar = new MaterialBuilder(142, "cinnabar")
                .gem(1).ore()
                .color(0x960000).iconSet(EMERALD)
                .flags(CRYSTALLIZABLE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Mercury, 1, Sulfur, 1)
                .build();

        Water = new MaterialBuilder(143, "water")
                .fluid()
                .color(0x0000FF).iconSet(FLUID)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .build();

        Clay = new MaterialBuilder(144, "clay")
                .dust(1)
                .color(0xC8C8DC).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE)
                .components(Sodium, 2, Lithium, 1, Aluminium, 2, Silicon, 2, Water, 6)
                .build();

        Coal = new MaterialBuilder(145, "coal")
                .gem(1).ore()
                .color(0x464646).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Cobaltite = new MaterialBuilder(146, "cobaltite")
                .dust(1).ore()
                .color(0x5050FA).iconSet(METALLIC)
                .components(Cobalt, 1, Arsenic, 1, Sulfur, 1)
                .build();

        Cooperite = new MaterialBuilder(147, "cooperite")
                .dust(1).ore()
                .color(0xFFFFC8).iconSet(METALLIC)
                .components(Platinum, 3, Nickel, 1, Sulfur, 1, Palladium, 1)
                .build();

        Cupronickel = new MaterialBuilder(148, "cupronickel")
                .ingot(1).fluid()
                .color(0xE39680).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Copper, 1, Nickel, 1)
                .build();

        DarkAsh = new MaterialBuilder(149, "dark_ash")
                .dust(1)
                .color(0x323232)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Diamond = new MaterialBuilder(150, "diamond")
                .gem(3).ore()
                .color(0xC8FFFF).iconSet(DIAMOND)
                .flags(GENERATE_BOLT_SCREW, GENERATE_LENS, GENERATE_GEAR, NO_SMASHING, NO_SMELTING, FLAMMABLE, HIGH_SIFTER_OUTPUT, DISABLE_DECOMPOSITION, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .toolStats(8.0f, 3.0f, 1280)
                .build();

        Electrum = new MaterialBuilder(151, "electrum")
                .ingot().fluid()
                .color(0xFFFF64).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .components(Silver, 1, Gold, 1)
                .build();

        Emerald = new MaterialBuilder(152, "emerald")
                .gem().ore()
                .color(0x50FF50).iconSet(EMERALD)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Beryllium, 3, Aluminium, 2, Silicon, 6, Oxygen, 18)
                .toolStats(10.0f, 2.0f, 368)
                .build();

        Galena = new MaterialBuilder(153, "galena")
                .dust(3).ore()
                .color(0x643C64)
                .flags(NO_SMELTING)
                .components(Lead, 3, Silver, 3, Sulfur, 2)
                .build();

        Garnierite = new MaterialBuilder(154, "garnierite")
                .dust(3).ore()
                .color(0x32C846).iconSet(METALLIC)
                .components(Nickel, 1, Oxygen, 1)
                .build();

        GreenSapphire = new MaterialBuilder(155, "green_sapphire")
                .gem().ore()
                .color(0x64C882).iconSet(GEM_HORIZONTAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(Aluminium, 2, Oxygen, 3)
                .toolStats(8.0f, 3.0f, 368)
                .build();

        Grossular = new MaterialBuilder(156, "grossular")
                .dust(1).ore()
                .color(0xC86400).iconSet(ROUGH)
                .components(Calcium, 3, Aluminium, 2, Silicon, 3, Oxygen, 12)
                .build();

        Ice = new MaterialBuilder(157, "ice")
                .dust(0).fluid()
                .color(0xC8C8FF).iconSet(SHINY)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .build();

        Ilmenite = new MaterialBuilder(158, "ilmenite")
                .dust(3).ore()
                .color(0x463732).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iron, 1, Titanium, 1, Oxygen, 3)
                .build();

        Rutile = new MaterialBuilder(159, "rutile")
                .gem().ore()
                .color(0xD40D5C).iconSet(GEM_HORIZONTAL)
                .flags(DISABLE_DECOMPOSITION)
                .components(Titanium, 1, Oxygen, 2)
                .build();

        Bauxite = new MaterialBuilder(160, "bauxite")
                .dust(1).ore()
                .color(0xC86400)
                .flags(DISABLE_DECOMPOSITION)
                .components(Rutile, 2, Aluminium, 16, Hydrogen, 10, Oxygen, 11)
                .build();

        Invar = new MaterialBuilder(161, "invar")
                .ingot().fluid()
                .color(0xB4B478).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_RING, GENERATE_FRAME)
                .components(Iron, 2, Nickel, 1)
                .toolStats(7.0f, 3.0f, 512)
                .build();

        Kanthal = new MaterialBuilder(162, "kanthal")
                .ingot().fluid()
                .color(0xC2D2DF).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Iron, 1, Aluminium, 1, Chrome, 1)
                .blastTemp(1800)
                .build();

        Lazurite = new MaterialBuilder(163, "lazurite")
                .gem(1).ore()
                .color(0x6478FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, GENERATE_ROD, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 6, Silicon, 6, Calcium, 8, Sodium, 8)
                .build();

        Magnalium = new MaterialBuilder(164, "magnalium")
                .ingot().fluid()
                .color(0xC8BEFF)
                .flags(EXT2_METAL)
                .components(Magnesium, 1, Aluminium, 2)
                .toolStats(6.0f, 2.0f, 256)
                .build();

        Magnesite = new MaterialBuilder(165, "magnesite")
                .dust().ore()
                .color(0xFAFAB4).iconSet(METALLIC)
                .components(Magnesium, 1, Carbon, 1, Oxygen, 3)
                .build();

        Magnetite = new MaterialBuilder(166, "magnetite")
                .dust().ore()
                .color(0x1E1E1E).iconSet(METALLIC)
                .components(Iron, 3, Oxygen, 4)
                .build();

        Molybdenite = new MaterialBuilder(167, "molybdenite")
                .dust().ore()
                .color(0x191919).iconSet(METALLIC)
                .components(Molybdenum, Sulfur, 2)
                .build();

        Nichrome = new MaterialBuilder(168, "nichrome")
                .ingot().fluid()
                .color(0xCDCEF6).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Nickel, 4, Chrome, 1)
                .blastTemp(2700)
                .build();

        NiobiumNitride = new MaterialBuilder(169, "niobium_nitride")
                .ingot().fluid()
                .color(0x1D291D)
                .flags(EXT_METAL)
                .components(Niobium, 1, Nitrogen, 1)
                .blastTemp(2573)
                .build();

        NiobiumTitanium = new MaterialBuilder(170, "niobium_titanium")
                .ingot().fluid()
                .color(0x1D1D29)
                .flags(EXT2_METAL)
                .components(Niobium, 1, Titanium, 1)
                .blastTemp(4500)
                .build();

        Obsidian = new MaterialBuilder(171, "obsidian")
                .dust(3)
                .color(0x503264)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_RECIPES)
                .components(Magnesium, 1, Iron, 1, Silicon, 2, Oxygen, 4)
                .build();

        Phosphate = new MaterialBuilder(172, "phosphate")
                .dust(1).ore()
                .color(0xFFFF00)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE, EXPLOSIVE)
                .components(Phosphorus, Oxygen, 4)
                .build();

        PigIron = new MaterialBuilder(173, "pig_iron")
                .ingot().fluid()
                .color(0xC8B4B4).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_RING)
                .components(Iron, 1)
                .toolStats(6.0f, 4.0f, 384)
                .build();

        SterlingSilver = new MaterialBuilder(174, "sterling_silver")
                .ingot().fluid()
                .color(0xFADCE1).iconSet(SHINY)
                .flags(EXT2_METAL)
                .components(Copper, 1, Silver, 4)
                .toolStats(13.0f, 2.0f, 196)
                .blastTemp(1700)
                .build();

        RoseGold = new MaterialBuilder(175, "rose_gold")
                .ingot().fluid()
                .color(0xFFE61E).iconSet(SHINY)
                .flags(EXT2_METAL)
                .components(Copper, 1, Gold, 4)
                .toolStats(14.0f, 2.0f, 152)
                .blastTemp(1600)
                .build();

        BlackBronze = new MaterialBuilder(176, "black_bronze")
                .ingot().fluid()
                .color(0x64327D)
                .flags(EXT2_METAL)
                .components(Gold, 1, Silver, 1, Copper, 3)
                .toolStats(12.0f, 2.0f, 256)
                .blastTemp(2000)
                .build();

        BismuthBronze = new MaterialBuilder(177, "bismuth_bronze")
                .ingot().fluid()
                .color(0x647D7D)
                .flags(EXT2_METAL)
                .components(Bismuth, 1, Zinc, 1, Copper, 3)
                .toolStats(8.0f, 3.0f, 256)
                .blastTemp(1100)
                .build();

        Biotite = new MaterialBuilder(178, "biotite")
                .dust(1)
                .color(0x141E14).iconSet(METALLIC)
                .components(Potassium, 1, Magnesium, 3, Aluminium, 3, Fluorine, 2, Silicon, 3, Oxygen, 10)
                .build();

        Powellite = new MaterialBuilder(179, "powellite")
                .dust().ore()
                .color(0xFFFF00)
                .components(Calcium, 1, Molybdenum, 1, Oxygen, 4)
                .build();

        Pyrite = new MaterialBuilder(180, "pyrite")
                .dust(1).ore()
                .color(0x967828).iconSet(ROUGH)
                .components(Iron, 1, Sulfur, 2)
                .build();

        Pyrolusite = new MaterialBuilder(181, "pyrolusite")
                .dust().ore()
                .color(0x9696AA)
                .components(Manganese, 1, Oxygen, 2)
                .build();

        Pyrope = new MaterialBuilder(182, "pyrope")
                .dust().ore()
                .color(0x783264).iconSet(METALLIC)
                .components(Aluminium, 2, Magnesium, 3, Silicon, 3, Oxygen, 12)
                .build();

        RockSalt = new MaterialBuilder(183, "rock_salt")
                .dust(1).ore()
                .color(0xF0C8C8).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Potassium, 1, Chlorine, 1)
                .build();

        Rubber = new MaterialBuilder(184, "rubber")
                .ingot(0).fluid()
                .color(0x000000).iconSet(SHINY)
                .flags(GENERATE_GEAR, GENERATE_RING, FLAMMABLE, NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Carbon, 5, Hydrogen, 8)
                .build();

        Ruby = new MaterialBuilder(185, "ruby")
                .gem().ore()
                .color(0xFF6464).iconSet(RUBY)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Chrome, 1, Aluminium, 2, Oxygen, 3)
                .toolStats(8.5f, 3.0f, 256)
                .build();

        Salt = new MaterialBuilder(186, "salt")
                .dust(1).ore()
                .color(0xFAFAFA).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Sodium, 1, Chlorine, 1)
                .build();

        Saltpeter = new MaterialBuilder(187, "saltpeter")
                .dust(1).ore()
                .color(0xE6E6E6).iconSet(FINE)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE)
                .components(Potassium, 1, Nitrogen, 1, Oxygen, 3)
                .build();

        Sapphire = new MaterialBuilder(188, "sapphire")
                .gem().ore()
                .color(0x6464C8).iconSet(GEM_VERTICAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Oxygen, 3)
                .toolStats(7.5f, 4.0f, 256)
                .build();

        Scheelite = new MaterialBuilder(189, "scheelite")
                .dust(3).ore()
                .color(0xC88C14)
                .flags(DECOMPOSITION_REQUIRES_HYDROGEN)
                .components(Tungsten, 1, Calcium, 2, Oxygen, 4)
                .build();

        Sodalite = new MaterialBuilder(190, "sodalite")
                .gem(1).ore()
                .color(0x1414FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, GENERATE_ROD, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 3, Silicon, 3, Sodium, 4, Chlorine, 1)
                .build();

        DiamericiumTitanium = new MaterialBuilder(191, "diamericium_titanium")
                .ingot(4).fluid()
                .color(0x755280).iconSet(METALLIC)
                .components(Americium, 2, Titanium, 1)
                .toolStats(6.0f, 6.0f, 2200)
                .blastTemp(10400)
                .build();

        Tantalite = new MaterialBuilder(192, "tantalite")
                .dust(3).ore()
                .color(0x915028).iconSet(METALLIC)
                .components(Manganese, 1, Tantalum, 2, Oxygen, 6)
                .build();

        Coke = new MaterialBuilder(193, "coke")
                .gem()
                .color(0x666666).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE)
                .components(Carbon, 1)
                .build();

        SolderingAlloy = new MaterialBuilder(194, "soldering_alloy")
                .ingot(1).fluid()
                .color(0x9696A0)
                .flags(EXT_METAL, GENERATE_FINE_WIRE)
                .components(Tin, 9, Antimony, 1)
                .build();

        Spessartine = new MaterialBuilder(195, "spessarite")
                .dust().ore()
                .color(0xFF6464)
                .components(Aluminium, 2, Manganese, 3, Silicon, 3, Oxygen, 12)
                .build();

        Sphalerite = new MaterialBuilder(196, "sphalerite")
                .dust(1).ore()
                .color(0xFFFFFF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zinc, 1, Sulfur)
                .build();

        StainlessSteel = new MaterialBuilder(197, "stainless_steel")
                .ingot().fluid()
                .color(0xC8C8DC).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_FRAME, GENERATE_LONG_ROD)
                .components(Iron, 7, Chrome, 1, Manganese, 1, Nickel, 1)
                .toolStats(7.0f, 4.0f, 480)
                .blastTemp(1700)
                .build();

        Steel = new MaterialBuilder(198, "steel")
                .ingot().fluid()
                .color(0x808080).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_DENSE, GENERATE_FRAME, GENERATE_LONG_ROD, DISABLE_DECOMPOSITION)
                .components(Iron, 1)
                .toolStats(6.0f, 3.0f, 512)
                .blastTemp(1000)
                .build();

        Stibnite = new MaterialBuilder(199, "stibnite")
                .dust().ore()
                .color(0x464646).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Antimony, 2, Sulfur, 3)
                .build();

        Tanzanite = new MaterialBuilder(200, "tanzanite")
                .gem().ore()
                .color(0x4000C8).iconSet(GEM_VERTICAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Calcium, 2, Aluminium, 3, Silicon, 3, Hydrogen, 1)
                .toolStats(7.0f, 2.0f, 256)
                .build();

        Tetrahedrite = new MaterialBuilder(201, "tetrahedrite")
                .dust().ore()
                .color(0xC82000)
                .components(Copper, 3, Antimony, 1, Sulfur, 3, Iron, 1)
                .build();

        TinAlloy = new MaterialBuilder(202, "tin_alloy")
                .ingot().fluid()
                .color(0xC8C8C8).iconSet(METALLIC)
                .components(Tin, 1, Iron, 1)
                .flags(EXT2_METAL)
                .build();

        Topaz = new MaterialBuilder(203, "topaz")
                .gem(3).ore()
                .color(0xFF8000).iconSet(GEM_HORIZONTAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Silicon, 1, Fluorine, 1, Hydrogen, 2)
                .toolStats(7.0f, 2.0f, 256)
                .build();

        Tungstate = new MaterialBuilder(204, "tungstate")
                .dust(3).ore()
                .color(0x373223)
                .flags(DECOMPOSITION_REQUIRES_HYDROGEN)
                .components(Tungsten, 1, Lithium, 2, Oxygen, 4)
                .build();

        Ultimet = new MaterialBuilder(205, "ultimet")
                .ingot(4).fluid()
                .color(0xB4B4E6).iconSet(SHINY)
                .flags(EXT2_METAL)
                .components(Cobalt, 5, Chrome, 2, Nickel, 1, Molybdenum, 1)
                .toolStats(9.0f, 4.0f, 2048)
                .blastTemp(2700)
                .build();

        Uraninite = new MaterialBuilder(206, "uraninite")
                .dust(3).ore()
                .color(0x232323).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium238, 1, Oxygen, 2)
                .build()
                .setFormula("UO2", true);

        Uvarovite = new MaterialBuilder(207, "uvarovite")
                .dust()
                .color(0xB4FFB4).iconSet(DIAMOND)
                .components(Calcium, 3, Chrome, 2, Silicon, 3, Oxygen, 12)
                .build();

        VanadiumGallium = new MaterialBuilder(208, "vanadium_gallium")
                .ingot().fluid()
                .color(0x80808C).iconSet(SHINY)
                .flags(STD_METAL, GENERATE_FOIL, GENERATE_ROD)
                .components(Vanadium, 3, Gallium, 1)
                .blastTemp(4500)
                .build();

        WroughtIron = new MaterialBuilder(209, "wrought_iron")
                .ingot().fluid()
                .color(0xC8B4B4).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_RING, GENERATE_LONG_ROD, DISABLE_DECOMPOSITION)
                .components(Iron, 1)
                .toolStats(6.0f, 3.5f, 384)
                .build();

        Wulfenite = new MaterialBuilder(210, "wulfenite")
                .dust(3).ore()
                .color(0xFF8000)
                .components(Lead, 1, Molybdenum, 1, Oxygen, 4)
                .build();

        YellowLimonite = new MaterialBuilder(211, "yellow_limonite")
                .dust().ore()
                .color(0xC8C800).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .build();

        YttriumBariumCuprate = new MaterialBuilder(212, "yttrium_barium_cuprate")
                .ingot().fluid()
                .color(0x504046).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FINE_WIRE)
                .components(Yttrium, 1, Barium, 2, Copper, 3, Oxygen, 7)
                .build();

        NetherQuartz = new MaterialBuilder(213, "nether_quartz")
                .gem(1).ore()
                .color(0xE6D2D2).iconSet(QUARTZ)
                .flags(STD_SOLID, NO_SMELTING, CRYSTALLIZABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        CertusQuartz = new MaterialBuilder(214, "certus_quartz")
                .gem(1).ore()
                .color(0xD2D2E6).iconSet(QUARTZ)
                .flags(STD_SOLID, NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        Quartzite = new MaterialBuilder(215, "quartzite")
                .gem(1).ore()
                .color(0xD2E6D2).iconSet(QUARTZ)
                .flags(NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        Graphite = new MaterialBuilder(216, "graphite")
                .ingot().ore().fluid()
                .color(0x808080)
                .flags(STD_METAL, NO_SMELTING, FLAMMABLE, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Graphene = new MaterialBuilder(217, "graphene")
                .ingot().fluid()
                .color(0x808080).iconSet(SHINY)
                .flags(GENERATE_FOIL, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Jasper = new MaterialBuilder(218, "jasper")
                .gem().ore()
                .color(0xC85050).iconSet(EMERALD)
                .flags(NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .build();

        Osmiridium = new MaterialBuilder(219, "osmiridium")
                .ingot(3).fluid()
                .color(0x6464FF).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Iridium, 3, Osmium, 1)
                .toolStats(9.0f, 3.0f, 3152)
                .blastTemp(2500)
                .build();

        Tenorite = new MaterialBuilder(220, "tenorite")
                .dust(1).ore()
                .color(0x606060)
                .components(Copper, 1, Oxygen, 1)
                .build();

        Cuprite = new MaterialBuilder(221, "cuprite")
                .dust().ore()
                .color(0x770000).iconSet(RUBY)
                .components(Copper, 2, Oxygen, 1)
                .build();

        Bornite = new MaterialBuilder(222, "bornite")
                .dust(1).ore()
                .color(0x97662B).iconSet(METALLIC)
                .components(Copper, 5, Iron, 1, Sulfur, 4)
                .build();

        Chalcocite = new MaterialBuilder(223, "chalcocite")
                .dust().ore()
                .color(0x353535).iconSet(GEM_VERTICAL)
                .components(Copper, 2, Sulfur, 1)
                .build();

        Enargite = new MaterialBuilder(224, "enargite")
                .dust().ore()
                .color(0xBBBBBB).iconSet(METALLIC)
                .components(Copper, 3, Arsenic, 1, Sulfur, 4)
                .build();

        Tennantite = new MaterialBuilder(225, "tennantite")
                .dust().ore()
                .color(0x909090).iconSet(METALLIC)
                .components(Copper, 12, Arsenic, 4, Sulfur, 13)
                .build();

        GalliumArsenide = new MaterialBuilder(226, "gallium_arsenide")
                .ingot(1).fluid()
                .color(0xA0A0A0)
                .flags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Gallium, 1, Arsenic, 1)
                .blastTemp(1200)
                .build();

        Potash = new MaterialBuilder(227, "potash")
                .dust(1)
                .color(0x784137)
                .components(Potassium, 2, Oxygen, 1)
                .build();

        SodaAsh = new MaterialBuilder(288, "soda_ash")
                .dust(1)
                .color(0xDCDCFF)
                .components(Sodium, 2, Carbon, 1, Oxygen, 3)
                .build();

        IndiumGalliumPhosphide = new MaterialBuilder(229, "indium_gallium_phosphide")
                .ingot(1).fluid()
                .color(0xA08CBE)
                .flags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Indium, 1, Gallium, 1, Phosphorus, 1)
                .build();

        NickelZincFerrite = new MaterialBuilder(230, "nickel_zinc_ferrite")
                .ingot(0).fluid()
                .color(0x3C3C3C).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_RING)
                .components(Nickel, 1, Zinc, 1, Iron, 4)
                .build();

        SiliconDioxide = new MaterialBuilder(232, "silicon_dioxide")
                .dust(1)
                .color(0xC8C8C8).iconSet(QUARTZ)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        MagnesiumChloride = new MaterialBuilder(232, "magnesium_chloride")
                .dust(1)
                .color(0xD40D5C)
                .components(Magnesium, 1, Chlorine, 2)
                .build();

        SodiumSulfide = new MaterialBuilder(233, "sodium_sulfide")
                .dust(1)
                .color(0xFFE680)
                .components(Sodium, 2, Sulfur, 1)
                .build();

        PhosphorusPentoxide = new MaterialBuilder(234, "phosphorus_pentoxide")
                .dust(1)
                .color(0xDCDC00)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Phosphorus, 4, Oxygen, 10)
                .build();

        Quicklime = new MaterialBuilder(235, "quicklime")
                .dust(1)
                .color(0xF0F0F0)
                .components(Calcium, 1, Oxygen, 1)
                .build();

        SodiumBisulfate = new MaterialBuilder(236, "sodium_bisulfate")
                .dust(1)
                .color(0x004455)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Hydrogen, 1, Sulfur, 1, Oxygen, 4)
                .build();

        FerriteMixture = new MaterialBuilder(237, "ferrite_mixture")
                .dust(1)
                .color(0xB4B4B4).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Nickel, 1, Zinc, 1, Iron, 4)
                .build();

        Magnesia = new MaterialBuilder(238, "magnesia")
                .dust(1)
                .color(0x887878)
                .components(Magnesium, 1, Oxygen, 1)
                .build();

        PlatinumGroupSludge = new MaterialBuilder(239, "platinum_group_sludge")
                .dust(1)
                .color(0x001E00).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .build();

        RealGar = new MaterialBuilder(240, "realgar")
                .dust(2)
                .color(0x8C6464)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Arsenic, 4, Sulfur, 4)
                .build();

        SodiumBicarbonate = new MaterialBuilder(241, "sodium_bicarbonate")
                .dust(1)
                .color(0x565b96).iconSet(ROUGH)
                .components(Sodium, 1, Hydrogen, 1, Carbon, 1, Oxygen, 5)
                .build();

        PotassiumDichromate = new MaterialBuilder(242, "potassium_dichromate")
                .dust(1)
                .color(0xFF084E)
                .components(Potassium, 2, Chrome, 2, Oxygen, 7)
                .build();

        ChromiumTrioxide = new MaterialBuilder(243, "chromium_trioxide")
                .dust(1)
                .color(0xFFE4E1)
                .components(Chrome, 1, Oxygen, 3)
                .build();

        AntimonyTrioxide = new MaterialBuilder(244, "antimony_trioxide")
                .dust(1)
                .color(0xE6E6F0)
                .components(Antimony, 2, Oxygen, 3)
                .build();

        Zincite = new MaterialBuilder(245, "zincite")
                .dust(1)
                .color(0xFFFFF5)
                .components(Zinc, 1, Oxygen, 1)
                .build();

        CupricOxide = new MaterialBuilder(246, "cupric_oxide")
                .dust(1)
                .color(0x0F0F0F)
                .components(Copper, 1, Oxygen, 1)
                .build();

        CobaltOxide = new MaterialBuilder(247, "cobalt_oxide")
                .dust(1)
                .color(0x788000)
                .components(Cobalt, 1, Oxygen, 1)
                .build();

        ArsenicTrioxide = new MaterialBuilder(248, "arsenic_tioxide")
                .dust(1)
                .iconSet(ROUGH)
                .components(Arsenic, 2, Oxygen, 3)
                .build();

        Massicot = new MaterialBuilder(249, "massicot")
                .dust(1)
                .color(0xFFDD55)
                .components(Lead, 1, Oxygen, 1)
                .build();

        Ferrosilite = new MaterialBuilder(250, "ferrosilite")
                .dust(1)
                .color(0x97632A)
                .components(Iron, 1, Silicon, 1, Oxygen, 3)
                .build();

        MetalMixture = new MaterialBuilder(251, "metal_mixture")
                .dust(1)
                .color(0x502d16).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .build();

        SodiumHydroxide = new MaterialBuilder(252, "sodium_hydroxide")
                .dust(1)
                .color(0x003380)
                .components(Sodium, 1, Oxygen, 1, Hydrogen, 1)
                .build();

        SodiumPersulfate = new MaterialBuilder(253, "sodium_persulfate")
                .fluid()
                .components(Sodium, 2, Sulfur, 2, Oxygen, 8)
                .build();

        Bastnasite = new MaterialBuilder(254, "bastnasite")
                .dust().ore()
                .color(0xC86E2D).iconSet(FINE)
                .components(Cerium, 1, Carbon, 1, Fluorine, 1, Oxygen, 3)
                .build();

        Pentlandite = new MaterialBuilder(255, "pentlandite")
                .dust().ore()
                .color(0xA59605)
                .components(Nickel, 9, Sulfur, 8)
                .build();

        Spodumene = new MaterialBuilder(256, "spodumene")
                .dust().ore()
                .color(0xBEAAAA)
                .components(Lithium, 1, Aluminium, 1, Silicon, 2, Oxygen, 6)
                .build();

        Lepidolite = new MaterialBuilder(257, "lepidolite")
                .dust().ore()
                .color(0xF0328C).iconSet(FINE)
                .components(Potassium, 1, Lithium, 3, Aluminium, 4, Fluorine, 2, Oxygen, 10)
                .build();

        Glauconite = new MaterialBuilder(258, "glauconite")
                .dust().ore()
                .color(0x82B43C)
                .components(Potassium, 1, Magnesium, 2, Aluminium, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        GlauconiteSand = new MaterialBuilder(259, "glauconite_sand")
                .dust()
                .color(0x82B43C).iconSet(SAND)
                .components(Potassium, 1, Magnesium, 2, Aluminium, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        Malachite = new MaterialBuilder(260, "malachite")
                .dust().ore()
                .color(0x055F05)
                .components(Copper, 2, Carbon, 1, Hydrogen, 2, Oxygen, 5)
                .build();

        Mica = new MaterialBuilder(261, "mica")
                .dust()
                .color(0xC3C3CD).iconSet(FINE)
                .components(Potassium, 1, Aluminium, 3, Silicon, 3, Fluorine, 2, Oxygen, 10)
                .build();

        Barite = new MaterialBuilder(262, "barite")
                .dust().ore()
                .color(0xE6EBEB)
                .components(Barium, 1, Sulfur, 1, Oxygen, 4)
                .build();

        Alunite = new MaterialBuilder(263, "alunite")
                .dust()
                .color(0xE1B441).iconSet(METALLIC)
                .components(Potassium, 1, Aluminium, 3, Silicon, 2, Hydrogen, 6, Oxygen, 14)
                .build();

        Dolomite = new MaterialBuilder(264, "dolomite")
                .dust(1)
                .color(0xE1CDCD).iconSet(FLINT)
                .components(Calcium, 1, Magnesium, 1, Carbon, 2, Oxygen, 6)
                .build();

        Wollastonite = new MaterialBuilder(265, "wollastonite")
                .dust()
                .color(0xF0F0F0)
                .components(Calcium, 1, Silicon, 1, Oxygen, 3)
                .build();

        Kaolinite = new MaterialBuilder(267, "kaolinite")
                .dust()
                .color(0xF3EBEB)
                .components(Aluminium, 2, Silicon, 2, Hydrogen, 4, Oxygen, 9)
                .build();

        Talc = new MaterialBuilder(268, "talc")
                .dust().ore()
                .color(0x5AB45A).iconSet(FINE)
                .components(Magnesium, 3, Silicon, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        Soapstone = new MaterialBuilder(269, "soapstone")
                .dust(1).ore()
                .color(0x5F915F)
                .components(Magnesium, 3, Silicon, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        Kyanite = new MaterialBuilder(270, "kyanite")
                .dust()
                .color(0x6E6EFA).iconSet(FLINT)
                .components(Aluminium, 2, Silicon, 1, Oxygen, 5)
                .build();

        IronMagnetic = new MaterialBuilder(271, "iron_magnetic")
                .ingot().fluid()
                .color(0xC8C8C8).iconSet(MAGNETIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .components(Iron, 1)
                .build();

        TungstenCarbide = new MaterialBuilder(272, "tungsten_carbide")
                .ingot().fluid()
                .color(0x330066).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Tungsten, 1, Carbon, 1)
                .toolStats(12.0f, 4.0f, 1280)
                .blastTemp(2460)
                .build();

    }
}
