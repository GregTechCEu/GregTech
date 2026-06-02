package gregtech.api.mui;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import org.jetbrains.annotations.NotNull;

/**
 * For receiving a JEI recipe transfer (the + button in a recipe). <br>
 * Implement this on a {@link com.cleanroommc.modularui.value.sync.SyncHandler} and ensure it's registered to the
 * {@link com.cleanroommc.modularui.value.sync.PanelSyncManager}. <br>
 * If there are multiple sync handlers implementing this, the handler that will be used to receive the recipe is
 * indeterminate.
 */
public interface IJEIRecipeReceiver {

    /**
     * Returning an {@link IRecipeTransferError} with a type of {@link IRecipeTransferError.Type#INTERNAL} will hide the
     * + button. <br>
     * JEI has a static instance available at {@link mezz.jei.transfer.RecipeTransferErrorInternal#INSTANCE} for this
     * purpose.
     * 
     * @param recipeLayout the recipe layout that contains the recipe category, and the item and fluid stacks
     * @param maxTransfer  if the receiver should try to move as many ingredients as possible to the crafting slots, ie
     *                     a crafting table
     * @param simulate     if this recipe should only be simulated being transferred
     * @return a {@link IRecipeTransferError} if something isn't right, or null if it's okay to transfer this recipe
     */
    @SideOnly(Side.CLIENT)
    IRecipeTransferError receiveRecipe(@NotNull IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate);
}
