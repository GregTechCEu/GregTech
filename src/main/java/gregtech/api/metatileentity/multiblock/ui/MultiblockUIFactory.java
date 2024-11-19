package gregtech.api.metatileentity.multiblock.ui;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IDistinctBusController;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayTextPort;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ProgressBarMultiblock;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MultiblockUIFactory<T extends MultiblockWithDisplayBase> {

    private final T mte;

    protected final Column rootColumn = new Column();

    protected final Row screenRow = new Row();
    protected int screenHeight = 117;

    protected final Row inventoryRow = new Row();

    protected final Column buttonColumn = new Column();

    public MultiblockUIFactory(@NotNull T mte) {
        this.mte = mte;
    }

    public @NotNull ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        var list = new ArrayList<Widget<?>>();
        configureDisplayText(list, panelSyncManager);
        var displayText = new Column()
                .padding(4, 4);

        list.forEach(displayText::child);

        var panel = createRootPanel();

        // todo indicator widget
        // IndicatorImageWidget(174, 101, 17, 17, getLogo())
        // .setWarningStatus(getWarningLogo(), this::addWarningText)
        // .setErrorStatus(getErrorLogo(), this::addErrorText))

        // todo voiding mode button
        // ImageCycleButtonWidget(173, 161, 18, 18)

        // createBars();
        // TODO createExtras() hook for overrides?
        // createInventory();

        return panel.child(createScreen()
                .child(displayText))
                .child(new Row()
                        .child(SlotGroupWidget.playerInventory().left(4))
                        .child(createButtons(panel, panelSyncManager)));
    }

    /**
     * Returns a list of text indicating any current warnings in this Multiblock.
     * Recommended to only display warnings if the structure is already formed.
     */
    protected void configureWarningText(List<Widget<?>> textList, PanelSyncManager manager) {
        MultiblockDisplayTextPort.builder(textList, mte.isStructureFormed(), false, manager)
                .addMaintenanceProblemLines(mte.getMaintenanceProblems());
    }

    /**
     * Returns a list of translation keys indicating any current errors in this Multiblock.
     * Prioritized over any warnings provided by {@link #configureWarningText(List, PanelSyncManager)}.
     */
    protected void configureErrorText(List<Widget<?>> textList, PanelSyncManager manager) {
        MultiblockDisplayTextPort.builder(textList, mte.isStructureFormed(), manager)
                .addMufflerObstructedLine(mte.hasMufflerMechanics() && !mte.isMufflerFaceFree());
    }

    /**
     * Called serverside to obtain text displayed in GUI
     * each element of list is displayed on new line
     * to use translation, use TextComponentTranslation
     */
    protected void configureDisplayText(List<Widget<?>> textList, PanelSyncManager manager) {
        MultiblockDisplayTextPort.builder(textList, mte.isStructureFormed(), manager);
    }

    /**
     * Add a custom third button to the Multiblock UI. By default, this is a placeholder stating that there is no
     * additional functionality for this Multiblock.
     * <br>
     * Size will be 18x18.
     *
     * @param mainPanel        the main panel, needed for creating popup panels
     * @param panelSyncManager the sync manager for synchronizing widgets
     */
    public @Nullable Widget<?> createFlexButton(@NotNull ModularPanel mainPanel,
                                                @NotNull PanelSyncManager panelSyncManager) {
        return null;
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
                // row.top(screenHeight + 6)
                // .child(progressMulti.createProgressBar(panelSyncManager, i)
                // .height(Bars.HEIGHT)
                // .width(cols == 3 ? Bars.THIRD_WIDTH : cols == 2 ? Bars.HALF_WIDTH : Bars.FULL_WIDTH)
                // .direction(ProgressWidget.Direction.RIGHT));
            }

            column.child(row);
        }
    }

    protected ParentWidget<?> createScreen() {
        return new ScrollWidget<>(new VerticalScrollData())
                .background(GTGuiTextures.DISPLAY)
                .size(190, 109)
                .pos(4, 4);
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

    @NotNull
    protected Column createButtons(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager) {
        var flexButton = createFlexButton(mainPanel, panelSyncManager);
        if (flexButton == null) {
            // 173, 125, 18, 18
            flexButton = GTGuiTextures.BUTTON_NO_FLEX.asWidget()
                    .size(18)
                    // .pos(173, 125)
                    .marginBottom(5);
        }
        var powerButton = createPowerButton(mainPanel, panelSyncManager);

        return new Column()
                .right(4)
                .bottom(7)
                .size(18, 77)
                .child(createDistinctButton(mainPanel, panelSyncManager))
                .child(createVoidingButton(mainPanel, panelSyncManager))
                .child(flexButton)
                .childIf(powerButton != null, powerButton);
    }

    // protected void createExtraButton() {
    // List<Widget<?>> list = new ArrayList<>();
    // mte.createExtraButtons(rootPanel, panelSyncManager, list);
    // if (list.isEmpty()) {
    // list.add(new Widget<>()
    // .background(GTGuiTextures.BUTTON)
    // .overlay(GTGuiTextures.BUTTON_NO_FLEX)
    // .tooltip(t -> t.addLine(IKey.lang("gregtech.multiblock.universal.no_flex_button"))));
    // }
    //
    // if (list.size() == 1) {
    // buttonColumn.child(list.get(0).size(18, 18));
    // return;
    // }
    //
    // PanelSyncHandler popupPanel = panelSyncManager.panel("throttle_panel", rootPanel,
    // (syncManager, syncHandler) -> {
    // ModularPanel panel = GTGuis.createPopupPanel("extra_buttons", Screen.WIDTH, screenHeight);
    // Row row = new Row().height(18)
    // .margin(4)
    // .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN);
    // panel.child(row);
    // for (var widget : list) {
    // row.child(widget.size(18, 18));
    // }
    // return panel;
    // });
    //
    // buttonColumn.child(new ButtonWidget<>()
    // .size(18, 18)
    // .overlay(GTGuiTextures.BUTTON_THROTTLE_MINUS) // TODO texture
    // .background(GTGuiTextures.BUTTON) // TODO make this work
    // .onMousePressed(i -> {
    // if (popupPanel.isPanelOpen()) {
    // popupPanel.closePanel();
    // } else {
    // popupPanel.openPanel();
    // }
    // Interactable.playButtonClickSound();
    // return true;
    // }));
    // }

    protected IWidget createDistinctButton(@NotNull ModularPanel mainPanel,
                                           @NotNull PanelSyncManager panelSyncManager) {
        if (mte instanceof IDistinctBusController distinct && distinct.canBeDistinct()) {
            BooleanSyncValue distinctValue = new BooleanSyncValue(distinct::isDistinct, distinct::setDistinct);

            return new CycleButtonWidget()
                    // .top(18)
                    .size(18, 18)
                    .value(distinctValue)
                    .textureGetter(
                            i -> i == 0 ? GTGuiTextures.BUTTON_NO_DISTINCT_BUSES : GTGuiTextures.BUTTON_DISTINCT_BUSES)
                    .background(GTGuiTextures.BUTTON)
                    .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                    .tooltipBuilder(t -> t.addLine(distinctValue.getBoolValue() ?
                            IKey.lang("gregtech.multiblock.universal.distinct_enabled") :
                            IKey.lang("gregtech.multiblock.universal.distinct_disabled")));
        } else {
            return new Widget<>()
                    // .top(18)
                    .size(18, 18)
                    .background(GTGuiTextures.BUTTON, GTGuiTextures.BUTTON_NO_DISTINCT_BUSES)
                    .tooltip(t -> t.addLine(IKey.lang("gregtech.multiblock.universal.distinct_not_supported")));
        }
    }

    protected IWidget createVoidingButton(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager) {
        if (mte.shouldShowVoidingModeButton()) {
            IntSyncValue voidingValue = new IntSyncValue(mte::getVoidingMode, mte::setVoidingMode);

            return new CycleButtonWidget()
                    .size(18, 18)
                    .textureGetter(i -> GTGuiTextures.MULTIBLOCK_VOID[i])
                    .background(GTGuiTextures.BUTTON)
                    .value(voidingValue)
                    .length(4)
                    .tooltipBuilder(t -> t.setAutoUpdate(true)
                            .addLine(IKey.lang(mte.getVoidingModeTooltip(voidingValue.getIntValue()))));
        } else {
            return GTGuiTextures.BUTTON_VOID_NONE.asWidget()
                    .size(18, 18)
                    .tooltip(t -> t.addLine(IKey.lang("gregtech.gui.multiblock_voiding_not_supported")));
        }
    }

    @Nullable
    protected Widget<?> createPowerButton(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager) {
        // todo in the future, refactor so that this multis are instanceof IControllable.
        IControllable controllable = mte.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable == null) return null;

        BooleanSyncValue workingStateValue = new BooleanSyncValue(controllable::isWorkingEnabled,
                controllable::setWorkingEnabled);

        return new CycleButtonWidget()
                .size(18)
                .textureGetter(i -> GTGuiTextures.BUTTON_POWER[i])
                .disableHoverBackground()
                .background(GTGuiTextures.BUTTON_POWER_DETAIL.asIcon().size(18, 6).marginTop(24), GTGuiTextures.BUTTON)
                .value(workingStateValue);
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
