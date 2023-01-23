package gregtech.api.fluids;

import gregtech.api.fluids.info.FluidState;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class AdvancedMaterialFluid extends AdvancedFluid {

    private static long AIR_MASS = -1;

    private final Material material;

    /**
     * <strong>Do not construct this outside of a definition's {@code constructFluid} method.</strong>
     * @param name       the name of the fluid
     * @param material   the fluid's backing material
     * @param definition the fluid's definition
     */
    public AdvancedMaterialFluid(@Nonnull String name, @Nonnull Material material, @Nonnull MaterialFluidDefinition definition) {
        super(name, material.getMaterialRGB(), definition);
        this.material = material;
        if (definition.getState() == FluidState.GAS) {
            // use positive density for materials heavier than air, so the universal bucket's orientation is correct
            if (AIR_MASS < 0) AIR_MASS = Materials.Air.getMass();
            this.density = Math.abs(this.density);
        }
        if (material.hasFlag(MaterialFlags.STICKY)) this.viscosity = 2000;
    }

    @Override
    public String getLocalizedName(FluidStack stack) {
        return I18n.format(getUnlocalizedName(), I18n.format(this.material.getUnlocalizedName()));
    }

    @Nonnull
    public Material getMaterial() {
        return this.material;
    }
}
