package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.FluidDrillLogic;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class MetaTileEntityFluidDrill extends MultiblockWithDisplayBase implements ITieredMetaTileEntity, IWorkable {

    private final FluidDrillLogic minerLogic;
    private final int tier;

    protected IMultipleTankHandler inputFluidInventory;
    protected IMultipleTankHandler outputFluidInventory;
    protected IEnergyContainer energyContainer;

    public MetaTileEntityFluidDrill(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId);
        this.minerLogic = new FluidDrillLogic(this);
        this.tier = tier;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFluidDrill(metaTileEntityId, tier);
    }

    protected void initializeAbilities() {
        this.inputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.IMPORT_FLUIDS));
        this.outputFluidInventory = new FluidTankList(true, getAbilities(MultiblockAbility.EXPORT_FLUIDS));
        this.energyContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    private void resetTileAbilities() {
        this.inputFluidInventory = new FluidTankList(true);
        this.outputFluidInventory = new FluidTankList(true);
        this.energyContainer = new EnergyContainerList(Lists.newArrayList());
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
    }

    @Override
    protected void updateFormedValid() {
        this.minerLogic.performDrilling();
        if (!getWorld().isRemote && this.minerLogic.wasActiveAndNeedsUpdate()) {
            this.minerLogic.setWasActiveAndNeedsUpdate(false);
            this.minerLogic.setActive(false);
        }
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                .aisle("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                .aisle("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(3)
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1)))
                .where('C', states(getCasingState()))
                .where('F', states(getFrameState()))
                .where('#', any())
                .build();
    }

    private IBlockState getCasingState() {
        if (tier == GTValues.MV)
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
        if (tier == GTValues.HV)
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
        if (tier == GTValues.EV)
            return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TUNGSTENSTEEL_ROBUST);
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Nonnull
    private IBlockState getFrameState() {
        if (tier == GTValues.MV)
            return MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel);
        if (tier == GTValues.HV)
            return MetaBlocks.FRAMES.get(Materials.Titanium).getBlock(Materials.Titanium);
        if (tier == GTValues.EV)
            return MetaBlocks.FRAMES.get(Materials.TungstenSteel).getBlock(Materials.TungstenSteel);
        return MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (tier == GTValues.MV)
            return Textures.SOLID_STEEL_CASING;
        if (tier == GTValues.HV)
            return Textures.STABLE_TITANIUM_CASING;
        if (tier == GTValues.EV)
            return Textures.ROBUST_TUNGSTENSTEEL_CASING;
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (!isStructureFormed())
            return;

        if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
            long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
            String voltageName = GTValues.VNF[GTUtility.getTierByVoltage(maxVoltage)];
            textList.add(new TextComponentTranslation("gregtech.multiblock.max_energy_per_tick", maxVoltage, voltageName));
        }

        if (!minerLogic.isWorkingEnabled()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.work_paused"));

        } else if (minerLogic.isActive()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.running"));
            int currentProgress = (int) (minerLogic.getProgressPercent() * 100);
            textList.add(new TextComponentTranslation("gregtech.multiblock.progress", currentProgress));
        } else {
            textList.add(new TextComponentTranslation("gregtech.multiblock.idling"));
        }

        if (!drainEnergy(true)) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.not_enough_energy").setStyle(new Style().setColor(TextFormatting.RED)));
        }

        if (minerLogic.isInventoryFull())
            textList.add(new TextComponentTranslation("gregtech.multiblock.large_miner.invfull").setStyle(new Style().setColor(TextFormatting.RED)));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.fluid_drilling_rig.description"));
        tooltip.add(I18n.format("gregtech.machine.fluid_drilling_rig.production", getRigMultiplier()));
        tooltip.add(I18n.format("gregtech.machine.fluid_drilling_rig.depletion", GTUtility.formatNumbers(100.0 / getDepletionChance())));
        tooltip.add(I18n.format("gregtech.machine.fluid_drilling_rig.energy", GTValues.VNF[tier], GTValues.VNF[tier + 1]));
        tooltip.add(I18n.format("gregtech.machine.fluid_drilling_rig.overclock"));
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    public int getRigMultiplier() {
        if (this.tier == GTValues.MV)
            return 1;
        if (this.tier == GTValues.HV)
            return 16;
        if (this.tier == GTValues.EV)
            return 64;
        return 1;
    }

    public int getDepletionChance() {
        if (this.tier == GTValues.MV)
            return 1;
        if (this.tier == GTValues.HV)
            return 2;
        if (this.tier == GTValues.EV)
            return 8;
        return 1;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.FLUID_RIG_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.minerLogic.isActive(), this.minerLogic.isWorkingEnabled());
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.minerLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.minerLogic.setWorkingEnabled(isActivationAllowed);
    }

    public boolean fillTanks(FluidStack stack, boolean simulate) {
        return GTTransferUtils.addFluidsToFluidHandler(outputFluidInventory, simulate, Collections.singletonList(stack));
    }

    public int getEnergyTier() {
        int minVoltage = Math.max(this.tier, GTUtility.getTierByVoltage(energyContainer.getInputVoltage()));
        return Math.min(minVoltage, this.tier + 1);
    }

    public long getEnergyInputPerSecond() {
        return energyContainer.getInputPerSec();
    }

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
    public boolean hasMaintenanceMechanics() {
        return false;
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
    public int getProgress() {
        return minerLogic.getProgressTime();
    }

    @Override
    public int getMaxProgress() {
        return FluidDrillLogic.MAX_PROGRESS;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE)
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    protected boolean shouldShowVoidingModeButton() {
        return false;
    }
}
