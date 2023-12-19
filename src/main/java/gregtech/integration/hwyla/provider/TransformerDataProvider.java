package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.electric.MetaTileEntityTransformer;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TransformerDataProvider extends ElectricContainerDataProvider {

    public static final TransformerDataProvider INSTANCE = new TransformerDataProvider();

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig(GTValues.MODID, "gregtech.transformer");
    }

    @Override
    protected NBTTagCompound getNBTData(IEnergyContainer capability, NBTTagCompound tag) {
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setLong("InputVoltage", capability.getInputVoltage());
        subTag.setLong("OutputVoltage", capability.getOutputVoltage());
        subTag.setLong("InputAmperage", capability.getInputAmperage());
        subTag.setLong("OutputAmperage", capability.getOutputAmperage());
        boolean isTransformUp = capability.getInputVoltage() < capability.getOutputVoltage();
        subTag.setBoolean("IsTransformUp", isTransformUp);
        EnumFacing frontFace = EnumFacing.UP; // so ide is happy
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (isTransformUp) {
                if (capability.outputsEnergy(facing)) {
                    frontFace = facing;
                    break;
                }
            } else {
                if (capability.inputsEnergy(facing)) {
                    frontFace = facing;
                    break;
                }
            }
        }
        subTag.setInteger("FrontFacing", frontFace.getIndex());
        tag.setTag("gregtech.MetaTileEntityTransformer", subTag);
        return tag;
    }

    @NotNull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                     IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.transformer") ||
                !(accessor.getTileEntity() instanceof IGregTechTileEntity gtte) ||
                !(gtte.getMetaTileEntity() instanceof MetaTileEntityTransformer)) {
            return tooltip;
        }
        if (accessor.getNBTData().hasKey("gregtech.MetaTileEntityTransformer")) {
            NBTTagCompound tag = accessor.getNBTData().getCompoundTag("gregtech.MetaTileEntityTransformer");
            final long inputVoltage = tag.getLong("InputVoltage");
            final long outputVoltage = tag.getLong("OutputVoltage");
            final long inputAmperage = tag.getLong("InputAmperage");
            final long outputAmperage = tag.getLong("OutputAmperage");
            final boolean isTransformUp = tag.getBoolean("IsTransformUp");
            final EnumFacing frontFacing = EnumFacing.byIndex(tag.getInteger("FrontFacing"));

            StringBuilder input = new StringBuilder()
                    .append(" ")
                    .append(GTValues.VNF[GTUtility.getTierByVoltage(inputVoltage)])
                    .append(TextFormatting.RESET)
                    .append(" (")
                    .append(inputAmperage)
                    .append("A)");

            StringBuilder output = new StringBuilder()
                    .append(" ")
                    .append(GTValues.VNF[GTUtility.getTierByVoltage(outputVoltage)])
                    .append(TextFormatting.RESET)
                    .append(" (")
                    .append(outputAmperage)
                    .append("A)");

            // Step Up/Step Down line
            tooltip.add((isTransformUp ? I18n.format("gregtech.top.transform_up") :
                    I18n.format("gregtech.top.transform_down")) + input + " -> " + output);

            // Input/Output side line
            EnumFacing hitFace = accessor.getSide();
            if ((isTransformUp && hitFace != frontFacing) || (!isTransformUp && hitFace == frontFacing)) {
                tooltip.add(I18n.format("gregtech.top.transform_input") + input);
            } else {
                tooltip.add(I18n.format("gregtech.top.transform_output") + output);
            }
        }

        return tooltip;
    }
}
