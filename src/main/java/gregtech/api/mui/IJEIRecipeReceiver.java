package gregtech.api.mui;

import net.minecraft.client.Minecraft;
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
     * Return this the recipe is invalid for the receiver to hide the + button.
     */
    IRecipeTransferError DEFAULT_JEI_ERROR = new IRecipeTransferError() {

        @Override
        public @NotNull Type getType() {
            return Type.INTERNAL;
        }

        @Override
        public void showError(@NotNull Minecraft minecraft, int mouseX, int mouseY,
                              @NotNull IRecipeLayout recipeLayout, int recipeX, int recipeY) {
            // nothing to show, just hide the + button
        }
    };

    /**
     * @param recipeLayout the recipe layout that contains the recipe category, and the item and fluid stacks
     * @param maxTransfer  if the receiver should try to move as many ingredients as possible to the crafting slots, ie
     *                     a crafting table
     * @param simulate     if this recipe should only be simulated being transferred
     * @return a {@link IRecipeTransferError} if something isn't right, or null if it's okay to transfer this recipe
     */
    @SideOnly(Side.CLIENT)
    IRecipeTransferError receiveRecipe(@NotNull IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate);
}
