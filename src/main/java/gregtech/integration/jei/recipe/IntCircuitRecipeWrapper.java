package gregtech.integration.jei.recipe;

import gregtech.api.recipes.ingredients.IntCircuitIngredient;

import net.minecraft.item.ItemStack;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IntCircuitRecipeWrapper implements IRecipeWrapper {

    public final boolean input;

    private IntCircuitRecipeWrapper(boolean input) {
        this.input = input;
    }

    public static Collection<IntCircuitRecipeWrapper> create() {
        return ImmutableList.of(
                new IntCircuitRecipeWrapper(true),
                new IntCircuitRecipeWrapper(false));
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> stacks = IntStream.range(0, IntCircuitIngredient.CIRCUIT_MAX + 1)
                .mapToObj(IntCircuitIngredient::getIntegratedCircuit)
                .collect(Collectors.toList());
        if (input)
            ingredients.setInputs(VanillaTypes.ITEM, stacks);
        else
            ingredients.setOutputs(VanillaTypes.ITEM, stacks);
    }
}
