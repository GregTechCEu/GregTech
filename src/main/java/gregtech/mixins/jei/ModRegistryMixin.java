package gregtech.mixins.jei;

import gregtech.integration.jei.JustEnoughItemsModule;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.startup.ModRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ModRegistry.class, remap = false)
public abstract class ModRegistryMixin {

    @Shadow
    @Final
    private List<IRecipeCategory<?>> recipeCategories;

    /**
     * @reason Sort recipe categories according to rules defined by GTCEu to allow a custom order for its JEI category
     *         tabs.
     * @see JustEnoughItemsModule#getRecipeCategoryComparator()
     */
    @Inject(method = "createRecipeRegistry", at = @At("HEAD"), order = 100)
    private void gregtech$sortRecipeCategories(CallbackInfoReturnable<RecipeRegistry> ci) {
        recipeCategories.sort(JustEnoughItemsModule.getRecipeCategoryComparator());
    }
}
