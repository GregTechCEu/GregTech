package gregtech.common.covers.filter.fluid;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.widgets.FluidSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

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
    public boolean matches(FluidStack fluidStack, boolean ignoreInverted) {
        boolean b = checkInputFluid(fluidFilterTanks, fluidStack);
        return ignoreInverted ? b : b != isInverted();
    }

    @Override
    public @NotNull IWidget createFilterUI(ModularPanel mainPanel, GuiSyncManager syncManager) {
        return SlotGroupWidget.builder()
                .matrix("FFF", "FFF", "FFF")
                .key('F', i -> {
                    FluidSlotSyncHandler syncHandler = new FluidSlotSyncHandler(this.fluidFilterTanks[i]);
                    return new FluidSlot().syncHandler(syncHandler)
                            .onUpdateListener(fluidSlot -> {
                                boolean showTip = shouldShowTip();
                                if (showTip != syncHandler.controlsAmount()) {
                                    syncHandler.controlsAmount(showTip);
                                }
                            });
                })
                .build();
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
