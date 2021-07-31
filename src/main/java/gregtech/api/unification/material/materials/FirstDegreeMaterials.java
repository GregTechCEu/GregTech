package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.MaterialBuilder;
import gregtech.api.unification.stack.MaterialStack;

import static com.google.common.collect.ImmutableList.of;
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












        Emerald = new MaterialBuilder(152, "emerald", 0x50FF50, EMERALD, 2, of(new MaterialStack(Beryllium, 3), new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 6), new MaterialStack(Oxygen, 18)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, 10.0F, 2.0f, 368);
        Galena = new MaterialBuilder(153, "galena", 0x643C64, DULL, 3, of(new MaterialStack(Lead, 3), new MaterialStack(Silver, 3), new MaterialStack(Sulfur, 2)), GENERATE_ORE | NO_SMELTING);
        Garnierite = new MaterialBuilder(154, "garnierite", 0x32C846, METALLIC, 3, of(new MaterialStack(Nickel, 1), new MaterialStack(Oxygen, 1)), GENERATE_ORE);
        GreenSapphire = new MaterialBuilder(155, "green_sapphire", 0x64C882, GEM_HORIZONTAL, 2, of(new MaterialStack(Aluminium, 2), new MaterialStack(Oxygen, 3)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT | GENERATE_LENS | GENERATE_PLATE, 8.0F, 3.0f, 368);
        Grossular = new MaterialBuilder(156, "grossular", 0xC86400, ROUGH, 1, of(new MaterialStack(Calcium, 3), new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 12)), GENERATE_ORE);
        Ice = new MaterialBuilder(157, "ice", 0xC8C8FF, SHINY, 0, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 1)), NO_SMASHING | NO_RECYCLING | SMELT_INTO_FLUID | EXCLUDE_BLOCK_CRAFTING_RECIPES | DISABLE_DECOMPOSITION);
        Ilmenite = new MaterialBuilder(158, "ilmenite", 0x463732, METALLIC, 3, of(new MaterialStack(Iron, 1), new MaterialStack(Titanium, 1), new MaterialStack(Oxygen, 3)), GENERATE_ORE | DISABLE_DECOMPOSITION);
        Rutile = new MaterialBuilder(159, "rutile", 0xD40D5C, GEM_HORIZONTAL, 2, of(new MaterialStack(Titanium, 1), new MaterialStack(Oxygen, 2)), STD_GEM | DISABLE_DECOMPOSITION);
        Bauxite = new MaterialBuilder(160, "bauxite", 0xC86400, DULL, 1, of(new MaterialStack(Rutile, 2), new MaterialStack(Aluminium, 16), new MaterialStack(Hydrogen, 10), new MaterialStack(Oxygen, 11)), GENERATE_ORE | DISABLE_DECOMPOSITION);
        Invar = new MaterialBuilder(161, "invar", 0xB4B478, METALLIC, 2, of(new MaterialStack(Iron, 2), new MaterialStack(Nickel, 1)), EXT2_METAL | MORTAR_GRINDABLE | GENERATE_RING | GENERATE_FRAME, 7.0F, 3.0f, 512);
        Kanthal = new MaterialBuilder(162, "kanthal", 0xC2D2DF, METALLIC, 2, of(new MaterialStack(Iron, 1), new MaterialStack(Aluminium, 1), new MaterialStack(Chrome, 1)), EXT_METAL| GENERATE_SPRING, null, 1800);
        Lazurite = new MaterialBuilder(163, "lazurite", 0x6478FF, LAPIS, 1, of(new MaterialStack(Aluminium, 6), new MaterialStack(Silicon, 6), new MaterialStack(Calcium, 8), new MaterialStack(Sodium, 8)), GENERATE_PLATE | GENERATE_ORE | NO_SMASHING | NO_SMELTING | CRYSTALLIZABLE | GENERATE_ROD | DECOMPOSITION_BY_ELECTROLYZING);
        Magnalium = new MaterialBuilder(164, "magnalium", 0xC8BEFF, DULL, 2, of(new MaterialStack(Magnesium, 1), new MaterialStack(Aluminium, 2)), EXT2_METAL | GENERATE_LONG_ROD, 6.0F, 2.0f, 256);
        Magnesite = new MaterialBuilder(165, "magnesite", 0xFAFAB4, METALLIC, 2, of(new MaterialStack(Magnesium, 1), new MaterialStack(Carbon, 1), new MaterialStack(Oxygen, 3)), GENERATE_ORE);
        Magnetite = new MaterialBuilder(166, "magnetite", 0x1E1E1E, METALLIC, 2, of(new MaterialStack(Iron, 3), new MaterialStack(Oxygen, 4)), GENERATE_ORE);
        Molybdenite = new MaterialBuilder(167, "molybdenite", 0x191919, METALLIC, 2, of(new MaterialStack(Molybdenum, 1), new MaterialStack(Sulfur, 2)), GENERATE_ORE);
        Nichrome = new MaterialBuilder(168, "nichrome", 0xCDCEF6, METALLIC, 2, of(new MaterialStack(Nickel, 4), new MaterialStack(Chrome, 1)), EXT_METAL| GENERATE_SPRING, null, 2700);
        NiobiumNitride = new MaterialBuilder(169, "niobium_nitride", 0x1D291D, DULL, 2, of(new MaterialStack(Niobium, 1), new MaterialStack(Nitrogen, 1)), EXT_METAL, null, 2573);
        NiobiumTitanium = new MaterialBuilder(170, "niobium_titanium", 0x1D1D29, DULL, 2, of(new MaterialStack(Niobium, 1), new MaterialStack(Titanium, 1)), EXT2_METAL, null, 4500);
        Obsidian = new MaterialBuilder(171, "obsidian", 0x503264, DULL, 3, of(new MaterialStack(Magnesium, 1), new MaterialStack(Iron, 1), new MaterialStack(Silicon, 2), new MaterialStack(Oxygen, 8)), NO_SMASHING | EXCLUDE_BLOCK_CRAFTING_RECIPES);
        Phosphate = new MaterialBuilder(172, "phosphate", 0xFFFF00, DULL, 1, of(new MaterialStack(Phosphorus, 1), new MaterialStack(Oxygen, 4)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | FLAMMABLE | EXPLOSIVE);
        PigIron = new MaterialBuilder(173, "pig_iron", 0xC8B4B4, METALLIC, 2, of(new MaterialStack(Iron, 1)), EXT_METAL | GENERATE_RING, 6.0F, 4.0f, 384);
        Polyethylene = new MaterialBuilder(174, "plastic", 0xC8C8C8, DULL, 1, of(new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 2)), GENERATE_PLATE | GENERATE_FOIL | FLAMMABLE | NO_SMASHING | SMELT_INTO_FLUID | DISABLE_DECOMPOSITION); //todo add polyethylene oredicts
        Epoxy = new MaterialBuilder(175, "epoxy", 0xC88C14, DULL, 1, of(new MaterialStack(Carbon, 21), new MaterialStack(Hydrogen, 25), new MaterialStack(Chlorine, 1), new MaterialStack(Oxygen, 5)), EXT2_METAL | DISABLE_DECOMPOSITION | NO_SMASHING);
        Polysiloxane = new MaterialBuilder(176, "polysiloxane", 0xDCDCDC, DULL, 1, of(new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(Silicon, 2), new MaterialStack(Oxygen, 1)), GENERATE_PLATE | FLAMMABLE | NO_SMASHING | SMELT_INTO_FLUID | DISABLE_DECOMPOSITION);
        Polycaprolactam = new MaterialBuilder(177, "polycaprolactam", 0x323232, DULL, 1, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 11), new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 1)), GENERATE_PLATE | DISABLE_DECOMPOSITION | NO_SMASHING);
        Polytetrafluoroethylene = new MaterialBuilder(178, "polytetrafluoroethylene", 0x646464, DULL, 1, of(new MaterialStack(Carbon, 2), new MaterialStack(Fluorine, 4)), GENERATE_PLATE | GENERATE_FRAME | SMELT_INTO_FLUID  | DISABLE_DECOMPOSITION | NO_SMASHING);
        Powellite = new MaterialBuilder(179, "powellite", 0xFFFF00, DULL, 2, of(new MaterialStack(Calcium, 1), new MaterialStack(Molybdenum, 1), new MaterialStack(Oxygen, 4)), GENERATE_ORE);
        Pyrite = new MaterialBuilder(180, "pyrite", 0x967828, ROUGH, 1, of(new MaterialStack(Iron, 1), new MaterialStack(Sulfur, 2)), GENERATE_ORE);
        Pyrolusite = new MaterialBuilder(181, "pyrolusite", 0x9696AA, DULL, 2, of(new MaterialStack(Manganese, 1), new MaterialStack(Oxygen, 2)), GENERATE_ORE);
        Pyrope = new MaterialBuilder(182, "pyrope", 0x783264, METALLIC, 2, of(new MaterialStack(Aluminium, 2), new MaterialStack(Magnesium, 3), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 12)), GENERATE_ORE);
        RockSalt = new MaterialBuilder(183, "rock_salt", 0xF0C8C8, FINE, 1, of(new MaterialStack(Potassium, 1), new MaterialStack(Chlorine, 1)), GENERATE_ORE | NO_SMASHING);
        Rubber = new MaterialBuilder(184, "rubber", 0x000000, SHINY, 0, of(new MaterialStack(Carbon, 5), new MaterialStack(Hydrogen, 8)), GENERATE_PLATE | GENERATE_GEAR | GENERATE_RING | FLAMMABLE | NO_SMASHING | GENERATE_RING | DISABLE_DECOMPOSITION);
        Ruby = new MaterialBuilder(185, "ruby", 0xFF6464, RUBY, 2, of(new MaterialStack(Chrome, 1), new MaterialStack(Aluminium, 2), new MaterialStack(Oxygen, 3)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, 8.5F, 3.0f, 256);
        Salt = new MaterialBuilder(186, "salt", 0xFAFAFA, FINE, 1, of(new MaterialStack(Sodium, 1), new MaterialStack(Chlorine, 1)), GENERATE_ORE | NO_SMASHING);
        Saltpeter = new MaterialBuilder(187, "saltpeter", 0xE6E6E6, FINE, 1, of(new MaterialStack(Potassium, 1), new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 3)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | FLAMMABLE);
        Sapphire = new MaterialBuilder(188, "sapphire", 0x6464C8, GEM_VERTICAL, 2, of(new MaterialStack(Aluminium, 2), new MaterialStack(Oxygen, 3)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, null, 7.5F, 4.0f, 256);
        Scheelite = new MaterialBuilder(189, "scheelite", 0xC88C14, DULL, 3, of(new MaterialStack(Tungsten, 1), new MaterialStack(Calcium, 2), new MaterialStack(Oxygen, 4)), GENERATE_ORE | DECOMPOSITION_REQUIRES_HYDROGEN);
        Sodalite = new MaterialBuilder(190, "sodalite", 0x1414FF, LAPIS, 1, of(new MaterialStack(Aluminium, 3), new MaterialStack(Silicon, 3), new MaterialStack(Sodium, 4), new MaterialStack(Chlorine, 1)), GENERATE_ORE | GENERATE_PLATE | GENERATE_ROD | NO_SMASHING | NO_SMELTING | CRYSTALLIZABLE | GENERATE_ROD | DECOMPOSITION_BY_ELECTROLYZING);
        Brick = new MaterialBuilder(191, "brick", 0x9B5643, ROUGH, 1, of(new MaterialStack(Clay, 1)), EXCLUDE_BLOCK_CRAFTING_RECIPES | DECOMPOSITION_BY_CENTRIFUGING);
        Fireclay = new MaterialBuilder(192, "fireclay", 0xADA09B, ROUGH, 2, of(new MaterialStack(Clay, 1), new MaterialStack(Brick, 1)), DECOMPOSITION_BY_CENTRIFUGING);
        Coke = new MaterialBuilder(193, "coke", 0x666666, LIGNITE, 1, of(new MaterialStack(Carbon, 1)), FLAMMABLE | NO_SMELTING | NO_SMASHING | MORTAR_GRINDABLE);


        SolderingAlloy = new MaterialBuilder(194, "soldering_alloy", 0x9696A0, DULL, 1, of(new MaterialStack(Tin, 9), new MaterialStack(Antimony, 1)), EXT_METAL | GENERATE_FINE_WIRE, null);
        Spessartine = new MaterialBuilder(195, "spessartine", 0xFF6464, DULL, 2, of(new MaterialStack(Aluminium, 2), new MaterialStack(Manganese, 3), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 12)), GENERATE_ORE);
        Sphalerite = new MaterialBuilder(196, "sphalerite", 0xFFFFFF, DULL, 1, of(new MaterialStack(Zinc, 1), new MaterialStack(Sulfur, 1)), GENERATE_ORE | DISABLE_DECOMPOSITION);
        StainlessSteel = new MaterialBuilder(197, "stainless_steel", 0xC8C8DC, SHINY, 2, of(new MaterialStack(Iron, 6), new MaterialStack(Chrome, 1), new MaterialStack(Manganese, 1), new MaterialStack(Nickel, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_FRAME | GENERATE_LONG_ROD, null, 7.0F, 4.0f, 480, 1700);
        Steel = new MaterialBuilder(198, "steel", 0x808080, METALLIC, 2, of(new MaterialStack(Iron, 1)), EXT2_METAL | MORTAR_GRINDABLE | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_DENSE | DISABLE_DECOMPOSITION | GENERATE_FRAME | GENERATE_LONG_ROD, null, 6.0F, 3.0f, 512, 1000);
        Stibnite = new MaterialBuilder(199, "stibnite", 0x464646, METALLIC, 2, of(new MaterialStack(Antimony, 2), new MaterialStack(Sulfur, 3)), GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING);
        Tanzanite = new MaterialBuilder(200, "tanzanite", 0x4000C8, GEM_VERTICAL, 2, of(new MaterialStack(Calcium, 2), new MaterialStack(Aluminium, 3), new MaterialStack(Silicon, 3), new MaterialStack(Hydrogen, 1), new MaterialStack(Oxygen, 13)), EXT_METAL | GENERATE_ORE | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, null, 7.0F, 2.0f, 256);
        Tetrahedrite = new MaterialBuilder(201, "tetrahedrite", 0xC82000, DULL, 2, of(new MaterialStack(Copper, 3), new MaterialStack(Antimony, 1), new MaterialStack(Sulfur, 3), new MaterialStack(Iron, 1)), GENERATE_ORE);
        TinAlloy = new MaterialBuilder(202, "tin_alloy", 0xC8C8C8, METALLIC, 2, of(new MaterialStack(Tin, 1), new MaterialStack(Iron, 1)), EXT2_METAL, null);
        Topaz = new MaterialBuilder(203, "topaz", 0xFF8000, GEM_HORIZONTAL, 3, of(new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 1), new MaterialStack(Fluorine, 2), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 6)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, null, 7.0F, 2.0f, 256);
        Tungstate = new MaterialBuilder(204, "tungstate", 0x373223, DULL, 3, of(new MaterialStack(Tungsten, 1), new MaterialStack(Lithium, 2), new MaterialStack(Oxygen, 4)), GENERATE_ORE | DECOMPOSITION_REQUIRES_HYDROGEN, null);
        Ultimet = new MaterialBuilder(205, "ultimet", 0xB4B4E6, SHINY, 4, of(new MaterialStack(Cobalt, 5), new MaterialStack(Chrome, 2), new MaterialStack(Nickel, 1), new MaterialStack(Molybdenum, 1)), EXT2_METAL, null, 9.0F, 4.0f, 2048, 2700);
        Uraninite = new MaterialBuilder(206, "uraninite", 0x232323, METALLIC, 3, of(new MaterialStack(Uranium238, 1), new MaterialStack(Oxygen, 2)), GENERATE_ORE | DISABLE_DECOMPOSITION).setFormula("UO2", true);
        Uvarovite = new MaterialBuilder(207, "uvarovite", 0xB4FFB4, DIAMOND, 2, of(new MaterialStack(Calcium, 3), new MaterialStack(Chrome, 2), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 12)), 0);
        VanadiumGallium = new MaterialBuilder(208, "vanadium_gallium", 0x80808C, SHINY, 2, of(new MaterialStack(Vanadium, 3), new MaterialStack(Gallium, 1)), STD_METAL | GENERATE_FOIL | GENERATE_ROD, null, 4500);
        WroughtIron = new MaterialBuilder(209, "wrought_iron", 0xC8B4B4, METALLIC, 2, of(new MaterialStack(Iron, 1)), EXT2_METAL | MORTAR_GRINDABLE | GENERATE_RING | GENERATE_LONG_ROD | DISABLE_DECOMPOSITION, null, 6.0F, 3.5f, 384);
        Wulfenite = new MaterialBuilder(210, "wulfenite", 0xFF8000, DULL, 3, of(new MaterialStack(Lead, 1), new MaterialStack(Molybdenum, 1), new MaterialStack(Oxygen, 4)), GENERATE_ORE);
        YellowLimonite = new MaterialBuilder(211, "yellow_limonite", 0xC8C800, METALLIC, 2, of(new MaterialStack(Iron, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(Oxygen, 2)), GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING);
        YttriumBariumCuprate = new MaterialBuilder(212, "yttrium_barium_cuprate", 0x504046, METALLIC, 2, of(new MaterialStack(Yttrium, 1), new MaterialStack(Barium, 2), new MaterialStack(Copper, 3), new MaterialStack(Oxygen, 7)), EXT_METAL | GENERATE_FOIL | GENERATE_FINE_WIRE, null, 4500);
        NetherQuartz = new MaterialBuilder(213, "nether_quartz", 0xE6D2D2, QUARTZ, 1, of(), STD_SOLID | NO_SMELTING | CRYSTALLIZABLE | GENERATE_ORE | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES);
        CertusQuartz = new MaterialBuilder(214, "certus_quartz", 0xD2D2E6, QUARTZ, 1, of(), STD_SOLID | NO_SMELTING | CRYSTALLIZABLE | GENERATE_ORE);
        Quartzite = new MaterialBuilder(215, "quartzite", 0xD2E6D2, QUARTZ, 1, of(), NO_SMELTING | CRYSTALLIZABLE | GENERATE_ORE);
        Graphite = new MaterialBuilder(216, "graphite", 0x808080, DULL, 2, of(), GENERATE_PLATE | GENERATE_ORE | NO_SMELTING | FLAMMABLE);
        Graphene = new MaterialBuilder(217, "graphene", 0x808080, SHINY, 2, of(), GENERATE_PLATE | GENERATE_FOIL);
        Jasper = new MaterialBuilder(218, "jasper", 0xC85050, EMERALD, 2, of(), STD_GEM | NO_SMELTING | HIGH_SIFTER_OUTPUT);
        Osmiridium = new MaterialBuilder(219, "osmiridium", 0x6464FF, METALLIC, 3, of(new MaterialStack(Iridium, 3), new MaterialStack(Osmium, 1)), EXT2_METAL, null, 9.0F, 3.0f, 3152, 2500);
        Tenorite = new MaterialBuilder(220, "tenorite", 0x606060, DULL, 1, of(new MaterialStack(Copper, 1), new MaterialStack(Oxygen, 1)), GENERATE_ORE);
        Cuprite = new MaterialBuilder(221, "cuprite", 0x770000, RUBY, 2, of(new MaterialStack(Copper, 2), new MaterialStack(Oxygen, 1)), GENERATE_ORE);
        Bornite = new MaterialBuilder(222, "bornite", 0x97662B, METALLIC, 1, of(new MaterialStack(Copper, 5), new MaterialStack(Iron, 1), new MaterialStack(Sulfur, 4)), GENERATE_ORE);
        Chalcocite = new MaterialBuilder(223, "chalcocite", 0x353535, GEM_VERTICAL, 2, of(new MaterialStack(Copper, 2), new MaterialStack(Sulfur, 1)), GENERATE_ORE);
        Enargite = new MaterialBuilder(224, "enargite", 0xBBBBBB, METALLIC, 2, of(new MaterialStack(Copper, 3), new MaterialStack(Arsenic, 1), new MaterialStack(Sulfur, 4)), GENERATE_ORE);
        Tennantite = new MaterialBuilder(225, "tennantite", 0x909090, METALLIC, 2, of(new MaterialStack(Copper, 12), new MaterialStack(Arsenic, 4), new MaterialStack(Sulfur, 13)), GENERATE_ORE);

        GalliumArsenide = new MaterialBuilder(226, "gallium_arsenide", 0xA0A0A0, DULL, 1, of(new MaterialStack(Arsenic, 1), new MaterialStack(Gallium, 1)), DECOMPOSITION_BY_CENTRIFUGING | GENERATE_PLATE, null, 1200);
        Potash = new MaterialBuilder(227, "potash", 0x784137, DULL, 1, of(new MaterialStack(Potassium, 2), new MaterialStack(Oxygen, 1)), 0);
        SodaAsh = new MaterialBuilder(228, "soda_ash", 0xDCDCFF, DULL, 1, of(new MaterialStack(Sodium, 2), new MaterialStack(Carbon, 1), new MaterialStack(Oxygen, 3)), 0);
        IndiumGalliumPhosphide = new MaterialBuilder(229, "indium_gallium_phosphide", 0xA08CBE, DULL, 1, of(new MaterialStack(Indium, 1), new MaterialStack(Gallium, 1), new MaterialStack(Phosphorus, 1)), DECOMPOSITION_BY_CENTRIFUGING | GENERATE_PLATE);
        NickelZincFerrite = new MaterialBuilder(230, "nickel_zinc_ferrite", 0x3C3C3C, METALLIC, 0, of(new MaterialStack(Nickel, 1), new MaterialStack(Zinc, 1), new MaterialStack(Iron, 4), new MaterialStack(Oxygen, 8)), EXT_METAL | GENERATE_RING, null, 1500);
        SiliconDioxide = new MaterialBuilder(231, "silicon_dioxide", 0xC8C8C8, QUARTZ, 1, of(new MaterialStack(Silicon, 1), new MaterialStack(Oxygen, 2)), NO_SMASHING | NO_SMELTING | CRYSTALLIZABLE);
        MagnesiumChloride = new MaterialBuilder(232, "magnesium_chloride", 0xD40D5C, DULL, 1, of(new MaterialStack(Magnesium, 1), new MaterialStack(Chlorine, 2)), 0);
        SodiumSulfide = new MaterialBuilder(233, "sodium_sulfide", 0xFFE680, DULL, 1, of(new MaterialStack(Sodium, 2), new MaterialStack(Sulfur, 1)), 0);
        PhosphorusPentoxide = new MaterialBuilder(234, "phosphorus_pentoxide", 0xDCDC00, DULL, 1, of(new MaterialStack(Phosphorus, 4), new MaterialStack(Oxygen, 10)), DECOMPOSITION_BY_CENTRIFUGING );
        Quicklime = new MaterialBuilder(235, "quicklime", 0xF0F0F0, DULL, 1, of(new MaterialStack(Calcium, 1), new MaterialStack(Oxygen, 1)), 0);
        SodiumBisulfate = new MaterialBuilder(236, "sodium_bisulfate", 0x004455, DULL, 1, of(new MaterialStack(Sodium, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 4)), DISABLE_DECOMPOSITION);
        FerriteMixture = new MaterialBuilder(237, "ferrite_mixture", 0xB4B4B4, METALLIC, 1, of(new MaterialStack(Nickel, 1), new MaterialStack(Zinc, 1), new MaterialStack(Iron, 4)), DECOMPOSITION_BY_CENTRIFUGING );
        Magnesia = new MaterialBuilder(238, "magnesia", 0x887878, DULL, 1, of(new MaterialStack(Magnesium, 1), new MaterialStack(Oxygen, 1)), 0);
        PlatinumGroupSludge = new MaterialBuilder(239, "platinum_group_sludge", 0x001E00, FINE, 1, of(), DISABLE_DECOMPOSITION);
        HydratedCoal = new MaterialBuilder(240, "hydrated_coal", 0x464664, ROUGH, 1, of(new MaterialStack(Coal, 8), new MaterialStack(Water, 1)), 0);
        SodiumBicarbonate = new MaterialBuilder(241, "sodium_bicarbonate", 0x565b96, ROUGH, 1, of(new MaterialStack(Sodium, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(Carbon, 1), new MaterialStack(Oxygen, 3)), 0);
        PotassiumDichromate = new MaterialBuilder(242, "potassium_dichromate", 0xFF084E, DULL, 1, of(new MaterialStack(Potassium, 2), new MaterialStack(Chrome, 2), new MaterialStack(Oxygen, 7)), 0);
        ChromiumTrioxide = new MaterialBuilder(243, "chromium_trioxide", 0xFFE4E1, DULL, 1, of(new MaterialStack(Chrome, 1), new MaterialStack(Oxygen, 3)), 0);
        AntimonyTrioxide = new MaterialBuilder(244, "antimony_trioxide", 0xE6E6F0, DULL, 1, of(new MaterialStack(Antimony, 2), new MaterialStack(Oxygen, 3)),0);
        Zincite = new MaterialBuilder(245, "zincite", 0xFFFFF5, DULL, 1, of(new MaterialStack(Zinc, 1), new MaterialStack(Oxygen, 1)), 0);
        CupricOxide = new MaterialBuilder(246, "cupric_oxide", 0x0F0F0F, DULL, 1, of(new MaterialStack(Copper, 1), new MaterialStack(Oxygen, 1)), 0);
        CobaltOxide = new MaterialBuilder(247, "cobalt_oxide", 0x788000, DULL, 1, of(new MaterialStack(Cobalt, 1), new MaterialStack(Oxygen, 1)), 0);
        ArsenicTrioxide = new MaterialBuilder(248, "arsenic_trioxide", 0xFFFFFF, ROUGH, 1, of(new MaterialStack(Arsenic, 2), new MaterialStack(Oxygen, 3)),0);
        Massicot = new MaterialBuilder(249, "massicot", 0xFFDD55, DULL, 1, of(new MaterialStack(Lead, 1), new MaterialStack(Oxygen, 1)), 0);
        Ferrosilite = new MaterialBuilder(250, "ferrosilite", 0x97632A, DULL, 1, of(new MaterialStack(Iron, 1), new MaterialStack(Silicon, 1), new MaterialStack(Oxygen, 3)), 0);
        MetalMixture = new MaterialBuilder(251, "metal_mixture", 0x502d16, METALLIC, 1, of(), DISABLE_DECOMPOSITION);
        SodiumHydroxide = new MaterialBuilder(252, "sodium_hydroxide", 0x003380, DULL, 1, of(new MaterialStack(Sodium, 1), new MaterialStack(Oxygen, 1), new MaterialStack(Hydrogen, 1)), 0);
        SodiumPersulfate = new MaterialBuilder(253, "sodium_persulfate", 0xFFFFFF, FLUID, of(new MaterialStack(Sodium, 2), new MaterialStack(Sulfur, 2), new MaterialStack(Oxygen, 8)), 0);
    }
}
