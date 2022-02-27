package gregtech.integration.jei.utils;

import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.gui.impl.ModularUIGui;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.gui.ingredient.IIngredientSlot;
import gregtech.api.gui.ingredient.IRecipeTransferHandlerWidget;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;

public class ModularUIGuiHandler implements IAdvancedGuiHandler<ModularUIGui>, IGhostIngredientHandler<ModularUIGui>, IRecipeTransferHandler<ModularUIContainer> {

    private final IRecipeTransferHandlerHelper transferHelper;
    private Predicate<IRecipeTransferHandlerWidget> validHandlers = widget -> true;

    public ModularUIGuiHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    public void setValidHandlers(Predicate<IRecipeTransferHandlerWidget> validHandlers) {
        this.validHandlers = validHandlers;
    }

    @Nonnull
    @Override
    public Class<ModularUIGui> getGuiContainerClass() {
        return ModularUIGui.class;
    }

    @Nonnull
    @Override
    public Class<ModularUIContainer> getContainerClass() {
        return ModularUIContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(ModularUIContainer container, @Nonnull IRecipeLayout recipeLayout, @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        Optional<IRecipeTransferHandlerWidget> transferHandler = container.getModularUI()
                .getFlatVisibleWidgetCollection().stream()
                .filter(it -> it instanceof IRecipeTransferHandlerWidget)
                .map(it -> (IRecipeTransferHandlerWidget) it)
                .filter(validHandlers)
                .findFirst();
        if (!transferHandler.isPresent()) {
            return transferHelper.createInternalError();
        }
        String errorTooltip = transferHandler.get().transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer);
        if (errorTooltip == null) {
            return null;
        }
        return transferHelper.createUserErrorWithTooltip(errorTooltip);
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(ModularUIGui gui, int mouseX, int mouseY) {
        Collection<Widget> widgets = gui.getModularUI().guiWidgets.values();
        for (Widget widget : widgets) {
            if (widget instanceof IIngredientSlot) {
                Object result = ((IIngredientSlot) widget).getIngredientOverMouse(mouseX, mouseY);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    @Nonnull
    @Override
    public <I> List<Target<I>> getTargets(ModularUIGui gui, @Nonnull I ingredient, boolean doStart) {
        Collection<Widget> widgets = gui.getModularUI().guiWidgets.values();
        List<Target<I>> targets = new ArrayList<>();
        for (Widget widget : widgets) {
            if (widget instanceof IGhostIngredientTarget) {
                IGhostIngredientTarget ghostTarget = (IGhostIngredientTarget) widget;
                List<Target<?>> widgetTargets = ghostTarget.getPhantomTargets(ingredient);
                //noinspection unchecked
                targets.addAll((List<Target<I>>) (Object) widgetTargets);
            }
        }
        return targets;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(@Nonnull ModularUIGui guiContainer) {
        return Collections.emptyList();
    }

    @Override
    public void onComplete() {
    }
}
