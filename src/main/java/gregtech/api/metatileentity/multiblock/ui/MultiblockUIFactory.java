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
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.keys.DynamicKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;

public class MultiblockUIFactory {

    protected static final int DEFAULT_HEIGHT = 202;
    protected static final int DEFAULT_WIDTH = 198;

    private final MultiblockWithDisplayBase mte;
    protected Consumer<PanelSyncManager> valueSyncer;
    protected Consumer<Builder> displayText, warningText, errorText;
    protected BiFunction<ModularPanel, PanelSyncManager, Widget<?>> flexButton = (panel, syncManager) -> null;
    private Consumer<PacketBuffer> writer, reader;

    public MultiblockUIFactory(@NotNull MultiblockWithDisplayBase mte) {
        this.mte = mte;
        var mufflerObstructed = new BooleanSyncValue(
                () -> mte.hasMufflerMechanics() && !mte.isMufflerFaceFree(), null);

        this.valueSyncer = syncManager -> syncManager.syncValue("muffler", mufflerObstructed);
        this.errorText = builder -> builder.addMufflerObstructedLine(mufflerObstructed.getBoolValue());
        this.warningText = builder -> builder.addMaintenanceProblemLines(mte.getMaintenanceProblems());
        this.displayText = builder -> builder.title(mte.getMetaFullName()).structureFormed(mte.isStructureFormed());
        this.writer = mufflerObstructed::write;
        this.reader = mufflerObstructed::read;
    }

    /**
     * Use this to sync a value to the PanelSyncManager <br />
     * Also initially syncs the value before ui is constructed
     */
    public MultiblockUIFactory syncValue(String name, ValueSyncHandler<?> syncHandler) {
        this.writer = this.writer.andThen(buffer -> {
            try {
                syncHandler.write(buffer);
            } catch (IOException ignored) {
                GTLog.logger.warn("Sync handler \"{}\" failed to write!", name);
            }
        });
        this.reader = this.reader.andThen(buffer -> {
            try {
                syncHandler.read(buffer);
            } catch (IOException ignored) {
                GTLog.logger.warn("Sync handler \"{}\" failed to read!", name);
            }
        });
        this.valueSyncer = this.valueSyncer.andThen(syncManager -> syncManager.syncValue(name, syncHandler));
        return this;
    }

    public void writeInitialSync(PacketBuffer buffer) {
        this.writer.accept(buffer);
    }

    public void readInitialSync(PacketBuffer buffer) {
        this.reader.accept(buffer);
    }

    /**
     * Constructs the multiblock ui panel<br />
     * <i>It is not recommended to override this method</i>
     */
    public @NotNull ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        this.valueSyncer.accept(panelSyncManager);
        var panel = createRootPanel();

        // TODO createExtras() hook for overrides?
        var bars = createBars(panel, panelSyncManager);

