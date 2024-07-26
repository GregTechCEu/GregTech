package gregtech.client.renderer.pipe;

import com.github.bsideup.jabel.Desugar;

import gregtech.client.renderer.pipe.AbstractPipeModel;

import gregtech.client.renderer.pipe.util.CacheKey;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class PipeItemModel<K extends CacheKey> implements IBakedModel {

    private final AbstractPipeModel<K> basis;
    private final K key;
    private final int argb;

    public PipeItemModel(AbstractPipeModel<K> basis, K key, int argb) {
        this.basis = basis;
        this.key = key;
        this.argb = argb;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return basis.getQuads(key, (byte) 0b1100, (byte) 0b0, (byte) 0b0, argb, null, (byte) 0b0);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return basis.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return basis.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return basis.getParticleTexture();
    }

    @Override
    public @NotNull ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
