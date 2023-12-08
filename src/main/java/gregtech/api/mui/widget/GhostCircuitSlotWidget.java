package gregtech.api.mui.widget;

import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.synchandler.GhostCircuitSyncHandler;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GhostCircuitSlotWidget extends ItemSlot {

    public GhostCircuitSlotWidget() {
        tooltipBuilder(this::getCircuitSlotTooltip);
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        if (!isSelectorPanelOpen()) {
            if (mouseButton == 0 && TooltipHelper.isShiftDown()) {
                createSelectorPanel();
            } else {
                MouseData mouseData = MouseData.create(mouseButton);
                getSyncHandler().syncToServer(2, mouseData::writeToPacket);
            }
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScroll(ModularScreen.UpOrDown scrollDirection, int amount) {
        if (isSelectorPanelOpen()) return true;
        MouseData mouseData = MouseData.create(scrollDirection.modifier);
        getSyncHandler().syncToServer(3, mouseData::writeToPacket);
        return false;
    }

    @Override
    public ItemSlot slot(ModularSlot slot) {
        ItemSlotSH sh = new GhostCircuitSyncHandler(slot);
        isValidSyncHandler(sh);
        setSyncHandler(sh);
        return this;
    }

    @Override
    protected List<String> getItemTooltip(ItemStack stack) {
        // we don't want the item tooltip
        return Collections.emptyList();
    }

    protected void getCircuitSlotTooltip(@NotNull Tooltip tooltip) {
        String configString;
        int value = getSyncHandler().getGhostCircuitHandler().getCircuitValue();
        if (value == GhostCircuitItemStackHandler.NO_CONFIG) {
            configString = new TextComponentTranslation("gregtech.gui.configurator_slot.no_value").getFormattedText();
        } else {
            configString = String.valueOf(value);
        }

        tooltip.addLine(IKey.lang("gregtech.gui.configurator_slot.tooltip", configString));
    }

    @Override
    public @NotNull GhostCircuitSyncHandler getSyncHandler() {
        return (GhostCircuitSyncHandler) super.getSyncHandler();
    }

    @Override
    public void onMouseDrag(int mouseButton, long timeSinceClick) {}

    @Override
    public boolean onMouseRelease(int mouseButton) {
        return true;
    }

    private boolean isSelectorPanelOpen() {
        return getPanel().getScreen().isPanelOpen("circuit_selector");
    }

    private void createSelectorPanel() {
        ItemDrawable circuitPreview = new ItemDrawable(getSyncHandler().getSlot().getStack());

        List<List<IWidget>> options = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            options.add(new ArrayList<>());
            for (int j = 0; j < 9; j++) {
                int index = i * 9 + j;
                if (index > 32) break;
                options.get(i).add(new ButtonWidget<>()
                        .size(18)
                        .background(GTGuiTextures.SLOT, new ItemDrawable(
                                IntCircuitIngredient.getIntegratedCircuit(index)).asIcon())
                        .onMousePressed(mouseButton -> {
                            getSyncHandler().syncToServer(10, buf -> buf.writeShort(index));
                            circuitPreview.setItem(IntCircuitIngredient.getIntegratedCircuit(index));
                            return true;
                        }));
            }
        }

        getPanel().getScreen().openPanel(GTGuis.createPanel("circuit_selector", 176, 120)
                .child(IKey.lang("metaitem.circuit.integrated.gui").asWidget().pos(5, 5))
                .child(new IDrawable.DrawableWidget(circuitPreview.asIcon().size(16))
                        .size(18)
                        .top(19).alignX(0.5f)
                        .background(GTGuiTextures.SLOT))
                .child(new Grid()
                        .left(7).right(7).top(41).height(4 * 18)
                        .matrix(options)
                        .minColWidth(18).minRowHeight(18)
                        .minElementMargin(0, 0)));
    }
}
