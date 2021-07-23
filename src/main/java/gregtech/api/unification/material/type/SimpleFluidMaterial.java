package gregtech.api.unification.material.type;

import com.google.common.collect.ImmutableList;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.GTControlledRegistry;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleFluidMaterial extends FluidMaterial {

    public static final GTControlledRegistry<String, SimpleFluidMaterial> MATERIAL_REGISTRY = new GTControlledRegistry<>(Integer.MAX_VALUE);
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    public SimpleFluidMaterial(String name, int materialRGB, MaterialIconSet materialIconSet, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags) {
        this(name, materialRGB, materialIconSet, materialComponents, materialGenerationFlags, null);
    }

    public SimpleFluidMaterial(String name, int materialRGB, MaterialIconSet materialIconSet, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, Element element) {
        super(idCounter.getAndIncrement(), name, materialRGB, materialIconSet, materialComponents, materialGenerationFlags, element);
    }

    public SimpleFluidMaterial setFluidTemperature(int temperature) {
        this.fluidTemperature = temperature;
        return this;
    }

    @Override
    protected void registerMaterial(int metaItemSubId, String name) {
        MATERIAL_REGISTRY.register(metaItemSubId, name, this);
    }

    @Override
    public String toString() {
        return MATERIAL_REGISTRY.getNameForObject(this);
    }
}
