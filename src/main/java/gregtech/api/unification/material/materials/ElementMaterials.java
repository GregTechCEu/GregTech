package gregtech.api.unification.material.materials;

import gregtech.api.GTValues;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.MaterialBuilder;
import gregtech.api.unification.material.MaterialBuilder.FluidType;

import static gregtech.api.unification.material.MaterialIconSet.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.type.MaterialFlags.*;

public class ElementMaterials {

    public static void register() {
        Actinium = new MaterialBuilder(1, "actinium")
                .ingot().fluid()
                .color(0xC3D1FF).iconSet(METALLIC)
                .element(Elements.Ac)
                .build();

        Aluminium = new MaterialBuilder(2, "aluminium")
                .ingot().fluid().ore() // todo use this to test lack of "ingot()" call (tool guarantees)
                .color(0x80C8F0)
                .flags(EXT2_METAL, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME)
                .element(Elements.Al)
                .toolStats(10.0f, 2.0f, 128)
                .addOreByproducts(Bauxite)
                .cableProperties(GTValues.V[4], 1, 1)
                .fluidPipeProperties(1166, 35, true)
                .blastTemp(1700)
                .build();

        Americium = new MaterialBuilder(3, "americium")
                .ingot(3).fluid()
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_ROD, GENERATE_LONG_ROD)
                .element(Elements.Am)
                .itemPipeProperties(64, 64)
                .build();

        Antimony = new MaterialBuilder(4, "antimony")
                .ingot().fluid()//.ore()
                .color(0xDCDCF0).iconSet(SHINY)
                .flags(EXT_METAL, MORTAR_GRINDABLE)
                .element(Elements.Sb)
                //.separatedInto(Iron)
                //.washedIn(SodiumPersulfate)
                //.addOreByproducts(Zinc, Iron, Zinc)
                .build();

        Argon = new MaterialBuilder(5, "argon")
                .fluid(FluidType.GAS).plasma()
                .color(0x01FF01).iconSet(FLUID)
                .element(Elements.Ar)
                .build();

        Arsenic = new MaterialBuilder(6, "arsenic")
                .dust().fluid()
                .element(Elements.As)
                .build();

        Astatine = new MaterialBuilder(7, "astatine")
                .ingot().fluid()
                .color(0x241A24)
                .element(Elements.At)
                .build();

        Barium = new MaterialBuilder(8, "barium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .element(Elements.Ba)
                .build();

        Berkelium = new MaterialBuilder(9, "berkelium")
                .ingot(3).fluid()
                .color(0x645A88).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Bk)
                .build();

