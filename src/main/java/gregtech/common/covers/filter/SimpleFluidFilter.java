package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.PhantomFluidWidget;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SimpleFluidFilter extends FluidFilter {

    private static final int MAX_FLUID_SLOTS = 9;

    protected final FluidStack[] fluidFilterSlots;

    public SimpleFluidFilter() {
        this.fluidFilterSlots = new FluidStack[MAX_FLUID_SLOTS];
    }

    @Nullable
    public FluidStack getFluidInSlot(int slotIndex) {
        return fluidFilterSlots[slotIndex];
    }

    public void setFluidInSlot(int slotIndex, FluidStack fluidStack) {
        this.fluidFilterSlots[slotIndex] = fluidStack == null ? null : fluidStack.copy();
    }

    @Override
    public boolean testFluid(FluidStack fluidStack) {
        return checkInputFluid(fluidFilterSlots, fluidStack);
    }

    @Override
    public int getMaxOccupiedHeight() {
        return 36;
    }

    @Override
    public void initUI(Consumer<Widget> widgetGroup) {
        for (int i = 0; i < 9; ++i) {
            int index = i;
            widgetGroup.accept((new PhantomFluidWidget(10 + 18 * (i % 3), 18 * (i / 3), 18, 18,
                    () -> getFluidInSlot(index),
                    (newFluid) -> setFluidInSlot(index, newFluid)))
                    .setBackgroundTexture(GuiTextures.SLOT).showTip(true));
        }
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList filterSlots = new NBTTagList();
        for (int i = 0; i < this.fluidFilterSlots.length; ++i) {
            FluidStack fluidStack = this.fluidFilterSlots[i];
            if (fluidStack != null) {
                NBTTagCompound stackTag = new NBTTagCompound();
                fluidStack.writeToNBT(stackTag);
                stackTag.setInteger("Slot", i);
                filterSlots.appendTag(stackTag);
            }
        }
        tagCompound.setTag("FluidFilter", filterSlots);
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList filterSlots = tagCompound.getTagList("FluidFilter", 10);
        for (NBTBase nbtBase : filterSlots) {
            NBTTagCompound stackTag = (NBTTagCompound) nbtBase;
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(stackTag);
            this.fluidFilterSlots[stackTag.getInteger("Slot")] = fluidStack;
        }
    }

    public static boolean checkInputFluid(FluidStack[] fluidFilterSlots, FluidStack fluidStack) {
        for (FluidStack filterStack : fluidFilterSlots) {
            if (filterStack != null && filterStack.isFluidEqual(fluidStack)) {
                return true;
            }
        }
        return false;
    }
}
