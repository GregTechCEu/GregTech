package gregtech.api.mui.widget;

import gregtech.api.mui.sync.MappedSyncHandler;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.value.DynamicValue;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class QuantumItemRendererWidget extends Widget<QuantumItemRendererWidget> implements Interactable,
                                       JeiGhostIngredientSlot<ItemStack>,
                                       JeiIngredientProvider {

    private final Supplier<ItemStack> virtualStack;
    private DynamicValue<ItemStack> lockedStack;
    private final SyncHandler syncHandler = new MappedSyncHandler()
            .addServerHandler(0, buffer -> lockedStack.setValue(NetworkUtils.readItemStack(buffer)));

    public QuantumItemRendererWidget(Supplier<ItemStack> virtualStack) {
        this.virtualStack = virtualStack;
        setSyncHandler(this.syncHandler);
        tooltip().setAutoUpdate(true).setHasTitleMargin(true);
        tooltipBuilder(tooltip -> {
            if (!isSynced()) return;
            ItemStack stack = virtualStack.get();
            if (stack.isEmpty()) stack = lockedStack.getValue();
            if (stack.isEmpty()) return;
            tooltip.addStringLines(getScreen().getScreenWrapper().getItemToolTip(stack));
        });
    }

    @Override
    public void onInit() {
        getContext().getJeiSettings().addJeiGhostIngredientSlot(this);
    }

    @NotNull
    @Override
    public Result onMousePressed(int mouseButton) {
        // todo handle locked
        return Result.IGNORE;
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        // draw stuff
        ItemStack stack = virtualStack.get();
        if (stack.isEmpty()) stack = lockedStack.getValue();
        if (stack.isEmpty()) return;

        GuiScreenWrapper screenWrapper = getScreen().getScreenWrapper();
        var renderer = screenWrapper.getItemRenderer();
        screenWrapper.setZ(100);
        renderer.zLevel = 100;
        GlStateManager.disableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        renderer.renderItemAndEffectIntoGUI(stack, 1, 1);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableDepth();
        screenWrapper.setZ(0);
        renderer.zLevel = 0;
    }

    @Override
    public void setGhostIngredient(@NotNull ItemStack ingredient) {
        lockedStack.setValue(ingredient);
        this.syncHandler.syncToServer(0, buffer -> NetworkUtils.writeItemStack(buffer, ingredient));
    }

    @Override
    public @Nullable ItemStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return ingredient instanceof ItemStack ? (ItemStack) ingredient : null;
    }

    @Override
    public @Nullable Object getIngredient() {
        return lockedStack.getValue();
    }

    public QuantumItemRendererWidget onLock(Supplier<ItemStack> getter, Consumer<ItemStack> setter) {
        this.lockedStack = new DynamicValue<>(getter, setter);
        return this;
    }
}
