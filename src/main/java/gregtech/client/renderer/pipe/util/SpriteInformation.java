package gregtech.client.renderer.pipe.util;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.github.bsideup.jabel.Desugar;

@SideOnly(Side.CLIENT)
@Desugar
public record SpriteInformation(TextureAtlasSprite sprite, int colorID) {

    public boolean colorable() {
        return colorID >= 0;
    }
}
