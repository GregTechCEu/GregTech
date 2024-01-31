package gregtech.common.metatileentities.storage;

import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.inventory.handlers.SingleItemStackHandler;
import gregtech.common.inventory.handlers.ToolItemStackHandler;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityWorkbench extends MetaTileEntity {

    private final ItemStackHandler internalInventory = new GTItemStackHandler(this, 18);
    private final ItemStackHandler craftingGrid = new SingleItemStackHandler(9);
    private final ItemStackHandler toolInventory = new ToolItemStackHandler(9);

    private IItemHandler combinedInventory;

    private final CraftingRecipeMemory recipeMemory = new CraftingRecipeMemory(9);
    private CraftingRecipeLogic recipeLogic = null;
    private int itemsCrafted = 0;

    public MetaTileEntityWorkbench(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

//    public static gregtech.api.gui.widgets.AbstractWidgetGroup createWorkbenchTab(CraftingRecipeLogic craftingRecipeLogic,
//                                                         ItemStackHandler craftingGrid,
//                                                         CraftingRecipeMemory recipeMemory,
//                                                         ItemStackHandler toolInventory,
//                                                         ItemStackHandler internalInventory) {
//        gregtech.api.gui.widgets.WidgetGroup widgetGroup = new gregtech.api.gui.widgets.WidgetGroup();
//        widgetGroup.addWidget(new gregtech.api.gui.widgets.ImageWidget(88 - 13, 44 - 14, 26, 26, gregtech.api.gui.GuiTextures.SLOT));
//        widgetGroup.addWidget(new gregtech.common.gui.widget.craftingstation.CraftingSlotWidget(craftingRecipeLogic, 0, 88 - 9, 44 - 9));
//
//        // crafting grid
//        widgetGroup.addWidget(new gregtech.api.gui.widgets.CraftingStationInputWidgetGroup(4, 7, craftingGrid, craftingRecipeLogic));
//
//        Supplier<String> textSupplier = () -> Integer.toString(craftingRecipeLogic.getItemsCraftedAmount());
//        widgetGroup.addWidget(new gregtech.api.gui.widgets.SimpleTextWidget(88, 44 + 19, "", textSupplier));
//
//        Consumer<gregtech.api.gui.Widget.ClickData> clearAction = (clickData) -> craftingRecipeLogic.clearCraftingGrid();
//        widgetGroup.addWidget(new gregtech.api.gui.widgets.ClickButtonWidget(8 + 18 * 3 + 3, 16, 8, 8, "", clearAction)
//                .setButtonTexture(gregtech.api.gui.GuiTextures.BUTTON_CLEAR_GRID));
//
//        widgetGroup.addWidget(new gregtech.api.gui.widgets.ImageWidget(168 - 18 * 3, 44 - 19 * 3 / 2, 18 * 3, 18 * 3,
//                gregtech.api.gui.resources.TextureArea.fullImage("textures/gui/base/darkened_slot.png")));
//        for (int i = 0; i < 3; ++i) {
//            for (int j = 0; j < 3; ++j) {
//                widgetGroup.addWidget(new gregtech.common.gui.widget.craftingstation.MemorizedRecipeWidget(recipeMemory, j + i * 3, craftingGrid,
//                        168 - 18 * 3 / 2 - 27 + j * 18, 44 - 28 + i * 18));
//            }
//        }
//        // tool inventory
//        for (int i = 0; i < 9; i++) {
//            widgetGroup.addWidget(new gregtech.api.gui.widgets.SlotWidget(toolInventory, i, 7 + i * 18, 75)
//                    .setBackgroundTexture(gregtech.api.gui.GuiTextures.SLOT, gregtech.api.gui.GuiTextures.TOOL_SLOT_OVERLAY));
//        }
//        // internal inventory
//        for (int i = 0; i < 2; ++i) {
//            for (int j = 0; j < 9; ++j) {
//                widgetGroup.addWidget(new gregtech.api.gui.widgets.SlotWidget(internalInventory, j + i * 9, 7 + j * 18, 98 + i * 18)
//                        .setBackgroundTexture(gregtech.api.gui.GuiTextures.SLOT));
//            }
//        }
//        return widgetGroup;
//    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityWorkbench(metaTileEntityId);
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.CRAFTING_TABLE.getParticleSprite(), getDefaultPaintingColor());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        int paintingColor = getPaintingColorForRendering();
        pipeline = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(paintingColor)));
        Textures.CRAFTING_TABLE.renderOriented(renderState, translation, pipeline, getFrontFacing());
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.itemsCrafted);
        for (int i = 0; i < craftingGrid.getSlots(); i++) {
            buf.writeItemStack(craftingGrid.getStackInSlot(i));
        }
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.itemsCrafted = buf.readInt();
        try {
            for (int i = 0; i < craftingGrid.getSlots(); i++) {
                craftingGrid.setStackInSlot(i, buf.readItemStack());
            }
        } catch (IOException ignored) {}
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("CraftingGridInventory", craftingGrid.serializeNBT());
        data.setTag("ToolInventory", toolInventory.serializeNBT());
        data.setTag("InternalInventory", internalInventory.serializeNBT());
        data.setInteger("ItemsCrafted", itemsCrafted);
        data.setTag("RecipeMemory", recipeMemory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.craftingGrid.deserializeNBT(data.getCompoundTag("CraftingGridInventory"));
        this.toolInventory.deserializeNBT(data.getCompoundTag("ToolInventory"));
        this.internalInventory.deserializeNBT(data.getCompoundTag("InternalInventory"));
        this.itemsCrafted = data.getInteger("ItemsCrafted");
        this.recipeMemory.deserializeNBT(data.getCompoundTag("RecipeMemory"));
    }

    public IItemHandler getAvailableHandlers() {
        var handlers = new ArrayList<IItemHandler>();
        for (var facing : EnumFacing.VALUES) {
            var neighbor = getNeighbor(facing);
            if (neighbor == null) continue;
            var handler = neighbor.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
            if (handler != null) handlers.add(handler);
        }
        handlers.add(this.internalInventory);
        handlers.add(this.toolInventory);
        this.combinedInventory = new ItemHandlerList(handlers);
        return this.combinedInventory;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            if (recipeLogic != null) {
                getCraftingRecipeLogic().update();
            }
        }
    }

    @Override
    public void onNeighborChanged() {
        this.recipeLogic.updateInventory(getAvailableHandlers());
    }

    private @NotNull CraftingRecipeLogic getCraftingRecipeLogic() {
        Preconditions.checkState(getWorld() != null, "getRecipeResolver called too early");
        if (this.recipeLogic == null) {
            this.recipeLogic = new CraftingRecipeLogic(getWorld(), getAvailableHandlers(), getCraftingGrid());
        }
        return this.recipeLogic;
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, internalInventory);
        clearInventory(itemBuffer, toolInventory);
    }

