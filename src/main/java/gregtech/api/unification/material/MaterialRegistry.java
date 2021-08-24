package gregtech.api.unification.material;

import gregtech.api.util.GTControlledRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.GameData;

public class MaterialRegistry extends GTControlledRegistry<Material> {

    public MaterialRegistry() {
        super(Short.MAX_VALUE);
    }

    @Override
    public void register(int id, ResourceLocation key, Material value) {
        if (id < 0 || id >= maxId) {
            throw new IndexOutOfBoundsException("Id is out of range: " + id);
        }
        key = GameData.checkPrefix(key.toString());
        value.verifyMaterial();
        value.postVerify();
        super.putObject(key, value);
        Material objectWithId = getObjectById(id);
        if (objectWithId != null) {
            throw new IllegalArgumentException(String.format("Tried to reassign id %d to %s (%s), but it is already assigned to %s (%s)!",
                    id, value, key, objectWithId, getNameForObject(objectWithId)));
        }
        underlyingIntegerMap.put(value, id);
    }
}
