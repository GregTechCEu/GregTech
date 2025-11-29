package gregtech.api.metatileentity.multiblock.ui;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.capability.impl.ComputationRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.mui.GTByteBufAdapters;
import gregtech.api.mui.drawable.GTObjectDrawable;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTHashMaps;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.api.util.function.ByteSupplier;
import gregtech.api.util.function.FloatSupplier;
import gregtech.common.ConfigHolder;
import gregtech.common.items.ToolItems;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.IRichTextBuilder;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.utils.serialization.ByteBufAdapters;
import com.cleanroommc.modularui.utils.serialization.IByteBufDeserializer;
import com.cleanroommc.modularui.utils.serialization.IByteBufSerializer;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

@SuppressWarnings({ "UnusedReturnValue", "unused" })
public class MultiblockUIBuilder {

    private final List<Operation> operations = new ArrayList<>();

    private Consumer<MultiblockUIBuilder> action;
    private final InternalSyncHandler syncHandler = new InternalSyncHandler();

    private static final int DEFAULT_MAX_RECIPE_LINES = 25;

    @Nullable
    private InternalSyncer syncer;

    private boolean isWorkingEnabled;
    private boolean isActive;
    private boolean isStructureFormed;

    // Keys for the three-state working system, can be set custom by multiblocks.
    private IKey idlingKey = IKey.lang("gregtech.multiblock.idling").style(TextFormatting.GRAY);
    private IKey pausedKey = IKey.lang("gregtech.multiblock.work_paused").style(TextFormatting.GOLD);
    private IKey runningKey = IKey.lang("gregtech.multiblock.running").style(TextFormatting.GREEN);
    private Runnable onRebuild;

    @NotNull
    InternalSyncer getSyncer() {
        if (this.syncer == null) {
            this.syncer = new InternalSyncer(isServer());
        }
        return this.syncer;
    }

    void updateFormed(boolean isStructureFormed) {
        this.isStructureFormed = getSyncer().syncBoolean(isStructureFormed);
    }

    private boolean isServer() {
        return !this.syncHandler.getSyncManager().isClient();
    }

    public MultiblockUIBuilder structureFormed(boolean structureFormed) {
        updateFormed(structureFormed);
        if (!this.isStructureFormed) {
            var base = KeyUtil.lang(TextFormatting.RED, "gregtech.multiblock.invalid_structure");
            var hover = KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.invalid_structure.tooltip");
            addHoverableKey(base, hover);
        }
        return this;
    }

    public MultiblockUIBuilder title(String lang) {
        addOperation(Operation.addLine(KeyUtil.lang(TextFormatting.WHITE, lang)).spaceLine(2));
        return this;
    }

    /** Set the current working enabled and active status of this multiblock, used by many line addition calls. */
    public MultiblockUIBuilder setWorkingStatus(boolean isWorkingEnabled, boolean isActive) {
        this.isWorkingEnabled = this.getSyncer().syncBoolean(isWorkingEnabled);
        this.isActive = this.getSyncer().syncBoolean(isActive);
        return this;
    }

