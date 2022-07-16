package gregtech.api.unification.material.materials;

import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.material.Material;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;

public class UnknownCompositionMaterials {

    public static void register() {

        WoodGas = new Material.Builder(1500, "wood_gas")
                .fluid(FluidTypes.GAS).color(0xDECD87).build();

        WoodVinegar = new Material.Builder(1501, "wood_vinegar")
                .fluid().color(0xD45500).build();

        WoodTar = new Material.Builder(1502, "wood_tar")
                .fluid().color(0x28170B)
                .flags(STICKY, FLAMMABLE).build();

        CharcoalByproducts = new Material.Builder(1503, "charcoal_byproducts")
                .fluid().color(0x784421).build();

        Biomass = new Material.Builder(1504, "biomass")
                .fluid().color(0x00FF00).build();

        BioDiesel = new Material.Builder(1505, "bio_diesel")
                .fluid().color(0xFF8000)
                .flags(FLAMMABLE, EXPLOSIVE).build();

        FermentedBiomass = new Material.Builder(1506, "fermented_biomass")
                .fluid().color(0x445500).fluidTemp(300).build();

        Creosote = new Material.Builder(1507, "creosote")
                .fluid().color(0x804000)
                .flags(STICKY).build();

        Diesel = new Material.Builder(1508, "diesel")
                .fluid().flags(FLAMMABLE, EXPLOSIVE).build();

        RocketFuel = new Material.Builder(1509, "rocket_fuel")
                .fluid().flags(FLAMMABLE, EXPLOSIVE).color(0xBDB78C).build();

        Glue = new Material.Builder(1510, "glue")
                .fluid().flags(STICKY).build();

        Lubricant = new Material.Builder(1511, "lubricant")
                .fluid().build();

        McGuffium239 = new Material.Builder(1512, "mc_guffium_239")
                .fluid().build();

        IndiumConcentrate = new Material.Builder(1513, "indium_concentrate")
                .fluid(FluidTypes.ACID).color(0x0E2950).build();

        SeedOil = new Material.Builder(1514, "seed_oil")
                .fluid().color(0xFFFFFF)
                .flags(STICKY, FLAMMABLE).build();

        DrillingFluid = new Material.Builder(1515, "drilling_fluid")
                .fluid().color(0xFFFFAA).build();

        ConstructionFoam = new Material.Builder(1516, "construction_foam")
                .fluid().color(0x808080).build();

        // Free IDs 1517-1521

        SulfuricHeavyFuel = new Material.Builder(1522, "sulfuric_heavy_fuel")
                .fluid().flags(FLAMMABLE).build();

        HeavyFuel = new Material.Builder(1523, "heavy_fuel")
                .fluid().flags(FLAMMABLE).build();

        LightlyHydroCrackedHeavyFuel = new Material.Builder(1524, "lightly_hydrocracked_heavy_fuel")
                .fluid().color(0xFFFF00).fluidTemp(775).flags(FLAMMABLE).build();

        SeverelyHydroCrackedHeavyFuel = new Material.Builder(1525, "severely_hydrocracked_heavy_fuel")
                .fluid().color(0xFFFF00).fluidTemp(775).flags(FLAMMABLE).build();

        LightlySteamCrackedHeavyFuel = new Material.Builder(1526, "lightly_steamcracked_heavy_fuel")
                .fluid().fluidTemp(775).flags(FLAMMABLE).build();

        SeverelySteamCrackedHeavyFuel = new Material.Builder(1527, "severely_steamcracked_heavy_fuel")
                .fluid().fluidTemp(775).flags(FLAMMABLE).build();

        SulfuricLightFuel = new Material.Builder(1528, "sulfuric_light_fuel")
                .fluid().fluidTemp(775).flags(FLAMMABLE).build();

        LightFuel = new Material.Builder(1529, "light_fuel")
                .fluid().flags(FLAMMABLE).build();

        LightlyHydroCrackedLightFuel = new Material.Builder(1530, "lightly_hydrocracked_light_fuel")
                .fluid().color(0xB7AF08).fluidTemp(775).flags(FLAMMABLE).build();

        SeverelyHydroCrackedLightFuel = new Material.Builder(1531, "severely_hydrocracked_light_fuel")
                .fluid().color(0xB7AF08).fluidTemp(775).flags(FLAMMABLE).build();

        LightlySteamCrackedLightFuel = new Material.Builder(1532, "lightly_steamcracked_light_fuel")
                .fluid().fluidTemp(775).flags(FLAMMABLE).build();

        SeverelySteamCrackedLightFuel = new Material.Builder(1533, "severely_steamcracked_light_fuel")
                .fluid().fluidTemp(775).flags(FLAMMABLE).build();

        SulfuricNaphtha = new Material.Builder(1534, "sulfuric_naphtha")
                .fluid().flags(FLAMMABLE).build();

        Naphtha = new Material.Builder(1535, "naphtha")
                .fluid().flags(FLAMMABLE).build();

        LightlyHydroCrackedNaphtha = new Material.Builder(1536, "lightly_hydrocracked_naphtha")
                .fluid().color(0xBFB608).fluidTemp(775).flags(FLAMMABLE).build();

        SeverelyHydroCrackedNaphtha = new Material.Builder(1537, "severely_hydrocracked_naphtha")
                .fluid().color(0xBFB608).fluidTemp(775).flags(FLAMMABLE).build();

        LightlySteamCrackedNaphtha = new Material.Builder(1538, "lightly_steamcracked_naphtha")
                .fluid().color(0xBFB608).fluidTemp(775).flags(FLAMMABLE).build();

        SeverelySteamCrackedNaphtha = new Material.Builder(1539, "severely_steamcracked_naphtha")
                .fluid().color(0xBFB608).fluidTemp(775).flags(FLAMMABLE).build();

        SulfuricGas = new Material.Builder(1540, "sulfuric_gas")
                .fluid(FluidTypes.GAS).build();

        RefineryGas = new Material.Builder(1541, "refinery_gas")
                .fluid(FluidTypes.GAS).flags(FLAMMABLE).build();

        LightlyHydroCrackedGas = new Material.Builder(1542, "lightly_hydrocracked_gas")
                .fluid(FluidTypes.GAS).color(0xB4B4B4)
                .fluidTemp(775).flags(FLAMMABLE).build();

        SeverelyHydroCrackedGas = new Material.Builder(1543, "severely_hydrocracked_gas")
                .fluid(FluidTypes.GAS).color(0xB4B4B4)
                .fluidTemp(775).flags(FLAMMABLE).build();

        LightlySteamCrackedGas = new Material.Builder(1544, "lightly_steamcracked_gas")
                .fluid(FluidTypes.GAS).color(0xB4B4B4)
                .fluidTemp(775).flags(FLAMMABLE).build();

        SeverelySteamCrackedGas = new Material.Builder(1545, "severely_steamcracked_gas")
                .fluid(FluidTypes.GAS).color(0xB4B4B4)
                .fluidTemp(775).flags(FLAMMABLE).build();

        HydroCrackedEthane = new Material.Builder(1546, "hydrocracked_ethane")
                .fluid(FluidTypes.GAS).color(0x9696BC)
                .fluidTemp(775).flags(FLAMMABLE).build();

        HydroCrackedEthylene = new Material.Builder(1547, "hydrocracked_ethylene")
                .fluid(FluidTypes.GAS).color(0xA3A3A0)
                .fluidTemp(775).flags(FLAMMABLE).build();

        HydroCrackedPropene = new Material.Builder(1548, "hydrocracked_propene")
                .fluid(FluidTypes.GAS).color(0xBEA540)
                .fluidTemp(775).flags(FLAMMABLE).build();

        HydroCrackedPropane = new Material.Builder(1549, "hydrocracked_propane")
                .fluid(FluidTypes.GAS).color(0xBEA540)
                .fluidTemp(775).flags(FLAMMABLE).build();

        HydroCrackedButane = new Material.Builder(1550, "hydrocracked_butane")
                .fluid(FluidTypes.GAS).color(0x852C18)
                .fluidTemp(775).flags(FLAMMABLE).build();

        HydroCrackedButene = new Material.Builder(1551, "hydrocracked_butene")
                .fluid(FluidTypes.GAS).color(0x993E05)
                .fluidTemp(775).flags(FLAMMABLE).build();

        HydroCrackedButadiene = new Material.Builder(1552, "hydrocracked_butadiene")
                .fluid(FluidTypes.GAS).color(0xAD5203)
                .fluidTemp(775).flags(FLAMMABLE).build();

        SteamCrackedEthane = new Material.Builder(1553, "steamcracked_ethane")
                .fluid(FluidTypes.GAS).color(0x9696BC)
                .fluidTemp(775).flags(FLAMMABLE).build();

        SteamCrackedEthylene = new Material.Builder(1554, "steamcracked_ethylene")
                .fluid(FluidTypes.GAS).color(0xA3A3A0)
                .fluidTemp(775).flags(FLAMMABLE).build();

        SteamCrackedPropene = new Material.Builder(1555, "steamcracked_propene")
                .fluid(FluidTypes.GAS).color(0xBEA540)
                .fluidTemp(775).flags(FLAMMABLE).build();

        SteamCrackedPropane = new Material.Builder(1556, "steamcracked_propane")
                .fluid(FluidTypes.GAS).color(0xBEA540)
                .fluidTemp(775).flags(FLAMMABLE).build();

        SteamCrackedButane = new Material.Builder(1557, "steamcracked_butane")
                .fluid(FluidTypes.GAS).color(0x852C18)
                .fluidTemp(775).flags(FLAMMABLE).build();

        SteamCrackedButene = new Material.Builder(1558, "steamcracked_butene")
                .fluid(FluidTypes.GAS).color(0x993E05)
                .fluidTemp(775).flags(FLAMMABLE).build();

        SteamCrackedButadiene = new Material.Builder(1559, "steamcracked_butadiene")
                .fluid(FluidTypes.GAS).color(0xAD5203)
                .fluidTemp(775).flags(FLAMMABLE).build();

        //Free IDs 1560-1575

        LPG = new Material.Builder(1576, "lpg")
                .fluid(FluidTypes.GAS).flags(FLAMMABLE, EXPLOSIVE).build();

        RawGrowthMedium = new Material.Builder(1577, "raw_growth_medium")
                .fluid().color(0xA47351).build();

        SterileGrowthMedium = new Material.Builder(1578, "sterilized_growth_medium")
                .fluid().color(0xAC876E).build();

        Oil = new Material.Builder(1579, "oil")
                .fluid(FluidTypes.LIQUID, true)
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .build();

        OilHeavy = new Material.Builder(1580, "oil_heavy")
                .fluid(FluidTypes.LIQUID, true)
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .build();

        RawOil = new Material.Builder(1581, "oil_medium")
                .fluid(FluidTypes.LIQUID, true)
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .build();

        OilLight = new Material.Builder(1582, "oil_light")
                .fluid(FluidTypes.LIQUID, true)
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .build();

        NaturalGas = new Material.Builder(1583, "natural_gas")
                .fluid(FluidTypes.GAS, true)
                .flags(FLAMMABLE, EXPLOSIVE).build();

        Bacteria = new Material.Builder(1584, "bacteria")
                .fluid().color(0x808000).build();

        BacterialSludge = new Material.Builder(1585, "bacterial_sludge")
                .fluid().color(0x355E3B).build();

        EnrichedBacterialSludge = new Material.Builder(1586, "enriched_bacterial_sludge")
                .fluid().color(0x7FFF00).build();

        // free id: 1587

        Mutagen = new Material.Builder(1588, "mutagen")
                .fluid().color(0x00FF7F).build();

        GelatinMixture = new Material.Builder(1589, "gelatin_mixture")
                .fluid().color(0x588BAE).build();

        RawGasoline = new Material.Builder(1590, "raw_gasoline")
                .fluid().color(0xFF6400).flags(FLAMMABLE).build();

        Gasoline = new Material.Builder(1591, "gasoline")
                .fluid().color(0xFAA500).flags(FLAMMABLE, EXPLOSIVE).build();

        HighOctaneGasoline = new Material.Builder(1592, "gasoline_premium")
                .fluid().color(0xFFA500).flags(FLAMMABLE, EXPLOSIVE).build();

        // free id: 1593

        CoalGas = new Material.Builder(1594, "coal_gas")
                .fluid(FluidTypes.GAS).color(0x333333).build();

        CoalTar = new Material.Builder(1595, "coal_tar")
                .fluid().color(0x1A1A1A).flags(STICKY, FLAMMABLE).build();

        Gunpowder = new Material.Builder(1596, "gunpowder")
                .dust(0)
                .color(0x808080).iconSet(ROUGH)
                .flags(FLAMMABLE, EXPLOSIVE, NO_SMELTING, NO_SMASHING)
                .build();

        Oilsands = new Material.Builder(1597, "oilsands")
                .dust(1).ore()
                .color(0x0A0A0A).iconSet(SAND)
                .flags(FLAMMABLE)
                .build();

        RareEarth = new Material.Builder(1598, "rare_earth")
                .dust(0)
                .color(0x808064).iconSet(FINE)
                .build();

        Stone = new Material.Builder(1599, "stone")
                .dust(2)
                .color(0xCDCDCD).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, GENERATE_GEAR, NO_SMASHING, NO_SMELTING)
                .build();

