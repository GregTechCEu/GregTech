package gregtech.api.metatileentity.multiblock;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;

public interface IBatteryDataProvider {

    IBatteryData getData(IBlockState state);

    interface IBatteryData {
        int getTier();

        long getCapacity();

        @Nonnull String getName();
    }
}
