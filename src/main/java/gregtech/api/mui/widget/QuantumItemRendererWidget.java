package gregtech.api.mui.widget;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuantumItemRendererWidget extends Widget<QuantumItemRendererWidget> implements Interactable,
                                       JeiGhostIngredientSlot<ItemStack>,
                                       JeiIngredientProvider {

    private final IItemHandler itemHandler;

    public QuantumItemRendererWidget(IItemHandler itemHandler) {
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
        // draw stuff
        ItemStack stack = itemHandler.getStackInSlot(0);
        if (stack.isEmpty()) return;

        GuiScreenWrapper screenWrapper = getScreen().getScreenWrapper();
        var renderer = screenWrapper.getItemRenderer();
        screenWrapper.setZ(100);
        renderer.zLevel = 100;
        GlStateManager.disableDepth();
        renderer.renderItemAndEffectIntoGUI(stack, 1, 1);
        GlStateManager.enableDepth();
        screenWrapper.setZ(0);
        renderer.zLevel = 0;
    }

    @Override
    public void setGhostIngredient(@NotNull ItemStack ingredient) {
        // itemHandler.setStackInSlot(0, ingredient);
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
