package gregtech.api.registry;

import gregtech.api.GTValues;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class GTSimpleRegistry<K, V> extends RegistrySimple<K, V> {

    protected boolean frozen = true;

    public boolean isFrozen() {
        return frozen;
    }

    public void freeze() {
        if (frozen) {
            throw new IllegalStateException("Registry is already frozen!");
        }

        if (!checkActiveModContainerIsGregtech()) {
            return;
        }

        this.frozen = true;
    }

    public void unfreeze() {
        if (!frozen) {
            throw new IllegalStateException("Registry is already unfrozen!");
        }

        if (!checkActiveModContainerIsGregtech()) {
            return;
        }

        this.frozen = false;
    }

    protected static boolean checkActiveModContainerIsGregtech() {
        ModContainer container = Loader.instance().activeModContainer();
        return container != null && container.getModId().equals(GTValues.MODID);
    }
}
