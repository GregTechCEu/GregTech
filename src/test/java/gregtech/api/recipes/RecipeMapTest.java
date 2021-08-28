package gregtech.api.recipes;

import gregtech.api.recipes.builders.SimpleRecipeBuilder;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;

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

        Recipe r = map.findRecipe(1, Collections.singletonList(new ItemStack(Blocks.COBBLESTONE)), Collections.singletonList(null), 0, MatchingMode.IGNORE_FLUIDS);
        assertNotNull(r);
    }


}
