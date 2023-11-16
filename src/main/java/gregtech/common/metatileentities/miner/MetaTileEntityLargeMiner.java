package gregtech.common.metatileentities.miner;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.ImageCycleButtonWidget;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static gregtech.api.unification.material.Materials.DrillingFluid;

public class MetaTileEntityLargeMiner extends MultiblockWithDisplayBase implements Miner, IControllable, IDataInfoProvider, IFastRenderMetaTileEntity, IProgressBarMultiblock {

    @Nonnull
    public final LargeMinerType type;
    public final int tier;
    public final int drillingFluidConsumePerTick;

    private final MultiblockMinerLogic minerLogic;

    private IEnergyContainer energyContainer;
    protected IMultipleTankHandler inputFluidInventory;
    protected IItemHandlerModifiable outputInventory;

    // last mined ore block
    protected final MutableBlockPos lastMinedOre = new MutableBlockPos();
    protected boolean hasLastMinedOre;
    // number of ores processed so far
    protected int minedOreCount;

    private boolean inventoryFull;

    public MetaTileEntityLargeMiner(ResourceLocation metaTileEntityId, int tier, int speed, int maxChunkDiameter,
                                    int drillingFluidConsumePerTick, @Nonnull LargeMinerType type) {
        super(metaTileEntityId);
        this.type = Objects.requireNonNull(type, "type == null");
        this.tier = tier;
        this.drillingFluidConsumePerTick = drillingFluidConsumePerTick;
        this.minerLogic = new MultiblockMinerLogic(this, speed, maxChunkDiameter);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeMiner(metaTileEntityId, this.tier, this.minerLogic.getWorkFrequency(),
                this.minerLogic.getMaximumChunkDiameter(),
                this.drillingFluidConsumePerTick, this.type);
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
    }

    public int getEnergyTier() {
        if (energyContainer == null) return this.tier;
        return Math.min(this.tier + 1, Math.max(this.tier, GTUtility.getFloorTierByVoltage(energyContainer.getInputVoltage())));
    }

    @Override
    public boolean drainMiningResources(@Nonnull MinedBlockType minedBlockType, boolean pipeExtended, boolean simulate) {
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
        int overclockAmount = getEnergyTier() - this.tier + 1;
        int amount = this.drillingFluidConsumePerTick * overclockAmount;
        FluidStack drained = this.inputFluidInventory.drain(DrillingFluid.getFluid(amount), !simulate);
        return drained != null && drained.amount >= amount;
    }

    @Override
    public boolean collectBlockDrops(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        NonNullList<ItemStack> drops = NonNullList.create();
        IItemHandlerModifiable inventory = this.outputInventory;

        if (this.minerLogic.isSilkTouchMode()) {
            drops.add(ToolHelper.getSilkTouchDrop(state));
        } else if (MinerUtil.applyTieredHammerDrops(GTUtility.toItem(state), drops,
                this.getEnergyTier(), RecipeMaps.MACERATOR_RECIPES, 3) == 0) {
            state.getBlock().getDrops(drops, world, pos, state, 0); // fallback
        }
        boolean result = GTTransferUtils.addItemsToItemHandler(inventory, true, drops);
        this.inventoryFull = result;
        if (result) GTTransferUtils.addItemsToItemHandler(inventory, false, drops);
        return result;
    }

    @Override
    public void onMineOperation(@Nonnull BlockPos pos, boolean isOre, boolean isOrigin) {
        if (isOre) {
            this.lastMinedOre.setPos(pos);
            this.hasLastMinedOre = true;
            this.minedOreCount++;
        }
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public MiningPipeModel getMiningPipeModel() {
        return this.type.getMiningPipeModel();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), isActive(), isWorkingEnabled());
        if (isStructureFormed()) {
            EnumFacing f = getFrontFacing().getOpposite();
            Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState,
                    translation.copy().translate(f.getXOffset(), 0, f.getZOffset()), pipeline);
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

    @Override
    public void update() {
        super.update();
        if (this.getWorld().isRemote) {
            this.minerLogic.update();
        }
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

        if (!this.isStructureFormed()) return;

        if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
            int energyContainer = getEnergyTier();
            long maxVoltage = GTValues.V[energyContainer];
            String voltageName = GTValues.VNF[energyContainer];
            textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
        }

        ITextComponent areaText;
        int area, maxArea;
        if (this.minerLogic.isChunkMode()) {
            area = this.minerLogic.getCurrentChunkDiameter();
            maxArea = this.minerLogic.getMaximumChunkDiameter();
            areaText = new TextComponentTranslation("gregtech.machine.miner.display.working_area.chunks", area, area);
        } else {
            area = this.minerLogic.getCurrentDiameter();
            maxArea = this.minerLogic.getMaximumDiameter();
            areaText = new TextComponentTranslation("gregtech.machine.miner.display.working_area", area, area);
        }
        areaText.appendText(" ").appendSibling(incrButton(area, maxArea, MinerUtil.DISPLAY_CLICK_AREA_INCR))
                .appendText(" ").appendSibling(decrButton(area, 1, MinerUtil.DISPLAY_CLICK_AREA_DECR));
        textList.add(areaText.appendText(" ").appendSibling(previewAreaButton(this.minerLogic.isPreviewEnabled())));

