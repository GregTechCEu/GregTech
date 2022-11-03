package gregtech.api.recipes;

import gregtech.Bootstrap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static gregtech.api.unification.material.Materials.*;
import static org.junit.Assert.*;

public class RecipeMapTest {

    @BeforeClass
    public static void bootstrap() {
        Bootstrap.perform();
    }

    RecipeMap<SimpleRecipeBuilder> map;

    @Before
    public void setupRecipes() {
        map = new RecipeMap<>("chemical_reactor",
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
        map.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildAndRegister();

        map.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildAndRegister();

        map.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(2).duration(1)
                .buildAndRegister();

        map.recipeBuilder()
                .inputs(new ItemStack(Blocks.STONE))
                .notConsumable(FluidRegistry.WATER)
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildAndRegister();

        map.recipeBuilder()
                .fluidInputs(Epichlorohydrin.getFluid(144))
                .fluidInputs(Naphtha.getFluid(3000))
                .fluidInputs(NitrogenDioxide.getFluid(1000))
                .fluidOutputs(Epoxy.getFluid(288))
                .duration(240).EUt(30).buildAndRegister();
    }

    @Test
    public void findRecipe() {
        assertEquals(3, map.getRecipeList().size());

        Recipe r = map.findRecipe(1, Collections.singletonList(new ItemStack(Blocks.COBBLESTONE)), Collections.singletonList(null), 0);
        assertNotNull(r);

        // This test is failing for me locally -dan
        Recipe r2 = map.findRecipe(1, Collections.singletonList(new ItemStack(Blocks.STONE)), Collections.singletonList(new FluidStack(FluidRegistry.WATER, 1)), 0);
        assertNotNull(r2);
    }

    // This test fails
    @Test
    public void findRecipeFluidOnly() {
        Recipe r = map.findRecipe(30,
                Collections.singletonList(ItemStack.EMPTY),
                Arrays.asList(
                        Epichlorohydrin.getFluid(144),
                        Naphtha.getFluid(3000),
                        NitrogenDioxide.getFluid(1000)),
                64000);
        assertNotNull(r);
    }

    @Test
    public void removeRecipe() {
        Recipe r = map.findRecipe(30,
                Collections.singletonList(ItemStack.EMPTY),
                Arrays.asList(
                        Epichlorohydrin.getFluid(144),
                        Naphtha.getFluid(3000),
                        NitrogenDioxide.getFluid(1000)),
                64000);
        assertNotNull(r);
        assert map.removeRecipe(r);
        assertNull(map.findRecipe(30,
                Collections.singletonList(ItemStack.EMPTY),
                Arrays.asList(
                        Epichlorohydrin.getFluid(144),
                        Naphtha.getFluid(3000),
                        NitrogenDioxide.getFluid(1000)),
                64000));
        assertEquals(2, map.getRecipeList().size());
    }

}
