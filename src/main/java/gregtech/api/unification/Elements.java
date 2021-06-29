package gregtech.api.unification;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

@ZenClass("mods.gregtech.material.Elements")
@ZenRegister
public class Elements {

    private static Map<String, Element> elements = new HashMap<>();

    public Elements() { }

    public static void register() {
        add(1, 0, -1, null, "Hydrogen", "H", false);
        add(1, 1, -1, "H", "Deuterium", "D", true);
        add(1, 2, -1, "D", "Tritium", "T", true);
        add(2, 2, -1, null, "Helium", "He", false);
        add(2, 1, -1, "H&D", "Helium-3", "He_3", true);
        add(3, 4, -1, null, "Lithium", "Li", false);
        add(4, 5, -1, null, "Beryllium", "Be", false);
        add(5, 5, -1, null, "Boron", "B", false);
        add(6, 6, -1, null, "Carbon", "C", false);
        add(7, 7, -1, null, "Nitrogen", "N", false);
        add(8, 8, -1, null, "Oxygen", "O", false);
        add(9, 9, -1, null, "Fluorine", "F", false);
        add(10, 10, -1, null, "Neon", "Ne", false);
        add(11, 11, -1, null, "Sodium", "Na", false);
        add(12, 12, -1, null, "Magnesium", "Mg", false);
        add(13, 13, -1, null, "Aluminium", "Al", false);
        add(14, 14, -1, null, "Silicon", "Si", false);
        add(15, 15, -1, null, "Phosphorus", "P", false);
        add(16, 16, -1, null, "Sulfur", "S", false);
        add(17, 18, -1, null, "Chlorine", "Cl", false);
        add(18, 22, -1, null, "Argon", "Ar", false);
        add(19, 20, -1, null, "Potassium", "K", false);
        add(20, 20, -1, null, "Calcium", "Ca", false);
        add(21, 24, -1, null, "Scandium", "Sc", false);
        add(22, 26, -1, null, "Titanium", "Ti", false);
        add(23, 28, -1, null, "Vanadium", "Va", false);
        add(24, 28, -1, null, "Chrome", "Cr", false);
        add(25, 30, -1, null, "Manganese", "Mn", false);
        add(26, 30, -1, null, "Iron", "Fe", false);
        add(27, 32, -1, null, "Cobalt", "Cp", false);
        add(28, 30, -1, null, "Nickel", "Ni", false);
        add(29, 34, -1, null, "Copper", "Cu", false);
        add(30, 35, -1, null, "Zinc", "Zn", false);
        add(31, 39, -1, null, "Gallium", "Ga", false);
        add(32, 40, -1, null, "Germanium", "Ge", false);
        add(33, 42, -1, null, "Arsenic", "As", false);
        add(34, 45, -1, null, "Selenium", "Se", false);
        add(35, 45, -1, null, "Bromine", "Br", false);
        add(36, 48, -1, null, "Krypton", "Kr", false);
        add(37, 48, -1, null, "Rubidium", "Rb", false);
        add(38, 49, -1, null, "Strontium", "Sr", false);
        add(39, 50, -1, null, "Yttrium", "Yt", false);
        add(40, 51, -1, null, "Zirconium", "Zr", false);
        add(41, 53, -1, null, "Niobium", "Nb", false);
        add(42, 53, -1, null, "Molybdenum", "Mo", false);
        add(43, 55, -1, null, "Technetium", "Tc", false);
        add(44, 57, -1, null, "Ruthenium", "Ru", false);
        add(45, 58, -1, null, "Rhodium", "Rh", false);
        add(46, 60, -1, null, "Palladium", "Pd", false);
        add(47, 60, -1, null, "Silver", "Ag", false);
        add(48, 64, -1, null, "Cadmium", "Cd", false);
        add(49, 65, -1, null, "Indium", "I", false);
        add(50, 68, -1, null, "Tin", "Sn", false);
        add(51, 70, -1, null, "Antimony", "Sb", false);
        add(52, 75, -1, null, "Tellurium", "Te", false);
        add(53, 74, -1, null, "Iodine", "I", false);
        add(54, 77, -1, null, "Xenon", "Xe", false);
        add(55, 77, -1, null, "Caesium", "Cs", false);
        add(56, 81, -1, null, "Barium", "Ba", false);
        add(57, 81, -1, null, "Lanthanum", "La", false);
        add(58, 82, -1, null, "Cerium", "Ce", false);
        add(59, 81, -1, null, "Praseodymium", "Pr", false);
        add(60, 84, -1, null, "Neodymium", "Nd", false);
        add(61, 83, -1, null, "Promethium", "Pm", false);
        add(62, 88, -1, null, "Samarium", "Sm", false);
        add(63, 88, -1, null, "Europium", "Eu", false);
        add(64, 93, -1, null, "Gadolinium", "Gd", false);
        add(65, 93, -1, null, "Terbium", "Tb", false);
        add(66, 96, -1, null, "Dysprosium", "Ds", false);
        add(67, 97, -1, null, "Holmium", "Ho", false);
        add(68, 99, -1, null, "Erbium", "Er", false);
        add(69, 99, -1, null, "Thulium", "Tm", false);
        add(70, 103, -1, null, "Ytterbium", "Yb", false);
        add(71, 103, -1, null, "Lutetium", "Lu", false);
        add(72, 106, -1, null, "Hafnium", "Hf", false);
        add(73, 107, -1, null, "Tantalum", "Ta", false);
        add(74, 109, -1, null, "Tungsten", "W", false);
        add(75, 111, -1, null, "Rhenium", "Re", false);
        add(76, 114, -1, null, "Osmium", "Os", false);
        add(77, 115, -1, null, "Iridium", "Ir", false);
        add(78, 117, -1, null, "Platinum", "Pt", false);
        add(79, 117, -1, null, "Gold", "Au", false);
        add(80, 120, -1, null, "Mercury", "Hg", false);
        add(81, 123, -1, null, "Thallium", "Tl", false);
        add(82, 125, -1, null, "Lead", "Pb", false);
        add(83, 125, -1, null, "Bismuth", "Bi", false);
        add(84, 124, -1, null, "Polonium", "Po", false);
        add(85, 124, -1, null, "Astatine", "At", false);
        add(86, 134, -1, null, "Radon", "Rn", false);
        add(87, 134, -1, null, "Francium", "Fr", false);
        add(88, 136, -1, null, "Radium", "Ra", false);
        add(89, 136, -1, null, "Actinium", "Ac", false);
        add(90, 140, -1, null, "Thorium", "Th", false);
        add(91, 138, -1, null, "Protactinium", "Pa", false);
        add(92, 146, -1, null, "Uranium", "U", false);
        add(92, 143, -1, null, "Uranium-235", "U_235", true);
        add(93, 144, -1, null, "Neptunium", "Np", false);
        add(94, 152, -1, null, "Plutonium", "Pu", false);
        add(94, 149, -1, null, "Plutonium-241", "Pu_241", true);
        add(95, 150, -1, null, "Americium", "Am", false);
        add(96, 153, -1, null, "Curium", "Cm", false);
        add(97, 152, -1, null, "Berkelium", "Bk", false);
        add(98, 153, -1, null, "Californium", "Cf", false);
        add(99, 153, -1, null, "Einsteinium", "Es", false);
        add(100, 157, -1, null, "Fermium", "Fm", false);
        add(101, 157, -1, null, "Mendelevium", "Md", false);
        add(102, 157, -1, null, "Nobelium", "No", false);
        add(103, 159, -1, null, "Lawrencium", "Lr", false);
        add(104, 161, -1, null, "Rutherfordium", "Rf", false);
        add(105, 163, -1, null, "Dubnium", "Db", false);
        add(106, 165, -1, null, "Seaborgium", "Sg", false);
        add(107, 163, -1, null, "Bohrium", "Bh", false);
        add(108, 169, -1, null, "Hassium", "Hs", false);
        add(109, 167, -1, null, "Meitnerium", "Mt", false);
        add(110, 171, -1, null, "Darmstadtium", "Ds", false);
        add(111, 169, -1, null, "Roentgenium", "Rg", false);
        add(112, 173, -1, null, "Copernicium", "Cn", false);
        add(113, 171, -1, null, "Nihonium", "Nh", false);
        add(114, 175, -1, null, "Flerovium", "Fl", false);
        add(115, 173, -1, null, "Moscovium", "Mc", false);
        add(116, 177, -1, null, "Livermorium", "Lv", false);
        add(117, 177, -1, null, "Tennessine", "Ts", false);
        add(118, 176, -1, null, "Oganesson", "Og", false);

        //fantasy
        add(119, 178, -1, null, "Tritanium", "Tr", false);
        add(120, 180, -1, null, "Duranium", "Dr", false);
        add(121, 172, 140, null, "Naquadah", "Nq", true);
        add(0, 5000, -1, null, "Neutronium", "Nt", false);
        add(1000, 1500, -1, null, "Trinium", "Ke", false);
        add(750, 1000, -1, null, "Adamantium", "Ad", false);
        add(850, 900, -1, null, "Vibranium", "Vb", false);
        add(550, 670, -1, null, "Taranium", "Tn", false);
    }

    @ZenMethod
    public static void add(long protons, long neutrons, long halfLifeSeconds, String decayTo, String name, String symbol, boolean isIsotope) {
        elements.put(name, new Element(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope));
    }

    @ZenMethod
    public static Element get(String name) {
        return elements.get(name);
    }
}
