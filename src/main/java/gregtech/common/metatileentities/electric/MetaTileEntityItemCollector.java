package gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.Text;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.screen.ModularWindow;
import com.cleanroommc.modularui.api.screen.UIBuildContext;
import com.cleanroommc.modularui.common.widget.ButtonWidget;
import com.cleanroommc.modularui.common.widget.Row;
import com.cleanroommc.modularui.common.widget.SlotGroup;
import com.cleanroommc.modularui.common.widget.TextWidget;
import com.cleanroommc.modularui.common.widget.textfield.TextFieldWidget;
import gregtech.api.GTValues;
import gregtech.api.gui.GuiFunctions;
import gregtech.api.gui.GuiTextures;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.covers.filter.item.ItemFilterHolder;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.IS_WORKING;

public class MetaTileEntityItemCollector extends TieredMetaTileEntity {

    private static final int[] INVENTORY_SIZES = {9, 18, 27};
    private static final double MOTION_MULTIPLIER = 0.04;
    private static final int BASE_EU_CONSUMPTION = 6;

    private final int maxItemSuckingRange;
    private int itemSuckingRange;
    private AxisAlignedBB areaBoundingBox;
    private BlockPos areaCenterPos;
    private boolean isWorking;
    private final ItemFilterHolder itemFilter;

    public MetaTileEntityItemCollector(ResourceLocation metaTileEntityId, int tier, int maxItemSuckingRange) {
        super(metaTileEntityId, tier);
        this.maxItemSuckingRange = maxItemSuckingRange;
        this.itemSuckingRange = maxItemSuckingRange;
        this.itemFilter = new ItemFilterHolder(this::markDirty);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityItemCollector(metaTileEntityId, getTier(), maxItemSuckingRange);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        SimpleOverlayRenderer renderer = isWorking ? Textures.BLOWER_ACTIVE_OVERLAY : Textures.BLOWER_OVERLAY;
        renderer.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        Textures.AIR_VENT_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    protected int getEnergyConsumedPerTick() {
        return BASE_EU_CONSUMPTION * (1 << (getTier() - 1));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isWorking);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isWorking = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_WORKING) {
            this.isWorking = buf.readBoolean();
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

        boolean isWorkingNow = energyContainer.getEnergyStored() >= getEnergyConsumedPerTick() && isBlockRedstonePowered();

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
        if (getOffsetTimer() % 5 == 0) {
            pushItemsIntoNearbyHandlers(getFrontFacing());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.item_collector.collect_range", maxItemSuckingRange, maxItemSuckingRange));
        tooltip.add(I18n.format("gregtech.universal.tooltip.max_voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.machine.item_controller.tooltip.redstone"));
        tooltip.add(I18n.format("gregtech.machine.item_controller.tooltip.consumption", getEnergyConsumedPerTick()));
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(INVENTORY_SIZES[MathHelper.clamp(getTier() - 1, 0, INVENTORY_SIZES.length - 1)]);
    }

    @Override
    public boolean canPlaceCoverOnSide(EnumFacing side) {
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
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.itemSuckingRange = data.getInteger("CollectRange");
        this.itemFilter.deserializeNBT(data.getCompoundTag("Filter"));
    }

    protected void setItemSuckingRange(int itemSuckingRange) {
        this.itemSuckingRange = itemSuckingRange;
        this.areaBoundingBox = null;
        markDirty();
    }

    protected void adjustSuckingRange(int amount) {
        setItemSuckingRange(MathHelper.clamp(itemSuckingRange + amount, 1, maxItemSuckingRange));
    }

    @Override
    public boolean useOldGui() {
        return false;
    }

    @Nullable
    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        int rowSize = 9;
        int rows = exportItems.getSlots() / 9;
        ModularWindow.Builder builder = ModularWindow.builder(176, 146 + rows * 18);
        builder.setBackground(GuiTextures.VANILLA_BACKGROUND)
                .bindPlayerInventory(buildContext.getPlayer())
                .widget(new TextWidget(Text.localised(getMetaFullName()))
                        .setPos(10, 5))
                .widget(new TextWidget(Text.localised("Range: "))
                        .setTextAlignment(Alignment.CenterLeft)
                        .setSize(80, 14)
                        .setPos(7, 18))
                .widget(new Row()
                        .widget(new ButtonWidget()
                                .setOnClick(GuiFunctions.getIncrementer(-1, -4, -16, -64, this::adjustSuckingRange))
                                .setBackground(GuiTextures.BASE_BUTTON, new Text("-").color(0xFFFFFF))
                                .setSize(14, 14))
                        .widget(new TextFieldWidget()
                                .setGetterInt(() -> itemSuckingRange)
                                .setSetterInt(this::setItemSuckingRange)
                                .setNumbers(1, maxItemSuckingRange)
                                .setTextAlignment(Alignment.Center)
                                .setTextColor(0xFFFFFF)
                                .setBackground(GuiTextures.DISPLAY_SMALL)
                                .setSize(52, 14))
                        .widget(new ButtonWidget()
                                .setOnClick(GuiFunctions.getIncrementer(1, 4, 16, 64, this::adjustSuckingRange))
                                .setBackground(GuiTextures.BASE_BUTTON, new Text("+").color(0xFFFFFF))
                                .setSize(14, 14))
                        .setPos(89, 18))
                .widget(SlotGroup.ofItemHandler(this.exportItems, rowSize, 0, false)
                        .setPos(88 - rowSize * 18 / 2, 36))
                .widget(itemFilter.createFilterUI(buildContext).setPos(7, 40 + rows * 18));
        return builder.build();
    }
}
