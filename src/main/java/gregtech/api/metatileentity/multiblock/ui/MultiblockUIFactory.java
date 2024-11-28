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

import com.cleanroommc.modularui.api.drawable.IDrawable;
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
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MultiblockUIFactory {

    private final MultiblockWithDisplayBase mte;
    protected final BooleanSyncValue mufflerObstructed;
    protected final IntSyncValue maintanence;
    protected Consumer<PanelSyncManager> valueSyncer = syncManager -> {};
    protected Consumer<MultiblockDisplayTextPort.Builder> displayText = builder -> {};
    protected Consumer<MultiblockDisplayTextPort.Builder> warningText = builder -> {};
    protected Consumer<MultiblockDisplayTextPort.Builder> errorText = builder -> {};
    protected BiFunction<ModularPanel, PanelSyncManager, Widget<?>> flexButton = (panel, syncManager) -> null;

    protected static final int DEFAULT_HEIGHT = 202;
    protected static final int DEFAULT_WIDTH = 198;

    public MultiblockUIFactory(@NotNull MultiblockWithDisplayBase mte) {
        this.mte = mte;
        this.mufflerObstructed = new BooleanSyncValue(mte::isStructureObstructed, null);
        this.maintanence = new IntSyncValue(mte::getMaintenanceProblems, null);
    }

    /**
     * Called once during ui construction.
     */
    public MultiblockUIFactory syncValues(Consumer<PanelSyncManager> valueSyncer) {
        this.valueSyncer = syncManager -> {
            syncManager.syncValue("muffler", mufflerObstructed);
            syncManager.syncValue("maintenance", maintanence);
            valueSyncer.accept(syncManager);
        };
        return this;
    }

    /**
     * Constructs the multiblock ui panel<br />
     * <i>It is not recommended to override this method</i>
     */
    public @NotNull ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        this.valueSyncer.accept(panelSyncManager);
        var panel = createRootPanel();

        var displayText = new ArrayList<Widget<?>>();

        // TODO createExtras() hook for overrides?
        var bars = createBars(panel, panelSyncManager);

        return panel.child(createScreen(displayText, panelSyncManager))
                .childIf(bars != null, bars)
                .child(new Row()
                        .bottom(7)
                        .height(77)
                        .margin(4, 0)
                        .child(SlotGroupWidget.playerInventory(0)
                                .alignX(0f))
                        .child(createButtons(panel, panelSyncManager)));
    }

    private Widget<?> createIndicator() {
        List<Widget<?>> textList = new ArrayList<>();
        return new Widget<>()
                .pos(174 - 5, 93 - 5)
                .onUpdateListener(w -> w.overlay(getIndicatorOverlay(textList)))
                .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                .tooltipBuilder(tooltip -> {
                    for (var text : textList) {
                        if (text instanceof TextWidget textWidget)
                            tooltip.addLine(textWidget.getKey());
                    }
                });
    }

    private IDrawable getIndicatorOverlay(List<Widget<?>> textList) {
        if (!textList.isEmpty()) textList.clear();

        var builder = MultiblockDisplayTextPort.builder(textList, mte, true, false);
        this.errorText.accept(builder);
        if (!textList.isEmpty()) {
            // error
            return GTGuiTextures.GREGTECH_LOGO_BLINKING_RED;
        }
        this.warningText.accept(builder);
        if (!textList.isEmpty()) {
            // warn
            return GTGuiTextures.GREGTECH_LOGO_BLINKING_YELLOW;
        }

        // todo getLogo()?
        return GTGuiTextures.GREGTECH_LOGO_DARK;
    }

    /**
     * Returns a list of text indicating any current warnings in this Multiblock. <br />
     * Recommended to only display warnings if the structure is already formed. <br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureWarningText(Consumer<MultiblockDisplayTextPort.Builder> warningText) {
        this.warningText = builder -> {
            builder.addMaintenanceProblemLines((byte) maintanence.getIntValue());
            warningText.accept(builder);
        };
        return this;
    }

    /**
     * Returns a list of translation keys indicating any current errors in this Multiblock. <br />
     * Prioritized over any warnings provided by {@link #configureWarningText(Consumer)}.<br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureErrorText(Consumer<MultiblockDisplayTextPort.Builder> errorText) {
        this.errorText = builder -> {
            builder.addMufflerObstructedLine(mufflerObstructed.getBoolValue());
            errorText.accept(builder);
        };
        return this;
    }

    /**
     * Called per tick on client side <br />
     * Each element of list is displayed on new line <br />
     * To use translation, use {@link KeyUtil#lang(TextFormatting, String, Object...)}
     * or {@link KeyUtil#lang(String, Object...)}
     */
    public MultiblockUIFactory configureDisplayText(Consumer<MultiblockDisplayTextPort.Builder> displayText) {
        this.displayText = displayText;
        return this;
    }

    /**
     * Add a custom third button to the Multiblock UI. By default, this is a placeholder stating that there is no
     * additional functionality for this Multiblock.
     * <br>
     * Size will be 18x18.
     */
    public MultiblockUIFactory createFlexButton(BiFunction<ModularPanel, PanelSyncManager, Widget<?>> flexButton) {
        this.flexButton = flexButton;
        return this;
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
                .margin(4, 0)
                .top(114)
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

    protected Widget<?> createScreen(List<Widget<?>> lines, PanelSyncManager syncManager) {
        var displayText = new Column()
                .expanded()
                .onUpdateListener(column -> {
                    column.getChildren().clear();
                    lines.clear();
                    // really debating on if the display screen should be its own widget
                    this.displayText.accept(MultiblockDisplayTextPort.builder(lines, mte));
                    lines.forEach(column::child);
                    resize(column);
                })
                .margin(4, 4);

        // lines.forEach(displayText::child);
        return new ParentWidget<>()
                .child(createIndicator())
                .child(new ScrollWidget<>(new VerticalScrollData())
                        .sizeRel(1f)
                        .child(displayText))
                .background(GTGuiTextures.DISPLAY)
                .size(190, 109)
                .pos(4, 4);
    }

    private void resize(IWidget parent) {
        int top = parent.getArea().getPadding().top;
        for (IWidget widget : parent.getChildren()) {
            widget.resizer().resize(widget);
            var area = widget.getArea();
            area.rx = parent.getArea().getPadding().left;
            area.ry = top;
            area.applyPos(parent);
            top += area.requestedHeight();
        }
    }

    @NotNull
    protected Column createButtons(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager) {
        var flexButton = this.flexButton.apply(mainPanel, panelSyncManager);
        if (flexButton == null) {
            flexButton = GTGuiTextures.BUTTON_NO_FLEX.asWidget()
                    .size(18);
        }
        var powerButton = createPowerButton(mainPanel, panelSyncManager);

        return new Column()
                .alignX(1f)
                .right(0)
                .size(18, 77)
                .child(createDistinctButton(mainPanel, panelSyncManager))
                .child(createVoidingButton(mainPanel, panelSyncManager))
                .child(flexButton)
                .childIf(powerButton != null, powerButton);
    }

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
                .value(workingStateValue)
                .marginTop(5);
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
