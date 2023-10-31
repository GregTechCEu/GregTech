package gregtech.common.metatileentities.miner;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static gregtech.api.unification.material.Materials.DrillingFluid;

public class MetaTileEntityLargeMiner extends MultiblockWithDisplayBase implements Miner, IControllable, IDataInfoProvider, IFastRenderMetaTileEntity {

    @Nonnull
    public final LargeMinerType type;
    public final int tier;
    public final int drillingFluidConsumePerTick;
    public final int oreMultiplier;

    private final MultiblockMinerLogic minerLogic;

    private IEnergyContainer energyContainer;
    protected IMultipleTankHandler inputFluidInventory;
    protected IItemHandlerModifiable outputInventory;

    public MetaTileEntityLargeMiner(ResourceLocation metaTileEntityId, int tier, int speed, int maxChunkDiameter,
                                    int oreMultiplier, int drillingFluidConsumePerTick, @Nonnull LargeMinerType type) {
        super(metaTileEntityId);
        this.type = Objects.requireNonNull(type, "type == null");
        this.tier = tier;
        this.drillingFluidConsumePerTick = drillingFluidConsumePerTick;
        this.oreMultiplier = oreMultiplier;
        this.minerLogic = new MultiblockMinerLogic(this, speed, maxChunkDiameter);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeMiner(metaTileEntityId, this.tier, this.minerLogic.getWorkFrequency(),
                this.minerLogic.getMaximumChunkDiameter(),
                this.oreMultiplier, this.drillingFluidConsumePerTick, this.type);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.inputFluidInventory = new FluidTankList(true);
        this.outputInventory = new ItemStackHandler(0);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.inputFluidInventory = new FluidTankList(false, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.minerLogic.reset();
    }

    public int getEnergyTier() {
        if (energyContainer == null) return this.tier;
        return Math.min(this.tier + 1, Math.max(this.tier, GTUtility.getFloorTierByVoltage(energyContainer.getInputVoltage())));
    }

    @Override
    public boolean drainMiningResources(@NotNull MinedBlockType minedBlockType, boolean pipeExtended, boolean simulate) {
        if (minedBlockType == MinedBlockType.NOTHING) return true;
        if (!drainEnergy(true) || !drainFluid(true)) return false;
        if (!simulate) {
            drainEnergy(false);
            drainFluid(false);
        }
        return true;
    }

    protected boolean drainEnergy(boolean simulate) {
        long energyToDrain = GTValues.VA[getEnergyTier()];
        long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate) {
                energyContainer.changeEnergy(-energyToDrain);
            }
            return true;
        }
        return false;
    }

    protected boolean drainFluid(boolean simulate) {
        int overclockAmount = getEnergyTier() - this.tier;
        int amount = this.drillingFluidConsumePerTick * overclockAmount;
        FluidStack drained = this.inputFluidInventory.drain(DrillingFluid.getFluid(amount), !simulate);
        return drained != null && drained.amount >= amount;
    }

    @Override
    public void getRegularBlockDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        if (this.minerLogic.isSilkTouchMode()) {
            drops.add(ToolHelper.getSilkTouchDrop(state));
        } else if (MinerUtil.applyTieredHammerDrops(GTUtility.toItem(state), drops,
                this.getEnergyTier(), RecipeMaps.MACERATOR_RECIPES, this.oreMultiplier) == 0) {
            Miner.super.getRegularBlockDrops(drops, world, pos, state); // fallback
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(), isWorkingEnabled());
        if (isStructureFormed()) {
            EnumFacing back = getFrontFacing().getOpposite();
            MinerRenderHelper.renderPipe(getBaseTexture(null), this.minerLogic.getPipeLength(), renderState,
                    translation.translate(back.getXOffset(), back.getYOffset(), back.getZOffset()), pipeline);
        }
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
    protected void updateFormedValid() {
        this.minerLogic.update();
    }

    @Nonnull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                .where('S', selfPredicate())
                .where('X', this.type.getCasing()
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setExactLimit(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3).setPreviewCount(1)))
                .where('C', this.type.getCasing())
                .where('F', this.type.getFrame())
                .where('#', any())
                .build();
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.machine.miner.multi.description")};
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        int workingAreaChunks = this.minerLogic.getMaximumChunkDiameter();
        tooltip.add(I18n.format("gregtech.machine.miner.multi.modes"));
        tooltip.add(I18n.format("gregtech.machine.miner.multi.production"));
        tooltip.add(I18n.format("gregtech.machine.miner.fluid_usage", this.drillingFluidConsumePerTick, DrillingFluid.getLocalizedName()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.working_area_chunks_max", workingAreaChunks, workingAreaChunks));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_tier_range", GTValues.VNF[this.tier], GTValues.VNF[this.tier + 1]));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.toggle_mode_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        if (getSound() != null) {
            tooltip.add(I18n.format("gregtech.tool_action.hammer"));
        }
        tooltip.add(I18n.format("gregtech.tool_action.crowbar"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);

        if (this.isStructureFormed()) {
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                int energyContainer = getEnergyTier();
                long maxVoltage = GTValues.V[energyContainer];
                String voltageName = GTValues.VNF[energyContainer];
                textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
            }
            this.minerLogic.addDisplayText(textList);
        }
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        switch (componentData) {
            case MinerUtil.DISPLAY_CLICK_AREA_PREVIEW -> this.minerLogic.setPreviewEnabled(true);
            case MinerUtil.DISPLAY_CLICK_AREA_PREVIEW_HIDE -> this.minerLogic.setPreviewEnabled(false);
            case MinerUtil.DISPLAY_CLICK_Y_LIMIT_DECR -> this.minerLogic.setYLimit(Math.max(0,
                    this.minerLogic.getYLimit() - (clickData.isShiftClick ? 5 : 1)));
            case MinerUtil.DISPLAY_CLICK_Y_LIMIT_INCR -> {
                int yLimit = this.minerLogic.getYLimit() + (clickData.isShiftClick ? 5 : 1);
                if (yLimit < 0) yLimit = this.minerLogic.getYLimit() < 0 ? 0 : Integer.MAX_VALUE;
                this.minerLogic.setYLimit(yLimit);
            }
            case MinerUtil.DISPLAY_CLICK_REPEAT_ENABLE -> this.minerLogic.setRepeat(true);
            case MinerUtil.DISPLAY_CLICK_REPEAT_DISABLE -> this.minerLogic.setRepeat(false);
        }
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        super.addWarningText(textList);
        if (isStructureFormed()) {
            if (this.minerLogic.isInventoryFull()) {
                textList.add(new TextComponentTranslation("gregtech.machine.miner.display.inventory_full")
                        .setStyle(new Style().setColor(TextFormatting.RED)));
            }
            if (!drainFluid(true)) {
                textList.add(new TextComponentTranslation("gregtech.machine.miner.multi.needsfluid")
                        .setStyle(new Style().setColor(TextFormatting.RED)));
            }
            if (!drainEnergy(true)) {
                textList.add(new TextComponentTranslation("gregtech.multiblock.not_enough_energy")
                        .setStyle(new Style().setColor(TextFormatting.RED)));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        return this.minerLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
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
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(@Nullable IMultiblockPart sourcePart) {
        return this.type.getBaseTexture(sourcePart);
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    protected ICubeRenderer getFrontOverlay() {
        return this.type.getFrontOverlay();
    }

    @Nonnull
    @Override
    protected Widget getFlexButton(int x, int y, int width, int height) {
        return new ImageCycleButtonWidget(x, y, width, height, GuiTextures.BUTTON_MINER_MODES, 4, () -> {
            int mode = 0;
            if (minerLogic.isChunkMode()) mode |= 1;
            if (minerLogic.isSilkTouchMode()) mode |= 2;
            return mode;
        }, m -> {
            minerLogic.setChunkMode((m & 1) != 0);
            minerLogic.setSilkTouchMode((m & 2) != 0);
        }).setTooltipHoverString(m -> switch (m) {
            case 0 -> "gregtech.multiblock.miner.neither_mode";
            case 1 -> "gregtech.multiblock.miner.chunk_mode";
            case 2 -> "gregtech.multiblock.miner.silk_touch_mode";
            default -> "gregtech.multiblock.miner.both_modes";
        });
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer player, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote || !this.isStructureFormed()) {
            return true;
        }

        if (!this.isActive()) {
            if (this.minerLogic.isChunkMode()) {
                int currentChunkDiameter = this.minerLogic.getCurrentChunkDiameter();
                this.minerLogic.setCurrentChunkDiameter(currentChunkDiameter <= 1 ?
                        this.minerLogic.getMaximumChunkDiameter() : currentChunkDiameter - 1);

                int workingAreaChunks = this.minerLogic.getCurrentChunkDiameter();
                player.sendMessage(new TextComponentTranslation("gregtech.machine.miner.working_area_chunks", workingAreaChunks, workingAreaChunks));
            } else {
                int diameter = this.minerLogic.getCurrentDiameter() - 8;
                if (diameter <= 0) diameter = this.minerLogic.getMaximumDiameter();
                this.minerLogic.setCurrentDiameter(diameter);

                player.sendMessage(new TextComponentTranslation("gregtech.universal.tooltip.working_area", diameter, diameter));
            }
        } else {
            player.sendMessage(new TextComponentTranslation("gregtech.machine.miner.errorradius"));
        }
        return true;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
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
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public IItemHandlerModifiable getExportItems() {
        return this.outputInventory;
    }

    @Override
    public SoundEvent getSound() {
        return GTSoundEvents.MINER;
    }

    @Override
    public boolean isActive() {
        return isStructureFormed() && minerLogic.isActive();
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        int diameter = this.minerLogic.getCurrentDiameter();
        return Collections.singletonList(new TextComponentTranslation("gregtech.machine.miner.working_area", diameter, diameter));
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
}
