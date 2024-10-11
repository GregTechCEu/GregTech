package gregtech.mixins.nothirium;

import meldexun.nothirium.api.renderer.chunk.ChunkRenderPass;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkRenderPass.class)
public class ChunkRenderPassMixin {

    @Final
    @Mutable
    @Shadow(remap = false)
    public static ChunkRenderPass[] $VALUES;

    @Final
    @Mutable
    @Shadow(remap = false)
    public static ChunkRenderPass[] ALL;

    @SuppressWarnings("all")
    @Invoker(value = "<init>")
    private static ChunkRenderPass create(String name, int ordinal) {
        throw new IllegalStateException("Unreachable");
    }

    static {
        ChunkRenderPass bloom = create("BLOOM", $VALUES.length);
        $VALUES = ArrayUtils.add($VALUES, bloom);
        ALL = ChunkRenderPass.values();
    }
}
