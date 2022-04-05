package gregtech.api.recipes.logic;

import gregtech.Bootstrap;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.api.util.world.DummyWorld;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityElectricBlastFurnace;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class IParallelableRecipeLogicTest implements IParallelableRecipeLogic {

    private static final ItemStackHashStrategy hashStrategy = ItemStackHashStrategy.comparingAll();
    private static RecipeMapMultiblockController mbt;
    private static MetaTileEntityItemBus importItemBus;
    private static MetaTileEntityItemBus exportItemBus;
    private static MetaTileEntityFluidHatch importFluidBus;
    private static MetaTileEntityFluidHatch exportFluidBus;
    private static boolean enableBonusOverride = false;

    @BeforeClass
    public static void bootstrap() {
        Bootstrap.perform();
    }

    private static ResourceLocation gregtechId(String name) {
        return new ResourceLocation(GTValues.MODID, name);
    }

    private MetaTileEntityElectricBlastFurnace initEBF(int id) {

        World world = DummyWorld.INSTANCE;


        mbt = MetaTileEntities.registerMetaTileEntity(id,
                new MetaTileEntityElectricBlastFurnace(
                        // super function calls the world, which equal null in test
                        new ResourceLocation(GTValues.MODID, "electric_blast_furnace")) {

                    @Override
                    public boolean canBeDistinct() {
                        return false;
                    }

                    @Override
                    public void reinitializeStructurePattern() {

                    }

                    // function checks for the temperature of the recipe against the coils
                    @Override
                    public boolean checkRecipe(@Nonnull Recipe recipe, boolean consumeIfSuccess) {
                        return true;
                    }

                    // ignore maintenance problems
                    @Override
                    public boolean hasMaintenanceMechanics() {
                        return false;
                    }
                });

        try {
            Field field = MetaTileEntityElectricBlastFurnace.class.getSuperclass().getDeclaredField("recipeMapWorkable");
            field.setAccessible(true);

            Object recipeMapWorkableField = field.get(mbt);
            Method setParallelLimitMethod = recipeMapWorkableField.getClass().getSuperclass().getSuperclass().getDeclaredMethod("setParallelLimit", int.class);
            setParallelLimitMethod.setAccessible(true);

            setParallelLimitMethod.invoke(recipeMapWorkableField, 4);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        //isValid() check in the dirtying logic requires both a metatileentity and a holder
        try {
            Field field = MetaTileEntity.class.getDeclaredField("holder");
            field.setAccessible(true);
            field.set(mbt, new MetaTileEntityHolder());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            Field field = MetaTileEntityHolder.class.getDeclaredField("metaTileEntity");
            field.setAccessible(true);
            field.set(mbt.getHolder(), mbt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        ((MetaTileEntityHolder) mbt.getHolder()).setWorld(world);

        //Controller and isAttachedToMultiBlock need the world so we fake it here.
        importItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, false) {
            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        exportItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, true) {
            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        importFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.lv"), 1, false) {
            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        exportFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.export.lv"), 1, true) {
            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };

        //Controller is a private field but we need that information
        try {
            Field field = MetaTileEntityMultiblockPart.class.getDeclaredField("controllerTile");
            field.setAccessible(true);
            field.set(importItemBus, mbt);
            field.set(exportItemBus, mbt);
            field.set(importFluidBus, mbt);
            field.set(exportFluidBus, mbt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return (MetaTileEntityElectricBlastFurnace) mbt;
    }

    @Test
    public void findMultipliedRecipe_AtMaxParallelsTest() {

        MetaTileEntityElectricBlastFurnace EBF = initEBF(511);

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();


        // Initially populate the input bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        RecipeBuilder<?> parallelRecipe = findMultipliedParallelRecipe(map, recipe, importItemBus.getImportItems(), importFluidBus.getImportFluids(),
                exportItemBus.getExportItems(), exportFluidBus.getExportFluids(), parallelLimit, Integer.MAX_VALUE, EBF);

        //Check if the correct number of parallels were done
        assertEquals(4, parallelRecipe.getParallel());

        //Check that the EUt of the recipe was multiplied correctly
        assertEquals(120, parallelRecipe.getEUt());

        //Check if the recipe duration was not modified
        assertEquals(100, parallelRecipe.getDuration());

        //Check the recipe outputs
        assertFalse(parallelRecipe.getOutputs().isEmpty());

        assertTrue(hashStrategy.equals(new ItemStack(Blocks.STONE, 4), parallelRecipe.getOutputs().get(0)));

        //Check the recipe inputs
        //assertEquals(CountableIngredient.from(new ItemStack(Blocks.COBBLESTONE), 4), parallelRecipe.getInputs().get(0));

    }

    @Test
    public void findMultipliedRecipe_LessThanMaxParallelsTest() {

        MetaTileEntityElectricBlastFurnace EBF = initEBF(512);

        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();


        // Initially populate the input bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 2), false);

        RecipeBuilder<?> parallelRecipe = findMultipliedParallelRecipe(map, recipe, importItemBus.getImportItems(), importFluidBus.getImportFluids(),
                exportItemBus.getExportItems(), exportFluidBus.getExportFluids(), parallelLimit, Integer.MAX_VALUE, EBF);

        //Check if the correct number of parallels were done
        assertEquals(2, parallelRecipe.getParallel());

        //Check that the EUt of the recipe was multiplied correctly
        assertEquals(60, parallelRecipe.getEUt());

        //Check if the recipe duration was not modified
        assertEquals(100, parallelRecipe.getDuration());

        //Check the recipe outputs
        assertFalse(parallelRecipe.getOutputs().isEmpty());

        assertTrue(hashStrategy.equals(new ItemStack(Blocks.STONE, 2), parallelRecipe.getOutputs().get(0)));

        //Check the recipe inputs
        //assertEquals(CountableIngredient.from(new ItemStack(Blocks.COBBLESTONE), 4), parallelRecipe.getInputs().get(0));

    }

    @Test
    public void findAppendedParallelItemRecipe_AtMaxParallelsTest() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(513);


        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .buildAndRegister();


        // Initially populate the input bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        RecipeBuilder<?> parallelRecipe = findAppendedParallelItemRecipe(map, importItemBus.getImportItems(),
                exportItemBus.getExportItems(), parallelLimit, 120, EBF);

        //Check if the correct number of parallels were done
        assertEquals(4, parallelRecipe.getParallel());

        //Check that the EUt of the recipe was not modified
        assertEquals(30, parallelRecipe.getEUt());

        //Check if the recipe duration was multiplied correctly
        assertEquals(400, parallelRecipe.getDuration());

        //Check the recipe outputs
        assertFalse(parallelRecipe.getOutputs().isEmpty());

        assertTrue(hashStrategy.equals(new ItemStack(Blocks.STONE, 4), parallelRecipe.getOutputs().get(0)));

    }

    @Test
    public void findAppendedParallelItemRecipe_LessThanMaxParallelsTest() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(514);


        int parallelLimit = 4;

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .buildAndRegister();


        // Initially populate the input bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 2), false);

        RecipeBuilder<?> parallelRecipe = findAppendedParallelItemRecipe(map, importItemBus.getImportItems(),
                exportItemBus.getExportItems(), parallelLimit, 120, EBF);

        //Check if the correct number of parallels were done
        assertEquals(2, parallelRecipe.getParallel());

        //Check that the EUt of the recipe was not modified
        assertEquals(30, parallelRecipe.getEUt());

        //Check if the recipe duration was multiplied correctly
        assertEquals(200, parallelRecipe.getDuration());

        //Check the recipe outputs
        assertFalse(parallelRecipe.getOutputs().isEmpty());

        assertTrue(hashStrategy.equals(new ItemStack(Blocks.STONE, 2), parallelRecipe.getOutputs().get(0)));

    }

    // An end to end test for finding parallel recipes
    @Test
    public void findParallelRecipe_Test() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(515);

        int parallelLimit = 4;

        MultiblockRecipeLogic mrl = new MultiblockRecipeLogic(mbt) {
            @Override
            protected long getEnergyStored() {
                return Long.MAX_VALUE;
            }

            @Override
            protected long getEnergyCapacity() {
                return Long.MAX_VALUE;
            }

            @Override
            protected boolean drawEnergy(int recipeEUt, boolean simulate) {
                return true;
            }

            @Override
            public long getMaxVoltage() {
                return 32;
            }

            @Override
            public MetaTileEntity getMetaTileEntity() {
                return EBF;
            }
        };

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();


        // Initially populate the input bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);


        Recipe outputRecipe = findParallelRecipe(mrl, recipe, importItemBus.getImportItems(), importFluidBus.getImportFluids(), exportItemBus.getExportItems(),
                exportFluidBus.getExportFluids(), 128, parallelLimit);


        //Check that the EUt of the recipe was multiplied correctly
        assertEquals(120, outputRecipe.getEUt());

        //Check if the recipe duration was not modified
        assertEquals(100, outputRecipe.getDuration());

        //Check the recipe outputs
        assertFalse(outputRecipe.getOutputs().isEmpty());

        assertTrue(hashStrategy.equals(new ItemStack(Blocks.STONE, 4), outputRecipe.getOutputs().get(0)));

    }

    // An end to end test for finding parallel recipes
    @Test
    public void findParallelRecipe_FailingFromInputTest() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(516);

        int parallelLimit = 4;

        MultiblockRecipeLogic mrl = new MultiblockRecipeLogic(mbt) {
            @Override
            protected long getEnergyStored() {
                return Long.MAX_VALUE;
            }

            @Override
            protected long getEnergyCapacity() {
                return Long.MAX_VALUE;
            }

            @Override
            protected boolean drawEnergy(int recipeEUt, boolean simulate) {
                return true;
            }

            @Override
            public long getMaxVoltage() {
                return 32;
            }

            @Override
            public MetaTileEntity getMetaTileEntity() {
                return EBF;
            }
        };

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();


        // Don't populate the input bus, so the recipe will fail
        //importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);


        Recipe outputRecipe = findParallelRecipe(mrl, recipe, importItemBus.getImportItems(), importFluidBus.getImportFluids(), exportItemBus.getExportItems(),
                exportFluidBus.getExportFluids(), 32, parallelLimit);

        assertNull(outputRecipe);
    }

    // An end to end test for finding parallel recipes
    @Test
    public void findParallelRecipe_FailingFromOutputTest() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(517);

        int parallelLimit = 4;

        MultiblockRecipeLogic mrl = new MultiblockRecipeLogic(mbt) {
            @Override
            protected long getEnergyStored() {
                return Long.MAX_VALUE;
            }

            @Override
            protected long getEnergyCapacity() {
                return Long.MAX_VALUE;
            }

            @Override
            protected boolean drawEnergy(int recipeEUt, boolean simulate) {
                return true;
            }

            @Override
            public long getMaxVoltage() {
                return 32;
            }

            @Override
            public MetaTileEntity getMetaTileEntity() {
                return EBF;
            }
        };

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();

        // Saturate the export bus
        exportItemBus.getExportItems().insertItem(0, new ItemStack(Blocks.BONE_BLOCK, 16), false);
        exportItemBus.getExportItems().insertItem(1, new ItemStack(Blocks.BONE_BLOCK, 16), false);
        exportItemBus.getExportItems().insertItem(2, new ItemStack(Blocks.BONE_BLOCK, 16), false);
        exportItemBus.getExportItems().insertItem(3, new ItemStack(Blocks.BONE_BLOCK, 16), false);


        Recipe outputRecipe = findParallelRecipe(mrl, recipe, importItemBus.getImportItems(), importFluidBus.getImportFluids(), exportItemBus.getExportItems(),
                exportFluidBus.getExportFluids(), Integer.MAX_VALUE, parallelLimit);

        assertNull(outputRecipe);
    }

    @Test
    public void applyParallelBonus_Test() {
        MetaTileEntityElectricBlastFurnace EBF = initEBF(518);

        int parallelLimit = 4;

        enableBonusOverride = true;

        MultiblockRecipeLogic mrl = new MultiblockRecipeLogic(mbt) {
            @Override
            protected long getEnergyStored() {
                return Long.MAX_VALUE;
            }

            @Override
            protected long getEnergyCapacity() {
                return Long.MAX_VALUE;
            }

            @Override
            protected boolean drawEnergy(int recipeEUt, boolean simulate) {
                return true;
            }

            @Override
            public long getMaxVoltage() {
                return 32;
            }

            @Override
            public MetaTileEntity getMetaTileEntity() {
                return EBF;
            }
        };

        // Create a recipe Map to be used for testing
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("electric_blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder(),
                false);

        // Create a simple recipe to be used for testing
        Recipe recipe = map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .blastFurnaceTemp(1000)
                .EUt(30).duration(100)
                .build().getResult();


        // Initially populate the input bus
        importItemBus.getImportItems().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        Recipe outputRecipe = findParallelRecipe(mrl, recipe, importItemBus.getImportItems(), importFluidBus.getImportFluids(), exportItemBus.getExportItems(),
                exportFluidBus.getExportFluids(), 128, parallelLimit);


        //Check that the EUt of the recipe was not modified
        assertEquals(1, outputRecipe.getEUt());

        //Check if the recipe duration was multiplied correctly
        assertEquals(50, outputRecipe.getDuration());

        //Check the recipe outputs
        assertFalse(outputRecipe.getOutputs().isEmpty());

        assertTrue(hashStrategy.equals(new ItemStack(Blocks.STONE, 4), outputRecipe.getOutputs().get(0)));

        enableBonusOverride = false;

    }


    @Override
    public void applyParallelBonus(@Nonnull RecipeBuilder<?> builder) {
        if (enableBonusOverride) {
            builder.EUt(1).duration(50);
        }
    }
}
