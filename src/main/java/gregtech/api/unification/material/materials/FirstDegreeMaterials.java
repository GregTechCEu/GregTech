package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.Material;
import net.minecraft.init.Enchantments;

import static gregtech.api.unification.material.MaterialIconSet.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.type.MaterialFlags.*;

public class FirstDegreeMaterials {
    /**
     * ID RANGE: 124-253 (incl.)
     */
    public static void register() {
        Almandine = new Material.Builder(124, "almandine")
                .gem(1).ore(6, 1)
                .color(0xFF0000).iconSet(GEM_VERTICAL)
                .flags(STD_GEM)
                .components(Aluminium, 2, Iron, 3, Silicon, 3, Oxygen, 12)
                .addOreByproducts(GarnetRed, Aluminium)
                .build();

        Andradite = new Material.Builder(125, "andradite")
                .dust(1)
                .color(0x967800).iconSet(ROUGH)
                .components(Calcium, 3, Iron, 2, Silicon, 3, Oxygen, 12)
                .addOreByproducts(GarnetYellow, Iron)
                .separatedInto(Iron)
                .build();

        AnnealedCopper = new Material.Builder(126, "annealed_copper")
                .ingot().fluid()
                .color(0xFF7814).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .components(Copper, 1)
                .build();

        Asbestos = new Material.Builder(127, "asbestos")
                .dust(1)
                .color(0xE6E6E6)
                .components(Magnesium, 3, Silicon, 2, Hydrogen, 4, Oxygen, 9)
                .addOreByproducts(Asbestos, Silicon, Magnesium)
                .build();

        Ash = new Material.Builder(128, "ash")
                .dust(1)
                .color(0x969696)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        BandedIron = new Material.Builder(129, "banded_iron")
                .dust().ore()
                .color(0x915A5A)
                .components(Iron, 2, Oxygen, 3)
                .separatedInto(Iron)
                .oreSmeltInto(Iron)
                .build();

        BatteryAlloy = new Material.Builder(130, "battery_alloy")
                .ingot(1).fluid()
                .color(0x9C7CA0)
                .flags(EXT_METAL)
                .components(Lead, 4, Antimony, 1)
                .build();

        BlueTopaz = new Material.Builder(131, "blue_topaz")
                .gem(3).ore()
                .color(0x0000FF).iconSet(GEM_HORIZONTAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Silicon, 1, Fluorine, 2, Hydrogen, 2, Oxygen, 6)
                .toolStats(7.0f, 3.0f, 256)
                .addOreByproducts(Topaz)
                .build();

        Bone = new Material.Builder(132, "bone")
                .dust(1)
                .color(0xFAFAFA)
                .flags(MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Calcium, 1)
                .build();

        Brass = new Material.Builder(133, "brass")
                .ingot(1).fluid()
                .color(0xFFB400).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_RING)
                .components(Zinc, 1, Copper, 3)
                .toolStats(8.0f, 3.0f, 152)
                .itemPipeProperties(2048, 1)
                .build();

