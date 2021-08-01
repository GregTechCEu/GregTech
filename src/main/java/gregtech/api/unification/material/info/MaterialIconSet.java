package gregtech.api.unification.material.info;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.gregtech.material.MaterialIconSet")
@ZenRegister
public enum MaterialIconSet {

    NONE,
    METALLIC,
    DULL,
    MAGNETIC,
    QUARTZ,
    DIAMOND,
    EMERALD,
    SHINY,
    SHARDS,
    ROUGH,
    FINE,
    SAND,
    FLINT,
    RUBY,
    LAPIS,
    FLUID,
    GAS,
    LIGNITE,
    OPAL,
    GLASS,
    WOOD,
    LEAF,
    GEM_HORIZONTAL,
    GEM_VERTICAL,
    PAPER,
    NETHERSTAR;

    @ZenGetter("name")
    public String getName() {
        return name().toLowerCase();
    }

    @ZenMethod("get")
    public static MaterialIconSet getByName(String name) {
        return valueOf(name.toUpperCase());
    }

    @Override
    @ZenMethod
    public String toString() {
        return super.toString();
    }
}
