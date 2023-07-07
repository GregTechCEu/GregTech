package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.capability.impl.LaserBufferWrapper;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityActiveTransformer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityLaserHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<ILaserContainer>, IDataInfoProvider {
    private final boolean isOutput;
    private ILaserContainer laserContainer;
    public MetaTileEntityLaserHatch(ResourceLocation metaTileEntityId, boolean isOutput) {
        super(metaTileEntityId, GTValues.ZPM);
        this.isOutput = isOutput;
        this.laserContainer = new LaserBufferWrapper(this, null, isOutput);
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        calculateLaserContainer(controllerBase);
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        this.laserContainer = new LaserBufferWrapper(this, null, isOutput);
    }

    private void calculateLaserContainer(MultiblockControllerBase controllerBase) {
        if (isOutput && (controllerBase instanceof MetaTileEntityActiveTransformer || controllerBase == null)) {
            // TODO: Handle null values by propagating up the net
            if (this.laserContainer instanceof LaserBufferWrapper bufferWrapper) {
                bufferWrapper.setController((MetaTileEntityActiveTransformer) controllerBase);
            }
        } else if (!isOutput) {
            EnumFacing side = getFrontFacing();
            TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(side));
            EnumFacing oppositeSide = side.getOpposite();
            if (tileEntity != null && tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_LASER, oppositeSide)) {
                ILaserContainer laserContainer = tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_LASER, oppositeSide);
                if (laserContainer != null && !laserContainer.inputsEnergy(oppositeSide)) {
                    this.laserContainer = laserContainer;
                }
            }
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLaserHatch(metaTileEntityId, isOutput);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public MultiblockAbility<ILaserContainer> getAbility() {
        return isOutput ? MultiblockAbility.OUTPUT_LASER : MultiblockAbility.INPUT_LASER;
    }

    @Override
    public void registerAbilities(List<ILaserContainer> abilityList) {
        calculateLaserContainer(null);
        abilityList.add(this.laserContainer);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            if (isOutput) {
                Textures.LASER_SOURCE.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.LASER_TARGET.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> textList = new ArrayList<>();
        textList.add(new TextComponentTranslation("gregtech.machine.active_transformer.buffer_size", TextFormattingUtil.formatNumbers(laserContainer.getEnergyCapacity())));
        if (laserContainer.getEnergyCapacity() != 0) {
            textList.add(new TextComponentTranslation("gregtech.machine.active_transformer.buffer_full",
                    (laserContainer.getEnergyStored() / laserContainer.getEnergyCapacity()) * 100.0, TextFormattingUtil.formatNumbers(laserContainer.getEnergyStored())));
        }
        return textList;
    }

    // TODO: add tooltips
}
