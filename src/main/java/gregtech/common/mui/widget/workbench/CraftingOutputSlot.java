package gregtech.common.mui.widget.workbench;

import gregtech.client.utils.RenderUtil;
import gregtech.common.metatileentities.storage.CraftingRecipeLogic;
import gregtech.common.metatileentities.storage.CraftingRecipeMemory;
import gregtech.common.metatileentities.storage.MetaTileEntityWorkbench;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.integration.jei.JeiIngredientProvider;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CraftingOutputSlot extends Widget<CraftingOutputSlot> implements Interactable, JeiIngredientProvider {

    private static final int MOUSE_CLICK = 2;
    private static final int SYNC_STACK = 5;
    private final CraftingSlotSH syncHandler;

    public CraftingOutputSlot(IntSyncValue amountCrafted, MetaTileEntityWorkbench workbench) {
        this.syncHandler = new CraftingSlotSH(amountCrafted, workbench);
        setSyncHandler(this.syncHandler);
        tooltipAutoUpdate(true);
        tooltipBuilder(tooltip -> {
            if (!isSynced()) return;
            ItemStack stack = this.syncHandler.getOutputStack();
            if (stack.isEmpty()) return;
            tooltip.addFromItem(stack);
        });
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler syncHandler) {
        return syncHandler instanceof CraftingSlotSH;
    }

    @Override
    public @NotNull Result onMousePressed(int mouseButton) {
        MouseData mouseData = MouseData.create(mouseButton);
        // if there's a valid recipe, then the output slot should not be empty
        if (!getIngredient().isEmpty())
            this.syncHandler.syncToServer(MOUSE_CLICK, mouseData::writeToPacket);
        return Result.SUCCESS;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetTheme widgetTheme) {
        ItemStack itemstack = this.syncHandler.getOutputStack();
        RenderUtil.drawItemStack(itemstack, 1, 1, true);
        RenderUtil.handleSlotOverlay(this, widgetTheme);
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        RichTooltip tooltip = getTooltip();
        if (tooltip != null && isHoveringFor(tooltip.getShowUpTimer())) {
            tooltip.draw(getContext(), this.syncHandler.getOutputStack());
        }
    }

    @Override
    public @NotNull ItemStack getIngredient() {
        return this.syncHandler.getOutputStack();
    }

    protected static class CraftingSlotSH extends SyncHandler {

        private final CraftingRecipeLogic recipeLogic;
        private final CraftingOutputMS slot;

        private final List<ModularSlot> shiftClickSlots = new ArrayList<>();

        public CraftingSlotSH(IntSyncValue amountCrafted, MetaTileEntityWorkbench workbench) {
            this.slot = new CraftingOutputMS(amountCrafted, workbench);
            this.recipeLogic = slot.recipeLogic;
        }

        @Override
        public void init(String key, PanelSyncManager syncManager) {
            super.init(key, syncManager);
            getSyncManager().getSlotGroups().stream()
                    .filter(SlotGroup::allowShiftTransfer)
                    .sorted(Comparator.comparingInt(SlotGroup::getShiftClickPriority))
                    .collect(Collectors.toList())
                    .forEach(slotGroup -> {
                        for (Slot slot : slotGroup.getSlots()) {
                            if (slot instanceof ModularSlot modularSlot) {
                                this.shiftClickSlots.add(modularSlot);
                            }
                        }
                    });
        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            if (id == MOUSE_CLICK) {
                EntityPlayer player = getSyncManager().getPlayer();
                ForgeHooks.setCraftingPlayer(player);
                var data = MouseData.readPacket(buf);

                if (recipeLogic.isRecipeValid()) {
                    ItemStack outputStack = getOutputStack();
                    boolean hasSpace;
                    if (data.shift) {
                        hasSpace = quickTransfer(getOutputStack(), true);
                    } else {
                        hasSpace = this.slot.canTakeStack(player);
                    }
                    if (hasSpace && recipeLogic.performRecipe()) {
                        handleItemCraft(outputStack, player);

                        if (data.shift) {
                            ItemStack finalStack = outputStack.copy();
                            while (quickTransfer(finalStack, true) &&
                                    finalStack.getCount() < outputStack.getMaxStackSize()) {
                                if (!recipeLogic.performRecipe()) break;
                                finalStack.setCount(finalStack.getCount() + outputStack.getCount());
                                handleItemCraft(outputStack, player);
                            }
                            quickTransfer(finalStack, false);
                        } else {
                            syncToClient(SYNC_STACK, this::syncCursorStack);
                        }
                    }
                }
                ForgeHooks.setCraftingPlayer(null);
            }
        }

        private boolean insertStack(ItemStack fromStack, ModularSlot toSlot, boolean simulate) {
            ItemStack toStack = toSlot.getStack().copy();
            if (ItemHandlerHelper.canItemStacksStack(fromStack, toStack)) {
                int combined = toStack.getCount() + fromStack.getCount();
                int maxSize = Math.min(toSlot.getSlotStackLimit(), fromStack.getMaxStackSize());

                // we can fit all of toStack
                if (combined <= maxSize) {
                    if (simulate) return true;
                    fromStack.setCount(0);
                    toStack.setCount(combined);
                    toSlot.putStack(toStack);
                } else if (toStack.getCount() < maxSize) {
                    if (simulate) return true;
                    // we can fit some of toStack, but not all
                    fromStack.shrink(maxSize - toStack.getCount());
                    toStack.setCount(maxSize);
                    toSlot.putStack(toStack);
                }

                return fromStack.isEmpty();
            } else if (toStack.isEmpty()) {
                if (simulate) return true;
                int maxSize = Math.max(toSlot.getSlotStackLimit(), fromStack.getCount());
                toSlot.putStack(fromStack.splitStack(maxSize));
                return fromStack.isEmpty();
            }
            return false;
        }

        public boolean quickTransfer(ItemStack fromStack, boolean simulate) {
            List<ModularSlot> emptySlots = new ArrayList<>();
            for (ModularSlot toSlot : this.shiftClickSlots) {
                if (toSlot.isEnabled() && toSlot.isItemValid(fromStack)) {
                    if (toSlot.getStack().isEmpty()) {
                        emptySlots.add(toSlot);
                        continue;
                    }

                    if (insertStack(fromStack, toSlot, simulate)) {
                        if (simulate || fromStack.isEmpty()) return true;
                    }
                }
            }
            for (ModularSlot emptySlot : emptySlots) {
                if (insertStack(fromStack, emptySlot, simulate)) {
                    if (simulate || fromStack.isEmpty()) return true;
                }
            }
            return false;
        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {
            if (id == SYNC_STACK && buf.readBoolean()) {
                getSyncManager().setCursorItem(NetworkUtils.readItemStack(buf));
            }
        }

        private void syncCursorStack(PacketBuffer buf) {
            ItemStack curStack = getSyncManager().getCursorItem();
            ItemStack outStack = this.slot.getStack();
            if (this.slot.canTakeStack(getSyncManager().getPlayer())) {
                ItemStack toSync = outStack.copy();
                int combined = curStack.getCount() + outStack.getCount();
                // clamp to max stack size
                toSync.setCount(Math.min(combined, outStack.getMaxStackSize()));
                buf.writeBoolean(true);
                NetworkUtils.writeItemStack(buf, toSync);
            } else {
                buf.writeBoolean(false);
            }
        }

        public ItemStack getOutputStack() {
            return slot.getStack();
        }

        public void handleItemCraft(ItemStack craftedStack, EntityPlayer player) {
            craftedStack.onCrafting(player.world, player, 1);

            var inventoryCrafting = recipeLogic.getCraftingMatrix();

            // if we're not simulated, fire the event, unlock recipe and add crafted items, and play sounds
            FMLCommonHandler.instance().firePlayerCraftingEvent(player, craftedStack, inventoryCrafting);

            var cachedRecipe = recipeLogic.getCachedRecipe();
            if (cachedRecipe != null) {
                if (!cachedRecipe.isDynamic()) {
                    player.unlockRecipes(Lists.newArrayList(cachedRecipe));
                }
                ItemStack resultStack = cachedRecipe.getCraftingResult(inventoryCrafting);
                this.slot.notifyRecipePerformed(resultStack);
            }
        }
    }

    protected static class CraftingOutputMS extends ModularSlot {

        private final IntSyncValue amountCrafted;
        private final CraftingRecipeLogic recipeLogic;
        private final CraftingRecipeMemory recipeMemory;
        private final IItemHandler craftingGrid;

        public CraftingOutputMS(IntSyncValue amountCrafted, MetaTileEntityWorkbench workbench) {
            super(new InventoryWrapper(
                    workbench.getCraftingRecipeLogic().getCraftingResultInventory(),
                    workbench.getCraftingRecipeLogic()), 0, true);
            this.amountCrafted = amountCrafted;
            this.recipeLogic = workbench.getCraftingRecipeLogic();
            this.recipeMemory = workbench.getRecipeMemory();
            this.craftingGrid = workbench.getCraftingGrid();
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            ItemStack curStack = playerIn.inventory.getItemStack();
            if (curStack.isEmpty()) return true;

            ItemStack outStack = getStack();
            if (ItemHandlerHelper.canItemStacksStack(curStack, outStack)) {
                int combined = curStack.getCount() + outStack.getCount();
                return combined <= outStack.getMaxStackSize();
            } else {
                return false;
            }
        }

        public void notifyRecipePerformed(ItemStack stack) {
            this.amountCrafted.setValue(this.amountCrafted.getValue() + stack.getCount(), true, true);
            this.recipeMemory.notifyRecipePerformed(this.craftingGrid, stack);
        }

        @Override
        public void putStack(@NotNull ItemStack stack) {
            super.putStack(getStack());
        }

        @Override
        public @NotNull ItemStack decrStackSize(int amount) {
            return getStack();
        }
    }

    private static class InventoryWrapper implements IItemHandlerModifiable {

        private final IInventory inventory;
        private final CraftingRecipeLogic recipeLogic;

        private InventoryWrapper(IInventory inventory, CraftingRecipeLogic recipeLogic) {
            this.inventory = inventory;
            this.recipeLogic = recipeLogic;
        }

        @Override
        public int getSlots() {
            return inventory.getSizeInventory();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot).copy();
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getInventoryStackLimit();
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (!recipeLogic.isRecipeValid()) {
                inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
            }

            if (!stack.isEmpty())
                inventory.setInventorySlotContents(slot, stack);
        }
    }
}
