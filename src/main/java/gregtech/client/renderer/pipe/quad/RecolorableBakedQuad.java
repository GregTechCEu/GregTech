package gregtech.client.renderer.pipe.quad;

import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class RecolorableBakedQuad extends UnpackedBakedQuad {

    private final SpriteInformation spriteInformation;

    public RecolorableBakedQuad(float[][][] unpackedData, int tint, EnumFacing orientation,
                                SpriteInformation texture, boolean applyDiffuseLighting, VertexFormat format) {
        super(unpackedData, tint, orientation, texture.sprite(), applyDiffuseLighting, format);
        this.spriteInformation = texture;
    }

    public RecolorableBakedQuad withColor(int argb) {
        if (!spriteInformation.colorable()) return this;

        float[][][] newData = new float[4][format.getElementCount()][];

        float a = ((argb >> 24) & 0xFF) / 255f; // alpha
        float r = ((argb >> 16) & 0xFF) / 255f; // red
        float g = ((argb >> 8) & 0xFF) / 255f; // green
        float b = ((argb) & 0xFF) / 255f; // blue
        float[] array = new float[] { r, g, b, a };
        for (int v = 0; v < 4; v++) {
            for (int e = 0; e < format.getElementCount(); e++) {
                if (format.getElement(e).getUsage() == VertexFormatElement.EnumUsage.COLOR) {
                    newData[v][e] = array;
                } else {
                    newData[v][e] = unpackedData[v][e];
                }
            }
        }
        return new RecolorableBakedQuad(newData, this.tintIndex, this.face, this.spriteInformation,
                this.applyDiffuseLighting, this.format);
    }

    public static class Builder {

        private final VertexFormat format;
        private final float[][][] unpackedData;
        private int tint = -1;
        private EnumFacing orientation;
        private SpriteInformation texture;
        private boolean applyDiffuseLighting = true;

        private int vertices = 0;
        private int elements = 0;
        private boolean full = false;
        private boolean contractUVs = false;

        public Builder(VertexFormat format) {
            this.format = format;
            unpackedData = new float[4][format.getElementCount()][4];
        }

        public @NotNull VertexFormat getVertexFormat() {
            return format;
        }

        public void setContractUVs(boolean value) {
            this.contractUVs = value;
        }

        public void setQuadTint(int tint) {
            this.tint = tint;
        }

        public void setQuadOrientation(@NotNull EnumFacing orientation) {
            this.orientation = orientation;
        }

        public void setTexture(@NotNull SpriteInformation texture) {
            this.texture = texture;
        }

        public void setApplyDiffuseLighting(boolean diffuse) {
            this.applyDiffuseLighting = diffuse;
        }

        public void put(int element, float @NotNull... data) {
            for (int i = 0; i < 4; i++) {
                if (i < data.length) {
                    unpackedData[vertices][element][i] = data[i];
                } else {
                    unpackedData[vertices][element][i] = 0;
                }
            }
            elements++;
            if (elements == format.getElementCount()) {
                vertices++;
                elements = 0;
            }
            if (vertices == 4) {
                full = true;
            }
        }

        public RecolorableBakedQuad build() {
            if (!full) {
                throw new IllegalStateException("not enough data");
            }
            if (texture == null) {
                throw new IllegalStateException("texture not set");
            }
            if (contractUVs) {
                float tX = texture.sprite().getIconWidth() / (texture.sprite().getMaxU() - texture.sprite().getMinU());
                float tY = texture.sprite().getIconHeight() / (texture.sprite().getMaxV() - texture.sprite().getMinV());
                float tS = Math.max(tX, tY);
                float ep = 1f / (tS * 0x100);
                int uve = 0;
                while (uve < format.getElementCount()) {
                    VertexFormatElement e = format.getElement(uve);
                    if (e.getUsage() == VertexFormatElement.EnumUsage.UV && e.getIndex() == 0) {
                        break;
                    }
                    uve++;
                }
                if (uve == format.getElementCount()) {
                    throw new IllegalStateException("Can't contract UVs: format doesn't contain UVs");
                }
                float[] uvc = new float[4];
                for (int v = 0; v < 4; v++) {
                    for (int i = 0; i < 4; i++) {
                        uvc[i] += unpackedData[v][uve][i] / 4;
                    }
                }
                for (int v = 0; v < 4; v++) {
                    for (int i = 0; i < 4; i++) {
                        float uo = unpackedData[v][uve][i];
                        float eps = 1f / 0x100;
                        float un = uo * (1 - eps) + uvc[i] * eps;
                        float ud = uo - un;
                        float aud = ud;
                        if (aud < 0) aud = -aud;
                        if (aud < ep) // not moving a fraction of a pixel
                        {
                            float udc = uo - uvc[i];
                            if (udc < 0) udc = -udc;
                            if (udc < 2 * ep) // center is closer than 2 fractions of a pixel, don't move too close
                            {
                                un = (uo + uvc[i]) / 2;
                            } else // move at least by a fraction
                            {
                                un = uo + (ud < 0 ? ep : -ep);
                            }
                        }
                        unpackedData[v][uve][i] = un;
                    }
                }
            }
            return new RecolorableBakedQuad(unpackedData, tint, orientation, texture, applyDiffuseLighting, format);
        }
    }
}
