package gregtech.api.recipes.logic.statemachine.builder;

import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.logic.RecipeLogicConstants;
import gregtech.api.recipes.logic.RecipeRun;
import gregtech.api.recipes.logic.distinct.DistinctInputGroup;
import gregtech.api.recipes.logic.statemachine.RecipeFluidMatchOperator;
import gregtech.api.recipes.logic.statemachine.RecipeItemMatchOperator;
import gregtech.api.recipes.logic.statemachine.RecipeMaintenanceOperator;
import gregtech.api.recipes.logic.statemachine.RecipeRunCheckOperator;
import gregtech.api.recipes.logic.statemachine.RecipeSearchOperator;
import gregtech.api.recipes.logic.statemachine.RecipeSelectionOperator;
import gregtech.api.recipes.logic.statemachine.RecipeViewOperator;
import gregtech.api.recipes.logic.statemachine.overclock.OverclockingOperatorFactory;
import gregtech.api.recipes.logic.statemachine.overclock.RecipeStandardOverclockingOperator;
import gregtech.api.recipes.logic.statemachine.parallel.ParallelLimitOperatorFactory;
import gregtech.api.recipes.logic.statemachine.parallel.RecipeParallelLimitOperator;
import gregtech.api.recipes.logic.statemachine.parallel.RecipeParallelMatchingOperator;
import gregtech.api.recipes.logic.statemachine.running.RecipeCleanupOperation;
import gregtech.api.recipes.logic.statemachine.running.RecipeDegressOperation;
import gregtech.api.recipes.logic.statemachine.running.RecipeFinalizer;
import gregtech.api.recipes.logic.statemachine.running.RecipeOutputOperation;
import gregtech.api.recipes.logic.statemachine.running.RecipeProgressOperation;
import gregtech.api.recipes.logic.statemachine.running.RecipeResetOperation;
import gregtech.api.recipes.lookup.AbstractRecipeLookup;
import gregtech.api.recipes.lookup.property.PowerCapacityProperty;
import gregtech.api.recipes.lookup.property.PowerSupplyProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.statemachine.GTStateMachine;
import gregtech.api.statemachine.GTStateMachineBuilder;
import gregtech.api.statemachine.GTStateMachineOperator;
import gregtech.api.statemachine.GTStateMachineTransientOperator;
import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

// ARL's state-machine cousin.
public class RecipeStandardStateMachineBuilder {

    protected @NotNull Supplier<AbstractRecipeLookup> lookup;

    protected @NotNull Predicate<NBTTagCompound> shouldStartRecipeLookup = workerNBT -> true;
    protected @Nullable Supplier<PropertySet> properties = null;
    protected @Nullable Supplier<IItemHandlerModifiable> itemInput = null;
    protected @NotNull Supplier<List<ItemStack>> itemInputView = () -> GTUtility.itemHandlerToList(itemInput.get());
    protected @NotNull Supplier<List<IItemHandlerModifiable>> notifiedItemInputs = Collections::emptyList;
    protected @Nullable Supplier<IMultipleTankHandler> fluidInput = null;
    protected @NotNull Supplier<List<FluidStack>> fluidInputView = () -> GTUtility.fluidHandlerToList(fluidInput.get());
    protected @NotNull Supplier<List<IFluidHandler>> notifiedFluidInputs = Collections::emptyList;
    protected @NotNull Consumer<List<ItemStack>> itemOutput = l -> {};
    protected @NotNull IntSupplier itemTrim = () -> Integer.MAX_VALUE;
    protected @NotNull Consumer<List<FluidStack>> fluidOutput = l -> {};
    protected @NotNull IntSupplier fluidTrim = () -> Integer.MAX_VALUE;
    protected @Nullable IntSupplier itemOutAmountLimit = null;
    protected @Nullable IntSupplier itemOutStackLimit = null;
    protected @Nullable IntSupplier fluidOutAmountLimit = null;
    protected @Nullable IntSupplier fluidOutStackLimit = null;
    protected @Nullable IntSupplier parallelLimit = null;
    protected final List<GTStateMachineTransientOperator> recipeSearchSetupAdditionalOperators = new ObjectArrayList<>();
    protected @Nullable Predicate<Recipe> recipeSearchPredicate = null;
    protected final List<Runnable> onInputsUpdate = new ObjectArrayList<>();
    protected @NotNull GTStateMachineTransientOperator itemMatchOperator = RecipeItemMatchOperator.STANDARD_INSTANCE;
    protected @NotNull GTStateMachineTransientOperator fluidMatchOperator = RecipeFluidMatchOperator.STANDARD_INSTANCE;
    protected @NotNull RecipeFinalizer recipeFinalizer = RecipeFinalizer.STANDARD_INSTANCE;
    protected @NotNull Predicate<RecipeRun> finalCheck = RecipeRunCheckOperator.standardConsumptionCheck(itemInput,
            fluidInput);
    protected @NotNull Predicate<NBTTagCompound> perTickWorkerCheck = workerNBT -> true;
    protected @NotNull Predicate<NBTTagCompound> perTickRecipeCheck = recipe -> true;
    protected @NotNull RecipeStallType stallType = RecipeStallType.DEGRESS;
    protected @Nullable BiConsumer<GTStateMachineBuilder, RecipeStallType> progressOperationOverride = null;
    protected @NotNull Supplier<GTStateMachineOperator> outputOperation = () -> new RecipeOutputOperation(itemOutput,
            fluidOutput);

