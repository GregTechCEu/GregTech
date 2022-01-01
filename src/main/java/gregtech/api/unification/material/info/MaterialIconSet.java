package gregtech.api.unification.material.info;

import com.google.common.base.Preconditions;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ZenClass("mods.gregtech.material.MaterialIconSet")
@ZenRegister
public class MaterialIconSet {

    public static final Map<String, MaterialIconSet> ICON_SETS = new HashMap<>();

    static int idCounter = 0;

    public static final MaterialIconSet NONE = new MaterialIconSet("NONE");
    public static final MaterialIconSet METALLIC = new MaterialIconSet("METALLIC");
    public static final MaterialIconSet DULL = new MaterialIconSet("DULL");
    public static final MaterialIconSet MAGNETIC = new MaterialIconSet("MAGNETIC");
    public static final MaterialIconSet QUARTZ = new MaterialIconSet("QUARTZ");
    public static final MaterialIconSet DIAMOND = new MaterialIconSet("DIAMOND");
    public static final MaterialIconSet EMERALD = new MaterialIconSet("EMERALD");
    public static final MaterialIconSet SHINY = new MaterialIconSet("SHINY");
    public static final MaterialIconSet ROUGH = new MaterialIconSet("ROUGH");
    public static final MaterialIconSet FINE = new MaterialIconSet("FINE");
    public static final MaterialIconSet SAND = new MaterialIconSet("SAND");
    public static final MaterialIconSet FLINT = new MaterialIconSet("FLINT");
    public static final MaterialIconSet RUBY = new MaterialIconSet("RUBY");
    public static final MaterialIconSet LAPIS = new MaterialIconSet("LAPIS");
    public static final MaterialIconSet FLUID = new MaterialIconSet("FLUID");
    public static final MaterialIconSet GAS = new MaterialIconSet("GAS");
    public static final MaterialIconSet LIGNITE = new MaterialIconSet("LIGNITE");
    public static final MaterialIconSet OPAL = new MaterialIconSet("OPAL");
    public static final MaterialIconSet GLASS = new MaterialIconSet("GLASS");
    public static final MaterialIconSet WOOD = new MaterialIconSet("WOOD");
    public static final MaterialIconSet GEM_HORIZONTAL = new MaterialIconSet("GEM_HORIZONTAL");
    public static final MaterialIconSet GEM_VERTICAL = new MaterialIconSet("GEM_VERTICAL");
    public static final MaterialIconSet PAPER = new MaterialIconSet("PAPER");
    public static final MaterialIconSet NETHERSTAR = new MaterialIconSet("NETHERSTAR");
    public static final MaterialIconSet BRIGHT = new MaterialIconSet("BRIGHT");

    public final String name;
    public final int id;

    public MaterialIconSet(String name) {
        this.name = name.toLowerCase(Locale.ROOT);
        Preconditions.checkArgument(!ICON_SETS.containsKey(this.name), "MaterialIconSet " + this.name + " already registered!");
        this.id = idCounter++;
        ICON_SETS.put(this.name, this);
    }

    @ZenGetter("name")
    public String getName() {
        return name;
    }

    @ZenMethod("get")
    public static MaterialIconSet getByName(String name) {
        return ICON_SETS.get(name.toUpperCase(Locale.ROOT));
    }

    @Override
    @ZenMethod
    public String toString() {
        return super.toString();
    }
}
