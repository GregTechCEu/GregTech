package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.IWorkable;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.MatchingMode;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.world.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.LongSupplier;

public abstract class AbstractRecipeLogic extends MTETrait implements IWorkable {

    private static final String ALLOW_OVERCLOCKING = "AllowOverclocking";
    private static final String OVERCLOCK_VOLTAGE = "OverclockVoltage";

    public final RecipeMap<?> recipeMap;

    protected Recipe previousRecipe;
    protected boolean allowOverclocking = true;
    private long overclockVoltage = 0;
    private LongSupplier overclockPolicy = this::getMaxVoltage;

    protected int progressTime;
    protected int maxProgressTime;
    protected int recipeEUt;
    protected List<FluidStack> fluidOutputs;
    protected NonNullList<ItemStack> itemOutputs;
    protected final Random random = new Random();

    protected boolean isActive;
    protected boolean workingEnabled = true;
    protected boolean hasNotEnoughEnergy;
    protected boolean wasActiveAndNeedsUpdate;
    protected boolean isOutputsFull;
    protected boolean invalidInputsForRecipes;

    protected boolean hasPerfectOC = false;

    public AbstractRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap) {
        super(tileEntity);
        this.recipeMap = recipeMap;
    }

    public AbstractRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, boolean hasPerfectOC) {
        super(tileEntity);
        this.recipeMap = recipeMap;
        this.hasPerfectOC = hasPerfectOC;
    }

    protected abstract long getEnergyStored();

    protected abstract long getEnergyCapacity();

    protected abstract boolean drawEnergy(int recipeEUt);

    protected abstract long getMaxVoltage();

    protected IItemHandlerModifiable getInputInventory() {
        return metaTileEntity.getImportItems();
    }

    protected IItemHandlerModifiable getOutputInventory() {
        return metaTileEntity.getExportItems();
    }

    protected IMultipleTankHandler getInputTank() {
        return metaTileEntity.getImportFluids();
    }

    protected IMultipleTankHandler getOutputTank() {
        return metaTileEntity.getExportFluids();
    }

    @Override
    public String getName() {
        return "RecipeMapWorkable";
    }

    @Override
    public int getNetworkID() {
        return TraitNetworkIds.TRAIT_ID_WORKABLE;
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
                if (progressTime > 0) {
                    updateRecipeProgress();
                }
                //check everything that would make a recipe never start here.
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

    protected boolean shouldSearchForRecipes() {
        return canWorkWithInputs() && canFitNewOutputs();
    }

    protected boolean hasNotifiedInputs() {
        return (metaTileEntity.getNotifiedItemInputList().size() > 0 ||
                metaTileEntity.getNotifiedFluidInputList().size() > 0);
    }

    protected boolean hasNotifiedOutputs() {
        return (metaTileEntity.getNotifiedItemOutputList().size() > 0 ||
                metaTileEntity.getNotifiedFluidOutputList().size() > 0);
    }

    protected boolean canFitNewOutputs() {
        // if the output is full check if the output changed so we can process recipes results again.
        if (this.isOutputsFull && !hasNotifiedOutputs()) return false;
        else {
            this.isOutputsFull = false;
            metaTileEntity.getNotifiedItemOutputList().clear();
            metaTileEntity.getNotifiedFluidOutputList().clear();
        }
        return true;
    }

    protected boolean canWorkWithInputs() {
        // if the inputs were bad last time, check if they've changed before trying to find a new recipe.
        if (this.invalidInputsForRecipes && !hasNotifiedInputs()) return false;
        else {
            this.invalidInputsForRecipes = false;
        }
        return true;
    }

    protected void updateRecipeProgress() {
        boolean drawEnergy = drawEnergy(recipeEUt);
        if (drawEnergy || (recipeEUt < 0)) {
            //as recipe starts with progress on 1 this has to be > only not => to compensate for it
            if (++progressTime > maxProgressTime) {
                completeRecipe();
            }
        } else if (recipeEUt > 0) {
            //only set hasNotEnoughEnergy if this recipe is consuming recipe
            //generators always have enough energy
            this.hasNotEnoughEnergy = true;
            //if current progress value is greater than 2, decrement it by 2
            if (progressTime >= 2) {
                if (ConfigHolder.insufficientEnergySupplyWipesRecipeProgress) {
                    this.progressTime = 1;
                } else {
                    this.progressTime = Math.max(1, progressTime - 2);
                }
            }
        }
    }

    protected void trySearchNewRecipe() {
        long maxVoltage = getMaxVoltage();
        Recipe currentRecipe = null;
        IItemHandlerModifiable importInventory = getInputInventory();
        IMultipleTankHandler importFluids = getInputTank();

        // see if the last recipe we used still works
        if (this.previousRecipe != null && this.previousRecipe.matches(false, importInventory, importFluids))
            currentRecipe = this.previousRecipe;
            // If there is no active recipe, then we need to find one.
        else {
            currentRecipe = findRecipe(maxVoltage, importInventory, importFluids, MatchingMode.DEFAULT);
        }
        // If a recipe was found, then inputs were valid. Cache found recipe.
        if (currentRecipe != null) {
            this.previousRecipe = currentRecipe;
        }
        this.invalidInputsForRecipes = (currentRecipe == null);

        // proceed if we have a usable recipe.
        if (currentRecipe != null && setupAndConsumeRecipeInputs(currentRecipe, importInventory))
            setupRecipe(currentRecipe);
        // Inputs have been inspected.
        metaTileEntity.getNotifiedItemInputList().clear();
        metaTileEntity.getNotifiedFluidInputList().clear();
    }

    protected int getMinTankCapacity(IMultipleTankHandler tanks) {
        if (tanks.getTanks() == 0) {
            return 0;
        }
        int result = Integer.MAX_VALUE;
        for (IFluidTank fluidTank : tanks.getFluidTanks()) {
            result = Math.min(fluidTank.getCapacity(), result);
        }
        return result;
    }

    protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, MatchingMode mode) {
        return recipeMap.findRecipe(maxVoltage, inputs, fluidInputs, getMinTankCapacity(getOutputTank()), mode);
    }

    protected static boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB) {
        return (stackA.isEmpty() && stackB.isEmpty()) ||
                (ItemStack.areItemsEqual(stackA, stackB) &&
                        ItemStack.areItemStackTagsEqual(stackA, stackB));
    }

    /**
     * Determines if the provided recipe is possible to run from the provided inventory, or if there is anything preventing
     * the Recipe from being completed.
     *
     * Will consume the inputs of the Recipe if it is possible to run.
     *
     * @param recipe - The Recipe that will be consumed from the inputs and ran in the machine
     * @param importInventory - The inventory that the recipe should be consumed from.
     *                        Used mainly for Distinct bus implementation for multiblocks to specify
     *                        a specific bus
     * @return - true if the recipe is successful, false if the recipe is not successful
     */
    protected boolean setupAndConsumeRecipeInputs(Recipe recipe, IItemHandlerModifiable importInventory) {
        int[] resultOverclock = calculateOverclock(recipe.getEUt(), recipe.getDuration());
        int totalEUt = resultOverclock[0] * resultOverclock[1];
        IItemHandlerModifiable exportInventory = getOutputInventory();
        IMultipleTankHandler importFluids = getInputTank();
        IMultipleTankHandler exportFluids = getOutputTank();
        if (!(totalEUt >= 0 ? getEnergyStored() >= (totalEUt > getEnergyCapacity() / 2 ? resultOverclock[0] : totalEUt) :
            (getEnergyStored() - resultOverclock[0] <= getEnergyCapacity()))) {
            return false;
        }
        if (!MetaTileEntity.addItemsToItemHandler(exportInventory, true, recipe.getAllItemOutputs(exportInventory.getSlots()))) {
            this.isOutputsFull = true;
            return false;
        }
        if (!MetaTileEntity.addFluidsToFluidHandler(exportFluids, true, recipe.getFluidOutputs())) {
            this.isOutputsFull = true;
            return false;
        }
        this.isOutputsFull = false;
        return recipe.matches(true, importInventory, importFluids);
    }

    protected int[] calculateOverclock(int EUt, int duration) {
        return calculateOverclock(EUt, this.overclockPolicy.getAsLong(), duration);
    }

    protected int[] calculateOverclock(int EUt, long voltage, int duration) {
        if (!allowOverclocking) {
            return new int[]{EUt, duration};
        }
        boolean negativeEU = EUt < 0;
        int tier = getOverclockingTier(voltage);

        // Cannot overclock
        if (GTValues.V[tier] <= EUt || tier == 0)
            return new int[]{EUt, duration};

        if (negativeEU)
            EUt = -EUt;

        int resultEUt = EUt;
        double resultDuration = duration;
        double divisor = hasPerfectOC ? 4.0 : ConfigHolder.U.overclockDivisor;
        int maxOverclocks = tier - 1; // exclude ULV overclocking

        //do not overclock further if duration is already too small
        while (resultDuration >= 3 && resultEUt <= GTValues.V[tier - 1] && maxOverclocks != 0) {
            resultEUt *= 4;
            resultDuration /= divisor;
            maxOverclocks--;
        }
        return new int[]{negativeEU ? -resultEUt : resultEUt, (int) Math.ceil(resultDuration)};
    }

    protected int getOverclockingTier(long voltage) {
        return GTUtility.getTierByVoltage(voltage);
    }

    protected long getVoltageByTier(final int tier) {
        return GTValues.V[tier];
    }

    public String[] getAvailableOverclockingTiers() {
        final int maxTier = getOverclockingTier(getMaxVoltage());
        final String[] result = new String[maxTier + 1];
        result[0] = "gregtech.gui.overclock.off";
        if (maxTier >= 0) System.arraycopy(GTValues.VN, 1, result, 1, maxTier);
        return result;
    }

    protected void setupRecipe(Recipe recipe) {
        int[] resultOverclock = calculateOverclock(recipe.getEUt(), recipe.getDuration());
        this.progressTime = 1;
        setMaxProgress(resultOverclock[1]);
        this.recipeEUt = resultOverclock[0];
        this.fluidOutputs = GTUtility.copyFluidList(recipe.getFluidOutputs());
        int tier = getMachineTierForRecipe(recipe);
        this.itemOutputs = GTUtility.copyStackList(recipe.getResultItemOutputs(getOutputInventory().getSlots(), random, tier));
        if (this.wasActiveAndNeedsUpdate) {
            this.wasActiveAndNeedsUpdate = false;
        } else {
            this.setActive(true);
        }
    }

    protected int getMachineTierForRecipe(Recipe recipe) {
        return GTUtility.getTierByVoltage(getMaxVoltage());
    }

    protected void completeRecipe() {
        MetaTileEntity.addItemsToItemHandler(getOutputInventory(), false, itemOutputs);
        MetaTileEntity.addFluidsToFluidHandler(getOutputTank(), false, fluidOutputs);
        this.progressTime = 0;
        setMaxProgress(0);
        this.recipeEUt = 0;
        this.fluidOutputs = null;
        this.itemOutputs = null;
        this.hasNotEnoughEnergy = false;
        this.wasActiveAndNeedsUpdate = true;
    }

    public double getProgressPercent() {
        return getMaxProgress() == 0 ? 0.0 : getProgress() / (getMaxProgress() * 1.0);
    }

    public int getTicksTimeLeft() {
        return maxProgressTime == 0 ? 0 : (maxProgressTime - progressTime);
    }

    @Override
    public int getProgress() {
        return progressTime;
    }

    @Override
    public int getMaxProgress() {
        return maxProgressTime;
    }

    public int getRecipeEUt() {
        return recipeEUt;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgressTime = maxProgress;
        metaTileEntity.markDirty();
    }

    protected void setActive(boolean active) {
        this.isActive = active;
        metaTileEntity.markDirty();
        World world = metaTileEntity.getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(1, buf -> buf.writeBoolean(active));
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        metaTileEntity.markDirty();
    }

    public void setAllowOverclocking(boolean allowOverclocking) {
        this.allowOverclocking = allowOverclocking;
        this.overclockVoltage = allowOverclocking ? getMaxVoltage() : 0;
        metaTileEntity.markDirty();
    }

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

    public boolean isAllowOverclocking() {
        return allowOverclocking;
    }

    public long getOverclockVoltage() {
        return overclockVoltage;
    }

    public void setOverclockVoltage(final long overclockVoltage) {
        this.overclockPolicy = this::getOverclockVoltage;
        this.overclockVoltage = overclockVoltage;
        this.allowOverclocking = (overclockVoltage != 0);
        metaTileEntity.markDirty();
    }

    /**
     * Sets the overclocking policy to use getOverclockVoltage() instead of getMaxVoltage()
     * and initialises the overclock voltage to max voltage.
     * The actual value will come from the saved tag when the tile is loaded for pre-existing machines.
     * <p>
     * NOTE: This should only be used directly after construction of the workable.
     * Use setOverclockVoltage() or setOverclockTier() for a more dynamic use case.
     */
    public void enableOverclockVoltage() {
        setOverclockVoltage(getMaxVoltage());
    }

    public int getOverclockTier() {
        if (this.overclockVoltage == 0) {
            return 0;
        }
        return getOverclockingTier(this.overclockVoltage);
    }

    public void setOverclockTier(final int tier) {
        if (tier == 0) {
            setOverclockVoltage(0);
            return;
        }
        setOverclockVoltage(getVoltageByTier(tier));
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == 1) {
            this.isActive = buf.readBoolean();
            getMetaTileEntity().getHolder().scheduleChunkForRenderUpdate();
        }
    }

    @Override
    public void writeInitialData(PacketBuffer buf) {
        buf.writeBoolean(this.isActive);
    }

    @Override
    public void receiveInitialData(PacketBuffer buf) {
        this.isActive = buf.readBoolean();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("WorkEnabled", workingEnabled);
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
    public void deserializeNBT(NBTTagCompound compound) {
        this.workingEnabled = compound.getBoolean("WorkEnabled");
        this.progressTime = compound.getInteger("Progress");
        if (compound.hasKey(ALLOW_OVERCLOCKING)) {
            this.allowOverclocking = compound.getBoolean(ALLOW_OVERCLOCKING);
        }
        if (compound.hasKey(OVERCLOCK_VOLTAGE)) {
            this.overclockVoltage = compound.getLong(OVERCLOCK_VOLTAGE);
        } else {
            // Calculate overclock voltage based on old allow flag
            this.overclockVoltage = this.allowOverclocking ? getMaxVoltage() : 0;
        }
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
