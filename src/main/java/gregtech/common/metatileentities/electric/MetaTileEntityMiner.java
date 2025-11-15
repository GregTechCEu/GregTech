package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IMiner;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.capability.impl.miner.MinerLogic;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.TextStandards;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityMiner extends TieredMetaTileEntity implements IMiner, IControllable, IDataInfoProvider {

    private final ItemStackHandler chargerInventory;

    private final int inventorySize;
    private final long energyPerTick;
    private boolean isInventoryFull = false;

    private final MinerLogic minerLogic;

    public MetaTileEntityMiner(ResourceLocation metaTileEntityId, int tier, int speed, int maximumRadius, int fortune) {
        super(metaTileEntityId, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.energyPerTick = GTValues.V[tier - 1];
        this.minerLogic = new MinerLogic(this, fortune, speed, maximumRadius, Textures.SOLID_STEEL_CASING);
        this.chargerInventory = new GTItemStackHandler(this, 1);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMiner(metaTileEntityId, getTier(), this.minerLogic.getSpeed(),
                this.minerLogic.getMaximumRadius(), this.minerLogic.getFortune());
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 0, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, inventorySize, this, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SCREEN.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        for (EnumFacing renderSide : EnumFacing.HORIZONTALS) {
            if (renderSide == getFrontFacing()) {
                Textures.PIPE_OUT_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
            } else
                Textures.CHUNK_MINER_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
        }
        minerLogic.renderPipe(renderState, translation, pipeline);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager, UISettings settings) {
        IntSyncValue radiusSync = new IntSyncValue(() -> getWorkingArea(minerLogic.getCurrentRadius()));
        BooleanSyncValue isDoneSync = new BooleanSyncValue(minerLogic::isDone);
        BooleanSyncValue isWorkingSync = new BooleanSyncValue(minerLogic::isWorking);
        BooleanSyncValue isWorkingEnabledSync = new BooleanSyncValue(minerLogic::isWorkingEnabled);
        BooleanSyncValue isInventoryFullSync = new BooleanSyncValue(() -> isInventoryFull);
        BooleanSyncValue hasEnoughEnergySync = new BooleanSyncValue(() -> drainEnergy(true));
        panelSyncManager.syncValue("radius", 0, radiusSync);
        panelSyncManager.syncValue("done", 0, isDoneSync);
        panelSyncManager.syncValue("working", 0, isWorkingSync);
        panelSyncManager.syncValue("workingEnabled", 0, isWorkingEnabledSync);
        panelSyncManager.syncValue("inventoryFull", 0, isInventoryFullSync);
        panelSyncManager.syncValue("enoughEnergy", 0, hasEnoughEnergySync);

        IntSyncValue xPosSync = new IntSyncValue(() -> minerLogic.getMineX().get());
        IntSyncValue yPosSync = new IntSyncValue(() -> minerLogic.getMineY().get());
        IntSyncValue zPosSync = new IntSyncValue(() -> minerLogic.getMineZ().get());
        panelSyncManager.syncValue("xPos", 0, xPosSync);
        panelSyncManager.syncValue("yPos", 0, yPosSync);
        panelSyncManager.syncValue("zPos", 0, zPosSync);

        return GTGuis.createPanel(this, 197, 176)
                .child(IKey.lang(getMetaFullName())
                        .asWidget()
                        .pos(5, 5))
                .child(createMinerWidgets(panelSyncManager, exportItems, inventorySize, GTGuiTextures.DISPLAY, text -> {
                    boolean isDone = isDoneSync.getBoolValue();
                    boolean isWorking = isWorkingSync.getBoolValue();
                    boolean isWorkingEnabled = isWorkingEnabledSync.getBoolValue();
                    boolean isInventoryFull = isInventoryFullSync.getBoolValue();
                    boolean hasEnoughEnergy = hasEnoughEnergySync.getBoolValue();

                    if (isWorking) {
                        text.addLine(KeyUtil.lang(TextFormatting.WHITE, "gregtech.machine.miner.mining_at"));
                        text.addLine(KeyUtil.lang(TextFormatting.WHITE, "gregtech.machine.miner.mining_pos_x",
                                xPosSync.getIntValue()));
                        text.addLine(KeyUtil.lang(TextFormatting.WHITE, "gregtech.machine.miner.mining_pos_y",
                                yPosSync.getIntValue()));
                        text.addLine(KeyUtil.lang(TextFormatting.WHITE, "gregtech.machine.miner.mining_pos_z",
                                zPosSync.getIntValue()));
                    }

                    text.addLine(KeyUtil.lang(TextFormatting.WHITE, "gregtech.machine.miner.working_area",
                            radiusSync.getIntValue(), radiusSync.getIntValue()));

                    if (isDone) {
                        text.addLine(KeyUtil.lang(TextStandards.Colors.MACHINE_DONE, "gregtech.machine.miner.done"));
                    } else if (isWorking) {
                        text.addLine(KeyUtil.lang(TextStandards.Colors.MACHINE_WORKING,
                                "gregtech.machine.miner.working"));
                    } else if (!isWorkingEnabled) {
                        text.addLine(TextStandards.Keys.MACHINE_PAUSED);
                    }

                    if (isInventoryFull) {
                        text.addLine(KeyUtil.lang(TextStandards.Colors.NO_OUTPUT_SPACE,
                                "gregtech.machine.miner.invfull"));
                    }

                    if (!hasEnoughEnergy) {
                        text.addLine(KeyUtil.lang(TextStandards.Colors.NO_POWER, "gregtech.machine.miner.needspower"));
                    }
                })
                        .left(10)
                        .top(18))
                .child(SlotGroupWidget.playerInventory(false)
                        .left(7)
                        .bottom(7))
                .child(new ItemSlot()
                        .right(7)
                        .bottom(7)
                        .slot(chargerInventory, 0)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.CHARGER_OVERLAY));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        int currentArea = getWorkingArea(minerLogic.getCurrentRadius());
        tooltip.add(I18n.format("gregtech.machine.miner.tooltip", currentArea, currentArea));
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_tick", energyPerTick) + TextFormatting.GRAY +
                ", " + I18n.format("gregtech.machine.miner.per_block", this.minerLogic.getSpeed() / 20));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(),
                GTValues.VNF[getTier()]));
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        int maxArea = getWorkingArea(minerLogic.getMaximumRadius());
        tooltip.add(I18n.format("gregtech.universal.tooltip.working_area_max", maxArea, maxArea));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.toggle_mode_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public boolean drainEnergy(boolean simulate) {
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(energyPerTick);
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        super.update();
        this.minerLogic.performMining();
        if (!getWorld().isRemote) {
            ((EnergyContainerHandler) this.energyContainer).dischargeOrRechargeEnergyContainers(chargerInventory, 0);

            if (getOffsetTimer() % 5 == 0)
                pushItemsIntoNearbyHandlers(getFrontFacing());

            if (this.minerLogic.wasActiveAndNeedsUpdate()) {
                this.minerLogic.setWasActiveAndNeedsUpdate(false);
                this.minerLogic.setActive(false);
            }
        }
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                      CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) return true;

        if (!this.isActive()) {
            int currentRadius = this.minerLogic.getCurrentRadius();
            if (currentRadius == 1)
                this.minerLogic.setCurrentRadius(this.minerLogic.getMaximumRadius());
            else if (playerIn.isSneaking())
                this.minerLogic.setCurrentRadius(Math.max(1, Math.round(currentRadius / 2.0f)));
            else
                this.minerLogic.setCurrentRadius(Math.max(1, currentRadius - 1));

            this.minerLogic.resetArea();

            int workingArea = getWorkingArea(minerLogic.getCurrentRadius());
            playerIn.sendMessage(
                    new TextComponentTranslation("gregtech.machine.miner.working_area", workingArea, workingArea));
        } else {
            playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.miner.errorradius"));
        }
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("ChargerInventory", chargerInventory.serializeNBT());
        return this.minerLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.chargerInventory.deserializeNBT(data.getCompoundTag("ChargerInventory"));
        this.minerLogic.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        this.minerLogic.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.minerLogic.receiveInitialSyncData(buf);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        this.minerLogic.receiveCustomData(dataId, buf);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, chargerInventory);
    }

    @Override
    public boolean isInventoryFull() {
        return isInventoryFull;
    }

    @Override
    public void setInventoryFull(boolean isFull) {
        this.isInventoryFull = isFull;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.minerLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.minerLogic.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.MINER;
    }

    @Override
    public boolean isActive() {
        return minerLogic.isActive() && isWorkingEnabled();
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        int workingArea = getWorkingArea(minerLogic.getCurrentRadius());
        return Collections.singletonList(
                new TextComponentTranslation("gregtech.machine.miner.working_area", workingArea, workingArea));
    }
}
