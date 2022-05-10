package gregtech.api.block;

import gregtech.api.GregTechAPI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

/**
 * @see gregtech.common.blocks.BlockWireCoil
 *
 * Register new heating coils in an Event Handler for {@link GregTechAPI.RegisterEvent<IHeatingCoilBlock>}
 *
 * @param <T> the class containing the heating Coil Block's coil types
 */
public interface IHeatingCoilBlock<T extends Enum<T> & IHeatingCoilBlockType & IStringSerializable> {

    /**
     *
     * @return the Enum containing the Heating Coil Block's coil types
     */
    @Nonnull
    Class<T> getCoilTypeEnum();

    IBlockState getState(IHeatingCoilBlockType type);
}
