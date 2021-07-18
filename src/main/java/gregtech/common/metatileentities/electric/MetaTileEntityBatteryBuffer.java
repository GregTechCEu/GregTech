package gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.EnergyContainerBatteryBuffer;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.render.Textures;
import gregtech.api.util.PipelineUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityBatteryBuffer extends TieredMetaTileEntity implements IControllable {

    private final int inventorySize;
    private boolean allowEnergyOutput = true;

    public MetaTileEntityBatteryBuffer(ResourceLocation metaTileEntityId, int tier, int inventorySize) {
        super(metaTileEntityId, tier);
        this.inventorySize = inventorySize;
        initializeInventory();
        reinitializeEnergyContainer();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityBatteryBuffer(metaTileEntityId, getTier(), inventorySize);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ENERGY_OUT.renderSided(getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
    }

    @Override
    protected void reinitializeEnergyContainer() {
        this.energyContainer = new EnergyContainerBatteryBuffer(this, getTier());
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isWorkingEnabled() {
        return allowEnergyOutput;
    }

    @Override
    protected boolean shouldUpdate(MTETrait trait) {
        return !(trait instanceof EnergyContainerBatteryBuffer) || allowEnergyOutput;
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.allowEnergyOutput = isActivationAllowed;
        getHolder().notifyBlockUpdate();
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(inventorySize) {
            @Override
            protected void onContentsChanged(int slot) {
                ((EnergyContainerBatteryBuffer) energyContainer).notifyEnergyListener(false);
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (((EnergyContainerBatteryBuffer) energyContainer).getBatteryContainer(stack) == null)
                    return stack; //do not allow to insert non-battery items
                return super.insertItem(slot, stack, simulate);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(0);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = importItems;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);
        Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
            18 + 18 * rowSize + 94)
            .label(10, 5, getMetaFullName());

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(importItems, index, 89 - rowSize * 9 + x * 18, 18 + y * 18, true, true)
                    .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.BATTERY_OVERLAY));
            }
        }
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * rowSize + 12);
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        String tierName = GTValues.VN[getTier()];

        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", inventorySize));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), tierName));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", energyContainer.getOutputVoltage(), tierName));
        tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out_till", inventorySize));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tagCompound = super.writeToNBT(data);
        tagCompound.setBoolean("AllowEnergyOutput", allowEnergyOutput);
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("AllowEnergyOutput", NBT.TAG_ANY_NUMERIC)) {
            this.allowEnergyOutput = data.getBoolean("AllowEnergyOutput");
        }
    }
}
