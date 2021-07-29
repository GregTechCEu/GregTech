package gregtech.api.unification.material.type;

import com.google.common.collect.ImmutableList;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.function.Supplier;

@ZenClass("mods.gregtech.material.RoughSolidMaterial")
@ZenRegister
public class RoughSolidMaterial extends SolidMaterial {

    //use Supplier to avoid directly referencing and resolving OrePrefix too early,
    //which leads to class initialization deadlock and probably crash during OrePrefix initialization
    //instead, we use lazy-computing OrePrefix solid form supplier
    public final Supplier<OrePrefix> solidFormSupplier;

    public RoughSolidMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, float toolSpeed, float attackDamage, int toolDurability, Supplier<OrePrefix> solidFormSupplier) {
        super(metaItemSubId, name, materialRGB, materialIconSet, harvestLevel, materialComponents, materialGenerationFlags, null, toolSpeed, attackDamage, toolDurability);
        this.solidFormSupplier = solidFormSupplier;
    }

    // TODO
    public RoughSolidMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, Supplier<OrePrefix> solidFormSupplier) {
        super(metaItemSubId, name, materialRGB, materialIconSet, harvestLevel, materialComponents, materialGenerationFlags, null, 0, 0, 0);
        this.solidFormSupplier = solidFormSupplier;
    }

}
