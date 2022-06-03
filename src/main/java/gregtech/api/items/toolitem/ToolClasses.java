package gregtech.api.items.toolitem;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public class ToolClasses {

    public static final String SWORD = "sword";
    public static final String PICKAXE = "pickaxe";
    public static final String SHOVEL = "shovel";
    public static final String AXE = "axe";
    public static final String HOE = "hoe";
    public static final String SAW = "saw";
    public static final String HARD_HAMMER = "hammer";
    public static final String SOFT_MALLET = "mallet";
    public static final String WRENCH = "wrench";
    public static final String FILE = "file";
    public static final String CROWBAR = "crowbar";
    public static final String SCREWDRIVER = "screwdriver";
    public static final String MORTAR = "mortar";
    public static final String WIRE_CUTTER = "wirecutter";
    public static final String SICKLE = "sickle";
    public static final String SCYTHE = "scythe";
    public static final String SHEARS = "shears";
    public static final String KNIFE = "knife";
    public static final String BUTCHERY_KNIFE = "butchery_knife";
    public static final String PLUNGER = "plunger";

    public static final Set<String> SCYTHES = ImmutableSet.of(SICKLE, SCYTHE);
    public static final Set<String> DRILL = ImmutableSet.of(PICKAXE, SHOVEL);
}
