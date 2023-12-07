package gregtech.integration.crafttweaker.item;

import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.ore.OrePrefix;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import static gregtech.integration.crafttweaker.CraftTweakerModule.CT_OREDICT_ITEM;

@ZenClass("mods.gregtech.item.ItemRegistry")
@ZenRegister
@SuppressWarnings("unused")
public class CTItemRegistry {

    @ZenMethod("registerItem")
    public static void registerItem(String name, short id, int rgb, String materialIconSet, String orePrefix,
                                    @Optional String chemicalFormula) {
        CT_OREDICT_ITEM.addOreDictItem(
                id, name, rgb, MaterialIconSet.ICON_SETS.get(materialIconSet), OrePrefix.getPrefix(orePrefix),
                chemicalFormula.isEmpty() ? null : chemicalFormula);
    }
}
