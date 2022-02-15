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

    public static final MaterialIconSet NONE = new MaterialIconSet("none");
    public static final MaterialIconSet METALLIC = new MaterialIconSet("metallic");
    public static final MaterialIconSet DULL = new MaterialIconSet("dull");
    public static final MaterialIconSet MAGNETIC = new MaterialIconSet("magnetic");
    public static final MaterialIconSet QUARTZ = new MaterialIconSet("quartz");
    public static final MaterialIconSet DIAMOND = new MaterialIconSet("diamond");
    public static final MaterialIconSet EMERALD = new MaterialIconSet("emerald");
    public static final MaterialIconSet SHINY = new MaterialIconSet("shiny");
    public static final MaterialIconSet ROUGH = new MaterialIconSet("rough");
    public static final MaterialIconSet FINE = new MaterialIconSet("fine");
    public static final MaterialIconSet SAND = new MaterialIconSet("sand");
    public static final MaterialIconSet FLINT = new MaterialIconSet("flint");
    public static final MaterialIconSet RUBY = new MaterialIconSet("ruby");
    public static final MaterialIconSet LAPIS = new MaterialIconSet("lapis");
    public static final MaterialIconSet FLUID = new MaterialIconSet("fluid");
    public static final MaterialIconSet GAS = new MaterialIconSet("gas");
    public static final MaterialIconSet LIGNITE = new MaterialIconSet("lignite");
    public static final MaterialIconSet OPAL = new MaterialIconSet("opal");
    public static final MaterialIconSet GLASS = new MaterialIconSet("glass");
    public static final MaterialIconSet WOOD = new MaterialIconSet("wood");
    public static final MaterialIconSet GEM_HORIZONTAL = new MaterialIconSet("gem_horizontal");
    public static final MaterialIconSet GEM_VERTICAL = new MaterialIconSet("gem_vertical");
    public static final MaterialIconSet PAPER = new MaterialIconSet("paper");
    public static final MaterialIconSet NETHERSTAR = new MaterialIconSet("netherstar");
    public static final MaterialIconSet BRIGHT = new MaterialIconSet("bright");

    public final String name;
    public final int id;

    public MaterialIconSet(String name) {
        this.name = name.toLowerCase(Locale.ENGLISH);
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
        return ICON_SETS.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    @ZenMethod
    public String toString() {
        return getName();
    }
}
