package gregtech.api.mui;

import com.cleanroommc.modularui.screen.ModularContainer;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;

import net.minecraft.entity.player.EntityPlayer;

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
    public @Nullable IRecipeTransferError transferRecipe(ModularContainer container, IRecipeLayout recipeLayout,
                                                         EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        return null;
    }
}
