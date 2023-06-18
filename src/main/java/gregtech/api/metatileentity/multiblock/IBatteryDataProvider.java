package gregtech.api.metatileentity.multiblock;

import net.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.NotNull;

public interface IBatteryDataProvider {

    IBatteryData getData(IBlockState state);

    interface IBatteryData {
        int getTier();

        long getCapacity();

        @NotNull String getName();
    }
}
