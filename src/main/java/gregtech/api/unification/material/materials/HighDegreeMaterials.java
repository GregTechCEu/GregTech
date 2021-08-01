package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.type.RoughSolidMaterial;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;

import static com.google.common.collect.ImmutableList.of;
import static gregtech.api.unification.material.MaterialIconSet.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.type.MaterialFlags.*;

public class HighDegreeMaterials {

    public static void register() {

        Glass = new Material.Builder(2000, "glass")
                .gem(0).fluid()
                .color(0xFAFAFA).iconSet(GLASS)
                .flags(GENERATE_LENS, NO_SMASHING, EXCLUDE_BLOCK_CRAFTING_RECIPES, DECOMPOSITION_BY_CENTRIFUGING)
                .components(SiliconDioxide, 1)
                .build();

        Perlite = new Material.Builder(2001, "perlite")
                .dust(1)
                .color(0x1E141E)
                .components(Obsidian, 2, Water, 1)
                .build();

        Borax = new Material.Builder(2002, "borax")
                .dust(1)
                .color(0xFAFAFA).iconSet(FINE)
                .components(Sodium, 2, Boron, 4, Water, 10, Oxygen, 7)
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

        Blaze = new Material.Builder(2008, "blaze")
                .dust(1).fluid()
                .color(0xFFC800).iconSet(FINE)
                .flags(NO_SMELTING, MORTAR_GRINDABLE, DECOMPOSITION_BY_CENTRIFUGING) //todo burning flag
                .components(DarkAsh, 1, Sulfur, 1)
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

        BlackSteel = new Material.Builder(2011, "black_steel")
                .ingot().fluid()
                .color(0x646464).iconSet(METALLIC)
                .flags(EXT_METAL)
                .components(Nickel, 1, BlackBronze, 1, Steel, 3)
                .toolStats(6.5f, 6.5f, 768)
                .blastTemp(1200)
                .build();

        DamascusSteel = new Material.Builder(2012, "damascus_steel")
                .ingot().fluid()
                .color(0x6E6E6E).iconSet(METALLIC)
                .flags(EXT_METAL)
                .components(Steel, 1)
                .toolStats(8.0f, 5.0f, 1280)
                .blastTemp(1500)
                .build();

        TungstenSteel = new Material.Builder(2013, "tungsten_steel")
                .ingot(4).fluid()
                .color(0x6464A0).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_DENSE, GENERATE_FRAME, GENERATE_SPRING)
                .components(Steel, 1, Tungsten, 1)
                .toolStats(8.0f, 4.0f, 2560)
                .fluidPipeProperties(7568, 100, true)
                .blastTemp(3000)
                .build();

