package gregtech;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.gui.UIFactory;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.material.MaterialRegistry;
import gregtech.api.util.GTControlledRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;

public class GregTechRegistries {

    static final MaterialRegistry MATERIAL_REGISTRY = new MaterialRegistry();
    static final GTControlledRegistry<MetaTileEntity> MTE_REGISTRY = new GTControlledRegistry<>(Short.MAX_VALUE);
    static final GTControlledRegistry<UIFactory> UI_FACTORY_REGISTRY = new GTControlledRegistry<>(Short.MAX_VALUE);
    static final GTControlledRegistry<CoverDefinition> COVER_REGISTRY = new GTControlledRegistry<>(Integer.MAX_VALUE);

    public static boolean isMaterialRegistryFrozen() {
        return MATERIAL_REGISTRY.isFrozen();
    }

    public static boolean isMetaTileEntityRegistryFrozen() {
        return MTE_REGISTRY.isFrozen();
    }

    public static boolean isUIFactoryRegistryFrozen() {
        return UI_FACTORY_REGISTRY.isFrozen();
    }

    public static boolean isCoverRegistryFrozen() {
        return COVER_REGISTRY.isFrozen();
    }

    public static MaterialRegistry getMaterialRegistry() {
        return MATERIAL_REGISTRY;
    }

    public static GTControlledRegistry<MetaTileEntity> getMetaTileEntityRegistry() {
        return MTE_REGISTRY;
    }

    public static GTControlledRegistry<UIFactory> getUIFactoryRegistry() {
        return UI_FACTORY_REGISTRY;
    }

    public static GTControlledRegistry<CoverDefinition> getCoverRegistry() {
        return COVER_REGISTRY;
    }

    public static class RegisterEvent<V> extends GenericEvent<V> {

        private final GTControlledRegistry<V> registry;

        RegisterEvent(GTControlledRegistry<V> registry, Class<V> clazz) {
            super(clazz);
            this.registry = registry;
        }

        public void register(int id, ResourceLocation key, V value) {
            registry.register(id, key, value);
        }

        public void register(int id, String key, V value) {
            registry.register(id, new ResourceLocation(Loader.instance().activeModContainer().getModId(), key), value);
        }
    }

}
