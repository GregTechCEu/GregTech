package gregtech.api.unification.material.materials;

import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.material.Material;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;

public class OrganicChemistryMaterials {
    /**
     * ID RANGE: 1000-1068 (incl.)
     */
    public static void register() {
        SiliconeRubber = new Material.Builder(1000, "silicone_rubber")
                .ingot().fluid()
                .color(0xDCDCDC)
                .flags(GENERATE_GEAR, GENERATE_RING, FLAMMABLE, NO_SMASHING, GENERATE_FOIL, DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .fluidTemp(900)
                .build();

        Nitrobenzene = new Material.Builder(1001, "nitrobenzene")
                .fluid(FluidTypes.GAS)
                .color(0x704936)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 5, Nitrogen, 1, Oxygen, 2)
                .fluidTemp(293)
                .build();

        RawRubber = new Material.Builder(1002, "raw_rubber")
                .dust()
                .color(0xCCC789)
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .components(Carbon, 5, Hydrogen, 8)
                .build();

        RawStyreneButadieneRubber = new Material.Builder(1003, "raw_styrene_butadiene_rubber")
                .dust()
                .color(0x54403D).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .components(Carbon, 20, Hydrogen, 26)
                .build()
                .setFormula("(C4H6)3C8H8", true);

        StyreneButadieneRubber = new Material.Builder(1004, "styrene_butadiene_rubber")
                .ingot().fluid()
                .color(0x211A18).iconSet(SHINY)
                .flags(GENERATE_FOIL, GENERATE_RING, FLAMMABLE, NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Carbon, 20, Hydrogen, 26)
                .fluidTemp(1000)
                .build()
                .setFormula("(C4H6)3C8H8", true);

        PolyvinylAcetate = new Material.Builder(1005, "polyvinyl_acetate")
                .fluid()
                .color(0xFF9955)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .fluidTemp(293)
                .build();

        ReinforcedEpoxyResin = new Material.Builder(1006, "reinforced_epoxy_resin")
                .ingot().fluid()
                .color(0xA07A10)
                .flags(STD_METAL, DISABLE_DECOMPOSITION, NO_SMASHING, FLAMMABLE)
                .components(Carbon, 6, Hydrogen, 4, Oxygen, 1)
                .fluidTemp(600)
                .build();

        PolyvinylChloride = new Material.Builder(1007, "polyvinyl_chloride")
                .ingot().fluid()
                .color(0xD7E6E6)
                .flags(EXT_METAL, GENERATE_FOIL, DISABLE_DECOMPOSITION, NO_SMASHING, FLAMMABLE)
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .itemPipeProperties(512, 4)
                .fluidTemp(373)
                .build();

        PolyphenyleneSulfide = new Material.Builder(1008, "polyphenylene_sulfide")
                .ingot().fluid()
                .color(0xAA8800)
                .flags(EXT_METAL, DISABLE_DECOMPOSITION, GENERATE_FOIL, FLAMMABLE, NO_SMASHING)
                .components(Carbon, 6, Hydrogen, 4, Sulfur, 1)
                .fluidTemp(500)
                .build();

        GlycerylTrinitrate = new Material.Builder(1009, "glyceryl_trinitrate")
                .fluid()
                .flags(FLAMMABLE, EXPLOSIVE)
                .components(Carbon, 3, Hydrogen, 5, Nitrogen, 3, Oxygen, 9)
                .fluidTemp(293)
                .build();

        Polybenzimidazole = new Material.Builder(1010, "polybenzimidazole")
                .ingot().fluid()
                .color(0x2D2D2D)
                .flags(EXCLUDE_BLOCK_CRAFTING_RECIPES, NO_SMASHING, DISABLE_DECOMPOSITION, GENERATE_FOIL, FLAMMABLE)
                .components(Carbon, 20, Hydrogen, 12, Nitrogen, 4)
                .fluidPipeProperties(1000, 350, true)
                .fluidTemp(1450)
                .build();

        Polydimethylsiloxane = new Material.Builder(1011, "polydimethylsiloxane")
                .dust()
                .color(0xF5F5F5)
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .build();

        Polyethylene = new Material.Builder(1012, "plastic") //todo add polyethylene oredicts
                .ingot(1).fluid()
                .color(0xC8C8C8)
                .flags(GENERATE_FOIL, FLAMMABLE, NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 4)
                .fluidPipeProperties(370, 50, true)
                .fluidTemp(408)
                .build();

        Epoxy = new Material.Builder(1013, "epoxy")
                .ingot(1).fluid()
                .color(0xC88C14)
                .flags(STD_METAL, DISABLE_DECOMPOSITION, NO_SMASHING, FLAMMABLE)
                .components(Carbon, 21, Hydrogen, 25, Chlorine, 1, Oxygen, 5)
                .fluidTemp(400)
                .build();

        // Free ID 1014

        Polycaprolactam = new Material.Builder(1015, "polycaprolactam")
                .ingot(1).fluid()
                .color(0x323232)
                .flags(STD_METAL, DISABLE_DECOMPOSITION, NO_SMASHING, GENERATE_FOIL, FLAMMABLE)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .fluidTemp(493)
                .build();

        Polytetrafluoroethylene = new Material.Builder(1016, "polytetrafluoroethylene")
                .ingot(1).fluid()
                .color(0x646464)
                .flags(STD_METAL, GENERATE_FRAME, DISABLE_DECOMPOSITION, NO_SMASHING, GENERATE_FOIL, FLAMMABLE)
                .components(Carbon, 2, Fluorine, 4)
                .fluidPipeProperties(600, 100, true, true, false, false)
                .fluidTemp(600)
                .build();

        Sugar = new Material.Builder(1017, "sugar")
                .gem(1)
                .color(0xFAFAFA).iconSet(FINE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 12, Oxygen, 6)
                .build();

        Methane = new Material.Builder(1018, "methane")
                .fluid(FluidTypes.GAS)
                .color(0xFF0078).iconSet(GAS)
                .components(Carbon, 1, Hydrogen, 4)
                .build();

        Epichlorohydrin = new Material.Builder(1019, "epichlorohydrin")
                .fluid()
                .color(0x712400)
                .components(Carbon, 3, Hydrogen, 5, Chlorine, 1, Oxygen, 1)
                .build();

        Monochloramine = new Material.Builder(1020, "monochloramine")
                .fluid(FluidTypes.GAS)
                .color(0x3F9F80)
                .components(Nitrogen, 1, Hydrogen, 2, Chlorine, 1)
                .fluidTemp(293)
                .build();

        Chloroform = new Material.Builder(1021, "chloroform")
                .fluid()
                .color(0x892CA0)
                .components(Carbon, 1, Hydrogen, 1, Chlorine, 3)
                .build();

        Cumene = new Material.Builder(1022, "cumene")
                .fluid(FluidTypes.GAS)
                .color(0x552200)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 9, Hydrogen, 12)
                .build();

