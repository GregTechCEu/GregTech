package gregtech.api.fluids;

import com.google.common.collect.BiMap;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.common.blocks.MetaBlocks;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;

public class GTFluidRegistration {

    public static final GTFluidRegistration INSTANCE = new GTFluidRegistration();

    private static final Collection<ResourceLocation> fluidSprites = new ObjectOpenHashSet<>();

    private static @Nullable BiMap<String, Fluid> MASTER_FLUID_REFERENCE;

    public void init() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            FluidProperty property = material.getProperty(PropertyKey.FLUID);
            if (property != null) {
                property.getStorage().register(material);
            }
        }
    }

    public void registerSprites(TextureMap textureMap) {
        for (ResourceLocation spriteLocation : fluidSprites) {
            textureMap.registerSprite(spriteLocation);
        }
    }

    public void registerFluid(@NotNull Fluid fluid, @NotNull String modid, boolean hasBucket) {
        fluidSprites.add(fluid.getStill());
        fluidSprites.add(fluid.getFlowing());

        FluidRegistry.registerFluid(fluid);
        fixFluidRegistryName(fluid, modid);

        if (hasBucket) {
            FluidRegistry.addBucketForFluid(fluid);
        }
    }

    public void registerFluidBlock(@NotNull BlockFluidClassic block) {
        MetaBlocks.FLUID_BLOCKS.add(block);
    }

    /**
     * Fixes all registered fluids being under the gregtech modid
     *
     * @param fluid the fluid to correct
     * @param modid the correct modid for the fluid
     */
    private static void fixFluidRegistryName(@NotNull Fluid fluid, @NotNull String modid) {
        if (GTValues.MODID.equals(modid)) return;

        if (MASTER_FLUID_REFERENCE == null) {
            try {
                Field field = FluidRegistry.class.getDeclaredField("masterFluidReference");
                field.setAccessible(true);
                //noinspection unchecked
                MASTER_FLUID_REFERENCE = (BiMap<String, Fluid>) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException("Could not reflect the Forge Master Fluid Registry", e);
            }
        }
        MASTER_FLUID_REFERENCE.inverse().put(fluid, modid + ':' + fluid.getName());
    }
}
