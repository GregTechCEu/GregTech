package gregtech.integration.theoneprobe.provider;

import gregtech.api.util.GTLog;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public abstract class CapabilityInfoProvider<T> implements IProbeInfoProvider {

    protected abstract Capability<T> getCapability();

    protected abstract void addProbeInfo(T capability, IProbeInfo probeInfo, TileEntity tileEntity, EnumFacing sideHit);

    protected boolean allowDisplaying(T capability) {
        return true;
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        if (blockState.getBlock().hasTileEntity(blockState)) {
            EnumFacing sideHit = data.getSideHit();
            TileEntity tileEntity = world.getTileEntity(data.getPos());
            if (tileEntity == null) return;
            try {
                T resultCapability = tileEntity.getCapability(getCapability(), null);
                if (resultCapability != null && allowDisplaying(resultCapability)) {
                    addProbeInfo(resultCapability, probeInfo, tileEntity, sideHit);
                }
            } catch (ClassCastException ignored) {
            } catch (Throwable e) {
                GTLog.logger.info("Bad One probe Implem: {} {} {}", e.getClass().toGenericString(), e.getMessage(), e.getStackTrace()[0].getClassName());
                e.printStackTrace();
            }
        }
    }
}