    protected @Nullable Supplier<RecipeMaintenanceOperator.MaintenanceValues> maintenance = null;

    protected @Nullable IntSupplier consumedParallelSupplier = null;
    protected @Nullable LongSupplier consumedAmperageSupplier = null;
    protected @Nullable Consumer<NBTTagCompound> onRecipeStarted = null;
    protected @Nullable Consumer<NBTTagCompound> onRecipeCompleted = null;

    protected @NotNull OverclockingOperatorFactory overclockFactory = RecipeStandardOverclockingOperator::new;
    protected @NotNull ParallelLimitOperatorFactory parallelLimitFactory = RecipeParallelLimitOperator::new;
    protected @NotNull GTStateMachineOperator cleanupOperator = RecipeCleanupOperation.STANDARD_INSTANCE;

    protected double overclockSpeedFactor = RecipeLogicConstants.OVERCLOCK_SPEED_FACTOR;
    protected double overclockCostFactor = RecipeLogicConstants.OVERCLOCK_VOLTAGE_FACTOR;
    protected @Nullable DoubleSupplier voltageDiscount = null;
    protected @Nullable DoubleSupplier durationDiscount = null;

    protected boolean downTransformForParallels = false;
    protected boolean upTransformForOverclocks = false;

    protected @Nullable BooleanSupplier hasNextDistinctGroup;
    protected Supplier<DistinctInputGroup> getCurrentDistinctGroup;
    protected Supplier<DistinctInputGroup> getNextDistinctGroup;
    protected Supplier<Collection<DistinctInputGroup>> getDistinctInputGroups;

    protected @NotNull Deque<RecipePair> preparedRecipeBuffer = new ArrayDeque<>();

    protected boolean asyncSearchAndSetup = false;

    public RecipeStandardStateMachineBuilder(@NotNull Supplier<AbstractRecipeLookup> lookup) {
        this.lookup = lookup;
    }

    public RecipeStandardStateMachineBuilder setLookup(@NotNull Supplier<AbstractRecipeLookup> lookup) {
        this.lookup = lookup;
        return this;
    }

    public RecipeStandardStateMachineBuilder setOffthreadSearchAndSetup(boolean asyncSearchAndSetup) {
        this.asyncSearchAndSetup = asyncSearchAndSetup;
        return this;
    }

    public RecipeStandardStateMachineBuilder setFluidInput(@Nullable Supplier<IMultipleTankHandler> fluidInput) {
        this.fluidInput = fluidInput;
        return this;
    }

    public RecipeStandardStateMachineBuilder setFluidOutput(@NotNull Consumer<List<FluidStack>> fluidOutput) {
        this.fluidOutput = fluidOutput;
        return this;
    }