        CobaltBrass = new Material.Builder(2014, "cobalt_brass")
                .ingot().fluid()
                .color(0xB4B4A0).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Brass, 7, Aluminium, 1, Cobalt, 1)
                .toolStats(8.0f, 2.0f, 256)
                .itemPipeProperties(2048, 1)
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
                .addOreByproducts(Magnetite, Vanadium)
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
                .build();

        Mirabilite = new Material.Builder(2030, "mirabilite")
                .dust()
                .color(0xF0FAD2)
                .components(Sodium, 2, Sulfur, 1, Water, 10, Oxygen, 4)
                .build();

        Trona = new Material.Builder(2031, "trona")
                .dust(1)
                .color(0x87875F).iconSet(METALLIC)
                .components(Sodium, 3, Carbon, 2, Hydrogen, 1, Water, 2, Oxygen, 6)
                .build();

        Gypsum = new Material.Builder(2032, "gypsum")
                .dust(1)
                .color(0xE6E6FA)
                .components(Calcium, 1, Sulfur, 1, Water, 2, Oxygen, 4)
                .build();

        Zeolite = new Material.Builder(2033, "zeolite")
                .dust()
                .color(0xF0E6E6)
                .components(Sodium, 1, Calcium, 4, Silicon, 27, Aluminium, 9, Water, 28, Oxygen, 72)
                .build();

        Concrete = new Material.Builder(2034, "concrete")
                .dust(1).fluid()
                .color(0x646464).iconSet(ROUGH)
                .flags(NO_SMASHING)
                .components(Stone, 1)
                .build();

        SteelMagnetic = new Material.Builder(2035, "steel_magnetic")
                .ingot().fluid()
                .color(0x808080).iconSet(MAGNETIC)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, MORTAR_GRINDABLE)
                .components(Steel, 1)
                .blastTemp(1000)
                .build();

        VanadiumSteel = new Material.Builder(2036, "vanadium_steel")
                .ingot(3).fluid()
                .color(0xc0c0c0).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Vanadium, 1, Chrome, 1, Steel, 7)
                .toolStats(7.0f, 3.0f, 1920)
                .fluidPipeProperties(2073, 100, true)
                .blastTemp(1453)
                .build();

        Potin = new Material.Builder(2037, "potin")
                .ingot().fluid()
                .color(0xc99781).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Lead, 2, Bronze, 2, Tin, 1)
                .fluidPipeProperties(2023, 96, true)
                .build();

        BorosilicateGlass = new Material.Builder(2038, "borosilicate_glass")
                .ingot(1).fluid()
                .color(0xE6F3E6).iconSet(SHINY)
                .flags(GENERATE_FINE_WIRE)
                .components(Boron, 1, SiliconDioxide, 7)
                .build();

        Brick = new Material.Builder(2039, "brick")
                .dust(1)
                .color(0x9B5643).iconSet(ROUGH)
                .flags(EXCLUDE_BLOCK_CRAFTING_RECIPES, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Clay, 1)
                .build();

        Fireclay = new Material.Builder(2040, "fireclay")
                .dust()
                .color(0xADA09B).iconSet(ROUGH)
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Clay, 1)
                .build();


        HydratedCoal = new Material.Builder(2041, "hydrated_coal")
                .dust(1)
                .color(0x464664).iconSet(ROUGH)
                .components(Coal, 8, Water, 1)
                .build();

        NaquadahAlloy = new Material.Builder(2042, "naquadah_alloy")
                .ingot(5).fluid()
                .color(0x282828).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_SPRING)
                .components(Naquadah, 1, Osmiridium, 1)
                .toolStats(8.0f, 5.0f, 5120)
                .blastTemp(7200)
                .build();

        NickelSulfateSolution = new Material.Builder(2043, "nickel_sulfate_water_solution")
                .fluid()
                .color(0x3EB640)
                .components(Nickel, 1, Sulfur, 1, Oxygen, 4, Water, 6)
                .build();

        CopperSulfateSolution = new Material.Builder(2043, "blue_vitriol_water_solution")
                .fluid()
                .color(0x48A5C0)
                .components(Copper, 1, Sulfur, 1, Oxygen, 4, Water, 5)
                .build();

        LeadZincSolution = new Material.Builder(2043, "lead_zinc_solution")
                .fluid()
                .flags(DECOMPOSITION_BY_CENTRIFUGING)
                .components(Lead, 1, Silver, 1, Zinc, 1, Sulfur, 3, Water, 1)
                .build();

        NitrationMixture = new Material.Builder(2044, "nitration_mixture")
                .fluid()
                .color(0xE6E2AB)
                .flags(DISABLE_DECOMPOSITION)
                .components(NitricAcid, 1, SulfuricAcid, 1)
                .build();

        DilutedSulfuricAcid = new Material.Builder(2045, "diluted_sulfuric_acid")
                .fluid()
                .color(0xC07820)
                .flags(DISABLE_DECOMPOSITION)
                .components(SulfuricAcid, 2, Water, 1)
                .build();

        DilutedHydrochloricAcid = new Material.Builder(2046, "diluted_hydrochloric_acid")
                .fluid()
                .color(0x99A7A3)
                .flags(DISABLE_DECOMPOSITION)
                .components(HydrochloricAcid, 1, Water, 1)
                .build();

        Air = new Material.Builder(2047, "air")
                .fluid(Material.FluidType.GAS)
                .color(0xA9D0F5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Nitrogen, 40, Oxygen, 11, Argon, 1, NobleGases, 1)
                .build();

        LiquidAir = new Material.Builder(2048, "liquid_air")
                .fluid()
                .color(0xA9D0F5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Nitrogen, 40, Oxygen, 11, Argon, 1, NobleGases, 1)
                .build();

        public static DustMaterial Diatomite = new DustMaterial(297, "diatomite", 0xE1E1E1, DULL, 1, of(new MaterialStack(Flint, 8), new MaterialStack(BandedIron, 1), new MaterialStack(Sapphire, 1)), 0);


        //todo roughsolid
        public static RoughSolidMaterial Flint = new RoughSolidMaterial(296, "flint", 0x002040, FLINT, 1, of(new MaterialStack(SiliconDioxide, 1)), NO_SMASHING | MORTAR_GRINDABLE | DECOMPOSITION_BY_CENTRIFUGING, () -> OrePrefix.gem);
    }
}
