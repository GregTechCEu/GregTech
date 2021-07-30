package gregtech.api.unification.material.type;

import gregtech.api.unification.material.IMaterialHandler;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.util.GTControlledRegistry;

import java.util.ArrayList;
import java.util.List;

public class MaterialNew {

    public static final GTControlledRegistry<String, Material> MATERIAL_REGISTRY = new GTControlledRegistry<>(32768);
    private static final List<IMaterialHandler> materialHandlers = new ArrayList<>();

    private final List<IMaterialProperty> properties = new ArrayList<>();
}
