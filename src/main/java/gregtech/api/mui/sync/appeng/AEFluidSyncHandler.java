package gregtech.api.mui.sync.appeng;

import gregtech.api.capability.impl.GhostCircuitItemStackHandler;
import gregtech.api.mui.GTByteBufAdapters;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.util.GTUtility;
import gregtech.api.util.JEIUtil;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.IConfigurableSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.storage.data.IAEFluidStack;
import com.cleanroommc.modularui.utils.serialization.IByteBufAdapter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class AEFluidSyncHandler extends AESyncHandler<IAEFluidStack> {

    protected final ExportOnlyAEFluidList fluidList;

    public AEFluidSyncHandler(ExportOnlyAEFluidList fluidList, @Nullable Runnable dirtyNotifier,
                              @NotNull IntConsumer circuitChangeConsumer) {
        super(fluidList.getInventory(), fluidList.isStocking(), dirtyNotifier, circuitChangeConsumer);
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
        return GTByteBufAdapters.WRAPPED_FLUID_STACK;
    }

    @Override
    public boolean isStackValidForSlot(int index, @Nullable IAEFluidStack stack) {
        if (stack == null) return true;
        if (!isStocking) return true;
        return !fluidList.hasStackInConfig(((WrappedFluidStack) stack).getDefinition(), true);
    }

    @Override
    public IRecipeTransferError receiveRecipe(@NotNull IRecipeLayout recipeLayout, boolean maxTransfer,
                                              boolean simulate) {
        if (simulate) return null;

        Int2ObjectMap<FluidStack> originalFluidInputs = JEIUtil
                .getDisplayedInputFluidStacks(recipeLayout.getFluidStacks(), false, true);
        List<FluidStack> fluidInputs = new ArrayList<>(originalFluidInputs.values());
        GTUtility.collapseFluidList(fluidInputs);

        int lastSlotIndex;
        for (lastSlotIndex = 0; lastSlotIndex < fluidInputs.size(); lastSlotIndex++) {
            FluidStack newConfig = fluidInputs.get(lastSlotIndex);
            setConfig(lastSlotIndex, newConfig);
        }
        clearConfigFrom(lastSlotIndex);

        Int2ObjectMap<ItemStack> itemInputs = JEIUtil.getDisplayedInputItemStacks(recipeLayout.getItemStacks(), false,
                false);
        int circuitValue = GhostCircuitItemStackHandler.NO_CONFIG;
        for (ItemStack inputStack : itemInputs.values()) {
            if (IntCircuitIngredient.isIntegratedCircuit(inputStack)) {
                circuitValue = IntCircuitIngredient.getCircuitConfiguration(inputStack);
                break;
            }
        }
        ghostCircuitConfig.accept(circuitValue);

        return null;
    }

    @SideOnly(Side.CLIENT)
    public void setConfig(int index, @Nullable FluidStack stack) {
        setConfig(index, WrappedFluidStack.fromFluidStack(stack));
    }
}
