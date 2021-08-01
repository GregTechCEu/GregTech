package gregtech.api.unification.material;

import gregtech.api.GTValues;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.materials.*;
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

    public static void register() {
        MarkerMaterials.register();

        /*
         * Ranges 1-249
         */
        ElementMaterials.register();

        /*
         * Ranges 250-999
         */
        FirstDegreeMaterials.register();

        /*
         * Ranges 1000-1499
         */
        OrganicChemistryMaterials.register();

        /*
         * Ranges 1500-1999
         */
        UnknownCompositionMaterials.register();

        /*
         * Ranges 2000-2499
         */
        HighDegreeMaterials.register();
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

    // Fantasy elements
    public static Material Naquadah;
    public static Material NaquadahEnriched;
    public static Material Naquadria;
    public static Material Neutronium;
    public static Material Tritanium;
    public static Material Duranium;
    public static Material Trinium;
    public static Material Adamantium;
    public static Material Vibranium;
    public static Material Taranium;

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
    public static Material SterlingSilver;
    public static Material RoseGold;
    public static Material BlackBronze;
    public static Material BismuthBronze;
    public static Material Biotite;
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
    public static Material DiamericiumTitanium;
    public static Material Tantalite;
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
    public static Material RealGar;
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
    public static Material Bastnasite;
    public static Material Pentlandite;
    public static Material Spodumene;
    public static Material Lepidolite;
    public static Material Glauconite;
    public static Material GlauconiteSand;
    public static Material Malachite;
    public static Material Mica;
    public static Material Barite;
    public static Material Alunite;
    public static Material Dolomite;
    public static Material Wollastonite;
    public static Material Kaolinite;
    public static Material Talc;
    public static Material Soapstone;
    public static Material Kyanite;
    public static Material IronMagnetic;
    public static Material TungstenCarbide;

    /**
     * Organic chemistry
     */
    public static Material SiliconeRubber;
    public static Material Polystyrene;
    public static Material RawRubber;
    public static Material RawStyreneButadieneRubber;
    public static Material StyreneButadieneRubber;
    public static Material PolyvinylAcetate;
    public static Material ReinforcedEpoxyResin;
    public static Material BorosilicateGlass;
    public static Material PolyvinylChloride;
    public static Material PolyphenyleneSulfide;
    public static Material GlycerylTrinitrate;
    public static Material Polybenzimidazole;
    public static Material Polydimethylsiloxane;
    public static Material Polyethylene;
    public static Material Epoxy;
    public static Material Polysiloxane;
    public static Material Polycaprolactam;
    public static Material Polytetrafluoroethylene;

    /**
     * Not possible to determine exact Components
     */
    public static Material WoodGas;
    public static Material WoodVinegar;
    public static Material WoodTar;
    public static Material CharcoalByproducts;
    public static Material Biomass;
    public static Material BioDiesel;
    public static Material FermentedBiomass;
    public static Material Creosote;
    public static Material Diesel;
    public static Material RocketFuel;
    public static Material Glue;
    public static Material Lubricant;
    public static Material McGuffium239;
    public static Material IndiumConcentrate;
    public static Material SeedOil;
    public static Material DrillingFluid;
    public static Material ConstructionFoam;
    public static Material HydroCrackedEthane;
    public static Material HydroCrackedEthylene;
    public static Material HydroCrackedPropene;
    public static Material HydroCrackedPropane;
    public static Material HydroCrackedLightFuel;
    public static Material HydroCrackedButane;
    public static Material HydroCrackedNaphtha;
    public static Material HydroCrackedHeavyFuel;
    public static Material HydroCrackedGas;
    public static Material HydroCrackedButene;
    public static Material HydroCrackedButadiene;
    public static Material SteamCrackedEthane;
    public static Material SteamCrackedEthylene;
    public static Material SteamCrackedPropene;
    public static Material SteamCrackedPropane;
    public static Material SteamCrackedButane;
    public static Material SteamCrackedNaphtha;
    public static Material SteamCrackedGas;
    public static Material SteamCrackedButene;
    public static Material SteamCrackedButadiene;
    public static Material SulfuricGas;
    public static Material RefineryGas;
    public static Material SulfuricNaphtha;
    public static Material SulfuricLightFuel;
    public static Material SulfuricHeavyFuel;
    public static Material Naphtha;
    public static Material LightFuel;
    public static Material HeavyFuel;
    public static Material LPG;
    public static Material SteamCrackedLightFuel;
    public static Material SteamCrackedHeavyFuel;
    public static Material UUAmplifier;
    public static Material UUMatter;
    public static Material Honey;
    public static Material Juice;
    public static Material RawGrowthMedium;
    public static Material SterileGrowthMedium;
    public static Material Oil;
    public static Material OilHeavy;
    public static Material OilMedium;
    public static Material OilLight;
    public static Material NaturalGas;
    public static Material Bacteria;
    public static Material BacterialSludge;
    public static Material EnrichedBacterialSludge;
    public static Material FermentedBacterialSludge;
    public static Material Mutagen;
    public static Material GelatinMixture;
    public static Material RawGasoline;
    public static Material Gasoline;
    public static Material HighOctaneGasoline;
    public static Material Nitrobenzene;
    public static Material CoalGas;
    public static Material CoalTar;
    public static Material Gunpowder;
    public static Material Oilsands;
    public static Material RareEarth;
    public static Material Stone;
    public static Material Lava;
    public static Material Glowstone;
    public static Material NetherStar;
    public static Material Endstone;
    public static Material Netherrack;
    public static Material NitroDiesel;
    public static Material Collagen;
    public static Material Gelatin;
    public static Material Agar;
    public static Material Andesite;
    public static Material Diorite;
    public static Material Granite;
    public static Material Wood;
    public static Material Paper;

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
    public static SimpleFluidMaterial Ethanol = new SimpleFluidMaterial("ethanol", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 1)), DISABLE_DECOMPOSITION);

    public static SimpleFluidMaterial Toluene = new SimpleFluidMaterial("toluene", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 7), new MaterialStack(Hydrogen, 8)), DISABLE_DECOMPOSITION);

    public static SimpleFluidMaterial DiphenylIsophtalate = new SimpleFluidMaterial("diphenyl_isophthalate", 0x246E57, DULL, of(new MaterialStack(Carbon, 20), new MaterialStack(Hydrogen, 14), new MaterialStack(Oxygen, 4)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial PhthalicAcid = new SimpleFluidMaterial("phthalic_acid", 0xD1D1D1, FLUID, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 6), new MaterialStack(Oxygen, 4)), GENERATE_FLUID_BLOCK | DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Dimethylbenzene = new SimpleFluidMaterial("dimethylbenzene", 0x669C40, FLUID, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 10)), DISABLE_DECOMPOSITION).setFormula("C6H4(CH3)2", true);
    public static SimpleFluidMaterial Diaminobenzidine = new SimpleFluidMaterial("diaminobenzidine", 0x337D59, DULL, of(new MaterialStack(Carbon, 12), new MaterialStack(Hydrogen, 14), new MaterialStack(Nitrogen, 4)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Dichlorobenzidine = new SimpleFluidMaterial("dichlorobenzidine", 0xA1DEA6, DULL, of(new MaterialStack(Carbon, 12), new MaterialStack(Hydrogen, 10), new MaterialStack(Chlorine, 2), new MaterialStack(Nitrogen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Nitrochlorobenzene = new SimpleFluidMaterial("nitrochlorobenzene", 0x8FB51A, DULL, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 4), new MaterialStack(Chlorine, 1), new MaterialStack(Nitrogen, 1), new MaterialStack(Oxygen, 2)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Chlorobenzene = new SimpleFluidMaterial("chlorobenzene", 0x326A3E, DULL, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 5), new MaterialStack(Chlorine, 1)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial Iron3Chloride = new SimpleFluidMaterial("iron_iii_chloride", 0x060B0B, FLUID, of(new MaterialStack(Iron, 1), new MaterialStack(Chlorine, 3)), DECOMPOSITION_BY_ELECTROLYZING);

    public static SimpleFluidMaterial UraniumHexafluoride = new SimpleFluidMaterial("uranium_hexafluoride", 0x42d126, GAS, of(new MaterialStack(Uranium238, 1), new MaterialStack(Fluorine, 6)), DISABLE_DECOMPOSITION | STATE_GAS).setFormula("UF6", true);
    public static SimpleFluidMaterial EnrichedUraniumHexafluoride = new SimpleFluidMaterial("enriched_uranium_hexafluoride", 0x4bf52a, GAS, of(new MaterialStack(Uranium235, 1), new MaterialStack(Fluorine, 6)), DISABLE_DECOMPOSITION | STATE_GAS).setFormula("UF6", true);
    public static SimpleFluidMaterial DepletedUraniumHexafluoride = new SimpleFluidMaterial("depleted_uranium_hexafluoride", 0x74ba66, GAS, of(new MaterialStack(Uranium238, 1), new MaterialStack(Fluorine, 6)), DISABLE_DECOMPOSITION | STATE_GAS).setFormula("UF6", true);

    // HOG


    public static SimpleFluidMaterial NitrousOxide = new SimpleFluidMaterial("nitrous_oxide", 0x7DC8FF, FLUID, of(new MaterialStack(Nitrogen, 2), new MaterialStack(Oxygen, 1)), 0);
    public static SimpleFluidMaterial Octane = new SimpleFluidMaterial("octane", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 18)), DISABLE_DECOMPOSITION);
    public static SimpleFluidMaterial EthylTertButylEther = new SimpleFluidMaterial("ethyl_tertbutyl_ether", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 6), new MaterialStack(Hydrogen, 14), new MaterialStack(Oxygen, 1)), DISABLE_DECOMPOSITION);


    // COAL GAS

    public static SimpleFluidMaterial Ethylbenzene = new SimpleFluidMaterial("ethylbenzene", 0xFFFFFF, FLUID, of(new MaterialStack(Carbon, 8), new MaterialStack(Hydrogen, 10)), DISABLE_DECOMPOSITION).setFormula("C6H5CH2CH3", true);
    public static SimpleFluidMaterial Naphthalene = new SimpleFluidMaterial("naphthalene", 0xF4F4D7, FLUID, of(new MaterialStack(Carbon, 10), new MaterialStack(Hydrogen, 8)), DISABLE_DECOMPOSITION);

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
    public static GemMaterial Apatite = new GemMaterial(300, "apatite", 0xC8C8FF, DIAMOND, 1, of(new MaterialStack(Calcium, 5), new MaterialStack(Phosphate, 3), new MaterialStack(Chlorine, 1)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | CRYSTALLIZABLE);
    public static IngotMaterial BlackSteel = new IngotMaterial(305, "black_steel", 0x646464, METALLIC, 2, of(new MaterialStack(Nickel, 1), new MaterialStack(BlackBronze, 1), new MaterialStack(Steel, 3)), EXT_METAL, null, 6.5F, 6.5f, 768, 1200);
    public static IngotMaterial RedSteel = new IngotMaterial(306, "red_steel", 0x8C6464, METALLIC, 2, of(new MaterialStack(SterlingSilver, 1), new MaterialStack(BismuthBronze, 1), new MaterialStack(Steel, 2), new MaterialStack(BlackSteel, 4)), EXT_METAL, null, 7.0F, 4.5f, 896, 1300);
    public static IngotMaterial BlueSteel = new IngotMaterial(307, "blue_steel", 0x64648C, METALLIC, 2, of(new MaterialStack(RoseGold, 1), new MaterialStack(Brass, 1), new MaterialStack(Steel, 2), new MaterialStack(BlackSteel, 4)), EXT_METAL | GENERATE_FRAME, null, 7.5F, 5.0f, 1024, 1400);
    public static IngotMaterial DamascusSteel = new IngotMaterial(308, "damascus_steel", 0x6E6E6E, METALLIC, 2, of(new MaterialStack(Steel, 1)), EXT_METAL, null, 8.0F, 5.0f, 1280, 1500);
    public static IngotMaterial TungstenSteel = new IngotMaterial(309, "tungsten_steel", 0x6464A0, METALLIC, 4, of(new MaterialStack(Steel, 1), new MaterialStack(Tungsten, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_DENSE | GENERATE_FRAME| GENERATE_SPRING, null, 8.0F, 4.0f, 2560, 3000);
    public static IngotMaterial RedAlloy = new IngotMaterial(310, "red_alloy", 0xC80000, DULL, 0, of(new MaterialStack(Copper, 1), new MaterialStack(Redstone, 1)), GENERATE_PLATE | GENERATE_FINE_WIRE | GENERATE_BOLT_SCREW);
    public static IngotMaterial CobaltBrass = new IngotMaterial(311, "cobalt_brass", 0xB4B4A0, METALLIC, 2, of(new MaterialStack(Brass, 7), new MaterialStack(Aluminium, 1), new MaterialStack(Cobalt, 1)), EXT2_METAL, null, 8.0F, 2.0f, 256);
    public static DustMaterial TricalciumPhosphate = new DustMaterial(312, "tricalcium_phosphate", 0xFFFF00, FLINT, 2, of(new MaterialStack(Calcium, 3), new MaterialStack(Phosphate, 2)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | FLAMMABLE | EXPLOSIVE | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Basalt = new DustMaterial(313, "basalt", 0x1E1414, ROUGH, 1, of(new MaterialStack(Olivine, 1), new MaterialStack(Calcite, 3), new MaterialStack(Flint, 8), new MaterialStack(DarkAsh, 4)), NO_SMASHING | DECOMPOSITION_BY_CENTRIFUGING);
    public static GemMaterial GarnetRed = new GemMaterial(317, "garnet_red", 0xC85050, RUBY, 2, of(new MaterialStack(Pyrope, 3), new MaterialStack(Almandine, 5), new MaterialStack(Spessartine, 8)), STD_SOLID | GENERATE_LENS | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT | GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING, null, 7.5F, 3.0f, 156);
    public static GemMaterial GarnetYellow = new GemMaterial(318, "garnet_yellow", 0xC8C850, RUBY, 2, of(new MaterialStack(Andradite, 5), new MaterialStack(Grossular, 8), new MaterialStack(Uvarovite, 3)), STD_SOLID | GENERATE_LENS | NO_SMASHING | NO_SMELTING | HIGH_SIFTER_OUTPUT | GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING, null, 7.5F, 3.0f, 156);
    public static DustMaterial Marble = new DustMaterial(319, "marble", 0xC8C8C8, FINE, 1, of(new MaterialStack(Magnesium, 1), new MaterialStack(Calcite, 7)), NO_SMASHING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial Sugar = new DustMaterial(320, "sugar", 0xFAFAFA, FINE, 1, of(new MaterialStack(Carbon, 2), new MaterialStack(Water, 5), new MaterialStack(Oxygen, 25)), 0);
    public static GemMaterial Vinteum = new GemMaterial(321, "vinteum", 0x64C8FF, EMERALD, 3, of(), STD_GEM | NO_SMASHING | NO_SMELTING, 12.0F, 3.0f, 128);
    public static DustMaterial Redrock = new DustMaterial(322, "redrock", 0xFF5032, ROUGH, 1, of(new MaterialStack(Calcite, 2), new MaterialStack(Flint, 1), new MaterialStack(Clay, 1)), NO_SMASHING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial PotassiumFeldspar = new DustMaterial(323, "potassium_feldspar", 0x782828, FINE, 1, of(new MaterialStack(Potassium, 1), new MaterialStack(Aluminium, 1), new MaterialStack(Silicon, 3), new MaterialStack(Oxygen, 8)), 0);
    public static DustMaterial GraniteBlack = new DustMaterial(325, "granite_black", 0x0A0A0A, ROUGH, 3, of(new MaterialStack(SiliconDioxide, 4), new MaterialStack(Biotite, 1)), NO_SMASHING | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial GraniteRed = new DustMaterial(326, "granite_red", 0xFF0080, ROUGH, 3, of(new MaterialStack(Aluminium, 2), new MaterialStack(PotassiumFeldspar, 1), new MaterialStack(Oxygen, 3)), NO_SMASHING);
    public static DustMaterial Chrysotile = new DustMaterial(327, "chrysotile", 0x6E8C6E, ROUGH, 2, of(new MaterialStack(Asbestos, 1)), 0);
    public static DustMaterial VanadiumMagnetite = new DustMaterial(329, "vanadium_magnetite", 0x23233C, METALLIC, 2, of(new MaterialStack(Magnetite, 1), new MaterialStack(Vanadium, 1)), GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING);
    public static DustMaterial BasalticMineralSand = new DustMaterial(330, "basaltic_mineral_sand", 0x283228, SAND, 1, of(new MaterialStack(Magnetite, 1), new MaterialStack(Basalt, 1)), 0);
    public static DustMaterial GraniticMineralSand = new DustMaterial(331, "granitic_mineral_sand", 0x283C3C, SAND, 1, of(new MaterialStack(Magnetite, 1), new MaterialStack(GraniteBlack, 1)), 0);
    public static DustMaterial GarnetSand = new DustMaterial(332, "garnet_sand", 0xC86400, SAND, 1, of(new MaterialStack(GarnetRed, 1), new MaterialStack(GarnetYellow, 1)), 0);
    public static DustMaterial QuartzSand = new DustMaterial(333, "quartz_sand", 0xC8C8C8, SAND, 1, of(new MaterialStack(CertusQuartz, 1), new MaterialStack(Quartzite, 1)), 0);
    public static DustMaterial Pollucite = new DustMaterial(337, "pollucite", 0xF0D2D2, DULL, 2, of(new MaterialStack(Caesium, 2), new MaterialStack(Aluminium, 2), new MaterialStack(Silicon, 4), new MaterialStack(Water, 2), new MaterialStack(Oxygen, 12)), 0);
    public static DustMaterial Vermiculite = new DustMaterial(341, "vermiculite", 0xC8B40F, METALLIC, 2, of(new MaterialStack(Iron, 3), new MaterialStack(Aluminium, 4), new MaterialStack(Silicon, 4), new MaterialStack(Hydrogen, 2), new MaterialStack(Water, 4), new MaterialStack(Oxygen, 12)), 0);
    public static DustMaterial Bentonite = new DustMaterial(342, "bentonite", 0xF5D7D2, ROUGH, 2, of(new MaterialStack(Sodium, 1), new MaterialStack(Magnesium, 6), new MaterialStack(Silicon, 12), new MaterialStack(Hydrogen, 4), new MaterialStack(Water, 5), new MaterialStack(Oxygen, 36)), GENERATE_ORE);
    public static DustMaterial FullersEarth = new DustMaterial(343, "fullers_earth", 0xA0A078, FINE, 2, of(new MaterialStack(Magnesium, 1), new MaterialStack(Silicon, 4), new MaterialStack(Hydrogen, 1), new MaterialStack(Water, 4), new MaterialStack(Oxygen, 11)), 0);
    public static DustMaterial Pitchblende = new DustMaterial(344, "pitchblende", 0xC8D200, DULL, 3, of(new MaterialStack(Uraninite, 3), new MaterialStack(Thorium, 1), new MaterialStack(Lead, 1)), GENERATE_ORE | DECOMPOSITION_BY_CENTRIFUGING).setFormula("(UO2)3ThPb", true);
    public static GemMaterial Monazite = new GemMaterial(345, "monazite", 0x324632, DIAMOND, 1, of(new MaterialStack(RareEarth, 1), new MaterialStack(Phosphate, 1)), GENERATE_ORE | NO_SMASHING | NO_SMELTING | CRYSTALLIZABLE);
    public static DustMaterial Mirabilite = new DustMaterial(347, "mirabilite", 0xF0FAD2, DULL, 2, of(new MaterialStack(Sodium, 2), new MaterialStack(Sulfur, 1), new MaterialStack(Water, 10), new MaterialStack(Oxygen, 4)), 0);
    public static DustMaterial Trona = new DustMaterial(349, "trona", 0x87875F, METALLIC, 1, of(new MaterialStack(Sodium, 3), new MaterialStack(Carbon, 2), new MaterialStack(Hydrogen, 1), new MaterialStack(Water, 2), new MaterialStack(Oxygen, 6)), 0);
    public static DustMaterial Gypsum = new DustMaterial(351, "gypsum", 0xE6E6FA, DULL, 1, of(new MaterialStack(Calcium, 1), new MaterialStack(Sulfur, 1), new MaterialStack(Water, 2), new MaterialStack(Oxygen, 4)), 0);
    public static DustMaterial Zeolite = new DustMaterial(355, "zeolite", 0xF0E6E6, DULL, 2, of(new MaterialStack(Sodium, 1), new MaterialStack(Calcium, 4), new MaterialStack(Silicon, 27), new MaterialStack(Aluminium, 9), new MaterialStack(Water, 28), new MaterialStack(Oxygen, 72)), DISABLE_DECOMPOSITION);
    public static DustMaterial Concrete = new DustMaterial(360, "concrete", 0x646464, ROUGH, 1, of(new MaterialStack(Stone, 1)), NO_SMASHING | SMELT_INTO_FLUID);
    public static IngotMaterial SteelMagnetic = new IngotMaterial(362, "steel_magnetic", 0x808080, MAGNETIC, 2, of(new MaterialStack(Steel, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | MORTAR_GRINDABLE, null, 1000);
    public static IngotMaterial NeodymiumMagnetic = new IngotMaterial(363, "neodymium_magnetic", 0x646464, MAGNETIC, 2, of(new MaterialStack(Neodymium, 1)), EXT2_METAL | GENERATE_LONG_ROD, null, 1297);
    public static IngotMaterial VanadiumSteel = new IngotMaterial(365, "vanadium_steel", 0xc0c0c0, METALLIC, 3, of(new MaterialStack(Vanadium, 1), new MaterialStack(Chrome, 1), new MaterialStack(Steel, 7)), EXT2_METAL, null, 7.0F, 3.0f, 1920, 1453);
    public static IngotMaterial HSSG = new IngotMaterial(366, "hssg", 0x999900, METALLIC, 3, of(new MaterialStack(TungstenSteel, 5), new MaterialStack(Chrome, 1), new MaterialStack(Molybdenum, 2), new MaterialStack(Vanadium, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_FRAME | GENERATE_SPRING, null, 10.0F, 5.5f, 4000, 4200);
    public static IngotMaterial HSSE = new IngotMaterial(367, "hsse", 0x336600, METALLIC, 4, of(new MaterialStack(HSSG, 6), new MaterialStack(Cobalt, 1), new MaterialStack(Manganese, 1), new MaterialStack(Silicon, 1)), EXT2_METAL | GENERATE_RING | GENERATE_ROTOR | GENERATE_SMALL_GEAR | GENERATE_LONG_ROD | GENERATE_FRAME, null, 10.0F, 8.0f, 5120, 5000);
    public static IngotMaterial HSSS = new IngotMaterial(368, "hsss", 0x660033, METALLIC, 4, of(new MaterialStack(HSSG, 6), new MaterialStack(Iridium, 2), new MaterialStack(Osmium, 1)), EXT2_METAL | GENERATE_GEAR | GENERATE_SMALL_GEAR, null, 15.0F, 7.0f, 3000, 5000);
    public static IngotMaterial Potin = new IngotMaterial(370, "potin", 0xc99781, MaterialIconSet.METALLIC, 6, of(new MaterialStack(Lead, 2), new MaterialStack(Bronze, 2), new MaterialStack(Tin, 1)), EXT2_METAL, null);
    public static Material Brick;
    public static Material Fireclay;
    public static Material HydratedCoal;
    public static IngotMaterial BorosilicateGlass = new IngotMaterial(263, "borosilicate_glass", 0xE6F3E6, SHINY, 1, of(new MaterialStack(Boron, 1), new MaterialStack(SiliconDioxide, 7)), GENERATE_FINE_WIRE);


    // TODO Move these three to SecondDegreeMaterials
    Brick = new Material.Builder(191, "brick")
                .dust(1)
                .color(0x9B5643).iconSet(ROUGH)
                .flags(EXCLUDE_BLOCK_CRAFTING_RECIPES, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Clay, 1)
                .build();

    Fireclay = new Material.Builder(192, "fireclay")
                .dust()
                .color(0xADA09B).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Clay, 1)
                .build();


    HydratedCoal = new Material.Builder(240, "hydrated_coal")
                .dust(1)
                .color(0x464664).iconSet(ROUGH)
                .components(Coal, 8, Water, 1)
                .build();

    public static IngotMaterial NaquadahAlloy = new IngotMaterial(372, "naquadah_alloy", 0x282828, METALLIC, 5, of(new MaterialStack(Naquadah, 1), new MaterialStack(Osmiridium, 1)), EXT2_METAL| GENERATE_SPRING, null, 8.0F, 5.0f, 5120, 7200);
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
        for (DustMaterial dustMaterial : new DustMaterial[]{Monazite}) {
            dustMaterial.separatedOnto = Neodymium;
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{VanadiumMagnetite, BasalticMineralSand, GraniticMineralSand}) {
            dustMaterial.separatedOnto = Gold;
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{Vermiculite}) {
            dustMaterial.separatedOnto = Iron;
        }
        for (DustMaterial dustMaterial : new DustMaterial[]{BasalticMineralSand, GraniticMineralSand}) {
            dustMaterial.addFlag(BLAST_FURNACE_CALCITE_DOUBLE);
        }

        // TODO Leave these 9 for now /////////////////////
        NeodymiumMagnetic.setSmeltingInto(Neodymium);
        NeodymiumMagnetic.setArcSmeltingInto(Neodymium);
        NeodymiumMagnetic.setMaceratingInto(Neodymium);

        SteelMagnetic.setSmeltingInto(Steel);
        SteelMagnetic.setArcSmeltingInto(Steel);
        SteelMagnetic.setMaceratingInto(Steel);

        IronMagnetic.setSmeltingInto(Iron);
        IronMagnetic.setArcSmeltingInto(WroughtIron);
        IronMagnetic.setMaceratingInto(Iron);
        // TODO ///////////////////////////////////////////

        BasalticMineralSand.setDirectSmelting(Iron);
        GraniticMineralSand.setDirectSmelting(Iron);

        Bentonite.setOreMultiplier(7);
        GarnetYellow.setOreMultiplier(4);
        GarnetRed.setOreMultiplier(4);
        Olivine.setOreMultiplier(2);
        Pitchblende.setOreMultiplier(2);
        TricalciumPhosphate.setOreMultiplier(3);
        Apatite.setOreMultiplier(4);
        Apatite.setByProductMultiplier(2);
        Redstone.setOreMultiplier(5);
        Lapis.setOreMultiplier(6);
        Lapis.setByProductMultiplier(4);
        Monazite.setOreMultiplier(8);
        Monazite.setByProductMultiplier(2);

        Lignite.setBurnTime(1200); //2/3 of burn time of coal
        Wood.setBurnTime(300); //default wood burn time in vanilla

        Vermiculite.addOreByProducts(Iron, Aluminium, Magnesium);
        FullersEarth.addOreByProducts(Aluminium, Silicon, Magnesium);
        Bentonite.addOreByProducts(Aluminium, Calcium, Magnesium);
        Pitchblende.addOreByProducts(Thorium, Uranium238, Lead);
        Lapis.addOreByProducts(Lazurite, Sodalite, Pyrite);
        GarnetRed.addOreByProducts(Spessartine, Pyrope, Almandine);
        GarnetYellow.addOreByProducts(Andradite, Grossular, Uvarovite);
        Pollucite.addOreByProducts(Caesium, Aluminium, Rubidium);
        Chrysotile.addOreByProducts(Asbestos, Silicon, Magnesium);
        QuartzSand.addOreByProducts(CertusQuartz, Quartzite, Barite);
        Redstone.addOreByProducts(Cinnabar, RareEarth, Glowstone);
        Monazite.addOreByProducts(Thorium, Neodymium, RareEarth);
        Glowstone.addOreByProducts(Redstone, Gold);
        Diatomite.addOreByProducts(BandedIron, Sapphire);
        Olivine.addOreByProducts(Pyrope, Magnesium, Manganese);
        GarnetSand.addOreByProducts(GarnetRed, GarnetYellow);
        GraniticMineralSand.addOreByProducts(GraniteBlack, Magnetite);
        BasalticMineralSand.addOreByProducts(Basalt, Magnetite);
        Basalt.addOreByProducts(Olivine, DarkAsh);
        VanadiumMagnetite.addOreByProducts(Magnetite, Vanadium);
        TricalciumPhosphate.addOreByProducts(Apatite, Phosphate);
        NaquadahEnriched.addOreByProducts(Naquadah, Naquadria);
        Naquadah.addOreByProducts(NaquadahEnriched);
        Lignite.addOreByProducts(Coal);
        Apatite.addOreByProducts(TricalciumPhosphate);
        Netherrack.addOreByProducts(Sulfur);
        Flint.addOreByProducts(Obsidian);
        Endstone.addOreByProducts(Helium3);
        Obsidian.addOreByProducts(Olivine);
        Redrock.addOreByProducts(Clay);
        Marble.addOreByProducts(Calcite);
        GraniteBlack.addOreByProducts(Biotite);
        GraniteRed.addOreByProducts(PotassiumFeldspar);
        Phosphate.addOreByProducts(Phosphorus);
        Opal.addOreByProducts(Tanzanite);
        Amethyst.addOreByProducts(Amethyst);
        Niter.addOreByProducts(Saltpeter);
        Vinteum.addOreByProducts(Vinteum);
        Andesite.addOreByProducts(Basalt);
        Diorite.addOreByProducts(NetherQuartz);

        Vinteum.addEnchantmentForTools(Enchantments.FORTUNE, 2);

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

        Steel.setFluidPipeProperties(2557, 40, true);
        Potin.setFluidPipeProperties(2023, 96, true);
        VanadiumSteel.setFluidPipeProperties(2073, 100, true);
        TungstenSteel.setFluidPipeProperties(7568, 100, true);
        Naquadah.setFluidPipeProperties(19200, 1500, true);
        Duranium.setFluidPipeProperties(100000, 2000, true);
        Neutronium.setFluidPipeProperties(1000000, 2800, true);

        CobaltBrass.setItemPipeProperties(2048, 1);
        Cupronickel.setItemPipeProperties(2048, 1);
        FluxedElectrum.setItemPipeProperties(128, 16);
    }
}
