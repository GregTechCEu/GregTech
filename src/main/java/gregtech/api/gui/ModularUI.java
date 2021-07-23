package gregtech.api.gui;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.*;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.util.Position;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * ModularUI is user-interface implementation concrete, based on widgets system
 * Each widget acts unique and manage different things
 * All widget information is synced to client from server for correct rendering
 * Widgets and UI are both-sided, so widgets should equal on both sides
 * However widget data will sync, widgets themselves, background, sizes and other important info will not
 * To open and create ModularUI, see {@link UIFactory}
 */
public final class ModularUI implements ISizeProvider {

    public final ImmutableBiMap<Integer, Widget> guiWidgets;

    public final TextureArea backgroundPath;
    private int screenWidth, screenHeight;
    private final int width, height;
    private final ImmutableList<Runnable> uiOpenCallback;
    private final ImmutableList<Runnable> uiCloseCallback;

    public boolean isJEIHandled;

    /**
     * UIHolder of this modular UI
     */
    public final IUIHolder holder;
    public final EntityPlayer entityPlayer;

    public ModularUI(ImmutableBiMap<Integer, Widget> guiWidgets, ImmutableList<Runnable> openListeners, ImmutableList<Runnable> closeListeners, TextureArea backgroundPath, int width, int height, IUIHolder holder, EntityPlayer entityPlayer) {
        this.guiWidgets = guiWidgets;
        this.uiOpenCallback = openListeners;
        this.uiCloseCallback = closeListeners;
        this.backgroundPath = backgroundPath;
        this.width = width;
        this.height = height;
        this.holder = holder;
        this.entityPlayer = entityPlayer;
    }

    public List<Widget> getFlatVisibleWidgetCollection() {
        List<Widget> widgetList = new ArrayList<>(guiWidgets.size());

        for (Widget widget : guiWidgets.values()) {
            widgetList.add(widget);

            if (widget instanceof AbstractWidgetGroup)
                widgetList.addAll(((AbstractWidgetGroup) widget).getContainedWidgets(false));
        }

        return widgetList;
    }

    public void updateScreenSize(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        Position displayOffset = new Position(getGuiLeft(), getGuiTop());
        guiWidgets.values().forEach(widget -> widget.setParentPosition(displayOffset));
    }

    public void initWidgets() {
        guiWidgets.values().forEach(widget -> {
            widget.setGui(this);
            widget.setSizes(this);
            widget.initWidget();
        });
    }

    public void triggerOpenListeners() {
        uiOpenCallback.forEach(Runnable::run);
    }

    public void triggerCloseListeners() {
        uiCloseCallback.forEach(Runnable::run);
    }

    public static Builder defaultBuilder() {
        return new Builder(GuiTextures.BACKGROUND, 176, 166);
    }

    public static Builder borderedBuilder() {
        return new Builder(GuiTextures.BORDERED_BACKGROUND, 195, 136);
    }

    public static Builder extendedBuilder() {
        return new Builder(GuiTextures.BACKGROUND, 176, 216);
    }

    public static Builder builder(TextureArea background, int width, int height) {
        return new Builder(background, width, height);
    }

    @Override
    public int getScreenWidth() {
        return screenWidth;
    }

    @Override
    public int getScreenHeight() {
        return screenHeight;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    /**
     * Simple builder for  ModularUI objects
     */
    public static class Builder {

        private ImmutableBiMap.Builder<Integer, Widget> widgets = ImmutableBiMap.builder();
        private ImmutableList.Builder<Runnable> openListeners = ImmutableList.builder();
        private ImmutableList.Builder<Runnable> closeListeners = ImmutableList.builder();
        private TextureArea background;
        private int width, height;
        private int nextFreeWidgetId = 0;

        public Builder(TextureArea background, int width, int height) {
            Preconditions.checkNotNull(background);
            this.background = background;
            this.width = width;
            this.height = height;
        }

        public Builder widget(Widget widget) {
            Preconditions.checkNotNull(widget);
            widgets.put(nextFreeWidgetId++, widget);
            return this;
        }

        public Builder label(int x, int y, String localizationKey) {
            return widget(new LabelWidget(x, y, localizationKey));
        }

        public Builder label(int x, int y, String localizationKey, int color) {
            return widget(new LabelWidget(x, y, localizationKey, color, new Object[0]));
        }

        public Builder image(int x, int y, int width, int height, TextureArea area) {
            return widget(new ImageWidget(x, y, width, height, area));
        }

        public Builder dynamicLabel(int x, int y, Supplier<String> text, int color) {
            return widget(new DynamicLabelWidget(x, y, text, color));
        }

        public Builder slot(IItemHandlerModifiable itemHandler, int slotIndex, int x, int y, TextureArea... overlays) {
            return widget(new SlotWidget(itemHandler, slotIndex, x, y).setBackgroundTexture(overlays));
        }

        public Builder progressBar(DoubleSupplier progressSupplier, int x, int y, int width, int height, TextureArea texture, MoveType moveType) {
            return widget(new ProgressWidget(progressSupplier, x, y, width, height, texture, moveType));
        }

        public Builder bindPlayerInventory(InventoryPlayer inventoryPlayer) {
            bindPlayerInventory(inventoryPlayer, GuiTextures.SLOT);
            return this;
        }

        public Builder bindPlayerInventory(InventoryPlayer inventoryPlayer, int startY) {
            bindPlayerInventory(inventoryPlayer, GuiTextures.SLOT, 7, startY);
            return this;
        }

        public Builder bindPlayerInventory(InventoryPlayer inventoryPlayer, TextureArea imageLocation) {
            return bindPlayerInventory(inventoryPlayer, imageLocation, 7, 84);
        }

        public Builder bindPlayerInventory(InventoryPlayer inventoryPlayer, TextureArea imageLocation, int x, int y) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    this.widget(new SlotWidget(inventoryPlayer, col + (row + 1) * 9, x + col * 18, y + row * 18)
                        .setBackgroundTexture(imageLocation)
                        .setLocationInfo(true, false));
                }
            }
            return bindPlayerHotbar(inventoryPlayer, imageLocation, x, y + 58);
        }

        public Builder bindPlayerHotbar(InventoryPlayer inventoryPlayer, TextureArea imageLocation, int x, int y) {
            for (int slot = 0; slot < 9; slot++) {
                this.widget(new SlotWidget(inventoryPlayer, slot, x + slot * 18, y)
                    .setBackgroundTexture(imageLocation)
                    .setLocationInfo(true, true));
            }
            return this;
        }

        public Builder bindOpenListener(Runnable onContainerOpen) {
            this.openListeners.add(onContainerOpen);
            return this;
        }

        public Builder bindCloseListener(Runnable onContainerClose) {
            this.closeListeners.add(onContainerClose);
            return this;
        }

        public ModularUI build(IUIHolder holder, EntityPlayer player) {
            return new ModularUI(widgets.build(), openListeners.build(), closeListeners.build(), background, width, height, holder, player);
        }
    }

}
