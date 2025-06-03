package gregtech.api.mui;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import org.jetbrains.annotations.NotNull;

/**
 * For receiving a JEI recipe transfer (the + button in a recipe) in a
 * {@link com.cleanroommc.modularui.value.sync.SyncHandler} <br>
 * If there are multiple sync handlers implementing this, the handler that will be used to receive the recipe is
 * indeterminate.
 */
public interface IJEIRecipeReceiver {

    IRecipeTransferError receiveRecipe(@NotNull IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate);
}
