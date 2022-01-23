package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityItemBus extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IItemHandlerModifiable> {
    private boolean autoCollapse;

    private static final int[] INVENTORY_SIZES = {1, 4, 9, 16, 25, 36, 49};

    public MetaTileEntityItemBus(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier, isExportHatch);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityItemBus(metaTileEntityId, getTier(), isExportHatch);
    }

    @Override
    public void update() {
        IItemHandlerModifiable inventory = (isExportHatch ? this.getExportItems() : this.getImportItems());
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 5 == 0) {
            if (isExportHatch) {
                pushItemsIntoNearbyHandlers(getFrontFacing());
            } else {
                pullItemsFromNearbyHandlers(getFrontFacing());
            }
            if (isAutoCollapse() && (isExportHatch ? this.getNotifiedItemOutputList().contains(inventory) : this.getNotifiedItemInputList().contains(inventory))){
                collapseInventorySlotContents(inventory);
            }
        }

    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = isExportHatch ? Textures.PIPE_OUT_OVERLAY : Textures.PIPE_IN_OVERLAY;
            renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
            SimpleOverlayRenderer overlay = isExportHatch ? Textures.ITEM_HATCH_OUTPUT_OVERLAY : Textures.ITEM_HATCH_INPUT_OVERLAY;
            overlay.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    private int getInventorySize() {
        int sizeRoot = 1 + Math.min(9, getTier());
        return sizeRoot * sizeRoot;
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return isExportHatch ? new NotifiableItemStackHandler(getInventorySize(), getController(), true) : new ItemStackHandler(0);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return isExportHatch ? new ItemStackHandler(0) : new NotifiableItemStackHandler(getInventorySize(), getController(), false);
    }


    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return isExportHatch ? MultiblockAbility.EXPORT_ITEMS : MultiblockAbility.IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> abilityList) {
        abilityList.add(isExportHatch ? this.exportItems : this.importItems);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(getInventorySize());
        return createUITemplate(entityPlayer, rowSize, rowSize == 10 ? 9 : 0)
                .build(getHolder(), entityPlayer);
    }

    private ModularUI.Builder createUITemplate(EntityPlayer player, int rowSize, int xOffset) {
        Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176 + xOffset * 2,
                18 + 18 * rowSize + 94)
                .label(10, 5, getMetaFullName());

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(isExportHatch ? exportItems : importItems, index,
                        (88 - rowSize * 9 + x * 18) + xOffset, 18 + y * 18, true, !isExportHatch)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        return builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7 + xOffset, 18 + 18 * rowSize + 12);
    }

    private static void collapseInventorySlotContents(IItemHandlerModifiable inventory) {
        //stack item stacks with equal items and compounds
        for (int i = 0; i < inventory.getSlots(); i++) {
            for (int j = i + 1; j < inventory.getSlots(); j++) {
                ItemStack stack1 = inventory.getStackInSlot(i);
                ItemStack stack2 = inventory.getStackInSlot(j);
                if (!stack1.isEmpty() && ItemStack.areItemsEqual(stack1, stack2) &&
                        ItemStack.areItemStackTagsEqual(stack1, stack2)) {
                    int maxStackSize = Math.min(stack1.getMaxStackSize(), inventory.getSlotLimit(i));
                    int itemsCanAccept = Math.min(stack2.getCount(), maxStackSize - Math.min(stack1.getCount(), maxStackSize));
                    if (itemsCanAccept > 0) {
                        stack1.grow(itemsCanAccept);
                        stack2.shrink(itemsCanAccept);
                    }
                }
            }
        }
        ArrayList<ItemStack> inventoryContents = new ArrayList<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                inventoryContents.add(itemStack);
            }
        }
        for (int i = 0; i < inventoryContents.size(); i++) {
            inventory.setStackInSlot(i, inventoryContents.get(i));
        }

    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        IItemHandlerModifiable inventory = (isExportHatch ? this.getExportItems() : this.getImportItems());
        if (!playerIn.isSneaking()){
            boolean isAttached = false;
            if (isExportHatch ? this.getNotifiedItemOutputList().contains(inventory) : this.getNotifiedItemInputList().contains(inventory)){
                setAutoCollapse(!this.autoCollapse);
                isAttached = true;
            }

            if(!getWorld().isRemote) {
                if (isAttached) {
                    playerIn.sendMessage(new TextComponentTranslation("gregtech.bus.collapse", this.autoCollapse));
                } else {
                    playerIn.sendMessage(new TextComponentTranslation("gregtech.bus.collapse.error"));
                }
            }
            return true;
        }
        return super.onScrewdriverClick(playerIn, hand, facing, hitResult);
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("autoCollapse", autoCollapse);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("autoCollapse")) {
            this.autoCollapse = data.getBoolean("autoCollapse");
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(autoCollapse);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.autoCollapse = buf.readBoolean();
    }


    public boolean isAutoCollapse() {
        return autoCollapse;
    }

    public void setAutoCollapse(boolean inverted) {
        autoCollapse = inverted;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        if (this.isExportHatch)
            tooltip.add(I18n.format("gregtech.machine.item_bus.export.tooltip"));
        else
            tooltip.add(I18n.format("gregtech.machine.item_bus.import.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", getInventorySize()));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }
}
