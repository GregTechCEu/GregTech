package gregtech.api.mui;

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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

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
                            this.closeIfOpen();
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
