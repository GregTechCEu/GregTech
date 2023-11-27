package gregtech.integration.theoneprobe.provider;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import org.jetbrains.annotations.NotNull;

public abstract class CapabilityInfoProvider<T> implements IProbeInfoProvider {

    @NotNull
    protected abstract Capability<T> getCapability();

    protected abstract void addProbeInfo(T capability, IProbeInfo probeInfo, EntityPlayer player, TileEntity tileEntity,
                                         IProbeHitData data);

    protected boolean allowDisplaying(T capability) {
        return true;
    }

    @Override
    public void addProbeInfo(@NotNull ProbeMode mode, @NotNull IProbeInfo probeInfo, @NotNull EntityPlayer player,
                             @NotNull World world, @NotNull IBlockState blockState, @NotNull IProbeHitData data) {
        if (blockState.getBlock().hasTileEntity(blockState)) {
            TileEntity tileEntity = world.getTileEntity(data.getPos());
            if (tileEntity == null) return;
            T resultCapability = tileEntity.getCapability(getCapability(), null);
            if (resultCapability != null && allowDisplaying(resultCapability)) {
                addProbeInfo(resultCapability, probeInfo, player, tileEntity, data);
            }
        }
    }
}
