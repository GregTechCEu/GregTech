package gregtech.api.unification.crafttweaker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.MaterialRegistry;
import gregtech.api.unification.stack.MaterialStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.List;

@ZenClass("mods.gregtech.material.MaterialRegistry")
@ZenRegister
public class CTMaterialRegistry {

    static int baseID = 32000;

    @ZenMethod
    @Nullable
    public static Material get(String name) {
        return MaterialRegistry.MATERIAL_REGISTRY.getObject(name);
    }

    @ZenMethod
    public static List<Material> getAllMaterials() {
        return Lists.newArrayList(MaterialRegistry.MATERIAL_REGISTRY);
    }

    private static ImmutableList<MaterialStack> validateComponentList(@Nullable MaterialStack[] components) {
        return components == null || components.length == 0 ? ImmutableList.of() : ImmutableList.copyOf(components);
    }
/*
    @ZenMethod
    public static FluidMaterial createFluidMaterial(int metaItemSubId, String name, int color, String iconSet, @Optional MaterialStack[] materialComponents) {
        return new FluidMaterial(metaItemSubId , name, color,
            MaterialIconSet.getByName(iconSet),
            validateComponentList(materialComponents), 0);
    }

    @ZenMethod
    public static DustMaterial createDustMaterial(int metaItemSubId, String name, int color, String iconSet, int harvestLevel, @Optional MaterialStack[] materialComponents) {
        return new DustMaterial(metaItemSubId, name, color,
            MaterialIconSet.getByName(iconSet), harvestLevel,
            validateComponentList(materialComponents), 0);
    }

    @ZenMethod
    public static GemMaterial createGemMaterial(int metaItemSubId, String name, int color, String iconSet, int harvestLevel, @Optional MaterialStack[] materialComponents, @Optional float toolSpeed, @Optional float attackDamage, @Optional int toolDurability) {
        return new GemMaterial(metaItemSubId, name, color,
            MaterialIconSet.getByName(iconSet), harvestLevel,
            validateComponentList(materialComponents), 0, null,
            Math.max(0.0f, toolSpeed), Math.max(0.0f, attackDamage), Math.max(0, toolDurability));
    }

    @ZenMethod
    public static IngotMaterial createIngotMaterial(int metaItemSubId, String name, int color, String iconSet, int harvestLevel, @Optional MaterialStack[] materialComponents, @Optional float toolSpeed, @Optional float attackDamage, @Optional int toolDurability, @Optional int blastFurnaceTemperature) {
        return new IngotMaterial(metaItemSubId, name, color,
            MaterialIconSet.getByName(iconSet), harvestLevel,
            validateComponentList(materialComponents), 0, null,
            Math.max(0.0f, toolSpeed), Math.max(0.0f, attackDamage), Math.max(0, toolDurability), blastFurnaceTemperature);
    }

    @ZenMethod
    public static FluidMaterial createFluidMaterial(String name, int color, String iconSet, @Optional MaterialStack[] materialComponents) {
        return new FluidMaterial(baseID++, name, color,
                MaterialIconSet.getByName(iconSet),
                validateComponentList(materialComponents), 0);
    }

    @ZenMethod
    public static DustMaterial createDustMaterial(String name, int color, String iconSet, int harvestLevel, @Optional MaterialStack[] materialComponents) {
        return new DustMaterial(baseID++, name, color,
                MaterialIconSet.getByName(iconSet), harvestLevel,
                validateComponentList(materialComponents), 0);
    }

    @ZenMethod
    public static GemMaterial createGemMaterial(String name, int color, String iconSet, int harvestLevel, @Optional MaterialStack[] materialComponents, @Optional float toolSpeed, @Optional float attackDamage, @Optional int toolDurability) {
        return new GemMaterial(baseID++, name, color,
                MaterialIconSet.getByName(iconSet), harvestLevel,
                validateComponentList(materialComponents), 0, null,
                Math.max(0.0f, toolSpeed), Math.max(0.0f, attackDamage), Math.max(0, toolDurability));
    }

    @ZenMethod
    public static IngotMaterial createIngotMaterial(String name, int color, String iconSet, int harvestLevel, @Optional MaterialStack[] materialComponents, @Optional float toolSpeed, @Optional float attackDamage, @Optional int toolDurability, @Optional int blastFurnaceTemperature) {
        return new IngotMaterial(baseID++, name, color,
                MaterialIconSet.getByName(iconSet), harvestLevel,
                validateComponentList(materialComponents), 0, null,
                Math.max(0.0f, toolSpeed), Math.max(0.0f, attackDamage), Math.max(0, toolDurability), blastFurnaceTemperature);
    }
*/
}
