package gregtech.api.util;

import gregtech.api.unification.ore.StoneType;

import net.minecraft.block.state.IBlockState;

public interface IBlockOre {

    IBlockState getOreBlock(StoneType stoneType);
}
