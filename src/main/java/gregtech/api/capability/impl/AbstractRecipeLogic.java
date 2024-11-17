package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IWorkable;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.ingredients.match.IngredientMatchHelper;
import gregtech.api.recipes.ingredients.match.MatchCalculation;
import gregtech.api.recipes.logic.PrimitiveRecipeRun;
import gregtech.api.recipes.logic.RecipeRun;
import gregtech.api.recipes.logic.RecipeRunRegistry;
import gregtech.api.recipes.logic.RecipeRunner;
import gregtech.api.recipes.logic.RecipeView;
import gregtech.api.recipes.logic.SingleRecipeRun;
import gregtech.api.recipes.logic.StandardRecipeView;
import gregtech.api.recipes.logic.TrimmedRecipeView;
import gregtech.api.recipes.lookup.CompactibleIterator;
import gregtech.api.recipes.lookup.property.BiomeInhabitedProperty;
import gregtech.api.recipes.lookup.property.CleanroomFulfilmentProperty;
import gregtech.api.recipes.lookup.property.DimensionInhabitedProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.lookup.property.filter.RecipePropertyWithFilter;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;
import java.util.function.Predicate;

import static gregtech.api.recipes.logic.OverclockingConstants.*;

public abstract class AbstractRecipeLogic extends MTETrait implements IWorkable, RecipeRunner {

    private static final String ALLOW_OVERCLOCKING = "AllowOverclocking";
    private static final String OVERCLOCK_VOLTAGE = "OverclockVoltage";

    @Nullable
    private final RecipeMap<?> recipeMap;

    protected Recipe previousRecipe;
    protected @Nullable RecipeRun currentRecipe;
    protected double progressTime;

    private double euDiscount = -1;
    private double speedBonus = -1;

    protected boolean canRecipeProgress = true;

    protected boolean isActive;
    protected boolean workingEnabled = true;
    protected boolean hasNotEnoughEnergy;
    protected boolean wasActiveAndNeedsUpdate;
    protected boolean isOutputsFull;
    private boolean invalidItemInputs;
    private boolean invalidFluidInputs;

    /**
     * DO NOT use the parallelLimit field directly, EVER
     * use {@link AbstractRecipeLogic#setParallelLimit(int)} instead
     */
    private int parallelLimit = 1;

