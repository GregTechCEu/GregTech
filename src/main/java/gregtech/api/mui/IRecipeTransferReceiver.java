package gregtech.api.mui;

import gregtech.api.mui.sync.RecipeTransferSyncHandler;
import gregtech.integration.jei.JustEnoughItemsModule;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * An interface for receiving a recipe from a recipe viewer to anything on the panel, such as a {@link Widget} or
 * {@link SyncHandler}. <br/>
 * Register it via {@link GregTechGuiScreen#registerRecipeTransferHandler(String, IRecipeTransferReceiver)} or
 * {@link GregTechGuiScreen#registerRecipeTransferHandler(String, IRecipeTransferReceiver, int)} if you want to
 * prioritize checking a certain handler first. <br/>
 * If you're implementing this on a {@link SyncHandler}, it's recommended to extend {@link RecipeTransferSyncHandler} so
 * registering and unregistering from {@link GregTechGuiScreen} is done for you.
 */
public interface IRecipeTransferReceiver {

    /**
     * Attempt or simulate transferring a recipe from a recipe viewer like JEI or HEI. <br/>
     * A factory for default {@link IRecipeTransferError}s is available at {@link JustEnoughItemsModule#transferHelper}.
     * There are three default options for errors: <br/>
     * - {@link IRecipeTransferHandlerHelper#createInternalError()}: mark the recipe as invalid for transferring by
     * graying out the + button. <br/>
     * - {@link IRecipeTransferHandlerHelper#createUserErrorWithTooltip(String)}: the same as above, but also display a
     * message when hovering over the + button. <br/>
     * - {@link IRecipeTransferHandlerHelper#createUserErrorForSlots(String, Collection)}: the same as above, but
     * additionally highlight certain slots in the recipe to, for example, mark missing ingredients. Important: will
     * throw {@link IllegalArgumentException} if the supplied {@link Collection} is empty!
     * 
     * @param recipeLayout the recipe layout that contains the recipe category, and the item and fluid stacks.
     * @param maxTransfer  if the receiver should try to move as many ingredients as possible to the crafting slots, ie
     *                     a crafting table.
     * @param simulate     if this recipe should only simulate being transferred
     * @return {@code null} if the transfer should succeed or an {@link IRecipeTransferError} if not. If there are
     *         multiple registered recipe transfer receivers on the same panel, returning an error with type
     *         {@link IRecipeTransferError.Type#INTERNAL} will skip this and attempt the next one.
     */
    @Nullable
    @SideOnly(Side.CLIENT)
    IRecipeTransferError receiveRecipe(@NotNull IRecipeLayout recipeLayout, boolean maxTransfer, boolean simulate);
}
