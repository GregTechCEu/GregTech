package gregtech.api.mui;

import gregtech.api.mui.sync.PagedWidgetSyncHandler;
import gregtech.common.metatileentities.storage.CraftingRecipeLogic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GregTechGuiTransferHandler implements IRecipeTransferHandler<ModularContainer> {

    private final IRecipeTransferHandlerHelper handlerHelper;

    public GregTechGuiTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
    }

    @Override
    public @NotNull Class<ModularContainer> getContainerClass() {
        return ModularContainer.class;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(ModularContainer container,
                                                         @NotNull IRecipeLayout recipeLayout,
                                                         @NotNull EntityPlayer player, boolean maxTransfer,
                                                         boolean doTransfer) {
        if (!container.getSyncManager().isOpen("workbench")) {
            return null;
        }
        PanelSyncManager syncManager = container.getSyncManager().getPanelSyncManager("workbench");
        var recipeLogic = (CraftingRecipeLogic) syncManager.getSyncHandler("recipe_logic:0");
        var pageController = (PagedWidgetSyncHandler) syncManager.getSyncHandler("page_controller:0");

        if (!doTransfer) {
            // todo highlighting in JEI?
            return null;
        }

        var matrix = extractMatrix(recipeLayout.getItemStacks());
        recipeLogic.fillCraftingGrid(matrix);
        pageController.setPage(0);
        return null;
    }

    private Int2ObjectMap<ItemStack> extractMatrix(IGuiItemStackGroup stackGroup) {
        var ingredients = stackGroup.getGuiIngredients();
        Int2ObjectMap<ItemStack> matrix = new Int2ObjectArrayMap<>(9);
        for (var slot : ingredients.keySet()) {
            if (slot != 0) {
                var ing = ingredients.get(slot).getDisplayedIngredient();
                if (ing == null) continue;
                matrix.put(slot - 1, ingredients.get(slot).getDisplayedIngredient());
            }
        }
        return matrix;
    }
}
