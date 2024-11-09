package gregtech.client.texture;

import net.minecraft.client.renderer.texture.TextureMap;

import net.minecraftforge.fml.relauncher.Side;

import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

/**
 * Functional Interface for registering ResourceLocations to a TextureMap.
 * <p>
 * Called during {@link net.minecraftforge.client.event.TextureStitchEvent.Pre}
 */
@FunctionalInterface
public interface IconRegistrar {

    /**
     * @param map the map to register textures to
     */
    @SideOnly(Side.CLIENT)
    void registerIcons(@NotNull TextureMap map);
}
