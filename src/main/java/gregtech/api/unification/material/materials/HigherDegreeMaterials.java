package gregtech.api.unification.material.materials;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.material.info.MaterialIconSet.*;

public class HigherDegreeMaterials {

    public static void register() {
        /**
         * Third Degree
         */
        Redstone = new Material.Builder(2507, "redstone")
                .dust().ore(5, 1)
                .color(0xC80000).iconSet(ROUGH)
                .flags(GENERATE_PLATE, NO_SMASHING, NO_SMELTING, EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Silicon, 1, Pyrite, 5, Ruby, 1, Mercury, 3)
                .build();

        EnderEye = new Material.Builder(2508, "ender_eye")
                .gem(1)
                .color(0x66FF66)
                .flags(GENERATE_LENS, NO_SMASHING, NO_SMELTING, DECOMPOSITION_BY_CENTRIFUGING)
                .build();

        Diatomite = new Material.Builder(2509, "diatomite")
                .dust(1).ore()
                .color(0xE1E1E1)
                .components(Flint, 8, BandedIron, 1, Sapphire, 1)
                .build();

        RedSteel = new Material.Builder(2510, "red_steel")
                .ingot().fluid()
                .color(0x8C6464).iconSet(METALLIC)
                .flags(EXT_METAL)
                .components(SterlingSilver, 1, BismuthBronze, 1, Steel, 2, BlackSteel, 4)
                .toolStats(7.0f, 4.5f, 896, 21)
                .blastTemp(1300)
                .build();

        BlueSteel = new Material.Builder(2511, "blue_steel")
                .ingot().fluid()
                .color(0x64648C).iconSet(METALLIC)
                .flags(EXT_METAL, GENERATE_FRAME, GENERATE_GEAR)
                .components(RoseGold, 1, Brass, 1, Steel, 2, BlackSteel, 4)
                .toolStats(7.5f, 5.0f, 1024, 21)
                .blastTemp(1400)
                .build();

        Basalt = new Material.Builder(2512, "basalt")
                .dust(1)
                .color(0x3C3232).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Olivine, 1, Calcite, 3, Flint, 8, DarkAsh, 4)
                .build();

        GraniticMineralSand = new Material.Builder(2513, "granitic_mineral_sand")
                .dust(1).ore()
                .color(0x283C3C).iconSet(SAND)
                .components(Magnetite, 1, GraniteBlack, 1)
                .flags(BLAST_FURNACE_CALCITE_DOUBLE)
                .build();

        Redrock = new Material.Builder(2514, "redrock")
                .dust(1)
                .color(0xFF5032).iconSet(ROUGH)
                .flags(NO_SMASHING, DECOMPOSITION_BY_CENTRIFUGING)
                .components(Calcite, 2, Flint, 1)
                .build();

        GarnetSand = new Material.Builder(2515, "garnet_sand")
                .dust(1)
                .color(0xC86400).iconSet(SAND)
                .components(GarnetRed, 1, GarnetYellow, 1)
                .build();

        HSSG = new Material.Builder(2516, "hssg")
                .ingot(3).fluid()
                .color(0x999900).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_FRAME, GENERATE_SPRING)
                .components(TungstenSteel, 5, Chrome, 1, Molybdenum, 2, Vanadium, 1)
                .toolStats(10.0f, 5.5f, 4000, 21)
                .cableProperties(GTValues.V[6], 4, 2)
                .blastTemp(4200)
                .build();

        /**
         * Fourth Degree
         */
        RedAlloy = new Material.Builder(2517, "red_alloy")
                .ingot(0).fluid()
                .color(0xC80000)
                .flags(STD_METAL, GENERATE_FINE_WIRE, GENERATE_BOLT_SCREW)
                .components(Copper, 1, Redstone, 1)
                .cableProperties(GTValues.V[0], 1, 0)
                .build();

        BasalticMineralSand = new Material.Builder(2518, "basaltic_mineral_sand")
                .dust(1).ore()
                .color(0x283228).iconSet(SAND)
                .components(Magnetite, 1, Basalt, 1)
                .flags(BLAST_FURNACE_CALCITE_DOUBLE)
                .build();

        HSSE = new Material.Builder(2519, "hsse")
                .ingot(4).fluid()
                .color(0x336600).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_ROTOR, GENERATE_SMALL_GEAR, GENERATE_FRAME)
                .components(HSSG, 6, Cobalt, 1, Manganese, 1, Silicon, 1)
                .toolStats(10.0f, 8.0f, 5120, 21)
                .blastTemp(5000)
                .build();

        HSSS = new Material.Builder(2520, "hsss")
                .ingot(4).fluid()
                .color(0x660033).iconSet(METALLIC)
                .flags(EXT2_METAL, GENERATE_SMALL_GEAR)
                .components(HSSG, 6, Iridium, 2, Osmium, 1)
                .toolStats(15.0f, 7.0f, 3000, 21)
                .blastTemp(5000)
                .build();

        FluxedElectrum = new Material.Builder(2521, "fluxed_electrum")
                .ingot(5).fluid()
                .color(0xf2ef27).iconSet(METALLIC)
                .flags(EXT2_METAL)
                .components(Electrum, 1, NaquadahAlloy, 1, BlueSteel, 1, RedSteel, 1)
                .toolStats(11.0f, 6.0f, 2100, 21)
                .cableProperties(GTValues.V[8], 3, 2)
                .itemPipeProperties(128, 16)
                .blastTemp(9000)
                .build();
    }
}
