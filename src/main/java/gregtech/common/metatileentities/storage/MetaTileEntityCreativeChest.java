package gregtech.common.metatileentities.storage;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.render.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.function.Function;

public class MetaTileEntityCreativeChest extends MetaTileEntity {

    private int itemsPerCycle = 1;
    private int ticksPerCycle = 1;
    private final ItemStackHandler handler = new ItemStackHandler(1);

    private boolean active = false;

    private final List<Character> ALLOWED_CHARS = Lists.newArrayList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');

    public MetaTileEntityCreativeChest(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        Textures.VOLTAGE_CASINGS[14].render(renderState, translation, pipeline, Cuboid6.full);
        for (EnumFacing face : EnumFacing.VALUES) {
            Textures.INFINITE_EMITTER_FACE.renderSided(face, renderState, translation, pipeline);
        }
        Textures.PIPE_OUT_OVERLAY.renderSided(this.getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityCreativeChest(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 209)
                .bindPlayerInventory(entityPlayer.inventory, 126);
        builder.widget(new PhantomSlotWidget(handler, 0, 36, 6).setBackgroundTexture(GuiTextures.SLOT_DARKENED).setChangeListener(this::markDirty));
        builder.label(7, 9, "Item");
        builder.widget(new ImageWidget(7, 48, 154, 14, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(9, 50, 152, 10, () -> String.valueOf(itemsPerCycle), value -> {
            if (!value.isEmpty()) {
                itemsPerCycle = Integer.parseInt(value);
            }
        }).setAllowedChars("0123456789").setMaxLength(19).setValidator(getTextFieldValidator()));
        builder.label(7, 28, "Items per cycle");

        builder.widget(new ImageWidget(7, 85, 154, 14, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(9, 87, 152, 10, () -> String.valueOf(ticksPerCycle), value -> {
            if (!value.isEmpty()) {
                ticksPerCycle = Integer.parseInt(value);
            }
        }).setMaxLength(10).setNumbersOnly(0, Integer.MAX_VALUE));
        builder.label(7, 65, "Ticks per cycle");


        builder.widget(new CycleButtonWidget(7, 101, 162, 20, () -> active, value -> active = value, "Not active", "Active"));

        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void update() {
        super.update();
        if (getOffsetTimer() % ticksPerCycle != 0) return;
        ItemStack stack = handler.getStackInSlot(0);
        if (getWorld().isRemote || !active || stack.isEmpty()) return;

        TileEntity tile = getWorld().getTileEntity(getPos().offset(this.getFrontFacing()));
        if (tile != null) {
            IItemHandler container = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, frontFacing);
            if (container == null || container.getSlots() == 0)
                return;
            stack.setCount(itemsPerCycle);

            ItemStack remainder = ItemHandlerHelper.insertItemStacked(container, stack, true);
            int amountToInsert = stack.getCount() - remainder.getCount();
            if (amountToInsert > 0) {
                ItemHandlerHelper.insertItemStacked(container, stack, false);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setTag("ItemStackHandler", handler.serializeNBT());
        data.setInteger("ItemsPerCycle", itemsPerCycle);
        data.setInteger("TicksPerCycle", ticksPerCycle);
        data.setBoolean("Active", active);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        handler.deserializeNBT(data.getCompoundTag("ItemStackHandler"));
        itemsPerCycle = data.getInteger("ItemsPerCycle");
        ticksPerCycle = data.getInteger("TicksPerCycle");
        active = data.getBoolean("Active");
        super.readFromNBT(data);
    }

    public Function<String, String> getTextFieldValidator() {
        return val -> {
            if (val.isEmpty()) {
                return "0";
            }
            long num;
            try {
                num = Long.parseLong(val);
            } catch (NumberFormatException ignored) {
                return "0";
            }
            if (num < 0) {
                return "0";
            }
            return val;
        };
    }

    @Override
    public IItemHandler getItemInventory() {
        ItemStackHandler fakeHandler = new ItemStackHandler(1);
        ItemStack fakeStack = new ItemStack(this.handler.getStackInSlot(0).getItem(), 69);
        fakeHandler.setStackInSlot(0, fakeStack);
        return fakeHandler;
    }
}
