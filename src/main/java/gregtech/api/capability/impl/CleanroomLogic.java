package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.IMaintenance;
import gregtech.common.ConfigHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;

public class CleanroomLogic {

    public static final int MAX_PROGRESS = 100; // five seconds
    public static final int BASE_CLEAN_AMOUNT = 5;

    private int progressTime = 0;

    private final int minEnergyTier;

    private final MetaTileEntity metaTileEntity;
    private final boolean hasMaintenance;

    private boolean isActive;
    private boolean isWorkingEnabled = true;
    private boolean wasActiveAndNeedsUpdate;

    private boolean hasNotEnoughEnergy;

    public CleanroomLogic(MetaTileEntity metaTileEntity, int minEnergyTier) {
        this.metaTileEntity = metaTileEntity;
        this.minEnergyTier = minEnergyTier;
        this.hasMaintenance = ConfigHolder.machines.enableMaintenance && ((IMaintenance) metaTileEntity).hasMaintenanceMechanics();
    }

    /**
     * Performs the actual cleaning
     * Call this method every tick in update
     */
    public void updateLogic() {
        if (metaTileEntity.getWorld().isRemote) return;

        if (hasMaintenance && ((IMaintenance) metaTileEntity).getNumMaintenanceProblems() > 5) {
            return;
        }

        // cleanrooms which cannot work do nothing
        if (!this.isWorkingEnabled) return;

        // drain the energy
        if (consumeEnergy(true)) {
            consumeEnergy(false);
        } else {
            if (progressTime >= 2) {
                if (ConfigHolder.machines.recipeProgressLowEnergy) this.progressTime = 1;
                else this.progressTime = Math.max(1, progressTime - 2);

                hasNotEnoughEnergy = true;
            }
            return;
        }

        if (!this.isActive) setActive(true);

        // increase progress
        progressTime++;
        if (progressTime % getMaxProgress() != 0) return;
        progressTime = 0;

        int amountToClean;
        if (hasEnoughPower()) amountToClean = BASE_CLEAN_AMOUNT * (getTierDifference() + 1);
        else amountToClean = -BASE_CLEAN_AMOUNT;

        // each maintenance problem lowers gain by 1 and increases loss by 1
        if (hasMaintenance) amountToClean -= ((IMaintenance) metaTileEntity).getNumMaintenanceProblems();
        ((ICleanroomProvider) metaTileEntity).adjustCleanAmount(amountToClean);
    }

    protected boolean consumeEnergy(boolean simulate) {
        return ((ICleanroomProvider) metaTileEntity).drainEnergy(simulate);
    }

    /**
     * @return true if the cleanroom is able to drain energy, else false
     */
    protected boolean hasEnoughPower() {
        if (this.hasNotEnoughEnergy && ((ICleanroomProvider) metaTileEntity).getEnergyInputPerSecond() > 19L * GTValues.VA[((ICleanroomProvider) metaTileEntity).getEnergyTier()]) {
            this.hasNotEnoughEnergy = false;
        }

        return true;
    }

    /**
     * @return true if the cleanroom is active
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     * @param active the new state of the cleanroom's activity: true to change to active, else false
     */
    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            this.metaTileEntity.markDirty();
            if (metaTileEntity.getWorld() != null && !metaTileEntity.getWorld().isRemote) {
                this.metaTileEntity.writeCustomData(GregtechDataCodes.IS_WORKING, buf -> buf.writeBoolean(active));
            }
        }
    }

    /**
     * @param workingEnabled the new state of the cleanroom's ability to work: true to change to enabled, else false
     */
    public void setWorkingEnabled(boolean workingEnabled) {
        this.isWorkingEnabled = workingEnabled;
        metaTileEntity.writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
        metaTileEntity.markDirty();
    }

    /**
     * @return whether working is enabled for the logic
     */
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    /**
     * @return whether the cleanroom is currently working
     */
    public boolean isWorking() {
        return isActive && !hasNotEnoughEnergy && isWorkingEnabled;
    }

    /**
     * @return the current progress towards completing one cycle of the cleanroom
     */
    public int getProgressTime() {
        return this.progressTime;
    }

    public int getMaxProgress() {
        return MAX_PROGRESS;
    }

    public int getProgressPercent() {
        return getProgressTime() / getMaxProgress() * 100;
    }

    protected int getTierDifference() {
        return ((ICleanroomProvider) metaTileEntity).getEnergyTier() - minEnergyTier;
    }

    /**
     * writes all needed values to NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeToNBT(NBTTagCompound)} method
     */
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound data) {
        data.setBoolean("isActive", this.isActive);
        data.setBoolean("isWorkingEnabled", this.isWorkingEnabled);
        data.setBoolean("wasActiveAndNeedsUpdate", this.wasActiveAndNeedsUpdate);
        data.setInteger("progressTime", progressTime);
        return data;
    }

    /**
     * reads all needed values from NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#readFromNBT(NBTTagCompound)} method
     */
    public void readFromNBT(@Nonnull NBTTagCompound data) {
        this.isActive = data.getBoolean("isActive");
        this.isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        this.wasActiveAndNeedsUpdate = data.getBoolean("wasActiveAndNeedsUpdate");
        this.progressTime = data.getInteger("progressTime");
    }

    /**
     * writes all needed values to InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeInitialSyncData(PacketBuffer)} method
     */
    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
        buf.writeBoolean(this.wasActiveAndNeedsUpdate);
        buf.writeInt(this.progressTime);
    }

    /**
     * reads all needed values from InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#receiveInitialSyncData(PacketBuffer)} method
     */
    public void receiveInitialSyncData(@Nonnull PacketBuffer buf) {
        setActive(buf.readBoolean());
        setWorkingEnabled(buf.readBoolean());
        setWasActiveAndNeedsUpdate(buf.readBoolean());
        this.progressTime = buf.readInt();
    }

    /**
     * reads all needed values from CustomData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#receiveCustomData(int, PacketBuffer)} method
     */
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.IS_WORKING) {
            setActive(buf.readBoolean());
            metaTileEntity.scheduleRenderUpdate();
        }
    }

    /**
     * @return whether the cleanroom was active and needs an update
     */
    public boolean wasActiveAndNeedsUpdate() {
        return this.wasActiveAndNeedsUpdate;
    }

    /**
     * set whether the cleanroom was active and needs an update
     *
     * @param wasActiveAndNeedsUpdate the state to set
     */
    public void setWasActiveAndNeedsUpdate(boolean wasActiveAndNeedsUpdate) {
        this.wasActiveAndNeedsUpdate = wasActiveAndNeedsUpdate;
    }
}
