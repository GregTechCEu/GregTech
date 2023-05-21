package gregtech.api.unification.material.materials;

import gregtech.api.GTValues;
import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.BlastProperty.GasTier;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import net.minecraft.init.Enchantments;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;

public class SecondDegreeMaterials {

    public static void register() {

        // MATERIALS MIGRATED UP FROM FIRST DEGREE
        // These IDs are from the First Degree block,
        // but had to be left as-is when moved to
        // avoid voiding items in worlds.

        Almandine = new Material.Builder(250, "almandine")
                .gem(1).ore()
                .color(0xFF0000)
                .components(Alumina, 5, Iron, 3, SiliconDioxide, 3, Oxygen, 3)
                .build()
                .setFormula("(Al2O3)Fe3(SiO2)3O3", true);

        Asbestos = new Material.Builder(253, "asbestos")
                .dust(1).ore()
                .color(0xE6E6E6)
                .flags(DISABLE_DECOMPOSITION, PURIFY_BY_SIFTING)
                .components(Magnesium, 3, SiliconDioxide, 2, Water, 2, Oxygen, 3)
                .build();

        BlueTopaz = new Material.Builder(257, "blue_topaz")
                .gem(3).ore()
                .color(0x7B96DC).iconSet(GEM_HORIZONTAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING)
                .components(Alumina, 5, Silicon, 1, Fluorine, 2, Water, 1)
                .build().setFormula("(Al2O3)(SiO2)F2(H2O)", true);

        Emerald = new Material.Builder(278, "emerald")
                .gem().ore()
                .color(0x50FF50).iconSet(EMERALD)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, GENERATE_LENS)
                .components(Alumina, 1, Beryllium, 3, SiliconDioxide, 6, Oxygen, 3)
                .build();

        Grossular = new Material.Builder(282, "grossular")
                .gem(1).ore()
                .color(0xC86400).iconSet(RUBY)
                .components(Alumina, 5, Calcium, 3, SiliconDioxide, 3, Oxygen, 3)
                .build().setFormula("(Al2O3)Ca3(SiO2)3O3", true);

        Pyrope = new Material.Builder(308, "pyrope")
                .gem().ore()
                .color(0x783264).iconSet(RUBY)
                .components(Alumina, 5, Magnesium, 3, SiliconDioxide, 3, Oxygen, 3)
                .build().setFormula("(Al2O3)Mg3(SiO2)3O3", true);

