package gregtech.api.block;

import net.minecraft.block.state.IBlockState;

import org.jetbrains.annotations.NotNull;

public interface IStateSpawnControl {

    boolean canCreatureSpawn(@NotNull IBlockState state);
}
