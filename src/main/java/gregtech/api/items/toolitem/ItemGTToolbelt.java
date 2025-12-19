package gregtech.api.items.toolitem;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.items.IDyeableItem;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.items.behaviors.ColorSprayBehaviour;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMaintenanceHatch;
import gregtech.core.network.packets.PacketToolbeltSelectionChange;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static gregtech.api.items.toolitem.ToolHelper.MATERIAL_KEY;

public class ItemGTToolbelt extends ItemGTTool implements IDyeableItem {

    private static final ThreadLocal<Integer> lastSlot = ThreadLocal.withInitial(() -> -999);
    private static final ThreadLocal<EntityPlayerMP> lastPlayer = ThreadLocal.withInitial(() -> null);

    public ItemGTToolbelt(String domain, String id, Supplier<ItemStack> markerItem, IToolBehavior... behaviors) {
        super(domain, id, -1,
                new ToolDefinitionBuilder().behaviors(behaviors).cannotAttack().attackSpeed(-2.4F).build(),
                null, false, new HashSet<>(), "toolbelt", new ArrayList<>(),
                markerItem);
    }

    public int getSelectedSlot(@NotNull ItemStack toolbelt) {
        return getHandler(toolbelt).getSelectedSlot();
    }

    public int getSlotCount(@NotNull ItemStack toolbelt) {
        return getHandler(toolbelt).getSlots();
    }

    public @NotNull ItemStack getSelectedTool(@NotNull ItemStack toolbelt) {
        return getHandler(toolbelt).getSelectedStack();
    }

    @NotNull
    public ItemStack getToolInSlot(@NotNull ItemStack toolbelt, int slot) {
        ToolStackHandler handler = getHandler(toolbelt);
        if (slot < 0 || slot >= handler.getSlots()) return ItemStack.EMPTY;
        return handler.getStackInSlot(slot);
    }

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        final var usedStack = guiData.getUsedItemStack();
        final var handler = getHandler(usedStack);
        final var selected = handler.getSelectedStack();
        if (!selected.isEmpty() && selected.getItem() instanceof ItemUIFactory factory) {
            return factory.buildUI(guiData, guiSyncManager, settings);
        }

        int heightBonus = (handler.getSlots() / 9) * 18;

        SlotGroup group = new SlotGroup("toolbelt_inventory", Math.min(handler.getSlots(), 9));
        guiSyncManager.registerSlotGroup(group);

