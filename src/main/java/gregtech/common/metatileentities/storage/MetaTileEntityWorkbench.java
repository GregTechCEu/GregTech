package gregtech.common.metatileentities.storage;

import gregtech.api.capability.GregtechDataCodes;
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

import net.minecraft.client.renderer.GlStateManager;
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
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Grid;
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
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class MetaTileEntityWorkbench extends MetaTileEntity {

    // todo move these to GregtechDataCodes
    public static final int UPDATE_CLIENT_HANDLER = GregtechDataCodes.assignId();

    private static final IDrawable CHEST = new ItemDrawable(new ItemStack(Blocks.CHEST))
            .asIcon().size(16);

    private final IDrawable WORKSTATION = new ItemDrawable(getStackForm())
            .asIcon().size(16);

    private final ItemStackHandler craftingGrid = new SingleItemStackHandler(9);
    private final ItemStackHandler internalInventory = new GTItemStackHandler(this, 18) {

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            var logic = getCraftingRecipeLogic();
            if (logic.isValid() && logic.getSyncManager().isClient())
                logic.syncToServer(4, buffer -> buffer.writeVarInt(slot));
        }
    };
    private final ItemStackHandler toolInventory = new ToolItemStackHandler(9) {

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            var logic = getCraftingRecipeLogic();
            if (logic.isValid() && logic.getSyncManager().isClient())
                logic.syncToServer(4, buffer -> buffer.writeVarInt(slot));
        }
    };

    private IItemHandlerModifiable combinedInventory;
    private IItemHandlerModifiable connectedInventory;

    private final CraftingRecipeMemory recipeMemory = new CraftingRecipeMemory(this.craftingGrid, 9);
    private CraftingRecipeLogic recipeLogic = null;
    private int itemsCrafted = 0;

    public MetaTileEntityWorkbench(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

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

    public IItemHandlerModifiable getAvailableHandlers() {
        var handlers = new ArrayList<IItemHandler>();
        for (var facing : EnumFacing.VALUES) {
            var neighbor = getNeighbor(facing);
            if (neighbor == null) continue;
            var handler = neighbor.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
            if (handler != null) handlers.add(handler);
        }
        this.connectedInventory = new HandlerListWrapper(handlers);
        handlers.clear();

        handlers.add(this.internalInventory);
        handlers.add(this.toolInventory);
        handlers.add(this.connectedInventory);
        return this.combinedInventory = new HandlerListWrapper(handlers);
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
        getCraftingRecipeLogic().updateInventory(getAvailableHandlers());
        writeCustomData(UPDATE_CLIENT_HANDLER, this::sendHandlerToClient);
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

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, GuiSyncManager guiSyncManager) {
        getCraftingRecipeLogic().updateCurrentRecipe();
        // if (!guiSyncManager.isClient()) {
        // writeCustomData(UPDATE_CLIENT_STACKS, getCraftingRecipeLogic()::writeAvailableStacks);
        // }

        guiSyncManager.syncValue("recipe_logic", this.recipeLogic);

        var controller = new PagedWidget.Controller();

        return GTGuis.createPanel(this, 176, 224)
                .child(new Row()
                        .debugName("tab row")
                        .widthRel(1f)
                        .leftRel(0.5f)
                        .margin(3, 0)
                        .coverChildrenHeight()
                        .topRel(0f, 3, 1f)
                        .child(new PageButton(0, controller)
                                .tab(GuiTextures.TAB_TOP, 0)
                                .overlay(WORKSTATION))
                        .child(new PageButton(1, controller)
                                .tab(GuiTextures.TAB_TOP, 0)
                                .overlay(CHEST)))
                .child(IKey.lang(getMetaFullName())
                        .asWidget()
                        .top(7).left(7))
                .child(new PagedWidget<>()
                        .top(22)
                        .margin(7)
                        .widthRel(0.9f)
                        .controller(controller)
                        // workstation page
                        .addPage(new Column()
                                .debugName("crafting page")
                                .coverChildrenWidth()
                                .child(new Row()
                                        .debugName("crafting row")
                                        .coverChildrenHeight()
                                        .widthRel(1f)
                                        // crafting grid
                                        .child(createCraftingGrid())
                                        // crafting output slot
                                        .child(createCraftingOutput(guiData, guiSyncManager))
                                        // recipe memory
                                        .child(createRecipeMemoryGrid(guiSyncManager)))
                                // tool inventory
                                .child(createToolInventory(guiSyncManager))
                                // internal inventory
                                .child(createInternalInventory(guiSyncManager)))
                        // storage page
                        .addPage(createInventoryPage(guiSyncManager)))
                .bindPlayerInventory();
    }

    public IWidget createToolInventory(GuiSyncManager syncManager) {
        var toolSlots = new SlotGroup("tool_slots", 9, true);
        syncManager.registerSlotGroup(toolSlots);

        return SlotGroupWidget.builder()
                .row("XXXXXXXXX")
                .key('X', i -> new ItemSlot()
                        .background(GTGuiTextures.SLOT, GTGuiTextures.INGOT_OVERLAY)
                        .slot(SyncHandlers.itemSlot(this.toolInventory, i)
                                .slotGroup(toolSlots)))
                .build().marginTop(2);
    }

    public IWidget createInternalInventory(GuiSyncManager syncManager) {
        var inventory = new SlotGroup("inventory", 9, true);
        syncManager.registerSlotGroup(inventory);

        return SlotGroupWidget.builder()
                .row("XXXXXXXXX")
                .row("XXXXXXXXX")
                .key('X', i -> new ItemSlot()
                        .slot(SyncHandlers.itemSlot(this.internalInventory, i)
                                .slotGroup(inventory)))
                .build().marginTop(2);
    }

    public IWidget createCraftingGrid() {
        return SlotGroupWidget.builder()
                .matrix("XXX", "XXX", "XXX")
                .key('X', i -> new ItemSlot()
                        .slot(SyncHandlers.phantomItemSlot(this.craftingGrid, i)
                                .changeListener(
                                        (newItem, onlyAmountChanged, client, init) -> {
                                            if (!init) {
                                                this.recipeLogic.updateCurrentRecipe();
                                            }
                                        })))
                .build();
    }

    public IWidget createCraftingOutput(PosGuiData guiData, GuiSyncManager syncManager) {
        var amountCrafted = new IntSyncValue(this::getItemsCrafted, this::setItemsCrafted);
        syncManager.syncValue("amount_crafted", amountCrafted);
        amountCrafted.updateCacheFromSource(true);

        return new Column()
                .size(54)
                .child(new ItemSlot().marginTop(18)
                        // todo figure this shit (recipe output slot) out
                        .slot(new CraftingOutputSlot(new InventoryWrapper(
                                this.recipeLogic.getCraftingResultInventory(),
                                guiData.getPlayer()), amountCrafted, getCraftingRecipeLogic()))
                        .background(GTGuiTextures.SLOT.asIcon().size(22))
                        .marginBottom(4))
                .child(IKey.dynamic(amountCrafted::getStringValue)
                        .alignment(Alignment.Center)
                        .asWidget().widthRel(1f));
    }

    public IWidget createRecipeMemoryGrid(GuiSyncManager syncManager) {
        return SlotGroupWidget.builder()
                .matrix("XXX",
                        "XXX",
                        "XXX")
                .key('X', i -> new RecipeMemorySlot(this.recipeMemory, i))
                .build().right(0);
    }

    public IWidget createInventoryPage(GuiSyncManager syncManager) {
        var connected = new SlotGroup("connected_inventory", 8, true);
        syncManager.registerSlotGroup(connected);

        // todo this needs to handle when inventories are removed/added
        List<IWidget> list = new ArrayList<>(this.connectedInventory.getSlots());
        Predicate<ItemSlot> checkSlotValid = itemSlot -> {
            int slot = itemSlot.getSlot().getSlotIndex();
            return slot < this.connectedInventory.getSlots();
        };

        if (this.connectedInventory.getSlots() == 0) {
            return new ParentWidget<>();
        }

        for (int i = 0; i < this.connectedInventory.getSlots(); i++) {
            // todo maybe show what inventory a slot belongs to?
            var widget = new ItemSlot()
                    .setEnabledIf(checkSlotValid)
                    .slot(SyncHandlers.itemSlot(this.connectedInventory, i)
                            .slotGroup(connected));
            widget.setEnabled(checkSlotValid.test(widget));
            list.add(widget);
        }
        return new Column()
                .debugName("inventory page")
                .padding(2)
                .leftRel(0.5f)
                .coverChildren()
                .background(GTGuiTextures.DISPLAY)
                .child(new Grid()
                        .scrollable(new VerticalScrollData(), null)
                        .coverChildrenWidth()
                        .height(18 * 6)
                        .minElementMargin(0, 0)
                        .mapTo(8, list, (index, value) -> value));
    }

    public void sendHandlerToClient(PacketBuffer buffer) {
        buffer.writeVarInt(this.connectedInventory.getSlots());
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_CLIENT_HANDLER) {
            int connected = buf.readVarInt();

            // check if sizes have changed, and keep any existing items

            // set connected inventory
            this.connectedInventory = new ItemStackHandler(connected);

            // set combined inventory
            this.combinedInventory = new ItemHandlerList(
                    Arrays.asList(this.connectedInventory, this.internalInventory));

            getCraftingRecipeLogic()
                    .updateInventory(this.combinedInventory);
        }
    }

    public int getItemsCrafted() {
        return this.itemsCrafted;
    }

    public void setItemsCrafted(int itemsCrafted) {
        this.itemsCrafted = itemsCrafted;
    }

    private static class RecipeMemorySlot extends Widget<RecipeMemorySlot> implements Interactable {

        private final CraftingRecipeMemory memory;
        private final int index;

        public RecipeMemorySlot(CraftingRecipeMemory memory, int index) {
            this.memory = memory;
            this.index = index;
        }

        @Override
        public void onInit() {
            size(ItemSlot.SIZE);
            background(GTGuiTextures.SLOT);
        }

        @Override
        public void draw(GuiContext context, WidgetTheme widgetTheme) {
            drawStack();
        }

        public void drawStack() {
            GuiScreenWrapper guiScreen = getScreen().getScreenWrapper();
            var recipe = memory.getRecipeAtIndex(index);
            if (recipe == null) return;
            ItemStack itemstack = recipe.getRecipeResult();

            guiScreen.setZ(100f);
            guiScreen.getItemRenderer().zLevel = 100.0F;

            // GuiDraw.drawRect(1, 1, 16, 16, -2130706433);

            if (!itemstack.isEmpty()) {
                GlStateManager.enableDepth();
                // render the item itself
                guiScreen.getItemRenderer().renderItemAndEffectIntoGUI(guiScreen.mc.player, itemstack, 1, 1);

                // render the amount overlay
                // String amountText = NumberFormat.formatWithMaxDigits(1);
                // textRenderer.setShadow(true);
                // textRenderer.setColor(Color.WHITE.main);
                // textRenderer.setAlignment(Alignment.BottomRight, getArea().width - 1, getArea().height - 1);
                // textRenderer.setPos(1, 1);
                // GlStateManager.disableLighting();
                // GlStateManager.disableDepth();
                // GlStateManager.disableBlend();
                // textRenderer.draw(amountText);
                // GlStateManager.enableLighting();
                // GlStateManager.enableDepth();
                // GlStateManager.enableBlend();

                int cachedCount = itemstack.getCount();
                itemstack.setCount(1); // required to not render the amount overlay
                // render other overlays like durability bar
                guiScreen.getItemRenderer().renderItemOverlayIntoGUI(guiScreen.getFontRenderer(), itemstack, 1, 1,
                        null);
                itemstack.setCount(cachedCount);
                GlStateManager.disableDepth();
            }

            guiScreen.getItemRenderer().zLevel = 0.0F;
            guiScreen.setZ(0f);
        }

        @NotNull
        @Override
        public Result onMousePressed(int mouseButton) {
            var data = MouseData.create(mouseButton);
            if (data.shift && data.mouseButton == 0 && memory.hasRecipe(index)) {
                var recipe = memory.getRecipeAtIndex(index);
                recipe.setRecipeLocked(!recipe.isRecipeLocked());
            } else if (data.mouseButton == 0) {
                memory.loadRecipe(index);
            } else if (data.mouseButton == 1) {
                if (memory.hasRecipe(index) && !memory.getRecipeAtIndex(index).isRecipeLocked())
                    memory.removeRecipe(index);
            }
            return Result.ACCEPT;
        }
    }

    private class CraftingOutputSlot extends ModularSlot {

        IntSyncValue syncValue;
        private final CraftingRecipeLogic recipeLogic;

        public CraftingOutputSlot(IItemHandler itemHandler, IntSyncValue syncValue, CraftingRecipeLogic recipeLogic) {
            super(itemHandler, 0, false);
            this.syncValue = syncValue;
            this.recipeLogic = recipeLogic;
        }

        @Override
        public boolean canTakeStack(EntityPlayer playerIn) {
            if (recipeLogic.getSyncManager().isClient()) {
                // recipeLogic.syncToServer(3);
                return false;
            }

            if (recipeLogic.isRecipeValid())
                recipeLogic.collectAvailableItems();

            return recipeLogic.attemptMatchRecipe();
        }

        @Override
        public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack) {
            recipeLogic.performRecipe();
            handleItemCraft(stack, thePlayer);
            return super.onTake(thePlayer, stack);
        }

        @Override
        public void putStack(@NotNull ItemStack stack) {
            super.putStack(getStack());
        }

        @Override
        public @NotNull ItemStack decrStackSize(int amount) {
            return getStack();
        }

        public void handleItemCraft(ItemStack itemStack, EntityPlayer player) {
            itemStack.onCrafting(player.world, player, 1);

            var inventoryCrafting = recipeLogic.getCraftingMatrix();

            // if we're not simulated, fire the event, unlock recipe and add crafted items, and play sounds
            FMLCommonHandler.instance().firePlayerCraftingEvent(player, itemStack, inventoryCrafting);

            var cachedRecipe = recipeLogic.getCachedRecipe();
            if (cachedRecipe != null && !cachedRecipe.isDynamic()) {
                player.unlockRecipes(Lists.newArrayList(cachedRecipe));
            }
            if (cachedRecipe != null) {
                ItemStack resultStack = cachedRecipe.getCraftingResult(inventoryCrafting);
                this.syncValue.setValue(this.syncValue.getValue() + resultStack.getCount(), true, true);
                // itemsCrafted += resultStack.getCount();
                recipeMemory.notifyRecipePerformed(craftingGrid, resultStack);
            }
            // call method from recipe logic to sync to client
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

    private static class HandlerListWrapper extends ItemHandlerList {

        public HandlerListWrapper(List<? extends IItemHandler> itemHandlerList) {
            super(itemHandlerList);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (validSlot(slot))
                return super.insertItem(slot, stack, simulate);

            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (validSlot(slot))
                return super.extractItem(slot, amount, simulate);

            return ItemStack.EMPTY;
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            if (validSlot(slot))
                super.setStackInSlot(slot, stack);
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (validSlot(slot))
                return super.getStackInSlot(slot);

            return ItemStack.EMPTY;
        }

        private boolean validSlot(int slot) {
            return slot >= 0 || slot < this.getSlots();
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.workbench.tooltip1"));
        tooltip.add(I18n.format("gregtech.machine.workbench.tooltip2"));
    }

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
