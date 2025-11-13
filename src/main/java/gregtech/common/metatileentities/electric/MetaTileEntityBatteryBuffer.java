package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.EnergyContainerBatteryBuffer;
import gregtech.api.metatileentity.*;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.PipelineUtil;
import gregtech.common.ConfigHolder;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityBatteryBuffer extends TieredMetaTileEntity implements IControllable, IDataInfoProvider {

    private final int inventorySize;
    private boolean allowEnergyOutput = true;

    public MetaTileEntityBatteryBuffer(ResourceLocation metaTileEntityId, int tier, int inventorySize) {
        super(metaTileEntityId, tier);
        this.inventorySize = inventorySize;
        initializeInventory();
        reinitializeEnergyContainer();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBatteryBuffer(metaTileEntityId, getTier(), inventorySize);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ENERGY_OUT.renderSided(getFrontFacing(), renderState, translation,
                PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
    }

    @Override
    protected void reinitializeEnergyContainer() {
        this.energyContainer = new EnergyContainerBatteryBuffer(this, getTier(), inventorySize);
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
        notifyBlockUpdate();
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
                MetaTileEntityBatteryBuffer.this.markDirty();
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if ((electricItem != null && getTier() >= electricItem.getTier()) ||
                        (ConfigHolder.compat.energy.nativeEUToFE &&
                                stack.hasCapability(CapabilityEnergy.ENERGY, null))) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = importItems;
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        int rowSize = (int) Math.sqrt(inventorySize);
        int colSize = rowSize;
        if (inventorySize == 8) {
            rowSize = 4;
            colSize = 2;
        }

        guiSyncManager.registerSlotGroup("item_inv", rowSize);

        int index = 0;
        List<List<IWidget>> widgets = new ArrayList<>();
        for (int y = 0; y < colSize; y++) {
            widgets.add(new ArrayList<>());
            for (int x = 0; x < rowSize; x++) {
                widgets.get(y).add(new ItemSlot().slot(SyncHandlers.itemSlot(this.importItems, index++)
                        .slotGroup("item_inv"))
                        .background(GTGuiTextures.SLOT, GTGuiTextures.BATTERY_OVERLAY));
            }
        }

        // TODO: Change the position of the name when it's standardized.
        return GTGuis.createPanel(this, 176, 18 + 18 * colSize + 94)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory(false).left(7).bottom(7))
                .child(new Grid()
                        .top(18).height(colSize * 18).width(rowSize * 18)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .alignX(0.5f)
                        .matrix(widgets));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        String tierName = GTValues.VNF[getTier()];

        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", inventorySize));
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.voltage_in_out", energyContainer.getInputVoltage(), tierName));
        tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till", energyContainer.getInputAmperage()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out_till", energyContainer.getOutputAmperage()));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
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

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ArrayList<>();
        list.add(new TextComponentTranslation("gregtech.battery_buffer.average_input",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getInputPerSec() / 20))
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        list.add(new TextComponentTranslation("gregtech.battery_buffer.average_output",
                new TextComponentTranslation(TextFormattingUtil.formatNumbers(energyContainer.getOutputPerSec() / 20))
                        .setStyle(new Style().setColor(TextFormatting.YELLOW))));
        return list;
    }
}
