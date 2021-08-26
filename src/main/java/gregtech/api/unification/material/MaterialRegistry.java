package gregtech.api.unification.material;

import gregtech.api.util.GTControlledRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class MaterialRegistry extends GTControlledRegistry<Material> {

    // Temp measure - TODO: what should we do about materials?
    private final Map<String, Material> namespaceless = new Object2ObjectOpenHashMap<>();

    public MaterialRegistry() {
        super(Short.MAX_VALUE);
    }

    public Material getObject(String material) {
        return namespaceless.get(material);
    }

    @Override
    public void freeze() {
        super.freeze();
        if (frozen) {
            underlyingIntegerMap.iterator().forEachRemaining(Material::verifyMaterial);
            underlyingIntegerMap.iterator().forEachRemaining(Material::postVerify);
        }
    }

    @Override
    public void register(int id, ResourceLocation key, Material value) {
        if (value instanceof MarkerMaterial) {
            throw new IllegalArgumentException("MarkerMaterial " + value + " should not be registered to the MaterialRegistry.");
        }
        super.register(id, key, value);
        namespaceless.put(key.getPath(), value);
    }
}