    /**
     * Set custom translation keys for the three-state "Idling", "Paused", "Running" display text.
     * <strong>You still must call {@link MultiblockUIBuilder#addWorkingStatusLine()} for these to appear!</strong>
     * <br>
     * Pass any key as null for it to continue to use the default key.
     *
     * @param idlingKey  The translation key for the Idle state, or "!isActive && isWorkingEnabled".
     * @param pausedKey  The translation key for the Paused state, or "!isWorkingEnabled".
     * @param runningKey The translation key for the Running state, or "isActive".
     */
    public MultiblockUIBuilder setWorkingStatusKeys(String idlingKey, String pausedKey, String runningKey) {
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
    @SuppressWarnings("Convert2MethodRef")
    public MultiblockUIBuilder addEnergyUsageLine(IEnergyContainer energyContainer) {
        if (!isStructureFormed) return this;

        // cannot use method reference since energy container can be null on client
        long capacity = getSyncer().syncLong(() -> energyContainer.getEnergyCapacity());
        long inV = getSyncer().syncLong(() -> energyContainer.getInputVoltage());
        long outV = getSyncer().syncLong(() -> energyContainer.getOutputVoltage());

        if (capacity <= 0) return this;

        long maxVoltage = Math.max(inV, outV);
        int tier = GTUtility.getFloorTierByVoltage(maxVoltage);

        IKey bodyText = KeyUtil.lang(TextFormatting.GRAY,
                "gregtech.multiblock.max_energy_per_tick",
                KeyUtil.number(maxVoltage),
                KeyUtil.string(GTValues.VOCNF[tier]));

        var hoverText = KeyUtil.lang(TextFormatting.GRAY,
                "gregtech.multiblock.max_energy_per_tick_hover");

        addHoverableKey(bodyText, hoverText);
        return this;
    }

    /**
     * Adds the max Recipe Tier that this multiblock can use for recipe lookup.
     * <br>
     * Added if the structure is formed and if the passed tier is a valid energy tier index for {@link GTValues#VNF}.
     */
    public MultiblockUIBuilder addEnergyTierLine(int tier) {
        if (!isStructureFormed) return this;
        tier = getSyncer().syncInt(tier);
        if (tier < GTValues.ULV || tier > GTValues.MAX_TRUE) return this;

        var bodyText = KeyUtil.lang(TextFormatting.GRAY,
                "gregtech.multiblock.max_recipe_tier", GTValues.VOCNF[tier]);
        var hoverText = KeyUtil.lang(TextFormatting.GRAY,
                "gregtech.multiblock.max_recipe_tier_hover");
        addHoverableKey(bodyText, hoverText);
        return this;
    }

    /**
     * Adds the exact EU/t that this multiblock needs to run.
     * <br>
     * Added if the structure is formed and if the passed value is greater than zero.
     */
    public MultiblockUIBuilder addEnergyUsageExactLine(long energyUsage) {
        if (!isStructureFormed) return this;
        energyUsage = getSyncer().syncLong(energyUsage);
        if (energyUsage > 0) {
            String energyFormatted = TextFormattingUtil.formatNumbers(energyUsage);
            // wrap in text component to keep it from being formatted
            int tier = GTUtility.getOCTierByVoltage(energyUsage);
            var voltageName = KeyUtil.string(GTValues.VOCNF[tier]);

            addKey(KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.energy_consumption", energyFormatted, voltageName));
        }
        return this;
    }

    /**
     * Adds the max EU/t that this multiblock can produce.
     * <br>
     * Added if the structure is formed and if the max voltage is not zero and greater than the recipe EU/t.
     */
    public MultiblockUIBuilder addEnergyProductionLine(long maxVoltage, long recipeEUt) {
        if (!isStructureFormed) return this;
        maxVoltage = getSyncer().syncLong(maxVoltage);
        recipeEUt = getSyncer().syncLong(recipeEUt);
        if (maxVoltage != 0 && maxVoltage >= recipeEUt) {
            String energyFormatted = TextFormattingUtil.formatNumbers(maxVoltage);
            // wrap in text component to keep it from being formatted
            int tier = GTUtility.getFloorTierByVoltage(maxVoltage);
            var voltageName = KeyUtil.string(GTValues.VOCNF[tier]);

            addKey(KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.max_energy_per_tick", energyFormatted, voltageName));
        }
        return this;
    }

    /**
     * Adds the max EU/t that this multiblock can produce, including how many amps. Recommended for multi-amp outputting
     * multis.
     * <br>
     * Added if the structure is formed, if the amperage is greater than zero and if the max voltage is greater than
     * zero.
     */
    public MultiblockUIBuilder addEnergyProductionAmpsLine(long maxVoltage, int amperage) {
        if (!isStructureFormed) return this;
        maxVoltage = getSyncer().syncLong(maxVoltage);
        amperage = getSyncer().syncInt(amperage);
        if (maxVoltage != 0 && amperage != 0) {
            String energyFormatted = TextFormattingUtil.formatNumbers(maxVoltage);
            // wrap in text component to keep it from being formatted
            int tier = GTUtility.getFloorTierByVoltage(maxVoltage);
            var voltageName = KeyUtil.string(GTValues.VOCNF[tier]);

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
    public MultiblockUIBuilder addComputationUsageLine(int maxCWUt) {
        if (!isStructureFormed) return this;
        maxCWUt = getSyncer().syncInt(maxCWUt);
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
    public MultiblockUIBuilder addComputationUsageExactLine(int currentCWUt) {
        if (!isStructureFormed) return this;
        currentCWUt = getSyncer().syncInt(currentCWUt);
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
    public MultiblockUIBuilder addWorkingStatusLine() {
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
     * Added if working is not enabled, or if the checkState passed parameter is false. Also added only if formed.
     */
    public MultiblockUIBuilder addWorkPausedLine(boolean checkState) {
        if (!isStructureFormed) return this;
        if (!checkState || !isWorkingEnabled) {
            addKey(pausedKey);
        }
        return this;
    }

    /**
     * Adds the "Running Perfectly." line.
     * <br>
     * Added if machine is active, or if the checkState passed parameter is false. Also added only if formed.
     */
    public MultiblockUIBuilder addRunningPerfectlyLine(boolean checkState) {
        if (!isStructureFormed) return this;
        if (!checkState || isActive) {
            addKey(runningKey);
        }
        return this;
    }

    /**
     * Adds the "Idling." line.
     * <br>
     * Added if the machine is not active and working is enabled, or if the checkState passed parameter is false. Also
     * added only if formed.
     */
    public MultiblockUIBuilder addIdlingLine(boolean checkState) {
        if (!isStructureFormed) return this;
        if (!checkState || (isWorkingEnabled && !isActive)) {
            addKey(idlingKey);
        }
        return this;
    }

    /**
     * Adds a progress line that displays recipe progress as "time / total time (percentage)".
     * <br>
     * Added if structure is formed and the machine is active.
     *
     * @param progress    current progress.
     * @param maxProgress total progress to be made.
     */
    public MultiblockUIBuilder addProgressLine(int progress, int maxProgress) {
        if (!isStructureFormed || !isActive) return this;

        progress = getSyncer().syncInt(progress);
        maxProgress = getSyncer().syncInt(maxProgress);

        if (maxProgress <= 20) {
            addKey(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.recipe_progress.ticks",
                    // %02d is not supported by lang
                    String.format("%02d", progress), String.format("%02d", maxProgress),
                    (float) progress / maxProgress * 100f));
        } else {
            addKey(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.recipe_progress.seconds",
                    progress / 20f, maxProgress / 20f, (float) progress / maxProgress * 100f));
        }

        return this;
    }

    /**
     * Adds a progress line that displays recipe progress as "time / total time (percentage)".
     * <br>
     * Added if structure is formed and the machine is active.
     *
     */
    public MultiblockUIBuilder addComputationProgressLine(ComputationRecipeLogic crl) {
        if (!isStructureFormed || !isActive) return this;

        int progress = getSyncer().syncInt(crl.getProgress());
        int maxProgress = getSyncer().syncInt(crl.getMaxProgress());
        int maxCwu = getSyncer().syncInt(() -> crl.getComputationProvider().getMaxCWUt());

        if (crl.shouldShowDuration()) {
            addKey(IKey.str("%s / %s CWU", KeyUtil.number(progress), KeyUtil.number(maxProgress))
                    .style(TextFormatting.GRAY));
        } else {
            // do fancy things
            int cwuRate = getSyncer().syncInt(crl.getCurrentDrawnCWUt());
            int currentCwu = progress * cwuRate;
        }
        return this;
    }

    /**
     * Adds a line indicating how many parallels this multi can potentially perform.
     * <br>
     * Added if structure is formed and the number of parallels is greater than one.
     */
    public MultiblockUIBuilder addParallelsLine(int numParallels) {
        if (!isStructureFormed) return this;
        numParallels = getSyncer().syncInt(numParallels);
        if (numParallels > 1) {
            addKey(KeyUtil.lang(TextFormatting.GRAY,
                    "gregtech.multiblock.parallel",
                    KeyUtil.number(TextFormatting.DARK_PURPLE, numParallels)));
        }
        return this;
    }

    /**
     * Adds a warning line when the machine is low on power.
     * <br>
     * Added if the structure is formed and if the supplier returns true.
     */
    public MultiblockUIBuilder addLowPowerLine(@NotNull BooleanSupplier isLowPower) {
        if (!isStructureFormed) return this;
        if (getSyncer().syncBoolean(isLowPower)) {
            addKey(KeyUtil.lang(TextFormatting.YELLOW, "gregtech.multiblock.not_enough_energy"));
        }
        return this;
    }

    /**
     * Adds a warning line when the machine is low on power.
     * <br>
     * Added if the structure is formed and if the passed parameter is true.
     */
    public MultiblockUIBuilder addLowPowerLine(boolean isLowPower) {
        return addLowPowerLine(() -> isLowPower);
    }

    /**
     * Adds a warning line when the machine is low on computation.
     * <br>
     * Added if the structure is formed and if the passed parameter is true.
     */
    public MultiblockUIBuilder addLowComputationLine(boolean isLowComputation) {
        if (!isStructureFormed) return this;
        if (getSyncer().syncBoolean(isLowComputation)) {
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
    public MultiblockUIBuilder addLowDynamoTierLine(boolean isTooLow) {
        if (!isStructureFormed) return this;
        if (getSyncer().syncBoolean(isTooLow)) {
            addKey(KeyUtil.lang(TextFormatting.YELLOW,
                    "gregtech.multiblock.not_enough_energy_output"));
        }
        return this;
    }

    /**
     * Adds warning line(s) when the machine has maintenance problems.
     * <br>
     * Added if there are any maintenance problems, one line per problem as well as a header. <br>
     * Will check the config
     * setting for if maintenance is enabled automatically.
     */
    public MultiblockUIBuilder addMaintenanceProblemLines(byte maintenanceProblems, boolean warning) {
        if (!isStructureFormed || !ConfigHolder.machines.enableMaintenance) return this;
        maintenanceProblems = getSyncer().syncByte(maintenanceProblems);

        if (warning && maintenanceProblems < 0b111111 && maintenanceProblems > 0b000000 ||
                !warning && maintenanceProblems == 0b000000) {
            addKey(KeyUtil.lang(TextFormatting.YELLOW,
                    "gregtech.multiblock.universal.has_problems"));

            // Wrench
            if ((maintenanceProblems & 1) == 0) {
                addOperation(richText -> richText
                        .add(new ItemDrawable(ToolItems.WRENCH.get(Materials.Iron)))
                        .add(IKey.SPACE)
                        .add(KeyUtil.lang(TextFormatting.YELLOW,
                                "gregtech.multiblock.universal.problem.wrench"))
                        .newLine());
            }

            // Screwdriver
            if (((maintenanceProblems >> 1) & 1) == 0) {
                addOperation(richText -> richText
                        .add(new ItemDrawable(ToolItems.SCREWDRIVER.get(Materials.Iron)))
                        .add(IKey.SPACE)
                        .add(KeyUtil.lang(TextFormatting.YELLOW,
                                "gregtech.multiblock.universal.problem.screwdriver"))
                        .newLine());
            }

            // Soft Mallet
            if (((maintenanceProblems >> 2) & 1) == 0) {
                addOperation(richText -> richText
                        .add(new ItemDrawable(ToolItems.SOFT_MALLET.get(Materials.Wood)))
                        .add(IKey.SPACE)
                        .add(KeyUtil.lang(TextFormatting.YELLOW,
                                "gregtech.multiblock.universal.problem.soft_mallet"))
                        .newLine());
            }

            // Hammer
            if (((maintenanceProblems >> 3) & 1) == 0) {
                addOperation(richText -> richText
                        .add(new ItemDrawable(ToolItems.HARD_HAMMER.get(Materials.Iron)))
                        .add(IKey.SPACE)
                        .add(KeyUtil.lang(TextFormatting.YELLOW,
                                "gregtech.multiblock.universal.problem.hard_hammer"))
                        .newLine());
            }

            // Wire Cutters
            if (((maintenanceProblems >> 4) & 1) == 0) {
                addOperation(richText -> richText
                        .add(new ItemDrawable(ToolItems.WIRE_CUTTER.get(Materials.Iron)))
                        .add(IKey.SPACE)
                        .add(KeyUtil.lang(TextFormatting.YELLOW,
                                "gregtech.multiblock.universal.problem.wire_cutter"))
                        .newLine());
            }

            // Crowbar
            if (((maintenanceProblems >> 5) & 1) == 0) {
                addOperation(richText -> richText
                        .add(new ItemDrawable(ToolItems.CROWBAR.get(Materials.Iron)))
                        .add(IKey.SPACE)
                        .add(KeyUtil.lang(TextFormatting.YELLOW,
                                "gregtech.multiblock.universal.problem.crowbar"))
                        .newLine());
            }
        }
        return this;
    }

    /**
     * Adds two error lines when the machine's muffler hatch is obstructed.
     * <br>
     * Added if the structure is formed and if the passed parameter is true.
     */
    public MultiblockUIBuilder addMufflerObstructedLine(boolean isObstructed) {
        if (!isStructureFormed) return this;
        if (getSyncer().syncBoolean(isObstructed)) {
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
    public MultiblockUIBuilder addFuelNeededLine(String fuelAmount, int previousRecipeDuration) {
        if (!isStructureFormed || !isActive) return this;
        fuelAmount = getSyncer().syncString(fuelAmount);
        previousRecipeDuration = getSyncer().syncInt(previousRecipeDuration);
        addKey(KeyUtil.lang(TextFormatting.GRAY,
                "gregtech.multiblock.turbine.fuel_needed",
                KeyUtil.string(TextFormatting.RED, fuelAmount),
                KeyUtil.number(TextFormatting.AQUA, previousRecipeDuration)));
        return this;
    }

    /**
     * Adds the name of a recipe map to the display.
     *
     * @param map the {@link RecipeMap} to get the name of
     */
    public MultiblockUIBuilder addRecipeMapLine(RecipeMap<?> map) {
        if (!isStructureFormed) return this;

        IKey mapName = KeyUtil.lang(TextFormatting.YELLOW, map.getTranslationKey());
        addKey(KeyUtil.lang(TextFormatting.GRAY, "gregtech.multiblock.machine_mode", mapName));

        return this;
    }

    /**
     * Adds the current outputs of a recipe from recipe logic. Items then fluids.
     *
     * @param arl an instance of an {@link AbstractRecipeLogic} to gather the outputs from.
     */
    public MultiblockUIBuilder addRecipeOutputLine(@NotNull AbstractRecipeLogic arl) {
        return addRecipeOutputLine(arl, DEFAULT_MAX_RECIPE_LINES);
    }

    /**
     * Adds the current outputs of a recipe from recipe logic. Items then fluids.
     *
     * @param arl      an instance of an {@link AbstractRecipeLogic} to gather the outputs from.
     * @param maxLines the maximum number of lines to print until truncating with {@code ...}
     */
    public MultiblockUIBuilder addRecipeOutputLine(AbstractRecipeLogic arl, int maxLines) {
        // todo recipe is null on first load, fix in the future
        Recipe recipe = arl.getPreviousRecipe();

        if (getSyncer().syncBoolean(recipe == null)) return this;
        RecipeMap<?> map = arl.getRecipeMap();
        if (getSyncer().syncBoolean(map == null)) return this;

        Recipe trimmed = null;
        if (isServer()) {
            MetaTileEntity mte = arl.getMetaTileEntity();
            trimmed = Recipe.trimRecipeOutputs(recipe, map, mte.getItemOutputLimit(), mte.getFluidOutputLimit());
        }

        int p = getSyncer().syncInt(arl.getParallelRecipesPerformed());
        if (p == 0) p = 1;

        long eut = getSyncer().syncLong(trimmed == null ? 0 : trimmed.getEUt());
        long maxVoltage = getSyncer().syncLong(arl.getMaximumOverclockVoltage());
        int maxProgress = getSyncer().syncInt(arl.getMaxProgress());

        if (maxProgress == 0) return this;

        List<ItemStack> itemOutputs = new ArrayList<>();
        List<ChancedItemOutput> chancedItemOutputs = new ArrayList<>();
        List<FluidStack> fluidOutputs = new ArrayList<>();
        List<ChancedFluidOutput> chancedFluidOutputs = new ArrayList<>();

        if (isServer()) {
            // recipe searching has to be done server only
            itemOutputs.addAll(trimmed.getOutputs());
            chancedItemOutputs.addAll(trimmed.getChancedOutputs().getChancedEntries());
            fluidOutputs.addAll(trimmed.getFluidOutputs());
            chancedFluidOutputs.addAll(trimmed.getChancedFluidOutputs().getChancedEntries());
        }

        itemOutputs = getSyncer().syncCollection(itemOutputs, ByteBufAdapters.ITEM_STACK);
        fluidOutputs = getSyncer().syncCollection(fluidOutputs, ByteBufAdapters.FLUID_STACK);
        chancedItemOutputs = getSyncer().syncCollection(chancedItemOutputs, GTByteBufAdapters.CHANCED_ITEM_OUTPUT);
        chancedFluidOutputs = getSyncer().syncCollection(chancedFluidOutputs, GTByteBufAdapters.CHANCED_FLUID_OUTPUT);

        addKey(KeyUtil.lang(TextFormatting.GRAY, "gregtech.gui.multiblock.recipe_producing"), Operation::addLine);

        int recipeTier = GTUtility.getTierByVoltage(eut);
        int machineTier = GTUtility.getOCTierByVoltage(maxVoltage);

        // items

        Object2IntMap<ItemStack> itemMap = GTHashMaps.fromItemStackCollection(itemOutputs);

        for (var stack : itemMap.keySet()) {
            addItemOutputLine(stack, (long) itemMap.getInt(stack) * p, maxProgress);
        }

        for (var chancedItemOutput : chancedItemOutputs) {
            // noinspection DataFlowIssue
            int chance = getSyncer()
                    .syncInt(() -> map.chanceFunction.getBoostedChance(chancedItemOutput, recipeTier, machineTier));
            int count = chancedItemOutput.getIngredient().getCount() * p;
            addChancedItemOutputLine(chancedItemOutput, count, chance, maxProgress);
        }

        // fluids

        Object2IntMap<FluidStack> fluidMap = GTHashMaps.fromFluidCollection(fluidOutputs);

        for (var stack : fluidMap.keySet()) {
            addFluidOutputLine(stack, fluidMap.getInt(stack), maxProgress);
        }

        for (var chancedFluidOutput : chancedFluidOutputs) {
            // noinspection DataFlowIssue
            int chance = getSyncer()
                    .syncInt(() -> map.chanceFunction.getBoostedChance(chancedFluidOutput, recipeTier, machineTier));
            int count = chancedFluidOutput.getIngredient().amount * p;
            addChancedFluidOutputLine(chancedFluidOutput, count, chance, maxProgress);
        }
        return this;
    }

    /**
     * Add an item output of a recipe to the display.
     *
     * @param stack        the {@link ItemStack} to display.
     * @param recipeLength the recipe length, in ticks.
     */
    private void addItemOutputLine(@NotNull ItemStack stack, long count, int recipeLength) {
        IKey name = KeyUtil.string(TextFormatting.AQUA, stack.getDisplayName());
        IKey amount = KeyUtil.number(TextFormatting.GOLD, count);
        IKey rate = KeyUtil.string(TextFormatting.WHITE,
                formatRecipeRate(getSyncer().syncInt(recipeLength), count));

        addKey(new GTObjectDrawable(stack, count)
                .asIcon()
                .asHoverable()
                .addTooltipLine(formatRecipeData(name, amount, rate)), Operation::add);
    }

    /**
     * Add the fluid outputs of a recipe to the display.
     *
     * @param stack        a {@link FluidStack}s to display.
     * @param recipeLength the recipe length, in ticks.
     */
    private void addFluidOutputLine(@NotNull FluidStack stack, long count, int recipeLength) {
        IKey name = KeyUtil.fluid(TextFormatting.AQUA, stack);
        IKey amount = KeyUtil.number(TextFormatting.GOLD, count);
        IKey rate = KeyUtil.string(TextFormatting.WHITE,
                formatRecipeRate(getSyncer().syncInt(recipeLength), count));

        addKey(new GTObjectDrawable(stack, count)
                .asIcon()
                .asHoverable()
                .addTooltipLine(formatRecipeData(name, amount, rate)), Operation::add);
    }

    /**
     * Add a chanced item output of a recipe to the display.
     *
     * @param recipeLength max duration of the recipe
     */
    private void addChancedItemOutputLine(@NotNull ChancedItemOutput output,
                                          int count, int chance, int recipeLength) {
        IKey name = KeyUtil.string(TextFormatting.AQUA, output.getIngredient().getDisplayName());
        IKey amount = KeyUtil.number(TextFormatting.GOLD, count);
        IKey rate = KeyUtil.string(TextFormatting.WHITE, formatRecipeRate(getSyncer().syncInt(recipeLength), count));

        addKey(new GTObjectDrawable(output, count)
                .setBoostFunction(entry -> chance)
                .asIcon()
                .asHoverable()
                .addTooltipLine(formatRecipeData(name, amount, rate)), Operation::add);
    }

    /**
     * Add a chanced fluid output of a recipe to the display.
     *
     * @param recipeLength max duration of the recipe
     */
    private void addChancedFluidOutputLine(ChancedFluidOutput output,
                                           int count, int chance, int recipeLength) {
        IKey name = KeyUtil.fluid(TextFormatting.AQUA, output.getIngredient());
        IKey amount = KeyUtil.number(TextFormatting.GOLD, count);
        IKey rate = KeyUtil.string(TextFormatting.WHITE,
                formatRecipeRate(getSyncer().syncInt(recipeLength), count));

        addKey(new GTObjectDrawable(output, count)
                .setBoostFunction(entry -> chance)
                .asIcon()
                .asHoverable()
                .addTooltipLine(formatRecipeData(name, amount, rate)), Operation::add);
    }

    private static String formatRecipeRate(int recipeLength, long amount) {
        float perSecond = ((float) amount / recipeLength) * 20f;

        String rate;
        if (perSecond > 1) {
            rate = "(" + String.format("%,.2f", perSecond).replaceAll("\\.?0+$", "") + "/s)";
        } else {
            rate = "(" + String.format("%,.2f", 1 / (perSecond)).replaceAll("\\.?0+$", "") + "s/ea)";
        }

        return rate;
    }

    private static IKey formatRecipeData(IKey name, IKey amount, IKey rate) {
        return IKey.comp(name, KeyUtil.string(TextFormatting.WHITE, " x "), amount, IKey.SPACE, rate);
    }

    /** Insert an empty line into the text list. */
    public MultiblockUIBuilder addEmptyLine() {
        addKey(IKey.LINE_FEED);
        return this;
    }

    /** Add custom text dynamically, allowing for custom application logic. */
    public MultiblockUIBuilder addCustom(BiConsumer<KeyManager, UISyncer> customConsumer) {
        customConsumer.accept(this::addOperation, getSyncer());
        return this;
    }

    public boolean isEmpty() {
        return this.operations.isEmpty();
    }

    public void clear() {
        this.operations.clear();
    }

    protected boolean hasChanged() {
        if (this.action == null) return false;
        return getSyncer().hasChanged();
    }

    public void sync(String key, PanelSyncManager syncManager) {
        syncManager.syncValue(key, this.syncHandler);
    }

    /**
     * Builds the passed in rich text with operations and drawables.
     *
     * @param richText the rich text to add drawables to
     */
    public void build(IRichTextBuilder<?> richText) {
        for (Operation op : operations) {
            op.accept(richText);
        }
    }

    private void onRebuild() {
        if (this.onRebuild != null) {
            this.onRebuild.run();
        }
    }

    /*
     * this is run on the server side to write values to the internal syncer
     * those values are then synced to the client and read back in the same order
     */
    private void runAction() {
        if (this.action != null) {
            this.action.accept(this);
        }
    }

    /**
     * Set the action for this builder. Called on server and client.
     * 
     * @param action the action to apply to this builder
     */
    public void setAction(Consumer<MultiblockUIBuilder> action) {
        this.action = action;
    }

    /**
     * The runnable is called prior to rebuilding, usually used for updating {@link #structureFormed(boolean)}
     *
     * @param onRebuild the runnable to run prior to rebuilding
     */
    public void onRebuild(Runnable onRebuild) {
        this.onRebuild = onRebuild;
    }

    private void addHoverableKey(IKey key, IDrawable... hover) {
        addKey(KeyUtil.setHover(key, hover));
    }

    private void addKey(IDrawable key) {
        addOperation(Operation.addLineSpace(key));
    }

    private void addKey(IDrawable key, Function<IDrawable, Operation> function) {
        addOperation(function.apply(key));
    }

    private void addOperation(@NotNull Operation op) {
        if (!isServer()) this.operations.add(op);
    }

    public class InternalSyncer implements UISyncer {

        private final PacketBuffer internal = new PacketBuffer(Unpooled.buffer());
        private final boolean isServer;

        private InternalSyncer(boolean isServer) {
            this.isServer = isServer;
        }

        private boolean isServer() {
            return this.isServer;
        }

        @Override
        public boolean syncBoolean(@NotNull BooleanSupplier initial) {
            if (isServer()) {
                boolean val = initial.getAsBoolean();
                internal.writeBoolean(val);
                return val;
            } else {
                return internal.readBoolean();
            }
        }

        @Override
        public int syncInt(@NotNull IntSupplier initial) {
            if (isServer()) {
                int val = initial.getAsInt();
                internal.writeInt(val);
                return val;
            } else {
                return internal.readInt();
            }
        }

        @Override
        public long syncLong(@NotNull LongSupplier initial) {
            if (isServer()) {
                long val = initial.getAsLong();
                internal.writeLong(val);
                return val;
            } else {
                return internal.readLong();
            }
        }

        @Override
        public byte syncByte(@NotNull ByteSupplier initial) {
            if (isServer()) {
                byte val = initial.getByte();
                internal.writeByte(val);
                return val;
            } else {
                return internal.readByte();
            }
        }

        @Override
        public double syncDouble(@NotNull DoubleSupplier initial) {
            if (isServer()) {
                double val = initial.getAsDouble();
                internal.writeDouble(val);
                return val;
            } else {
                return internal.readDouble();
            }
        }

        @Override
        public float syncFloat(@NotNull FloatSupplier initial) {
            if (isServer()) {
                float val = initial.getFloat();
                internal.writeFloat(val);
                return val;
            } else {
                return internal.readFloat();
            }
        }

        @Override
        @NotNull
        public <T> T syncObject(@NotNull Supplier<T> initial, IByteBufSerializer<T> serializer,
                                IByteBufDeserializer<T> deserializer) {
            if (isServer()) {
                T val = initial.get();
                serializer.serializeSafe(internal, Objects.requireNonNull(val));
                return val;
            } else {
                try {
                    return deserializer.deserialize(internal);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        @Override
        public <T, C extends Collection<T>> C syncCollection(C initial, IByteBufSerializer<T> serializer,
                                                             IByteBufDeserializer<T> deserializer) {
            if (isServer()) {
                internal.writeVarInt(initial.size());
                initial.forEach(t -> serializer.serializeSafe(internal, t));
            } else {
                int size = internal.readVarInt();
                try {
                    for (int i = 0; i < size; i++) {
                        initial.add(deserializer.deserialize(internal));
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
            return initial;
        }

        @Override
        public <T> T[] syncArray(T[] initial, IByteBufSerializer<T> serializer, IByteBufDeserializer<T> deserializer) {
            if (isServer()) {
                internal.writeVarInt(initial.length);
                for (T t : initial) {
                    serializer.serializeSafe(internal, t);
                }
            } else {
                initial = Arrays.copyOf(initial, internal.readVarInt());
                Arrays.setAll(initial, i -> deserializer.deserializeSafe(internal));
            }
            return initial;
        }

        public void readBuffer(ByteBuf buf) {
            clear();
            internal.clear();
            internal.writeBytes(buf);
        }

        public void writeBuffer(ByteBuf buf) {
            buf.writeBytes(internal);
        }

        public boolean hasChanged() {
            byte[] old = internal.array().clone();
            this.internal.clear();
            clear();
            onRebuild();
            runAction();
            return !Arrays.equals(old, internal.array());
        }
    }

    public class InternalSyncHandler extends SyncHandler {

        private InternalSyncHandler() {}

        @Override
        public void detectAndSendChanges(boolean init) {
            if (init || hasChanged()) {
                if (init) {
                    onRebuild();
                    runAction();
                }
                syncToClient(0, buf -> getSyncer().writeBuffer(buf));
            }
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {
            if (id == 0) {
                getSyncer().readBuffer(buf);
                onRebuild();
                runAction();
            }
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) {}
    }
}
