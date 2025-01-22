package gregtech.mixins.minecraft;

import net.minecraft.util.BlockRenderLayer;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockRenderLayer.class)
public class BlockRenderLayerMixin {

    @Final
    @Shadow
    @Mutable
    private static BlockRenderLayer[] $VALUES;

    @SuppressWarnings("all")
    @Invoker("<init>")
    private static BlockRenderLayer create(String name, int ordinal, String directoryName) {
        throw new IllegalStateException("Unreachable");
    }

    static {
        BlockRenderLayer bloom = create("BLOOM", $VALUES.length, "Bloom");

        $VALUES = ArrayUtils.add($VALUES, bloom);
    }
}
