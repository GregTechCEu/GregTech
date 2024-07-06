package gregtech.common.mui.widget;

import gregtech.api.GTValues;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.LocalizationUtils;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.drawable.TextRenderer;
import com.cleanroommc.modularui.integration.jei.JeiGhostIngredientSlot;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

public class GTFluidSlot extends Widget<GTFluidSlot> implements Interactable, JeiGhostIngredientSlot<FluidStack>,
                         JeiIngredientProvider {

    private final TextRenderer textRenderer = new TextRenderer();
    private GTFluidSyncHandler syncHandler;

    public GTFluidSlot() {
        tooltip().setAutoUpdate(true).setHasTitleMargin(true);
        tooltipBuilder(tooltip -> {
            var fluid = this.syncHandler.getFluid();
            if (fluid == null) return;

            tooltip.addLine(fluid.getLocalizedName());
            tooltip.addLine(IKey.lang("gregtech.fluid.amount", fluid.amount, this.syncHandler.getCapacity()));

            // Add various tooltips from the material
            List<String> formula = FluidTooltipUtil.getFluidTooltip(fluid);
            if (formula != null) {
                for (String s : formula) {
                    if (s.isEmpty()) continue;
                    tooltip.addLine(s);
                }
            }

            addIngotMolFluidTooltip(fluid, tooltip);
        });
    }

    @Override
    public void onInit() {
        this.textRenderer.setShadow(true);
        this.textRenderer.setScale(0.5f);
        this.textRenderer.setColor(Color.WHITE.main);
        getContext().getJeiSettings().addJeiGhostIngredientSlot(this);
    }

    public GTFluidSlot syncHandler(IFluidTank fluidTank) {
        return syncHandler(new GTFluidSyncHandler(fluidTank));
    }

    public GTFluidSlot syncHandler(GTFluidSyncHandler syncHandler) {
        setSyncHandler(syncHandler);
        this.syncHandler = syncHandler;
        return this;
    }

    @Override
    public void draw(GuiContext context, WidgetTheme widgetTheme) {
        FluidStack content = this.syncHandler.getFluid();
        if (content != null) {
            GuiDraw.drawFluidTexture(content, 1, 1, getArea().w() - 2, getArea().h() - 2, 0);

            String s = NumberFormat.formatWithMaxDigits(getBaseUnitAmount(content.amount)) + getBaseUnit();
            this.textRenderer.setAlignment(Alignment.CenterRight, getArea().width - 1f);
            this.textRenderer.setPos(0, 12);
            this.textRenderer.draw(s);
        }
        if (isHovering()) {
            GlStateManager.colorMask(true, true, true, false);
            GuiDraw.drawRect(1, 1, getArea().w() - 2, getArea().h() - 2,
                    getWidgetTheme(context.getTheme()).getSlotHoverColor());
            GlStateManager.colorMask(true, true, true, true);
        }
    }

    protected double getBaseUnitAmount(double amount) {
        return amount / 1000;
    }

    protected String getBaseUnit() {
        return "L";
    }

    @NotNull
    @Override
    public Result onMouseTapped(int mouseButton) {
        this.syncHandler.syncToServer(1, buffer -> buffer.writeBoolean(mouseButton == 0));
        return Result.SUCCESS;
    }

    @Override
    public WidgetSlotTheme getWidgetTheme(ITheme theme) {
        return theme.getFluidSlotTheme();
    }

    @Override
    public void setGhostIngredient(@NotNull FluidStack ingredient) {
        this.syncHandler.setFluid(ingredient);
    }

    @Override
    public @Nullable FluidStack castGhostIngredientIfValid(@NotNull Object ingredient) {
        return ingredient instanceof FluidStack fluidStack ? fluidStack : null;
    }

    @Override
    public @Nullable Object getIngredient() {
        return this.syncHandler.getFluid();
    }

    public static void addIngotMolFluidTooltip(FluidStack fluidStack, Tooltip tooltip) {
        // Add tooltip showing how many "ingot moles" (increments of 144) this fluid is if shift is held
        if (TooltipHelper.isShiftDown() && fluidStack.amount > GTValues.L) {
            int numIngots = fluidStack.amount / GTValues.L;
            int extra = fluidStack.amount % GTValues.L;
            String fluidAmount = String.format(" %,d L = %,d * %d L", fluidStack.amount, numIngots, GTValues.L);
            if (extra != 0) {
                fluidAmount += String.format(" + %d L", extra);
            }
            tooltip.addLine(TextFormatting.GRAY + LocalizationUtils.format("gregtech.gui.amount_raw") + fluidAmount);
        }
    }

    public static class GTFluidSyncHandler extends SyncHandler {

        private final IFluidTank tank;
        private boolean canDrainSlot = true;
        private boolean canFillSlot = true;

        public GTFluidSyncHandler(IFluidTank tank) {
            this.tank = tank;
        }

        public FluidStack getFluid() {
            return this.tank.getFluid();
        }

        public int getCapacity() {
            return this.tank.getCapacity();
        }

        public void setFluid(FluidStack fluid) {
            this.tank.drain(Integer.MAX_VALUE, true);
            this.tank.fill(fluid, true);
        }

        public GTFluidSyncHandler canDrainSlot(boolean canDrainSlot) {
            this.canDrainSlot = canDrainSlot;
            return this;
        }

        public GTFluidSyncHandler canFillSlot(boolean canFillSlot) {
            this.canFillSlot = canFillSlot;
            return this;
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) throws IOException {
            if (id == 1) {
                var stack = NetworkUtils.readItemStack(buf);
                getSyncManager().setCursorItem(stack);
            }
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) throws IOException {
            if (id == 1) {
                var stack = tryClickContainer(buf.readBoolean());
                if (!stack.isEmpty())
                    syncToClient(1, buffer -> NetworkUtils.writeItemStack(buffer, stack));
            }
        }

        public ItemStack tryClickContainer(boolean tryFillAll) {
            ItemStack currentStack = getSyncManager().getCursorItem();
            if (currentStack == ItemStack.EMPTY || currentStack.getCount() == 0)
                return ItemStack.EMPTY;

            ItemStack heldItemSizedOne = currentStack.copy();
            heldItemSizedOne.setCount(1);
            IFluidHandlerItem fluidHandlerItem = heldItemSizedOne
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem == null) return ItemStack.EMPTY;

            FluidStack tankFluid = tank.getFluid();
            FluidStack heldFluid = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
            if (heldFluid != null && heldFluid.amount <= 0) {
                heldFluid = null;
            }

            if (tankFluid == null) {
                // Tank is empty, only try to drain the held item
                if (!canDrainSlot) {
                    // tank does not allow emptying cells into it, return
                    return ItemStack.EMPTY;
                }
                if (heldFluid == null) {
                    // held item has no fluid, return
                    return ItemStack.EMPTY;
                }

                // empty held item into the tank
                return fillTankFromStack(fluidHandlerItem, heldFluid, tryFillAll);
            }

            if (heldFluid != null && tank.getFluidAmount() < tank.getCapacity()) {
                // held item has a fluid, and so does the tank. tank still has some room left in it
                // either action is possible here
                if (canDrainSlot) {
                    // try to empty the item into the tank
                    return fillTankFromStack(fluidHandlerItem, heldFluid, tryFillAll);
                }
                if (!canFillSlot) {
                    // cannot fill the item from the tank, return
                    return ItemStack.EMPTY;
                }
                // slot does not allow filling, so try to take from the slot
                return drainTankFromStack(fluidHandlerItem, tryFillAll);
            } else {
                // tank is full, and there is some fluid available to take
                if (!canFillSlot) {
                    // slot does not allow taking, return
                    return ItemStack.EMPTY;
                }
                // try to take from the slot
                return drainTankFromStack(fluidHandlerItem, tryFillAll);
            }
        }

        private ItemStack fillTankFromStack(IFluidHandlerItem fluidHandler, @NotNull FluidStack heldFluid,
                                            boolean tryFillAll) {
            ItemStack heldItem = getSyncManager().getCursorItem();
            if (heldItem == ItemStack.EMPTY || heldItem.getCount() == 0) return ItemStack.EMPTY;

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

        private ItemStack drainTankFromStack(IFluidHandlerItem fluidHandler, boolean tryFillAll) {
            ItemStack heldItem = getSyncManager().getCursorItem();
            if (heldItem == ItemStack.EMPTY || heldItem.getCount() == 0) return ItemStack.EMPTY;

            FluidStack currentFluid = tank.getFluid();
            if (currentFluid == null) return ItemStack.EMPTY;
            currentFluid = currentFluid.copy();

            int originalFluidAmount = tank.getFluidAmount();

            ItemStack filledContainer = fillFluidContainer(currentFluid, fluidHandler);
            if (filledContainer != ItemStack.EMPTY) {
                int filledAmount = originalFluidAmount - currentFluid.amount;
                if (filledAmount <= 0) {
                    return ItemStack.EMPTY;
                }
                tank.drain(filledAmount, true);
                if (tryFillAll) {
                    // Determine how many more items we can fill. One item is already filled. Integer division means
                    // it will round down, so it will only fill equivalent fluid amounts. For example:
                    // Click with 3 cells, with 2500L of fluid in the tank. 2 cells will be filled, and 500L will
                    // be left behind in the tank.
                    int additional = Math.min(heldItem.getCount() - 1, currentFluid.amount / filledAmount);
                    tank.drain(filledAmount * additional, true);
                    filledContainer.grow(additional);
                }
                replaceCursorItemStack(filledContainer);
                playSound(currentFluid, false);
            }
            return filledContainer;
        }

        private ItemStack fillFluidContainer(FluidStack fluidStack, IFluidHandlerItem fluidHandler) {
            int filledAmount = fluidHandler.fill(fluidStack, true);
            if (filledAmount > 0) {
                fluidStack.amount -= filledAmount;
                return fluidHandler.getContainer();
            }
            return ItemStack.EMPTY;
        }

        /**
         * Replace the ItemStack on the player's cursor with the passed stack.
         * Use to replace empty cells with filled, or filled cells with empty.
         * If it is not fully emptied/filled, it will place the new items into the player inventory instead,
         * and shrink the held stack by the appropriate amount.
         */
        private void replaceCursorItemStack(ItemStack resultStack) {
            EntityPlayer player = getSyncManager().getPlayer();
            int resultStackSize = resultStack.getMaxStackSize();
            while (resultStack.getCount() > resultStackSize) {
                player.inventory.getItemStack().shrink(resultStackSize);
                addItemToPlayerInventory(player, resultStack.splitStack(resultStackSize));
            }
            if (player.inventory.getItemStack().getCount() == resultStack.getCount()) {
                // every item on the cursor is mutated, so leave it there
                player.inventory.setItemStack(resultStack);
            } else {
                // some items not mutated. Mutated items go into the inventory/world.
                ItemStack heldStack = player.inventory.getItemStack();
                heldStack.shrink(resultStack.getCount());
                player.inventory.setItemStack(heldStack);
                addItemToPlayerInventory(player, resultStack);
            }
        }

        /** Place an item into the player's inventory, or drop it in-world as an item entity if it cannot fit. */
        private static void addItemToPlayerInventory(EntityPlayer player, ItemStack stack) {
            if (stack == null) return;
            if (!player.inventory.addItemStackToInventory(stack) && !player.world.isRemote) {
                EntityItem dropItem = player.entityDropItem(stack, 0);
                if (dropItem != null) dropItem.setPickupDelay(0);
            }
        }

        /** Play the appropriate fluid interaction sound for the fluid. */
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
                    soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }
}
