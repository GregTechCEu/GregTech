package gregtech.common.covers.filter;

import gregtech.api.gui.Widget;
import gregtech.api.util.IDirtyNotifiable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

public abstract class FluidFilter {

    private IDirtyNotifiable dirtyNotifiable;
    boolean showTip;

    public abstract boolean testFluid(FluidStack fluidStack);

    public abstract int getFluidTransferLimit(FluidStack fluidStack);

    public abstract int getMaxOccupiedHeight();

    public abstract void initUI(Consumer<Widget> widgetGroup);

    public abstract void writeToNBT(NBTTagCompound tagCompound);

    public abstract void readFromNBT(NBTTagCompound tagCompound);

    public final void setDirtyNotifiable(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public abstract void configureFilterTanks(int amount);

    public abstract void setMaxConfigurableFluidSize(int maxStackSize);

    public final void markDirty() {
        if (dirtyNotifiable != null) {
            dirtyNotifiable.markAsDirty();
        }
    }
}
