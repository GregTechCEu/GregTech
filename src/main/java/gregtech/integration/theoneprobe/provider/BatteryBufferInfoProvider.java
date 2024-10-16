package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.metatileentities.electric.MetaTileEntityBatteryBuffer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
import org.jetbrains.annotations.NotNull;

public class BatteryBufferInfoProvider extends CapabilityInfoProvider<IEnergyContainer> {

    @Override
    public String getID() {
        return GTValues.MODID + ":battery_buffer_provider";
    }

    @Override
    protected @NotNull Capability<IEnergyContainer> getCapability() {
        return GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
    }

    @Override
    protected void addProbeInfo(IEnergyContainer capability, IProbeInfo probeInfo, EntityPlayer player,
                                TileEntity tileEntity, IProbeHitData data) {
        if (tileEntity instanceof IGregTechTileEntity iGregTechTileEntity) {
            MetaTileEntity metaTileEntity = iGregTechTileEntity.getMetaTileEntity();

            if (metaTileEntity instanceof MetaTileEntityBatteryBuffer) {
                long averageIn = capability.getInputPerSec() / 20;
                ITextComponent averageInFormatted = TextComponentUtil.translationWithColor(
                        TextFormatting.GREEN,
                        "gregtech.battery_buffer.average_input.top",
                        TextComponentUtil.stringWithColor(
                                TextFormatting.WHITE,
                                player.isSneaking() || averageIn < 10_000 ?
                                        TextFormattingUtil.formatNumbers(averageIn) + " EU/t" :
                                        ElementProgress.format(averageIn, NumberFormat.COMPACT, "EU/t")));
                probeInfo.text(averageInFormatted.getFormattedText());

                long averageOut = capability.getOutputPerSec() / 20;
                ITextComponent averageOutFormatted = TextComponentUtil.translationWithColor(
                        TextFormatting.RED,
                        "gregtech.battery_buffer.average_output.top",
                        TextComponentUtil.stringWithColor(
                                TextFormatting.WHITE,
                                player.isSneaking() || averageOut < 10_000 ?
                                        TextFormattingUtil.formatNumbers(averageOut) + " EU/t" :
                                        ElementProgress.format(averageOut, NumberFormat.COMPACT, "EU/t")));
                probeInfo.text(averageOutFormatted.getFormattedText());
            }
        }
    }
}
