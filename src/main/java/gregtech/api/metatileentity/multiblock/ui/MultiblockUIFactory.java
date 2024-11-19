package gregtech.api.metatileentity.multiblock.ui;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IDistinctBusController;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayTextPort;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ProgressBarMultiblock;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.KeyUtil;

import net.minecraft.util.text.TextFormatting;

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
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MultiblockUIFactory {

    private final MultiblockWithDisplayBase mte;
    protected static final int DEFAULT_HEIGHT = 202;
    protected static final int DEFAULT_WIDTH = 198;

    public MultiblockUIFactory(@NotNull MultiblockWithDisplayBase mte) {
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

        // TODO createExtras() hook for overrides?
        var bars = createBars(panel, panelSyncManager);

        return panel.child(createScreen()
                .child(displayText))
                .childIf(bars != null, bars)
                .child(new Row()
                        .bottom(7)
                        .height(77)
                        .child(SlotGroupWidget.playerInventory(0).left(4))
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
     * Called on both sides to obtain text displayed in GUI <br />
     * Each element of list is displayed on new line <br />
     * To use translation, use {@link KeyUtil#coloredLang(TextFormatting, String, Object...)}
     * or {@link KeyUtil#unformattedLang(String, Object...)}
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
        return GTGuis.createPanel(mte, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * @param mainPanel        the main panel, needed for creating popup panels
     * @param panelSyncManager the sync manager for synchronizing widgets
     */
    @Nullable
    protected Column createBars(@NotNull ModularPanel mainPanel,
                                @NotNull PanelSyncManager panelSyncManager) {
        if (!(mte instanceof ProgressBarMultiblock progressMulti)) return null;

        final int count = progressMulti.getProgressBarCount();
        if (count < 1) return null;
        mainPanel.height(DEFAULT_HEIGHT + (Bars.HEIGHT * 2) - 2);

        final int rows = progressMulti.getProgressBarRows();
        final int cols = progressMulti.getProgressBarCols();

        Column column = new Column()
                .padding(4, 0)
                .pos(0, 114)
                .widthRel(1f)
                .height(Bars.HEIGHT * 2);
        int rowWidth = (Bars.FULL_WIDTH / cols) - (cols - 1);

        for (int r = 0; r < rows; r++) {

            Row row = new Row()
                    .widthRel(1f)
                    .height(Bars.HEIGHT);

            int from = r * cols;
            int to = Math.min(from + cols, cols);

            if (to - from > 1) {
                // TODO MUI2 bug workaround, should be able to apply this to every row but it crashes with single
                // element rows
                row.mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN);
            }

            for (int i = from; i < to; i++) {
                row.child(progressMulti.createProgressBar(panelSyncManager, i)
                        .height(Bars.HEIGHT)
                        .width(rowWidth)
                        .direction(ProgressWidget.Direction.RIGHT));
            }

            column.child(row);
        }
        return column;
    }

    protected ParentWidget<?> createScreen() {
        return new ScrollWidget<>(new VerticalScrollData())
                .background(GTGuiTextures.DISPLAY)
                .size(190, 109)
                .pos(4, 4);
    }

    @NotNull
    protected Column createButtons(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager) {
        var flexButton = createFlexButton(mainPanel, panelSyncManager);
        if (flexButton == null) {
            flexButton = GTGuiTextures.BUTTON_NO_FLEX.asWidget()
                    .size(18)
                    .marginBottom(5);
        }
        var powerButton = createPowerButton(mainPanel, panelSyncManager);

        return new Column()
                .right(4)
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
                    .size(18, 18)
                    .value(distinctValue)
                    .textureGetter(i -> GTGuiTextures.BUTTON_DISTINCT_BUSES[i])
                    .background(GTGuiTextures.BUTTON)
                    .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                    .tooltipBuilder(t -> t.addLine(distinctValue.getBoolValue() ?
                            IKey.lang("gregtech.multiblock.universal.distinct_enabled") :
                            IKey.lang("gregtech.multiblock.universal.distinct_disabled")));
        } else {
            return GTGuiTextures.BUTTON_NO_DISTINCT_BUSES.asWidget()
                    .size(18, 18)
                    .addTooltipLine(IKey.lang("gregtech.multiblock.universal.distinct_not_supported"));
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