    public RecipeStandardStateMachineBuilder setFluidOutAmountLimit(@Nullable IntSupplier fluidOutAmountLimit) {
        this.fluidOutAmountLimit = fluidOutAmountLimit;
        return this;
    }

    public RecipeStandardStateMachineBuilder setFluidOutStackLimit(@Nullable IntSupplier fluidOutStackLimit) {
        this.fluidOutStackLimit = fluidOutStackLimit;
        return this;
    }

    public RecipeStandardStateMachineBuilder setItemInput(@Nullable Supplier<IItemHandlerModifiable> itemInput) {
        this.itemInput = itemInput;
        return this;
    }

    public RecipeStandardStateMachineBuilder setItemOutput(@NotNull Consumer<List<ItemStack>> itemOutput) {
        this.itemOutput = itemOutput;
        return this;
    }

    public RecipeStandardStateMachineBuilder setItemOutAmountLimit(@Nullable IntSupplier itemOutAmountLimit) {
        this.itemOutAmountLimit = itemOutAmountLimit;
        return this;
    }

    public RecipeStandardStateMachineBuilder setItemOutStackLimit(@Nullable IntSupplier itemOutStackLimit) {
        this.itemOutStackLimit = itemOutStackLimit;
        return this;
    }

    public RecipeStandardStateMachineBuilder setNotifiedFluidInputs(@NotNull Supplier<List<IFluidHandler>> notifiedFluidInputs) {
        this.notifiedFluidInputs = notifiedFluidInputs;
        return this;
    }

    public RecipeStandardStateMachineBuilder setNotifiedItemInputs(@NotNull Supplier<List<IItemHandlerModifiable>> notifiedItemInputs) {
        this.notifiedItemInputs = notifiedItemInputs;
        return this;
    }

    public RecipeStandardStateMachineBuilder setParallelLimit(@Nullable IntSupplier parallelLimit) {
        this.parallelLimit = parallelLimit;
        return this;
    }

    public RecipeStandardStateMachineBuilder setPerTickWorkerCheck(@NotNull Predicate<NBTTagCompound> perTickWorkerCheck) {
        this.perTickWorkerCheck = perTickWorkerCheck;
        return this;
    }

    public RecipeStandardStateMachineBuilder setPerTickRecipeCheck(@NotNull Predicate<NBTTagCompound> perTickRecipeCheck) {
        this.perTickRecipeCheck = perTickRecipeCheck;
        return this;
    }

    public RecipeStandardStateMachineBuilder setProperties(@Nullable Supplier<PropertySet> properties) {
        this.properties = properties;
        return this;
    }

    public RecipeStandardStateMachineBuilder setStallType(@NotNull RecipeStallType stallType) {
        this.stallType = stallType;
        return this;
    }

    public RecipeStandardStateMachineBuilder setMaintenance(@Nullable Supplier<RecipeMaintenanceOperator.MaintenanceValues> maintenance) {
        this.maintenance = maintenance;
        return this;
    }

    public RecipeStandardStateMachineBuilder setOverclockCostFactor(double overclockCostFactor) {
        this.overclockCostFactor = overclockCostFactor;
        return this;
    }

    public RecipeStandardStateMachineBuilder setOverclockSpeedFactor(double overclockSpeedFactor) {
        this.overclockSpeedFactor = overclockSpeedFactor;
        return this;
    }

    public RecipeStandardStateMachineBuilder setDurationDiscount(DoubleSupplier durationDiscount) {
        this.durationDiscount = durationDiscount;
        return this;
    }

    public RecipeStandardStateMachineBuilder setVoltageDiscount(DoubleSupplier voltageDiscount) {
        this.voltageDiscount = voltageDiscount;
        return this;
    }

    public RecipeStandardStateMachineBuilder setDownTransformForParallels(boolean downTransformForParallels) {
        this.downTransformForParallels = downTransformForParallels;
        return this;
    }

    public RecipeStandardStateMachineBuilder setUpTransformForOverclocks(boolean upTransformForOverclocks) {
        this.upTransformForOverclocks = upTransformForOverclocks;
        return this;
    }

