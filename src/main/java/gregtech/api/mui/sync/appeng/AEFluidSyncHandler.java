package gregtech.api.mui.sync.appeng;

import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEFluidStack;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AEFluidSyncHandler extends AESyncHandler<IAEFluidStack> {

    private final ExportOnlyAEFluidList fluidList;

    public AEFluidSyncHandler(ExportOnlyAEFluidList fluidList, @Nullable Runnable dirtyNotifier) {
        super(fluidList.getInventory(), fluidList.isStocking(), dirtyNotifier);
        this.fluidList = fluidList;
    }

    @Override
    protected @NotNull IConfigurableSlot<IAEFluidStack> @NotNull [] initializeCache() {
        // noinspection unchecked
        IConfigurableSlot<IAEFluidStack>[] cache = new IConfigurableSlot[slots.length];
        for (int index = 0; index < slots.length; index++) {
            cache[index] = new ExportOnlyAEFluidSlot();
        }
        return cache;
    }

    @Override
    protected @NotNull IByteBufAdapter<IAEFluidStack> initializeByteBufAdapter() {
        return IAEStackByteBufAdapter.wrappedFluidStackAdapter;
    }

    @Override
    public boolean isStackValidForSlot(int index, @Nullable IAEFluidStack stack) {
        if (stack == null) return true;
        if (!isStocking) return true;
        return !fluidList.hasStackInConfig(((WrappedFluidStack) stack).getDelegate(), true);
    }

    @SideOnly(Side.CLIENT)
    public void setConfig(int index, @Nullable FluidStack stack) {
        setConfig(index, WrappedFluidStack.fromFluidStack(stack));
    }
}
