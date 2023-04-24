package gregtech.common.metatileentities.storage;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.sync.GuiSyncHandler;
import com.cleanroommc.modularui.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.layout.Grid;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityCrate extends MetaTileEntity {

    private final Material material;
    private final int inventorySize;
    private final int rowSize;
    private ItemStackHandler inventory;

    public MetaTileEntityCrate(ResourceLocation metaTileEntityId, Material material, int inventorySize, int rowSize) {
        super(metaTileEntityId);
        if (inventorySize % rowSize != 0) {
            throw new IllegalArgumentException("Row size should chosen so that all rows are filled! Currently the last row would only have " + inventorySize % rowSize + " of " + rowSize + " slots.");
        }
        this.material = material;
        this.inventorySize = inventorySize;
        this.rowSize = rowSize;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCrate(metaTileEntityId, material, inventorySize, rowSize);
    }

    @Override
    public boolean hasFrontFacing() {
        return false;
    }

    @Override
    public int getLightOpacity() {
        return 1;
    }

    @Override
    public String getHarvestTool() {
        return ModHandler.isMaterialWood(material) ? ToolClasses.AXE : ToolClasses.WRENCH;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.inventory = new ItemStackHandler(inventorySize);
        this.itemInventory = inventory;
    }

    @Override
    public int getActualComparatorValue() {
        return ItemHandlerHelper.calcRedstoneFromInventory(inventory);
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        clearInventory(itemBuffer, inventory);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        if (ModHandler.isMaterialWood(material)) {
            return Pair.of(Textures.WOODEN_CRATE.getParticleTexture(), getPaintingColorForRendering());
        } else {
            int color = ColourRGBA.multiply(
                    GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()),
                    GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            color = GTUtility.convertOpaqueRGBA_CLtoRGB(color);
            return Pair.of(Textures.METAL_CRATE.getParticleTexture(), color);
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if (material.toString().contains("wood")) {
            Textures.WOODEN_CRATE.render(renderState, translation, GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()), pipeline);
        } else {
            int baseColor = ColourRGBA.multiply(GTUtility.convertRGBtoOpaqueRGBA_CL(material.getMaterialRGB()), GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            Textures.METAL_CRATE.render(renderState, translation, baseColor, pipeline);
        }
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int factor = inventorySize / 9 > 8 ? 18 : 9;
        Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176 + (factor == 18 ? 176 : 0), 8 + inventorySize / factor * 18 + 104).label(5, 5, getMetaFullName());
        for (int i = 0; i < inventorySize; i++) {
            builder.slot(inventory, i, 7 * (factor == 18 ? 2 : 1) + i % factor * 18, 18 + i / factor * 18, GuiTextures.SLOT);
        }
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7 + (factor == 18 ? 88 : 0), 18 + inventorySize / factor * 18 + 11);
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    protected ModularPanel createUIPanel(GuiContext context, EntityPlayer player) {
        int rows = this.inventorySize / this.rowSize;
        List<List<IWidget>> widgets = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            widgets.add(new ArrayList<>());
            for (int j = 0; j < this.rowSize; j++) {
                widgets.get(i).add(new ItemSlot().setSynced(i * this.rowSize + j));
            }
        }
        return ModularPanel.defaultPanel(context, this.rowSize * 18 + 14, 7 + 4 * 18 + 5 + 14 + 18 * rows)
                .bindPlayerInventory()
                .child(new Grid()
                        .top(7).left(7).right(7).height(rows * 18)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .matrix(widgets));
    }

    @Override
    public void buildSyncHandler(GuiSyncHandler guiSyncHandler, EntityPlayer entityPlayer) {
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            guiSyncHandler.syncValue(i, SyncHandlers.itemSlot(this.inventory, i).slotGroup("item_inv"));
        }
        guiSyncHandler.registerSlotGroup("item_inv", this.rowSize);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("Inventory", inventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.inventory.deserializeNBT(data.getCompoundTag("Inventory"));
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", inventorySize));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
