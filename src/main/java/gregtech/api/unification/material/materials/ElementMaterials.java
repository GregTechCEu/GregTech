package gregtech.api.unification.material.materials;

import gregtech.api.GTValues;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Material.FluidType;
import gregtech.api.unification.material.properties.BlastProperty.GasTier;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;

public class ElementMaterials {

    public static void register() {
        Actinium = new Material.Builder(1, "actinium")
                .ingot().fluid()
                .color(0xC3D1FF).iconSet(METALLIC)
                .element(Elements.Ac)
                .setHidden()
                .build();

        Aluminium = new Material.Builder(2, "aluminium")
                .ingot().fluid().ore()
                .color(0x80C8F0)
                .flags(EXT2_METAL, GENERATE_SMALL_GEAR, GENERATE_RING, GENERATE_FRAME, GENERATE_SPRING, GENERATE_SPRING_SMALL)
                .element(Elements.Al)
                .toolStats(10.0f, 2.0f, 128, 21)
                .cableProperties(GTValues.V[4], 1, 1)
                .fluidPipeProperties(1166, 35, true)
                .blastTemp(1700, GasTier.LOW)
                .build();

        Americium = new Material.Builder(3, "americium")
                .ingot(3).fluid()
                .color(0x287869).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FOIL)
                .element(Elements.Am)
                .itemPipeProperties(64, 64)
                .build();

        Antimony = new Material.Builder(4, "antimony")
                .ingot().fluid().ore()
                .color(0xDCDCF0).iconSet(SHINY)
                .flags(EXT_METAL, MORTAR_GRINDABLE)
                .element(Elements.Sb)
                .build();

        Argon = new Material.Builder(5, "argon")
                .fluid(FluidType.GAS).plasma()
                .color(0x00FF00).iconSet(GAS)
                .element(Elements.Ar)
                .build();

        Arsenic = new Material.Builder(6, "arsenic")
                .dust()
                .color(0x676756)
                .element(Elements.As)
                .build();

        Astatine = new Material.Builder(7, "astatine")
                .ingot().fluid()
                .color(0x241A24)
                .element(Elements.At)
                .setHidden()
                .build();

        Barium = new Material.Builder(8, "barium")
                .ingot().fluid()
                .color(0x83824C).iconSet(METALLIC)
                .element(Elements.Ba)
                .flags(GENERATE_FOIL)
                .build();

