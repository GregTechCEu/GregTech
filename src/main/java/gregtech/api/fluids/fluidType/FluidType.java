package gregtech.api.fluids.fluidType;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.liquid.ILiquidStack;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.GTLog;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ZenClass("mods.gregtech.material.FluidType")
@ZenRegister
public abstract class FluidType {

    private static final Map<String, FluidType> FLUID_TYPES = new HashMap<>();

    private final String name;
    private final String prefix;
    private final String suffix;
    private final String localization;

    public FluidType(@Nonnull String name, @Nullable String prefix, @Nullable String suffix, @Nonnull String localization) {
        if (FLUID_TYPES.get(name) != null)
            throw new IllegalArgumentException("Cannot register FluidType with duplicate name: " + name);

        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
        this.localization = localization;
        FLUID_TYPES.put(name, this);
    }

    public String getNameForMaterial(@Nonnull Material material) {
        StringBuilder builder = new StringBuilder();

        if (this.prefix != null)
            builder.append(this.prefix).append(".");

        builder.append(material);

        if (this.suffix != null)
            builder.append(".").append(this.suffix);

        return builder.toString();
    }

    public static void setFluidProperties(@Nonnull FluidType fluidType, @Nonnull Fluid fluid) {
        fluidType.setFluidProperties(fluid);
    }

    protected abstract void setFluidProperties(@Nonnull Fluid fluid);

    @ZenMethod
    @Optional.Method(modid = GTValues.MODID_CT)
    public static void setFluidPropertiesCT(FluidType fluidType, @Nonnull ILiquidStack liquidStack) {
        Material material = GregTechAPI.MATERIAL_REGISTRY.getObject(liquidStack.getName());
        if (material == null || !material.hasProperty(PropertyKey.FLUID)) {
            GTLog.logger.warn("LiquidStack {} does not have a FluidProperty!", liquidStack.getName());
            return;
        }

        fluidType.setFluidProperties(material.getFluid());
    }

    @ZenGetter
    public String getLocalization() {
        return this.localization;
    }

    @ZenGetter
    public String getPrefix() {
        return this.prefix;
    }

    @ZenGetter
    public String getName() {
        return this.name;
    }

    @ZenGetter
    public abstract String getUnlocalizedTooltip();

    @ZenGetter
    public List<String> getAdditionalTooltips() {
        return new ArrayList<>();
    }

    @Nullable
    @ZenMethod
    public static FluidType getByName(@Nonnull String name) {
        return FLUID_TYPES.get(name);
    }
}
