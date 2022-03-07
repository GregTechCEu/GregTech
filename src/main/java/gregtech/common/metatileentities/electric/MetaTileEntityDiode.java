package gregtech.common.metatileentities.electric;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IPassthroughHatch;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.PipelineUtil;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import gregtech.common.tools.DamageValues;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.AMP_INDEX;

public class MetaTileEntityDiode extends MetaTileEntityMultiblockPart implements IPassthroughHatch, IMultiblockAbilityPart<IPassthroughHatch> {

    protected IEnergyContainer energyContainer;

    private static final String AMP_NBT_KEY = "amp_mode";
    private int amps;

    public MetaTileEntityDiode(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        amps = 1;
        reinitializeEnergyContainer();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDiode(metaTileEntityId, getTier());
    }

    @Override
    public int getActualComparatorValue() {
        long energyStored = energyContainer.getEnergyStored();
        long energyCapacity = energyContainer.getEnergyCapacity();
        float f = energyCapacity == 0L ? 0.0f : energyStored / (energyCapacity * 1.0f);
        return MathHelper.floor(f * 14.0f) + (energyStored > 0 ? 1 : 0);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger(AMP_NBT_KEY, amps);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.amps = data.getInteger(AMP_NBT_KEY);
        reinitializeEnergyContainer();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(amps);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.amps = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == AMP_INDEX) {
            this.amps = buf.readInt();
        }
    }

    private void setAmpMode() {
        amps = amps == 16 ? 1 : amps << 1;
        if (!getWorld().isRemote) {
            reinitializeEnergyContainer();
            writeCustomData(AMP_INDEX, b -> b.writeInt(amps));
            notifyBlockUpdate();
            markDirty();
        }
    }

    protected void reinitializeEnergyContainer() {
        long tierVoltage = GTValues.V[getTier()];
        this.energyContainer = new EnergyContainerHandler(this, tierVoltage * 16, tierVoltage, amps, tierVoltage, amps);
        ((EnergyContainerHandler) this.energyContainer).setSideInputCondition(s -> s != getFrontFacing());
        ((EnergyContainerHandler) this.energyContainer).setSideOutputCondition(s -> s == getFrontFacing());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ENERGY_IN_MULTI.renderSided(getFrontFacing(), renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()]));
        Arrays.stream(EnumFacing.values()).filter(f -> f != frontFacing).forEach(f ->
                Textures.ENERGY_OUT.renderSided(f, renderState, translation, PipelineUtil.color(pipeline, GTValues.VC[getTier()])));
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        return true;
    }

    @Override
    public boolean onRightClick(@Nonnull EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        ItemStack itemStack = playerIn.getHeldItem(hand);
        if (itemStack.getItem().getToolClasses(itemStack).contains("mallet")) {
            if (getWorld().isRemote) {
                scheduleRenderUpdate();
                return true;
            }
            ToolHelper.damageItem(itemStack, playerIn);
            setAmpMode();
            playerIn.sendMessage(new TextComponentTranslation("gregtech.machine.diode.message", amps));
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
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.diode.tooltip_general"));
        tooltip.add(I18n.format("gregtech.machine.diode.tooltip_tool_usage"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in",
                energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
    }

    @Override
    public MultiblockAbility<IPassthroughHatch> getAbility() {
        return MultiblockAbility.PASSTHROUGH_HATCH;
    }

    @Override
    public void registerAbilities(@Nonnull List<IPassthroughHatch> abilityList) {
        abilityList.add(this);
    }

    @Nonnull
    @Override
    public Class<?> getPassthroughType() {
        return IEnergyContainer.class;
    }
}
