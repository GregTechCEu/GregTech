package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.PipelineUtil;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityEnergyHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IEnergyContainer> {

    private final boolean isExportHatch;
    private final int amperage;
    private final IEnergyContainer energyContainer;

    public MetaTileEntityEnergyHatch(ResourceLocation metaTileEntityId, int tier, int amperage, boolean isExportHatch) {
        super(metaTileEntityId, tier);
        this.isExportHatch = isExportHatch;
        this.amperage = amperage;
        if (isExportHatch) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this, GTValues.V[tier] * 64L * amperage, GTValues.V[tier], amperage);
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            this.energyContainer = EnergyContainerHandler.receiverContainer(this, GTValues.V[tier] * 16L * amperage, GTValues.V[tier], amperage);
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityEnergyHatch(metaTileEntityId, getTier(), amperage, isExportHatch);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            getOverlay().renderSided(getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
        }
    }

    @Override
    public void update() {
        super.update();
        checkWeatherOrTerrainExplosion(getTier(), getTier() * 10, energyContainer);
    }

    @Nonnull
    private SimpleOverlayRenderer getOverlay() {
        if (isExportHatch) {
            if (amperage <= 2) {
                return Textures.ENERGY_OUT_MULTI;
            } else if (amperage <= 4) {
                return Textures.ENERGY_OUT_HI;
            } else {
                return Textures.ENERGY_OUT_ULTRA;
            }
        } else {
            if (amperage <= 2) {
                return Textures.ENERGY_IN_MULTI;
            } else if (amperage <= 4) {
                return Textures.ENERGY_IN_HI;
            } else {
                return Textures.ENERGY_IN_ULTRA;
            }
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
        String tierName = GTValues.VNF[getTier()];

        if (isExportHatch) {
            if (amperage > 2) {
                tooltip.add(I18n.format("gregtech.machine.energy_hatch.output_hi_amp.tooltip"));
            } else {
                tooltip.add(I18n.format("gregtech.machine.energy_hatch.output.tooltip"));
            }
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", energyContainer.getOutputVoltage(), tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out_till", energyContainer.getOutputAmperage()));
        } else {
            if (amperage > 2) {
                tooltip.add(I18n.format("gregtech.machine.energy_hatch.input_hi_amp.tooltip"));
            } else {
                tooltip.add(I18n.format("gregtech.machine.energy_hatch.input.tooltip"));
            }
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_in_till", energyContainer.getInputAmperage()));
        }
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public boolean canRenderFrontFaceX() {
        return isExportHatch;
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        // override here is gross, but keeps things in order despite
        // IDs being out of order, due to EV 4A hatches being added later
        if (this == MetaTileEntities.ENERGY_INPUT_HATCH[0]) {
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.ENERGY_INPUT_HATCH) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.ENERGY_OUTPUT_HATCH) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.ENERGY_INPUT_HATCH_4A) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.ENERGY_INPUT_HATCH_16A) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.ENERGY_OUTPUT_HATCH_4A) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
            for (MetaTileEntityEnergyHatch hatch : MetaTileEntities.ENERGY_OUTPUT_HATCH_16A) {
                if (hatch != null) subItems.add(hatch.getStackForm());
            }
        }
    }

    @Override
    public void doExplosion(float explosionPower) {
        if (getController() != null)
            getController().explodeMultiblock();
        else {
            super.doExplosion(explosionPower);
        }
    }
}
