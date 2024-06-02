package gregtech.api.mui.widget;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidTank;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumItemRendererWidget extends Widget<QuantumItemRendererWidget> implements Interactable,
                                                                                             JeiGhostIngredientSlot<ItemStack>,
                                                                                             JeiIngredientProvider {
    private final IItemHandlerModifiable itemHandler;

    public QuantumItemRendererWidget(IItemHandlerModifiable itemHandler) {
        this.itemHandler = itemHandler;
    }

    @Override
    public void onInit() {
        getContext().getJeiSettings().addJeiGhostIngredientSlot(this);
    }

    @NotNull
    @Override
    public Result onMousePressed(int mouseButton) {
        // todo interaction maybe?
        return Result.IGNORE;
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        //draw stuff
    }

    @Override
    public void setGhostIngredient(@NotNull ItemStack ingredient) {
        itemHandler.setStackInSlot(0, ingredient);
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return ingredient instanceof ItemStack ? (ItemStack) ingredient : null;
    }

    @Override
    public @Nullable Object getIngredient() {
        return itemHandler.getStackInSlot(0);
    }
}
