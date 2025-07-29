package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.BlockOre;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;

public class BlockOreInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return GTValues.MODID + ":ore_block_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, EntityPlayer entityPlayer, World world,
                             IBlockState blockState, IProbeHitData probeHitData) {
        if (blockState.getBlock() instanceof BlockOre blockOre) {
            if (entityPlayer.isSneaking()) {
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.block_drops*}:");
                ItemStack itemDropped = OreDictUnifier.get(OrePrefix.rawOre, blockOre.material);
                IProbeInfo horizontalInfo = probeInfo
                        .horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                horizontalInfo.item(itemDropped);
                horizontalInfo.itemLabel(itemDropped);
            }
        }
    }
}
