package gregtech.api.unification.material;

import gregtech.api.GTValues;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.materials.ElementMaterials;
import gregtech.api.unification.material.type.MarkerMaterial;
import gregtech.api.unification.material.type.MaterialFlag;
import gregtech.api.unification.material.type.RoughSolidMaterial;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import net.minecraft.init.Enchantments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static gregtech.api.unification.material.MaterialIconSet.*;
import static gregtech.api.unification.material.type.MaterialFlags.*;

/**
 * Material Registration.
 *
 * All Material Builders should follow this general formatting:
 *
 * material = new MaterialBuilder(id, name)
 *     .ingot().fluid().ore()                <--- types
 *     .color().iconSet()                    <--- appearance
 *     .flags()                              <--- special generation
 *     .element() / .components()            <--- composition
 *     .toolStats()                          <---
 *     .oreByProducts()                         | additional properties
 *     ...                                   <---
 *     .blastTemp()                          <--- blast temperature
 *     .build();
 *
 * Use defaults to your advantage! Some defaults:
 * - iconSet: DULL
 * - color: 0xFFFFFF
 */
public class Materials {

    /*
     * TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
     *
     * Flag additions as of this branch being created:
     * - Gold: ring
     * - Zinc: ring
     * - Diamond: bolt_screw
     * - Steel: both Springs
     * - Quartzite: plate
     * - Iron: small gear, both springs
     * - Obsidian: plate
     *
     * TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
     */

    public static void register() {
        MarkerMaterials.register();
        ElementMaterials.register();
    }

    public static final List<MaterialFlag> STD_SOLID = new ArrayList<>();
    public static final List<MaterialFlag> STD_GEM = new ArrayList<>(); // TODO Had ORE flag
    public static final List<MaterialFlag> STD_METAL = new ArrayList<>();
    public static final List<MaterialFlag> EXT_METAL = new ArrayList<>();
    public static final List<MaterialFlag> EXT2_METAL = new ArrayList<>();

    static {
        STD_SOLID.addAll(Arrays.asList(GENERATE_PLATE, GENERATE_ROD, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD));

        STD_GEM.addAll(STD_SOLID);
        STD_GEM.add(GENERATE_LENS);

        STD_METAL.add(GENERATE_PLATE);

        EXT_METAL.addAll(STD_METAL);
        EXT_METAL.addAll(Arrays.asList(GENERATE_ROD, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD));

        EXT2_METAL.addAll(EXT_METAL);
        EXT2_METAL.addAll(Arrays.asList(GENERATE_GEAR, GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_ROUND));
    }

    public static final MarkerMaterial _NULL = new MarkerMaterial("_null");

    /**
     * Direct Elements
     */
    public static Material Actinium;
    public static Material Aluminium;
    public static Material Americium;
    public static Material Antimony;
    public static Material Argon;
    public static Material Arsenic;
    public static Material Astatine;
    public static Material Barium;
    public static Material Berkelium;
    public static Material Beryllium;
    public static Material Bismuth;
    public static Material Bohrium;
    public static Material Boron;
    public static Material Bromine;
    public static Material Caesium;
    public static Material Calcium;
    public static Material Californium;
    public static Material Carbon;
    public static Material Cadmium;
    public static Material Cerium;
    public static Material Chlorine;
    public static Material Chrome;
    public static Material Cobalt;
    public static Material Copernicium;
    public static Material Copper;
    public static Material Curium;
    public static Material Darmstadtium;
    public static Material Deuterium;
    public static Material Dubnium;
    public static Material Dysprosium;
    public static Material Einsteinium;
    public static Material Erbium;
    public static Material Europium;
    public static Material Fermium;
    public static Material Flerovium;
    public static Material Fluorine;
    public static Material Francium;
    public static Material Gadolinium;
    public static Material Gallium;
    public static Material Germanium;
    public static Material Gold;
    public static Material Hafnium;
    public static Material Hassium;
    public static Material Holmium;
    public static Material Hydrogen;
    public static Material Helium;
    public static Material Helium3;
    public static Material Indium;
    public static Material Iodine;
    public static Material Iridium;
    public static Material Iron;
    public static Material Krypton;
    public static Material Lanthanum;
    public static Material Lawrencium;
    public static Material Lead;
    public static Material Lithium;
    public static Material Livermorium;
    public static Material Lutetium;
    public static Material Magnesium;
    public static Material Mendelevium;
    public static Material Manganese;
    public static Material Meitnerium;
    public static Material Mercury;
    public static Material Molybdenum;
    public static Material Moscovium;
    public static Material Neodymium;
    public static Material Neon;
    public static Material Neptunium;
    public static Material Nickel;
    public static Material Nihonium;
    public static Material Niobium;
    public static Material Nitrogen;
    public static Material Nobelium;
    public static Material Oganesson;
    public static Material Osmium;
    public static Material Oxygen;
    public static Material Palladium;
    public static Material Phosphorus;
    public static Material Polonium;
    public static Material Platinum;
    public static Material Plutonium239;
    public static Material Plutonium241;
    public static Material Potassium;
    public static Material Praseodymium;
    public static Material Promethium;
    public static Material Protactinium;
    public static Material Radon;
    public static Material Radium;
    public static Material Rhenium;
    public static Material Rhodium;
    public static Material Roentgenium;
    public static Material Rubidium;
    public static Material Ruthenium;
    public static Material Rutherfordium;
    public static Material Samarium;
    public static Material Scandium;
    public static Material Seaborgium;
    public static Material Selenium;
    public static Material Silicon;
    public static Material Silver;
    public static Material Sodium;
    public static Material Strontium;
    public static Material Sulfur;
    public static Material Tantalum;
    public static Material Technetium;
    public static Material Tellurium;
    public static Material Tennessine;
    public static Material Terbium;
    public static Material Thorium;
    public static Material Thallium;
    public static Material Thulium;
    public static Material Tin;
    public static Material Titanium;
    public static Material Tritium;
    public static Material Tungsten;
    public static Material Uranium238;
    public static Material Uranium235;
    public static Material Vanadium;
    public static Material Xenon;
    public static Material Ytterbium;
    public static Material Yttrium;
    public static Material Zinc;
    public static Material Zirconium;

    /**
     * First Degree Compounds
     */
    public static Material Almandine;
    public static Material Andradite;
    public static Material AnnealedCopper;
    public static Material Asbestos;
    public static Material Ash;
    public static Material BandedIron;
    public static Material BatteryAlloy;
    public static Material BlueTopaz;
    public static Material Bone;
    public static Material Brass;
    public static Material Bronze;
    public static Material BrownLimonite;
    public static Material Calcite;
    public static Material Cassiterite;
    public static Material CassiteriteSand;
    public static Material Chalcopyrite;
    public static Material Charcoal;
    public static Material Chromite;
    public static Material Cinnabar;
    public static Material Water;
    public static Material Clay;
    public static Material Coal;
    public static Material Cobaltite;
    public static Material Cooperite;
    public static Material Cupronickel;
    public static Material DarkAsh;
    public static Material Diamond;
    public static Material Electrum;
    public static Material Emerald;
    public static Material Galena;
    public static Material Garnierite;
    public static Material GreenSapphire;
    public static Material Grossular;
    public static Material Ice;
    public static Material Ilmenite;
    public static Material Rutile;
    public static Material Bauxite;
    public static Material Invar;
    public static Material Kanthal;
    public static Material Lazurite;
    public static Material Magnalium;
    public static Material Magnesite;
    public static Material Magnetite;
    public static Material Molybdenite;
    public static Material Nichrome;
    public static Material NiobiumNitride;
    public static Material NiobiumTitanium;
    public static Material Obsidian;
    public static Material Phosphate;
    public static Material PigIron;
    public static Material Polyethylene;
    public static Material Epoxy;
    public static Material Polysiloxane;
    public static Material Polycaprolactam;
    public static Material Polytetrafluoroethylene;
    public static Material Powellite;
    public static Material Pyrite;
    public static Material Pyrolusite;
    public static Material Pyrope;
    public static Material RockSalt;
    public static Material Rubber;
    public static Material Ruby;
    public static Material Salt;
    public static Material Saltpeter;
    public static Material Sapphire;
    public static Material Scheelite;
    public static Material Sodalite;
    public static Material Brick;
    public static Material Fireclay;
    public static Material Coke;


    public static Material SolderingAlloy;
    public static Material Spessartine;
    public static Material Sphalerite;
    public static Material StainlessSteel;
    public static Material Steel;
    public static Material Stibnite;
    public static Material Tanzanite;
    public static Material Tetrahedrite;
    public static Material TinAlloy;
    public static Material Topaz;
    public static Material Tungstate;
    public static Material Ultimet;
    public static Material Uraninite;
    public static Material Uvarovite;
    public static Material VanadiumGallium;
    public static Material WroughtIron;
    public static Material Wulfenite;
    public static Material YellowLimonite;
    public static Material YttriumBariumCuprate;
    public static Material NetherQuartz;
    public static Material CertusQuartz;
    public static Material Quartzite;
    public static Material Graphite;
    public static Material Graphene;
    public static Material Jasper;
    public static Material Osmiridium;
    public static Material Tenorite;
    public static Material Cuprite;
    public static Material Bornite;
    public static Material Chalcocite;
    public static Material Enargite;
    public static Material Tennantite;

    public static Material GalliumArsenide;
    public static Material Potash;
    public static Material SodaAsh;
    public static Material IndiumGalliumPhosphide;
    public static Material NickelZincFerrite;
    public static Material SiliconDioxide;
    public static Material MagnesiumChloride;
    public static Material SodiumSulfide;
    public static Material PhosphorusPentoxide;
    public static Material Quicklime;
    public static Material SodiumBisulfate;
    public static Material FerriteMixture;
    public static Material Magnesia;
    public static Material PlatinumGroupSludge;
    public static Material HydratedCoal;
    public static Material SodiumBicarbonate;
    public static Material PotassiumDichromate;
    public static Material ChromiumTrioxide;
    public static Material AntimonyTrioxide;
    public static Material Zincite;
    public static Material CupricOxide;
    public static Material CobaltOxide;
    public static Material ArsenicTrioxide;
    public static Material Massicot;
    public static Material Ferrosilite;
    public static Material MetalMixture;
    public static Material SodiumHydroxide;
    public static Material SodiumPersulfate;

