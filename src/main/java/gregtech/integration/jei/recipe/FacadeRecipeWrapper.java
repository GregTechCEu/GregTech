package gregtech.integration.jei.recipe;

import gregtech.common.items.behaviors.FacadeItem;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.Lists;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.wrapper.ICraftingRecipeWrapper;
import org.jetbrains.annotations.Nullable;

public class FacadeRecipeWrapper implements ICraftingRecipeWrapper {

    private final ResourceLocation registryName;
    private final ItemStack plateStack;
    private final ItemStack facadeStack;
    private final ItemStack resultStack;

    public FacadeRecipeWrapper(ResourceLocation registryName, ItemStack plateStack, ItemStack resultStack) {
        this.registryName = registryName;
        this.plateStack = plateStack;
        this.facadeStack = FacadeItem.getFacadeStack(resultStack);
        this.resultStack = resultStack;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, Lists.newArrayList(plateStack, facadeStack));
        ingredients.setOutput(VanillaTypes.ITEM, resultStack);
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }
}