//    private gregtech.api.gui.widgets.AbstractWidgetGroup createItemListTab() {
//        gregtech.api.gui.widgets.WidgetGroup widgetGroup = new gregtech.api.gui.widgets.WidgetGroup();
//        widgetGroup.addWidget(new gregtech.api.gui.widgets.LabelWidget(5, 20, "gregtech.machine.workbench.storage_note_1"));
//        widgetGroup.addWidget(new gregtech.api.gui.widgets.LabelWidget(5, 30, "gregtech.machine.workbench.storage_note_2"));
//        CraftingRecipeLogic recipeResolver = getCraftingRecipeLogic();
//        IItemList itemList = recipeResolver == null ? null : recipeResolver.getItemSourceList();
//        widgetGroup.addWidget(new gregtech.common.gui.widget.craftingstation.ItemListGridWidget(11, 45, 8, 5, itemList));
//        return widgetGroup;
//    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, GuiSyncManager guiSyncManager) {
        final String nineSlot = "XXXXXXXXX";
        final String[] craftingGrid = new String[] {"XXX", "XXX", "XXX"};
        final char key = 'X';

        var toolSlots = new SlotGroup("tool_slots", 9, true);
        var inventory = new SlotGroup("inventory", 9, true);
        guiSyncManager.registerSlotGroup(toolSlots);
        guiSyncManager.registerSlotGroup(inventory);

        getCraftingRecipeLogic().updateCurrentRecipe();

        var amountCrafted = new IntSyncValue(this::getItemsCrafted, this::setItemsCrafted);
        guiSyncManager.syncValue("amount_crafted", amountCrafted);
        guiSyncManager.syncValue("recipe_logic", this.recipeLogic);
        amountCrafted.updateCacheFromSource(true);

        var controller = new PagedWidget.Controller();

        return GTGuis.createPanel(this, 176, 224)
                .child(new Row().widthRel(1f)
                        .leftRel(0.5f)
                        .margin(3, 0)
                        .coverChildrenHeight()
                        .topRel(0f, 3, 1f)
                        .child(new PageButton(0, controller)
                                .tab(GuiTextures.TAB_TOP, 0)
                                .overlay(new ItemDrawable(getStackForm())
                                        .asIcon().size(16)))
                        .child(new PageButton(1, controller)
                                .tab(GuiTextures.TAB_TOP, 0)
                                .overlay(new ItemDrawable(new ItemStack(Blocks.CHEST))
                                        .asIcon().size(16))))
                .child(new PagedWidget<>()
                        .top(7)
                        .margin(7)
                        .expanded()
                        .controller(controller)
                        .addPage(new Column()
                                .coverChildren()
                                .child(new Row().coverChildrenHeight()
                                        .widthRel(1f)
                                        .marginBottom(2)
                                        //todo
                                        // make JEI transfer work correctly
                                        // currently it's not possible due to getCurrent() in ModularScreen returning null
                                        .child(SlotGroupWidget.builder()
                                                .matrix(craftingGrid)
                                                .key(key, i -> new ItemSlot()
                                                        .slot(SyncHandlers.phantomItemSlot(this.craftingGrid, i)
                                                                .changeListener((newItem, onlyAmountChanged, client, init) -> {
                                                                    if (!init) {
                                                                        this.recipeLogic.updateCurrentRecipe();
                                                                    }
                                                                })))
                                                .build())
                                        .child(new Column()
                                                .size(54)
                                                .child(new ItemSlot().marginTop(18)
                                                        // todo figure this shit (recipe output slot) out
                                                        .slot(new CraftingOutputSlot(new InventoryWrapper(
                                                                this.recipeLogic.getCraftingResultInventory(),
                                                                guiData.getPlayer()), amountCrafted))
                                                        .background(GTGuiTextures.SLOT.asIcon().size(22))
                                                        .marginBottom(4))
                                                .child(IKey.dynamic(amountCrafted::getStringValue)
                                                        .alignment(Alignment.Center)
                                                        .asWidget().width(22)))
                                        .child(SlotGroupWidget.builder()
                                                .matrix(craftingGrid)
                                                .key(key, i -> new ItemSlot()
                                                        // todo recipe memory
                                                        .slot(SyncHandlers.phantomItemSlot(new ItemStackHandler(9), i)))
                                                .build().right(0)))
                                .child(SlotGroupWidget.builder()
                                        .row(nineSlot)
                                        .key(key, i -> new ItemSlot()
                                                .background(GTGuiTextures.SLOT, GTGuiTextures.INGOT_OVERLAY)
                                                .slot(SyncHandlers.itemSlot(this.toolInventory, i)
                                                        .slotGroup(toolSlots)))
                                        .build().marginBottom(2))
                                .child(SlotGroupWidget.builder()
                                        .row(nineSlot)
                                        .row(nineSlot)
                                        .key(key, i -> new ItemSlot()
                                                .slot(SyncHandlers.itemSlot(this.internalInventory, i)
                                                        .slotGroup(inventory)))
                                        .build()))
                        .addPage(new Column().coverChildren()
                                .child(IKey.str("add storage things").asWidget())))
                .bindPlayerInventory();
    }

    public int getItemsCrafted() {
        return this.itemsCrafted;
    }

    public void setItemsCrafted(int itemsCrafted) {
        this.itemsCrafted = itemsCrafted;
    }

    @Override
    protected boolean createTransferableScreen() {
        return true;
    }

    private class CraftingOutputSlot extends ModularSlot {
        IntSyncValue syncValue;

        public CraftingOutputSlot(IItemHandler itemHandler, IntSyncValue syncValue) {
            super(itemHandler, 0, false);
            this.syncValue = syncValue;
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            return recipeLogic.performRecipe(playerIn);
        }

        @Override
        public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
            handleItemCraft(stack, thePlayer);
            return super.onTake(thePlayer, stack);
        }

        @Override
        public void putStack(@NotNull ItemStack stack) {
            super.putStack(stack);
        }

        public void handleItemCraft(ItemStack itemStack, EntityPlayer player) {
            itemStack.onCrafting(getWorld(), player, 1);

            var inventoryCrafting = recipeLogic.getCraftingMatrix();

            // if we're not simulated, fire the event, unlock recipe and add crafted items, and play sounds
            FMLCommonHandler.instance().firePlayerCraftingEvent(player, itemStack, inventoryCrafting);

            var cachedRecipe = recipeLogic.getCachedRecipe();
            if (cachedRecipe != null && !cachedRecipe.isDynamic()) {
                player.unlockRecipes(Lists.newArrayList(cachedRecipe));
            }
            if (cachedRecipe != null) {
                ItemStack resultStack = cachedRecipe.getCraftingResult(inventoryCrafting);
                this.syncValue.setValue(this.syncValue.getValue() + resultStack.getCount(), true, false);
//                itemsCrafted += resultStack.getCount();
                recipeMemory.notifyRecipePerformed(craftingGrid, resultStack);
            }
        }
    }

    private class InventoryWrapper implements IItemHandlerModifiable {

        IInventory inventory;
        EntityPlayer player;

        private InventoryWrapper(IInventory inventory, EntityPlayer player) {
            this.inventory = inventory;
            this.player = player;
        }

        @Override
        public int getSlots() {
            return inventory.getSizeInventory();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(slot).copy();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getInventoryStackLimit();
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (!recipeLogic.isRecipeValid()) {
                inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
            }

            if (!stack.isEmpty())
                inventory.setInventorySlotContents(slot, stack);
        }
    }

