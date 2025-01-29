package gregtech.api.mui;

import gregtech.common.metatileentities.storage.CraftingRecipeLogic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GregTechGuiTransferHandler implements IRecipeTransferHandler<ModularContainer> {

    private final IRecipeTransferHandlerHelper handlerHelper;
    private InventoryCrafting craftingMatrix;

    public GregTechGuiTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
    }

    @Override
    public @NotNull Class<ModularContainer> getContainerClass() {
        return ModularContainer.class;
    }

    @Override
    public @Nullable IRecipeTransferError transferRecipe(ModularContainer container, IRecipeLayout recipeLayout,
                                                         EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        String key = PanelSyncManager.makeSyncKey("recipe_logic", 0);
        CraftingRecipeLogic recipeLogic = (CraftingRecipeLogic) container.getSyncManager()
                .getSyncHandler("workbench", key);

        if (!doTransfer) {
            // todo highlighting in JEI?
            return null;
        }

        var ingredients = recipeLayout.getItemStacks().getGuiIngredients();
        this.craftingMatrix = recipeLogic.getCraftingMatrix();

        for (int i = 0; i < craftingMatrix.getSizeInventory(); i++) {
            var ing = ingredients.get(i + 1).getDisplayedIngredient();
            this.craftingMatrix.setInventorySlotContents(i, ing == null ? ItemStack.EMPTY : ing);
        }

        recipeLogic.syncToServer(CraftingRecipeLogic.UPDATE_MATRIX, this::writeCraftingMatrix);
        recipeLogic.updateCurrentRecipe();
        return null;
    }

    private void writeCraftingMatrix(PacketBuffer buffer) {
        buffer.writeVarInt(this.craftingMatrix.getSizeInventory());
        for (int i = 0; i < this.craftingMatrix.getSizeInventory(); i++) {
            var stack = this.craftingMatrix.getStackInSlot(i);
            NetworkUtils.writeItemStack(buffer, stack);
        }
    }
}
