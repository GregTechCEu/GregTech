package gregtech.api.mui;

import com.cleanroommc.modularui.api.drawable.IKey;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Column;

import com.cleanroommc.modularui.widgets.layout.Row;

import gregtech.api.cover.Cover;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.mui.factory.CoverGuiFactory;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.mui.factory.MetaTileEntityGuiFactory;

import gregtech.api.mui.widget.QuantumFluidRendererWidget;

import gregtech.api.mui.widget.QuantumItemRendererWidget;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class GTGuis {

    @ApiStatus.Internal
    public static void registerFactories() {
        GuiManager.registerFactory(MetaTileEntityGuiFactory.INSTANCE);
        GuiManager.registerFactory(MetaItemGuiFactory.INSTANCE);
        GuiManager.registerFactory(CoverGuiFactory.INSTANCE);
    }

    public static ModularPanel createPanel(String name, int width, int height) {
        return ModularPanel.defaultPanel(name, width, height);
    }

    public static ModularPanel createPanel(MetaTileEntity mte, int width, int height) {
        return createPanel(mte.metaTileEntityId.getPath(), width, height);
    }

    public static ModularPanel createPanel(Cover cover, int width, int height) {
        return createPanel(cover.getDefinition().getResourceLocation().getPath(), width, height);
    }

    public static ModularPanel createPanel(ItemStack stack, int width, int height) {
        MetaItem<?>.MetaValueItem valueItem = ((MetaItem<?>) stack.getItem()).getItem(stack);
        if (valueItem == null) throw new IllegalArgumentException("Item must be a meta item!");
        return createPanel(valueItem.unlocalizedName, width, height);
    }

    public static ModularPanel createQuantumPanel(MetaTileEntity qstorage) {
        return createPanel(qstorage, 176, 166)
                .padding(4)
                .child(IKey.lang(qstorage.getMetaFullName()).asWidget());
    }

    public static Column createQuantumDisplay(String lang,
                                              Supplier<String> name, Predicate<TextWidget> condition,
                                              Supplier<String> count) {
        return new Column()
                .background(GTGuiTextures.DISPLAY)
                .padding(4)
                .height(46)
                .left(7).right(7)
                .top(16)
                .child(IKey.lang(lang)
                        .alignment(Alignment.TopLeft)
                        .color(Color.WHITE.main)
                        .asWidget()
                        .widthRel(0.5f)
                        .left(4)
                        .marginBottom(2))
                .child(IKey.dynamic(name)
                        .alignment(Alignment.TopLeft)
                        .color(Color.WHITE.main)
                        .asWidget()
                        .setEnabledIf(condition)
                        .widthRel(0.5f)
                        .left(4)
                        .height(20)
                        .marginBottom(2))
                .child(IKey.dynamic(count)
                        .alignment(Alignment.TopLeft)
                        .color(Color.WHITE.main)
                        .asWidget()
                        .widthRel(0.5f)
                        .left(4));
    }

    public static ParentWidget<?> createQuantumIO(IItemHandlerModifiable importHandler, IItemHandlerModifiable exportHandler) {
        return new Row()
                .pos(79, 18 + 45)
                .coverChildren()
                .child(new ItemSlot()
                        .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                        .slot(SyncHandlers.itemSlot(importHandler, 0)
                                .accessibility(true, false)
                                .singletonSlotGroup(200))
                        .marginRight(18))
                .child(new ItemSlot()
                        .background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
                        .slot(SyncHandlers.itemSlot(exportHandler, 0)
                                .accessibility(false, true)));
    }

    public static IWidget createQuantumRenderer(IItemHandlerModifiable handler) {
        return new QuantumItemRendererWidget(handler);
    }

    public static IWidget createQuantumRenderer(FluidTank handler) {
        return new QuantumFluidRendererWidget(handler);
    }

    public static ModularPanel createPopupPanel(String name, int width, int height) {
        return createPopupPanel(name, width, height, false, false);
    }

    public static ModularPanel createPopupPanel(String name, int width, int height, boolean disableBelow,
                                                boolean closeOnOutsideClick) {
        return new PopupPanel(name, width, height, disableBelow, closeOnOutsideClick);
    }

    private static class PopupPanel extends ModularPanel {

        private final boolean disableBelow;
        private final boolean closeOnOutsideClick;

        public PopupPanel(@NotNull String name, int width, int height, boolean disableBelow,
                          boolean closeOnOutsideClick) {
            super(name);
            size(width, height).align(Alignment.Center);
            background(GTGuiTextures.BACKGROUND_POPUP);
            child(ButtonWidget.panelCloseButton().top(5).right(5));
            this.disableBelow = disableBelow;
            this.closeOnOutsideClick = closeOnOutsideClick;
        }

        @Override
        public boolean disablePanelsBelow() {
            return disableBelow;
        }

        @Override
        public boolean closeOnOutOfBoundsClick() {
            return closeOnOutsideClick;
        }
    }
}
