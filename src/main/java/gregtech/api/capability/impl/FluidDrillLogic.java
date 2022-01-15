package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityFluidDrill;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FluidDrillLogic {

    private static final int MAX_PROGRESS = 20;
    private int currentProgress = 0;

    private final MetaTileEntityFluidDrill metaTileEntity;

    private boolean isActive;
    private boolean isWorkingEnabled = true;
    private boolean wasActiveAndNeedsUpdate;
    private boolean isDone = false;
    protected boolean isInventoryFull;

    private Fluid veinFluid;

    private int voltageTier;
    private float coefficient;

    public FluidDrillLogic(MetaTileEntityFluidDrill metaTileEntity) {
        this.metaTileEntity = metaTileEntity;
        this.veinFluid = null;
    }

    /**
     * Performs the actual drilling
     * Call this method every tick in update
     */
    public void performDrilling() {
        if (metaTileEntity.getWorld().isRemote) return;

        // if we have no fluid, try to get a new one
        if (veinFluid == null)
            if (!acquireNewFluid())
                return; // stop if we still have no fluid


        // Inactive drills do nothing
        if (!this.isWorkingEnabled)
            return;

        // check if drilling is possible
        if (!checkCanMine())
            return;

        // if the inventory is not full, drain energy etc. from the drill
        // the storages have already been checked earlier
        if (!isInventoryFull) {
            // actually drain the energy
            metaTileEntity.drainEnergy(false);

            // since energy is being consumed the rig is now active
            if (!this.isActive)
                setActive(true);
        } else {
            // the rig cannot drain, therefore it is inactive
            if (this.isActive)
                setActive(false);
            return;
        }

        currentProgress++;
        if (currentProgress % MAX_PROGRESS != 0)
            return;
        currentProgress = 0;

        int rate = getFluidRate();

        if (metaTileEntity.fillTanks(new FluidStack(veinFluid, rate), true)) {
            metaTileEntity.fillTanks(new FluidStack(veinFluid, rate), false);
            depleteVein();
        } else {
            isInventoryFull = true;
            setActive(false);
            setWasActiveAndNeedsUpdate(true);
        }
    }

    private boolean acquireNewFluid() {
        this.veinFluid = BedrockFluidVeinHandler.getFluid(metaTileEntity.getWorld(), getChunkX(), getChunkZ());
        return this.veinFluid != null;
    }

    protected void depleteVein() {
        // adjust depletion for rig multiplier
        if (GTValues.RNG.nextInt(metaTileEntity.getDepletionChance()) == 0)
            BedrockFluidVeinHandler.depleteVein(coefficient, metaTileEntity.getWorld(), getChunkX(), getChunkZ());
    }

    private int getFluidRate() {
        int rate;
        if (BedrockFluidVeinHandler.isChunkDepleted(metaTileEntity.getWorld(), getChunkX(), getChunkZ()))
            rate = BedrockFluidVeinHandler.getDepletedFluidRate(metaTileEntity.getWorld(), getChunkX(), getChunkZ());
        else
            rate = (int) Math.floor(BedrockFluidVeinHandler.getFluidRateInChunk(metaTileEntity.getWorld(), getChunkX(), getChunkZ()) * coefficient);

        return rate * metaTileEntity.getRigMultiplier();
    }

    /**
     *
     * @return true if the rig is able to mine, else false
     */
    protected boolean checkCanMine() {
        if (!metaTileEntity.drainEnergy(true)) {
            if (isActive()) {
                setActive(false);
                setWasActiveAndNeedsUpdate(true);
            }
            return false;
        }

        if (metaTileEntity.fillTanks(new FluidStack(veinFluid, getFluidRate()), true)) {
            this.isInventoryFull = false;
            return true;
        }
        this.isInventoryFull = true;
        if (isActive()) {
            setActive(false);
            setWasActiveAndNeedsUpdate(true);
        }
        return false;
    }

    public void setVoltageTier(int tier) {
        this.voltageTier = tier;
        setCoefficient();
    }

    public void setCoefficient() {
        this.coefficient = 0.5F + 0.25F * (Math.max(metaTileEntity.getTier(), voltageTier) - metaTileEntity.getTier());
    }

    private int getChunkX() {
        return metaTileEntity.getPos().getX() / 16;
    }

    private int getChunkZ() {
        return metaTileEntity.getPos().getZ() / 16;
    }

    /**
     *
     * @return true if the rig is active
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     *
     * @param active the new state of the rig's activity: true to change to active, else false
     */
    public void setActive(boolean active) {
        this.isActive = active;
        this.metaTileEntity.markDirty();
        if (metaTileEntity.getWorld() != null && !metaTileEntity.getWorld().isRemote) {
            this.metaTileEntity.writeCustomData(GregtechDataCodes.IS_WORKING, buf -> buf.writeBoolean(active));
        }
    }

    /**
     *
     * @param workingEnabled the new state of the rig's ability to work: true to change to enabled, else false
     */
    public void setWorkingEnabled(boolean workingEnabled) {
        this.isWorkingEnabled = workingEnabled;
        metaTileEntity.markDirty();
    }

    /**
     *
     * @return whether working is enabled for the logic
     */
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    /**
     *
     * @return whether the rig is currently working
     */
    public boolean isWorking() {
        return isActive && isWorkingEnabled;
    }

    /**
     *
     * @return the current progress towards producing fluid of the rig
     */
    public int getCurrentProgress() {
        return this.currentProgress;
    }

    /**
     *
     * @return whether the inventory is full
     */
    public boolean isInventoryFull() {
        return this.isInventoryFull;
    }

    /**
     * writes all needed values to NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeToNBT(NBTTagCompound)} method
     */
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound data) {
        data.setBoolean("isActive", this.isActive);
        data.setBoolean("isWorkingEnabled", this.isWorkingEnabled);
        data.setBoolean("wasActiveAndNeedsUpdate", this.wasActiveAndNeedsUpdate);
        data.setBoolean("isDone", isDone);
        data.setInteger("currentProgress",currentProgress);
        data.setBoolean("isInventoryFull", isInventoryFull);
        return data;
    }

    /**
     * reads all needed values from NBT
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#readFromNBT(NBTTagCompound)} method
     */
    public void readFromNBT(@Nonnull NBTTagCompound data) {
        setActive(data.getBoolean("isActive"));
        setWorkingEnabled(data.getBoolean("isWorkingEnabled"));
        setWasActiveAndNeedsUpdate(data.getBoolean("wasActiveAndNeedsUpdate"));
        this.isDone = data.getBoolean("isDone");
        this.currentProgress = data.getInteger("currentProgress");
        this.isInventoryFull = data.getBoolean("isInventoryFull");
    }

    /**
     * writes all needed values to InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#writeInitialSyncData(PacketBuffer)} method
     */
    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
        buf.writeBoolean(this.wasActiveAndNeedsUpdate);
        buf.writeInt(this.currentProgress);
        buf.writeBoolean(this.isInventoryFull);
    }

    /**
     * reads all needed values from InitialSyncData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#receiveInitialSyncData(PacketBuffer)} method
     */
    public void receiveInitialSyncData(@Nonnull PacketBuffer buf) {
        setActive(buf.readBoolean());
        setWorkingEnabled(buf.readBoolean());
        setWasActiveAndNeedsUpdate(buf.readBoolean());
        this.currentProgress = buf.readInt();
        this.isInventoryFull = buf.readBoolean();
    }

    /**
     * reads all needed values from CustomData
     * This MUST be called and returned in the MetaTileEntity's {@link MetaTileEntity#receiveCustomData(int, PacketBuffer)} method
     */
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == GregtechDataCodes.IS_WORKING) {
            setActive(buf.readBoolean());
            metaTileEntity.getHolder().scheduleChunkForRenderUpdate();
        }
    }

    /**
     *
     * @return whether the rig was active and needs an update
     */
    public boolean wasActiveAndNeedsUpdate() {
        return this.wasActiveAndNeedsUpdate;
    }

    /**
     * set whether the rig was active and needs an update
     *
     * @param wasActiveAndNeedsUpdate the state to set
     */
    public void setWasActiveAndNeedsUpdate(boolean wasActiveAndNeedsUpdate) {
        this.wasActiveAndNeedsUpdate = wasActiveAndNeedsUpdate;
    }
}
