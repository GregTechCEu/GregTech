package gregtech.api.unification.material.materials;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.unification.material.Material;

import net.minecraftforge.fluids.FluidRegistry;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;
import static gregtech.api.util.GTUtility.gregtechId;

public class UnknownCompositionMaterials {

    public static void register() {
        WoodGas = Material.builder(1500, gregtechId("wood_gas"))
                .gas().color(0xDECD87).build();

        WoodVinegar = Material.builder(1501, gregtechId("wood_vinegar"))
                .fluid().color(0xD45500).build();

        WoodTar = Material.builder(1502, gregtechId("wood_tar"))
                .fluid().color(0x28170B)
                .flags(STICKY, FLAMMABLE).build();

        CharcoalByproducts = Material.builder(1503, gregtechId("charcoal_byproducts"))
                .fluid().color(0x784421).build();

        Biomass = Material.builder(1504, gregtechId("biomass"))
                .liquid(new FluidBuilder().customStill())
                .color(0x14CC04).build();

        BioDiesel = Material.builder(1505, gregtechId("bio_diesel"))
                .fluid().color(0xFF8000)
                .flags(FLAMMABLE, EXPLOSIVE).build();

        FermentedBiomass = Material.builder(1506, gregtechId("fermented_biomass"))
                .liquid(new FluidBuilder().temperature(300))
                .color(0x445500)
                .build();

        Creosote = Material.builder(1507, gregtechId("creosote"))
                .liquid(new FluidBuilder().customStill())
                .color(0x804000)
                .flags(STICKY).build();

        Diesel = Material.builder(1508, gregtechId("diesel"))
                .liquid(new FluidBuilder().customStill().alternativeName("fuel"))
                .color(0xFCF404)
                .flags(FLAMMABLE, EXPLOSIVE).build();

        RocketFuel = Material.builder(1509, gregtechId("rocket_fuel"))
                .fluid().flags(FLAMMABLE, EXPLOSIVE).color(0xBDB78C).build();

        Glue = Material.builder(1510, gregtechId("glue"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFCCC8C).flags(STICKY).build();

        Lubricant = Material.builder(1511, gregtechId("lubricant"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFEEDAF).build();

        McGuffium239 = Material.builder(1512, gregtechId("mc_guffium_239"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFC04FC).build();

        IndiumConcentrate = Material.builder(1513, gregtechId("indium_concentrate"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x0E2950).build();

        SeedOil = Material.builder(1514, gregtechId("seed_oil"))
                .liquid(new FluidBuilder().customStill().alternativeName("seed.oil"))
                .color(0xE4FC8C)
                .flags(STICKY, FLAMMABLE).build();

        DrillingFluid = Material.builder(1515, gregtechId("drilling_fluid"))
                .fluid().color(0xFFFFAA).build();

        ConstructionFoam = Material.builder(1516, gregtechId("construction_foam"))
                .fluid().color(0x808080).build();

        // Free IDs 1517-1521

        SulfuricHeavyFuel = Material.builder(1522, gregtechId("sulfuric_heavy_fuel"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFCEC94)
                .flags(FLAMMABLE).build();

        HeavyFuel = Material.builder(1523, gregtechId("heavy_fuel"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFCECAC)
                .flags(FLAMMABLE).build();

        LightlyHydroCrackedHeavyFuel = Material.builder(1524, gregtechId("lightly_hydrocracked_heavy_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xD4C494)
                .flags(FLAMMABLE)
                .build();

        SeverelyHydroCrackedHeavyFuel = Material.builder(1525, gregtechId("severely_hydrocracked_heavy_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBCAC84)
                .flags(FLAMMABLE)
                .build();

        LightlySteamCrackedHeavyFuel = Material.builder(1526, gregtechId("lightly_steamcracked_heavy_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFCDC7C)
                .flags(FLAMMABLE)
                .build();

        SeverelySteamCrackedHeavyFuel = Material.builder(1527, gregtechId("severely_steamcracked_heavy_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFCFCD4)
                .flags(FLAMMABLE)
                .build();

        SulfuricLightFuel = Material.builder(1528, gregtechId("sulfuric_light_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFCCC04)
                .flags(FLAMMABLE).build();

        LightFuel = Material.builder(1529, gregtechId("light_fuel"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFCF404)
                .flags(FLAMMABLE).build();

        LightlyHydroCrackedLightFuel = Material.builder(1530, gregtechId("lightly_hydrocracked_light_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xA49C04)
                .flags(FLAMMABLE)
                .build();

        SeverelyHydroCrackedLightFuel = Material.builder(1531, gregtechId("severely_hydrocracked_light_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0x847C04)
                .flags(FLAMMABLE)
                .build();

        LightlySteamCrackedLightFuel = Material.builder(1532, gregtechId("lightly_steamcracked_light_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFCFC0C)
                .flags(FLAMMABLE)
                .build();

        SeverelySteamCrackedLightFuel = Material.builder(1533, gregtechId("severely_steamcracked_light_fuel"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xFCFC2C)
                .flags(FLAMMABLE)
                .build();

        SulfuricNaphtha = Material.builder(1534, gregtechId("sulfuric_naphtha"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFCF404)
                .flags(FLAMMABLE)
                .build();

        Naphtha = Material.builder(1535, gregtechId("naphtha"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFFF404)
                .flags(FLAMMABLE)
                .build();

        LightlyHydroCrackedNaphtha = Material.builder(1536, gregtechId("lightly_hydrocracked_naphtha"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xD4C404)
                .flags(FLAMMABLE)
                .build();

        SeverelyHydroCrackedNaphtha = Material.builder(1537, gregtechId("severely_hydrocracked_naphtha"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xDCD40C)
                .flags(FLAMMABLE)
                .build();

        LightlySteamCrackedNaphtha = Material.builder(1538, gregtechId("lightly_steamcracked_naphtha"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xBFB608)
                .flags(FLAMMABLE)
                .build();

        SeverelySteamCrackedNaphtha = Material.builder(1539, gregtechId("severely_steamcracked_naphtha"))
                .liquid(new FluidBuilder()
                        .temperature(775)
                        .customStill())
                .color(0xCCC434)
                .flags(FLAMMABLE)
                .build();

        SulfuricGas = Material.builder(1540, gregtechId("sulfuric_gas"))
                .gas(new FluidBuilder().customStill())
                .color(0xECDCCC).build();

        RefineryGas = Material.builder(1541, gregtechId("refinery_gas"))
                .gas(new FluidBuilder().customStill())
                .color(0xB4B4B4)
                .flags(FLAMMABLE).build();

        LightlyHydroCrackedGas = Material.builder(1542, gregtechId("lightly_hydrocracked_gas"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xA0A0A0)
                .flags(FLAMMABLE)
                .build();

        SeverelyHydroCrackedGas = Material.builder(1543, gregtechId("severely_hydrocracked_gas"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x919191)
                .flags(FLAMMABLE)
                .build();

        LightlySteamCrackedGas = Material.builder(1544, gregtechId("lightly_steamcracked_gas"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xC8C8C8)
                .flags(FLAMMABLE)
                .build();

        SeverelySteamCrackedGas = Material.builder(1545, gregtechId("severely_steamcracked_gas"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xE0E0E0)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedEthane = Material.builder(1546, gregtechId("hydrocracked_ethane"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x9696BC)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedEthylene = Material.builder(1547, gregtechId("hydrocracked_ethylene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xA3A3A0)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedPropene = Material.builder(1548, gregtechId("hydrocracked_propene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedPropane = Material.builder(1549, gregtechId("hydrocracked_propane"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedButane = Material.builder(1550, gregtechId("hydrocracked_butane"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x852C18)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedButene = Material.builder(1551, gregtechId("hydrocracked_butene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x993E05)
                .flags(FLAMMABLE)
                .build();

        HydroCrackedButadiene = Material.builder(1552, gregtechId("hydrocracked_butadiene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xAD5203)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedEthane = Material.builder(1553, gregtechId("steamcracked_ethane"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x9696BC)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedEthylene = Material.builder(1554, gregtechId("steamcracked_ethylene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xA3A3A0)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedPropene = Material.builder(1555, gregtechId("steamcracked_propene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedPropane = Material.builder(1556, gregtechId("steamcracked_propane"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xBEA540)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedButane = Material.builder(1557, gregtechId("steamcracked_butane"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x852C18)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedButene = Material.builder(1558, gregtechId("steamcracked_butene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0x993E05)
                .flags(FLAMMABLE)
                .build();

        SteamCrackedButadiene = Material.builder(1559, gregtechId("steamcracked_butadiene"))
                .gas(new FluidBuilder().temperature(775))
                .color(0xAD5203)
                .flags(FLAMMABLE)
                .build();

        // Free IDs 1560-1575

        LPG = Material.builder(1576, gregtechId("lpg"))
                .liquid(new FluidBuilder().customStill())
                .color(0xFCFCAC)
                .flags(FLAMMABLE, EXPLOSIVE).build();

        RawGrowthMedium = Material.builder(1577, gregtechId("raw_growth_medium"))
                .fluid().color(0xA47351).build();

        SterileGrowthMedium = Material.builder(1578, gregtechId("sterilized_growth_medium"))
                .fluid().color(0xAC876E).build();

        Oil = Material.builder(1579, gregtechId("oil"))
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .build();

        OilHeavy = Material.builder(1580, gregtechId("oil_heavy"))
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .build();

        RawOil = Material.builder(1581, gregtechId("oil_medium"))
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .build();

        OilLight = Material.builder(1582, gregtechId("oil_light"))
                .liquid(new FluidBuilder().block().customStill())
                .color(0x0A0A0A)
                .flags(STICKY, FLAMMABLE)
                .build();

        NaturalGas = Material.builder(1583, gregtechId("natural_gas"))
                .gas(new FluidBuilder().block().customStill())
                .color(0xDCDCDC)
                .flags(FLAMMABLE, EXPLOSIVE).build();

        Bacteria = Material.builder(1584, gregtechId("bacteria"))
                .fluid().color(0x808000).build();

        BacterialSludge = Material.builder(1585, gregtechId("bacterial_sludge"))
                .fluid().color(0x355E3B).build();

        EnrichedBacterialSludge = Material.builder(1586, gregtechId("enriched_bacterial_sludge"))
                .fluid().color(0x7FFF00).build();

        // free id: 1587

        Mutagen = Material.builder(1588, gregtechId("mutagen"))
                .fluid().color(0x00FF7F).build();

        GelatinMixture = Material.builder(1589, gregtechId("gelatin_mixture"))
                .fluid().color(0x588BAE).build();

        RawGasoline = Material.builder(1590, gregtechId("raw_gasoline"))
                .fluid().color(0xFF6400).flags(FLAMMABLE).build();

        Gasoline = Material.builder(1591, gregtechId("gasoline"))
                .fluid().color(0xFAA500).flags(FLAMMABLE, EXPLOSIVE).build();

        HighOctaneGasoline = Material.builder(1592, gregtechId("gasoline_premium"))
                .fluid().color(0xFFA500).flags(FLAMMABLE, EXPLOSIVE).build();

        // free id: 1593

        CoalGas = Material.builder(1594, gregtechId("coal_gas"))
                .gas().color(0x333333).build();

        CoalTar = Material.builder(1595, gregtechId("coal_tar"))
                .fluid().color(0x1A1A1A).flags(STICKY, FLAMMABLE).build();

        Gunpowder = Material.builder(1596, gregtechId("gunpowder"))
                .dust(0)
                .color(0x808080).iconSet(ROUGH)
                .flags(FLAMMABLE, EXPLOSIVE, NO_SMELTING, NO_SMASHING)
                .build();

        Oilsands = Material.builder(1597, gregtechId("oilsands"))
                .dust(1).ore()
                .color(0x0A0A0A).iconSet(SAND)
                .flags(FLAMMABLE)
                .build();

        RareEarth = Material.builder(1598, gregtechId("rare_earth"))
                .dust(0)
                .color(0x808064).iconSet(FINE)
                .build();

        Stone = Material.builder(1599, gregtechId("stone"))
                .dust(2)
                .color(0xCDCDCD).iconSet(ROUGH)
                .flags(MORTAR_GRINDABLE, GENERATE_GEAR, NO_SMASHING, NO_SMELTING)
                .build();

        Lava = Material.builder(1600, gregtechId("lava"))
                .fluid(FluidRegistry.LAVA, FluidStorageKeys.LIQUID, FluidState.LIQUID)
                .color(0xFF4000)
                .flags(GLOWING)
                .build();

        Glowstone = Material.builder(1601, gregtechId("glowstone"))
                .dust(1)
                .liquid(new FluidBuilder().temperature(500))
                .color(0xFFFF00).iconSet(SHINY)
                .flags(NO_SMASHING, GENERATE_PLATE, EXCLUDE_PLATE_COMPRESSOR_RECIPE,
                        EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, GLOWING)
                .build();

        NetherStar = Material.builder(1602, gregtechId("nether_star"))
                .gem(4)
                .iconSet(NETHERSTAR)
                .flags(NO_SMASHING, NO_SMELTING, GENERATE_LENS)
                .build();

        Endstone = Material.builder(1603, gregtechId("endstone"))
                .dust(1)
                .color(0xD9DE9E)
                .flags(NO_SMASHING)
                .build();

        Netherrack = Material.builder(1604, gregtechId("netherrack"))
                .dust(1)
                .color(0xC80000)
                .flags(NO_SMASHING, FLAMMABLE)
                .build();

        CetaneBoostedDiesel = Material.builder(1605, gregtechId("nitro_fuel"))
                .liquid(new FluidBuilder().customStill())
                .color(0xC8FF00)
                .flags(FLAMMABLE, EXPLOSIVE)
                .build();

        Collagen = Material.builder(1606, gregtechId("collagen"))
                .dust(1)
                .color(0x80471C).iconSet(ROUGH)
                .build();

        Gelatin = Material.builder(1607, gregtechId("gelatin"))
                .dust(1)
                .color(0x588BAE).iconSet(ROUGH)
                .build();

        Agar = Material.builder(1608, gregtechId("agar"))
                .dust(1)
                .color(0x4F7942).iconSet(ROUGH)
                .build();

        // FREE ID 1609

        // FREE ID 1610

        // FREE ID 1611

        // Free ID 1612

        Milk = Material.builder(1613, gregtechId("milk"))
                .liquid(new FluidBuilder()
                        .temperature(295)
                        .customStill())
                .color(0xFEFEFE).iconSet(FINE)
                .build();

        Cocoa = Material.builder(1614, gregtechId("cocoa"))
                .dust(0)
                .color(0x643200).iconSet(FINE)
                .build();

        Wheat = Material.builder(1615, gregtechId("wheat"))
                .dust(0)
                .color(0xFFFFC4).iconSet(FINE)
                .build();

        Meat = Material.builder(1616, gregtechId("meat"))
                .dust(1)
                .color(0xC14C4C).iconSet(SAND)
                .build();

        Wood = Material.builder(1617, gregtechId("wood"))
                .wood()
                .color(0x896727).iconSet(WOOD)
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_BOLT_SCREW, GENERATE_LONG_ROD, GENERATE_GEAR,
                        GENERATE_FRAME)
                .fluidPipeProperties(340, 5, false)
                .build();

        Paper = Material.builder(1618, gregtechId("paper"))
                .dust(0)
                .color(0xFAFAFA).iconSet(FINE)
                .flags(GENERATE_PLATE, FLAMMABLE, NO_SMELTING, NO_SMASHING,
                        MORTAR_GRINDABLE, EXCLUDE_PLATE_COMPRESSOR_RECIPE)
                .build();

        FishOil = Material.builder(1619, gregtechId("fish_oil"))
                .fluid()
                .color(0xDCC15D)
                .flags(STICKY, FLAMMABLE)
                .build();

        RubySlurry = Material.builder(1620, gregtechId("ruby_slurry"))
                .fluid().color(0xff6464).build();

        SapphireSlurry = Material.builder(1621, gregtechId("sapphire_slurry"))
                .fluid().color(0x6464c8).build();

        GreenSapphireSlurry = Material.builder(1622, gregtechId("green_sapphire_slurry"))
                .fluid().color(0x64c882).build();

        // These colors are much nicer looking than those in MC's EnumDyeColor
        DyeBlack = Material.builder(1623, gregtechId("dye_black"))
                .fluid().color(0x202020).build();

        DyeRed = Material.builder(1624, gregtechId("dye_red"))
                .fluid().color(0xFF0000).build();

        DyeGreen = Material.builder(1625, gregtechId("dye_green"))
                .fluid().color(0x00FF00).build();

        DyeBrown = Material.builder(1626, gregtechId("dye_brown"))
                .fluid().color(0x604000).build();

        DyeBlue = Material.builder(1627, gregtechId("dye_blue"))
                .fluid().color(0x0020FF).build();

        DyePurple = Material.builder(1628, gregtechId("dye_purple"))
                .fluid().color(0x800080).build();

        DyeCyan = Material.builder(1629, gregtechId("dye_cyan"))
                .fluid().color(0x00FFFF).build();

        DyeLightGray = Material.builder(1630, gregtechId("dye_light_gray"))
                .fluid().color(0xC0C0C0).build();

        DyeGray = Material.builder(1631, gregtechId("dye_gray"))
                .fluid().color(0x808080).build();

        DyePink = Material.builder(1632, gregtechId("dye_pink"))
                .fluid().color(0xFFC0C0).build();

        DyeLime = Material.builder(1633, gregtechId("dye_lime"))
                .fluid().color(0x80FF80).build();

        DyeYellow = Material.builder(1634, gregtechId("dye_yellow"))
                .fluid().color(0xFFFF00).build();

        DyeLightBlue = Material.builder(1635, gregtechId("dye_light_blue"))
                .fluid().color(0x6080FF).build();

        DyeMagenta = Material.builder(1636, gregtechId("dye_magenta"))
                .fluid().color(0xFF00FF).build();

        DyeOrange = Material.builder(1637, gregtechId("dye_orange"))
                .fluid().color(0xFF8000).build();

        DyeWhite = Material.builder(1638, gregtechId("dye_white"))
                .fluid().color(0xFFFFFF).build();

        ImpureEnrichedNaquadahSolution = Material.builder(1639, gregtechId("impure_enriched_naquadah_solution"))
                .fluid().color(0x388438).build();

        EnrichedNaquadahSolution = Material.builder(1640, gregtechId("enriched_naquadah_solution"))
                .fluid().color(0x3AAD3A).build();

        AcidicEnrichedNaquadahSolution = Material.builder(1641, gregtechId("acidic_enriched_naquadah_solution"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x3DD63D).build();

        EnrichedNaquadahWaste = Material.builder(1642, gregtechId("enriched_naquadah_waste"))
                .fluid().color(0x355B35).build();

        ImpureNaquadriaSolution = Material.builder(1643, gregtechId("impure_naquadria_solution"))
                .fluid().color(0x518451).build();

        NaquadriaSolution = Material.builder(1644, gregtechId("naquadria_solution"))
                .fluid().color(0x61AD61).build();

        AcidicNaquadriaSolution = Material.builder(1645, gregtechId("acidic_naquadria_solution"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .color(0x70D670).build();

        NaquadriaWaste = Material.builder(1646, gregtechId("naquadria_waste"))
                .fluid().color(0x425B42).build();

        Lapotron = Material.builder(1647, gregtechId("lapotron"))
                .gem()
                .color(0x2C39B1).iconSet(DIAMOND)
                .flags(NO_UNIFICATION)
                .build();

        TreatedWood = Material.builder(1648, gregtechId("treated_wood"))
                .wood()
                .color(0x674A28).iconSet(WOOD)
                .flags(GENERATE_PLATE, GENERATE_ROD, GENERATE_FRAME)
                .fluidPipeProperties(340, 10, false)
                .build();

        UUMatter = Material.builder(1649, gregtechId("uu_matter"))
                .liquid(new FluidBuilder()
                        .temperature(300)
                        .customStill())
                .color(0x36042C)
                .build();

        PCBCoolant = Material.builder(1650, gregtechId("pcb_coolant"))
                .fluid().color(0xD5D69C).build();

        BauxiteSlurry = Material.builder(1651, gregtechId("bauxite_slurry"))
                .fluid().color(0x051650).build();

        CrackedBauxiteSlurry = Material.builder(1652, gregtechId("cracked_bauxite_slurry"))
                .liquid(new FluidBuilder().temperature(775)).color(0x052C50).build();

        BauxiteSludge = Material.builder(1653, gregtechId("bauxite_sludge"))
                .fluid().color(0x563D2D).build();

        DecalcifiedBauxiteSludge = Material.builder(1654, gregtechId("decalcified_bauxite_sludge"))
                .fluid().color(0x6F2DA8).build();

        BauxiteSlag = Material.builder(1655, gregtechId("bauxite_slag"))
                .dust(1)
                .color(0x0C0550).iconSet(SAND)
                .build();
    }
}