        Beryllium = new MaterialBuilder(10, "beryllium")
                .ingot().fluid().ore()
                .color(0x64B464).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Be)
                .addOreByproducts(Emerald)
                .build();

        Bismuth = new MaterialBuilder(11, "bismuth")
                .ingot(1).fluid().ore()
                .color(0x64A0A0).iconSet(METALLIC)
                .element(Elements.Bi)
                .build();

        Bohrium = new MaterialBuilder(12, "bohrium")
                .ingot(7).fluid()
                .color(0xDC57FF).iconSet(SHINY)
                .element(Elements.Bh)
                .build();

        Boron = new MaterialBuilder(13, "boron")
                .dust().fluid()
                .color(0xD2FAD2)
                .element(Elements.B)
                .build();

        Bromine = new MaterialBuilder(14, "bromine")
                .fluid()
                .color(0x500A0A).iconSet(SHINY)
                .element(Elements.Br)
                .build();

        Caesium = new MaterialBuilder(15, "caesium")
                .ingot()
                .iconSet(METALLIC)
                .element(Elements.Cs)
                .build();

        Calcium = new MaterialBuilder(16, "calcium")
                .ingot().fluid()
                .color(0xFFF5F5).iconSet(METALLIC)
                .element(Elements.Ca)
                .build();

        Californium = new MaterialBuilder(17, "californium")
                .ingot(3).fluid()
                .color(0xA85A12).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Cf)
                .build();

        Carbon = new MaterialBuilder(18, "carbon")
                .ingot().fluid()
                .color(0x141414)
                .element(Elements.C)
                .build();

        Cadmium = new MaterialBuilder(19, "cadmium")
                .ingot().fluid()
                .color(0x32323C).iconSet(SHINY)
                .element(Elements.Cd)
                .build();

        Cerium = new MaterialBuilder(20, "cerium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .element(Elements.Ce)
                .blastTemp(1068)
                .build();

        Chlorine = new MaterialBuilder(21, "chlorine")
                .fluid(FluidType.GAS)
                .iconSet(GAS)
                .element(Elements.Cl)
                .build();

        Chrome = new MaterialBuilder(22, "chrome")
                .ingot(3).fluid()//.ore()
                .color(0xFFE6E6).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE)
                .element(Elements.Cr)
                .toolStats(12.0f, 3.0f, 512)
                //.separatedInto(Iron)
                //.addOreByproducts(Iron, Magnesium)
                .fluidPipeProperties(2725, 40, true)
                .blastTemp(1700)
                .build();

        Cobalt = new MaterialBuilder(23, "cobalt")
                .ingot().fluid().ore()
                .color(0x5050FA).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Co)
                .toolStats(10.0f, 3.0f, 256)
                .washedIn(SodiumPersulfate)
                .addOreByproducts(Cobaltite)
                .cableProperties(GTValues.V[1], 2, 2)
                .itemPipeProperties(2560, 2.0f)
                .build();

        Copernicium = new MaterialBuilder(24, "copernicium")
                .ingot(4).fluid()
                .color(0xFFFEFF)
                .element(Elements.Cn)
                .build();

        Copper = new MaterialBuilder(25, "copper")
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

        Curium = new MaterialBuilder(26, "curium")
                .ingot(3).fluid()
                .color(0x7B544E).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Cm)
                .build();

        Darmstadtium = new MaterialBuilder(27, "darmstadtium")
                .ingot().fluid()
                .color(0xAAAAAA)
                .element(Elements.Ds)
                .build();

        Deuterium = new MaterialBuilder(28, "deuterium")
                .fluid(FluidType.GAS)
                .iconSet(FLUID)
                .element(Elements.D)
                .build();

        Dubnium = new MaterialBuilder(29, "dubnium")
                .ingot(7).fluid()
                .color(0xD3FDFF).iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Db)
                .build();

        Dysprosium = new MaterialBuilder(30, "dysprosium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .element(Elements.Dy)
                .blastTemp(1680)
                .build();

        Einsteinium = new MaterialBuilder(31, "einsteinium")
                .ingot(3).fluid()
                .color(0xCE9F00).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Es)
                .build();

        Erbium = new MaterialBuilder(32, "erbium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Er)
                .blastTemp(1802)
                .build();

        Europium = new MaterialBuilder(33, "europium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_ROD)
                .element(Elements.Eu)
                .fluidPipeProperties(7780, 1200, true)
                .blastTemp(1099)
                .build();

        Fermium = new MaterialBuilder(34, "fermium")
                .ingot(3).fluid()
                .color(0x984ACF).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Fm)
                .build();

        Flerovium = new MaterialBuilder(35, "flerovium")
                .ingot(3).fluid()
                .iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Fl)
                .build();

        Fluorine = new MaterialBuilder(36, "fluorine")
                .fluid(FluidType.GAS)
                .iconSet(GAS)
                .element(Elements.F)
                .fluidTemp(253)
                .build();

        Francium = new MaterialBuilder(37, "francium")
                .ingot().fluid()
                .color(0xAAAAAA).iconSet(SHINY)
                .element(Elements.Fr)
                .build();

        Gadolinium = new MaterialBuilder(38, "gadolinium")
                .ingot().fluid()
                .color(0xDDDDFF).iconSet(METALLIC)
                .element(Elements.Gd)
                .blastTemp(1585)
                .build();

        Gallium = new MaterialBuilder(39, "gallium")
                .ingot().fluid()
                .color(0xDCDCFF).iconSet(SHINY)
                .flags(STD_METAL, GENERATE_FOIL)
                .element(Elements.Ga)
                .build();

        Germanium = new MaterialBuilder(40, "germanium")
                .ingot().fluid()
                .color(0x434343).iconSet(SHINY)
                .element(Elements.Ge)
                .build();

        Gold = new MaterialBuilder(41, "gold")
                .ingot().fluid().ore()
                .color(0xFFFF1E).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .element(Elements.Au)
                .washedIn(Mercury)
                .addOreByproducts(Copper, Nickel, Gold)
                .cableProperties(GTValues.V[3], 2, 2)
                .fluidPipeProperties(1671, 35, true)
                .build();

        Hafnium = new MaterialBuilder(42, "hafnium")
                .ingot().fluid()
                .color(0x99999A).iconSet(SHINY)
                .element(Elements.Hf)
                .build();

        Hassium = new MaterialBuilder(43, "hassium")
                .ingot(3).fluid()
                .color(0xDDDDDD).flags(EXT2_METAL)
                .element(Elements.Hs)
                .build();

        Holmium = new MaterialBuilder(44, "holmium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .element(Elements.Ho)
                .blastTemp(1734)
                .build();

        Hydrogen = new MaterialBuilder(45, "hydrogen")
                .fluid(FluidType.GAS)
                .iconSet(GAS)
                .element(Elements.H)
                .build();

        Helium = new MaterialBuilder(46, "helium")
                .fluid(FluidType.GAS).plasma()
                .iconSet(GAS)
                .element(Elements.He)
                .build();

        Helium3 = new MaterialBuilder(47, "helium3")
                .fluid(FluidType.GAS)
                .iconSet(GAS)
                .element(Elements.He3)
                .build();

        Indium = new MaterialBuilder(48, "indium")
                .ingot().fluid()
                .color(0x400080).iconSet(METALLIC)
                .element(Elements.In)
                .build();

        Iodine = new MaterialBuilder(49, "iodine")
                .dust().fluid()
                .color(0x2C344F).iconSet(SHINY)
                .element(Elements.I)
                .build();

        Iridium = new MaterialBuilder(50, "iridium")
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

        Iron = new MaterialBuilder(51, "iron")
                .ingot().fluid().plasma().ore()
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_DENSE, GENERATE_FRAME, GENERATE_ROTOR, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, BLAST_FURNACE_CALCITE_TRIPLE)
                .element(Elements.Fe)
                .toolStats(7.0f, 2.5f, 256)
                .washedIn(SodiumPersulfate)
                .polarizesInto(IronMagnetic)
                .arcSmeltInto(WroughtIron)
                .addOreByproducts(Nickel, Tin, Nickel)
                .cableProperties(GTValues.V[2], 2, 3)
                .build();

        Krypton = new MaterialBuilder(52, "krypton")
                .fluid(FluidType.GAS)
                .iconSet(GAS)
                .element(Elements.Kr)
                .build();

        Lanthanum = new MaterialBuilder(53, "lanthanum")
                .ingot().fluid()
                .iconSet(METALLIC)
                .element(Elements.La)
                .blastTemp(1193)
                .build();

        Lawrencium = new MaterialBuilder(54, "lawrencium")
                .ingot(3).fluid()
                .iconSet(METALLIC)
                .element(Elements.Lr)
                .build();

        Lead = new MaterialBuilder(55, "lead")
                .ingot(1).fluid().ore()
                .color(0x8C648C)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_DENSE)
                .element(Elements.Pb)
                .washedIn(Mercury)
                .addOreByproducts(Silver, Sulfur, Silver)
                .cableProperties(GTValues.V[1], 2, 2)
                .fluidPipeProperties(1200, 15, true)
                .build();

        Lithium = new MaterialBuilder(56, "lithium")
                .ingot().fluid().ore()
                .color(0xE1DCE1)
                .flags(STD_METAL)
                .element(Elements.Li)
                .addOreByproducts(Lithium)
                .build();

        Livermorium = new MaterialBuilder(57, "livermorium")
                .ingot().fluid()
                .color(0xC7B204).iconSet(SHINY)
                .element(Elements.Lv)
                .build();

        Lutetium = new MaterialBuilder(58, "lutetium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .element(Elements.Lu)
                .blastTemp(1925)
                .build();

        Magnesium = new MaterialBuilder(59, "magnesium")
                .ingot().fluid()
                .color(0xE1C8C8).iconSet(METALLIC)
                .element(Elements.Mg)
                .addOreByproducts(Olivine)
                .build();

        Mendelevium = new MaterialBuilder(60, "mendelevium")
                .ingot(3).fluid()
                .color(0x1D4ACF).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Md)
                .build();

        Manganese = new MaterialBuilder(61, "manganese")
                .ingot().fluid()
                .color(0xFAFAFA)
                .flags(STD_METAL, GENERATE_FOIL)
                .element(Elements.Mn)
                .addOreByproducts(Chrome, Iron)
                .separatedInto(Iron)
                .toolStats(7.0f, 2.0f, 512)
                .build();

        Meitnerium = new MaterialBuilder(62, "meitnerium")
                .ingot().fluid()
                .color(0x2246BE).iconSet(SHINY)
                .element(Elements.Mt)
                .build();

        Mercury = new MaterialBuilder(63, "mercury")
                .fluid()
                .iconSet(FLUID)
                .element(Elements.Hg)
                .build();

        Molybdenum = new MaterialBuilder(64, "molybdenum")
                .ingot().fluid().ore()
                .color(0xB4B4DC).iconSet(SHINY)
                .element(Elements.Mo)
                .toolStats(7.0f, 2.0f, 512)
                .build();

        Moscovium = new MaterialBuilder(65, "moscovium")
                .ingot().fluid()
                .color(0x7854AD).iconSet(SHINY)
                .element(Elements.Mc)
                .build();

        Neodymium = new MaterialBuilder(66, "neodymium")
                .ingot().fluid().ore()
                .color(0x646464).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_ROD)
                .element(Elements.Nd)
                .toolStats(7.0f, 2.0f, 512)
                .polarizesInto(NeodymiumMagnetic)
                .addOreByproducts(Monazite, RareEarth)
                .blastTemp(1297)
                .build();

        Neon = new MaterialBuilder(67, "neon")
                .fluid(FluidType.GAS)
                .iconSet(GAS)
                .element(Elements.Ne)
                .build();

        Neptunium = new MaterialBuilder(68, "neptunium")
                .ingot(3).fluid()
                .color(0x284D7B).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Np)
                .build();

        Nickel = new MaterialBuilder(69, "nickel")
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

        Nihonium = new MaterialBuilder(70, "nihonium")
                .ingot().fluid()
                .color(0x08269E).iconSet(SHINY)
                .element(Elements.Nh)
                .build();

        Niobium = new MaterialBuilder(71, "niobium")
                .ingot().fluid().ore()
                .color(0xBEB4C8).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Nb)
                .blastTemp(2750)
                .build();

        Nitrogen = new MaterialBuilder(72, "nitrogen")
                .fluid(FluidType.GAS).plasma()
                .iconSet(FLUID)
                .element(Elements.N)
                .build();

        Nobelium = new MaterialBuilder(73, "nobelium")
                .ingot().fluid()
                .iconSet(SHINY)
                .element(Elements.No)
                .build();

        Oganesson = new MaterialBuilder(74, "oganesson")
                .ingot(3).fluid()
                .color(0x142D64).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Og)
                .build();

        Osmium = new MaterialBuilder(75, "osmium")
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

        Oxygen = new MaterialBuilder(76, "oxygen")
                .fluid(FluidType.GAS).plasma()
                .iconSet(GAS)
                .element(Elements.O)
                .build();

        Palladium = new MaterialBuilder(77, "palladium")
                .ingot().fluid(FluidType.FLUID, true).ore()
                .color(0x808080).iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Pd)
                .toolStats(8.0f, 2.0f, 512)
                .cableProperties(GTValues.V[5], 2, 1)
                .blastTemp(1228)
                .build();

        Phosphorus = new MaterialBuilder(78, "phosphorus")
                .dust()
                .color(0xFFFF00)
                .element(Elements.P)
                .addOreByproducts(Phosphate)
                .build();

        Polonium = new MaterialBuilder(79, "polonium")
                .ingot(4).fluid()
                .color(0xC9D47E)
                .element(Elements.Po)
                .build();

        Platinum = new MaterialBuilder(80, "platinum")
                .ingot().fluid(FluidType.GAS, true).ore()
                .color(0xFFFFC8).iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Pt)
                .washedIn(Mercury)
                .cableProperties(GTValues.V[5], 2, 1)
                .itemPipeProperties(512, 4.0f)
                .addOreByproducts(Nickel, Iridium)
                .build();

        Plutonium239 = new MaterialBuilder(81, "plutonium")
                .ingot(3).fluid()
                .color(0xF03232).iconSet(METALLIC)
                .flags(EXT_METAL)
                .element(Elements.Pu239)
                //.addOreByproducts(Uranium238, Lead)
                .build();

        Plutonium241 = new MaterialBuilder(82, "plutonium241")
                .ingot(3).fluid()
                .color(0xFA4646).iconSet(SHINY)
                .flags(EXT_METAL)
                .element(Elements.Pu241)
                .build();

        Potassium = new MaterialBuilder(83, "potassium")
                .ingot(1).fluid()
                .color(0xFAFAFA).iconSet(METALLIC)
                .flags(EXT_METAL)
                .element(Elements.K)
                .build();

        Praseodymium = new MaterialBuilder(84, "praseodymium")
                .ingot().fluid()
                .color(0xCECECE).iconSet(METALLIC)
                .flags(EXT_METAL)
                .element(Elements.Pr)
                .blastTemp(1208)
                .build();

        Promethium = new MaterialBuilder(85, "promethium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(EXT_METAL)
                .element(Elements.Pm)
                .blastTemp(1315)
                .build();

        Protactinium = new MaterialBuilder(86, "protactinium")
                .ingot(3).fluid()
                .color(0xA78B6D).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Pa)
                .build();

        Radon = new MaterialBuilder(87, "radon")
                .fluid(FluidType.GAS)
                .iconSet(GAS)
                .element(Elements.Rn)
                .build();

        Radium = new MaterialBuilder(88, "radium")
                .ingot().fluid()
                .color(0xFFC840).iconSet(SHINY)
                .element(Elements.Ra)
                .build();

        Rhenium = new MaterialBuilder(89, "rhenium")
                .ingot().fluid()
                .color(0xB6BAC3).iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Re)
                .build();

        Rhodium = new MaterialBuilder(90, "rhodium")
                .ingot().fluid()
                .color(0xF4F4F4).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Rh)
                .blastTemp(2237)
                .build();

        Roentgenium = new MaterialBuilder(91, "roentgenium")
                .ingot().fluid()
                .color(0xE3FDEC).iconSet(SHINY)
                .element(Elements.Rg)
                .build();

        Rubidium = new MaterialBuilder(92, "rubidium")
                .ingot().fluid()
                .color(0xF01E1E).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Rb)
                .build();

        Ruthenium = new MaterialBuilder(93, "ruthenium")
                .ingot().fluid()
                .color(0x646464).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Ru)
                .blastTemp(2607)
                .build();

        Rutherfordium = new MaterialBuilder(94, "rutherfordium")
                .ingot(7).fluid()
                .color(0xFFF6A1).iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Rf)
                .build();

        Samarium = new MaterialBuilder(95, "samarium")
                .ingot().fluid()
                .color(0xFFFFCC).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Sm)
                .blastTemp(1345)
                .build();

        Scandium = new MaterialBuilder(96, "scandium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Sc)
                .blastTemp(1814)
                .build();

        Seaborgium = new MaterialBuilder(97, "seaborgium")
                .ingot(7).fluid()
                .color(0x19C5FF).iconSet(SHINY)
                .element(Elements.Sg)
                .build();

        Selenium = new MaterialBuilder(98, "selenium")
                .ingot().fluid()
                .color(0xB6BA6B).iconSet(SHINY)
                .element(Elements.Se)
                .build();

        Silicon = new MaterialBuilder(99, "silicon")
                .ingot().fluid()
                .color(0x3C3C50).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_FOIL)
                .element(Elements.Si)
                //.addOreByproducts(SiliconDioxide)
                .blastTemp(1687)
                .build();

        Silver = new MaterialBuilder(100, "silver")
                .ingot().fluid().ore()
                .color(0xDCDCFF).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .element(Elements.Ag)
                .washedIn(Mercury)
                .addOreByproducts(Lead, Sulfur, Silver)
                .cableProperties(GTValues.V[3], 1, 1)
                .build();

        Sodium = new MaterialBuilder(101, "sodium")
                .ingot().fluid()
                .color(0x000096).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Na)
                .build();

        Strontium = new MaterialBuilder(102, "strontium")
                .ingot().fluid()
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Sr)
                .build();

        Sulfur = new MaterialBuilder(103, "sulfur") // todo fluid?
                .dust().ore()
                .color(0xC8C800)
                .flags(NO_SMASHING, NO_SMELTING, FLAMMABLE)
                .element(Elements.S)
                .addOreByproducts(Sulfur)
                .build();

        Tantalum = new MaterialBuilder(104, "tantalum")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_FOIL)
                .element(Elements.Ta)
                .build();

        Technetium = new MaterialBuilder(105, "technetium")
                .ingot().fluid()
                .color(0x545455).iconSet(SHINY)
                .element(Elements.Tc)
                .build();

        Tellurium = new MaterialBuilder(106, "tellurium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Te)
                .build();

        Tennessine = new MaterialBuilder(107, "tennessine")
                .ingot().fluid()
                .color(0x977FD6).iconSet(SHINY)
                .element(Elements.Ts)
                .build();

        Terbium = new MaterialBuilder(108, "terbium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Tb)
                .blastTemp(1629)
                .build();

        Thorium = new MaterialBuilder(109, "thorium")
                .ingot().fluid().ore()
                .color(0x001E00).iconSet(SHINY)
                .flags(STD_METAL)
                .element(Elements.Th)
                .toolStats(6.0f, 2.0f, 512)
                .addOreByproducts(Uranium238, Lead)
                .build();

        Thallium = new MaterialBuilder(110, "thallium")
                .ingot().fluid()
                .color(0xC1C1DE).iconSet(SHINY)
                .element(Elements.Tl)
                .build();

        Thulium = new MaterialBuilder(111, "thulium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Tm)
                .blastTemp(1818)
                .build();

        Tin = new MaterialBuilder(112, "tin")
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

        Titanium = new MaterialBuilder(113, "titanium") // todo Ore?
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

        Tritium = new MaterialBuilder(114, "tritium")
                .fluid(FluidType.GAS)
                .iconSet(METALLIC)
                .element(Elements.T)
                .build();

        Tungsten = new MaterialBuilder(115, "tungsten")
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

        Uranium238 = new MaterialBuilder(116, "uranium")
                .ingot(3).fluid().ore()
                .color(0x32F032).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.U238)
                .toolStats(6.0f, 3.0f, 512)
                .addOreByproducts(Lead, Uranium235, Thorium)
                .build();

        Uranium235 = new MaterialBuilder(117, "uranium235")
                .ingot(3).fluid().ore()
                .color(0x46FA46).iconSet(SHINY)
                .flags(STD_METAL, GENERATE_ROD)
                .element(Elements.U235)
                .toolStats(6.0f, 3.0f, 512)
                .build();

        Vanadium = new MaterialBuilder(118, "vanadium")
                .ingot().fluid()
                .color(0x323232).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.V)
                .blastTemp(2183)
                .build();

        Xenon = new MaterialBuilder(119, "xenon")
                .fluid(FluidType.GAS)
                .iconSet(GAS)
                .element(Elements.Xe)
                .build();

        Ytterbium = new MaterialBuilder(120, "ytterbium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Yb)
                .blastTemp(1097)
                .build();

        Yttrium = new MaterialBuilder(121, "yttrium")
                .ingot().fluid()
                .color(0xDCFADC).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Yt)
                .blastTemp(1799)
                .build();

        Zinc = new MaterialBuilder(122, "zinc")
                .ingot(1).fluid().ore()
                .color(0xFAF0F0).iconSet(METALLIC)
                .flags(STD_METAL, MORTAR_GRINDABLE, GENERATE_FOIL)
                .element(Elements.Zn)
                .washedIn(SodiumPersulfate)
                .addOreByproducts(Tin, Gallium)
                .cableProperties(GTValues.V[1], 1, 1)
                .build();

        Zirconium = new MaterialBuilder(123, "zirconium")
                .ingot(6).fluid()
                .color(0xE0E1E1).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Zr)
                .build();
    }
}
