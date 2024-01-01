package gregtech.loaders.recipe.handlers;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.recipes.logic.OverclockingLogic;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlag;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.BlastProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;

import gregtech.loaders.recipe.CraftingComponent;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ForgingFurnaceRecipeHandler {

    private static final Map<MaterialFlag, OrePrefix> FORGEABLE_MATERIAL_FLAGS = new Object2ObjectOpenHashMap<>(15, 1);

    private static final List<OrePrefix> FORGEABLE_PIPES = new ObjectArrayList<>(9);

    private static void populateReferences() {
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_DENSE, OrePrefix.plateDense);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_FRAME, OrePrefix.frameGt);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_PLATE, OrePrefix.plate);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_DOUBLE_PLATE, OrePrefix.plateDouble);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_FOIL, OrePrefix.foil);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_BOLT_SCREW, OrePrefix.bolt);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_GEAR, OrePrefix.gear);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_SMALL_GEAR, OrePrefix.gearSmall);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_LONG_ROD, OrePrefix.stickLong);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_ROD, OrePrefix.stick);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_RING, OrePrefix.ring);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_ROTOR, OrePrefix.rotor);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_ROUND, OrePrefix.round);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_SPRING, OrePrefix.spring);
        FORGEABLE_MATERIAL_FLAGS.put(MaterialFlags.GENERATE_SPRING_SMALL, OrePrefix.springSmall);

        FORGEABLE_PIPES.add(OrePrefix.pipeHugeItem);
        FORGEABLE_PIPES.add(OrePrefix.pipeLargeItem);
        FORGEABLE_PIPES.add(OrePrefix.pipeNormalItem);
        FORGEABLE_PIPES.add(OrePrefix.pipeSmallItem);
        FORGEABLE_PIPES.add(OrePrefix.pipeHugeFluid);
        FORGEABLE_PIPES.add(OrePrefix.pipeLargeFluid);
        FORGEABLE_PIPES.add(OrePrefix.pipeNormalFluid);
        // skip quadruple and nonuple; they can be straightforwardly crafted.
        FORGEABLE_PIPES.add(OrePrefix.pipeSmallFluid);
        FORGEABLE_PIPES.add(OrePrefix.pipeTinyFluid);
    }

    public static void runRecipeGeneration() {
        populateReferences();
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            if (material.hasProperty(PropertyKey.BLAST))
                processForging(material);
        }
    }

    private static void processForging(Material material) {
        if (material.hasFlag(MaterialFlags.DISABLE_FORGING)) return;

        BlastProperty property = material.getProperty(PropertyKey.BLAST);
        RecipeBuilder<BlastRecipeBuilder> builder = createBuilder(property, material);

        // Cooled ingot
        buildRecipes(property, OrePrefix.ingot, material, 0, builder);
        int i = 0;
        // Forgeable components
        for (Map.Entry<MaterialFlag, OrePrefix> entry : FORGEABLE_MATERIAL_FLAGS.entrySet()) {
            i++;
            if (material.hasFlag(entry.getKey())) {
                buildRecipes(property, entry.getValue(), material, i, builder);
            }
        }

        // Pipes
        if (material.hasProperty(PropertyKey.ITEM_PIPE)) {
            for (OrePrefix prefix : FORGEABLE_PIPES.subList(0, 3)) {
                i++;
                buildRecipes(property, prefix, material, i, builder);
            }
        } else if (material.hasProperty(PropertyKey.FLUID_PIPE)) {
            for (OrePrefix prefix : FORGEABLE_PIPES.subList(4, 8)) {
                i++;
                buildRecipes(property, prefix, material, i, builder);
            }

        }
    }

    protected static @NotNull BlastRecipeBuilder createBuilder(BlastProperty property, Material material) {
        BlastRecipeBuilder builder = RecipeMaps.FORGING_RECIPES.recipeBuilder();
        // apply the duration override
        int duration = property.getDurationOverride();
        if (duration < 0) duration = Math.max(1, (int) (material.getMass() * property.getBlastTemperature() / 50L));
        // 5% & 100K penalty for forging
        builder.duration((int) (duration * 1.05))
                .blastFurnaceTemp(property.getBlastTemperature() + 100);

        // apply the EUt override
        int EUt = property.getEUtOverride();
        if (EUt < 0) EUt = GTValues.VA[GTValues.MV];
        builder.EUt(EUt);

        return builder;
    }

    protected static void buildRecipes(BlastProperty property, OrePrefix prefix, Material material, int circuitMeta,
                                       RecipeBuilder<BlastRecipeBuilder> builder) {
        // early copy in order to keep us separate.
        builder = builder.copy();

        long ratio = prefix.getMaterialAmount(material);

        // these should always return proper whole numbers
        // only weird ratios like 3/2 will break
        int inputAmount = (int) Math.max(ratio / GTValues.M, 1);
        int outputAmount = (int) Math.max(GTValues.M / ratio, 1);

        builder.input(OrePrefix.dust, material, inputAmount);
        builder.output(prefix, material, outputAmount);

        int duration = inputAmount * builder.getDuration();
        if (duration <= 0) return;

        int coolerDuration = 0;
        // don't bother with cooling time if no hot ingot exists
        if (OrePrefix.ingotHot.doGenerateItem(material)) {
            int coolerEU = property.getVacuumEUtOverride();
            if (coolerEU == -1) {
                coolerEU = 120;
            }

            // log base x of y = log(y)/log(x)
            int forgingTier = (int) Math.ceil(Math.log(builder.getEUt() / 8d) / Math.log(4));
            int coolingTier = (int) Math.ceil(Math.log(coolerEU / 8d) / Math.log(4));

            // calculate overclock level of inline cooling
            int coolingOverclock = forgingTier - coolingTier;

            coolerDuration = property.getVacuumDurationOverride();
            if (coolerDuration == -1) {
                coolerDuration = (int) (material.getMass() * 3);
            }
            coolerDuration *= inputAmount
                    // inefficiency - square root of temp/1000
                    // aka, increasing temp by x4 increases cooling time by x2; perfect efficiency at 1000K
                    * Math.max(1, Math.pow(material.getBlastTemperature() / 1000.0, 1 / 2.0))
                    // overclocking; do not allow underclocks.
                    / Math.pow(OverclockingLogic.STANDARD_OVERCLOCK_DURATION_DIVISOR, Math.max(0, coolingOverclock));

            // add liquid helium if necessary
            if (material.getBlastTemperature() >= 5000) {
                builder = builder
                        .fluidInputs(Materials.Helium.getFluid(FluidStorageKeys.LIQUID, 500 * inputAmount))
                        .fluidOutputs(Materials.Helium.getFluid(250 * inputAmount));
            }
        }

        // nonconsumable fragment of graphite to prevent recipe conflict between gas and non-gas

        // build the gas recipe if it exists
        if (property.getGasTier() != null) {
            FluidStack gas = CraftingComponent.EBF_GASES.get(property.getGasTier());
            builder.copy().circuitMeta(circuitMeta)
                    .notConsumable(OrePrefix.dustSmall, Materials.Graphite)
                    .fluidInputs(new FluidStack(gas, gas.amount * inputAmount))
                    .duration((int) (duration * 0.67) + coolerDuration)
                    .buildAndRegister();
        }

        // build the non-gas recipe
        builder.circuitMeta(circuitMeta)
                .notConsumable(OrePrefix.dustTiny, Materials.Graphite)
                .duration(duration + coolerDuration)
                .buildAndRegister();
    }
}