        int yLimit = this.minerLogic.getYLimit();
        ITextComponent value;
        ITextComponent hoverText;
        if (yLimit > 0) {
            value = new TextComponentString(String.format("%,d", yLimit));
            hoverText = new TextComponentTranslation("gregtech.machine.miner.display.y_limit.value_hover_tooltip", value.createCopy());
        } else {
            value = new TextComponentTranslation("gregtech.machine.miner.display.y_limit.no_value");
            hoverText = new TextComponentTranslation("gregtech.machine.miner.display.y_limit.value_hover_tooltip.no_value");
        }
        textList.add(new TextComponentTranslation(
                "gregtech.machine.miner.display.y_limit", new TextComponentString("")
                .appendSibling(incrButton(yLimit, Integer.MAX_VALUE, MinerUtil.DISPLAY_CLICK_Y_LIMIT_INCR))
                .appendText(" ").appendSibling(decrButton(yLimit, 0, MinerUtil.DISPLAY_CLICK_Y_LIMIT_DECR))
                .appendText(" ").appendSibling(value))
                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))));

        boolean currentValue = this.minerLogic.isRepeat();
        textList.add(new TextComponentTranslation("gregtech.machine.miner.display.repeat",
                this.minerLogic.isWorking() ?
                        new TextComponentTranslation(this.minerLogic.isRepeat() ?
                                "gregtech.machine.miner.display.enabled" :
                                "gregtech.machine.miner.display.disabled") :
                        new TextComponentTranslation(currentValue ?
                                "gregtech.machine.miner.display.toggle.enabled" :
                                "gregtech.machine.miner.display.toggle.disabled")
                                .setStyle(button(currentValue ? MinerUtil.DISPLAY_CLICK_REPEAT_DISABLE : MinerUtil.DISPLAY_CLICK_REPEAT_ENABLE))));

        ITextComponent replaceOreText = new TextComponentTranslation(
                this.minerLogic.getOreReplacement().getBlock().getTranslationKey() + ".name");
        if (!this.minerLogic.isWorking()) {
            replaceOreText = new TextComponentString("[")
                    .appendSibling(replaceOreText.setStyle(new Style().setColor(TextFormatting.AQUA)))
                    .appendText("]").setStyle(button(this.minerLogic.isReplaceOreWithAir() ?
                            MinerUtil.DISPLAY_CLICK_REPLACE_ORE_DISABLE : MinerUtil.DISPLAY_CLICK_REPLACE_ORE_ENABLE));
        }

        textList.add(new TextComponentTranslation("gregtech.machine.miner.display.replace_ore", replaceOreText));

        appendWorkingStatus(textList);

        textList.add(new TextComponentTranslation("gregtech.machine.miner.display.stats.total_mined", this.minedOreCount));
        if (this.hasLastMinedOre) {
            textList.add(new TextComponentTranslation("gregtech.machine.miner.display.stats.last_mined",
                    this.lastMinedOre.getX(), this.lastMinedOre.getY(), this.lastMinedOre.getZ()));
        }
    }

    private void appendWorkingStatus(List<ITextComponent> textList) {
        if (!this.minerLogic.isDone()) {
            MiningArea miningArea = minerLogic.getMiningArea();
            if (miningArea != null) {
                MutableBlockPos mpos = new MutableBlockPos();
                if (miningArea.getCurrentBlockPos(mpos)) {
                    if (this.minerLogic.isWorking()) {
                        textList.add(TextComponentUtil.translationWithColor(TextFormatting.GOLD,
                                "gregtech.machine.miner.display.working", mpos.getX(), mpos.getY(), mpos.getZ()));
                    } else if (!isWorkingEnabled()) {
                        textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
                    }
                    return;
                }
            }
        }
        textList.add(TextComponentUtil.translationWithColor(TextFormatting.GREEN,
                "gregtech.machine.miner.display.done"));
    }

    @Nonnull
    protected static ITextComponent previewAreaButton(boolean previewEnabled) {
        return new TextComponentTranslation(previewEnabled ?
                "gregtech.machine.miner.display.working_area.hide_preview" :
                "gregtech.machine.miner.display.working_area.preview")
                .setStyle(button(previewEnabled ? MinerUtil.DISPLAY_CLICK_AREA_PREVIEW_HIDE : MinerUtil.DISPLAY_CLICK_AREA_PREVIEW));
    }

    @Nonnull
    protected static ITextComponent incrButton(int currentValue, int maxValue, @Nonnull String event) {
        return currentValue >= maxValue ?
                new TextComponentTranslation("gregtech.machine.miner.display.incr.disabled") :
                new TextComponentTranslation("gregtech.machine.miner.display.incr").setStyle(button(event));
    }

    @Nonnull
    protected static ITextComponent decrButton(int currentValue, int minValue, @Nonnull String event) {
        return currentValue <= minValue ?
                new TextComponentTranslation("gregtech.machine.miner.display.decr.disabled") :
                new TextComponentTranslation("gregtech.machine.miner.display.decr").setStyle(button(event));
    }

    @Nonnull
    protected static Style button(@Nonnull String event) {
        return new Style().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "@!" + event));
    }

    @Override
    protected void handleDisplayClick(String componentData, Widget.ClickData clickData) {
        switch (componentData) {
            case MinerUtil.DISPLAY_CLICK_AREA_PREVIEW -> this.minerLogic.setPreviewEnabled(true);
            case MinerUtil.DISPLAY_CLICK_AREA_PREVIEW_HIDE -> this.minerLogic.setPreviewEnabled(false);
            case MinerUtil.DISPLAY_CLICK_AREA_DECR -> {
                int diff = clickData.isShiftClick ? 5 : 1;
                if (this.minerLogic.isChunkMode()) {
                    this.minerLogic.setCurrentChunkDiameter(this.minerLogic.getCurrentChunkDiameter() - diff);
                } else {
                    this.minerLogic.setCurrentDiameter(this.minerLogic.getCurrentDiameter() - diff);
                }
            }
            case MinerUtil.DISPLAY_CLICK_AREA_INCR -> {
                int diff = clickData.isShiftClick ? 5 : 1;
                if (this.minerLogic.isChunkMode()) {
                    this.minerLogic.setCurrentChunkDiameter(this.minerLogic.getCurrentChunkDiameter() + diff);
                } else {
                    this.minerLogic.setCurrentDiameter(this.minerLogic.getCurrentDiameter() + diff);
                }
            }
            case MinerUtil.DISPLAY_CLICK_Y_LIMIT_DECR -> this.minerLogic.setYLimit(Math.max(0,
                    this.minerLogic.getYLimit() - (clickData.isShiftClick ? 5 : 1)));
            case MinerUtil.DISPLAY_CLICK_Y_LIMIT_INCR -> {
                int yLimit = this.minerLogic.getYLimit() + (clickData.isShiftClick ? 5 : 1);
                if (yLimit < 0) yLimit = this.minerLogic.getYLimit() < 0 ? 0 : Integer.MAX_VALUE;
                this.minerLogic.setYLimit(yLimit);
            }
            case MinerUtil.DISPLAY_CLICK_REPEAT_ENABLE -> this.minerLogic.setRepeat(true);
            case MinerUtil.DISPLAY_CLICK_REPEAT_DISABLE -> this.minerLogic.setRepeat(false);
            case MinerUtil.DISPLAY_CLICK_REPLACE_ORE_ENABLE -> this.minerLogic.setReplaceOreWithAir(true);
            case MinerUtil.DISPLAY_CLICK_REPLACE_ORE_DISABLE -> this.minerLogic.setReplaceOreWithAir(false);
        }
    }

    @Override
    protected void addWarningText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed(), false)
                .addMaintenanceProblemLines(getMaintenanceProblems())
                .addLowPowerLine(isStructureFormed() && !drainEnergy(true))
                .addCustom(l -> {
                    if (isStructureFormed()) {
                        if (inventoryFull) {
                            l.add(new TextComponentTranslation("gregtech.machine.miner.display.inventory_full")
                                    .setStyle(new Style().setColor(TextFormatting.RED)));
                        }
                    }
                });
    }

    @Override
    protected void addErrorText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .addCustom(l -> {
                    if (!drainFluid(true)) {
                        l.add(new TextComponentTranslation("gregtech.machine.miner.multi.needsfluid")
                                .setStyle(new Style().setColor(TextFormatting.RED)));
                    }
                });
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        this.minerLogic.writeToNBT(data);

        if (this.hasLastMinedOre) {
            data.setInteger("lastMinedOreX", this.lastMinedOre.getX());
            data.setInteger("lastMinedOreY", this.lastMinedOre.getY());
            data.setInteger("lastMinedOreZ", this.lastMinedOre.getZ());
        }

        data.setInteger("minedOreCount", this.minedOreCount);

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.minerLogic.readFromNBT(data);
        if (data.hasKey("lastMinedOreX", Constants.NBT.TAG_INT)) {
            this.lastMinedOre.setPos(data.getInteger("lastMinedOreX"),
                    data.getInteger("lastMinedOreY"),
                    data.getInteger("lastMinedOreZ"));
            this.hasLastMinedOre = true;
        } else {
            this.hasLastMinedOre = false;
        }

        this.minedOreCount = Math.max(0, data.getInteger("minedOreCount"));
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

    @Override
    public double getFillPercentage(int index) {
        long drillingFluidAmount = 0, fluidCapacity = 0;
        for (IMultipleTankHandler.MultiFluidTankEntry tank : this.inputFluidInventory.getFluidTanks()) {
            FluidStack fluid = tank.getFluid();
            if (fluid != null && CommonFluidFilters.matchesFluid(fluid, Materials.DrillingFluid)) {
                drillingFluidAmount += tank.getFluidAmount();
            }
            fluidCapacity += tank.getCapacity();
        }
        return (double) drillingFluidAmount / fluidCapacity;
    }

    @Override
    public TextureArea getProgressBarTexture(int index) {
        return GuiTextures.PROGRESS_BAR_MINER_DRILLING_FLUID;
    }
}
