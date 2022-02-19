package gregtech.common.metatileentities.storage;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.api.gui.widgets.TextFieldWidget2;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.cclop.ColourOperation;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

public class MetaTileEntityCreativeChest extends MetaTileEntityQuantumChest {

    private int itemsPerCycle = 1;
    private int ticksPerCycle = 1;
    private final ItemStackHandler handler = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            this.validateSlotIndex(slot);
            stack.setCount(1);
            this.stacks.set(slot, stack);
            this.onContentsChanged(slot);
        }
    };

    private boolean active = false;

    public MetaTileEntityCreativeChest(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 15, 0);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.VOLTAGE_CASINGS[14].render(renderState, translation, ArrayUtils.add(pipeline,
                new ColourOperation(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))));
        Textures.CREATIVE_CONTAINER_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
        Textures.ITEM_OUTPUT_OVERLAY.renderSided(this.getOutputFacing(), renderState, translation, pipeline);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCreativeChest(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 209)
                .bindPlayerInventory(entityPlayer.inventory, 126);
        builder.widget(new PhantomSlotWidget(handler, 0, 36, 6)
                .setClearSlotOnRightClick(true)
                .setBackgroundTexture(GuiTextures.SLOT)
                .setChangeListener(this::markDirty));
        builder.label(7, 9, "gregtech.creative.chest.item");
        builder.widget(new ImageWidget(7, 48, 154, 14, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(9, 50, 152, 10, () -> String.valueOf(itemsPerCycle), value -> {
            if (!value.isEmpty()) {
                itemsPerCycle = Integer.parseInt(value);
            }
        }).setMaxLength(11).setNumbersOnly(1, Integer.MAX_VALUE));
        builder.label(7, 28, "gregtech.creative.chest.ipc");

        builder.widget(new ImageWidget(7, 85, 154, 14, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(9, 87, 152, 10, () -> String.valueOf(ticksPerCycle), value -> {
            if (!value.isEmpty()) {
                ticksPerCycle = Integer.parseInt(value);
            }
        }).setMaxLength(11).setNumbersOnly(1, Integer.MAX_VALUE));
        builder.label(7, 65, "gregtech.creative.chest.tpc");


        builder.widget(new CycleButtonWidget(7, 101, 162, 20, () -> active, value -> active = value, "gregtech.creative.activity.off", "gregtech.creative.activity.on"));

        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void update() {
        super.update();
        if (ticksPerCycle == 0 || getOffsetTimer() % ticksPerCycle != 0) return;
        ItemStack stack = handler.getStackInSlot(0).copy();
        if (getWorld().isRemote || !active || stack.isEmpty()) return;

        TileEntity tile = getWorld().getTileEntity(getPos().offset(this.getOutputFacing()));
        if (tile != null) {
            IItemHandler container = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.getOutputFacing().getOpposite());
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
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("ItemStackHandler", handler.serializeNBT());
        data.setInteger("ItemsPerCycle", itemsPerCycle);
        data.setInteger("TicksPerCycle", ticksPerCycle);
        data.setBoolean("Active", active);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        handler.deserializeNBT(data.getCompoundTag("ItemStackHandler"));
        itemsPerCycle = data.getInteger("ItemsPerCycle");
        ticksPerCycle = data.getInteger("TicksPerCycle");
        active = data.getBoolean("Active");
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.VOLTAGE_CASINGS[14].getParticleSprite(), this.getPaintingColorForRendering());
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey("id", 8)) { // Check if ItemStack wrote to this
            this.handler.setStackInSlot(0, new ItemStack(itemStack));
        }
        itemsPerCycle = itemStack.getInteger("mBPerCycle");
        ticksPerCycle = itemStack.getInteger("ticksPerCycle");
    }

    @Override
    public void writeItemStackData(NBTTagCompound tag) {
        super.writeItemStackData(tag);
        ItemStack stack = this.handler.getStackInSlot(0);
        if (!stack.isEmpty()) {
            stack.writeToNBT(tag);
        }
        tag.setInteger("mBPerCycle", itemsPerCycle);
        tag.setInteger("ticksPerCycle", ticksPerCycle);
    }
}
