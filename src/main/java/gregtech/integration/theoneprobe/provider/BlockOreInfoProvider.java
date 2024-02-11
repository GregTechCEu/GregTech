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

    }
}
