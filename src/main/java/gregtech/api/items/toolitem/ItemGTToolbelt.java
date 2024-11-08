package gregtech.api.items.toolitem;

import gregtech.api.GregTechAPI;
import gregtech.api.items.IDyeableItem;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.util.LocalizationUtils;
import gregtech.common.items.behaviors.ColorSprayBehaviour;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMaintenanceHatch;
import gregtech.core.network.packets.PacketToolbeltSelectionChange;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static gregtech.api.items.toolitem.ToolHelper.MATERIAL_KEY;

public class ItemGTToolbelt extends ItemGTTool implements IDyeableItem {

    protected final static Set<String> VALID_OREDICTS = new ObjectOpenHashSet<>();

    public ItemGTToolbelt(String domain, String id, Supplier<ItemStack> markerItem, IToolBehavior... behaviors) {
        super(domain, id, -1,
                new ToolDefinitionBuilder().behaviors(behaviors).cannotAttack().attackSpeed(-2.4F).build(),
                null, false, new HashSet<>(), "toolbelt", new ArrayList<>(),
                markerItem);
    }

    public @Nullable Integer getSelectedSlot(@NotNull ItemStack toolbelt) {
        return getHandler(toolbelt).getSelectedSlot();
    }

    public int getSlotCount(@NotNull ItemStack toolbelt) {
        return getHandler(toolbelt).getSlots();
    }

    public @Nullable ItemStack getSelectedTool(@NotNull ItemStack toolbelt) {
        return getHandler(toolbelt).getSelectedStack();
    }

