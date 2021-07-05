package gregtech.api.unification.material.type;

import com.google.common.collect.ImmutableList;
import crafttweaker.annotations.ZenRegister;
import gregtech.api.unification.Element;
import gregtech.api.unification.material.MaterialIconSet;
import gregtech.api.unification.stack.MaterialStack;
import stanhebben.zenscript.annotations.ZenClass;

import static gregtech.api.unification.material.type.DustMaterial.MatFlags.GENERATE_PLATE;
import static gregtech.api.util.GTUtility.createFlag;

@ZenClass("mods.gregtech.material.GemMaterial")
@ZenRegister
public class GemMaterial extends SolidMaterial {

    public static final class MatFlags {

        /**
         * If this material is crystallisable
         */
        public static final long CRYSTALLISABLE = createFlag(34);

        public static final long GENERATE_LENSE = createFlag(37);

        public static final long HIGH_SIFTER_OUTPUT = createFlag(38);

        static {
            Material.MatFlags.registerMaterialFlagsHolder(MatFlags.class, GemMaterial.class);
        }
    }

    public GemMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, Element element, float toolSpeed, float attackDamage, int toolDurability) {
        super(metaItemSubId, name, materialRGB, materialIconSet, harvestLevel, materialComponents, materialGenerationFlags, element, toolSpeed, attackDamage, toolDurability);
    }

    public GemMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags, float toolSpeed, float attackDamage, int toolDurability) {
        super(metaItemSubId, name, materialRGB, materialIconSet, harvestLevel, materialComponents, materialGenerationFlags, null, toolSpeed, attackDamage, toolDurability);
    }

    public GemMaterial(int metaItemSubId, String name, int materialRGB, MaterialIconSet materialIconSet, int harvestLevel, ImmutableList<MaterialStack> materialComponents, long materialGenerationFlags) {
        super(metaItemSubId, name, materialRGB, materialIconSet, harvestLevel, materialComponents, materialGenerationFlags, null, 0, 0, 0);
    }

    @Override
    public long verifyMaterialBits(long generationBits) {
        if ((generationBits & MatFlags.GENERATE_LENSE) > 0) {
            generationBits |= GENERATE_PLATE;
        }
        return super.verifyMaterialBits(generationBits);
    }
}

