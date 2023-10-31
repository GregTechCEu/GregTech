package gregtech.api.fluids;

import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.attribute.FluidAttribute;
import gregtech.api.unification.material.Material;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

public class GTFluid extends Fluid implements AttributedFluid {

    private final Collection<FluidAttribute> attributes = new ObjectLinkedOpenHashSet<>();
    private final FluidState state;

    public GTFluid(@NotNull String fluidName, ResourceLocation still, ResourceLocation flowing, @NotNull FluidState state) {
        super(fluidName, still, flowing);
        setGaseous(state != FluidState.LIQUID);
        this.state = state;
    }

    @Override
    public @NotNull FluidState getState() {
        return state;
    }

    @Override
    public @NotNull @Unmodifiable Collection<FluidAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public void addAttribute(@NotNull FluidAttribute attribute) {
        attributes.add(attribute);
    }

    public static class GTMaterialFluid extends GTFluid {

        private final Material material;
        private final String translationKey;

        public GTMaterialFluid(@NotNull String fluidName, ResourceLocation still, ResourceLocation flowing,
                               @NotNull FluidState state, @Nullable String translationKey, @NotNull Material material) {
            super(fluidName, still, flowing, state);
            this.material = material;
            this.translationKey = translationKey;
        }

        public @NotNull Material getMaterial() {
            return this.material;
        }

        @Override
        @SideOnly(Side.CLIENT)
        public String getLocalizedName(FluidStack stack) {
            String localizedName;
            String customMaterialTranslation = "fluid." + material.getUnlocalizedName();

            if (I18n.hasKey(customMaterialTranslation)) {
                localizedName = I18n.format(customMaterialTranslation);
            } else {
                localizedName = I18n.format(material.getUnlocalizedName());
            }

            if (translationKey != null) {
                return I18n.format(translationKey, localizedName);
            }
            return localizedName;
        }
    }
}
