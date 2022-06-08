package gregtech.api.capability.impl;

import gregtech.Bootstrap;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.util.world.DummyWorld;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class AbstractRecipeLogicTest {

    @BeforeClass
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void trySearchNewRecipe() {

        World world = DummyWorld.INSTANCE;

        // Create an empty recipe map to work with
        RecipeMap<SimpleRecipeBuilder> map = new RecipeMap<>("chemical_reactor",
                0,
                2,
                0,
                2,
                0,
                3,
                0,
                2,
                new SimpleRecipeBuilder().EUt(30),
                false);

        MetaTileEntity at =
                MetaTileEntities.registerMetaTileEntity(190,
                        new SimpleMachineMetaTileEntity(
                                new ResourceLocation(GTValues.MODID, "chemical_reactor.lv"),
                                map,
                                null,
                                1, false));
        MetaTileEntity atte = new MetaTileEntityHolder().setMetaTileEntity(at);
        ((MetaTileEntityHolder) atte.getHolder()).setWorld(world);
        map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildAndRegister();

        AbstractRecipeLogic arl = new AbstractRecipeLogic(atte, map) {
            @Override
            protected long getEnergyInputPerSecond() {
                return Long.MAX_VALUE;
            }

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
            protected long getMaxVoltage() {
                return 32;
            }
        };

        arl.isOutputsFull = false;
        arl.invalidInputsForRecipes = false;
        arl.trySearchNewRecipe();

        // no recipe found
        assertTrue(arl.invalidInputsForRecipes);
        assertFalse(arl.isActive);
        assertNull(arl.previousRecipe);

        // put an item in the inventory that will trigger recipe recheck
        arl.getInputInventory().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        // Inputs change. did we detect it ?
        assertTrue(arl.hasNotifiedInputs());
        arl.trySearchNewRecipe();
        assertFalse(arl.invalidInputsForRecipes);
        assertNotNull(arl.previousRecipe);
        assertTrue(arl.isActive);
        assertEquals(15, arl.getInputInventory().getStackInSlot(0).getCount());

        // Save a reference to the old recipe so we can make sure it's getting reused
        Recipe prev = arl.previousRecipe;

        // Finish the recipe, the output should generate, and the next iteration should begin
        arl.update();
        assertEquals(prev, arl.previousRecipe);
        assertTrue(AbstractRecipeLogic.areItemStacksEqual(arl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)));
        assertTrue(arl.isActive);

        // Complete the second iteration, but the machine stops because its output is now full
        arl.getOutputInventory().setStackInSlot(0, new ItemStack(Blocks.STONE, 63));
        arl.getOutputInventory().setStackInSlot(1, new ItemStack(Blocks.STONE, 64));
        arl.update();
        assertFalse(arl.isActive);
        assertTrue(arl.isOutputsFull);

        // Try to process again and get failed out because of full buffer.
        arl.update();
        assertFalse(arl.isActive);
        assertTrue(arl.isOutputsFull);

        // Some room is freed in the output bus, so we can continue now.
        arl.getOutputInventory().setStackInSlot(1, ItemStack.EMPTY);
        arl.update();
        assertTrue(arl.isActive);
        assertFalse(arl.isOutputsFull);
        assertTrue(AbstractRecipeLogic.areItemStacksEqual(arl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)));
    }
}
