package gregtech.mixins.ctm;

import gregtech.asm.hooks.CTMHooks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.chisel.ctm.client.model.AbstractCTMBakedModel;

import java.util.List;

//TODO, I don't think this works. Might need to wrap the return for the list of quads
@Mixin(AbstractCTMBakedModel.class)
public class AbstractCTMBakedModelMixin {

    @Inject(method = "getQuads", at = @At(value = "TAIL"), remap = false)
    public void getQuadsWithOptifine(IBlockState state, EnumFacing side, long rand,
                                     CallbackInfoReturnable<List<BakedQuad>> cir, @Local BlockRenderLayer layer,
                                     @Local Object ret) {
        CTMHooks.getQuadsWithOptiFine((List<BakedQuad>) ret, layer, (IBakedModel) this, state, side, rand);
    }
}
