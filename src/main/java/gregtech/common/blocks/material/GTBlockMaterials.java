package gregtech.common.blocks.material;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class GTBlockMaterials {

    public static final Material POWDERBARREL = new PowderbarrelMaterial();

    private static class PowderbarrelMaterial extends Material {

        public PowderbarrelMaterial() {
            super(MapColor.STONE);
            setAdventureModeExempt();
            setImmovableMobility();
        }
    }
}
