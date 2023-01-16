package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.miner.MultiblockMinerLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static gregtech.api.unification.material.Materials.DrillingFluid;

public class MetaTileEntityLargeMiner extends MultiblockWithDisplayBase implements IMiner, IControllable, IDataInfoProvider {

    private static final int CHUNK_LENGTH = 16;

    private final Material material;
    private final int tier;

    private IEnergyContainer energyContainer;
    protected IMultipleTankHandler inputFluidInventory;
    protected IItemHandlerModifiable outputInventory;

    private boolean silkTouch = false;
    private boolean chunkMode = false;

    private boolean isInventoryFull = false;

    private final int drillingFluidConsumePerTick;
    private final String romanNumeralString;

    private final MultiblockMinerLogic minerLogic;

    public MetaTileEntityLargeMiner(ResourceLocation metaTileEntityId, int tier, int speed, int maximumChunkDiameter, int fortune, Material material, int drillingFluidConsumePerTick) {
        super(metaTileEntityId);
        this.material = material;
        this.tier = tier;
        this.drillingFluidConsumePerTick = drillingFluidConsumePerTick;
        this.romanNumeralString = GTUtility.romanNumeralString(fortune);
        this.minerLogic = new MultiblockMinerLogic(this, fortune, speed, maximumChunkDiameter * CHUNK_LENGTH / 2, getBaseTexture(null), RecipeMaps.MACERATOR_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeMiner(metaTileEntityId, this.tier, this.minerLogic.getSpeed(), this.minerLogic.getMaximumRadius() * 2 / CHUNK_LENGTH, this.minerLogic.getFortune(), getMaterial(), getDrillingFluidConsumePerTick());
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        if (this.minerLogic.isActive())
            this.minerLogic.setActive(false);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    private void initializeAbilities() {
        this.inputFluidInventory = new FluidTankList(false, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputInventory = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.minerLogic.setVoltageTier(GTUtility.getTierByVoltage(this.energyContainer.getInputVoltage()));
        this.minerLogic.setOverclockAmount(Math.max(1, GTUtility.getTierByVoltage(this.energyContainer.getInputVoltage()) - this.tier));
        this.minerLogic.initPos(getPos(), this.minerLogic.getCurrentRadius());
    }

    private void resetTileAbilities() {
        this.inputFluidInventory = new FluidTankList(true);
        this.outputInventory = new ItemStackHandler(0);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    public int getEnergyTier() {
        if (energyContainer == null) return this.tier;
        return Math.min(this.tier + 1 , Math.max(this.tier, GTUtility.getFloorTierByVoltage(energyContainer.getInputVoltage())));
    }

    @Override
    public boolean drainEnergy(boolean simulate) {
        long energyToDrain = GTValues.VA[getEnergyTier()];
        long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-energyToDrain);
            return true;
        }
        return false;
    }

    @Override
    public boolean drainFluid(boolean simulate) {
        FluidStack drillingFluid = DrillingFluid.getFluid(this.drillingFluidConsumePerTick * this.minerLogic.getOverclockAmount());
        FluidStack fluidStack = inputFluidInventory.getTankAt(0).getFluid();
        if (fluidStack != null && fluidStack.isFluidEqual(DrillingFluid.getFluid(1)) && fluidStack.amount >= drillingFluid.amount) {
            if (!simulate)
                inputFluidInventory.drain(drillingFluid, true);
            return true;
        }
        return false;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.minerLogic.isWorking(), this.isWorkingEnabled());
        minerLogic.renderPipe(renderState, translation, pipeline);
    }

    @Override
    protected void updateFormedValid() {
        this.minerLogic.performMining();
        if (!getWorld().isRemote && this.minerLogic.wasActiveAndNeedsUpdate()) {
            this.minerLogic.setWasActiveAndNeedsUpdate(false);
            this.minerLogic.setActive(false);
        }
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return material == null ? null : FactoryBlockPattern.start()
                .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                .where('S', selfPredicate())
                .where('X', states(getCasingState())
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS).setExactLimit(1).setPreviewCount(1))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3).setPreviewCount(1)))
                .where('C', states(getCasingState()))
                .where('F', getFramePredicate())
                .where('#', any())
                .build();
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.machine.large_miner.description")};
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        int workingAreaChunks = this.minerLogic.getCurrentRadius() * 2 / CHUNK_LENGTH;
        tooltip.add(I18n.format("gregtech.machine.large_miner.modes"));
        tooltip.add(I18n.format("gregtech.machine.large_miner.production"));
        tooltip.add(I18n.format("gregtech.machine.large_miner.fluid_usage", getDrillingFluidConsumePerTick(), DrillingFluid.getLocalizedName()));
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
            int workingAreaChunks = this.minerLogic.getCurrentRadius() * 2 / CHUNK_LENGTH;
            int workingArea = getWorkingArea(minerLogic.getCurrentRadius());
            textList.add(new TextComponentTranslation("gregtech.machine.miner.startx", this.minerLogic.getX().get() == Integer.MAX_VALUE ? 0 : this.minerLogic.getX().get()));
            textList.add(new TextComponentTranslation("gregtech.machine.miner.starty", this.minerLogic.getY().get() == Integer.MAX_VALUE ? 0 : this.minerLogic.getY().get()));
            textList.add(new TextComponentTranslation("gregtech.machine.miner.startz", this.minerLogic.getZ().get() == Integer.MAX_VALUE ? 0 : this.minerLogic.getZ().get()));
            if (this.minerLogic.isChunkMode())
                textList.add(new TextComponentTranslation(I18n.format("gregtech.universal.tooltip.working_area_chunks", workingAreaChunks, workingAreaChunks)));
            else
                textList.add(new TextComponentTranslation(I18n.format("gregtech.universal.tooltip.working_area", workingArea, workingArea)));
            if (this.minerLogic.isDone())
                textList.add(new TextComponentTranslation("gregtech.machine.miner.done").setStyle(new Style().setColor(TextFormatting.GREEN)));
            else if (this.minerLogic.isWorking())
                textList.add(new TextComponentTranslation("gregtech.machine.miner.working").setStyle(new Style().setColor(TextFormatting.GOLD)));
            else if (!this.isWorkingEnabled())
                textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));
            if (this.isInventoryFull)
                textList.add(new TextComponentTranslation("gregtech.machine.miner.invfull").setStyle(new Style().setColor(TextFormatting.RED)));
            if (!drainFluid(true))
                textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.needsfluid").setStyle(new Style().setColor(TextFormatting.RED)));
            if (!drainEnergy(true))
                textList.add(new TextComponentTranslation("gregtech.machine.miner.needspower").setStyle(new Style().setColor(TextFormatting.RED)));
        }
    }

    private void addDisplayText2(List<ITextComponent> textList) {
        if (this.isStructureFormed()) {
            ITextComponent mCoords = new TextComponentString("    ")
                .appendSibling(new TextComponentTranslation("gregtech.machine.miner.minex", this.minerLogic.getMineX().get()))
                .appendText("\n    ")
                .appendSibling(new TextComponentTranslation("gregtech.machine.miner.miney", this.minerLogic.getMineY().get()))
                .appendText("\n    ")
                .appendSibling(new TextComponentTranslation("gregtech.machine.miner.minez", this.minerLogic.getMineZ().get()));
            textList.add(mCoords);
        }
    }

    public IBlockState getCasingState() {
        if (this.material.equals(Materials.Titanium))
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
        if (this.material.equals(Materials.TungstenSteel))
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Nonnull
    private TraceabilityPredicate getFramePredicate() {
        if (this.material.equals(Materials.Titanium))
            return frames(Materials.Titanium);
        if (this.material.equals(Materials.TungstenSteel))
            return frames(Materials.TungstenSteel);
        return frames(Materials.Steel);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (this.material.equals(Materials.Titanium))
            return Textures.STABLE_TITANIUM_CASING;
        if (this.material.equals(Materials.TungstenSteel))
            return Textures.ROBUST_TUNGSTENSTEEL_CASING;
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("chunkMode", new NBTTagInt(chunkMode ? 1 : 0));
        data.setTag("silkTouch", new NBTTagInt(silkTouch ? 1 : 0));
        return this.minerLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        chunkMode = data.getInteger("chunkMode") != 0;
        silkTouch = data.getInteger("silkTouch") != 0;
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

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        if (this.tier == 5)
            return Textures.LARGE_MINER_OVERLAY_ADVANCED;
        if (this.tier == 6)
            return Textures.LARGE_MINER_OVERLAY_ADVANCED_2;
        return Textures.LARGE_MINER_OVERLAY_BASIC;
    }

    public long getMaxVoltage() {
        return GTValues.V[GTUtility.getTierByVoltage(energyContainer.getInputVoltage())];
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.extendedBuilder();
        builder.image(7, 4, 162, 121, GuiTextures.DISPLAY);
        builder.label(11, 9, this.getMetaFullName(), 0xFFFFFF);
        builder.widget((new AdvancedTextWidget(11, 19, this::addDisplayText,
                0xFFFFFF)).setMaxWidthLimit(139).setClickHandler(this::handleDisplayClick));
        builder.widget((new AdvancedTextWidget(63, 30, this::addDisplayText2,
                0xFFFFFF)).setMaxWidthLimit(68).setClickHandler(this::handleDisplayClick));
        builder.bindPlayerInventory(entityPlayer.inventory, 134);

        builder.widget(new ToggleButtonWidget(133, 107, 18, 18,
                this.minerLogic::isChunkMode, this.minerLogic::setChunkMode).setButtonTexture(GuiTextures.BUTTON_CHUNK_MODE)
                .setTooltipText("gregtech.gui.chunkmode"));
        builder.widget(new ToggleButtonWidget(151, 107, 18, 18,
                this.minerLogic::isSilkTouchMode, this.minerLogic::setSilkTouchMode).setButtonTexture(GuiTextures.BUTTON_SILK_TOUCH_MODE)
                .setTooltipText("gregtech.gui.silktouch"));

        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote || !this.isStructureFormed())
            return true;

        if (!this.isActive()) {
            int currentRadius = this.minerLogic.getCurrentRadius();
            
            if (this.minerLogic.isChunkMode()){
                if (currentRadius - CHUNK_LENGTH <= 0)
                    this.minerLogic.setCurrentRadius(this.minerLogic.getMaximumRadius());
                else
                    this.minerLogic.setCurrentRadius(currentRadius - CHUNK_LENGTH);
                int workingAreaChunks = this.minerLogic.getCurrentRadius() * 2 / CHUNK_LENGTH;
                playerIn.sendMessage(new TextComponentTranslation(I18n.format("gregtech.universal.tooltip.working_area_chunks", workingAreaChunks, workingAreaChunks)));
            } else {
                if (currentRadius - CHUNK_LENGTH / 2 <= 0)
                    this.minerLogic.setCurrentRadius(this.minerLogic.getMaximumRadius());
                else
                    this.minerLogic.setCurrentRadius(currentRadius - CHUNK_LENGTH / 2);
                int workingArea = getWorkingArea(minerLogic.getCurrentRadius());
                playerIn.sendMessage(new TextComponentTranslation(I18n.format("gregtech.universal.tooltip.working_area", workingArea, workingArea)));
            }

            this.minerLogic.resetArea();

																																			 
        } else {
            playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.miner.errorradius"));
        }
        return true;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean isInventoryFull() {
        return this.isInventoryFull;
    }

    @Override
    public void setInventoryFull(boolean isFull) {
        this.isInventoryFull = isFull;
    }

    public Material getMaterial() {
        return material;
    }

    public int getTier() {
        return this.tier;
    }

    public int getDrillingFluidConsumePerTick() {
        return this.drillingFluidConsumePerTick;
    }

    public String getRomanNumeralString() {
        return this.romanNumeralString;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.minerLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.minerLogic.setWorkingEnabled(isActivationAllowed);
    }

    public int getMaxChunkRadius() {
        return this.minerLogic.getMaximumRadius() / CHUNK_LENGTH;
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
        return minerLogic.isActive() && isWorkingEnabled();
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        int workingArea = getWorkingArea(minerLogic.getCurrentRadius());
        return Collections.singletonList(new TextComponentTranslation(I18n.format("gregtech.universal.tooltip.working_area", workingArea, workingArea)));
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
}
