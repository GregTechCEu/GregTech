package gregtech.common.metatileentities.electric.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.LaserContainerHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MTETrait;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.render.SimpleOverlayRenderer;
import gregtech.api.render.Textures;
import gregtech.api.util.PipelineUtil;
import gregtech.common.pipelike.laser.tile.LaserContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static gregtech.api.metatileentity.multiblock.MultiblockAbility.OUTPUT_LASER;

public class MetaTileEntityLaserHatch  extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<LaserContainer> {
    private final boolean isExportHatch;

    private final LaserContainer Laser;
    private final long  MAPCAP;


    public MetaTileEntityLaserHatch(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier);
        this.MAPCAP = GTValues.V[tier] * 102400;
        this.isExportHatch  = isExportHatch;
        if (isExportHatch) {
            this.Laser =LaserContainerHandler.LaseremitterContainer(this, MAPCAP,  GTValues.V[tier],102400);
        } else {
            this.Laser = LaserContainerHandler.LaserreceiverContainer(this, MAPCAP, GTValues.V[tier], 102400);
        }
    }
    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityLaserHatch(metaTileEntityId, getTier(), isExportHatch);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = isExportHatch ? Textures.ENERGY_OUT_MULTI : Textures.ENERGY_IN_MULTI;
            renderer.renderSided(getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
        }
    }


    public MultiblockAbility<LaserContainer> getAbility() {
        return isExportHatch ? MultiblockAbility.OUTPUT_LASER : MultiblockAbility.INPUT_LASER;
    }

    @Override
    public void registerAbilities(List<LaserContainer> abilityList) { {
        abilityList.add(Laser);
    }

    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }


    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        String tierName = GTValues.VN[getTier()];

        if (isExportHatch) {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", Laser.getOutputLaser(), tierName +" Laser"));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out_till", Laser.getOutputParallel())+" Laser");
        } else {
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", Laser.getInputLaser(), tierName)+" Laser");
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till", Laser.getInputParallel())+" Laser");
        }
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", MAPCAP)+" Laser");
    }
}
