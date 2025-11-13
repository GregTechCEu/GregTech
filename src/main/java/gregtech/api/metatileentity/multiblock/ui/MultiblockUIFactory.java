package gregtech.api.metatileentity.multiblock.ui;

import gregtech.api.capability.IControllable;
import gregtech.api.capability.IDistinctBusController;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ProgressBarMultiblock;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTLambdaUtils;
import gregtech.api.util.KeyUtil;
import gregtech.common.mui.widget.ScrollableTextWidget;

import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IBoolValue;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.drawable.Icon;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class MultiblockUIFactory {

    private static final Consumer<MultiblockUIBuilder> NO_OP = b -> {};
    private static final IBoolValue<Boolean> ALWAYS_ON = new BoolValue.Dynamic(() -> true, b -> {});

    private final MultiblockWithDisplayBase mte;
    protected Consumer<MultiblockUIBuilder> displayText, warningText, errorText;
    protected BiFunction<PosGuiData, PanelSyncManager, IWidget> flexButton = (guiData, syncManager) -> null;
    private int width = 198, height = 202;
    private int screenHeight = 109;
    private ScreenFunction screenFunction;
    private boolean disableDisplay = false;
    private boolean disableIndicator = false;
    private boolean disableButtons = false;

    public MultiblockUIFactory(@NotNull MultiblockWithDisplayBase mte) {
        this.mte = mte;
        configureDisplayText(builder -> builder.title(mte.getMetaFullName()).structureFormed(mte.isStructureFormed()));
    }

    private Widget<?> createIndicator(PanelSyncManager syncManager) {
        if (warningText == NO_OP && errorText == NO_OP) {
            return new Widget<>()
                    .debugName("indicator_none")
                    .size(18)
                    .pos(174 - 5, screenHeight - 18 - 3)
                    .overlay(GTGuiTextures.GREGTECH_LOGO_DARK);
        }

        MultiblockUIBuilder error = builder();
        error.sync("error", syncManager);
        error.setAction(this.errorText);

        MultiblockUIBuilder warning = builder();
        warning.sync("warning", syncManager);
        warning.setAction(this.warningText);
        warning.onRebuild(() -> warning.updateFormed(mte.isStructureFormed()));

        IDrawable indicator = new DynamicDrawable(() -> {
            if (!error.isEmpty()) {
                return GTGuiTextures.GREGTECH_LOGO_BLINKING_RED;
            } else if (!warning.isEmpty()) {
                return GTGuiTextures.GREGTECH_LOGO_BLINKING_YELLOW;
            } else {
                return GTGuiTextures.GREGTECH_LOGO_DARK;
            }
        });

        return new Widget<>()
                .debugName("indicator")
                .size(18)
                .pos(174 - 5, screenHeight - 18 - 3)
                .overlay(indicator)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(t -> {
                    if (!error.isEmpty()) {
                        error.build(t);
                    } else if (!warning.isEmpty()) {
                        warning.build(t);
                    }
                });
    }

    /**
     * Returns a list of text indicating any current warnings in this Multiblock. <br />
     * Recommended to only display warnings if the structure is already formed. <br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureWarningText(boolean merge, Consumer<MultiblockUIBuilder> warningText) {
        this.warningText = merge ? GTLambdaUtils.mergeConsumers(this.warningText, warningText) : warningText;
        return this;
    }

    /**
     * Returns a list of text indicating any current warnings in this Multiblock. <br />
     * Recommended to only display warnings if the structure is already formed. <br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureWarningText(Consumer<MultiblockUIBuilder> warningText) {
        return configureWarningText(true, warningText);
    }

    public MultiblockUIFactory disableWarningText() {
        this.warningText = NO_OP;
        return this;
    }

    /**
     * Returns a list of translation keys indicating any current errors in this Multiblock. <br />
     * Prioritized over any warnings provided by {@link #configureWarningText(Consumer)}.<br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureErrorText(boolean merge, Consumer<MultiblockUIBuilder> errorText) {
        this.errorText = merge ? GTLambdaUtils.mergeConsumers(this.errorText, errorText) : errorText;
        return this;
    }

    /**
     * Returns a list of translation keys indicating any current errors in this Multiblock. <br />
     * Prioritized over any warnings provided by {@link #configureWarningText(Consumer)}.<br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureErrorText(Consumer<MultiblockUIBuilder> errorText) {
        return configureErrorText(true, errorText);
    }

    public MultiblockUIFactory disableErrorText() {
        this.errorText = NO_OP;
        return this;
    }

    /**
     * Called per tick on client side <br />
     * Each element of list is displayed on new line <br />
     * To use translation, use {@link KeyUtil#lang(TextFormatting, String, Object...)}
     * or {@link KeyUtil#lang(String, Object...)}
     */
    public MultiblockUIFactory configureDisplayText(boolean merge, Consumer<MultiblockUIBuilder> displayText) {
        this.displayText = merge ? GTLambdaUtils.mergeConsumers(this.displayText, displayText) : displayText;
        return this;
    }

    /**
     * Called per tick on client side <br />
     * Each element of list is displayed on new line <br />
     * To use translation, use {@link KeyUtil#lang(TextFormatting, String, Object...)}
     * or {@link KeyUtil#lang(String, Object...)}
     */
    public MultiblockUIFactory configureDisplayText(Consumer<MultiblockUIBuilder> displayText) {
        return configureDisplayText(true, displayText);
    }

    public MultiblockUIFactory disableDisplayText() {
        this.displayText = NO_OP;
        return this;
    }

    public MultiblockUIFactory disableDisplay() {
        disableDisplayText();
        this.disableDisplay = true;
        return disableIndicator();
    }

    public MultiblockUIFactory disableIndicator() {
        disableWarningText();
        disableErrorText();
        this.disableIndicator = true;
        return this;
    }

    public MultiblockUIFactory disableButtons() {
        this.disableButtons = true;
        return this;
    }

    /**
     * Add a custom third button to the Multiblock UI. By default, this is a placeholder stating that there is no
     * additional functionality for this Multiblock. <br/>
     * Size will be 18x18. <br/>
     * Return {@code null} in the function to indicate no flex button
     */
    public MultiblockUIFactory createFlexButton(BiFunction<PosGuiData, PanelSyncManager, IWidget> flexButton) {
        this.flexButton = flexButton;
        return this;
    }

    public MultiblockUIFactory setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public MultiblockUIFactory setScreenHeight(int height) {
        int diff = height - this.screenHeight;
        this.height += diff;
        this.screenHeight += diff;
        return this;
    }

    public MultiblockUIFactory addScreenChildren(ScreenFunction function) {
        this.screenFunction = function;
        return this;
    }

    /**
     * Constructs the multiblock ui panel<br />
     * <i>It is not recommended to override this method</i>
     */
    public @NotNull ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        var panel = GTGuis.createPanel(mte, width, height)
                .debugName("root_panel")
                .childIf(!disableDisplay, () -> createScreen(panelSyncManager));

        // TODO createExtras() hook for overrides?
        if (mte instanceof ProgressBarMultiblock progressBarMultiblock && progressBarMultiblock.hasBars()) {
            panel.height(height + (Bars.HEIGHT * calculateRows(progressBarMultiblock.getProgressBarCount())) - 2);
            panel.child(createBars(progressBarMultiblock, panelSyncManager));
        }

        if (disableDisplay && screenFunction != null) {
            this.screenFunction.addWidgets(panel, panelSyncManager);
        }

        var playerInv = SlotGroupWidget.playerInventory(false);
        if (disableButtons) {
            playerInv.alignX(0.5f);
        } else {
            playerInv.left(4);
        }

        return panel.child(Flow.row()
                .debugName("bottom_row")
                .bottom(7)
                .coverChildrenHeight()
                .margin(4, 0)
                .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                .child(playerInv)
                .childIf(!disableButtons, () -> createButtons(panel, panelSyncManager, guiData)));
    }

    private static int calculateRows(int count) {
        if (count <= 3) {
            return 1;
        }

        if (count <= 8) {
            return 2;
        }

        throw new UnsupportedOperationException("Cannot compute progress bar rows for count " + count);
    }

    private static int calculateCols(int count, int row) {
        return switch (count) {
            case 0, 1, 2, 3 -> count;
            case 4 -> 2;
            case 5 -> row == 0 ? 3 : 2;
            case 6 -> 3;
            case 7 -> row == 0 ? 4 : 3;
            case 8 -> 4;
            default -> throw new UnsupportedOperationException("Cannot compute progress bar cols for count " + count);
        };
    }

    /**
     * @param progressMulti    the multiblock with progress bars
     * @param panelSyncManager the sync manager for synchronizing widgets
     */
    @Nullable
    protected Flow createBars(@NotNull ProgressBarMultiblock progressMulti,
                              @NotNull PanelSyncManager panelSyncManager) {
        final int count = progressMulti.getProgressBarCount();
        final int calculatedRows = calculateRows(count);

        Flow column = Flow.column()
                .debugName("bar_col")
                .margin(4, 0)
                .top(5 + screenHeight)
                .widthRel(1f)
                .height(Bars.HEIGHT * calculatedRows);

        List<UnaryOperator<TemplateBarBuilder>> barBuilders = new ArrayList<>(progressMulti.getProgressBarCount());
        progressMulti.registerBars(barBuilders, panelSyncManager);

        for (int r = 0; r < calculatedRows; r++) {

            final int calculatedCols = calculateCols(count, r);

            Flow row = Flow.row()
                    .debugName("bar_row:" + r)
                    .widthRel(1f)
                    .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                    .height(Bars.HEIGHT);

            // the numbers for the given row of bars
            int from = r * (count - calculatedCols);
            int to = from + calculatedCols;

            // calculate bar width
            int barCount = Math.max(1, to - from);
            int barWidth = (Bars.FULL_WIDTH / barCount) - (barCount - 1);

            for (int i = from; i < to; i++) {
                ProgressWidget widget;
                if (i < barBuilders.size()) {
                    widget = barBuilders.get(i)
                            .apply(new TemplateBarBuilder())
                            .build();
                } else {
                    widget = new ProgressWidget()
                            .addTooltipLine("Error! no bar for index: " + i)
                            .background(new Rectangle().setColor(Color.RED.main));
                }

                row.child(widget.size(barWidth, Bars.HEIGHT)
                        .debugName(mte.getClass().getSimpleName() + ":bar:" + i)
                        .direction(ProgressWidget.Direction.RIGHT));
            }

            column.child(row);
        }
        return column;
    }

    protected Widget<?> createScreen(PanelSyncManager syncManager) {
        var parent = new ParentWidget<>();

        if (displayText != NO_OP) {
            MultiblockUIBuilder display = builder();
            display.setAction(this.displayText);
            display.sync("display", syncManager);

            parent.child(new ScrollableTextWidget()
                    .debugName("display_text")
                    .sizeRel(1f)
                    .alignment(Alignment.TopLeft)
                    .margin(4)
                    .autoUpdate(true)
                    .textBuilder(display::build));
        }

        if (this.screenFunction != null) {
            this.screenFunction.addWidgets(parent, syncManager);
        }

        return parent.childIf(!disableIndicator, () -> createIndicator(syncManager))
                .debugName("display_root")
                .background(getDisplayBackground())
                .size(190, screenHeight)
                .pos(4, 4);
    }

    private UITexture getDisplayBackground() {
        return mte.getUITheme().getDisplayBackground();
    }

    @NotNull
    protected Flow createButtons(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager,
                                 PosGuiData guiData) {
        IWidget flexButton = this.flexButton.apply(guiData, panelSyncManager);
        if (flexButton == null) {
            flexButton = new ToggleButton()
                    .debugName("flex_none")
                    .value(ALWAYS_ON)
                    .overlay(GTGuiTextures.OVERLAY_NO_FLEX)
                    .size(18)
                    .addTooltipLine(IKey.lang("gregtech.multiblock.universal.no_flex_button"));
        }
        var powerButton = createPowerButton(mainPanel, panelSyncManager);

        return Flow.column()
                .debugName("button_col")
                .right(4)
                .coverChildren()
                .child(createDistinctButton(mainPanel, panelSyncManager))
                .child(createVoidingButton(mainPanel, panelSyncManager))
                .child(flexButton)
                .childIf(powerButton != null, powerButton);
    }

    protected IWidget createDistinctButton(@NotNull ModularPanel mainPanel,
                                           @NotNull PanelSyncManager panelSyncManager) {
        if (!(mte instanceof IDistinctBusController distinct) || !distinct.canBeDistinct()) {
            return new ToggleButton()
                    .debugName("distinct_none")
                    .value(ALWAYS_ON)
                    .size(18)
                    .overlay(GTGuiTextures.OVERLAY_DISTINCT_BUSES[0])
                    .addTooltipLine(IKey.lang("gregtech.multiblock.universal.distinct_not_supported"));
        }

        return new ToggleButton()
                .debugName("distinct_button")
                .size(18)
                .value(new BooleanSyncValue(distinct::isDistinct, distinct::setDistinct))
                .disableHoverBackground()
                .overlay(true, GTGuiTextures.OVERLAY_DISTINCT_BUSES[1])
                .overlay(false, GTGuiTextures.OVERLAY_DISTINCT_BUSES[0])
                .addTooltip(true, IKey.lang("gregtech.multiblock.universal.distinct_enabled"))
                .addTooltip(false, IKey.lang("gregtech.multiblock.universal.distinct_disabled"));
    }

    protected IWidget createVoidingButton(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager) {
        if (!mte.shouldShowVoidingModeButton()) {
            return new ToggleButton()
                    .debugName("voiding_none")
                    .value(ALWAYS_ON)
                    .size(18)
                    .overlay(GTGuiTextures.OVERLAY_VOID_NONE)
                    .addTooltipLine(IKey.lang("gregtech.gui.multiblock_voiding_not_supported"));
        }

        IntSyncValue voidingValue = new IntSyncValue(mte::getVoidingMode, mte::setVoidingMode);

        return new CycleButtonWidget()
                .debugName("voiding_button")
                .size(18)
                .value(voidingValue)
                .length(4)
                .stateOverlay(0, GTGuiTextures.MULTIBLOCK_VOID[0])
                .stateOverlay(1, GTGuiTextures.MULTIBLOCK_VOID[1])
                .stateOverlay(2, GTGuiTextures.MULTIBLOCK_VOID[2])
                .stateOverlay(3, GTGuiTextures.MULTIBLOCK_VOID[3])
                .tooltipBuilder(t -> t.addLine(IKey.lang(mte.getVoidingModeTooltip(voidingValue.getIntValue()))));
    }

    @Nullable
    protected Widget<?> createPowerButton(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager) {
        if (mte instanceof IControllable controllable) {
            Icon detail = GTGuiTextures.BUTTON_POWER_DETAIL.asIcon().size(18, 6).marginTop(24);
            BooleanSyncValue controllableSync = new BooleanSyncValue(controllable::isWorkingEnabled,
                    controllable::setWorkingEnabled);

            return new ToggleButton()
                    .debugName("power_button")
                    .size(18)
                    .disableHoverBackground()
                    .overlay(true, detail, GTGuiTextures.BUTTON_POWER[1])
                    .overlay(false, detail, GTGuiTextures.BUTTON_POWER[0])
                    .value(controllableSync)
                    .marginTop(4);
        }

        return null;
    }

    public static final class Screen {

        public static int WIDTH = 190;

        private Screen() {}
    }

    public static final class Bars {

        public static int FULL_WIDTH = Screen.WIDTH;
        public static int HEIGHT = 7;

        private Bars() {}
    }

    @FunctionalInterface
    public interface ScreenFunction {

        void addWidgets(ParentWidget<?> parent, PanelSyncManager syncManager);
    }

    public static MultiblockUIBuilder builder() {
        return new MultiblockUIBuilder();
    }

    public static MultiblockUIBuilder builder(String key, PanelSyncManager syncManager) {
        var b = builder();
        b.sync(key, syncManager);
        return b;
    }
}
