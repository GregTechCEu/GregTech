package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IMultiblockController;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.TextStyleClass;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;

public class MultiblockInfoProvider extends CapabilityInfoProvider<IMultiblockController> {

    @Override
    public String getID() {
        return GTValues.MODID + ":multiblock_controller_provider";
    }

    @Nonnull
    @Override
    protected Capability<IMultiblockController> getCapability() {
        return GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER;
    }

    @Override
    protected void addProbeInfo(@Nonnull IMultiblockController capability, @Nonnull IProbeInfo probeInfo, @Nonnull EntityPlayer player, @Nonnull TileEntity tileEntity, @Nonnull IProbeHitData data) {
        if (capability.isStructureFormed()) {
            probeInfo.text(TextStyleClass.OK + "{*gregtech.top.valid_structure*}");
            if (capability.isStructureObstructed()) {
                probeInfo.text(TextFormatting.RED + "{*gregtech.top.obstructed_structure*}");
            }
        } else {
            probeInfo.text(TextFormatting.RED + "{*gregtech.top.invalid_structure*}");
        }
    }
}
