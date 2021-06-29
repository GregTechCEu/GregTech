package gregtech.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.Lists;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.capability.impl.laserContainerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.multiblock.PatternMatchContext;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.pipelike.laser.tile.CableLaserContainer;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.common.pipelike.laser.tile.LaserContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

//Todo fix crash

public class MetaTileEntityEnergyConverterGTEUtoLaser extends MultiblockWithDisplayBase {

    private final MultiblockAbility<?>[] ALLOWED_ABILITIES =new MultiblockAbility[] {MultiblockAbility.INPUT_ENERGY, GregtechCapabilities.OUTPUT_LASER};
    private IEnergyContainer input;
    private LaserContainer output;
    private boolean isActive = false;
    private long currentDrain = 0;
    private long drain = 0;
    DecimalFormat formatter = new DecimalFormat("#0.0");
    public MetaTileEntityEnergyConverterGTEUtoLaser(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);}
    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
        if (isActive)
            setActive(false);
    }
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechCapabilities.LASER_CAPABILITY || capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {

            return (T) this;
        }
        else if (capability == GregtechTileCapabilities.CAPABILITY_COVERABLE.cast(this)){;

        }
        return null;
    }
    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeAbilities();
    }

    private void initializeAbilities() {
        this.input = new EnergyContainerList(getAbilities(MultiblockAbility.INPUT_ENERGY));
        this.output = new laserContainerList(getAbilities(GregtechCapabilities.OUTPUT_LASER));
    }
    private void resetTileAbilities() {
        this.input = new EnergyContainerList(Lists.newArrayList());
        this.output = new laserContainerList(Lists.newArrayList());
    }
    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            if (!isActive)
                setActive(true);
            if (output.getLaserStored() < output.getLaserCapacity()) {
                if (input.getEnergyStored() < output.getLaserCapacity() - output.getLaserStored()) {
                    long drain = input.getEnergyStored();
                    output.addLaser(drain);
                    input.removeEnergy(drain);
                    currentDrain += drain;
                } else {
                    long left = output.getLaserCapacity() - output.getLaserStored();
                    output.addLaser(left);
                    input.removeEnergy(left);
                    currentDrain += left;
                }
            }
            if (getTimer() % 20 == 0) {
                drain = currentDrain / 20;
                currentDrain = 0;
            }
        }
    }
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("IMTZ")
                .where('T',abilityPartPredicate(GregtechCapabilities.OUTPUT_LASER ))
                .where('Z',abilityPartPredicate( MultiblockAbility.OUTPUT_ENERGY))
                .where('M', selfPredicate())
                .where('I', abilityPartPredicate(MultiblockAbility.INPUT_ENERGY ))
                .build();
    }
    public IBlockState getCasingState() {
        return  MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }


    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityEnergyConverterGTEUtoLaser(metaTileEntityId);
    }
    @Override
    protected boolean checkStructureComponents(List<IMultiblockPart> parts, Map<MultiblockAbility<Object>, List<Object>> abilities) {
        return abilities.containsKey(GregtechCapabilities.OUTPUT_LASER) && abilities.containsKey(MultiblockAbility.INPUT_ENERGY)&& abilities.containsKey(MultiblockAbility.OUTPUT_ENERGY);
    }
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.MULTIBLOCK_WORKABLE_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), isActive);
    }
    protected void setActive(boolean active) {
        this.isActive = active;
        markDirty();
        if (!getWorld().isRemote) {
            writeCustomData(1, buf -> buf.writeBoolean(active));
        }
    }
    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 1) {
            this.isActive = buf.readBoolean();
            getHolder().scheduleChunkForRenderUpdate();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
    }
}
