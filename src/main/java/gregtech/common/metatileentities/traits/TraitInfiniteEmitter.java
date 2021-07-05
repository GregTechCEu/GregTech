package gregtech.common.metatileentities.traits;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.InfiniteEnergyTileEntityBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.math.BigInteger;


public class TraitInfiniteEmitter extends TraitInfiniteEnergy implements IEnergyContainer {
    private boolean isInfinite;
    private int tier = GTValues.MAX;
    private long voltage = GTValues.V[GTValues.MAX];

    public TraitInfiniteEmitter(InfiniteEnergyTileEntityBase mte) {
        super(mte);
    }

    public boolean isInfinite() {
        return isInfinite;
    }

    public void setInfinite(boolean infinite) {
        this.isInfinite = infinite;
    }

    @Override
    public void update() {
        if (!metaTileEntity.getWorld().isRemote) {
            if (isInfinite) for (EnumFacing facing : EnumFacing.VALUES) send(facing);
            else if (energy.signum() == 1) for (EnumFacing facing : EnumFacing.VALUES)
                if (send(facing) && energy.signum() != 1) break;
        }
    }


    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        voltage = GTValues.V[this.tier = tier];
    }

    @Override
    public String getName() {
        return "infinite_gteu_emitter";
    }

    @Override
    public int getNetworkID() {
        return TraitNetworkIds.TRAIT_ID_ENERGY_CONTAINER;
    }

    protected boolean send(EnumFacing facing) {
        TileEntity te = metaTileEntity.getWorld().getTileEntity(this.metaTileEntity.getPos().offset(facing));
        if (te != null) {
            IEnergyContainer s = te.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, facing.getOpposite());
            if (s != null && s.inputsEnergy(facing.getOpposite())) {
                long stored = getEnergyStored(), voltage = Math.min(this.voltage, s.getInputVoltage());
                if (stored / voltage > 0) {
                    long accepted = s.acceptEnergyFromNetwork(facing.getOpposite(), voltage, Math.min(s.getInputAmperage(), stored / voltage)) * voltage;
                    if (accepted > 0) {
                        if (!isInfinite()) subtract(BigInteger.valueOf(accepted));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        return 0;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return false;
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return true;
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        add(BigInteger.valueOf(differenceAmount));
        return differenceAmount;
    }

    @Override
    public long getInputAmperage() {
        return 64;
    }

    @Override
    public long getInputVoltage() {
        return voltage;
    }

    @Override
    public long getEnergyStored() {
        return isInfinite() || energy.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) >= 0 ? Long.MAX_VALUE : energy.longValueExact();
    }

    @Override
    public long getEnergyCapacity() {
        return getEnergyStored();
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability) {
        return capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER ? (T) this : null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        if (isInfinite) nbt.setBoolean("infinite", true);
        nbt.setByte("tier", (byte) tier);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        setInfinite(nbt.getBoolean("infinite"));
        setTier(nbt.getByte("tier"));
    }
}
