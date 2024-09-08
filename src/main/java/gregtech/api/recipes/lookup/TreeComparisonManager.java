package gregtech.api.recipes.lookup;

import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;

import gregtech.api.recipes.ingredients.old.IntCircuitIngredient;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;

import gregtech.common.blocks.BlockFusionCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockMultiblockCasing;
import gregtech.common.blocks.MetaBlocks;

import gregtech.common.items.MetaItems;

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

    private static RecipeLookup ASSEMBLER;
    private static RecipeLookup EBF;
    private static RecipeLookup ASSEMBLY_LINE;
    private static RecipeLookup MACERATOR;

    private static final int WARMUP = 100;
    private static final int RECORDING = 1000;
    private static final int REPETITIONS = WARMUP + RECORDING;

    // this ends up in the run directory
    private static final Path path = Paths.get("TreeComparisonReport.csv");

    public static void run() {
        setup();
        assemblerLookup();
        ebfLookup();
        assemblyLineLookup();
        maceratorLookup();
    }

    private static void setup() {
        ASSEMBLER = new RecipeLookup();
        RecipeMaps.ASSEMBLER_RECIPES.getLookup().getRecipes(false).forEach(r -> ASSEMBLER.addRecipe(r));
        EBF = new RecipeLookup();
        RecipeMaps.BLAST_RECIPES.getLookup().getRecipes(false).forEach(r -> EBF.addRecipe(r));
        ASSEMBLY_LINE = new RecipeLookup();
        RecipeMaps.ASSEMBLY_LINE_RECIPES.getLookup().getRecipes(false).forEach(r -> ASSEMBLY_LINE.addRecipe(r));
        MACERATOR = new RecipeLookup();
        RecipeMaps.MACERATOR_RECIPES.getLookup().getRecipes(false).forEach(r -> MACERATOR.addRecipe(r));
        RecipeLookup.rebuildRecipeLookups();
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

        List<LookupHelper> helpers = new ObjectArrayList<>();
        LookupHelper helper = new LookupHelper("Assembly Line", "insufficient", RecipeMaps.ASSEMBLY_LINE_RECIPES, ASSEMBLY_LINE, 6000);
        helper.add(stickLong, SamariumMagnetic);
        helper.add(stickLong, HSSS, 2);
        helper.add(ring, HSSS, 2);
        helper.add(round, HSSS, 4);
        helper.add(wireFine, Ruridit, 32);
        helper.add(cableGtSingle, NiobiumTitanium, 1);
        helper.add(SolderingAlloy.getFluid(2));
        helper.add(Lubricant.getFluid(200));
        helpers.add(helper);

        helper = helper.newInstance("exact");
        helper.add(stickLong, SamariumMagnetic);
        helper.add(stickLong, HSSS, 2);
        helper.add(ring, HSSS, 2);
        helper.add(round, HSSS, 4);
        helper.add(wireFine, Ruridit, 64);
        helper.add(cableGtSingle, NiobiumTitanium, 2);
        helper.add(SolderingAlloy.getFluid(L));
        helper.add(Lubricant.getFluid(250));
        helpers.add(helper);

        helper = helper.newInstance("excessive");
        helper.add(stickLong, SamariumMagnetic, 50);
        helper.add(stickLong, HSSS, 20);
        helper.add(ring, HSSS, 22);
        helper.add(round, HSSS, 41);
        helper.add(wireFine, Ruridit, 64);
        helper.add(cableGtSingle, NiobiumTitanium, 23);
        helper.add(SolderingAlloy.getFluid(2000));
        helper.add(Lubricant.getFluid(8000));
        helpers.add(helper);

        helper = helper.newInstance("inapplicable", V[ZPM]);
        helper.add(dust, FerriteMixture);
        helper.add(Oxygen.getFluid(2000));
        helper.add(dust, Ash, 4);
        helper.add(ingot, Neutronium, 32);
        helper.add(new ItemStack(Blocks.WOODEN_BUTTON, 34));
        helper.add(Water.getFluid(2000));
        helper.add(Neutronium.getFluid(1));
        helper.add(Steam.getFluid(100));
        helpers.add(helper);


        helper = helper.newInstance("exact");
        helper.add(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL));
        helper.add(circuit, MarkerMaterials.Tier.UHV, 4);
        helper.add(MetaItems.QUANTUM_STAR.getStackForm());
        helper.add(plateDouble, Americium);
        helper.add(MetaItems.FIELD_GENERATOR_ZPM.getStackForm(2));
        helper.add(MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(64));
        helper.add(MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(64));
        helper.add(wireGtSingle, EnrichedNaquadahTriniumEuropiumDuranide, 32);
        helper.add(SolderingAlloy.getFluid(L * 8));
        helper.add(YttriumBariumCuprate.getFluid(L * 8));
        helpers.add(helper);


        helper = helper.newInstance("overlap");
        helper.add(MetaBlocks.FUSION_CASING.getItemVariant(BlockFusionCasing.CasingType.FUSION_COIL));
        helper.add(circuit, MarkerMaterials.Tier.UHV, 4);
        helper.add(MetaItems.QUANTUM_STAR.getStackForm());
        helper.add(plateDouble, Americium);
        helper.add(MetaItems.FIELD_GENERATOR_ZPM.getStackForm(2));
        helper.add(MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(64));
        helper.add(MetaItems.ULTRA_HIGH_POWER_INTEGRATED_CIRCUIT.getStackForm(64));
        helper.add(wireGtSingle, EnrichedNaquadahTriniumEuropiumDuranide, 32);
        helper.add(SolderingAlloy.getFluid(L * 8));
        helper.add(YttriumBariumCuprate.getFluid(L * 8));
        helper.add(stickLong, SamariumMagnetic);
        helper.add(stickLong, HSSS, 2);
        helper.add(ring, HSSS, 2);
        helper.add(round, HSSS, 4);
        helper.add(wireFine, Ruridit, 64);
        helper.add(cableGtSingle, NiobiumTitanium, 2);
        helper.add(SolderingAlloy.getFluid(L));
        helper.add(Lubricant.getFluid(250));
        helpers.add(helper);

        helper = helper.newInstance("missing", 100000);
        helper.add(MetaItems.EMITTER_LuV.getStackForm(8));
        helper.add(MetaItems.ROBOT_ARM_ZPM.getStackForm(2));
        helper.add(MetaItems.ELECTRIC_MOTOR_ZPM.getStackForm(2));
        helper.add(new ItemStack(MetaBlocks.OPTICAL_PIPES[0], 2));
        helper.add(SolderingAlloy.getFluid(L * 4));
        helper.add(Polybenzimidazole.getFluid(L * 2));
        helpers.add(helper);

        helper = helper.newInstance("exact");
        helper.add(ITEM_IMPORT_BUS[ZPM].getStackForm());
        helper.add(MetaItems.EMITTER_LuV.getStackForm(8));
        helper.add(circuit, MarkerMaterials.Tier.ZPM);
        helper.add(MetaItems.ROBOT_ARM_ZPM.getStackForm(2));
        helper.add(MetaItems.ELECTRIC_MOTOR_ZPM.getStackForm(2));
        helper.add(wireGtDouble, UraniumRhodiumDinaquadide, 16);
        helper.add(new ItemStack(MetaBlocks.OPTICAL_PIPES[0], 2));
        helper.add(SolderingAlloy.getFluid(L * 4));
        helper.add(Polybenzimidazole.getFluid(L * 2));
        helpers.add(helper);

        helper = helper.newInstance("extra");
        helper.add(ITEM_IMPORT_BUS[ZPM].getStackForm());
        helper.add(MetaItems.EMITTER_LuV.getStackForm(8));
        helper.add(circuit, MarkerMaterials.Tier.ZPM);
        helper.add(MetaItems.ROBOT_ARM_ZPM.getStackForm(2));
        helper.add(MetaItems.ELECTRIC_MOTOR_ZPM.getStackForm(2));
        helper.add(wireGtDouble, UraniumRhodiumDinaquadide, 16);
        helper.add(new ItemStack(MetaBlocks.OPTICAL_PIPES[0], 2));
        helper.add(SolderingAlloy.getFluid(L * 4));
        helper.add(Polybenzimidazole.getFluid(L * 2));
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

    private static void maceratorLookup() {
        List<LookupHelper> helpers = new ObjectArrayList<>();
        LookupHelper helper = new LookupHelper("Macerator", "exact", RecipeMaps.MACERATOR_RECIPES, MACERATOR, V[UV]);
        helper.add(ARC_FURNACE[5].getStackForm());
        helpers.add(helper);

        helper = helper.newInstance("excessive");
        helper.add(FLUID_SOLIDIFIER[5].getStackForm(64));
        helpers.add(helper);

        helper = helper.newInstance("inapplicable");
        helper.add(dust, Ash);
        helpers.add(helper);

        helper = helper.newInstance("overlap");
        helper.add(ARC_FURNACE[5].getStackForm());
        helper.add(FLUID_SOLIDIFIER[5].getStackForm());
        helper.add(FORGE_HAMMER[5].getStackForm());
        helpers.add(helper);

        helper = helper.newInstance("extra");
        helper.add(LATHE[5].getStackForm());
        helper.add(dust, Ash ,5);
        helper.add(dust, DarkAsh, 10);
        helper.add(dust, SodaAsh, 15);
        helper.add(dust, Potash, 20);

        for (int i = 0; i < REPETITIONS; i++) {
            for (LookupHelper helperr : helpers) {
                helperr.lookup(i);
            }
        }
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
        RecipeLookup tree;

        List<ItemStack> itemInputs = new ObjectArrayList<>();
        List<FluidStack> fluidInputs = new ObjectArrayList<>();

        long[] nsMap = new long[RECORDING];
        long[] nsTree = new long[RECORDING];

        public LookupHelper(String name, String type, RecipeMap<?> map, RecipeLookup tree, long voltage) {
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
            // mimic RecipeLookup's need to create an iterator.
            Iterator<Recipe> iter = new SingletonIterator<>(recipe);
            long finish = System.nanoTime();
            if (i >= WARMUP) nsMap[i - WARMUP] = GTUtility.safeCastLongToInt(finish - start);
            start = System.nanoTime();
            iter = tree.findRecipes(itemInputs, fluidInputs, PropertySet.empty().circuits(itemInputs));
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
