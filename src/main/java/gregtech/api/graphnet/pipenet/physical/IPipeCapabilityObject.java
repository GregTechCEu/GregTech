package gregtech.api.graphnet.pipenet.physical;

import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.NotNull;

public interface IPipeCapabilityObject extends ICapabilityProvider {

    void init(@NotNull PipeTileEntity tile, @NotNull PipeCapabilityWrapper wrapper);

    @Override
    default boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
        return getCapability(capability, facing) != null;
    }
}