    public RecipeStandardStateMachineBuilder setDistinct(@NotNull BooleanSupplier hasNextDistinctGroup,
                                                         @NotNull Supplier<DistinctInputGroup> getCurrentDistinctGroup,
                                                         @NotNull Supplier<DistinctInputGroup> getNextDistinctGroup,
                                                         @NotNull Supplier<Collection<DistinctInputGroup>> getDistinctInputGroups) {
        this.hasNextDistinctGroup = hasNextDistinctGroup;
        this.getCurrentDistinctGroup = getCurrentDistinctGroup;
        this.getNextDistinctGroup = getNextDistinctGroup;
        this.getDistinctInputGroups = getDistinctInputGroups;
        return this;
    }

    public RecipeStandardStateMachineBuilder setFluidTrim(@NotNull IntSupplier fluidTrim) {
        this.fluidTrim = fluidTrim;
        return this;
    }

    public RecipeStandardStateMachineBuilder setItemTrim(@NotNull IntSupplier itemTrim) {
        this.itemTrim = itemTrim;
        return this;
    }

    public RecipeStandardStateMachineBuilder setOverclockFactory(@NotNull OverclockingOperatorFactory overclockFactory) {
        this.overclockFactory = overclockFactory;
        return this;
    }

    public RecipeStandardStateMachineBuilder setParallelLimitFactory(@NotNull ParallelLimitOperatorFactory parallelLimitFactory) {
        this.parallelLimitFactory = parallelLimitFactory;
        return this;
    }

    public RecipeStandardStateMachineBuilder addSyncedSearchSetupOperation(@NotNull GTStateMachineTransientOperator operator) {
        this.recipeSearchSetupAdditionalOperators.add(operator);
        return this;
    }

    public RecipeStandardStateMachineBuilder setCleanupOperator(@NotNull GTStateMachineOperator cleanupOperator) {
        this.cleanupOperator = cleanupOperator;
        return this;
    }

    public RecipeStandardStateMachineBuilder addOnInputsUpdate(@NotNull Runnable runnable) {
        this.onInputsUpdate.add(runnable);
        return this;
    }

    public RecipeStandardStateMachineBuilder setFluidMatchOperator(GTStateMachineTransientOperator fluidMatchOperator) {
        this.fluidMatchOperator = fluidMatchOperator;
        return this;
    }

    public RecipeStandardStateMachineBuilder setItemMatchOperator(GTStateMachineTransientOperator itemMatchOperator) {
        this.itemMatchOperator = itemMatchOperator;
        return this;
    }

    public RecipeStandardStateMachineBuilder setItemInputView(Supplier<List<ItemStack>> itemInputView) {
        this.itemInputView = itemInputView;
        return this;
    }

    public RecipeStandardStateMachineBuilder setFluidInputView(Supplier<List<FluidStack>> fluidInputView) {
        this.fluidInputView = fluidInputView;
        return this;
    }

    public RecipeStandardStateMachineBuilder setRecipeSearchPredicate(Predicate<Recipe> recipeSearchPredicate) {
        this.recipeSearchPredicate = recipeSearchPredicate;
        return this;
    }

    public RecipeStandardStateMachineBuilder setFinalCheck(@NotNull Predicate<RecipeRun> finalCheck) {
        this.finalCheck = finalCheck;
        return this;
    }

    public RecipeStandardStateMachineBuilder setShouldStartRecipeLookup(@NotNull Predicate<NBTTagCompound> shouldStartRecipeLookup) {
        this.shouldStartRecipeLookup = shouldStartRecipeLookup;
        return this;
    }

    public RecipeStandardStateMachineBuilder setOutputOperation(@NotNull Supplier<GTStateMachineOperator> outputOperation) {
        this.outputOperation = outputOperation;
        return this;
    }

    public RecipeStandardStateMachineBuilder setRecipeFinalizer(@NotNull RecipeFinalizer recipeFinalizer) {
        this.recipeFinalizer = recipeFinalizer;
        return this;
    }