    public @Nullable ItemStack getToolInSlot(@NotNull ItemStack toolbelt, int slot) {
        ToolStackHandler handler = getHandler(toolbelt);
        if (slot < 0 || slot >= handler.getSlots()) return null;
        return handler.getStackInSlot(slot);
    }

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager) {
        ToolStackHandler handler = getHandler(guiData.getUsedItemStack());
        ItemStack selected = handler.getSelectedStack();
        if (selected != null && selected.getItem() instanceof ItemUIFactory factory) {
            return factory.buildUI(guiData, guiSyncManager);
        }

        int heightBonus = (handler.getSlots() / 9) * 18;

        ModularPanel panel = GTGuis.createPanel(guiData.getUsedItemStack().getDisplayName(), 176, 120 + heightBonus);

        SlotGroup group = new SlotGroup("toolbelt_inventory", 9);
        guiSyncManager.registerSlotGroup(group);

        SlotGroupWidget slotGroupWidget = new SlotGroupWidget();
        slotGroupWidget.flex().coverChildren().leftRel(0.5f).top(7);
        slotGroupWidget.debugName("toolbelt_inventory");
        for (int i = 0; i < handler.getSlots(); i++) {
            int finalI = i;
            slotGroupWidget.child(new ItemSlot()
                    .slot(SyncHandlers.itemSlot(handler, i).slotGroup(group)
                            .changeListener(
                                    (newItem, onlyAmountChanged, client, init) -> handler.onContentsChanged(finalI)))
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
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull IBlockState state) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().getDestroySpeed(selected, state);
        } else return definition$getDestroySpeed(stack, state);
    }

    @Override
    public boolean hitEntity(@NotNull ItemStack stack, @NotNull EntityLivingBase target,
                             @NotNull EntityLivingBase attacker) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().hitEntity(selected, target, attacker);
        } else return definition$hitEntity(stack, target, attacker);
    }

    @Override
    public boolean onBlockStartBreak(@NotNull ItemStack itemstack, @NotNull BlockPos pos,
                                     @NotNull EntityPlayer player) {
        ItemStack selected = getHandler(itemstack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().onBlockStartBreak(selected, pos, player);
        } else return definition$onBlockStartBreak(itemstack, pos, player);
    }

    @Override
    public boolean onBlockDestroyed(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull IBlockState state,
                                    @NotNull BlockPos pos, @NotNull EntityLivingBase entityLiving) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().onBlockDestroyed(selected, worldIn, state, pos, entityLiving);
        } else return definition$onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }

    @Override
    public boolean getIsRepairable(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        // I think this lets repairs go through to the selected tool, in combination with the setDamage passthroughs?
        // Idk testing required.
        ItemStack selected = getHandler(toRepair).getSelectedStack();
        if (selected != null) {
            return selected.getItem().getIsRepairable(selected, repair);
        } else return definition$getIsRepairable(toRepair, repair);
    }

    @NotNull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@NotNull EntityEquipmentSlot slot,
                                                                     @NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().getAttributeModifiers(slot, selected);
        } else return definition$getAttributeModifiers(slot, stack);
    }

    @Override
    public int getHarvestLevel(@NotNull ItemStack stack, @NotNull String toolClass, @Nullable EntityPlayer player,
                               @Nullable IBlockState blockState) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().getHarvestLevel(stack, toolClass, player, blockState);
        } else return super.getHarvestLevel(stack, toolClass, player, blockState);
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
        if (selected != null) {
            return selected.getItem().canDisableShield(selected, shield, entity, attacker);
        } else return definition$canDisableShield(stack, shield, entity, attacker);
    }

    @Override
    public boolean doesSneakBypassUse(@NotNull ItemStack stack, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                      @NotNull EntityPlayer player) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().doesSneakBypassUse(selected, world, pos, player);
        } else return definition$doesSneakBypassUse(stack, world, pos, player);
    }

    @Override
    public boolean onEntitySwing(@NotNull EntityLivingBase entityLiving, @NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().onEntitySwing(entityLiving, selected);
        } else return definition$onEntitySwing(entityLiving, stack);
    }

    @Override
    public boolean canDestroyBlockInCreative(@NotNull World world, @NotNull BlockPos pos, @NotNull ItemStack stack,
                                             @NotNull EntityPlayer player) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().canDestroyBlockInCreative(world, pos, selected, player);
        } else return definition$canDestroyBlockInCreative(world, pos, stack, player);
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack,
                                               boolean slotChanged) {
        return false;
    }

    @Override
    public boolean isDamaged(@NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().isDamaged(selected);
        } else return definition$isDamaged(stack);
    }

    @Override
    public int getDamage(@NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().getDamage(selected);
        } else return super.getDamage(stack);
    }

    @Override
    public int getMaxDamage(@NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().getMaxDamage(selected);
        } else return definition$getMaxDamage(stack);
    }

    @Override
    public void setDamage(@NotNull ItemStack stack, int damage) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            selected.getItem().setDamage(selected, damage);
        } else super.setDamage(stack, damage);
    }

    @Override
    public double getDurabilityForDisplay(@NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
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
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            selected.getItem().addInformation(selected, world, tooltip, flag);
        } else definition$addInformation(stack, world, tooltip, flag);
    }

    @Override
    public boolean canHarvestBlock(@NotNull IBlockState state, @NotNull ItemStack stack) {
        ItemStack selected = getHandler(stack).getSelectedStack();
        if (selected != null) {
            return selected.getItem().canHarvestBlock(state, selected);
        } else return ToolHelper.isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack));
    }

    @Override
    public void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        ItemStack selected = getHandler(wrench).getSelectedStack();
        if (selected != null && selected.getItem() instanceof IGTTool tool) {
            tool.wrenchUsed(player, hand, selected, rayTrace);
        } else super.wrenchUsed(player, hand, wrench, rayTrace);
    }

    @Override
    public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {
        ItemStack selected = getHandler(item).getSelectedStack();
        if (selected != null && selected.getItem() instanceof IGTTool tool) {
            tool.toolUsed(selected, user, pos);
        } else super.toolUsed(item, user, pos);
    }

    @Override
    public void used(@NotNull EnumHand hand, @NotNull EntityPlayer player, @NotNull BlockPos pos) {
        ItemStack selected = getHandler(player.getHeldItem(hand)).getSelectedStack();
        if (selected != null && selected.getItem() instanceof IGTTool tool) {
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
            stack = stack.copy();
            return stack;
        }
        return super.getContainerItem(stack);
    }

    public void setOnCraftIngredient(ItemStack stack, Ingredient ingredient) {
        Integer match = getHandler(stack).checkIngredientAgainstTools(ingredient);
        if (match != null) {
            setSelectedTool(match, stack);
        }
    }

    public boolean damageAgainstMaintenanceProblem(ItemStack stack, String toolClass,
                                                   @Nullable EntityPlayer entityPlayer) {
        return getHandler(stack).checkMaintenanceAgainstTools(toolClass, true, entityPlayer);
    }

    public boolean supportsIngredient(ItemStack stack, Ingredient ingredient) {
        return getHandler(stack).checkIngredientAgainstTools(ingredient) != null;
    }

    public boolean supportsTool(ItemStack stack, ItemStack tool) {
        return getHandler(stack).checkToolAgainstTools(tool) != null;
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
        if (slot == null || slot < 0 || slot >= handler.getSlots() || handler.getStackInSlot(slot).isEmpty())
            handler.selectedSlot = null;
        else handler.selectedSlot = slot;
    }

    @Override
    public @NotNull String getItemStackDisplayName(@NotNull ItemStack stack) {
        ItemStack tool = getHandler(stack).getSelectedStack();
        String selectedToolDisplay = "";
        if (tool != null) {
            selectedToolDisplay = " (" + tool.getDisplayName() + ")";
        }
        getHandler(stack).disablePassthrough();
        String name = LocalizationUtils.format(getTranslationKey(), getToolMaterial(stack).getLocalizedName(),
                selectedToolDisplay);
        getHandler(stack).enablePassthrough();
        return name;
    }

    @Override
    public @NotNull EnumActionResult onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world,
                                                    @NotNull BlockPos pos, @NotNull EnumFacing side, float hitX,
                                                    float hitY, float hitZ, @NotNull EnumHand hand) {
        EnumActionResult result = IDyeableItem.super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
        if (result == EnumActionResult.PASS) {
            ToolStackHandler handler = getHandler(player.getHeldItem(hand));
            if (handler.getSelectedSlot() == null && world.getTileEntity(pos) instanceof MetaTileEntityHolder holder &&
                    holder.getMetaTileEntity() instanceof MetaTileEntityMaintenanceHatch maintenance) {
                maintenance.fixMaintenanceProblems(player);
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
        if (selected != null) {
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

    public static boolean checkToolAgainstToolbelt(@NotNull ItemStack toolbelt, @NotNull ItemStack tool) {
        if (toolbelt.getItem() instanceof ItemGTToolbelt belt && ToolHelper.isUtilityItem(tool)) {
            return belt.supportsTool(toolbelt, tool);
        }
        return false;
    }

    protected static class ToolbeltCapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

        protected final Supplier<Integer> slotCountSupplier;

        private @Nullable ToolStackHandler handler;

        public ToolbeltCapabilityProvider(ItemStack stack) {
            slotCountSupplier = () -> {
                NBTTagCompound toolTag = stack.getOrCreateSubCompound(ToolHelper.TOOL_TAG_KEY);
                String string = toolTag.getString(MATERIAL_KEY);
                Material material = GregTechAPI.materialManager.getMaterial(string);
                if (material == null) {
                    toolTag.setString(MATERIAL_KEY, (material = Materials.Iron).getRegistryName());
                }
                ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);
                return Math.min(8, 2 + (toolProperty == null ? 0 : toolProperty.getToolHarvestLevel()));
            };
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

    protected static class ToolStackHandler extends ItemStackHandler {

        private Ingredient nextCraftIngredient;

        private static final Set<String> EMPTY = ImmutableSet.of();

        private @Nullable Integer selectedSlot = null;

        protected final ItemTool[] tools = new ItemTool[this.getSlots()];
        protected final IGTTool[] gtTools = new IGTTool[this.getSlots()];
        protected final Set<String> toolClasses = new ObjectOpenHashSet<>();
        public final Set<String> oreDicts = new ObjectOpenHashSet<>();

        private boolean passthrough = true;

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
            if (passthrough) return selectedSlot;
            else return null;
        }

        public void enablePassthrough() {
            this.passthrough = true;
        }

        public void disablePassthrough() {
            this.passthrough = false;
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
            return ToolHelper.isUtilityItem(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (this.selectedSlot != null && this.selectedSlot == slot) this.selectedSlot = null;
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

        @Nullable
        public Integer checkIngredientAgainstTools(Ingredient ingredient) {
            for (int i = 0; i < this.getSlots(); i++) {
                ItemStack stack = this.getStackInSlot(i);
                if (ingredient.test(stack)) {
                    return i;
                }
            }
            return null;
        }

        public void dealCraftDamageToSlot(int slot) {
            ItemStack stack = this.getStackInSlot(slot);
            this.setStackInSlot(slot, stack.getItem().getContainerItem(stack));
        }

        public boolean dealCraftDamageToSelected() {
            if (selectedSlot != null) {
                dealCraftDamageToSlot(selectedSlot);
                return true;
            } else return false;
        }

        @Nullable
        public Integer checkToolAgainstTools(ItemStack tool) {
            for (int i = 0; i < this.getSlots(); i++) {
                ItemStack stack = this.getStackInSlot(i);
                if (OreDictionary.itemMatches(stack, tool, false)) {
                    return i;
                }
            }
            return null;
        }
    }
}
