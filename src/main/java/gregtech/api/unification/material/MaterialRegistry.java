package gregtech.api.unification.material;

import gregtech.api.util.GTControlledRegistry;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.ResourceLocation;

public class MaterialRegistry extends GTControlledRegistry<Material> {

    // Temp measure - TODO: what should we do about materials?
    private final ObjectOpenHashSet<Material> namespaceless = new ObjectOpenHashSet<>();

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
        super.register(id, key, value);
        namespaceless.add(value);
    }
}
