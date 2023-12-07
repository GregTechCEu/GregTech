package gregtech.common.terminal.app.recipechart;

import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.terminal.os.TerminalTheme;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class FluidStackHelper implements IngredientHelper<FluidStack> {

    public static final FluidStackHelper INSTANCE = new FluidStackHelper();

    @Override
    public byte getTypeId() {
        return 2;
    }

    @Override
    public int getAmount(FluidStack fluidStack) {
        return fluidStack.amount;
    }

    @Override
    public void setAmount(FluidStack fluidStack, int amount) {
        fluidStack.amount = amount;
    }

    @Override
    public boolean areEqual(FluidStack t1, FluidStack t2) {
        return t1 != null && t1.isFluidEqual(t2);
    }

    @Override
    public boolean isEmpty(FluidStack fluidStack) {
        return fluidStack.getFluid() == null || fluidStack.amount <= 0;
    }

    @Override
    public String getDisplayName(FluidStack fluidStack) {
        return fluidStack.getLocalizedName();
    }

    @Override
    public Widget createWidget(FluidStack fluidStack) {
        FluidTank tank = new FluidTank(fluidStack, Integer.MAX_VALUE);
        return new TankWidget(tank, 0, 0, 18, 18).setAlwaysShowFull(true).setBackgroundTexture(TerminalTheme.COLOR_B_2)
                .setClient();
    }

    @Override
    public FluidStack deserialize(NBTTagCompound nbt) {
        return FluidStack.loadFluidStackFromNBT(nbt);
    }

    @Override
    public NBTTagCompound serialize(FluidStack fluidStack) {
        return fluidStack.writeToNBT(new NBTTagCompound());
    }
}
