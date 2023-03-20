package gregtech.api.capability.impl;

import gregtech.Bootstrap;
import gregtech.api.GTValues;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.util.world.DummyWorld;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityElectricBlastFurnace;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityFluidHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;

public class MultiblockRecipeLogicTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    private static ResourceLocation gregtechId(String name) {
        return new ResourceLocation(GTValues.MODID, name);
    }

    @Test
    public void trySearchNewRecipe() {

        World world = DummyWorld.INSTANCE;

        // Create an empty recipe map to work with
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder().EUt(32),
                false);

        RecipeMaps.BLAST_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .blastFurnaceTemp(1)
                .buildAndRegister();

        RecipeMapMultiblockController mbt =
                MetaTileEntities.registerMetaTileEntity(509,
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

                            // ignore muffler outputs
                            @Override
                            public boolean hasMufflerMechanics() {
                                return false;
                            }
                        });

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
        MetaTileEntityItemBus importItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, false) {
            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityItemBus exportItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, true) {
            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityFluidHatch importFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.lv"), 1, false) {
            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityFluidHatch exportFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.export.lv"), 1, true) {
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

        MultiblockRecipeLogic mbl = new MultiblockRecipeLogic(mbt) {

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

            // since the hatches were not really added to a valid multiblock structure,
            // refer to their inventories directly
            @Override
            protected IItemHandlerModifiable getInputInventory() {
                return importItemBus.getImportItems();
            }

            @Override
            protected IItemHandlerModifiable getOutputInventory() {
                return exportItemBus.getExportItems();
            }

            @Override
            protected IMultipleTankHandler getInputTank() {
                return importFluidBus.getImportFluids();
            }

            @Override
            protected IMultipleTankHandler getOutputTank() {
                return importFluidBus.getExportFluids();
            }

        };

        mbl.isOutputsFull = false;
        mbl.invalidInputsForRecipes = false;
        mbl.trySearchNewRecipe();

        // no recipe found
        MatcherAssert.assertThat(mbt.isDistinct(), is(false));
        MatcherAssert.assertThat(mbl.invalidInputsForRecipes, is(true));
        MatcherAssert.assertThat(mbl.isActive, is(false));
        MatcherAssert.assertThat(mbl.previousRecipe, nullValue());

        // put an item in the inventory that will trigger recipe recheck
        mbl.getInputInventory().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        // Inputs change. did we detect it ?
        MatcherAssert.assertThat(mbl.hasNotifiedInputs(), is(true));
        mbl.trySearchNewRecipe();
        MatcherAssert.assertThat(mbl.invalidInputsForRecipes, is(false));
        MatcherAssert.assertThat(mbl.previousRecipe, notNullValue());
        MatcherAssert.assertThat(mbl.isActive, is(true));
        MatcherAssert.assertThat(mbl.getInputInventory().getStackInSlot(0).getCount(), is(15));

        // Save a reference to the old recipe so we can make sure it's getting reused
        Recipe prev = mbl.previousRecipe;

        // Finish the recipe, the output should generate, and the next iteration should begin
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.previousRecipe, is(prev));
        MatcherAssert.assertThat(AbstractRecipeLogic.areItemStacksEqual(mbl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)), is(true));
        MatcherAssert.assertThat(mbl.isActive, is(true));

        // Complete the second iteration, but the machine stops because its output is now full
        mbl.getOutputInventory().setStackInSlot(0, new ItemStack(Blocks.STONE, 63));
        mbl.getOutputInventory().setStackInSlot(1, new ItemStack(Blocks.STONE, 64));
        mbl.getOutputInventory().setStackInSlot(2, new ItemStack(Blocks.STONE, 64));
        mbl.getOutputInventory().setStackInSlot(3, new ItemStack(Blocks.STONE, 64));
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.isActive, is(false));
        MatcherAssert.assertThat(mbl.isOutputsFull, is(true));

        // Try to process again and get failed out because of full buffer.
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.isActive, is(false));
        MatcherAssert.assertThat(mbl.isOutputsFull, is(true));

        // Some room is freed in the output bus, so we can continue now.
        mbl.getOutputInventory().setStackInSlot(1, ItemStack.EMPTY);
        MatcherAssert.assertThat(mbl.hasNotifiedOutputs(), is(true));
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.isActive, is(true));
        MatcherAssert.assertThat(mbl.isOutputsFull, is(false));
        mbl.completeRecipe();
        MatcherAssert.assertThat(AbstractRecipeLogic.areItemStacksEqual(mbl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)), is(true));
    }

    @Test
    public void trySearchNewRecipeDistinct() {

        World world = DummyWorld.INSTANCE;

        // Create an empty recipe map to work with
        RecipeMap<BlastRecipeBuilder> map = new RecipeMap<>("blast_furnace",
                1,
                3,
                1,
                2,
                0,
                1,
                0,
                1,
                new BlastRecipeBuilder().EUt(32),
                false);

        RecipeMaps.BLAST_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .blastFurnaceTemp(1)
                .buildAndRegister();

        RecipeMapMultiblockController mbt =
                MetaTileEntities.registerMetaTileEntity(510,
                        new MetaTileEntityElectricBlastFurnace(
                                // super function calls the world, which equal null in test
                                new ResourceLocation(GTValues.MODID, "electric_blast_furnace")) {

                            @Override
                            public boolean hasMufflerMechanics() {
                                return false;
                            }

                            // ignore maintenance problems
                            @Override
                            public boolean hasMaintenanceMechanics() {
                                return false;
                            }


                            @Override
                            public void reinitializeStructurePattern() {

                            }

                            @Override
                            public boolean isDistinct() {
                                return true;
                            }

                            // function checks for the temperature of the recipe against the coils
                            @Override
                            public boolean checkRecipe(@Nonnull Recipe recipe, boolean consumeIfSuccess) {
                                return true;
                            }
                        });

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
        MetaTileEntityItemBus importItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, false) {
            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityItemBus importItemBus2 = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, false) {
            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityItemBus exportItemBus = new MetaTileEntityItemBus(gregtechId("item_bus.export.lv"), 1, true) {
            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityFluidHatch importFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.import.lv"), 1, false) {
            @Override
            public boolean isAttachedToMultiBlock() {
                return true;
            }

            @Override
            public MultiblockControllerBase getController() {
                return mbt;
            }
        };
        MetaTileEntityFluidHatch exportFluidBus = new MetaTileEntityFluidHatch(gregtechId("fluid_hatch.export.lv"), 1, true) {
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
            field.set(importItemBus2, mbt);
            field.set(exportItemBus, mbt);
            field.set(importFluidBus, mbt);
            field.set(exportFluidBus, mbt);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        MultiblockRecipeLogic mbl = new MultiblockRecipeLogic(mbt) {

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

            // since the hatches were not really added to a valid multiblock structure,
            // refer to their inventories directly
            @Override
            protected IItemHandlerModifiable getInputInventory() {
                return importItemBus.getImportItems();
            }

            @Override
            protected IItemHandlerModifiable getOutputInventory() {
                return exportItemBus.getExportItems();
            }

            @Override
            protected IMultipleTankHandler getInputTank() {
                return importFluidBus.getImportFluids();
            }

            @Override
            protected IMultipleTankHandler getOutputTank() {
                return importFluidBus.getExportFluids();
            }

            @Override
            protected List<IItemHandlerModifiable> getInputBuses() {
                List<IItemHandlerModifiable> a = new ArrayList<>();
                a.add(importItemBus.getImportItems());
                a.add(importItemBus2.getImportItems());
                return a;
            }

        };

        MatcherAssert.assertThat(mbt.isDistinct(), is(true));

        mbl.isOutputsFull = false;
        mbl.invalidInputsForRecipes = false;
        mbl.trySearchNewRecipe();

        // no recipe found
        MatcherAssert.assertThat(mbt.isDistinct(), is(true));
        MatcherAssert.assertThat(mbl.invalidatedInputList.containsAll(mbl.getInputBuses()), is(true));
        MatcherAssert.assertThat(mbl.isActive, is(false));
        MatcherAssert.assertThat(mbl.previousRecipe, nullValue());

        // put an item in the first input bus that will trigger recipe recheck

        IItemHandlerModifiable firstBus = mbl.getInputBuses().get(0);
        firstBus.insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);

        // Inputs change. did we detect it ?
        MatcherAssert.assertThat(mbl.hasNotifiedInputs(), is(true));
        MatcherAssert.assertThat(mbl.getMetaTileEntity().getNotifiedItemInputList(), hasItem(firstBus));
        MatcherAssert.assertThat(mbl.canWorkWithInputs(), is(true));
        mbl.trySearchNewRecipe();
        MatcherAssert.assertThat(mbl.invalidatedInputList, not(hasItem(firstBus)));
        MatcherAssert.assertThat(mbl.previousRecipe, notNullValue());
        MatcherAssert.assertThat(mbl.isActive, is(true));
        MatcherAssert.assertThat(firstBus.getStackInSlot(0).getCount(), is(15));

        // Save a reference to the old recipe so we can make sure it's getting reused
        Recipe prev = mbl.previousRecipe;

        // Finish the recipe, the output should generate, and the next iteration should begin
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.previousRecipe, is(prev));
        MatcherAssert.assertThat(AbstractRecipeLogic.areItemStacksEqual(mbl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)), is(true));
        MatcherAssert.assertThat(mbl.isActive, is(true));

        // Complete the second iteration, but the machine stops because its output is now full
        mbl.getOutputInventory().setStackInSlot(0, new ItemStack(Blocks.STONE, 63));
        mbl.getOutputInventory().setStackInSlot(1, new ItemStack(Blocks.STONE, 64));
        mbl.getOutputInventory().setStackInSlot(2, new ItemStack(Blocks.STONE, 64));
        mbl.getOutputInventory().setStackInSlot(3, new ItemStack(Blocks.STONE, 64));
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.isActive, is(false));
        MatcherAssert.assertThat(mbl.isOutputsFull, is(true));

        // Try to process again and get failed out because of full buffer.
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.isActive, is(false));
        MatcherAssert.assertThat(mbl.isOutputsFull, is(true));

        // Some room is freed in the output bus, so we can continue now.
        mbl.getOutputInventory().setStackInSlot(1, ItemStack.EMPTY);
        MatcherAssert.assertThat(mbl.hasNotifiedOutputs(), is(true));
        mbl.updateWorkable();
        MatcherAssert.assertThat(mbl.isActive, is(true));
        MatcherAssert.assertThat(mbl.isOutputsFull, is(false));
        mbl.completeRecipe();
        MatcherAssert.assertThat(AbstractRecipeLogic.areItemStacksEqual(mbl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)), is(true));
    }
}