    public AbstractRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap) {
        super(tileEntity);
        this.recipeMap = recipeMap;
    }

    protected boolean handleEnergy(double progress, @NotNull RecipeRunner runner, @NotNull RecipeRun run) {
        long eu = (long) (progress * run.getRequiredVoltage() *
                run.getRequiredAmperage());
        boolean returnable;
        if (run.isGenerating()) {
            returnable = produceEnergy(eu, true);
            if (returnable) produceEnergy(eu, false);
        } else {
            returnable = drawEnergy(eu, true);
            if (returnable) drawEnergy(eu, false);
        }
        if (returnable) {
            this.hasNotEnoughEnergy = false;
            return true;
        } else {
            this.hasNotEnoughEnergy = true;
            decreaseProgress(runner);
            return false;
        }
    }

    /**
     * Draw energy from the energy container
     *
     * @param eu       the eu to draw
     * @param simulate whether to simulate energy extraction or not
     * @return true if the energy can/was drained, otherwise false
     */
    protected abstract boolean drawEnergy(long eu, boolean simulate);

    protected abstract boolean produceEnergy(long eu, boolean simulate);

    /**
     * @return the maximum input voltage the machine can use/handle
     */
    public abstract long getMaxVoltageIn();

    /**
     * @return the maximum output voltage the machine can use/handle
     */
    public abstract long getMaxVoltageOut();

    /**
     * @return the maximum input amperage the machine can use/handle
     */
    public abstract long getMaxAmperageIn();

    /**
     * @return the maximum output amperage the machine can use/handle
     */
    public abstract long getMaxAmperageOut();

    /**
     * @return The maximum voltage the machine/multiblock can overclock to
     */
    public long getMaxOverclockVoltage(boolean generatingRecipe) {
        return generatingRecipe ? getMaxVoltageOut() : getMaxVoltageIn();
    }

    /**
     *
     * @param recipeVoltage the voltage to test
     * @return the maximum amperage the machine/multiblock can support at this voltage. Note that if this is less than
     *         {@link #getMaxAmperageIn()} at voltage {@link #getMaxVoltageIn()}, this machine/multiblock will not work.
     */
    protected long getMaxParallelAmperage(long recipeVoltage, boolean generatingRecipe) {
        if (generatingRecipe) return getMaxAmperageOut() * getMaxVoltageOut() / recipeVoltage;
        else return getMaxAmperageIn() * getMaxVoltageIn() / recipeVoltage;
    }

    /**
     * @return the inventory to input items from
     */
    protected IItemHandlerModifiable getInputInventory() {
        return metaTileEntity.getImportItems();
    }

    /**
     * @return the inventory to output items to
     */
    protected IItemHandlerModifiable getOutputInventory() {
        return metaTileEntity.getExportItems();
    }

    /**
     * @return the fluid inventory to input fluids from
     */
    protected IMultipleTankHandler getInputTank() {
        return metaTileEntity.getImportFluids();
    }

    /**
     * @return the fluid inventory to output fluids to
     */
    protected IMultipleTankHandler getOutputTank() {
        return metaTileEntity.getExportFluids();
    }

    @NotNull
    @Override
    public final String getName() {
        // this is final so machines are not accidentally given multiple workable instances
        return GregtechDataCodes.ABSTRACT_WORKABLE_TRAIT;
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC) {
            return GregtechTileCapabilities.CAPABILITY_RECIPE_LOGIC.cast(this);
        }
        return null;
    }

    @Override
    public void update() {
        World world = getMetaTileEntity().getWorld();
        if (world != null && !world.isRemote) {
            if (workingEnabled) {
                if (getMetaTileEntity().getOffsetTimer() % 20 == 0)
                    this.canRecipeProgress = canProgressRecipe();

                updateRecipeStatus();
            }
            if (wasActiveAndNeedsUpdate) {
                this.wasActiveAndNeedsUpdate = false;
                setActive(false);
            }
        }
    }

    /**
     * DO NOT use the recipeMap field directly, EVER
     * <p>
     * This can be null due to Processing Array logic.
     * Normally this should never be null.
     *
     * @return the current RecipeMap of the logic
     */
    @Nullable
    public RecipeMap<?> getRecipeMap() {
        return this.recipeMap;
    }

    /**
     * Can be null if a recipe has not yet been run
     *
     * @return the previous recipe run
     */
    @Nullable
    public Recipe getPreviousRecipe() {
        return previousRecipe;
    }

    /**
     * @return true if recipes should be searched for
     */
    protected boolean shouldSearchForRecipes(RecipeRunner runner) {
        return true;
    }

    /**
     * @return true if output inventory contents have changed
     */
    protected boolean hasNotifiedOutputs() {
        return !metaTileEntity.getNotifiedItemOutputList().isEmpty() ||
                !metaTileEntity.getNotifiedFluidOutputList().isEmpty();
    }

    /**
     * @return if the output inventory can fit new outputs
     */
    protected boolean canFitNewOutputs() {
        // if the output is full check if the output changed, so we can process recipes results again.
        if (this.isOutputsFull && !hasNotifiedOutputs()) {
            return false;
        } else {
            this.isOutputsFull = false;
            metaTileEntity.getNotifiedItemOutputList().clear();
            metaTileEntity.getNotifiedFluidOutputList().clear();
            return true;
        }
    }

    /**
     * @return true if the input inventory's content can be worked with
     */
    protected boolean canWorkWithInputs() {
        // if the inputs were bad last time, check if they've changed before trying to find a new recipe.
        if (this.hasInvalidItemInputs() && !metaTileEntity.getNotifiedItemInputList().isEmpty()) {
            // the change in inputs (especially by removal of ingredient by the player) might change the current valid
            // recipe.
            // and if the previous recipe produced fluids and the new recipe doesn't, then outputs are not full.
            this.isOutputsFull = false;
            setInvalidItemInputs(false);
            this.metaTileEntity.getNotifiedItemInputList().clear();
        }
        if (this.hasInvalidFluidInputs() && !metaTileEntity.getNotifiedFluidInputList().isEmpty()) {
            // the change in inputs (especially by removal of ingredient by the player) might change the current valid
            // recipe.
            // and if the previous recipe produced fluids and the new recipe doesn't, then outputs are not full.
            this.isOutputsFull = false;
            setInvalidFluidInputs(false);
            this.metaTileEntity.getNotifiedFluidInputList().clear();
        }
        return !this.hasInvalidItemInputs() && !this.hasInvalidFluidInputs();
    }

    /**
     * DO NOT set the {@link #invalidFluidInputs} field directly, EVER
     */
    public void setInvalidFluidInputs(boolean invalidFluidInputs) {
        this.invalidFluidInputs = invalidFluidInputs;
    }

    public boolean hasInvalidFluidInputs() {
        return invalidFluidInputs;
    }

    /**
     * DO NOT set the {@link #invalidItemInputs} field directly, EVER
     */
    public void setInvalidItemInputs(boolean invalidItemInputs) {
        this.invalidItemInputs = invalidItemInputs;
    }

    public boolean hasInvalidItemInputs() {
        return invalidItemInputs;
    }

    /**
     * Invalidate the current state of output inventory contents
     */
    public void invalidateOutputs() {
        this.isOutputsFull = true;
    }

    /**
     * Update the recipe this machine is running.
     * <p>
     * Also handles consuming running energy by default
     * </p>
     */
    protected void updateRecipeStatus() {
        if (canRecipeProgress) {
            IItemHandlerModifiable importInventory = getInputInventory();
            List<ItemStack> items = GTUtility.itemHandlerToList(importInventory);
            IMultipleTankHandler importFluids = getInputTank();
            List<FluidStack> fluids = GTUtility.fluidHandlerToList(importFluids);
            PropertySet properties = computePropertySet();
            tickRecipes(items, fluids, properties);
        } else decreaseProgress(null);
    }

    protected void tickRecipes(@NotNull List<ItemStack> items, @NotNull List<FluidStack> fluids,
                               @NotNull PropertySet properties) {
        tickRecipe(items, fluids, properties, this);
    }

    /**
     * Ticks the recipe runner passed in, referencing the property set
     * and consuming inputs from the list views as necessary
     *
     * @param items      the list view of the input inventory
     * @param fluids     the list view of the import tank
     * @param properties the current property set
     */
    protected void tickRecipe(@NotNull List<ItemStack> items, @NotNull List<FluidStack> fluids,
                              @NotNull PropertySet properties, @NotNull RecipeRunner runner) {
        double progressLeftThisTick = 1;
        while (progressLeftThisTick > 0) {
            RecipeRun current = runner.getCurrent();
            if (current == null) {
                findAndSetupRecipeToRun(items, fluids, properties, runner);
                if ((current = runner.getCurrent()) == null) return;
            }
            double progress = Math.min(current.getDuration() - runner.getRecipeProgress(), progressLeftThisTick);
            if (progress > 0) {
                if (handleEnergy(progress, runner, current)) {
                    runner.setRecipeProgress(runner.getRecipeProgress() + progress);
                    progressLeftThisTick -= progress;
                } else return;
            }
            if (runner.getRecipeProgress() >= current.getDuration())
                attemptRecipeCompletion(runner);
            if (!canSubtick()) progressLeftThisTick = -1;
        }
    }

    /**
     * @return whether multiple recipes can occur within a single tick, if their durations are short enough.
     */
    protected abstract boolean canSubtick();

    protected void findAndSetupRecipeToRun(@NotNull List<ItemStack> listViewOfItemInputs,
                                           @NotNull List<FluidStack> listViewOfFluidInputs,
                                           @NotNull PropertySet properties, @NotNull RecipeRunner runner) {
        if (!canWorkWithInputs() || !canFitNewOutputs()) return;

        RecipeRun run = runner.getCurrent();
        if (run != null) return;
        Recipe prev = runner.getPrevious();
        if (checkPreviousRecipe(prev, properties)) {
            Pair<RecipeRun, Recipe> pair = matchRecipe(prev, listViewOfItemInputs, listViewOfFluidInputs, properties);
            run = pair == null ? null : pair.getLeft();
        }
        if (run == null && shouldSearchForRecipes(runner)) {
            var found = findRecipeRun(listViewOfItemInputs, listViewOfFluidInputs, properties);
            if (found != null) {
                prev = found.getRight();
                run = found.getLeft();
            }
        }

        runner.setRunning(prev, run);
    }

    /**
     * Decrease the recipe progress time in the case that some state was not right, like available EU to drain.
     */
    protected void decreaseProgress(@Nullable RecipeRunner runner) {
        if (runner == null) runner = this;
        RecipeRun run = runner.getCurrent();
        // generating recipes do not lose progress
        if (run == null || run.isGenerating()) return;
        if (runner.getRecipeProgress() > 0) {
            if (ConfigHolder.machines.recipeProgressLowEnergy) {
                runner.setRecipeProgress(0);
            } else {
                runner.setRecipeProgress(Math.max(0, runner.getRecipeProgress() - 2));
            }
        }
    }

    /**
     * @return true if the recipe can progress, otherwise false
     */
    protected boolean canProgressRecipe() {
        return true;
    }

    /**
     * Force the workable to search for new recipes.
     * This can be performance intensive. Use sparingly.
     */
    public void forceRecipeRecheck() {
        this.previousRecipe = null;
        this.currentRecipe = null;
        this.progressTime = 0;
        findAndSetupRecipeToRun(GTUtility.itemHandlerToList(getInputInventory()),
                GTUtility.fluidHandlerToList(getInputTank()), computePropertySet(), this);
    }

    protected @NotNull PropertySet computePropertySet() {
        PropertySet set = PropertySet.comprehensive(getMaxVoltageIn(), getMaxAmperageIn(), getMaxVoltageOut(),
                getMaxAmperageOut());
        set.add(new DimensionInhabitedProperty(this.getMetaTileEntity().getWorld().provider.getDimension()));
        set.add(new BiomeInhabitedProperty(
                this.getMetaTileEntity().getWorld().getBiomeForCoordsBody(this.getMetaTileEntity().getPos())));
        set.add(new CleanroomFulfilmentProperty(getCleanroomPredicate()));
        return set;
    }

    protected Predicate<CleanroomType> getCleanroomPredicate() {
        MetaTileEntity mte = getMetaTileEntity();
        if (mte instanceof ICleanroomReceiver receiver) {
            if (ConfigHolder.machines.cleanMultiblocks && mte instanceof IMultiblockController) return c -> true;

            ICleanroomProvider cleanroomProvider = receiver.getCleanroom();
            if (cleanroomProvider != null)
                return c -> cleanroomProvider.isClean() && cleanroomProvider.checkCleanroomType(c);
        }
        return c -> false;
    }

    /**
     * Find the recipe that works with the given inputs, and then match it to get a recipe run.
     *
     * @param items      the items for the search
     * @param fluids     the fluids for the search
     * @param properties the properties for the search
     * @return the recipe run for the found recipe, or null if none was found.
     */
    @Nullable
    public Pair<RecipeRun, Recipe> findRecipeRun(List<ItemStack> items, List<FluidStack> fluids,
                                                 PropertySet properties) {
        RecipeMap<?> map = getRecipeMap();
        if (map == null || !isRecipeMapValid(map)) {
            return null;
        }
        PropertySet set = computePropertySet();
        set.circuits(items);
        CompactibleIterator<Recipe> iterator = map.findRecipes(items, fluids, properties);
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            Pair<RecipeRun, Recipe> run = matchRecipe(recipe, items, fluids, properties);
            if (run != null) {
                setInvalidItemInputs(false);
                setInvalidFluidInputs(false);
                this.isOutputsFull = false;
                return run;
            }
        }
        return null;
    }

    /**
     * Called to determine if a recipe can be run based on just items and fluids,
     * and if so what the characteristics of the run will be.
     * Should also consume inputs.
     *
     * @param recipe     the recipe to check
     * @param items      the list view of the item input inventories for this check. Will be modified in order to
     *                   consume.
     * @param fluids     the list view of the fluid input inventories for this check. WIll be modified in order to
     *                   consume.
     * @param properties the properties for this check
     * @return the recipe run for the recipe, or null if it could not be matched.
     */
    @Nullable
    public Pair<RecipeRun, Recipe> matchRecipe(@NotNull Recipe recipe, @NotNull List<ItemStack> items,
                                               @NotNull List<FluidStack> fluids, @NotNull PropertySet properties) {
        if (!checkRecipe(recipe)) return null;
        MatchCalculation<ItemStack> itemMatch = getItemMatch(recipe, items);
        if (!itemMatch.attemptScale(1)) {
            setInvalidItemInputs(true);
            return null;
        }
        MatchCalculation<FluidStack> fluidMatch = getFluidMatch(recipe, fluids);
        if (!fluidMatch.attemptScale(1)) {
            setInvalidFluidInputs(true);
            return null;
        }

        RecipeView recipeView = applyParallel(recipe, itemMatch, fluidMatch);
        if (recipeView == null) return null;

        RecipeRun returnable = applyOverclocking(recipeView, properties);
        if (returnable == null) return null;

        if (!performConsumption(recipeView, returnable, items, fluids)) return null;

        return Pair.of(returnable, recipe);
    }

    @NotNull
    protected MatchCalculation<ItemStack> getItemMatch(@NotNull Recipe recipe, @NotNull List<ItemStack> items) {
        return IngredientMatchHelper.matchItems(recipe.getItemIngredients(), items);
    }

    @NotNull
    protected MatchCalculation<FluidStack> getFluidMatch(@NotNull Recipe recipe, @NotNull List<FluidStack> fluids) {
        return IngredientMatchHelper.matchFluids(recipe.getFluidIngredients(), fluids);
    }

    /**
     * Take match information and return a recipe view, suitably modified from parallel.
     */
    @Nullable
    protected RecipeView applyParallel(@NotNull Recipe recipe, @NotNull MatchCalculation<ItemStack> itemMatch,
                                       @NotNull MatchCalculation<FluidStack> fluidMatch) {
        StandardRecipeView recipeView = getTrimmedRecipeView(recipe, itemMatch, fluidMatch);

        int parallel = determineParallel(recipeView);
        if (parallel == 0) return null;
        parallel = recipeView.getItemMatch().largestSucceedingScale(parallel);
        parallel = recipeView.getFluidMatch().largestSucceedingScale(parallel);
        // shouldn't happen since we succeeded at scale 1 for both, but just in case
        if (parallel == 0) return null;
        recipeView.setParallel(parallel);
        int minValue = 0;
        while (parallel - minValue > 1) {
            int middle = (minValue + parallel) / 2;
            if (!canFit(recipeView.setParallel(middle))) {
                parallel = middle;
            } else {
                minValue = middle;
            }
        }
        if (!canFit(recipeView.setParallel(parallel))) {
            if (minValue == 0) {
                invalidateOutputs();
                return null;
            } else recipeView.setParallel(minValue);
        }
        return recipeView;
    }

    protected int determineParallel(@NotNull RecipeView recipeView) {
        if (recipeView.getActualEUt() == 0) return getParallelLimit(recipeView.getRecipe());
        return (int) Math.min(getParallelLimit(recipeView.getRecipe()),
                getMaxParallelAmperage(recipeView.getActualVoltage(), recipeView.getRecipe().isGenerating()) /
                        recipeView.getActualAmperage());
    }

    @NotNull
    protected StandardRecipeView getTrimmedRecipeView(@NotNull Recipe recipe,
                                                      @NotNull MatchCalculation<ItemStack> itemMatch,
                                                      @NotNull MatchCalculation<FluidStack> fluidMatch) {
        if (recipe.getItemOutputProvider().getMaximumOutputs(1) <= metaTileEntity.getItemOutputLimit() &&
                recipe.getFluidOutputProvider().getMaximumOutputs(1) <= metaTileEntity.getFluidOutputLimit())
            return new StandardRecipeView(recipe, itemMatch, fluidMatch, getEUtDiscount(), 1);
        else {
            return new TrimmedRecipeView(recipe, itemMatch, fluidMatch, getEUtDiscount(), 1,
                    metaTileEntity.getItemOutputLimit(), metaTileEntity.getFluidOutputLimit());
        }
    }

    /**
     * Take a recipe view and return a recipe run, suitably modified from overclocking.
     */
    @Nullable
    protected RecipeRun applyOverclocking(@NotNull RecipeView recipeView, @NotNull PropertySet properties) {
        if (recipeView.getActualEUt() == 0)
            return new PrimitiveRecipeRun(recipeView, properties, computeDuration(recipeView, 0));
        // the recipe's tier for chance boosting is not affected by discount
        int recipeVoltageTier = GTUtility.getTierByVoltage(recipeView.getRecipe().getVoltage());
        int machineVoltageTier = GTUtility
                .getFloorTierByVoltage(getMaxOverclockVoltage(recipeView.getRecipe().isGenerating()));
        int overclocks = computeOverclockCount(recipeView, properties, machineVoltageTier);
        float multiplier = computeVoltageMultiplier(recipeView, overclocks);
        // ensure that we can still support our amperage requirements at overclock
        while (getMaxParallelAmperage((long) (recipeView.getActualVoltage() * multiplier),
                recipeView.getRecipe().isGenerating()) < recipeView.getActualAmperage()) {
            overclocks--;
            if (overclocks < 0) return null;
            multiplier = computeVoltageMultiplier(recipeView, overclocks);
        }
        assert getRecipeMap() != null;
        return new SingleRecipeRun(recipeView, recipeVoltageTier, machineVoltageTier,
                properties, multiplier, computeDuration(recipeView, overclocks));
    }

    protected @Range(from = 0, to = Integer.MAX_VALUE) int computeOverclockCount(@NotNull RecipeView recipeView,
                                                                                 @NotNull PropertySet properties,
                                                                                 int machineVoltageTier) {
        return Math.max(0, machineVoltageTier - GTUtility.getTierByVoltage(recipeView.getActualVoltage()));
    }

    protected boolean performConsumption(@NotNull RecipeView view, @NotNull RecipeRun run,
                                         @NotNull List<ItemStack> items, @NotNull List<FluidStack> fluids) {
        long[] consumption = run.getItemArrayConsumption();
        if (consumption == null) return false; // shouldn't happen
        for (int i = 0; i < consumption.length; i++) {
            int used = (int) consumption[i];
            if (used == 0) continue;
            ItemStack stack = items.get(i).copy();
            stack.setCount(stack.getCount() - used);
            items.set(i, stack);
        }

        consumption = run.getFluidArrayConsumption();
        if (consumption == null) return false; // shouldn't happen
        for (int i = 0; i < consumption.length; i++) {
            int used = (int) consumption[i];
            if (used == 0) continue;
            FluidStack stack = fluids.get(i).copy();
            if (stack.amount <= used) stack = null;
            else stack.amount -= used;
            fluids.set(i, stack);
        }

        return true;
    }

    protected float computeVoltageMultiplier(RecipeView recipe,
                                             @Range(from = 0, to = Integer.MAX_VALUE) int overclocks) {
        return (float) Math.pow(getOverclockingVoltageFactor(), overclocks);
    }

    protected float computeDuration(RecipeView recipe,
                                    @Range(from = 0, to = Integer.MAX_VALUE) int overclocks) {
        return recipe.getActualDuration() * computeDurationMultiplier(recipe, overclocks);
    }

    protected float computeDurationMultiplier(RecipeView recipe,
                                              @Range(from = 0, to = Integer.MAX_VALUE) int overclocks) {
        return (float) Math.pow(getOverclockingDurationFactor(), overclocks);
    }

    protected boolean canFit(StandardRecipeView view) {
        return canFitItems(view.getMaximumItems()) && canFitFluids(view.getMaximumFluids());
    }

    protected boolean canFitItems(List<ItemStack> items) {
        return metaTileEntity.canVoidRecipeItemOutputs() ||
                GTTransferUtils.addItemsToItemHandler(getOutputInventory(), true, items);
    }

    protected boolean canFitFluids(List<FluidStack> fluids) {
        return metaTileEntity.canVoidRecipeFluidOutputs() ||
                GTTransferUtils.addFluidsToFluidHandler(getOutputTank(), true, fluids);
    }

    @Override
    public @Nullable Recipe getPrevious() {
        return this.previousRecipe;
    }

    @Override
    public @Nullable RecipeRun getCurrent() {
        return this.currentRecipe;
    }

    @Override
    public void setRunning(Recipe recipe, RecipeRun run) {
        this.progressTime = 0;
        this.previousRecipe = recipe;
        this.currentRecipe = run;
        if (this.wasActiveAndNeedsUpdate) {
            this.wasActiveAndNeedsUpdate = false;
        } else {
            this.setActive(true);
        }
    }

    @Override
    public void notifyOfCompletion() {
        this.progressTime = 0;
        this.currentRecipe = null;
    }

    @Override
    public void setOutputInvalid(boolean invalid) {
        isOutputsFull = invalid;
    }

    @Override
    public boolean isOutputInvalid() {
        if (isOutputsFull && hasNotifiedOutputs()) {
            isOutputsFull = false;
            metaTileEntity.getNotifiedItemOutputList().clear();
            metaTileEntity.getNotifiedFluidOutputList().clear();
        }
        return isOutputsFull;
    }

    @Override
    public double getRecipeProgress() {
        return this.progressTime;
    }

    @Override
    public void setRecipeProgress(double progress) {
        this.progressTime = progress;
    }

    /**
     * @return true if the previous recipe is valid and can be run again, ignoring input items/fluids.
     */
    @Contract("null, _ -> false")
    protected boolean checkPreviousRecipe(@Nullable Recipe previousRecipe, @NotNull PropertySet properties) {
        if (previousRecipe == null || !checkRecipe(previousRecipe)) return false;
        return RecipePropertyWithFilter.matches(previousRecipe.getRecipePropertyStorage(), properties);
    }

    /**
     * checks the recipe before preparing it
     *
     * @param recipe the recipe to check
     * @return true if the recipe is allowed to be used, else false
     */
    public boolean checkRecipe(@NotNull Recipe recipe) {
        return true;
    }

    /**
     * DO NOT use the parallelLimit field directly, EVER
     *
     * @return the current parallel limit of the logic
     */
    public int getParallelLimit(@Nullable Recipe recipe) {
        return parallelLimit;
    }

    /**
     * Set the parallel limit
     * 
     * @param amount the amount to set
     */
    public void setParallelLimit(int amount) {
        parallelLimit = amount;
    }

    /**
     * Sets an EU/t discount to apply to a machine when running recipes.<br>
     * This does NOT affect recipe lookup voltage, even if the discount drops it to a lower voltage tier.<br>
     * This discount is applied pre-parallel/pre-overclock.
     *
     * @param discount The discount, must be greater than 0 and less than 1.
     *                 If discount == 0.75, then the recipe will only require 75% of the listed power to run.
     *                 If discount is > 1, then the recipe will require more than the listed power to run.
     *                 <strong>Be careful as this may not always be possible within the EU/t maximums of the machine!
     *                 </strong>
     */
    public void setEUDiscount(double discount) {
        if (discount <= 0) {
            GTLog.logger.warn("Cannot set EU discount for recipe logic to {}, discount must be > 0", discount);
            return;
        }
        euDiscount = discount;
    }

    /**
     * @return the EU/t discount, or -1 if no discount.
     */
    public double getEUtDiscount() {
        return euDiscount;
    }

    /**
     * Sets a speed multiplier to apply to a machine when running recipes.<br>
     * This discount is applied pre-parallel/pre-overclock.
     *
     * @param bonus The bonus, must be greater than 0.
     *              If bonus == 0.2, then the recipe will be 20% of the normal duration.
     *              If bonus is > 1, then the recipe will be slower than the normal duration.
     */
    public void setSpeedBonus(double bonus) {
        if (bonus <= 0) {
            GTLog.logger.warn("Cannot set speed bonus for recipe logic to {}, bonus must be > 0", bonus);
            return;
        }
        speedBonus = bonus;
    }

    /**
     * @return the speed bonus, or -1 if no bonus.
     */
    public double getSpeedBonus() {
        return speedBonus;
    }

    /**
     * @param recipeMap the recipemap to check
     * @return true if the recipemap is valid for recipe search
     */
    public boolean isRecipeMapValid(@NotNull RecipeMap<?> recipeMap) {
        return true;
    }

    /**
     * @return the multiplier to use for reducing duration upon overclocking
     */
    protected double getOverclockingDurationFactor() {
        return STD_DURATION_FACTOR;
    }

    /**
     * @return the multiplier to use for increasing voltage upon overclocking
     */
    protected double getOverclockingVoltageFactor() {
        return STD_VOLTAGE_FACTOR;
    }

    /**
     * Creates an array of Voltage Names that the machine/multiblock can overclock to.
     * Since this is for use with the customizable overclock button, all tiers up to
     * {@link AbstractRecipeLogic#getMaxVoltageIn()}
     * are allowed, since the button is initialized to this value.
     *
     * @return a String array of the voltage names allowed to be used for overclocking
     */
    public String[] getAvailableOverclockingTiers() {
        final int maxTier = getMaxOverclockTier();
        final String[] result = new String[maxTier + 1];
        result[0] = "gregtech.gui.overclock.off";
        if (maxTier >= 0) System.arraycopy(GTValues.VNF, 1, result, 1, maxTier);
        return result;
    }

    protected int getMaxOverclockTier() {
        return GTUtility.getTierByVoltage(getMaxVoltageIn());
    }

    /**
     * Completes the recipe which was being run if possible, and performs actions done upon recipe completion
     */
    @MustBeInvokedByOverriders
    protected void attemptRecipeCompletion(RecipeRunner runner) {
        if (runner.isOutputInvalid()) return;
        RecipeRun run = runner.getCurrent();
        if (run == null) return;
        if (outputRecipeOutputs(run)) {
            runner.notifyOfCompletion();
            this.hasNotEnoughEnergy = false;
            this.wasActiveAndNeedsUpdate = true;
        } else {
            runner.setOutputInvalid(true);
        }
    }

    /**
     * outputs the items created by the recipe
     */
    protected boolean outputRecipeOutputs(@NotNull RecipeRun run) {
        if (GTTransferUtils.addItemsToItemHandler(getOutputInventory(), true, run.getItemsOut()) &&
                GTTransferUtils.addFluidsToFluidHandler(getOutputTank(), true, run.getFluidsOut())) {
            GTTransferUtils.addItemsToItemHandler(getOutputInventory(), false, run.getItemsOut());
            GTTransferUtils.addFluidsToFluidHandler(getOutputTank(), false, run.getFluidsOut());
            return true;
        } else return false;
    }

    /**
     * @return the progress percentage towards completion. Format: {@code 0.1 = 10%}.
     */
    public double getProgressPercent() {
        if (currentRecipe == null || currentRecipe.getDuration() <= 0) return 0;
        return progressTime / currentRecipe.getDuration();
    }

    @Override
    public int getProgress() {
        return (int) progressTime;
    }

    @Override
    public int getMaxProgress() {
        if (currentRecipe == null) return 0;
        int dur = (int) currentRecipe.getDuration();
        if (dur == 0) return 1;
        return dur;
    }

    /**
     * @return the current recipe's EU/t
     */
    public long getRecipeEUt() {
        if (currentRecipe == null) return 0;
        return currentRecipe.getRequiredAmperage() * currentRecipe.getRequiredVoltage();
    }

    /**
     * @return the current recipe's EU/t for TOP/Waila/Tricorder
     */
    public long getInfoProviderEUt() {
        return getRecipeEUt();
    }

    /**
     * @return the previous recipe's duration
     */
    public int getPreviousRecipeDuration() {
        return getPreviousRecipe() == null ? 0 : getPreviousRecipe().getDuration();
    }

    /**
     * Set the workable as active or inactive
     *
     * @param active true to set active, false to set inactive
     */
    protected void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            metaTileEntity.markDirty();
            World world = metaTileEntity.getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        metaTileEntity.markDirty();
        World world = metaTileEntity.getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        }
    }

    /**
     * @return true if there is not enough energy to continue recipe progress
     */
    public boolean isHasNotEnoughEnergy() {
        return hasNotEnoughEnergy;
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    /**
     * @return true if the workable is actively working
     */
    public boolean isWorking() {
        return isActive && !hasNotEnoughEnergy && workingEnabled;
    }

    /**
     * This is needed as CycleButtonWidget requires an IntSupplier, without making an Enum of tiers.
     *
     * @return The current Tier for the voltage the machine is allowed to overclock to
     */
    public int getOverclockTier() {
        // This will automatically handle ULV, and return 0
        return GTUtility.getTierByVoltage(getMaxOverclockVoltage(false));
    }

    /**
     * Used to reset cached values in the Recipe Logic on events such as multiblock structure deformation
     */
    @MustBeInvokedByOverriders
    public void invalidate() {
        previousRecipe = null;
        currentRecipe = null;
        progressTime = 0;
        isOutputsFull = false;
        setInvalidItemInputs(false);
        setInvalidFluidInputs(false);
        setActive(false); // this marks dirty for us
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            getMetaTileEntity().scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
            getMetaTileEntity().scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        this.isActive = buf.readBoolean();
        this.workingEnabled = buf.readBoolean();
    }

    @NotNull
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("WorkEnabled", workingEnabled);
        compound.setBoolean("CanRecipeProgress", canRecipeProgress);
        if (this.currentRecipe != null) {
            compound.setString("RunType", this.currentRecipe.getRegistryName());
            compound.setDouble("Progress", this.progressTime);
            compound.setTag("Running", this.currentRecipe.serializeNBT());
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        this.workingEnabled = compound.getBoolean("WorkEnabled");
        this.canRecipeProgress = compound.getBoolean("CanRecipeProgress");
        this.progressTime = compound.getDouble("Progress");
        if (compound.hasKey("RunType"))
            this.currentRecipe = RecipeRunRegistry.deserialize(compound.getString("RunType"),
                    compound.getCompoundTag("Running"));
        else {
            if (progressTime <= 0) return;
            this.currentRecipe = RecipeRunRegistry.deserialize("Legacy", compound);
        }
        this.isActive = currentRecipe != null;
    }
}
