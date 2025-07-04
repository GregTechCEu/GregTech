package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.KeyUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.covers.filter.ItemFilterContainer;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_AUTO_OUTPUT_ITEMS;

public class MetaTileEntityItemCollector extends TieredMetaTileEntity {

    private static final int[] INVENTORY_SIZES = { 4, 9, 16, 25, 25 };
    private static final double MOTION_MULTIPLIER = 0.04;
    private static final int BASE_EU_CONSUMPTION = 6;

    private final int maxItemSuckingRange;
    private int itemSuckingRange;
    private AxisAlignedBB areaBoundingBox;
    private BlockPos areaCenterPos;
    private boolean isWorking;
    private boolean autoOutput = true;
    private final ItemFilterContainer itemFilter;

    public MetaTileEntityItemCollector(ResourceLocation metaTileEntityId, int tier, int maxItemSuckingRange) {
        super(metaTileEntityId, tier);
        this.maxItemSuckingRange = maxItemSuckingRange;
        this.itemSuckingRange = maxItemSuckingRange;
        this.itemFilter = new ItemFilterContainer(this::markDirty);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityItemCollector(metaTileEntityId, getTier(), maxItemSuckingRange);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        SimpleOverlayRenderer renderer = isWorking ? Textures.BLOWER_ACTIVE_OVERLAY : Textures.BLOWER_OVERLAY;
        renderer.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        Textures.AIR_VENT_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        if (autoOutput) {
            Textures.ITEM_OUTPUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    protected int getEnergyConsumedPerTick() {
        return BASE_EU_CONSUMPTION * (1 << (getTier() - 1));
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isWorking);
        buf.writeBoolean(autoOutput);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isWorking = buf.readBoolean();
        this.autoOutput = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_WORKING) {
            this.isWorking = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_ITEMS) {
            this.autoOutput = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return true;
    }

    @Override
    public void update() {
        super.update();

        if (getWorld().isRemote) {
            return;
        }

        boolean isWorkingNow = energyContainer.getEnergyStored() >= getEnergyConsumedPerTick() &&
                isBlockRedstonePowered();

        if (isWorkingNow) {
            energyContainer.removeEnergy(getEnergyConsumedPerTick());
            BlockPos selfPos = getPos();
            if (areaCenterPos == null || areaBoundingBox == null || areaCenterPos.getX() != selfPos.getX() ||
                    areaCenterPos.getZ() != selfPos.getZ() || areaCenterPos.getY() != selfPos.getY() + 1) {
                this.areaCenterPos = selfPos.up();
                this.areaBoundingBox = new AxisAlignedBB(areaCenterPos).grow(itemSuckingRange, 1.0, itemSuckingRange);
            }
            moveItemsInEffectRange();
        }

        if (autoOutput && getOffsetTimer() % 5 == 0) {
            pushItemsIntoNearbyHandlers(getFrontFacing());
        }

        if (isWorkingNow != isWorking) {
            this.isWorking = isWorkingNow;
            writeCustomData(IS_WORKING, buffer -> buffer.writeBoolean(isWorkingNow));
        }
    }

    protected void moveItemsInEffectRange() {
        List<EntityItem> itemsInRange = getWorld().getEntitiesWithinAABB(EntityItem.class, areaBoundingBox);
        for (EntityItem entityItem : itemsInRange) {
            if (entityItem.isDead) continue;
            double distanceX = (areaCenterPos.getX() + 0.5) - entityItem.posX;
            double distanceZ = (areaCenterPos.getZ() + 0.5) - entityItem.posZ;
            double distance = MathHelper.sqrt(distanceX * distanceX + distanceZ * distanceZ);
            if (!itemFilter.test(entityItem.getItem())) {
                continue;
            }
            if (distance >= 0.7) {
                if (!entityItem.cannotPickup()) {
                    double directionX = distanceX / distance;
                    double directionZ = distanceZ / distance;
                    entityItem.motionX = directionX * MOTION_MULTIPLIER * getTier();
                    entityItem.motionZ = directionZ * MOTION_MULTIPLIER * getTier();
                    entityItem.velocityChanged = true;
                    entityItem.setPickupDelay(1);
                }
            } else {
                ItemStack itemStack = entityItem.getItem();
                ItemStack remainder = GTTransferUtils.insertItem(exportItems, itemStack, false);
                if (remainder.isEmpty()) {
                    entityItem.setDead();
                } else if (itemStack.getCount() > remainder.getCount()) {
                    entityItem.setItem(remainder);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.item_collector.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_tick", getEnergyConsumedPerTick()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.max_voltage_in", energyContainer.getInputVoltage(),
                GTValues.VNF[getTier()]));
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.working_area", maxItemSuckingRange, maxItemSuckingRange));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this,
                INVENTORY_SIZES[MathHelper.clamp(getTier(), 0, INVENTORY_SIZES.length - 1)]);
    }

    @Override
    public boolean canPlaceCoverOnSide(@NotNull EnumFacing side) {
        return side != EnumFacing.DOWN && side != EnumFacing.UP;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        return canPlaceCoverOnSide(side) ? super.getCapability(capability, side) : null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("CollectRange", itemSuckingRange);
        data.setTag("Filter", itemFilter.serializeNBT());
        data.setBoolean("AutoOutput", autoOutput);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.itemSuckingRange = data.getInteger("CollectRange");
        this.itemFilter.deserializeNBT(data.getCompoundTag("Filter"));
        if (data.hasKey("AutoOutput")) {
            this.autoOutput = data.getBoolean("AutoOutput");
        }
    }

    protected void setItemSuckingRange(int itemSuckingRange) {
        this.itemSuckingRange = itemSuckingRange;
        this.areaBoundingBox = null;
        markDirty();
    }

    public int getItemSuckingRange() {
        return itemSuckingRange;
    }

    protected void setAutoOutput(boolean autoOutput) {
        this.autoOutput = autoOutput;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_ITEMS, buf -> buf.writeBoolean(autoOutput));
            markDirty();
        }
    }

    public boolean autoOutputs() {
        return autoOutput;
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        int rowSize = (int) Math.sqrt(exportItems.getSlots());

        IntSyncValue rangeSync = SyncHandlers.intNumber(this::getItemSuckingRange, this::setItemSuckingRange);
        BooleanSyncValue autoOutputSync = SyncHandlers.bool(this::autoOutputs, this::setAutoOutput);

        return GTGuis.createPanel(this, 176, 45 + rowSize * 18 + 35 + 82)
                .child(Flow.column()
                        .margin(5)
                        .coverChildrenHeight()
                        .child(IKey.lang(getMetaFullName())
                                .asWidget()
                                .alignX(0.0f))
                        .child(Flow.row()
                                .widthRel(1.0f)
                                .coverChildrenHeight()
                                .margin(0, 4)
                                .child(new ButtonWidget<>()
                                        .marginRight(5)
                                        .size(20)
                                        .onMousePressed(mouse -> {
                                            int range = rangeSync.getIntValue();
                                            if (range > 1) {
                                                rangeSync.setIntValue(range - 1);
                                            }
                                            return true;
                                        })
                                        .overlay(IKey.str("-1")))
                                .child(KeyUtil.lang(TextFormatting.WHITE,
                                        "gregtech.machine.item_collector.gui.collect_range",
                                        () -> new Object[] {
                                                TextFormattingUtil.formatNumbers(rangeSync.getIntValue()) })
                                        .alignment(Alignment.Center)
                                        .asWidget()
                                        .height(20)
                                        .expanded()
                                        .background(GTGuiTextures.DISPLAY))
                                .child(new ButtonWidget<>()
                                        .marginLeft(5)
                                        .size(20)
                                        .onMousePressed(mouse -> {
                                            int range = rangeSync.getIntValue();
                                            if (range < maxItemSuckingRange) {
                                                rangeSync.setIntValue(range + 1);
                                            }
                                            return true;
                                        })
                                        .overlay(IKey.str("-1"))))
                        .child(itemFilter.initUI(guiData, panelSyncManager))
                        .child(Flow.row()
                                .widthRel(1.0f)
                                .marginTop(4)
                                .coverChildrenHeight()
                                .child(new Grid()
                                        .alignX(0.5f)
                                        .height(rowSize * 18)
                                        .minElementMargin(0, 0)
                                        .minColWidth(18).minRowHeight(18)
                                        .mapTo(rowSize, rowSize * rowSize, index -> new ItemSlot()
                                                .slot(SyncHandlers.itemSlot(exportItems, index)
                                                        .accessibility(false, true))))
                                .child(new ToggleButton()
                                        .right(2)
                                        .bottom(0)
                                        .value(autoOutputSync)
                                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                                        .tooltipAutoUpdate(true)
                                        .tooltipBuilder(tooltip -> tooltip.addLine(autoOutputSync.getBoolValue() ?
                                                IKey.lang("gregtech.gui.item_auto_output.tooltip.enabled") :
                                                IKey.lang("gregtech.gui.item_auto_output.tooltip.disabled"))))))
                .child(SlotGroupWidget.playerInventory()
                        .bottom(7)
                        .left(7));
    }
}
