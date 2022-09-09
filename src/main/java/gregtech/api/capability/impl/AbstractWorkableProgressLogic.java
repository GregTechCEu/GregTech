package gregtech.api.capability.impl;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.ConfigHolder;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;

public abstract class AbstractWorkableProgressLogic extends AbstractWorkableLogic {

    protected int progressTime;
    protected int maxProgressTime;

    public AbstractWorkableProgressLogic(@Nonnull MetaTileEntity metaTileEntity) {
        super(metaTileEntity);
    }

    /**
     * Updates work progress towards completion
     */
    protected void updateRecipeProgress() {
        if (this.progressTime <= 0) return;
        if (this.canWorkProgress && drawPerTick(true)) {
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
            if (this.progressTime >= 2) {
                if (ConfigHolder.machines.recipeProgressLowEnergy) {
                    this.progressTime = 1;
                } else {
                    this.progressTime = Math.max(1, this.progressTime - 2);
                }
            }
        }
    }

    /**
     * Completes the work which was being run
     * <p>
     * Performs actions done upon work completion
     */
    protected void completeWork() {
        this.hasNotEnoughEnergy = false;
        this.wasActiveAndNeedsUpdate = true;
        this.progressTime = 0;
        setMaxProgress(0);
    }

    @Override
    protected boolean shouldSearchForWork() {
        // look for new work when nothing is running
        return this.progressTime == 0;
    }

    /**
     * @return the amount of ticks progressed towards completing work
     */
    @Override
    public int getProgress() {
        return this.progressTime;
    }

    /**
     * @return the amount of ticks required to progress in order to complete work
     */
    @Override
    public int getMaxProgress() {
        return this.maxProgressTime;
    }

    /**
     * Sets the amount of ticks of running time to finish the work
     *
     * @param maxProgress the amount of ticks to set
     */
    public void setMaxProgress(int maxProgress) {
        this.maxProgressTime = maxProgress;
        this.metaTileEntity.markDirty();
    }

    /**
     * @return the percentage of current progress towards the maximum
     */
    public double getProgressPercent() {
        return getMaxProgress() == 0 ? 0.0 : getProgress() / (getMaxProgress() * 1.0);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        if (this.progressTime > 0) {
            compound.setInteger("Progress", this.progressTime);
            compound.setInteger("MaxProgress", this.maxProgressTime);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        if (this.progressTime > 0) {
            this.isActive = true;
            this.progressTime = compound.getInteger("Progress");
            this.maxProgressTime = compound.getInteger("MaxProgress");
        }
    }
}
