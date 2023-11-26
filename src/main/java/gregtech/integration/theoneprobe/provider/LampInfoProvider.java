package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.common.blocks.BlockLamp;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;

public class LampInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return GTValues.MODID + ":lamp_info_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo info, EntityPlayer player, World world, IBlockState state,
                             IProbeHitData hitData) {
        if (state.getBlock() instanceof BlockLamp) {
            BlockLamp lamp = (BlockLamp) state.getBlock();
            boolean inverted = lamp.isInverted(state);
            boolean bloomEnabled = lamp.isBloomEnabled(state);
            boolean lightEnabled = lamp.isLightEnabled(state);

            if (inverted) info.text("{*tile.gregtech_lamp.tooltip.inverted*}");
            if (!bloomEnabled) info.text("{*tile.gregtech_lamp.tooltip.no_bloom*}");
            if (!lightEnabled) info.text("{*tile.gregtech_lamp.tooltip.no_light*}");
        }
    }
}
