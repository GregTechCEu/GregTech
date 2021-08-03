package gregtech.api.unification.material.materials;

import gregtech.api.GTValues;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraft.init.Enchantments;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;

public class MaterialFlagAddition {

    public static void register() {
        OreProperty prop = Aluminium.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Bauxite);

        prop = Antimony.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Zinc, Iron, Zinc);
        prop.setSeparatedInto(Iron);
        prop.setWashedIn(SodiumPersulfate);

        prop = Beryllium.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Emerald);

        prop = Chrome.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Iron, Magnesium);
        prop.setSeparatedInto(Iron);

        prop = Cobalt.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Cobaltite);
        prop.setWashedIn(SodiumPersulfate);

        prop = Copper.getProperty(PropertyKey.ORE);
        prop.setOreByProducts(Cobalt, Gold, Nickel, Gold);
        prop.setWashedIn(Mercury);

        Copper = new Material.Builder(25, "copper")
                .ingot(1).fluid().ore()
                .color(0xFF6400).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_DENSE, GENERATE_SPRING)
                .element(Elements.Cu)
                .washedIn(Mercury)
                .arcSmeltInto(AnnealedCopper)
                .addOreByproducts(Cobalt, Gold, Nickel, Gold)
                .cableProperties(GTValues.V[2], 1, 2)
                .fluidPipeProperties(1696, 10, true)
                .build();

        Gold = new Material.Builder(41, "gold")
                .ingot().fluid().ore()
                .color(0xFFFF1E).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_RING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .element(Elements.Au)
                .washedIn(Mercury)
                .addOreByproducts(Copper, Nickel, Gold)
                .cableProperties(GTValues.V[3], 2, 2)
                .fluidPipeProperties(1671, 35, true)
                .build();

        Iridium = new Material.Builder(50, "iridium")
                .ingot(3).fluid().ore()
                .color(0xF0F0F5)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE)
                .element(Elements.Ir)
                .toolStats(7.0f, 3.0f, 2560)
                .washedIn(Mercury)
                .addOreByproducts(Platinum, Osmium, Platinum)
                .fluidPipeProperties(3398, 140, true)
                .blastTemp(2719)
                .build();

        Iron = new Material.Builder(51, "iron")
                .ingot().fluid().plasma().ore()
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_DENSE, GENERATE_FRAME, GENERATE_ROTOR, GENERATE_SMALL_GEAR,
                        GENERATE_SPRING, GENERATE_SPRING_SMALL, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, BLAST_FURNACE_CALCITE_TRIPLE)
                .element(Elements.Fe)
                .toolStats(7.0f, 2.5f, 256)
                .washedIn(SodiumPersulfate)
                .polarizesInto(IronMagnetic)
                .arcSmeltInto(WroughtIron)
                .addOreByproducts(Nickel, Tin, Nickel)
                .cableProperties(GTValues.V[2], 2, 3)
                .build();

        Lead = new Material.Builder(55, "lead")
                .ingot(1).fluid().ore()
                .color(0x8C648C)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_DENSE)
                .element(Elements.Pb)
                .washedIn(Mercury)
                .addOreByproducts(Silver, Sulfur, Silver)
                .cableProperties(GTValues.V[1], 2, 2)
                .fluidPipeProperties(1200, 15, true)
                .build();

        Lithium = new Material.Builder(56, "lithium")
                .ingot().fluid().ore()
                .color(0xE1DCE1)
                .flags(STD_METAL)
                .element(Elements.Li)
                .addOreByproducts(Lithium)
                .build();

        Magnesium = new Material.Builder(59, "magnesium")
                .ingot().fluid()
                .color(0xE1C8C8).iconSet(METALLIC)
                .element(Elements.Mg)
                .addOreByproducts(Olivine)
                .build();

        Manganese = new Material.Builder(61, "manganese")
                .ingot().fluid()
                .color(0xFAFAFA)
                .flags(STD_METAL, GENERATE_FOIL)
                .element(Elements.Mn)
                .addOreByproducts(Chrome, Iron)
                .separatedInto(Iron)
                .toolStats(7.0f, 2.0f, 512)
                .build();

        Neodymium = new Material.Builder(66, "neodymium")
                .ingot().fluid().ore()
                .color(0x646464).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_ROD)
                .element(Elements.Nd)
                .toolStats(7.0f, 2.0f, 512)
                .polarizesInto(NeodymiumMagnetic)
                .addOreByproducts(Monazite, RareEarth)
                .blastTemp(1297)
                .build();

        Nickel = new Material.Builder(69, "nickel")
                .ingot().fluid().plasma().ore()
                .color(0xC8C8FA).iconSet(METALLIC)
                .flags(STD_METAL, MORTAR_GRINDABLE)
                .element(Elements.Ni)
                .separatedInto(Iron)
                .washedIn(Mercury)
                .addOreByproducts(Cobalt, Platinum, Iron, Platinum)
                .cableProperties(GTValues.V[2], 3, 3)
                .itemPipeProperties(2048, 1.0f)
                .build();

        Osmium = new Material.Builder(75, "osmium")
                .ingot(4).fluid().ore()
                .color(0x3232FF).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE)
                .element(Elements.Os)
                .toolStats(16.0f, 4.0f, 1280)
                .washedIn(Mercury)
                .addOreByproducts(Iridium, Osmium)
                .cableProperties(GTValues.V[5], 4, 2)
                .itemPipeProperties(256, 8.0f)
                .blastTemp(3306)
                .build();

        Platinum = new Material.Builder(80, "platinum")
                .ingot().fluid(Material.FluidType.GAS, true).ore()
                .color(0xFFFFC8).iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Pt)
                .washedIn(Mercury)
                .cableProperties(GTValues.V[5], 2, 1)
                .itemPipeProperties(512, 4.0f)
                .addOreByproducts(Nickel, Iridium)
                .build();

        Plutonium239 = new Material.Builder(81, "plutonium")
                .ingot(3).fluid()
                .color(0xF03232).iconSet(METALLIC)
                .flags(EXT_METAL)
                .element(Elements.Pu239)
                //.addOreByproducts(Uranium238, Lead)
                .build();

        Silicon = new Material.Builder(99, "silicon")
                .ingot().fluid()
                .color(0x3C3C50).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_FOIL)
                .element(Elements.Si)
                //.addOreByproducts(SiliconDioxide)
                .blastTemp(1687)
                .build();

        Silver = new Material.Builder(100, "silver")
                .ingot().fluid().ore()
                .color(0xDCDCFF).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .element(Elements.Ag)
                .washedIn(Mercury)
                .addOreByproducts(Lead, Sulfur, Silver)
                .cableProperties(GTValues.V[3], 1, 1)
                .build();

        Sulfur = new Material.Builder(103, "sulfur")
                .dust().ore()
                .color(0xC8C800)
                .flags(FLAMMABLE)
                .element(Elements.S)
                .addOreByproducts(Sulfur)
                .build();

        Thorium = new Material.Builder(109, "thorium")
                .ingot().fluid().ore()
                .color(0x001E00).iconSet(SHINY)
                .flags(STD_METAL)
                .element(Elements.Th)
                .toolStats(6.0f, 2.0f, 512)
                .addOreByproducts(Uranium238, Lead)
                .build();

        Tin = new Material.Builder(112, "tin")
                .ingot(1).fluid().ore()
                .color(0xDCDCDC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR)
                .element(Elements.Sn)
                .separatedInto(Iron)
                .washedIn(SodiumPersulfate)
                .addOreByproducts(Iron, Zinc)
                .cableProperties(GTValues.V[1], 1, 1)
                .itemPipeProperties(4096, 0.5f)
                .build();

        Titanium = new Material.Builder(113, "titanium") // todo Ore?
                .ingot(3).fluid()
                .color(0xDCA0F0).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_SPRING, GENERATE_FRAME, GENERATE_DENSE)
                .element(Elements.Ti)
                .toolStats(7.0f, 3.0f, 1600)
                //.addOreByproducts(Almandine)
                .cableProperties(GTValues.V[4], 4, 2)
                .fluidPipeProperties(2426, 80, true)
                .blastTemp(1941)
                .build();

        Tungsten = new Material.Builder(115, "tungsten")
                .ingot(3).fluid()
                .color(0x323232).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.W)
                .toolStats(7.0f, 3.0f, 2560)
                //.addOreByproducts(Manganese, Molybdenum)
                .cableProperties(GTValues.V[5], 2, 2)
                .fluidPipeProperties(4618, 90, true)
                .blastTemp(3000)
                .build();

        Uranium238 = new Material.Builder(116, "uranium")
                .ingot(3).fluid().ore()
                .color(0x32F032).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.U238)
                .toolStats(6.0f, 3.0f, 512)
                .addOreByproducts(Lead, Uranium235, Thorium)
                .build();

        Zinc = new Material.Builder(122, "zinc")
                .ingot(1).fluid().ore()
                .color(0xFAF0F0).iconSet(METALLIC)
                .flags(STD_METAL, MORTAR_GRINDABLE, GENERATE_FOIL, GENERATE_RING)
                .element(Elements.Zn)
                .washedIn(SodiumPersulfate)
                .addOreByproducts(Tin, Gallium)
                .cableProperties(GTValues.V[1], 1, 1)
                .build();

        Naquadah = new Material.Builder(124, "naquadah")
                .ingot(4).fluid().ore()
                .color(0x323232).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FOIL, GENERATE_SPRING)
                .element(Elements.Nq)
                .toolStats(6.0f, 4.0f, 1280)
                .addOreByproducts(NaquadahEnriched)
                .cableProperties(GTValues.V[7], 2, 2)
                .fluidPipeProperties(19200, 1500, true)
                .blastTemp(5400)
                .build();

        NaquadahEnriched = new Material.Builder(125, "naquadah_enriched")
                .ingot(4).fluid().ore()
                .color(0x323232).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FOIL)
                .element(Elements.Nq1)
                .toolStats(6.0f, 4.0f, 1280)
                .addOreByproducts(Naquadah, Naquadria)
                .blastTemp(4500)
                .build();

        Almandine = new Material.Builder(250, "almandine")
                .gem(1).ore(6, 1)
                .color(0xFF0000)
                .flags(STD_GEM)
                .components(Aluminium, 2, Iron, 3, Silicon, 3, Oxygen, 12)
                .addOreByproducts(GarnetRed, Aluminium)
                .build();

        Andradite = new Material.Builder(251, "andradite")
                .dust(1)
                .color(0x967800).iconSet(ROUGH)
                .components(Calcium, 3, Iron, 2, Silicon, 3, Oxygen, 12)
                .addOreByproducts(GarnetYellow, Iron)
                .separatedInto(Iron)
                .build();

        Asbestos = new Material.Builder(253, "asbestos")
                .dust(1)
                .color(0xE6E6E6)
                .components(Magnesium, 3, Silicon, 2, Hydrogen, 4, Oxygen, 9)
                .addOreByproducts(Asbestos, Silicon, Magnesium)
                .build();

        BlueTopaz = new Material.Builder(257, "blue_topaz")
                .gem(3).ore()
                .color(0x0000FF).iconSet(GEM_HORIZONTAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Silicon, 1, Fluorine, 2, Hydrogen, 2, Oxygen, 6)
                .toolStats(7.0f, 3.0f, 256)
                .addOreByproducts(Topaz)
                .build();

        BrownLimonite = new Material.Builder(261, "brown_limonite")
                .dust(1).ore()
                .color(0xC86400).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, BLAST_FURNACE_CALCITE_TRIPLE)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .addOreByproducts(Malachite, YellowLimonite)
                .separatedInto(Iron)
                .oreSmeltInto(Iron)
                .build();

        Calcite = new Material.Builder(262, "calcite")
                .dust(1).ore()
                .color(0xFAE6DC)
                .components(Calcium, 1, Carbon, 1, Oxygen, 3)
                .addOreByproducts(Andradite, Malachite)
                .build();

        Cassiterite = new Material.Builder(263, "cassiterite")
                .dust(1).ore(2, 1)
                .color(0xDCDCDC).iconSet(METALLIC)
                .components(Tin, 1, Oxygen, 2)
                .addOreByproducts(Tin, Bismuth)
                .oreSmeltInto(Tin)
                .build();

        CassiteriteSand = new Material.Builder(264, "cassiterite_sand")
                .dust(1).ore(2, 1)
                .color(0xDCDCDC).iconSet(SAND)
                .components(Tin, 1, Oxygen, 2)
                .addOreByproducts(Tin)
                .oreSmeltInto(Tin)
                .build();

        Chalcopyrite = new Material.Builder(265, "chalcopyrite")
                .dust(1).ore()
                .color(0xA07828)
                .components(Copper, 1, Iron, 1, Sulfur, 2)
                .addOreByproducts(Pyrite, Cobalt, Cadmium, Gold)
                .washedIn(Mercury)
                .oreSmeltInto(Copper)
                .build();

        Chromite = new Material.Builder(267, "chromite")
                .dust(1).ore()
                .color(0x23140F).iconSet(METALLIC)
                .components(Iron, 1, Chrome, 2, Oxygen, 4)
                .addOreByproducts(Iron, Magnesium)
                .separatedInto(Iron)
                .oreSmeltInto(Chrome)
                .build();

        Cinnabar = new Material.Builder(268, "cinnabar")
                .gem(1).ore()
                .color(0x960000).iconSet(EMERALD)
                .flags(CRYSTALLIZABLE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Mercury, 1, Sulfur, 1)
                .addOreByproducts(Redstone, Sulfur, Glowstone)
                .build();

        Coal = new Material.Builder(271, "coal")
                .gem(1, 1600).ore() //default coal burn time in vanilla
                .color(0x464646).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .addOreByproducts(Lignite, Thorium)
                .build();

        Cobaltite = new Material.Builder(272, "cobaltite")
                .dust(1).ore()
                .color(0x5050FA).iconSet(METALLIC)
                .components(Cobalt, 1, Arsenic, 1, Sulfur, 1)
                .addOreByproducts(Cobalt, Cobaltite)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Cobalt)
                .build();

        Cooperite = new Material.Builder(273, "cooperite")
                .dust(1).ore()
                .color(0xFFFFC8).iconSet(METALLIC)
                .components(Platinum, 3, Nickel, 1, Sulfur, 1, Palladium, 1)
                .addOreByproducts(Palladium, Nickel, Iridium, Cooperite)
                .washedIn(Mercury)
                .oreSmeltInto(Platinum)
                .build();

        Diamond = new Material.Builder(276, "diamond")
                .gem(3).ore()
                .color(0xC8FFFF).iconSet(DIAMOND)
                .flags(GENERATE_BOLT_SCREW, GENERATE_LENS, GENERATE_GEAR, NO_SMASHING, NO_SMELTING, FLAMMABLE, GENERATE_BOLT_SCREW,
                        HIGH_SIFTER_OUTPUT, DISABLE_DECOMPOSITION, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .toolStats(8.0f, 3.0f, 1280)
                .addOreByproducts(Graphite)
                .build();

        Emerald = new Material.Builder(278, "emerald")
                .gem().ore()
                .color(0x50FF50).iconSet(EMERALD)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Beryllium, 3, Aluminium, 2, Silicon, 6, Oxygen, 18)
                .toolStats(10.0f, 2.0f, 368)
                .addOreByproducts(Beryllium, Aluminium)
                .build();

        Galena = new Material.Builder(279, "galena")
                .dust(3).ore()
                .color(0x643C64)
                .flags(NO_SMELTING)
                .components(Lead, 3, Silver, 3, Sulfur, 2)
                .addOreByproducts(Sulfur, Silver, Lead, Silver)
                .washedIn(Mercury)
                .oreSmeltInto(Lead)
                .build();

        Garnierite = new Material.Builder(280, "garnierite")
                .dust(3).ore()
                .color(0x32C846).iconSet(METALLIC)
                .components(Nickel, 1, Oxygen, 1)
                .addOreByproducts(Nickel)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Nickel)
                .build();

        GreenSapphire = new Material.Builder(281, "green_sapphire")
                .gem().ore()
                .color(0x64C882).iconSet(GEM_HORIZONTAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, GENERATE_LENS)
                .components(Aluminium, 2, Oxygen, 3)
                .toolStats(8.0f, 3.0f, 368)
                .addOreByproducts(Aluminium, Sapphire)
                .build();

        Grossular = new Material.Builder(282, "grossular")
                .dust(1).ore(6, 1)
                .color(0xC86400).iconSet(ROUGH)
                .components(Calcium, 3, Aluminium, 2, Silicon, 3, Oxygen, 12)
                .addOreByproducts(GarnetYellow, Calcium)
                .build();

        Ilmenite = new Material.Builder(284, "ilmenite")
                .dust(3).ore(3, 1)
                .color(0x463732).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iron, 1, Titanium, 1, Oxygen, 3)
                .addOreByproducts(Iron, Rutile)
                .separatedInto(Iron)
                .build();

        Bauxite = new Material.Builder(286, "bauxite")
                .dust(1).ore(3, 1)
                .color(0xC86400)
                .flags(DISABLE_DECOMPOSITION)
                .components(Rutile, 2, Aluminium, 16, Hydrogen, 10, Oxygen, 11)
                .addOreByproducts(Grossular, Rutile, Gallium)
                .build();

        Lazurite = new Material.Builder(289, "lazurite")
                .gem(1).ore(6, 4)
                .color(0x6478FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, GENERATE_ROD, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 6, Silicon, 6, Calcium, 8, Sodium, 8)
                .addOreByproducts(Sodalite, Lapis)
                .build();

        Magnesite = new Material.Builder(291, "magnesite")
                .dust().ore()
                .color(0xFAFAB4).iconSet(METALLIC)
                .components(Magnesium, 1, Carbon, 1, Oxygen, 3)
                .addOreByproducts(Magnesium)
                .oreSmeltInto(Magnesium)
                .build();

        Magnetite = new Material.Builder(292, "magnetite")
                .dust().ore()
                .color(0x1E1E1E).iconSet(METALLIC)
                .components(Iron, 3, Oxygen, 4)
                .addOreByproducts(Iron, Gold)
                .separatedInto(Gold)
                .washedIn(Mercury)
                .oreSmeltInto(Iron)
                .build();

        Molybdenite = new Material.Builder(293, "molybdenite")
                .dust().ore()
                .color(0x191919).iconSet(METALLIC)
                .components(Molybdenum, 1, Sulfur, 2)
                .addOreByproducts(Molybdenum)
                .oreSmeltInto(Molybdenum)
                .build();

        Phosphate = new Material.Builder(298, "phosphate")
                .dust(1).ore()
                .color(0xFFFF00)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE, EXPLOSIVE)
                .components(Phosphorus, 1, Oxygen, 4)
                .addOreByproducts(Phosphorus)
                .build();

        Pyrite = new Material.Builder(306, "pyrite")
                .dust(1).ore()
                .color(0x967828).iconSet(ROUGH)
                .flags(BLAST_FURNACE_CALCITE_DOUBLE)
                .components(Iron, 1, Sulfur, 2)
                .addOreByproducts(Sulfur, TricalciumPhosphate, Iron)
                .separatedInto(Iron)
                .oreSmeltInto(Iron)
                .build();

        Pyrolusite = new Material.Builder(307, "pyrolusite")
                .dust().ore()
                .color(0x9696AA)
                .components(Manganese, 1, Oxygen, 2)
                .addOreByproducts(Manganese)
                .oreSmeltInto(Manganese)
                .build();

        Pyrope = new Material.Builder(308, "pyrope")
                .dust().ore(4, 1)
                .color(0x783264).iconSet(METALLIC)
                .components(Aluminium, 2, Magnesium, 3, Silicon, 3, Oxygen, 12)
                .addOreByproducts(GarnetRed, Magnesium)
                .build();

        RockSalt = new Material.Builder(309, "rock_salt")
                .dust(1).ore(2, 1)
                .color(0xF0C8C8).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Potassium, 1, Chlorine, 1)
                .addOreByproducts(Salt, Borax)
                .build();

        Ruby = new Material.Builder(311, "ruby")
                .gem().ore()
                .color(0xFF6464).iconSet(RUBY)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Chrome, 1, Aluminium, 2, Oxygen, 3)
                .toolStats(8.5f, 3.0f, 256)
                .addOreByproducts(Chrome, GarnetRed)
                .build();

        Salt = new Material.Builder(312, "salt")
                .dust(1).ore(2, 1)
                .color(0xFAFAFA).iconSet(FINE)
                .flags(NO_SMASHING)
                .components(Sodium, 1, Chlorine, 1)
                .addOreByproducts(RockSalt, Borax)
                .build();

        Saltpeter = new Material.Builder(313, "saltpeter")
                .dust(1).ore(4, 1)
                .color(0xE6E6E6).iconSet(FINE)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE)
                .components(Potassium, 1, Nitrogen, 1, Oxygen, 3)
                .addOreByproducts(Saltpeter)
                .build();

        Sapphire = new Material.Builder(314, "sapphire")
                .gem().ore()
                .color(0x6464C8).iconSet(GEM_VERTICAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Oxygen, 3)
                .toolStats(7.5f, 4.0f, 256)
                .addOreByproducts(Aluminium, GreenSapphire)
                .build();

        Scheelite = new Material.Builder(315, "scheelite")
                .dust(3).ore(2, 1)
                .color(0xC88C14)
                .flags(DECOMPOSITION_REQUIRES_HYDROGEN)
                .components(Tungsten, 1, Calcium, 2, Oxygen, 4)
                .addOreByproducts(Manganese, Molybdenum, Calcium)
                .build();

        Sodalite = new Material.Builder(316, "sodalite")
                .gem(1).ore(6, 4)
                .color(0x1414FF).iconSet(LAPIS)
                .flags(GENERATE_PLATE, GENERATE_ROD, NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, DECOMPOSITION_BY_ELECTROLYZING)
                .components(Aluminium, 3, Silicon, 3, Sodium, 4, Chlorine, 1)
                .addOreByproducts(Lazurite, Lapis)
                .build();

        Tantalite = new Material.Builder(318, "tantalite")
                .dust(3).ore(2, 1)
                .color(0x915028).iconSet(METALLIC)
                .components(Manganese, 1, Tantalum, 2, Oxygen, 6)
                .addOreByproducts(Manganese, Niobium, Tantalum)
                .build();

        Spessartine = new Material.Builder(321, "spessartine")
                .dust().ore(2, 1)
                .color(0xFF6464)
                .components(Aluminium, 2, Manganese, 3, Silicon, 3, Oxygen, 12)
                .addOreByproducts(GarnetRed, Manganese)
                .build();

        Sphalerite = new Material.Builder(322, "sphalerite")
                .dust(1).ore()
                .color(0xFFFFFF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Zinc, 1, Sulfur, 1)
                .addOreByproducts(GarnetYellow, Cadmium, Gallium, Zinc)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Zinc)
                .build();

        Stibnite = new Material.Builder(325, "stibnite")
                .dust().ore()
                .color(0x464646).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Antimony, 2, Sulfur, 3)
                .addOreByproducts(Antimony)
                .oreSmeltInto(Antimony)
                .build();

        Tanzanite = new Material.Builder(326, "tanzanite")
                .gem().ore(2, 1)
                .color(0x4000C8).iconSet(GEM_VERTICAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Calcium, 2, Aluminium, 3, Silicon, 3, Hydrogen, 1)
                .toolStats(7.0f, 2.0f, 256)
                .addOreByproducts(Opal)
                .build();

        Tetrahedrite = new Material.Builder(327, "tetrahedrite")
                .dust().ore()
                .color(0xC82000)
                .components(Copper, 3, Antimony, 1, Sulfur, 3, Iron, 1)
                .addOreByproducts(Antimony, Zinc, Tetrahedrite)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Copper)
                .build();

        Topaz = new Material.Builder(329, "topaz")
                .gem(3).ore(2, 1)
                .color(0xFF8000).iconSet(GEM_HORIZONTAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Aluminium, 2, Silicon, 1, Fluorine, 1, Hydrogen, 2)
                .toolStats(7.0f, 2.0f, 256)
                .addOreByproducts(BlueTopaz)
                .build();

        Tungstate = new Material.Builder(330, "tungstate")
                .dust(3).ore(2, 1)
                .color(0x373223)
                .flags(DECOMPOSITION_REQUIRES_HYDROGEN)
                .components(Tungsten, 1, Lithium, 2, Oxygen, 4)
                .addOreByproducts(Manganese, Silver, Lithium, Silver)
                .washedIn(Mercury)
                .build();

        Uraninite = new Material.Builder(332, "uraninite")
                .dust(3).ore()
                .color(0x232323).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Uranium238, 1, Oxygen, 2)
                .addOreByproducts(Uranium238, Thorium, Uranium235)
                .build()
                .setFormula("UO2", true);

        Uvarovite = new Material.Builder(333, "uvarovite")
                .dust()
                .color(0xB4FFB4).iconSet(DIAMOND)
                .components(Calcium, 3, Chrome, 2, Silicon, 3, Oxygen, 12)
                .addOreByproducts(GarnetYellow, Chrome)
                .build();

        YellowLimonite = new Material.Builder(337, "yellow_limonite")
                .dust().ore()
                .color(0xC8C800).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, BLAST_FURNACE_CALCITE_DOUBLE)
                .components(Iron, 1, Hydrogen, 1, Oxygen, 2)
                .addOreByproducts(Nickel, BrownLimonite, Cobalt, Nickel)
                .separatedInto(Iron)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Iron)
                .build();

        NetherQuartz = new Material.Builder(339, "nether_quartz")
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

        Quartzite = new Material.Builder(340, "quartzite")
                .gem(1).ore(2, 1)
                .color(0xD2E6D2).iconSet(QUARTZ)
                .flags(NO_SMELTING, CRYSTALLIZABLE, DISABLE_DECOMPOSITION, GENERATE_PLATE)
                .components(Silicon, 1, Oxygen, 2)
                .addOreByproducts(CertusQuartz, Barite)
                .build();

        Graphite = new Material.Builder(341, "graphite")
                .ingot().ore().fluid()
                .color(0x808080)
                .flags(STD_METAL, NO_SMELTING, FLAMMABLE, DISABLE_DECOMPOSITION)
                .components(Carbon, 1)
                .addOreByproducts(Carbon)
                .build();

        Tenorite = new Material.Builder(345, "tenorite")
                .dust(1).ore()
                .color(0x606060)
                .components(Copper, 1, Oxygen, 1)
                .addOreByproducts(Iron, Manganese, Malachite)
                .oreSmeltInto(Copper)
                .build();

        Cuprite = new Material.Builder(346, "cuprite")
                .dust().ore()
                .color(0x770000).iconSet(RUBY)
                .components(Copper, 2, Oxygen, 1)
                .addOreByproducts(Iron, Antimony, Malachite)
                .oreSmeltInto(Copper)
                .build();

        Bornite = new Material.Builder(347, "bornite")
                .dust(1).ore()
                .color(0x97662B).iconSet(METALLIC)
                .components(Copper, 5, Iron, 1, Sulfur, 4)
                .addOreByproducts(Pyrite, Cobalt, Cadmium, Gold)
                .washedIn(Mercury)
                .oreSmeltInto(Copper)
                .build();

        Chalcocite = new Material.Builder(348, "chalcocite")
                .dust().ore()
                .color(0x353535).iconSet(GEM_VERTICAL)
                .components(Copper, 2, Sulfur, 1)
                .addOreByproducts(Sulfur, Lead, Silver)
                .oreSmeltInto(Copper)
                .build();

        Enargite = new Material.Builder(349, "enargite")
                .dust().ore(2, 1)
                .color(0xBBBBBB).iconSet(METALLIC)
                .components(Copper, 3, Arsenic, 1, Sulfur, 4)
                .addOreByproducts(Pyrite, Zinc, Quartzite)
                .build();

        Tennantite = new Material.Builder(350, "tennantite")
                .dust().ore(2, 1)
                .color(0x909090).iconSet(METALLIC)
                .components(Copper, 12, Arsenic, 4, Sulfur, 13)
                .addOreByproducts(Iron, Antimony, Zinc)
                .build();

        Bastnasite = new Material.Builder(379, "bastnasite")
                .dust().ore(2, 1)
                .color(0xC86E2D).iconSet(FINE)
                .components(Cerium, 1, Carbon, 1, Fluorine, 1, Oxygen, 3)
                .addOreByproducts(Neodymium, RareEarth)
                .separatedInto(Neodymium)
                .build();

        Pentlandite = new Material.Builder(380, "pentlandite")
                .dust().ore()
                .color(0xA59605)
                .components(Nickel, 9, Sulfur, 8)
                .addOreByproducts(Iron, Sulfur, Cobalt)
                .separatedInto(Iron)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Nickel)
                .build();

        Spodumene = new Material.Builder(381, "spodumene")
                .dust().ore(2, 1)
                .color(0xBEAAAA)
                .components(Lithium, 1, Aluminium, 1, Silicon, 2, Oxygen, 6)
                .addOreByproducts(Aluminium, Lithium)
                .build();

        Lepidolite = new Material.Builder(382, "lepidolite")
                .dust().ore(5, 1)
                .color(0xF0328C).iconSet(FINE)
                .components(Potassium, 1, Lithium, 3, Aluminium, 4, Fluorine, 2, Oxygen, 10)
                .addOreByproducts(Lithium, Caesium, Boron)
                .build();

        Glauconite = new Material.Builder(383, "glauconite")
                .dust().ore()
                .color(0x82B43C)
                .components(Potassium, 1, Magnesium, 2, Aluminium, 4, Hydrogen, 2, Oxygen, 12)
                .addOreByproducts(Sodium, Aluminium, Iron)
                .separatedInto(Iron)
                .build();

        GlauconiteSand = new Material.Builder(384, "glauconite_sand")
                .dust()
                .color(0x82B43C).iconSet(SAND)
                .components(Potassium, 1, Magnesium, 2, Aluminium, 4, Hydrogen, 2, Oxygen, 12)
                .addOreByproducts(Sodium, Aluminium, Iron)
                .separatedInto(Iron)
                .build();

        Malachite = new Material.Builder(385, "malachite")
                .dust().ore()
                .color(0x055F05)
                .components(Copper, 2, Carbon, 1, Hydrogen, 2, Oxygen, 5)
                .addOreByproducts(Copper, BrownLimonite, Calcite, Copper)
                .washedIn(SodiumPersulfate)
                .oreSmeltInto(Copper)
                .build();

        Lignite = new Material.Builder(2003, "lignite")
                .gem(0, 1200).ore() // 2/3 of burn time of coal
                .color(0x644646).iconSet(LIGNITE)
                .flags(FLAMMABLE, NO_SMELTING, NO_SMASHING, MORTAR_GRINDABLE)
                .components(Carbon, 3, Water, 1)
                .addOreByproducts(Coal)
                .build();

        Olivine = new Material.Builder(2004, "olivine")
                .gem().ore(2, 1)
                .color(0x96FF96).iconSet(RUBY)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(Magnesium, 2, Iron, 1, SiliconDioxide, 2)
                .toolStats(7.5f, 3.0f, 312)
                .addOreByproducts(Pyrope, Magnesium, Manganese)
                .build();

        Opal = new Material.Builder(2005, "opal")
                .gem().ore()
                .color(0x0000FF).iconSet(OPAL)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .toolStats(7.5f, 3.0f, 312)
                .addOreByproducts(Tanzanite)
                .build();

        Amethyst = new Material.Builder(2006, "amethyst")
                .gem(3).ore()
                .color(0xD232D2).iconSet(RUBY)
                .flags(NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT)
                .components(SiliconDioxide, 4, Iron, 1)
                .toolStats(7.5f, 3.0f, 312)
                .addOreByproducts(Amethyst)
                .build();

        Lapis = new Material.Builder(2007, "lapis")
                .gem(1).ore(6, 4)
                .color(0x4646DC).iconSet(LAPIS)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, NO_WORKING, DECOMPOSITION_BY_ELECTROLYZING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Lazurite, 12, Sodalite, 2, Pyrite, 1, Calcite, 1)
                .addOreByproducts(Lazurite, Sodalite, Pyrite)
                .build();

        Niter = new Material.Builder(2009, "niter")
                .dust(1)
                .color(0xFFC8C8).iconSet(FLINT)
                .flags(NO_SMASHING, NO_SMELTING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Saltpeter, 1)
                .addOreByproducts(Saltpeter)
                .build();

        Apatite = new Material.Builder(2010, "apatite")
                .gem(1).ore(4, 2)
                .color(0xC8C8FF).iconSet(DIAMOND)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE)
                .components(Calcium, 5, Phosphate, 3, Chlorine, 1)
                .addOreByproducts(TricalciumPhosphate)
                .build();

        TricalciumPhosphate = new Material.Builder(2015, "tricalcium_phosphate")
                .dust().ore(3, 1)
                .color(0xFFFF00).iconSet(FLINT)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE, EXPLOSIVE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Calcium, 3, Phosphate, 2)
                .addOreByproducts(Apatite, Phosphate)
                .build();

        GarnetRed = new Material.Builder(2016, "garnet_red")
                .gem().ore(4, 1)
                .color(0xC85050).iconSet(RUBY)
                .flags(STD_SOLID, GENERATE_LENS, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Pyrope, 3, Almandine, 5, Spessartine, 8)
                .toolStats(7.5f, 3.0f, 156)
                .addOreByproducts(Spessartine, Pyrope, Almandine)
                .build();

        GarnetYellow = new Material.Builder(2017, "garnet_yellow")
                .gem().ore(4, 1)
                .color(0xC8C850).iconSet(RUBY)
                .flags(STD_SOLID, GENERATE_LENS, NO_SMASHING, NO_SMELTING, HIGH_SIFTER_OUTPUT, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Andradite, 5, Grossular, 8, Uvarovite, 3)
                .toolStats(7.5f, 3.0f, 156)
                .addOreByproducts(Andradite, Grossular, Uvarovite)
                .build();

        Marble = new Material.Builder(2018, "marble")
                .dust(1)
                .color(0xC8C8C8).iconSet(FINE)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnesium, 1, Calcite, 7)
                .addOreByproducts(Calcite)
                .build();

        GraniteBlack = new Material.Builder(2019, "granite_black")
                .dust(3)
                .color(0x0A0A0A).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 4, Biotite, 1)
                .addOreByproducts(Biotite)
                .build();

        GraniteRed = new Material.Builder(2020, "granite_red")
                .dust(3)
                .color(0xFF0080).iconSet(ROUGH)
                .flags(NO_SMASHING)
                .components(Aluminium, 2, PotassiumFeldspar, 1, Oxygen, 3)
                .addOreByproducts(PotassiumFeldspar)
                .build();

        Chrysotile = new Material.Builder(2021, "chrysotile")
                .dust()
                .color(0x6E8C6E).iconSet(ROUGH)
                .components(Asbestos, 1)
                .addOreByproducts(Asbestos, Silicon, Magnesium)
                .build();

        VanadiumMagnetite = new Material.Builder(2022, "vanadium_magnetite")
                .dust().ore()
                .color(0x23233C).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnetite, 1, Vanadium, 1)
                .addOreByproducts(Magnetite, Vanadium)
                .separatesInto(Gold)
                .build();

        QuartzSand = new Material.Builder(2023, "quartz_sand")
                .dust(1)
                .color(0xC8C8C8).iconSet(SAND)
                .components(CertusQuartz, 1, Quartzite, 1)
                .addOreByproducts(CertusQuartz, Quartzite, Barite)
                .build();

        Pollucite = new Material.Builder(2024, "pollucite")
                .dust()
                .color(0xF0D2D2)
                .components(Caesium, 2, Aluminium, 2, Silicon, 4, Water, 2, Oxygen, 12)
                .addOreByproducts(Caesium, Aluminium, Rubidium)
                .build();

        Vermiculite = new Material.Builder(2025, "vermiculite")
                .dust()
                .color(0xC8B40F).iconSet(METALLIC)
                .components(Iron, 3, Aluminium, 4, Silicon, 4, Hydrogen, 2, Water, 4, Oxygen, 12)
                .addOreByproducts(Iron, Aluminium, Magnesium)
                .separatesInto(Iron)
                .build();

        Bentonite = new Material.Builder(2026, "bentonite")
                .dust().ore(7, 1)
                .color(0xF5D7D2).iconSet(ROUGH)
                .components(Sodium, 1, Magnesium, 6, Silicon, 12, Hydrogen, 4, Water, 5, Oxygen, 36)
                .addOreByproducts(Aluminium, Calcium, Magnesium)
                .build();

        FullersEarth = new Material.Builder(2027, "fullers_earth")
                .dust()
                .color(0xA0A078).iconSet(FINE)
                .components(Magnesium, 1, Silicon, 4, Hydrogen, 1, Water, 4, Oxygen, 11)
                .addOreByproducts(Aluminium, Silicon, Magnesium)
                .build();

        Pitchblende = new Material.Builder(2028, "pitchblende")
                .dust(3).ore(2, 1)
                .color(0xC8D200)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Uraninite, 3, Thorium, 1, Lead, 1)
                .addOreByproducts(Thorium, Uranium238, Lead)
                .build()
                .setFormula("(UO2)3ThPb", true);

        Monazite = new Material.Builder(2029, "monazite")
                .gem(1).ore(8, 2)
                .color(0x324632).iconSet(DIAMOND)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE)
                .components(RareEarth, 1, Phosphate, 1)
                .addOreByproducts(Thorium, Neodymium, RareEarth)
                .separatedInto(Neodymium)
                .build();

        Vinteum = new Material.Builder(1612, "vinteum")
                .gem(3).ore()
                .color(0x64C8FF).iconSet(EMERALD)
                .flags(STD_GEM, NO_SMASHING, NO_SMELTING)
                .toolStats(12.0f, 3.0f, 128)
                .addOreByproducts(Vinteum)
                .addDefaultEnchant(Enchantments.FORTUNE, 2)
                .build();

        Redstone = new Material.Builder(2507, "redstone")
                .dust().ore(5, 1)
                .color(0xC80000).iconSet(ROUGH)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Silicon, 1, Pyrite, 5, Ruby, 1, Mercury, 3)
                .addOreByproducts(Cinnabar, RareEarth, Glowstone)
                .build();

        Diatomite = new Material.Builder(2509, "diatomite")
                .dust(1)
                .color(0xE1E1E1)
                .components(Flint, 8, BandedIron, 1, Sapphire, 1)
                .addOreByproducts(BandedIron, Sapphire)
                .build();

        Basalt = new Material.Builder(2512, "basalt")
                .dust(1)
                .color(0x1E1414).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Olivine, 1, Calcite, 3, Flint, 8, DarkAsh, 4)
                .addOreByproducts(Olivine, DarkAsh)
                .build();

        GraniticMineralSand = new Material.Builder(2513, "granitic_mineral_sand")
                .dust(1).ore()
                .color(0x283C3C).iconSet(SAND)
                .components(Magnetite, 1, GraniteBlack, 1)
                .flags(BLAST_FURNACE_CALCITE_DOUBLE)
                .addOreByproducts(GraniteBlack, Magnetite)
                .separatedInto(Gold)
                .oreSmeltInto(Iron)
                .build();

        Redrock = new Material.Builder(2514, "redrock")
                .dust(1)
                .color(0xFF5032).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Calcite, 2, Flint, 1)
                .addOreByproducts(Clay)
                .build();

        GarnetSand = new Material.Builder(2515, "garnet_sand")
                .dust(1)
                .color(0xC86400).iconSet(SAND)
                .components(GarnetRed, 1, GarnetYellow, 1)
                .addOreByproducts(GarnetRed, GarnetYellow)
                .build();

        BasalticMineralSand = new Material.Builder(2518, "basaltic_mineral_sand")
                .dust(1).ore()
                .color(0x283228).iconSet(SAND)
                .components(Magnetite, 1, Basalt, 1)
                .flags(BLAST_FURNACE_CALCITE_DOUBLE)
                .addOreByproducts(Basalt, Magnetite)
                .separatesInto(Gold)
                .oreSmeltInto(Iron)
                .build();
    }
}