        Lava = new Material.Builder(1600, "lava")
                .fluid().color(0xFF4000).fluidTemp(1300).build();

        Glowstone = new Material.Builder(1601, "glowstone")
                .dust(1).fluid()
                .color(0xFFFF00).iconSet(SHINY)
                .flags(NO_SMASHING, GENERATE_PLATE, EXCLUDE_PLATE_COMPRESSOR_RECIPE, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES)
                .fluidTemp(500)
                .build();

        NetherStar = new Material.Builder(1602, "nether_star")
                .gem(4)
                .iconSet(NETHERSTAR)
                .flags(NO_SMASHING, NO_SMELTING, GENERATE_LENS)
                .build();

        Endstone = new Material.Builder(1603, "endstone")
                .dust(1)
                .color(0xD9DE9E)
                .flags(NO_SMASHING)
                .build();

        Netherrack = new Material.Builder(1604, "netherrack")
                .dust(1)
                .color(0xC80000)
                .flags(NO_SMASHING, FLAMMABLE)
                .build();

        CetaneBoostedDiesel = new Material.Builder(1605, "nitro_fuel")
                .fluid()
                .color(0xC8FF00)
                .flags(FLAMMABLE, EXPLOSIVE)
                .build();

        Collagen = new Material.Builder(1606, "collagen")
                .dust(1)
                .color(0x80471C).iconSet(ROUGH)
                .build();