        Ruby = new Material.Builder(311, "ruby")
                .gem().ore()
                .color(0xFF6464).iconSet(RUBY)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, GENERATE_LENS, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Alumina, 5, Chromium, 1)
                .build().setFormula("(Al2O3)Cr", true);

        Spessartine = new Material.Builder(321, "spessartine")
                .gem().ore()
                .color(0xFF6464).iconSet(RUBY)
                .components(Alumina, 5, Manganese, 3, SiliconDioxide, 3, Oxygen, 3)
                .build().setFormula("(Al2O3)Mn3(SiO2)3O3", true);

        Topaz = new Material.Builder(329, "topaz")
                .gem(3).ore()
                .color(0xFF8000).iconSet(GEM_HORIZONTAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING)
                .components(Alumina, 5, Fluorine, 1, SiliconDioxide, 1, Water, 1)
                .build().setFormula("(Al2O3)F(SiO2)(H2O)", true);

        Spodumene = new Material.Builder(381, "spodumene")
                .dust().ore()
                .color(0xBEAAAA)
                .components(Alumina, 5, Lithium, 2, SiliconDioxide, 4, Oxygen, 1)
                .build().setFormula("(Al2O3)Li2(SiO2)4O", true);

        Lepidolite = new Material.Builder(382, "lepidolite")
                .dust().ore()
                .color(0xF0328C).iconSet(FINE)
                .components(Alumina, 2, Potassium, 1, Lithium, 3, Fluorine, 2, Oxygen, 6)
                .build();

        Talc = new Material.Builder(392, "talc")
                .dust().ore()
                .color(0x5AB45A).iconSet(FINE)
                .components(Magnesium, 3, SiliconDioxide, 4, Water, 1, Oxygen, 3)
                .build();

        Soapstone = new Material.Builder(393, "soapstone")
                .dust(1).ore()
                .color(0x5F915F)
                .components(Magnesium, 3, SiliconDioxide, 4, Water, 1, Oxygen, 3)
                .build();

        Kyanite = new Material.Builder(394, "kyanite")
                .dust().ore()
                .color(0x6E6EFA).iconSet(FLINT)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Alumina, 1, SiliconDioxide, 1)
                .build();


        ////////////////////////////
        // Start of new Materials //
        ////////////////////////////


        Glass = new Material.Builder(2000, "glass")
                .gem(0).fluid()
                .color(0xFAFAFA).iconSet(GLASS)
                .flags(GENERATE_LENS, NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_RECIPES, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .fluidTemp(1200)
                .build();

        // FREE ID 2001

        Borax = new Material.Builder(2002, "borax")
                .dust(1)
                .color(0xFAFAFA).iconSet(FINE)
                .components(Sodium, 2, Boron, 4, Water, 10, Oxygen, 7)
                .build();

        SaltWater = new Material.Builder(2003, "salt_water")
                .fluid()
                .color(0x0000C8)
                .flags(DISABLE_DECOMPOSITION)
                .components(Salt, 1, Water, 1)
                .build();

        Olivine = new Material.Builder(2004, "olivine")
                .gem().ore()
                .color(0x96FF96).iconSet(RUBY)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING)
                .components(Magnesium, 2, Iron, 1, SiliconDioxide, 2)
                .build();

        Opal = new Material.Builder(2005, "opal")
                .gem().ore()
                .color(0x0000FF).iconSet(OPAL)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .build();

        Amethyst = new Material.Builder(2006, "amethyst")
                .gem(3).ore()
                .color(0xD232D2).iconSet(RUBY)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 4, Iron, 1)
                .build();

        Lapis = new Material.Builder(2007, "lapis")
                .gem(1).ore(5)
                .color(0x4646DC).iconSet(LAPIS)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, NO_WORKING, DECOMPOSITION_BY_ELECTROLYZING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        GENERATE_PLATE, GENERATE_ROD)
                .components(Lazurite, 12, Sodalite, 2, Pyrite, 1, Calcite, 1)
                .build();

        Blaze = new Material.Builder(2008, "blaze")
                .dust(1).fluid()
                .color(0xFFC800, false).iconSet(FINE)
                .flags(NO_SMELTING, MORTAR_GRINDABLE, DECOMPOSITION_BY_CENTRIFUGING) //todo burning flag
                .components(DarkAsh, 1, Sulfur, 1)
                .fluidTemp(4000)
                .build();

        // Free ID 2009

        Apatite = new Material.Builder(2010, "apatite")
                .gem(1).ore(2)
                .color(0xC8C8FF).iconSet(DIAMOND)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE, GENERATE_BOLT_SCREW)
                .components(Calcium, 5, Phosphorus, 3, Oxygen, 12, Chlorine, 1)
                .build();

        BlackSteel = new Material.Builder(2011, "black_steel")
                .ingot().fluid()
                .color(0x646464).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FINE_WIRE, GENERATE_GEAR, GENERATE_FRAME)
                .components(Nickel, 1, BlackBronze, 1, Steel, 3)
                .cableProperties(GTValues.V[4], 3, 2)
                .blastTemp(1200, GasTier.LOW)
                .build();

        DamascusSteel = new Material.Builder(2012, "damascus_steel")
                .ingot(3).fluid()
                .color(0x6E6E6E).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_GEAR)
                .components(Steel, 1)
                .toolStats(ToolProperty.Builder.of(6.0F, 4.0F, 1024, 3)
                        .attackSpeed(0.3F).enchantability(33)
                        .enchantment(Enchantments.LOOTING, 3)
                        .enchantment(Enchantments.FORTUNE, 3).build())
                .blastTemp(1500, GasTier.LOW)
                .build();

        TungstenSteel = new Material.Builder(2013, "tungsten_steel")
                .ingot(4).fluid()
                .color(0x6464A0).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_DENSE, GENERATE_FRAME, GENERATE_SPRING, GENERATE_FOIL, GENERATE_FINE_WIRE, GENERATE_GEAR)
                .components(Steel, 1, Tungsten, 1)
                .toolStats(ToolProperty.Builder.of(9.0F, 7.0F, 2048, 4)
                        .enchantability(14).build())
                .rotorStats(8.0f, 4.0f, 2560)
                .fluidPipeProperties(3587, 225, true)
                .cableProperties(GTValues.V[5], 3, 2)
                .blastTemp(3000, GasTier.MID, GTValues.VA[EV], 1000)
                .build();

        CobaltBrass = new Material.Builder(2014, "cobalt_brass")
                .ingot().fluid()
                .color(0xB4B4A0).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_GEAR)
                .components(Brass, 7, Aluminium, 1, Cobalt, 1)
                .toolStats(ToolProperty.Builder.of(2.5F, 2.0F, 1024, 2)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(8.0f, 2.0f, 256)
                .itemPipeProperties(2048, 1)
                .fluidTemp(1202)
                .build();

        TricalciumPhosphate = new Material.Builder(2015, "tricalcium_phosphate")
                .dust().ore(2)
                .color(0xFFFF00).iconSet(FLINT)
                .flags(NO_SMASHING, NO_SMELTING)
                .components(Calcium, 3, Phosphorus, 2, Oxygen, 8)
                .build();

        // TODO REMOVE
        GarnetRed = new Material.Builder(2016, "garnet_red")
                .gem().ore()
                .color(0xC85050).iconSet(RUBY)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Pyrope, 3, Almandine, 5, Spessartine, 8)
                .build();

        // TODO REMOVE
        GarnetYellow = new Material.Builder(2017, "garnet_yellow")
                .gem().ore()
                .color(0xC8C850).iconSet(RUBY)
                .flags(EXT_METAL, NO_SMASHING, NO_SMELTING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Andradite, 5, Grossular, 8, Uvarovite, 3)
                .build();

        Marble = new Material.Builder(2018, "marble")
                .dust()
                .color(0xC8C8C8).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnesium, 1, Calcite, 7)
                .build();

        GraniteBlack = new Material.Builder(2019, "granite_black")
                .dust()
                .color(0x0A0A0A).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 4, Biotite, 1)
                .build();

        GraniteRed = new Material.Builder(2020, "granite_red")
                .dust()
                .color(0xFF0080).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Alumina, 5, PotassiumFeldspar, 1)
                .build()
                .setFormula("Al2O3(KAlSiO8)", true);

        // Free ID 2021

        VanadiumMagnetite = new Material.Builder(2022, "vanadium_magnetite")
                .dust().ore()
                .color(0x23233C).iconSet(METALLIC)
                .flags(DECOMPOSITION_BY_CENTRIFUGING, MAGNETIC_ORE)
                .components(Magnetite, 1, Vanadium, 1)
                .build();

        QuartzSand = new Material.Builder(2023, "quartz_sand")
                .dust(1)
                .color(0xC8C8C8).iconSet(SAND)
                .flags(DISABLE_DECOMPOSITION)
                .components(CertusQuartz, 1, Quartzite, 1)
                .build();

        Pollucite = new Material.Builder(2024, "pollucite")
                .dust().ore()
                .color(0xF0D2D2)
                .components(Alumina, 1, Caesium, 2, SiliconDioxide, 4, Water, 2, Oxygen, 1)
                .build();

        // Free ID 2025

        // ID 2026 RESERVED: Bentonite

        // ID 2027 RESERVED: FullersEarth

        Pitchblende = new Material.Builder(2028, "pitchblende")
                .dust(3).ore(true)
                .color(0xC8D200)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Uraninite, 3, Thorianite, 1, Lead, 1)
                .build();

        Monazite = new Material.Builder(2029, "monazite")
                .gem(1).ore(2, true)
                .color(0x324632).iconSet(DIAMOND)
                .flags(NO_SMASHING, NO_SMELTING, CRYSTALLIZABLE)
                .components(RareEarth, 1, Phosphorus, 1, Oxygen, 4)
                .build();

        Mirabilite = new Material.Builder(2030, "mirabilite")
                .dust()
                .color(0xF0FAD2)
                .components(Sodium, 2, Sulfur, 1, Water, 10, Oxygen, 4)
                .build();

        // FREE ID 2031

        Gypsum = new Material.Builder(2032, "gypsum")
                .dust(1).ore()
                .color(0xE6E6FA)
                .components(Calcium, 1, Sulfur, 1, Oxygen, 4, Water, 2)
                .build();

        Zeolite = new Material.Builder(2033, "zeolite")
                .dust().ore()
                .color(0xF0E6E6)
                .flags(PURIFY_BY_SIFTING)
                .components(Alumina, 1, Sodium, 2, SiliconDioxide, 3, Water, 2, Oxygen, 1)
                .build();

        Concrete = new Material.Builder(2034, "concrete")
                .dust().fluid()
                .color(0x646464).iconSet(ROUGH)
                .flags(NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Stone, 1)
                .fluidTemp(286)
                .build();

        SteelMagnetic = new Material.Builder(2035, "steel_magnetic")
                .ingot()
                .color(0x808080).iconSet(MAGNETIC)
                .flags(GENERATE_ROD, IS_MAGNETIC)
                .components(Steel, 1)
                .ingotSmeltInto(Steel)
                .arcSmeltInto(Steel)
                .macerateInto(Steel)
                .build();
        Steel.getProperty(PropertyKey.INGOT).setMagneticMaterial(SteelMagnetic);

        VanadiumSteel = new Material.Builder(2036, "vanadium_steel")
                .ingot(3).fluid()
                .color(0xc0c0c0).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_FOIL, GENERATE_GEAR)
                .components(Steel, 7, Vanadium, 1, Chromium, 1)
                .toolStats(ToolProperty.Builder.of(3.0F, 3.0F, 1536, 3)
                        .attackSpeed(-0.2F).enchantability(5).build())
                .rotorStats(7.0f, 3.0f, 1920)
                .fluidPipeProperties(2073, 50, true, true, false, false)
                .blastTemp(1453, GasTier.LOW)
                .fluidTemp(2073)
                .build();

        Potin = new Material.Builder(2037, "potin")
                .ingot().fluid()
                .color(0xc99781).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_GEAR)
                .components(Copper, 6, Tin, 2, Lead, 1)
                .fluidPipeProperties(1456, 32, true)
                .fluidTemp(1084)
                .build();

        BorosilicateGlass = new Material.Builder(2038, "borosilicate_glass")
                .ingot(1).fluid()
                .color(0xE6F3E6).iconSet(SHINY)
                .flags(GENERATE_FINE_WIRE, GENERATE_PLATE)
                .components(Boron, 1, SiliconDioxide, 7)
                .fluidTemp(1921)
                .build();

        Andesite = new Material.Builder(2039, "andesite")
                .dust()
                .color(0xBEBEBE).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Asbestos, 4, Saltpeter, 1)
                .build();

        // FREE ID 2040

        // FREE ID 2041

        NaquadahAlloy = new Material.Builder(2042, "naquadah_alloy")
                .ingot(5).fluid()
                .color(0x282828).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_SPRING, GENERATE_RING, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_FRAME, GENERATE_DENSE, GENERATE_FOIL, GENERATE_GEAR)
                .components(Naquadah, 2, Osmiridium, 1, Trinium, 1)
                .toolStats(ToolProperty.Builder.of(40.0F, 12.0F, 3072, 5)
                        .attackSpeed(0.3F).enchantability(33).magnetic().build())
                .rotorStats(8.0f, 5.0f, 5120)
                .cableProperties(GTValues.V[8], 2, 4)
                .blastTemp(7200, GasTier.HIGH, VA[LuV], 1000)
                .build();

        //FREE ID 2043

        //FREE ID 2044

        //FREE ID 2045

        NitrationMixture = new Material.Builder(2046, "nitration_mixture")
                .fluid(FluidTypes.ACID)
                .color(0xE6E2AB)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitricAcid, 1, SulfuricAcid, 1)
                .build();

        DilutedSulfuricAcid = new Material.Builder(2047, "diluted_sulfuric_acid")
                .fluid(FluidTypes.ACID)
                .color(0xC07820)
                .flags(DISABLE_DECOMPOSITION)
                .components(SulfuricAcid, 2, Water, 1)
                .build();

        DilutedHydrochloricAcid = new Material.Builder(2048, "diluted_hydrochloric_acid")
                .fluid(FluidTypes.ACID)
                .color(0x99A7A3)
                .flags(DISABLE_DECOMPOSITION)
                .components(HydrochloricAcid, 1, Water, 1)
                .build();

        Flint = new Material.Builder(2049, "flint")
                .gem(1)
                .color(0x002040).iconSet(FLINT)
                .flags(NO_SMASHING, MORTAR_GRINDABLE, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .toolStats(ToolProperty.Builder.of(0.0F, 1.0F, 64, 1)
                        .enchantability(5).ignoreCraftingTools()
                        .enchantment(Enchantments.FIRE_ASPECT, 2).build())
                .build();

        Air = new Material.Builder(2050, "air")
                .fluid(FluidTypes.GAS)
                .color(0xA9D0F5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Nitrogen, 78, Oxygen, 21, Argon, 9)
                .build();

        LiquidAir = new Material.Builder(2051, "liquid_air")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .components(Nitrogen, 70, Oxygen, 22, CarbonDioxide, 5, Helium, 2, Argon, 1, Ice, 1)
                .fluidTemp(79)
                .build();

        NetherAir = new Material.Builder(2052, "nether_air")
                .fluid(FluidTypes.GAS)
                .color(0x4C3434)
                .flags(DISABLE_DECOMPOSITION)
                .components(CarbonMonoxide, 78, HydrogenSulfide, 21, Neon, 9)
                .build();

        LiquidNetherAir = new Material.Builder(2053, "liquid_nether_air")
                .fluid()
                .color(0x4C3434)
                .flags(DISABLE_DECOMPOSITION)
                .components(CarbonMonoxide, 144, CoalGas, 20, HydrogenSulfide, 15, SulfurDioxide, 15, Helium3, 5, Neon, 1, Ash, 1)
                .fluidTemp(58)
                .build();

        EnderAir = new Material.Builder(2054, "ender_air")
                .fluid(FluidTypes.GAS)
                .color(0x283454)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitrogenDioxide, 78, Deuterium, 21, Xenon, 9)
                .build();

        LiquidEnderAir = new Material.Builder(2055, "liquid_ender_air")
                .fluid()
                .color(0x283454)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitrogenDioxide, 122, Deuterium, 50, Helium, 15, Tritium, 10, Krypton, 1, Xenon, 1, Radon, 1, EnderPearl, 1)
                .fluidTemp(36)
                .build();

        AquaRegia = new Material.Builder(2056, "aqua_regia")
                .fluid(FluidTypes.ACID)
                .color(0xFFB132)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitricAcid, 1, HydrochloricAcid, 2)
                .build();

        PlatinumSludgeResidue = new Material.Builder(2057, "platinum_sludge_residue")
                .dust()
                .color(0x827951)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 2, Gold, 3)
                .build();

        PalladiumRaw = new Material.Builder(2058, "palladium_raw")
                .dust()
                .color(Palladium.getMaterialRGB()).iconSet(METALLIC)
                .flags(DISABLE_DECOMPOSITION)
                .components(Palladium, 1, Ammonia, 1)
                .build();

        RarestMetalMixture = new Material.Builder(2059, "rarest_metal_mixture")
                .dust()
                .color(0x832E11).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Iridium, 1, Osmium, 1, Oxygen, 4, Water, 1)
                .build();

        SalAmmoniac = new Material.Builder(2060, "sal_ammoniac")
                .dust().ore()
                .color(0xE0C3E3).iconSet(FINE)
                .components(Ammonia, 1, HydrochloricAcid, 1)
                .build()
                .setFormula("NH4Cl", true);

        AcidicOsmiumSolution = new Material.Builder(2061, "acidic_osmium_solution")
                .fluid(FluidTypes.ACID)
                .color(0xA3AA8A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Osmium, 1, Oxygen, 4, Water, 1, HydrochloricAcid, 1)
                .build();

        Rhodallium = new Material.Builder(2062, "rhodallium")
                .ingot().fluid()
                .color(0xDAC5C5).iconSet(SHINY)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_DENSE, GENERATE_SMALL_GEAR)
                .components(Palladium, 3, Rhodium, 1)
                .rotorStats(12.0f, 3.0f, 1024)
                .blastTemp(4500, GasTier.HIGH, VA[IV], 1200)
                .build();

        Clay = new Material.Builder(2063, "clay")
                .dust(1)
                .color(0xC8C8DC).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .components(Sodium, 2, Lithium, 1, Aluminium, 2, Silicon, 2, Water, 6)
                .build();

        Redstone = new Material.Builder(2064, "redstone")
                .dust().ore(4, true).fluid()
                .color(0xC80000).iconSet(ROUGH)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        EXCLUDE_PLATE_COMPRESSOR_RECIPE, DECOMPOSITION_BY_CENTRIFUGING, PURIFY_BY_SIFTING)
                .components(Silicon, 1, Pyrite, 5, Ruby, 1, Mercury, 3)
                .fluidTemp(500)
                .build();

        PlatinumGroupSlurry = new Material.Builder(2065, "platinum_group_slurry")
                .fluid()
                .color(0x2F3030)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(PlatinumGroupSludge, 1, Water, 3)
                .build();

        Bentonite = new Material.Builder(2026, "bentonite")
                .dust().ore()
                .color(0xF5D7D2).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(SodiumHydroxide, 1, Clay, 13)
                .build();

        FullersEarth = new Material.Builder(2027, "fullers_earth")
                .dust().ore()
                .color(0xA0A078).iconSet(FINE)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Magnesium, 1, Clay, 13)
                .build();
    }
}
