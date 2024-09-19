package gregtech.client.utils;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SideOnly(Side.CLIENT)
public class BloomEffectVintagiumUtil {

    public static BlockRenderPass bloom;

    /**
     * @return {@link BlockRenderPass} instance for the bloom render layer.
     */
    @NotNull
    @SuppressWarnings("unused")
    public static BlockRenderPass getBloomPass() {
        return Objects.requireNonNull(bloom, "Bloom effect is not initialized yet");
    }
}
