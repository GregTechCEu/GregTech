package gregtech.integration.crafttweaker.material;

import crafttweaker.annotations.ZenRegister;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Collection;

@ZenClass("mods.gregtech.material.MaterialRegistry")
@ZenRegister
public class CTMaterialRegistry {

    @ZenMethod
    public Material get(String modid, String name) {
        return GregTechAPI.materialManager.getRegistry(modid).getObject(name);
    }

    @ZenMethod
    public Collection<Material> getAllMaterials() {
        return GregTechAPI.materialManager.getRegisteredMaterials();
    }
}
