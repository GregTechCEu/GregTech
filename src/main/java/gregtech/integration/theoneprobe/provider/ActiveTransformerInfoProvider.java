package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IMultiblockController;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityActiveTransformer;

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

public class ActiveTransformerInfoProvider extends CapabilityInfoProvider<IMultiblockController> {

    @Override
    public String getID() {
        return GTValues.MODID + ":active_transformer_info_provider";
    }

    @Override
    protected boolean allowDisplaying(@NotNull IMultiblockController capability) {
        return capability instanceof MetaTileEntityActiveTransformer;
    }

    @Override
    protected @NotNull Capability<IMultiblockController> getCapability() {
        return GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER;
    }

    @Override
    protected void addProbeInfo(IMultiblockController capability, IProbeInfo probeInfo, EntityPlayer player,
                                TileEntity tileEntity, IProbeHitData data) {
        if (capability.isStructureFormed() && capability instanceof MetaTileEntityActiveTransformer activeTransformer &&
                activeTransformer.isActive()) {
            long averageIO = activeTransformer.getAverageIOLastSec();
            ITextComponent text = TextComponentUtil.translationWithColor(
                    TextFormatting.AQUA,
                    "gregtech.multiblock.active_transformer.average_io",
                    TextComponentUtil.stringWithColor(TextFormatting.WHITE,
                            player.isSneaking() || averageIO < 10_000 ?
                                    TextFormattingUtil.formatNumbers(averageIO) + " EU/t" :
                                    ElementProgress.format(averageIO, NumberFormat.COMPACT, "EU/t")));
            probeInfo.text(text.getFormattedText());
        }
    }
}
