package gregtech.client.utils;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.colour.Colour;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;

@SideOnly(Side.CLIENT)
public class AdvCCRSConsumer implements IVertexConsumer {

    private final CCRenderState ccrs;
    private Matrix4 translation = new Matrix4();

    public AdvCCRSConsumer(CCRenderState ccrs) {
        this.ccrs = ccrs;
    }

    @NotNull
    @Override
    public VertexFormat getVertexFormat() {
        return ccrs.getVertexFormat();
    }

    @Override
    public void setTexture(@NotNull TextureAtlasSprite texture) {
        ccrs.sprite = texture;
    }

    @Override
    public void put(int e, float @NotNull... data) {
        VertexFormat format = getVertexFormat();

        VertexFormatElement fmte = format.getElement(e);
        switch (fmte.getUsage()) {
            case POSITION:
                ccrs.vert.vec.set(data).apply(translation);
                break;
            case UV:
                if (fmte.getIndex() == 0) {
                    ccrs.vert.uv.set(data[0], data[1]);
                } else {
                    ccrs.brightness = (int) (data[1] * 0xFFFF / 2) << 16 | (int) (data[0] * 0xFFFF / 2);
                }
                break;
            case COLOR:
                ccrs.colour = Colour.packRGBA(data);
                break;
            case NORMAL:
                ccrs.normal.set(data);
                break;
            case PADDING:
                break;
            default:
                throw new UnsupportedOperationException("Generic vertex format element");
        }
        if (e == format.getElementCount() - 1) {
            ccrs.writeVert();
        }
    }

    public void setTranslation(Matrix4 translation) {
        this.translation = translation;
    }

    @Override
    public void setQuadTint(int tint) {}

    @Override
    public void setQuadOrientation(@NotNull EnumFacing orientation) {}

    @Override
    public void setApplyDiffuseLighting(boolean diffuse) {}
}
