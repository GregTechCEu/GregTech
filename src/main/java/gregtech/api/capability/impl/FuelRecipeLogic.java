package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.*;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.machines.FuelRecipeMap;
import gregtech.api.recipes.recipes.FuelRecipe;
import gregtech.api.util.GTUtility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class FuelRecipeLogic extends MTETrait implements IControllable, IFuelable {

    public final FuelRecipeMap recipeMap;
    protected FuelRecipe previousRecipe;

    protected final Supplier<IEnergyContainer> energyContainer;
    protected final Supplier<IMultipleTankHandler> fluidTank;
    public final long maxVoltage;

    private int recipeDurationLeft;
    private long recipeOutputVoltage;

    private boolean isActive;
    private boolean workingEnabled = true;
    private boolean wasActiveAndNeedsUpdate = false;

    public FuelRecipeLogic(MetaTileEntity metaTileEntity, FuelRecipeMap recipeMap, Supplier<IEnergyContainer> energyContainer, Supplier<IMultipleTankHandler> fluidTank, long maxVoltage) {
        super(metaTileEntity);
        this.recipeMap = recipeMap;
        this.energyContainer = energyContainer;
        this.fluidTank = fluidTank;
        this.maxVoltage = maxVoltage;
    }

    public long getRecipeOutputVoltage() {
        return recipeOutputVoltage;
    }

    @Override
    public String getName() {
        return "FuelRecipeMapWorkable";
    }

    @Override
    public int getNetworkID() {
        return TraitNetworkIds.TRAIT_ID_WORKABLE;
    }

    @Override
    public Collection<IFuelInfo> getFuels() {
        if (!isReadyForRecipes())
            return Collections.emptySet();
        final IMultipleTankHandler fluidTanks = this.fluidTank.get();
        if (fluidTanks == null)
            return Collections.emptySet();

        final LinkedHashMap<String, IFuelInfo> fuels = new LinkedHashMap<>();
        // Fuel capacity is all tanks
        int fuelCapacity = 0;
        for (IFluidTank fluidTank : fluidTanks) {
            fuelCapacity += fluidTank.getCapacity();
        }

        for (IFluidTank fluidTank : fluidTanks) {
            final FluidStack tankContents = fluidTank.drain(Integer.MAX_VALUE, false);
            if (tankContents == null || tankContents.amount <= 0)
                continue;
            int fuelRemaining = tankContents.amount;
            FuelRecipe recipe = findRecipe(tankContents);
            if (recipe == null)
                continue;
            int amountPerRecipe = calculateFuelAmount(recipe);
            int duration = calculateRecipeDuration(recipe);
            long fuelBurnTime = (duration * fuelRemaining) / amountPerRecipe;

            FluidFuelInfo fuelInfo = (FluidFuelInfo) fuels.get(tankContents.getUnlocalizedName());
            if (fuelInfo == null) {
                fuelInfo = new FluidFuelInfo(tankContents, fuelRemaining, fuelCapacity, amountPerRecipe, fuelBurnTime);
                fuels.put(tankContents.getUnlocalizedName(), fuelInfo);
            }
            else {
                fuelInfo.addFuelRemaining(fuelRemaining);
                fuelInfo.addFuelBurnTime(fuelBurnTime);
            }
        }
        return fuels.values();
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if(capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        if(capability == GregtechCapabilities.CAPABILITY_FUELABLE) {
            return GregtechCapabilities.CAPABILITY_FUELABLE.cast(this);
        }
        return null;
    }

    @Override
    public void update() {
        if (getMetaTileEntity().getWorld().isRemote) return;
        if (workingEnabled) {
            if (recipeDurationLeft > 0) {
                if (energyContainer.get().getEnergyCanBeInserted() >=
                    recipeOutputVoltage || shouldVoidExcessiveEnergy()) {
                    energyContainer.get().addEnergy(recipeOutputVoltage);
                    if (--this.recipeDurationLeft == 0) {
                        this.wasActiveAndNeedsUpdate = true;
                    }
                }
            }
            if (recipeDurationLeft == 0 && isReadyForRecipes()) {
                tryAcquireNewRecipe();
            }
        }
        if (wasActiveAndNeedsUpdate) {
            setActive(false);
            this.wasActiveAndNeedsUpdate = false;
        }
    }

    protected boolean isReadyForRecipes() {
       return true;
    }

    protected boolean shouldVoidExcessiveEnergy() {
        return false;
    }

    private void tryAcquireNewRecipe() {
        IMultipleTankHandler fluidTanks = this.fluidTank.get();
        for (IFluidTank fluidTank : fluidTanks) {
            FluidStack tankContents = fluidTank.getFluid();
            if (tankContents != null && tankContents.amount > 0) {
                int fuelAmountUsed = tryAcquireNewRecipe(tankContents);
                if (fuelAmountUsed > 0) {
                    fluidTank.drain(fuelAmountUsed, true);
                    break; //recipe is found and ready to use
                }
            }
        }
    }

    public boolean isActive() {
        return isActive;
    }

    private int tryAcquireNewRecipe(FluidStack fluidStack) {
        FuelRecipe currentRecipe;
        if (previousRecipe != null && previousRecipe.matches(getMaxVoltage(), fluidStack)) {
            //if previous recipe still matches inputs, try to use it
            currentRecipe = previousRecipe;
        } else {
            //else, try searching new recipe for given inputs
            currentRecipe = recipeMap.findRecipe(getMaxVoltage(), fluidStack);
            //if we found recipe that can be buffered, buffer it
            if (currentRecipe != null) {
                this.previousRecipe = currentRecipe;
            }
        }
        if (currentRecipe != null && checkRecipe(currentRecipe)) {
            int fuelAmountToUse = calculateFuelAmount(currentRecipe);
            if (fluidStack.amount >= fuelAmountToUse) {
                this.recipeDurationLeft = calculateRecipeDuration(currentRecipe);
                this.recipeOutputVoltage = startRecipe(currentRecipe, fuelAmountToUse, recipeDurationLeft);
                if (wasActiveAndNeedsUpdate) {
                    this.wasActiveAndNeedsUpdate = false;
                } else {
                    setActive(true);
                }
                return fuelAmountToUse;
            }
        }
        return 0;
    }

    // Similar to tryAcquire but with no side effects
    private FuelRecipe findRecipe(FluidStack fluidStack) {
        FuelRecipe currentRecipe;
        if (previousRecipe != null && previousRecipe.matches(getMaxVoltage(), fluidStack)) {
            currentRecipe = previousRecipe;
        } else {
            currentRecipe = recipeMap.findRecipe(getMaxVoltage(), fluidStack);
        }
        if (currentRecipe != null && checkRecipe(currentRecipe))
            return currentRecipe;
        return null;
    }

    protected boolean checkRecipe(FuelRecipe recipe) {
        return true;
    }

    public long getMaxVoltage() {
        return maxVoltage;
    }

    protected int calculateFuelAmount(FuelRecipe currentRecipe) {
        return currentRecipe.getRecipeFluid().amount * getVoltageMultiplier(getMaxVoltage(), currentRecipe.getMinVoltage());
    }

    protected int calculateRecipeDuration(FuelRecipe currentRecipe) {
        return currentRecipe.getDuration();
    }

    /**
     * Performs preparations for starting given recipe and determines it's output voltage
     *
     * @return recipe's output voltage
     */
    protected long startRecipe(FuelRecipe currentRecipe, int fuelAmountUsed, int recipeDuration) {
        return getMaxVoltage();
    }

    public static int getVoltageMultiplier(long maxVoltage, long minVoltage) {
        return (int) (maxVoltage / minVoltage);
    }

    public static long getTieredVoltage(long voltage) {
        return GTValues.V[GTUtility.getTierByVoltage(voltage)];
    }

    protected void setActive(boolean active) {
        // If we are changing states (active -> inactive), clear remaining recipe duration
        if (this.isActive && !active) {
            recipeDurationLeft = 0;
        }
        this.isActive = active;
        if (!metaTileEntity.getWorld().isRemote) {
            metaTileEntity.markDirty();
            writeCustomData(1, buf -> buf.writeBoolean(active));
        }
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        metaTileEntity.markDirty();
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
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
        compound.setBoolean("WorkEnabled", this.workingEnabled);
        compound.setInteger("RecipeDurationLeft", this.recipeDurationLeft);
        if (recipeDurationLeft > 0) {
            compound.setLong("RecipeOutputVoltage", this.recipeOutputVoltage);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound compound) {
        if (!compound.hasKey("WorkEnabled", NBT.TAG_BYTE)) {
            //change working mode only if there is a tag compound with it's value
            this.workingEnabled = compound.getBoolean("WorkEnabled");
        }
        this.recipeDurationLeft = compound.getInteger("RecipeDurationLeft");
        if (recipeDurationLeft > 0) {
            this.recipeOutputVoltage = compound.getLong("RecipeOutputVoltage");
        }
        this.isActive = recipeDurationLeft > 0;
    }

}
