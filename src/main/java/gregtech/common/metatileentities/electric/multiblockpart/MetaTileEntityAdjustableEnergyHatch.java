package gregtech.common.metatileentities.electric.multiblockpart;

import codechicken.lib.raytracer.CuboidRayTraceResult;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.AMP_INDEX;

public class MetaTileEntityAdjustableEnergyHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<IEnergyContainer> {

    private static final int[] ampRange = {2, 4, 8, 16};
    private int ampIndex;

    private final boolean isExportHatch;
    private IEnergyContainer energyContainer;

    public MetaTileEntityAdjustableEnergyHatch(ResourceLocation metaTileEntityId, int tier, boolean isExportHatch) {
        super(metaTileEntityId, tier);
        this.isExportHatch = isExportHatch;
        this.ampIndex = 1;
        if (isExportHatch) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this, GTValues.V[tier] * 32L, GTValues.V[tier], 2);
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            this.energyContainer = EnergyContainerHandler.receiverContainer(this, GTValues.V[tier] * 32L, GTValues.V[tier], 2);
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityAdjustableEnergyHatch(metaTileEntityId, getTier(), isExportHatch);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer;
            switch (ampIndex) {
                case 1:
                    renderer = isExportHatch ? Textures.ENERGY_OUT_MULTI : Textures.ENERGY_IN_MULTI;
                    break;

                case 2:
                    renderer = isExportHatch ? Textures.ENERGY_OUT_HI : Textures.ENERGY_IN_HI;
                    break;
                case 3:
                    renderer = isExportHatch ? Textures.ENERGY_OUT_ULTRA : Textures.ENERGY_IN_ULTRA;
                    break;
                default:
                    renderer = isExportHatch ? Textures.ENERGY_OUT : Textures.ENERGY_IN;
            }

            renderer.renderSided(getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("ampIndex", ampIndex);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.ampIndex = data.getInteger("ampIndex");
        reinitializeEnergyContainer();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(ampIndex);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.ampIndex = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == AMP_INDEX) {
            this.ampIndex = buf.readInt();
            scheduleRenderUpdate();
        }
    }

    protected void incrementAmpIndex() {
        this.ampIndex = (this.ampIndex + 1) % ampRange.length;
        if (!getWorld().isRemote) {
            reinitializeEnergyContainer();
            writeCustomData(AMP_INDEX, b -> b.writeInt(ampIndex));
            getHolder().notifyBlockUpdate();
            markDirty();
        }
    }

    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        if (isExportHatch) {
            this.energyContainer = EnergyContainerHandler.emitterContainer(this, tierVoltage * 32L, tierVoltage, ampRange[ampIndex]);
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            this.energyContainer = EnergyContainerHandler.receiverContainer(this, tierVoltage * 32L, tierVoltage, ampRange[ampIndex]);
        }
    }

    @Override
    public boolean onScrewdriverClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (getWorld().isRemote) {
            scheduleRenderUpdate();
            return true;
        }

        incrementAmpIndex();
        playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.energy_hatch_adjustable.message_adjust",
                isExportHatch ? energyContainer.getOutputAmperage() : energyContainer.getInputAmperage()));

        return true;
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

        tooltip.add(I18n.format("gregtech.machine.transformer_adjustable.tooltip_tool_usage"));

        if (isExportHatch) {
            tooltip.add(I18n.format("gregtech.machine.energy_hatch.adjustable.output.tooltip"));
            tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_out", energyContainer.getOutputVoltage(), tierName));
            tooltip.add(I18n.format("gregtech.universal.tooltip.amperage_out_till", energyContainer.getOutputAmperage()));
        } else {
            tooltip.add(I18n.format("gregtech.machine.energy_hatch.adjustable.input.tooltip"));
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
}