        Gelatin = new Material.Builder(1607, "gelatin")
                .dust(1)
                .color(0x588BAE).iconSet(ROUGH)
                .build();

        Agar = new Material.Builder(1608, "agar")
                .dust(1)
                .color(0x4F7942).iconSet(ROUGH)
                .build();

        // FREE ID 1609

        // FREE ID 1610

        // FREE ID 1611

        // Free ID 1612

        Milk = new Material.Builder(1613, "milk")
                .fluid()
                .color(0xFEFEFE).iconSet(FINE)
                .fluidTemp(295)
                .build();

        Cocoa = new Material.Builder(1614, "cocoa")
                .dust(0)
                .color(0x643200).iconSet(FINE)
                .build();

        Wheat = new Material.Builder(1615, "wheat")
                .dust(0)
                .color(0xFFFFC4).iconSet(FINE)
                .build();

        Meat = new Material.Builder(1616, "meat")
                .dust(1)
                .color(0xC14C4C).iconSet(SAND)
                .build();

        Wood = new Material.Builder(1617, "wood")
                .dust(0, 300)
                .color(0x643200).iconSet(WOOD)
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD, FLAMMABLE, GENERATE_GEAR, GENERATE_FRAME)
                .build();

        Paper = new Material.Builder(1618, "paper")
                .dust(0)
                .color(0xFAFAFA).iconSet(PAPER)
                .flags(GENERATE_PLATE, FLAMMABLE, NO_SMELTING, NO_SMASHING,
                        MORTAR_GRINDABLE, EXCLUDE_PLATE_COMPRESSOR_RECIPE)
                .build();

        FishOil = new Material.Builder(1619, "fish_oil")
                .fluid()
                .color(0xDCC15D)
                .flags(STICKY, FLAMMABLE)
                .build();

        RubySlurry = new Material.Builder(1620, "ruby_slurry")
                .fluid().color(0xff6464).build();

        SapphireSlurry = new Material.Builder(1621, "sapphire_slurry")
                .fluid().color(0x6464c8).build();

        GreenSapphireSlurry = new Material.Builder(1622, "green_sapphire_slurry")
                .fluid().color(0x64c882).build();

        // These colors are much nicer looking than those in MC's EnumDyeColor
        DyeBlack = new Material.Builder(1623, "dye_black")
                .fluid().color(0x202020).build();

        DyeRed = new Material.Builder(1624, "dye_red")
                .fluid().color(0xFF0000).build();

        DyeGreen = new Material.Builder(1625, "dye_green")
                .fluid().color(0x00FF00).build();

        DyeBrown = new Material.Builder(1626, "dye_brown")
                .fluid().color(0x604000).build();

        DyeBlue = new Material.Builder(1627, "dye_blue")
                .fluid().color(0x0020FF).build();

        DyePurple = new Material.Builder(1628, "dye_purple")
                .fluid().color(0x800080).build();

        DyeCyan = new Material.Builder(1629, "dye_cyan")
                .fluid().color(0x00FFFF).build();

        DyeLightGray = new Material.Builder(1630, "dye_light_gray")
                .fluid().color(0xC0C0C0).build();

        DyeGray = new Material.Builder(1631, "dye_gray")
                .fluid().color(0x808080).build();

        DyePink = new Material.Builder(1632, "dye_pink")
                .fluid().color(0xFFC0C0).build();

        DyeLime = new Material.Builder(1633, "dye_lime")
                .fluid().color(0x80FF80).build();

        DyeYellow = new Material.Builder(1634, "dye_yellow")
                .fluid().color(0xFFFF00).build();

        DyeLightBlue = new Material.Builder(1635, "dye_light_blue")
                .fluid().color(0x6080FF).build();

        DyeMagenta = new Material.Builder(1636, "dye_magenta")
                .fluid().color(0xFF00FF).build();

        DyeOrange = new Material.Builder(1637, "dye_orange")
                .fluid().color(0xFF8000).build();

        DyeWhite = new Material.Builder(1638, "dye_white")
                .fluid().color(0xFFFFFF).build();

        ImpureEnrichedNaquadahSolution = new Material.Builder(1639, "impure_enriched_naquadah_solution")
                .fluid().color(0x388438).build();

        EnrichedNaquadahSolution = new Material.Builder(1640, "enriched_naquadah_solution")
                .fluid().color(0x3AAD3A).build();

        AcidicEnrichedNaquadahSolution = new Material.Builder(1641, "acidic_enriched_naquadah_solution")
                .fluid(FluidTypes.ACID).color(0x3DD63D).build();

        EnrichedNaquadahWaste = new Material.Builder(1642, "enriched_naquadah_waste")
                .fluid().color(0x355B35).build();

        ImpureNaquadriaSolution = new Material.Builder(1643, "impure_naquadria_solution")
                .fluid().color(0x518451).build();

        NaquadriaSolution = new Material.Builder(1644, "naquadria_solution")
                .fluid().color(0x61AD61).build();

        AcidicNaquadriaSolution = new Material.Builder(1645, "acidic_naquadria_solution")
                .fluid(FluidTypes.ACID).color(0x70D670).build();

        NaquadriaWaste = new Material.Builder(1646, "naquadria_waste")
                .fluid().color(0x425B42).build();

        Lapotron = new Material.Builder(1647, "lapotron")
                .gem()
                .color(0x2C39B1).iconSet(DIAMOND)
                .flags(NO_UNIFICATION)
                .build();

        TreatedWood = new Material.Builder(1648, "treated_wood")
                .dust(0, 300)
                .color(0x502800).iconSet(WOOD)
                .flags(GENERATE_PLATE, FLAMMABLE, GENERATE_ROD, GENERATE_FRAME)
                .build();

        UUMatter = new Material.Builder(1649, "uu_matter").fluid().fluidTemp(300).build();
    }
}
