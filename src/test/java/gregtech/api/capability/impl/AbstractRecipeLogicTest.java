package gregtech.api.capability.impl;

import gregtech.Bootstrap;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.world.DummyWorld;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;

public class AbstractRecipeLogicTest {

    @BeforeAll
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
        MatcherAssert.assertThat(arl.invalidInputsForRecipes, is(true));
        MatcherAssert.assertThat(arl.isActive, is(false));
        MatcherAssert.assertThat(arl.previousRecipe, nullValue());

        // put an item in the inventory that will trigger recipe recheck
        arl.getInputInventory().insertItem(0, new ItemStack(Blocks.COBBLESTONE, 16), false);
        // Inputs change. did we detect it ?
        MatcherAssert.assertThat(arl.hasNotifiedInputs(), is(true));
        arl.trySearchNewRecipe();
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
    public void trySearchNewRecipe_testEdgeCase() {
        RecipeMaps.CHEMICAL_RECIPES.recipeBuilder()
                .fluidInputs(Materials.SulfurDioxide.getFluid(1000), Materials.Oxygen.getFluid(1000))
                .notConsumable(OreDictUnifier.get(OrePrefix.dust, Materials.Iron))
                .fluidOutputs(Materials.SulfurTrioxide.getFluid(1000))
                .duration(200).EUt(7).buildAndRegister();

        List<FluidStack> fluidList = new ArrayList<>();
        fluidList.add(Materials.SulfurDioxide.getFluid(1000));
        fluidList.add(Materials.Oxygen.getFluid(1000));

        List<ItemStack> itemList = new ArrayList<>();
        itemList.add(OreDictUnifier.get(OrePrefix.dust, Materials.Iron));


        Recipe recipe = RecipeMaps.CHEMICAL_RECIPES.findRecipe(GTValues.V[GTValues.EV], Collections.emptyList(), fluidList, 64000);

        Recipe recipe2 = RecipeMaps.LARGE_CHEMICAL_RECIPES.findRecipe(GTValues.V[GTValues.EV], Collections.emptyList(), fluidList, 64000);

        MatcherAssert.assertThat(recipe, nullValue());

        MatcherAssert.assertThat(recipe2, nullValue());

        Recipe recipe3 = RecipeMaps.CHEMICAL_RECIPES.findRecipe(GTValues.V[GTValues.EV], itemList, fluidList, 64000);

        Recipe recipe4 = RecipeMaps.LARGE_CHEMICAL_RECIPES.findRecipe(GTValues.V[GTValues.EV], itemList, fluidList, 64000);

        MatcherAssert.assertThat(recipe3, notNullValue());

        MatcherAssert.assertThat(recipe4, notNullValue());

    }
}
