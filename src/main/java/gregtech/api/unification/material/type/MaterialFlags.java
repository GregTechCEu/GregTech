package gregtech.api.unification.material.type;

import gregtech.api.unification.material.Material;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.Map;
import java.util.TreeMap;

public class MaterialFlags {

    private static final Map<String, Map.Entry<Long, Class<? extends Material>>> materialFlagRegistry = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public static void registerMaterialFlag(String name, long value, Class<? extends Material> classFilter) {
        if (materialFlagRegistry.containsKey(name))
            throw new IllegalArgumentException("Flag with name " + name + " already registered!");

        for (Map.Entry<Long, Class<? extends Material>> entry : materialFlagRegistry.values()) {
            if (entry.getKey() == value)
                throw new IllegalArgumentException("Flag with ID " + getIntValueOfFlag(value) + " already registered!");
        }
        materialFlagRegistry.put(name, new AbstractMap.SimpleEntry<>(value, classFilter));
    }

    private static int getIntValueOfFlag(long value) {
        int index = 0;
        while (value != 1) {
            value >>= 1;
            index++;
        }
        return index;
    }

    public static void registerMaterialFlagsHolder(Class<?> holder, Class<? extends Material> lowerBounds) {
        for (Field holderField : holder.getFields()) {
            int modifiers = holderField.getModifiers();
            if (holderField.getType() != long.class ||
                    !Modifier.isPublic(modifiers) ||
                    !Modifier.isStatic(modifiers) ||
                    !Modifier.isFinal(modifiers))
                continue;
            String flagName = holderField.getName();
            long flagValue;
            try {
                flagValue = holderField.getLong(null);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            }
            registerMaterialFlag(flagName, flagValue, lowerBounds);
        }
    }

    public static long resolveFlag(String name, Class<? extends Material> selfClass) {
        Map.Entry<Long, Class<? extends Material>> flagEntry = materialFlagRegistry.get(name);
        if (flagEntry == null)
            throw new IllegalArgumentException("Flag with name " + name + " not registered");
        else if (!flagEntry.getValue().isAssignableFrom(selfClass))
            throw new IllegalArgumentException("Flag " + name + " cannot be applied to material type " +
                    selfClass.getSimpleName() + ", lower bound is " + flagEntry.getValue().getSimpleName());
        return flagEntry.getKey();
    }

    // TODO Extra Info TODO

    /**
     * Add to material if it is some kind of explosive
     */
    public static final long EXPLOSIVE = createFlag(4);

    /**
     * Add to material to disable it's unification fully
     * todo implement
     */
    public static final long NO_UNIFICATION = createFlag(5);

    /**
     * Add to material if any of it's items cannot be recycled to get scrub
     */
    public static final long NO_RECYCLING = createFlag(6);

    /**
     * Add to material if it has constantly burning aura
     */
    public static final long BURNING = createFlag(7);

    /**
     * Decomposition recipe requires hydrogen as additional input. Amount is equal to input amount
     */
    public static final long DECOMPOSITION_REQUIRES_HYDROGEN = createFlag(8);

    /**
     * Enables electrolyzer decomposition recipe generation
     */
    public static final long DECOMPOSITION_BY_ELECTROLYZING = createFlag(40);

    /**
     * Enables centrifuge decomposition recipe generation
     */
    public static final long DECOMPOSITION_BY_CENTRIFUGING = createFlag(41);

    /**
     * Add to material if it is some kind of flammable
     */
    public static final long FLAMMABLE = createFlag(42);

    /**
     * Disables decomposition recipe generation for this material and all materials that has it as component
     */
    public static final long DISABLE_DECOMPOSITION = createFlag(43);

    /**
     * Add to material if it cannot be worked by any other means, than smashing or smelting. This is used for coated Materials.
     */
    public static final long NO_WORKING = createFlag(13);
    /**
     * Add to material if it cannot be used for regular Metal working techniques since it is not possible to bend it.
     */
    public static final long NO_SMASHING = createFlag(14);

    /**
     * Add to material if it's impossible to smelt it
     */
    public static final long NO_SMELTING = createFlag(15);

    /**
     * Add to material if it melts into fluid (and it will also generate fluid for this material)
     */
    public static final long SMELT_INTO_FLUID = createFlag(17);

    /**
     * This will prevent material from creating Shapeless recipes for dust to block and vice versa
     * Also preventing extruding and alloy smelting recipes via SHAPE_EXTRUDING/MOLD_BLOCK
     */
    public static final long EXCLUDE_BLOCK_CRAFTING_RECIPES = createFlag(18);

    public static final long EXCLUDE_PLATE_COMPRESSOR_RECIPE = createFlag(19);

    /**
     * This will prevent material from creating Shapeless recipes for dust to block and vice versa
     */
    public static final long EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES = createFlag(46);

    /**
     * Marks material state as gas
     * Examples: Air, Argon, Refinery Gas, Oxygen, Hydrogen
     * todo remove
     */
    public static final long STATE_GAS = createFlag(10);

    /**
     * If this material can be crystallized.
     */
    public static final long CRYSTALLIZABLE = createFlag(34);

    public static final long HIGH_SIFTER_OUTPUT = createFlag(38);

    /**
     * Add this to your Material if you want to have its Ore Calcite heated in a Blast Furnace for more output. Already listed are:
     * Iron, Pyrite, PigIron, WroughtIron.
     */
    public static final long BLAST_FURNACE_CALCITE_DOUBLE = createFlag(35);
    public static final long BLAST_FURNACE_CALCITE_TRIPLE = createFlag(36);

    public static final long MORTAR_GRINDABLE = createFlag(24);

    // TODO Generation Flags TODO

    public static final long GENERATE_ORE = createFlag(11); // todo remove

    /**
     * Generate a plate for this material
     * If it's dust material, dust compressor recipe into plate will be generated
     * If it's metal material, bending machine recipes will be generated
     * If block is found, cutting machine recipe will be also generated
     */
    public static final long GENERATE_PLATE = createFlag(12);

    /**
     * Whenever system should generate fluid block for this fluid material
     * Renamed to GENERATE_FLUID_BLOCK to avoid confusion with dust blocks
     * todo remove
     */
    public static final long GENERATE_FLUID_BLOCK = createFlag(44);
    /**
     * Add this flag to enable plasma generation for this material
     * todo remove
     */
    public static final long GENERATE_PLASMA = createFlag(9);


    public static final long GENERATE_LENS = createFlag(37);
    public static final long GENERATE_FOIL = createFlag(25);
    public static final long GENERATE_BOLT_SCREW = createFlag(26);
    public static final long GENERATE_RING = createFlag(27);
    public static final long GENERATE_SPRING = createFlag(28);
    public static final long GENERATE_FINE_WIRE = createFlag(29);
    public static final long GENERATE_ROTOR = createFlag(30);
    public static final long GENERATE_SMALL_GEAR = createFlag(31);
    public static final long GENERATE_DENSE = createFlag(32);
    public static final long GENERATE_SPRING_SMALL = createFlag(33);
    public static final long GENERATE_ROUND = createFlag(51);
    public static final long GENERATE_DOUBLE_PLATE = createFlag(52);
    public static final long GENERATE_ROD = createFlag(20);
    public static final long GENERATE_GEAR = createFlag(21);
    public static final long GENERATE_LONG_ROD = createFlag(22);
    public static final long GENERATE_FRAME = createFlag(45);

    static {
        registerMaterialFlagsHolder(Material.MatFlags.class, Material.class);
    }

    public static long createFlag(int id) {
        return 1L << id;
    }
}
