package gregtech.common.metatileentities.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
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
import java.util.Arrays;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.SYNC_TILE_MODE;

public class MetaTileEntityTransformer extends TieredMetaTileEntity {

    private boolean isTransformUp;

    public MetaTileEntityTransformer(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityTransformer(metaTileEntityId, getTier());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("Inverted", isTransformUp);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isTransformUp = data.getBoolean("Inverted");
        reinitializeEnergyContainer();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isTransformUp);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isTransformUp = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == SYNC_TILE_MODE) {
            this.isTransformUp = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    public boolean isInverted() {
        return isTransformUp;
    }

    public void setTransformUp(boolean inverted) {
        isTransformUp = inverted;
        if (!getWorld().isRemote) {
            reinitializeEnergyContainer();
            writeCustomData(SYNC_TILE_MODE, b -> b.writeBoolean(isTransformUp));
            notifyBlockUpdate();
            markDirty();
        }
    }

    @Override
    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        if (isTransformUp) {
            //storage = 1 amp high; input = tier / 4; amperage = 4; output = tier; amperage = 1
            this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 8L, tierVoltage, 4, tierVoltage * 4, 1);
            ((EnergyContainerHandler) this.energyContainer).setSideInputCondition(s -> s != getFrontFacing());
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
        } else {
            //storage = 1 amp high; input = tier; amperage = 1; output = tier / 4; amperage = 4
            this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 8L, tierVoltage * 4, 1, tierVoltage, 4);
            ((EnergyContainerHandler) this.energyContainer).setSideInputCondition(s -> s == getFrontFacing());
            ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s != getFrontFacing());
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        SimpleOverlayRenderer otherFaceTexture = isTransformUp ? Textures.ENERGY_IN : Textures.ENERGY_OUT;
        SimpleOverlayRenderer frontFaceTexture = isTransformUp ? Textures.ENERGY_OUT_MULTI : Textures.ENERGY_IN_MULTI;
        frontFaceTexture.renderSided(frontFacing, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier() + 1]));
        Arrays.stream(EnumFacing.values()).filter(f -> f != frontFacing)
                .forEach((f -> otherFaceTexture.renderSided(f, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]))));
    }


    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        ItemStack itemStack = playerIn.getHeldItem(hand);
        if (itemStack.getItem().getToolClasses(itemStack).contains("mallet")) {
            if (getWorld().isRemote) {
                scheduleRenderUpdate();
                return true;
            }
            ToolHelper.damageItem(itemStack, playerIn);
            if (isTransformUp) {
                setTransformUp(false);
                playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.transformer.message_transform_down",
                        energyContainer.getInputVoltage(), energyContainer.getInputAmperage(), energyContainer.getOutputVoltage(), energyContainer.getOutputAmperage()));
            } else {
                setTransformUp(true);
                playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.transformer.message_transform_up",
                        energyContainer.getInputVoltage(), energyContainer.getInputAmperage(), energyContainer.getOutputVoltage(), energyContainer.getOutputAmperage()));
            }
            return true;
        }
        return false;
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
        String lowerTierName = GTValues.VNF[getTier()];
        String higherTierName = GTValues.VNF[getTier() + 1];
        long lowerVoltage = energyContainer.getOutputVoltage();
        long higherVoltage = energyContainer.getInputVoltage();
        long lowerAmperage = energyContainer.getInputAmperage();
        long higherAmperage = energyContainer.getOutputAmperage();

        tooltip.add(I18n.format("gregtech.machine.transformer.tooltip_tool_usage"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.machine.transformer.tooltip_transform_down", lowerAmperage, higherVoltage, higherTierName, higherAmperage, lowerVoltage, lowerTierName));
        tooltip.add(I18n.format("gregtech.machine.transformer.tooltip_transform_up", higherAmperage, lowerVoltage, lowerTierName, lowerAmperage, higherVoltage, higherTierName));
    }
}
