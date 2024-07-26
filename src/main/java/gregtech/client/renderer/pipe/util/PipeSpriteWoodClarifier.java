package gregtech.client.renderer.pipe.util;

import gregtech.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@FunctionalInterface
public interface PipeSpriteWoodClarifier {

    SpriteInformation getSprite(boolean isWoodVariant);
}
