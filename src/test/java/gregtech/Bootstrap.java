package gregtech;

import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.MetaFluids;
import gregtech.common.items.MetaItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;

import java.lang.reflect.Method;

public class Bootstrap {

    private static boolean bootstrapped = false;

    public static void perform() {
        if (bootstrapped) {
            return;
        }
        try {
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
