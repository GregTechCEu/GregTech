package gregtech.integration.hwyla.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.metatileentities.converter.ConverterTrait;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConverterDataProvider extends CapabilityDataProvider<ConverterTrait> {

    public static final ConverterDataProvider INSTANCE = new ConverterDataProvider();

    @Override
    public void register(@NotNull IWailaRegistrar registrar) {
        registrar.registerBodyProvider(this, TileEntity.class);
        registrar.registerNBTProvider(this, TileEntity.class);
        registrar.addConfig("GregTech", "gregtech.converter");
    }

    @Override
    protected @NotNull Capability<ConverterTrait> getCapability() {
        return GregtechCapabilities.CAPABILITY_CONVERTER;
    }

    @Override
    protected NBTTagCompound getNBTData(ConverterTrait capability, NBTTagCompound tag) {
        NBTTagCompound subTag = new NBTTagCompound();
        subTag.setBoolean("IsFeToEu", capability.isFeToEu());
        subTag.setLong("Voltage", capability.getVoltage());
        subTag.setInteger("BaseAmps", capability.getBaseAmps());
        subTag.setInteger("FrontFacing", capability.getMetaTileEntity().getFrontFacing().getIndex());
        tag.setTag("gregtech.MetaTileEntityConverter", subTag);
        return tag;
    }

    @Override
    public @NotNull List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor,
                                              IWailaConfigHandler config) {
        if (!config.getConfig("gregtech.converter") || accessor.getTileEntity() == null) {
            return tooltip;
        }

        if (accessor.getNBTData().hasKey("gregtech.MetaTileEntityConverter")) {
            NBTTagCompound tag = accessor.getNBTData().getCompoundTag("gregtech.MetaTileEntityConverter");
            final boolean isFeToEu = tag.getBoolean("IsFeToEu");
            final long voltage = tag.getLong("Voltage");
            final int amperage = tag.getInteger("BaseAmps");
            final EnumFacing frontFacing = EnumFacing.byIndex(tag.getInteger("FrontFacing"));
            final String voltageName = GTValues.VNF[GTUtility.getTierByVoltage(voltage)];

            if (isFeToEu) {
                tooltip.add(I18n.format("gregtech.top.convert_fe"));
                if (accessor.getSide() == frontFacing) {
                    tooltip.add(I18n.format("gregtech.top.transform_output") + " " + voltageName +
                            TextFormatting.RESET + " (" + TextFormattingUtil.formatNumbers(amperage) + "A)");
                } else {
                    tooltip.add(I18n.format("gregtech.top.transform_input") + " " +
                            TextFormattingUtil.formatNumbers(FeCompat.toFe(voltage * amperage, FeCompat.ratio(true))) +
                            " FE");
                }
            } else {
                tooltip.add(I18n.format("gregtech.top.convert_eu"));
                if (accessor.getSide() == frontFacing) {
                    tooltip.add(I18n.format("gregtech.top.transform_output") + " " +
                            TextFormattingUtil.formatNumbers(FeCompat.toFe(voltage * amperage, FeCompat.ratio(false))) +
                            " FE");
                } else {
                    tooltip.add(I18n.format("gregtech.top.transform_input") + " " + voltageName + TextFormatting.RESET +
                            " (" + TextFormattingUtil.formatNumbers(amperage) + "A)");
                }
            }
        }
        return tooltip;
    }
}
