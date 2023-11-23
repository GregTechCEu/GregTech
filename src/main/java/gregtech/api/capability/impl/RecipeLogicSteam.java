package gregtech.api.capability.impl;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IVentable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;

public class RecipeLogicSteam extends AbstractRecipeLogic implements IVentable {

    private final IFluidTank steamFluidTank;
    private final boolean isHighPressure;
    private final double conversionRate; //energy units per millibucket

    private boolean needsVenting;
    private boolean ventingStuck;
    private EnumFacing ventingSide;

    public RecipeLogicSteam(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, boolean isHighPressure, IFluidTank steamFluidTank, double conversionRate) {
        super(tileEntity, recipeMap);
        this.steamFluidTank = steamFluidTank;
        this.conversionRate = conversionRate;
        this.isHighPressure = isHighPressure;
    }

    @Override
    public boolean isVentingStuck() {
        return needsVenting && ventingStuck;
    }

    @Override
    public boolean isNeedsVenting() {
        return needsVenting;
    }

    @Override
    public void onFrontFacingSet(EnumFacing newFrontFacing) {
        if (ventingSide == null) {
            setVentingSide(newFrontFacing.getOpposite());
        }
    }

    public EnumFacing getVentingSide() {
        return ventingSide == null ? EnumFacing.SOUTH : ventingSide;
    }

    public void setVentingStuck(boolean ventingStuck) {
        this.ventingStuck = ventingStuck;
        if (!metaTileEntity.getWorld().isRemote) {
            metaTileEntity.markDirty();
            writeCustomData(GregtechDataCodes.VENTING_STUCK, buf -> buf.writeBoolean(ventingStuck));
        }
    }

    @Override
    public void setNeedsVenting(boolean needsVenting) {
        this.needsVenting = needsVenting;
        if (!needsVenting && ventingStuck)
            setVentingStuck(false);
        if (!metaTileEntity.getWorld().isRemote) {
            metaTileEntity.markDirty();
            writeCustomData(GregtechDataCodes.NEEDS_VENTING, buf -> buf.writeBoolean(needsVenting));
        }
    }

    public void setVentingSide(EnumFacing ventingSide) {
        this.ventingSide = ventingSide;
        if (!metaTileEntity.getWorld().isRemote) {
            metaTileEntity.markDirty();
            writeCustomData(GregtechDataCodes.VENTING_SIDE, buf -> buf.writeByte(ventingSide.getIndex()));
        }
    }

    @Override
    public void receiveCustomData(int dataId, @Nonnull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.NEEDS_VENTING) {
            this.needsVenting = buf.readBoolean();
        } else if (dataId == GregtechDataCodes.VENTING_SIDE) {
            this.ventingSide = EnumFacing.VALUES[buf.readByte()];
            getMetaTileEntity().scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.VENTING_STUCK) {
            this.ventingStuck = buf.readBoolean();
        }
    }

    @Override
    public void writeInitialSyncData(@Nonnull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(getVentingSide().getIndex());
        buf.writeBoolean(needsVenting);
        buf.writeBoolean(ventingStuck);
    }

    @Override
    public void receiveInitialSyncData(@Nonnull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.ventingSide = EnumFacing.VALUES[buf.readByte()];
        this.needsVenting = buf.readBoolean();
        this.ventingStuck = buf.readBoolean();
    }

    @Override
    public void tryDoVenting() {
        if (GTUtility.tryVenting(metaTileEntity.getWorld(), metaTileEntity.getPos(), getVentingSide(),
                this.isHighPressure ? 12 : 6, true,
                ConfigHolder.machines.machineSounds && !this.metaTileEntity.isMuffled())) {
            setNeedsVenting(false);
        } else {
            setVentingStuck(true);
        }
    }

    @Override
    public void update() {
        if (getMetaTileEntity().getWorld().isRemote)
            return;
        if (this.needsVenting && metaTileEntity.getOffsetTimer() % 10 == 0) {
            tryDoVenting();
        }
        super.update();
    }

    @Override
    public boolean checkRecipe(@Nonnull Recipe recipe) {
        return super.checkRecipe(recipe) && !this.needsVenting;
    }

    @Override
    protected void completeRecipe() {
        super.completeRecipe();
        setNeedsVenting(true);
        tryDoVenting();
    }

    @Nonnull
    @Override
    protected int[] calculateOverclock(@Nonnull Recipe recipe) {

        //EUt, Duration
        int[] result = new int[2];

        result[0] = isHighPressure ? recipe.getEUt() * 2 : recipe.getEUt();
        result[1] = isHighPressure ? recipe.getDuration() : recipe.getDuration() * 2;

        return result;
    }

    @Override
    protected long getEnergyInputPerSecond() {
        return 0;
    }

    @Override
    protected long getEnergyStored() {
        return (long) Math.ceil(steamFluidTank.getFluidAmount() * conversionRate);
    }

    @Override
    protected long getEnergyCapacity() {
        return (long) Math.floor(steamFluidTank.getCapacity() * conversionRate);
    }

    @Override
    protected boolean drawEnergy(int recipeEUt, boolean simulate) {
        int resultDraw = (int) Math.ceil(recipeEUt / conversionRate);
        return resultDraw >= 0 && steamFluidTank.getFluidAmount() >= resultDraw &&
                steamFluidTank.drain(resultDraw, !simulate) != null;
    }

    @Override
    protected long getMaxVoltage() {
        return GTValues.V[GTValues.LV];
    }

    @Nonnull
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound compound = super.serializeNBT();
        compound.setInteger("VentingSide", getVentingSide().getIndex());
        compound.setBoolean("NeedsVenting", needsVenting);
        compound.setBoolean("VentingStuck", ventingStuck);
        return compound;
    }

    @Override
    public void deserializeNBT(@Nonnull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        this.ventingSide = EnumFacing.VALUES[compound.getInteger("VentingSide")];
        this.needsVenting = compound.getBoolean("NeedsVenting");
        this.ventingStuck = compound.getBoolean("VentingStuck");
    }
}
