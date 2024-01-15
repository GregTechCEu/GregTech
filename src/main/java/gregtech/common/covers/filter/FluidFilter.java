package gregtech.common.covers.filter;

import gregtech.api.gui.Widget;
import gregtech.api.util.IDirtyNotifiable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Consumer;

public abstract class FluidFilter implements Filter<FluidStack> {

    private IDirtyNotifiable dirtyNotifiable;
    boolean showTip;

    private OnMatch<FluidStack> onMatch = null;

    public abstract void match(FluidStack toMatch);

    public abstract boolean test(FluidStack fluidStack);

    @Override
    public final void setOnMatched(OnMatch<FluidStack> onMatch) {
        this.onMatch = onMatch;
    }

    protected final void onMatch(boolean matched, FluidStack stack, int index) {
        if (this.onMatch != null) this.onMatch.onMatch(matched, stack, index);
    }

    public abstract int getFluidTransferLimit(FluidStack fluidStack);

    @Deprecated
    public abstract void initUI(Consumer<Widget> widgetGroup);

    public abstract ItemStack getContainerStack();

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
