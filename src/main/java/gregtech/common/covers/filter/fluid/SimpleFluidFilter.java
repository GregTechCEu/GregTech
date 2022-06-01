package gregtech.common.covers.filter.fluid;

import com.cleanroommc.modularui.api.widget.Widget;
import com.cleanroommc.modularui.common.widget.FluidSlotWidget;
import com.cleanroommc.modularui.common.widget.MultiChildWidget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.function.Consumer;

public class SimpleFluidFilter extends FluidFilter {

    private final FluidTank[] tanks = new FluidTank[9];

    public SimpleFluidFilter() {
        for (int i = 0; i < 9; i++) {
            tanks[i] = new FluidTank(Integer.MAX_VALUE);
        }
    }

    @Override
    public boolean matches(FluidStack fluidStack, boolean ignoreInverted) {
        if (fluidStack == null) {
            return true;
        }
        for (FluidTank tank : tanks) {
            if (fluidStack.isFluidEqual(tank.getFluid())) {
                return ignoreInverted || !isInverted();
            }
        }
        return !ignoreInverted && isInverted();
    }

    @Override
    public int getTransferLimit(Object object, int globalTransferLimit) {
        if (object instanceof FluidStack) {
            for (FluidTank fluidTank : tanks) {
                if (fluidTank.getFluid() != null && fluidTank.getFluid().isFluidEqual((FluidStack) object)) {
                    return fluidTank.getFluid().amount;
                }
            }
        }
        return 0;
    }

    public void configureFilterTanks(int amount) {
        for (FluidTank fluidTank : tanks) {
            if (fluidTank.getFluid() != null)
                fluidTank.getFluid().amount = amount;
        }
        this.markDirty();
    }

    @Override
    public Widget createFilterUI(EntityPlayer player, Consumer<Widget> controlsAmountHandler) {
        MultiChildWidget widget = new MultiChildWidget()
                .addChild(createBlacklistButton(player));
        for (int i = 0; i < 9; i++) {
            widget.addChild(FluidSlotWidget.phantom(tanks[i], true)
                    .setTicker(controlsAmountHandler)
                    .setPos(i % 3 * 18, i / 3 * 18));
        }
        return widget.setSize(140, 54);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (FluidTank tank : tanks) {
            list.appendTag(tank.writeToNBT(new NBTTagCompound()));
        }
        nbt.setTag("FilterSlots", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("FilterSlots")) {
            NBTTagList list = nbt.getTagList("FilterSlots", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                tanks[i].readFromNBT(list.getCompoundTagAt(i));
            }
        } else if (nbt.hasKey("FluidFilter")) {
            // legacy
            NBTTagList list = nbt.getTagList("FilterSlots", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < list.tagCount(); i++) {
                tanks[i].setFluid(FluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(i)));
            }
        }
    }
}
