package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.unification.ore.StoneType;
import gregtech.common.blocks.BlockOre;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.*;

public class BlockOreInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return GTValues.MODID + ":ore_block_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo probeInfo, EntityPlayer entityPlayer, World world,
                             IBlockState blockState, IProbeHitData probeHitData) {
        if (blockState.getBlock() instanceof BlockOre) {
            StoneType stoneType = blockState.getValue(((BlockOre) blockState.getBlock()).STONE_TYPE);
            if (entityPlayer.isSneaking() && !stoneType.shouldBeDroppedAsItem) {
                probeInfo.text(TextStyleClass.INFO + "{*gregtech.top.block_drops*}:");
                ItemStack itemDropped = blockState.getBlock().getItem(world, probeHitData.getPos(), blockState);
                IProbeInfo horizontalInfo = probeInfo
                        .horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
                horizontalInfo.item(itemDropped);
                horizontalInfo.itemLabel(itemDropped);
            }
        }
    }
}
