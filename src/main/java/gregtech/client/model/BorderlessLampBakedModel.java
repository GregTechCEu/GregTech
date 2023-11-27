package gregtech.client.model;

import gregtech.client.model.lamp.LampBakedModel;
import gregtech.client.utils.RenderUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BorderlessLampBakedModel extends LampBakedModel {

    // for each 6 side plus "no face" quads
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] quads = new List[7];

    public BorderlessLampBakedModel(ModelResourceLocation modelLocation) {
        super(modelLocation);
    }

    public BorderlessLampBakedModel(IBakedModel model) {
        super(model);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        int index = (side == null ? 6 : side.getIndex());
        if (this.quads[index] == null) {
            List<BakedQuad> quads = new ArrayList<>();
            for (BakedQuad q : getModel().getQuads(null, side, 0)) {
                quads.add(RenderUtil.makeEmissive(q));
            }
            return this.quads[index] = quads;
        }
        return this.quads[index];
    }
}
