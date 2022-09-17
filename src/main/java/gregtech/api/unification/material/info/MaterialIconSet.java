package gregtech.api.unification.material.info;

import com.google.common.base.Preconditions;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import java.util.*;

@ZenClass("mods.gregtech.material.MaterialIconSet")
@ZenRegister
public class MaterialIconSet {

    public static final Map<String, MaterialIconSet> ICON_SETS = new HashMap<>();

    private static int idCounter = 0;

    public static final MaterialIconSet DULL = new MaterialIconSet("dull");
    public static final MaterialIconSet METALLIC = new MaterialIconSet("metallic");
    public static final MaterialIconSet MAGNETIC = new MaterialIconSet("magnetic", METALLIC);
    public static final MaterialIconSet SHINY = new MaterialIconSet("shiny", METALLIC);
    public static final MaterialIconSet BRIGHT = new MaterialIconSet("bright", SHINY);
    public static final MaterialIconSet DIAMOND = new MaterialIconSet("diamond", SHINY);
    public static final MaterialIconSet EMERALD = new MaterialIconSet("emerald", SHINY);
    public static final MaterialIconSet GEM_HORIZONTAL = new MaterialIconSet("gem_horizontal", SHINY);
    public static final MaterialIconSet GEM_VERTICAL = new MaterialIconSet("gem_vertical", SHINY);
    public static final MaterialIconSet RUBY = new MaterialIconSet("ruby", SHINY);
    public static final MaterialIconSet OPAL = new MaterialIconSet("opal", RUBY);
    public static final MaterialIconSet GLASS = new MaterialIconSet("glass", RUBY);
    public static final MaterialIconSet NETHERSTAR = new MaterialIconSet("netherstar", GLASS);
    public static final MaterialIconSet FINE = new MaterialIconSet("fine");
    public static final MaterialIconSet SAND = new MaterialIconSet("sand", FINE);
    public static final MaterialIconSet WOOD = new MaterialIconSet("wood", FINE);
    public static final MaterialIconSet ROUGH = new MaterialIconSet("rough", FINE);
    public static final MaterialIconSet FLINT = new MaterialIconSet("flint", ROUGH);
    public static final MaterialIconSet LIGNITE = new MaterialIconSet("lignite", ROUGH);
    public static final MaterialIconSet QUARTZ = new MaterialIconSet("quartz", ROUGH);
    public static final MaterialIconSet CERTUS = new MaterialIconSet("certus", QUARTZ);
    public static final MaterialIconSet LAPIS = new MaterialIconSet("lapis", QUARTZ);
    public static final MaterialIconSet FLUID = new MaterialIconSet("fluid");
    public static final MaterialIconSet GAS = new MaterialIconSet("gas");

    public final String name;
    public final int id;
    public final boolean isRootIconset;
    public final MaterialIconSet parentIconset;

    public MaterialIconSet(@Nonnull String name) {
        this(name, MaterialIconSet.DULL);
    }

    public MaterialIconSet(@Nonnull String name, @Nonnull MaterialIconSet parentIconSet) {
        this.name = name.toLowerCase(Locale.ENGLISH);
        Preconditions.checkArgument(!ICON_SETS.containsKey(this.name), "MaterialIconSet " + this.name + " already registered!");
        this.id = idCounter++;
        this.isRootIconset = this.name.equals("dull");
        this.parentIconset = parentIconSet;
        ICON_SETS.put(this.name, this);
    }

    @Nonnull
    public Collection<MaterialIconSet> getHeirarchy() {
        List<MaterialIconSet> iconSets = new ArrayList<>(Collections.singletonList(this));
        MaterialIconSet parent = this.parentIconset;
        while (!parent.isRootIconset) {
            iconSets.add(parent);
            parent = parent.parentIconset;
        }
        return iconSets;
    }

    @ZenGetter("name")
    public String getName() {
        return name;
    }

    @ZenMethod("get")
    public static MaterialIconSet getByName(@Nonnull String name) {
        return ICON_SETS.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    @ZenMethod
    public String toString() {
        return getName();
    }
}
