package gregtech.mixins.minecraft;

import net.minecraft.util.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringUtils.class)
public abstract class StringUtilsMixin {

    /**
     * 修复当传入 null 字符串时导致的 NullPointerException
     * @param input 可能为 null 的输入字符串
     * @param cir 回调信息
     */
    @Inject(
            method = "func_76338_a",  // stripControlCodes 的混淆方法名
            at = @At("HEAD"),
            cancellable = true
    )
    private static void handleNullInput(String input, CallbackInfoReturnable<String> cir) {
        if (input == null) {
            // 返回空字符串代替 null，防止后续正则操作崩溃
            cir.setReturnValue("");
            cir.cancel();
        }
    }
}
