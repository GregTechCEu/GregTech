package gregtech.api.fluids;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.common.blocks.MetaBlocks;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.google.common.collect.BiMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;

public class GTFluidRegistration {

    public static final GTFluidRegistration INSTANCE = new GTFluidRegistration();

    private static final Collection<ResourceLocation> fluidSprites = new ObjectOpenHashSet<>();

    private static @Nullable BiMap<String, Fluid> MASTER_FLUID_REFERENCE;

    private static @Nullable BiMap<String, String> DEFAULT_FLUID_NAME;

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
                // noinspection unchecked
                MASTER_FLUID_REFERENCE = (BiMap<String, Fluid>) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException("Could not reflect the Forge Master Fluid Registry", e);
            }
        }
        Objects.requireNonNull(MASTER_FLUID_REFERENCE);

        if (DEFAULT_FLUID_NAME == null) {
            try {
                Field field = FluidRegistry.class.getDeclaredField("defaultFluidName");
                field.setAccessible(true);
                // noinspection unchecked
                DEFAULT_FLUID_NAME = (BiMap<String, String>) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException("Could not reflect the Forge Default Fluid Name map", e);
            }
        }
        Objects.requireNonNull(DEFAULT_FLUID_NAME);

        String masterKey = MASTER_FLUID_REFERENCE.inverse().get(fluid);
        if (masterKey != null && masterKey.startsWith(GTValues.MODID + ":")) {
            MASTER_FLUID_REFERENCE.inverse().put(fluid, modid + ':' + fluid.getName());
        }

        String defaultName = DEFAULT_FLUID_NAME.get(fluid.getName());
        if (defaultName.startsWith(GTValues.MODID + ":")) {
            DEFAULT_FLUID_NAME.put(fluid.getName(), modid + ':' + fluid.getName());
        }
    }

    @ApiStatus.Internal
    public void register() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            FluidProperty property = material.getProperty(PropertyKey.FLUID);
            if (property != null) {
                property.getStorage().registerFluids(material);
            }
        }
    }

    @ApiStatus.Internal
    public void registerSprites(@NotNull TextureMap textureMap) {
        for (ResourceLocation spriteLocation : fluidSprites) {
            textureMap.registerSprite(spriteLocation);
        }
    }

    /**
     * Register a fluid.
     *
     * @param fluid          the fluid to register
     * @param modid          the modid which owns the fluid
     * @param generateBucket if a universal bucket entry should be generated
     */
    public void registerFluid(@NotNull Fluid fluid, @NotNull String modid, boolean generateBucket) {
        boolean didExist = FluidRegistry.getFluid(fluid.getName()) != null;
        FluidRegistry.registerFluid(fluid);
        if (!didExist) {
            // If it didn't exist, that means that this is a fresh fluid of our own
            // creation and not one which is being transformed by a Material.
            fluidSprites.add(fluid.getStill());
            fluidSprites.add(fluid.getFlowing());
            fixFluidRegistryName(fluid, modid);
        }
        if (generateBucket) {
            FluidRegistry.addBucketForFluid(fluid);
        }
    }

    /**
     * Register a fluid block.
     * <p>
     * Requires using {@link BlockFluidBase#setRegistryName} before calling.
     *
     * @param block the fluid block to register
     */
    public void registerFluidBlock(@NotNull BlockFluidBase block) {
        MetaBlocks.FLUID_BLOCKS.add(block);
    }
}
