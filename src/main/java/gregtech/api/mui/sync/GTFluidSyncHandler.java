package gregtech.api.mui.sync;

import com.cleanroommc.modularui.utils.MouseData;

import gregtech.api.util.GTUtility;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;

public class GTFluidSyncHandler extends SyncHandler {

    public static final int TRY_CLICK_CONTAINER = 1;
    public static final int UPDATE_TANK = 2;
    public static final int UPDATE_AMOUNT = 3;

    private final IFluidTank tank;
    private FluidStack lastFluid;
    private FluidStack lockedFluid;
    private boolean canDrainSlot = true;
    private boolean canFillSlot = true;
    private boolean phantom;

    public GTFluidSyncHandler(IFluidTank tank) {
        this.tank = tank;
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        var current = getFluid();
        if (current == null && lastFluid == null) return;
        if (current == null || lastFluid == null || lastFluid.getFluid() != current.getFluid()) {
            lastFluid = current == null ? null : current.copy();
            syncToClient(UPDATE_TANK, buffer -> NetworkUtils.writeFluidStack(buffer, current));
        } else if (current.amount != lastFluid.amount) {
            syncToClient(UPDATE_AMOUNT, buffer -> buffer.writeInt(current.amount));
        }
    }

    public FluidStack getFluid() {
        return this.tank.getFluid();
    }

    public void setFluid(FluidStack fluid) {
        if (tank instanceof FluidTank fluidTank) {
            fluidTank.setFluid(fluid);
        } else {
            tank.drain(Integer.MAX_VALUE, true);
            tank.fill(fluid, true);
        }
    }

    public void setAmount(int amount) {
        if (getFluid() == null) return;
        getFluid().amount = amount;
    }

    public int getCapacity() {
        return this.tank.getCapacity();
    }

    public GTFluidSyncHandler canDrainSlot(boolean canDrainSlot) {
        this.canDrainSlot = canDrainSlot;
        return this;
    }

    public boolean canDrainSlot() {
        return this.canDrainSlot;
    }

    public GTFluidSyncHandler canFillSlot(boolean canFillSlot) {
        this.canFillSlot = canFillSlot;
        return this;
    }

    public boolean canFillSlot() {
        return this.canFillSlot;
    }

    public GTFluidSyncHandler phantom(boolean phantom) {
        this.phantom = phantom;
        return this;
    }

    public boolean isPhantom() {
        return phantom;
    }

    public String getFormattedFluidAmount() {
        return String.format("%,d", tank.getFluid() == null ? 0 : tank.getFluid().amount);
    }

    public String getFluidLocalizedName() {
        return tank.getFluid() == null ? "" : tank.getFluid().getLocalizedName();
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        switch (id) {
            case TRY_CLICK_CONTAINER -> replaceCursorItemStack(NetworkUtils.readItemStack(buf));
            case UPDATE_TANK -> setFluid(NetworkUtils.readFluidStack(buf));
            case UPDATE_AMOUNT -> setAmount(buf.readInt());
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == TRY_CLICK_CONTAINER) {
            var data = MouseData.readPacket(buf);
            var stack = tryClickContainer(data.mouseButton == 0);
            if (!stack.isEmpty())
                syncToClient(TRY_CLICK_CONTAINER, buffer -> NetworkUtils.writeItemStack(buffer, stack));
        }
    }

    public ItemStack tryClickContainer(boolean tryFillAll) {
        ItemStack playerHeldStack = getSyncManager().getCursorItem();
        if (playerHeldStack.isEmpty())
            return ItemStack.EMPTY;

        ItemStack useStack = GTUtility.copy(1, playerHeldStack);
        IFluidHandlerItem fluidHandlerItem = useStack
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandlerItem == null) return ItemStack.EMPTY;

