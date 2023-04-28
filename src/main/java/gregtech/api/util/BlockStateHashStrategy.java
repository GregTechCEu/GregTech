package gregtech.api.util;

import it.unimi.dsi.fastutil.Hash;
import net.minecraft.block.state.IBlockState;

import java.util.Objects;

/**
 * Hash Strategy for IBlockState, which takes into account the state's Block
 */
public class BlockStateHashStrategy implements Hash.Strategy<IBlockState> {

    public static final BlockStateHashStrategy STRATEGY = new BlockStateHashStrategy();

    @Override
    public int hashCode(IBlockState o) {
        if (o == null || o.getBlock().getRegistryName() == null) return 0;
        int hash = o.getBlock().getRegistryName().hashCode();
        return hash * 31 + o.getProperties().hashCode();
    }

    @Override
    public boolean equals(IBlockState a, IBlockState b) {
        // IBlockState implementations have correct equals() methods, so delegate to that
        return Objects.equals(a, b);
    }
}
