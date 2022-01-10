package gregtech.common.metatileentities.storage;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.base.Preconditions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.Widget.ClickData;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.*;
import gregtech.api.gui.widgets.TabGroup.TabLocation;
import gregtech.api.gui.widgets.tab.ItemTabInfo;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.storage.ICraftingStorage;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Position;
import gregtech.common.gui.widget.craftingstation.CraftingSlotWidget;
import gregtech.common.gui.widget.craftingstation.ItemListGridWidget;
import gregtech.common.gui.widget.craftingstation.MemorizedRecipeWidget;
import gregtech.common.inventory.IItemList;
import gregtech.common.inventory.handlers.SingleItemStackHandler;
import gregtech.common.inventory.handlers.ToolItemStackHandler;
import gregtech.common.inventory.itemsource.ItemSourceList;
import gregtech.common.inventory.itemsource.sources.InventoryItemSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MetaTileEntityWorkbench extends MetaTileEntity implements ICraftingStorage {
    private final ItemStackHandler internalInventory = new ItemStackHandler(18);
    private final ItemStackHandler craftingGrid = new SingleItemStackHandler(9);
    private final ItemStackHandler toolInventory = new ToolItemStackHandler(9);

    private final CraftingRecipeMemory recipeMemory = new CraftingRecipeMemory(9);
    private CraftingRecipeResolver recipeResolver = null;
    private int itemsCrafted = 0;

    public MetaTileEntityWorkbench(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public static AbstractWidgetGroup createWorkbenchTab(CraftingRecipeResolver recipeResolver, ItemStackHandler craftingGrid, CraftingRecipeMemory recipeMemory,
                                                        ItemStackHandler toolInventory, ItemStackHandler internalInventory) {
        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.addWidget(new ImageWidget(88 - 13, 44 - 14, 26, 26, GuiTextures.SLOT));
        widgetGroup.addWidget(new CraftingSlotWidget(recipeResolver, 0, 88 - 9, 44 - 9));

        //crafting grid
        widgetGroup.addWidget(new CraftingStationInputWidgetGroup(4, 7, craftingGrid, recipeResolver));

        Supplier<String> textSupplier = () -> Integer.toString(recipeResolver.getItemsCrafted());
        widgetGroup.addWidget(new SimpleTextWidget(88, 44 + 19, "", textSupplier));

        Consumer<ClickData> clearAction = (clickData) -> recipeResolver.clearCraftingGrid();
        widgetGroup.addWidget(new ClickButtonWidget(8 + 18 * 3 + 3, 16, 8, 8, "", clearAction).setButtonTexture(GuiTextures.BUTTON_CLEAR_GRID));

        widgetGroup.addWidget(new ImageWidget(168 - 18 * 3, 44 - 19 * 3 / 2, 18 * 3, 18 * 3, TextureArea.fullImage("textures/gui/base/darkened_slot.png")));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                widgetGroup.addWidget(new MemorizedRecipeWidget(recipeMemory, j + i * 3, craftingGrid, 168 - 18 * 3 / 2 - 27 + j * 18, 44 - 28 + i * 18));
            }
        }
        //tool inventory
        for (int i = 0; i < 9; i++) {
            widgetGroup.addWidget(new SlotWidget(toolInventory, i, 7 + i * 18, 75).setBackgroundTexture(GuiTextures.SLOT, GuiTextures.TOOL_SLOT_OVERLAY));
        }
        //internal inventory
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 9; ++j) {
                widgetGroup.addWidget(new SlotWidget(internalInventory, j + i * 9, 7 + j * 18, 98 + i * 18).setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        return widgetGroup;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityWorkbench(metaTileEntityId);
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
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
        data.setInteger("ItemsCrafted", recipeResolver == null ? itemsCrafted : recipeResolver.getItemsCrafted());
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

    private void createRecipeResolver() {
        this.recipeResolver = new CraftingRecipeResolver(getWorld(), craftingGrid, recipeMemory);
        this.recipeResolver.setItemsCrafted(itemsCrafted);
        ItemSourceList itemSourceList = this.recipeResolver.getItemSourceList();
        itemSourceList.addItemHandler(InventoryItemSource.direct(getWorld(), toolInventory, -2));
        itemSourceList.addItemHandler(InventoryItemSource.direct(getWorld(), internalInventory, -1));
        this.recipeResolver.checkNeighbourInventories(getPos());
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && recipeResolver == null) {
            createRecipeResolver();
        }
        if (!getWorld().isRemote) {
            getRecipeResolver().update();
        }
    }

    private CraftingRecipeResolver getRecipeResolver() {
        Preconditions.checkState(getWorld() != null, "getRecipeResolver called too early");
        return recipeResolver;
    }

    @Override
    public void onNeighborChanged() {
        super.onNeighborChanged();
        if (recipeResolver != null) {
            this.recipeResolver.checkNeighbourInventories(getPos());
        }
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, internalInventory);
        clearInventory(itemBuffer, toolInventory);
    }

    private AbstractWidgetGroup createItemListTab() {
        WidgetGroup widgetGroup = new WidgetGroup();
        widgetGroup.addWidget(new LabelWidget(5, 20, "gregtech.machine.workbench.storage_note_1"));
        widgetGroup.addWidget(new LabelWidget(5, 30, "gregtech.machine.workbench.storage_note_2"));
        CraftingRecipeResolver recipeResolver = getRecipeResolver();
        IItemList itemList = recipeResolver == null ? null : recipeResolver.getItemSourceList();
        widgetGroup.addWidget(new ItemListGridWidget(11, 45, 8, 5, itemList));
        return widgetGroup;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 221)
                .bindPlayerInventory(entityPlayer.inventory, 138);
        builder.label(5, 5, getMetaFullName());

        TabGroup<AbstractWidgetGroup> tabGroup = new TabGroup<>(TabLocation.HORIZONTAL_TOP_LEFT, Position.ORIGIN);
        tabGroup.addTab(new ItemTabInfo("gregtech.machine.workbench.tab.workbench", new ItemStack(Blocks.CRAFTING_TABLE)), createWorkbenchTab(getRecipeResolver(), craftingGrid, recipeMemory, toolInventory, internalInventory));
        tabGroup.addTab(new ItemTabInfo("gregtech.machine.workbench.tab.item_list", new ItemStack(Blocks.CHEST)), createItemListTab());
        builder.widget(tabGroup);

        return builder.build(getHolder(), entityPlayer);
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
}
