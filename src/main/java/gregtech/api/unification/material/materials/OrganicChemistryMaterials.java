package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.Material;

import static gregtech.api.unification.material.MaterialIconSet.SHINY;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.type.MaterialFlags.*;

public class OrganicChemistryMaterials {
    /**
     * ID RANGE: 500-511 (incl.)
     */
    public static void register() {
        SiliconeRubber = new Material.Builder(500, "silicone_rubber")
                .ingot().fluid()
                .color(0xDCDCDC)
                .flags(GENERATE_GEAR, GENERATE_RING, FLAMMABLE, NO_SMASHING, GENERATE_FOIL, DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .build();

        Polystyrene = new Material.Builder(501, "polystyrene")
                .ingot().fluid()
                .color(0xBEB4AA)
                .flags(DISABLE_DECOMPOSITION, GENERATE_FOIL, NO_SMASHING)
                .components(Carbon, 8, Hydrogen, 8)
                .build();

        RawRubber = new Material.Builder(502, "raw_rubber")
                .dust()
                .color(0xCCC789)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 5, Hydrogen, 8)
                .build();

        RawStyreneButadieneRubber = new Material.Builder(503, "raw_styrene_butadiene_rubber")
                .dust()
                .color(0x54403D).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 8)
                .build();

        StyreneButadieneRubber = new Material.Builder(504, "styrene_butadiene_rubber")
                .ingot().fluid()
                .color(0x211A18).iconSet(SHINY)
                .flags(GENERATE_GEAR, GENERATE_RING, FLAMMABLE, NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 8)
                .build();

        PolyvinylAcetate = new Material.Builder(505, "polyvinyl_acetate")
                .fluid()
                .color(0xFF9955)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .itemPipeProperties(512, 4)
                .build();

        ReinforcedEpoxyResin = new Material.Builder(506, "reinforced_epoxy_resin")
                .ingot().fluid()
                .color(0xA07A10)
                .flags(STD_METAL, DISABLE_DECOMPOSITION, NO_SMASHING)
                .components(Carbon, 6, Hydrogen, 4, Oxygen, 1)
                .build();

        PolyvinylChloride = new Material.Builder(507, "polyvinyl_chloride")
                .ingot().fluid()
                .color(0xD7E6E6)
                .flags(EXT_METAL, GENERATE_FOIL, DISABLE_DECOMPOSITION, NO_SMASHING)
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .build();

        PolyphenyleneSulfide = new Material.Builder(508, "polyphenylene_sulfide")
                .ingot().fluid()
                .color(0xAA8800)
                .flags(EXT_METAL, DISABLE_DECOMPOSITION, GENERATE_FOIL)
                .components(Carbon, 6, Hydrogen, 4, Sulfur, 1)
                .build();

        GlycerylTrinitrate = new Material.Builder(509, "glyceryl_trinitrate")
                .fluid()
                .flags(FLAMMABLE, EXPLOSIVE, NO_SMELTING, NO_SMASHING)
                .components(Carbon, 3, Hydrogen, 5, Nitrogen, 3, Oxygen, 9)
                .build();

        Polybenzimidazole = new Material.Builder(510, "polybenzimidazole")
                .ingot().fluid()
                .color(0x2D2D2D)
                .flags(EXCLUDE_BLOCK_CRAFTING_RECIPES, NO_SMASHING, DISABLE_DECOMPOSITION, GENERATE_FOIL)
                .components(Carbon, 20, Hydrogen, 12, Nitrogen, 4)
                .fluidPipeProperties(1000, 100, true)
                .build();

        Polydimethylsiloxane = new Material.Builder(511, "polydimethylsiloxane")
                .dust()
                .color(0xF5F5F5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .build();

        Polyethylene = new Material.Builder(174, "plastic") //todo add polyethylene oredicts
                .ingot(1).fluid()
                .color(0xC8C8C8)
                .flags(GENERATE_FOIL, FLAMMABLE, NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Carbon, 1, Hydrogen, 2)
                .fluidPipeProperties(350, 60, true)
                .build();

        Epoxy = new Material.Builder(175, "epoxy")
                .ingot(1).fluid()
                .color(0xC88C14)
                .flags(EXT2_METAL, DISABLE_DECOMPOSITION, NO_SMASHING)
                .components(Carbon, 21, Hydrogen, 25, Chlorine, 1, Oxygen, 5)
                .build();

        Polysiloxane = new Material.Builder(176, "polysiloxane")
                .ingot(1).fluid()
                .color(0xDCDCDC)
                .flags(STD_METAL, FLAMMABLE, NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Carbon, 1, Hydrogen, 1, Silicon, 2, Oxygen, 1)
                .build();

        Polycaprolactam = new Material.Builder(177, "polycaprolactam")
                .ingot(1).fluid()
                .color(0x323232)
                .flags(STD_METAL, DISABLE_DECOMPOSITION, NO_SMASHING)
                .components(Carbon, 6, Hydrogen, 11, Nitrogen, 1, Oxygen, 1)
                .build();

        Polytetrafluoroethylene = new Material.Builder(178, "polytetrafluoroethylene")
                .ingot(1).fluid()
                .color(0x646464)
                .flags(STD_METAL, GENERATE_FRAME, DISABLE_DECOMPOSITION, NO_SMASHING)
                .components(Carbon, 2, Fluorine, 4)
                .fluidPipeProperties(600, 80, true)
                .build();
    }
}
