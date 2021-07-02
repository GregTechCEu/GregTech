package gregtech.common.pipelike.itempipe.net;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class ItemNetHandler implements IItemHandler {

    private final ItemPipeNet net;
    private final BlockPos pos;

    public ItemNetHandler(ItemPipeNet net, BlockPos pos) {
        this.net = net;
        this.pos = pos;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int i) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return net.getTransferNetwork().requestItemTransfer(stack, pos, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int i) {
        return 64;
    }

}
