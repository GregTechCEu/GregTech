package gregtech.api.unification.material.materials;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PhysicalProperties;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;
import static gregtech.api.util.GTUtility.gregtechId;

public class OrganicChemistryMaterials {

    /**
     * ID RANGE: 1000-1068 (incl.)
     */
    public static void register() {
        SiliconeRubber = new Material.Builder(1000, gregtechId("silicone_rubber"))
                .polymer()
                .liquid(new FluidBuilder().temperature(900))
                .color(0xDCDCDC)
                .flags(GENERATE_GEAR, GENERATE_RING, GENERATE_FOIL)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .build()
                .setFormula("Si(CH3)2O", true);

        Nitrobenzene = new Material.Builder(1001, gregtechId("nitrobenzene"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().bp(484).mp(279))
                .color(0x704936)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 5, Nitrogen, 1, Oxygen, 2)
                .build();

        RawRubber = new Material.Builder(1002, gregtechId("raw_rubber"))
                .polymer()
                .color(0xCCC789)
                .components(Carbon, 5, Hydrogen, 8)
                .build();

        RawStyreneButadieneRubber = new Material.Builder(1003, gregtechId("raw_styrene_butadiene_rubber"))
                .dust()
                .color(0x54403D).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .components(Carbon, 20, Hydrogen, 26)
                .build()
                .setFormula("(C4H6)3C8H8", true);

        StyreneButadieneRubber = new Material.Builder(1004, gregtechId("styrene_butadiene_rubber"))
                .polymer()
                .liquid(new FluidBuilder().temperature(1000))
                .color(0x211A18).iconSet(SHINY)
                .flags(GENERATE_FOIL, GENERATE_RING)
                .components(Carbon, 20, Hydrogen, 26)
                .build()
                .setFormula("(C4H6)3C8H8", true);

        PolyvinylAcetate = new Material.Builder(1005, gregtechId("polyvinyl_acetate"))
                .fluid()
                .color(0xFF9955)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .build();

        ReinforcedEpoxyResin = new Material.Builder(1006, gregtechId("reinforced_epoxy_resin"))
                .polymer()
                .liquid(new FluidBuilder().temperature(600))
                .color(0xA07A10)
                .flags(STD_METAL)
                .components(Carbon, 6, Hydrogen, 4, Oxygen, 1)
                .build();

        PolyvinylChloride = new Material.Builder(1007, gregtechId("polyvinyl_chloride"))
                .polymer()
                .liquid(new FluidBuilder().temperature(373))
                .color(0xD7E6E6)
                .flags(EXT_METAL, GENERATE_FOIL)
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .itemPipeProperties(512, 4)
                .build();

        PolyphenyleneSulfide = new Material.Builder(1008, gregtechId("polyphenylene_sulfide"))
                .polymer()
                .liquid(new FluidBuilder().temperature(500))
                .color(0xAA8800)
                .flags(EXT_METAL, GENERATE_FOIL)
                .components(Carbon, 6, Hydrogen, 4, Sulfur, 1)
                .build();

        GlycerylTrinitrate = new Material.Builder(1009, gregtechId("glyceryl_trinitrate"))
                .liquid(new FluidBuilder().customStill())
                .color(0x04443C)
                .flags(FLAMMABLE, EXPLOSIVE)
                .components(Carbon, 3, Hydrogen, 5, Nitrogen, 3, Oxygen, 9)
                .build();

        Polybenzimidazole = new Material.Builder(1010, gregtechId("polybenzimidazole"))
                .polymer()
                .liquid(new FluidBuilder().temperature(1450))
                .color(0x2D2D2D)
                .flags(GENERATE_FOIL)
                .components(Carbon, 20, Hydrogen, 12, Nitrogen, 4)
                .fluidPipeProperties(1000, 350, true)
                .build();

        Polydimethylsiloxane = new Material.Builder(1011, gregtechId("polydimethylsiloxane"))
                .dust()
                .color(0xF5F5F5)
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .build()
                .setFormula("Si(CH3)2O", true);

        Polyethylene = new Material.Builder(1012, gregtechId("plastic")) // todo add polyethylene oredicts
                .polymer(1)
                .liquid(new FluidBuilder().temperature(408))
                .color(0xC8C8C8)
                .flags(GENERATE_FOIL)
                .components(Carbon, 2, Hydrogen, 4)
                .fluidPipeProperties(370, 60, true)
                .build();

        Epoxy = new Material.Builder(1013, gregtechId("epoxy"))
                .polymer(1)
                .liquid(new FluidBuilder().temperature(400))
                .color(0xC88C14)
                .flags(STD_METAL)
                .components(Carbon, 21, Hydrogen, 25, Chlorine, 1, Oxygen, 5)
                .build();

        // Free ID 1014

        Polycaprolactam = new Material.Builder(1015, gregtechId("polycaprolactam"))
                .polymer(1)
                .liquid(new FluidBuilder().temperature(493))
                .color(0x323232)
                .flags(STD_METAL, GENERATE_FOIL)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .build();

        Polytetrafluoroethylene = new Material.Builder(1016, gregtechId("polytetrafluoroethylene"))
                .polymer(1)
                .liquid(new FluidBuilder().temperature(600))
                .color(0x646464)
                .flags(STD_METAL, GENERATE_FRAME, GENERATE_FOIL)
                .components(Carbon, 2, Fluorine, 4)
                .fluidPipeProperties(600, 100, true, true, false, false, true, true)
                .build();

        Sugar = new Material.Builder(1017, gregtechId("sugar"))
                .gem(1)
                .color(0xFAFAFA).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 12, Oxygen, 6)
                .build();