//    @Override
//    protected gregtech.api.gui.ModularUI createUI(EntityPlayer entityPlayer) {
//
//        gregtech.api.gui.ModularUI.Builder builder = gregtech.api.gui.ModularUI.builder(gregtech.api.gui.GuiTextures.BACKGROUND, 176, 221)
//                .bindPlayerInventory(entityPlayer.inventory, 138);
//        builder.label(5, 5, getMetaFullName());
//
//        gregtech.api.gui.widgets.TabGroup<gregtech.api.gui.widgets.AbstractWidgetGroup> tabGroup = new gregtech.api.gui.widgets.TabGroup<>(
//                gregtech.api.gui.widgets.TabGroup.TabLocation.HORIZONTAL_TOP_LEFT, Position.ORIGIN);
//        tabGroup.addTab(new gregtech.api.gui.widgets.tab.ItemTabInfo("gregtech.machine.workbench.tab.workbench",
//                new ItemStack(Blocks.CRAFTING_TABLE)),
//                createWorkbenchTab(recipeLogic, craftingGrid, recipeMemory, toolInventory, internalInventory));
//        tabGroup.addTab(new gregtech.api.gui.widgets.tab.ItemTabInfo("gregtech.machine.workbench.tab.item_list",
//                new ItemStack(Blocks.CHEST)), createItemListTab());
//        builder.widget(tabGroup);
//        builder.bindCloseListener(() -> discardRecipeResolver(entityPlayer));
//
//        return builder.build(getHolder(), entityPlayer);
//    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.workbench.tooltip1"));
        tooltip.add(I18n.format("gregtech.machine.workbench.tooltip2"));
    }

//    public void discardRecipeResolver(EntityPlayer entityPlayer) {
//        this.listeners.remove(entityPlayer);
//        if (listeners.isEmpty()) {
//            if (recipeLogic != null) {
//                itemsCrafted = recipeLogic.getItemsCraftedAmount();
//                this.markDirty();
//            }
//            recipeLogic = null;
//        }
//    }

    public ItemStackHandler getCraftingGrid() {
        return craftingGrid;
    }

    public ItemStackHandler getToolInventory() {
        return toolInventory;
    }

    public CraftingRecipeMemory getRecipeMemory() {
        return recipeMemory;
    }

    @Override
    public boolean canPlaceCoverOnSide(@NotNull EnumFacing side) {
        return false;
    }

    @Override
    public boolean acceptsCovers() {
        return false;
    }

    @Override
    public boolean canRenderMachineGrid(@NotNull ItemStack mainHandStack, @NotNull ItemStack offHandStack) {
        return false;
    }

    @Override
    public boolean showToolUsages() {
        return false;
    }
}
