package gregtech.api.unification.material.type;

import com.google.common.collect.ImmutableList;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.stack.MaterialStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.util.GTUtility.createFlag;

@ZenClass("mods.gregtech.material.DustMaterial")
@ZenRegister
public class DustMaterial extends FluidMaterial {

    public static final class MatFlags {

        public static final long GENERATE_ORE = createFlag(11);

        /**
         * Generate a plate for this material
         * If it's dust material, dust compressor recipe into plate will be generated
         * If it's metal material, bending machine recipes will be generated
         * If block is found, cutting machine recipe will be also generated
         */
        public static final long GENERATE_PLATE = createFlag(12);

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
         * Add to material if it is outputting less in an Induction Smelter.
         * todo remove
         */
        public static final long INDUCTION_SMELTING_LOW_OUTPUT = createFlag(16);

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

        static {
            Material.MatFlags.registerMaterialFlagsHolder(MatFlags.class, DustMaterial.class);
        }
    }


    /**
     * List of ore by products
     */
    @ZenProperty
    public final List<FluidMaterial> oreByProducts = new ArrayList<>();

    /**
     * Crushed ore output amount multiplier during maceration
     */
    @ZenProperty
    public int oreMultiplier = 1;

    /**
     * Byproducts output amount multiplier during pulverization
     */
    @ZenProperty
    public int byProductMultiplier = 1;

    /**
     * Tool level needed to harvest block of this material
     */
    @ZenProperty
    public int harvestLevel;

    /**
     * Material to which smelting of this material ore will result
     */
    @ZenProperty
    public SolidMaterial directSmelting;

    /**
     * Disable directSmelting
     */
    @ZenProperty
    public boolean disableDirectSmelting = false;

    /**
     * Material in which this material's ore should be washed to give additional output
     */
    @ZenProperty
    public FluidMaterial washedIn;

    /**
     * During electromagnetic separation, this material ore will be separated onto this material and material specified by this field
     */
    @ZenProperty
    public DustMaterial separatedOnto;

    /**
     * Burn time of this material when used as fuel in furnace smelting
     * Zero or negative value indicates that this material cannot be used as fuel
     */
    @ZenProperty
    public int burnTime = 0;

    /**
     * During OreProcessing (Macerator, OreWasher, ThermalCentrifuge), this material will be turned into crushedInto
     */
    @ZenProperty
    public DustMaterial crushedInto = this;

    @ZenMethod
    public void setHarvestLevel(int harvestLevel) {
        this.harvestLevel = harvestLevel;
    }

    public DustMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, Element element) {
        super(metaItemSubId, name, materialRGB, materialIconSet, materialComponents, materialGenerationFlags, element);
        this.harvestLevel = harvestLevel;
    }

    public DustMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags) {
        super(metaItemSubId, name, materialRGB, materialIconSet, materialComponents, materialGenerationFlags, null);
        this.harvestLevel = harvestLevel;
    }

    @Override
    protected void initializeMaterial() {
        super.initializeMaterial();
        if (shouldGenerateFluid()) {
            setFluidTemperature(1200); //default value for dusts
        }
    }

    @Override
    public boolean shouldGenerateFluid() {
        return hasFlag(MatFlags.SMELT_INTO_FLUID);
    }

    public void addOreByProducts(FluidMaterial... byProducts) {
        this.oreByProducts.addAll(Arrays.asList(byProducts));
    }

    //kept here for binary compatibility
    public void setDirectSmelting(IngotMaterial directSmelting) {
        this.directSmelting = directSmelting;
    }

    public void setDirectSmelting(SolidMaterial directSmelting) {
        this.directSmelting = directSmelting;
    }

    public void setOreMultiplier(int oreMultiplier) {
        this.oreMultiplier = oreMultiplier;
    }

    public void setByProductMultiplier(int byProductMultiplier) {
        this.byProductMultiplier = byProductMultiplier;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
    }
}
