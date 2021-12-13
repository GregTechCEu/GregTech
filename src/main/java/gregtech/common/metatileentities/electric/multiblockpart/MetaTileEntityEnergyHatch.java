package gregtech.common.metatileentities.electric.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.PipelineUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityEnergyHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IEnergyContainer> {

    private final boolean isExportHatch;
    private final IEnergyContainer energyContainer;

    public MetaTileEntityEnergyHatch(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier);
        this.isExportHatch = isExportHatch;
        if (isExportHatch) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this, GTValues.V[tier] * 128L, GTValues.V[tier], 1);
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            this.energyContainer = EnergyContainerHandler.receiverContainer(this, GTValues.V[tier] * 16L, GTValues.V[tier], 2);
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityEnergyHatch(metaTileEntityId, getTier(), isExportHatch);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = isExportHatch ? Textures.ENERGY_OUT_MULTI : Textures.ENERGY_IN_MULTI;
            renderer.renderSided(getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
        }
    }

    @Override
    public MultiblockAbility<IEnergyContainer> getAbility() {
        return isExportHatch ? MultiblockAbility.OUTPUT_ENERGY : MultiblockAbility.INPUT_ENERGY;
    }

    @Override
    public void registerAbilities(List<IEnergyContainer> abilityList) {
        abilityList.add(energyContainer);
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
            tooltip.add(I18n.format("gregtech.machine.energy_hatch.output.tooltip"));
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", energyContainer.getOutputVoltage(), tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out_till", energyContainer.getOutputAmperage()));
        } else {
            tooltip.add(I18n.format("gregtech.machine.energy_hatch.input.tooltip"));
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till", energyContainer.getInputAmperage()));
        }
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }
}
