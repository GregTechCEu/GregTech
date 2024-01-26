package gregtech.common.metatileentities.storage;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.Widget.ClickData;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.CraftingStationInputWidgetGroup;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TabGroup;
import gregtech.api.gui.widgets.TabGroup.TabLocation;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.gui.widgets.tab.ItemTabInfo;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.storage.ICraftingStorage;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Position;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.inventory.IItemList;
import gregtech.common.inventory.handlers.SingleItemStackHandler;
import gregtech.common.inventory.handlers.ToolItemStackHandler;
import gregtech.common.inventory.itemsource.ItemSources;
import gregtech.common.inventory.itemsource.sources.InventoryItemSource;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MetaTileEntityWorkbench extends MetaTileEntity implements ICraftingStorage {

    private final ItemStackHandler internalInventory = new GTItemStackHandler(this, 18);
    private final ItemStackHandler craftingGrid = new SingleItemStackHandler(9);
    private final ItemStackHandler toolInventory = new ToolItemStackHandler(9);

    private final CraftingRecipeMemory recipeMemory = new CraftingRecipeMemory(9);
    private CraftingRecipeLogic recipeLogic = null;
    private int itemsCrafted = 0;

    private final ArrayList<EntityPlayer> listeners = new ArrayList<>();

    public MetaTileEntityWorkbench(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public static gregtech.api.gui.widgets.AbstractWidgetGroup createWorkbenchTab(CraftingRecipeLogic craftingRecipeLogic,
                                                         ItemStackHandler craftingGrid,
                                                         CraftingRecipeMemory recipeMemory,
                                                         ItemStackHandler toolInventory,
                                                         ItemStackHandler internalInventory) {
        gregtech.api.gui.widgets.WidgetGroup widgetGroup = new gregtech.api.gui.widgets.WidgetGroup();
        widgetGroup.addWidget(new gregtech.api.gui.widgets.ImageWidget(88 - 13, 44 - 14, 26, 26, gregtech.api.gui.GuiTextures.SLOT));
        widgetGroup.addWidget(new gregtech.common.gui.widget.craftingstation.CraftingSlotWidget(craftingRecipeLogic, 0, 88 - 9, 44 - 9));

        // crafting grid
        widgetGroup.addWidget(new gregtech.api.gui.widgets.CraftingStationInputWidgetGroup(4, 7, craftingGrid, craftingRecipeLogic));

        Supplier<String> textSupplier = () -> Integer.toString(craftingRecipeLogic.getItemsCraftedAmount());
        widgetGroup.addWidget(new gregtech.api.gui.widgets.SimpleTextWidget(88, 44 + 19, "", textSupplier));

        Consumer<gregtech.api.gui.Widget.ClickData> clearAction = (clickData) -> craftingRecipeLogic.clearCraftingGrid();
        widgetGroup.addWidget(new gregtech.api.gui.widgets.ClickButtonWidget(8 + 18 * 3 + 3, 16, 8, 8, "", clearAction)
                .setButtonTexture(gregtech.api.gui.GuiTextures.BUTTON_CLEAR_GRID));

        widgetGroup.addWidget(new gregtech.api.gui.widgets.ImageWidget(168 - 18 * 3, 44 - 19 * 3 / 2, 18 * 3, 18 * 3,
                gregtech.api.gui.resources.TextureArea.fullImage("textures/gui/base/darkened_slot.png")));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                widgetGroup.addWidget(new gregtech.common.gui.widget.craftingstation.MemorizedRecipeWidget(recipeMemory, j + i * 3, craftingGrid,
                        168 - 18 * 3 / 2 - 27 + j * 18, 44 - 28 + i * 18));
            }
        }
        // tool inventory
        for (int i = 0; i < 9; i++) {
            widgetGroup.addWidget(new gregtech.api.gui.widgets.SlotWidget(toolInventory, i, 7 + i * 18, 75)
                    .setBackgroundTexture(gregtech.api.gui.GuiTextures.SLOT, gregtech.api.gui.GuiTextures.TOOL_SLOT_OVERLAY));
        }
        // internal inventory
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 9; ++j) {
                widgetGroup.addWidget(new gregtech.api.gui.widgets.SlotWidget(internalInventory, j + i * 9, 7 + j * 18, 98 + i * 18)
                        .setBackgroundTexture(gregtech.api.gui.GuiTextures.SLOT));
            }
        }
        return widgetGroup;
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
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("CraftingGridInventory", craftingGrid.serializeNBT());
        data.setTag("ToolInventory", toolInventory.serializeNBT());
        data.setTag("InternalInventory", internalInventory.serializeNBT());
        data.setInteger("ItemsCrafted", recipeLogic == null ? itemsCrafted : recipeLogic.getItemsCraftedAmount());
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

    private void createCraftingRecipeLogic(EntityPlayer entityPlayer) {
        if (!getWorld().isRemote) {
            if (recipeLogic == null) {
                this.recipeLogic = new CraftingRecipeLogic(this);
                this.recipeLogic.setItemsCraftedAmount(itemsCrafted);
                ItemSources itemSources = this.recipeLogic.getItemSourceList();
                itemSources.addItemHandler(new InventoryItemSource(getWorld(), toolInventory, -2));
                itemSources.addItemHandler(new InventoryItemSource(getWorld(), internalInventory, -1));
                this.recipeLogic.checkNeighbourInventories(getPos());
            }
            this.listeners.add(entityPlayer);
        }
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

    private CraftingRecipeLogic getCraftingRecipeLogic() {
        Preconditions.checkState(getWorld() != null, "getRecipeResolver called too early");
        return recipeLogic;
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, internalInventory);
        clearInventory(itemBuffer, toolInventory);
    }

    private gregtech.api.gui.widgets.AbstractWidgetGroup createItemListTab() {
        gregtech.api.gui.widgets.WidgetGroup widgetGroup = new gregtech.api.gui.widgets.WidgetGroup();
        widgetGroup.addWidget(new gregtech.api.gui.widgets.LabelWidget(5, 20, "gregtech.machine.workbench.storage_note_1"));
        widgetGroup.addWidget(new gregtech.api.gui.widgets.LabelWidget(5, 30, "gregtech.machine.workbench.storage_note_2"));
        CraftingRecipeLogic recipeResolver = getCraftingRecipeLogic();
        IItemList itemList = recipeResolver == null ? null : recipeResolver.getItemSourceList();
        widgetGroup.addWidget(new gregtech.common.gui.widget.craftingstation.ItemListGridWidget(11, 45, 8, 5, itemList));
        return widgetGroup;
    }

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

        var controller = new PagedWidget.Controller();

        return GTGuis.createPanel(this, 176, 224)
                .child(new Row()
                        .coverChildren()
                        .topRel(0f, 4, 1f)
                        .child(new PageButton(0, controller)
                                .tab(com.cleanroommc.modularui.drawable.GuiTextures.TAB_TOP, -1))
                        .child(new PageButton(1, controller)
                                .tab(com.cleanroommc.modularui.drawable.GuiTextures.TAB_TOP, 0)))
                .child(new PagedWidget<>()
                        .top(7).leftRel(0.5f)
                        .coverChildren()
                        .controller(controller)
                        .addPage(new Column().coverChildren()
                                .child(new Row().coverChildrenHeight()
                                        .widthRel(1f)
                                        .marginBottom(2)
                                        .child(SlotGroupWidget.builder()
                                                .matrix(craftingGrid)
                                                .key(key, i -> new ItemSlot()
                                                        .slot(SyncHandlers.phantomItemSlot(this.craftingGrid, i)))
                                                .build())
                                        .child(new ItemSlot()
                                                // todo figure this shit (recipe output slot) out
                                                .slot(new ItemStackHandler(1), 0)
                                                .background(GTGuiTextures.SLOT.asIcon().size(22))
                                                .align(Alignment.Center))
                                        .child(SlotGroupWidget.builder()
                                                .matrix(craftingGrid)
                                                .key(key, i -> new ItemSlot()
                                                        // todo recipe memory
                                                        .slot(SyncHandlers.phantomItemSlot(new ItemStackHandler(9), i)))
                                                .build()
                                                .right(0)))
                                .child(SlotGroupWidget.builder()
                                        .row(nineSlot)
                                        .key(key, i -> new ItemSlot()
                                                .overlay(GTGuiTextures.INGOT_OVERLAY)
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
                        .addPage(new Column()
                                .child(IKey.str("add storage things").asWidget())))
                .bindPlayerInventory(7);
    }

    @Override
    protected gregtech.api.gui.ModularUI createUI(EntityPlayer entityPlayer) {
        createCraftingRecipeLogic(entityPlayer);

        gregtech.api.gui.ModularUI.Builder builder = gregtech.api.gui.ModularUI.builder(gregtech.api.gui.GuiTextures.BACKGROUND, 176, 221)
                .bindPlayerInventory(entityPlayer.inventory, 138);
        builder.label(5, 5, getMetaFullName());

        gregtech.api.gui.widgets.TabGroup<gregtech.api.gui.widgets.AbstractWidgetGroup> tabGroup = new gregtech.api.gui.widgets.TabGroup<>(
                gregtech.api.gui.widgets.TabGroup.TabLocation.HORIZONTAL_TOP_LEFT, Position.ORIGIN);
        tabGroup.addTab(new gregtech.api.gui.widgets.tab.ItemTabInfo("gregtech.machine.workbench.tab.workbench",
                new ItemStack(Blocks.CRAFTING_TABLE)),
                createWorkbenchTab(recipeLogic, craftingGrid, recipeMemory, toolInventory, internalInventory));
        tabGroup.addTab(new gregtech.api.gui.widgets.tab.ItemTabInfo("gregtech.machine.workbench.tab.item_list",
                new ItemStack(Blocks.CHEST)), createItemListTab());
        builder.widget(tabGroup);
        builder.bindCloseListener(() -> discardRecipeResolver(entityPlayer));

        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.workbench.tooltip1"));
        tooltip.add(I18n.format("gregtech.machine.workbench.tooltip2"));
    }

    public void discardRecipeResolver(EntityPlayer entityPlayer) {
        this.listeners.remove(entityPlayer);
        if (listeners.isEmpty()) {
            if (!getWorld().isRemote && recipeLogic != null) {
                itemsCrafted = recipeLogic.getItemsCraftedAmount();
                this.markDirty();
            }
            recipeLogic = null;
        }
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
