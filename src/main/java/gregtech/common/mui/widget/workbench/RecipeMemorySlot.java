package gregtech.common.mui.widget.workbench;

import gregtech.api.mui.GTGuiTextures;
import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.storage.CraftingRecipeMemory;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipeMemorySlot extends Widget<RecipeMemorySlot> implements Interactable, JeiIngredientProvider {

    private final CraftingRecipeMemory memory;
    private final int index;

    public RecipeMemorySlot(CraftingRecipeMemory memory, int index) {
        this.memory = memory;
        this.index = index;
        tooltipAutoUpdate(true);
        tooltipBuilder(tooltip -> {
            var recipe = memory.getRecipeAtIndex(this.index);
            if (recipe == null) return;

            tooltip.addFromItem(recipe.getRecipeResult());

            tooltip.spaceLine(2);
            tooltip.addLine(IKey.lang("gregtech.recipe_memory_widget.tooltip.1"));
            tooltip.addLine(IKey.lang("gregtech.recipe_memory_widget.tooltip.2"));
            tooltip.addLine(IKey.lang("gregtech.recipe_memory_widget.tooltip.3"));
            tooltip.addLine(IKey.lang("gregtech.recipe_memory_widget.tooltip.0", recipe.timesUsed)
                    .style(TextFormatting.WHITE));
        });
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        ItemStack itemStack = this.memory.getRecipeOutputAtIndex(this.index);

        if (!itemStack.isEmpty()) {
            int cachedCount = itemStack.getCount();
            itemStack.setCount(1); // required to not render the amount overlay
            RenderUtil.renderItem(itemStack, 1, 1, 16, 16);
            itemStack.setCount(cachedCount);

            // noinspection DataFlowIssue
            if (this.memory.getRecipeAtIndex(this.index).isRecipeLocked()) {
                GlStateManager.disableDepth();
                GTGuiTextures.RECIPE_LOCK.draw(context, 10, 1, 8, 8, widgetTheme);
                GlStateManager.enableDepth();
            }
        }

        RenderUtil.handleSlotOverlay(this, widgetTheme);
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(getContext(), this.memory.getRecipeOutputAtIndex(this.index));
        }
    }

    @NotNull
    @Override
    public Result onMousePressed(int mouseButton) {
        var recipe = memory.getRecipeAtIndex(this.index);
        if (recipe == null)
            return Result.IGNORE;

        var data = MouseData.create(mouseButton);
        this.memory.syncToServer(CraftingRecipeMemory.MOUSE_CLICK, buffer -> {
            buffer.writeByte(this.index);
            data.writeToPacket(buffer);
        });

        if (data.shift && data.mouseButton == 0) {
            recipe.setRecipeLocked(!recipe.isRecipeLocked());
        } else if (data.mouseButton == 1 && !recipe.isRecipeLocked()) {
            this.memory.removeRecipe(index);
        }

        return Result.ACCEPT;
    }

    @Override
    public @Nullable Object getIngredient() {
        if (!this.memory.hasRecipe(this.index)) return null;
        return this.memory.getRecipeOutputAtIndex(this.index);
    }
}