    /**
     * Water Related
     */
    public static FluidMaterial Steam = new FluidMaterial(254, "steam", 0xFFFFFF, GAS, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 1)), NO_RECYCLING | GENERATE_FLUID_BLOCK | DISABLE_DECOMPOSITION | STATE_GAS).setFluidTemperature(380);
    public static FluidMaterial DistilledWater = new FluidMaterial(255, "distilled_water", 0x0000FF, FLUID, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 1)), NO_RECYCLING | DISABLE_DECOMPOSITION);

    /**
     * Simple Fluids
     */
    public static SimpleFluidMaterial Methane = new SimpleFluidMaterial("methane", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 4)), STATE_GAS);
    public static SimpleFluidMaterial CarbonDioxide = new SimpleFluidMaterial("carbon_dioxide", 0xA9D0F5, FLUID, of(new MaterialStack(Carbon, 1), new MaterialStack(Oxygen, 2)), STATE_GAS);
    public static SimpleFluidMaterial NobleGases = new SimpleFluidMaterial("noble_gases", 0xA9D0F5, FLUID, of(new MaterialStack(CarbonDioxide, 25), new MaterialStack(Helium, 11), new MaterialStack(Methane, 4), new MaterialStack(Deuterium, 2), new MaterialStack(Radon, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Air = new SimpleFluidMaterial("air", 0xA9D0F5, GAS, of(new MaterialStack(Nitrogen, 40), new MaterialStack(Oxygen, 11), new MaterialStack(Argon, 1), new MaterialStack(NobleGases, 1)), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial LiquidAir = new SimpleFluidMaterial("liquid_air", 0xA9D0F5, FLUID, of(new MaterialStack(Nitrogen, 40), new MaterialStack(Oxygen, 11), new MaterialStack(Argon, 1), new MaterialStack(NobleGases, 1)), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial TitaniumTetrachloride = new SimpleFluidMaterial("titanium_tetrachloride", 0xD40D5C, FLUID, of(new MaterialStack(Titanium, 1), new MaterialStack(Chlorine, 4)), DISABLE_DECOMPOSITION).setFluidTemperature(2200);
    public static SimpleFluidMaterial NitrogenDioxide = new SimpleFluidMaterial("nitrogen_dioxide", 0xFFFFFF, GAS, of(new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 2)), STATE_GAS);
    public static SimpleFluidMaterial HydrogenSulfide = new SimpleFluidMaterial("hydrogen_sulfide", 0xFFFFFF, FLUID, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Sulfur, 1)), STATE_GAS);
    public static SimpleFluidMaterial Epichlorohydrin = new SimpleFluidMaterial("epichlorohydrin", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 5), new MaterialStack(Chlorine, 1), new MaterialStack(Oxygen, 1)), 0);
    public static SimpleFluidMaterial NitricAcid = new SimpleFluidMaterial("nitric_acid", 0xCCCC00, FLUID, of(new MaterialStack(Hydrogen, 1), new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 3)), 0);
    public static SimpleFluidMaterial SulfuricAcid = new SimpleFluidMaterial("sulfuric_acid", 0xFFFFFF, FLUID, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 4)), 0);
    public static SimpleFluidMaterial NitrationMixture = new SimpleFluidMaterial("nitration_mixture", 0xE6E2AB, FLUID, of(new MaterialStack(NitricAcid, 1), new MaterialStack(SulfuricAcid, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial PhosphoricAcid = new SimpleFluidMaterial("phosphoric_acid", 0xDCDC01, FLUID, of(new MaterialStack(Hydrogen, 3), new MaterialStack(Phosphorus, 1), new MaterialStack(Oxygen, 4)), 0);
    public static SimpleFluidMaterial SulfurTrioxide = new SimpleFluidMaterial("sulfur_trioxide", 0xA0A014, GAS, of(new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 3)), STATE_GAS);
    public static SimpleFluidMaterial SulfurDioxide = new SimpleFluidMaterial("sulfur_dioxide", 0xC8C819, GAS, of(new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 2)), STATE_GAS);
    public static SimpleFluidMaterial CarbonMonoxide = new SimpleFluidMaterial("carbon_monoxide", 0x0E4880, GAS, of(new MaterialStack(Carbon, 1), new MaterialStack(Oxygen, 1)), STATE_GAS);
    public static SimpleFluidMaterial DilutedSulfuricAcid = new SimpleFluidMaterial("diluted_sulfuric_acid", 0xC07820, FLUID, of(new MaterialStack(Hydrogen, 2), new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 4)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial HydrochloricAcid = new SimpleFluidMaterial("hydrochloric_acid", 0xFFFFFF, FLUID, of(new MaterialStack(Hydrogen, 1), new MaterialStack(Chlorine, 1)), 0);
    public static SimpleFluidMaterial DilutedHydrochloricAcid = new SimpleFluidMaterial("diluted_hydrochloric_acid", 0x99A7A3, FLUID, of(new MaterialStack(Hydrogen, 1), new MaterialStack(Chlorine, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial HypochlorousAcid = new SimpleFluidMaterial("hypochlorous_acid", 0x6F8A91, FLUID, of(new MaterialStack(Hydrogen, 1), new MaterialStack(Chlorine, 1), new MaterialStack(Oxygen, 1)), 0);
    public static SimpleFluidMaterial Ammonia = new SimpleFluidMaterial("ammonia", 0x3F3480, GAS, of(new MaterialStack(Nitrogen, 1), new MaterialStack(Hydrogen, 3)), STATE_GAS);
    public static SimpleFluidMaterial Chloramine = new SimpleFluidMaterial("chloramine", 0x3F9F80, GAS, of(new MaterialStack(Nitrogen, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(HydrochloricAcid, 1)), STATE_GAS);
    public static SimpleFluidMaterial NickelSulfateSolution = new SimpleFluidMaterial("nickel_sulfate_water_solution", 0x3EB640, FLUID, of(new MaterialStack(Nickel, 1), new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 4), new MaterialStack(Water, 6)), 0);
    public static SimpleFluidMaterial CopperSulfateSolution = new SimpleFluidMaterial("blue_vitriol_water_solution", 0x48A5C0, FLUID, of(new MaterialStack(Copper, 1), new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 4), new MaterialStack(Water, 5)), 0);
    public static SimpleFluidMaterial LeadZincSolution = new SimpleFluidMaterial("lead_zinc_solution", 0xFFFFFF, FLUID, of(new MaterialStack(Lead, 1), new MaterialStack(Silver, 1), new MaterialStack(Zinc, 1), new MaterialStack(Sulfur, 3), new MaterialStack(Water, 1)), DECOMPOSITION_BY_CENTRIFUGING);
    public static SimpleFluidMaterial HydrofluoricAcid = new SimpleFluidMaterial("hydrofluoric_acid", 0x0088AA, FLUID, of(new MaterialStack(Hydrogen, 1), new MaterialStack(Fluorine, 1)), 0);
    public static SimpleFluidMaterial NitricOxide = new SimpleFluidMaterial("nitric_oxide", 0x7DC8F0, GAS, of(new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 1)), STATE_GAS);
    public static SimpleFluidMaterial Chloroform = new SimpleFluidMaterial("chloroform", 0x892CA0, FLUID, of(new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 1), new MaterialStack(Chlorine, 3)), 0);
    public static SimpleFluidMaterial Cumene = new SimpleFluidMaterial("cumene", 0x552200, FLUID, of(new MaterialStack(Carbon, 9), new MaterialStack(Hydrogen, 12)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Tetrafluoroethylene = new SimpleFluidMaterial("tetrafluoroethylene", 0x7D7D7D, GAS, of(new MaterialStack(Carbon, 2), new MaterialStack(Fluorine, 4)), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Chloromethane = new SimpleFluidMaterial("chloromethane", 0xC82CA0, GAS, of(new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 3), new MaterialStack(Chlorine, 1)), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial AllylChloride = new SimpleFluidMaterial("allyl_chloride", 0x87DEAA, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Methane, 1), new MaterialStack(HydrochloricAcid, 1)), 0);
    public static SimpleFluidMaterial Isoprene = new SimpleFluidMaterial("isoprene", 0x141414, FLUID, of(new MaterialStack(Carbon, 5), new MaterialStack(Hydrogen, 8)), 0);
    public static SimpleFluidMaterial Propane = new SimpleFluidMaterial("propane", 0xFAE250, GAS, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 8)), STATE_GAS);
    public static SimpleFluidMaterial Propene = new SimpleFluidMaterial("propene", 0xFFDD55, GAS, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 6)), STATE_GAS);
    public static SimpleFluidMaterial Ethane = new SimpleFluidMaterial("ethane", 0xC8C8FF, GAS, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 6)), STATE_GAS);
    public static SimpleFluidMaterial Butene = new SimpleFluidMaterial("butene", 0xCF5005, GAS, of(new MaterialStack(Carbon, 4), new MaterialStack(Hydrogen, 8)), STATE_GAS);
    public static SimpleFluidMaterial Butane = new SimpleFluidMaterial("butane", 0xB6371E, GAS, of(new MaterialStack(Carbon, 4), new MaterialStack(Hydrogen, 10)), STATE_GAS);
    public static SimpleFluidMaterial DissolvedCalciumAcetate = new SimpleFluidMaterial("dissolved_calcium_acetate", 0xDCC8B4, FLUID, of(new MaterialStack(Calcium, 1), new MaterialStack(Carbon, 4), new MaterialStack(Oxygen, 4), new MaterialStack(Hydrogen, 6), new MaterialStack(Water, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial VinylAcetate = new SimpleFluidMaterial("vinyl_acetate", 0xE1B380, FLUID, of(new MaterialStack(Carbon, 4), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial MethylAcetate = new SimpleFluidMaterial("methyl_acetate", 0xEEC6AF, FLUID, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Ethenone = new SimpleFluidMaterial("ethenone", 0x141446, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 1)), DISABLE_DECOMPOSITION | STATE_GAS);
    public static SimpleFluidMaterial Tetranitromethane = new SimpleFluidMaterial("tetranitromethane", 0x0F2828, FLUID, of(new MaterialStack(Carbon, 1), new MaterialStack(Nitrogen, 4), new MaterialStack(Oxygen, 8)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Dimethylamine = new SimpleFluidMaterial("dimethylamine", 0x554469, GAS, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 7), new MaterialStack(Nitrogen, 1)), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Dimethylhydrazine = new SimpleFluidMaterial("dimethylhidrazine", 0x000055, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 8), new MaterialStack(Nitrogen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial DinitrogenTetroxide = new SimpleFluidMaterial("dinitrogen_tetroxide", 0x004184, GAS, of(new MaterialStack(Nitrogen, 2), new MaterialStack(Oxygen, 4)), STATE_GAS);
    public static SimpleFluidMaterial Dimethyldichlorosilane = new SimpleFluidMaterial("dimethyldichlorosilane", 0x441650, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 6), new MaterialStack(Chlorine, 2), new MaterialStack(Silicon, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Styrene = new SimpleFluidMaterial("styrene", 0xD2C8BE, FLUID, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 8)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Butadiene = new SimpleFluidMaterial("butadiene", 11885072, GAS, of(new MaterialStack(Carbon, 4), new MaterialStack(Hydrogen, 6)), DISABLE_DECOMPOSITION | STATE_GAS);
    public static SimpleFluidMaterial Dichlorobenzene = new SimpleFluidMaterial("dichlorobenzene", 0x004455, FLUID, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 4), new MaterialStack(Chlorine, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial AceticAcid = new SimpleFluidMaterial("acetic_acid", 0xC8B4A0, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 4), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Phenol = new SimpleFluidMaterial("phenol", 0x784421, FLUID, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial BisphenolA = new SimpleFluidMaterial("bisphenol_a", 0xD4AA00, FLUID, of(new MaterialStack(Carbon, 15), new MaterialStack(Hydrogen, 16), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial VinylChloride = new SimpleFluidMaterial("vinyl_chloride", 0xE1F0F0, GAS, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 3), new MaterialStack(Chlorine, 1)), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Ethylene = new SimpleFluidMaterial("ethylene", 0xE1E1E1, GAS, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 4)), STATE_GAS);
    public static SimpleFluidMaterial Benzene = new SimpleFluidMaterial("benzene", 0x1A1A1A, FLUID, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 6)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Acetone = new SimpleFluidMaterial("acetone", 0xAFAFAF, FLUID, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Glycerol = new SimpleFluidMaterial("glycerol", 0x87DE87, FLUID, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 8), new MaterialStack(Oxygen, 3)), 0);
    public static SimpleFluidMaterial Methanol = new SimpleFluidMaterial("methanol", 0xAA8800, FLUID, of(new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 4), new MaterialStack(Oxygen, 1)), 0);
    public static SimpleFluidMaterial SaltWater = new SimpleFluidMaterial("salt_water", 0x0000C8, FLUID, of(new MaterialStack(Salt, 1), new MaterialStack(Water, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial WoodGas = new SimpleFluidMaterial("wood_gas", 0xDECD87, GAS, of(), STATE_GAS | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial WoodVinegar = new SimpleFluidMaterial("wood_vinegar", 0xD45500, FLUID, of(), 0);
    public static SimpleFluidMaterial WoodTar = new SimpleFluidMaterial("wood_tar", 0x28170B, FLUID, of(), 0);
    public static SimpleFluidMaterial CharcoalByproducts = new SimpleFluidMaterial("charcoal_byproducts", 0x784421, FLUID, of(), 0);
    public static SimpleFluidMaterial Biomass = new SimpleFluidMaterial("biomass", 0x00FF00, FLUID, of(), 0);
    public static SimpleFluidMaterial BioDiesel = new SimpleFluidMaterial("bio_diesel", 0xFF8000, FLUID, of(), 0);
    public static SimpleFluidMaterial FermentedBiomass = new SimpleFluidMaterial("fermented_biomass", 0x445500, FLUID, of(), 0);
    public static SimpleFluidMaterial Creosote = new SimpleFluidMaterial("creosote", 0x804000, FLUID, of(), 0);
    public static SimpleFluidMaterial Ethanol = new SimpleFluidMaterial("ethanol", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Diesel = new SimpleFluidMaterial("fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial RocketFuel = new SimpleFluidMaterial("rocket_fuel", 0xBDB78C, FLUID, of(), 0);
    public static SimpleFluidMaterial Glue = new SimpleFluidMaterial("glue", 0xC8C400, FLUID, of(), 0);
    public static SimpleFluidMaterial Lubricant = new SimpleFluidMaterial("lubricant", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial McGuffium239 = new SimpleFluidMaterial("mc_guffium239", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial IndiumConcentrate = new SimpleFluidMaterial("indium_concentrate", 0x0e2950, FLUID, of(), 0);
    public static SimpleFluidMaterial SeedOil = new SimpleFluidMaterial("seed_oil", 0xC4FF00, FLUID, of(), 0);
    public static SimpleFluidMaterial DrillingFluid = new SimpleFluidMaterial("drilling_fluid", 0xFFFFAA, FLUID, of(), 0);
    public static SimpleFluidMaterial ConstructionFoam = new SimpleFluidMaterial("construction_foam", 0x808080, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedEthane = new SimpleFluidMaterial("hydrocracked_ethane", 0x9696BC, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial HydroCrackedEthylene = new SimpleFluidMaterial("hydrocracked_ethylene", 0xA3A3A0, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial HydroCrackedPropene = new SimpleFluidMaterial("hydrocracked_propene", 0xBEA540, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial HydroCrackedPropane = new SimpleFluidMaterial("hydrocracked_propane", 0xBEA540, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial HydroCrackedLightFuel = new SimpleFluidMaterial("hydrocracked_light_fuel", 0xB7AF08, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedButane = new SimpleFluidMaterial("hydrocracked_butane", 0x852C18, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial HydroCrackedNaphtha = new SimpleFluidMaterial("hydrocracked_naphtha", 0xBFB608, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedHeavyFuel = new SimpleFluidMaterial("hydrocracked_heavy_fuel", 0xFFFF00, FLUID, of(), 0);
    public static SimpleFluidMaterial HydroCrackedGas = new SimpleFluidMaterial("hydrocracked_gas", 0xB4B4B4, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial HydroCrackedButene = new SimpleFluidMaterial("hydrocracked_butene", 0x993E05, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial HydroCrackedButadiene = new SimpleFluidMaterial("hydrocracked_butadiene", 0xAD5203, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial SteamCrackedEthane = new SimpleFluidMaterial("steamcracked_ethane", 0x9696BC, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial SteamCrackedEthylene = new SimpleFluidMaterial("steamcracked_ethylene", 0xA3A3A0, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial SteamCrackedPropene = new SimpleFluidMaterial("steamcracked_propene", 0xBEA540, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial SteamCrackedPropane = new SimpleFluidMaterial("steamcracked_propane", 0xBEA540, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial SteamCrackedButane = new SimpleFluidMaterial("steamcracked_butane", 0x852C18, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial SteamCrackedNaphtha = new SimpleFluidMaterial("steamcracked_naphtha", 0xBFB608, FLUID, of(), 0);
    public static SimpleFluidMaterial SteamCrackedGas = new SimpleFluidMaterial("steamcracked_gas", 0xB4B4B4, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial SteamCrackedButene = new SimpleFluidMaterial("steamcracked_butene", 0x993E05, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial SteamCrackedButadiene = new SimpleFluidMaterial("steamcracked_butadiene", 0xAD5203, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial SulfuricGas = new SimpleFluidMaterial("sulfuric_gas", 0xFFFFFF, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial RefineryGas = new SimpleFluidMaterial("refinery_gas", 0xFFFFFF, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial SulfuricNaphtha = new SimpleFluidMaterial("sulfuric_naphtha", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial SulfuricLightFuel = new SimpleFluidMaterial("sulfuric_light_fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial SulfuricHeavyFuel = new SimpleFluidMaterial("sulfuric_heavy_fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial Naphtha = new SimpleFluidMaterial("naphtha", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial LightFuel = new SimpleFluidMaterial("light_fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial HeavyFuel = new SimpleFluidMaterial("heavy_fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial LPG = new SimpleFluidMaterial("lpg", 0xFFFFFF, GAS, of(), STATE_GAS);
    public static SimpleFluidMaterial SteamCrackedLightFuel = new SimpleFluidMaterial("steamcracked_light_fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial SteamCrackedHeavyFuel = new SimpleFluidMaterial("steamcracked_heavy_fuel", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial Toluene = new SimpleFluidMaterial("toluene", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 7), new MaterialStack(Hydrogen, 8)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial UUAmplifier = new SimpleFluidMaterial("uuamplifier", 0x000000, FLUID, of(), 0);
    public static SimpleFluidMaterial UUMatter = new SimpleFluidMaterial("uumatter", 0x8000C4, FLUID, of(), 0);
    public static SimpleFluidMaterial Honey = new SimpleFluidMaterial("honey", 0xFFFFFF, FLUID, of(), 0);
    public static SimpleFluidMaterial Juice = new SimpleFluidMaterial("juice", 0xA8C972, FLUID, of(), 0);
    public static SimpleFluidMaterial RawGrowthMedium = new SimpleFluidMaterial("raw_growth_medium", 10777425, FLUID, of(), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial SterileGrowthMedium = new SimpleFluidMaterial("sterilized_growth_medium", 11306862, FLUID, of(), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Oil = new SimpleFluidMaterial("oil", 0x0A0A0A, FLUID, of(), GENERATE_FLUID_BLOCK);
    public static SimpleFluidMaterial OilHeavy = new SimpleFluidMaterial("oil_heavy", 0x0A0A0A, FLUID, of(), GENERATE_FLUID_BLOCK);
    public static SimpleFluidMaterial OilMedium = new SimpleFluidMaterial("oil_medium", 0x0A0A0A, FLUID, of(), GENERATE_FLUID_BLOCK);
    public static SimpleFluidMaterial OilLight = new SimpleFluidMaterial("oil_light", 0x0A0A0A, FLUID, of(), GENERATE_FLUID_BLOCK);
    public static SimpleFluidMaterial NaturalGas = new SimpleFluidMaterial("natural_gas", 0xFFFFFF, GAS, of(), STATE_GAS | GENERATE_FLUID_BLOCK);
    public static SimpleFluidMaterial DiphenylIsophtalate = new SimpleFluidMaterial("diphenyl_isophthalate", 0x246E57, DULL, of(new MaterialStack(Carbon, 20), new MaterialStack(Hydrogen, 14), new MaterialStack(Oxygen, 4)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial PhthalicAcid = new SimpleFluidMaterial("phthalic_acid", 0xD1D1D1, FLUID, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 4)), GENERATE_FLUID_BLOCK | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Dimethylbenzene = new SimpleFluidMaterial("dimethylbenzene", 0x669C40, FLUID, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 10)), DISABLE_DECOMPOSITION).setFormula("C6H4(CH3)2", true);
    public static SimpleFluidMaterial Diaminobenzidine = new SimpleFluidMaterial("diaminobenzidine", 0x337D59, DULL, of(new MaterialStack(Carbon, 12), new MaterialStack(Hydrogen, 14), new MaterialStack(Nitrogen, 4)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Dichlorobenzidine = new SimpleFluidMaterial("dichlorobenzidine", 0xA1DEA6, DULL, of(new MaterialStack(Carbon, 12), new MaterialStack(Hydrogen, 10), new MaterialStack(Chlorine, 2), new MaterialStack(Nitrogen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Nitrochlorobenzene = new SimpleFluidMaterial("nitrochlorobenzene", 0x8FB51A, DULL, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 4), new MaterialStack(Chlorine, 1), new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Chlorobenzene = new SimpleFluidMaterial("chlorobenzene", 0x326A3E, DULL, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 5), new MaterialStack(Chlorine, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Iron3Chloride = new SimpleFluidMaterial("iron_iii_chloride", 0x060B0B, FLUID, of(new MaterialStack(Iron, 1), new MaterialStack(Chlorine, 3)), DECOMPOSITION_BY_ELECTROLYZING);
    public static SimpleFluidMaterial Bacteria = new SimpleFluidMaterial("bacteria", 0x808000, FLUID, of(), 0);
    public static SimpleFluidMaterial BacterialSludge = new SimpleFluidMaterial("bacterial_sludge", 0x355E3B, FLUID, of(), 0);
    public static SimpleFluidMaterial EnrichedBacterialSludge = new SimpleFluidMaterial("enriched_bacterial_sludge", 0x7FFF00, FLUID, of(), 0);
    public static SimpleFluidMaterial FermentedBacterialSludge = new SimpleFluidMaterial("fermented_bacterial_sludge", 0x32CD32, FLUID, of(), 0);
    public static SimpleFluidMaterial Mutagen = new SimpleFluidMaterial("mutagen", 0x00FF7F, FLUID, of(), 0);
    public static SimpleFluidMaterial GelatinMixture = new SimpleFluidMaterial("gelatin_mixture", 0x588BAE, FLUID, of(), 0);
    public static SimpleFluidMaterial UraniumHexafluoride = new SimpleFluidMaterial("uranium_hexafluoride", 0x42d126, GAS, of(new MaterialStack(Uranium238, 1), new MaterialStack(Fluorine, 6)), DISABLE_DECOMPOSITION | STATE_GAS).setFormula("UF6", true);
    public static SimpleFluidMaterial EnrichedUraniumHexafluoride = new SimpleFluidMaterial("enriched_uranium_hexafluoride", 0x4bf52a, GAS, of(new MaterialStack(Uranium235, 1), new MaterialStack(Fluorine, 6)), DISABLE_DECOMPOSITION | STATE_GAS).setFormula("UF6", true);
    public static SimpleFluidMaterial DepletedUraniumHexafluoride = new SimpleFluidMaterial("depleted_uranium_hexafluoride", 0x74ba66, GAS, of(new MaterialStack(Uranium238, 1), new MaterialStack(Fluorine, 6)), DISABLE_DECOMPOSITION | STATE_GAS).setFormula("UF6", true);

    // HOG
    public static SimpleFluidMaterial RawGasoline = new SimpleFluidMaterial("raw_gasoline", 0xFF6400, FLUID, of(), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Gasoline = new SimpleFluidMaterial("gasoline", 0xFFA500, FLUID, of(), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial NitrousOxide = new SimpleFluidMaterial("nitrous_oxide", 0x7DC8FF, FLUID, of(new MaterialStack(Nitrogen, 2), new MaterialStack(Oxygen, 1)), 0);
    public static SimpleFluidMaterial Octane = new SimpleFluidMaterial("octane", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 18)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial EthylTertButylEther = new SimpleFluidMaterial("ethyl_tertbutyl_ether", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 14), new MaterialStack(Oxygen, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial HighOctaneGasoline = new SimpleFluidMaterial("gasoline_premium", 0xFFA500, FLUID, of(), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Nitrobenzene = new SimpleFluidMaterial("nitrobenzene", 0x704936, FLUID, of(), DISABLE_DECOMPOSITION);

    // COAL GAS
    public static SimpleFluidMaterial CoalGas = new SimpleFluidMaterial("coal_gas", 0x333333, GAS, of(), DISABLE_DECOMPOSITION); // todo lang, color
    public static SimpleFluidMaterial CoalTar = new SimpleFluidMaterial("coal_tar", 0x1A1A1A, FLUID, of(), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Ethylbenzene = new SimpleFluidMaterial("ethylbenzene", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 10)), DISABLE_DECOMPOSITION).setFormula("C6H5CH2CH3", true);
    public static SimpleFluidMaterial Naphthalene = new SimpleFluidMaterial("naphthalene", 0xF4F4D7, FLUID, of(new MaterialStack(Carbon, 10), new MaterialStack(Hydrogen, 8)), DISABLE_DECOMPOSITION);

    /**
     * Organic chemistry
     */
    public static IngotMaterial SiliconeRubber = new IngotMaterial(256, "silicon_rubber", 0xDCDCDC, DULL, 1, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 1), new MaterialStack(Silicon, 1)), GENERATE_PLATE | GENERATE_GEAR | GENERATE_RING | FLAMMABLE | NO_SMASHING | GENERATE_FOIL | DISABLE_DECOMPOSITION);
    public static IngotMaterial Polystyrene = new IngotMaterial(257, "polystyrene", 0xBEB4AA, DULL, 1, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 8)), DISABLE_DECOMPOSITION | GENERATE_FOIL | NO_SMASHING);
    public static DustMaterial RawRubber = new DustMaterial(258, "raw_rubber", 0xCCC789, DULL, 1, of(new MaterialStack(Carbon, 5), new MaterialStack(Hydrogen, 8)), DISABLE_DECOMPOSITION);
    public static DustMaterial RawStyreneButadieneRubber = new DustMaterial(259, "raw_styrene_butadiene_rubber", 0x54403D, SHINY, 1, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 8), new MaterialStack(Butadiene, 3)), DISABLE_DECOMPOSITION);
    public static IngotMaterial StyreneButadieneRubber = new IngotMaterial(260, "styrene_butadiene_rubber", 0x211A18, SHINY, 1, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 8), new MaterialStack(Butadiene, 3)), GENERATE_PLATE | GENERATE_GEAR | GENERATE_RING | FLAMMABLE | NO_SMASHING | DISABLE_DECOMPOSITION);
    public static FluidMaterial PolyvinylAcetate = new FluidMaterial(261, "polyvinyl_acetate", 0xFF9955, FLUID, of(new MaterialStack(Carbon, 4), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static IngotMaterial ReinforcedEpoxyResin = new IngotMaterial(262, "reinforced_epoxy_resin", 0xA07A10, DULL, 1, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 4), new MaterialStack(Oxygen, 1)), GENERATE_PLATE | DISABLE_DECOMPOSITION | NO_SMASHING);
    public static IngotMaterial BorosilicateGlass = new IngotMaterial(263, "borosilicate_glass", 0xE6F3E6, SHINY, 1, of(new MaterialStack(Boron, 1), new MaterialStack(SiliconDioxide, 7)), GENERATE_FINE_WIRE);
    public static IngotMaterial PolyvinylChloride = new IngotMaterial(264, "polyvinyl_chloride", 0xD7E6E6, DULL, 1, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 3), new MaterialStack(Chlorine, 1)), EXT_METAL | GENERATE_FOIL | DISABLE_DECOMPOSITION | NO_SMASHING);
    public static IngotMaterial PolyphenyleneSulfide = new IngotMaterial(265, "polyphenylene_sulfide", 0xAA8800, DULL, 1, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 4), new MaterialStack(Sulfur, 1)), DISABLE_DECOMPOSITION | EXT_METAL | GENERATE_FOIL);
    public static FluidMaterial GlycerylTrinitrate = new FluidMaterial(266, "glyceryl_trinitrate", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 3), new MaterialStack(Hydrogen, 5), new MaterialStack(Nitrogen, 3), new MaterialStack(Oxygen, 9)), FLAMMABLE | EXPLOSIVE | NO_SMELTING | NO_SMASHING);
    public static IngotMaterial Polybenzimidazole = new IngotMaterial(267, "polybenzimidazole", 0x2D2D2D, DULL, 0, of(new MaterialStack(Carbon, 20), new MaterialStack(Hydrogen, 12), new MaterialStack(Nitrogen, 4)), EXCLUDE_BLOCK_CRAFTING_RECIPES | SMELT_INTO_FLUID | NO_SMASHING | DISABLE_DECOMPOSITION | GENERATE_FOIL);
    public static DustMaterial Polydimethylsiloxane = new DustMaterial(268, "polydimethylsiloxane", 0xF5F5F5, DULL, 1, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 1), new MaterialStack(Silicon, 1)), DISABLE_DECOMPOSITION);

    /**
     * Not possible to determine exact Components
     */
    public static RoughSolidMaterial Wood = new RoughSolidMaterial(269, "wood", 0x643200, WOOD, 0, of(), STD_SOLID | FLAMMABLE | NO_SMELTING | GENERATE_GEAR | GENERATE_LONG_ROD | GENERATE_FRAME, () -> OrePrefix.plank);
    public static DustMaterial Gunpowder = new DustMaterial(270, "gunpowder", 0x808080, ROUGH, 0, of(), FLAMMABLE | EXPLOSIVE | NO_SMELTING | NO_SMASHING);
    public static DustMaterial Oilsands = new DustMaterial(271, "oilsands", 0x0A0A0A, SAND, 1, of(new MaterialStack(Oil, 1L)), GENERATE_ORE);
    public static RoughSolidMaterial Paper = new RoughSolidMaterial(272, "paper", 0xFAFAFA, PAPER, 0, of(), GENERATE_PLATE | FLAMMABLE | NO_SMELTING | NO_SMASHING | MORTAR_GRINDABLE | GENERATE_RING | EXCLUDE_PLATE_COMPRESSOR_RECIPE, () -> OrePrefix.plate);
    public static DustMaterial RareEarth = new DustMaterial(273, "rare_earth", 0x808064, FINE, 0, of(), 0);
    public static DustMaterial Stone = new DustMaterial(274, "stone", 0xCDCDCD, ROUGH, 1, of(), MORTAR_GRINDABLE | GENERATE_GEAR | GENERATE_PLATE | NO_SMASHING | NO_RECYCLING);
    public static FluidMaterial Lava = new FluidMaterial(275, "lava", 0xFF4000, FLUID, of(), 0);
    public static DustMaterial Glowstone = new DustMaterial(276, "glowstone", 0xFFFF00, SHINY, 1, of(), NO_SMASHING | SMELT_INTO_FLUID | GENERATE_PLATE | EXCLUDE_PLATE_COMPRESSOR_RECIPE);
    public static GemMaterial NetherStar = new GemMaterial(277, "nether_star", 0xFFFFFF, NETHERSTAR, 4, of(), STD_SOLID | GENERATE_LENS | NO_SMASHING | NO_SMELTING);
    public static DustMaterial Endstone = new DustMaterial(278, "endstone", 0xFFFFFF, DULL, 1, of(), NO_SMASHING);
    public static DustMaterial Netherrack = new DustMaterial(279, "netherrack", 0xC80000, DULL, 1, of(), NO_SMASHING | FLAMMABLE);
    public static FluidMaterial NitroDiesel = new FluidMaterial(280, "nitro_fuel", 0xC8FF00, FLUID, of(), FLAMMABLE | EXPLOSIVE | NO_SMELTING | NO_SMASHING);
    public static DustMaterial Collagen = new DustMaterial(281, "collagen", 0x80471C, ROUGH, 1, of(), 0);
    public static DustMaterial Gelatin = new DustMaterial(282, "gelatin", 0x588BAE, ROUGH, 1, of(), 0);
    public static DustMaterial Agar = new DustMaterial(283, "agar", 0x4F7942, ROUGH, 1, of(), 0);

    /**
     * Second Degree Compounds
     */

    public static GemMaterial Glass = new GemMaterial(284, "glass", 0xFAFAFA, GLASS, 0, of(new MaterialStack(SiliconDioxide, 1)), GENERATE_PLATE | GENERATE_LENS | NO_SMASHING | NO_RECYCLING | SMELT_INTO_FLUID | EXCLUDE_BLOCK_CRAFTING_RECIPES | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Perlite = new DustMaterial(285, "perlite", 0x1E141E, DULL, 1, of(new MaterialStack(Obsidian, 2), new MaterialStack(Water, 1)), 0);
    public static DustMaterial Borax = new DustMaterial(286, "borax", 0xFAFAFA, FINE, 1, of(new MaterialStack(Sodium, 2), new MaterialStack(Boron, 4), new MaterialStack(Water, 10), new MaterialStack(Oxygen, 7)), 0);
    public static GemMaterial Lignite = new GemMaterial(287, "lignite", 0x644646, LIGNITE, 0, of(new MaterialStack(Carbon, 3), new MaterialStack(Water, 1)), GENERATE_ORE | FLAMMABLE | NO_SMELTING | NO_SMASHING | MORTAR_GRINDABLE);
    public static GemMaterial Olivine = new GemMaterial(288, "olivine", 0x96FF96, RUBY, 2, of(new MaterialStack(Magnesium, 2), new MaterialStack(Iron, 1), new MaterialStack(SiliconDioxide, 2)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, 7.5F, 3.0f, 312);
    public static GemMaterial Opal = new GemMaterial(289, "opal", 0x0000FF, OPAL, 2, of(new MaterialStack(SiliconDioxide, 1)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT | DECOMPOSITION_BY_CENTRIFUGING, 7.5F, 3.0f, 312);
    public static GemMaterial Amethyst = new GemMaterial(290, "amethyst", 0xD232D2, RUBY, 3, of(new MaterialStack(SiliconDioxide, 4), new MaterialStack(Iron, 1)), STD_GEM | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT, 7.5F, 3.0f, 312);
    public static DustMaterial Redstone = new DustMaterial(291, "redstone", 0xC80000, ROUGH, 2, of(new MaterialStack(Silicon, 1), new MaterialStack(Pyrite, 5), new MaterialStack(Ruby, 1), new MaterialStack(Mercury, 3)), GENERATE_PLATE | GENERATE_ORE | NO_SMASHING | SMELT_INTO_FLUID | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES | DECOMPOSITION_BY_CENTRIFUGING);
    public static GemMaterial Lapis = new GemMaterial(292, "lapis", 0x4646DC, LAPIS, 1, of(new MaterialStack(Lazurite, 12), new MaterialStack(Sodalite, 2), new MaterialStack(Pyrite, 1), new MaterialStack(Calcite, 1)), STD_GEM | NO_SMASHING | NO_SMELTING | CRYSTALLIZABLE | NO_WORKING | DECOMPOSITION_BY_ELECTROLYZING | EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Blaze = new DustMaterial(293, "blaze", 0xFFC800, FINE, 1, of(new MaterialStack(DarkAsh, 1), new MaterialStack(Sulfur, 1)), NO_SMELTING | SMELT_INTO_FLUID | MORTAR_GRINDABLE | BURNING | DECOMPOSITION_BY_CENTRIFUGING);
    public static GemMaterial EnderPearl = new GemMaterial(294, "ender_pearl", 0x6CDCC8, GEM_VERTICAL, 1, of(new MaterialStack(Beryllium, 1), new MaterialStack(Potassium, 4), new MaterialStack(Nitrogen, 5)), GENERATE_PLATE | GENERATE_LENS | NO_SMASHING | NO_SMELTING);
    public static GemMaterial EnderEye = new GemMaterial(295, "ender_eye", 0x66FF66, GEM_VERTICAL, 1, of(new MaterialStack(EnderPearl, 1), new MaterialStack(Blaze, 1)), GENERATE_PLATE | GENERATE_LENS | NO_SMASHING | NO_SMELTING | DECOMPOSITION_BY_CENTRIFUGING);
    public static RoughSolidMaterial Flint = new RoughSolidMaterial(296, "flint", 0x002040, FLINT, 1, of(new MaterialStack(SiliconDioxide, 1)), NO_SMASHING | MORTAR_GRINDABLE | DECOMPOSITION_BY_CENTRIFUGING, () -> OrePrefix.gem);
    public static DustMaterial Diatomite = new DustMaterial(297, "diatomite", 0xE1E1E1, DULL, 1, of(new MaterialStack(Flint, 8), new MaterialStack(BandedIron, 1), new MaterialStack(Sapphire, 1)), 0);
    public static DustMaterial Niter = new DustMaterial(298, "niter", 0xFFC8C8, FLINT, 1, of(new MaterialStack(Saltpeter, 1)), NO_SMASHING | NO_SMELTING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Tantalite = new DustMaterial(299, "tantalite", 0x915028, METALLIC, 3, of(new MaterialStack(Manganese, 1), new MaterialStack(Tantalum, 2), new MaterialStack(Oxygen, 6)), GENERATE_ORE);
    public static GemMaterial Apatite = new GemMaterial(300, "apatite", 0xC8C8FF, DIAMOND, 1, of(new MaterialStack(Calcium, 5), new MaterialStack(Phosphate, 3), new MaterialStack(Chlorine, 1)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | CRYSTALLIZABLE);
    public static IngotMaterial SterlingSilver = new IngotMaterial(301, "sterling_silver", 0xFADCE1, SHINY, 2, of(new MaterialStack(Copper, 1), new MaterialStack(Silver, 4)), EXT2_METAL, null, 13.0F, 2.0f, 196, 1700);
    public static IngotMaterial RoseGold = new IngotMaterial(302, "rose_gold", 0xFFE61E, SHINY, 2, of(new MaterialStack(Copper, 1), new MaterialStack(Gold, 4)), EXT2_METAL, null, 14.0F, 2.0f, 152, 1600);
    public static IngotMaterial BlackBronze = new IngotMaterial(303, "black_bronze", 0x64327D, DULL, 2, of(new MaterialStack(Gold, 1), new MaterialStack(Silver, 1), new MaterialStack(Copper, 3)), EXT2_METAL, null, 12.0F, 2.0f, 256, 2000);
    public static IngotMaterial BismuthBronze = new IngotMaterial(304, "bismuth_bronze", 0x647D7D, DULL, 2, of(new MaterialStack(Bismuth, 1), new MaterialStack(Zinc, 1), new MaterialStack(Copper, 3)), EXT2_METAL, null, 8.0F, 3.0f, 256, 1100);
    public static IngotMaterial BlackSteel = new IngotMaterial(305, "black_steel", 0x646464, METALLIC, 2, of(new MaterialStack(Nickel, 1), new MaterialStack(BlackBronze, 1), new MaterialStack(Steel, 3)), EXT_METAL, null, 6.5F, 6.5f, 768, 1200);
    public static IngotMaterial RedSteel = new IngotMaterial(306, "red_steel", 0x8C6464, METALLIC, 2, of(new MaterialStack(SterlingSilver, 1), new MaterialStack(BismuthBronze, 1), new MaterialStack(Steel, 2), new MaterialStack(BlackSteel, 4)), EXT_METAL, null, 7.0F, 4.5f, 896, 1300);
    public static IngotMaterial BlueSteel = new IngotMaterial(307, "blue_steel", 0x64648C, METALLIC, 2, of(new MaterialStack(RoseGold, 1), new MaterialStack(Brass, 1), new MaterialStack(Steel, 2), new MaterialStack(BlackSteel, 4)), EXT_METAL | GENERATE_FRAME, null, 7.5F, 5.0f, 1024, 1400);
    public static IngotMaterial DamascusSteel = new IngotMaterial(308, "damascus_steel", 0x6E6E6E, METALLIC, 2, of(new MaterialStack(Steel, 1)), EXT_METAL, null, 8.0F, 5.0f, 1280, 1500);
    public static IngotMaterial TungstenSteel = new IngotMaterial(309, "tungsten_steel", 0x6464A0, METALLIC, 4, of(new MaterialStack(Steel, 1), new MaterialStack(Tungsten, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_DENSE | GENERATE_FRAME| GENERATE_SPRING, null, 8.0F, 4.0f, 2560, 3000);
    public static IngotMaterial RedAlloy = new IngotMaterial(310, "red_alloy", 0xC80000, DULL, 0, of(new MaterialStack(Copper, 1), new MaterialStack(Redstone, 1)), GENERATE_PLATE | GENERATE_FINE_WIRE | GENERATE_BOLT_SCREW);
    public static IngotMaterial CobaltBrass = new IngotMaterial(311, "cobalt_brass", 0xB4B4A0, METALLIC, 2, of(new MaterialStack(Brass, 7), new MaterialStack(Aluminium, 1), new MaterialStack(Cobalt, 1)), EXT2_METAL, null, 8.0F, 2.0f, 256);
    public static DustMaterial TricalciumPhosphate = new DustMaterial(312, "tricalcium_phosphate", 0xFFFF00, FLINT, 2, of(new MaterialStack(Calcium, 3), new MaterialStack(Phosphate, 2)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | FLAMMABLE | EXPLOSIVE | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Basalt = new DustMaterial(313, "basalt", 0x1E1414, ROUGH, 1, of(new MaterialStack(Olivine, 1), new MaterialStack(Calcite, 3), new MaterialStack(Flint, 8), new MaterialStack(DarkAsh, 4)), NO_SMASHING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Andesite = new DustMaterial(314, "andesite", 0xBEBEBE, ROUGH, 2, of(), NO_SMASHING);
    public static DustMaterial Diorite = new DustMaterial(315, "diorite", 0xFFFFFF, ROUGH, 2, of(), NO_SMASHING);
    public static DustMaterial Granite = new DustMaterial(316, "granite", 0xCFA18C, ROUGH, 2, of(), NO_SMASHING);
    public static GemMaterial GarnetRed = new GemMaterial(317, "garnet_red", 0xC85050, RUBY, 2, of(new MaterialStack(Pyrope, 3), new MaterialStack(Almandine, 5), new MaterialStack(Spessartine, 8)), STD_SOLID | GENERATE_LENS | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT | GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING, null, 7.5F, 3.0f, 156);
    public static GemMaterial GarnetYellow = new GemMaterial(318, "garnet_yellow", 0xC8C850, RUBY, 2, of(new MaterialStack(Andradite, 5), new MaterialStack(Grossular, 8), new MaterialStack(Uvarovite, 3)), STD_SOLID | GENERATE_LENS | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT | GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING, null, 7.5F, 3.0f, 156);
    public static DustMaterial Marble = new DustMaterial(319, "marble", 0xC8C8C8, FINE, 1, of(new MaterialStack(Magnesium, 1), new MaterialStack(Calcite, 7)), NO_SMASHING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Sugar = new DustMaterial(320, "sugar", 0xFAFAFA, FINE, 1, of(new MaterialStack(Carbon, 2), new MaterialStack(Water, 5), new MaterialStack(Oxygen, 25)), 0);
    public static GemMaterial Vinteum = new GemMaterial(321, "vinteum", 0x64C8FF, EMERALD, 3, of(), STD_GEM | NO_SMASHING | NO_SMELTING, 12.0F, 3.0f, 128);
    public static DustMaterial Redrock = new DustMaterial(322, "redrock", 0xFF5032, ROUGH, 1, of(new MaterialStack(Calcite, 2), new MaterialStack(Flint, 1), new MaterialStack(Clay, 1)), NO_SMASHING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial PotassiumFeldspar = new DustMaterial(323, "potassium_feldspar", 0x782828, FINE, 1, of(new MaterialStack(Potassium, 1), new MaterialStack(Aluminium, 1), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 8)), 0);
    public static DustMaterial Biotite = new DustMaterial(324, "biotite", 0x141E14, METALLIC, 1, of(new MaterialStack(Potassium, 1), new MaterialStack(Magnesium, 3), new MaterialStack(Aluminium, 3), new MaterialStack(Fluorine, 2), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 10)), 0);
    public static DustMaterial GraniteBlack = new DustMaterial(325, "granite_black", 0x0A0A0A, ROUGH, 3, of(new MaterialStack(SiliconDioxide, 4), new MaterialStack(Biotite, 1)), NO_SMASHING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial GraniteRed = new DustMaterial(326, "granite_red", 0xFF0080, ROUGH, 3, of(new MaterialStack(Aluminium, 2), new MaterialStack(PotassiumFeldspar, 1), new MaterialStack(Oxygen, 3)), NO_SMASHING);
    public static DustMaterial Chrysotile = new DustMaterial(327, "chrysotile", 0x6E8C6E, ROUGH, 2, of(new MaterialStack(Asbestos, 1)), 0);
    public static DustMaterial Realgar = new DustMaterial(328, "realgar", 0x8C6464, DULL, 2, of(new MaterialStack(Arsenic, 4), new MaterialStack(Sulfur, 4)), DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial VanadiumMagnetite = new DustMaterial(329, "vanadium_magnetite", 0x23233C, METALLIC, 2, of(new MaterialStack(Magnetite, 1), new MaterialStack(Vanadium, 1)), GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial BasalticMineralSand = new DustMaterial(330, "basaltic_mineral_sand", 0x283228, SAND, 1, of(new MaterialStack(Magnetite, 1), new MaterialStack(Basalt, 1)), 0);
    public static DustMaterial GraniticMineralSand = new DustMaterial(331, "granitic_mineral_sand", 0x283C3C, SAND, 1, of(new MaterialStack(Magnetite, 1), new MaterialStack(GraniteBlack, 1)), 0);
    public static DustMaterial GarnetSand = new DustMaterial(332, "garnet_sand", 0xC86400, SAND, 1, of(new MaterialStack(GarnetRed, 1), new MaterialStack(GarnetYellow, 1)), 0);
    public static DustMaterial QuartzSand = new DustMaterial(333, "quartz_sand", 0xC8C8C8, SAND, 1, of(new MaterialStack(CertusQuartz, 1), new MaterialStack(Quartzite, 1)), 0);
    public static DustMaterial Bastnasite = new DustMaterial(334, "bastnasite", 0xC86E2D, FINE, 2, of(new MaterialStack(Cerium, 1), new MaterialStack(Carbon, 1), new MaterialStack(Fluorine, 1), new MaterialStack(Oxygen, 3)), GENERATE_ORE);
    public static DustMaterial Pentlandite = new DustMaterial(335, "pentlandite", 0xA59605, DULL, 2, of(new MaterialStack(Nickel, 9), new MaterialStack(Sulfur, 8)), GENERATE_ORE);
    public static DustMaterial Spodumene = new DustMaterial(336, "spodumene", 0xBEAAAA, DULL, 2, of(new MaterialStack(Lithium, 1), new MaterialStack(Aluminium, 1), new MaterialStack(Silicon, 2), new MaterialStack(Oxygen, 6)), GENERATE_ORE);
    public static DustMaterial Pollucite = new DustMaterial(337, "pollucite", 0xF0D2D2, DULL, 2, of(new MaterialStack(Caesium, 2), new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 4), new MaterialStack(Water, 2), new MaterialStack(Oxygen, 12)), 0);
    public static DustMaterial Lepidolite = new DustMaterial(338, "lepidolite", 0xF0328C, FINE, 2, of(new MaterialStack(Potassium, 1), new MaterialStack(Lithium, 3), new MaterialStack(Aluminium, 4), new MaterialStack(Fluorine, 2), new MaterialStack(Oxygen, 10)), GENERATE_ORE);
    public static DustMaterial Glauconite = new DustMaterial(339, "glauconite", 0x82B43C, DULL, 2, of(new MaterialStack(Potassium, 1), new MaterialStack(Magnesium, 2), new MaterialStack(Aluminium, 4), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 12)), GENERATE_ORE);
    public static DustMaterial GlauconiteSand = new DustMaterial(340, "glauconite_sand", 0x82B43C, SAND, 2, of(new MaterialStack(Potassium, 1), new MaterialStack(Magnesium, 2), new MaterialStack(Aluminium, 4), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 12)), 0);
    public static DustMaterial Vermiculite = new DustMaterial(341, "vermiculite", 0xC8B40F, METALLIC, 2, of(new MaterialStack(Iron, 3), new MaterialStack(Aluminium, 4), new MaterialStack(Silicon, 4), new MaterialStack(Hydrogen, 2), new MaterialStack(Water, 4), new MaterialStack(Oxygen, 12)), 0);
    public static DustMaterial Bentonite = new DustMaterial(342, "bentonite", 0xF5D7D2, ROUGH, 2, of(new MaterialStack(Sodium, 1), new MaterialStack(Magnesium, 6), new MaterialStack(Silicon, 12), new MaterialStack(Hydrogen, 4), new MaterialStack(Water, 5), new MaterialStack(Oxygen, 36)), GENERATE_ORE);
    public static DustMaterial FullersEarth = new DustMaterial(343, "fullers_earth", 0xA0A078, FINE, 2, of(new MaterialStack(Magnesium, 1), new MaterialStack(Silicon, 4), new MaterialStack(Hydrogen, 1), new MaterialStack(Water, 4), new MaterialStack(Oxygen, 11)), 0);
    public static DustMaterial Pitchblende = new DustMaterial(344, "pitchblende", 0xC8D200, DULL, 3, of(new MaterialStack(Uraninite, 3), new MaterialStack(Thorium, 1), new MaterialStack(Lead, 1)), GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING).setFormula("(UO2)3ThPb", true);
    public static GemMaterial Monazite = new GemMaterial(345, "monazite", 0x324632, DIAMOND, 1, of(new MaterialStack(RareEarth, 1), new MaterialStack(Phosphate, 1)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | CRYSTALLIZABLE);
    public static DustMaterial Malachite = new DustMaterial(346, "malachite", 0x055F05, DULL, 2, of(new MaterialStack(Copper, 2), new MaterialStack(Carbon, 1), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 5)), GENERATE_ORE);
    public static DustMaterial Mirabilite = new DustMaterial(347, "mirabilite", 0xF0FAD2, DULL, 2, of(new MaterialStack(Sodium, 2), new MaterialStack(Sulfur, 1), new MaterialStack(Water, 10), new MaterialStack(Oxygen, 4)), 0);
    public static DustMaterial Mica = new DustMaterial(348, "mica", 0xC3C3CD, FINE, 1, of(new MaterialStack(Potassium, 1), new MaterialStack(Aluminium, 3), new MaterialStack(Silicon, 3), new MaterialStack(Fluorine, 2), new MaterialStack(Oxygen, 10)), 0);
    public static DustMaterial Trona = new DustMaterial(349, "trona", 0x87875F, METALLIC, 1, of(new MaterialStack(Sodium, 3), new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 1), new MaterialStack(Water, 2), new MaterialStack(Oxygen, 6)), 0);
    public static DustMaterial Barite = new DustMaterial(350, "barite", 0xE6EBEB, DULL, 2, of(new MaterialStack(Barium, 1), new MaterialStack(Sulfur, 1), new MaterialStack(Oxygen, 4)), GENERATE_ORE);
    public static DustMaterial Gypsum = new DustMaterial(351, "gypsum", 0xE6E6FA, DULL, 1, of(new MaterialStack(Calcium, 1), new MaterialStack(Sulfur, 1), new MaterialStack(Water, 2), new MaterialStack(Oxygen, 4)), 0);
    public static DustMaterial Alunite = new DustMaterial(352, "alunite", 0xE1B441, METALLIC, 2, of(new MaterialStack(Potassium, 1), new MaterialStack(Aluminium, 3), new MaterialStack(Silicon, 2), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 14)), 0);
    public static DustMaterial Dolomite = new DustMaterial(353, "dolomite", 0xE1CDCD, FLINT, 1, of(new MaterialStack(Calcium, 1), new MaterialStack(Magnesium, 1), new MaterialStack(Carbon, 2), new MaterialStack(Oxygen, 6)), 0);
    public static DustMaterial Wollastonite = new DustMaterial(354, "wollastonite", 0xF0F0F0, DULL, 2, of(new MaterialStack(Calcium, 1), new MaterialStack(Silicon, 1), new MaterialStack(Oxygen, 3)), 0);
    public static DustMaterial Zeolite = new DustMaterial(355, "zeolite", 0xF0E6E6, DULL, 2, of(new MaterialStack(Sodium, 1), new MaterialStack(Calcium, 4), new MaterialStack(Silicon, 27), new MaterialStack(Aluminium, 9), new MaterialStack(Water, 28), new MaterialStack(Oxygen, 72)), DISABLE_DECOMPOSITION);
    public static DustMaterial Kyanite = new DustMaterial(356, "kyanite", 0x6E6EFA, FLINT, 2, of(new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 1), new MaterialStack(Oxygen, 5)), 0);
    public static DustMaterial Kaolinite = new DustMaterial(357, "kaolinite", 0xF3EBEB, DULL, 2, of(new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 2), new MaterialStack(Hydrogen, 4), new MaterialStack(Oxygen, 9)), 0);
    public static DustMaterial Talc = new DustMaterial(358, "talc", 0x5AB45A, FINE, 2, of(new MaterialStack(Magnesium, 3), new MaterialStack(Silicon, 4), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 12)), GENERATE_ORE);
    public static DustMaterial Soapstone = new DustMaterial(359, "soapstone", 0x5F915F, DULL, 1, of(new MaterialStack(Magnesium, 3), new MaterialStack(Silicon, 4), new MaterialStack(Hydrogen, 2), new MaterialStack(Oxygen, 12)), GENERATE_ORE);
    public static DustMaterial Concrete = new DustMaterial(360, "concrete", 0x646464, ROUGH, 1, of(new MaterialStack(Stone, 1)), NO_SMASHING | SMELT_INTO_FLUID);
    public static IngotMaterial IronMagnetic = new IngotMaterial(361, "iron_magnetic", 0xC8C8C8, MAGNETIC, 2, of(new MaterialStack(Iron, 1)), EXT2_METAL | MORTAR_GRINDABLE);
    public static IngotMaterial SteelMagnetic = new IngotMaterial(362, "steel_magnetic", 0x808080, MAGNETIC, 2, of(new MaterialStack(Steel, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | MORTAR_GRINDABLE, null, 1000);
    public static IngotMaterial NeodymiumMagnetic = new IngotMaterial(363, "neodymium_magnetic", 0x646464, MAGNETIC, 2, of(new MaterialStack(Neodymium, 1)), EXT2_METAL | GENERATE_LONG_ROD, null, 1297);
    public static IngotMaterial TungstenCarbide = new IngotMaterial(364, "tungsten_carbide", 0x330066, METALLIC, 4, of(new MaterialStack(Tungsten, 1), new MaterialStack(Carbon, 1)), EXT2_METAL, null, 12.0F, 4.0f, 1280, 2460);
    public static IngotMaterial VanadiumSteel = new IngotMaterial(365, "vanadium_steel", 0xc0c0c0, METALLIC, 3, of(new MaterialStack(Vanadium, 1), new MaterialStack(Chrome, 1), new MaterialStack(Steel, 7)), EXT2_METAL, null, 7.0F, 3.0f, 1920, 1453);
    public static IngotMaterial HSSG = new IngotMaterial(366, "hssg", 0x999900, METALLIC, 3, of(new MaterialStack(TungstenSteel, 5), new MaterialStack(Chrome, 1), new MaterialStack(Molybdenum, 2), new MaterialStack(Vanadium, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_FRAME | GENERATE_SPRING, null, 10.0F, 5.5f, 4000, 4200);
    public static IngotMaterial HSSE = new IngotMaterial(367, "hsse", 0x336600, METALLIC, 4, of(new MaterialStack(HSSG, 6), new MaterialStack(Cobalt, 1), new MaterialStack(Manganese, 1), new MaterialStack(Silicon, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_FRAME, null, 10.0F, 8.0f, 5120, 5000);
    public static IngotMaterial HSSS = new IngotMaterial(368, "hsss", 0x660033, METALLIC, 4, of(new MaterialStack(HSSG, 6), new MaterialStack(Iridium, 2), new MaterialStack(Osmium, 1)), EXT2_METAL | GENERATE_GEAR | GENERATE_SMALL_GEAR, null, 15.0F, 7.0f, 3000, 5000);
    public static IngotMaterial DiamericiumTitanium = new IngotMaterial(369, "diamericium_titanium", 0x755280, METALLIC, 4, of(new MaterialStack(Americium, 2), new MaterialStack(Titanium, 1)), EXT2_METAL, null, 6.0F, 6.0F, 2200, 10400);
    public static IngotMaterial Potin = new IngotMaterial(370, "potin", 0xc99781, MaterialIconSet.METALLIC, 6, of(new MaterialStack(Lead, 2), new MaterialStack(Bronze, 2), new MaterialStack(Tin, 1)), EXT2_METAL, null);

    /**
     * Fantasy materials
     */
    public static IngotMaterial Naquadah = new IngotMaterial(371, "naquadah", 0x323232, METALLIC, 4, of(), EXT_METAL | GENERATE_ORE | GENERATE_FOIL| GENERATE_SPRING, Elements.get("Naquadah"), 6.0F, 4.0f, 1280, 5400);
    public static IngotMaterial NaquadahAlloy = new IngotMaterial(372, "naquadah_alloy", 0x282828, METALLIC, 5, of(new MaterialStack(Naquadah, 1), new MaterialStack(Osmiridium, 1)), EXT2_METAL| GENERATE_SPRING, null, 8.0F, 5.0f, 5120, 7200);
    public static IngotMaterial NaquadahEnriched = new IngotMaterial(373, "naquadah_enriched", 0x323232, METALLIC, 4, of(), EXT_METAL | GENERATE_ORE | GENERATE_FOIL, Elements.get("NaquadahEnriched"), 6.0F, 4.0f, 1280, 4500);
    public static IngotMaterial Naquadria = new IngotMaterial(374, "naquadria", 0x1E1E1E, SHINY, 3, of(), EXT_METAL | GENERATE_FOIL, Elements.get("Naquadria"), 9000);
    public static IngotMaterial Neutronium = new IngotMaterial(375, "neutronium", 0xFAFAFA, DULL, 6, of(), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_FRAME, Elements.get("Neutronium"), 24.0F, 12F, 655360);
    public static IngotMaterial Tritanium = new IngotMaterial(376, "tritanium", 0x600000, METALLIC, 6, of(), EXT_METAL | GENERATE_FRAME, Elements.get("Tritanium"), 20.0F, 6.0f, 10240);
    public static IngotMaterial Duranium = new IngotMaterial(377, "duranium", 0xFFFFFF, METALLIC, 5, of(), EXT_METAL | GENERATE_FOIL, Elements.get("Duranium"), 16.0F, 5.0f, 5120);
    public static IngotMaterial Trinium = new IngotMaterial(378, "trinium", 0xC8C8D2, SHINY, 7, of(), GENERATE_FOIL, Elements.get("Trinium"), 8600);
    public static IngotMaterial Adamantium = new IngotMaterial(379, "adamantium", 0x2d365c, SHINY, 7, of(), 0, Elements.get("Adamantium"), 10850);
    public static IngotMaterial Vibranium = new IngotMaterial(380, "vibranium", 0x828aad, SHINY, 7, of(), 0, Elements.get("Vibranium"), 11220);
    public static IngotMaterial Taranium = new IngotMaterial(381, "taranium", 0x0c0c0d, SHINY, 7, of(), 0, Elements.get("Taranium"), 10000);
    public static IngotMaterial FluxedElectrum = new IngotMaterial(382, "fluxed_electrum", 0xf2ef27, METALLIC, 4, of(new MaterialStack(Electrum, 1), new MaterialStack(NaquadahAlloy, 1), new MaterialStack(BlueSteel, 1), new MaterialStack(RedSteel, 1)), EXT2_METAL, null, 11.0F, 6.0f, 2100, 9000);

    /**
     * Actual food
     */
    public static FluidMaterial Milk = new FluidMaterial(383, "milk", 0xFEFEFE, FINE, of(), 0);
    public static DustMaterial Cocoa = new DustMaterial(384, "cocoa", 0xBE5F00, FINE, 0, of(), 0);
    public static DustMaterial Wheat = new DustMaterial(385, "wheat", 0xFFFFC4, FINE, 0, of(), 0);
    public static DustMaterial Meat = new DustMaterial(386, "meat", 12667980, SAND, 1, of(), DISABLE_DECOMPOSITION);

    // Superconductor here at ID 387. Assigned in MarkerMaterials:98

    static {
        for (DustMaterial dustMaterial : new DustMaterial[]{Bastnasite, Monazite}) {
            dustMaterial.separatedOnto = Neodymium;
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{Magnetite, VanadiumMagnetite, BasalticMineralSand, GraniticMineralSand}) {
            dustMaterial.separatedOnto = Gold;
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{YellowLimonite, BrownLimonite, Pyrite, BandedIron, Vermiculite, Glauconite, GlauconiteSand, Pentlandite, Ilmenite, Manganese, Chromite, Andradite}) {
            dustMaterial.separatedOnto = Iron;
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{Pyrite, YellowLimonite, BasalticMineralSand, GraniticMineralSand}) {
            dustMaterial.addFlag(BLAST_FURNACE_CALCITE_DOUBLE);
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{PigIron, WroughtIron, BrownLimonite}) {
            dustMaterial.addFlag(BLAST_FURNACE_CALCITE_TRIPLE);
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{Cooperite, Chalcopyrite, Bornite, Tungstate, Magnetite, Galena}) {
            dustMaterial.washedIn = Mercury;
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{Cobaltite, Tetrahedrite, Sphalerite, Malachite, Garnierite, YellowLimonite, Pentlandite}) {
            dustMaterial.washedIn = SodiumPersulfate;
        }

        Steel.magneticMaterial = SteelMagnetic;

        NeodymiumMagnetic.setSmeltingInto(Neodymium);
        NeodymiumMagnetic.setArcSmeltingInto(Neodymium);
        NeodymiumMagnetic.setMaceratingInto(Neodymium);

        SteelMagnetic.setSmeltingInto(Steel);
        IronMagnetic.setArcSmeltingInto(Steel);
        IronMagnetic.setMaceratingInto(Steel);

        IronMagnetic.setSmeltingInto(Iron);
        IronMagnetic.setArcSmeltingInto(WroughtIron);
        IronMagnetic.setMaceratingInto(Iron);

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
        BasalticMineralSand.setDirectSmelting(Iron);
        GraniticMineralSand.setDirectSmelting(Iron);
        Chromite.setDirectSmelting(Chrome);
        Galena.setDirectSmelting(Lead);

        Salt.setOreMultiplier(2);
        RockSalt.setOreMultiplier(2);
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
        Apatite.setOreMultiplier(4);
        Apatite.setByProductMultiplier(2);
        Redstone.setOreMultiplier(5);

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
        GarnetRed.addOreByProducts(Spessartine, Pyrope, Almandine);
        GarnetYellow.addOreByProducts(Andradite, Grossular, Uvarovite);
        Cooperite.addOreByProducts(Palladium, Nickel, Iridium, Cooperite);
        Cinnabar.addOreByProducts(Redstone, Sulfur, Glowstone);
        Tantalite.addOreByProducts(Manganese, Niobium, Tantalum);
        Pollucite.addOreByProducts(Caesium, Aluminium, Rubidium);
        Chrysotile.addOreByProducts(Asbestos, Silicon, Magnesium);
        Asbestos.addOreByProducts(Asbestos, Silicon, Magnesium);
        Pentlandite.addOreByProducts(Iron, Sulfur, Cobalt);
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
        Diatomite.addOreByProducts(BandedIron, Sapphire);
        Lepidolite.addOreByProducts(Lithium, Caesium);
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
        Saltpeter.addOreByProducts(Saltpeter);
        Endstone.addOreByProducts(Helium3);
        Magnesium.addOreByProducts(Olivine);
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

        SolderingAlloy.setCableProperties(GTValues.V[1], 1, 1);

        Cupronickel.setCableProperties(GTValues.V[2], 2, 3);
        AnnealedCopper.setCableProperties(GTValues.V[2], 1, 1);

        Kanthal.setCableProperties(GTValues.V[3], 4, 3);
        Electrum.setCableProperties(GTValues.V[3], 3, 2);

        Nichrome.setCableProperties(GTValues.V[4], 4, 4);
        Steel.setCableProperties(GTValues.V[4], 2, 2);
        BlackSteel.setCableProperties(GTValues.V[4], 3, 2);

        Graphene.setCableProperties(GTValues.V[5], 1, 1);
        TungstenSteel.setCableProperties(GTValues.V[5], 3, 2);

        HSSG.setCableProperties(GTValues.V[6], 4, 2);
        NiobiumTitanium.setCableProperties(GTValues.V[6], 4, 2);
        VanadiumGallium.setCableProperties(GTValues.V[6], 4, 2);
        YttriumBariumCuprate.setCableProperties(GTValues.V[6], 4, 4);

        Naquadah.setCableProperties(GTValues.V[7], 2, 2);

        NaquadahAlloy.setCableProperties(GTValues.V[8], 2, 4);
        Duranium.setCableProperties(GTValues.V[8], 1, 8);
        FluxedElectrum.setCableProperties(GTValues.V[8], 3, 2);
        DiamericiumTitanium.setCableProperties(GTValues.V[10], 8, 16);

        Bronze.setFluidPipeProperties(1696, 20, true);
        WroughtIron.setFluidPipeProperties(2387, 30, true);
        TinAlloy.setFluidPipeProperties(1572, 38, true);
        Invar.setFluidPipeProperties(2395, 40, true);
        Steel.setFluidPipeProperties(2557, 40, true);
        StainlessSteel.setFluidPipeProperties(2428, 60, true);
        Potin.setFluidPipeProperties(2023, 96, true);
        VanadiumSteel.setFluidPipeProperties(2073, 100, true);
        TungstenSteel.setFluidPipeProperties(7568, 100, true);
        TungstenCarbide.setFluidPipeProperties(7568, 125, true);
        NiobiumTitanium.setFluidPipeProperties(2900, 150, true);
        Naquadah.setFluidPipeProperties(19200, 1500, true);
        Duranium.setFluidPipeProperties(100000, 2000, true);
        Neutronium.setFluidPipeProperties(1000000, 2800, true);

        Polyethylene.setFluidPipeProperties(350, 60, true);
        Polytetrafluoroethylene.setFluidPipeProperties(600, 80, true);
        Polybenzimidazole.setFluidPipeProperties(1000, 100, true);

        Brass.setItemPipeProperties(2048, 1);
        CobaltBrass.setItemPipeProperties(2048, 1);
        Cupronickel.setItemPipeProperties(2048, 1);
        Electrum.setItemPipeProperties(1024, 2);
        SterlingSilver.setItemPipeProperties(1024, 2);
        RoseGold.setItemPipeProperties(1024, 2);
        Magnalium.setItemPipeProperties(1024, 2);
        BlackBronze.setItemPipeProperties(1024, 2);
        FluxedElectrum.setItemPipeProperties(128, 16);
        Ultimet.setItemPipeProperties(128, 16);
        Osmiridium.setItemPipeProperties(64, 32);
        Americium.setItemPipeProperties(64, 64);
        DiamericiumTitanium.setItemPipeProperties(32, 128);
        PolyvinylChloride.setItemPipeProperties(512, 4);
    }
}
