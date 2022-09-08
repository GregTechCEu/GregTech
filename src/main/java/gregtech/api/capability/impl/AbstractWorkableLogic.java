package gregtech.api.capability.impl;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.ConfigHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

public abstract class AbstractWorkableLogic extends MTETrait implements IWorkable {

    protected int progressTime;
    protected int maxProgressTime;

    protected boolean canWorkProgress = true;

    protected boolean isActive;
    protected boolean workingEnabled = true;
    protected boolean wasActiveAndNeedsUpdate;
    protected boolean hasNotEnoughEnergy;

    public AbstractWorkableLogic(@Nonnull MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    @Override
    public void update() {
        World world = getMetaTileEntity().getWorld();
        if (world != null && !world.isRemote) {
            if (this.isWorkingEnabled()) {
                if (getMetaTileEntity().getOffsetTimer() % 20 == 0) {
                    this.canWorkProgress = canWorkProgress();
                }

                if (progressTime > 0) {
                    updateRecipeProgress();
                }
                //check everything that would make a recipe never start here.
                if (progressTime == 0 && shouldSearchForWork()) {
                    searchForWork();
                }
            }
            if (wasActiveAndNeedsUpdate) {
                this.wasActiveAndNeedsUpdate = false;
                setActive(false);
            }
        }
    }

    /**
     * @return true if the work can progress, else false
     */
    protected abstract boolean canWorkProgress();

    /**
     * Updates work progress towards completion
     */
    protected void updateRecipeProgress() {
        if (canWorkProgress && drawPerTick(true)) {
            drawPerTick(false);
            //as recipe starts with progress on 1 this has to be > only not => to compensate for it
            if (++progressTime > maxProgressTime) {
                completeWork();
            }
            resetNotEnoughEnergy();
        } else if (isWorkEnergyConsuming()) {
            //only set hasNotEnoughEnergy if this recipe is consuming recipe
            //generators always have enough energy
            this.hasNotEnoughEnergy = true;
            //if current progress value is greater than 2, decrement it by 2
            if (progressTime >= 2) {
                if (ConfigHolder.machines.recipeProgressLowEnergy) {
                    this.progressTime = 1;
                } else {
                    this.progressTime = Math.max(1, progressTime - 2);
                }
            }
        }
    }

    /**
     * Draws consumables every tick which are required for work to progress
     *
     * @param simulate if the consumables should actually be consumed
     * @return true if drawing was successful, otherwise false
     */
    protected abstract boolean drawPerTick(boolean simulate);

    /**
     * Handles what should happen when this workable does not have enough consumables to progress
     */
    protected void resetNotEnoughEnergy() {
        if (isHasNotEnoughEnergy()) {
            this.hasNotEnoughEnergy = false;
        }
    }

    /**
     * @return true if this does not have enough energy, otherwise false
     */
    public boolean isHasNotEnoughEnergy() {
        return hasNotEnoughEnergy;
    }

    /**
     * @return true if the workable's work consumes energy, otherwise false
     */
    protected abstract boolean isWorkEnergyConsuming();

    /**
     * Completes the work which was being run
     * <p>
     * Performs actions done upon work completion
     */
    protected void completeWork() {
        this.progressTime = 0;
        setMaxProgress(0);
        this.hasNotEnoughEnergy = false;
        this.wasActiveAndNeedsUpdate = true;
    }

    /**
     * @return true if this workable should search for new work, otherwise false
     */
    protected abstract boolean shouldSearchForWork();

    /**
     * Looks for work to do, and then performs it
     */
    protected abstract void searchForWork();

    /**
     * Used to force this workable to search for new work
     * Use sparingly
     */
    protected abstract void forceWorkRecheck();

    /**
     * @return true if this workable is allowed to perform work, otherwise false
     */
    @Override
    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    /**
     * @param workingEnabled whether this workable should be allowed to perform work
     */
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
     * @return the amount of ticks progressed towards completing work
     */
    @Override
    public int getProgress() {
        return progressTime;
    }

    /**
     * @return the amount of ticks required to progress in order to complete work
     */
    @Override
    public int getMaxProgress() {
        return maxProgressTime;
    }

    /**
     * Sets the amount of ticks of running time to finish the work
     *
     * @param maxProgress the amount of ticks to set
     */
    public void setMaxProgress(int maxProgress) {
        this.maxProgressTime = maxProgress;
        metaTileEntity.markDirty();
    }

    /**
     * @return the percentage of current progress towards the maximum
     */
    public double getProgressPercent() {
        return getMaxProgress() == 0 ? 0.0 : getProgress() / (getMaxProgress() * 1.0);
    }

    /**
     * @return true if this workable is active
     */
    @Override
    public boolean isActive() {
        return this.isActive;
    }

    /**
     * @param active whether this workable should be active
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

    /**
     * @return true if this workable is actively working with enough energy, otherwise false
     */
    public boolean isWorking() {
        return isActive && !hasNotEnoughEnergy && workingEnabled;
    }

    @Nonnull
    @Override
    public String getName() {
        return "AbstractWorkable";
    }

    @Override
    public final int getNetworkID() {
        return TraitNetworkIds.TRAIT_ID_WORKABLE;
    }

    @Override
    public void receiveCustomData(int dataId, @Nonnull PacketBuffer buf) {
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            getMetaTileEntity().scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.workingEnabled = buf.readBoolean();
            getMetaTileEntity().scheduleRenderUpdate();
        }
    }

    @Override
    public void writeInitialData(@Nonnull PacketBuffer buf) {
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.workingEnabled);
    }

    @Override
    public void receiveInitialData(@Nonnull PacketBuffer buf) {
        this.isActive = buf.readBoolean();
        this.workingEnabled = buf.readBoolean();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("WorkEnabled", workingEnabled);
        compound.setBoolean("CanRecipeProgress", canWorkProgress);
        if (progressTime > 0) {
            compound.setInteger("Progress", progressTime);
            compound.setInteger("MaxProgress", maxProgressTime);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound compound) {
        this.workingEnabled = compound.getBoolean("WorkEnabled");
        this.canWorkProgress = compound.getBoolean("CanRecipeProgress");
        this.progressTime = compound.getInteger("Progress");
        this.isActive = false;
        if (progressTime > 0) {
            this.isActive = true;
            this.maxProgressTime = compound.getInteger("MaxProgress");
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        } else if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return null;
    }
}
