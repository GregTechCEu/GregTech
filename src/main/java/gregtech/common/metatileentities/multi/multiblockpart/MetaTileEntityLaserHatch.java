package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.ILaserContainer;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityActiveTransformer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class MetaTileEntityLaserHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<ILaserContainer> {
    private final boolean isOutput;
    public MetaTileEntityLaserHatch(ResourceLocation metaTileEntityId, boolean isOutput) {
        super(metaTileEntityId, GTValues.ZPM);
        this.isOutput = isOutput;
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
        if (isOutput && getController() instanceof MetaTileEntityActiveTransformer activeTransformer) {
            ILaserContainer buffer = activeTransformer.getBuffer();
            if (buffer != null) {
                // TODO: Handle null values by propagating up the net
                abilityList.add(buffer);
            }
        } else if (!isOutput) {
            EnumFacing side = getFrontFacing();
            TileEntity tileEntity = getWorld().getTileEntity(getPos().offset(side));
            EnumFacing oppositeSide = side.getOpposite();
            if (tileEntity != null && tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_LASER, oppositeSide)) {
                ILaserContainer laserContainer = tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_LASER, oppositeSide);
                if (laserContainer != null && !laserContainer.inputsEnergy(oppositeSide)) {
                    abilityList.add(laserContainer);
                }
            }
        }
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

    // TODO: add tooltips
}
