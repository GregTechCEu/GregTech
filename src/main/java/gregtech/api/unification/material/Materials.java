package gregtech.api.unification.material;

import gregtech.api.GTValues;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.type.*;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.init.Enchantments;

import static com.google.common.collect.ImmutableList.of;
import static gregtech.api.unification.material.MaterialIconSet.*;
import static gregtech.api.unification.material.type.DustMaterial.MatFlags.*;
import static gregtech.api.unification.material.type.FluidMaterial.MatFlags.*;
import static gregtech.api.unification.material.type.GemMaterial.MatFlags.*;
import static gregtech.api.unification.material.type.IngotMaterial.MatFlags.*;
import static gregtech.api.unification.material.type.Material.MatFlags.*;
import static gregtech.api.unification.material.type.SimpleDustMaterial.GENERATE_SMALL_TINY;
import static gregtech.api.unification.material.type.SolidMaterial.MatFlags.*;

@SuppressWarnings("unused")
public class Materials {

    public static void register() {
        MarkerMaterials.register();
    }

    private static final long STD_SOLID = GENERATE_PLATE | GENERATE_ROD | GENERATE_BOLT_SCREW | GENERATE_LONG_ROD;
    private static final long STD_GEM = GENERATE_ORE | STD_SOLID | GENERATE_LENSE;
    private static final long STD_METAL = GENERATE_PLATE;
    private static final long EXT_METAL = STD_METAL | GENERATE_ROD | GENERATE_BOLT_SCREW | GENERATE_LONG_ROD;
    private static final long EXT2_METAL = EXT_METAL | GENERATE_GEAR | GENERATE_FOIL | GENERATE_FINE_WIRE | GENERATE_ROUND;

    public static final MarkerMaterial _NULL = new MarkerMaterial("_null");

    /**
     * Direct Elements
     */
    public static IngotMaterial Actinium = new IngotMaterial(1, "actinium", 0xC3D1FF, SHINY, 2, of(), 0, Elements.get("Actinium"));
    public static IngotMaterial Aluminium = new IngotMaterial(2, "aluminium", 0x80C8F0, DULL, 2, of(), EXT2_METAL | GENERATE_SMALL_GEAR | GENERATE_ORE | GENERATE_RING | GENERATE_FRAME, Elements.get("Aluminium"), 10.0F, 2.0f, 128, 1700);
    public static IngotMaterial Americium = new IngotMaterial(3, "americium", 0xC8C8C8, METALLIC, 3, of(), STD_METAL | GENERATE_ROD | GENERATE_LONG_ROD, Elements.get("Americium"));
    public static IngotMaterial Antimony = new IngotMaterial(4, "antimony", 0xDCDCF0, SHINY, 2, of(), EXT_METAL | MORTAR_GRINDABLE, Elements.get("Antimony"));
    public static FluidMaterial Argon = new FluidMaterial(5, "argon", 0x01FF01, FLUID, of(), STATE_GAS | GENERATE_PLASMA, Elements.get("Argon"));
    public static DustMaterial Arsenic = new DustMaterial(6, "arsenic", 0xFFFFFF, DULL, 2, of(), 0, Elements.get("Arsenic"));
    public static IngotMaterial Astatine = new IngotMaterial(7, "astatine", 0x241a24, DULL, 2, of(), 0, Elements.get("Astatine"));
    public static IngotMaterial Barium = new IngotMaterial(8, "barium", 0xFFFFFF, METALLIC, 2, of(), 0, Elements.get("Barium"));
    public static IngotMaterial Berkelium = new IngotMaterial(9, "berkelium", 0x645A88, METALLIC, 3, of(), EXT2_METAL, Elements.get("Berkelium"));
    public static IngotMaterial Beryllium = new IngotMaterial(10, "beryllium", 0x64B464, METALLIC, 2, of(), STD_METAL | GENERATE_ORE, Elements.get("Beryllium"));
    public static IngotMaterial Bismuth = new IngotMaterial(11, "bismuth", 0x64A0A0, METALLIC, 1, of(), GENERATE_ORE, Elements.get("Bismuth"));
    public static IngotMaterial Bohrium = new IngotMaterial(12, "bohrium", 0xdc57ff, SHINY, 7, of(), 0, Elements.get("Bohrium"));
    public static DustMaterial Boron = new DustMaterial(13, "boron", 0xD2FAD2, DULL, 2, of(), 0, Elements.get("Boron"));
    public static FluidMaterial Bromine = new FluidMaterial(14, "bromine", 0xB64D6B, SHINY, of(), 0, Elements.get("Bromine"));
    public static IngotMaterial Caesium = new IngotMaterial(15, "caesium", 0xFFFFFF, METALLIC, 2, of(), 0, Elements.get("Caesium"));
    public static IngotMaterial Calcium = new IngotMaterial(16, "calcium", 0xFFF5F5, METALLIC, 2, of(), 0, Elements.get("Calcium"));
    public static IngotMaterial Californium = new IngotMaterial(17, "californium", 0xA85A12, METALLIC, 3, of(), EXT2_METAL, Elements.get("Californium"));
    public static IngotMaterial Carbon = new IngotMaterial(18, "carbon", 0x141414, DULL, 2, of(), 0, Elements.get("Carbon"));
    public static IngotMaterial Cadmium = new IngotMaterial(19, "cadmium", 0x32323C, SHINY, 2, of(), 0, Elements.get("Cadmium"));
    public static IngotMaterial Cerium = new IngotMaterial(20, "cerium", 0xFFFFFF, METALLIC, 2, of(), 0, Elements.get("Cerium"), 1068);
    public static FluidMaterial Chlorine = new FluidMaterial(21, "chlorine", 0xFFFFFF, GAS, of(), STATE_GAS, Elements.get("Chlorine"));
    public static IngotMaterial Chrome = new IngotMaterial(22, "chrome", 0xFFE6E6, SHINY, 3, of(), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR, Elements.get("Chrome"), 12.0f, 3.0f, 512, 1700);
    public static IngotMaterial Cobalt = new IngotMaterial(23, "cobalt", 0x5050FA, METALLIC, 2, of(), GENERATE_ORE | STD_METAL, Elements.get("Cobalt"), 10.0F, 3.0f, 256);
    public static IngotMaterial Copernicium = new IngotMaterial(24, "copernicium", 0xFFFEFF, DULL, 4, of(), 0, Elements.get("Copernicium"));
    public static IngotMaterial Copper = new IngotMaterial(25, "copper", 0xFF6400, SHINY, 1, of(), EXT2_METAL | GENERATE_ORE | MORTAR_GRINDABLE | GENERATE_DENSE | GENERATE_SPRING, Elements.get("Copper"));
    public static IngotMaterial Curium = new IngotMaterial(26, "curium", 0x7B544E, METALLIC, 3, of(), EXT2_METAL, Elements.get("Curium"));
    public static IngotMaterial Darmstadtium = new IngotMaterial(27, "darmstadtium", 0xAAAAAA, METALLIC, 2, of(), 0, Elements.get("Darmstadtium"));
    public static FluidMaterial Deuterium = new FluidMaterial(28, "deuterium", 0xFFFFFF, FLUID, of(), STATE_GAS, Elements.get("Deuterium"));
    public static IngotMaterial Dubnium = new IngotMaterial(29, "dubnium", 0xD3FDFF, SHINY, 7, of(), EXT2_METAL, Elements.get("Dubnium"));
    public static IngotMaterial Dysprosium = new IngotMaterial(30, "dysprosium", 0xFFFFFF, METALLIC, 2, of(), 0, Elements.get("Dysprosium"), 1680);
    public static IngotMaterial Einsteinium = new IngotMaterial(31, "einsteinium", 0xCE9F00, METALLIC, 3, of(), EXT2_METAL, Elements.get("Einsteinium"));
    public static IngotMaterial Erbium = new IngotMaterial(32, "erbium", 0xFFFFFF, METALLIC, 2, of(), STD_METAL, Elements.get("Erbium"), 1802);
    public static IngotMaterial Europium = new IngotMaterial(33, "europium", 0xFFFFFF, METALLIC, 2, of(), STD_METAL | GENERATE_ROD, Elements.get("Europium"), 1099);
    public static IngotMaterial Fermium = new IngotMaterial(34, "fermium", 0x984ACF, METALLIC, 3, of(), EXT2_METAL, Elements.get("Fermium"));
    public static IngotMaterial Flerovium = new IngotMaterial(35, "flerovium", 0xFFFFFF, SHINY, 3, of(), EXT2_METAL, Elements.get("Flerovium"));
    public static FluidMaterial Fluorine = new FluidMaterial(36, "fluorine", 0xFFFFFF, GAS, of(), STATE_GAS, Elements.get("Fluorine")).setFluidTemperature(253);
    public static IngotMaterial Francium = new IngotMaterial(37, "francium", 0xAAAAAA, SHINY, 2, of(), 0, Elements.get("Francium"));
    public static IngotMaterial Gadolinium = new IngotMaterial(38, "gadolinium", 0xDDDDFF, METALLIC, 2, of(), 0, Elements.get("Gadolinium"), 1585);
    public static IngotMaterial Gallium = new IngotMaterial(39, "gallium", 0xDCDCFF, SHINY, 2, of(), GENERATE_PLATE | GENERATE_FOIL, Elements.get("Gallium"));
    public static IngotMaterial Germanium = new IngotMaterial(40, "germanium", 0x434343, SHINY, 2, of(), 0, Elements.get("Germanium"));
    public static IngotMaterial Gold = new IngotMaterial(41, "gold", 0xFFFF1E, SHINY, 2, of(), EXT2_METAL | GENERATE_ORE | MORTAR_GRINDABLE | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, Elements.get("Gold"));
    public static IngotMaterial Hafnium = new IngotMaterial(42, "hafnium", 0x99999a, SHINY, 2, of(), 0, Elements.get("Hafnium"));
    public static IngotMaterial Hassium = new IngotMaterial(43, "hassium", 0xDDDDDD, DULL, 3, of(), EXT2_METAL, Elements.get("Hassium"));
    public static IngotMaterial Holmium = new IngotMaterial(44, "holmium", 0xFFFFFF, METALLIC, 2, of(), 0, Elements.get("Holmium"), 1734);
    public static FluidMaterial Hydrogen = new FluidMaterial(45, "hydrogen", 0xFFFFFF, GAS, of(), STATE_GAS, Elements.get("Hydrogen"));
    public static FluidMaterial Helium = new FluidMaterial(46, "helium", 0xFFFFFF, GAS, of(), STATE_GAS | GENERATE_PLASMA, Elements.get("Helium"));
    public static FluidMaterial Helium3 = new FluidMaterial(47, "helium3", 0xFFFFFF, GAS, of(), STATE_GAS, Elements.get("Helium-3"));
    public static IngotMaterial Indium = new IngotMaterial(48, "indium", 0x400080, METALLIC, 2, of(), 0, Elements.get("Indium"));
    public static DustMaterial Iodine = new DustMaterial(49, "iodine", 0x2C344F, SHINY, 2, of(), 0, Elements.get("Iodine"));
    public static IngotMaterial Iridium = new IngotMaterial(50, "iridium", 0xF0F0F5, DULL, 3, of(), GENERATE_ORE | EXT2_METAL | GENERATE_ORE | GENERATE_RING | GENERATE_ROTOR, Elements.get("Iridium"), 7.0F, 3.0f, 2560, 2719);
    public static IngotMaterial Iron = new IngotMaterial(51, "iron", 0xC8C8C8, METALLIC, 2, of(), EXT2_METAL | GENERATE_ORE | MORTAR_GRINDABLE | GENERATE_RING | GENERATE_DENSE | GENERATE_FRAME | GENERATE_LONG_ROD | GENERATE_PLASMA | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, Elements.get("Iron"), 7.0F, 2.5f, 256);
    public static FluidMaterial Krypton = new FluidMaterial(52, "krypton", 0x31C42F, FLUID, of(), 0, Elements.get("Krypton"));
    public static IngotMaterial Lanthanum = new IngotMaterial(53, "lanthanum", 0xFFFFFF, METALLIC, 2, of(), 0, Elements.get("Lanthanum"), 1193);
    public static IngotMaterial Lawrencium = new IngotMaterial(54, "lawrencium", 0xFFFFFF, METALLIC, 3, of(), 0, Elements.get("Lawrencium"));
    public static IngotMaterial Lead = new IngotMaterial(55, "lead", 0x8C648C, DULL, 1, of(), EXT2_METAL | GENERATE_ORE | MORTAR_GRINDABLE | GENERATE_DENSE, Elements.get("Lead"));
    public static IngotMaterial Lithium = new IngotMaterial(56, "lithium", 0xE1DCE1, DULL, 2, of(), STD_METAL | GENERATE_ORE, Elements.get("Lithium"));
    public static IngotMaterial Livermorium = new IngotMaterial(57, "livermorium" , 0xc7b204, SHINY, 2, of(), 0, Elements.get("Livermorium"));
    public static IngotMaterial Lutetium = new IngotMaterial(58, "lutetium", 0xFFFFFF, METALLIC, 2, of(), 0, Elements.get("Lutetium"), 1925);
    public static IngotMaterial Magnesium = new IngotMaterial(59, "magnesium", 0xE1C8C8, METALLIC, 2, of(), 0, Elements.get("Magnesium"));
    public static IngotMaterial Mendelevium = new IngotMaterial(60, "mendelevium", 0x1D4ACF, METALLIC, 3, of(), EXT2_METAL, Elements.get("Mendelevium"));
    public static IngotMaterial Manganese = new IngotMaterial(61, "manganese", 0xFAFAFA, DULL, 2, of(), GENERATE_FOIL, Elements.get("Manganese"), 7.0F, 2.0f, 512);
    public static IngotMaterial Meitnerium = new IngotMaterial(62, "meitnerium" , 0x2246be, SHINY, 2, of(), 0, Elements.get("Meitnerium"));
    public static FluidMaterial Mercury = new FluidMaterial(63, "mercury", 0xFFDCDC, FLUID, of(), SMELT_INTO_FLUID, Elements.get("Mercury"));
    public static IngotMaterial Molybdenum = new IngotMaterial(64, "molybdenum", 0xB4B4DC, SHINY, 2, of(), GENERATE_ORE, Elements.get("Molybdenum"), 7.0F, 2.0f, 512);
    public static IngotMaterial Moscovium = new IngotMaterial(65, "moscovium" , 0x7854ad, SHINY, 2, of(), 0, Elements.get("Moscovium"));
    public static IngotMaterial Neodymium = new IngotMaterial(66, "neodymium", 0x646464, METALLIC, 2, of(), STD_METAL | GENERATE_ROD | GENERATE_ORE, Elements.get("Neodymium"), 7.0F, 2.0f, 512, 1297);
    public static FluidMaterial Neon = new FluidMaterial(67, "neon", 0xFF422A, FLUID, of(), 0, Elements.get("Neon"));
    public static IngotMaterial Neptunium = new IngotMaterial(68, "neptunium", 0x284D7B, METALLIC, 3, of(), EXT2_METAL, Elements.get("Neptunium"));
    public static IngotMaterial Nickel = new IngotMaterial(69, "nickel", 0xC8C8FA, METALLIC, 2, of(), STD_METAL | GENERATE_ORE | MORTAR_GRINDABLE | GENERATE_PLASMA, Elements.get("Nickel"));
    public static IngotMaterial Nihonium = new IngotMaterial(70, "nihonium" , 0x08269e, SHINY, 2, of(), 0, Elements.get("Nihonium"));
    public static IngotMaterial Niobium = new IngotMaterial(71, "niobium", 0xBEB4C8, METALLIC, 2, of(), STD_METAL | GENERATE_ORE, Elements.get("Niobium"), 2750);
    public static FluidMaterial Nitrogen = new FluidMaterial(72, "nitrogen", 0xFFFFFF, FLUID, of(), STATE_GAS | GENERATE_PLASMA, Elements.get("Nitrogen"));
    public static IngotMaterial Nobelium = new IngotMaterial(73, "nobelium", 0xFFFFFF, SHINY, 2, of(), 0, Elements.get("Nobelium"));
    public static IngotMaterial Oganesson = new IngotMaterial(74, "oganesson", 0x142d64, METALLIC, 3, of(), EXT2_METAL, Elements.get("Oganesson"));
    public static IngotMaterial Osmium = new IngotMaterial(75, "osmium", 0x3232FF, METALLIC, 4, of(), GENERATE_ORE | EXT2_METAL | GENERATE_RING | GENERATE_ROTOR, Elements.get("Osmium"), 16.0F, 4.0f, 1280, 3306);
    public static FluidMaterial Oxygen = new FluidMaterial(76, "oxygen", 0xFFFFFF, FLUID, of(), STATE_GAS | GENERATE_PLASMA, Elements.get("Oxygen"));
    public static IngotMaterial Palladium = new IngotMaterial(77, "palladium", 0x808080, SHINY, 2, of(), EXT2_METAL | GENERATE_ORE | GENERATE_FLUID_BLOCK, Elements.get("Palladium"), 8.0f, 2.0f, 512, 1228);
    public static DustMaterial Phosphorus = new DustMaterial(78, "phosphorus", 0xFFFF00, DULL, 2, of(), 0, Elements.get("Phosphorus"));
    public static IngotMaterial Polonium = new IngotMaterial(79, "polonium", 0xC9D47E, DULL, 4, of(), 0, Elements.get("Polonium"));
    public static IngotMaterial Platinum = new IngotMaterial(80, "platinum", 0xFFFFC8, SHINY, 2, of(), EXT2_METAL | GENERATE_ORE | GENERATE_FLUID_BLOCK, Elements.get("Platinum"));
    public static IngotMaterial Plutonium239 = new IngotMaterial(81, "plutonium", 0xF03232, METALLIC, 3, of(), EXT_METAL, Elements.get("Plutonium-239"));
    public static IngotMaterial Plutonium241 = new IngotMaterial(82, "plutonium241", 0xFA4646, SHINY, 3, of(), EXT_METAL, Elements.get("Plutonium-241"));
    public static IngotMaterial Potassium = new IngotMaterial(83, "potassium", 0xFAFAFA, METALLIC, 1, of(), EXT_METAL, Elements.get("Potassium"));
    public static IngotMaterial Praseodymium = new IngotMaterial(84, "praseodymium", 0xCECECE, METALLIC, 2, of(), EXT_METAL, Elements.get("Praseodymium"), 1208);
    public static IngotMaterial Promethium = new IngotMaterial(85, "promethium", 0xFFFFFF, METALLIC, 2, of(), EXT_METAL, Elements.get("Promethium"), 1315);
    public static IngotMaterial Protactinium = new IngotMaterial(86, "protactinium", 0xA78B6D, METALLIC, 3, of(), EXT2_METAL, Elements.get("Protactinium"));
    public static FluidMaterial Radon = new FluidMaterial(87, "radon", 0xFFFFFF, FLUID, of(), STATE_GAS, Elements.get("Radon"));
    public static IngotMaterial Radium = new IngotMaterial(88, "radium", 0xFFC840, SHINY, 2, of(), 0, Elements.get("Radium"));
    public static IngotMaterial Rhenium = new IngotMaterial(89, "rhenium", 0xb6bac3, SHINY, 2, of(), EXT2_METAL, Elements.get("Rhenium"));
    public static IngotMaterial Rhodium = new IngotMaterial(90, "rhodium", 0xF4F4F4, METALLIC, 2, of(), EXT2_METAL, Elements.get("Rhodium"), 2237);
    public static IngotMaterial Roentgenium = new IngotMaterial(91, "roentgenium" , 0xe3fdec, SHINY, 2, of(), 0, Elements.get("Roentgenium"));
    public static IngotMaterial Rubidium = new IngotMaterial(92, "rubidium", 0xF01E1E, METALLIC, 2, of(), STD_METAL, Elements.get("Rubidium"));
    public static IngotMaterial Ruthenium = new IngotMaterial(93, "ruthenium", 0x646464, METALLIC, 2, of(), EXT2_METAL, Elements.get("Ruthenium"), 2607);
    public static IngotMaterial Rutherfordium = new IngotMaterial(94, "rutherfordium", 0xFFF6A1, SHINY, 7, of(), EXT2_METAL, Elements.get("Rutherfordium"));
    public static IngotMaterial Samarium = new IngotMaterial(95, "samarium", 0xFFFFCC, METALLIC, 2, of(), STD_METAL, Elements.get("Samarium"), 1345);
    public static IngotMaterial Scandium = new IngotMaterial(96, "scandium", 0xFFFFFF, METALLIC, 2, of(), STD_METAL, Elements.get("Scandium"), 1814);
    public static IngotMaterial Seaborgium = new IngotMaterial(97, "seaborgium", 0x19c5ff, SHINY, 7, of(), 0, Elements.get("Seaborgium"));
    public static DustMaterial Selenium = new IngotMaterial(98, "selenium", 0xB6BA6B, SHINY, 2, of(), 0, Elements.get("Selenium"));
    public static IngotMaterial Silicon = new IngotMaterial(99, "silicon", 0x3C3C50, METALLIC, 2, of(), STD_METAL | GENERATE_FOIL, Elements.get("Silicon"), 1687);
    public static IngotMaterial Silver = new IngotMaterial(100, "silver", 0xDCDCFF, SHINY, 2, of(), EXT2_METAL | GENERATE_ORE | MORTAR_GRINDABLE, Elements.get("Silver"));
    public static IngotMaterial Sodium = new IngotMaterial(101, "sodium", 0x000096, METALLIC, 2, of(), STD_METAL, Elements.get("Sodium"));
    public static IngotMaterial Strontium = new IngotMaterial(102, "strontium", 0xC8C8C8, METALLIC, 2, of(), STD_METAL, Elements.get("Strontium"));
    public static DustMaterial Sulfur = new DustMaterial(103, "sulfur", 0xC8C800, DULL, 2, of(), NO_SMASHING | NO_SMELTING | FLAMMABLE | GENERATE_ORE, Elements.get("Sulfur"));
    public static IngotMaterial Tantalum = new IngotMaterial(104, "tantalum", 0xFFFFFF, METALLIC, 2, of(), STD_METAL | GENERATE_FOIL, Elements.get("Tantalum"));
    public static IngotMaterial Technetium = new IngotMaterial(105, "technetium", 0x545455, SHINY, 2, of(), 0, Elements.get("Technetium"));
    public static IngotMaterial Tellurium = new IngotMaterial(106, "tellurium", 0xFFFFFF, METALLIC, 2, of(), STD_METAL, Elements.get("Tellurium"));
    public static IngotMaterial Tennessine = new IngotMaterial(107, "tennessine" , 0x977fd6, SHINY, 2, of(), 0, Elements.get("Tennessine"));
    public static IngotMaterial Terbium = new IngotMaterial(108, "terbium", 0xFFFFFF, METALLIC, 2, of(), STD_METAL, Elements.get("Terbium"), 1629);
    public static IngotMaterial Thorium = new IngotMaterial(109, "thorium", 0x001E00, SHINY, 2, of(), STD_METAL | GENERATE_ORE, Elements.get("Thorium"), 6.0F, 2.0f, 512);
    public static IngotMaterial Thallium = new IngotMaterial(110, "thallium", 0xc1c1de, SHINY, 2, of(), 0, Elements.get("Thallium"));
    public static IngotMaterial Thulium = new IngotMaterial(111, "thulium", 0xFFFFFF, METALLIC, 2, of(), STD_METAL, Elements.get("Thulium"), 1818);
    public static IngotMaterial Tin = new IngotMaterial(112, "tin", 0xDCDCDC, DULL, 1, of(), EXT2_METAL | MORTAR_GRINDABLE | GENERATE_RING | GENERATE_ROTOR | GENERATE_ORE, Elements.get("Tin"));
    public static IngotMaterial Titanium = new IngotMaterial(113, "titanium", 0xDCA0F0, METALLIC, 3, of(), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_SPRING | GENERATE_FRAME | GENERATE_DENSE, Elements.get("Titanium"), 7.0F, 3.0f, 1600, 1941);
    public static FluidMaterial Tritium = new FluidMaterial(114, "tritium", 0xFFFFFF, METALLIC, of(), STATE_GAS, Elements.get("Tritium"));
    public static IngotMaterial Tungsten = new IngotMaterial(115, "tungsten", 0x323232, METALLIC, 3, of(), EXT2_METAL, Elements.get("Tungsten"), 7.0F, 3.0f, 2560, 3000);
    public static IngotMaterial Uranium238 = new IngotMaterial(116, "uranium", 0x32F032, METALLIC, 3, of(), STD_METAL | GENERATE_ORE, Elements.get("Uranium-238"), 6.0F, 3.0f, 512);
    public static IngotMaterial Uranium235 = new IngotMaterial(117, "uranium235", 0x46FA46, SHINY, 3, of(), STD_METAL | GENERATE_ORE | GENERATE_ROD, Elements.get("Uranium-235"), 6.0F, 3.0f, 512);
    public static IngotMaterial Vanadium = new IngotMaterial(118, "vanadium", 0x323232, METALLIC, 2, of(), STD_METAL, Elements.get("Vanadium"), 2183);
    public static FluidMaterial Xenon = new FluidMaterial(119, "xenon", 0x270095, FLUID, of(), 0, Elements.get("Xenon"));
    public static IngotMaterial Ytterbium = new IngotMaterial(120, "ytterbium", 0xFFFFFF, METALLIC, 2, of(), STD_METAL, Elements.get("Ytterbium"), 1097);
    public static IngotMaterial Yttrium = new IngotMaterial(121, "yttrium", 0xDCFADC, METALLIC, 2, of(), STD_METAL, Elements.get("Yttrium"), 1799);
    public static IngotMaterial Zinc = new IngotMaterial(122, "zinc", 0xFAF0F0, METALLIC, 1, of(), STD_METAL | GENERATE_ORE | MORTAR_GRINDABLE | GENERATE_FOIL, Elements.get("Zinc"));
    public static IngotMaterial Zirconium = new IngotMaterial(123, "zirconium", 0xE0E1E1, METALLIC, 6, of(), EXT2_METAL, Elements.get("Zirconium"));