    @ApiStatus.Internal
    public RecipeStandardStateMachineBuilder setConsumedAmperageSupplier(@Nullable LongSupplier consumedAmperageSupplier) {
        this.consumedAmperageSupplier = consumedAmperageSupplier;
        return this;
    }

    @ApiStatus.Internal
    public RecipeStandardStateMachineBuilder setConsumedParallelSupplier(@Nullable IntSupplier consumedParallelSupplier) {
        this.consumedParallelSupplier = consumedParallelSupplier;
        return this;
    }

    @ApiStatus.Internal
    public RecipeStandardStateMachineBuilder setOnRecipeStarted(@Nullable Consumer<NBTTagCompound> onRecipeStarted) {
        this.onRecipeStarted = onRecipeStarted;
        return this;
    }

    @ApiStatus.Internal
    public RecipeStandardStateMachineBuilder setOnRecipeCompleted(@Nullable Consumer<NBTTagCompound> onRecipeCompleted) {
        this.onRecipeCompleted = onRecipeCompleted;
        return this;
    }

    public RecipeStandardStateMachineBuilder setProgressOperationOverride(@Nullable BiConsumer<GTStateMachineBuilder, RecipeStallType> progressOperationOverride) {
        this.progressOperationOverride = progressOperationOverride;
        return this;
    }

    protected static Tuple<List<ItemStack>, List<FluidStack>> toTuple(DistinctInputGroup group) {
        return new Tuple<>(group.itemInventoryView(), group.fluidInventoryView());
    }

    protected void buildProgressTrack(GTStateMachineBuilder builder, int startOp) {
        builder.setPointer(startOp);
        builder.andThenDefault(data -> {
            while (!preparedRecipeBuffer.isEmpty()) {
                RecipePair next = preparedRecipeBuffer.removeFirst();
                int availableParallel = (parallelLimit == null ? 1 : parallelLimit.getAsInt());
                if (consumedParallelSupplier != null) availableParallel -= consumedParallelSupplier.getAsInt();
                if (next.run().getParallel() > availableParallel) return;
                long availableAmperage;
                if (properties == null) {
                    availableAmperage = 0;
                } else if (next.run().isGenerating()) {
                    availableAmperage = properties.get().getDefaultable(PowerCapacityProperty.EMPTY).amperage();
                } else {
                    availableAmperage = properties.get().getDefaultable(PowerSupplyProperty.EMPTY).amperage();
                }
                if (consumedAmperageSupplier != null) {
                    availableAmperage -= consumedAmperageSupplier.getAsLong();
                }
                if (next.run().getRequiredAmperage() > availableAmperage) return;
                if (finalCheck.test(next.run)) {
                    data.getTagList("ActiveRecipes", Constants.NBT.TAG_COMPOUND).appendTag(next.finalized);
                    if (onRecipeStarted != null) onRecipeStarted.accept(next.finalized);
                }
            }
        }, false);
        builder.andThenDefault(d -> d.setBoolean("TickCheckSuccess", perTickWorkerCheck.test(d)), false);

        builder.andThenDefault(d -> d.setInteger("Index", d.getInteger("Index") + 1), false);
        builder.andThenIf(
                d -> d.getInteger("Index") >= d.getTagList("ActiveRecipes", Constants.NBT.TAG_COMPOUND).tagCount(),
                d -> d.removeTag("Integer"), false).movePointerBack();
        int indexIncrement = builder.getPointer();
        builder.andThenDefault(d -> d.setTag(RecipeCleanupOperation.STANDARD_RECIPE_KEY,
                d.getTagList(RecipeFinalizer.STANDARD_RECIPES_KEY, Constants.NBT.TAG_COMPOUND)
                        .getCompoundTagAt(d.getInteger("Index"))),
                false);

        builder.andThenDefault(d -> d.setBoolean("RecipeCheckSuccess", d.getBoolean("TickCheckSuccess") &&
                perTickRecipeCheck.test(d.getCompoundTag(RecipeCleanupOperation.STANDARD_RECIPE_KEY))), false);
        if (progressOperationOverride == null) {
            if (stallType == RecipeStallType.DEGRESS) {
                builder.andThenDefault(RecipeDegressOperation.STANDARD_INSTANCE, false);
            } else if (stallType == RecipeStallType.RESET) {
                builder.andThenDefault(RecipeResetOperation.STANDARD_INSTANCE, false);
            }
            builder.movePointerBack();
            builder.andThenIf(d -> d.getBoolean("RecipeCheckSuccess"), RecipeProgressOperation.STANDARD_INSTANCE,
                    false);
        } else {
            progressOperationOverride.accept(builder, stallType);
        }

        builder.andThenToDefault(indexIncrement).movePointerBack()
                .andThenIf(RecipeCleanupOperation.recipeIsComplete(), outputOperation.get(), false);
        if (onRecipeCompleted != null) {
            // do not save the cleanup to nbt unless onRecipeCompleted was notified successfully.
            builder.andThenDefaultTransient(cleanupOperator, false);
            builder.andThenDefault(
                    d -> onRecipeCompleted.accept(d.getCompoundTag(RecipeCleanupOperation.STANDARD_RECIPE_KEY)), false);
        } else {
            builder.andThenDefault(cleanupOperator, false);
        }
        builder.andThenToDefault(indexIncrement);
    }

