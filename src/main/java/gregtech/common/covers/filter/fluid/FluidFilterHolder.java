package gregtech.common.covers.filter.fluid;

import com.cleanroommc.modularui.api.ModularUITextures;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.ButtonWidget;
import com.cleanroommc.modularui.common.widget.MultiChildWidget;
import com.cleanroommc.modularui.common.widget.SlotWidget;
import com.cleanroommc.modularui.common.widget.TextWidget;
import gregtech.api.gui.GuiTextures;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.common.covers.filter.FilterHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.function.Consumer;

public class FluidFilterHolder extends FilterHolder<FluidStack, FluidFilter> {

    public FluidFilterHolder(IDirtyNotifiable dirtyNotifiable) {
        super(dirtyNotifiable);
    }

    public FluidFilterHolder(IItemHandlerModifiable filterInventory, int filterSlotIndex, IDirtyNotifiable dirtyNotifiable) {
        super(filterInventory, filterSlotIndex, dirtyNotifiable);
    }

    @Override
    public Class<FluidFilter> getFilterClass() {
        return FluidFilter.class;
    }

    @Override
    public Widget createFilterUI(UIBuildContext buildContext) {
        return createFilterUI(buildContext, null);
    }

    public Widget createFilterUI(UIBuildContext buildContext, Consumer<Widget> controlsAmountHandler) {
        buildContext.addSyncedWindow(1, player -> openFilterWindow(player, controlsAmountHandler));
        return new MultiChildWidget()
                .addChild(new TextWidget(Text.localised("cover.filter.label"))
                        .setPos(0, 4))
                .addChild(new SlotWidget(filterInventory, filterSlotIndex)
                        .setFilter(item -> getFilterOf(item) != null)
                        .setChangeListener(slotWidget -> {
                            checkFilter(filterInventory.getStackInSlot(filterSlotIndex));
                            if (!slotWidget.isClient()) {
                                if (slotWidget.getContext().isWindowOpen(1)) {
                                    slotWidget.getContext().closeWindow(1);
                                }
                                if (hasFilter()) {
                                    slotWidget.getContext().openSyncedWindow(1);
                                }
                            }
                        })
                        .setPos(62, 0))
                .addChild(new ButtonWidget()
                        .setOnClick((clickData, widget) -> {
                            if (!widget.isClient() && hasFilter())
                                widget.getContext().openSyncedWindow(1);
                        })
                        .setTicker(widget -> widget.setEnabled(hasFilter()))
                        .setBackground(GuiTextures.BASE_BUTTON, Text.localised("cover.filter.settings_open.label").color(Color.WHITE.normal).shadow())
                        .setPos(82, 0)
                        .setSize(80, 18));
    }

    public ModularWindow openFilterWindow(EntityPlayer player, Consumer<Widget> controlsAmountHandler) {
        if (!hasFilter()) {
            return ModularWindow.builder(130, 20)
                    .setBackground(GuiTextures.VANILLA_BACKGROUND)
                    .widget(new TextWidget(new Text("An Error occurred!").color(Color.RED.normal))
                            .setTextAlignment(Alignment.Center)
                            .setSize(130, 20))
                    .widget(ButtonWidget.closeWindowButton(true)
                            .setPos(73, 4))
                    .build();
        }
        Widget filterUI = getCurrentFilter().createFilterUI(player, controlsAmountHandler);
        int height = filterUI.getSize().height > 0 ? filterUI.getSize().height + 25 : 90;
        int width = filterUI.getSize().width > 0 ? filterUI.getSize().width + 10 : 150;
        ModularWindow.Builder builder = ModularWindow.builder(width, height);
        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND)
                .setPos((screenSize, mainWindow) -> new Pos2d(screenSize.width / 2 - width / 2, mainWindow.getPos().y - 5))
                .widget(new TextWidget(Text.localised("cover.filter.settings.label"))
                        .setPos(5, 5))
                .widget(ButtonWidget.closeWindowButton(true)
                        .setPos(133, 5))
                .widget(filterUI
                        .setPos(5, 20));
        return builder.build();
    }
}
