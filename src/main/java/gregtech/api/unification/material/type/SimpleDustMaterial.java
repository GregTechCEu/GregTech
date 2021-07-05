package gregtech.api.unification.material.type;

import com.google.common.collect.ImmutableList;
import gregtech.api.unification.material.IMaterial;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTControlledRegistry;

import javax.annotation.Nonnull;

import static gregtech.api.unification.material.type.Material.MatFlags.registerMaterialFlag;
import static gregtech.api.util.GTUtility.createFlag;

public class SimpleDustMaterial extends SimpleMaterial<SimpleDustMaterial> implements Comparable<SimpleDustMaterial> {

    public static final long GENERATE_SMALL_TINY = createFlag(54);
    static {
        registerMaterialFlag("GENERATE_SMALL_TINY", GENERATE_SMALL_TINY, SimpleDustMaterial.class);
    }

    public static final GTControlledRegistry<String, SimpleDustMaterial> MATERIAL_REGISTRY = new GTControlledRegistry<>(10000);

    public SimpleDustMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet iconSet, ImmutableList<MaterialStack> components, long flags) {
        super(materialRGB, iconSet, components, flags);
        long materialBits = verifyMaterialBits(flags);
        if (materialBits != 0) {
            throw new IllegalArgumentException("Invalid flags on SimpleDustMaterial: "
                    + IMaterial.getIntValueOfFlag(materialBits));
        }
        MATERIAL_REGISTRY.register(metaItemSubId, name, this);
    }

    public long verifyMaterialBits(long materialBits) {
        materialBits = super.verifyMaterialBits(materialBits);
        materialBits &= ~GENERATE_SMALL_TINY;
        return materialBits;
    }

    @Override
    public GTControlledRegistry<String, SimpleDustMaterial> getRegistry() {
        return MATERIAL_REGISTRY;
    }

    @Override
    public Class<SimpleDustMaterial> getMaterialClass() {
        return SimpleDustMaterial.class;
    }

    @Override
    public int compareTo(@Nonnull SimpleDustMaterial o) {
        return toString().compareTo(toString());
    }

    @Override
    public String toString() {
        return MATERIAL_REGISTRY.getNameForObject(this);
    }
}
