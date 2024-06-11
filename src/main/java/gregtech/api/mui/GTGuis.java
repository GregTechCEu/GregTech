package gregtech.api.mui;

import com.cleanroommc.modularui.api.drawable.IKey;

import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;

import com.cleanroommc.modularui.widgets.layout.Row;

import gregtech.api.cover.Cover;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.mui.factory.CoverGuiFactory;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.mui.factory.MetaTileEntityGuiFactory;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;

import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class GTGuis {

    public static final int DEFAULT_WIDTH = 176, DEFAULT_HIEGHT = 166;

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
        String locale;
        if (stack.getItem() instanceof MetaItem<?>metaItem) {
            var valueItem = metaItem.getItem(stack);
            if (valueItem == null) throw new IllegalArgumentException("Item must be a meta item!");
            locale = valueItem.unlocalizedName;
        } else {
            locale = stack.getTranslationKey();
        }
        return createPanel(locale, width, height);
    }

    public static ModularPanel createPanel(String name) {
        return ModularPanel.defaultPanel(name, DEFAULT_WIDTH, DEFAULT_HIEGHT);
    }

    public static ModularPanel defaultPanel(MetaTileEntity mte) {
        return createPanel(mte.metaTileEntityId.getPath());
    }

    public static ModularPanel defaultPanel(Cover cover) {
        return createPanel(cover.getDefinition().getResourceLocation().getPath());
    }

    public static ModularPanel defaultPanel(ItemStack stack) {
        return createPanel(stack, DEFAULT_WIDTH, DEFAULT_HIEGHT);
    }

    public static ModularPanel defaultPanel(MetaItem<?>.MetaValueItem valueItem) {
        return createPanel(valueItem.unlocalizedName);
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

    public static Row createQuantumButtonRow(boolean isFluid, BooleanSyncValue autoOutput,
                                             BooleanSyncValue isLocked, BooleanSyncValue isVoiding) {
        return new Row()
                .coverChildren()
                .pos(7, 63)
                .child(new ToggleButton()
                        .overlay(isFluid ? GTGuiTextures.BUTTON_FLUID_OUTPUT : GTGuiTextures.BUTTON_ITEM_OUTPUT)
                        .value(autoOutput))
                .child(new ToggleButton()
                        .overlay(GTGuiTextures.FLUID_LOCK_OVERLAY)
                        .value(isLocked))
                .child(new ToggleButton()
                        .overlay(isFluid ? GTGuiTextures.FLUID_VOID_OVERLAY : GTGuiTextures.ITEM_VOID_OVERLAY)
                        .value(isVoiding));
    }

    public static PopupPanel createPopupPanel(String name, int width, int height) {
        return defaultPopupPanel(name)
                .size(width, height);
    }

    public static PopupPanel createPopupPanel(String name, int width, int height, boolean deleteCachedPanel) {
        return createPopupPanel(name, width, height)
                .deleteCachedPanel(deleteCachedPanel);
    }

    public static PopupPanel defaultPopupPanel(String name) {
        return new PopupPanel(name)
                .size(DEFAULT_WIDTH, DEFAULT_HIEGHT);
    }

    public static PopupPanel defaultPopupPanel(String name, boolean disableBelow,
                                               boolean closeOnOutsideClick, boolean deleteCachedPanel) {
        return defaultPopupPanel(name)
                .disablePanelsBelow(disableBelow)
                .closeOnOutOfBoundsClick(closeOnOutsideClick)
                .deleteCachedPanel(deleteCachedPanel);
    }

    public static class PopupPanel extends ModularPanel {

        private boolean disableBelow;
        private boolean closeOnOutsideClick;
        private boolean deleteCachedPanel;

        private PopupPanel(@NotNull String name) {
            super(name);
            align(Alignment.Center);
            background(GTGuiTextures.BACKGROUND_POPUP);
            child(ButtonWidget.panelCloseButton().top(5).right(5)
                    .onMousePressed(mouseButton -> {
                        if (mouseButton == 0 || mouseButton == 1) {
                            this.closeIfOpen(true);
                            return true;
                        }
                        return false;
                    }));
        }

        @Override
        public void onClose() {
            super.onClose();
            if (deleteCachedPanel && isSynced() && getSyncHandler() instanceof IPanelHandler handler) {
                handler.deleteCachedPanel();
            }
        }

        public PopupPanel disablePanelsBelow(boolean disableBelow) {
            this.disableBelow = disableBelow;
            return this;
        }

        public PopupPanel closeOnOutOfBoundsClick(boolean closeOnOutsideClick) {
            this.closeOnOutsideClick = closeOnOutsideClick;
            return this;
        }

        public PopupPanel deleteCachedPanel(boolean deleteCachedPanel) {
            this.deleteCachedPanel = deleteCachedPanel;
            return this;
        }

        @Override
        public PopupPanel size(int w, int h) {
            super.size(w, h);
            return this;
        }

        @Override
        public PopupPanel size(int val) {
            super.size(val);
            return this;
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