        FluidStack tankFluid = tank.getFluid();
        FluidStack heldFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, false);

        // nothing to do, return
        if (tankFluid == null && heldFluid == null)
            return ItemStack.EMPTY;

        ItemStack returnable = ItemStack.EMPTY;

        // tank is empty, try to fill tank
        if (canFillSlot && tankFluid == null) {
            returnable = fillTankFromStack(fluidHandlerItem, heldFluid, tryFillAll);

            // hand is empty, try to drain tank
        } else if (canDrainSlot && heldFluid == null) {
            returnable = drainTankIntoStack(fluidHandlerItem, tankFluid, tryFillAll);

            // neither is empty but tank is not full, try to fill tank
        } else if (canFillSlot && tank.getFluidAmount() < tank.getCapacity() && heldFluid != null) {
            returnable = fillTankFromStack(fluidHandlerItem, heldFluid, tryFillAll);
        }

        syncToClient(UPDATE_TANK, buffer -> NetworkUtils.writeFluidStack(buffer, tank.getFluid()));

        return returnable;
    }

    private ItemStack fillTankFromStack(IFluidHandlerItem fluidHandler, @NotNull FluidStack heldFluid,
                                        boolean tryFillAll) {
        ItemStack heldItem = getSyncManager().getCursorItem();
        if (heldItem.isEmpty()) return ItemStack.EMPTY;

        FluidStack currentFluid = tank.getFluid();
        // Fluid type does not match
        if (currentFluid != null && !currentFluid.isFluidEqual(heldFluid)) return ItemStack.EMPTY;

        int freeSpace = tank.getCapacity() - tank.getFluidAmount();
        if (freeSpace <= 0) return ItemStack.EMPTY;

        ItemStack itemStackEmptied = ItemStack.EMPTY;
        int fluidAmountTaken = 0;

        FluidStack drained = fluidHandler.drain(freeSpace, true);
        if (drained != null && drained.amount > 0) {
            itemStackEmptied = fluidHandler.getContainer();
            fluidAmountTaken = drained.amount;
        }
        if (itemStackEmptied == ItemStack.EMPTY) {
            return ItemStack.EMPTY;
        }

        // find out how many fills we can do
        // same round down behavior as drain
        int additional = tryFillAll ? Math.min(freeSpace / fluidAmountTaken, heldItem.getCount()) : 1;
        FluidStack copiedFluidStack = heldFluid.copy();
        copiedFluidStack.amount = fluidAmountTaken * additional;
        tank.fill(copiedFluidStack, true);

        itemStackEmptied.setCount(additional);
        replaceCursorItemStack(itemStackEmptied);
        playSound(heldFluid, true);
        return itemStackEmptied;
    }

    private ItemStack drainTankIntoStack(IFluidHandlerItem fluidHandler, FluidStack tankFluid, boolean tryFillAll) {
        ItemStack heldItem = getSyncManager().getCursorItem();
        if (heldItem.isEmpty()) return ItemStack.EMPTY;

        ItemStack fluidContainer = ItemStack.EMPTY;
        int filled = fluidHandler.fill(tankFluid, false);
        int stored = tankFluid.amount;
        if (filled > 0) {
            fluidHandler.fill(tankFluid, true);
            tank.drain(filled, true);
            fluidContainer = fluidHandler.getContainer();
            if (tryFillAll) {
                // Determine how many more items we can fill. One item is already filled.
                // Integer division means it will round down, so it will only fill equivalent fluid amounts.
                // For example:
                // Click with 3 cells, with 2500L of fluid in the tank.
                // 2 cells will be filled, and 500L will be left behind in the tank.
                int additional = Math.min(heldItem.getCount(), stored / filled) - 1;
                tank.drain(filled * additional, true);
                fluidContainer.grow(additional);
            }
            replaceCursorItemStack(fluidContainer);
            playSound(tankFluid, false);
        }
        return fluidContainer;
    }

    /**
     * Replace the ItemStack on the player's cursor with the passed stack. Use to replace empty cells with filled, or
     * filled cells with empty. If it is not fully emptied/filled, it will place the new items into the player inventory
     * instead, and shrink the held stack by the appropriate amount.
     */
    private void replaceCursorItemStack(ItemStack resultStack) {
        int resultStackSize = resultStack.getMaxStackSize();
        ItemStack playerStack = getSyncManager().getCursorItem();

        if (!getSyncManager().isClient())
            syncToClient(TRY_CLICK_CONTAINER, buffer -> NetworkUtils.writeItemStack(buffer, resultStack));

        while (resultStack.getCount() > resultStackSize) {
            playerStack.shrink(resultStackSize);
            addItemToPlayerInventory(resultStack.splitStack(resultStackSize));
        }
        if (playerStack.getCount() == resultStack.getCount()) {
            // every item on the cursor is mutated, so leave it there
            getSyncManager().setCursorItem(resultStack);
        } else {
            // some items not mutated. Mutated items go into the inventory/world.
            playerStack.shrink(resultStack.getCount());
            getSyncManager().setCursorItem(playerStack);
            addItemToPlayerInventory(resultStack);
        }
    }

    /** Place an item into the player's inventory, or drop it in-world as an item entity if it cannot fit. */
    private void addItemToPlayerInventory(ItemStack stack) {
        if (stack == null) return;
        var player = getSyncManager().getPlayer();

        if (!player.inventory.addItemStackToInventory(stack) && !player.world.isRemote) {
            EntityItem dropItem = player.entityDropItem(stack, 0);
            if (dropItem != null) dropItem.setPickupDelay(0);
        }
    }

    /**
     * Play the appropriate fluid interaction sound for the fluid. <br />
     * Must be called on server to work correctly
     **/
    private void playSound(FluidStack fluid, boolean fill) {
        if (fluid == null) return;
        SoundEvent soundEvent;
        if (fill) {
            soundEvent = fluid.getFluid().getFillSound(fluid);
        } else {
            soundEvent = fluid.getFluid().getEmptySound(fluid);
        }
        EntityPlayer player = getSyncManager().getPlayer();
        player.world.playSound(null, player.posX, player.posY + 0.5, player.posZ,
                soundEvent, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }
}
