package gregtech.integration.jei.recipe;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.facade.FacadeHelper;
import gregtech.common.items.MetaItems;
import gregtech.common.items.behaviors.FacadeItem;

import net.minecraft.item.ItemStack;

import com.google.common.collect.Lists;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.IFocus.Mode;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class FacadeRegistryPlugin implements IRecipeRegistryPlugin {

    @NotNull
    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        if (focus.getValue() instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) focus.getValue();
            if (focus.getMode() == Mode.OUTPUT) {
                if (MetaItems.COVER_FACADE.isItemEqual(itemStack)) {
                    // looking up recipes of facade cover
                    return Collections.singletonList(VanillaRecipeCategoryUid.CRAFTING);
                }
            } else if (focus.getMode() == Mode.INPUT) {
                if (FacadeHelper.isValidFacade(itemStack)) {
                    // looking up usage of block to make a facade cover
                    return Collections.singletonList(VanillaRecipeCategoryUid.CRAFTING);
                }
            }
        }
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory,
                                                                   @NotNull IFocus<V> focus) {
        if (!VanillaRecipeCategoryUid.CRAFTING.equals(recipeCategory.getUid())) {
            return Collections.emptyList();
        }
        if (focus.getValue() instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) focus.getValue();
            if (focus.getMode() == Mode.OUTPUT) {
                if (MetaItems.COVER_FACADE.isItemEqual(itemStack)) {
                    // looking up recipes of facade cover
                    return (List<T>) createFacadeRecipes(itemStack);
                }
            } else if (focus.getMode() == Mode.INPUT) {
                if (FacadeHelper.isValidFacade(itemStack)) {
                    // looking up usage of block to make a facade cover
                    ItemStack coverStack = MetaItems.COVER_FACADE.getStackForm();
                    FacadeItem.setFacadeStack(coverStack, itemStack);
                    return (List<T>) createFacadeRecipes(coverStack);
                }
            }
        }
        return Collections.emptyList();
    }

    private static List<IRecipeWrapper> createFacadeRecipes(ItemStack itemStack) {
        return Lists.newArrayList(createFacadeRecipe(itemStack, Materials.Iron, 4));
    }

    private static IRecipeWrapper createFacadeRecipe(ItemStack itemStack, Material material, int facadeAmount) {
        ItemStack itemStackCopy = itemStack.copy();
        itemStackCopy.setCount(facadeAmount);
        return new FacadeRecipeWrapper(GTUtility.gregtechId("facade_" + material),
                OreDictUnifier.get(OrePrefix.plate, material), itemStackCopy);
    }

    @NotNull
    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(@NotNull IRecipeCategory<T> recipeCategory) {
        return Collections.emptyList();
    }
}
