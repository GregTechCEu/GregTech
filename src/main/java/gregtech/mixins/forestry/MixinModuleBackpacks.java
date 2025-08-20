package gregtech.mixins.forestry;

import forestry.api.storage.BackpackManager;
import forestry.storage.ModuleBackpacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(ModuleBackpacks.class)
public class MixinModuleBackpacks {

    @Inject(method = "setDefaultsForConfig", at = @At("TAIL"), remap = false)
    private void injectRawOreDictRegexp(CallbackInfo ci) {
        ModuleBackpacks instance = (ModuleBackpacks) (Object) this;

        try {
            Field field = ModuleBackpacks.class.getDeclaredField("backpackAcceptedOreDictRegexpDefaults");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            java.util.Map<String, java.util.List<String>> backpackAcceptedOreDictRegexpDefaults =
                    (java.util.Map<String, java.util.List<String>>) field.get(instance);

            // 向MINER_UID的列表中添加"raw[A-Z].*"
            java.util.List<String> minerList = backpackAcceptedOreDictRegexpDefaults.get(BackpackManager.MINER_UID);
            if (minerList != null && !minerList.contains("raw[A-Z].*")) {
                minerList.add("raw[A-Z].*");
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {

        }
    }
}
