package gregtech.common.covers.filter.fluid;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import gregtech.api.cover.filter.FilterHolder;
import gregtech.api.gui.GuiTextures;
import gregtech.api.util.IDirtyNotifiable;
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

    /*@Override
    public IWidget createFilterUI(ModularPanel mainPanel, GuiCreationContext creationContext, GuiSyncManager syncManager) {
        //return createFilterUI(mainPanel, creationContext, syncManager, null);
    }

    public IWidget createFilterUI(ModularPanel mainPanel, GuiCreationContext creationContext, GuiSyncManager syncManager, Consumer<IWidget> controlsAmountHandler) {
        IWidget widget = super.createFilterUI(mainPanel, creationContext, syncManager);
        //buildContext.addSyncedWindow(1, player -> openFilterWindow(player, controlsAmountHandler));
        return widget;
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
    }*/
}
