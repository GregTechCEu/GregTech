package gregtech.api;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.gui.UIFactory;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTControlledRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;

public class GregTechRegistries {

    public static final GTControlledRegistry<ResourceLocation, MetaTileEntity> MTE_REGISTRY = new GTControlledRegistry<>(Short.MAX_VALUE);
    public static final GTControlledRegistry<ResourceLocation, UIFactory> UI_FACTORY_REGISTRY = new GTControlledRegistry<>(Short.MAX_VALUE);
    public static final GTControlledRegistry<ResourceLocation, CoverDefinition> COVER_REGISTRY = new GTControlledRegistry<>(Integer.MAX_VALUE);

    public static class RegisterEvent<V> extends GenericEvent<V> {

        private final GTControlledRegistry<ResourceLocation, V> registry;

        public RegisterEvent(GTControlledRegistry<ResourceLocation, V> registry, Class<V> clazz) {
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
