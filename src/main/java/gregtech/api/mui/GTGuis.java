package gregtech.api.mui;

import gregtech.api.cover.Cover;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.mui.factory.CoverGuiFactory;
import gregtech.api.mui.factory.MetaItemGuiFactory;
import gregtech.api.mui.factory.MetaTileEntityGuiFactory;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

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
            flex().startDefaultMode();
            flex().size(width, height).align(Alignment.Center);
            flex().endDefaultMode();
            background(GTGuiTextures.BACKGROUND_POPUP);
            child(new ButtonWidget<>()
                    .size(10, 10)
                    .top(5).right(5)
                    .background(GTGuiTextures.BUTTON) // todo
                    .overlay(GuiTextures.CROSS_TINY)
                    .onMousePressed(button -> {
                        closeIfOpen();
                        return true;
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
