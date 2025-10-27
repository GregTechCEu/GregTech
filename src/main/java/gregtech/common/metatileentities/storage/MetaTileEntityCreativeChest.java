package gregtech.common.metatileentities.storage;

import gregtech.api.GTValues;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.custom.QuantumStorageRenderer;
import gregtech.client.utils.TooltipHelper;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MetaTileEntityCreativeChest extends MetaTileEntityQuantumChest {

    private int itemsPerCycle = 1;
    private int ticksPerCycle = 1;

    private GTItemStackHandler creativeHandler;
    private ModifiableHandler modifiableHandler;

    private boolean active;

    public MetaTileEntityCreativeChest(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.MAX, 0);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = this.modifiableHandler = new ModifiableHandler();
        this.creativeHandler = new CreativeItemStackHandler(1);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.QUANTUM_STORAGE_RENDERER.renderMachine(renderState, translation,
                ArrayUtils.add(pipeline,
                        new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))),
                this);
        Textures.CREATIVE_CONTAINER_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        if (!isConnected() && active) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        }
        renderIndicatorOverlay(renderState, translation, pipeline);
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        QuantumStorageRenderer.renderChestStack(x, y, z, this, this.virtualItemStack, 420, partialTicks);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCreativeChest(this.metaTileEntityId);
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        guiSyncManager.syncValue("handler", this.modifiableHandler);
        return appendCreativeUI(GTGuis.createPanel(this, 176, 166), false,
                new BoolValue.Dynamic(() -> active, b -> active = b),
                new IntSyncValue(() -> itemsPerCycle, v -> itemsPerCycle = v),
                new IntSyncValue(() -> ticksPerCycle, v -> ticksPerCycle = v))
                        .child(IKey.lang("gregtech.creative.chest.item").asWidget()
                                .pos(7, 9))
                        .child(new ItemSlot()
                                .slot(SyncHandlers.phantomItemSlot(modifiableHandler, 0)
                                        .changeListener((newItem, onlyAmountChanged, client, init) -> markDirty()))
                                .pos(36, 6));
    }

    @Override
    public void update() {
        ItemStack stack = creativeHandler.getStackInSlot(0).copy();
        this.virtualItemStack = stack; // For rendering purposes
        super.update();
        if (ticksPerCycle == 0 || getOffsetTimer() % ticksPerCycle != 0) return;
        if (getWorld().isRemote || !active || stack.isEmpty() || isConnected()) return;

        TileEntity tile = getNeighbor(getOutputFacing());
        if (tile != null) {
            IItemHandler container = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    this.getOutputFacing().getOpposite());
            if (container == null || container.getSlots() == 0)
                return;
            stack.setCount(itemsPerCycle);

            ItemStack remainder = GTTransferUtils.insertItem(container, stack, true);
            int amountToInsert = stack.getCount() - remainder.getCount();
            if (amountToInsert > 0) {
                GTTransferUtils.insertItem(container, stack, false);
            }
        }
    }

    @Override
    protected boolean shouldTransferImport() {
        return false;
    }

    @Override
    protected boolean shouldTransferExport() {
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("ItemsPerCycle", itemsPerCycle);
        data.setInteger("TicksPerCycle", ticksPerCycle);
        data.setBoolean("Active", active);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (virtualItemStack.isEmpty())
            creativeHandler.deserializeNBT(data.getCompoundTag("ItemStackHandler"));
        itemsPerCycle = data.getInteger("ItemsPerCycle");
        ticksPerCycle = data.getInteger("TicksPerCycle");
        active = data.getBoolean("Active");
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey("id", 8)) { // Check if ItemStack wrote to this
            this.creativeHandler.setStackInSlot(0, new ItemStack(itemStack));
        }
        itemsPerCycle = itemStack.getInteger("mBPerCycle");
        ticksPerCycle = itemStack.getInteger("ticksPerCycle");
    }

    @Override
    public void writeItemStackData(NBTTagCompound tag) {
        super.writeItemStackData(tag);
        ItemStack stack = this.creativeHandler.getStackInSlot(0);
        if (!stack.isEmpty()) {
            stack.writeToNBT(tag);
        }
        tag.setInteger("mBPerCycle", itemsPerCycle);
        tag.setInteger("ticksPerCycle", ticksPerCycle);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.creative_tooltip.1") + TooltipHelper.RAINBOW +
                I18n.format("gregtech.creative_tooltip.2") + I18n.format("gregtech.creative_tooltip.3"));
        // do not append the normal tooltips
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.creativeHandler.setStackInSlot(0, this.virtualItemStack);
    }

    @Override
    public IItemHandler getTypeValue() {
        return this.creativeHandler;
    }

    // todo try to refactor this with mui2 rc6
    protected class ModifiableHandler extends QuantumChestItemHandler implements IItemHandlerModifiable {

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            virtualItemStack = GTUtility.copy(1, stack);
            itemsStoredInside = stack.isEmpty() ? 0 : 1;
            updateClient();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack insertedStack, boolean simulate) {
            if (insertedStack.isEmpty()) {
                return ItemStack.EMPTY;
            }

            setStackInSlot(slot, insertedStack);

            return ItemStack.EMPTY;
        }
    }

    protected class CreativeItemStackHandler extends GTItemStackHandler {

        CreativeItemStackHandler(int size) {
            super(MetaTileEntityCreativeChest.this, size);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack; // do not allow inserts
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!active) return ItemStack.EMPTY;
            return GTUtility.copy(Math.min(amount, itemsPerCycle), getStackInSlot(0));
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            modifiableHandler.setStackInSlot(slot, stack);
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return modifiableHandler.getStackInSlot(slot);
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            modifiableHandler.setStackInSlot(0, stacks.get(0)); // legacy nbt
        }
    }
}
