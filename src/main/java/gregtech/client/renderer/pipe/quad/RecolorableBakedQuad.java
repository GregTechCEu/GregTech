package gregtech.client.renderer.pipe.quad;

import gregtech.api.util.reference.WeakHashSet;
import gregtech.client.renderer.pipe.util.SpriteInformation;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class RecolorableBakedQuad extends BakedQuad {

    private final Int2ObjectOpenHashMap<RecolorableBakedQuad> cache;
    private final SpriteInformation spriteInformation;

    /**
     * Create a new recolorable quad based off of a baked quad prototype.
     * @param prototype the prototype.
     * @param spriteInformation the sprite information of this baked quad.
     */
    public RecolorableBakedQuad(BakedQuad prototype, SpriteInformation spriteInformation) {
        this(prototype, prototype.getTintIndex(), spriteInformation, new Int2ObjectOpenHashMap<>());
    }

    protected RecolorableBakedQuad(BakedQuad prototype, int tintIndex,
                                SpriteInformation spriteInformation, Int2ObjectOpenHashMap<RecolorableBakedQuad> cache) {
        super(prototype.vertexData, tintIndex, prototype.getFace(), spriteInformation.sprite(),
                prototype.shouldApplyDiffuseLighting(), prototype.getFormat());
        this.spriteInformation = spriteInformation;
        this.cache = cache;
    }

    /**
     * Get a recolorable quad based off of this quad but aligned with the given color data.
     * @param data the color data.
     * @return a quad colored based on the color data.
     */
    public RecolorableBakedQuad withColor(ColorData data) {
        if (!spriteInformation.colorable()) return this;
        int argb = data.colorsARGB()[spriteInformation.colorID()];
        return cache.computeIfAbsent(argb, (c) -> new RecolorableBakedQuad(this, c, this.spriteInformation, this.cache));
    }
}
