package gregtech.api.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

import org.jetbrains.annotations.NotNull;

public interface IStateSoundType {

    @NotNull
    SoundType getSoundType(@NotNull IBlockState state);
}
