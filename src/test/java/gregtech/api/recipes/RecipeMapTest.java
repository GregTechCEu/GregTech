package gregtech.api.recipes;

import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class RecipeMapTest {

    @BeforeClass
    public static void init() {
        Bootstrap.register();
    }

    @Test
    public void findRecipe() {
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

        assertEquals(1, map.getRecipeList().size());

        map.recipeBuilder()
                .notConsumable(new ItemStack(Blocks.COBBLESTONE))
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(2).duration(1)
                .buildAndRegister();

        assertEquals(1,map.getRecipeList().size());

        map.recipeBuilder()
                .notConsumable(FluidRegistry.WATER)
                .outputs(new ItemStack(Blocks.STONE))
                .EUt(1).duration(1)
                .buildAndRegister();

        Recipe r = map.findRecipe(1, Collections.singletonList(new ItemStack(Blocks.COBBLESTONE)), Collections.singletonList(null), 0, MatchingMode.DEFAULT);
        assertNotNull(r);

        Recipe r2 = map.findRecipe(1, Collections.singletonList(new ItemStack(Blocks.STONE)), Collections.singletonList(new FluidStack(FluidRegistry.WATER,1)), 0, MatchingMode.DEFAULT);
        assertNotNull(r2);
    }

}
