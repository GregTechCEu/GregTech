package gregtech.api.unification.material.materials;

import gregtech.api.unification.Elements;
import gregtech.api.unification.material.MaterialBuilder;
import gregtech.api.unification.material.MaterialBuilder.FluidType;

import static com.google.common.collect.ImmutableList.of;
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
                .blastTemp(1700)
                .build();

        Americium = new MaterialBuilder(3, "americium")
                .ingot(3).fluid()
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(STD_METAL, GENERATE_ROD, GENERATE_LONG_ROD)
                .element(Elements.Am)
                .build();

        Antimony = new MaterialBuilder(4, "antimony")
                .ingot().fluid()
                .color(0xDCDCF0).iconSet(SHINY)
                .flags(EXT_METAL, MORTAR_GRINDABLE)
                .element(Elements.Sb)
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
                .ingot(3).fluid()
                .color(0xFFE6E6).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE)
                .element(Elements.Cr)
                .toolStats(12.0f, 3.0f, 512)
                .blastTemp(1700)
                .build();

        Cobalt = new MaterialBuilder(23, "cobalt")
                .ingot().fluid().ore()
                .color(0x5050FA).iconSet(METALLIC)
                .flags(STD_METAL)
                .element(Elements.Co)
                .toolStats(10.0f, 3.0f, 256)
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
                .blastTemp(2719)
                .build();

        Iron = new MaterialBuilder(51, "iron")
                .ingot().fluid().plasma().ore()
                .color(0xC8C8C8).iconSet(METALLIC)
                .flags(EXT2_METAL, MORTAR_GRINDABLE, GENERATE_DENSE, GENERATE_FRAME, GENERATE_ROTOR, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .element(Elements.Fe)
                .toolStats(7.0f, 2.5f, 256)
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
                .build();

        Lithium = new MaterialBuilder(56, "lithium")
                .ingot().fluid().ore()
                .color(0xE1DCE1)
                .flags(STD_METAL)
                .element(Elements.Li)
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
                .build();

        Nihonium = new MaterialBuilder(70, "nihonium")
                .ingot().fluid()
                .color(0x08269E).iconSet(SHINY)
                .element(Elements.Nh)
                .build();

        Nihonium = new IngotMaterial(70, "nihonium" , 0x08269e, SHINY, 2, of(), 0, Elements.get("Nihonium"));
        Niobium = new IngotMaterial(71, "niobium", 0xBEB4C8, METALLIC, 2, of(), STD_METAL | GENERATE_ORE, Elements.get("Niobium"), 2750);
        Nitrogen = new FluidMaterial(72, "nitrogen", 0xFFFFFF, FLUID, of(), STATE_GAS | GENERATE_PLASMA, Elements.get("Nitrogen"));
        Nobelium = new IngotMaterial(73, "nobelium", 0xFFFFFF, SHINY, 2, of(), 0, Elements.get("Nobelium"));
        Oganesson = new IngotMaterial(74, "oganesson", 0x142d64, METALLIC, 3, of(), EXT2_METAL, Elements.get("Oganesson"));
        Osmium = new IngotMaterial(75, "osmium", 0x3232FF, METALLIC, 4, of(), GENERATE_ORE | EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_DENSE, Elements.get("Osmium"), 16.0F, 4.0f, 1280, 3306);
        Oxygen = new FluidMaterial(76, "oxygen", 0xFFFFFF, GAS, of(), STATE_GAS | GENERATE_PLASMA, Elements.get("Oxygen"));
        Palladium = new IngotMaterial(77, "palladium", 0x808080, SHINY, 2, of(), EXT2_METAL | GENERATE_ORE | GENERATE_FLUID_BLOCK, Elements.get("Palladium"), 8.0f, 2.0f, 512, 1228);
        Phosphorus = new DustMaterial(78, "phosphorus", 0xFFFF00, DULL, 2, of(), 0, Elements.get("Phosphorus"));
        Polonium = new IngotMaterial(79, "polonium", 0xC9D47E, DULL, 4, of(), 0, Elements.get("Polonium"));
        Platinum = new IngotMaterial(80, "platinum", 0xFFFFC8, SHINY, 2, of(), EXT2_METAL | GENERATE_ORE | GENERATE_FLUID_BLOCK, Elements.get("Platinum"));
        Plutonium239 = new IngotMaterial(81, "plutonium", 0xF03232, METALLIC, 3, of(), EXT_METAL, Elements.get("Plutonium-239"));
        Plutonium241 = new IngotMaterial(82, "plutonium241", 0xFA4646, SHINY, 3, of(), EXT_METAL, Elements.get("Plutonium-241"));
        Potassium = new IngotMaterial(83, "potassium", 0xFAFAFA, METALLIC, 1, of(), EXT_METAL, Elements.get("Potassium"));
        Praseodymium = new IngotMaterial(84, "praseodymium", 0xCECECE, METALLIC, 2, of(), EXT_METAL, Elements.get("Praseodymium"), 1208);
        Promethium = new IngotMaterial(85, "promethium", 0xFFFFFF, METALLIC, 2, of(), EXT_METAL, Elements.get("Promethium"), 1315);
        Protactinium = new IngotMaterial(86, "protactinium", 0xA78B6D, METALLIC, 3, of(), EXT2_METAL, Elements.get("Protactinium"));
        Radon = new FluidMaterial(87, "radon", 0xFFFFFF, GAS, of(), STATE_GAS, Elements.get("Radon"));
        Radium = new IngotMaterial(88, "radium", 0xFFC840, SHINY, 2, of(), 0, Elements.get("Radium"));
        Rhenium = new IngotMaterial(89, "rhenium", 0xb6bac3, SHINY, 2, of(), EXT2_METAL, Elements.get("Rhenium"));
        Rhodium = new IngotMaterial(90, "rhodium", 0xF4F4F4, METALLIC, 2, of(), EXT2_METAL, Elements.get("Rhodium"), 2237);
        Roentgenium = new IngotMaterial(91, "roentgenium" , 0xe3fdec, SHINY, 2, of(), 0, Elements.get("Roentgenium"));
        Rubidium = new IngotMaterial(92, "rubidium", 0xF01E1E, METALLIC, 2, of(), STD_METAL, Elements.get("Rubidium"));
        Ruthenium = new IngotMaterial(93, "ruthenium", 0x646464, METALLIC, 2, of(), EXT2_METAL, Elements.get("Ruthenium"), 2607);
        Rutherfordium = new IngotMaterial(94, "rutherfordium", 0xFFF6A1, SHINY, 7, of(), EXT2_METAL, Elements.get("Rutherfordium"));
        Samarium = new IngotMaterial(95, "samarium", 0xFFFFCC, METALLIC, 2, of(), STD_METAL, Elements.get("Samarium"), 1345);
        Scandium = new IngotMaterial(96, "scandium", 0xFFFFFF, METALLIC, 2, of(), STD_METAL, Elements.get("Scandium"), 1814);
        Seaborgium = new IngotMaterial(97, "seaborgium", 0x19c5ff, SHINY, 7, of(), 0, Elements.get("Seaborgium"));
        Selenium = new IngotMaterial(98, "selenium", 0xB6BA6B, SHINY, 2, of(), 0, Elements.get("Selenium"));
        Silicon = new IngotMaterial(99, "silicon", 0x3C3C50, METALLIC, 2, of(), STD_METAL | GENERATE_FOIL, Elements.get("Silicon"), 1687);
        Silver = new IngotMaterial(100, "silver", 0xDCDCFF, SHINY, 2, of(), EXT2_METAL | GENERATE_ORE | MORTAR_GRINDABLE, Elements.get("Silver"));
        Sodium = new IngotMaterial(101, "sodium", 0x000096, METALLIC, 2, of(), STD_METAL, Elements.get("Sodium"));
        Strontium = new IngotMaterial(102, "strontium", 0xC8C8C8, METALLIC, 2, of(), STD_METAL, Elements.get("Strontium"));
        Sulfur = new DustMaterial(103, "sulfur", 0xC8C800, DULL, 2, of(), NO_SMASHING | NO_SMELTING | FLAMMABLE | GENERATE_ORE, Elements.get("Sulfur"));
        Tantalum = new IngotMaterial(104, "tantalum", 0xFFFFFF, METALLIC, 2, of(), STD_METAL | GENERATE_FOIL, Elements.get("Tantalum"));
        Technetium = new IngotMaterial(105, "technetium", 0x545455, SHINY, 2, of(), 0, Elements.get("Technetium"));
        Tellurium = new IngotMaterial(106, "tellurium", 0xFFFFFF, METALLIC, 2, of(), STD_METAL, Elements.get("Tellurium"));
        Tennessine = new IngotMaterial(107, "tennessine" , 0x977fd6, SHINY, 2, of(), 0, Elements.get("Tennessine"));
        Terbium = new IngotMaterial(108, "terbium", 0xFFFFFF, METALLIC, 2, of(), STD_METAL, Elements.get("Terbium"), 1629);
        Thorium = new IngotMaterial(109, "thorium", 0x001E00, SHINY, 2, of(), STD_METAL | GENERATE_ORE, Elements.get("Thorium"), 6.0F, 2.0f, 512);
        Thallium = new IngotMaterial(110, "thallium", 0xc1c1de, SHINY, 2, of(), 0, Elements.get("Thallium"));
        Thulium = new IngotMaterial(111, "thulium", 0xFFFFFF, METALLIC, 2, of(), STD_METAL, Elements.get("Thulium"), 1818);
        Tin = new IngotMaterial(112, "tin", 0xDCDCDC, DULL, 1, of(), EXT2_METAL | MORTAR_GRINDABLE | GENERATE_RING | GENERATE_ROTOR | GENERATE_ORE, Elements.get("Tin"));
        Titanium = new IngotMaterial(113, "titanium", 0xDCA0F0, METALLIC, 3, of(), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_SPRING | GENERATE_FRAME | GENERATE_DENSE, Elements.get("Titanium"), 7.0F, 3.0f, 1600, 1941);
        Tritium = new FluidMaterial(114, "tritium", 0xFFFFFF, METALLIC, of(), STATE_GAS, Elements.get("Tritium"));
        Tungsten = new IngotMaterial(115, "tungsten", 0x323232, METALLIC, 3, of(), EXT2_METAL, Elements.get("Tungsten"), 7.0F, 3.0f, 2560, 3000);
        Uranium238 = new IngotMaterial(116, "uranium", 0x32F032, METALLIC, 3, of(), STD_METAL | GENERATE_ORE, Elements.get("Uranium-238"), 6.0F, 3.0f, 512);
        Uranium235 = new IngotMaterial(117, "uranium235", 0x46FA46, SHINY, 3, of(), STD_METAL | GENERATE_ORE | GENERATE_ROD, Elements.get("Uranium-235"), 6.0F, 3.0f, 512);
        Vanadium = new IngotMaterial(118, "vanadium", 0x323232, METALLIC, 2, of(), STD_METAL, Elements.get("Vanadium"), 2183);
        Xenon = new FluidMaterial(119, "xenon", 0xFFFFFF, GAS, of(), STATE_GAS, Elements.get("Xenon"));
        Ytterbium = new IngotMaterial(120, "ytterbium", 0xFFFFFF, METALLIC, 2, of(), STD_METAL, Elements.get("Ytterbium"), 1097);
        Yttrium = new IngotMaterial(121, "yttrium", 0xDCFADC, METALLIC, 2, of(), STD_METAL, Elements.get("Yttrium"), 1799);
        Zinc = new IngotMaterial(122, "zinc", 0xFAF0F0, METALLIC, 1, of(), STD_METAL | GENERATE_ORE | MORTAR_GRINDABLE | GENERATE_FOIL, Elements.get("Zinc"));
        Zirconium = new IngotMaterial(123, "zirconium", 0xE0E1E1, METALLIC, 6, of(), EXT2_METAL, Elements.get("Zirconium"));
    }
}
