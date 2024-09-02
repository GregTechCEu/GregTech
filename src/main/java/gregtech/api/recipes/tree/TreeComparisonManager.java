package gregtech.api.recipes.tree;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.recipes.tree.property.PropertySet;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;

import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static gregtech.api.GTValues.*;
import static gregtech.api.unification.material.Materials.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static gregtech.common.metatileentities.MetaTileEntities.*;

public final class TreeComparisonManager {

    private static RecipeTree ASSEMBLER;
    private static RecipeTree EBF;
    private static RecipeTree ASSEMBLY_LINE;
    private static RecipeTree PYROLYSE;

    private static final int WARMUP = 100;
    private static final int RECORDING = 1000;
    private static final int REPETITIONS = WARMUP + RECORDING;

    // this ends up in the run directory
    private static final Path path = Paths.get("TreeComparisonReport.csv");

    public static void run() {
        setup();
        assemblerLookup();
        ebfLookup();
    }

    private static void setup() {
        ASSEMBLER = new RecipeTree();
        RecipeMaps.ASSEMBLER_RECIPES.getLookup().getRecipes(false).forEach(r -> ASSEMBLER.addRecipe(r));
        EBF = new RecipeTree();
        RecipeMaps.BLAST_RECIPES.getLookup().getRecipes(false).forEach(r -> EBF.addRecipe(r));
        ASSEMBLY_LINE = new RecipeTree();
        RecipeMaps.ASSEMBLY_LINE_RECIPES.getLookup().getRecipes(false).forEach(r -> ASSEMBLY_LINE.addRecipe(r));
        PYROLYSE = new RecipeTree();
        RecipeMaps.PYROLYSE_RECIPES.getLookup().getRecipes(false).forEach(r -> PYROLYSE.addRecipe(r));
        RecipeTree.rebuildRecipeTrees();
        try {
            Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException ignored) {}
    }

    private static void assemblerLookup() {

        List<LookupHelper> helpers = new ObjectArrayList<>();
        LookupHelper helper = new LookupHelper("Assembler", "insufficient", RecipeMaps.ASSEMBLER_RECIPES, ASSEMBLER, V[UV]);
        helper.add(HI_AMP_TRANSFORMER[UV].getStackForm());
        helper.add(ENERGY_INPUT_HATCH_4A[5].getStackForm(1));
        helper.add(wireGtOctal, Europium, 2);
        helper.add(plate, Neutronium, 4);
        helpers.add(helper);

        helper = helper.newInstance("exact");
        helper.add(HI_AMP_TRANSFORMER[UV].getStackForm());
        helper.add(ENERGY_INPUT_HATCH_4A[5].getStackForm(2));
        helper.add(wireGtOctal, Europium, 2);
        helper.add(plate, Neutronium, 4);
        helpers.add(helper);

        helper = helper.newInstance("excessive");
        helper.add(HI_AMP_TRANSFORMER[UV].getStackForm(3));
        helper.add(ENERGY_INPUT_HATCH_4A[5].getStackForm(32));
        helper.add(wireGtOctal, Europium, 19);
        helper.add(plate, Neutronium, 64);
        helpers.add(helper);

        helper = helper.newInstance("inapplicable");
        helper.voltage = V[HV];
        helper.add(new ItemStack(Blocks.WOODEN_BUTTON, 34));
        helper.add(pipeQuadrupleFluid, Neutronium, 10);
        helper.add(Water.getFluid(488));
        helpers.add(helper);

        helper = helper.newInstance("exact");
        helper.add(dust, GalliumArsenide);
        helper.add(wireFine, Platinum, 8);
        helper.add(Polyethylene.getFluid(L * 2));
        helpers.add(helper);

        helper = helper.newInstance("overlap");
        helper.add(HI_AMP_TRANSFORMER[UV].getStackForm());
        helper.add(ENERGY_INPUT_HATCH_4A[5].getStackForm(2));
        helper.add(wireGtOctal, Europium, 2);
        helper.add(plate, Neutronium, 4);
        helper.add(dust, GalliumArsenide);
        helper.add(wireFine, Platinum, 8);
        helper.add(Polyethylene.getFluid(L * 2));
        helpers.add(helper);

        helper = helper.newInstance("missing");
        helper.voltage = 32;
        helper.add(rotor, Titanium, 2);
        helper.add(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE));
        helpers.add(helper);

