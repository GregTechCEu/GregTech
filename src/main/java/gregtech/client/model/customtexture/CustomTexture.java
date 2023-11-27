package gregtech.client.model.customtexture;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;

@SideOnly(Side.CLIENT)
public class CustomTexture {

    private final MetadataSectionCTM meta;

    public CustomTexture(MetadataSectionCTM meta) {
        this.meta = meta;
    }

    public BlockRenderLayer getLayer() {
        return meta == null ? null : meta.layer;
    }

    public BakedQuad transformQuad(BakedQuad quad) {
        if (meta == null) {
            return quad;
        }
        return rebake(meta.blockLight, meta.skyLight, quad);
    }

    public static BakedQuad rebake(int blockLight, int skyLight, BakedQuad quad) {
        Builder builder = new Builder(quad.getFormat(), quad.getSprite());
        quad.pipe(builder);
        VertexFormat format = builder.vertexFormat;
        // Sorry OF users
        boolean hasLightmap = (blockLight > 0 || skyLight > 0) && !FMLClientHandler.instance().hasOptifine();
        if (hasLightmap) {
            if (format == DefaultVertexFormats.ITEM) { // ITEM is convertable to BLOCK (replace normal+padding with
                                                       // lmap)
                format = DefaultVertexFormats.BLOCK;
            } else if (!format.getElements().contains(DefaultVertexFormats.TEX_2S)) { // Otherwise, this format is
                                                                                      // unknown, add TEX_2S if it does
                                                                                      // not exist
                format = new VertexFormat(format).addElement(DefaultVertexFormats.TEX_2S);
            }
        }

        UnpackedBakedQuad.Builder unpackedBuilder = new UnpackedBakedQuad.Builder(format);
        unpackedBuilder.setQuadOrientation(builder.quadOrientation);
        unpackedBuilder.setQuadTint(builder.quadTint);
        unpackedBuilder.setApplyDiffuseLighting(builder.applyDiffuseLighting);
        unpackedBuilder.setTexture(builder.sprite);

        Vector2f[] uvs = builder.uvs();
        for (int v = 0; v < 4; v++) {
            for (int i = 0; i < format.getElementCount(); i++) {
                VertexFormatElement ele = format.getElement(i);
                // Stuff for Light or UV
                if (ele.getUsage() == VertexFormatElement.EnumUsage.COLOR) {
                    unpackedBuilder.put(i, 1, 1, 1, 1);
                } else if (ele.getUsage() == VertexFormatElement.EnumUsage.UV) {
                    if (ele.getIndex() == 1) {
                        unpackedBuilder.put(i, ((float) blockLight * 0x20) / 0xFFFF,
                                ((float) skyLight * 0x20) / 0xFFFF);
                    } else if (ele.getIndex() == 0) {
                        Vector2f uv = uvs[v];
                        unpackedBuilder.put(i, uv.x, uv.y, 0, 1);
                    }
                } else {
                    unpackedBuilder.put(i, builder.data.get(ele.getUsage()).get(v));
                }
            }
        }

        return unpackedBuilder.build();
    }

    public static class Builder implements IVertexConsumer {

        public final VertexFormat vertexFormat;
        public final TextureAtlasSprite sprite;
        public int quadTint = -1;
        public EnumFacing quadOrientation;
        public boolean applyDiffuseLighting;
        public final ListMultimap<VertexFormatElement.EnumUsage, float[]> data = MultimapBuilder
                .enumKeys(VertexFormatElement.EnumUsage.class).arrayListValues().build();

        public Builder(VertexFormat vertexFormat, TextureAtlasSprite sprite) {
            this.vertexFormat = vertexFormat;
            this.sprite = sprite;
        }

        @Override
        public void put(int element, @Nullable float... data) {
            if (data == null) return;
            float[] copy = new float[data.length];
            System.arraycopy(data, 0, copy, 0, data.length);
            VertexFormatElement ele = vertexFormat.getElement(element);
            this.data.put(ele.getUsage(), copy);
        }

        public Vector3f[] verts() {
            return fromData(data.get(VertexFormatElement.EnumUsage.POSITION), 3);
        }

        public Vector2f[] uvs() {
            return fromData(data.get(VertexFormatElement.EnumUsage.UV), 2);
        }

        @SuppressWarnings("unchecked")
        private static <T extends Vector> T[] fromData(List<float[]> data, int size) {
            Vector[] ret = size == 2 ? new Vector2f[data.size()] : new Vector3f[data.size()];
            for (int i = 0; i < data.size(); i++) {
                ret[i] = size == 2 ? new Vector2f(data.get(i)[0], data.get(i)[1]) :
                        new Vector3f(data.get(i)[0], data.get(i)[1], data.get(i)[2]);
            }
            return (T[]) ret;
        }

        @NotNull
        @Override
        public VertexFormat getVertexFormat() {
            return vertexFormat;
        }

        @Override
        public void setQuadTint(int tint) {
            quadTint = tint;
        }

        @Override
        public void setQuadOrientation(EnumFacing orientation) {
            this.quadOrientation = orientation;
        }

        @Override
        public void setApplyDiffuseLighting(boolean diffuse) {
            this.applyDiffuseLighting = diffuse;
        }

        // @Override //soft override, only exists in new forge versions
        public void setTexture(@Nullable TextureAtlasSprite texture) {}
    }
}
