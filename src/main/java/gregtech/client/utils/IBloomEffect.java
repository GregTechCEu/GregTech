package gregtech.client.utils;

import gregtech.client.renderer.IRenderSetup;
import gregtech.client.shader.postprocessing.BloomType;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

/**
 * Render callback interface for {@link BloomEffectUtil#registerBloomRender(IRenderSetup, BloomType, IBloomEffect)}.
 */
@FunctionalInterface
public interface IBloomEffect {

    /**
     * Render the bloom effect.
     *
     * @param buffer  buffer builder
     * @param context render context
     */
    @SideOnly(Side.CLIENT)
    void renderBloomEffect(@NotNull BufferBuilder buffer, @NotNull EffectRenderContext context);

    /**
     * @param context render context
     * @return if this effect should be rendered; returning {@code false} skips {@link #renderBloomEffect(BufferBuilder,
     *         EffectRenderContext)} call.
     */
    @SideOnly(Side.CLIENT)
    default boolean shouldRenderBloomEffect(@NotNull EffectRenderContext context) {
        return true;
    }
}
