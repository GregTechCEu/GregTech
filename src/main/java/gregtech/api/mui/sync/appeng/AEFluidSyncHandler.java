package gregtech.api.mui.sync.appeng;

import gregtech.api.util.GTUtility;
import gregtech.api.util.JEIUtil;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEFluidStack;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public IRecipeTransferError receiveRecipe(@NotNull IRecipeLayout recipeLayout, boolean maxTransfer,
                                              boolean simulate) {
        if (simulate) return null;

        List<FluidStack> originalFluidStacks = JEIUtil.getDisplayedInputFluidStacks(recipeLayout.getFluidStacks());
        List<FluidStack> fluidInputs = new ArrayList<>(originalFluidStacks.size());
        originalFluidStacks.forEach(stack -> {
            if (stack != null) {
                fluidInputs.add(stack.copy());
            }
        });
        GTUtility.collapseFluidList(fluidInputs);

        for (int index = 0; index < slots.length; index++) {
            FluidStack stackToSet = index >= fluidInputs.size() ? null : fluidInputs.get(index);
            setConfig(index, stackToSet);
        }

        return null;
    }

    @SideOnly(Side.CLIENT)
    public void setConfig(int index, @Nullable FluidStack stack) {
        setConfig(index, WrappedFluidStack.fromFluidStack(stack));
    }
}
