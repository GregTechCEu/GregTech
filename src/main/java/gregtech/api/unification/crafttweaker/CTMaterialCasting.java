package gregtech.api.unification.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.material.type.*;
import stanhebben.zenscript.annotations.ZenCaster;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;

@ZenExpansion("mods.gregtech.material.Material")
@ZenRegister
public class CTMaterialCasting {

    @ZenMethod
    @ZenCaster
    @Nullable
    public static FluidMaterial toFluid(Material material) {
        return cast(material, FluidMaterial.class);
    }

    @ZenMethod
    @ZenCaster
    @Nullable
    public static DustMaterial toDust(Material material) {
        return cast(material, DustMaterial.class);
    }

    @ZenMethod
    @ZenCaster
    @Nullable
    public static SolidMaterial toSolid(Material material) {
        return cast(material, SolidMaterial.class);
    }

    @ZenMethod
    @ZenCaster
    @Nullable
    public static GemMaterial toGem(Material material) {
        return cast(material, GemMaterial.class);
    }

    @ZenMethod
    @ZenCaster
    @Nullable
    public static IngotMaterial toIngot(Material material) {
        return cast(material, IngotMaterial.class);
    }

    @ZenMethod
    @ZenCaster
    @Nullable
    public static RoughSolidMaterial toRoughSolid(Material material) {
        return cast(material, RoughSolidMaterial.class);
    }

    private static <T, S extends Material> S cast(T material, Class<S> to) {
        if (material == null)
            return null;

        if (to.isAssignableFrom(material.getClass()))
            return to.cast(material);

        return null;
    }
}
