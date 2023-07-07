package gregtech.api.capability.impl;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityActiveTransformer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class LaserBufferWrapper extends MTETrait implements ILaserContainer {

    @Nullable
    private MetaTileEntityActiveTransformer controller;
    private final boolean isOutput;
    private Predicate<EnumFacing> sideInputCondition;
    private Predicate<EnumFacing> sideOutputCondition;

    /**
     * Create a new MTE trait.
     *
     * @param metaTileEntity the MTE to reference, and add the trait to
     */
    public LaserBufferWrapper(@NotNull MetaTileEntity metaTileEntity, @Nullable MetaTileEntityActiveTransformer controller, boolean isOutput) {
        super(metaTileEntity);
        this.controller = controller;
        this.isOutput = isOutput;
    }

    public void setSideInputCondition(Predicate<EnumFacing> sideInputCondition) {
        this.sideInputCondition = sideInputCondition;
    }

    public void setSideOutputCondition(Predicate<EnumFacing> sideOutputCondition) {
        this.sideOutputCondition = sideOutputCondition;
    }

    @Override
    public long acceptEnergy(EnumFacing side, long amount) {
        if (controller == null || controller.getBuffer() == null) {
            return 0;
        } else if (amount > 0 && (side == null || inputsEnergy(side))) {
            return controller.getBuffer().acceptEnergy(side, amount);
        } else if (amount < 0 && (side == null || outputsEnergy(side))) {
            return controller.getBuffer().acceptEnergy(side, amount);
        }
        return 0;
    }

    @Override
    public long changeEnergy(long amount) {
        if (controller == null || controller.getBuffer() == null) {
            return 0;
        } else {
            return controller.getBuffer().changeEnergy(amount);
        }
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        if (controller == null || controller.getBuffer() == null) {
            return false;
        } else {
            return !outputsEnergy(side) && controller.getBuffer().inputsEnergy(side) && (sideInputCondition == null || sideInputCondition.test(side)) && !isOutput;
        }
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        if (controller == null || controller.getBuffer() == null) {
            return false;
        } else {
            return controller.getBuffer().outputsEnergy(side) && (sideOutputCondition == null || sideOutputCondition.test(side)) && isOutput;
        }
    }
    @Override
    public long getEnergyStored() {
        if (controller == null || controller.getBuffer() == null) {
            return 0;
        } else {
            return controller.getBuffer().getEnergyStored();
        }
    }

    @Override
    public long getEnergyCapacity() {
        if (controller == null || controller.getBuffer() == null) {
            return 0;
        } else {
            return controller.getBuffer().getEnergyCapacity();
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "LaserContainer";
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        if (capability == GregtechTileCapabilities.CAPABILITY_LASER) {
            return GregtechTileCapabilities.CAPABILITY_LASER.cast(this);
        }
        return null;
    }

    @Nullable
    public MetaTileEntityActiveTransformer getController() {
        return controller;
    }

    public void setController(@Nullable MetaTileEntityActiveTransformer controller) {
        this.controller = controller;
    }
}
