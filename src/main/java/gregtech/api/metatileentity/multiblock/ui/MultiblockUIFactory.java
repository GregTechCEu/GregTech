package gregtech.api.metatileentity.multiblock.ui;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IDistinctBusController;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ProgressBarMultiblock;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MultiblockUIFactory<T extends MultiblockWithDisplayBase> {

    private final T mte;

    protected ModularPanel rootPanel;

    protected final PosGuiData guiData;
    protected final PanelSyncManager panelSyncManager;

    protected final Column rootColumn = new Column();

    protected final Row screenRow = new Row();
    protected int screenHeight = 117;

    protected final Row inventoryRow = new Row();

    protected final Column buttonColumn = new Column();

    public MultiblockUIFactory(@NotNull T mte, @NotNull PosGuiData guiData,
                               @NotNull PanelSyncManager panelSyncManager) {
        this.mte = mte;
        this.guiData = guiData;
        this.panelSyncManager = panelSyncManager;
    }

    public @NotNull ModularPanel buildUI() {
        this.rootPanel = createRootPanel().child(rootColumn);

        createBars();
        createScreen();
        // TODO createExtras() hook for overrides?
        createInventory();
        createButtons();
        createPowerButton();

        return rootPanel;
    }

    protected @NotNull ModularPanel createRootPanel() {
        return GTGuis.createPanel(mte, 198, 208);
    }

    protected void createBars() {
        if (!(mte instanceof ProgressBarMultiblock progressMulti)) {
            return;
        }

        final int count = progressMulti.getProgressBarCount();
        if (count < 1) {
            return;
        }

        final int rows = progressMulti.getProgressBarRows();
        final int cols = progressMulti.getProgressBarCols();

        Column column = new Column();
        rootColumn.child(column);

        for (int r = 0; r < rows; r++) {
            screenHeight -= (r + 1) * 8;

            Row row = new Row()
                    .size(Bars.FULL_WIDTH, Bars.HEIGHT)
                    .left(4);

            int from = r * cols;
            int to = Math.min(from + cols, cols);

            if (to - from > 1) {
                // TODO MUI2 bug workaround, should be able to apply this to every row but it crashes with single
                // element rows
                row.mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN);
            }

            for (int i = from; i < to; i++) {
                row.top(screenHeight + 6)
                        .child(progressMulti.createProgressBar(panelSyncManager, i)
                                .height(Bars.HEIGHT)
                                .width(cols == 3 ? Bars.THIRD_WIDTH : cols == 2 ? Bars.HALF_WIDTH : Bars.FULL_WIDTH)
                                .direction(ProgressWidget.Direction.RIGHT));
            }

            column.child(row);
        }
    }

    protected void createScreen() {
        screenRow.size(Screen.WIDTH, screenHeight)
                .left(4)
                .top(4)
                .background(GTGuiTextures.DISPLAY);
        rootColumn.child(screenRow);
    }

    protected void createInventory() {
        inventoryRow.size(Screen.WIDTH, 81)
                .left(4)
                .bottom(3);

        Column inventory = new Column()
                .child(SlotGroupWidget.playerInventory()
                        .left(3)
                        .bottom(4));

        inventoryRow.child(inventory);
        rootColumn.child(inventoryRow);
    }

    protected void createButtons() {
        buttonColumn.size(18, 54)
                .right(3)
                .top(0);
        inventoryRow.child(buttonColumn);

        createExtraButton();
        createDistinctButton();
        createVoidingButton();
    }

    protected void createExtraButton() {
        List<Widget<?>> list = new ArrayList<>();
        mte.createExtraButtons(rootPanel, panelSyncManager, list);
        if (list.isEmpty()) {
            list.add(new Widget<>()
                    .background(GTGuiTextures.BUTTON)
                    .overlay(GTGuiTextures.BUTTON_NO_FLEX)
                    .tooltip(t -> t.addLine(IKey.lang("gregtech.multiblock.universal.no_flex_button"))));
        }

        if (list.size() == 1) {
            buttonColumn.child(list.get(0).size(18, 18));
            return;
        }

        PanelSyncHandler popupPanel = panelSyncManager.panel("throttle_panel", rootPanel,
                (syncManager, syncHandler) -> {
                    ModularPanel panel = GTGuis.createPopupPanel("extra_buttons", Screen.WIDTH, screenHeight);
                    Row row = new Row().height(18)
                            .margin(4)
                            .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN);
                    panel.child(row);
                    for (var widget : list) {
                        row.child(widget.size(18, 18));
                    }
                    return panel;
                });

        buttonColumn.child(new ButtonWidget<>()
                .size(18, 18)
                .overlay(GTGuiTextures.BUTTON_THROTTLE_MINUS) // TODO texture
                .background(GTGuiTextures.BUTTON) // TODO make this work
                .onMousePressed(i -> {
                    if (popupPanel.isPanelOpen()) {
                        popupPanel.closePanel();
                    } else {
                        popupPanel.openPanel();
                    }
                    Interactable.playButtonClickSound();
                    return true;
                }));
    }

    protected void createDistinctButton() {
        if (mte instanceof IDistinctBusController distinct && distinct.canBeDistinct()) {
            BooleanSyncValue distinctValue = new BooleanSyncValue(distinct::isDistinct, distinct::setDistinct);
            panelSyncManager.syncValue("distinct_state", distinctValue);

            buttonColumn.child(new CycleButtonWidget()
                    .top(18)
                    .size(18, 18)
                    .value(new BoolValue.Dynamic(distinctValue::getBoolValue, distinctValue::setBoolValue))
                    .textureGetter(
                            i -> i == 0 ? GTGuiTextures.BUTTON_NO_DISTINCT_BUSES : GTGuiTextures.BUTTON_DISTINCT_BUSES)
                    .background(GTGuiTextures.BUTTON)
                    .tooltipBuilder(t -> t.setAutoUpdate(true)
                            .addLine(distinctValue.getBoolValue() ?
                                    IKey.lang("gregtech.multiblock.universal.distinct_enabled") :
                                    IKey.lang("gregtech.multiblock.universal.distinct_disabled"))));
        } else {
            buttonColumn.child(new Widget<>()
                    .top(18)
                    .size(18, 18)
                    .background(GTGuiTextures.BUTTON, GTGuiTextures.BUTTON_NO_DISTINCT_BUSES)
                    .tooltip(t -> t.addLine(IKey.lang("gregtech.multiblock.universal.distinct_not_supported"))));
        }
    }

    protected void createVoidingButton() {
        if (mte.shouldShowVoidingModeButton()) {
            IntSyncValue voidingValue = new IntSyncValue(mte::getVoidingMode, mte::setVoidingMode);
            panelSyncManager.syncValue("voiding_state", voidingValue);

            buttonColumn.child(new CycleButtonWidget()
                    .top(36)
                    .size(18, 18)
                    .textureGetter(i -> switch (i) {
                    case 1 -> GTGuiTextures.BUTTON_VOID_ITEM;
                    case 2 -> GTGuiTextures.BUTTON_VOID_FLUID;
                    case 3 -> GTGuiTextures.BUTTON_VOID_ITEM_FLUID;
                    default -> GTGuiTextures.BUTTON_VOID_DISABLED;
                    })
                    .background(GTGuiTextures.BUTTON)
                    .value(new IntValue.Dynamic(voidingValue::getIntValue, voidingValue::setIntValue))
                    .length(4)
                    .tooltipBuilder(t -> t.setAutoUpdate(true)
                            .addLine(IKey.lang(mte.getVoidingModeTooltip(voidingValue.getIntValue())))));
        } else {
            buttonColumn.child(new Widget<>()
                    .top(36)
                    .size(18, 18)
                    .background(GTGuiTextures.BUTTON, GTGuiTextures.BUTTON_VOID_NONE)
                    .tooltip(t -> t.addLine(IKey.lang("gregtech.gui.multiblock_voiding_not_supported"))));
        }
    }

    protected void createPowerButton() {
        // todo in the future, refactor so that this multis are instanceof IControllable.
        IControllable controllable = mte.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable != null) {
            BooleanSyncValue workingStateValue = new BooleanSyncValue(controllable::isWorkingEnabled,
                    controllable::setWorkingEnabled);
            panelSyncManager.syncValue("working_state", workingStateValue);

            Column column = new Column()
                    .size(18, 29)
                    .right(3)
                    .bottom(-2);

            inventoryRow.child(column);
            column.child(new CycleButtonWidget()
                    .top(5)
                    .size(18, 18)
                    .textureGetter(i -> i == 0 ? GTGuiTextures.BUTTON_POWER_OFF : GTGuiTextures.BUTTON_POWER_ON)
                    .background(GTGuiTextures.BUTTON)
                    .value(new BoolValue.Dynamic(workingStateValue::getBoolValue, workingStateValue::setBoolValue)))
                    .child(new Widget<>()
                            .background(GTGuiTextures.BUTTON_POWER_DETAIL)
                            .bottom(0)
                            .size(18, 6));
        }
    }

    public static final class Screen {

        public static int WIDTH = 190;

        private Screen() {}
    }

    public static final class Bars {

        public static int FULL_WIDTH = Screen.WIDTH;
        public static int HALF_WIDTH = 94;
        public static int THIRD_WIDTH = 62;
        public static int HEIGHT = 7;

        private Bars() {}
    }
}
