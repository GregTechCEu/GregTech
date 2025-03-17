package gregtech.api.recipes.logic.workable;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class OutputBufferTrait extends MTETrait {

    protected @NotNull Deque<ItemStack> bufferedItemOutputs;
    protected boolean awaitingItemOutputSpace;
    protected @NotNull Deque<FluidStack> bufferedFluidOutputs;
    protected boolean awaitingFluidOutputSpace;

    public <T extends MetaTileEntity & IBufferingMTE> OutputBufferTrait(@NotNull T metaTileEntity) {
        super(metaTileEntity);
        this.bufferedItemOutputs = new ArrayDeque<>();
        this.bufferedFluidOutputs = new ArrayDeque<>();
    }

    protected IBufferingMTE getBuffering() {
        return (IBufferingMTE) getMetaTileEntity();
    }

    public @NotNull Deque<ItemStack> getBufferedItemOutputs() {
        return bufferedItemOutputs;
    }

    public void bufferItems(List<ItemStack> items) {
        bufferedItemOutputs.addAll(items);
    }

    public @NotNull Deque<FluidStack> getBufferedFluidOutputs() {
        return bufferedFluidOutputs;
    }

    public void bufferFluids(List<FluidStack> fluids) {
        bufferedFluidOutputs.addAll(fluids);
    }

    public void setAwaitingItemOutputSpace(boolean awaitingItemOutputSpace) {
        this.awaitingItemOutputSpace = awaitingItemOutputSpace;
    }

    public boolean awaitingItemOutputSpace() {
        return awaitingItemOutputSpace;
    }

    public void setAwaitingFluidOutputSpace(boolean awaitingFluidOutputSpace) {
        this.awaitingFluidOutputSpace = awaitingFluidOutputSpace;
    }

    public boolean awaitingFluidOutputSpace() {
        return awaitingFluidOutputSpace;
    }

    public boolean awaitingSpace() {
        return awaitingItemOutputSpace() || awaitingFluidOutputSpace();
    }

    public void updateBufferedOutputs() {
        if (awaitingItemOutputSpace && !getMetaTileEntity().getNotifiedItemOutputList().isEmpty()) {
            awaitingItemOutputSpace = false;
        }
        if (awaitingFluidOutputSpace && !getMetaTileEntity().getNotifiedFluidOutputList().isEmpty()) {
            awaitingFluidOutputSpace = false;
        }
        if (!awaitingItemOutputSpace) {
            getMetaTileEntity().getNotifiedItemOutputList().clear();
            while (!bufferedItemOutputs.isEmpty()) {
                ItemStack first = bufferedItemOutputs.removeFirst();
                ItemStack remainder = getBuffering().outputFromBuffer(first);
                if (!remainder.isEmpty() && !getMetaTileEntity().canVoidRecipeItemOutputs()) {
                    bufferedItemOutputs.addFirst(remainder);
                    awaitingItemOutputSpace = true;
                    break;
                }
            }
        }
        if (!awaitingFluidOutputSpace) {
            getMetaTileEntity().getNotifiedFluidOutputList().clear();
            while (!bufferedFluidOutputs.isEmpty()) {
                FluidStack first = bufferedFluidOutputs.peekFirst();
                first.amount -= getBuffering().outputFromBuffer(first);
                if (first.amount <= 0 || getMetaTileEntity().canVoidRecipeFluidOutputs()) {
                    bufferedFluidOutputs.removeFirst();
                } else {
                    awaitingFluidOutputSpace = true;
                    break;
                }
            }
        }
    }

    @Override
    public void onRemoval() {
        for (ItemStack itemStack : bufferedItemOutputs) {
            Block.spawnAsEntity(getMetaTileEntity().getWorld(), getMetaTileEntity().getPos(), itemStack);
        }
    }

    @Override
    public @NotNull String getName() {
        return GregtechDataCodes.OUTPUT_BUFFER_TRAIT;
    }

    @Override
    public <T> T getCapability(Capability<T> capability) {
        return null;
    }

    @Override
    public @NotNull NBTTagCompound serializeNBT() {
        NBTTagCompound data = super.serializeNBT();
        data.setTag("BufferedItems", GTUtility.serializeItems(bufferedItemOutputs));
        data.setTag("BufferedFluids", GTUtility.serializeFluids(bufferedFluidOutputs));
        return data;
    }

    @Override
    public void deserializeNBT(@NotNull NBTTagCompound compound) {
        super.deserializeNBT(compound);
        bufferedItemOutputs = GTUtility
                .deserializeItems(compound.getTagList("BufferedItems", Constants.NBT.TAG_COMPOUND), ArrayDeque::new);
        bufferedFluidOutputs = GTUtility
                .deserializeFluids(compound.getTagList("BufferedFluids", Constants.NBT.TAG_COMPOUND), ArrayDeque::new);
    }

    public interface IBufferingMTE {

        /**
         * Attempt to output the given item from the buffer.
         * 
         * @param stack the item to output.
         * @return the remaining stack after outputting as much as possible.
         */
        @NotNull
        ItemStack outputFromBuffer(@NotNull ItemStack stack);

        /**
         * Attempt to output the given fluid from the buffer.
         * 
         * @param stack the fluid to output.
         * @return the amount of fluid successfully outputted.
         */
        int outputFromBuffer(@NotNull FluidStack stack);
    }
}
