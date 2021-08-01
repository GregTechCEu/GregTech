package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.Material;

import static gregtech.api.unification.material.MaterialIconSet.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.type.MaterialFlags.*;

public class ThirdDegreeMaterials {

    public static void register() {
        Redstone = new Material.Builder(2007, "redstone")
                .dust().ore(5, 1)
                .color(0xC80000).iconSet(ROUGH)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Silicon, 1, Pyrite, 5, Ruby, 1, Mercury, 3)
                .addOreByproducts(Cinnabar, RareEarth, Glowstone)
                .build();

        EnderEye = new Material.Builder(2008, "ender_eye")
                .gem(1)
                .color(0x66FF66)
                .flags(GENERATE_LENS, NO_SMASHING, NO_SMELTING, DECOMPOSITION_BY_CENTRIFUGING)
                .build();

        Diatomite = new Material.Builder(2009, "diatomite")
                .dust(1)
                .color(0xE1E1E1)
                .components(Flint, 8, BandedIron, 1, Sapphire, 1)
                .addOreByproducts(BandedIron, Sapphire)
                .build();

        RedSteel = new Material.Builder(2010, "red_steel")
                .ingot().fluid()
                .color(0x8C6464).iconSet(METALLIC)
                .flags(EXT_METAL)
                .components(SterlingSilver, 1, BismuthBronze, 1, Steel, 2, BlackSteel, 4)
                .toolStats(7.0f, 4.5f, 896)
                .blastTemp(1300)
                .build();

        BlueSteel = new Material.Builder(2011, "blue_steel")
                .ingot().fluid()
                .color(0x64648C).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FRAME)
                .components(RoseGold, 1, Brass, 1, Steel, 2, BlackSteel, 4)
                .toolStats(7.5f, 5.0f, 1024)
                .blastTemp(1400)
                .build();

        Basalt = new Material.Builder(2012, "basalt")
                .dust(1)
                .color(0x1E1414).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Olivine, 1, Calcite, 3, Flint, 8, DarkAsh, 4)
                .addOreByproducts(Olivine, DarkAsh)
                .build();

        GraniticMineralSand = new Material.Builder(2013, "granitic_mineral_sand")
                .dust(1)
                .color(0x283C3C).iconSet(SAND)
                .components(Magnetite, 1, GraniteBlack, 1)
                .addOreByproducts(GraniteBlack, Magnetite)
                .build();

        Redrock = new Material.Builder(2014, "redrock")
                .dust(1)
                .color(0xFF5032).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Calcite, 2, Flint, 1)
                .addOreByproducts(Clay)
                .build();

        GarnetSand = new Material.Builder(2015, "garnet_sand")
                .dust(1)
                .color(0xC86400).iconSet(SAND)
                .components(GarnetRed, 1, GarnetYellow, 1)
                .addOreByproducts(GarnetRed, GarnetYellow)
                .build();
    }
}
