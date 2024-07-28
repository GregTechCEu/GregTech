package gregtech.client.renderer.pipe.util;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@FunctionalInterface
public interface PipeSpriteWoodClarifier {

    SpriteInformation getSprite(boolean isWoodVariant);
}
