package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ServerWidgetGroup;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.layout.Row;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ItemFilterContainer extends BaseFilterContainer
                                 implements INBTSerializable<NBTTagCompound> {

    public ItemFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        super(dirtyNotifiable);
    }

    public boolean test(ItemStack toTest) {
        return !hasFilter() || getItemFilter().test(toTest);
    }

    public MatchResult<ItemStack> match(ItemStack toMatch) {
        if (!hasFilter())
            return MatchResult.create(true, toMatch, -1);

        return getItemFilter().match(toMatch);
    }

    public int getTransferLimit(ItemStack stack) {
        if (!hasFilter() || isBlacklistFilter()) {
            return getTransferSize();
        }
        return getItemFilter().getTransferLimit(stack, getTransferSize());
    }

    public @Nullable IItemFilter getItemFilter() {
        return (IItemFilter) super.getFilter();
    }

    /** @deprecated uses old builtin MUI */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public void initUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup) {
        widgetGroup.accept(new LabelWidget(10, y, "cover.conveyor.item_filter.title"));
        widgetGroup.accept(new SlotWidget(this, 0, 10, y + 15)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));

        this.initFilterUI(y + 38, widgetGroup);
    }

    /** @deprecated uses old builtin MUI */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public void initFilterUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup) {
        widgetGroup.accept(new WidgetGroupItemFilter(y, this::getItemFilter));
    }

    /** @deprecated uses old builtin MUI */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.10")
    public void blacklistUI(int y, Consumer<gregtech.api.gui.Widget> widgetGroup, BooleanSupplier showBlacklistButton) {
        ServerWidgetGroup blacklistButton = new ServerWidgetGroup(this::hasFilter);
        blacklistButton.addWidget(new ToggleButtonWidget(144, y, 20, 20, GuiTextures.BUTTON_BLACKLIST,
                this::isBlacklistFilter, this::setBlacklistFilter).setPredicate(showBlacklistButton)
                        .setTooltipText("cover.filter.blacklist"));
        widgetGroup.accept(blacklistButton);
    }

    /** Uses Cleanroom MUI */
    public IWidget initUI(ModularPanel main, GuiSyncManager manager) {
        var panel = new PanelSyncHandler(main) {

            // the panel can't be opened if there's no filter, so `getFilter()` will never be null
            @SuppressWarnings("DataFlowIssue")
            @Override
            public ModularPanel createUI(ModularPanel mainPanel, GuiSyncManager syncManager) {
                getItemFilter().setMaxTransferSize(getMaxTransferSize());
                return getItemFilter().createPopupPanel(syncManager);
            }
        };
        manager.syncValue("filter_panel", panel);
        var filterButton = new ButtonWidget<>();
        filterButton.setEnabled(hasFilter());

        return new Row().coverChildrenHeight()
                .marginBottom(2).widthRel(1f)
                .child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(this, 0)
                                .filter(FilterTypeRegistry::isItemFilter)
                                .singletonSlotGroup(101))
                        .onUpdateListener(w -> {
                            if (!hasFilter() && panel.isPanelOpen()) {
                                panel.closePanel();
                            }
                        }, true)
                        .size(18).marginRight(2)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.FILTER_SLOT_OVERLAY))
                .child(filterButton
                        .setEnabledIf(w -> hasFilter())
                        .onMousePressed(i -> {
                            boolean success = false;
                            if (!panel.isPanelOpen()) {
                                panel.openPanel();
                                success = true;
                            } else if (panel.isValid()) {
                                panel.closePanel();
                                success = true;
                            }
                            Interactable.playButtonClickSound();
                            return success;
                        }))
                .child(IKey.dynamic(() -> hasFilter() ?
                        getFilterStack().getDisplayName() :
                        IKey.lang("metaitem.item_filter.name").get())
                        .alignment(Alignment.CenterRight).asWidget()
                        .left(36).right(0).height(18));
    }
}
