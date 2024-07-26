package gregtech.client.renderer.pipe.util;

import com.github.bsideup.jabel.Desugar;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Desugar
public record SpriteInformation(TextureAtlasSprite sprite, boolean colorable) {}