        Tetrafluoroethylene = new Material.Builder(1023, "tetrafluoroethylene")
                .fluid(FluidTypes.GAS)
                .color(0x7D7D7D)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Fluorine, 4)
                .build();

        Chloromethane = new Material.Builder(1024, "chloromethane")
                .fluid(FluidTypes.GAS)
                .color(0xC82CA0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1, Hydrogen, 3, Chlorine, 1)
                .build();

        AllylChloride = new Material.Builder(1025, "allyl_chloride")
                .fluid()
                .color(0x87DEAA)
                .components(Carbon, 2, Methane, 1, HydrochloricAcid, 1)
                .build()
                .setFormula("C3H5Cl", true);

        Isoprene = new Material.Builder(1026, "isoprene")
                .fluid()
                .color(0x141414)
                .components(Carbon, 5, Hydrogen, 8)
                .build();

        Propane = new Material.Builder(1027, "propane")
                .fluid(FluidTypes.GAS)
                .color(0xFAE250)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 8)
                .build();

        Propene = new Material.Builder(1028, "propene")
                .fluid(FluidTypes.GAS)
                .color(0xFFDD55)
                .components(Carbon, 3, Hydrogen, 6)
                .build();

        Ethane = new Material.Builder(1029, "ethane")
                .fluid(FluidTypes.GAS)
                .color(0xC8C8FF)
                .components(Carbon, 2, Hydrogen, 6)
                .build();

        Butene = new Material.Builder(1030, "butene")
                .fluid(FluidTypes.GAS)
                .color(0xCF5005)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 8)
                .build();

        Butane = new Material.Builder(1031, "butane")
                .fluid(FluidTypes.GAS)
                .color(0xB6371E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 10)
                .build();

        DissolvedCalciumAcetate = new Material.Builder(1032, "dissolved_calcium_acetate")
                .fluid()
                .color(0xDCC8B4)
                .flags(DISABLE_DECOMPOSITION)
                .components(Calcium, 1, Carbon, 4, Oxygen, 4, Hydrogen, 6, Water, 1)
                .build();

        VinylAcetate = new Material.Builder(1033, "vinyl_acetate")
                .fluid()
                .color(0xE1B380)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .build();

        MethylAcetate = new Material.Builder(1034, "methyl_acetate")
                .fluid()
                .color(0xEEC6AF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 6, Oxygen, 2)
                .build();

        Ethenone = new Material.Builder(1035, "ethenone")
                .fluid()
                .color(0x141446)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 2, Oxygen, 1)
                .build();

        Tetranitromethane = new Material.Builder(1036, "tetranitromethane")
                .fluid()
                .color(0x0F2828)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 1, Nitrogen, 4, Oxygen, 8)
                .build();

        Dimethylamine = new Material.Builder(1037, "dimethylamine")
                .fluid(FluidTypes.GAS)
                .color(0x554469)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 7, Nitrogen, 1)
                .build();

        Dimethylhydrazine = new Material.Builder(1038, "dimethylhydrazine")
                .fluid()
                .color(0x000055)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 8, Nitrogen, 2)
                .build();

        DinitrogenTetroxide = new Material.Builder(1039, "dinitrogen_tetroxide")
                .fluid(FluidTypes.GAS)
                .color(0x004184)
                .components(Nitrogen, 2, Oxygen, 4)
                .build();

        Dimethyldichlorosilane = new Material.Builder(1040, "dimethyldichlorosilane")
                .fluid(FluidTypes.GAS)
                .color(0x441650)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Chlorine, 2, Silicon, 1)
                .build();

        Styrene = new Material.Builder(1041, "styrene")
                .fluid()
                .color(0xD2C8BE)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 8)
                .build();

        Butadiene = new Material.Builder(1042, "butadiene")
                .fluid(FluidTypes.GAS)
                .color(0xB55A10)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6)
                .build();

        Dichlorobenzene = new Material.Builder(1043, "dichlorobenzene")
                .fluid()
                .color(0x004455)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 4, Chlorine, 2)
                .build();

        AceticAcid = new Material.Builder(1044, "acetic_acid")
                .fluid(FluidTypes.ACID)
                .color(0xC8B4A0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 4, Oxygen, 2)
                .build();

        Phenol = new Material.Builder(1045, "phenol")
                .fluid()
                .color(0x784421)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 6, Oxygen, 1)
                .build();

        BisphenolA = new Material.Builder(1046, "bisphenol_a")
                .fluid()
                .color(0xD4AA00)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 15, Hydrogen, 16, Oxygen, 2)
                .build();

        VinylChloride = new Material.Builder(1047, "vinyl_chloride")
                .fluid(FluidTypes.GAS)
                .color(0xE1F0F0)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .build();

        Ethylene = new Material.Builder(1048, "ethylene")
                .fluid(FluidTypes.GAS)
                .color(0xE1E1E1)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 4)
                .build();

        Benzene = new Material.Builder(1049, "benzene")
                .fluid()
                .color(0x1A1A1A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 6)
                .build();

        Acetone = new Material.Builder(1050, "acetone")
                .fluid()
                .color(0xAFAFAF)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 3, Hydrogen, 6, Oxygen, 1)
                .build();

        Glycerol = new Material.Builder(1051, "glycerol")
                .fluid()
                .color(0x87DE87)
                .components(Carbon, 3, Hydrogen, 8, Oxygen, 3)
                .build();

        Methanol = new Material.Builder(1052, "methanol")
                .fluid()
                .color(0xAA8800)
                .components(Carbon, 1, Hydrogen, 4, Oxygen, 1)
                .build();

        // FREE ID 1053

        Ethanol = new Material.Builder(1054, "ethanol")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1)
                .build();

        Toluene = new Material.Builder(1055, "toluene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 7, Hydrogen, 8)
                .build();

        DiphenylIsophtalate = new Material.Builder(1056, "diphenyl_isophthalate")
                .fluid()
                .color(0x246E57)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 20, Hydrogen, 14, Oxygen, 4)
                .build();

        PhthalicAcid = new Material.Builder(1057, "phthalic_acid")
                .fluid(FluidTypes.ACID, true)
                .color(0xD1D1D1)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 6, Oxygen, 4)
                .build()
                .setFormula("C6H4(CO2H)2", true);

        Dimethylbenzene = new Material.Builder(1058, "dimethylbenzene")
                .fluid()
                .color(0x669C40)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 10)
                .build()
                .setFormula("C6H4(CH3)2", true);

        Diaminobenzidine = new Material.Builder(1059, "diaminobenzidine")
                .fluid()
                .color(0x337D59)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 14, Nitrogen, 4)
                .build()
                .setFormula("(C6H3(NH2)2)2", true);

        Dichlorobenzidine = new Material.Builder(1060, "dichlorobenzidine")
                .fluid()
                .color(0xA1DEA6)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 12, Hydrogen, 10, Chlorine, 2, Nitrogen, 2)
                .build()
                .setFormula("(C6H3Cl(NH2))2", true);

        Nitrochlorobenzene = new Material.Builder(1061, "nitrochlorobenzene")
                .fluid()
                .color(0x8FB51A)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 4, Chlorine, 1, Nitrogen, 1, Oxygen, 2)
                .build();

        Chlorobenzene = new Material.Builder(1062, "chlorobenzene")
                .fluid()
                .color(0x326A3E)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 6, Hydrogen, 5, Chlorine, 1)
                .build();

        Octane = new Material.Builder(1063, "octane")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .color(0x8A0A09)
                .components(Carbon, 8, Hydrogen, 18)
                .build();

        EthylTertButylEther = new Material.Builder(1064, "ethyl_tertbutyl_ether")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .color(0xB15C06)
                .components(Carbon, 6, Hydrogen, 14, Oxygen, 1)
                .build();

        Ethylbenzene = new Material.Builder(1066, "ethylbenzene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 10)
                .build();

        Naphthalene = new Material.Builder(1067, "naphthalene")
                .fluid()
                .flags(DISABLE_DECOMPOSITION)
                .color(0xF4F4D7)
                .components(Carbon, 10, Hydrogen, 8)
                .build();

        Rubber = new Material.Builder(1068, "rubber")
                .ingot(0).fluid()
                .color(0x000000).iconSet(SHINY)
                .flags(GENERATE_GEAR, GENERATE_RING, FLAMMABLE, NO_SMASHING, DISABLE_DECOMPOSITION, GENERATE_FOIL, GENERATE_BOLT_SCREW)
                .components(Carbon, 5, Hydrogen, 8)
                .fluidTemp(400)
                .build();

        Cyclohexane = new Material.Builder(1069, "cyclohexane")
                .fluid()
                .color(0xF2F2F2E7)
                .components(Carbon, 6, Hydrogen, 12)
                .build();

        NitrosylChloride = new Material.Builder(1070, "nitrosyl_chloride")
                .fluid(FluidTypes.GAS)
                .flags(FLAMMABLE)
                .color(0xF3F100)
                .components(Nitrogen, 1, Oxygen, 1, Chlorine, 1)
                .build();

        CyclohexanoneOxime = new Material.Builder(1071, "cyclohexanone_oxime")
                .dust()
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .color(0xEBEBF0).iconSet(ROUGH)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .build()
                .setFormula("C6H11NO", true);

        Caprolactam = new Material.Builder(1072, "caprolactam")
                .dust()
                .flags(DISABLE_DECOMPOSITION, FLAMMABLE)
                .color(0x676768)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .build()
                .setFormula("(CH2)5C(O)NH", true);

        Butyraldehyde = new Material.Builder(1073, "butyraldehyde")
                .fluid()
                .color(0x554A3F)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 8, Oxygen, 1)
                .build();

        PolyvinylButyral = new Material.Builder(1074, "polyvinyl_butyral")
                .ingot().fluid()
                .color(0x347D41)
                .flags(GENERATE_PLATE, DISABLE_DECOMPOSITION, NO_SMASHING)
                .components(Butyraldehyde, 1, PolyvinylAcetate, 1)
                .build();
    }
}
