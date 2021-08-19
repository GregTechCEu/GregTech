package gregtech.api.util;

import gregtech.api.unification.material.Material;
import net.minecraft.block.state.IBlockState;

public interface IBlockOre {

    IBlockState getOreBlock(Material stoneType);

}
