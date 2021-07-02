package gregtech.common.pipelike.itempipe.tile;

import gregtech.api.pipenet.block.simple.EmptyNodeData;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.pipelike.fluidpipe.FluidPipeProperties;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.fluidpipe.net.FluidPipeNet;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import gregtech.common.pipelike.itempipe.net.ItemPipeNet;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;

public class ItemPipeInventory implements IItemHandlerModifiable {

    private final IPipeTile<ItemPipeType, EmptyNodeData> pipeTile;
    private WeakReference<ItemPipeNet> currentPipeNet = new WeakReference<>(null);

    public ItemPipeInventory(IPipeTile<ItemPipeType, EmptyNodeData> pipeTile) {
        this.pipeTile = pipeTile;
    }

    @Override
    public void setStackInSlot(int i, @Nonnull ItemStack itemStack) {

    }

    @Override
    public int getSlots() {
        return 0;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int i) {
        return null;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int i, @Nonnull ItemStack itemStack, boolean b) {
        return null;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int i, int i1, boolean b) {
        return null;
    }

    @Override
    public int getSlotLimit(int i) {
        return 0;
    }
}
