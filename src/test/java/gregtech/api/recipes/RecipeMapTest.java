package gregtech.api.recipes;

import gregtech.Bootstrap;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.map.MapFluidIngredient;
import gregtech.api.recipes.map.MapItemStackIngredient;
import gregtech.api.recipes.map.MapOreDictIngredient;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsNot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static gregtech.api.items.OreDictNames.cobblestone;
import static gregtech.api.unification.material.Materials.*;
import static org.hamcrest.CoreMatchers.*;

public class RecipeMapTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    RecipeMap<SimpleRecipeBuilder> map;

    @BeforeEach
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
        MatcherAssert.assertThat(map.getRecipeList().size(), is(3));

        Recipe r = map.findRecipe(1, Collections.singletonList(new ItemStack(Blocks.COBBLESTONE)), Collections.singletonList(null), 0);
        MatcherAssert.assertThat(r, notNullValue());

        // This test is failing for me locally -dan
        Recipe r2 = map.findRecipe(1, Collections.singletonList(new ItemStack(Blocks.STONE)), Collections.singletonList(new FluidStack(FluidRegistry.WATER, 1)), 0);
        MatcherAssert.assertThat(r2, notNullValue());
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
        MatcherAssert.assertThat(r, notNullValue());
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
        MatcherAssert.assertThat(r, notNullValue());
        assert map.removeRecipe(r);
        MatcherAssert.assertThat(map.findRecipe(30,
                Collections.singletonList(ItemStack.EMPTY),
                Arrays.asList(
                        Epichlorohydrin.getFluid(144),
                        Naphtha.getFluid(3000),
                        NitrogenDioxide.getFluid(1000)),
                64000), nullValue());
        MatcherAssert.assertThat(map.getRecipeList().size(), is(2));
    }

    @Test
    public void recipeLookupIgnoresStackAmount() {
        MapItemStackIngredient ingFromStack = new MapItemStackIngredient(
                new ItemStack(Blocks.COBBLESTONE), 0, null
        );

        RecipeBuilder r = new RecipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .inputs(new ItemStack(Blocks.COBBLESTONE, 2))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1);
        Recipe rec = (Recipe) r.build().getResult();

        MapItemStackIngredient ing0FromGTRecipeInput = new MapItemStackIngredient(rec.getInputs().get(0).getInputStacks()[0], rec.getInputs().get(0));
        MapItemStackIngredient ing1FromGTRecipeInput = new MapItemStackIngredient(rec.getInputs().get(1).getInputStacks()[0], rec.getInputs().get(1));

        MatcherAssert.assertThat(ingFromStack, equalTo(ing0FromGTRecipeInput));
        MatcherAssert.assertThat(ingFromStack, equalTo(ing1FromGTRecipeInput));

        MatcherAssert.assertThat(ing0FromGTRecipeInput, equalTo(ing1FromGTRecipeInput));
    }

    @Test
    public void recipeLookupIgnoresFluidStackAmount() {
        MapFluidIngredient ingFromStack = new MapFluidIngredient(
                new FluidStack(FluidRegistry.getFluid("water"), 1000)
        );

        RecipeBuilder r = new RecipeBuilder()
                .fluidInputs(new FluidStack(FluidRegistry.getFluid("water"), 1000))
                .fluidInputs(new FluidStack(FluidRegistry.getFluid("water"), 1001))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1);
        Recipe rec = (Recipe) r.build().getResult();

        MapFluidIngredient ing0FromGTRecipeInput = new MapFluidIngredient(rec.getFluidInputs().get(0));
        MapFluidIngredient ing1FromGTRecipeInput = new MapFluidIngredient(rec.getFluidInputs().get(1));

        MatcherAssert.assertThat(ingFromStack, equalTo(ing0FromGTRecipeInput));
        MatcherAssert.assertThat(ingFromStack, equalTo(ing1FromGTRecipeInput));

        MatcherAssert.assertThat(ing0FromGTRecipeInput, equalTo(ing1FromGTRecipeInput));
    }

    @Test
    public void GTRecipeInputEquals() {
        RecipeBuilder r = new RecipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .inputs(new ItemStack(Blocks.COBBLESTONE, 2))
                .input("cobblestone", 2)
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1);

        MatcherAssert.assertThat(r.inputs.get(0), new IsNot(equalTo(r.inputs.get(1))));
        MatcherAssert.assertThat(r.inputs.get(1), new IsNot(equalTo(r.inputs.get(2))));
    }

    @Test
    public void MapIngredientEquals() {
        RecipeBuilder r = new RecipeBuilder()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .inputs(new ItemStack(Blocks.COBBLESTONE, 2))
                .input("cobblestone", 2)
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1);
        Recipe rec = (Recipe) r.build().getResult();

        MapItemStackIngredient ing0FromGTRecipeInput = new MapItemStackIngredient(rec.getInputs().get(0).getInputStacks()[0], rec.getInputs().get(0));
        MapOreDictIngredient ing1FromGTRecipeInput = new MapOreDictIngredient(OreDictionary.getOreID("cobblestone"));

        MatcherAssert.assertThat(ing0FromGTRecipeInput, new IsNot(equalTo(ing1FromGTRecipeInput)));
        MatcherAssert.assertThat(ing1FromGTRecipeInput, new IsNot(equalTo(ing0FromGTRecipeInput)));

        MatcherAssert.assertThat(rec.getInputs().get(0).acceptsStack(new ItemStack(Blocks.COBBLESTONE)), is(true));
        MatcherAssert.assertThat(rec.getInputs().get(1).acceptsStack(new ItemStack(Blocks.COBBLESTONE)), is(true));


    }


}
