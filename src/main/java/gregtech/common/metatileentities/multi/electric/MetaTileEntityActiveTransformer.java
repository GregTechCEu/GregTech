package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.*;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.LaserContainerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.BlockFusionCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
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
    private ILaserContainer laserInputContainer;
    private ILaserContainer laserOutputContainer;
    private boolean isActive = true;

    public MetaTileEntityActiveTransformer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
        this.energyInputContainer = new EnergyContainerList(new ArrayList<>());
        this.energyOutputContainer = new EnergyContainerList(new ArrayList<>());
        this.laserInputContainer = new LaserContainerList(new ArrayList<>());
        this.laserOutputContainer = new LaserContainerList(new ArrayList<>());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityActiveTransformer(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        long maxEnergyInput = energyInputContainer.getEnergyStored();
        long maxLaserInput = laserInputContainer.getEnergyStored();

        if (maxLaserInput == 0 && maxEnergyInput == 0) {
            setActive(false);
            return;
        }

        long energyUsed = 0;
        long laserUsed = 0;

        energyUsed += energyOutputContainer.addEnergy(maxEnergyInput - energyUsed);
        laserUsed += energyOutputContainer.addEnergy(maxLaserInput - laserUsed);
        energyUsed += laserOutputContainer.addEnergy(maxEnergyInput - energyUsed);
        laserUsed += laserOutputContainer.addEnergy(maxLaserInput - laserUsed);

        if (energyUsed == 0 && laserUsed == 0) {
            setActive(false);
        } else {
            setActive(true);
            energyInputContainer.removeEnergy(energyUsed);
            laserInputContainer.removeEnergy(laserUsed);
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.energyInputContainer = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.energyOutputContainer = new EnergyContainerList(getAbilities(MultiblockAbility.OUTPUT_ENERGY));
        this.laserInputContainer = new LaserContainerList(getAbilities(MultiblockAbility.INPUT_LASER));
        this.laserOutputContainer = new LaserContainerList(getAbilities(MultiblockAbility.OUTPUT_LASER));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.energyInputContainer = new EnergyContainerList(new ArrayList<>());
        this.energyOutputContainer = new EnergyContainerList(new ArrayList<>());
        this.laserInputContainer = new LaserContainerList(new ArrayList<>());
        this.laserOutputContainer = new LaserContainerList(new ArrayList<>());
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "XCX", "XXX")
                .aisle("XXX", "XSX", "XXX")
                .where('X', states(getCasingState()).setMinGlobalLimited(12)
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMaxGlobalLimited(3, 1))
                        .or(abilities(MultiblockAbility.OUTPUT_ENERGY).setMaxGlobalLimited(3, 1))
                        .or(abilities(MultiblockAbility.INPUT_LASER).setMaxGlobalLimited(3, 1))
                        .or(abilities(MultiblockAbility.OUTPUT_LASER).setMaxGlobalLimited(3, 1)))
                .where('S', selfPredicate())
                .where('C', states(MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.SUPERCONDUCTOR_COIL)))
                .build();
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
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isActive = data.getBoolean("isActive");
        this.isWorkingEnabled = data.getBoolean("isWorkingEnabled");
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
}
