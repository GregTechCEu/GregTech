package gregtech.api.fluids.fluid;

import gregtech.api.fluids.definition.MaterialFluidDefinition;
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
        super(name, definition.getColor() == -1 ? material.getMaterialRGB() : definition.getColor(), definition);
        this.material = material;
        if (AIR_MASS == -1) AIR_MASS = Materials.Air.getMass();

        int mass = (int) material.getMass();
        if (mass > AIR_MASS) {
            this.density = (int) (100 * material.getMass());
        } else if (mass < AIR_MASS){
            this.density = (int) ((material.getMass() / -10.0));
            // use positive density for materials heavier than air, so the universal bucket's orientation is correct
            if (definition.getState() == FluidState.GAS) this.density = Math.abs(this.density);
        } else {
            this.density = 0;
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
