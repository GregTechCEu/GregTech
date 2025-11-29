package gregtech.api.mui.widget.appeng.fluid;

import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.sync.appeng.AEFluidSyncHandler;
import gregtech.api.mui.widget.appeng.AEConfigSlot;
import gregtech.api.mui.widget.appeng.AEStackPreviewWidget;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.storage.data.IAEFluidStack;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.text.TextRenderer;
import com.cleanroommc.modularui.integration.recipeviewer.RecipeViewerGhostIngredientSlot;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;

public class AEFluidConfigSlot extends AEConfigSlot<IAEFluidStack>
                               implements Interactable, RecipeViewerGhostIngredientSlot<FluidStack> {

    public AEFluidConfigSlot(boolean isStocking, int index, @NotNull BooleanSupplier isAutoPull) {
        super(isStocking, index, isAutoPull);
        tooltipAutoUpdate(true);
    }

    @Override
    public void onInit() {
        super.onInit();
        getContext().getRecipeViewerSettings().addGhostIngredientSlot(this);
    }

    @Override
    protected void buildTooltip(@NotNull RichTooltip tooltip) {
        WrappedFluidStack config = (WrappedFluidStack) getSyncHandler().getConfig(index);
        if (config != null) {
            FluidStack stack = config.getDefinition();
            tooltip.addLine(KeyUtil.fluid(stack));
            FluidTooltipUtil.fluidInfo(stack, tooltip, false, true, true);
            tooltip.addLine(FluidTooltipUtil.getFluidModNameKey(stack));
            tooltip.addLine((context, x, y, width, height, widgetTheme) -> {
                final int color = Color.GREY.darker(2);
                // TODO: do I need to access the text renderer like this?
                codechicken.lib.gui.GuiDraw.drawRect(x, y + 3, (int) TextRenderer.SHARED.getLastActualWidth(), 2,
                        color);
            });
        }

        super.buildTooltip(tooltip);
    }

    @Override
    public @NotNull AEFluidSyncHandler getSyncHandler() {
        return (AEFluidSyncHandler) super.getSyncHandler();
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof AEFluidSyncHandler;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        WrappedFluidStack config = (WrappedFluidStack) getSyncHandler().getConfig(index);
        if (config != null) {
            GuiDraw.drawFluidTexture(config.getDefinition(), 1, 1, getArea().w() - 2, getArea().h() - 2, 0);
            if (!isStocking) {
                RenderUtil.renderTextFixedCorner(TextFormattingUtil.formatLongToCompactString(config.getStackSize(), 4),
                        17d, 18d, 0xFFFFFF, true, 0.5f);
            }
        }

        RenderUtil.handleJEIGhostSlotOverlay(this, widgetTheme);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (isAutoPull.getAsBoolean()) return Result.IGNORE;

        if (mouseButton == 0) {
            ItemStack heldItem = getSyncHandler().getSyncManager().getCursorItem();
            FluidStack heldFluid = FluidUtil.getFluidContained(heldItem);

            if (heldFluid != null) {
                getSyncHandler().setConfig(index, heldFluid);
                return Result.SUCCESS;
            }
        }

        return super.onMousePressed(mouseButton);
    }

    @Override
    public void setGhostIngredient(@NotNull FluidStack ingredient) {
        getSyncHandler().setConfig(index, ingredient);
    }

    @Override
    public @Nullable FluidStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return !isAutoPull.getAsBoolean() && ingredient instanceof FluidStack stack ? stack : null;
    }

    @Override
    public @Nullable Object getIngredient() {
        IAEFluidStack config = getSyncHandler().getConfig(index);
        return config == null ? null : config.getFluidStack();
    }

    @Override
    protected @NotNull AEStackPreviewWidget<IAEFluidStack> createPopupDrawable() {
        return new AEFluidStackPreviewWidget(() -> getSyncHandler().getConfig(index))
                .background(GTGuiTextures.FLUID_SLOT);
    }
}
