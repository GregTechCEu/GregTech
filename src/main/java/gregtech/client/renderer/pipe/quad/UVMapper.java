package gregtech.client.renderer.pipe.quad;

import com.github.bsideup.jabel.Desugar;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@FunctionalInterface
interface UVMapper {

    UVPair map(UVCorner corner, TextureAtlasSprite sprite);

    static UVPair uvPair(float u, float v) {
        return new UVPair(u, v);
    }

    @Desugar
    record UVPair(float u, float v) {}
}
