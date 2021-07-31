package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.MaterialBuilder;
import static gregtech.api.unification.material.MaterialIconSet.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.type.MaterialFlags.*;

public class OrganicChemistryMaterials {
    /**
     * ID RANGE: 500-511 (incl.)
     */
    public static void register() {
        SiliconeRubber = new MaterialBuilder(500, "silicone_rubber")
                .ingot().fluid()
                .color(0xDCDCDC)
                .flags(GENERATE_PLATE, GENERATE_GEAR, GENERATE_RING, FLAMMABLE, NO_SMASHING, GENERATE_FOIL, DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .build();

        Polystyrene = new MaterialBuilder(501, "polystyrene")
                .ingot().fluid()
                .color(0xBEB4AA)
                .flags(DISABLE_DECOMPOSITION, GENERATE_FOIL, NO_SMASHING)
                .components(Carbon, 8, Hydrogen, 8)
                .build();

        RawRubber = new MaterialBuilder(502, "raw_rubber")
                .dust().fluid()
                .color(0xCCC789)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 5, Hydrogen, 8)
                .build();

        RawStyreneButadieneRubber = new MaterialBuilder(503, "raw_styrene_butadiene_rubber")
                .dust().fluid()
                .color(0x54403D).iconSet(SHINY)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 8)
                .build();

        StyreneButadieneRubber = new MaterialBuilder(504, "styrene_butadiene_rubber")
                .ingot().fluid()
                .color(0x211A18).iconSet(SHINY)
                .flags(GENERATE_GEAR, GENERATE_RING, FLAMMABLE, NO_SMASHING, DISABLE_DECOMPOSITION)
                .components(Carbon, 8, Hydrogen, 8)
                .build();

        PolyvinylAcetate = new MaterialBuilder(505, "polyvinyl_acetate")
                .fluid()
                .color(0xFF9955).iconSet(FLUID)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 4, Hydrogen, 6, Oxygen, 2)
                .build();

        ReinforcedEpoxyResin = new MaterialBuilder(506, "reinforced_epoxy_resin")
                .ingot().fluid()
                .color(0xA07A10)
                .flags(GENERATE_PLATE, DISABLE_DECOMPOSITION, NO_SMASHING)
                .components(Carbon, 6, Hydrogen, 4, Oxygen, 1)
                .build();

        PolyvinylChloride = new MaterialBuilder(507, "polyvinyl_chloride")
                .ingot().fluid()
                .color(0xD7E6E6)
                .flags(EXT_METAL, GENERATE_FOIL, DISABLE_DECOMPOSITION, NO_SMASHING)
                .components(Carbon, 2, Hydrogen, 3, Chlorine, 1)
                .build();

        PolyphenyleneSulfide = new MaterialBuilder(508, "polyphenylene_sulfide")
                .ingot().fluid()
                .color(0xAA8800)
                .flags(EXT_METAL, DISABLE_DECOMPOSITION, GENERATE_FOIL)
                .components(Carbon, 6, Hydrogen, 4, Sulfur, 1)
                .build();

        GlycerylTrinitrate = new MaterialBuilder(509, "glyceryl_trinitrate")
                .fluid()
                .color(0xFFFFFF).iconSet(FLUID)
                .flags(FLAMMABLE, EXPLOSIVE, NO_SMELTING, NO_SMASHING)
                .components(Carbon, 3, Hydrogen, 5, Nitrogen, 3, Oxygen, 9)
                .build();

        Polybenzimidazole = new MaterialBuilder(510, "polybenzimidazole")
                .ingot().fluid()
                .color(0x2D2D2D)
                .flags(EXCLUDE_BLOCK_CRAFTING_RECIPES, NO_SMASHING, DISABLE_DECOMPOSITION, GENERATE_FOIL)
                .components(Carbon, 20, Hydrogen, 12, Nitrogen, 4)
                .build();

        Polydimethylsiloxane = new MaterialBuilder(511, "polydimethylsiloxane")
                .dust().fluid()
                .color(0xF5F5F5)
                .flags(DISABLE_DECOMPOSITION)
                .components(Carbon, 2, Hydrogen, 6, Oxygen, 1, Silicon, 1)
                .build();

    }
}