    protected void buildLookupTrack(GTStateMachineBuilder builder, int startOp) {
        if (hasNextDistinctGroup == null) {
            builder.newOperator(d -> d.setBoolean("AwaitingInputUpdate", true), false);
        } else {
            builder.newOperator(d -> getCurrentDistinctGroup.get().setAwaitingUpdate(true), false);
        }
        int inputsInvalidPointer = builder.getPointer();

        builder.setPointer(startOp);

        if (consumedParallelSupplier != null && parallelLimit == null) {
            // if we have no parallel, exit if parallel is consumed.
            builder.andThenToIf(t -> consumedParallelSupplier.getAsInt() > 0, -1).movePointerBack();
        }

        // first, the async-incompatible operations, e.g. item/fluid availability and output space info.
        if (hasNextDistinctGroup == null) {
            builder.andThenIf(t -> !t.getBoolean("AwaitingInputUpdate") && shouldStartRecipeLookup.test(t),
                    GTStateMachineOperator.emptyOp(), false);
            if (itemInput != null) {
                builder.andThenDefault(RecipeSearchOperator.standardItemsProvider(itemInputView), false);
            }
            if (fluidInput != null) {
                builder.andThenDefault(RecipeSearchOperator.standardFluidsProvider(fluidInputView), false);
            }
        } else {
            builder.andThenIf(t -> hasNextDistinctGroup.getAsBoolean() && shouldStartRecipeLookup.test(t),
                    RecipeSearchOperator.standardCombinedProvider(() -> toTuple(getNextDistinctGroup.get())), false);
        }
        if (properties != null) {
            builder.andThenDefault(RecipeSearchOperator.standardPropertiesProvider(properties), false);
            if (consumedAmperageSupplier != null) {
                builder.andThenDefault((d, t) -> {
                    long consumed = consumedAmperageSupplier.getAsLong();
                    if (consumed > 0) {
                        PropertySet props = (PropertySet) t.get(RecipeSearchOperator.STANDARD_PROPERTIES_KEY);
                        if (props == null) return;
                        PowerSupplyProperty supply = props.getNullable(PowerSupplyProperty.EMPTY);
                        if (supply != null) {
                            long result = Math.max(supply.amperage() - consumed, 0);
                            props.add(new PowerSupplyProperty(supply.voltage(), result));
                        }
                        PowerCapacityProperty capacity = props.getNullable(PowerCapacityProperty.EMPTY);
                        if (capacity != null) {
                            long result = Math.max(capacity.amperage() - consumed, 0);
                            props.add(new PowerCapacityProperty(capacity.voltage(), result));
                        }
                    }
                }, false);
            }
        }
        if (itemOutAmountLimit != null) {
            builder.andThenDefaultTransient(RecipeParallelMatchingOperator.itemAmountLimitProvider(itemOutAmountLimit),
                    false);
        }
        if (itemOutStackLimit != null) {
            builder.andThenDefaultTransient(RecipeParallelMatchingOperator.itemUniqueLimitProvider(itemOutStackLimit),
                    false);
        }
        if (fluidOutAmountLimit != null) {
            builder.andThenDefaultTransient(
                    RecipeParallelMatchingOperator.fluidAmountLimitProvider(fluidOutAmountLimit), false);
        }
        if (fluidOutStackLimit != null) {
            builder.andThenDefaultTransient(RecipeParallelMatchingOperator.fluidUniqueLimitProvider(fluidOutStackLimit),
                    false);
        }
        if (maintenance != null) {
            builder.andThenDefault(new RecipeMaintenanceOperator(maintenance), false);
        }
        for (GTStateMachineTransientOperator operator : recipeSearchSetupAdditionalOperators) {
            builder.andThenDefault(operator, false);
        }

        // second, the async compatible operations of recipe search and recipe setup
        builder.andThenDefault(new RecipeSearchOperator(lookup), asyncSearchAndSetup);
        builder.andThenDefault(new RecipeSelectionOperator(recipeSearchPredicate), asyncSearchAndSetup);
        int selectionOp = builder.getPointer();
        builder.andThenToDefault(inputsInvalidPointer).movePointerBack()
                .andThenIf(RecipeSelectionOperator.SUCCESS_PREDICATE, itemMatchOperator, asyncSearchAndSetup);
        builder.andThenToDefault(selectionOp).movePointerBack()
                .andThenIf(RecipeItemMatchOperator.SUCCESS_PREDICATE, fluidMatchOperator, asyncSearchAndSetup);
        builder.andThenToDefault(selectionOp).movePointerBack()
                .andThenIf(RecipeFluidMatchOperator.SUCCESS_PREDICATE, GTStateMachineTransientOperator.emptyOp(),
                        asyncSearchAndSetup);
        builder.andThenDefault(new RecipeViewOperator(voltageDiscount, itemTrim, fluidTrim), asyncSearchAndSetup);
        if (parallelLimit != null) {
            builder.andThenDefault(parallelLimitFactory.produce(
                    () -> Math.max(0, parallelLimit.getAsInt() - consumedParallelSupplier.getAsInt()),
                    downTransformForParallels), asyncSearchAndSetup);
            builder.andThenToDefault(selectionOp).movePointerBack()
                    .andThenIf(RecipeParallelLimitOperator.SUCCESS_PREDICATE, GTStateMachineTransientOperator.emptyOp(),
                            asyncSearchAndSetup);
        } else if (consumedParallelSupplier != null) {
            // if we have no parallel limit, exit if parallel is consumed.
            builder.andThenToIf(t -> consumedParallelSupplier.getAsInt() > 0, -1).movePointerBack();
        }
        builder.andThenDefault(RecipeParallelMatchingOperator.STANDARD_INSTANCE, asyncSearchAndSetup);
        builder.andThenDefault(
                overclockFactory.produce(overclockCostFactor, overclockSpeedFactor, upTransformForOverclocks,
                        durationDiscount),
                asyncSearchAndSetup);
        builder.andThenDefault((data, transientData) -> {
            RecipeRun run = (RecipeRun) transientData.get(RecipeStandardOverclockingOperator.STANDARD_RESULT_KEY);
            if (run == null) throw new IllegalStateException();
            preparedRecipeBuffer.add(new RecipePair(run, recipeFinalizer.finalize(run)));
        }, false);
        builder.andThenToDefault(selectionOp);
    }

    public GTStateMachine build() {
        GTStateMachineBuilder builder = new GTStateMachineBuilder();
        builder.newOperator(GTStateMachineOperator.emptyOp(), false); // op 0
        builder.newOperator(GTStateMachineOperator.emptyOp(), false); // op 1
        buildProgressTrack(builder, 0);
        buildLookupTrack(builder, 1);
        return builder.getConstructing();
    }

    @Desugar
    protected record RecipePair(RecipeRun run, NBTTagCompound finalized) {}
}
