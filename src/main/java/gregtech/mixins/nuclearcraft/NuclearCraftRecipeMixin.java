package gregtech.mixins.nuclearcraft;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// TODO, Remove after NC updates
@Mixin(targets = "nc/integration/gtce/GTCERecipeHelper")
public class NuclearCraftRecipeMixin {

    @WrapOperation(method = "addGTCERecipe", at = @At(value = "INVOKE_ASSIGN", target = "Lgregtech/api/recipes/RecipeMap;recipeBuilder()Lgregtech/api/recipes/RecipeBuilder;"))
    private static void fixRecipeMapName() {

    }
}
