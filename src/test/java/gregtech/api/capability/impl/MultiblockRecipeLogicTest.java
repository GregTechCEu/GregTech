package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.BlastRecipeBuilder;
import gregtech.api.util.world.DummyWorld;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityFluidHatch;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityItemBus;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityMultiblockPart;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityElectricBlastFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

public class MultiblockRecipeLogicTest {

    @BeforeClass
    public static void init() {
        Bootstrap.register();
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
                new BlastRecipeBuilder().EUt(32));

        RecipeMaps.BLAST_RECIPES.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .blastFurnaceTemp(1)
                .buildAndRegister();

        RecipeMapMultiblockController mbt =
                GregTechAPI.registerMetaTileEntity(511,
                        new MetaTileEntityElectricBlastFurnace(
                                // super function calls the world, which equal null in test
                                new ResourceLocation(GTValues.MODID, "electric_blast_furnace")) {
                            @Override
                            protected void reinitializeStructurePattern() {

                            }

                            // function checks for the temperature of the recipe against the coils
                            @Override
                            public boolean checkRecipe(Recipe recipe, boolean consumeIfSuccess) {
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

        mbt.getHolder().setWorld(world);

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
            protected boolean drawEnergy(int recipeEUt) {
                return true;
            }

            @Override
            protected long getMaxVoltage() {
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
        assertTrue(mbl.invalidInputsForRecipes);
        assertFalse(mbl.isActive);
        assertNull(mbl.previousRecipe);

        // put an item in the inventory that will trigger recipe recheck
        mbl.getInputInventory().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        // Inputs change. did we detect it ?
        assertTrue(mbl.hasNotifiedInputs());
        mbl.trySearchNewRecipe();
        assertFalse(mbl.invalidInputsForRecipes);
        assertNotNull(mbl.previousRecipe);
        assertTrue(mbl.isActive);
        assertEquals(15, mbl.getInputInventory().getStackInSlot(0).getCount());
        //assert the consumption of the inputs did not mark the arl to look for a new recipe
        assertFalse(mbl.hasNotifiedInputs());

        // Save a reference to the old recipe so we can make sure it's getting reused
        Recipe prev = mbl.previousRecipe;

        // Finish the recipe, the output should generate, and the next iteration should begin
        mbl.updateWorkable();
        assertEquals(prev, mbl.previousRecipe);
        assertTrue(AbstractRecipeLogic.areItemStacksEqual(mbl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)));
        assertTrue(mbl.isActive);

        // Complete the second iteration, but the machine stops because its output is now full
        mbl.getOutputInventory().setStackInSlot(0, new ItemStack(Blocks.STONE, 63));
        mbl.getOutputInventory().setStackInSlot(1, new ItemStack(Blocks.STONE, 64));
        mbl.getOutputInventory().setStackInSlot(2, new ItemStack(Blocks.STONE, 64));
        mbl.getOutputInventory().setStackInSlot(3, new ItemStack(Blocks.STONE, 64));
        mbl.updateWorkable();
        assertFalse(mbl.isActive);
        assertTrue(mbl.isOutputsFull);

        // Try to process again and get failed out because of full buffer.
        mbl.updateWorkable();
        assertFalse(mbl.isActive);
        assertTrue(mbl.isOutputsFull);

        // Some room is freed in the output bus, so we can continue now.
        mbl.getOutputInventory().setStackInSlot(1, ItemStack.EMPTY);
        assertTrue(mbl.hasNotifiedOutputs());
        mbl.updateWorkable();
        assertTrue(mbl.isActive);
        assertFalse(mbl.isOutputsFull);
        mbl.completeRecipe();
        assertTrue(AbstractRecipeLogic.areItemStacksEqual(mbl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)));
    }
}
