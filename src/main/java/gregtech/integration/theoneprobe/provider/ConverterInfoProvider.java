package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.FeCompat;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.converter.ConverterTrait;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

public class ConverterInfoProvider extends CapabilityInfoProvider<ConverterTrait> {

    @Override
    public String getID() {
        return "gregtech:converter_info_provider";
    }

    @Override
    protected Capability<ConverterTrait> getCapability() {
        return GregtechCapabilities.CAPABILITY_CONVERTER;
    }

    @Override
    protected void addProbeInfo(ConverterTrait capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit) {
        // Info on current converter mode
        IProbeInfo pane = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
        if (capability.isFeToEu()) {
            pane.text(TextStyleClass.INFO + "{*gregtech.top.convert_fe*}");
        } else {
            pane.text(TextStyleClass.INFO + "{*gregtech.top.convert_eu*}");
        }

        // Info on the current side of the converter
        pane = probeInfo.vertical(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
        MetaTileEntity mte = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
        String voltageN = GTValues.VNF[GTUtility.getTierByVoltage(capability.getVoltage())];
        long amperage = capability.getBaseAmps();
        if (capability.isFeToEu()) {
            if (sideHit == mte.getFrontFacing()) {
                pane.text(TextStyleClass.INFO + "{*gregtech.top.transform_output*} " + voltageN + TextFormatting.GREEN + " (" + amperage + "A)");
            } else {
                pane.text(TextStyleClass.INFO + "{*gregtech.top.transform_input*} " + TextFormatting.RED + FeCompat.toFe(capability.getVoltage()) + "FE");
            }
        } else {
            if (sideHit == mte.getFrontFacing()) {
                pane.text(TextStyleClass.INFO + "{*gregtech.top.transform_output*} " + TextFormatting.RED + FeCompat.toFe(capability.getVoltage()) + "FE");
            } else {
                pane.text(TextStyleClass.INFO + "{*gregtech.top.transform_input*} " + voltageN + TextFormatting.GREEN + " (" + amperage + "A)");
            }
        }
    }
}
