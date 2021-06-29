package gregtech.api.unification.material.type;

import com.google.common.collect.ImmutableList;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.common.pipelike.cable.WireProperties;
import gregtech.common.pipelike.fluidpipe.FluidPipeProperties;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import stanhebben.zenscript.annotations.ZenProperty;

import javax.annotation.Nullable;

import static gregtech.api.unification.material.type.DustMaterial.MatFlags.GENERATE_PLATE;
import static gregtech.api.unification.material.type.DustMaterial.MatFlags.SMELT_INTO_FLUID;
import static gregtech.api.unification.material.type.IngotMaterial.MatFlags.*;
import static gregtech.api.unification.material.type.SolidMaterial.MatFlags.GENERATE_FRAME;
import static gregtech.api.unification.material.type.SolidMaterial.MatFlags.GENERATE_ROD;
import static gregtech.api.util.GTUtility.createFlag;

@ZenClass("mods.gregtech.material.IngotMaterial")
@ZenRegister
public class IngotMaterial extends SolidMaterial {

    public static final class MatFlags {

        public static final long GENERATE_FOIL = createFlag(25);
        public static final long GENERATE_BOLT_SCREW = createFlag(26);
        public static final long GENERATE_RING = createFlag(27);
        public static final long GENERATE_SPRING = createFlag(28);
        public static final long GENERATE_FINE_WIRE = createFlag(29);
        public static final long GENERATE_ROTOR = createFlag(30);
        public static final long GENERATE_SMALL_GEAR = createFlag(31);
        public static final long GENERATE_DENSE = createFlag(32);
        public static final long GENERATE_SPRING_SMALL = createFlag(33);

        /**
         * Add this to your Material if you want to have its Ore Calcite heated in a Blast Furnace for more output. Already listed are:
         * Iron, Pyrite, PigIron, WroughtIron.
         */
        public static final long BLAST_FURNACE_CALCITE_DOUBLE = createFlag(35);
        public static final long BLAST_FURNACE_CALCITE_TRIPLE = createFlag(36);

        public static final long GENERATE_ROUND = createFlag(51);
        public static final long GENERATE_DOUBLE_PLATE = createFlag(52);

        static {
            Material.MatFlags.registerMaterialFlagsHolder(MatFlags.class, IngotMaterial.class);
        }
    }

    /**
     * Specifies a material into which this material parts turn when heated
     */
    public IngotMaterial smeltInto;

    /**
     * Specifies a material into which this material parts turn when heated in arc furnace
     */
    public IngotMaterial arcSmeltInto;

    /**
     * Material which obtained when this material is polarized
     */
    @Nullable
    public IngotMaterial magneticMaterial;

    /**
     * Blast furnace temperature of this material
     * Equal to zero if material doesn't use blast furnace
     * If below 1000K, primitive blast furnace recipes will be also added.
     * If above 1750K, a Hot Ingot and its Vacuum Freezer recipe will be also added.
     */
    @ZenProperty
    public final int blastFurnaceTemperature;

    /**
     * If set, cable will be generated for this material with base stats
     * specified by this field
     */
    @Nullable
    public WireProperties cableProperties;

    /**
     * If set, fluid pipe will be generated for this materials with stats
     * specified by this field
     */
    @Nullable
    public FluidPipeProperties fluidPipeProperties;

    public IngotMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, Element element, float toolSpeed, float attackDamage, int toolDurability, int blastFurnaceTemperature) {
        super(metaItemSubId, name, materialRGB, materialIconSet, harvestLevel, materialComponents, materialGenerationFlags, element, toolSpeed, attackDamage, toolDurability);
        this.blastFurnaceTemperature = blastFurnaceTemperature;
        this.smeltInto = this;
        this.arcSmeltInto = this;
        addFlag(SMELT_INTO_FLUID);
    }

    public IngotMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, Element element) {
        this(metaItemSubId, name, materialRGB, materialIconSet, harvestLevel, materialComponents, materialGenerationFlags, element, 0, 0, 0, 0);
    }

    public IngotMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, Element element, int blastFurnaceTemperature) {
        this(metaItemSubId, name, materialRGB, materialIconSet, harvestLevel, materialComponents, materialGenerationFlags, element, 0, 0, 0, blastFurnaceTemperature);
    }

    public IngotMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, Element element, float toolSpeed, float attackDamage, int toolDurability) {
        this(metaItemSubId, name, materialRGB, materialIconSet, harvestLevel, materialComponents, materialGenerationFlags, element, toolSpeed, attackDamage, toolDurability, 0);
    }

    public IngotMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, float toolSpeed, float attackDamage, int toolDurability) {
        this(metaItemSubId, name, materialRGB, materialIconSet, harvestLevel, materialComponents, materialGenerationFlags, null, toolSpeed, attackDamage, toolDurability, 0);
    }

    public IngotMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags) {
        this(metaItemSubId, name, materialRGB, materialIconSet, harvestLevel, materialComponents, materialGenerationFlags, null, 0, 0, 0, 0);
    }

    @Override
    protected void initializeMaterial() {
        super.initializeMaterial();
        if (blastFurnaceTemperature > 0) {
            setFluidTemperature(blastFurnaceTemperature);
        } else {
            setFluidTemperature(1273);
        }
    }

    @Override
    protected long verifyMaterialBits(long generationBits) {
        if ((generationBits & GENERATE_DENSE) > 0) {
            generationBits |= GENERATE_PLATE;
        }
        if ((generationBits & GENERATE_ROTOR) > 0) {
            generationBits |= GENERATE_BOLT_SCREW;
            generationBits |= GENERATE_RING;
            generationBits |= GENERATE_PLATE;
        }
        if ((generationBits & GENERATE_SMALL_GEAR) > 0) {
            generationBits |= GENERATE_PLATE;
        }
        if ((generationBits & GENERATE_FINE_WIRE) > 0) {
            generationBits |= GENERATE_FOIL;
        }
        if ((generationBits & GENERATE_FOIL) > 0) {
            generationBits |= GENERATE_PLATE;
        }
        if ((generationBits & GENERATE_RING) > 0) {
            generationBits |= GENERATE_ROD;
        }
        if ((generationBits & GENERATE_BOLT_SCREW) > 0) {
            generationBits |= GENERATE_ROD;
        }
        if ((generationBits & GENERATE_FRAME) > 0) {
            generationBits |= GENERATE_ROD;
        }
        if ((generationBits & GENERATE_DOUBLE_PLATE) > 0) {
            generationBits |= GENERATE_PLATE;
        }

        return super.verifyMaterialBits(generationBits);
    }

    public void setSmeltingInto(IngotMaterial smeltInto) {
        this.smeltInto = smeltInto;
    }

    public void setArcSmeltingInto(IngotMaterial arcSmeltingInto) {
        this.arcSmeltInto = arcSmeltingInto;
    }

    @ZenMethod
    public void setCableProperties(long voltage, int baseAmperage, int lossPerBlock) {
        this.cableProperties = new WireProperties((int) voltage, baseAmperage, lossPerBlock);
    }

    @ZenMethod
    public void setFluidPipeProperties(int maxTemperature, int throughput, boolean gasProof) {
        this.fluidPipeProperties = new FluidPipeProperties(maxTemperature, throughput, gasProof);
    }

}
