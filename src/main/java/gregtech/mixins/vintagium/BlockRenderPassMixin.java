package gregtech.mixins.vintagium;

import gregtech.client.utils.BloomEffectUtil;

import net.minecraft.util.BlockRenderLayer;

import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import me.jellysquid.mods.sodium.client.util.BufferSizeUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockRenderPass.class)
public abstract class BlockRenderPassMixin {

    @Final
    @Mutable
    @Shadow(remap = false)
    public static BlockRenderPass[] $VALUES;

    @Final
    @Mutable
    @Shadow(remap = false)
    public static BlockRenderPass[] VALUES;

    @Final
    @Mutable
    @Shadow(remap = false)
    public static int COUNT;

    @SuppressWarnings("all")
    @Invoker("<init>")
    private static BlockRenderPass create(String name, int ordinal, BlockRenderLayer layer, boolean translucent) {
        throw new IllegalStateException("Unreachable");
    }

    static {
        BufferSizeUtil.BUFFER_SIZES.put(BloomEffectUtil.getBloomLayer(), 131072);

        BlockRenderPass bloom = create("BLOOM", $VALUES.length, BloomEffectUtil.getBloomLayer(), true);
        $VALUES = ArrayUtils.add($VALUES, bloom);
        VALUES = BlockRenderPass.values();
        COUNT = VALUES.length;
    }
}
