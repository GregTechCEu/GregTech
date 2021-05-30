package gregtech.api.capability.impl;

import gregtech.api.*;
import gregtech.api.metatileentity.*;
import gregtech.api.recipes.*;
import gregtech.api.recipes.builders.*;
import gregtech.api.render.*;
import gregtech.api.util.world.DummyWorld;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.junit.*;

import static org.junit.Assert.*;

public class AbstractRecipeLogicTest {

    @BeforeClass
    public static void init() {
        Bootstrap.register();
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
                new SimpleRecipeBuilder().EUt(30));

        MetaTileEntity at =
                GregTechAPI.registerMetaTileEntity(190,
                        new SimpleMachineMetaTileEntity(
                                new ResourceLocation(GTValues.MODID, "chemical_reactor.lv"),
                                map,
                                Textures.CHEMICAL_REACTOR_OVERLAY,
                                1));

        MetaTileEntity atte = new MetaTileEntityHolder().setMetaTileEntity(at);
        atte.getHolder().setWorld(world);
        map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildAndRegister();

        AbstractRecipeLogic arl = new AbstractRecipeLogic(atte, map) {
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
        //assert the consumption of the inputs did not mark the arl to look for a new recipe
        assertFalse(arl.hasNotifiedInputs());

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
