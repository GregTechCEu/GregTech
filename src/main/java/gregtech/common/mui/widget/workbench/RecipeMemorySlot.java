package gregtech.common.mui.widget.workbench;

import gregtech.api.mui.GTGuiTextures;
import gregtech.common.metatileentities.storage.CraftingRecipeMemory;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

public class RecipeMemorySlot extends Widget<RecipeMemorySlot> implements Interactable {

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
            tooltip.addLine(IKey.lang("gregtech.recipe_memory_widget.tooltip.0", recipe.timesUsed)
                    .format(TextFormatting.WHITE));
        });
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        ItemStack itemstack = this.memory.getRecipeOutputAtIndex(this.index);
        if (itemstack.isEmpty()) return;

        int cachedCount = itemstack.getCount();
        itemstack.setCount(1); // required to not render the amount overlay
        GuiDraw.drawItem(itemstack, 1, 1, 16, 16);
        itemstack.setCount(cachedCount);

        // noinspection DataFlowIssue
        if (this.memory.getRecipeAtIndex(this.index).isRecipeLocked()) {
            GlStateManager.disableDepth();
            GTGuiTextures.RECIPE_LOCK.draw(context, 10, 1, 8, 8, widgetTheme);
            GlStateManager.enableDepth();
        }
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
        this.memory.syncToServer(2, buffer -> {
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
}