        helper = helper.newInstance("exact");
        helper.add(rotor, Titanium, 2);
        helper.add(pipeNormalFluid, Titanium, 4);
        helper.add(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE));
        helper.add(MetaBlocks.MULTIBLOCK_CASING.getItemVariant(BlockMultiblockCasing.MultiblockCasingType.ENGINE_INTAKE_CASING, 2));
        helpers.add(helper);

        helper = helper.newInstance("extra");
        helper.add(rotor, Titanium, 2);
        helper.add(pipeNormalFluid, Titanium, 4);
        helper.add(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE));
        helper.add(MetaBlocks.MULTIBLOCK_CASING.getItemVariant(BlockMultiblockCasing.MultiblockCasingType.ENGINE_INTAKE_CASING, 2));
        helper.add(SolderingAlloy.getFluid(200));
        helper.add(new ItemStack(Blocks.WOODEN_BUTTON, 34));
        helper.add(HI_AMP_TRANSFORMER[LuV].getStackForm(4));
        helper.add(new ItemStack(Blocks.STONE, 10, 10));
        helper.add(OreDictUnifier.get(bolt, Steel, 8));
        helpers.add(helper);

        for (int i = 0; i < REPETITIONS; i++) {
            for (LookupHelper helperr : helpers) {
                helperr.lookup(i);
            }
        }
    }

    private static void ebfLookup() {
        String name = "EBF";

        List<LookupHelper> helpers = new ObjectArrayList<>();
        LookupHelper helper = new LookupHelper("EBF", "insufficient", RecipeMaps.BLAST_RECIPES, EBF, V[MV]);
        helper.add(dust, WroughtIron);
        helper.add(2);
        helper.add(Oxygen.getFluid(100));
        helpers.add(helper);

        helper = helper.newInstance("exact");
        helper.add(dust, WroughtIron);
        helper.add(Oxygen.getFluid(200));
        helper.add(2);
        helpers.add(helper);
        
        helper = helper.newInstance("excessive");
        helper.add(dust, WroughtIron, 64);
        helper.add(2);
        helper.add(Oxygen.getFluid(99999));
        helpers.add(helper);

        helper = helper.newInstance("inapplicable", V[HV]);
        helper.add(dust, Ash, 4);
        helper.add(ingot, Neutronium, 32);
        helper.add(new ItemStack(Blocks.WOODEN_BUTTON, 34));
        helper.add(Water.getFluid(2000));
        helper.add(Neutronium.getFluid(1));
        helpers.add(helper);

        helper = helper.newInstance("exact");
        helper.add(dust, Magnesium, 2);
        helper.add(TitaniumTetrachloride.getFluid(1000));
        helpers.add(helper);

        helper = helper.newInstance("overlap");
        helper.add(dust, Magnesium, 2);
        helper.add(TitaniumTetrachloride.getFluid(1000));
        helper.add(dust, WroughtIron);
        helper.add(Oxygen.getFluid(200));
        helper.add(2);
        helpers.add(helper);

        helper = helper.newInstance("missing", V[HV]);
        helper.add(dust, FerriteMixture);
        helpers.add(helper);

        helper = helper.newInstance("exact");
        helper.add(dust, FerriteMixture);
        helper.add(Oxygen.getFluid(2000));
        helpers.add(helper);

        helper = helper.newInstance("extra");
        helper.add(dust, FerriteMixture);
        helper.add(Oxygen.getFluid(2000));
        helper.add(dust, Ash, 4);
        helper.add(ingot, Neutronium, 32);
        helper.add(new ItemStack(Blocks.WOODEN_BUTTON, 34));
        helper.add(Water.getFluid(2000));
        helper.add(Neutronium.getFluid(1));
        helper.add(Steam.getFluid(100));
        helpers.add(helper);


        for (int i = 0; i < REPETITIONS; i++) {
            for (LookupHelper helperr : helpers) {
                helperr.lookup(i);
            }
        }
    }

    private static void assemblyLineLookup() {
        String name = "Assembly Line";

//        ASSEMBLY_LINE_RECIPES.recipeBuilder()
//                .input(stickLong, SamariumMagnetic)
//                .input(stickLong, HSSS, 2)
//                .input(ring, HSSS, 2)
//                .input(round, HSSS, 4)
//                .input(wireFine, Ruridit, 64)
//                .input(cableGtSingle, NiobiumTitanium, 2)
//                .fluidInputs(SolderingAlloy.getFluid(L))
//                .fluidInputs(Lubricant.getFluid(250))
//                .output(ELECTRIC_MOTOR_LuV)
//                .scannerResearch(ELECTRIC_MOTOR_IV.getStackForm())
//                .duration(600).EUt(6000).buildAndRegister();

//        ASSEMBLY_LINE_RECIPES.recipeBuilder()
//                .inputs(FUSION_CASING.getItemVariant(FUSION_COIL))
//                .input(circuit, MarkerMaterials.Tier.UHV, 4)
//                .input(QUANTUM_STAR)
//                .input(plateDouble, Americium)
//                .input(FIELD_GENERATOR_ZPM, 2)
//                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
//                .input(ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT, 64)
//                .input(wireGtSingle, EnrichedNaquadahTriniumEuropiumDuranide, 32)
//                .fluidInputs(SolderingAlloy.getFluid(L * 8))
//                .fluidInputs(YttriumBariumCuprate.getFluid(L * 8))
//                .outputs(FUSION_REACTOR[2].getStackForm())
//                .stationResearch(b -> b
//                        .researchStack(FUSION_REACTOR[1].getStackForm())
//                        .CWUt(96)
//                        .EUt(VA[UV]))
//                .duration(1000).EUt(VA[ZPM]).buildAndRegister();

//        ASSEMBLY_LINE_RECIPES.recipeBuilder()
//                .input(ITEM_IMPORT_BUS[ZPM])
//                .input(EMITTER_LuV, 8)
//                .input(circuit, MarkerMaterials.Tier.ZPM)
//                .input(ROBOT_ARM_ZPM, 2)
//                .input(ELECTRIC_MOTOR_ZPM, 2)
//                .input(wireGtDouble, UraniumRhodiumDinaquadide, 16)
//                .input(OPTICAL_PIPES[0], 2)
//                .fluidInputs(SolderingAlloy.getFluid(L * 4))
//                .fluidInputs(Polybenzimidazole.getFluid(L * 2))
//                .output(OBJECT_HOLDER)
//                .scannerResearch(b -> b
//                        .researchStack(ITEM_IMPORT_BUS[ZPM].getStackForm())
//                        .duration(2400)
//                        .EUt(VA[IV]))
//                .duration(1200).EUt(100000).buildAndRegister();
    }

    private static void pyrolyseLookup() {
        String name = "Pyrolyse";

//        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(9)
//                .input(log, Wood, 16)
//                .outputs(new ItemStack(Items.COAL, 20, 1))
//                .fluidOutputs(WoodTar.getFluid(1500))
//                .duration(640).EUt(64)
//                .buildAndRegister();

//        PYROLYSE_RECIPES.recipeBuilder().circuitMeta(22)
//                .input(gem, Coal, 16)
//                .fluidInputs(Steam.getFluid(1000))
//                .output(gem, Coke, 16)
//                .fluidOutputs(CoalGas.getFluid(4000))
//                .duration(320).EUt(96)
//                .buildAndRegister();

//        PYROLYSE_RECIPES.recipeBuilder().EUt(10).duration(200)
//                .input(BIO_CHAFF)
//                .circuitMeta(2)
//                .fluidInputs(Water.getFluid(1500))
//                .fluidOutputs(FermentedBiomass.getFluid(1500))
//                .buildAndRegister();
    }

    private static void report(String name, String type, int items, int fluids, long[] nsMap, long[] nsTree) {
        try (var writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
            writer.write(name + " MAP " + type + " w/ " + items + " items & " + fluids + " fluids," + StringUtils.join(nsMap, ',') + "\n");
            writer.write(name + " TREE " + type + " w/ " + items + " items & " + fluids + " fluids," + StringUtils.join(nsTree, ',') + "\n");
        } catch (IOException ignored) {}
    }

    private static class LookupHelper {
        long voltage;
        String name;
        String type;
        RecipeMap<?> map;
        RecipeTree tree;

        List<ItemStack> itemInputs = new ObjectArrayList<>();
        List<FluidStack> fluidInputs = new ObjectArrayList<>();

        long[] nsMap = new long[RECORDING];
        long[] nsTree = new long[RECORDING];

        public LookupHelper(String name, String type, RecipeMap<?> map, RecipeTree tree, long voltage) {
            this.name = name;
            this.map = map;
            this.tree = tree;
            this.type = type;
            this.voltage = voltage;
        }

        public LookupHelper newInstance(String type) {
            return new LookupHelper(name, type, map, tree, voltage);
        }

        public LookupHelper newInstance(String type, long voltage) {
            return new LookupHelper(name, type, map, tree, voltage);
        }

        void add(int circuit) {
            itemInputs.add(IntCircuitIngredient.getIntegratedCircuit(circuit));
        }

        void add(ItemStack item) {
            itemInputs.add(item);
        }

        void add(OrePrefix orePrefix, Material material) {
            add(OreDictUnifier.get(orePrefix, material));
        }

        void add(OrePrefix orePrefix, Material material, int stackSize) {
            add(OreDictUnifier.get(orePrefix, material, stackSize));
        }

        void add(FluidStack fluid) {
            fluidInputs.add(fluid);
        }

        public void lookup(int i) {
            Collections.shuffle(itemInputs);
            Collections.shuffle(fluidInputs);
            long start = System.nanoTime();
            Recipe recipe = map.findRecipe(voltage, itemInputs, fluidInputs);
            // mimic RecipeTree's need to create an iterator.
            Iterator<Recipe> iter = new SingletonIterator<>(recipe);
            long finish = System.nanoTime();
            if (i >= WARMUP) nsMap[i - WARMUP] = GTUtility.safeCastLongToInt(finish - start);
            start = System.nanoTime();
            iter = tree.findRecipes(itemInputs, fluidInputs, PropertySet.of(voltage, itemInputs));
            finish = System.nanoTime();
            if (i >= WARMUP) nsTree[i - WARMUP] = GTUtility.safeCastLongToInt(finish - start);
            if (i == 0) {
                boolean oneMatch = false;
                while (iter.hasNext() && !oneMatch) {
                    if (recipe == iter.next()) oneMatch = true;
                }
                if (!oneMatch && recipe != null)
                    throw new AssertionError("Tree was not equivalent to Map on run " + name + " " + type);
            } else if (i == REPETITIONS - 1) {
                report(name, type, itemInputs.size(), fluidInputs.size(), nsMap, nsTree);

            }
        }
    }

    private static class SingletonIterator<T> implements Iterator<T> {

        private T element;

        public SingletonIterator(T element) {
            this.element = element;
        }

        @Override
        public boolean hasNext() {
            return element != null;
        }

        @Override
        public T next() {
            T e = element;
            element = null;
            return e;
        }
    }
}