        return panel.child(createScreen(panelSyncManager))
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
        var builder = builder();
        return new Widget<>()
                .pos(174 - 5, 93 - 5)
                .onUpdateListener(w -> w.overlay(getIndicatorOverlay(builder)))
                .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                .tooltipBuilder(tooltip -> tooltip.addDrawableLines(builder.getTextList()));
    }

    private IDrawable getIndicatorOverlay(Builder builder) {
        if (!builder.isEmpty()) builder.clear();

        this.errorText.accept(builder);
        if (!builder.isEmpty()) {
            // error
            return GTGuiTextures.GREGTECH_LOGO_BLINKING_RED;
        }
        this.warningText.accept(builder);
        if (!builder.isEmpty()) {
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
    public MultiblockUIFactory configureWarningText(boolean merge, Consumer<Builder> warningText) {
        this.warningText = merge ? this.warningText.andThen(warningText) : warningText;
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
        this.errorText = merge ? this.errorText.andThen(errorText) : errorText;
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
        this.displayText = merge ? this.displayText.andThen(displayText) : displayText;
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

    protected Widget<?> createScreen(PanelSyncManager syncManager) {
        final var builder = builder();
        this.displayText.accept(builder);
        var col = new Column();
        builder.build(col);

        return new ParentWidget<>()
                .child(createIndicator())
                .child(new ScrollWidget<>(new VerticalScrollData())
                        .sizeRel(1f)
                        .child(col.expanded()
                                .margin(4, 4)
                                .onUpdateListener(column -> {
                                    var b = builder();
                                    this.displayText.accept(b);
                                    if (!builder.hasChanged(b)) return;
                                    column.getChildren().clear();
                                    // really debating on if the display screen should be its own widget
                                    b.build(column);
                                    resize(column);
                                })))
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
                    .size(18)
                    .addTooltipLine(IKey.lang("gregtech.multiblock.universal.no_flex_button"));
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
                    .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                    .tooltipBuilder(t -> t.addLine(IKey.lang(mte.getVoidingModeTooltip(voidingValue.getIntValue()))));
        } else {
            return GTGuiTextures.BUTTON_VOID_NONE.asWidget()
                    .size(18, 18)
                    .tooltip(t -> t.addLine(IKey.lang("gregtech.gui.multiblock_voiding_not_supported")));
        }
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
                .textureGetter(i -> GTGuiTextures.BUTTON_POWER[i])
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

    protected static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings({ "UnusedReturnValue", "unused" })
    public static class Builder {

        private final List<IDrawable> textList;
        private Function<IDrawable, Widget<?>> widgetFunction = Builder::keyMapper;
        private final Int2ObjectMap<IDrawable> tooltips = new Int2ObjectArrayMap<>();

        private BooleanSupplier isWorkingEnabled = () -> false;
        private BooleanSupplier isActive = () -> false;
        private boolean isStructureFormed;

        // Keys for the three-state working system, can be set custom by multiblocks.
        private IKey idlingKey = IKey.lang("gregtech.multiblock.idling");
        private IKey pausedKey = IKey.lang("gregtech.multiblock.work_paused");
        private IKey runningKey = IKey.lang("gregtech.multiblock.running");

        protected static Widget<?> keyMapper(IDrawable key) {
            return key.asWidget()
                    .widthRel(1f)
                    .height(12);
        }

        private Builder() {
            this.textList = new ArrayList<>();
        }

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
        public Builder setWorkingStatus(BooleanSupplier isWorkingEnabled, BooleanSupplier isActive) {
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
            if (idlingKey != null) this.idlingKey = IKey.str(idlingKey);
            if (pausedKey != null) this.pausedKey = IKey.str(pausedKey);
            if (runningKey != null) this.runningKey = IKey.str(runningKey);
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can use.
         * <br>
         * Added if the structure is formed and if the passed energy container has greater than zero capacity.
         */
        public Builder addEnergyUsageLine(IEnergyContainer energyContainer) {
            if (!isStructureFormed || energyContainer == null) return this;

            if (energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());

                String energyFormatted = TextFormattingUtil.formatNumbers(maxVoltage);
                // wrap in text component to keep it from being formatted
                IKey voltageName = IKey.str(GTValues.VOCNF[GTUtility.getFloorTierByVoltage(maxVoltage)]);

                var bodyText = KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.max_energy_per_tick", energyFormatted, voltageName);
                var hoverText = KeyUtil.lang(TextFormatting.GRAY,
                        "gregtech.multiblock.max_energy_per_tick_hover");
                addKey(bodyText, hoverText);
            }
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
                var voltageName = KeyUtil.string(
                        GTValues.VOCNF[GTUtility.getOCTierByVoltage(energyUsage)]);

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
                var voltageName = KeyUtil.string(
                        GTValues.VOCNF[GTUtility.getFloorTierByVoltage(maxVoltage)]);

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
                var voltageName = KeyUtil.string(
                        GTValues.VOCNF[GTUtility.getFloorTierByVoltage(maxVoltage)]);

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
                var computation = KeyUtil.string(TextFormatting.AQUA, TextFormattingUtil.formatNumbers(maxCWUt));
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
            if (isActive.getAsBoolean() && currentCWUt > 0) {
                var computation = KeyUtil.string(TextFormatting.AQUA,
                        TextFormattingUtil.formatNumbers(currentCWUt) + " CWU/t");
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

            addKey(KeyUtil.string(() -> {
                if (!isWorkingEnabled.getAsBoolean()) {
                    return TextFormatting.GOLD + pausedKey.get();
                } else if (isActive.getAsBoolean()) {
                    return TextFormatting.GREEN + runningKey.get();
                } else {
                    return TextFormatting.GRAY + idlingKey.get();
                }
            }));
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
            if (!checkState || !isWorkingEnabled.getAsBoolean()) {
                addKey(KeyUtil.colored(TextFormatting.GOLD, pausedKey));
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
            if (!checkState || isActive.getAsBoolean()) {
                addKey(KeyUtil.colored(TextFormatting.GREEN, runningKey));
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
            if (!checkState || (isWorkingEnabled.getAsBoolean() && !isActive.getAsBoolean())) {
                addKey(KeyUtil.colored(TextFormatting.GRAY, idlingKey));
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
        public Builder addProgressLine(DoubleSupplier progressPercent) {
            if (!isStructureFormed || !isActive.getAsBoolean()) return this;
            addKey(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.progress",
                    () -> ((int) (progressPercent.getAsDouble() * 100))));
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
                var parallels = KeyUtil.string(TextFormatting.DARK_PURPLE,
                        TextFormattingUtil.formatNumbers(numParallels));

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
                addKey(IKey.comp(IKey.str(TextFormatting.YELLOW.toString()),
                        IKey.lang("gregtech.multiblock.computation.not_enough_computation")));
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
        public Builder addFuelNeededLine(String fuelName, IntSupplier previousRecipeDuration) {
            if (!isStructureFormed || !isActive.getAsBoolean() || fuelName == null) return this;

            addKey(KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.turbine.fuel_needed",
                    KeyUtil.string(TextFormatting.RED, fuelName),
                    KeyUtil.number(TextFormatting.AQUA, previousRecipeDuration::getAsInt)));
            return this;
        }

        /** Insert an empty line into the text list. */
        public Builder addEmptyLine() {
            addKey(IKey.EMPTY); // this is going to cause problems maybe
            return this;
        }

        /** Add custom text dynamically, allowing for custom application logic. */
        public Builder addCustom(Consumer<List<IDrawable>> customConsumer) {
            List<IDrawable> customKeys = new ArrayList<>();
            customConsumer.accept(customKeys);
            customKeys.forEach(this::addKey);
            return this;
        }

        /**
         * @param widgetFunction function to build widgets from keys
         */
        public Builder widgetFunction(Function<IDrawable, Widget<?>> widgetFunction) {
            this.widgetFunction = widgetFunction;
            return this;
        }

        protected boolean isEmpty() {
            return textList.isEmpty();
        }

        protected void clear() {
            textList.clear();
        }

        protected void build(ParentWidget<?> parent) {
            for (int i = 0; i < textList.size(); i++) {
                var line = this.widgetFunction.apply(textList.get(i));
                if (tooltips.containsKey(i))
                    line.addTooltipLine(tooltips.get(i));
                parent.child(line);
            }
        }

        protected List<IDrawable> getTextList() {
            return Collections.unmodifiableList(textList);
        }

        protected boolean hasChanged(Builder other) {
            if (textList.size() != other.textList.size()) return true;
            for (int i = 0; i < textList.size(); i++) {
                IDrawable left = textList.get(i), right = other.textList.get(i);

                // dynamic keys are impossible to check, skip
                if (left instanceof DynamicKey && right instanceof DynamicKey)
                    continue;

                if (!left.equals(right))
                    return true;
            }
            return false;
        }

        private void addKey(IDrawable key) {
            this.textList.add(key);
        }

        private void addKey(IDrawable key, IDrawable hover) {
            this.tooltips.put(textList.size(), hover);
            addKey(key);
        }
    }
}
