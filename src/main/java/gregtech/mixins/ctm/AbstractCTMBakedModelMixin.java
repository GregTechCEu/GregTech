package gregtech.mixins.ctm;

import gregtech.client.utils.CTMHooks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;

import java.util.List;

@Mixin(AbstractCTMBakedModel.class)
public class AbstractCTMBakedModelMixin {

    @ModifyReturnValue(method = "getQuads", at = @At(value = "RETURN", ordinal = 1))
    private List<BakedQuad> getQuadsWithOptifine(List<BakedQuad> original, @Local(ordinal = 0) BlockRenderLayer layer,
                                                 @Local(ordinal = 0) IBlockState state,
                                                 @Local(ordinal = 0) EnumFacing side, @Local(ordinal = 0) long rand) {
        return CTMHooks.getQuadsWithOptiFine(original, layer, (IBakedModel) this, state, side, rand);
    }
}
