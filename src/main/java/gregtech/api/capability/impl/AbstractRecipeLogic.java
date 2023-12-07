package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.*;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.IParallelableRecipeLogic;
import gregtech.api.recipes.recipeproperties.CleanroomProperty;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.recipes.logic.OverclockingLogic.*;

public abstract class AbstractRecipeLogic extends MTETrait implements IWorkable, IParallelableRecipeLogic {

    private static final String ALLOW_OVERCLOCKING = "AllowOverclocking";
    private static final String OVERCLOCK_VOLTAGE = "OverclockVoltage";

    private final RecipeMap<?> recipeMap;

    protected Recipe previousRecipe;
    private boolean allowOverclocking = true;
    protected int parallelRecipesPerformed;
    private long overclockVoltage = 0;
    protected int[] overclockResults;

    protected boolean canRecipeProgress = true;

    protected int progressTime;
    protected int maxProgressTime;
    protected int recipeEUt;
    protected List<FluidStack> fluidOutputs;
    protected NonNullList<ItemStack> itemOutputs;

    protected boolean isActive;
    protected boolean workingEnabled = true;
    protected boolean hasNotEnoughEnergy;
    protected boolean wasActiveAndNeedsUpdate;
    protected boolean isOutputsFull;
    protected boolean invalidInputsForRecipes;

    protected boolean hasPerfectOC = false;

    /**
     * DO NOT use the parallelLimit field directly, EVER
     * use {@link AbstractRecipeLogic#setParallelLimit(int)} instead
     */
    private int parallelLimit = 1;

