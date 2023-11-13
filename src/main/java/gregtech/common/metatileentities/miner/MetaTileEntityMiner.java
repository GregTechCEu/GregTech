package gregtech.common.metatileentities.miner;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.math.IntMath;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

public class MetaTileEntityMiner extends TieredMetaTileEntity implements Miner, IControllable, IDataInfoProvider, IFastRenderMetaTileEntity {

    private final ItemStackHandler chargerInventory;

    private final int inventorySize;
    private final long energyPerTick;

    private final MinerLogic<MetaTileEntityMiner> minerLogic;

    public MetaTileEntityMiner(@Nonnull ResourceLocation metaTileEntityId, int tier, int workFrequency, int maximumDiameter) {
        super(metaTileEntityId, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.energyPerTick = GTValues.V[tier - 1];
        this.minerLogic = new MinerLogic<>(this, workFrequency, maximumDiameter);
        this.chargerInventory = new ItemStackHandler(1);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMiner(metaTileEntityId, getTier(), this.minerLogic.getWorkFrequency(), this.minerLogic.getMaximumDiameter());
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(0, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(inventorySize, this, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MINER_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), minerLogic.isActive(), minerLogic.isWorkingEnabled());
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        MiningArea previewArea = this.minerLogic.getPreviewArea();
        if (previewArea != null) previewArea.renderMetaTileEntity(this, x, y, z, partialTicks);
    }

    @Override
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        MiningArea previewArea = this.minerLogic.getPreviewArea();
        if (previewArea != null) previewArea.renderMetaTileEntityFast(this, renderState, translation, partialTicks);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        MiningArea previewArea = this.minerLogic.getPreviewArea();
        return previewArea != null ? previewArea.getRenderBoundingBox() : MinerUtil.EMPTY_AABB;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        MiningArea previewArea = this.minerLogic.getPreviewArea();
        return previewArea != null && previewArea.shouldRenderInPass(pass);
    }

    @Override
    public boolean isGlobalRenderer() {
        return true;
    }

    @Override
    protected ModularUI createUI(@Nonnull EntityPlayer entityPlayer) {
        IItemHandlerModifiable exportItems = this.getExportItems();
        int slots = exportItems.getSlots();
        int columns = IntMath.sqrt(slots, RoundingMode.UP);
        int xStart = (176 - (18 * columns)) / 2;
        int yOffset = Math.max(0, 16 + ((columns + 1) * 18) + 4 - 80);
        int yStart = yOffset > 0 ? 16 : 21;
        int sideWidgetY = yStart + (columns * 18 - 20) / 2;

        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset)
                .label(5, 5, getMetaFullName())
                .widget(new ToggleButtonWidget(152, yStart, 18, 18,
                        GuiTextures.BUTTON_MINER_AREA_PREVIEW,
                        this.minerLogic::isPreviewEnabled, this.minerLogic::setPreviewEnabled))
                .widget(new ClickButtonWidget(161, yStart + 18 + 2, 9, 9,
                        "", cd -> this.minerLogic.setCurrentDiameter(this.minerLogic.getCurrentDiameter() + (cd.isShiftClick ? 5 : 1)))
                        .setButtonTexture(GuiTextures.BUTTON_INT_CIRCUIT_PLUS))
                .widget(new ClickButtonWidget(161, yStart + 18 + 2 + 9, 9, 9,
                        "", cd -> this.minerLogic.setCurrentDiameter(this.minerLogic.getCurrentDiameter() - (cd.isShiftClick ? 5 : 1)))
                        .setButtonTexture(GuiTextures.BUTTON_INT_CIRCUIT_MINUS))
                .widget(new AdvancedTextWidget(159, yStart + 18 + 2 + (18 - 11) / 2, list -> {
                    int currentDiameter = this.minerLogic.getCurrentDiameter();
                    list.add(new TextComponentString(currentDiameter + "x" + currentDiameter));
                }, 0x404040) {
                    @Override
                    protected void onSizeUpdate() { // >:(
                        Size size = this.getSize();
                        this.setSelfPosition(new Position(159 - size.width, this.getSelfPosition().y));
                    }
                })
                .widget(new SlotWidget(this.chargerInventory, 0, 79, 62 + yOffset, true, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY)
                        .setTooltipText("gregtech.gui.charger_slot.tooltip", GTValues.VNF[getTier()], GTValues.VNF[getTier()]));

        for (int i = 0; i < slots; i++) {
            builder.slot(exportItems, i, xStart + 18 * (i % columns), yStart + 18 * (i / columns),
                    true, false, GuiTextures.SLOT);
        }

        builder.widget(
                new ProgressWidget(this.minerLogic::getWorkProgress, xStart - 4 - 20, sideWidgetY, 20, 20,
                        GuiTextures.PROGRESS_BAR_MACERATE, ProgressWidget.MoveType.HORIZONTAL)
        ).widget(
                new ImageWidget(xStart - 4 - 20, sideWidgetY + 20, 18, 18,
                        GuiTextures.INDICATOR_NO_ENERGY)
                        .setIgnoreColor(true)
                        .setPredicate(minerLogic::hasNotEnoughEnergy)
        ).widget(
                new ImageWidget(152, 63 + yOffset, 17, 17,
                        GTValues.XMAS.get() ? GuiTextures.GREGTECH_LOGO_XMAS : GuiTextures.GREGTECH_LOGO)
                        .setIgnoreColor(true)
        ).bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, yOffset);

        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.miner.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_tick", energyPerTick)
                + TextFormatting.GRAY + ", " + I18n.format("gregtech.machine.miner.per_block", this.minerLogic.getWorkFrequency() / 20));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        int maxArea = minerLogic.getMaximumDiameter();
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
    public boolean drainMiningResources(@Nonnull MinedBlockType minedBlockType, boolean pipeExtended, boolean simulate) {
        if (minedBlockType == MinedBlockType.NOTHING) return true;
        long resultEnergy = energyContainer.getEnergyStored() - energyPerTick;
        if (resultEnergy < 0 || resultEnergy > energyContainer.getEnergyCapacity()) return false;
        if (!simulate) {
            energyContainer.removeEnergy(energyPerTick);
        }
        return true;
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public MiningPipeModel getMiningPipeModel() {
        return MiningPipeModels.STEEL;
    }

    @Override
    public void update() {
        super.update();
        this.minerLogic.update();
        if (!getWorld().isRemote) {
            ((EnergyContainerHandler) this.energyContainer).dischargeOrRechargeEnergyContainers(chargerInventory, 0);

            if (getOffsetTimer() % 5 == 0)
                pushItemsIntoNearbyHandlers(getFrontFacing());
        }
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
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, chargerInventory);
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
        return minerLogic.isActive();
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        int diameter = minerLogic.getCurrentDiameter();
        return Collections.singletonList(new TextComponentTranslation("gregtech.machine.miner.working_area", diameter, diameter));
    }
}
