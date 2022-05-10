package gregtech;

import gregtech.api.fluids.MetaFluids;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.relauncher.CoreModManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Bootstrap {

    private static boolean bootstrapped = false;

    public static void perform() {
        if (bootstrapped) {
            return;
        }
        try {
            Field deobfuscatedEnvironment = CoreModManager.class.getDeclaredField("deobfuscatedEnvironment");
            deobfuscatedEnvironment.setAccessible(true);
            deobfuscatedEnvironment.setBoolean(null, true);
            Method setLocale = I18n.class.getDeclaredMethod("setLocale", Locale.class); // No need to care about obfuscation
            setLocale.setAccessible(true);
            setLocale.invoke(null, new Locale());
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        net.minecraft.init.Bootstrap.register();
        ModMetadata meta = new ModMetadata();
        meta.modId = "gregtech";
        Loader.instance().setupTestHarness(new DummyModContainer(meta));
        Materials.register();
        OrePrefix.runMaterialHandlers();
        MetaFluids.init();
        MetaItems.init();
        bootstrapped = true;
    }

}
