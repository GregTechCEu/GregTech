package gregtech.client.model;

import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.client.model.modelfactories.MaterialBlockModelLoader;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MaterialStateMapper implements IStateMapper {

    private final Function<IBlockState, MaterialIconSet> materialFunction;
    private final MaterialIconType iconType;

    public MaterialStateMapper(MaterialIconType iconType, Function<IBlockState, MaterialIconSet> materialFunction) {
        this.materialFunction = materialFunction;
        this.iconType = iconType;
    }

    @Override
    public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block) {
        return block.getBlockState().getValidStates().stream().collect(Collectors.toMap(
                s -> s,
                s -> MaterialBlockModelLoader.registerBlockModel(
                        this.iconType,
                        this.materialFunction.apply(s))));
    }
}
