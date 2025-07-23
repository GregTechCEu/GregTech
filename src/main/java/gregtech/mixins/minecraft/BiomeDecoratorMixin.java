package gregtech.mixins.minecraft;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.OreGenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static gregtech.common.ConfigHolder.vanillaOptimizeOptions;

@Mixin(BiomeDecorator.class)
public abstract class BiomeDecoratorMixin {

    // 使用 @Shadow 注解来访问原始类中的 chunkPos 字段
    @Shadow
    public BlockPos chunkPos;

    /**
     * 重写 generateOres 方法，使其不生成任何矿物
     * 但保留 Forge 事件触发以确保兼容性
     */
    @Inject(method = "generateOres", at = @At("HEAD"), cancellable = true)
    private void disableOreGeneration(World worldIn, Random random, CallbackInfo ci) {
        if(vanillaOptimizeOptions.disableOreGeneration) {
            // 触发 PRE 事件
            OreGenEvent.Pre preEvent = new OreGenEvent.Pre(worldIn, random, this.chunkPos);
            MinecraftForge.ORE_GEN_BUS.post(preEvent);

            // 触发 POST 事件
            OreGenEvent.Post postEvent = new OreGenEvent.Post(worldIn, random, this.chunkPos);
            MinecraftForge.ORE_GEN_BUS.post(postEvent);

            // 取消原始方法执行
            ci.cancel();
        }
    }
}
