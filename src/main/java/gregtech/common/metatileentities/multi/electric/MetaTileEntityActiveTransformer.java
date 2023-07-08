package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.LaserBuffer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.BlockFusionCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityActiveTransformer extends MultiblockWithDisplayBase implements IControllable {
    private boolean isWorkingEnabled = true;
    private IEnergyContainer energyInputContainer;
    private IEnergyContainer energyOutputContainer;
    private LaserBuffer buffer;
    private ILaserContainer laserInContainer;
    private boolean isActive = true;

    public MetaTileEntityActiveTransformer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.energyInputContainer = new EnergyContainerList(new ArrayList<>());
        this.energyOutputContainer = new EnergyContainerList(new ArrayList<>());
        this.buffer = null;
        this.laserInContainer = null;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityActiveTransformer(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        double maintenancePenalty = getMaintenancePenalty();
        if (maintenancePenalty <= 0) {
            setActive(false);
            return;
        }
        if (buffer == null) {
            return;
        }

        long maxEnergyInput = (long) (energyInputContainer.getEnergyStored() * maintenancePenalty);
        long maxLaserInput = 0;
        if (laserInContainer != null) {
            maxLaserInput = (long) (laserInContainer.getEnergyStored() * maintenancePenalty);
        }

        energyInputContainer.removeEnergy(buffer.changeEnergy(maxEnergyInput));
        if (laserInContainer != null) {
            laserInContainer.removeEnergy(buffer.changeEnergy(maxLaserInput));
        }

        buffer.removeEnergy(energyOutputContainer.addEnergy(buffer.getEnergyStored()));
    }

    private double getMaintenancePenalty() {
        int maintenanceProblems = getNumMaintenanceProblems();
        if (maintenanceProblems > 3) {
            return 0;
        } else {
            return 1 - maintenanceProblems * 0.15;
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        energyInputContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        energyOutputContainer = new EnergyContainerList(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        if (energyInputContainer.getEnergyCapacity() > 0 && (buffer == null || buffer.getEnergyCapacity() != energyInputContainer.getEnergyCapacity())) {
            buffer = new LaserBuffer(energyInputContainer.getEnergyCapacity());
        } else if (energyOutputContainer.getEnergyCapacity() > 0 && (buffer == null || buffer.getEnergyCapacity() != energyOutputContainer.getEnergyCapacity())) {
            buffer = new LaserBuffer(energyOutputContainer.getEnergyCapacity());
        }
        if (getAbilities(MultiblockAbility.INPUT_LASER).size() == 1) {
            laserInContainer = getAbilities(MultiblockAbility.INPUT_LASER).get(0);
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyInputContainer = new EnergyContainerList(new ArrayList<>());
        this.energyOutputContainer = new EnergyContainerList(new ArrayList<>());
        this.buffer = null;
        this.laserInContainer = null;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XCX", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('X', states(getCasingState()).setMinGlobalLimited(12).or(getHatchPredicates()))
                .where('S', selfPredicate())
                .where('C', states(MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL)))
                .build();
    }

    private TraceabilityPredicate getHatchPredicates() {
        return abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(3, 1)
                .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setMaxGlobalLimited(3, 1))
                .or(abilities(MultiblockAbility.INPUT_LASER).setMaxGlobalLimited(1))
                .or(abilities(MultiblockAbility.OUTPUT_LASER).setMaxGlobalLimited(1))
                // Disallow the config maintenance hatch because that would probably break the conservation of energy
                .or(metaTileEntities(MetaTileEntities.MAINTENANCE_HATCH,
                        MetaTileEntities.AUTO_MAINTENANCE_HATCH, MetaTileEntities.CLEANING_MAINTENANCE_HATCH).setExactLimit(1));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        double maintenancePenalty = getMaintenancePenalty();
        if (maintenancePenalty < 1.0) {
            textList.add(new TextComponentTranslation("gregtech.machine.active_transformer.rate", maintenancePenalty));
        }
        if (isStructureFormed() && buffer != null) {
            textList.add(new TextComponentTranslation("gregtech.machine.active_transformer.buffer_size", TextFormattingUtil.formatNumbers(buffer.getEnergyCapacity())));
            textList.add(new TextComponentTranslation("gregtech.machine.active_transformer.buffer_full",
                    (buffer.getEnergyStored() * 100.0) / buffer.getEnergyCapacity(), TextFormattingUtil.formatNumbers(buffer.getEnergyStored())));
        }
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.HIGH_POWER_CASING;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.COMPUTER_CASING.getState(BlockComputerCasing.CasingType.HIGH_POWER_CASING);
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.DATA_BANK_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(), this.isWorkingEnabled());
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.isWorkingEnabled = isWorkingAllowed;
        markDirty();
        World world = getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(isWorkingEnabled));
        }
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.isActive;
    }

    public void setActive(boolean active) {
        if (this.isActive != active) {
            this.isActive = active;
            markDirty();
            World world = getWorld();
            if (world != null && !world.isRemote) {
                writeCustomData(GregtechDataCodes.WORKABLE_ACTIVE, buf -> buf.writeBoolean(active));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isActive", this.isActive);
        data.setBoolean("isWorkingEnabled", this.isWorkingEnabled);
        data.setLong("bufferEnergyStored", this.buffer != null ? this.buffer.getEnergyStored() : 0L);
        data.setLong("bufferEnergyCapacity", this.buffer != null ? this.buffer.getEnergyCapacity() : 0L);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isActive = data.getBoolean("isActive");
        this.isWorkingEnabled = data.getBoolean("isWorkingEnabled");
        if (data.getLong("bufferEnergyStored") != 0 && data.getLong("bufferEnergyCapacity") != 0) {
            this.buffer = new LaserBuffer(data.getLong("bufferEnergyCapacity"));
            this.buffer.changeEnergy(data.getLong("bufferEnergyStored"));
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isActive);
        buf.writeBoolean(this.isWorkingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
        this.isWorkingEnabled = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @Nonnull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.isActive = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.isWorkingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.active_transformer.tooltip.1"));
    }

    public ILaserContainer getBuffer() {
        if (buffer != null) {
            return buffer;
        } else if (isStructureFormed() && getAbilities(MultiblockAbility.INPUT_LASER).size() == 1) {
            return getAbilities(MultiblockAbility.INPUT_LASER).get(0);
        }
        return null;
    }
}
