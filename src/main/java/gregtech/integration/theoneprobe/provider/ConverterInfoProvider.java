package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.converter.ConverterTrait;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

public class ConverterInfoProvider extends CapabilityInfoProvider<ConverterTrait> {

    @Override
    public String getID() {
        return GTValues.MODID + ":converter_info_provider";
    }

    @Nonnull
    @Override
    protected Capability<ConverterTrait> getCapability() {
        return GregtechCapabilities.CAPABILITY_CONVERTER;
    }

    @Override
    protected void addProbeInfo(@Nonnull ConverterTrait capability, @Nonnull IProbeInfo probeInfo, EntityPlayer player, @Nonnull TileEntity tileEntity, @Nonnull IProbeHitData data) {
        // Info on current converter mode
        probeInfo.text(TextStyleClass.INFO + ((capability.isFeToEu()) ? "{*gregtech.top.convert_fe*}" : "{*gregtech.top.convert_eu*}"));

        // Info on the current side of the converter
        EnumFacing facing = ((IGregTechTileEntity) tileEntity).getMetaTileEntity().getFrontFacing();
        String voltageN = GTValues.VNF[GTUtility.getTierByVoltage(capability.getVoltage())];
        long amperage = capability.getBaseAmps();
        if (capability.isFeToEu()) {
            if (data.getSideHit() == facing) {
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.transform_output*} " + voltageN + TextFormatting.GREEN + " (" + amperage + "A)");
            } else {
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.transform_input*} " + TextFormatting.RED + FeCompat.toFe(capability.getVoltage(), FeCompat.ratio(true)) + " FE");
            }
        } else {
            if (data.getSideHit() == facing) {
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.transform_output*} " + TextFormatting.RED + FeCompat.toFe(capability.getVoltage(), FeCompat.ratio(false)) + " FE");
            } else {
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.transform_input*} " + voltageN + TextFormatting.GREEN + " (" + amperage + "A)");
            }
        }
    }
}
