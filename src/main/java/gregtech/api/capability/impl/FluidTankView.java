package gregtech.api.capability.impl;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.Nullable;

//https://github.com/MCTian-mi/SussyPatches/blob/main/src/main/java/dev/tianmi/sussypatches/api/capability/impl/FluidTankView.java
/// A read-only dummy impl of [FluidTank]
public class FluidTankView extends FluidTank {

    public static FluidTankView of(IFluidHandler handler) {
        var tankProperty = handler.getTankProperties()[0];
        return new FluidTankView(tankProperty.getContents(), tankProperty.getCapacity());
    }

    public static FluidTankView full(IFluidHandler handler) {
        var fluidStack = handler.drain(1, false);
        return new FluidTankView(fluidStack, 1);
    }

    private FluidTankView(@Nullable FluidStack fluidStack, int capacity) {
        super(fluidStack, capacity);
        this.canFill = false;
    }

    @Override
    public FluidTank readFromNBT(NBTTagCompound nbt) {
        return this;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        return nbt;
    }

    @Override
    public void setFluid(@Nullable FluidStack fluid) {}

    @Override
    public void setCapacity(int capacity) {}

    @Override
    public void setTileEntity(TileEntity tile) {}

    @Nullable
    @Override
    public FluidStack drainInternal(FluidStack resource, boolean doDrain) {
        if (doDrain) return null;
        return super.drainInternal(resource, false);
    }

    @Nullable
    @Override
    public FluidStack drainInternal(int maxDrain, boolean doDrain) {
        if (doDrain) return null;
        return super.drainInternal(maxDrain, false);
    }
}
