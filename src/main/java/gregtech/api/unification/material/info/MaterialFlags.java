package gregtech.api.unification.material.info;

import gregtech.api.unification.material.Material;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MaterialFlags {

    private final Set<MaterialFlag> flags = new HashSet<>();

    public MaterialFlags addFlags(MaterialFlag... flags) {
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    public void verify(Material material) {
        flags.addAll(flags.stream()
                .map(f -> f.verifyFlag(material))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));
    }

    public boolean hasFlag(MaterialFlag flag) {
        return flags.contains(flag);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        flags.forEach(f -> sb.append(f.toString()).append("\n"));
        return sb.toString();
    }

    /////////////////
    //   GENERIC   //
    /////////////////

    /**
     * Add to material to disable it's unification fully
     * todo implement
     */
    public static final MaterialFlag NO_UNIFICATION =
            new MaterialFlag.Builder(0, "no_unification").build();

    /**
     * Decomposition recipe requires hydrogen as additional input. Amount is equal to input amount
     */
    public static final MaterialFlag DECOMPOSITION_REQUIRES_HYDROGEN =
            new MaterialFlag.Builder(1, "decomposition_requires_hydrogen").build();

    /**
     * Enables electrolyzer decomposition recipe generation
     */
    public static final MaterialFlag DECOMPOSITION_BY_ELECTROLYZING =
            new MaterialFlag.Builder(2, "decomposition_by_electrolyzing").build();

    /**
     * Enables centrifuge decomposition recipe generation
     */
    public static final MaterialFlag DECOMPOSITION_BY_CENTRIFUGING =
            new MaterialFlag.Builder(3, "decomposition_by_centrifuging").build();

    /**
     * Disables decomposition recipe generation for this material
     */
    public static final MaterialFlag DISABLE_DECOMPOSITION =
            new MaterialFlag.Builder(4, "disable_decomposition").build();

    /**
     * Add to material if it is some kind of explosive
     */
    public static final MaterialFlag EXPLOSIVE =
            new MaterialFlag.Builder(5, "explosive").build();

    /**
     * Add to material if it is some kind of flammable
     */
    public static final MaterialFlag FLAMMABLE =
            new MaterialFlag.Builder(6, "flammable").build();

    //////////////////
    //     DUST     //
    //////////////////

    /**
     * Generate a plate for this material
     * If it's dust material, dust compressor recipe into plate will be generated
     * If it's metal material, bending machine recipes will be generated
     * If block is found, cutting machine recipe will be also generated
     */
    public static final MaterialFlag GENERATE_PLATE =
            new MaterialFlag.Builder(7, "generate_plate").build();

    public static final MaterialFlag GENERATE_ROD =
            new MaterialFlag.Builder(8, "generate_rod").build();

    public static final MaterialFlag GENERATE_FRAME =
            new MaterialFlag.Builder(9, "generate_frame")
                    .requireFlags(GENERATE_ROD).build();

    public static final MaterialFlag GENERATE_GEAR =
            new MaterialFlag.Builder(10, "generate_gear")
                    .requireFlags(GENERATE_PLATE, GENERATE_ROD).build();

    public static final MaterialFlag GENERATE_LONG_ROD =
            new MaterialFlag.Builder(11, "generate_long_rod")
                    .requireFlags(GENERATE_ROD).build();

    /**
     * This will prevent material from creating Shapeless recipes for dust to block and vice versa
     * Also preventing extruding and alloy smelting recipes via SHAPE_EXTRUDING/MOLD_BLOCK
     */
    public static final MaterialFlag EXCLUDE_BLOCK_CRAFTING_RECIPES =
            new MaterialFlag.Builder(12, "exclude_block_crafting_recipes").build();

    public static final MaterialFlag EXCLUDE_PLATE_COMPRESSOR_RECIPE =
            new MaterialFlag.Builder(13, "exclude_plate_compressor_recipe")
                    .requireFlags(GENERATE_PLATE).build();

    /**
     * This will prevent material from creating Shapeless recipes for dust to block and vice versa
     */
    public static final MaterialFlag EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES =
            new MaterialFlag.Builder(14, "exclude_block_crafting_by_hand_recipes").build();

    public static final MaterialFlag MORTAR_GRINDABLE =
            new MaterialFlag.Builder(15, "mortar_grindable").build();

    /////////////////
    //    INGOT    //
    /////////////////

    /**
     * Add to material if it cannot be worked by any other means, than smashing or smelting. This is used for coated Materials.
     */
    public static final MaterialFlag NO_WORKING =
            new MaterialFlag.Builder(16, "no_working").build();

    /**
     * Add to material if it cannot be used for regular Metal working techniques since it is not possible to bend it.
     */
    public static final MaterialFlag NO_SMASHING =
            new MaterialFlag.Builder(17, "no_smashing").build();

    /**
     * Add to material if it's impossible to smelt it
     */
    public static final MaterialFlag NO_SMELTING =
            new MaterialFlag.Builder(18, "no_smelting").build();

    /**
     * Add this to your Material if you want to have its Ore Calcite heated in a Blast Furnace for more output. Already listed are:
     * Iron, Pyrite, PigIron, WroughtIron.
     */
    public static final MaterialFlag BLAST_FURNACE_CALCITE_DOUBLE =
            new MaterialFlag.Builder(19, "blast_furnace_calcite_double").build();

    public static final MaterialFlag BLAST_FURNACE_CALCITE_TRIPLE =
            new MaterialFlag.Builder(20, "blast_furnace_calcite_triple").build();

    public static final MaterialFlag GENERATE_FOIL =
            new MaterialFlag.Builder(21, "generate_foil")
                    .requireFlags(GENERATE_PLATE).build();

    public static final MaterialFlag GENERATE_BOLT_SCREW =
            new MaterialFlag.Builder(22, "generate_bolt_screw")
                    .requireFlags(GENERATE_ROD).build();

    public static final MaterialFlag GENERATE_RING =
            new MaterialFlag.Builder(23, "generate_ring")
                    .requireFlags(GENERATE_ROD).build();

    public static final MaterialFlag GENERATE_SPRING =
            new MaterialFlag.Builder(24, "generate_spring")
                    .requireFlags(GENERATE_LONG_ROD).build();

    public static final MaterialFlag GENERATE_SPRING_SMALL =
            new MaterialFlag.Builder(25, "generate_spring_small")
                    .requireFlags(GENERATE_ROD).build();

    public static final MaterialFlag GENERATE_SMALL_GEAR =
            new MaterialFlag.Builder(26, "generate_small_gear")
                    .requireFlags(GENERATE_PLATE, GENERATE_ROD).build();

    public static final MaterialFlag GENERATE_FINE_WIRE =
            new MaterialFlag.Builder(27, "generate_fine_wire")
                    .requireFlags(GENERATE_FOIL).build();

    public static final MaterialFlag GENERATE_ROTOR =
            new MaterialFlag.Builder(28, "generate_rotor")
                    .requireFlags(GENERATE_BOLT_SCREW, GENERATE_RING, GENERATE_PLATE)
                    .build();

    public static final MaterialFlag GENERATE_DENSE =
            new MaterialFlag.Builder(29, "generate_dense")
                    .requireFlags(GENERATE_PLATE).build();

    public static final MaterialFlag GENERATE_ROUND =
            new MaterialFlag.Builder(30, "generate_round").build();

    /////////////////
    //     GEM     //
    /////////////////

    /**
     * If this material can be crystallized.
     */
    public static final MaterialFlag CRYSTALLIZABLE =
            new MaterialFlag.Builder(31, "crystallizable").build();

    public static final MaterialFlag GENERATE_LENS =
            new MaterialFlag.Builder(32, "generate_lens")
                    .requireFlags(GENERATE_PLATE).build();

    /////////////////
    //     ORE     //
    /////////////////

    public static final MaterialFlag HIGH_SIFTER_OUTPUT =
            new MaterialFlag.Builder(33, "high_sifter_output").build();
}
