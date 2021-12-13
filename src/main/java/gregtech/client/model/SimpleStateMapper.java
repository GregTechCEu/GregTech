package gregtech.client.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class SimpleStateMapper implements IStateMapper {

    private final ModelResourceLocation mrl;

    public SimpleStateMapper(ModelResourceLocation mrl) {
        this.mrl = mrl;
    }

    @Override
    @Nonnull
    public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block) {
        Map<IBlockState, ModelResourceLocation> map = new Object2ObjectOpenHashMap<>(block.getBlockState().getValidStates().size());
        for (IBlockState state : block.getBlockState().getValidStates()) {
            map.put(state, mrl);
        }
        return map;
    }
}