        List<ItemSlot> slots = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            slots.add(new ItemSlot());
        }

        return GTGuis.createPanel(usedStack.getTranslationKey(), 176, 120 + heightBonus + 12)
                .child(IKey.str(usedStack.getDisplayName()).asWidget()
                        .pos(5, 5)
                        .height(12))
                .child(new Grid()
                        .margin(0)
                        .leftRel(0.5f)
                        .top(7 + 12)
                        .coverChildren()
                        .mapTo(group.getRowSize(), slots, (index, value) -> value
                                .slot(SyncHandlers.itemSlot(handler, index)
                                        .slotGroup(group)
                                        .changeListener(
                                                (newItem, onlyAmountChanged, client, init) -> handler
                                                        .onContentsChanged(index)))
                                .background(GTGuiTextures.SLOT, GTGuiTextures.TOOL_SLOT_OVERLAY)
                                .name("slot_" + index))
                        .name("toolbelt_inventory"))
                .bindPlayerInventory();
    }

    @Override
    public @NotNull List<IToolBehavior> getBehaviors(ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected.isEmpty()) return super.getBehaviors(stack);
        else if (selected.getItem() instanceof IGTTool tool) {
            return tool.getBehaviors(selected);
        } else return Collections.emptyList();
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull IBlockState state) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().getDestroySpeed(selected, state);
        } else return definition$getDestroySpeed(stack, state);
    }

    @Override
    public boolean hitEntity(@NotNull ItemStack stack, @NotNull EntityLivingBase target,
                             @NotNull EntityLivingBase attacker) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().hitEntity(selected, target, attacker);
        } else return definition$hitEntity(stack, target, attacker);
    }

    @Override
    public boolean onBlockStartBreak(@NotNull ItemStack itemstack, @NotNull BlockPos pos,
                                     @NotNull EntityPlayer player) {
        ItemStack selected = getHandler(itemstack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().onBlockStartBreak(selected, pos, player);
        } else return definition$onBlockStartBreak(itemstack, pos, player);
    }

    @Override
    public boolean onBlockDestroyed(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull IBlockState state,
                                    @NotNull BlockPos pos, @NotNull EntityLivingBase entityLiving) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().onBlockDestroyed(selected, worldIn, state, pos, entityLiving);
        } else return definition$onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }

    @Override
    public boolean getIsRepairable(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        ItemStack selected = getHandler(toRepair).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().getIsRepairable(selected, repair);
        } else return definition$getIsRepairable(toRepair, repair);
    }

    @NotNull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@NotNull EntityEquipmentSlot slot,
                                                                     @NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().getAttributeModifiers(slot, selected);
        } else return definition$getAttributeModifiers(slot, stack);
    }

    @Override
    public int getHarvestLevel(@NotNull ItemStack stack, @NotNull String toolClass, @Nullable EntityPlayer player,
                               @Nullable IBlockState blockState) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().getHarvestLevel(stack, toolClass, player, blockState);
        } else return definition$getHarvestLevel(stack, toolClass, player, blockState);
    }

    @NotNull
    @Override
    public Set<String> getToolClasses(@NotNull ItemStack stack) {
        return getHandler(stack).getToolClasses(true);
    }

    @Override
    public boolean canDisableShield(@NotNull ItemStack stack, @NotNull ItemStack shield,
                                    @NotNull EntityLivingBase entity, @NotNull EntityLivingBase attacker) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().canDisableShield(selected, shield, entity, attacker);
        } else return definition$canDisableShield(stack, shield, entity, attacker);
    }

    @Override
    public boolean doesSneakBypassUse(@NotNull ItemStack stack, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                      @NotNull EntityPlayer player) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().doesSneakBypassUse(selected, world, pos, player);
        } else return definition$doesSneakBypassUse(stack, world, pos, player);
    }

    @Override
    public boolean onEntitySwing(@NotNull EntityLivingBase entityLiving, @NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().onEntitySwing(entityLiving, selected);
        } else return definition$onEntitySwing(entityLiving, stack);
    }

    @Override
    public boolean canDestroyBlockInCreative(@NotNull World world, @NotNull BlockPos pos, @NotNull ItemStack stack,
                                             @NotNull EntityPlayer player) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().canDestroyBlockInCreative(world, pos, selected, player);
        } else return definition$canDestroyBlockInCreative(world, pos, stack, player);
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack,
                                               boolean slotChanged) {
        return false;
    }

    @Override
    public int getMetadata(@NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().getMetadata(selected);
        } else return super.getMetadata(stack);
    }

    @Override
    public boolean isDamaged(@NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().isDamaged(selected);
        } else return definition$isDamaged(stack);
    }

    @Override
    public int getDamage(@NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().getDamage(selected);
        } else return definition$getDamage(stack);
    }

    @Override
    public int getMaxDamage(@NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().getMaxDamage(selected);
        } else return definition$getMaxDamage(stack);
    }

    @Override
    public void setDamage(@NotNull ItemStack stack, int damage) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            selected.getItem().setDamage(selected, damage);
        } else definition$setDamage(stack, damage);
    }

    @Override
    public double getDurabilityForDisplay(@NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            double dis = selected.getItem().getDurabilityForDisplay(selected);
            // vanillaesque tools need to be inverted
            if (selected.getItem() instanceof ItemTool) dis = 1 - dis;
            return dis;
        } else return definition$getDurabilityForDisplay(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        ToolStackHandler handler = getHandler(stack);
        ItemStack selected = handler.getSelectedStack();
        if (!selected.isEmpty()) {
            selected.getItem().addInformation(selected, world, tooltip, flag);
        } else {
            if (stack.getItemDamage() > 0) {
                int damageRemaining = this.getTotalMaxDurability(stack) - stack.getItemDamage() + 1;
                tooltip.add(I18n.format("item.gt.tool.tooltip.general_uses",
                        TextFormattingUtil.formatNumbers(damageRemaining)));
            }
            tooltip.add(I18n.format("item.gt.tool.toolbelt.size",
                    TextFormattingUtil.formatNumbers(handler.getSlots())));
            tooltip.add("");
            if (TooltipHelper.isShiftDown()) {
                tooltip.add(I18n.format("item.gt.tool.toolbelt.tooltip"));
                tooltip.add("");
                tooltip.add(I18n.format("item.gt.tool.toolbelt.paint"));
                tooltip.add("");
                tooltip.add(I18n.format("item.gt.tool.toolbelt.maintenance"));
            } else tooltip.add(I18n.format("gregtech.tooltip.hold_shift"));
        }
        if (TooltipHelper.isCtrlDown()) {
            tooltip.add("");
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack tool = handler.getStackInSlot(i);
                String name = tool.isEmpty() ? "x" : tool.getDisplayName();
                tooltip.add(I18n.format(
                        handler.selectedSlot == i ? "item.gt.tool.toolbelt.selected" : "item.gt.tool.toolbelt.tool",
                        i + 1, name));
            }
        } else tooltip.add(I18n.format("gregtech.tooltip.hold_ctrl"));
    }

    @Override
    public boolean canHarvestBlock(@NotNull IBlockState state, @NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (!selected.isEmpty()) {
            return selected.getItem().canHarvestBlock(state, selected);
        } else return ToolHelper.isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack));
    }

    @Override
    public void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        ItemStack selected = getHandler(wrench).getSelectedStack();
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool) {
            tool.wrenchUsed(player, hand, selected, rayTrace);
        } else super.wrenchUsed(player, hand, wrench, rayTrace);
    }

    @Override
    public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {
        ItemStack selected = getHandler(item).getSelectedStack();
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool) {
            tool.toolUsed(selected, user, pos);
        } else super.toolUsed(item, user, pos);
    }

    @Override
    public void used(@NotNull EnumHand hand, @NotNull EntityPlayer player, @NotNull BlockPos pos) {
        ItemStack selected = getHandler(player.getHeldItem(hand)).getSelectedStack();
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool) {
            tool.used(hand, player, pos);
        } else super.used(hand, player, pos);
    }

    @Override
    public boolean hasContainerItem(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull ItemStack getContainerItem(@NotNull ItemStack stack) {
        if (getHandler(stack).dealCraftDamageToSelected()) {
            return stack.copy();
        }
        return super.getContainerItem(stack);
    }

    public void setOnCraftIngredient(ItemStack stack, Ingredient ingredient) {
        int match = getHandler(stack).checkIngredientAgainstTools(ingredient);
        if (match != -1) {
            setSelectedTool(match, stack);
            PacketToolbeltSelectionChange.toClient(match,
                    lastSlot.get(), lastPlayer.get());
        }
    }

    public boolean damageAgainstMaintenanceProblem(ItemStack stack, String toolClass,
                                                   @Nullable EntityPlayer entityPlayer) {
        return getHandler(stack).checkMaintenanceAgainstTools(toolClass, true, entityPlayer);
    }

    public boolean supportsIngredient(ItemStack stack, Ingredient ingredient) {
        return getHandler(stack).checkIngredientAgainstTools(ingredient) != -1;
    }

    public boolean supportsTool(ItemStack stack, ItemStack tool) {
        return getHandler(stack).checkToolAgainstTools(tool) != -1;
    }

    private ToolStackHandler getHandler(ItemStack stack) {
        IItemHandler handler = stack.getCapability(GregtechCapabilities.CAPABILITY_TOOLBELT_HANDLER, null);
        if (handler instanceof ToolStackHandler h) return h;
        else return FALLBACK;
    }

    @Override
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, NBTTagCompound nbt) {
        return new ToolbeltCapabilityProvider(stack);
    }

    @SideOnly(Side.CLIENT)
    public void changeSelectedToolMousewheel(int direction, ItemStack stack) {
        ToolStackHandler handler = getHandler(stack);
        if (direction < 0) handler.incrementSelectedSlot();
        else handler.decrementSelectedSlot();
        PacketToolbeltSelectionChange.toServer(handler.selectedSlot);
    }

    @SideOnly(Side.CLIENT)
    public void changeSelectedToolHotkey(int slot, ItemStack stack) {
        ToolStackHandler handler = getHandler(stack);
        handler.setSelectedSlot(slot);
        PacketToolbeltSelectionChange.toServer(handler.selectedSlot);
    }

    /**
     * For use by {@link PacketToolbeltSelectionChange} only!
     */
    @ApiStatus.Internal
    public void setSelectedTool(int slot, ItemStack stack) {
        ToolStackHandler handler = getHandler(stack);
        if (slot < 0 || slot >= handler.getSlots())
            handler.selectedSlot = -1;
        else handler.selectedSlot = slot;
    }

    @Override
    public @NotNull String getItemStackDisplayName(@NotNull ItemStack stack) {
        ItemStack tool = getHandler(stack).getSelectedStack();
        getHandler(stack).disablePassthrough();
        String name;
        if (!tool.isEmpty()) {
            name = LocalizationUtils.format(getTranslationKey() + ".select", getToolMaterial(stack).getLocalizedName(),
                    tool.getDisplayName());
        } else {
            name = LocalizationUtils.format(getTranslationKey(), getToolMaterial(stack).getLocalizedName());
        }
        getHandler(stack).enablePassthrough();
        return name;
    }

    @Override
    public @NotNull EnumActionResult onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world,
                                                    @NotNull BlockPos pos, @NotNull EnumFacing side, float hitX,
                                                    float hitY, float hitZ, @NotNull EnumHand hand) {
        EnumActionResult result = IDyeableItem.super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
        if (result == EnumActionResult.PASS) {
            ItemStack stack = player.getHeldItem(hand);
            ToolStackHandler handler = getHandler(stack);
            if (handler.getSelectedStack().isEmpty() &&
                    world.getTileEntity(pos) instanceof MetaTileEntityHolder holder &&
                    holder.getMetaTileEntity() instanceof MetaTileEntityMaintenanceHatch maintenance) {
                maintenance.fixMaintenanceProblemsWithToolbelt(player, this, stack);
                return EnumActionResult.SUCCESS;
            }
            return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
        } else return result;
    }

    @Override
    public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World world,
                                               @NotNull BlockPos pos, @NotNull EnumHand hand,
                                               @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ToolStackHandler handler = getHandler(player.getHeldItem(hand));
        ItemStack selected = handler.getSelectedStack();
        if (!selected.isEmpty()) {
            ColorSprayBehaviour spray = ColorSprayBehaviour.getBehavior(selected);
            if (spray != null) {
                EnumActionResult result = spray.useFromToolbelt(player, world, pos, hand, facing, hitX, hitY, hitZ,
                        selected);
                if (result != EnumActionResult.PASS) return result;
            }
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return this.getColor(stack);
        }
        getHandler(stack).disablePassthrough();
        int color = super.getColor(stack, tintIndex);
        getHandler(stack).enablePassthrough();
        return color;
    }

    @Override
    public int getDefaultColor(ItemStack stack) {
        return 0xA06540;
    }

    @Override
    public boolean shouldGetContainerItem() {
        return false;
    }

    public static boolean checkIngredientAgainstToolbelt(@NotNull ItemStack input, @NotNull OreIngredient ingredient) {
        if (input.getItem() instanceof ItemGTToolbelt toolbelt) {
            if (toolbelt.supportsIngredient(input, ingredient)) {
                toolbelt.setOnCraftIngredient(input, ingredient);
                return true;
            }
        }
        return false;
    }

    public static void setCraftingSlot(int slot, EntityPlayerMP player) {
        lastSlot.set(slot);
        lastPlayer.set(player);
    }

    public static boolean checkToolAgainstToolbelt(@NotNull ItemStack toolbelt, @NotNull ItemStack tool) {
        if (toolbelt.getItem() instanceof ItemGTToolbelt belt && ToolHelper.isUtilityItem(tool)) {
            return belt.supportsTool(toolbelt, tool);
        }
        return false;
    }

    protected static class ToolbeltCapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

        protected final IntSupplier slotCountSupplier;

        private @Nullable ToolStackHandler handler;

        public ToolbeltCapabilityProvider(ItemStack stack) {
            slotCountSupplier = () -> {
                if (!ToolHelper.hasMaterial(stack)) return 4;
                NBTTagCompound toolTag = stack.getOrCreateSubCompound(ToolHelper.TOOL_TAG_KEY);
                Material material = GregTechAPI.materialManager.getMaterial(toolTag.getString(MATERIAL_KEY));
                if (material == null) return 4;
                ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);
                return Math.min(8, 2 + toolProperty.getToolHarvestLevel());
            };
        }

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
            if (capability == GregtechCapabilities.CAPABILITY_TOOLBELT_HANDLER)
                return true;
            ItemStack selected = getHandler().getSelectedStack();
            if (!selected.isEmpty()) {
                return selected.hasCapability(capability, facing);
            } else return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
        }

        @Override
        public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
            if (capability == GregtechCapabilities.CAPABILITY_TOOLBELT_HANDLER)
                return GregtechCapabilities.CAPABILITY_TOOLBELT_HANDLER.cast(this.getHandler());
            ItemStack selected = getHandler().getSelectedStack();
            if (!selected.isEmpty()) {
                return selected.getCapability(capability, facing);
            } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                // if nothing is selected, expose the handler under the item handler capability
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this.getHandler());
            } else return null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return this.getHandler().serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            // .copy() prevents double damage ticks in singleplayer
            this.getHandler().deserializeNBT(nbt.copy());
        }

        protected @NotNull ToolStackHandler getHandler() {
            int size = slotCountSupplier.getAsInt();
            if (handler == null) handler = new ToolStackHandler(size);
            else if (handler.getSlots() != size) handler.setSize(size);
            return handler;
        }
    }

    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        if (stack.getTagCompound() != null) {
            tag.setTag("NBT", stack.getTagCompound());
        }
        tag.setTag("Cap", getHandler(stack).serializeNBT());
        return tag;
    }

    @Override
    public void readNBTShareTag(ItemStack stack, NBTTagCompound nbt) {
        // cap syncing is handled separately, we only need it on the share tag so that changes are detected properly.
        stack.setTagCompound(nbt == null ? null : (nbt.hasKey("NBT") ? nbt.getCompoundTag("NBT") : null));
    }

    protected static final ToolStackHandler FALLBACK = new ToolStackHandler(0);

    public static class ToolStackHandler extends ItemStackHandler {

        private static final Set<String> EMPTY = ImmutableSet.of();

        private @Range(from = -1, to = 128) int selectedSlot = -1;

        protected ItemTool[] tools;
        protected IGTTool[] gtTools;
        protected final Set<String> toolClasses = new ObjectOpenHashSet<>();

        private boolean passthrough = true;

        public ToolStackHandler(int size) {
            setSize(size);
        }

        public void incrementSelectedSlot() {
            if ((this.selectedSlot += 1) >= this.getSlots()) this.selectedSlot = -1;
        }

        public void decrementSelectedSlot() {
            if ((this.selectedSlot -= 1) < -1) this.selectedSlot = this.getSlots() - 1;
        }

        public int getSelectedSlot() {
            if (passthrough) return selectedSlot;
            else return -1;
        }

        public void setSelectedSlot(int selectedSlot) {
            if (selectedSlot >= getSlots() || selectedSlot < 0) this.selectedSlot = -1;
            else this.selectedSlot = selectedSlot;
        }

        public void enablePassthrough() {
            this.passthrough = true;
        }

        public void disablePassthrough() {
            this.passthrough = false;
        }

        public @NotNull ItemStack getSelectedStack() {
            if (getSelectedSlot() == -1) return ItemStack.EMPTY;
            else return this.getStackInSlot(getSelectedSlot());
        }

        public Set<String> getToolClasses(boolean defaultEmpty) {
            ItemStack selectedStack = getSelectedStack();
            if (!selectedStack.isEmpty()) {
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
            return ToolHelper.isUtilityItem(stack);
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
            if (this.selectedSlot != -1) tag.setByte("SelectedSlot", (byte) this.selectedSlot);
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            if (nbt.hasKey("SelectedSlot")) this.selectedSlot = nbt.getByte("SelectedSlot");
        }

        @Override
        public void setSize(int size) {
            super.setSize(size);
            this.gtTools = new IGTTool[size];
            this.tools = new ItemTool[size];
        }

        @Override
        protected void onLoad() {
            for (int i = 0; i < getSlots(); i++) {
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
            this.toolClasses.clear();
            for (int i = 0; i < getSlots(); i++) {
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

        public int checkIngredientAgainstTools(Ingredient ingredient) {
            for (int i = 0; i < this.getSlots(); i++) {
                ItemStack stack = this.getStackInSlot(i);
                if (ingredient.test(stack)) {
                    return i;
                }
            }
            return -1;
        }

        public void dealCraftDamageToSlot(int slot) {
            ItemStack stack = this.getStackInSlot(slot);
            this.setStackInSlot(slot, stack.getItem().getContainerItem(stack));
        }

        public boolean dealCraftDamageToSelected() {
            if (selectedSlot != -1) {
                dealCraftDamageToSlot(selectedSlot);
                return true;
            } else return false;
        }

        public int checkToolAgainstTools(ItemStack tool) {
            for (int i = 0; i < this.getSlots(); i++) {
                ItemStack stack = this.getStackInSlot(i);
                if (OreDictionary.itemMatches(stack, tool, false)) {
                    return i;
                }
            }
            return -1;
        }
    }
}
