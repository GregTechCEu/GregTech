package gregtech.integration.hwyla.provider;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import org.jetbrains.annotations.NotNull;

public abstract class CapabilityDataProvider<T> implements IWailaDataProvider {

    public abstract void register(@NotNull IWailaRegistrar registrar);

    protected abstract @NotNull Capability<T> getCapability();

    protected boolean allowDisplaying(@NotNull T capability) {
        return true;
    }

    /** Set server-side data needed for HWYLA to the passed NBT tag. */
    protected abstract NBTTagCompound getNBTData(T capability, NBTTagCompound tag);

    @Override
    public @NotNull NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world,
                                              BlockPos pos) {
        if (te != null) {
            T capability = te.getCapability(getCapability(), null);
            if (capability != null && allowDisplaying(capability)) {
                return getNBTData(capability, tag);
            }
        }
        return tag;
    }
}
