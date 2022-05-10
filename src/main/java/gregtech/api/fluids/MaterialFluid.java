package gregtech.api.fluids;

import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.material.Material;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.GTUtility;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class MaterialFluid extends Fluid {

    private final Material material;
    private final FluidType fluidType;

    public MaterialFluid(String fluidName, @Nonnull Material material, @Nonnull FluidType fluidType, ResourceLocation texture) {
        super(fluidName, texture, texture, GTUtility.convertRGBtoOpaqueRGBA_MC(material.getMaterialRGB()));
        this.material = material;
        this.fluidType = fluidType;
    }

    public void registerFluidTooltip() {
        FluidTooltipUtil.registerTooltip(this, FluidTooltipUtil.getMaterialTooltip(material, getTemperature(), fluidType.equals(FluidTypes.PLASMA)));
    }

    @Nonnull
    public Material getMaterial() {
        return this.material;
    }

    @Nonnull
    public FluidType getFluidType() {
        return this.fluidType;
    }

    @Override
    public String getUnlocalizedName() {
        return material.getUnlocalizedName();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getLocalizedName(FluidStack stack) {
        String localizedName = I18n.format(getUnlocalizedName());
        if (fluidType != null) {
            return I18n.format(fluidType.getLocalization(), localizedName);
        }
        return localizedName;
    }
}
