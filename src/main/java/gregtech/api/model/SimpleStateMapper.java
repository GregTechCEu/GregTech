package gregtech.api.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class SimpleStateMapper implements IStateMapper {

    private final ModelResourceLocation mrl;

    public SimpleStateMapper(ResourceLocation rl) {
        this.mrl = new ModelResourceLocation(rl, "normal");
    }

    public SimpleStateMapper(ModelResourceLocation mrl) {
        this.mrl = mrl;
    }

    @Override
    public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block) {
        Map<IBlockState, ModelResourceLocation> map = new Object2ObjectOpenHashMap<>(block.getBlockState().getValidStates().size());
        for (IBlockState state : block.getBlockState().getValidStates()) {
            map.put(state, mrl);
        }
        return map;
    }

}
