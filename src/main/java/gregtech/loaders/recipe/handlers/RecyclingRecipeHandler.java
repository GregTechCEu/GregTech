package gregtech.loaders.recipe.handlers;

import gregtech.api.GregTechAPI;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTUtility;
import gregtech.loaders.recipe.RecyclingRecipes;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

public final class RecyclingRecipeHandler {

    private static final Collection<OrePrefix> CRUSHING_PREFIXES = new ReferenceArrayList<>(
            new OrePrefix[] { OrePrefix.ingot, OrePrefix.gem, OrePrefix.stick, OrePrefix.plate, OrePrefix.plank,
                    OrePrefix.ring, OrePrefix.stickLong, OrePrefix.foil, OrePrefix.bolt, OrePrefix.screw,
                    OrePrefix.nugget, OrePrefix.gearSmall, OrePrefix.gear, OrePrefix.frameGt, OrePrefix.plateDense,
                    OrePrefix.spring, OrePrefix.springSmall, OrePrefix.block, OrePrefix.wireFine, OrePrefix.rotor,
                    OrePrefix.lens, OrePrefix.turbineBlade, OrePrefix.round, OrePrefix.plateDouble, OrePrefix.dust });

    private static final Predicate<String> CRUSHING_PREFIX_PREDICATE = s -> s.startsWith("toolHead") ||
            s.startsWith("gem") || s.startsWith("cableGt") || s.startsWith("wireGt") || s.startsWith("pipe");

    private static final Collection<OrePrefix> IGNORE_ARC_SMELTING = Arrays.asList(
            OrePrefix.ingot, OrePrefix.gem, OrePrefix.nugget);

    private RecyclingRecipeHandler() {}

    public static void register() {
        // registers universal maceration recipes for specified ore prefixes
        for (OrePrefix orePrefix : OrePrefix.values()) {
            if (CRUSHING_PREFIXES.contains(orePrefix) ||
                    CRUSHING_PREFIX_PREDICATE.test(orePrefix.name())) {
                GregTechAPI.oreProcessorManager.registerProcessor(orePrefix,
                        GTUtility.gregtechId("process_crushing_recycling"), PropertyKey.DUST,
                        RecyclingRecipeHandler::processCrushing);
            }
        }
    }

    public static void processCrushing(OrePrefix thingPrefix, Material material, DustProperty property) {
        ArrayList<MaterialStack> materialStacks = new ArrayList<>();
        materialStacks.add(new MaterialStack(material, thingPrefix.getMaterialAmount(material)));
        materialStacks.addAll(thingPrefix.secondaryMaterials);
        // only ignore arc smelting for blacklisted prefixes if yielded material is the same as input material
        // if arc smelting gives different material, allow it
        boolean ignoreArcSmelting = IGNORE_ARC_SMELTING.contains(thingPrefix) &&
                !(material.hasProperty(PropertyKey.INGOT) &&
                        material.getProperty(PropertyKey.INGOT).getArcSmeltInto() != material);
        RecyclingRecipes.registerRecyclingRecipes(OreDictUnifier.get(thingPrefix, material), materialStacks,
                ignoreArcSmelting, thingPrefix);
    }
}
