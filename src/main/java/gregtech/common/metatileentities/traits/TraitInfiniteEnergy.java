package gregtech.common.metatileentities.traits;

import gregtech.api.metatileentity.InfiniteEnergyTileEntityBase;
import gregtech.api.metatileentity.MTETrait;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

import java.math.BigInteger;

public abstract class TraitInfiniteEnergy extends MTETrait {
    protected final InfiniteEnergyTileEntityBase<?> mte;
    protected BigInteger energy = BigInteger.ZERO;

    public TraitInfiniteEnergy(InfiniteEnergyTileEntityBase<?> mte) {
        super(mte);
        this.mte = mte;
    }

    public BigInteger getEnergy() {
        return energy;
    }

    public void setEnergy(BigInteger bigInteger) {
        energy = bigInteger.signum() == 1 ? bigInteger : BigInteger.ZERO;
    }

    protected void add(BigInteger bInt) {
        switch (bInt.signum()) {
            case 1:
                energy = energy.add(bInt);
                break;
            case -1:
                subtract(bInt.negate());
        }
    }

    protected void subtract(BigInteger bInt) {
        if (energy.compareTo(bInt) > 0) {
            energy = energy.subtract(bInt);
        } else energy = BigInteger.ZERO;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (energy.signum() > 0) nbt.setByteArray("energy", energy.toByteArray());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("energy", NBT.TAG_BYTE_ARRAY)) {
            byte[] bArr = nbt.getByteArray("energy");
            energy = bArr.length > 0 ? new BigInteger(bArr) : BigInteger.ZERO;
        } else energy = BigInteger.ZERO;
    }
}
