package gregtech.api.recipes;

import gregtech.Bootstrap;
import gregtech.api.GTValues;
import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import gregtech.api.recipes.map.AbstractMapIngredient;
import gregtech.api.recipes.map.MapFluidIngredient;
import gregtech.api.recipes.map.MapItemStackIngredient;
import gregtech.api.recipes.map.MapOreDictIngredient;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsNot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static gregtech.api.unification.material.Materials.*;
import static org.hamcrest.CoreMatchers.*;

public class RecipeMapTest {

    @BeforeAll
    public static void bootstrap() {
        Bootstrap.perform();
    }

    RecipeMap<SimpleRecipeBuilder> map;
    private static int mapId = 0;

    @BeforeEach
    public void setupRecipes() {
        map = new RecipeMap<>("test_reactor_" + mapId++,
                2,
                2,
                3,
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

        Recipe r = map.findRecipe(1, Collections.singletonList(new ItemStack(Blocks.COBBLESTONE)),
                Collections.singletonList(null));
        MatcherAssert.assertThat(r, notNullValue());

        // This test is failing for me locally -dan
        Recipe r2 = map.findRecipe(1, Collections.singletonList(new ItemStack(Blocks.STONE)),
                Collections.singletonList(new FluidStack(FluidRegistry.WATER, 1)));
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
                        NitrogenDioxide.getFluid(1000)));
        MatcherAssert.assertThat(r, notNullValue());
    }

    @Test
    public void removeRecipe() {
        Recipe r = map.findRecipe(30,
                Collections.singletonList(ItemStack.EMPTY),
                Arrays.asList(
                        Epichlorohydrin.getFluid(144),
                        Naphtha.getFluid(3000),
                        NitrogenDioxide.getFluid(1000)));
        MatcherAssert.assertThat(r, notNullValue());
        assert map.removeRecipe(r);
        MatcherAssert.assertThat(map.findRecipe(30,
                Collections.singletonList(ItemStack.EMPTY),
                Arrays.asList(
                        Epichlorohydrin.getFluid(144),
                        Naphtha.getFluid(3000),
                        NitrogenDioxide.getFluid(1000))),
                nullValue());
        MatcherAssert.assertThat(map.getRecipeList().size(), is(2));
    }

    @Test
    public void recipeLookupIgnoresStackAmount() {
        MapItemStackIngredient ingFromStack = new MapItemStackIngredient(
                new ItemStack(Blocks.COBBLESTONE), 0, null);

        RecipeBuilder r = new RecipeBuilder<>()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .inputs(new ItemStack(Blocks.COBBLESTONE, 2))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1);
        Recipe rec = (Recipe) r.build().getResult();

        MapItemStackIngredient ing0FromGTRecipeInput = new MapItemStackIngredient(
                rec.getInputs().get(0).getInputStacks()[0], rec.getInputs().get(0));
        MapItemStackIngredient ing1FromGTRecipeInput = new MapItemStackIngredient(
                rec.getInputs().get(1).getInputStacks()[0], rec.getInputs().get(1));

        MatcherAssert.assertThat(ingFromStack, equalTo(ing0FromGTRecipeInput));
        MatcherAssert.assertThat(ingFromStack, equalTo(ing1FromGTRecipeInput));

        MatcherAssert.assertThat(ing0FromGTRecipeInput, equalTo(ing1FromGTRecipeInput));
    }

    @Test
    public void recipeLookupIgnoresFluidStackAmount() {
        MapFluidIngredient ingFromStack = new MapFluidIngredient(
                new FluidStack(FluidRegistry.getFluid("water"), 1000));

        RecipeBuilder r = new RecipeBuilder<>()
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
        RecipeBuilder r = new RecipeBuilder<>()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .inputs(new ItemStack(Blocks.COBBLESTONE, 2))
                .input("cobblestone", 2)
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1);

        MatcherAssert.assertThat(r.inputs.get(0), new IsNot<>(equalTo(r.inputs.get(1))));
        MatcherAssert.assertThat(r.inputs.get(1), new IsNot<>(equalTo(r.inputs.get(2))));
    }

    @Test
    public void MapIngredientEquals() {
        RecipeBuilder r = new RecipeBuilder<>()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .inputs(new ItemStack(Blocks.COBBLESTONE, 2))
                .input("cobblestone", 2)
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1);
        Recipe rec = (Recipe) r.build().getResult();

        MapItemStackIngredient ing0FromGTRecipeInput = new MapItemStackIngredient(
                rec.getInputs().get(0).getInputStacks()[0], rec.getInputs().get(0));
        MapOreDictIngredient ing1FromGTRecipeInput = new MapOreDictIngredient(OreDictionary.getOreID("cobblestone"));

        MatcherAssert.assertThat(ing0FromGTRecipeInput, new IsNot<>(equalTo(ing1FromGTRecipeInput)));
        MatcherAssert.assertThat(ing1FromGTRecipeInput, new IsNot<>(equalTo(ing0FromGTRecipeInput)));

        MatcherAssert.assertThat(rec.getInputs().get(0).acceptsStack(new ItemStack(Blocks.COBBLESTONE)), is(true));
        MatcherAssert.assertThat(rec.getInputs().get(1).acceptsStack(new ItemStack(Blocks.COBBLESTONE)), is(true));
    }

    @Test
    public void MapHashCollision() {
        RecipeBuilder r = new RecipeBuilder<>()
                .inputs(new ItemStack(Blocks.COBBLESTONE))
                .inputs(new ItemStack(Blocks.COBBLESTONE, 2))
                .inputs(new ItemStack(Blocks.STONE, 2))
                .input("cobblestone", 2)
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1);

        Recipe rec = (Recipe) r.build().getResult();

        // create the MapItemStackIngredient and call hashCode so the hash cached to the "hash" field

        MapItemStackIngredient ing0FromGTRecipeInput = new MapItemStackIngredient(
                rec.getInputs().get(0).getInputStacks()[0], rec.getInputs().get(0));
        ing0FromGTRecipeInput.hashCode();
        MapItemStackIngredient ing1FromGTRecipeInput = new MapItemStackIngredient(
                rec.getInputs().get(1).getInputStacks()[0], rec.getInputs().get(0));
        ing1FromGTRecipeInput.hashCode();
        MapItemStackIngredient ing2FromGTRecipeInput = new MapItemStackIngredient(
                rec.getInputs().get(2).getInputStacks()[0], rec.getInputs().get(0));
        ing1FromGTRecipeInput.hashCode();

        // Reflection so the equals in AbstractMapIngredient doesn't return false due to anonymous class check failure

        try {
            Field hash = AbstractMapIngredient.class.getDeclaredField("hash");
            hash.setAccessible(true);
            hash.set(ing0FromGTRecipeInput, 1);
            hash.set(ing1FromGTRecipeInput, 1);
            hash.set(ing2FromGTRecipeInput, 1);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        // Add the cobblestone ingredients to the map, which should considered equals.
        Object2ObjectOpenHashMap<MapItemStackIngredient, Object> map = new Object2ObjectOpenHashMap<>();
        map.put(ing0FromGTRecipeInput, ing0FromGTRecipeInput);
        map.put(ing1FromGTRecipeInput, ing1FromGTRecipeInput);

        MatcherAssert.assertThat(map.keySet().size(), is(1));

        // Add the stone, which is not equal and is a new key
        map.put(ing2FromGTRecipeInput, ing2FromGTRecipeInput);

        MatcherAssert.assertThat(map.keySet().size(), is(2));
    }

    @Test
    public void wildcardInput() {
        // test that all variants of a wildcard input can be used to find a recipe
        RecipeMap<?> recipeMap = new RecipeMap<>("test", 1, 4, 0, 0, new SimpleRecipeBuilder(), false);
        recipeMap.recipeBuilder()
                .inputs(new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, GTValues.W))
                .outputs(new ItemStack(Blocks.COBBLESTONE, 1))
                .EUt(1).duration(1)
                .buildAndRegister();

        for (int i = 0; i < 16; i++) { // Blocks.STAINED_HARDENED_CLAY has 16 variants
            Recipe recipe = recipeMap.findRecipe(Integer.MAX_VALUE,
                    Collections.singletonList(new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, i)),
                    Collections.emptyList());

            MatcherAssert.assertThat(recipe, notNullValue());
        }
    }
}
