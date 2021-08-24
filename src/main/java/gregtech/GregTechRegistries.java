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
    static final GTControlledRegistry<UIFactory> UI_REGISTRY = new GTControlledRegistry<>(Short.MAX_VALUE);
    static final GTControlledRegistry<CoverDefinition> COVER_REGISTRY = new GTControlledRegistry<>(Integer.MAX_VALUE);

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
