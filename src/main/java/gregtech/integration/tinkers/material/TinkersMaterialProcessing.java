package gregtech.integration.tinkers.material;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.ConfigHolder;
import gregtech.integration.IntegrationModule;
import gregtech.integration.tinkers.TinkersUtil;
import org.jetbrains.annotations.Nullable;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.TinkerRegistry;

import java.util.HashMap;
import java.util.Map;

import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static slimeknights.tconstruct.library.materials.Material.UNKNOWN;

public class TinkersMaterialProcessing {

    private static Map<Material, TinkersMaterialStats.Builder> materialStats;
    private static OrePrefix[] orePrefixes;

    public static void init() {
        materialStats = new HashMap<>();

        for (Material m : GregTechAPI.materialManager.getRegisteredMaterials()) {
            //if (m.hasProperty(PropertyKey.FLUID)) {
            //    registerMelting(m);
            //}

            if (m.hasProperty(PropertyKey.TOOL)) {
                if (m.hasProperty(PropertyKey.INGOT)) {
                    materialStats.put(m, TinkersMaterialStats.createIngotTemplate(m));
                } else { // gem
                    materialStats.put(m, TinkersMaterialStats.createGemTemplate(m));
                }
            } else if (m.hasProperty(PropertyKey.ORE) && m.hasProperty(PropertyKey.FLUID)) {
                // Allow smeltery melting of non-tool materials that have an ore and a fluid
                TMaterial tconMaterial = new TMaterial(m.toString(), m.getMaterialRGB());
                TinkersUtil.registerMelting(tconMaterial, m);
                if (TinkerRegistry.getMaterial(tconMaterial.identifier) == UNKNOWN) {
                    TinkerRegistry.addMaterial(tconMaterial);
                    TinkerRegistry.integrate(tconMaterial, tconMaterial.getFluid(), TinkersUtil.getFormattedName(m));
                }
            }
        }
        TinkersMaterialAdditions.init();
        materialStats.values().forEach(TinkersMaterialStats.Builder::build);
        materialStats = null; // discard the list
    }

    public static TinkersMaterialStats.Builder getBuilder(Material m) {
        TinkersMaterialStats.Builder b = materialStats.get(m);
        if (b == null) {
            // To not crash if a material we reference somewhere loses its tool property
            IntegrationModule.logger.error("Could not get Tinker tool builder from material {}, as it does not exist!", m);
            return TinkersMaterialStats.builder(null).cancel();
        }
        return b;
    }

    private static void registerMelting(Material m) {
        Material smeltInto = getSmelteryResult(m);
        if (smeltInto == null) return; // this case is already handled by Tinkers TODO is this needed?

        if (m.hasProperty(PropertyKey.ORE)) {
            OreProperty prop = m.getProperty(PropertyKey.ORE);
            for (OrePrefix p : getOrePrefixes()) {
                TinkerRegistry.registerMelting(getOreName(p, m), smeltInto.getFluid(), (int) (GTValues.L * prop.getOreMultiplier() * Config.oreToIngotRatio));
            }
        }

        if (m.hasProperty(PropertyKey.DUST)) {

        }
    }

    @Nullable
    private static Material getSmelteryResult(Material m) {
        Material smeltInto = m.getProperty(PropertyKey.INGOT).getSmeltingInto();
        if (m != smeltInto) return smeltInto;

        // Special cases
        if (m == AnnealedCopper) return Copper;
        if (m == WroughtIron) return Iron;

        // Excluded materials that are already in Tinkers
        // TODO is this needed?
        if (m == Iron || m == Cobalt || m == Copper || m == Bronze || m == Lead || m == Electrum || m == Silver || m == Steel) return null;

        return m;
    }

    private static String getOreName(OrePrefix p, Material m) {
        return new UnificationEntry(p, m).toString();
    }

    // TODO CEu should have a way to get this
    private static OrePrefix[] getOrePrefixes() {
        if (orePrefixes != null) return orePrefixes;
        if (ConfigHolder.worldgen.allUniqueStoneTypes) {
            return orePrefixes = new OrePrefix[]{
                    ore, oreGranite, oreDiorite, oreAndesite, oreBlackgranite, oreRedgranite,
                    oreMarble, oreBasalt, oreNetherrack, oreEndstone, oreSand, oreRedSand,
            };
        }
        return orePrefixes = new OrePrefix[]{
                ore, oreNetherrack, oreEndstone,
        };
    }
}
