package gregtech.api.mui.sync;

import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.common.covers.filter.readers.SimpleFluidFilterReader;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import com.cleanroommc.modularui.api.MCHelper;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.utils.BooleanConsumer;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GTFluidSyncHandler extends SyncHandler {

    public static final int TRY_CLICK_CONTAINER = 1;
    public static final int UPDATE_TANK = 2;
    public static final int UPDATE_AMOUNT = 3;
    public static final int PHANTOM_SCROLL = 4;
    public static final int LOCK_FLUID = 5;

    private final IFluidTank tank;
    private Consumer<FluidStack> jeiHandler;
    private BooleanConsumer lockHandler;
    private BooleanSupplier isLocked;
    private Supplier<FluidStack> lockedFluid;
    private FluidStack lastFluid;
    private FluidStack phantomFluid;
    private boolean canDrainSlot = true;
    private boolean canFillSlot = true;
    private boolean phantom;
    private BooleanSupplier showAmountInTooltip = () -> true;
    private BooleanSupplier showAmountOnSlot = () -> true;
    private BooleanSupplier drawAlwaysFull = () -> true;
    @Nullable
    private Consumer<@Nullable FluidStack> changeConsumer;

    public GTFluidSyncHandler(IFluidTank tank) {
        this.tank = tank;
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        FluidStack current = getFluid();
        if (init || !GTUtility.areFluidStacksEqual(current, lastFluid)) {
            lastFluid = current == null ? null : current.copy();
            syncToClient(UPDATE_TANK, buffer -> NetworkUtils.writeFluidStack(buffer, current));
        } else if (lastFluid != null && current.amount != lastFluid.amount) {
            lastFluid.amount = current.amount;
            syncToClient(UPDATE_AMOUNT, buffer -> buffer.writeInt(current.amount));
        } else if (!isPhantom() && canLockFluid() &&
                !GTUtility.areFluidStacksEqual(this.phantomFluid, this.lockedFluid.get())) {
                    this.phantomFluid = this.lockedFluid.get();
                    sync(LOCK_FLUID, buffer -> {
                        buffer.writeBoolean(this.isLocked.getAsBoolean());
                        NetworkUtils.writeFluidStack(buffer, this.phantomFluid);
                    });
                }
    }

    public void lockFluid(FluidStack stack) {
        if (canLockFluid() && GTUtility.isEmpty(getLockedFluid())) {
            this.jeiHandler.accept(stack);
        }
    }

    public void lockFluid(boolean locked) {
        if (!canLockFluid()) return;
        this.lockHandler.accept(locked);
    }

    public GTFluidSyncHandler handleLocking(@NotNull Supplier<FluidStack> lockedFluid,
                                            @NotNull Consumer<FluidStack> jeiHandler,
                                            @NotNull BooleanConsumer lockHandler, @NotNull BooleanSupplier isLocked) {
        this.lockedFluid = lockedFluid;
        this.jeiHandler = jeiHandler;
        this.lockHandler = lockHandler;
        this.isLocked = isLocked;
        return this;
    }

    public FluidStack getFluid() {
        return this.tank.getFluid();
    }

    public void setFluid(FluidStack fluid) {
        if (tank instanceof FluidTank fluidTank) {
            fluidTank.setFluid(fluid);
        } else {
            tank.drain(Integer.MAX_VALUE, true);
            if (!GTUtility.isEmpty(fluid)) tank.fill(fluid, true);
        }
        if (canLockFluid() && isLocked.getAsBoolean() && !GTUtility.isEmpty(fluid)) {
            lockFluid(fluid);
        }
        if (!isPhantom() || GTUtility.isEmpty(fluid)) return;
        if (GTUtility.isEmpty(this.phantomFluid) || this.phantomFluid.getFluid() != fluid.getFluid()) {
            this.phantomFluid = fluid;
        }
    }

    public void setAmount(int amount) {
        if (this.tank instanceof SimpleFluidFilterReader.WritableFluidTank writableFluidTank) {
            writableFluidTank.setFluidAmount(amount);
            return;
        }
        FluidStack stack = getFluid();
        if (stack == null) return;
        stack.amount = amount;
    }

    public int getCapacity() {
        return this.tank.getCapacity();
    }

    public GTFluidSyncHandler accessibility(boolean canDrain, boolean canFill) {
        this.canDrainSlot = canDrain;
        this.canFillSlot = canFill;
        return this;
    }

    public boolean canDrainSlot() {
        return this.canDrainSlot;
    }

    public boolean canFillSlot() {
        return this.canFillSlot;
    }

    public GTFluidSyncHandler phantom(boolean phantom) {
        this.phantom = phantom;
        if (phantom && this.tank.getFluid() != null)
            this.phantomFluid = this.tank.getFluid().copy();
        return this;
    }

    public boolean isPhantom() {
        return phantom;
    }

    public GTFluidSyncHandler showAmount(boolean inSlot, boolean inTooltip) {
        return showAmount(() -> inSlot, () -> inTooltip);
    }

    public GTFluidSyncHandler showAmount(BooleanSupplier inSlot, BooleanSupplier inTooltip) {
        return showAmountOnSlot(inSlot).showAmountInTooltip(inTooltip);
    }

    public GTFluidSyncHandler showAmountInTooltip(boolean showAmount) {
        return showAmountInTooltip(() -> showAmount);
    }

    public GTFluidSyncHandler showAmountInTooltip(BooleanSupplier showAmount) {
        this.showAmountInTooltip = showAmount;
        return this;
    }

    public boolean showAmountInTooltip() {
        if (!isPhantom() && phantomFluid != null)
            return false;
        return this.showAmountInTooltip.getAsBoolean();
    }

    public GTFluidSyncHandler showAmountOnSlot(boolean showAmount) {
        return showAmountOnSlot(() -> showAmount);
    }

    public GTFluidSyncHandler showAmountOnSlot(BooleanSupplier showAmount) {
        this.showAmountOnSlot = showAmount;
        return this;
    }

    public boolean showAmountOnSlot() {
        if (!isPhantom() && phantomFluid != null)
            return false;
        return this.showAmountOnSlot.getAsBoolean();
    }

    public GTFluidSyncHandler drawAlwaysFull(boolean drawAsFull) {
        this.drawAlwaysFull = () -> drawAsFull;
        return this;
    }

    public GTFluidSyncHandler drawAlwaysFull(BooleanSupplier drawAsFull) {
        this.drawAlwaysFull = drawAsFull;
        return this;
    }

    public boolean drawAlwaysFull() {
        return this.drawAlwaysFull.getAsBoolean();
    }

    public void setChangeConsumer(@Nullable Consumer<@Nullable FluidStack> changeConsumer) {
        this.changeConsumer = changeConsumer;
    }

    protected void onChange(@Nullable FluidStack fluidStack) {
        if (changeConsumer != null) {
            changeConsumer.accept(fluidStack);
        }
    }

    public @NotNull String getFormattedFluidAmount() {
        var tankFluid = this.tank.getFluid();
        return String.format("%,d", tankFluid == null ? 0 : tankFluid.amount);
    }

    public int getFluidAmount() {
        FluidStack tankFluid = tank.getFluid();
        return tankFluid == null ? 0 : tankFluid.amount;
    }

    public @Nullable String getFluidLocalizedName() {
        var tankFluid = this.tank.getFluid();
        if (tankFluid == null)
            tankFluid = getLockedFluid();

        return tankFluid == null ? null : tankFluid.getLocalizedName();
    }

    public @NotNull IKey getFluidNameKey() {
        FluidStack tankFluid = tank.getFluid();
        if (tankFluid == null) {
            tankFluid = getLockedFluid();
        }
        return tankFluid == null ? IKey.EMPTY : KeyUtil.fluid(tankFluid);
    }

    public void handleTooltip(@NotNull RichTooltip tooltip) {
        FluidStack tankFluid = getFluid();
        if (GTUtility.isEmpty(tankFluid)) {
            tankFluid = getLockedFluid();
        }

        if (!GTUtility.isEmpty(tankFluid)) {
            tooltip.addLine(KeyUtil.fluid(tankFluid));

            FluidTooltipUtil.handleFluidTooltip(tooltip, tankFluid);

            if (showAmountInTooltip()) {
                FluidTooltipUtil.addIngotMolFluidTooltip(tooltip, tankFluid);
            }

            tooltip.addLine(MCHelper.getFluidModName(tankFluid));

            if (isPhantom() && showAmountInTooltip()) {
                tooltip.addLine(IKey.lang("modularui.fluid.phantom.control"));
            }
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        switch (id) {
            case TRY_CLICK_CONTAINER -> replaceCursorItemStack(NetworkUtils.readItemStack(buf));
            case UPDATE_TANK -> {
                FluidStack stack = NetworkUtils.readFluidStack(buf);
                setFluid(stack);
                onChange(stack);
            }
            case UPDATE_AMOUNT -> {
                setAmount(buf.readInt());
                onChange(getFluid());
            }
            case LOCK_FLUID -> {
                lockHandler.accept(buf.readBoolean());
                lockFluid(NetworkUtils.readFluidStack(buf));
                FluidStack stack = getFluid();
                onChange(stack == null ? getLockedFluid() : stack);
            }
        }
    }

    public void handlePhantomScroll(MouseData data) {
        syncToServer(PHANTOM_SCROLL, data::writeToPacket);
    }

    public void handleClick(MouseData data) {
        syncToServer(TRY_CLICK_CONTAINER, data::writeToPacket);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == TRY_CLICK_CONTAINER) {
            var data = MouseData.readPacket(buf);
            if (canLockFluid())
                toggleLockFluid();
            if (isPhantom()) {
                tryClickPhantom(data);
            } else {
                var stack = tryClickContainer(data.mouseButton == 0);
                if (!stack.isEmpty())
                    syncToClient(TRY_CLICK_CONTAINER, buffer -> NetworkUtils.writeItemStack(buffer, stack));
            }
        } else if (id == UPDATE_TANK) {
            var fluid = NetworkUtils.readFluidStack(buf);
            setFluid(fluid);
        } else if (id == PHANTOM_SCROLL) {
            tryScrollPhantom(MouseData.readPacket(buf));
        }
    }

    public void tryClickPhantom(MouseData data) {
        EntityPlayer player = getSyncManager().getPlayer();
        ItemStack currentStack = player.inventory.getItemStack();
        FluidStack currentFluid = this.tank.getFluid();
        IFluidHandlerItem fluidHandlerItem = FluidUtil.getFluidHandler(currentStack);

        switch (data.mouseButton) {
            case 0 -> {
                if (currentStack.isEmpty() || fluidHandlerItem == null) {
                    if (this.canDrainSlot()) {
                        this.tank.drain(data.shift ? Integer.MAX_VALUE : 1000, true);
                    }
                } else {
                    FluidStack cellFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
                    if (!GTUtility.areFluidStacksEqual(cellFluid, currentFluid)) {

                        // drain existing
                        if (this.canDrainSlot()) {
                            int amt = data.shift ? Integer.MAX_VALUE : 1000;
                            this.tank.drain(amt, true);
                        }

                        // then fill
                        if (this.canFillSlot()) {
                            FluidStack fill;
                            if (this.showAmountOnSlot.getAsBoolean() && !GTUtility.isEmpty(cellFluid)) {
                                fill = GTUtility.copy(cellFluid);
                            } else {
                                fill = GTUtility.copy(1, cellFluid);
                            }
                            if (fill == null || this.tank.fill(fill, true) > 0) {
                                this.phantomFluid = fill;
                            }
                        }
                    }
                }
            }
            case 1 -> {
                if (this.canFillSlot()) {
                    if (currentFluid != null) {
                        if (this.showAmountOnSlot.getAsBoolean()) {
                            FluidStack toFill = currentFluid.copy();
                            toFill.amount = 1000;
                            this.tank.fill(toFill, true);
                        }
                    } else if (this.phantomFluid != null) {
                        FluidStack toFill = this.phantomFluid.copy();
                        toFill.amount = this.showAmountOnSlot.getAsBoolean() ? 1 : toFill.amount;
                        this.tank.fill(toFill, true);
                    }
                }
            }
            case 2 -> {
                if (currentFluid != null && canDrainSlot())
                    this.tank.drain(data.shift ? Integer.MAX_VALUE : 1000, true);
            }
        }
    }

    public void tryScrollPhantom(MouseData mouseData) {
        FluidStack currentFluid = this.tank.getFluid();
        int amount = mouseData.mouseButton;
        if (!this.showAmountOnSlot()) {
            int newAmt = amount == 1 ? 1 : 0;
            if (newAmt == 0) {
                setFluid(null);
            } else if (currentFluid != null && currentFluid.amount != newAmt) {
                setAmount(newAmt);
            }
            return;
        }
        if (mouseData.shift) {
            amount *= 10;
        }
        if (mouseData.ctrl) {
            amount *= 100;
        }
        if (mouseData.alt) {
            amount *= 1000;
        }
        if (currentFluid == null) {
            if (amount > 0 && this.phantomFluid != null) {
                FluidStack toFill = this.phantomFluid.copy();
                toFill.amount = this.showAmountOnSlot() ? amount : 1;
                this.tank.fill(toFill, true);
            }
            return;
        }
        if (amount > 0) {
            FluidStack toFill = currentFluid.copy();
            toFill.amount = amount;
            this.tank.fill(toFill, true);
        } else if (amount < 0) {
            this.tank.drain(-amount, true);
        }
    }

    public ItemStack tryClickContainer(boolean tryFillAll) {
        ItemStack playerHeldStack = getSyncManager().getCursorItem();
        if (playerHeldStack.isEmpty())
            return ItemStack.EMPTY;

        ItemStack useStack = GTUtility.copy(1, playerHeldStack);
        IFluidHandlerItem fluidHandlerItem = FluidUtil.getFluidHandler(useStack);
        FluidStack heldFluid = FluidUtil.getFluidContained(playerHeldStack);
        FluidStack tankFluid = tank.getFluid();

        if (fluidHandlerItem == null || heldFluid == tankFluid)
            return ItemStack.EMPTY;

        if (canLockFluid() && isLocked.getAsBoolean()) {
            FluidStack lockedFluid = getLockedFluid();
            if (lockedFluid == null && heldFluid != null) {
                lockFluid(heldFluid);
            } else if (!Objects.equals(heldFluid, lockedFluid)) {
                return ItemStack.EMPTY;
            }
        }

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

    public FluidStack getPhantomFluid() {
        return isPhantom() ? phantomFluid : null;
    }

    public FluidStack getLockedFluid() {
        return !isPhantom() && canLockFluid() ? lockedFluid.get() : null;
    }

    public boolean canLockFluid() {
        return jeiHandler != null && lockHandler != null && lockedFluid != null && isLocked != null;
    }

    public void toggleLockFluid() {
        ItemStack cursorItem = getSyncManager().getCursorItem();
        FluidStack fluidStack = FluidUtil.getFluidContained(cursorItem);
        FluidStack stack;
        if (GTUtility.isEmpty(getLockedFluid()) && !GTUtility.isEmpty(fluidStack)) {
            stack = fluidStack.copy();
        } else if (!GTUtility.isEmpty(getLockedFluid()) && !Objects.equals(getLockedFluid(), fluidStack)) {
            return;
        } else {
            stack = null;
        }
        lockFluid(stack);
        sync(LOCK_FLUID, buffer -> {
            buffer.writeBoolean(this.isLocked.getAsBoolean());
            NetworkUtils.writeFluidStack(buffer, stack);
        });
    }
}
