package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.PhantomFluidWidget;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class SimpleFluidFilter extends FluidFilter {

    private static final int MAX_FLUID_SLOTS = 9;

    protected final FluidTank[] fluidFilterTanks = new FluidTank[MAX_FLUID_SLOTS];

    public SimpleFluidFilter() {
        for (int i = 0; i < MAX_FLUID_SLOTS; ++i) {
            fluidFilterTanks[i] = new FluidTank(1000) {

                @Override
                public void setFluid(@Nullable FluidStack fluid) {
                    super.setFluid(fluid);
                    SimpleFluidFilter.this.markDirty();
                }
            };
        }
    }

    @Override
    public void configureFilterTanks(int amount) {
        for (FluidTank fluidTank : fluidFilterTanks) {
            if (fluidTank.getFluid() != null)
                fluidTank.getFluid().amount = amount;
        }
        this.markDirty();
    }

    @Override
    public void setMaxConfigurableFluidSize(int maxSize) {
        for (FluidTank fluidTank : fluidFilterTanks) {
            fluidTank.setCapacity(maxSize);
        }
    }

    @Override
    public boolean testFluid(FluidStack fluidStack) {
        return checkInputFluid(fluidFilterTanks, fluidStack);
    }

    @Override
    public int getMaxOccupiedHeight() {
        return 36;
    }

    @Override
    public void initUI(Consumer<Widget> widgetGroup) {
        for (int i = 0; i < 9; ++i) {
            widgetGroup.accept((new PhantomFluidWidget(10 + 18 * (i % 3), 18 * (i / 3), 18, 18,
                    this.fluidFilterTanks[i]))
                            .setBackgroundTexture(GuiTextures.SLOT).showTipSupplier(this::shouldShowTip));
        }
    }

    private boolean shouldShowTip() {
        return showTip;
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList filterSlots = new NBTTagList();
        for (int i = 0; i < this.fluidFilterTanks.length; ++i) {
            FluidTank fluidTank = this.fluidFilterTanks[i];
            if (fluidTank.getFluid() != null) {
                NBTTagCompound stackTag = new NBTTagCompound();
                fluidTank.getFluid().writeToNBT(stackTag);
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
            this.fluidFilterTanks[stackTag.getInteger("Slot")].setFluid(fluidStack);
        }
    }

    public static boolean checkInputFluid(FluidTank[] fluidFilterTanks, FluidStack fluidStack) {
        for (FluidTank fluidTank : fluidFilterTanks) {
            if (fluidTank.getFluid() != null && fluidTank.getFluid().isFluidEqual(fluidStack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getFluidTransferLimit(FluidStack fluidStack) {
        int limit = 0;
        for (FluidTank fluidTank : fluidFilterTanks) {
            if (fluidTank.getFluid() != null && fluidTank.getFluid().isFluidEqual(fluidStack)) {
                limit = fluidTank.getFluid().amount;
                break;
            }
        }
        return limit;
    }
}
