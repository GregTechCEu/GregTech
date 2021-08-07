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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.LongSupplier;

public abstract class AbstractRecipeLogic extends MTETrait implements IWorkable {

    private static final String ALLOW_OVERCLOCKING = "AllowOverclocking";
    private static final String OVERCLOCK_VOLTAGE = "OverclockVoltage";

    public final RecipeMap<?> recipeMap;

    protected boolean forceRecipeRecheck;
    protected ItemStack[] lastItemInputs;
    protected FluidStack[] lastFluidInputs;
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
        if(capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        } else if(capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return null;
    }

    @Override
    public void update() {
        if (!getMetaTileEntity().getWorld().isRemote) {
            if (workingEnabled) {
                if (progressTime > 0) {
                    updateRecipeProgress();
                }
                if (progressTime == 0) {
                    trySearchNewRecipe();
                }
            }
            if (wasActiveAndNeedsUpdate) {
                this.wasActiveAndNeedsUpdate = false;
                setActive(false);
            }
        }
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
        if (previousRecipe != null && previousRecipe.matches(false, importInventory, importFluids)) {
            //if previous recipe still matches inputs, try to use it
            currentRecipe = previousRecipe;
        } else {
            boolean dirty = checkRecipeInputsDirty(importInventory, importFluids);
            if (dirty || forceRecipeRecheck) {
                this.forceRecipeRecheck = false;
                //else, try searching new recipe for given inputs
                currentRecipe = findRecipe(maxVoltage, importInventory, importFluids, MatchingMode.DEFAULT);
                if (currentRecipe != null) {
                    this.previousRecipe = currentRecipe;
                }
            }
        }
        if (currentRecipe != null && setupAndConsumeRecipeInputs(currentRecipe)) {
            setupRecipe(currentRecipe);
        }
    }

    public void forceRecipeRecheck() {
        this.forceRecipeRecheck = true;
    }

    protected int getMinTankCapacity(IMultipleTankHandler tanks) {
        if(tanks.getTanks() == 0) {
            return 0;
        }
        int result = Integer.MAX_VALUE;
        for(IFluidTank fluidTank : tanks.getFluidTanks()) {
            result = Math.min(fluidTank.getCapacity(), result);
        }
        return result;
    }

    protected Recipe findRecipe(long maxVoltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, MatchingMode mode) {
        return recipeMap.findRecipe(maxVoltage, inputs, fluidInputs, getMinTankCapacity(getOutputTank()), mode);
    }

    protected boolean checkRecipeInputsDirty(IItemHandler inputs, IMultipleTankHandler fluidInputs) {
        boolean shouldRecheckRecipe = false;
        if (lastItemInputs == null || lastItemInputs.length != inputs.getSlots()) {
            this.lastItemInputs = new ItemStack[inputs.getSlots()];
            Arrays.fill(lastItemInputs, ItemStack.EMPTY);
        }
        if (lastFluidInputs == null || lastFluidInputs.length != fluidInputs.getTanks()) {
            this.lastFluidInputs = new FluidStack[fluidInputs.getTanks()];
        }
        for (int i = 0; i < lastItemInputs.length; i++) {
            ItemStack currentStack = inputs.getStackInSlot(i);
            ItemStack lastStack = lastItemInputs[i];
            if (!areItemStacksEqual(currentStack, lastStack)) {
                this.lastItemInputs[i] = currentStack.isEmpty() ? ItemStack.EMPTY : currentStack.copy();
                shouldRecheckRecipe = true;
            } else if (currentStack.getCount() != lastStack.getCount()) {
                lastStack.setCount(currentStack.getCount());
                shouldRecheckRecipe = true;
            }
        }
        for (int i = 0; i < lastFluidInputs.length; i++) {
            FluidStack currentStack = fluidInputs.getTankAt(i).getFluid();
            FluidStack lastStack = lastFluidInputs[i];
            if ((currentStack == null && lastStack != null) ||
                (currentStack != null && !currentStack.isFluidEqual(lastStack))) {
                this.lastFluidInputs[i] = currentStack == null ? null : currentStack.copy();
                shouldRecheckRecipe = true;
            } else if (currentStack != null && lastStack != null &&
                currentStack.amount != lastStack.amount) {
                lastStack.amount = currentStack.amount;
                shouldRecheckRecipe = true;
            }
        }
        return shouldRecheckRecipe;
    }

    protected static boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB) {
        return (stackA.isEmpty() && stackB.isEmpty()) ||
            (ItemStack.areItemsEqual(stackA, stackB) &&
                ItemStack.areItemStackTagsEqual(stackA, stackB));
    }

    protected boolean setupAndConsumeRecipeInputs(Recipe recipe) {
        int[] resultOverclock = calculateOverclock(recipe.getEUt(), recipe.getDuration());
        int totalEUt = resultOverclock[0] * resultOverclock[1];
        IItemHandlerModifiable importInventory = getInputInventory();
        IItemHandlerModifiable exportInventory = getOutputInventory();
        IMultipleTankHandler importFluids = getInputTank();
        IMultipleTankHandler exportFluids = getOutputTank();
        return (totalEUt >= 0 ? getEnergyStored() >= (totalEUt > getEnergyCapacity() / 2 ? resultOverclock[0] : totalEUt) :
            (getEnergyStored() - resultOverclock[0] <= getEnergyCapacity())) &&
            MetaTileEntity.addItemsToItemHandler(exportInventory, true, recipe.getAllItemOutputs(exportInventory.getSlots())) &&
            MetaTileEntity.addFluidsToFluidHandler(exportFluids, true, recipe.getFluidOutputs()) &&
            recipe.matches(true, importInventory, importFluids);
    }

    protected int[] calculateOverclock(int EUt, int duration) {
        return calculateOverclock(EUt, this.overclockPolicy.getAsLong(), duration);
    }

    protected int[] calculateOverclock(int EUt, long voltage, int duration) {
        if (!allowOverclocking) {
            return new int[] {EUt, duration};
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
        final String[] result = new String[maxTier + 2];
        result[0] = "gregtech.gui.overclock.off";
        for (int i = 0; i < maxTier + 1; ++i) {
            result[i+1] = GTValues.VN[i];
        }
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
        //force recipe recheck because inputs could have changed since last time
        //we checked them before starting our recipe, especially if recipe took long time
        this.forceRecipeRecheck = true;
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
        if (!metaTileEntity.getWorld().isRemote) {
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
     *
     * NOTE: This should only be used directly after construction of the workable.
     * Use setOverclockVoltage() or setOverclockTier() for a more dynamic use case.
     */
    public void enableOverclockVoltage() {
        setOverclockVoltage(getMaxVoltage());
    }

    // The overclocking tier
    // it is 1 greater than the index into GTValues.V since here the "0 tier" represents 0 EU or no overclock
    public int getOverclockTier() {
        if (this.overclockVoltage == 0) {
            return 0;
        }
        return 1 + getOverclockingTier(this.overclockVoltage);
    }

    public void setOverclockTier(final int tier) {
        if (tier == 0) {
            setOverclockVoltage(0);
            return;
        }
        setOverclockVoltage(getVoltageByTier(tier - 1));
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
        if(compound.hasKey(ALLOW_OVERCLOCKING)) {
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
