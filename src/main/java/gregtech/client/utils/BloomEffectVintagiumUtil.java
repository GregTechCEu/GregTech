package gregtech.client.utils;

import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.util.BufferSizeUtil;
import me.jellysquid.mods.sodium.client.util.EnumUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class BloomEffectVintagiumUtil {

    private static BlockRenderPass bloom;

    public static void init() {
        EnumUtil.LAYERS = BlockRenderLayer.values();
        BufferSizeUtil.BUFFER_SIZES.put(BloomEffectUtil.getBloomLayer(), 131072);
        bloom = EnumHelper.addEnum(BlockRenderPass.class, "BLOOM",
                new Class[] { BlockRenderLayer.class, boolean.class }, BloomEffectUtil.getBloomLayer(), true);

        try {
            Field values = BlockRenderPass.class.getField("VALUES");
            values.set(null, BlockRenderPass.values());
            Field count = BlockRenderPass.class.getField("COUNT");
            count.set(null, BlockRenderPass.values().length);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return {@link BlockRenderPass} instance for the bloom render layer.
     */
    @NotNull
    @SuppressWarnings("unused")
    public static BlockRenderPass getBloomPass() {
        return Objects.requireNonNull(bloom, "Bloom effect is not initialized yet");
    }
}
