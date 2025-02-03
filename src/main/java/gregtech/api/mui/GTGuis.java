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

    public static ModularPanel createPopupPanel(String name, int width, int height) {
        return createPopupPanel(name, width, height, false, false);
    }

    public static ModularPanel createPopupPanel(String name, int width, int height, boolean disableBelow,
                                                boolean closeOnOutsideClick) {
        return new PopupPanel(name, width, height, disableBelow, closeOnOutsideClick);
    }

    public static ModularPanel defaultPopupPanel(String name) {
        return defaultPopupPanel(name, false, false);
    }

    public static ModularPanel defaultPopupPanel(String name, boolean disableBelow,
                                                 boolean closeOnOutsideClick) {
        return new PopupPanel(name, DEFAULT_WIDTH, DEFAULT_HIEGHT, disableBelow, closeOnOutsideClick);
    }

    private static class PopupPanel extends ModularPanel {

        private final boolean disableBelow;
        private final boolean closeOnOutsideClick;

        public PopupPanel(@NotNull String name, int width, int height, boolean disableBelow,
                          boolean closeOnOutsideClick) {
            super(name);
            size(width, height).align(Alignment.Center);
            background(GTGuiTextures.BACKGROUND_POPUP);
            child(ButtonWidget.panelCloseButton().top(5).right(5)
                    .onMousePressed(mouseButton -> {
                        if (mouseButton == 0 || mouseButton == 1) {
                            this.closeIfOpen(true);
                            if (isSynced() && getSyncHandler() instanceof IPanelHandler handler) {
                                handler.deleteCachedPanel();
                            }
                            return true;
                        }
                        return false;
                    }));
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
