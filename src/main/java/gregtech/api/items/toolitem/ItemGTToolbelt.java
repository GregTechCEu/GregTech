package gregtech.api.items.toolitem;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.LocalizationUtils;
import gregtech.core.network.packets.PacketToolbeltSelectionChange;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.factory.ItemGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class ItemGTToolbelt extends ItemGTTool {

    protected final static Set<String> VALID_OREDICTS = new ObjectOpenHashSet<>();

    private final ItemStack orestack;

    public ItemGTToolbelt(String domain, String id, Supplier<ItemStack> markerItem) {
        super(domain, id, -1, new ToolDefinitionBuilder().cannotAttack().attackSpeed(-2.4F).build(), null,
                false, new HashSet<>(), "", new ArrayList<>(),
                markerItem);
        this.orestack = new ItemStack(this, 1, GTValues.W);
    }

    @Override
    public int getDamage(@NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            selected.getItemDamage();
        }
        return super.getDamage(stack);
    }

    @Override
    public void setDamage(@NotNull ItemStack stack, int damage) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            selected.setItemDamage(damage);
        } else super.setDamage(stack, damage);
    }

    public @Nullable ItemStack getSelectedItem(@NotNull ItemStack stack) {
        return getHandler(stack).getSelectedStack();
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, @NotNull EntityPlayer player,
                                                             @NotNull EnumHand hand) {
        if (player.isSneaking()) {
            if (!world.isRemote) {
                ItemGuiFactory.open((EntityPlayerMP) player, hand);
            }
            return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        } else return definition$onItemRightClick(world, player, hand);
    }

    @Override
    public ModularPanel buildUI(HandGuiData guiData, GuiSyncManager guiSyncManager) {
        ToolStackHandler handler = getHandler(guiData.getUsedItemStack());
        int heightBonus = (handler.getSlots() / 9) * 18;

        ModularPanel panel = GTGuis.createPanel(guiData.getUsedItemStack().getDisplayName(), 176, 120 + heightBonus);

        SlotGroupWidget slotGroupWidget = new SlotGroupWidget();
        slotGroupWidget.flex()
                .coverChildren()
                .startDefaultMode()
                .leftRel(0.5f);
        slotGroupWidget.flex().top(7);
        slotGroupWidget.flex().endDefaultMode();
        slotGroupWidget.debugName("toolbelt_inventory");
        for (int i = 0; i < handler.getSlots(); i++) {
            slotGroupWidget.child(new ItemSlot()
                    .slot(SyncHandlers.itemSlot(handler, i))
                    .background(GTGuiTextures.SLOT, GTGuiTextures.TOOL_SLOT_OVERLAY)
                    .pos(i % 9 * 18, i / 9 * 18)
                    .debugName("slot_" + i));
        }
        panel.child(slotGroupWidget);

        return panel.bindPlayerInventory();
    }

    public static boolean isToolbeltableOredict(String oredict) {
        return VALID_OREDICTS.contains(oredict);
    }

    public void registerValidOredict(String oredict) {
        VALID_OREDICTS.add(oredict);
        OreDictUnifier.registerOre(this.orestack, oredict);
    }

    @Override
    public @NotNull Set<String> getToolClasses(@NotNull ItemStack stack) {
        return getHandler(stack).getToolClasses(true);
    }

    @Override
    public boolean hasContainerItem(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull ItemStack getContainerItem(@NotNull ItemStack stack) {
        return stack.copy();
    }

    public boolean damageAgainstMaintenanceProblem(ItemStack stack, String toolClass,
                                                   @Nullable EntityPlayer entityPlayer) {
        return getHandler(stack).checkMaintenanceAgainstTools(toolClass, true, entityPlayer);
    }

    public boolean supportsIngredient(ItemStack stack, Ingredient ingredient) {
        return getHandler(stack).checkIngredientAgainstTools(ingredient, false);
    }

    public void craftDamageTools(ItemStack stack, Ingredient ingredient) {
        getHandler(stack).checkIngredientAgainstTools(ingredient, true);
    }

    private ToolStackHandler getHandler(ItemStack stack) {
        return (ToolStackHandler) stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    @Override
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, NBTTagCompound nbt) {
        return new ToolbeltCapabilityProvider(stack);
    }

    public void changeSelectedTool(int direction, ItemStack stack) {
        ToolStackHandler handler = getHandler(stack);
        if (direction > 0) handler.incrementSelectedSlot();
        else handler.decrementSelectedSlot();
        GregTechAPI.networkHandler.sendToServer(
                new PacketToolbeltSelectionChange(handler.selectedSlot == null ? -1 : handler.selectedSlot));
    }

    public void setSelectedTool(@Nullable Integer slot, ItemStack stack) {
        ToolStackHandler handler = getHandler(stack);
        if (slot == null || slot < 0 || slot >= handler.getSlots()) handler.selectedSlot = null;
        else handler.selectedSlot = slot;
    }

    @Override
    public @NotNull String getItemStackDisplayName(@NotNull ItemStack stack) {
        ItemStack tool = getHandler(stack).getSelectedStack();
        String selectedToolDisplay = "";
        if (tool != null) {
            selectedToolDisplay = " (" + tool.getDisplayName() + ")";
        }
        return LocalizationUtils.format(getTranslationKey(), getToolMaterial(stack).getLocalizedName(),
                selectedToolDisplay);
    }

    protected class ToolbeltCapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

        protected final Supplier<Integer> slotCountSupplier;

        private @Nullable ToolStackHandler handler;

        public ToolbeltCapabilityProvider(ItemStack stack) {
            slotCountSupplier = () -> (int) (ItemGTToolbelt.super.getTotalHarvestLevel(stack) * 5.4f);
        }

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
            return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) this.getHandler(0);
            else return null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return this.getHandler(0).serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            // make sure we can load all the slots, no matter what we're supposed to be limited to.
            int minsize = nbt.hasKey("Size") ? nbt.getInteger("Size") : 0;
            // .copy() prevents double damage ticks in singleplayer
            this.getHandler(minsize).deserializeNBT(nbt.copy());
        }

        protected ToolStackHandler getHandler(int minsize) {
            int slots = Math.max(slotCountSupplier.get(), minsize);
            if (handler == null || handler.getSlots() != slots) handler = new ToolStackHandler(slots);
            return handler;
        }
    }

    protected static class ToolStackHandler extends ItemStackHandler {

        private static final Set<String> EMPTY = ImmutableSet.of();

        private @Nullable Integer selectedSlot = null;

        protected final ItemTool[] tools = new ItemTool[this.getSlots()];
        protected final IGTTool[] gtTools = new IGTTool[this.getSlots()];
        protected final Set<String> toolClasses = new ObjectOpenHashSet<>();
        public final Set<String> oreDicts = new ObjectOpenHashSet<>();

        public ToolStackHandler(int size) {
            super(size);
        }

        public void incrementSelectedSlot() {
            for (int slot = (this.selectedSlot == null ? -1 : this.selectedSlot) + 1; slot != this.getSlots(); slot++) {
                if (this.getStackInSlot(slot).isEmpty()) continue;
                this.selectedSlot = slot;
                return;
            }
            this.selectedSlot = null;
        }

        public void decrementSelectedSlot() {
            for (int slot = (this.selectedSlot == null ? this.getSlots() : this.selectedSlot) - 1; slot != -1; slot--) {
                if (this.getStackInSlot(slot).isEmpty()) continue;
                this.selectedSlot = slot;
                return;
            }
            this.selectedSlot = null;
        }

        public @Nullable Integer getSelectedSlot() {
            return selectedSlot;
        }

        public @Nullable ItemStack getSelectedStack() {
            if (getSelectedSlot() == null) return null;
            else return this.stacks.get(getSelectedSlot());
        }

        public Set<String> getToolClasses(boolean defaultEmpty) {
            ItemStack selectedStack = getSelectedStack();
            if (selectedStack != null) {
                if (selectedStack.getItem() instanceof ItemTool tool) {
                    return tool.getToolClasses(selectedStack);
                } else if (selectedStack.getItem() instanceof IGTTool tool) {
                    return tool.getToolClasses(selectedStack);
                }
            }
            if (defaultEmpty) return EMPTY;
            return toolClasses;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            Item item = stack.getItem();
            if (item instanceof ItemGTToolbelt) return false;
            return item instanceof ItemTool || item instanceof IGTTool;
        }

        @Override
        protected void onContentsChanged(int slot) {
            this.updateSlot(slot);
            this.update();

            super.onContentsChanged(slot);
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = super.serializeNBT();
            if (this.selectedSlot != null) tag.setByte("SelectedSlot", this.selectedSlot.byteValue());
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            if (nbt.hasKey("SelectedSlot")) this.selectedSlot = (int) nbt.getByte("SelectedSlot");
        }

        @Override
        protected void onLoad() {
            super.onLoad();
            for (int i = 0; i < this.getSlots(); i++) {
                this.updateSlot(i);
            }
            this.update();
        }

        protected void updateSlot(int slot) {
            Item item = this.getStackInSlot(slot).getItem();
            if (item instanceof ItemTool tool) {
                tools[slot] = tool;
            } else {
                tools[slot] = null;
            }
            if (item instanceof IGTTool tool) {
                gtTools[slot] = tool;
            } else {
                gtTools[slot] = null;
            }
        }

        protected void update() {
            this.oreDicts.clear();
            Arrays.stream(gtTools).filter(Objects::nonNull).map(igtTool -> {
                Set<String> set = new ObjectOpenHashSet<>(igtTool.getSecondaryOreDicts());
                set.add(igtTool.getOreDictName());
                return set;
            }).forEach(this.oreDicts::addAll);
            this.oreDicts.retainAll(VALID_OREDICTS);

            this.toolClasses.clear();
            for (int i = 0; i < this.getSlots(); i++) {
                if (tools[i] != null) this.toolClasses.addAll(tools[i].getToolClasses(stacks.get(i)));
            }
        }

        public boolean checkMaintenanceAgainstTools(String toolClass, boolean doCraftingDamage,
                                                    @Nullable EntityPlayer entityPlayer) {
            for (int i = 0; i < this.getSlots(); i++) {
                ItemStack stack = this.getStackInSlot(i);
                if (ToolHelper.isTool(stack, toolClass)) {
                    if (doCraftingDamage) ToolHelper.damageItemWhenCrafting(stack, entityPlayer);
                    return true;
                }
            }
            return false;
        }

        public boolean checkIngredientAgainstTools(Ingredient ingredient, boolean doCraftingDamage) {
            for (int i = 0; i < this.getSlots(); i++) {
                ItemStack stack = this.getStackInSlot(i);
                if (ingredient.test(stack)) {
                    if (doCraftingDamage && stack.getItem().hasContainerItem(stack)) {
                        this.setStackInSlot(i, stack.getItem().getContainerItem(stack));
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
