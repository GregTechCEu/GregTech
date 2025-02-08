package gregtech.api.metatileentity.multiblock.ui;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IDistinctBusController;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.metatileentity.multiblock.ProgressBarMultiblock;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTLog;
import gregtech.api.util.JsonUtils;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.RichTextWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class MultiblockUIFactory {

    private final MultiblockWithDisplayBase mte;
    protected Consumer<Builder> displayText, warningText, errorText;
    protected BiFunction<PosGuiData, PanelSyncManager, IWidget> flexButton = (guiData, syncManager) -> null;
    private int width = 198, height = 202;
    private int screenHeight = 109;
    private Consumer<List<IWidget>> childrenConsumer;

    public MultiblockUIFactory(@NotNull MultiblockWithDisplayBase mte) {
        this.mte = mte;
        configureErrorText(builder -> {
            if (mte.hasMufflerMechanics())
                builder.addMufflerObstructedLine(!mte.isMufflerFaceFree());
        });
        configureWarningText(builder -> {
            if (mte.hasMaintenanceMechanics())
                builder.addMaintenanceProblemLines(mte.getMaintenanceProblems());
        });
        configureDisplayText(builder -> builder.title(mte.getMetaFullName()).structureFormed(mte.isStructureFormed()));
    }

    private static @NotNull <T> Consumer<T> addAction(@Nullable Consumer<T> first, @NotNull Consumer<T> andThen) {
        return first == null ? andThen : first.andThen(andThen);
    }

    /**
     * Constructs the multiblock ui panel<br />
     * <i>It is not recommended to override this method</i>
     */
    public @NotNull ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        // this.valueSyncer.accept(panelSyncManager);
        var panel = createRootPanel();

        panel.child(createScreen(panelSyncManager));

        // TODO createExtras() hook for overrides?
        if (mte instanceof ProgressBarMultiblock progressBarMultiblock &&
                progressBarMultiblock.getProgressBarCount() > 0) {
            panel.height(height + (Bars.HEIGHT * 2) - 2);
            panel.child(createBars(progressBarMultiblock, panelSyncManager));
        }

        return panel.child(Flow.row()
                .bottom(7)
                .height(77)
                .margin(4, 0)
                .child(SlotGroupWidget.playerInventory(0)
                        .alignX(0f))
                .child(createButtons(panel, panelSyncManager, guiData)));
    }

    private Widget<?> createIndicator(PanelSyncManager syncManager) {
        Builder error = builder();
        error.sync("error", syncManager);
        error.setAction(this.errorText);
        error.onRebuild(() -> error.isStructureFormed = mte.isStructureFormed());

        Builder warning = builder();
        warning.sync("warning", syncManager);
        warning.setAction(this.warningText);
        warning.onRebuild(() -> warning.isStructureFormed = mte.isStructureFormed());

        IDrawable indicator = new DynamicDrawable(() -> {
            if (!error.isEmpty()) {
                return GTGuiTextures.GREGTECH_LOGO_BLINKING_RED;
            } else if (!warning.isEmpty()) {
                return GTGuiTextures.GREGTECH_LOGO_BLINKING_YELLOW;
            } else {
                // todo getLogo()?
                return GTGuiTextures.GREGTECH_LOGO;
            }
        });

        return new Widget<>()
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
    public MultiblockUIFactory configureWarningText(boolean merge, Consumer<Builder> warningText) {
        this.warningText = merge ? addAction(this.warningText, warningText) : warningText;
        return this;
    }

    /**
     * Returns a list of text indicating any current warnings in this Multiblock. <br />
     * Recommended to only display warnings if the structure is already formed. <br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureWarningText(Consumer<Builder> warningText) {
        return configureWarningText(true, warningText);
    }

    /**
     * Returns a list of translation keys indicating any current errors in this Multiblock. <br />
     * Prioritized over any warnings provided by {@link #configureWarningText(Consumer)}.<br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureErrorText(boolean merge, Consumer<Builder> errorText) {
        this.errorText = merge ? addAction(this.errorText, errorText) : errorText;
        return this;
    }

    /**
     * Returns a list of translation keys indicating any current errors in this Multiblock. <br />
     * Prioritized over any warnings provided by {@link #configureWarningText(Consumer)}.<br />
     * This is called every tick on the client-side
     */
    public MultiblockUIFactory configureErrorText(Consumer<Builder> errorText) {
        return configureErrorText(true, errorText);
    }

    /**
     * Called per tick on client side <br />
     * Each element of list is displayed on new line <br />
     * To use translation, use {@link KeyUtil#lang(TextFormatting, String, Object...)}
     * or {@link KeyUtil#lang(String, Object...)}
     */
    public MultiblockUIFactory configureDisplayText(boolean merge, Consumer<Builder> displayText) {
        this.displayText = merge ? addAction(this.displayText, displayText) : displayText;
        return this;
    }

    /**
     * Called per tick on client side <br />
     * Each element of list is displayed on new line <br />
     * To use translation, use {@link KeyUtil#lang(TextFormatting, String, Object...)}
     * or {@link KeyUtil#lang(String, Object...)}
     */
    public MultiblockUIFactory configureDisplayText(Consumer<Builder> displayText) {
        return configureDisplayText(true, displayText);
    }

    /**
     * Add a custom third button to the Multiblock UI. By default, this is a placeholder stating that there is no
     * additional functionality for this Multiblock.
     * <br>
     * Size will be 18x18.
     */
    public MultiblockUIFactory createFlexButton(
                                                BiFunction<PosGuiData, PanelSyncManager, IWidget> flexButton) {
        this.flexButton = flexButton;
        return this;
    }

    public MultiblockUIFactory setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public MultiblockUIFactory setScreenHeight(int height) {
        this.screenHeight = height;
        return this;
    }

    protected @NotNull ModularPanel createRootPanel() {
        return GTGuis.createPanel(mte, width, height);
    }

    /**
     * @param progressMulti    the multiblock with progress bars
     * @param panelSyncManager the sync manager for synchronizing widgets
     */
    @Nullable
    protected Flow createBars(@NotNull ProgressBarMultiblock progressMulti,
                              @NotNull PanelSyncManager panelSyncManager) {
        final int rows = progressMulti.getProgressBarRows();
        final int cols = progressMulti.getProgressBarCols();

        Flow column = Flow.column()
                .margin(4, 0)
                .top(114)
                .widthRel(1f)
                .height(Bars.HEIGHT * 2);

        for (int r = 0; r < rows; r++) {

            Flow row = Flow.row()
                    .widthRel(1f)
                    .mainAxisAlignment(Alignment.MainAxis.SPACE_BETWEEN)
                    .height(Bars.HEIGHT);

            // the numbers for the given row of bars
            int from = r * cols;
            int to = Math.min(from + cols, cols);

            // calculate bar width
            int barCount = Math.max(1, to - from);
            int barWidth = (Bars.FULL_WIDTH / barCount) - (barCount - 1);

            for (int i = from; i < to; i++) {
                row.child(progressMulti.createProgressBar(panelSyncManager, i)
                        .height(Bars.HEIGHT)
                        .width(barWidth)
                        .direction(ProgressWidget.Direction.RIGHT));
            }

            column.child(row);
        }
        return column;
    }

    public MultiblockUIFactory addScreenChildren(Consumer<List<IWidget>> consumer) {
        this.childrenConsumer = consumer;
        return this;
    }

    protected Widget<?> createScreen(PanelSyncManager syncManager) {
        Builder display = builder();
        display.setAction(this.displayText);
        display.sync("display", syncManager);

        // todo scrolling doesn't work for rich text widget
        var scrollWidget = new ScrollWidget<>(new VerticalScrollData())
                .sizeRel(1f)
                .child(new RichTextWidget()
                        .sizeRel(1f)
                        .alignment(Alignment.TopLeft)
                        .margin(4, 4)
                        .autoUpdate(true)
                        .textBuilder(display::build));

        if (this.childrenConsumer != null) {
            List<IWidget> extra = new ArrayList<>();
            this.childrenConsumer.accept(extra);
            extra.forEach(scrollWidget::child);
        }

        return new ParentWidget<>()
                .child(scrollWidget)
                .child(createIndicator(syncManager))
                .background(GTGuiTextures.DISPLAY)
                .size(190, screenHeight)
                .pos(4, 4);
    }

    @NotNull
    protected Flow createButtons(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager,
                                 PosGuiData guiData) {
        IWidget flexButton = this.flexButton.apply(guiData, panelSyncManager);
        if (flexButton == null) {
            flexButton = GTGuiTextures.BUTTON_NO_FLEX.asWidget()
                    .size(18)
                    .addTooltipLine(IKey.lang("gregtech.multiblock.universal.no_flex_button"));
        }
        var powerButton = createPowerButton(mainPanel, panelSyncManager);

        return Flow.column()
                .alignX(1f)
                .right(4)
                .size(18, 77)
                .child(createDistinctButton(mainPanel, panelSyncManager))
                .child(createVoidingButton(mainPanel, panelSyncManager))
                .child(flexButton)
                .childIf(powerButton != null, powerButton);
    }

    protected IWidget createDistinctButton(@NotNull ModularPanel mainPanel,
                                           @NotNull PanelSyncManager panelSyncManager) {
        if (!(mte instanceof IDistinctBusController distinct) || !distinct.canBeDistinct()) {
            return GTGuiTextures.BUTTON_NO_DISTINCT_BUSES.asWidget()
                    .size(18, 18)
                    .addTooltipLine(IKey.lang("gregtech.multiblock.universal.distinct_not_supported"));
        }

        BooleanSyncValue distinctValue = new BooleanSyncValue(distinct::isDistinct, distinct::setDistinct);

        return new CycleButtonWidget()
                .size(18, 18)
                .value(distinctValue)
                .stateBackground(true, GTGuiTextures.BUTTON_DISTINCT_BUSES[1])
                .stateBackground(false, GTGuiTextures.BUTTON_DISTINCT_BUSES[0])
                .background(GTGuiTextures.BUTTON)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(t -> t.addLine(distinctValue.getBoolValue() ?
                        IKey.lang("gregtech.multiblock.universal.distinct_enabled") :
                        IKey.lang("gregtech.multiblock.universal.distinct_disabled")));
    }

    protected IWidget createVoidingButton(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager) {
        if (!mte.shouldShowVoidingModeButton()) {
            return GTGuiTextures.BUTTON_VOID_NONE.asWidget()
                    .size(18, 18)
                    .addTooltipLine(IKey.lang("gregtech.gui.multiblock_voiding_not_supported"));
        }

        IntSyncValue voidingValue = new IntSyncValue(mte::getVoidingMode, mte::setVoidingMode);

        return new CycleButtonWidget()
                .size(18, 18)
                .stateOverlay(0, GTGuiTextures.MULTIBLOCK_VOID[0])
                .stateOverlay(1, GTGuiTextures.MULTIBLOCK_VOID[1])
                .stateOverlay(2, GTGuiTextures.MULTIBLOCK_VOID[2])
                .stateOverlay(3, GTGuiTextures.MULTIBLOCK_VOID[3])
                .background(GTGuiTextures.BUTTON)
                .value(voidingValue)
                .length(4)
                .tooltipAutoUpdate(true)
                .tooltipBuilder(t -> t.addLine(IKey.lang(mte.getVoidingModeTooltip(voidingValue.getIntValue()))));
    }

    @Nullable
    protected Widget<?> createPowerButton(@NotNull ModularPanel mainPanel, @NotNull PanelSyncManager panelSyncManager) {
        IControllable controllable;
        if (!(mte instanceof IControllable)) {
            // is this actually relevant?
            // todo in the future, refactor so that this multis are instanceof IControllable.
            controllable = mte.getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
            if (controllable == null) return null;
            GTLog.logger.warn("MTE [{}] does not extend IControllable when it should!", mte.getClass().getSimpleName());
        } else {
            controllable = (IControllable) mte;
        }

        return new CycleButtonWidget()
                .size(18)
                .stateOverlay(true, GTGuiTextures.BUTTON_POWER[1])
                .stateOverlay(false, GTGuiTextures.BUTTON_POWER[0])
                .disableHoverBackground()
                .background(GTGuiTextures.BUTTON_POWER_DETAIL.asIcon().size(18, 6).marginTop(24), GTGuiTextures.BUTTON)
                .value(new BooleanSyncValue(controllable::isWorkingEnabled, controllable::setWorkingEnabled))
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

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings({ "UnusedReturnValue", "unused" })
    public static class Builder {

        private final List<IDrawable> textList = new ArrayList<>();
        private Consumer<Builder> action;
        private final SyncHandler syncHandler = makeSyncHandler();

        private boolean isWorkingEnabled;
        private boolean isActive;
        private boolean isStructureFormed;

        // Keys for the three-state working system, can be set custom by multiblocks.
        private IKey idlingKey = IKey.lang("gregtech.multiblock.idling").style(TextFormatting.GRAY);
        private IKey pausedKey = IKey.lang("gregtech.multiblock.work_paused").style(TextFormatting.GOLD);
        private IKey runningKey = IKey.lang("gregtech.multiblock.running").style(TextFormatting.GREEN);
        private boolean dirty;
        private Runnable onRebuild;

        public Builder structureFormed(boolean structureFormed) {
            this.isStructureFormed = structureFormed;
            if (!structureFormed) {
                var base = KeyUtil.lang(TextFormatting.RED, "gregtech.multiblock.invalid_structure");
                var hover = KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.invalid_structure.tooltip");
                addKey(base, hover);
            }
            return this;
        }

        public Builder title(String lang) {
            addKey(KeyUtil.lang(TextFormatting.WHITE, lang));
            return this;
        }

        /** Set the current working enabled and active status of this multiblock, used by many line addition calls. */
        public Builder setWorkingStatus(boolean isWorkingEnabled, boolean isActive) {
            this.isWorkingEnabled = isWorkingEnabled;
            this.isActive = isActive;
            return this;
        }

        /**
         * Set custom translation keys for the three-state "Idling", "Paused", "Running" display text.
         * <strong>You still must call {@link Builder#addWorkingStatusLine()} for these to appear!</strong>
         * <br>
         * Pass any key as null for it to continue to use the default key.
         *
         * @param idlingKey  The translation key for the Idle state, or "!isActive && isWorkingEnabled".
         * @param pausedKey  The translation key for the Paused state, or "!isWorkingEnabled".
         * @param runningKey The translation key for the Running state, or "isActive".
         */
        public Builder setWorkingStatusKeys(String idlingKey, String pausedKey, String runningKey) {
            if (idlingKey != null) this.idlingKey = IKey.lang(idlingKey).style(TextFormatting.GRAY);
            if (pausedKey != null) this.pausedKey = IKey.lang(pausedKey).style(TextFormatting.GOLD);
            if (runningKey != null) this.runningKey = IKey.lang(runningKey).style(TextFormatting.GREEN);
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can use.
         * <br>
         * Added if the structure is formed and if the passed energy container has greater than zero capacity.
         */
        public Builder addEnergyUsageLine(IEnergyContainer energyContainer) {
            if (!isStructureFormed || energyContainer == null) return this;
            if (energyContainer.getEnergyCapacity() <= 0) return this;

            long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());

            IKey bodyText = KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.max_energy_per_tick",
                    KeyUtil.number(maxVoltage),
                    KeyUtil.voltage(GTValues.VOCNF, maxVoltage));

            var hoverText = KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.max_energy_per_tick_hover");

            addKey(bodyText, hoverText);
            return this;
        }

        /**
         * Adds the max Recipe Tier that this multiblock can use for recipe lookup.
         * <br>
         * Added if the structure is formed and if the passed tier is a valid energy tier index for
         * {@link GTValues#VNF}.
         */
        public Builder addEnergyTierLine(int tier) {
            if (!isStructureFormed) return this;
            if (tier < GTValues.ULV || tier > GTValues.MAX) return this;

            var bodyText = KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.max_recipe_tier", GTValues.VNF[tier]);
            var hoverText = KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.max_recipe_tier_hover");
            addKey(bodyText, hoverText);
            return this;
        }

        /**
         * Adds the exact EU/t that this multiblock needs to run.
         * <br>
         * Added if the structure is formed and if the passed value is greater than zero.
         */
        public Builder addEnergyUsageExactLine(long energyUsage) {
            if (!isStructureFormed) return this;
            if (energyUsage > 0) {
                String energyFormatted = TextFormattingUtil.formatNumbers(energyUsage);
                // wrap in text component to keep it from being formatted
                var voltageName = KeyUtil.overclock(GTValues.VOCNF, energyUsage);

                addKey(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.energy_consumption", energyFormatted, voltageName));
            }
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can produce.
         * <br>
         * Added if the structure is formed and if the max voltage is greater than zero and the recipe EU/t.
         */
        public Builder addEnergyProductionLine(long maxVoltage, long recipeEUt) {
            if (!isStructureFormed) return this;
            if (maxVoltage != 0 && maxVoltage >= -recipeEUt) {
                String energyFormatted = TextFormattingUtil.formatNumbers(maxVoltage);
                // wrap in text component to keep it from being formatted
                var voltageName = KeyUtil.voltage(GTValues.VOCNF, maxVoltage);

                addKey(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.max_energy_per_tick", energyFormatted, voltageName));
            }
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can produce, including how many amps. Recommended for multi-amp
         * outputting multis.
         * <br>
         * Added if the structure is formed, if the amperage is greater than zero and if the max voltage is greater than
         * zero.
         */
        public Builder addEnergyProductionAmpsLine(long maxVoltage, int amperage) {
            if (!isStructureFormed) return this;
            if (maxVoltage != 0 && amperage != 0) {
                String energyFormatted = TextFormattingUtil.formatNumbers(maxVoltage);
                // wrap in text component to keep it from being formatted
                var voltageName = KeyUtil.voltage(GTValues.VOCNF, maxVoltage);

                addKey(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.max_energy_per_tick_amps",
                        energyFormatted, amperage, voltageName));
            }
            return this;
        }

        /**
         * Adds the max CWU/t that this multiblock can use.
         * <br>
         * Added if the structure is formed and if the max CWU/t is greater than zero.
         */
        public Builder addComputationUsageLine(int maxCWUt) {
            if (!isStructureFormed) return this;
            if (maxCWUt > 0) {
                var computation = KeyUtil.number(TextFormatting.AQUA, maxCWUt);
                addKey(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.computation.max", computation));
            }
            return this;
        }

        /**
         * Adds a currently used CWU/t line.
         * <br>
         * Added if the structure is formed, the machine is active, and the current CWU/t is greater than zero.
         */
        public Builder addComputationUsageExactLine(int currentCWUt) {
            if (!isStructureFormed) return this;
            if (isActive && currentCWUt > 0) {
                var computation = KeyUtil.number(TextFormatting.AQUA, currentCWUt, " CWU/t");
                addKey(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.computation.usage", computation));
            }
            return this;
        }

        /**
         * Adds a three-state indicator line, showing if the machine is running, paused, or idling.
         * <br>
         * Added if the structure is formed.
         */
        public Builder addWorkingStatusLine() {
            if (!isStructureFormed) return this;

            if (!isWorkingEnabled) {
                addKey(pausedKey);
            } else if (isActive) {
                addKey(runningKey);
            } else {
                addKey(idlingKey);
            }
            return this;
        }

        /**
         * Adds the "Work Paused." line.
         * <br>
         * Added if working is not enabled, or if the checkState passed parameter is false.
         * Also added only if formed.
         */
        public Builder addWorkPausedLine(boolean checkState) {
            if (!isStructureFormed) return this;
            if (!checkState || !isWorkingEnabled) {
                addKey(pausedKey);
            }
            return this;
        }

        /**
         * Adds the "Running Perfectly." line.
         * <br>
         * Added if machine is active, or if the checkState passed parameter is false.
         * Also added only if formed.
         */
        public Builder addRunningPerfectlyLine(boolean checkState) {
            if (!isStructureFormed) return this;
            if (!checkState || isActive) {
                addKey(runningKey);
            }
            return this;
        }

        /**
         * Adds the "Idling." line.
         * <br>
         * Added if the machine is not active and working is enabled, or if the checkState passed parameter is false.
         * Also added only if formed.
         */
        public Builder addIdlingLine(boolean checkState) {
            if (!isStructureFormed) return this;
            if (!checkState || (isWorkingEnabled && !isActive)) {
                addKey(idlingKey);
            }
            return this;
        }

        /**
         * Adds a simple progress line that displays progress as a percentage.
         * <br>
         * Added if structure is formed and the machine is active.
         *
         * @param progressPercent Progress formatted as a range of [0,1] representing the progress of the recipe.
         */
        public Builder addProgressLine(double progressPercent) {
            if (!isStructureFormed || !isActive) return this;
            addKey(KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.progress",
                    (int) (progressPercent * 100)));
            return this;
        }

        /**
         * Adds a line indicating how many parallels this multi can potentially perform.
         * <br>
         * Added if structure is formed and the number of parallels is greater than one.
         */
        public Builder addParallelsLine(int numParallels) {
            if (!isStructureFormed) return this;
            if (numParallels > 1) {
                var parallels = KeyUtil.number(TextFormatting.DARK_PURPLE, numParallels);

                addKey(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.parallel", parallels));
            }
            return this;
        }

        /**
         * Adds a warning line when the machine is low on power.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addLowPowerLine(boolean isLowPower) {
            if (!isStructureFormed) return this;
            if (isLowPower) {
                addKey(KeyUtil.lang(TextFormatting.YELLOW,
                        "gregtech.multiblock.not_enough_energy"));
            }
            return this;
        }

        /**
         * Adds a warning line when the machine is low on computation.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addLowComputationLine(boolean isLowComputation) {
            if (!isStructureFormed) return this;
            if (isLowComputation) {
                addKey(KeyUtil.lang(TextFormatting.YELLOW,
                        "gregtech.multiblock.computation.not_enough_computation"));
            }
            return this;
        }

        /**
         * Adds a warning line when the machine's dynamo tier is too low for current conditions.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addLowDynamoTierLine(boolean isTooLow) {
            if (!isStructureFormed) return this;
            if (isTooLow) {
                addKey(KeyUtil.lang(TextFormatting.YELLOW,
                        "gregtech.multiblock.not_enough_energy_output"));
            }
            return this;
        }

        /**
         * Adds warning line(s) when the machine has maintenance problems.
         * <br>
         * Added if there are any maintenance problems, one line per problem as well as a header. <br>
         * Will check the config setting for if maintenance is enabled automatically.
         */
        public Builder addMaintenanceProblemLines(byte maintenanceProblems) {
            if (!isStructureFormed || !ConfigHolder.machines.enableMaintenance) return this;
            if (maintenanceProblems < 63) {
                addKey(KeyUtil.lang(TextFormatting.YELLOW,
                        "gregtech.multiblock.universal.has_problems"));

                // Wrench
                if ((maintenanceProblems & 1) == 0) {
                    addKey(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.wrench"));
                }

                // Screwdriver
                if (((maintenanceProblems >> 1) & 1) == 0) {
                    addKey(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.screwdriver"));
                }

                // Soft Mallet
                if (((maintenanceProblems >> 2) & 1) == 0) {
                    addKey(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.soft_mallet"));
                }

                // Hammer
                if (((maintenanceProblems >> 3) & 1) == 0) {
                    addKey(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.hard_hammer"));
                }

                // Wire Cutters
                if (((maintenanceProblems >> 4) & 1) == 0) {
                    addKey(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.wire_cutter"));
                }

                // Crowbar
                if (((maintenanceProblems >> 5) & 1) == 0) {
                    addKey(KeyUtil.lang(TextFormatting.GRAY,
                            "gregtech.multiblock.universal.problem.crowbar"));
                }
            }
            return this;
        }

        /**
         * Adds two error lines when the machine's muffler hatch is obstructed.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addMufflerObstructedLine(boolean isObstructed) {
            if (!isStructureFormed) return this;
            if (isObstructed) {
                addKey(KeyUtil.lang(TextFormatting.RED,
                        "gregtech.multiblock.universal.muffler_obstructed"));
                addKey(KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.universal.muffler_obstructed_desc"));
            }
            return this;
        }

        /**
         * Adds a fuel consumption line showing the fuel name and the number of ticks per recipe run.
         * <br>
         * Added if structure is formed, the machine is active, and the passed fuelName parameter is not null.
         */
        public Builder addFuelNeededLine(String fuelName, int previousRecipeDuration) {
            if (!isStructureFormed || !isActive || fuelName == null) return this;

            addKey(KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.turbine.fuel_needed",
                    KeyUtil.string(TextFormatting.RED, fuelName),
                    KeyUtil.number(TextFormatting.AQUA, previousRecipeDuration)));
            return this;
        }

        /**
         * Adds the name of the current recipe map to the display.
         * 
         * @param map the {@link RecipeMap} to get the name of
         */
        public Builder addRecipeMapLine(RecipeMap<?> map) {
            if (!isStructureFormed) return this;

            IKey mapName = KeyUtil.lang(TextFormatting.YELLOW, map.getTranslationKey());
            addKey(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.machine_mode", mapName));

            return this;
        }

        /** Insert an empty line into the text list. */
        public Builder addEmptyLine() {
            this.textList.add(IKey.LINE_FEED);
            return this;
        }

        /** Add custom text dynamically, allowing for custom application logic. */
        public Builder addCustom(Consumer<List<IDrawable>> customConsumer) {
            customConsumer.accept(this.textList);
            return this;
        }

        public boolean isEmpty() {
            return this.textList.isEmpty();
        }

        public void clear() {
            this.textList.clear();
        }

        protected boolean hasChanged() {
            if (this.action == null) return false;
            List<String> old = new ArrayList<>();
            for (var drawable : this.textList) old.add(JsonUtils.toJsonString(drawable));
            build();
            if (textList.size() != old.size()) return true;
            for (int i = 0; i < textList.size(); i++) {
                if (!JsonUtils.toJsonString(textList.get(i)).equals(old.get(i)))
                    return true;
            }
            return false;
        }

        protected void sync(String key, PanelSyncManager syncManager) {
            syncManager.syncValue(key, this.syncHandler);
        }

        private SyncHandler makeSyncHandler() {
            return new SyncHandler() {

                @Override
                public void detectAndSendChanges(boolean init) {
                    if (init || hasChanged()) {
                        if (init) {
                            onRebuild();
                            build();
                        }
                        sync(0, this::syncText);
                        markDirty();
                    }
                }

                private void syncText(PacketBuffer buffer) {
                    buffer.writeVarInt(textList.size());
                    for (IDrawable drawable : textList) {
                        var jsonString = JsonUtils.toJsonString(drawable);
                        NetworkUtils.writeStringSafe(buffer, jsonString);
                    }
                }

                @Override
                public void readOnClient(int id, PacketBuffer buf) {
                    if (id == 0) {
                        clear();
                        for (int i = buf.readVarInt(); i > 0; i--) {
                            String jsonString = NetworkUtils.readStringSafe(buf);
                            addKey(JsonUtils.fromJsonString(jsonString));
                        }
                    }
                }

                @Override
                public void readOnServer(int id, PacketBuffer buf) {}
            };
        }

        public void build(IRichTextBuilder<?> richText) {
            if (dirty) {
                onRebuild();
                build();
                dirty = false;
            }
            for (IDrawable drawable : textList) {
                richText.addLine(drawable).spaceLine(2);
            }
        }

        private void onRebuild() {
            if (this.onRebuild != null) {
                this.onRebuild.run();
            }
        }

        public void markDirty() {
            dirty = true;
        }

        protected void build() {
            clear();
            if (this.action != null) this.action.accept(this);
        }

        protected void setAction(Consumer<Builder> action) {
            this.action = action;
        }

        public void onRebuild(Runnable onRebuild) {
            this.onRebuild = onRebuild;
        }

        private void addKey(IDrawable key) {
            this.textList.add(key);
        }

        private void addKey(IKey key, IDrawable... hover) {
            addKey(KeyUtil.setHover(key, hover));
        }
    }
}
