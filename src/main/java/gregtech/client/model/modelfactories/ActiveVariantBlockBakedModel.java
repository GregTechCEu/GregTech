package gregtech.client.model.modelfactories;

import gregtech.api.block.VariantActiveBlock;
import gregtech.client.model.ModelFactory;
import gregtech.client.utils.BloomEffectUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;
import team.chisel.ctm.client.model.ModelBakedCTM;
import team.chisel.ctm.client.state.CTMExtendedState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static gregtech.common.blocks.MetaBlocks.statePropertiesToString;

public class ActiveVariantBlockBakedModel implements IBakedModel {
    private final ThreadLocal<TextureAtlasSprite> particle;

    ActiveVariantBlockBakedModel() {
        this.particle = ThreadLocal.withInitial(() -> Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite());
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> quads = Collections.emptyList();
        if (side == null) return quads;
        if (state != null) {
            ModelResourceLocation mrl;
            if (((IExtendedBlockState) state).getValue(VariantActiveBlock.ACTIVE)) {
                mrl = new ModelResourceLocation(state.getBlock().getRegistryName(),
                        "active=true," + statePropertiesToString(state.getProperties()));
            } else {
                mrl = new ModelResourceLocation(state.getBlock().getRegistryName(),
                        "active=false," + statePropertiesToString(state.getProperties()));
            }
            IBakedModel m = Minecraft.getMinecraft().blockRenderDispatcher.getBlockModelShapes().getModelManager().getModel(mrl);
            TextureAtlasSprite textureAtlasSprite = m.getParticleTexture();
            particle.set(textureAtlasSprite);
            quads = new ArrayList<>(m.getQuads(state, side, rand));
        }
        return quads;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.particle.get();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