        Bronze = new Material.Builder(134, "bronze")
                .ingot().fluid()
                .color(0xFF8000).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_FRAME)
                .components(Tin, 1, Copper, 3)
                .toolStats(6.0f, 2.5f, 192)
                .fluidPipeProperties(1696, 20, true)
                .build();

        BrownLimonite = new Material.Builder(135, "brown_limonite")
                .dust(1).ore()
                .color(0xC86400).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, BLAST_FURNACE_CALCITE_TRIPLE)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .addOreByproducts(Malachite, YellowLimonite)
                .separatedInto(Iron)
                .oreSmeltInto(Iron)
                .build();

        Calcite = new Material.Builder(136, "calcite")
                .dust(1).ore()
                .color(0xFAE6DC)
                .components(Calcium, 1, Carbon, 1, Oxygen, 3)
                .addOreByproducts(Andradite, Malachite)
                .build();

        Cassiterite = new Material.Builder(137, "cassiterite")
                .dust(1).ore(2, 1)
                .color(0xDCDCDC).iconSet(METALLIC)
                .components(Tin, 1, Oxygen, 2)
                .addOreByproducts(Tin, Bismuth)
                .oreSmeltInto(Tin)
                .build();

        CassiteriteSand = new Material.Builder(138, "cassiterite_sand")
                .dust(1).ore(2, 1)
                .color(0xDCDCDC).iconSet(SAND)
                .components(Tin, 1, Oxygen, 2)
                .addOreByproducts(Tin)
                .oreSmeltInto(Tin)
                .build();

        Chalcopyrite = new Material.Builder(139, "chalcopyrite")
                .dust(1).ore()
                .color(0xA07828)
                .components(Copper, 1, Iron, 1, Sulfur, 2)
                .addOreByproducts(Pyrite, Cobalt, Cadmium, Gold)
                .washedIn(Mercury)
                .oreSmeltInto(Copper)
                .build();

        Charcoal = new Material.Builder(140, "charcoal")
                .gem(1, 1600) //default charcoal burn time in vanilla
                .color(0x644646).iconSet(FINE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE)
                .components(Carbon, 1)
                .build();

        Chromite = new Material.Builder(141, "chromite")
                .dust(1).ore()
                .color(0x23140F).iconSet(METALLIC)
                .components(Iron, 1, Chrome, 2, Oxygen, 4)
                .addOreByproducts(Iron, Magnesium)
                .separatedInto(Iron)
                .oreSmeltInto(Chrome)
                .build();

        Cinnabar = new Material.Builder(142, "cinnabar")
                .gem(1).ore()
                .color(0x960000).iconSet(EMERALD)
                .flags(CRYSTALLIZABLE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Mercury, 1, Sulfur, 1)
                .addOreByproducts(Redstone, Sulfur, Glowstone)
                .build();

        Water = new Material.Builder(143, "water")
                .fluid()
                .color(0x0000FF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .build();

        Clay = new Material.Builder(144, "clay")
                .dust(1)
                .color(0xC8C8DC).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE)
                .components(Sodium, 2, Lithium, 1, Aluminium, 2, Silicon, 2, Water, 6)
                .build();

        Coal = new Material.Builder(145, "coal")
                .gem(1, 1600).ore() //default coal burn time in vanilla
                .color(0x464646).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .addOreByproducts(Lignite, Thorium)
                .build();

        Cobaltite = new Material.Builder(146, "cobaltite")
                .dust(1).ore()
                .color(0x5050FA).iconSet(METALLIC)
                .components(Cobalt, 1, Arsenic, 1, Sulfur, 1)
                .addOreByproducts(Cobalt, Cobaltite)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Cobalt)
                .build();

        Cooperite = new Material.Builder(147, "cooperite")
                .dust(1).ore()
                .color(0xFFFFC8).iconSet(METALLIC)
                .components(Platinum, 3, Nickel, 1, Sulfur, 1, Palladium, 1)
                .addOreByproducts(Palladium, Nickel, Iridium, Cooperite)
                .washedIn(Mercury)
                .oreSmeltInto(Platinum)
                .build();

        Cupronickel = new Material.Builder(148, "cupronickel")
                .ingot(1).fluid()
                .color(0xE39680).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Copper, 1, Nickel, 1)
                .build();

        DarkAsh = new Material.Builder(149, "dark_ash")
                .dust(1)
                .color(0x323232)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Diamond = new Material.Builder(150, "diamond")
                .gem(3).ore()
                .color(0xC8FFFF).iconSet(DIAMOND)
                .flags(GENERATE_BOLT_SCREW, GENERATE_LENS, GENERATE_GEAR, NO_SMASHING, NO_SMELTING, FLAMMABLE, GENERATE_BOLT_SCREW,
                        HIGH_SIFTER_OUTPUT, DISABLE_DECOMPOSITION, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .toolStats(8.0f, 3.0f, 1280)
                .addOreByproducts(Graphite)
                .build();

        Electrum = new Material.Builder(151, "electrum")
                .ingot().fluid()
                .color(0xFFFF64).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .components(Silver, 1, Gold, 1)
                .addOreByproducts(Gold, Silver)
                .itemPipeProperties(1024, 2)
                .build();

        Emerald = new Material.Builder(152, "emerald")
                .gem().ore()
                .color(0x50FF50).iconSet(EMERALD)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Beryllium, 3, Aluminium, 2, Silicon, 6, Oxygen, 18)
                .toolStats(10.0f, 2.0f, 368)
                .addOreByproducts(Beryllium, Aluminium)
                .build();

        Galena = new Material.Builder(153, "galena")
                .dust(3).ore()
                .color(0x643C64)
                .flags(NO_SMELTING)
                .components(Lead, 3, Silver, 3, Sulfur, 2)
                .addOreByproducts(Sulfur, Silver, Lead, Silver)
                .washedIn(Mercury)
                .oreSmeltInto(Lead)
                .build();

        Garnierite = new Material.Builder(154, "garnierite")
                .dust(3).ore()
                .color(0x32C846).iconSet(METALLIC)
                .components(Nickel, 1, Oxygen, 1)
                .addOreByproducts(Nickel)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Nickel)
                .build();

        GreenSapphire = new Material.Builder(155, "green_sapphire")
                .gem().ore()
                .color(0x64C882).iconSet(GEM_HORIZONTAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(Aluminium, 2, Oxygen, 3)
                .toolStats(8.0f, 3.0f, 368)
                .addOreByproducts(Aluminium, Sapphire)
                .build();

        Grossular = new Material.Builder(156, "grossular")
                .dust(1).ore(6, 1)
                .color(0xC86400).iconSet(ROUGH)
                .components(Calcium, 3, Aluminium, 2, Silicon, 3, Oxygen, 12)
                .addOreByproducts(GarnetYellow, Calcium)
                .build();

        Ice = new Material.Builder(157, "ice")
                .dust(0).fluid()
                .color(0xC8C8FF).iconSet(SHINY)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Hydrogen, 2, Oxygen, 1)
                .build();

        Ilmenite = new Material.Builder(158, "ilmenite")
                .dust(3).ore(3, 1)
                .color(0x463732).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iron, 1, Titanium, 1, Oxygen, 3)
                .addOreByproducts(Iron, Rutile)
                .separatedInto(Iron)
                .build();

        Rutile = new Material.Builder(159, "rutile")
                .gem().ore(3, 1)
                .color(0xD40D5C).iconSet(GEM_HORIZONTAL)
                .flags(DISABLE_DECOMPOSITION)
                .components(Titanium, 1, Oxygen, 2)
                .build();

        Bauxite = new Material.Builder(160, "bauxite")
                .dust(1).ore(3, 1)
                .color(0xC86400)
                .flags(DISABLE_DECOMPOSITION)
                .components(Rutile, 2, Aluminium, 16, Hydrogen, 10, Oxygen, 11)
                .addOreByproducts(Grossular, Rutile, Gallium)
                .build();

        Invar = new Material.Builder(161, "invar")
                .ingot().fluid()
                .color(0xB4B478).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_RING, GENERATE_FRAME)
                .components(Iron, 2, Nickel, 1)
                .toolStats(7.0f, 3.0f, 512)
                .addDefaultEnchant(Enchantments.BANE_OF_ARTHROPODS, 3)
                .fluidPipeProperties(2395, 40, true)
                .build();

        Kanthal = new Material.Builder(162, "kanthal")
                .ingot().fluid()
                .color(0xC2D2DF).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Iron, 1, Aluminium, 1, Chrome, 1)
                .blastTemp(1800)
                .build();

        Lazurite = new Material.Builder(163, "lazurite")
                .gem(1).ore(6, 4)
                .color(0x6478FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, GENERATE_ROD, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 6, Silicon, 6, Calcium, 8, Sodium, 8)
                .addOreByproducts(Sodalite, Lapis)
                .build();

        Magnalium = new Material.Builder(164, "magnalium")
                .ingot().fluid()
                .color(0xC8BEFF)
                .flags(EXT2_METAL)
                .components(Magnesium, 1, Aluminium, 2)
                .toolStats(6.0f, 2.0f, 256)
                .itemPipeProperties(1024, 2)
                .build();

        Magnesite = new Material.Builder(165, "magnesite")
                .dust().ore()
                .color(0xFAFAB4).iconSet(METALLIC)
                .components(Magnesium, 1, Carbon, 1, Oxygen, 3)
                .addOreByproducts(Magnesium)
                .oreSmeltInto(Magnesium)
                .build();

        Magnetite = new Material.Builder(166, "magnetite")
                .dust().ore()
                .color(0x1E1E1E).iconSet(METALLIC)
                .components(Iron, 3, Oxygen, 4)
                .addOreByproducts(Iron, Gold)
                .separatedInto(Gold)
                .washedIn(Mercury)
                .oreSmeltInto(Iron)
                .build();

        Molybdenite = new Material.Builder(167, "molybdenite")
                .dust().ore()
                .color(0x191919).iconSet(METALLIC)
                .components(Molybdenum, Sulfur, 2)
                .addOreByproducts(Molybdenum)
                .oreSmeltInto(Molybdenum)
                .build();

        Nichrome = new Material.Builder(168, "nichrome")
                .ingot().fluid()
                .color(0xCDCEF6).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_SPRING)
                .components(Nickel, 4, Chrome, 1)
                .blastTemp(2700)
                .build();

        NiobiumNitride = new Material.Builder(169, "niobium_nitride")
                .ingot().fluid()
                .color(0x1D291D)
                .flags(EXT_METAL)
                .components(Niobium, 1, Nitrogen, 1)
                .blastTemp(2573)
                .build();

        NiobiumTitanium = new Material.Builder(170, "niobium_titanium")
                .ingot().fluid()
                .color(0x1D1D29)
                .flags(EXT2_METAL)
                .components(Niobium, 1, Titanium, 1)
                .fluidPipeProperties(2900, 150, true)
                .blastTemp(4500)
                .build();

        Obsidian = new Material.Builder(171, "obsidian")
                .dust(3)
                .color(0x503264)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_RECIPES, GENERATE_PLATE)
                .components(Magnesium, 1, Iron, 1, Silicon, 2, Oxygen, 4)
                .build();

        Phosphate = new Material.Builder(172, "phosphate")
                .dust(1).ore()
                .color(0xFFFF00)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE, EXPLOSIVE)
                .components(Phosphorus, Oxygen, 4)
                .build();

        PigIron = new Material.Builder(173, "pig_iron")
                .ingot().fluid()
                .color(0xC8B4B4).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_RING, BLAST_FURNACE_CALCITE_TRIPLE)
                .components(Iron, 1)
                .toolStats(6.0f, 4.0f, 384)
                .build();

        SterlingSilver = new Material.Builder(174, "sterling_silver")
                .ingot().fluid()
                .color(0xFADCE1).iconSet(SHINY)
                .flags(EXT2_METAL)
                .components(Copper, 1, Silver, 4)
                .toolStats(13.0f, 2.0f, 196)
                .itemPipeProperties(1024, 2)
                .blastTemp(1700)
                .build();

        RoseGold = new Material.Builder(175, "rose_gold")
                .ingot().fluid()
                .color(0xFFE61E).iconSet(SHINY)
                .flags(EXT2_METAL)
                .components(Copper, 1, Gold, 4)
                .toolStats(14.0f, 2.0f, 152)
                .addDefaultEnchant(Enchantments.SMITE, 4)
                .itemPipeProperties(1024, 2)
                .blastTemp(1600)
                .build();

        BlackBronze = new Material.Builder(176, "black_bronze")
                .ingot().fluid()
                .color(0x64327D)
                .flags(EXT2_METAL)
                .components(Gold, 1, Silver, 1, Copper, 3)
                .toolStats(12.0f, 2.0f, 256)
                .addDefaultEnchant(Enchantments.SMITE, 2)
                .itemPipeProperties(1024, 2)
                .blastTemp(2000)
                .build();

        BismuthBronze = new Material.Builder(177, "bismuth_bronze")
                .ingot().fluid()
                .color(0x647D7D)
                .flags(EXT2_METAL)
                .components(Bismuth, 1, Zinc, 1, Copper, 3)
                .toolStats(8.0f, 3.0f, 256)
                .addDefaultEnchant(Enchantments.BANE_OF_ARTHROPODS, 5)
                .blastTemp(1100)
                .build();

        Biotite = new Material.Builder(178, "biotite")
                .dust(1)
                .color(0x141E14).iconSet(METALLIC)
                .components(Potassium, 1, Magnesium, 3, Aluminium, 3, Fluorine, 2, Silicon, 3, Oxygen, 10)
                .build();

        Powellite = new Material.Builder(179, "powellite")
                .dust().ore()
                .color(0xFFFF00)
                .components(Calcium, 1, Molybdenum, 1, Oxygen, 4)
                .build();

        Pyrite = new Material.Builder(180, "pyrite")
                .dust(1).ore()
                .color(0x967828).iconSet(ROUGH)
                .flags(BLAST_FURNACE_CALCITE_DOUBLE)
                .components(Iron, 1, Sulfur, 2)
                .addOreByproducts(Sulfur, TricalciumPhosphate, Iron)
                .separatedInto(Iron)
                .oreSmeltInto(Iron)
                .build();

        Pyrolusite = new Material.Builder(181, "pyrolusite")
                .dust().ore()
                .color(0x9696AA)
                .components(Manganese, 1, Oxygen, 2)
                .addOreByproducts(Manganese)
                .oreSmeltInto(Manganese)
                .build();

        Pyrope = new Material.Builder(182, "pyrope")
                .dust().ore(4, 1)
                .color(0x783264).iconSet(METALLIC)
                .components(Aluminium, 2, Magnesium, 3, Silicon, 3, Oxygen, 12)
                .addOreByproducts(GarnetRed, Magnesium)
                .build();

        RockSalt = new Material.Builder(183, "rock_salt")
                .dust(1).ore(2, 1)
                .color(0xF0C8C8).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Potassium, 1, Chlorine, 1)
                .addOreByproducts(Salt, Borax)
                .build();

        Rubber = new Material.Builder(184, "rubber")
                .ingot(0).fluid()
                .color(0x000000).iconSet(SHINY)
                .flags(GENERATE_GEAR, GENERATE_RING, FLAMMABLE, NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Carbon, 5, Hydrogen, 8)
                .build();

        Ruby = new Material.Builder(185, "ruby")
                .gem().ore()
                .color(0xFF6464).iconSet(RUBY)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Chrome, 1, Aluminium, 2, Oxygen, 3)
                .toolStats(8.5f, 3.0f, 256)
                .addOreByproducts(Chrome, GarnetRed)
                .build();

        Salt = new Material.Builder(186, "salt")
                .dust(1).ore(2, 1)
                .color(0xFAFAFA).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Sodium, 1, Chlorine, 1)
                .addOreByproducts(RockSalt, Borax)
                .build();

        Saltpeter = new Material.Builder(187, "saltpeter")
                .dust(1).ore(4, 1)
                .color(0xE6E6E6).iconSet(FINE)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE)
                .components(Potassium, 1, Nitrogen, 1, Oxygen, 3)
                .addOreByproducts(Saltpeter)
                .build();

        Sapphire = new Material.Builder(188, "sapphire")
                .gem().ore()
                .color(0x6464C8).iconSet(GEM_VERTICAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Oxygen, 3)
                .toolStats(7.5f, 4.0f, 256)
                .addOreByproducts(Aluminium, GreenSapphire)
                .build();

        Scheelite = new Material.Builder(189, "scheelite")
                .dust(3).ore(2, 1)
                .color(0xC88C14)
                .flags(DECOMPOSITION_REQUIRES_HYDROGEN)
                .components(Tungsten, 1, Calcium, 2, Oxygen, 4)
                .addOreByproducts(Manganese, Molybdenum, Calcium)
                .build();

        Sodalite = new Material.Builder(190, "sodalite")
                .gem(1).ore(6, 4)
                .color(0x1414FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, GENERATE_ROD, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 3, Silicon, 3, Sodium, 4, Chlorine, 1)
                .addOreByproducts(Lazurite, Lapis)
                .build();

        DiamericiumTitanium = new Material.Builder(191, "diamericium_titanium")
                .ingot(4).fluid()
                .color(0x755280).iconSet(METALLIC)
                .components(Americium, 2, Titanium, 1)
                .toolStats(6.0f, 6.0f, 2200)
                .itemPipeProperties(32, 128)
                .blastTemp(10400)
                .build();

        Tantalite = new Material.Builder(192, "tantalite")
                .dust(3).ore(2, 1)
                .color(0x915028).iconSet(METALLIC)
                .components(Manganese, 1, Tantalum, 2, Oxygen, 6)
                .addOreByproducts(Manganese, Niobium, Tantalum)
                .build();

        Coke = new Material.Builder(193, "coke")
                .gem(2, 3200) // 2x burn time of coal
                .color(0x666666).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE)
                .components(Carbon, 1)
                .build();

        SolderingAlloy = new Material.Builder(194, "soldering_alloy")
                .ingot(1).fluid()
                .color(0x9696A0)
                .flags(EXT_METAL, GENERATE_FINE_WIRE)
                .components(Tin, 9, Antimony, 1)
                .build();

        Spessartine = new Material.Builder(195, "spessarite")
                .dust().ore(2, 1)
                .color(0xFF6464)
                .components(Aluminium, 2, Manganese, 3, Silicon, 3, Oxygen, 12)
                .addOreByproducts(GarnetRed, Manganese)
                .build();

        Sphalerite = new Material.Builder(196, "sphalerite")
                .dust(1).ore()
                .color(0xFFFFFF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zinc, 1, Sulfur)
                .addOreByproducts(GarnetYellow, Cadmium, Gallium, Zinc)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Zinc)
                .build();

        StainlessSteel = new Material.Builder(197, "stainless_steel")
                .ingot().fluid()
                .color(0xC8C8DC).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_FRAME, GENERATE_LONG_ROD)
                .components(Iron, 7, Chrome, 1, Manganese, 1, Nickel, 1)
                .toolStats(7.0f, 4.0f, 480)
                .fluidPipeProperties(2428, 60, true)
                .blastTemp(1700)
                .build();

        Steel = new Material.Builder(198, "steel")
                .ingot().fluid()
                .color(0x808080).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_DENSE, GENERATE_SPRING,
                        GENERATE_SPRING_SMALL, GENERATE_FRAME, GENERATE_LONG_ROD, DISABLE_DECOMPOSITION)
                .components(Iron, 1)
                .toolStats(6.0f, 3.0f, 512)
                .polarizesInto(SteelMagnetic)
                .blastTemp(1000)
                .build();

        Stibnite = new Material.Builder(199, "stibnite")
                .dust().ore()
                .color(0x464646).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Antimony, 2, Sulfur, 3)
                .addOreByproducts(Antimony)
                .oreSmeltInto(Antimony)
                .build();

        Tanzanite = new Material.Builder(200, "tanzanite")
                .gem().ore(2, 1)
                .color(0x4000C8).iconSet(GEM_VERTICAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Calcium, 2, Aluminium, 3, Silicon, 3, Hydrogen, 1)
                .toolStats(7.0f, 2.0f, 256)
                .addOreByproducts(Opal)
                .build();

        Tetrahedrite = new Material.Builder(201, "tetrahedrite")
                .dust().ore()
                .color(0xC82000)
                .components(Copper, 3, Antimony, 1, Sulfur, 3, Iron, 1)
                .addOreByproducts(Antimony, Zinc, Tetrahedrite)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Copper)
                .build();

        TinAlloy = new Material.Builder(202, "tin_alloy")
                .ingot().fluid()
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Tin, 1, Iron, 1)
                .fluidPipeProperties(1572, 38, true)
                .build();

        Topaz = new Material.Builder(203, "topaz")
                .gem(3).ore(2, 1)
                .color(0xFF8000).iconSet(GEM_HORIZONTAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Silicon, 1, Fluorine, 1, Hydrogen, 2)
                .toolStats(7.0f, 2.0f, 256)
                .addOreByproducts(BlueTopaz)
                .build();

        Tungstate = new Material.Builder(204, "tungstate")
                .dust(3).ore(2, 1)
                .color(0x373223)
                .flags(DECOMPOSITION_REQUIRES_HYDROGEN)
                .components(Tungsten, 1, Lithium, 2, Oxygen, 4)
                .addOreByproducts(Manganese, Silver, Lithium, Silver)
                .washedIn(Mercury)
                .build();

        Ultimet = new Material.Builder(205, "ultimet")
                .ingot(4).fluid()
                .color(0xB4B4E6).iconSet(SHINY)
                .flags(EXT2_METAL)
                .components(Cobalt, 5, Chrome, 2, Nickel, 1, Molybdenum, 1)
                .toolStats(9.0f, 4.0f, 2048)
                .itemPipeProperties(128, 16)
                .blastTemp(2700)
                .build();

        Uraninite = new Material.Builder(206, "uraninite")
                .dust(3).ore()
                .color(0x232323).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium238, 1, Oxygen, 2)
                .addOreByproducts(Uranium238, Thorium, Uranium235)
                .build()
                .setFormula("UO2", true);

        Uvarovite = new Material.Builder(207, "uvarovite")
                .dust()
                .color(0xB4FFB4).iconSet(DIAMOND)
                .components(Calcium, 3, Chrome, 2, Silicon, 3, Oxygen, 12)
                .addOreByproducts(GarnetYellow, Chrome)
                .build();

        VanadiumGallium = new Material.Builder(208, "vanadium_gallium")
                .ingot().fluid()
                .color(0x80808C).iconSet(SHINY)
                .flags(STD_METAL, GENERATE_FOIL, GENERATE_ROD)
                .components(Vanadium, 3, Gallium, 1)
                .blastTemp(4500)
                .build();

        WroughtIron = new Material.Builder(209, "wrought_iron")
                .ingot().fluid()
                .color(0xC8B4B4).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_RING, GENERATE_LONG_ROD, DISABLE_DECOMPOSITION, BLAST_FURNACE_CALCITE_TRIPLE)
                .components(Iron, 1)
                .toolStats(6.0f, 3.5f, 384)
                .fluidPipeProperties(2387, 30, true)
                .build();

        Wulfenite = new Material.Builder(210, "wulfenite")
                .dust(3).ore()
                .color(0xFF8000)
                .components(Lead, 1, Molybdenum, 1, Oxygen, 4)
                .build();

        YellowLimonite = new Material.Builder(211, "yellow_limonite")
                .dust().ore()
                .color(0xC8C800).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, BLAST_FURNACE_CALCITE_DOUBLE)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .addOreByproducts(Nickel, BrownLimonite, Cobalt, Nickel)
                .separatedInto(Iron)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Iron)
                .build();

        YttriumBariumCuprate = new Material.Builder(212, "yttrium_barium_cuprate")
                .ingot().fluid()
                .color(0x504046).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FINE_WIRE)
                .components(Yttrium, 1, Barium, 2, Copper, 3, Oxygen, 7)
                .build();

        NetherQuartz = new Material.Builder(213, "nether_quartz")
                .gem(1).ore(2, 1)
                .color(0xE6D2D2).iconSet(QUARTZ)
                .flags(STD_SOLID, NO_SMELTING, CRYSTALLIZABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .addOreByproducts(Netherrack)
                .build();

        CertusQuartz = new Material.Builder(214, "certus_quartz")
                .gem(1).ore(2, 1)
                .color(0xD2D2E6).iconSet(QUARTZ)
                .flags(STD_SOLID, NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION)
                .components(Silicon, 1, Oxygen, 2)
                .addOreByproducts(Quartzite, Barite)
                .build();

        Quartzite = new Material.Builder(215, "quartzite")
                .gem(1).ore(2, 1)
                .color(0xD2E6D2).iconSet(QUARTZ)
                .flags(NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION, GENERATE_PLATE)
                .components(Silicon, 1, Oxygen, 2)
                .addOreByproducts(CertusQuartz, Barite)
                .build();

        Graphite = new Material.Builder(216, "graphite")
                .ingot().ore().fluid()
                .color(0x808080)
                .flags(STD_METAL, NO_SMELTING, FLAMMABLE, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .addOreByproducts(Carbon)
                .build();

        Graphene = new Material.Builder(217, "graphene")
                .ingot().fluid()
                .color(0x808080).iconSet(SHINY)
                .flags(GENERATE_FOIL, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .build();

        Jasper = new Material.Builder(218, "jasper")
                .gem().ore()
                .color(0xC85050).iconSet(EMERALD)
                .flags(NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .build();

        Osmiridium = new Material.Builder(219, "osmiridium")
                .ingot(3).fluid()
                .color(0x6464FF).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Iridium, 3, Osmium, 1)
                .toolStats(9.0f, 3.0f, 3152)
                .itemPipeProperties(64, 32)
                .blastTemp(2500)
                .build();

        Tenorite = new Material.Builder(220, "tenorite")
                .dust(1).ore()
                .color(0x606060)
                .components(Copper, 1, Oxygen, 1)
                .addOreByproducts(Iron, Manganese, Malachite)
                .oreSmeltInto(Copper)
                .build();

        Cuprite = new Material.Builder(221, "cuprite")
                .dust().ore()
                .color(0x770000).iconSet(RUBY)
                .components(Copper, 2, Oxygen, 1)
                .addOreByproducts(Iron, Antimony, Malachite)
                .oreSmeltInto(Copper)
                .build();

        Bornite = new Material.Builder(222, "bornite")
                .dust(1).ore()
                .color(0x97662B).iconSet(METALLIC)
                .components(Copper, 5, Iron, 1, Sulfur, 4)
                .addOreByproducts(Pyrite, Cobalt, Cadmium, Gold)
                .washedIn(Mercury)
                .oreSmeltInto(Copper)
                .build();

        Chalcocite = new Material.Builder(223, "chalcocite")
                .dust().ore()
                .color(0x353535).iconSet(GEM_VERTICAL)
                .components(Copper, 2, Sulfur, 1)
                .addOreByproducts(Sulfur, Lead, Silver)
                .oreSmeltInto(Copper)
                .build();

        Enargite = new Material.Builder(224, "enargite")
                .dust().ore(2, 1)
                .color(0xBBBBBB).iconSet(METALLIC)
                .components(Copper, 3, Arsenic, 1, Sulfur, 4)
                .addOreByproducts(Pyrite, Zinc, Quartzite)
                .build();

        Tennantite = new Material.Builder(225, "tennantite")
                .dust().ore(2, 1)
                .color(0x909090).iconSet(METALLIC)
                .components(Copper, 12, Arsenic, 4, Sulfur, 13)
                .addOreByproducts(Iron, Antimony, Zinc)
                .build();

        GalliumArsenide = new Material.Builder(226, "gallium_arsenide")
                .ingot(1).fluid()
                .color(0xA0A0A0)
                .flags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Gallium, 1, Arsenic, 1)
                .blastTemp(1200)
                .build();

        Potash = new Material.Builder(227, "potash")
                .dust(1)
                .color(0x784137)
                .components(Potassium, 2, Oxygen, 1)
                .build();

        SodaAsh = new Material.Builder(288, "soda_ash")
                .dust(1)
                .color(0xDCDCFF)
                .components(Sodium, 2, Carbon, 1, Oxygen, 3)
                .build();

        IndiumGalliumPhosphide = new Material.Builder(229, "indium_gallium_phosphide")
                .ingot(1).fluid()
                .color(0xA08CBE)
                .flags(STD_METAL, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Indium, 1, Gallium, 1, Phosphorus, 1)
                .build();

        NickelZincFerrite = new Material.Builder(230, "nickel_zinc_ferrite")
                .ingot(0).fluid()
                .color(0x3C3C3C).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_RING)
                .components(Nickel, 1, Zinc, 1, Iron, 4)
                .build();

        SiliconDioxide = new Material.Builder(232, "silicon_dioxide")
                .dust(1)
                .color(0xC8C8C8).iconSet(QUARTZ)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE)
                .components(Silicon, 1, Oxygen, 2)
                .build();

        MagnesiumChloride = new Material.Builder(232, "magnesium_chloride")
                .dust(1)
                .color(0xD40D5C)
                .components(Magnesium, 1, Chlorine, 2)
                .build();

        SodiumSulfide = new Material.Builder(233, "sodium_sulfide")
                .dust(1)
                .color(0xFFE680)
                .components(Sodium, 2, Sulfur, 1)
                .build();

        PhosphorusPentoxide = new Material.Builder(234, "phosphorus_pentoxide")
                .dust(1)
                .color(0xDCDC00)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Phosphorus, 4, Oxygen, 10)
                .build();

        Quicklime = new Material.Builder(235, "quicklime")
                .dust(1)
                .color(0xF0F0F0)
                .components(Calcium, 1, Oxygen, 1)
                .build();

        SodiumBisulfate = new Material.Builder(236, "sodium_bisulfate")
                .dust(1)
                .color(0x004455)
                .flags(DISABLE_DECOMPOSITION)
                .components(Sodium, 1, Hydrogen, 1, Sulfur, 1, Oxygen, 4)
                .build();

        FerriteMixture = new Material.Builder(237, "ferrite_mixture")
                .dust(1)
                .color(0xB4B4B4).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Nickel, 1, Zinc, 1, Iron, 4)
                .build();

        Magnesia = new Material.Builder(238, "magnesia")
                .dust(1)
                .color(0x887878)
                .components(Magnesium, 1, Oxygen, 1)
                .build();

        PlatinumGroupSludge = new Material.Builder(239, "platinum_group_sludge")
                .dust(1)
                .color(0x001E00).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .build();

        RealGar = new Material.Builder(240, "realgar")
                .dust(2)
                .color(0x8C6464)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Arsenic, 4, Sulfur, 4)
                .build();

        SodiumBicarbonate = new Material.Builder(241, "sodium_bicarbonate")
                .dust(1)
                .color(0x565b96).iconSet(ROUGH)
                .components(Sodium, 1, Hydrogen, 1, Carbon, 1, Oxygen, 5)
                .build();

        PotassiumDichromate = new Material.Builder(242, "potassium_dichromate")
                .dust(1)
                .color(0xFF084E)
                .components(Potassium, 2, Chrome, 2, Oxygen, 7)
                .build();

        ChromiumTrioxide = new Material.Builder(243, "chromium_trioxide")
                .dust(1)
                .color(0xFFE4E1)
                .components(Chrome, 1, Oxygen, 3)
                .build();

        AntimonyTrioxide = new Material.Builder(244, "antimony_trioxide")
                .dust(1)
                .color(0xE6E6F0)
                .components(Antimony, 2, Oxygen, 3)
                .build();

        Zincite = new Material.Builder(245, "zincite")
                .dust(1)
                .color(0xFFFFF5)
                .components(Zinc, 1, Oxygen, 1)
                .build();

        CupricOxide = new Material.Builder(246, "cupric_oxide")
                .dust(1)
                .color(0x0F0F0F)
                .components(Copper, 1, Oxygen, 1)
                .build();

        CobaltOxide = new Material.Builder(247, "cobalt_oxide")
                .dust(1)
                .color(0x788000)
                .components(Cobalt, 1, Oxygen, 1)
                .build();

        ArsenicTrioxide = new Material.Builder(248, "arsenic_tioxide")
                .dust(1)
                .iconSet(ROUGH)
                .components(Arsenic, 2, Oxygen, 3)
                .build();

        Massicot = new Material.Builder(249, "massicot")
                .dust(1)
                .color(0xFFDD55)
                .components(Lead, 1, Oxygen, 1)
                .build();

        Ferrosilite = new Material.Builder(250, "ferrosilite")
                .dust(1)
                .color(0x97632A)
                .components(Iron, 1, Silicon, 1, Oxygen, 3)
                .build();

        MetalMixture = new Material.Builder(251, "metal_mixture")
                .dust(1)
                .color(0x502d16).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .build();

        SodiumHydroxide = new Material.Builder(252, "sodium_hydroxide")
                .dust(1)
                .color(0x003380)
                .components(Sodium, 1, Oxygen, 1, Hydrogen, 1)
                .build();

        SodiumPersulfate = new Material.Builder(253, "sodium_persulfate")
                .fluid()
                .components(Sodium, 2, Sulfur, 2, Oxygen, 8)
                .build();

        Bastnasite = new Material.Builder(254, "bastnasite")
                .dust().ore(2, 1)
                .color(0xC86E2D).iconSet(FINE)
                .components(Cerium, 1, Carbon, 1, Fluorine, 1, Oxygen, 3)
                .addOreByproducts(Neodymium, RareEarth)
                .separatedInto(Neodymium)
                .build();

        Pentlandite = new Material.Builder(255, "pentlandite")
                .dust().ore()
                .color(0xA59605)
                .components(Nickel, 9, Sulfur, 8)
                .addOreByproducts(Iron, Sulfur, Cobalt)
                .separatedInto(Iron)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Nickel)
                .build();

        Spodumene = new Material.Builder(256, "spodumene")
                .dust().ore(2, 1)
                .color(0xBEAAAA)
                .components(Lithium, 1, Aluminium, 1, Silicon, 2, Oxygen, 6)
                .addOreByproducts(Aluminium, Lithium)
                .build();

        Lepidolite = new Material.Builder(257, "lepidolite")
                .dust().ore(5, 1)
                .color(0xF0328C).iconSet(FINE)
                .components(Potassium, 1, Lithium, 3, Aluminium, 4, Fluorine, 2, Oxygen, 10)
                .addOreByproducts(Lithium, Caesium, Boron)
                .build();

        Glauconite = new Material.Builder(258, "glauconite")
                .dust().ore()
                .color(0x82B43C)
                .components(Potassium, 1, Magnesium, 2, Aluminium, 4, Hydrogen, 2, Oxygen, 12)
                .addOreByproducts(Sodium, Aluminium, Iron)
                .separatedInto(Iron)
                .build();

        GlauconiteSand = new Material.Builder(259, "glauconite_sand")
                .dust()
                .color(0x82B43C).iconSet(SAND)
                .components(Potassium, 1, Magnesium, 2, Aluminium, 4, Hydrogen, 2, Oxygen, 12)
                .addOreByproducts(Sodium, Aluminium, Iron)
                .separatedInto(Iron)
                .build();

        Malachite = new Material.Builder(260, "malachite")
                .dust().ore()
                .color(0x055F05)
                .components(Copper, 2, Carbon, 1, Hydrogen, 2, Oxygen, 5)
                .addOreByproducts(Copper, BrownLimonite, Calcite, Copper)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Copper)
                .build();

        Mica = new Material.Builder(261, "mica")
                .dust()
                .color(0xC3C3CD).iconSet(FINE)
                .components(Potassium, 1, Aluminium, 3, Silicon, 3, Fluorine, 2, Oxygen, 10)
                .build();

        Barite = new Material.Builder(262, "barite")
                .dust().ore()
                .color(0xE6EBEB)
                .components(Barium, 1, Sulfur, 1, Oxygen, 4)
                .build();

        Alunite = new Material.Builder(263, "alunite")
                .dust()
                .color(0xE1B441).iconSet(METALLIC)
                .components(Potassium, 1, Aluminium, 3, Silicon, 2, Hydrogen, 6, Oxygen, 14)
                .build();

        Dolomite = new Material.Builder(264, "dolomite")
                .dust(1)
                .color(0xE1CDCD).iconSet(FLINT)
                .components(Calcium, 1, Magnesium, 1, Carbon, 2, Oxygen, 6)
                .build();

        Wollastonite = new Material.Builder(265, "wollastonite")
                .dust()
                .color(0xF0F0F0)
                .components(Calcium, 1, Silicon, 1, Oxygen, 3)
                .build();

        Kaolinite = new Material.Builder(267, "kaolinite")
                .dust()
                .color(0xF3EBEB)
                .components(Aluminium, 2, Silicon, 2, Hydrogen, 4, Oxygen, 9)
                .build();

        Talc = new Material.Builder(268, "talc")
                .dust().ore()
                .color(0x5AB45A).iconSet(FINE)
                .components(Magnesium, 3, Silicon, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        Soapstone = new Material.Builder(269, "soapstone")
                .dust(1).ore(3, 1)
                .color(0x5F915F)
                .components(Magnesium, 3, Silicon, 4, Hydrogen, 2, Oxygen, 12)
                .build();

        Kyanite = new Material.Builder(270, "kyanite")
                .dust()
                .color(0x6E6EFA).iconSet(FLINT)
                .components(Aluminium, 2, Silicon, 1, Oxygen, 5)
                .build();

        IronMagnetic = new Material.Builder(271, "iron_magnetic")
                .ingot().fluid()
                .color(0xC8C8C8).iconSet(MAGNETIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .components(Iron, 1)
                .build();

        TungstenCarbide = new Material.Builder(272, "tungsten_carbide")
                .ingot().fluid()
                .color(0x330066).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Tungsten, 1, Carbon, 1)
                .toolStats(12.0f, 4.0f, 1280)
                .fluidPipeProperties(7568, 125, true)
                .blastTemp(2460)
                .build();

    }
}
