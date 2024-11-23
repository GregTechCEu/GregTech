package gregtech.api.capability.impl;

import gregtech.Bootstrap;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.util.GTUtility;
import gregtech.api.util.world.DummyWorld;
import gregtech.common.metatileentities.MetaTileEntities;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;

public class AbstractRecipeLogicTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    @Test
    public void trySearchNewRecipe() {
        AbstractRecipeLogic arl = createTestLogic(1, 1);
        arl.trySearchNewRecipe();

        // no recipe found
        MatcherAssert.assertThat(arl.invalidInputsForRecipes, is(true));
        MatcherAssert.assertThat(arl.isActive, is(false));
        MatcherAssert.assertThat(arl.previousRecipe, nullValue());

        queryTestRecipe(arl);
        MatcherAssert.assertThat(arl.invalidInputsForRecipes, is(false));
        MatcherAssert.assertThat(arl.previousRecipe, notNullValue());
        MatcherAssert.assertThat(arl.isActive, is(true));
        MatcherAssert.assertThat(arl.getInputInventory().getStackInSlot(0).getCount(), is(15));

        // Save a reference to the old recipe so we can make sure it's getting reused
        Recipe prev = arl.previousRecipe;

        // Finish the recipe, the output should generate, and the next iteration should begin
        arl.update();
        MatcherAssert.assertThat(arl.previousRecipe, is(prev));
        MatcherAssert.assertThat(AbstractRecipeLogic.areItemStacksEqual(arl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)), is(true));
        MatcherAssert.assertThat(arl.isActive, is(true));

        // Complete the second iteration, but the machine stops because its output is now full
        arl.getOutputInventory().setStackInSlot(0, new ItemStack(Blocks.STONE, 63));
        arl.getOutputInventory().setStackInSlot(1, new ItemStack(Blocks.STONE, 64));
        arl.update();
        MatcherAssert.assertThat(arl.isActive, is(false));
        MatcherAssert.assertThat(arl.isOutputsFull, is(true));

        // Try to process again and get failed out because of full buffer.
        arl.update();
        MatcherAssert.assertThat(arl.isActive, is(false));
        MatcherAssert.assertThat(arl.isOutputsFull, is(true));

        // Some room is freed in the output bus, so we can continue now.
        arl.getOutputInventory().setStackInSlot(1, ItemStack.EMPTY);
        arl.update();
        MatcherAssert.assertThat(arl.isActive, is(true));
        MatcherAssert.assertThat(arl.isOutputsFull, is(false));
        MatcherAssert.assertThat(AbstractRecipeLogic.areItemStacksEqual(arl.getOutputInventory().getStackInSlot(0),
                new ItemStack(Blocks.STONE, 1)), is(true));
    }

    @Test
    public void euAndSpeedBonus() {
        final int initialEUt = 30;
        final int initialDuration = 100;

        AbstractRecipeLogic arl = createTestLogic(initialEUt, initialDuration);
        arl.setEUDiscount(0.75); // 75% EU cost required
        arl.setSpeedBonus(0.2);  // 20% faster than normal

        queryTestRecipe(arl);
        MatcherAssert.assertThat(arl.recipeEUt, is(Math.round(initialEUt * 0.75)));
        MatcherAssert.assertThat(arl.maxProgressTime, is((int) Math.round(initialDuration * 0.2)));
    }

    @Test
    public void euAndSpeedBonusParallel() {
        final int initialEUt = 30;
        final int initialDuration = 100;

        AbstractRecipeLogic arl = createTestLogic(initialEUt, initialDuration);
        arl.setEUDiscount(0.5);  // 50% EU cost required
        arl.setSpeedBonus(0.2);  // 20% faster than normal
        arl.setParallelLimit(4); // Allow parallels

        queryTestRecipe(arl);

        // The EU discount should drop the EU/t of this recipe to 15 EU/t. As a result, this should now
        // be able to parallel 2 times.
        MatcherAssert.assertThat(arl.parallelRecipesPerformed, is(2));
        // Because of the parallel, now the paralleled recipe EU/t should be back to 30 EU/t.
        MatcherAssert.assertThat(arl.recipeEUt, is(30L));
        // Duration should be static regardless of parallels.
        MatcherAssert.assertThat(arl.maxProgressTime, is((int) Math.round(initialDuration * 0.2)));
    }

    private static int TEST_ID = 190;

    private static AbstractRecipeLogic createTestLogic(int testRecipeEUt, int testRecipeDuration) {
        World world = DummyWorld.INSTANCE;

        // Create an empty recipe map to work with
        RecipeMap<SimpleRecipeBuilder> map = new RecipeMap<>("test_reactor_" + TEST_ID,
                2,
                2,
                3,
                2,
                new SimpleRecipeBuilder().EUt(30),
                false);

        MetaTileEntity at = MetaTileEntities.registerMetaTileEntity(TEST_ID,
                new SimpleMachineMetaTileEntity(
                        GTUtility.gregtechId("chemical_reactor.lv_" + TEST_ID),
                        map,
                        null,
                        1, false));
        MetaTileEntity atte = new MetaTileEntityHolder().setMetaTileEntity(at);
        ((MetaTileEntityHolder) atte.getHolder()).setWorld(world);
        map.recipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(testRecipeEUt).duration(testRecipeDuration)
                .buildAndRegister();

        TEST_ID++;

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
            protected boolean drawEnergy(long recipeEUt, boolean simulate) {
                return true;
            }

            @Override
            protected IEnergyContainer getEnergyContainer() {
                return IEnergyContainer.DEFAULT;
            }

            @Override
            public long getMaxVoltage() {
                return 32;
            }
        };

        arl.isOutputsFull = false;
        arl.invalidInputsForRecipes = false;
        return arl;
    }

    private static void queryTestRecipe(AbstractRecipeLogic arl) {
        // put an item in the inventory that will trigger recipe recheck
        arl.getInputInventory().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        MatcherAssert.assertThat(arl.hasNotifiedInputs(), is(true));
        arl.trySearchNewRecipe();
    }
}