        Methane = new Material.Builder(1018, gregtechId("methane"))
                .gas(new FluidBuilder().translation("gregtech.fluid.gas_generic"))
                .physicalProperties(new PhysicalProperties.Builder().mp(91).bp(112).flameTemperature(2173)
                        .autoIgnitionTemperature(810))
                .color(0xFF0078)
                .components(Carbon, 1, Hydrogen, 4)
                .build();

        Epichlorohydrin = new Material.Builder(1019, gregtechId("epichlorohydrin"))
                .liquid(new FluidBuilder().customStill())
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().mp(247).bp(391))
                .color(0x640C04)
                .components(Carbon, 3, Hydrogen, 5, Chlorine, 1, Oxygen, 1)
                .build();

        Monochloramine = new Material.Builder(1020, gregtechId("monochloramine"))
                .fluid()
                .physicalProperties(new PhysicalProperties.Builder().mp(207))
                .color(0x3F9F80)
                .components(Nitrogen, 1, Hydrogen, 2, Chlorine, 1)
                .build();

        Chloroform = new Material.Builder(1021, gregtechId("chloroform"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().mp(210).bp(334))
                .color(0x892CA0)
                .components(Carbon, 1, Hydrogen, 1, Chlorine, 3)
                .build();

        Cumene = new Material.Builder(1022, gregtechId("cumene"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().mp(177).bp(425).autoIgnitionTemperature(697))
                .color(0x552200)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 9, Hydrogen, 12)
                .build();

        Tetrafluoroethylene = new Material.Builder(1023, gregtechId("tetrafluoroethylene"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().bp(197).mp(131))
                .color(0x7D7D7D)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Fluorine, 4)
                .build();

        Chloromethane = new Material.Builder(1024, gregtechId("chloromethane"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().autoIgnitionTemperature(898).mp(176).bp(249))
                .color(0xC82CA0)
                .components(Carbon, 1, Hydrogen, 3, Chlorine, 1)
                .build();

        AllylChloride = new Material.Builder(1025, gregtechId("allyl_chloride"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().mp(138).bp(318).autoIgnitionTemperature(663))
                .color(0x87DEAA)
                .components(Carbon, 2, Methane, 1, HydrochloricAcid, 1)
                .build()
                .setFormula("C3H5Cl", true);

        Isoprene = new Material.Builder(1026, gregtechId("isoprene"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().bp(307).mp(129))
                .color(0x141414)
                .components(Carbon, 5, Hydrogen, 8)
                .build();

        Propane = new Material.Builder(1027, gregtechId("propane"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().mp(86).bp(231).autoIgnitionTemperature(743)
                        .flameTemperature(2243))
                .color(0xFAE250)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 8)
                .build();

        Propene = new Material.Builder(1028, gregtechId("propene"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().bp(223).mp(88).flameTemperature(2223)
                        .autoIgnitionTemperature(728))
                .color(0xFFDD55)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 6)
                .build();

        Ethane = new Material.Builder(1029, gregtechId("ethane"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().mp(90).bp(185).autoIgnitionTemperature(745)
                        .flameTemperature(2235))
                .color(0xC8C8FF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6)
                .build();

        Butene = new Material.Builder(1030, gregtechId("butene"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().flameTemperature(2200).autoIgnitionTemperature(658)
                        .bp(267).mp(88))
                .color(0xCF5005)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 8)
                .build();

        Butane = new Material.Builder(1031, gregtechId("butane"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().mp(136).bp(273).autoIgnitionTemperature(678)
                        .flameTemperature(2200))
                .color(0xB6371E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 10)
                .build();

        DissolvedCalciumAcetate = new Material.Builder(1032, gregtechId("dissolved_calcium_acetate"))
                .fluid()
                .color(0xDCC8B4)
                .flags(DISABLE_DECOMPOSITION)
                .components(Calcium, 1, Carbon, 4, Oxygen, 4, Hydrogen, 6, Water, 1)
                .build();

        VinylAcetate = new Material.Builder(1033, gregtechId("vinyl_acetate"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().bp(346).mp(180).autoIgnitionTemperature(700)
                        .flameTemperature(2200))
                .color(0xE1B380)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .build();

        MethylAcetate = new Material.Builder(1034, gregtechId("methyl_acetate"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().bp(330).mp(175))
                .color(0xEEC6AF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 6, Oxygen, 2)
                .build();

        Ethenone = new Material.Builder(1035, gregtechId("ethenone"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().mp(123).bp(217))
                .color(0x141446)
                .components(Carbon, 2, Hydrogen, 2, Oxygen, 1)
                .build();

        Tetranitromethane = new Material.Builder(1036, gregtechId("tetranitromethane"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().bp(399).mp(287).flameTemperature(3780)
                        .autoIgnitionTemperature(533))
                .color(0x0F2828)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1, Nitrogen, 4, Oxygen, 8)
                .build();

        Dimethylamine = new Material.Builder(1037, gregtechId("dimethylamine"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().autoIgnitionTemperature(674).mp(180).bp(282)
                        .flameTemperature(2500))
                .color(0x554469)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 7, Nitrogen, 1)
                .build();

        Dimethylhydrazine = new Material.Builder(1038, gregtechId("dimethylhydrazine"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().bp(337).mp(216).autoIgnitionTemperature(521)
                        .flameTemperature(2700))
                .color(0x000055)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 8, Nitrogen, 2)
                .build();

        DinitrogenTetroxide = new Material.Builder(1039, gregtechId("dinitrogen_tetroxide"))
                .liquid(new FluidBuilder().attributes(FluidAttributes.OXIDANT))
                .physicalProperties(new PhysicalProperties.Builder().mp(262).bp(295))
                .color(0x570C02)
                .components(Nitrogen, 2, Oxygen, 4)
                .build();

        Dimethyldichlorosilane = new Material.Builder(1040, gregtechId("dimethyldichlorosilane"))
                .liquid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().mp(197).bp(343).autoIgnitionTemperature(690)
                        .flameTemperature(2280))
                .color(0x441650)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Chlorine, 2, Silicon, 1)
                .build()
                .setFormula("Si(CH3)2Cl2", true);

        Styrene = new Material.Builder(1041, gregtechId("styrene"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().bp(418).mp(243))
                .color(0xD2C8BE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 8)
                .build();

        Butadiene = new Material.Builder(1042, gregtechId("butadiene"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().mp(164).bp(269).autoIgnitionTemperature(687)
                        .flameTemperature(2223))
                .color(0xB55A10)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6)
                .build();

        Dichlorobenzene = new Material.Builder(1043, gregtechId("dichlorobenzene"))
                .fluid()
                .color(0x004455)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 4, Chlorine, 2)
                .build();

        AceticAcid = new Material.Builder(1044, gregtechId("acetic_acid"))
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID))
                .physicalProperties(new PhysicalProperties.Builder().mp(290).bp(391).autoIgnitionTemperature(700)
                        .flameTemperature(2123))
                .color(0xC8B4A0)
                .components(Carbon, 2, Hydrogen, 4, Oxygen, 2)
                .build();

        Phenol = new Material.Builder(1045, gregtechId("phenol"))
                .dust()
                .fluid()
                .physicalProperties(new PhysicalProperties.Builder().bp(455).mp(314).hygroscopic())
                .color(0x784421)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 6, Oxygen, 1)
                .build();

        BisphenolA = new Material.Builder(1046, gregtechId("bisphenol_a"))
                .fluid()
                .physicalProperties(new PhysicalProperties.Builder().mp(428).bp(525, 1722))
                .color(0xD4AA00)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 15, Hydrogen, 16, Oxygen, 2)
                .build();

        VinylChloride = new Material.Builder(1047, gregtechId("vinyl_chloride"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().bp(260).mp(119).autoIgnitionTemperature(745)
                        .flameTemperature(2180))
                .color(0xE1F0F0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .build();

        Ethylene = new Material.Builder(1048, gregtechId("ethylene"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().mp(104).bp(170).autoIgnitionTemperature(816)
                        .flameTemperature(2250))
                .color(0xE1E1E1)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 4)
                .build();

        Benzene = new Material.Builder(1049, gregtechId("benzene"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().autoIgnitionTemperature(771).bp(353).mp(279)
                        .flameTemperature(2250))
                .color(0x1A1A1A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 6)
                .build();

        Acetone = new Material.Builder(1050, gregtechId("acetone"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().autoIgnitionTemperature(738).bp(329).mp(178)
                        .flameTemperature(2250))
                .color(0xAFAFAF)
                .components(Carbon, 3, Hydrogen, 6, Oxygen, 1)
                .build();

        Glycerol = new Material.Builder(1051, gregtechId("glycerol"))
                .fluid()
                .physicalProperties(new PhysicalProperties.Builder().bp(563).mp(291))
                .color(0x87DE87)
                .components(Carbon, 3, Hydrogen, 8, Oxygen, 3)
                .build();

        Methanol = new Material.Builder(1052, gregtechId("methanol"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().autoIgnitionTemperature(658).bp(338).mp(176)
                        .flameTemperature(2150))
                .color(0xAA8800)
                .components(Carbon, 1, Hydrogen, 4, Oxygen, 1)
                .build();

        // FREE ID 1053

        Ethanol = new Material.Builder(1054, gregtechId("ethanol"))
                .liquid(new FluidBuilder().customStill().alternativeName("bio.ethanol"))
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().bp(351).mp(159).autoIgnitionTemperature(642)
                        .flameTemperature(2250))
                .color(0xFC4C04)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1)
                .build();

        Toluene = new Material.Builder(1055, gregtechId("toluene"))
                .liquid(new FluidBuilder().customStill())
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().autoIgnitionTemperature(753).bp(384).mp(178)
                        .flameTemperature(2300))
                .color(0x712400)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 7, Hydrogen, 8)
                .build();

        DiphenylIsophtalate = new Material.Builder(1056, gregtechId("diphenyl_isophthalate"))
                .liquid(new FluidBuilder().temperature(410))
                .dust()
                .color(0x246E57)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 20, Hydrogen, 14, Oxygen, 4)
                .build();

        PhthalicAcid = new Material.Builder(1057, gregtechId("phthalic_acid"))
                .dust()
                .liquid(new FluidBuilder().attribute(FluidAttributes.ACID).temperature(480))
                .physicalProperties(new PhysicalProperties.Builder().mp(480))
                .color(0xD1D1D1)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 6, Oxygen, 4)
                .build()
                .setFormula("C6H4(CO2H)2", true);

        Dimethylbenzene = new Material.Builder(1058, gregtechId("dimethylbenzene"))
                .fluid()
                .color(0x669C40)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 10)
                .build()
                .setFormula("C6H4(CH3)2", true);

        Diaminobenzidine = new Material.Builder(1059, gregtechId("diaminobenzidine"))
                .dust()
                .color(0x337D59)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 14, Nitrogen, 4)
                .build()
                .setFormula("(C6H3(NH2)2)2", true);

        Dichlorobenzidine = new Material.Builder(1060, gregtechId("dichlorobenzidine"))
                .dust()
                .color(0xA1DEA6)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 10, Chlorine, 2, Nitrogen, 2)
                .build()
                .setFormula("(C6H3Cl(NH2))2", true);

        Nitrochlorobenzene = new Material.Builder(1061, gregtechId("nitrochlorobenzene"))
                .fluid()
                .color(0x8FB51A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 4, Chlorine, 1, Nitrogen, 1, Oxygen, 2)
                .build();

        Chlorobenzene = new Material.Builder(1062, gregtechId("chlorobenzene"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().mp(228).bp(405).autoIgnitionTemperature(813)
                        .flameTemperature(2220))
                .color(0x326A3E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 5, Chlorine, 1)
                .build();

        Octane = new Material.Builder(1063, gregtechId("octane"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().flameTemperature(2420).autoIgnitionTemperature(493)
                        .bp(398).mp(216))
                .flags(DISABLE_DECOMPOSITION)
                .color(0x8A0A09)
                .components(Carbon, 8, Hydrogen, 18)
                .build();

        EthylTertButylEther = new Material.Builder(1064, gregtechId("ethyl_tertbutyl_ether"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().mp(179).bp(344).autoIgnitionTemperature(648)
                        .flameTemperature(2220))
                .flags(DISABLE_DECOMPOSITION)
                .color(0xB15C06)
                .components(Carbon, 6, Hydrogen, 14, Oxygen, 1)
                .build();

        Ethylbenzene = new Material.Builder(1066, gregtechId("ethylbenzene"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().autoIgnitionTemperature(703).bp(409).mp(178)
                        .flameTemperature(2220))
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 10)
                .build();

        Naphthalene = new Material.Builder(1067, gregtechId("naphthalene"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().autoIgnitionTemperature(798).mp(351).bp(491)
                        .flameTemperature(2220))
                .flags(DISABLE_DECOMPOSITION)
                .color(0xF4F4D7)
                .components(Carbon, 10, Hydrogen, 8)
                .build();

        Rubber = new Material.Builder(1068, gregtechId("rubber"))
                .polymer(0)
                .liquid(new FluidBuilder().temperature(400))
                .color(0x000000).iconSet(SHINY)
                .flags(GENERATE_GEAR, GENERATE_RING, GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .components(Carbon, 5, Hydrogen, 8)
                .build();

        Cyclohexane = new Material.Builder(1069, gregtechId("cyclohexane"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().autoIgnitionTemperature(518).bp(354).mp(280)
                        .flameTemperature(2220))
                .color(0xF2F2F2E7)
                .components(Carbon, 6, Hydrogen, 12)
                .build();

        NitrosylChloride = new Material.Builder(1070, gregtechId("nitrosyl_chloride"))
                .gas()
                .physicalProperties(new PhysicalProperties.Builder().bp(268).mp(214))
                .flags(FLAMMABLE)
                .color(0xF3F100)
                .components(Nitrogen, 1, Oxygen, 1, Chlorine, 1)
                .build();

        CyclohexanoneOxime = new Material.Builder(1071, gregtechId("cyclohexanone_oxime"))
                .dust()
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .color(0xEBEBF0).iconSet(ROUGH)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .build()
                .setFormula("C6H11NO", true);

        Caprolactam = new Material.Builder(1072, gregtechId("caprolactam"))
                .dust()
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .color(0x676768)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .build()
                .setFormula("(CH2)5C(O)NH", true);

        Butyraldehyde = new Material.Builder(1073, gregtechId("butyraldehyde"))
                .fluid()
                .distilledFluid()
                .physicalProperties(new PhysicalProperties.Builder().autoIgnitionTemperature(503).bp(348).mp(176)
                        .flameTemperature(2120))
                .color(0x554A3F)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 8, Oxygen, 1)
                .build();

        PolyvinylButyral = new Material.Builder(1074, gregtechId("polyvinyl_butyral"))
                .ingot().fluid()
                .color(0x347D41)
                .flags(GENERATE_PLATE, DISABLE_DECOMPOSITION, NO_SMASHING)
                .components(Butyraldehyde, 1, PolyvinylAcetate, 1)
                .build();

        Biphenyl = new Material.Builder(1075, gregtechId("biphenyl"))
                .dust()
                .color(0x8B8C4F).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 10)
                .build()
                .setFormula("(C6H5)2", true);

        PolychlorinatedBiphenyl = new Material.Builder(1076, gregtechId("polychlorinated_biphenyl"))
                .fluid()
                .color(0xCACC0E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 8, Chlorine, 2)
                .build()
                .setFormula("(C6H4Cl)2", true);
    }
}
