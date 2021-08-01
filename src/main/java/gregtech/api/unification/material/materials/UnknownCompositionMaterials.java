package gregtech.api.unification.material.materials;

import gregtech.api.unification.material.MaterialBuilder;
import gregtech.api.unification.material.type.RoughSolidMaterial;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;

import static com.google.common.collect.ImmutableList.of;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.material.MaterialIconSet.*;
import static gregtech.api.unification.material.MaterialIconSet.FLUID;
import static gregtech.api.unification.material.type.MaterialFlags.*;
import static gregtech.api.unification.material.type.MaterialFlags.NO_SMELTING;

public class UnknownCompositionMaterials {

    public static void register() {

        WoodGas = new MaterialBuilder()

        public static SimpleFluidMaterial WoodGas = new SimpleFluidMaterial("wood_gas", 0xDECD87, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial WoodVinegar = new SimpleFluidMaterial("wood_vinegar", 0xD45500, FLUID, of(), 0);
        public static SimpleFluidMaterial WoodTar = new SimpleFluidMaterial("wood_tar", 0x28170B, FLUID, of(), 0);
        public static SimpleFluidMaterial CharcoalByproducts = new SimpleFluidMaterial("charcoal_byproducts", 0x784421, FLUID, of(), 0);
        public static SimpleFluidMaterial Biomass = new SimpleFluidMaterial("biomass", 0x00FF00, FLUID, of(), 0);
        public static SimpleFluidMaterial BioDiesel = new SimpleFluidMaterial("bio_diesel", 0xFF8000, FLUID, of(), 0);
        public static SimpleFluidMaterial FermentedBiomass = new SimpleFluidMaterial("fermented_biomass", 0x445500, FLUID, of(), 0);
        public static SimpleFluidMaterial Creosote = new SimpleFluidMaterial("creosote", 0x804000, FLUID, of(), 0);
        public static SimpleFluidMaterial Diesel = new SimpleFluidMaterial("fuel", 0xFFFFFF, FLUID, of(), 0);
        public static SimpleFluidMaterial RocketFuel = new SimpleFluidMaterial("rocket_fuel", 0xBDB78C, FLUID, of(), 0);
        public static SimpleFluidMaterial Glue = new SimpleFluidMaterial("glue", 0xC8C400, FLUID, of(), 0);
        public static SimpleFluidMaterial Lubricant = new SimpleFluidMaterial("lubricant", 0xFFFFFF, FLUID, of(), 0);
        public static SimpleFluidMaterial McGuffium239 = new SimpleFluidMaterial("mc_guffium239", 0xFFFFFF, FLUID, of(), 0);
        public static SimpleFluidMaterial IndiumConcentrate = new SimpleFluidMaterial("indium_concentrate", 0x0e2950, FLUID, of(), 0);
        public static SimpleFluidMaterial SeedOil = new SimpleFluidMaterial("seed_oil", 0xC4FF00, FLUID, of(), 0);
        public static SimpleFluidMaterial DrillingFluid = new SimpleFluidMaterial("drilling_fluid", 0xFFFFAA, FLUID, of(), 0);
        public static SimpleFluidMaterial ConstructionFoam = new SimpleFluidMaterial("construction_foam", 0x808080, FLUID, of(), 0);
        public static SimpleFluidMaterial HydroCrackedEthane = new SimpleFluidMaterial("hydrocracked_ethane", 0x9696BC, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial HydroCrackedEthylene = new SimpleFluidMaterial("hydrocracked_ethylene", 0xA3A3A0, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial HydroCrackedPropene = new SimpleFluidMaterial("hydrocracked_propene", 0xBEA540, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial HydroCrackedPropane = new SimpleFluidMaterial("hydrocracked_propane", 0xBEA540, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial HydroCrackedLightFuel = new SimpleFluidMaterial("hydrocracked_light_fuel", 0xB7AF08, FLUID, of(), 0);
        public static SimpleFluidMaterial HydroCrackedButane = new SimpleFluidMaterial("hydrocracked_butane", 0x852C18, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial HydroCrackedNaphtha = new SimpleFluidMaterial("hydrocracked_naphtha", 0xBFB608, FLUID, of(), 0);
        public static SimpleFluidMaterial HydroCrackedHeavyFuel = new SimpleFluidMaterial("hydrocracked_heavy_fuel", 0xFFFF00, FLUID, of(), 0);
        public static SimpleFluidMaterial HydroCrackedGas = new SimpleFluidMaterial("hydrocracked_gas", 0xB4B4B4, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial HydroCrackedButene = new SimpleFluidMaterial("hydrocracked_butene", 0x993E05, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial HydroCrackedButadiene = new SimpleFluidMaterial("hydrocracked_butadiene", 0xAD5203, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial SteamCrackedEthane = new SimpleFluidMaterial("steamcracked_ethane", 0x9696BC, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial SteamCrackedEthylene = new SimpleFluidMaterial("steamcracked_ethylene", 0xA3A3A0, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial SteamCrackedPropene = new SimpleFluidMaterial("steamcracked_propene", 0xBEA540, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial SteamCrackedPropane = new SimpleFluidMaterial("steamcracked_propane", 0xBEA540, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial SteamCrackedButane = new SimpleFluidMaterial("steamcracked_butane", 0x852C18, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial SteamCrackedNaphtha = new SimpleFluidMaterial("steamcracked_naphtha", 0xBFB608, FLUID, of(), 0);
        public static SimpleFluidMaterial SteamCrackedGas = new SimpleFluidMaterial("steamcracked_gas", 0xB4B4B4, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial SteamCrackedButene = new SimpleFluidMaterial("steamcracked_butene", 0x993E05, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial SteamCrackedButadiene = new SimpleFluidMaterial("steamcracked_butadiene", 0xAD5203, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial SulfuricGas = new SimpleFluidMaterial("sulfuric_gas", 0xFFFFFF, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial RefineryGas = new SimpleFluidMaterial("refinery_gas", 0xFFFFFF, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial SulfuricNaphtha = new SimpleFluidMaterial("sulfuric_naphtha", 0xFFFFFF, FLUID, of(), 0);
        public static SimpleFluidMaterial SulfuricLightFuel = new SimpleFluidMaterial("sulfuric_light_fuel", 0xFFFFFF, FLUID, of(), 0);
        public static SimpleFluidMaterial SulfuricHeavyFuel = new SimpleFluidMaterial("sulfuric_heavy_fuel", 0xFFFFFF, FLUID, of(), 0);
        public static SimpleFluidMaterial Naphtha = new SimpleFluidMaterial("naphtha", 0xFFFFFF, FLUID, of(), 0);
        public static SimpleFluidMaterial LightFuel = new SimpleFluidMaterial("light_fuel", 0xFFFFFF, FLUID, of(), 0);
        public static SimpleFluidMaterial HeavyFuel = new SimpleFluidMaterial("heavy_fuel", 0xFFFFFF, FLUID, of(), 0);
        public static SimpleFluidMaterial LPG = new SimpleFluidMaterial("lpg", 0xFFFFFF, GAS, of(), STATE_GAS);
        public static SimpleFluidMaterial SteamCrackedLightFuel = new SimpleFluidMaterial("steamcracked_light_fuel", 0xFFFFFF, FLUID, of(), 0);
        public static SimpleFluidMaterial SteamCrackedHeavyFuel = new SimpleFluidMaterial("steamcracked_heavy_fuel", 0xFFFFFF, FLUID, of(), 0);
        public static SimpleFluidMaterial UUAmplifier = new SimpleFluidMaterial("uuamplifier", 0x000000, FLUID, of(), 0);
        public static SimpleFluidMaterial UUMatter = new SimpleFluidMaterial("uumatter", 0x8000C4, FLUID, of(), 0);
        public static SimpleFluidMaterial Honey = new SimpleFluidMaterial("honey", 0xFFFFFF, FLUID, of(), 0);
        public static SimpleFluidMaterial Juice = new SimpleFluidMaterial("juice", 0xA8C972, FLUID, of(), 0);
        public static SimpleFluidMaterial RawGrowthMedium = new SimpleFluidMaterial("raw_growth_medium", 10777425, FLUID, of(), DISABLE_DECOMPOSITION);
        public static SimpleFluidMaterial SterileGrowthMedium = new SimpleFluidMaterial("sterilized_growth_medium", 11306862, FLUID, of(), DISABLE_DECOMPOSITION);
        public static SimpleFluidMaterial Oil = new SimpleFluidMaterial("oil", 0x0A0A0A, FLUID, of(), GENERATE_FLUID_BLOCK);
        public static SimpleFluidMaterial OilHeavy = new SimpleFluidMaterial("oil_heavy", 0x0A0A0A, FLUID, of(), GENERATE_FLUID_BLOCK);
        public static SimpleFluidMaterial OilMedium = new SimpleFluidMaterial("oil_medium", 0x0A0A0A, FLUID, of(), GENERATE_FLUID_BLOCK);
        public static SimpleFluidMaterial OilLight = new SimpleFluidMaterial("oil_light", 0x0A0A0A, FLUID, of(), GENERATE_FLUID_BLOCK);
        public static SimpleFluidMaterial NaturalGas = new SimpleFluidMaterial("natural_gas", 0xFFFFFF, GAS, of(), STATE_GAS | GENERATE_FLUID_BLOCK);
        public static SimpleFluidMaterial Bacteria = new SimpleFluidMaterial("bacteria", 0x808000, FLUID, of(), 0);
        public static SimpleFluidMaterial BacterialSludge = new SimpleFluidMaterial("bacterial_sludge", 0x355E3B, FLUID, of(), 0);
        public static SimpleFluidMaterial EnrichedBacterialSludge = new SimpleFluidMaterial("enriched_bacterial_sludge", 0x7FFF00, FLUID, of(), 0);
        public static SimpleFluidMaterial FermentedBacterialSludge = new SimpleFluidMaterial("fermented_bacterial_sludge", 0x32CD32, FLUID, of(), 0);
        public static SimpleFluidMaterial Mutagen = new SimpleFluidMaterial("mutagen", 0x00FF7F, FLUID, of(), 0);
        public static SimpleFluidMaterial GelatinMixture = new SimpleFluidMaterial("gelatin_mixture", 0x588BAE, FLUID, of(), 0);
        public static SimpleFluidMaterial RawGasoline = new SimpleFluidMaterial("raw_gasoline", 0xFF6400, FLUID, of(), DISABLE_DECOMPOSITION);
        public static SimpleFluidMaterial Gasoline = new SimpleFluidMaterial("gasoline", 0xFFA500, FLUID, of(), DISABLE_DECOMPOSITION);
        public static SimpleFluidMaterial HighOctaneGasoline = new SimpleFluidMaterial("gasoline_premium", 0xFFA500, FLUID, of(), DISABLE_DECOMPOSITION);
        public static SimpleFluidMaterial Nitrobenzene = new SimpleFluidMaterial("nitrobenzene", 0x704936, FLUID, of(), DISABLE_DECOMPOSITION);
        public static SimpleFluidMaterial CoalGas = new SimpleFluidMaterial("coal_gas", 0x333333, GAS, of(), DISABLE_DECOMPOSITION);
        public static SimpleFluidMaterial CoalTar = new SimpleFluidMaterial("coal_tar", 0x1A1A1A, FLUID, of(), DISABLE_DECOMPOSITION);
        public static RoughSolidMaterial Wood = new RoughSolidMaterial(269, "wood", 0x643200, WOOD, 0, of(), STD_SOLID | FLAMMABLE | NO_SMELTING | GENERATE_GEAR | GENERATE_LONG_ROD | GENERATE_FRAME, () -> OrePrefix.plank);
        public static DustMaterial Gunpowder = new DustMaterial(270, "gunpowder", 0x808080, ROUGH, 0, of(), FLAMMABLE | EXPLOSIVE | NO_SMELTING | NO_SMASHING);
        public static DustMaterial Oilsands = new DustMaterial(271, "oilsands", 0x0A0A0A, SAND, 1, of(new MaterialStack(Oil, 1L)), GENERATE_ORE);
        public static RoughSolidMaterial Paper = new RoughSolidMaterial(272, "paper", 0xFAFAFA, PAPER, 0, of(), GENERATE_PLATE | FLAMMABLE | NO_SMELTING | NO_SMASHING | MORTAR_GRINDABLE | GENERATE_RING | EXCLUDE_PLATE_COMPRESSOR_RECIPE, () -> OrePrefix.plate);
        public static DustMaterial RareEarth = new DustMaterial(273, "rare_earth", 0x808064, FINE, 0, of(), 0);
        public static DustMaterial Stone = new DustMaterial(274, "stone", 0xCDCDCD, ROUGH, 1, of(), MORTAR_GRINDABLE | GENERATE_GEAR | GENERATE_PLATE | NO_SMASHING | NO_RECYCLING);
        public static FluidMaterial Lava = new FluidMaterial(275, "lava", 0xFF4000, FLUID, of(), 0);
        public static DustMaterial Glowstone = new DustMaterial(276, "glowstone", 0xFFFF00, SHINY, 1, of(), NO_SMASHING | SMELT_INTO_FLUID | GENERATE_PLATE | EXCLUDE_PLATE_COMPRESSOR_RECIPE);
        public static GemMaterial NetherStar = new GemMaterial(277, "nether_star", 0xFFFFFF, NETHERSTAR, 4, of(), STD_SOLID | GENERATE_LENS | NO_SMASHING | NO_SMELTING);
        public static DustMaterial Endstone = new DustMaterial(278, "endstone", 0xFFFFFF, DULL, 1, of(), NO_SMASHING);
        public static DustMaterial Netherrack = new DustMaterial(279, "netherrack", 0xC80000, DULL, 1, of(), NO_SMASHING | FLAMMABLE);
        public static FluidMaterial NitroDiesel = new FluidMaterial(280, "nitro_fuel", 0xC8FF00, FLUID, of(), FLAMMABLE | EXPLOSIVE | NO_SMELTING | NO_SMASHING);
        public static DustMaterial Collagen = new DustMaterial(281, "collagen", 0x80471C, ROUGH, 1, of(), 0);
        public static DustMaterial Gelatin = new DustMaterial(282, "gelatin", 0x588BAE, ROUGH, 1, of(), 0);
        public static DustMaterial Agar = new DustMaterial(283, "agar", 0x4F7942, ROUGH, 1, of(), 0);
        public static DustMaterial Andesite = new DustMaterial(314, "andesite", 0xBEBEBE, ROUGH, 2, of(), NO_SMASHING);
        public static DustMaterial Diorite = new DustMaterial(315, "diorite", 0xFFFFFF, ROUGH, 2, of(), NO_SMASHING);
        public static DustMaterial Granite = new DustMaterial(316, "granite", 0xCFA18C, ROUGH, 2, of(), NO_SMASHING);
    }
}