    public AbstractRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap) {
        super(tileEntity);
        this.recipeMap = recipeMap;
    }

    public AbstractRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, boolean hasPerfectOC) {
        super(tileEntity);
        this.recipeMap = recipeMap;
        this.hasPerfectOC = hasPerfectOC;
    }

    /**
     * @return the energy container's energy input per second
     */
    protected abstract long getEnergyInputPerSecond();

    /**
     * @return the energy container's current stored energy
     */
    protected abstract long getEnergyStored();

    /**
     * @return the energy container's maximum energy capacity
     */
    protected abstract long getEnergyCapacity();

    /**
     * Draw energy from the energy container
     *
     * @param recipeEUt the EUt to remove
     * @param simulate  whether to simulate energy extraction or not
     * @return true if the energy can/was drained, otherwise false
     */
    protected abstract boolean drawEnergy(int recipeEUt, boolean simulate);

    /**
     * @return the maximum voltage the machine can use/handle for recipe searching
     */
    public abstract long getMaxVoltage();

    /**
     *
     * @return the maximum voltage the machine can use/handle for parallel recipe creation
     */
    protected long getMaxParallelVoltage() {
        return getMaxVoltage();
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

    /**
     * @return true if energy is consumed by this Recipe Logic, otherwise false
     */
    public boolean consumesEnergy() {
        return true;
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

                if (progressTime > 0) {
                    updateRecipeProgress();
                }
                // check everything that would make a recipe never start here.
                if (progressTime == 0 && shouldSearchForRecipes()) {
                    trySearchNewRecipe();
                }
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
    @Override
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
    protected boolean shouldSearchForRecipes() {
        return canWorkWithInputs() && canFitNewOutputs();
    }

    /**
     * @return true if input inventory contents have changed
     */
    protected boolean hasNotifiedInputs() {
        return (metaTileEntity.getNotifiedItemInputList().size() > 0 ||
                metaTileEntity.getNotifiedFluidInputList().size() > 0);
    }

    /**
     * @return true if output inventory contents have changed
     */
    protected boolean hasNotifiedOutputs() {
        return (metaTileEntity.getNotifiedItemOutputList().size() > 0 ||
                metaTileEntity.getNotifiedFluidOutputList().size() > 0);
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
        if (this.invalidInputsForRecipes && !hasNotifiedInputs()) return false;

        // the change in inputs (especially by removal of ingredient by the player) might change the current valid
        // recipe.
        // and if the previous recipe produced fluids and the new recipe doesn't, then outputs are not full.
        this.isOutputsFull = false;
        this.invalidInputsForRecipes = false;
        this.metaTileEntity.getNotifiedItemInputList().clear();
        this.metaTileEntity.getNotifiedFluidInputList().clear();
        return true;
    }

    /**
     * Invalidate the current state of input inventory contents
     */
    @Override
    public void invalidateInputs() {
        this.invalidInputsForRecipes = true;
    }

    /**
     * Invalidate the current state of output inventory contents
     */
    @Override
    public void invalidateOutputs() {
        this.isOutputsFull = true;
    }

    /**
     * Set the amount of parallel recipes currently being performed
     *
     * @param amount the amount to set
     */
    @Override
    public void setParallelRecipesPerformed(int amount) {
        this.parallelRecipesPerformed = amount;
    }

    /**
     * Update the current running recipe's progress
     * <p>
     * Also handles consuming running energy by default
     * </p>
     */
    protected void updateRecipeProgress() {
        if (canRecipeProgress && drawEnergy(recipeEUt, true)) {
            drawEnergy(recipeEUt, false);
            // as recipe starts with progress on 1 this has to be > only not => to compensate for it
            if (++progressTime > maxProgressTime) {
                completeRecipe();
            }
            if (this.hasNotEnoughEnergy && getEnergyInputPerSecond() > 19L * recipeEUt) {
                this.hasNotEnoughEnergy = false;
            }
        } else if (recipeEUt > 0) {
            // only set hasNotEnoughEnergy if this recipe is consuming recipe
            // generators always have enough energy
            this.hasNotEnoughEnergy = true;
            decreaseProgress();
        }
    }

    /**
     * Decrease the recipe progress time in the case that some state was not right, like available EU to drain.
     */
    protected void decreaseProgress() {
        // if current progress value is greater than 2, decrement it by 2
        if (progressTime >= 2) {
            if (ConfigHolder.machines.recipeProgressLowEnergy) {
                this.progressTime = 1;
            } else {
                this.progressTime = Math.max(1, progressTime - 2);
            }
        }
    }

    /**
     * @return true if the recipe can progress, otherwise false
     */
    protected boolean canProgressRecipe() {
        if (previousRecipe == null) return true;
        return checkCleanroomRequirement(previousRecipe);
    }

    /**
     * Force the workable to search for new recipes.
     * This can be performance intensive. Use sparingly.
     */
    public void forceRecipeRecheck() {
        this.previousRecipe = null;
        trySearchNewRecipe();
    }

    /**
     * Try to search for a new recipe
     */
    protected void trySearchNewRecipe() {
        long maxVoltage = getMaxVoltage();
        Recipe currentRecipe;
        IItemHandlerModifiable importInventory = getInputInventory();
        IMultipleTankHandler importFluids = getInputTank();

        // see if the last recipe we used still works
        if (checkPreviousRecipe()) {
            currentRecipe = this.previousRecipe;
            // If there is no active recipe, then we need to find one.
        } else {
            currentRecipe = findRecipe(maxVoltage, importInventory, importFluids);
        }
        // If a recipe was found, then inputs were valid. Cache found recipe.
        if (currentRecipe != null) {
            this.previousRecipe = currentRecipe;
        }
        this.invalidInputsForRecipes = (currentRecipe == null);

        // proceed if we have a usable recipe.
        if (currentRecipe != null && checkRecipe(currentRecipe)) {
            prepareRecipe(currentRecipe);
        }
    }

    /**
     * @return true if the previous recipe is valid and can be run again
     */
    protected boolean checkPreviousRecipe() {
        if (this.previousRecipe == null) return false;
        if (this.previousRecipe.getEUt() > this.getMaxVoltage()) return false;
        return this.previousRecipe.matches(false, getInputInventory(), getInputTank());
    }

    /**
     * checks the recipe before preparing it
     *
     * @param recipe the recipe to check
     * @return true if the recipe is allowed to be used, else false
     */
    public boolean checkRecipe(@NotNull Recipe recipe) {
        return checkCleanroomRequirement(recipe);
    }

    /**
     * @param recipe the recipe to check
     * @return if the cleanroom requirement is met
     */
    protected boolean checkCleanroomRequirement(@NotNull Recipe recipe) {
        CleanroomType requiredType = recipe.getProperty(CleanroomProperty.getInstance(), null);
        if (requiredType == null) return true;

        MetaTileEntity mte = getMetaTileEntity();
        if (mte instanceof ICleanroomReceiver receiver) {
            if (ConfigHolder.machines.cleanMultiblocks && mte instanceof IMultiblockController) return true;

            ICleanroomProvider cleanroomProvider = receiver.getCleanroom();
            if (cleanroomProvider == null) return false;

            return cleanroomProvider.isClean() && cleanroomProvider.checkCleanroomType(requiredType);
        }

        return false;
    }

    /**
     * Prepares the recipe to be run.
     * <ol>
     * <li>The recipe is run in parallel if possible.</li>
     * <li>The potentially parallel recipe is then checked to exist.</li>
     * <li>If it exists, it checks if the recipe is runnable with the current inputs.</li>
     * </ol>
     * If the above conditions are met, the recipe is engaged to be run
     *
     * @param recipe the recipe to prepare
     * @return true if the recipe was successfully prepared, else false
     */
    protected boolean prepareRecipe(Recipe recipe) {
        recipe = Recipe.trimRecipeOutputs(recipe, getRecipeMap(), metaTileEntity.getItemOutputLimit(),
                metaTileEntity.getFluidOutputLimit());

        // Pass in the trimmed recipe to the parallel logic
        recipe = findParallelRecipe(
                recipe,
                getInputInventory(),
                getInputTank(),
                getOutputInventory(),
                getOutputTank(),
                getMaxParallelVoltage(),
                getParallelLimit());

        if (recipe != null && setupAndConsumeRecipeInputs(recipe, getInputInventory())) {
            setupRecipe(recipe);
            return true;
        }
        return false;
    }

    /**
     * DO NOT use the parallelLimit field directly, EVER
     *
     * @return the current parallel limit of the logic
     */
    public int getParallelLimit() {
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
     * @return the parallel logic type to use for recipes
     */
    @Override
    @NotNull
    public ParallelLogicType getParallelLogicType() {
        return ParallelLogicType.MULTIPLY;
    }

    /**
     * @param tanks the tanks to check
     * @return the minimum fluid capacity of the tanks
     */
    protected static int getMinTankCapacity(@NotNull IMultipleTankHandler tanks) {
        if (tanks.getTanks() == 0) {
            return 0;
        }
        int result = Integer.MAX_VALUE;
        for (IFluidTank fluidTank : tanks.getFluidTanks()) {
            result = Math.min(fluidTank.getCapacity(), result);
        }
        return result;
    }

    /**
     * Find a recipe using inputs
     * 
     * @param maxVoltage  the maximum voltage the recipe can have
     * @param inputs      the item inputs used to search for the recipe
     * @param fluidInputs the fluid inputs used to search for the recipe
     * @return the recipe if found, otherwise null
     */
    @Nullable
    protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs) {
        RecipeMap<?> map = getRecipeMap();
        if (map == null || !isRecipeMapValid(map)) {
            return null;
        }

        return map.findRecipe(maxVoltage, inputs, fluidInputs);
    }

    /**
     * @param recipeMap the recipemap to check
     * @return true if the recipemap is valid for recipe search
     */
    public boolean isRecipeMapValid(@NotNull RecipeMap<?> recipeMap) {
        return true;
    }

    /**
     * @param stackA the first stack to check
     * @param stackB the second stack to check
     * @return true if both ItemStacks are equal
     */
    protected static boolean areItemStacksEqual(@NotNull ItemStack stackA, @NotNull ItemStack stackB) {
        return (stackA.isEmpty() && stackB.isEmpty()) ||
                (ItemStack.areItemsEqual(stackA, stackB) &&
                        ItemStack.areItemStackTagsEqual(stackA, stackB));
    }

    /**
     * Determines if the provided recipe is possible to run from the provided inventory, or if there is anything
     * preventing
     * the Recipe from being completed.
     * <p>
     * Will consume the inputs of the Recipe if it is possible to run.
     *
     * @param recipe          - The Recipe that will be consumed from the inputs and ran in the machine
     * @param importInventory - The inventory that the recipe should be consumed from.
     *                        Used mainly for Distinct bus implementation for multiblocks to specify
     *                        a specific bus
     * @return - true if the recipe is successful, false if the recipe is not successful
     */
    protected boolean setupAndConsumeRecipeInputs(@NotNull Recipe recipe,
                                                  @NotNull IItemHandlerModifiable importInventory) {
        this.overclockResults = calculateOverclock(recipe);

        modifyOverclockPost(overclockResults, recipe.getRecipePropertyStorage());

        if (!hasEnoughPower(overclockResults)) {
            return false;
        }

        IItemHandlerModifiable exportInventory = getOutputInventory();
        IMultipleTankHandler importFluids = getInputTank();
        IMultipleTankHandler exportFluids = getOutputTank();

        // We have already trimmed outputs and chanced outputs at this time
        // Attempt to merge all outputs + chanced outputs into the output bus, to prevent voiding chanced outputs
        if (!metaTileEntity.canVoidRecipeItemOutputs() &&
                !GTTransferUtils.addItemsToItemHandler(exportInventory, true, recipe.getAllItemOutputs())) {
            this.isOutputsFull = true;
            return false;
        }

        // We have already trimmed fluid outputs at this time
        if (!metaTileEntity.canVoidRecipeFluidOutputs() &&
                !GTTransferUtils.addFluidsToFluidHandler(exportFluids, true, recipe.getAllFluidOutputs())) {
            this.isOutputsFull = true;
            return false;
        }

        this.isOutputsFull = false;
        if (recipe.matches(true, importInventory, importFluids)) {
            this.metaTileEntity.addNotifiedInput(importInventory);
            return true;
        }
        return false;
    }

    /**
     * @param resultOverclock the overclock data to use. Format: {@code [EUt, duration]}.
     * @return true if there is enough energy to continue recipe progress
     */
    protected boolean hasEnoughPower(@NotNull int[] resultOverclock) {
        // Format of resultOverclock: EU/t, duration
        int totalEUt = resultOverclock[0] * resultOverclock[1];

        // RIP Ternary
        // Power Consumption case
        if (totalEUt >= 0) {
            int capacity;
            // If the total consumed power is greater than half the internal capacity
            if (totalEUt > getEnergyCapacity() / 2) {
                // Only draw 1A of power from the internal buffer to allow for recharging of the internal buffer from
                // external sources
                capacity = resultOverclock[0];
            } else {
                // If the total consumed power is less than half the capacity, just drain the whole thing
                capacity = totalEUt;
            }

            // Return true if we have enough energy stored to progress the recipe, either 1A or the whole amount
            return getEnergyStored() >= capacity;
        }
        // Power Generation case
        else {
            // This is the EU/t generated by the generator
            int power = resultOverclock[0];
            // Return true if we can fit at least 1A of energy into the energy output
            return getEnergyStored() - (long) power <= getEnergyCapacity();
        }
    }

    /**
     * Method for modifying the overclock results, such as for Multiblock coil bonuses.
     * Is always called, even if no overclocks are performed.
     *
     * @param overclockResults The overclocked recipe EUt and duration, in format [EUt, duration]
     * @param storage          the RecipePropertyStorage of the recipe being processed
     */
    protected void modifyOverclockPost(int[] overclockResults, @NotNull IRecipePropertyStorage storage) {}

    /**
     * Calculates the overclocked Recipe's final duration and EU/t
     *
     * @param recipe the recipe to run
     * @return an int array of {OverclockedEUt, OverclockedDuration}
     */
    @NotNull
    protected int[] calculateOverclock(@NotNull Recipe recipe) {
        // perform the actual overclocking
        return performOverclocking(recipe);
    }

    /**
     * Determines the maximum number of overclocks that can be performed for a recipe.
     * Then performs overclocking on the Recipe.
     *
     * @param recipe the recipe to overclock
     * @return an int array of {OverclockedEUt, OverclockedDuration}
     */
    @NotNull
    protected int[] performOverclocking(@NotNull Recipe recipe) {
        int[] values = { recipe.getEUt(), recipe.getDuration(), getNumberOfOCs(recipe.getEUt()) };
        modifyOverclockPre(values, recipe.getRecipePropertyStorage());

        if (values[2] <= 0) {
            // number of OCs is <= 0, so do not overclock
            return new int[] { values[0], values[1] };
        }

        return runOverclockingLogic(recipe.getRecipePropertyStorage(), values[0], getMaximumOverclockVoltage(),
                values[1], values[2]);
    }

    /**
     * @param recipeEUt the EUt of the recipe
     * @return the number of times to overclock the recipe
     */
    protected int getNumberOfOCs(int recipeEUt) {
        if (!isAllowOverclocking()) return 0;

        int recipeTier = GTUtility.getTierByVoltage(recipeEUt);
        int maximumTier = getOverclockForTier(getMaximumOverclockVoltage());
        if (maximumTier <= GTValues.LV) return 0;

        // The maximum number of overclocks is determined by the difference between the tier the recipe is running at,
        // and the maximum tier that the machine can overclock to.
        int numberOfOCs = maximumTier - recipeTier;
        if (recipeTier == ULV) numberOfOCs--; // no ULV overclocking

        return numberOfOCs;
    }

    /**
     * Perform changes to the recipe EUt, duration, and OC count before overclocking.
     * Is always called, even if no overclocks are to be performed.
     *
     * @param values  an array of [recipeEUt, recipeDuration, numberOfOCs]
     * @param storage the RecipePropertyStorage of the recipe being processed
     */
    protected void modifyOverclockPre(@NotNull int[] values, @NotNull IRecipePropertyStorage storage) {}

    /**
     * Calls the desired overclocking logic to be run for the recipe.
     * Performs the actual overclocking on the provided recipe.
     * Override this to call custom overclocking mechanics
     *
     * @param propertyStorage the recipe's property storage
     * @param recipeEUt       the EUt of the recipe
     * @param maxVoltage      the maximum voltage the recipe is allowed to be run at
     * @param duration        the duration of the recipe
     * @param amountOC        the maximum amount of overclocks to perform
     * @return an int array of {OverclockedEUt, OverclockedDuration}
     */
    @NotNull
    protected int[] runOverclockingLogic(@NotNull IRecipePropertyStorage propertyStorage, int recipeEUt,
                                         long maxVoltage, int duration, int amountOC) {
        return standardOverclockingLogic(
                Math.abs(recipeEUt),
                maxVoltage,
                duration,
                amountOC,
                getOverclockingDurationDivisor(),
                getOverclockingVoltageMultiplier());
    }

    /**
     * @return the divisor to use for reducing duration upon overclocking
     */
    protected double getOverclockingDurationDivisor() {
        return hasPerfectOC ? PERFECT_OVERCLOCK_DURATION_DIVISOR : STANDARD_OVERCLOCK_DURATION_DIVISOR;
    }

    /**
     * @return the multiplier to use for increasing voltage upon overclocking
     */
    protected double getOverclockingVoltageMultiplier() {
        return STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER;
    }

    /**
     * Finds the maximum tier that a recipe can overclock to, when provided the maximum voltage a recipe can overclock
     * to.
     *
     * @param voltage The maximum voltage the recipe is allowed to overclock to.
     * @return the highest voltage tier the machine should use to overclock with
     */
    protected int getOverclockForTier(long voltage) {
        return GTUtility.getTierByVoltage(voltage);
    }

    /**
     * Creates an array of Voltage Names that the machine/multiblock can overclock to.
     * Since this is for use with the customizable overclock button, all tiers up to
     * {@link AbstractRecipeLogic#getMaxVoltage()}
     * are allowed, since the button is initialized to this value.
     *
     * @return a String array of the voltage names allowed to be used for overclocking
     */
    public String[] getAvailableOverclockingTiers() {
        final int maxTier = getOverclockForTier(getMaxVoltage());
        final String[] result = new String[maxTier + 1];
        result[0] = "gregtech.gui.overclock.off";
        if (maxTier >= 0) System.arraycopy(GTValues.VNF, 1, result, 1, maxTier);
        return result;
    }

    /**
     * sets up the recipe to be run
     *
     * @param recipe the recipe to run
     */
    protected void setupRecipe(Recipe recipe) {
        this.progressTime = 1;
        setMaxProgress(overclockResults[1]);
        this.recipeEUt = overclockResults[0];
        int recipeTier = GTUtility.getTierByVoltage(recipe.getEUt());
        int machineTier = getOverclockForTier(getMaximumOverclockVoltage());
        this.fluidOutputs = GTUtility
                .copyFluidList(recipe.getResultFluidOutputs(recipeTier, machineTier, getRecipeMap()));
        this.itemOutputs = GTUtility
                .copyStackList(recipe.getResultItemOutputs(recipeTier, machineTier, getRecipeMap()));

        if (this.wasActiveAndNeedsUpdate) {
            this.wasActiveAndNeedsUpdate = false;
        } else {
            this.setActive(true);
        }
    }

    /**
     * completes the recipe which was being run, and performs actions done upon recipe completion
     */
    protected void completeRecipe() {
        outputRecipeOutputs();
        this.progressTime = 0;
        setMaxProgress(0);
        this.recipeEUt = 0;
        this.fluidOutputs = null;
        this.itemOutputs = null;
        this.hasNotEnoughEnergy = false;
        this.wasActiveAndNeedsUpdate = true;
        this.parallelRecipesPerformed = 0;
        this.overclockResults = new int[] { 0, 0 };
    }

    /**
     * outputs the items created by the recipe
     */
    protected void outputRecipeOutputs() {
        GTTransferUtils.addItemsToItemHandler(getOutputInventory(), false, itemOutputs);
        GTTransferUtils.addFluidsToFluidHandler(getOutputTank(), false, fluidOutputs);
    }

    /**
     * @return the progress percentage towards completion. Format: {@code 0.1 = 10%}.
     */
    public double getProgressPercent() {
        return getMaxProgress() == 0 ? 0.0 : getProgress() / (getMaxProgress() * 1.0);
    }

    @Override
    public int getProgress() {
        return progressTime;
    }

    @Override
    public int getMaxProgress() {
        return maxProgressTime;
    }

    /**
     * @return the current recipe's EU/t
     */
    public int getRecipeEUt() {
        return recipeEUt;
    }

    /**
     * @return the current recipe's EU/t for TOP/Waila/Tricorder
     */
    public int getInfoProviderEUt() {
        return getRecipeEUt();
    }

    /**
     * @return the previous recipe's duration
     */
    public int getPreviousRecipeDuration() {
        return getPreviousRecipe() == null ? 0 : getPreviousRecipe().getDuration();
    }

    /**
     * sets the amount of ticks of running time to finish the recipe
     *
     * @param maxProgress the amount of ticks to set
     */
    public void setMaxProgress(int maxProgress) {
        this.maxProgressTime = maxProgress;
        metaTileEntity.markDirty();
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
     * Toggles if the Machine/Multiblock is allowed to overclock.
     *
     * @param allowOverclocking If overclocking is allowed
     */
    public void setAllowOverclocking(boolean allowOverclocking) {
        this.allowOverclocking = allowOverclocking;
        this.overclockVoltage = allowOverclocking ? getMaximumOverclockVoltage() : GTValues.V[GTValues.ULV];
        metaTileEntity.markDirty();
    }

    /**
     * @return Whether Overclocking is allowed for the current machine/multiblock
     */
    public boolean isAllowOverclocking() {
        return allowOverclocking;
    }

    /**
     * Sets the maximum voltage that the machine is allowed to overclock to.
     * If the passed tier is ULV, overclocking for this machine is disabled.
     *
     * @param overclockVoltage The maximum voltage that the machine can overclock to. This must correspond to a
     *                         voltage tier in <B>GTValues.V</>
     */
    public void setMaximumOverclockVoltage(final long overclockVoltage) {
        this.overclockVoltage = overclockVoltage;
        // Overclocking is not allowed if the passed voltage is ULV
        this.allowOverclocking = (overclockVoltage != GTValues.V[GTValues.ULV]);
        metaTileEntity.markDirty();
    }

    /**
     * @return The maximum voltage the machine/multiblock can overclock to
     */
    public long getMaximumOverclockVoltage() {
        return overclockVoltage;
    }

    /**
     * This is needed as CycleButtonWidget requires an IntSupplier, without making an Enum of tiers.
     *
     * @return The current Tier for the voltage the machine is allowed to overclock to
     */
    public int getOverclockTier() {
        // If we do not allow overclocking, return ULV tier
        if (!isAllowOverclocking()) {
            return GTValues.ULV;
        }

        // This will automatically handle ULV, and return 0
        return getOverclockForTier(this.overclockVoltage);
    }

    /**
     * Sets the maximum Tier that the machine/multiblock is allowed to overclock to.
     * This is used for the Overclock button in Machine GUIs.
     * This is needed as CycleButtonWidget requires an Int Supplier, without making an Enum of tiers.
     *
     * @param tier The maximum tier the multiblock/machine can overclock to
     */
    public void setOverclockTier(final int tier) {
        setMaximumOverclockVoltage(GTValues.V[tier]);
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
        compound.setBoolean(ALLOW_OVERCLOCKING, allowOverclocking);
        compound.setLong(OVERCLOCK_VOLTAGE, this.overclockVoltage);
        if (progressTime > 0) {
            compound.setInteger("Progress", progressTime);
            compound.setInteger("MaxProgress", maxProgressTime);
            compound.setInteger("RecipeEUt", this.recipeEUt);
            NBTTagList itemOutputsList = new NBTTagList();
            for (ItemStack itemOutput : itemOutputs) {
                itemOutputsList.appendTag(itemOutput.writeToNBT(new NBTTagCompound()));
            }
            NBTTagList fluidOutputsList = new NBTTagList();
            for (FluidStack fluidOutput : fluidOutputs) {
                fluidOutputsList.appendTag(fluidOutput.writeToNBT(new NBTTagCompound()));
            }
            compound.setTag("ItemOutputs", itemOutputsList);
            compound.setTag("FluidOutputs", fluidOutputsList);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        this.workingEnabled = compound.getBoolean("WorkEnabled");
        this.canRecipeProgress = compound.getBoolean("CanRecipeProgress");
        this.progressTime = compound.getInteger("Progress");
        this.allowOverclocking = compound.getBoolean(ALLOW_OVERCLOCKING);
        this.overclockVoltage = compound.getLong(OVERCLOCK_VOLTAGE);
        this.isActive = false;
        if (progressTime > 0) {
            this.isActive = true;
            this.maxProgressTime = compound.getInteger("MaxProgress");
            this.recipeEUt = compound.getInteger("RecipeEUt");
            NBTTagList itemOutputsList = compound.getTagList("ItemOutputs", Constants.NBT.TAG_COMPOUND);
            this.itemOutputs = NonNullList.create();
            for (int i = 0; i < itemOutputsList.tagCount(); i++) {
                this.itemOutputs.add(new ItemStack(itemOutputsList.getCompoundTagAt(i)));
            }
            NBTTagList fluidOutputsList = compound.getTagList("FluidOutputs", Constants.NBT.TAG_COMPOUND);
            this.fluidOutputs = new ArrayList<>();
            for (int i = 0; i < fluidOutputsList.tagCount(); i++) {
                this.fluidOutputs.add(FluidStack.loadFluidStackFromNBT(fluidOutputsList.getCompoundTagAt(i)));
            }
        }
    }
}