    /**
     * First Degree Compounds
     */
    public static GemMaterial Almandine = new GemMaterial(124, "almandine", 0xFF0000, GEM_VERTICAL, 1, of(new MaterialStack(Aluminium, 2), new MaterialStack(Iron, 3), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 12)), STD_GEM);
    public static DustMaterial Andradite = new DustMaterial(125, "andradite", 0x967800, ROUGH, 1, of(new MaterialStack(Calcium, 3), new MaterialStack(Iron, 2), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 12)), 0);
    public static IngotMaterial AnnealedCopper = new IngotMaterial(126, "annealed_copper", 0xFF7814, SHINY, 2, of(new MaterialStack(Copper, 1)), EXT2_METAL | MORTAR_GRINDABLE);
    public static DustMaterial Asbestos = new DustMaterial(127, "asbestos", 0xE6E6E6, DULL, 1, of(new MaterialStack(Magnesium, 3), new MaterialStack(Silicon, 2), new MaterialStack(Hydrogen, 4), new MaterialStack(Oxygen, 9)), 0);
    public static DustMaterial Ash = new DustMaterial(128, "ash", 0x969696, DULL, 1, of(new MaterialStack(Carbon, 1)), DISABLE_DECOMPOSITION);
    public static DustMaterial BandedIron = new DustMaterial(129, "banded_iron", 0x915A5A, DULL, 2, of(new MaterialStack(Iron, 2), new MaterialStack(Oxygen, 3)), GENERATE_ORE);
    public static IngotMaterial BatteryAlloy = new IngotMaterial(130, "battery_alloy", 0x9C7CA0, DULL, 1, of(new MaterialStack(Lead, 4), new MaterialStack(Antimony, 1)), EXT_METAL);
    public static GemMaterial BlueTopaz = new GemMaterial(131, "blue_topaz", 0x0000FF, GEM_HORIZONTAL, 3, of(new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 1), new MaterialStack(Fluorine, 2), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 6)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, 7.0F, 3.0f, 256);
    public static DustMaterial Bone = new DustMaterial(132, "bone", 0xFAFAFA, DULL, 1, of(new MaterialStack(Calcium, 1)), MORTAR_GRINDABLE | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES);
    public static IngotMaterial Brass = new IngotMaterial(133, "brass", 0xFFB400, METALLIC, 1, of(new MaterialStack(Zinc, 1), new MaterialStack(Copper, 3)), EXT2_METAL | MORTAR_GRINDABLE | GENERATE_RING, 8.0F, 3.0f, 152);
    public static IngotMaterial Bronze = new IngotMaterial(134, "bronze", 0xFF8000, METALLIC, 2, of(new MaterialStack(Tin, 1), new MaterialStack(Copper, 3)), EXT2_METAL | MORTAR_GRINDABLE | GENERATE_RING | GENERATE_ROTOR | GENERATE_FRAME | GENERATE_LONG_ROD, 6.0F, 2.5f, 192);
    public static DustMaterial BrownLimonite = new DustMaterial(135, "brown_limonite", 0xC86400, METALLIC, 1, of(new MaterialStack(Iron, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(Oxygen, 2)), GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Calcite = new DustMaterial(136, "calcite", 0xFAE6DC, DULL, 1, of(new MaterialStack(Calcium, 1), new MaterialStack(Carbon, 1), new MaterialStack(Oxygen, 3)), GENERATE_ORE);
    public static DustMaterial Cassiterite = new DustMaterial(137, "cassiterite", 0xDCDCDC, METALLIC, 1, of(new MaterialStack(Tin, 1), new MaterialStack(Oxygen, 2)), GENERATE_ORE);
    public static DustMaterial CassiteriteSand = new DustMaterial(138, "cassiterite_sand", 0xDCDCDC, SAND, 1, of(new MaterialStack(Tin, 1), new MaterialStack(Oxygen, 2)), GENERATE_ORE);
    public static DustMaterial Chalcopyrite = new DustMaterial(139, "chalcopyrite", 0xA07828, DULL, 1, of(new MaterialStack(Copper, 1), new MaterialStack(Iron, 1), new MaterialStack(Sulfur, 2)), GENERATE_ORE | INDUCTION_SMELTING_LOW_OUTPUT);
    public static GemMaterial Charcoal = new GemMaterial(140, "charcoal", 0x644646, FINE, 1, of(new MaterialStack(Carbon, 1)), FLAMMABLE | NO_SMELTING | NO_SMASHING | MORTAR_GRINDABLE);
    public static DustMaterial Chromite = new DustMaterial(141, "chromite", 0x23140F, METALLIC, 1, of(new MaterialStack(Iron, 1), new MaterialStack(Chrome, 2), new MaterialStack(Oxygen, 4)), GENERATE_ORE, null);
    public static GemMaterial Cinnabar = new GemMaterial(142, "cinnabar", 0x960000, EMERALD, 1, of(new MaterialStack(Mercury, 1), new MaterialStack(Sulfur, 1)), GENERATE_ORE | CRYSTALLISABLE | DECOMPOSITION_BY_CENTRIFUGING);
    public static FluidMaterial Water = new FluidMaterial(143, "water", 0x0000FF, FLUID, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 1)), NO_RECYCLING | DISABLE_DECOMPOSITION);
    public static DustMaterial Clay = new DustMaterial(144, "clay", 0xC8C8DC, ROUGH, 1, of(new MaterialStack(Sodium, 2), new MaterialStack(Lithium, 1), new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 2), new MaterialStack(Water, 6)), MORTAR_GRINDABLE);
    public static GemMaterial Coal = new GemMaterial(145, "coal", 0x464646, LIGNITE, 1, of(new MaterialStack(Carbon, 1)), GENERATE_ORE | FLAMMABLE | NO_SMELTING | NO_SMASHING | MORTAR_GRINDABLE | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES | DISABLE_DECOMPOSITION);
    public static DustMaterial Cobaltite = new DustMaterial(146, "cobaltite", 0x5050FA, METALLIC, 1, of(new MaterialStack(Cobalt, 1), new MaterialStack(Arsenic, 1), new MaterialStack(Sulfur, 1)), GENERATE_ORE);
    public static DustMaterial Cooperite = new DustMaterial(147, "cooperite", 0xFFFFC8, METALLIC, 1, of(new MaterialStack(Platinum, 3), new MaterialStack(Nickel, 1), new MaterialStack(Sulfur, 1), new MaterialStack(Palladium, 1)), GENERATE_ORE);
    public static IngotMaterial Cupronickel = new IngotMaterial(148, "cupronickel", 0xE39680, METALLIC, 1, of(new MaterialStack(Copper, 1), new MaterialStack(Nickel, 1)), EXT_METAL| GENERATE_SPRING);
    public static DustMaterial DarkAsh = new DustMaterial(149, "dark_ash", 0x323232, DULL, 1, of(new MaterialStack(Carbon, 1)), DISABLE_DECOMPOSITION);
    public static GemMaterial Diamond = new GemMaterial(150, "diamond", 0xC8FFFF, DIAMOND, 3, of(new MaterialStack(Carbon, 1)), GENERATE_ROD | GENERATE_BOLT_SCREW | GENERATE_LENSE | GENERATE_GEAR | NO_SMASHING | NO_SMELTING | FLAMMABLE | HIGH_SIFTER_OUTPUT | GENERATE_ORE | DISABLE_DECOMPOSITION | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, 8.0F, 3.0f, 1280);
    public static IngotMaterial Electrum = new IngotMaterial(151, "electrum", 0xFFFF64, SHINY, 2, of(new MaterialStack(Silver, 1), new MaterialStack(Gold, 1)), EXT2_METAL | MORTAR_GRINDABLE);
    public static GemMaterial Emerald = new GemMaterial(152, "emerald", 0x50FF50, EMERALD, 2, of(new MaterialStack(Beryllium, 3), new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 6), new MaterialStack(Oxygen, 18)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, 10.0F, 2.0f, 368);
    public static DustMaterial Galena = new DustMaterial(153, "galena", 0x643C64, DULL, 3, of(new MaterialStack(Lead, 3), new MaterialStack(Silver, 3), new MaterialStack(Sulfur, 2)), GENERATE_ORE | NO_SMELTING);
    public static DustMaterial Garnierite = new DustMaterial(154, "garnierite", 0x32C846, METALLIC, 3, of(new MaterialStack(Nickel, 1), new MaterialStack(Oxygen, 1)), GENERATE_ORE);
    public static GemMaterial GreenSapphire = new GemMaterial(155, "green_sapphire", 0x64C882, GEM_HORIZONTAL, 2, of(new MaterialStack(Aluminium, 2), new MaterialStack(Oxygen, 3)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT | GENERATE_LENSE | GENERATE_PLATE, 8.0F, 3.0f, 368);
    public static DustMaterial Grossular = new DustMaterial(156, "grossular", 0xC86400, ROUGH, 1, of(new MaterialStack(Calcium, 3), new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 12)), GENERATE_ORE);
    public static DustMaterial Ice = new DustMaterial(157, "ice", 0xC8C8FF, SHINY, 0, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 1)), NO_SMASHING | NO_RECYCLING | SMELT_INTO_FLUID | EXCLUDE_BLOCK_CRAFTING_RECIPES | DISABLE_DECOMPOSITION);
    public static DustMaterial Ilmenite = new DustMaterial(158, "ilmenite", 0x463732, METALLIC, 3, of(new MaterialStack(Iron, 1), new MaterialStack(Titanium, 1), new MaterialStack(Oxygen, 3)), GENERATE_ORE | DISABLE_DECOMPOSITION);
    public static GemMaterial Rutile = new GemMaterial(159, "rutile", 0xD40D5C, GEM_HORIZONTAL, 2, of(new MaterialStack(Titanium, 1), new MaterialStack(Oxygen, 2)), STD_GEM | DISABLE_DECOMPOSITION);
    public static DustMaterial Bauxite = new DustMaterial(160, "bauxite", 0xC86400, DULL, 1, of(new MaterialStack(Rutile, 2), new MaterialStack(Aluminium, 16), new MaterialStack(Hydrogen, 10), new MaterialStack(Oxygen, 11)), GENERATE_ORE | DISABLE_DECOMPOSITION);
    public static IngotMaterial Invar = new IngotMaterial(161, "invar", 0xB4B478, METALLIC, 2, of(new MaterialStack(Iron, 2), new MaterialStack(Nickel, 1)), EXT2_METAL | MORTAR_GRINDABLE | GENERATE_RING | GENERATE_FRAME, 7.0F, 3.0f, 512);
    public static IngotMaterial Kanthal = new IngotMaterial(162, "kanthal", 0xC2D2DF, METALLIC, 2, of(new MaterialStack(Iron, 1), new MaterialStack(Aluminium, 1), new MaterialStack(Chrome, 1)), EXT_METAL| GENERATE_SPRING, null, 1800);
    public static GemMaterial Lazurite = new GemMaterial(163, "lazurite", 0x6478FF, LAPIS, 1, of(new MaterialStack(Aluminium, 6), new MaterialStack(Silicon, 6), new MaterialStack(Calcium, 8), new MaterialStack(Sodium, 8)), GENERATE_PLATE | GENERATE_ORE | NO_SMASHING | NO_SMELTING | CRYSTALLISABLE | GENERATE_ROD | DECOMPOSITION_BY_ELECTROLYZING);
    public static IngotMaterial Magnalium = new IngotMaterial(164, "magnalium", 0xC8BEFF, DULL, 2, of(new MaterialStack(Magnesium, 1), new MaterialStack(Aluminium, 2)), EXT2_METAL | GENERATE_LONG_ROD, 6.0F, 2.0f, 256);
    public static DustMaterial Magnesite = new DustMaterial(165, "magnesite", 0xFAFAB4, METALLIC, 2, of(new MaterialStack(Magnesium, 1), new MaterialStack(Carbon, 1), new MaterialStack(Oxygen, 3)), GENERATE_ORE);
    public static DustMaterial Magnetite = new DustMaterial(166, "magnetite", 0x1E1E1E, METALLIC, 2, of(new MaterialStack(Iron, 3), new MaterialStack(Oxygen, 4)), GENERATE_ORE);
    public static DustMaterial Molybdenite = new DustMaterial(167, "molybdenite", 0x191919, METALLIC, 2, of(new MaterialStack(Molybdenum, 1), new MaterialStack(Sulfur, 2)), GENERATE_ORE);
    public static IngotMaterial Nichrome = new IngotMaterial(168, "nichrome", 0xCDCEF6, METALLIC, 2, of(new MaterialStack(Nickel, 4), new MaterialStack(Chrome, 1)), EXT_METAL| GENERATE_SPRING, null, 2700);
    public static IngotMaterial NiobiumNitride = new IngotMaterial(169, "niobium_nitride", 0x1D291D, DULL, 2, of(new MaterialStack(Niobium, 1), new MaterialStack(Nitrogen, 1)), EXT_METAL, null, 2573);
    public static IngotMaterial NiobiumTitanium = new IngotMaterial(170, "niobium_titanium", 0x1D1D29, DULL, 2, of(new MaterialStack(Niobium, 1), new MaterialStack(Titanium, 1)), EXT2_METAL, null, 4500);
    public static DustMaterial Obsidian = new DustMaterial(171, "obsidian", 0x503264, DULL, 3, of(new MaterialStack(Magnesium, 1), new MaterialStack(Iron, 1), new MaterialStack(Silicon, 2), new MaterialStack(Oxygen, 8)), NO_SMASHING | EXCLUDE_BLOCK_CRAFTING_RECIPES);
    public static DustMaterial Phosphate = new DustMaterial(172, "phosphate", 0xFFFF00, DULL, 1, of(new MaterialStack(Phosphorus, 1), new MaterialStack(Oxygen, 4)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | FLAMMABLE | EXPLOSIVE);
    public static IngotMaterial PigIron = new IngotMaterial(173, "pig_iron", 0xC8B4B4, METALLIC, 2, of(new MaterialStack(Iron, 1)), EXT_METAL | GENERATE_RING, 6.0F, 4.0f, 384);
    public static IngotMaterial Polyethylene = new IngotMaterial(174, "plastic", 0xC8C8C8, DULL, 1, of(new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 2)), GENERATE_PLATE | GENERATE_FOIL | FLAMMABLE | NO_SMASHING | SMELT_INTO_FLUID | DISABLE_DECOMPOSITION); //todo add polyethylene oredicts
    public static IngotMaterial Epoxy = new IngotMaterial(175, "epoxy", 0xC88C14, DULL, 1, of(new MaterialStack(Carbon, 21), new MaterialStack(Hydrogen, 25), new MaterialStack(Chlorine, 1), new MaterialStack(Oxygen, 5)), EXT2_METAL | DISABLE_DECOMPOSITION | NO_SMASHING);
    public static DustMaterial Polysiloxane = new DustMaterial(176, "polysiloxane", 0xDCDCDC, DULL, 1, of(new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(Silicon, 2), new MaterialStack(Oxygen, 1)), GENERATE_PLATE | FLAMMABLE | NO_SMASHING | SMELT_INTO_FLUID | DISABLE_DECOMPOSITION);
    public static IngotMaterial Polycaprolactam = new IngotMaterial(177, "polycaprolactam", 0x323232, DULL, 1, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 11), new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 1)), GENERATE_PLATE | DISABLE_DECOMPOSITION | NO_SMASHING);
    public static IngotMaterial Polytetrafluoroethylene = new IngotMaterial(178, "polytetrafluoroethylene", 0x646464, DULL, 1, of(new MaterialStack(Carbon, 2), new MaterialStack(Fluorine, 4)), GENERATE_PLATE | GENERATE_FRAME | SMELT_INTO_FLUID  | DISABLE_DECOMPOSITION | NO_SMASHING);
    public static DustMaterial Powellite = new DustMaterial(179, "powellite", 0xFFFF00, DULL, 2, of(new MaterialStack(Calcium, 1), new MaterialStack(Molybdenum, 1), new MaterialStack(Oxygen, 4)), GENERATE_ORE);
    public static DustMaterial Pyrite = new DustMaterial(180, "pyrite", 0x967828, ROUGH, 1, of(new MaterialStack(Iron, 1), new MaterialStack(Sulfur, 2)), GENERATE_ORE | INDUCTION_SMELTING_LOW_OUTPUT);
    public static DustMaterial Pyrolusite = new DustMaterial(181, "pyrolusite", 0x9696AA, DULL, 2, of(new MaterialStack(Manganese, 1), new MaterialStack(Oxygen, 2)), GENERATE_ORE);
    public static DustMaterial Pyrope = new DustMaterial(182, "pyrope", 0x783264, METALLIC, 2, of(new MaterialStack(Aluminium, 2), new MaterialStack(Magnesium, 3), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 12)), GENERATE_ORE);
    public static DustMaterial RockSalt = new DustMaterial(183, "rock_salt", 0xF0C8C8, FINE, 1, of(new MaterialStack(Potassium, 1), new MaterialStack(Chlorine, 1)), GENERATE_ORE | NO_SMASHING);
    public static IngotMaterial Rubber = new IngotMaterial(184, "rubber", 0x000000, SHINY, 0, of(new MaterialStack(Carbon, 5), new MaterialStack(Hydrogen, 8)), GENERATE_PLATE | GENERATE_GEAR | GENERATE_RING | FLAMMABLE | NO_SMASHING | GENERATE_RING | DISABLE_DECOMPOSITION);
    public static GemMaterial Ruby = new GemMaterial(185, "ruby", 0xFF6464, RUBY, 2, of(new MaterialStack(Chrome, 1), new MaterialStack(Aluminium, 2), new MaterialStack(Oxygen, 3)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, 8.5F, 3.0f, 256);
    public static DustMaterial Salt = new DustMaterial(186, "salt", 0xFAFAFA, FINE, 1, of(new MaterialStack(Sodium, 1), new MaterialStack(Chlorine, 1)), GENERATE_ORE | NO_SMASHING);
    public static DustMaterial Saltpeter = new DustMaterial(187, "saltpeter", 0xE6E6E6, FINE, 1, of(new MaterialStack(Potassium, 1), new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 3)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | FLAMMABLE);
    public static GemMaterial Sapphire = new GemMaterial(188, "sapphire", 0x6464C8, GEM_VERTICAL, 2, of(new MaterialStack(Aluminium, 2), new MaterialStack(Oxygen, 3)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, null, 7.5F, 4.0f, 256);
    public static DustMaterial Scheelite = new DustMaterial(189, "scheelite", 0xC88C14, DULL, 3, of(new MaterialStack(Tungsten, 1), new MaterialStack(Calcium, 2), new MaterialStack(Oxygen, 4)), GENERATE_ORE | DECOMPOSITION_REQUIRES_HYDROGEN);
    public static GemMaterial Sodalite = new GemMaterial(190, "sodalite", 0x1414FF, LAPIS, 1, of(new MaterialStack(Aluminium, 3), new MaterialStack(Silicon, 3), new MaterialStack(Sodium, 4), new MaterialStack(Chlorine, 1)), GENERATE_ORE | GENERATE_PLATE | GENERATE_ROD | NO_SMASHING | NO_SMELTING | CRYSTALLISABLE | GENERATE_ROD | DECOMPOSITION_BY_ELECTROLYZING);
    public static DustMaterial Brick = new DustMaterial(191, "brick", 0x9B5643, ROUGH, 1, of(new MaterialStack(Clay, 1)), EXCLUDE_BLOCK_CRAFTING_RECIPES | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Fireclay = new DustMaterial(192, "fireclay", 0xADA09B, ROUGH, 2, of(new MaterialStack(Clay, 1), new MaterialStack(Brick, 1)), DECOMPOSITION_BY_CENTRIFUGING);
    public static GemMaterial Coke = new GemMaterial(193, "coke", 0x666666, LIGNITE, 1, of(new MaterialStack(Carbon, 1)), FLAMMABLE | NO_SMELTING | NO_SMASHING | MORTAR_GRINDABLE);


    public static IngotMaterial SolderingAlloy = new IngotMaterial(194, "soldering_alloy", 0xDCDCE6, DULL, 1, of(new MaterialStack(Tin, 9), new MaterialStack(Antimony, 1)), EXT_METAL | GENERATE_FINE_WIRE, null);
    public static DustMaterial Spessartine = new DustMaterial(195, "spessartine", 0xFF6464, DULL, 2, of(new MaterialStack(Aluminium, 2), new MaterialStack(Manganese, 3), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 12)), GENERATE_ORE);
    public static DustMaterial Sphalerite = new DustMaterial(196, "sphalerite", 0xFFFFFF, DULL, 1, of(new MaterialStack(Zinc, 1), new MaterialStack(Sulfur, 1)), GENERATE_ORE | INDUCTION_SMELTING_LOW_OUTPUT | DISABLE_DECOMPOSITION);
    public static IngotMaterial StainlessSteel = new IngotMaterial(197, "stainless_steel", 0xC8C8DC, SHINY, 2, of(new MaterialStack(Iron, 6), new MaterialStack(Chrome, 1), new MaterialStack(Manganese, 1), new MaterialStack(Nickel, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_FRAME | GENERATE_LONG_ROD, null, 7.0F, 4.0f, 480, 1700);
    public static IngotMaterial Steel = new IngotMaterial(198, "steel", 0x808080, METALLIC, 2, of(new MaterialStack(Iron, 1)), EXT2_METAL | MORTAR_GRINDABLE | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_DENSE | DISABLE_DECOMPOSITION | GENERATE_FRAME | GENERATE_LONG_ROD, null, 6.0F, 3.0f, 512, 1000);
    public static DustMaterial Stibnite = new DustMaterial(199, "stibnite", 0x464646, METALLIC, 2, of(new MaterialStack(Antimony, 2), new MaterialStack(Sulfur, 3)), GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING);
    public static GemMaterial Tanzanite = new GemMaterial(200, "tanzanite", 0x4000C8, GEM_VERTICAL, 2, of(new MaterialStack(Calcium, 2), new MaterialStack(Aluminium, 3), new MaterialStack(Silicon, 3), new MaterialStack(Hydrogen, 1), new MaterialStack(Oxygen, 13)), EXT_METAL | GENERATE_ORE | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, null, 7.0F, 2.0f, 256);
    public static DustMaterial Tetrahedrite = new DustMaterial(201, "tetrahedrite", 0xC82000, DULL, 2, of(new MaterialStack(Copper, 3), new MaterialStack(Antimony, 1), new MaterialStack(Sulfur, 3), new MaterialStack(Iron, 1)), GENERATE_ORE | INDUCTION_SMELTING_LOW_OUTPUT);
    public static IngotMaterial TinAlloy = new IngotMaterial(202, "tin_alloy", 0xC8C8C8, METALLIC, 2, of(new MaterialStack(Tin, 1), new MaterialStack(Iron, 1)), EXT2_METAL, null);
    public static GemMaterial Topaz = new GemMaterial(203, "topaz", 0xFF8000, GEM_HORIZONTAL, 3, of(new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 1), new MaterialStack(Fluorine, 2), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 6)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, null, 7.0F, 2.0f, 256);
    public static DustMaterial Tungstate = new DustMaterial(204, "tungstate", 0x373223, DULL, 3, of(new MaterialStack(Tungsten, 1), new MaterialStack(Lithium, 2), new MaterialStack(Oxygen, 4)), GENERATE_ORE | DECOMPOSITION_REQUIRES_HYDROGEN, null);
    public static IngotMaterial Ultimet = new IngotMaterial(205, "ultimet", 0xB4B4E6, SHINY, 4, of(new MaterialStack(Cobalt, 5), new MaterialStack(Chrome, 2), new MaterialStack(Nickel, 1), new MaterialStack(Molybdenum, 1)), EXT2_METAL, null, 9.0F, 4.0f, 2048, 2700);
    public static DustMaterial Uraninite = new DustMaterial(206, "uraninite", 0x232323, METALLIC, 3, of(new MaterialStack(Uranium238, 1), new MaterialStack(Oxygen, 2)), GENERATE_ORE | DISABLE_DECOMPOSITION);
    public static DustMaterial Uvarovite = new DustMaterial(207, "uvarovite", 0xB4FFB4, DIAMOND, 2, of(new MaterialStack(Calcium, 3), new MaterialStack(Chrome, 2), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 12)), 0);
    public static IngotMaterial VanadiumGallium = new IngotMaterial(208, "vanadium_gallium", 0x80808C, SHINY, 2, of(new MaterialStack(Vanadium, 3), new MaterialStack(Gallium, 1)), STD_METAL | GENERATE_FOIL | GENERATE_ROD, null, 4500);
    public static IngotMaterial WroughtIron = new IngotMaterial(209, "wrought_iron", 0xC8B4B4, METALLIC, 2, of(new MaterialStack(Iron, 1)), EXT2_METAL | MORTAR_GRINDABLE | GENERATE_RING | GENERATE_LONG_ROD | DISABLE_DECOMPOSITION, null, 6.0F, 3.5f, 384);
    public static DustMaterial Wulfenite = new DustMaterial(210, "wulfenite", 0xFF8000, DULL, 3, of(new MaterialStack(Lead, 1), new MaterialStack(Molybdenum, 1), new MaterialStack(Oxygen, 4)), GENERATE_ORE);
    public static DustMaterial YellowLimonite = new DustMaterial(211, "yellow_limonite", 0xC8C800, METALLIC, 2, of(new MaterialStack(Iron, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(Oxygen, 2)), GENERATE_ORE | INDUCTION_SMELTING_LOW_OUTPUT | DECOMPOSITION_BY_CENTRIFUGING);
    public static IngotMaterial YttriumBariumCuprate = new IngotMaterial(212, "yttrium_barium_cuprate", 0x504046, METALLIC, 2, of(new MaterialStack(Yttrium, 1), new MaterialStack(Barium, 2), new MaterialStack(Copper, 3), new MaterialStack(Oxygen, 7)), EXT_METAL | GENERATE_FOIL | GENERATE_FINE_WIRE, null, 4500);
    public static GemMaterial NetherQuartz = new GemMaterial(213, "nether_quartz", 0xE6D2D2, QUARTZ, 1, of(), STD_SOLID | NO_SMELTING | CRYSTALLISABLE | GENERATE_ORE | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES);
    public static GemMaterial CertusQuartz = new GemMaterial(214, "certus_quartz", 0xD2D2E6, QUARTZ, 1, of(), STD_SOLID | NO_SMELTING | CRYSTALLISABLE | GENERATE_ORE);
    public static GemMaterial Quartzite = new GemMaterial(215, "quartzite", 0xD2E6D2, QUARTZ, 1, of(), NO_SMELTING | CRYSTALLISABLE | GENERATE_ORE);
    public static IngotMaterial Graphite = new IngotMaterial(216, "graphite", 0x808080, DULL, 2, of(), GENERATE_PLATE | GENERATE_ORE | NO_SMELTING | FLAMMABLE);
    public static IngotMaterial Graphene = new IngotMaterial(217, "graphene", 0x808080, SHINY, 2, of(), GENERATE_PLATE | GENERATE_FOIL);
    public static GemMaterial Jasper = new GemMaterial(218, "jasper", 0xC85050, EMERALD, 2, of(), STD_GEM | NO_SMELTING | HIGH_SIFTER_OUTPUT);
    public static IngotMaterial Osmiridium = new IngotMaterial(219, "osmiridium", 0x6464FF, METALLIC, 3, of(new MaterialStack(Iridium, 3), new MaterialStack(Osmium, 1)), EXT2_METAL, null, 9.0F, 3.0f, 3152, 2500);
    public static DustMaterial Tenorite = new DustMaterial(220, "tenorite", 0x606060, DULL, 1, of(new MaterialStack(Copper, 1), new MaterialStack(Oxygen, 1)), GENERATE_ORE);
    public static DustMaterial Cuprite = new DustMaterial(221, "cuprite", 0x770000, RUBY, 2, of(new MaterialStack(Copper, 2), new MaterialStack(Oxygen, 1)), GENERATE_ORE);
    public static DustMaterial Bornite = new DustMaterial(222, "bornite", 0x97662B, METALLIC, 1, of(new MaterialStack(Copper, 5), new MaterialStack(Iron, 1), new MaterialStack(Sulfur, 4)), GENERATE_ORE);
    public static DustMaterial Chalcocite = new DustMaterial(223, "chalcocite", 0x353535, GEM_VERTICAL, 2, of(new MaterialStack(Copper, 2), new MaterialStack(Sulfur, 1)), GENERATE_ORE);
    public static DustMaterial Enargite = new DustMaterial(224, "enargite", 0xBBBBBB, METALLIC, 2, of(new MaterialStack(Copper, 3), new MaterialStack(Arsenic, 1), new MaterialStack(Sulfur, 4)), GENERATE_ORE);
    public static DustMaterial Tennantite = new DustMaterial(225, "tennantite", 0x909090, METALLIC, 2, of(new MaterialStack(Copper, 12), new MaterialStack(Arsenic, 4), new MaterialStack(Sulfur, 13)), GENERATE_ORE);

    public static IngotMaterial GalliumArsenide = new IngotMaterial(226, "gallium_arsenide", 0xA0A0A0, DULL, 1, of(new MaterialStack(Arsenic, 1), new MaterialStack(Gallium, 1)), DECOMPOSITION_BY_CENTRIFUGING | GENERATE_PLATE, null, 1200);
    public static DustMaterial Potash = new DustMaterial(227, "potash", 0x784137, DULL, 1, of(new MaterialStack(Potassium, 2), new MaterialStack(Oxygen, 1)), 0);
    public static DustMaterial SodaAsh = new DustMaterial(228, "soda_ash", 0xDCDCFF, DULL, 1, of(new MaterialStack(Sodium, 2), new MaterialStack(Carbon, 1), new MaterialStack(Oxygen, 3)), 0);
    public static IngotMaterial IndiumGalliumPhosphide = new IngotMaterial(229, "indium_gallium_phosphide", 0xA08CBE, DULL, 1, of(new MaterialStack(Indium, 1), new MaterialStack(Gallium, 1), new MaterialStack(Phosphorus, 1)), DECOMPOSITION_BY_CENTRIFUGING | GENERATE_PLATE);
    public static IngotMaterial NickelZincFerrite = new IngotMaterial(230, "nickel_zinc_ferrite", 0x3C3C3C, METALLIC, 0, of(new MaterialStack(Nickel, 1), new MaterialStack(Zinc, 1), new MaterialStack(Iron, 4), new MaterialStack(Oxygen, 8)), EXT_METAL | GENERATE_RING, null, 1500);
    public static DustMaterial SiliconDioxide = new DustMaterial(231, "silicon_dioxide", 0xC8C8C8, QUARTZ, 1, of(new MaterialStack(Silicon, 1), new MaterialStack(Oxygen, 2)), NO_SMASHING | NO_SMELTING | CRYSTALLISABLE);

    /**
     * Water Related
     */
    public static FluidMaterial Steam = new FluidMaterial(232, "steam", 0xFFFFFF, GAS, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 1)), NO_RECYCLING | GENERATE_FLUID_BLOCK | DISABLE_DECOMPOSITION).setFluidTemperature(380);
    public static FluidMaterial DistilledWater = new FluidMaterial(233, "distilled_water", 0x0000FF, FLUID, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 1)), NO_RECYCLING | DISABLE_DECOMPOSITION);
    public static FluidMaterial SodiumPersulfate = new FluidMaterial(244, "sodium_persulfate", 0xFFFFFF, FLUID, of(new MaterialStack(Sodium, 2), new MaterialStack(Sulfur, 2), new MaterialStack(Oxygen, 8)), 0);

    /**
     * Marked for conversion to simple
     */
    public static SimpleFluidMaterial Methane = new SimpleFluidMaterial(1, "methane", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 4)), 0);
    public static SimpleFluidMaterial CarbonDioxide = new SimpleFluidMaterial(2, "carbon_dioxide", 0xA9D0F5, FLUID, of(new MaterialStack(Carbon, 1), new MaterialStack(Oxygen, 2)), 0);
    public static SimpleFluidMaterial NobleGases = new SimpleFluidMaterial(3, "noble_gases", 0xA9D0F5, FLUID, of(new MaterialStack(CarbonDioxide, 25), new MaterialStack(Helium, 11), new MaterialStack(Methane, 4), new MaterialStack(Deuterium, 2), new MaterialStack(Radon, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Air = new SimpleFluidMaterial(4, "air", 0xA9D0F5, FLUID, of(new MaterialStack(Nitrogen, 40), new MaterialStack(Oxygen, 11), new MaterialStack(Argon, 1), new MaterialStack(NobleGases, 1)), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial LiquidAir = new SimpleFluidMaterial(5, "liquid_air", 0xA9D0F5, FLUID, of(new MaterialStack(Nitrogen, 40), new MaterialStack(Oxygen, 11), new MaterialStack(Argon, 1), new MaterialStack(NobleGases, 1)), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial TitaniumTetrachloride = new SimpleFluidMaterial(6, "titanium_tetrachloride", 0xD40D5C, FLUID, of(new MaterialStack(Titanium, 1), new MaterialStack(Chlorine, 4)), DISABLE_DECOMPOSITION).setFluidTemperature(2200);
    public static SimpleFluidMaterial NitrogenDioxide = new SimpleFluidMaterial(7, "nitrogen_dioxide", 0xFFFFFF, FLUID, of(new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 2)), 0);
    public static SimpleFluidMaterial HydrogenSulfide = new SimpleFluidMaterial(8, "hydrogen_sulfide", 0xFFFFFF, FLUID, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Sulfur, 1)), 0);
    public static SimpleFluidMaterial Epichlorohydrin = new SimpleFluidMaterial(9, "epichlorohydrin", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 5), new MaterialStack(Chlorine, 1), new MaterialStack(Oxygen, 1)), 0);
    public static SimpleFluidMaterial NitricAcid = new SimpleFluidMaterial(10, "nitric_acid", 0xCCCC00, FLUID, of(new MaterialStack(Hydrogen, 1), new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 3)), 0);
    public static SimpleFluidMaterial SulfuricAcid = new SimpleFluidMaterial(11, "sulfuric_acid", 0xFFFFFF, FLUID, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 4)), 0);
    public static SimpleFluidMaterial NitrationMixture = new SimpleFluidMaterial(12, "nitration_mixture", 0xE6E2AB, FLUID, of(new MaterialStack(NitricAcid, 1), new MaterialStack(SulfuricAcid, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial PhosphoricAcid = new SimpleFluidMaterial(13, "phosphoric_acid", 0xDCDC01, FLUID, of(new MaterialStack(Hydrogen, 3), new MaterialStack(Phosphorus, 1), new MaterialStack(Oxygen, 4)), 0);
    public static SimpleFluidMaterial SulfurTrioxide = new SimpleFluidMaterial(14, "sulfur_trioxide", 0xA0A014, GAS, of(new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 3)), STATE_GAS);
    public static SimpleFluidMaterial SulfurDioxide = new SimpleFluidMaterial(15, "sulfur_dioxide", 0xC8C819, GAS, of(new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 2)), STATE_GAS);
    public static SimpleFluidMaterial CarbonMonoxide = new SimpleFluidMaterial(16, "carbon_monoxide", 0x0E4880, GAS, of(new MaterialStack(Carbon, 1), new MaterialStack(Oxygen, 1)), STATE_GAS);
    public static SimpleFluidMaterial DilutedSulfuricAcid = new SimpleFluidMaterial(17, "diluted_sulfuric_acid", 0xC07820, FLUID, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 4)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial HydrochloricAcid = new SimpleFluidMaterial(18, "hydrochloric_acid", 0xFFFFFF, FLUID, of(new MaterialStack(Hydrogen, 1), new MaterialStack(Chlorine, 1)), 0);
    public static SimpleFluidMaterial DilutedHydrochloricAcid = new SimpleFluidMaterial(19, "diluted_hydrochloric_acid", 0x99A7A3, FLUID, of(new MaterialStack(Hydrogen, 1), new MaterialStack(Chlorine, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial HypochlorousAcid = new SimpleFluidMaterial(20, "hypochlorous_acid", 0x6F8A91, FLUID, of(new MaterialStack(Hydrogen, 1), new MaterialStack(Chlorine, 1), new MaterialStack(Oxygen, 1)), 0);
    public static SimpleFluidMaterial Ammonia = new SimpleFluidMaterial(21, "ammonia", 0x3F3480, GAS, of(new MaterialStack(Nitrogen, 1), new MaterialStack(Hydrogen, 3)), STATE_GAS);
    public static SimpleFluidMaterial Chloramine = new SimpleFluidMaterial(22, "chloramine", 0x3F9F80, GAS, of(new MaterialStack(Nitrogen, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(HydrochloricAcid, 1)), STATE_GAS);
    public static SimpleFluidMaterial NickelSulfateSolution = new SimpleFluidMaterial(23, "nickel_sulfate_water_solution", 0x3EB640, FLUID, of(new MaterialStack(Nickel, 1), new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 4), new MaterialStack(Water, 6)), 0);
    public static SimpleFluidMaterial CopperSulfateSolution = new SimpleFluidMaterial(24, "blue_vitriol_water_solution", 0x48A5C0, FLUID, of(new MaterialStack(Copper, 1), new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 4), new MaterialStack(Water, 5)), 0);
    public static SimpleFluidMaterial LeadZincSolution = new SimpleFluidMaterial(25, "lead_zinc_solution", 0xFFFFFF, FLUID, of(new MaterialStack(Lead, 1), new MaterialStack(Silver, 1), new MaterialStack(Zinc, 1), new MaterialStack(Sulfur, 3), new MaterialStack(Water, 1)), DECOMPOSITION_BY_CENTRIFUGING);
    public static SimpleFluidMaterial HydrofluoricAcid = new SimpleFluidMaterial(26, "hydrofluoric_acid", 0x0088AA, FLUID, of(new MaterialStack(Hydrogen, 1), new MaterialStack(Fluorine, 1)), 0);
    public static SimpleFluidMaterial NitricOxide = new SimpleFluidMaterial(27, "nitric_oxide", 0x7DC8F0, GAS, of(new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 1)), STATE_GAS);
    public static SimpleFluidMaterial Chloroform = new SimpleFluidMaterial(28, "chloroform", 0x892CA0, FLUID, of(new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(Chlorine, 3)), 0);
    public static SimpleFluidMaterial Cumene = new SimpleFluidMaterial(29, "cumene", 0x552200, FLUID, of(new MaterialStack(Carbon, 9), new MaterialStack(Hydrogen, 12)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Tetrafluoroethylene = new SimpleFluidMaterial(30, "tetrafluoroethylene", 0x7D7D7D, GAS, of(new MaterialStack(Carbon, 2), new MaterialStack(Fluorine, 4)), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Chloromethane = new SimpleFluidMaterial(31, "chloromethane", 0xC82CA0, GAS, of(new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 3), new MaterialStack(Chlorine, 1)), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial AllylChloride = new SimpleFluidMaterial(32, "allyl_chloride", 0x87DEAA, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Methane, 1), new MaterialStack(HydrochloricAcid, 1)), 0);
    public static SimpleFluidMaterial Isoprene = new SimpleFluidMaterial(33, "isoprene", 0x141414, FLUID, of(new MaterialStack(Carbon, 5), new MaterialStack(Hydrogen, 8)), 0);
    public static SimpleFluidMaterial Propane = new SimpleFluidMaterial(34, "propane", 0xFAE250, GAS, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 8)), STATE_GAS);
    public static SimpleFluidMaterial Propene = new SimpleFluidMaterial(35, "propene", 0xFFDD55, GAS, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 6)), STATE_GAS);
    public static SimpleFluidMaterial Ethane = new SimpleFluidMaterial(36, "ethane", 0xC8C8FF, GAS, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 6)), STATE_GAS);
    public static SimpleFluidMaterial Butene = new SimpleFluidMaterial(37, "butene", 0xCF5005, GAS, of(new MaterialStack(Carbon, 4), new MaterialStack(Hydrogen, 8)), STATE_GAS);
    public static SimpleFluidMaterial Butane = new SimpleFluidMaterial(38, "butane", 0xB6371E, GAS, of(new MaterialStack(Carbon, 4), new MaterialStack(Hydrogen, 10)), STATE_GAS);
    public static SimpleFluidMaterial DissolvedCalciumAcetate = new SimpleFluidMaterial(39, "dissolved_calcium_acetate", 0xDCC8B4, FLUID, of(new MaterialStack(Calcium, 1), new MaterialStack(Carbon, 4), new MaterialStack(Oxygen, 4), new MaterialStack(Hydrogen, 6), new MaterialStack(Water, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial VinylAcetate = new SimpleFluidMaterial(40, "vinyl_acetate", 0xE1B380, FLUID, of(new MaterialStack(Carbon, 4), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial MethylAcetate = new SimpleFluidMaterial(41, "methyl_acetate", 0xEEC6AF, FLUID, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Ethenone = new SimpleFluidMaterial(42, "ethenone", 0x141446, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Tetranitromethane = new SimpleFluidMaterial(43, "tetranitromethane", 0x0F2828, FLUID, of(new MaterialStack(Carbon, 1), new MaterialStack(Nitrogen, 4), new MaterialStack(Oxygen, 8)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Dimethylamine = new SimpleFluidMaterial(44, "dimethylamine", 0x554469, GAS, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 7), new MaterialStack(Nitrogen, 1)), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Dimethylhydrazine = new SimpleFluidMaterial(45, "dimethylhidrazine", 0x000055, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 8), new MaterialStack(Nitrogen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial DinitrogenTetroxide = new SimpleFluidMaterial(46, "dinitrogen_tetroxide", 0x004184, GAS, of(new MaterialStack(Nitrogen, 2), new MaterialStack(Oxygen, 4)), STATE_GAS);
    public static SimpleFluidMaterial Dimethyldichlorosilane = new SimpleFluidMaterial(47, "dimethyldichlorosilane", 0x441650, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 6), new MaterialStack(Chlorine, 2), new MaterialStack(Silicon, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Styrene = new SimpleFluidMaterial(48, "styrene", 0xD2C8BE, FLUID, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 8)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Butadiene = new SimpleFluidMaterial(49, "butadiene", 11885072, GAS, of(new MaterialStack(Carbon, 4), new MaterialStack(Hydrogen, 6)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Dichlorobenzene = new SimpleFluidMaterial(50, "dichlorobenzene", 0x004455, FLUID, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 4), new MaterialStack(Chlorine, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial AceticAcid = new SimpleFluidMaterial(51, "acetic_acid", 0xC8B4A0, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 4), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Phenol = new SimpleFluidMaterial(52, "phenol", 0x784421, FLUID, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial BisphenolA = new SimpleFluidMaterial(53, "bisphenol_a", 0xD4AA00, FLUID, of(new MaterialStack(Carbon, 15), new MaterialStack(Hydrogen, 16), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial VinylChloride = new SimpleFluidMaterial(54, "vinyl_chloride", 0xE1F0F0, GAS, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 3), new MaterialStack(Chlorine, 1)), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Ethylene = new SimpleFluidMaterial(55, "ethylene", 0xE1E1E1, GAS, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 4)), STATE_GAS);
    public static SimpleFluidMaterial Benzene = new SimpleFluidMaterial(56, "benzene", 0x1A1A1A, FLUID, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 6)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Acetone = new SimpleFluidMaterial(57, "acetone", 0xAFAFAF, FLUID, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Glycerol = new SimpleFluidMaterial(58, "glycerol", 0x87DE87, FLUID, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 8), new MaterialStack(Oxygen, 3)), 0);
    public static SimpleFluidMaterial Methanol = new SimpleFluidMaterial(59, "methanol", 0xAA8800, FLUID, of(new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 4), new MaterialStack(Oxygen, 1)), 0);
    public static SimpleFluidMaterial SaltWater = new SimpleFluidMaterial(60, "salt_water", 0x0000C8, FLUID, of(new MaterialStack(Salt, 1), new MaterialStack(Water, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial WoodGas = new SimpleFluidMaterial(61, "wood_gas", 0xDECD87, GAS, of(), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial WoodVinegar = new SimpleFluidMaterial(62, "wood_vinegar", 0xD45500, FLUID, of(), 0);
    public static SimpleFluidMaterial WoodTar = new SimpleFluidMaterial(63, "wood_tar", 0x28170B, FLUID, of(), 0);
    public static SimpleFluidMaterial CharcoalByproducts = new SimpleFluidMaterial(64, "charcoal_byproducts", 0x784421, FLUID, of(), 0);
    public static SimpleFluidMaterial Biomass = new SimpleFluidMaterial(65, "biomass", 0x00FF00, FLUID, of(), 0);
    public static SimpleFluidMaterial BioDiesel = new SimpleFluidMaterial(66, "bio_diesel", 0xFF8000, FLUID, of(), 0);
    public static SimpleFluidMaterial FermentedBiomass = new SimpleFluidMaterial(67, "fermented_biomass", 0x445500, FLUID, of(), 0);
    public static SimpleFluidMaterial Creosote = new SimpleFluidMaterial(68, "creosote", 0x804000, FLUID, of(), 0);
    public static SimpleFluidMaterial Ethanol = new SimpleFluidMaterial(69, "ethanol", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Diesel = new SimpleFluidMaterial(70, "fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial RocketFuel = new SimpleFluidMaterial(71, "rocket_fuel", 0xBDB78C, FLUID, of(), 0);
    public static SimpleFluidMaterial Glue = new SimpleFluidMaterial(72, "glue", 0xC8C400, FLUID, of(), 0);
    public static SimpleFluidMaterial Lubricant = new SimpleFluidMaterial(73, "lubricant", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial McGuffium239 = new SimpleFluidMaterial(74, "mc_guffium239", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial IndiumConcentrate = new SimpleFluidMaterial(75, "indium_concentrate", 0x0e2950, FLUID, of(), 0);
    public static SimpleFluidMaterial SeedOil = new SimpleFluidMaterial(76, "seed_oil", 0xC4FF00, FLUID, of(), 0);
    public static SimpleFluidMaterial DrillingFluid = new SimpleFluidMaterial(77, "drilling_fluid", 0xFFFFAA, FLUID, of(), 0);
    public static SimpleFluidMaterial ConstructionFoam = new SimpleFluidMaterial(78, "construction_foam", 0x808080, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedEthane = new SimpleFluidMaterial(79, "hydrocracked_ethane", 0x9696BC, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedEthylene = new SimpleFluidMaterial(80, "hydrocracked_ethylene", 0xA3A3A0, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial HydroCrackedPropene = new SimpleFluidMaterial(81, "hydrocracked_propene", 0xBEA540, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedPropane = new SimpleFluidMaterial(82, "hydrocracked_propane", 0xBEA540, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedLightFuel = new SimpleFluidMaterial(83, "hydrocracked_light_fuel", 0xB7AF08, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedButane = new SimpleFluidMaterial(84, "hydrocracked_butane", 0x852C18, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedNaphtha = new SimpleFluidMaterial(85, "hydrocracked_naphtha", 0xBFB608, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedHeavyFuel = new SimpleFluidMaterial(86, "hydrocracked_heavy_fuel", 0xFFFF00, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedGas = new SimpleFluidMaterial(87, "hydrocracked_gas", 0xB4B4B4, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedButene = new SimpleFluidMaterial(88, "hydrocracked_butene", 0x993E05, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedButadiene = new SimpleFluidMaterial(89, "hydrocracked_butadiene", 0xAD5203, FLUID, of(), 0);
    public static SimpleFluidMaterial SteamCrackedEthane = new SimpleFluidMaterial(90, "steamcracked_ethane", 0x9696BC, FLUID, of(), 0);
    public static SimpleFluidMaterial SteamCrackedEthylene = new SimpleFluidMaterial(91, "steamcracked_ethylene", 0xA3A3A0, GAS, of(), 0);
    public static SimpleFluidMaterial SteamCrackedPropene = new SimpleFluidMaterial(92, "steamcracked_propene", 0xBEA540, FLUID, of(), 0);
    public static SimpleFluidMaterial SteamCrackedPropane = new SimpleFluidMaterial(93, "steamcracked_propane", 0xBEA540, FLUID, of(), 0);
    public static SimpleFluidMaterial SteamCrackedButane = new SimpleFluidMaterial(94, "steamcracked_butane", 0x852C18, FLUID, of(), 0);
    public static SimpleFluidMaterial SteamCrackedNaphtha = new SimpleFluidMaterial(95, "steamcracked_naphtha", 0xBFB608, FLUID, of(), 0);
    public static SimpleFluidMaterial SteamCrackedGas = new SimpleFluidMaterial(96, "steamcracked_gas", 0xB4B4B4, FLUID, of(), 0);
    public static SimpleFluidMaterial SteamCrackedButene = new SimpleFluidMaterial(97, "steamcracked_butene", 0x993E05, FLUID, of(), 0);
    public static SimpleFluidMaterial SteamCrackedButadiene = new SimpleFluidMaterial(98, "steamcracked_butadiene", 0xAD5203, FLUID, of(), 0);
    public static SimpleFluidMaterial SulfuricGas = new SimpleFluidMaterial(99, "sulfuric_gas", 0xFFFFFF, FLUID, of(), STATE_GAS);
    public static SimpleFluidMaterial RefineryGas = new SimpleFluidMaterial(100, "refinery_gas", 0xFFFFFF, FLUID, of(), STATE_GAS);
    public static SimpleFluidMaterial SulfuricNaphtha = new SimpleFluidMaterial(101, "sulfuric_naphtha", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial SulfuricLightFuel = new SimpleFluidMaterial(102, "sulfuric_light_fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial SulfuricHeavyFuel = new SimpleFluidMaterial(103, "sulfuric_heavy_fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial Naphtha = new SimpleFluidMaterial(104, "naphtha", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial LightFuel = new SimpleFluidMaterial(105, "light_fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial HeavyFuel = new SimpleFluidMaterial(106, "heavy_fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial LPG = new SimpleFluidMaterial(107, "lpg", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial SteamCrackedLightFuel = new SimpleFluidMaterial(108, "steamcracked_light_fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial SteamCrackedHeavyFuel = new SimpleFluidMaterial(109, "steamcracked_heavy_fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial Toluene = new SimpleFluidMaterial(110, "toluene", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 7), new MaterialStack(Hydrogen, 8)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial UUAmplifier = new SimpleFluidMaterial(111, "uuamplifier", 0x000000, FLUID, of(), 0);
    public static SimpleFluidMaterial UUMatter = new SimpleFluidMaterial(112, "uumatter", 0x8000C4, FLUID, of(), 0);
    public static SimpleFluidMaterial Honey = new SimpleFluidMaterial(113, "honey", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial Juice = new SimpleFluidMaterial(114, "juice", 0xA8C972, FLUID, of(), 0);
    public static SimpleFluidMaterial RawGrowthMedium = new SimpleFluidMaterial(115, "raw_growth_medium", 10777425, FLUID, of(), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial SterileGrowthMedium = new SimpleFluidMaterial(116, "sterilized_growth_medium", 11306862, FLUID, of(), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Oil = new SimpleFluidMaterial(117, "oil", 0x0A0A0A, FLUID, of(), GENERATE_FLUID_BLOCK);
    public static SimpleFluidMaterial OilHeavy = new SimpleFluidMaterial(118, "oil_heavy", 0x0A0A0A, FLUID, of(), GENERATE_FLUID_BLOCK);
    public static SimpleFluidMaterial OilMedium = new SimpleFluidMaterial(119, "oil_medium", 0x0A0A0A, FLUID, of(), GENERATE_FLUID_BLOCK);
    public static SimpleFluidMaterial OilLight = new SimpleFluidMaterial(120, "oil_light", 0x0A0A0A, FLUID, of(), GENERATE_FLUID_BLOCK);
    public static SimpleFluidMaterial NaturalGas = new SimpleFluidMaterial(121, "natural_gas", 0xFFFFFF, FLUID, of(), STATE_GAS | GENERATE_FLUID_BLOCK);
    public static SimpleFluidMaterial DiphenylIsophtalate = new SimpleFluidMaterial(122, "diphenyl_isophthalate", 0x246E57, DULL, of(new MaterialStack(Carbon, 20), new MaterialStack(Hydrogen, 14), new MaterialStack(Oxygen, 4)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial PhthalicAcid = new SimpleFluidMaterial(123, "phthalic_acid", 0xD1D1D1, FLUID, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 4)), GENERATE_FLUID_BLOCK | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Dimethylbenzene = new SimpleFluidMaterial(124, "dimethylbenzene", 0x669C40, FLUID, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 10)), DISABLE_DECOMPOSITION).setFormula("C6H4(CH3)2");
    public static SimpleFluidMaterial Diaminobenzidine = new SimpleFluidMaterial(125, "diaminobenzidine", 0x337D59, DULL, of(new MaterialStack(Carbon, 12), new MaterialStack(Hydrogen, 14), new MaterialStack(Nitrogen, 4)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Dichlorobenzidine = new SimpleFluidMaterial(126, "dichlorobenzidine", 0xA1DEA6, DULL, of(new MaterialStack(Carbon, 12), new MaterialStack(Hydrogen, 10), new MaterialStack(Chlorine, 2), new MaterialStack(Nitrogen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Nitrochlorobenzene = new SimpleFluidMaterial(127, "nitrochlorobenzene", 0x8FB51A, DULL, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 4), new MaterialStack(Chlorine, 1), new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Chlorobenzene = new SimpleFluidMaterial(128, "chlorobenzene", 0x326A3E, DULL, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 5), new MaterialStack(Chlorine, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Iron3Chloride = new SimpleFluidMaterial(129, "iron_iii_chloride", 0x060B0B, FLUID, of(new MaterialStack(Iron, 1), new MaterialStack(Chlorine, 3)), DECOMPOSITION_BY_ELECTROLYZING);
    public static SimpleFluidMaterial Bacteria = new SimpleFluidMaterial(130, "bacteria", 0x808000, FLUID, of(), 0);
    public static SimpleFluidMaterial BacterialSludge = new SimpleFluidMaterial(131, "bacterial_sludge", 0x355E3B, FLUID, of(), 0);
    public static SimpleFluidMaterial EnrichedBacterialSludge = new SimpleFluidMaterial(132, "enriched_bacterial_sludge", 0x7FFF00, FLUID, of(), 0);
    public static SimpleFluidMaterial FermentedBacterialSludge = new SimpleFluidMaterial(133, "fermented_bacterial_sludge", 0x32CD32, FLUID, of(), 0);
    public static SimpleFluidMaterial Mutagen = new SimpleFluidMaterial(134, "mutagen", 0x00FF7F, FLUID, of(), 0);
    public static SimpleFluidMaterial GelatinMixture = new SimpleFluidMaterial(135, "gelatin_mixture", 0x588BAE, FLUID, of(), 0);

    public static SimpleDustMaterial SodiumHydroxide = new SimpleDustMaterial(1, "sodium_hydroxide", 0x003380, DULL, of(new MaterialStack(Sodium, 1), new MaterialStack(Oxygen, 1), new MaterialStack(Hydrogen, 1)), GENERATE_SMALL_TINY);
    public static SimpleDustMaterial MagnesiumChloride = new SimpleDustMaterial(2, "magnesium_chloride", 0xD40D5C, DULL, of(new MaterialStack(Magnesium, 1), new MaterialStack(Chlorine, 2)), GENERATE_SMALL_TINY);
    public static SimpleDustMaterial RawRubber = new SimpleDustMaterial(3, "raw_rubber", 0xCCC789, DULL, of(new MaterialStack(Carbon, 5), new MaterialStack(Hydrogen, 8)), GENERATE_SMALL_TINY | DISABLE_DECOMPOSITION);
    public static SimpleDustMaterial SodiumSulfide = new SimpleDustMaterial(4, "sodium_sulfide", 0xFFE680, DULL, of(new MaterialStack(Sodium, 2), new MaterialStack(Sulfur, 1)), GENERATE_SMALL_TINY);
    public static SimpleDustMaterial PhosphorusPentoxide = new SimpleDustMaterial(5, "phosphorus_pentoxide", 0xDCDC00, DULL, of(new MaterialStack(Phosphorus, 4), new MaterialStack(Oxygen, 10)), DISABLE_DECOMPOSITION | GENERATE_SMALL_TINY);
    public static SimpleDustMaterial Quicklime = new SimpleDustMaterial(6, "quicklime", 0xF0F0F0, DULL, of(new MaterialStack(Calcium, 1), new MaterialStack(Oxygen, 1)), GENERATE_SMALL_TINY);
    public static SimpleDustMaterial SodiumBisulfate = new SimpleDustMaterial(7, "sodium_bisulfate", 0x004455, DULL, of(new MaterialStack(Sodium, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 4)), DISABLE_DECOMPOSITION | GENERATE_SMALL_TINY);
    public static SimpleDustMaterial FerriteMixture = new SimpleDustMaterial(8, "ferrite_mixture", 0xB4B4B4, METALLIC, of(new MaterialStack(Nickel, 1), new MaterialStack(Zinc, 1), new MaterialStack(Iron, 4)), DECOMPOSITION_BY_CENTRIFUGING | GENERATE_SMALL_TINY);
    public static SimpleDustMaterial Magnesia = new SimpleDustMaterial(9, "magnesia", 0x887878, DULL, of(new MaterialStack(Magnesium, 1), new MaterialStack(Oxygen, 1)), GENERATE_SMALL_TINY);
    public static SimpleDustMaterial Polydimethylsiloxane = new SimpleDustMaterial(10, "polydimethylsiloxane", 0xF5F5F5, DULL, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 1), new MaterialStack(Silicon, 1)), DISABLE_DECOMPOSITION | GENERATE_SMALL_TINY);
    public static SimpleDustMaterial RawStyreneButadieneRubber = new SimpleDustMaterial(11, "raw_styrene_butadiene_rubber", 0x54403D, SHINY, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 8), new MaterialStack(Butadiene, 3)), DISABLE_DECOMPOSITION | GENERATE_SMALL_TINY);
    public static SimpleDustMaterial PlatinumGroupSludge = new SimpleDustMaterial(12, "platinum_group_sludge", 0x001E00, FINE, of(), DISABLE_DECOMPOSITION | GENERATE_SMALL_TINY);
    public static SimpleDustMaterial HydratedCoal = new SimpleDustMaterial(13, "hydrated_coal", 0x464664, ROUGH, of(new MaterialStack(Coal, 8), new MaterialStack(Water, 1)), GENERATE_SMALL_TINY);
    public static SimpleDustMaterial SodiumBicarbonate = new SimpleDustMaterial(14, "sodium_bicarbonate", 0x565b96, ROUGH, of(new MaterialStack(Sodium, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(Carbon, 1), new MaterialStack(Oxygen, 3)), GENERATE_SMALL_TINY);
    public static SimpleDustMaterial Collagen = new SimpleDustMaterial(15, "collagen", 0x80471C, ROUGH, of(), 0);
    public static SimpleDustMaterial Gelatin = new SimpleDustMaterial(16, "gelatin", 0x588BAE, ROUGH, of(), 0);
    public static SimpleDustMaterial Agar = new SimpleDustMaterial(17, "agar", 0x4F7942, ROUGH, of(), 0);
    public static SimpleDustMaterial PotassiumDichromate = new SimpleDustMaterial(18, "potassium_dichromate", 0xFF084E, DULL, of(new MaterialStack(Potassium, 2), new MaterialStack(Chrome, 2), new MaterialStack(Oxygen, 7)), GENERATE_SMALL_TINY);
    public static SimpleDustMaterial ChromiumTrioxide = new SimpleDustMaterial(19, "chromium_trioxide", 0xFFE4E1, DULL, of(new MaterialStack(Chrome, 1), new MaterialStack(Oxygen, 3)), GENERATE_SMALL_TINY);
    public static SimpleDustMaterial AntimonyTrioxide = new SimpleDustMaterial(20, "antimony_trioxide", 0xE6E6F0, DULL, of(new MaterialStack(Antimony, 2), new MaterialStack(Oxygen, 3)), GENERATE_SMALL_TINY);
    public static SimpleDustMaterial Zincite = new SimpleDustMaterial(21, "zincite", 0xFFFFF5, DULL, of(new MaterialStack(Zinc, 1), new MaterialStack(Oxygen, 1)), 0);
    public static SimpleDustMaterial CupricOxide = new SimpleDustMaterial(22, "cupric_oxide", 0x0F0F0F, DULL, of(new MaterialStack(Copper, 1), new MaterialStack(Oxygen, 1)), 0);
    public static SimpleDustMaterial CobaltOxide = new SimpleDustMaterial(23, "cobalt_oxide", 0x788000, DULL, of(new MaterialStack(Cobalt, 1), new MaterialStack(Oxygen, 1)), 0);
    public static SimpleDustMaterial ArsenicTrioxide = new SimpleDustMaterial(24, "arsenic_trioxide", 0xFFFFFF, ROUGH, of(new MaterialStack(Arsenic, 2), new MaterialStack(Oxygen, 3)), GENERATE_SMALL_TINY);
    public static SimpleDustMaterial Massicot = new SimpleDustMaterial(25, "massicot", 0xFFDD55, DULL, of(new MaterialStack(Lead, 1), new MaterialStack(Oxygen, 1)), 0);
    public static SimpleDustMaterial Ferrosilite = new SimpleDustMaterial(26, "ferrosilite", 0x97632A, DULL, of(new MaterialStack(Iron, 1), new MaterialStack(Silicon, 1), new MaterialStack(Oxygen, 3)), 0);


    /**
     * Organic chemistry
     */
    public static IngotMaterial SiliconeRubber = new IngotMaterial(245, "silicon_rubber", 0xDCDCDC, DULL, 1, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 1), new MaterialStack(Silicon, 1)), GENERATE_PLATE | GENERATE_GEAR | GENERATE_RING | FLAMMABLE | NO_SMASHING | GENERATE_FOIL | DISABLE_DECOMPOSITION);
    public static IngotMaterial Polystyrene = new IngotMaterial(246, "polystyrene", 0xBEB4AA, DULL, 1, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 8)), DISABLE_DECOMPOSITION | GENERATE_FOIL | NO_SMASHING);
    public static IngotMaterial StyreneButadieneRubber = new IngotMaterial(247, "styrene_butadiene_rubber", 0x211A18, SHINY, 1, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 8), new MaterialStack(Butadiene, 3)), GENERATE_PLATE | GENERATE_GEAR | GENERATE_RING | FLAMMABLE | NO_SMASHING | DISABLE_DECOMPOSITION);
    public static FluidMaterial PolyvinylAcetate = new FluidMaterial(248, "polyvinyl_acetate", 0xFF9955, FLUID, of(new MaterialStack(Carbon, 4), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static IngotMaterial ReinforcedEpoxyResin = new IngotMaterial(249, "reinforced_epoxy_resin", 0xA07A10, DULL, 1, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 4), new MaterialStack(Oxygen, 1)), GENERATE_PLATE | DISABLE_DECOMPOSITION | NO_SMASHING);
    public static IngotMaterial BorosilicateGlass = new IngotMaterial(250, "borosilicate_glass", 0xE6F3E6, SHINY, 1, of(new MaterialStack(Boron, 1), new MaterialStack(SiliconDioxide, 7)), 0);
    public static IngotMaterial PolyvinylChloride = new IngotMaterial(251, "polyvinyl_chloride", 0xD7E6E6, DULL, 1, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 3), new MaterialStack(Chlorine, 1)), EXT_METAL | GENERATE_FOIL | DISABLE_DECOMPOSITION | NO_SMASHING);
    public static IngotMaterial PolyphenyleneSulfide = new IngotMaterial(252, "polyphenylene_sulfide", 0xAA8800, DULL, 1, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 4), new MaterialStack(Sulfur, 1)), DISABLE_DECOMPOSITION | EXT_METAL | GENERATE_FOIL);
    public static FluidMaterial Glyceryl = new FluidMaterial(253, "glyceryl", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 5), new MaterialStack(Nitrogen, 3), new MaterialStack(Oxygen, 9)), FLAMMABLE | EXPLOSIVE | NO_SMELTING | NO_SMASHING);
    public static IngotMaterial Polybenzimidazole = new IngotMaterial(254, "polybenzimidazole", 0x2D2D2D, DULL, 0, of(new MaterialStack(Carbon, 20), new MaterialStack(Hydrogen, 12), new MaterialStack(Nitrogen, 4)), EXCLUDE_BLOCK_CRAFTING_RECIPES | SMELT_INTO_FLUID | NO_SMASHING | DISABLE_DECOMPOSITION | GENERATE_FOIL);

    /**
     * Not possible to determine exact Components
     */
    public static RoughSolidMaterial Wood = new RoughSolidMaterial(255, "wood", 0x643200, WOOD, 0, of(), STD_SOLID | FLAMMABLE | NO_SMELTING | GENERATE_GEAR | GENERATE_LONG_ROD | GENERATE_FRAME, () -> OrePrefix.plank);
    public static DustMaterial Gunpowder = new DustMaterial(256, "gunpowder", 0x808080, ROUGH, 0, of(), FLAMMABLE | EXPLOSIVE | NO_SMELTING | NO_SMASHING);
    public static DustMaterial Oilsands = new DustMaterial(257, "oilsands", 0x0A0A0A, SAND, 1, of(new MaterialStack(Oil, 1L)), GENERATE_ORE);
    public static RoughSolidMaterial Paper = new RoughSolidMaterial(258, "paper", 0xFAFAFA, PAPER, 0, of(), GENERATE_PLATE | FLAMMABLE | NO_SMELTING | NO_SMASHING | MORTAR_GRINDABLE | GENERATE_RING | EXCLUDE_PLATE_COMPRESSOR_RECIPE, () -> OrePrefix.plate);
    public static DustMaterial RareEarth = new DustMaterial(259, "rare_earth", 0x808064, FINE, 0, of(), 0);
    public static DustMaterial Stone = new DustMaterial(260, "stone", 0xCDCDCD, ROUGH, 1, of(), MORTAR_GRINDABLE | GENERATE_GEAR | GENERATE_PLATE | NO_SMASHING | NO_RECYCLING);
    public static FluidMaterial Lava = new FluidMaterial(261, "lava", 0xFF4000, FLUID, of(), 0);
    public static DustMaterial Glowstone = new DustMaterial(262, "glowstone", 0xFFFF00, SHINY, 1, of(), NO_SMASHING | SMELT_INTO_FLUID | GENERATE_PLATE | EXCLUDE_PLATE_COMPRESSOR_RECIPE);
    public static GemMaterial NetherStar = new GemMaterial(263, "nether_star", 0xFFFFFF, NETHERSTAR, 4, of(), STD_SOLID | GENERATE_LENSE | NO_SMASHING | NO_SMELTING);
    public static DustMaterial Endstone = new DustMaterial(264, "endstone", 0xFFFFFF, DULL, 1, of(), NO_SMASHING);
    public static DustMaterial Netherrack = new DustMaterial(265, "netherrack", 0xC80000, DULL, 1, of(), NO_SMASHING | FLAMMABLE);
    public static FluidMaterial NitroDiesel = new FluidMaterial(266, "nitro_fuel", 0xC8FF00, FLUID, of(), FLAMMABLE | EXPLOSIVE | NO_SMELTING | NO_SMASHING);


    /*
     * Oil refining sources & products
     */

    /**
     * Second Degree Compounds
     */
    public static GemMaterial Glass = new GemMaterial(267, "glass", 0xFAFAFA, GLASS, 0, of(new MaterialStack(SiliconDioxide, 1)), GENERATE_PLATE | GENERATE_LENSE | NO_SMASHING | NO_RECYCLING | SMELT_INTO_FLUID | EXCLUDE_BLOCK_CRAFTING_RECIPES | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Perlite = new DustMaterial(268, "perlite", 0x1E141E, DULL, 1, of(new MaterialStack(Obsidian, 2), new MaterialStack(Water, 1)), 0);
    public static DustMaterial Borax = new DustMaterial(269, "borax", 0xFAFAFA, FINE, 1, of(new MaterialStack(Sodium, 2), new MaterialStack(Boron, 4), new MaterialStack(Water, 10), new MaterialStack(Oxygen, 7)), 0);
    public static GemMaterial Lignite = new GemMaterial(270, "lignite", 0x644646, LIGNITE, 0, of(new MaterialStack(Carbon, 2), new MaterialStack(Water, 4), new MaterialStack(DarkAsh, 1)), GENERATE_ORE | FLAMMABLE | NO_SMELTING | NO_SMASHING | MORTAR_GRINDABLE);
    public static GemMaterial Olivine = new GemMaterial(271, "olivine", 0x96FF96, RUBY, 2, of(new MaterialStack(Magnesium, 2), new MaterialStack(Iron, 1), new MaterialStack(SiliconDioxide, 2)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, 7.5F, 3.0f, 312);
    public static GemMaterial Opal = new GemMaterial(272, "opal", 0x0000FF, OPAL, 2, of(new MaterialStack(SiliconDioxide, 1)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT | DECOMPOSITION_BY_CENTRIFUGING, 7.5F, 3.0f, 312);
    public static GemMaterial Amethyst = new GemMaterial(273, "amethyst", 0xD232D2, RUBY, 3, of(new MaterialStack(SiliconDioxide, 4), new MaterialStack(Iron, 1)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, 7.5F, 3.0f, 312);
    public static DustMaterial Redstone = new DustMaterial(274, "redstone", 0xC80000, ROUGH, 2, of(new MaterialStack(Silicon, 1), new MaterialStack(Pyrite, 5), new MaterialStack(Ruby, 1), new MaterialStack(Mercury, 3)), GENERATE_PLATE | GENERATE_ORE | NO_SMASHING | SMELT_INTO_FLUID | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES | DECOMPOSITION_BY_CENTRIFUGING);
    public static GemMaterial Lapis = new GemMaterial(275, "lapis", 0x4646DC, LAPIS, 1, of(new MaterialStack(Lazurite, 12), new MaterialStack(Sodalite, 2), new MaterialStack(Pyrite, 1), new MaterialStack(Calcite, 1)), STD_GEM | NO_SMASHING | NO_SMELTING | CRYSTALLISABLE | NO_WORKING | DECOMPOSITION_BY_ELECTROLYZING | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Blaze = new DustMaterial(276, "blaze", 0xFFC800, FINE, 1, of(new MaterialStack(DarkAsh, 1), new MaterialStack(Sulfur, 1)), NO_SMELTING | SMELT_INTO_FLUID | MORTAR_GRINDABLE | BURNING | DECOMPOSITION_BY_CENTRIFUGING);
    public static GemMaterial EnderPearl = new GemMaterial(277, "ender_pearl", 0x6CDCC8, GEM_VERTICAL, 1, of(new MaterialStack(Beryllium, 1), new MaterialStack(Potassium, 4), new MaterialStack(Nitrogen, 5)), GENERATE_PLATE | GENERATE_LENSE | NO_SMASHING | NO_SMELTING);
    public static GemMaterial EnderEye = new GemMaterial(278, "ender_eye", 0x66FF66, GEM_VERTICAL, 1, of(new MaterialStack(EnderPearl, 1), new MaterialStack(Blaze, 1)), GENERATE_PLATE | GENERATE_LENSE | NO_SMASHING | NO_SMELTING | DECOMPOSITION_BY_CENTRIFUGING);
    public static RoughSolidMaterial Flint = new RoughSolidMaterial(279, "flint", 0x002040, FLINT, 1, of(new MaterialStack(SiliconDioxide, 1)), NO_SMASHING | MORTAR_GRINDABLE | DECOMPOSITION_BY_CENTRIFUGING, () -> OrePrefix.gem);
    public static DustMaterial Diatomite = new DustMaterial(280, "diatomite", 0xE1E1E1, DULL, 1, of(new MaterialStack(Flint, 8), new MaterialStack(BandedIron, 1), new MaterialStack(Sapphire, 1)), 0);
    public static DustMaterial Niter = new DustMaterial(281, "niter", 0xFFC8C8, FLINT, 1, of(new MaterialStack(Saltpeter, 1)), NO_SMASHING | NO_SMELTING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Tantalite = new DustMaterial(282, "tantalite", 0x915028, METALLIC, 3, of(new MaterialStack(Manganese, 1), new MaterialStack(Tantalum, 2), new MaterialStack(Oxygen, 6)), GENERATE_ORE);
    public static GemMaterial Apatite = new GemMaterial(283, "apatite", 0xC8C8FF, DIAMOND, 1, of(new MaterialStack(Calcium, 5), new MaterialStack(Phosphate, 3), new MaterialStack(Chlorine, 1)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | CRYSTALLISABLE);
    public static IngotMaterial SterlingSilver = new IngotMaterial(284, "sterling_silver", 0xFADCE1, SHINY, 2, of(new MaterialStack(Copper, 1), new MaterialStack(Silver, 4)), EXT2_METAL, null, 13.0F, 2.0f, 196, 1700);
    public static IngotMaterial RoseGold = new IngotMaterial(285, "rose_gold", 0xFFE61E, SHINY, 2, of(new MaterialStack(Copper, 1), new MaterialStack(Gold, 4)), EXT2_METAL, null, 14.0F, 2.0f, 152, 1600);
    public static IngotMaterial BlackBronze = new IngotMaterial(286, "black_bronze", 0x64327D, DULL, 2, of(new MaterialStack(Gold, 1), new MaterialStack(Silver, 1), new MaterialStack(Copper, 3)), EXT2_METAL, null, 12.0F, 2.0f, 256, 2000);
    public static IngotMaterial BismuthBronze = new IngotMaterial(287, "bismuth_bronze", 0x647D7D, DULL, 2, of(new MaterialStack(Bismuth, 1), new MaterialStack(Zinc, 1), new MaterialStack(Copper, 3)), EXT2_METAL, null, 8.0F, 3.0f, 256, 1100);
    public static IngotMaterial BlackSteel = new IngotMaterial(288, "black_steel", 0x646464, METALLIC, 2, of(new MaterialStack(Nickel, 1), new MaterialStack(BlackBronze, 1), new MaterialStack(Steel, 3)), EXT_METAL, null, 6.5F, 6.5f, 768, 1200);
    public static IngotMaterial RedSteel = new IngotMaterial(289, "red_steel", 0x8C6464, METALLIC, 2, of(new MaterialStack(SterlingSilver, 1), new MaterialStack(BismuthBronze, 1), new MaterialStack(Steel, 2), new MaterialStack(BlackSteel, 4)), EXT_METAL, null, 7.0F, 4.5f, 896, 1300);
    public static IngotMaterial BlueSteel = new IngotMaterial(290, "blue_steel", 0x64648C, METALLIC, 2, of(new MaterialStack(RoseGold, 1), new MaterialStack(Brass, 1), new MaterialStack(Steel, 2), new MaterialStack(BlackSteel, 4)), EXT_METAL | GENERATE_FRAME, null, 7.5F, 5.0f, 1024, 1400);
    public static IngotMaterial DamascusSteel = new IngotMaterial(291, "damascus_steel", 0x6E6E6E, METALLIC, 2, of(new MaterialStack(Steel, 1)), EXT_METAL, null, 8.0F, 5.0f, 1280, 1500);
    public static IngotMaterial TungstenSteel = new IngotMaterial(292, "tungsten_steel", 0x6464A0, METALLIC, 4, of(new MaterialStack(Steel, 1), new MaterialStack(Tungsten, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_DENSE | GENERATE_FRAME| GENERATE_SPRING, null, 8.0F, 4.0f, 2560, 3000);
    public static IngotMaterial RedAlloy = new IngotMaterial(293, "red_alloy", 0xC80000, DULL, 0, of(new MaterialStack(Copper, 1), new MaterialStack(Redstone, 1)), GENERATE_PLATE | GENERATE_FINE_WIRE | GENERATE_BOLT_SCREW);
    public static IngotMaterial CobaltBrass = new IngotMaterial(294, "cobalt_brass", 0xB4B4A0, METALLIC, 2, of(new MaterialStack(Brass, 7), new MaterialStack(Aluminium, 1), new MaterialStack(Cobalt, 1)), EXT2_METAL, null, 8.0F, 2.0f, 256);
    public static DustMaterial TricalciumPhosphate = new DustMaterial(295, "tricalcium_phosphate", 0xFFFF00, FLINT, 2, of(new MaterialStack(Calcium, 3), new MaterialStack(Phosphate, 2)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | FLAMMABLE | EXPLOSIVE | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Basalt = new DustMaterial(296, "basalt", 0x1E1414, ROUGH, 1, of(new MaterialStack(Olivine, 1), new MaterialStack(Calcite, 3), new MaterialStack(Flint, 8), new MaterialStack(DarkAsh, 4)), NO_SMASHING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Andesite = new DustMaterial(297, "andesite", 0xBEBEBE, ROUGH, 2, of(), NO_SMASHING);
    public static DustMaterial Diorite = new DustMaterial(298, "diorite", 0xFFFFFF, ROUGH, 2, of(), NO_SMASHING);
    public static DustMaterial Granite = new DustMaterial(299, "granite", 0xCFA18C, ROUGH, 2, of(), NO_SMASHING);
    public static GemMaterial GarnetRed = new GemMaterial(300, "garnet_red", 0xC85050, RUBY, 2, of(new MaterialStack(Pyrope, 3), new MaterialStack(Almandine, 5), new MaterialStack(Spessartine, 8)), STD_SOLID | GENERATE_LENSE | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT | GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING, null, 7.5F, 3.0f, 156);
    public static GemMaterial GarnetYellow = new GemMaterial(301, "garnet_yellow", 0xC8C850, RUBY, 2, of(new MaterialStack(Andradite, 5), new MaterialStack(Grossular, 8), new MaterialStack(Uvarovite, 3)), STD_SOLID | GENERATE_LENSE | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT | GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING, null, 7.5F, 3.0f, 156);
    public static DustMaterial Marble = new DustMaterial(302, "marble", 0xC8C8C8, FINE, 1, of(new MaterialStack(Magnesium, 1), new MaterialStack(Calcite, 7)), NO_SMASHING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Sugar = new DustMaterial(303, "sugar", 0xFAFAFA, FINE, 1, of(new MaterialStack(Carbon, 2), new MaterialStack(Water, 5), new MaterialStack(Oxygen, 25)), 0);
    public static GemMaterial Vinteum = new GemMaterial(304, "vinteum", 0x64C8FF, EMERALD, 3, of(), STD_GEM | NO_SMASHING | NO_SMELTING, 12.0F, 3.0f, 128);
    public static DustMaterial Redrock = new DustMaterial(305, "redrock", 0xFF5032, ROUGH, 1, of(new MaterialStack(Calcite, 2), new MaterialStack(Flint, 1), new MaterialStack(Clay, 1)), NO_SMASHING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial PotassiumFeldspar = new DustMaterial(306, "potassium_feldspar", 0x782828, FINE, 1, of(new MaterialStack(Potassium, 1), new MaterialStack(Aluminium, 1), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 8)), 0);
    public static DustMaterial Biotite = new DustMaterial(307, "biotite", 0x141E14, METALLIC, 1, of(new MaterialStack(Potassium, 1), new MaterialStack(Magnesium, 3), new MaterialStack(Aluminium, 3), new MaterialStack(Fluorine, 2), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 10)), 0);
    public static DustMaterial GraniteBlack = new DustMaterial(308, "granite_black", 0x0A0A0A, ROUGH, 3, of(new MaterialStack(SiliconDioxide, 4), new MaterialStack(Biotite, 1)), NO_SMASHING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial GraniteRed = new DustMaterial(309, "granite_red", 0xFF0080, ROUGH, 3, of(new MaterialStack(Aluminium, 2), new MaterialStack(PotassiumFeldspar, 1), new MaterialStack(Oxygen, 3)), NO_SMASHING);
    public static DustMaterial Chrysotile = new DustMaterial(310, "chrysotile", 0x6E8C6E, ROUGH, 2, of(new MaterialStack(Asbestos, 1)), 0);
    public static DustMaterial Realgar = new DustMaterial(311, "realgar", 0x8C6464, DULL, 2, of(new MaterialStack(Arsenic, 4), new MaterialStack(Sulfur, 4)), DISABLE_DECOMPOSITION);
    public static DustMaterial VanadiumMagnetite = new DustMaterial(312, "vanadium_magnetite", 0x23233C, METALLIC, 2, of(new MaterialStack(Magnetite, 1), new MaterialStack(Vanadium, 1)), GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial BasalticMineralSand = new DustMaterial(313, "basaltic_mineral_sand", 0x283228, SAND, 1, of(new MaterialStack(Magnetite, 1), new MaterialStack(Basalt, 1)), INDUCTION_SMELTING_LOW_OUTPUT);
    public static DustMaterial GraniticMineralSand = new DustMaterial(314, "granitic_mineral_sand", 0x283C3C, SAND, 1, of(new MaterialStack(Magnetite, 1), new MaterialStack(GraniteBlack, 1)), INDUCTION_SMELTING_LOW_OUTPUT);
    public static DustMaterial GarnetSand = new DustMaterial(315, "garnet_sand", 0xC86400, SAND, 1, of(new MaterialStack(GarnetRed, 1), new MaterialStack(GarnetYellow, 1)), 0);
    public static DustMaterial QuartzSand = new DustMaterial(316, "quartz_sand", 0xC8C8C8, SAND, 1, of(new MaterialStack(CertusQuartz, 1), new MaterialStack(Quartzite, 1)), 0);
    public static DustMaterial Bastnasite = new DustMaterial(317, "bastnasite", 0xC86E2D, FINE, 2, of(new MaterialStack(Cerium, 1), new MaterialStack(Carbon, 1), new MaterialStack(Fluorine, 1), new MaterialStack(Oxygen, 3)), GENERATE_ORE);
    public static DustMaterial Pentlandite = new DustMaterial(318, "pentlandite", 0xA59605, DULL, 2, of(new MaterialStack(Nickel, 9), new MaterialStack(Sulfur, 8)), GENERATE_ORE | INDUCTION_SMELTING_LOW_OUTPUT);
    public static DustMaterial Spodumene = new DustMaterial(319, "spodumene", 0xBEAAAA, DULL, 2, of(new MaterialStack(Lithium, 1), new MaterialStack(Aluminium, 1), new MaterialStack(Silicon, 2), new MaterialStack(Oxygen, 6)), GENERATE_ORE);
    public static DustMaterial Pollucite = new DustMaterial(320, "pollucite", 0xF0D2D2, DULL, 2, of(new MaterialStack(Caesium, 2), new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 4), new MaterialStack(Water, 2), new MaterialStack(Oxygen, 12)), 0);
    public static DustMaterial Lepidolite = new DustMaterial(321, "lepidolite", 0xF0328C, FINE, 2, of(new MaterialStack(Potassium, 1), new MaterialStack(Lithium, 3), new MaterialStack(Aluminium, 4), new MaterialStack(Fluorine, 2), new MaterialStack(Oxygen, 10)), GENERATE_ORE);
    public static DustMaterial Glauconite = new DustMaterial(322, "glauconite", 0x82B43C, DULL, 2, of(new MaterialStack(Potassium, 1), new MaterialStack(Magnesium, 2), new MaterialStack(Aluminium, 4), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 12)), GENERATE_ORE);
    public static DustMaterial GlauconiteSand = new DustMaterial(323, "glauconite_sand", 0x82B43C, SAND, 2, of(new MaterialStack(Potassium, 1), new MaterialStack(Magnesium, 2), new MaterialStack(Aluminium, 4), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 12)), 0);
    public static DustMaterial Vermiculite = new DustMaterial(324, "vermiculite", 0xC8B40F, METALLIC, 2, of(new MaterialStack(Iron, 3), new MaterialStack(Aluminium, 4), new MaterialStack(Silicon, 4), new MaterialStack(Hydrogen, 2), new MaterialStack(Water, 4), new MaterialStack(Oxygen, 12)), 0);
    public static DustMaterial Bentonite = new DustMaterial(325, "bentonite", 0xF5D7D2, ROUGH, 2, of(new MaterialStack(Sodium, 1), new MaterialStack(Magnesium, 6), new MaterialStack(Silicon, 12), new MaterialStack(Hydrogen, 4), new MaterialStack(Water, 5), new MaterialStack(Oxygen, 36)), GENERATE_ORE);
    public static DustMaterial FullersEarth = new DustMaterial(326, "fullers_earth", 0xA0A078, FINE, 2, of(new MaterialStack(Magnesium, 1), new MaterialStack(Silicon, 4), new MaterialStack(Hydrogen, 1), new MaterialStack(Water, 4), new MaterialStack(Oxygen, 11)), 0);
    public static DustMaterial Pitchblende = new DustMaterial(327, "pitchblende", 0xC8D200, DULL, 3, of(new MaterialStack(Uraninite, 3), new MaterialStack(Thorium, 1), new MaterialStack(Lead, 1)), GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING);
    public static GemMaterial Monazite = new GemMaterial(328, "monazite", 0x324632, DIAMOND, 1, of(new MaterialStack(RareEarth, 1), new MaterialStack(Phosphate, 1)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | CRYSTALLISABLE);
    public static DustMaterial Malachite = new DustMaterial(329, "malachite", 0x055F05, DULL, 2, of(new MaterialStack(Copper, 2), new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 5)), GENERATE_ORE | INDUCTION_SMELTING_LOW_OUTPUT);
    public static DustMaterial Mirabilite = new DustMaterial(330, "mirabilite", 0xF0FAD2, DULL, 2, of(new MaterialStack(Sodium, 2), new MaterialStack(Sulfur, 1), new MaterialStack(Water, 10), new MaterialStack(Oxygen, 4)), 0);
    public static DustMaterial Mica = new DustMaterial(331, "mica", 0xC3C3CD, FINE, 1, of(new MaterialStack(Potassium, 1), new MaterialStack(Aluminium, 3), new MaterialStack(Silicon, 3), new MaterialStack(Fluorine, 2), new MaterialStack(Oxygen, 10)), 0);
    public static DustMaterial Trona = new DustMaterial(332, "trona", 0x87875F, METALLIC, 1, of(new MaterialStack(Sodium, 3), new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 1), new MaterialStack(Water, 2), new MaterialStack(Oxygen, 6)), 0);
    public static DustMaterial Barite = new DustMaterial(333, "barite", 0xE6EBEB, DULL, 2, of(new MaterialStack(Barium, 1), new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 4)), GENERATE_ORE);
    public static DustMaterial Gypsum = new DustMaterial(334, "gypsum", 0xE6E6FA, DULL, 1, of(new MaterialStack(Calcium, 1), new MaterialStack(Sulfur, 1), new MaterialStack(Water, 2), new MaterialStack(Oxygen, 4)), 0);
    public static DustMaterial Alunite = new DustMaterial(335, "alunite", 0xE1B441, METALLIC, 2, of(new MaterialStack(Potassium, 1), new MaterialStack(Aluminium, 3), new MaterialStack(Silicon, 2), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 14)), 0);
    public static DustMaterial Dolomite = new DustMaterial(336, "dolomite", 0xE1CDCD, FLINT, 1, of(new MaterialStack(Calcium, 1), new MaterialStack(Magnesium, 1), new MaterialStack(Carbon, 2), new MaterialStack(Oxygen, 6)), 0);
    public static DustMaterial Wollastonite = new DustMaterial(337, "wollastonite", 0xF0F0F0, DULL, 2, of(new MaterialStack(Calcium, 1), new MaterialStack(Silicon, 1), new MaterialStack(Oxygen, 3)), 0);
    public static DustMaterial Zeolite = new DustMaterial(338, "zeolite", 0xF0E6E6, DULL, 2, of(new MaterialStack(Sodium, 1), new MaterialStack(Calcium, 4), new MaterialStack(Silicon, 27), new MaterialStack(Aluminium, 9), new MaterialStack(Water, 28), new MaterialStack(Oxygen, 72)), DISABLE_DECOMPOSITION);
    public static DustMaterial Kyanite = new DustMaterial(339, "kyanite", 0x6E6EFA, FLINT, 2, of(new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 1), new MaterialStack(Oxygen, 5)), 0);
    public static DustMaterial Kaolinite = new DustMaterial(340, "kaolinite", 0xF3EBEB, DULL, 2, of(new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 2), new MaterialStack(Hydrogen, 4), new MaterialStack(Oxygen, 9)), 0);
    public static DustMaterial Talc = new DustMaterial(341, "talc", 0x5AB45A, FINE, 2, of(new MaterialStack(Magnesium, 3), new MaterialStack(Silicon, 4), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 12)), GENERATE_ORE);
    public static DustMaterial Soapstone = new DustMaterial(342, "soapstone", 0x5F915F, DULL, 1, of(new MaterialStack(Magnesium, 3), new MaterialStack(Silicon, 4), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 12)), GENERATE_ORE);
    public static DustMaterial Concrete = new DustMaterial(343, "concrete", 0x646464, ROUGH, 1, of(new MaterialStack(Stone, 1)), NO_SMASHING | SMELT_INTO_FLUID);
    public static IngotMaterial IronMagnetic = new IngotMaterial(344, "iron_magnetic", 0xC8C8C8, MAGNETIC, 2, of(new MaterialStack(Iron, 1)), EXT2_METAL | MORTAR_GRINDABLE);
    public static IngotMaterial SteelMagnetic = new IngotMaterial(345, "steel_magnetic", 0x808080, MAGNETIC, 2, of(new MaterialStack(Steel, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | MORTAR_GRINDABLE, null, 1000);
    public static IngotMaterial NeodymiumMagnetic = new IngotMaterial(346, "neodymium_magnetic", 0x646464, MAGNETIC, 2, of(new MaterialStack(Neodymium, 1)), EXT2_METAL | GENERATE_LONG_ROD, null, 1297);
    public static IngotMaterial TungstenCarbide = new IngotMaterial(347, "tungsten_carbide", 0x330066, METALLIC, 4, of(new MaterialStack(Tungsten, 1), new MaterialStack(Carbon, 1)), EXT2_METAL, null, 12.0F, 4.0f, 1280, 2460);
    public static IngotMaterial VanadiumSteel = new IngotMaterial(348, "vanadium_steel", 0xc0c0c0, METALLIC, 3, of(new MaterialStack(Vanadium, 1), new MaterialStack(Chrome, 1), new MaterialStack(Steel, 7)), EXT2_METAL, null, 7.0F, 3.0f, 1920, 1453);
    public static IngotMaterial HSSG = new IngotMaterial(349, "hssg", 0x999900, METALLIC, 3, of(new MaterialStack(TungstenSteel, 5), new MaterialStack(Chrome, 1), new MaterialStack(Molybdenum, 2), new MaterialStack(Vanadium, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_FRAME | GENERATE_SPRING, null, 10.0F, 5.5f, 4000, 4200);
    public static IngotMaterial HSSE = new IngotMaterial(350, "hsse", 0x336600, METALLIC, 4, of(new MaterialStack(HSSG, 6), new MaterialStack(Cobalt, 1), new MaterialStack(Manganese, 1), new MaterialStack(Silicon, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_FRAME, null, 10.0F, 8.0f, 5120, 5000);
    public static IngotMaterial HSSS = new IngotMaterial(351, "hsss", 0x660033, METALLIC, 4, of(new MaterialStack(HSSG, 6), new MaterialStack(Iridium, 2), new MaterialStack(Osmium, 1)), EXT2_METAL | GENERATE_GEAR, null, 15.0F, 7.0f, 3000, 5000);
    public static IngotMaterial DiamericiumTitanium = new IngotMaterial(352, "diamericium_titanium", 0x755280, METALLIC, 4, of(new MaterialStack(Americium, 2), new MaterialStack(Titanium, 1)), EXT2_METAL, null, 6.0F, 6.0F, 2200, 10400);

    /*
     * Clear matter materials
     */

    /**
     * Fantasy materials
     */
    public static IngotMaterial Naquadah = new IngotMaterial(353, "naquadah", 0x323232, METALLIC, 4, of(), EXT_METAL | GENERATE_ORE | GENERATE_FOIL| GENERATE_SPRING, Elements.get("Naquadah"), 6.0F, 4.0f, 1280, 5400);
    public static IngotMaterial NaquadahAlloy = new IngotMaterial(354, "naquadah_alloy", 0x282828, METALLIC, 5, of(new MaterialStack(Naquadah, 1), new MaterialStack(Osmiridium, 1)), EXT2_METAL| GENERATE_SPRING, null, 8.0F, 5.0f, 5120, 7200);
    public static IngotMaterial NaquadahEnriched = new IngotMaterial(355, "naquadah_enriched", 0x323232, METALLIC, 4, of(), EXT_METAL | GENERATE_ORE | GENERATE_FOIL, null, 6.0F, 4.0f, 1280, 4500);
    public static IngotMaterial Naquadria = new IngotMaterial(356, "naquadria", 0x1E1E1E, SHINY, 3, of(), EXT_METAL, Elements.get("Naquadah"), 9000);
    public static IngotMaterial Neutronium = new IngotMaterial(357, "neutronium", 0xFAFAFA, DULL, 6, of(), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_FRAME, Elements.get("Neutronium"), 24.0F, 12F, 655360);
    public static IngotMaterial Tritanium = new IngotMaterial(358, "tritanium", 0x600000, METALLIC, 6, of(), EXT_METAL | GENERATE_FRAME, Elements.get("Tritanium"), 20.0F, 6.0f, 10240);
    public static IngotMaterial Duranium = new IngotMaterial(359, "duranium", 0xFFFFFF, METALLIC, 5, of(), EXT_METAL | GENERATE_FOIL, Elements.get("Duranium"), 16.0F, 5.0f, 5120);
    public static IngotMaterial Trinium = new IngotMaterial(360, "trinium", 0xC8C8D2, SHINY, 7, of(), 0, Elements.get("Trinium"), 8600);
    public static IngotMaterial Adamantium = new IngotMaterial(361, "adamantium", 0x2d365c, SHINY, 7, of(), 0, Elements.get("Adamantium"), 10850);
    public static IngotMaterial Vibranium = new IngotMaterial(362, "vibranium", 0x828aad, SHINY, 7, of(), 0, Elements.get("Vibranium"), 11220);
    public static IngotMaterial Taranium = new IngotMaterial(363, "taranium", 0x0c0c0d, SHINY, 7, of(), 0, Elements.get("Taranium"), 10000);
    public static IngotMaterial FluxedElectrum = new IngotMaterial(364, "fluxed_electrum", 0xf2ef27, METALLIC, 4, of(new MaterialStack(Electrum, 1), new MaterialStack(NaquadahAlloy, 1), new MaterialStack(BlueSteel, 1), new MaterialStack(RedSteel, 1)), EXT2_METAL, null, 11.0F, 6.0f, 2100, 9000);

    /**
     * Actual food
     */
    public static FluidMaterial Milk = new FluidMaterial(365, "milk", 0xFEFEFE, FINE, of(), 0);
    public static DustMaterial Cocoa = new DustMaterial(366, "cocoa", 0xBE5F00, FINE, 0, of(), 0);
    public static DustMaterial Wheat = new DustMaterial(367, "wheat", 0xFFFFC4, FINE, 0, of(), 0);
    public static DustMaterial Meat = new DustMaterial(368, "meat", 12667980, SAND, 1, of(), DISABLE_DECOMPOSITION);

    static {
        for (DustMaterial dustMaterial : new DustMaterial[]{Bastnasite, Monazite}) {
            dustMaterial.separatedOnto = Neodymium;
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{Magnetite, VanadiumMagnetite, BasalticMineralSand, GraniticMineralSand}) {
            dustMaterial.separatedOnto = Gold;
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{YellowLimonite, BrownLimonite, Pyrite, BandedIron, Nickel, Vermiculite, Glauconite, GlauconiteSand, Pentlandite, Tin, Antimony, Ilmenite, Manganese, Chrome, Chromite, Andradite}) {
            dustMaterial.separatedOnto = Iron;
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{Pyrite, YellowLimonite, BasalticMineralSand, GraniticMineralSand}) {
            dustMaterial.addFlag(BLAST_FURNACE_CALCITE_DOUBLE);
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{Iron, PigIron, WroughtIron, BrownLimonite}) {
            dustMaterial.addFlag(BLAST_FURNACE_CALCITE_TRIPLE);
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{Gold, Silver, Osmium, Platinum, Cooperite, Chalcopyrite, Bornite, Tungstate, Lead, Nickel, Magnetite, Iridium, Galena, Copper}) {
            dustMaterial.washedIn = Mercury;
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{Zinc, Cobalt, Cobaltite, Tetrahedrite, Sphalerite, Iron, Malachite, Tin, Garnierite, YellowLimonite, Antimony, Pentlandite}) {
            dustMaterial.washedIn = SodiumPersulfate;
        }

        Neodymium.magneticMaterial = NeodymiumMagnetic;
        Steel.magneticMaterial = SteelMagnetic;
        Iron.magneticMaterial = IronMagnetic;

        NeodymiumMagnetic.setSmeltingInto(Neodymium);
        NeodymiumMagnetic.setArcSmeltingInto(Neodymium);
        NeodymiumMagnetic.setMaceratingInto(Neodymium);

        SteelMagnetic.setSmeltingInto(Steel);
        IronMagnetic.setArcSmeltingInto(Steel);
        IronMagnetic.setMaceratingInto(Steel);

        IronMagnetic.setSmeltingInto(Iron);
        IronMagnetic.setArcSmeltingInto(WroughtIron);
        IronMagnetic.setMaceratingInto(Iron);

        Iron.setArcSmeltingInto(WroughtIron);
        Copper.setArcSmeltingInto(AnnealedCopper);
        Tetrahedrite.setDirectSmelting(Copper);
        Malachite.setDirectSmelting(Copper);
        Chalcopyrite.setDirectSmelting(Copper);
        Tenorite.setDirectSmelting(Copper);
        Bornite.setDirectSmelting(Copper);
        Chalcocite.setDirectSmelting(Copper);
        Cuprite.setDirectSmelting(Copper);
        Pentlandite.setDirectSmelting(Nickel);
        Sphalerite.setDirectSmelting(Zinc);
        Pyrite.setDirectSmelting(Iron);
        Magnetite.setDirectSmelting(Iron);
        YellowLimonite.setDirectSmelting(Iron);
        BrownLimonite.setDirectSmelting(Iron);
        BandedIron.setDirectSmelting(Iron);
        Cassiterite.setDirectSmelting(Tin);
        CassiteriteSand.setDirectSmelting(Tin);
        Garnierite.setDirectSmelting(Nickel);
        Cobaltite.setDirectSmelting(Cobalt);
        Stibnite.setDirectSmelting(Antimony);
        Cooperite.setDirectSmelting(Platinum);
        Pyrolusite.setDirectSmelting(Manganese);
        Magnesite.setDirectSmelting(Magnesium);
        Molybdenite.setDirectSmelting(Molybdenum);

        Salt.setOreMultiplier(3);
        RockSalt.setOreMultiplier(3);
        Lepidolite.setOreMultiplier(5);

        Spodumene.setOreMultiplier(2);
        Spessartine.setOreMultiplier(2);
        Soapstone.setOreMultiplier(3);

        Almandine.setOreMultiplier(6);
        Grossular.setOreMultiplier(6);
        Bentonite.setOreMultiplier(7);
        Pyrope.setOreMultiplier(4);

        GarnetYellow.setOreMultiplier(4);
        GarnetRed.setOreMultiplier(4);
        Olivine.setOreMultiplier(2);
        Topaz.setOreMultiplier(2);

        Bastnasite.setOreMultiplier(2);
        Tennantite.setOreMultiplier(2);
        Enargite.setOreMultiplier(2);
        Tantalite.setOreMultiplier(2);
        Tanzanite.setOreMultiplier(2);
        Pitchblende.setOreMultiplier(2);

        Scheelite.setOreMultiplier(2);
        Tungstate.setOreMultiplier(2);
        Ilmenite.setOreMultiplier(3);
        Bauxite.setOreMultiplier(3);
        Rutile.setOreMultiplier(3);

        Cassiterite.setOreMultiplier(2);
        CassiteriteSand.setOreMultiplier(2);
        NetherQuartz.setOreMultiplier(2);
        CertusQuartz.setOreMultiplier(2);
        Quartzite.setOreMultiplier(2);

        TricalciumPhosphate.setOreMultiplier(3);
        Saltpeter.setOreMultiplier(4);
        Apatite.setOreMultiplier(5);
        Apatite.setByProductMultiplier(2);
        Redstone.setOreMultiplier(6);

        Lapis.setOreMultiplier(6);
        Lapis.setByProductMultiplier(4);
        Sodalite.setOreMultiplier(6);
        Sodalite.setByProductMultiplier(4);
        Lazurite.setOreMultiplier(6);
        Lazurite.setByProductMultiplier(4);
        Monazite.setOreMultiplier(8);
        Monazite.setByProductMultiplier(2);

        Coal.setBurnTime(1600); //default coal burn time in vanilla
        Charcoal.setBurnTime(1600); //default coal burn time in vanilla
        Lignite.setBurnTime(1200); //2/3 of burn time of coal
        Coke.setBurnTime(3200); //2x burn time of coal
        Wood.setBurnTime(300); //default wood burn time in vanilla

        Tenorite.addOreByProducts(Iron, Manganese, Malachite);
        Bornite.addOreByProducts(Pyrite, Cobalt, Cadmium, Gold);
        Chalcocite.addOreByProducts(Sulfur, Lead, Silver);
        Cuprite.addOreByProducts(Iron, Antimony, Malachite);
        Enargite.addOreByProducts(Pyrite, Zinc, Quartzite);
        Tennantite.addOreByProducts(Iron, Antimony, Zinc);

        Chalcopyrite.addOreByProducts(Pyrite, Cobalt, Cadmium, Gold);
        Sphalerite.addOreByProducts(GarnetYellow, Cadmium, Gallium, Zinc);
        GlauconiteSand.addOreByProducts(Sodium, Aluminium, Iron);
        Glauconite.addOreByProducts(Sodium, Aluminium, Iron);
        Vermiculite.addOreByProducts(Iron, Aluminium, Magnesium);
        FullersEarth.addOreByProducts(Aluminium, Silicon, Magnesium);
        Bentonite.addOreByProducts(Aluminium, Calcium, Magnesium);
        Uraninite.addOreByProducts(Uranium238, Thorium, Uranium235);
        Pitchblende.addOreByProducts(Thorium, Uranium238, Lead);
        Galena.addOreByProducts(Sulfur, Silver, Lead, Silver);
        Lapis.addOreByProducts(Lazurite, Sodalite, Pyrite);
        Pyrite.addOreByProducts(Sulfur, TricalciumPhosphate, Iron);
        Copper.addOreByProducts(Cobalt, Gold, Nickel, Gold);
        Nickel.addOreByProducts(Cobalt, Platinum, Iron, Platinum);
        GarnetRed.addOreByProducts(Spessartine, Pyrope, Almandine);
        GarnetYellow.addOreByProducts(Andradite, Grossular, Uvarovite);
        Cooperite.addOreByProducts(Palladium, Nickel, Iridium, Cooperite);
        Cinnabar.addOreByProducts(Redstone, Sulfur, Glowstone);
        Tantalite.addOreByProducts(Manganese, Niobium, Tantalum);
        Pollucite.addOreByProducts(Caesium, Aluminium, Rubidium);
        Chrysotile.addOreByProducts(Asbestos, Silicon, Magnesium);
        Asbestos.addOreByProducts(Asbestos, Silicon, Magnesium);
        Pentlandite.addOreByProducts(Iron, Sulfur, Cobalt);
        Uranium238.addOreByProducts(Lead, Uranium235, Thorium);
        Scheelite.addOreByProducts(Manganese, Molybdenum, Calcium);
        Tungstate.addOreByProducts(Manganese, Silver, Lithium, Silver);
        Bauxite.addOreByProducts(Grossular, Rutile, Gallium);
        QuartzSand.addOreByProducts(CertusQuartz, Quartzite, Barite);
        Quartzite.addOreByProducts(CertusQuartz, Barite);
        CertusQuartz.addOreByProducts(Quartzite, Barite);
        Redstone.addOreByProducts(Cinnabar, RareEarth, Glowstone);
        Monazite.addOreByProducts(Thorium, Neodymium, RareEarth);
        Malachite.addOreByProducts(Copper, BrownLimonite, Calcite, Copper);
        YellowLimonite.addOreByProducts(Nickel, BrownLimonite, Cobalt, Nickel);
        BrownLimonite.addOreByProducts(Malachite, YellowLimonite);
        Neodymium.addOreByProducts(Monazite, RareEarth);
        Bastnasite.addOreByProducts(Neodymium, RareEarth);
        Glowstone.addOreByProducts(Redstone, Gold);
        Zinc.addOreByProducts(Tin, Gallium);
        Tungsten.addOreByProducts(Manganese, Molybdenum);
        Diatomite.addOreByProducts(BandedIron, Sapphire);
        Iron.addOreByProducts(Nickel, Tin, Nickel);
        Lepidolite.addOreByProducts(Lithium, Caesium);
        Gold.addOreByProducts(Copper, Nickel, Gold);
        Tin.addOreByProducts(Iron, Zinc);
        Antimony.addOreByProducts(Zinc, Iron, Zinc);
        Silver.addOreByProducts(Lead, Sulfur, Silver);
        Lead.addOreByProducts(Silver, Sulfur, Silver);
        Thorium.addOreByProducts(Uranium238, Lead);
        Plutonium239.addOreByProducts(Uranium238, Lead);
        Electrum.addOreByProducts(Gold, Silver);
        Bronze.addOreByProducts(Copper, Tin);
        Brass.addOreByProducts(Copper, Zinc);
        Coal.addOreByProducts(Lignite, Thorium);
        Ilmenite.addOreByProducts(Iron, Rutile);
        Manganese.addOreByProducts(Chrome, Iron);
        Sapphire.addOreByProducts(Aluminium, GreenSapphire);
        GreenSapphire.addOreByProducts(Aluminium, Sapphire);
        Platinum.addOreByProducts(Nickel, Iridium);
        Emerald.addOreByProducts(Beryllium, Aluminium);
        Olivine.addOreByProducts(Pyrope, Magnesium, Manganese);
        Chrome.addOreByProducts(Iron, Magnesium);
        Chromite.addOreByProducts(Iron, Magnesium);
        Tetrahedrite.addOreByProducts(Antimony, Zinc, Tetrahedrite);
        GarnetSand.addOreByProducts(GarnetRed, GarnetYellow);
        Magnetite.addOreByProducts(Iron, Gold);
        GraniticMineralSand.addOreByProducts(GraniteBlack, Magnetite);
        BasalticMineralSand.addOreByProducts(Basalt, Magnetite);
        Basalt.addOreByProducts(Olivine, DarkAsh);
        VanadiumMagnetite.addOreByProducts(Magnetite, Vanadium);
        Lazurite.addOreByProducts(Sodalite, Lapis);
        Sodalite.addOreByProducts(Lazurite, Lapis);
        Spodumene.addOreByProducts(Aluminium, Lithium);
        Ruby.addOreByProducts(Chrome, GarnetRed);
        TricalciumPhosphate.addOreByProducts(Apatite, Phosphate);
        Iridium.addOreByProducts(Platinum, Osmium, Platinum);
        Pyrope.addOreByProducts(GarnetRed, Magnesium);
        Almandine.addOreByProducts(GarnetRed, Aluminium);
        Spessartine.addOreByProducts(GarnetRed, Manganese);
        Andradite.addOreByProducts(GarnetYellow, Iron);
        Grossular.addOreByProducts(GarnetYellow, Calcium);
        Uvarovite.addOreByProducts(GarnetYellow, Chrome);
        Calcite.addOreByProducts(Andradite, Malachite);
        NaquadahEnriched.addOreByProducts(Naquadah, Naquadria);
        Naquadah.addOreByProducts(NaquadahEnriched);
        Pyrolusite.addOreByProducts(Manganese);
        Molybdenite.addOreByProducts(Molybdenum);
        Stibnite.addOreByProducts(Antimony);
        Garnierite.addOreByProducts(Nickel);
        Lignite.addOreByProducts(Coal);
        Diamond.addOreByProducts(Graphite);
        Beryllium.addOreByProducts(Emerald);
        Apatite.addOreByProducts(TricalciumPhosphate);
        Magnesite.addOreByProducts(Magnesium);
        NetherQuartz.addOreByProducts(Netherrack);
        PigIron.addOreByProducts(Iron);
        Steel.addOreByProducts(Iron);
        Graphite.addOreByProducts(Carbon);
        Netherrack.addOreByProducts(Sulfur);
        Flint.addOreByProducts(Obsidian);
        Cobaltite.addOreByProducts(Cobalt, Cobaltite);
        Cobalt.addOreByProducts(Cobaltite);
        Sulfur.addOreByProducts(Sulfur);
        Saltpeter.addOreByProducts(Saltpeter);
        Endstone.addOreByProducts(Helium3);
        Osmium.addOreByProducts(Iridium, Osmium);
        Magnesium.addOreByProducts(Olivine);
        Aluminium.addOreByProducts(Bauxite);
        Titanium.addOreByProducts(Almandine);
        Obsidian.addOreByProducts(Olivine);
        Ash.addOreByProducts(Carbon);
        DarkAsh.addOreByProducts(Carbon);
        Redrock.addOreByProducts(Clay);
        Marble.addOreByProducts(Calcite);
        Clay.addOreByProducts(Clay);
        Cassiterite.addOreByProducts(Tin, Bismuth);
        CassiteriteSand.addOreByProducts(Tin);
        GraniteBlack.addOreByProducts(Biotite);
        GraniteRed.addOreByProducts(PotassiumFeldspar);
        Phosphate.addOreByProducts(Phosphorus);
        Phosphorus.addOreByProducts(Phosphate);
        Tanzanite.addOreByProducts(Opal);
        Opal.addOreByProducts(Tanzanite);
        Amethyst.addOreByProducts(Amethyst);
        Topaz.addOreByProducts(BlueTopaz);
        BlueTopaz.addOreByProducts(Topaz);
        Niter.addOreByProducts(Saltpeter);
        Vinteum.addOreByProducts(Vinteum);
        Lithium.addOreByProducts(Lithium);
        Silicon.addOreByProducts(SiliconDioxide);
        Salt.addOreByProducts(RockSalt, Borax);
        RockSalt.addOreByProducts(Salt, Borax);
        Andesite.addOreByProducts(Basalt);
        Diorite.addOreByProducts(NetherQuartz);
        Lepidolite.addOreByProducts(Boron);

        Vinteum.addEnchantmentForTools(Enchantments.FORTUNE, 2);
        BlackBronze.addEnchantmentForTools(Enchantments.SMITE, 2);
        RoseGold.addEnchantmentForTools(Enchantments.SMITE, 4);
        Invar.addEnchantmentForTools(Enchantments.BANE_OF_ARTHROPODS, 3);
        BismuthBronze.addEnchantmentForTools(Enchantments.BANE_OF_ARTHROPODS, 5);

        RedAlloy.setCableProperties(GTValues.V[0], 1, 0);
        Tin.setCableProperties(GTValues.V[1], 1, 1);
        Copper.setCableProperties(GTValues.V[2], 1, 2);

        Cobalt.setCableProperties(GTValues.V[1], 2, 2);
        Lead.setCableProperties(GTValues.V[1], 2, 2);
        Tin.setCableProperties(GTValues.V[1], 1, 1);
        Zinc.setCableProperties(GTValues.V[1], 1, 1);
        SolderingAlloy.setCableProperties(GTValues.V[1], 1, 1);

        Iron.setCableProperties(GTValues.V[2], 2, 3);
        Nickel.setCableProperties(GTValues.V[2], 3, 3);
        Cupronickel.setCableProperties(GTValues.V[2], 2, 3);
        Copper.setCableProperties(GTValues.V[2], 1, 2);
        AnnealedCopper.setCableProperties(GTValues.V[2], 1, 1);

        Kanthal.setCableProperties(GTValues.V[3], 4, 3);
        Gold.setCableProperties(GTValues.V[3], 2, 2);
        Electrum.setCableProperties(GTValues.V[3], 3, 2);
        Silver.setCableProperties(GTValues.V[3], 1, 1);

        Nichrome.setCableProperties(GTValues.V[4], 4, 4);
        Steel.setCableProperties(GTValues.V[4], 2, 2);
        BlackSteel.setCableProperties(GTValues.V[4], 3, 2);
        Titanium.setCableProperties(GTValues.V[4], 4, 2);
        Aluminium.setCableProperties(GTValues.V[4], 1, 1);

        Graphene.setCableProperties(GTValues.V[5], 1, 1);
        Osmium.setCableProperties(GTValues.V[5], 4, 2);
        Platinum.setCableProperties(GTValues.V[5], 2, 1);
        Palladium.setCableProperties(GTValues.V[5], 2, 1);
        TungstenSteel.setCableProperties(GTValues.V[5], 3, 2);
        Tungsten.setCableProperties(GTValues.V[5], 2, 2);

        HSSG.setCableProperties(GTValues.V[6], 4, 2);
        NiobiumTitanium.setCableProperties(GTValues.V[6], 4, 2);
        VanadiumGallium.setCableProperties(GTValues.V[6], 4, 2);
        YttriumBariumCuprate.setCableProperties(GTValues.V[6], 4, 4);

        Naquadah.setCableProperties(GTValues.V[7], 2, 2);

        NaquadahAlloy.setCableProperties(GTValues.V[8], 2, 4);
        Duranium.setCableProperties(GTValues.V[8], 1, 8);
        FluxedElectrum.setCableProperties(GTValues.V[8], 3, 2);
        DiamericiumTitanium.setCableProperties(GTValues.V[10], 8, 16);

        Copper.setFluidPipeProperties(1000, 10, true);
        Bronze.setFluidPipeProperties(2000, 20, true);
        Steel.setFluidPipeProperties(2500, 40, true);
        StainlessSteel.setFluidPipeProperties(3000, 60, true);
        Titanium.setFluidPipeProperties(5000, 80, true);
        TungstenSteel.setFluidPipeProperties(7500, 100, true);
        NiobiumTitanium.setFluidPipeProperties(2900, 150, true);
        Ultimet.setFluidPipeProperties(1500, 12000, true);

        Polyethylene.setFluidPipeProperties(350, 60, true);
        Polytetrafluoroethylene.setFluidPipeProperties(600, 80, true);
        Polybenzimidazole.setFluidPipeProperties(1000, 100, true);
    }
}