        Berkelium = new Material.Builder(9, "berkelium")
                .ingot(3).fluid()
                .color(0x645A88).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Bk)
                .setHidden()
                .build();

        Beryllium = new Material.Builder(10, "beryllium")
                .ingot().fluid().ore()
                .color(0x64B464).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Be)
                .build();

        Bismuth = new Material.Builder(11, "bismuth")
                .ingot(1).fluid().ore()
                .color(0x64A0A0).iconSet(METALLIC)
                .element(Elements.Bi)
                .build();

        Bohrium = new Material.Builder(12, "bohrium")
                .ingot(7).fluid()
                .color(0xDC57FF).iconSet(SHINY)
                .element(Elements.Bh)
                .setHidden()
                .build();

        Boron = new Material.Builder(13, "boron")
                .dust()
                .color(0xD2FAD2)
                .element(Elements.B)
                .flags(GENERATE_ROD)
                .build();

        Bromine = new Material.Builder(14, "bromine")
                .fluid()
                .color(0x500A0A).iconSet(SHINY)
                .element(Elements.Br)
                .setHidden()
                .build();

        Caesium = new Material.Builder(15, "caesium")
                .ingot().fluid()
                .color(0x80620B).iconSet(METALLIC)
                .element(Elements.Cs)
                .build();

        Calcium = new Material.Builder(16, "calcium")
                .ingot().fluid()
                .color(0xFFF5DE).iconSet(METALLIC)
                .element(Elements.Ca)
                .flags(GENERATE_FOIL)
                .build();

        Californium = new Material.Builder(17, "californium")
                .ingot(3).fluid()
                .color(0xA85A12).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Cf)
                .setHidden()
                .build();

        Carbon = new Material.Builder(18, "carbon")
                .ingot().fluid()
                .color(0x141414)
                .element(Elements.C)
                .build();

        Cadmium = new Material.Builder(19, "cadmium")
                .ingot().fluid()
                .color(0x32323C).iconSet(SHINY)
                .element(Elements.Cd)
                .build();

        Cerium = new Material.Builder(20, "cerium")
                .ingot().fluid()
                .color(0x87917D).iconSet(METALLIC)
                .element(Elements.Ce)
                .flags(GENERATE_FINE_WIRE)
                .blastTemp(1068)
                .build();

        Chlorine = new Material.Builder(21, "chlorine")
                .fluid(FluidType.GAS)
                .element(Elements.Cl)
                .build();

        Chrome = new Material.Builder(22, "chrome")
                .ingot(3).fluid().ore()
                .color(0xEAC4D8).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE)
                .element(Elements.Cr)
                .toolStats(12.0f, 3.0f, 512, 33)
                .fluidPipeProperties(2725, 40, true)
                .blastTemp(1700, GasTier.LOW)
                .build();

        Cobalt = new Material.Builder(23, "cobalt")
                .ingot().fluid().ore()
                .color(0x5050FA).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_GEAR, GENERATE_BOLT_SCREW)
                .element(Elements.Co)
                .toolStats(10.0f, 3.0f, 256, 21)
                .cableProperties(GTValues.V[1], 2, 2)
                .itemPipeProperties(2560, 2.0f)
                .build();

        Copernicium = new Material.Builder(24, "copernicium")
                .ingot(4).fluid()
                .color(0xFFFEFF)
                .element(Elements.Cn)
                .setHidden()
                .build();

        Copper = new Material.Builder(25, "copper")
                .ingot(1).fluid().ore()
                .color(0xFF6400).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_DENSE, GENERATE_SPRING, GENERATE_SPRING_SMALL)
                .element(Elements.Cu)
                .cableProperties(GTValues.V[2], 1, 2)
                .fluidPipeProperties(1696, 10, true)
                .build();

        Curium = new Material.Builder(26, "curium")
                .ingot(3).fluid()
                .color(0x7B544E).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Cm)
                .setHidden()
                .build();

        Darmstadtium = new Material.Builder(27, "darmstadtium")
                .ingot().fluid()
                .color(0xAAAAAA)
                .element(Elements.Ds)
                .setHidden()
                .build();

        Deuterium = new Material.Builder(28, "deuterium")
                .fluid(FluidType.GAS)
                .element(Elements.D)
                .build();

        Dubnium = new Material.Builder(29, "dubnium")
                .ingot(7).fluid()
                .color(0xD3FDFF).iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Db)
                .setHidden()
                .build();

        Dysprosium = new Material.Builder(30, "dysprosium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .element(Elements.Dy)
                .blastTemp(1680)
                .setHidden()
                .build();

        Einsteinium = new Material.Builder(31, "einsteinium")
                .ingot(3).fluid()
                .color(0xCE9F00).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Es)
                .setHidden()
                .build();

        Erbium = new Material.Builder(32, "erbium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Er)
                .blastTemp(1802)
                .setHidden()
                .build();

        Europium = new Material.Builder(33, "europium")
                .ingot().fluid()
                .color(0x20FFFF).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_LONG_ROD, GENERATE_FINE_WIRE, GENERATE_SPRING, GENERATE_SPRING_SMALL, GENERATE_FOIL)
                .element(Elements.Eu)
                .cableProperties(GTValues.V[GTValues.UHV], 2, 32)
                .fluidPipeProperties(7780, 1200, true)
                .blastTemp(6000, GasTier.MID, VA[IV], 180)
                .build();

        Fermium = new Material.Builder(34, "fermium")
                .ingot(3).fluid()
                .color(0x984ACF).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Fm)
                .setHidden()
                .build();

        Flerovium = new Material.Builder(35, "flerovium")
                .ingot(3).fluid()
                .iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Fl)
                .setHidden()
                .build();

        Fluorine = new Material.Builder(36, "fluorine")
                .fluid(FluidType.GAS)
                .element(Elements.F)
                .fluidTemp(253)
                .build();

        Francium = new Material.Builder(37, "francium")
                .ingot().fluid()
                .color(0xAAAAAA).iconSet(SHINY)
                .element(Elements.Fr)
                .setHidden()
                .build();

        Gadolinium = new Material.Builder(38, "gadolinium")
                .ingot().fluid()
                .color(0xDDDDFF).iconSet(METALLIC)
                .element(Elements.Gd)
                .blastTemp(1585)
                .setHidden()
                .build();

        Gallium = new Material.Builder(39, "gallium")
                .ingot().fluid()
                .color(0xDCDCFF).iconSet(SHINY)
                .flags(STD_METAL, GENERATE_FOIL)
                .element(Elements.Ga)
                .build();

        Germanium = new Material.Builder(40, "germanium")
                .ingot().fluid()
                .color(0x434343).iconSet(SHINY)
                .element(Elements.Ge)
                .setHidden()
                .build();

        Gold = new Material.Builder(41, "gold")
                .ingot().fluid().ore()
                .color(0xFFE650).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_RING, MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, GENERATE_SPRING, GENERATE_SPRING_SMALL)
                .element(Elements.Au)
                .cableProperties(GTValues.V[3], 2, 2)
                .fluidPipeProperties(1671, 35, true)
                .build();

        Hafnium = new Material.Builder(42, "hafnium")
                .ingot().fluid()
                .color(0x99999A).iconSet(SHINY)
                .element(Elements.Hf)
                .setHidden()
                .build();

        Hassium = new Material.Builder(43, "hassium")
                .ingot(3).fluid()
                .color(0xDDDDDD).flags(EXT2_METAL)
                .element(Elements.Hs)
                .setHidden()
                .build();

        Holmium = new Material.Builder(44, "holmium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .element(Elements.Ho)
                .blastTemp(1734)
                .setHidden()
                .build();

        Hydrogen = new Material.Builder(45, "hydrogen")
                .fluid(FluidType.GAS)
                .color(0x0000B5)
                .element(Elements.H)
                .build();

        Helium = new Material.Builder(46, "helium")
                .fluid(FluidType.GAS).plasma()
                .element(Elements.He)
                .build();

        Helium3 = new Material.Builder(47, "helium3")
                .fluid(FluidType.GAS)
                .element(Elements.He3)
                .build();

        Indium = new Material.Builder(48, "indium")
                .ingot().fluid()
                .color(0x400080).iconSet(SHINY)
                .element(Elements.In)
                .build();

        Iodine = new Material.Builder(49, "iodine")
                .dust()
                .color(0x2C344F).iconSet(SHINY)
                .element(Elements.I)
                .setHidden()
                .build();

        Iridium = new Material.Builder(50, "iridium")
                .ingot(3).fluid().ore()
                .color(0xA1E4E4).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE)
                .element(Elements.Ir)
                .toolStats(7.0f, 3.0f, 2560, 21)
                .fluidPipeProperties(3398, 140, true)
                .blastTemp(4500, GasTier.HIGH, VA[IV], 1100)
                .build();

        Iron = new Material.Builder(51, "iron")
                .ingot().fluid().plasma().ore()
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_DENSE, GENERATE_FRAME, GENERATE_ROTOR, GENERATE_SMALL_GEAR,
                        GENERATE_SPRING, GENERATE_SPRING_SMALL, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, BLAST_FURNACE_CALCITE_TRIPLE)
                .element(Elements.Fe)
                .toolStats(7.0f, 2.5f, 256, 21)
                .cableProperties(GTValues.V[2], 2, 3)
                .build();

        Krypton = new Material.Builder(52, "krypton")
                .fluid(FluidType.GAS)
                .color(0x80FF80).iconSet(GAS)
                .element(Elements.Kr)
                .build();

        Lanthanum = new Material.Builder(53, "lanthanum")
                .ingot().fluid()
                .color(0x5D7575).iconSet(METALLIC)
                .element(Elements.La)
                .blastTemp(1193, GasTier.MID)
                .build();

        Lawrencium = new Material.Builder(54, "lawrencium")
                .ingot(3).fluid()
                .iconSet(METALLIC)
                .element(Elements.Lr)
                .setHidden()
                .build();

        Lead = new Material.Builder(55, "lead")
                .ingot(1).fluid().ore()
                .color(0x8C648C)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_DENSE, GENERATE_ROTOR, GENERATE_SPRING, GENERATE_SPRING_SMALL)
                .element(Elements.Pb)
                .cableProperties(GTValues.V[0], 2, 2)
                .fluidPipeProperties(1200, 15, true)
                .build();

        Lithium = new Material.Builder(56, "lithium")
                .ingot().fluid().ore()
                .color(0xBDC7DB)
                .flags(STD_METAL)
                .element(Elements.Li)
                .build();

        Livermorium = new Material.Builder(57, "livermorium")
                .ingot().fluid()
                .color(0x578062).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE)
                .element(Elements.Lv)
                .build();

        Lutetium = new Material.Builder(58, "lutetium")
                .ingot().fluid()
                .color(0x00AAFF).iconSet(METALLIC)
                .element(Elements.Lu)
                .blastTemp(1925)
                .build();

        Magnesium = new Material.Builder(59, "magnesium")
                .ingot().fluid()
                .color(0xFFC8C8).iconSet(METALLIC)
                .element(Elements.Mg)
                .build();

        Mendelevium = new Material.Builder(60, "mendelevium")
                .ingot(3).fluid()
                .color(0x1D4ACF).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Md)
                .setHidden()
                .build();

        Manganese = new Material.Builder(61, "manganese")
                .ingot().fluid()
                .color(0xCDE1B9)
                .flags(STD_METAL, GENERATE_FOIL, GENERATE_DENSE, GENERATE_BOLT_SCREW)
                .element(Elements.Mn)
                .toolStats(7.0f, 2.0f, 512, 21)
                .build();

        Meitnerium = new Material.Builder(62, "meitnerium")
                .ingot().fluid()
                .color(0x2246BE).iconSet(SHINY)
                .element(Elements.Mt)
                .setHidden()
                .build();

        Mercury = new Material.Builder(63, "mercury")
                .fluid()
                .color(0xE6DCDC).iconSet(DULL)
                .element(Elements.Hg)
                .build();

        Molybdenum = new Material.Builder(64, "molybdenum")
                .ingot().fluid().ore()
                .color(0xB4B4DC).iconSet(SHINY)
                .element(Elements.Mo)
                .flags(GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .toolStats(7.0f, 2.0f, 512, 33)
                .build();

        Moscovium = new Material.Builder(65, "moscovium")
                .ingot().fluid()
                .color(0x7854AD).iconSet(SHINY)
                .element(Elements.Mc)
                .setHidden()
                .build();

        Neodymium = new Material.Builder(66, "neodymium")
                .ingot().fluid().ore()
                .color(0x646464).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_ROD, GENERATE_BOLT_SCREW)
                .element(Elements.Nd)
                .toolStats(7.0f, 2.0f, 512, 21)
                .blastTemp(1289, GasTier.MID)
                .build();

        Neon = new Material.Builder(67, "neon")
                .fluid(FluidType.GAS)
                .color(0xFAB4B4).iconSet(GAS)
                .element(Elements.Ne)
                .build();

        Neptunium = new Material.Builder(68, "neptunium")
                .ingot(3).fluid()
                .color(0x284D7B).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Np)
                .setHidden()
                .build();

        Nickel = new Material.Builder(69, "nickel")
                .ingot().fluid().plasma().ore()
                .color(0xC8C8FA).iconSet(METALLIC)
                .flags(STD_METAL, MORTAR_GRINDABLE)
                .element(Elements.Ni)
                .cableProperties(GTValues.V[GTValues.LV], 3, 3)
                .itemPipeProperties(2048, 1.0f)
                .build();

        Nihonium = new Material.Builder(70, "nihonium")
                .ingot().fluid()
                .color(0x08269E).iconSet(SHINY)
                .element(Elements.Nh)
                .setHidden()
                .build();

        Niobium = new Material.Builder(71, "niobium")
                .ingot().fluid().ore()
                .color(0xBEB4C8).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Nb)
                .blastTemp(2750, GasTier.MID, VA[HV], 900)
                .build();

        Nitrogen = new Material.Builder(72, "nitrogen")
                .fluid(FluidType.GAS).plasma()
                .color(0x00BFC1).iconSet(GAS)
                .element(Elements.N)
                .build();

        Nobelium = new Material.Builder(73, "nobelium")
                .ingot().fluid()
                .iconSet(SHINY)
                .element(Elements.No)
                .setHidden()
                .build();

        Oganesson = new Material.Builder(74, "oganesson")
                .ingot(3).fluid()
                .color(0x142D64).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Og)
                .setHidden()
                .build();

        Osmium = new Material.Builder(75, "osmium")
                .ingot(4).fluid().ore()
                .color(0x3232FF).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE)
                .element(Elements.Os)
                .toolStats(16.0f, 4.0f, 1280, 21)
                .cableProperties(GTValues.V[6], 4, 2)
                .itemPipeProperties(256, 8.0f)
                .blastTemp(4500, GasTier.HIGH, VA[LuV], 1000)
                .build();

        Oxygen = new Material.Builder(76, "oxygen")
                .fluid(FluidType.GAS).plasma()
                .color(0x4CC3FF)
                .element(Elements.O)
                .build();

        Palladium = new Material.Builder(77, "palladium")
                .ingot().fluid(FluidType.FLUID, true).ore()
                .color(0x808080).iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Pd)
                .toolStats(8.0f, 2.0f, 512, 33)
                .blastTemp(1828, GasTier.LOW, VA[HV], 900)
                .build();

        Phosphorus = new Material.Builder(78, "phosphorus")
                .dust()
                .color(0xFFFF00)
                .element(Elements.P)
                .build();

        Polonium = new Material.Builder(79, "polonium")
                .ingot(4).fluid()
                .color(0xC9D47E)
                .element(Elements.Po)
                .setHidden()
                .build();

        Platinum = new Material.Builder(80, "platinum")
                .ingot().fluid(FluidType.GAS, true).ore()
                .color(0xFFFFC8).iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Pt)
                .cableProperties(GTValues.V[5], 2, 1)
                .itemPipeProperties(512, 4.0f)
                .build();

        Plutonium239 = new Material.Builder(81, "plutonium")
                .ingot(3).fluid()
                .color(0xF03232).iconSet(METALLIC)
                .flags(EXT_METAL)
                .element(Elements.Pu239)
                .build();

        Plutonium241 = new Material.Builder(82, "plutonium241")
                .ingot(3).fluid()
                .color(0xFA4646).iconSet(SHINY)
                .flags(EXT_METAL)
                .element(Elements.Pu241)
                .build();

        Potassium = new Material.Builder(83, "potassium")
                .ingot(1).fluid()
                .color(0xBEDCFF).iconSet(METALLIC)
                .flags(EXT_METAL)
                .element(Elements.K)
                .build();

        Praseodymium = new Material.Builder(84, "praseodymium")
                .ingot().fluid()
                .color(0xCECECE).iconSet(METALLIC)
                .flags(EXT_METAL)
                .element(Elements.Pr)
                .blastTemp(1208)
                .setHidden()
                .build();

        Promethium = new Material.Builder(85, "promethium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(EXT_METAL)
                .element(Elements.Pm)
                .blastTemp(1315)
                .setHidden()
                .build();

        Protactinium = new Material.Builder(86, "protactinium")
                .ingot(3).fluid()
                .color(0xA78B6D).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Pa)
                .setHidden()
                .build();

        Radon = new Material.Builder(87, "radon")
                .fluid(FluidType.GAS)
                .color(0xFF39FF)
                .element(Elements.Rn)
                .build();

        Radium = new Material.Builder(88, "radium")
                .ingot().fluid()
                .color(0xFFFFCD).iconSet(SHINY)
                .element(Elements.Ra)
                .setHidden()
                .build();

        Rhenium = new Material.Builder(89, "rhenium")
                .ingot().fluid()
                .color(0xB6BAC3).iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Re)
                .setHidden()
                .build();

        Rhodium = new Material.Builder(90, "rhodium")
                .ingot().fluid()
                .color(0xDC0C58).iconSet(BRIGHT)
                .flags(EXT2_METAL)
                .element(Elements.Rh)
                .blastTemp(2237, GasTier.MID, VA[EV], 1200)
                .build();

        Roentgenium = new Material.Builder(91, "roentgenium")
                .ingot().fluid()
                .color(0xE3FDEC).iconSet(SHINY)
                .element(Elements.Rg)
                .setHidden()
                .build();

        Rubidium = new Material.Builder(92, "rubidium")
                .ingot().fluid()
                .color(0xF01E1E).iconSet(SHINY)
                .flags(STD_METAL)
                .element(Elements.Rb)
                .setHidden()
                .build();

        Ruthenium = new Material.Builder(93, "ruthenium")
                .ingot().fluid()
                .color(0x50ACCD).iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Ru)
                .blastTemp(2607, GasTier.MID, VA[EV], 900)
                .build();

        Rutherfordium = new Material.Builder(94, "rutherfordium")
                .ingot(7).fluid()
                .color(0xFFF6A1).iconSet(SHINY)
                .flags(EXT2_METAL)
                .element(Elements.Rf)
                .setHidden()
                .build();

        Samarium = new Material.Builder(95, "samarium")
                .ingot().fluid()
                .color(0xFFFFCC).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Sm)
                .blastTemp(5400, GasTier.HIGH, VA[EV], 1500)
                .build();

        Scandium = new Material.Builder(96, "scandium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Sc)
                .blastTemp(1814)
                .setHidden()
                .build();

        Seaborgium = new Material.Builder(97, "seaborgium")
                .ingot(7).fluid()
                .color(0x19C5FF).iconSet(SHINY)
                .element(Elements.Sg)
                .setHidden()
                .build();

        Selenium = new Material.Builder(98, "selenium")
                .ingot().fluid()
                .color(0xB6BA6B).iconSet(SHINY)
                .element(Elements.Se)
                .setHidden()
                .build();

        Silicon = new Material.Builder(99, "silicon")
                .ingot().fluid()
                .color(0x3C3C50).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_FOIL)
                .element(Elements.Si)
                .blastTemp(1687) // no gas tier for silicon
                .build();

        Silver = new Material.Builder(100, "silver")
                .ingot().fluid().ore()
                .color(0xDCDCFF).iconSet(SHINY)
                .flags(EXT2_METAL, MORTAR_GRINDABLE)
                .element(Elements.Ag)
                .cableProperties(GTValues.V[3], 1, 1)
                .build();

        Sodium = new Material.Builder(101, "sodium")
                .ingot().fluid()
                .color(0x000096).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Na)
                .build();

        Strontium = new Material.Builder(102, "strontium")
                .ingot().fluid()
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Sr)
                .setHidden()
                .build();

        Sulfur = new Material.Builder(103, "sulfur")
                .dust().ore().fluid()
                .color(0xC8C800)
                .flags(FLAMMABLE)
                .element(Elements.S)
                .build();

        Tantalum = new Material.Builder(104, "tantalum")
                .ingot().fluid()
                .color(0x78788c).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_FOIL)
                .element(Elements.Ta)
                .build();

        Technetium = new Material.Builder(105, "technetium")
                .ingot().fluid()
                .color(0x545455).iconSet(SHINY)
                .element(Elements.Tc)
                .setHidden()
                .build();

        Tellurium = new Material.Builder(106, "tellurium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Te)
                .setHidden()
                .build();

        Tennessine = new Material.Builder(107, "tennessine")
                .ingot().fluid()
                .color(0x977FD6).iconSet(SHINY)
                .element(Elements.Ts)
                .setHidden()
                .build();

        Terbium = new Material.Builder(108, "terbium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Tb)
                .blastTemp(1629)
                .setHidden()
                .build();

        Thorium = new Material.Builder(109, "thorium")
                .ingot().fluid().ore()
                .color(0x001E00).iconSet(SHINY)
                .flags(STD_METAL, GENERATE_ROD)
                .element(Elements.Th)
                .toolStats(6.0f, 2.0f, 512, 33)
                .build();

        Thallium = new Material.Builder(110, "thallium")
                .ingot().fluid()
                .color(0xC1C1DE).iconSet(SHINY)
                .element(Elements.Tl)
                .setHidden()
                .build();

        Thulium = new Material.Builder(111, "thulium")
                .ingot().fluid()
                .iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Tm)
                .blastTemp(1818)
                .setHidden()
                .build();

        Tin = new Material.Builder(112, "tin")
                .ingot(1).fluid().ore()
                .color(0xDCDCDC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_ROTOR, GENERATE_SPRING, GENERATE_SPRING_SMALL)
                .element(Elements.Sn)
                .cableProperties(GTValues.V[1], 1, 1)
                .itemPipeProperties(4096, 0.5f)
                .build();

        Titanium = new Material.Builder(113, "titanium") // todo Ore? Look at EBF recipe here if we do Ti ores
                .ingot(3).fluid()
                .color(0xDCA0F0).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_SPRING, GENERATE_FRAME, GENERATE_DENSE)
                .element(Elements.Ti)
                .toolStats(7.0f, 3.0f, 1600, 21)
                .fluidPipeProperties(2426, 80, true)
                .blastTemp(1941, GasTier.MID, VA[HV], 1500)
                .build();

        Tritium = new Material.Builder(114, "tritium")
                .fluid(FluidType.GAS)
                .iconSet(METALLIC)
                .element(Elements.T)
                .build();

        Tungsten = new Material.Builder(115, "tungsten")
                .ingot(3).fluid()
                .color(0x323232).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_SPRING, GENERATE_SPRING_SMALL)
                .element(Elements.W)
                .toolStats(7.0f, 3.0f, 2560, 21)
                .cableProperties(GTValues.V[5], 2, 2)
                .fluidPipeProperties(4618, 90, true)
                .blastTemp(3600, GasTier.MID, VA[EV], 1800)
                .build();

        Uranium238 = new Material.Builder(116, "uranium")
                .ingot(3).fluid().ore(true)
                .color(0x32F032).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.U238)
                .toolStats(6.0f, 3.0f, 512, 21)
                .build();

        Uranium235 = new Material.Builder(117, "uranium235")
                .ingot(3).fluid().ore(true)
                .color(0x46FA46).iconSet(SHINY)
                .flags(STD_METAL, GENERATE_ROD)
                .element(Elements.U235)
                .toolStats(6.0f, 3.0f, 512, 33)
                .build();

        Vanadium = new Material.Builder(118, "vanadium")
                .ingot().fluid()
                .color(0x323232).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.V)
                .blastTemp(2183, GasTier.MID)
                .build();

        Xenon = new Material.Builder(119, "xenon")
                .fluid(FluidType.GAS)
                .color(0x00FFFF).iconSet(GAS)
                .element(Elements.Xe)
                .build();

        Ytterbium = new Material.Builder(120, "ytterbium")
                .ingot().fluid()
                .color(0xA7A7A7).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Yb)
                .blastTemp(1097)
                .setHidden()
                .build();

        Yttrium = new Material.Builder(121, "yttrium")
                .ingot().fluid()
                .color(0x76524C).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Y)
                .blastTemp(1799)
                .build();

        Zinc = new Material.Builder(122, "zinc")
                .ingot(1).fluid().ore()
                .color(0xEBEBFA).iconSet(METALLIC)
                .flags(STD_METAL, MORTAR_GRINDABLE, GENERATE_FOIL, GENERATE_RING, GENERATE_FINE_WIRE)
                .element(Elements.Zn)
                .build();

        Zirconium = new Material.Builder(123, "zirconium")
                .ingot(6).fluid()
                .color(0xC8FFFF).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .element(Elements.Zr)
                .setHidden()
                .build();

        Naquadah = new Material.Builder(124, "naquadah")
                .ingot(4).fluid().ore()
                .color(0x323232, false).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FOIL, GENERATE_SPRING, GENERATE_FINE_WIRE)
                .element(Elements.Nq)
                .toolStats(6.0f, 4.0f, 1280, 21)
                .cableProperties(GTValues.V[7], 2, 2)
                .fluidPipeProperties(19200, 1500, true)
                .blastTemp(5000, GasTier.HIGH, VA[IV], 600)
                .build();

        NaquadahEnriched = new Material.Builder(125, "naquadah_enriched")
                .ingot(4).fluid().ore()
                .color(0x3C3C3C, false).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FOIL)
                .element(Elements.Nq1)
                .toolStats(6.0f, 4.0f, 1280, 21)
                .blastTemp(7000, GasTier.HIGH, VA[IV], 1000)
                .build();

        Naquadria = new Material.Builder(126, "naquadria")
                .ingot(3).fluid()
                .color(0x1E1E1E, false).iconSet(SHINY)
                .flags(EXT_METAL, GENERATE_FOIL, GENERATE_GEAR, GENERATE_DENSE, GENERATE_FINE_WIRE)
                .element(Elements.Nq2)
                .blastTemp(9000, GasTier.HIGH, VA[ZPM], 1200)
                .build();

        Neutronium = new Material.Builder(127, "neutronium")
                .ingot(6).fluid()
                .color(0xFAFAFA)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_FRAME)
                .element(Elements.Nt)
                .toolStats(24.0f, 12.0f, 655360, 21)
                .fluidPipeProperties(1000000, 2800, true)
                .build();

        Tritanium = new Material.Builder(128, "tritanium")
                .ingot(6).fluid()
                .color(0x600000).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_FRAME, GENERATE_RING, GENERATE_SPRING, GENERATE_SMALL_GEAR, GENERATE_ROTOR)
                .element(Elements.Tr)
                .toolStats(20.0f, 6.0f, 10240, 21)
                .build();

        Duranium = new Material.Builder(129, "duranium")
                .ingot(5).fluid()
                .color(0x4BAFAF).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FOIL, GENERATE_FINE_WIRE)
                .element(Elements.Dr)
                .toolStats(16.0f, 5.0f, 5120, 21)
                .cableProperties(GTValues.V[8], 1, 8)
                .fluidPipeProperties(100000, 2000, true)
                .build();

        Trinium = new Material.Builder(130, "trinium")
                .ingot(7).fluid()
                .color(0x9973BD).iconSet(SHINY)
                .flags(GENERATE_FOIL)
                .element(Elements.Ke)
                .blastTemp(8600, GasTier.HIGH, VA[LuV], 1500)
                .build();

        Adamantium = new Material.Builder(131, "adamantium")
                .ingot(7).fluid()
                .color(0x2D365C).iconSet(SHINY)
                .element(Elements.Ad)
                .blastTemp(10850)
                .setHidden()
                .build();

        Vibranium = new Material.Builder(132, "vibranium")
                .ingot(7).fluid()
                .color(0x828AAD).iconSet(SHINY)
                .element(Elements.Vb)
                .blastTemp(11220)
                .setHidden()
                .build();

        Taranium = new Material.Builder(133, "taranium")
                .ingot(7).fluid()
                .color(0x0C0C0D).iconSet(SHINY)
                .element(Elements.Tn)
                .blastTemp(10000)
                .setHidden()
                .build();
    }
}
