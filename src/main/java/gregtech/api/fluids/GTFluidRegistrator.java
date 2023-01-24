package gregtech.api.fluids;

import gregtech.api.GregTechAPI;
import gregtech.api.fluids.info.FluidState;
import gregtech.api.fluids.info.FluidType;
import gregtech.api.fluids.info.FluidTypes;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.common.blocks.MetaBlocks;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class GTFluidRegistrator {

    private static final Collection<ResourceLocation> fluidSprites = new ObjectOpenHashSet<>();
    private static final Map<String, String> alternativeFluidNames = new Object2ObjectOpenHashMap<>();
    private static final Map<String, Material> fluidToMaterialMappings = new Object2ObjectOpenHashMap<>();

    private GTFluidRegistrator() {/**/}

    public static void registerSprites(TextureMap textureMap) {
        for (ResourceLocation spriteLocation : fluidSprites) {
            textureMap.registerSprite(spriteLocation);
        }
    }

    public static void init() {
        overrideMaterialFluid(Materials.Water, FluidTypes.LIQUID, FluidRegistry.WATER);
        overrideMaterialFluid(Materials.Lava, FluidTypes.LIQUID, FluidRegistry.LAVA);

        registerMaterialFluids();
    }

    /**
     * Override a material's fluid with another
     *
     * @param material the material whose fluid should be overriden
     * @param type     the type corresponding to the fluid
     * @param fluid    the fluid to use
     */
    public static void overrideMaterialFluid(@Nonnull Material material, @Nonnull FluidType type, @Nonnull Fluid fluid) {
        material.getProperty(PropertyKey.FLUID).setFluid(type, fluid);
        createMaterialFluidTooltip(material, fluid);
    }

    private static void addAlternativeNames() {
        setAlternativeFluidName(Materials.Ethanol, FluidTypes.LIQUID, "bio.ethanol");
        setAlternativeFluidName(Materials.SeedOil, FluidTypes.LIQUID, "seed.oil");
        setAlternativeFluidName(Materials.Ice, FluidTypes.LIQUID, "fluid.ice");
        setAlternativeFluidName(Materials.Diesel, FluidTypes.LIQUID, "fuel");
    }

    /**
     * Set an alternative name for a fluid, to use if found instead of the default
     *
     * @param material        the material whose fluid to get
     * @param type            the type for the fluid
     * @param alternativeName the alternative name
     */
    public static void setAlternativeFluidName(@Nonnull Material material, @Nonnull FluidType type, @Nonnull String alternativeName) {
        setAlternativeFluidName(type.getFluidNameForMaterial(material), alternativeName);
    }

    /**
     * Set an alternative name for a fluid, to use if found instead of the default
     *
     * @param registeredName  the default name of the fluid
     * @param alternativeName the alternative name
     */
    public static void setAlternativeFluidName(@Nonnull String registeredName, @Nonnull String alternativeName) {
        alternativeFluidNames.put(registeredName, alternativeName);
    }

    /**
     * Registers the fluid texture for use
     *
     * @param textureLocation the location of the texture to use
     */
    public static void registerFluidTexture(@Nonnull ResourceLocation textureLocation) {
        fluidSprites.add(textureLocation);
    }

    private static void registerMaterialFluids() {
        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {
            FluidProperty fluidProperty = material.getProperty(PropertyKey.FLUID);
            if (fluidProperty == null) continue;
            createMaterialFluid(material, fluidProperty);
        }
    }

    private static void createMaterialFluid(@Nonnull Material material, @Nonnull FluidProperty property) {
        for (MaterialFluidDefinition definition : property.getDefinitions()) {

            String fluidName = definition.getType().getFluidNameForMaterial(material);

            // if the fluid already exists, exit
            if (FluidRegistry.getFluid(fluidName) != null) return;

            // check if fluid is registered from elsewhere under an alternative name, if not already found
            if (alternativeFluidNames.containsKey(fluidName)) {
                String altName = alternativeFluidNames.get(fluidName);
                // if the fluid already exists, exit
                if (FluidRegistry.getFluid(altName) != null) return;
            }

            // construct the fluid
            Fluid fluid = definition.constructFluid(material, fluidName);

            // register the fluid's textures
            // must be done after material fluids are created
            registerFluidTexture(definition.getStill());
            registerFluidTexture(definition.getFlowing());

            property.setFluid(definition.getType(), fluid);

            // add buckets for each fluid - this also registers it
            FluidRegistry.addBucketForFluid(fluid);

            // create the tooltip for each fluid
            FluidTooltipUtil.registerTooltip(fluid, createMaterialFluidTooltip(material, fluid));

            // create fluid blocks for fluids which have them
            if (definition.hasBlock() && fluid.getBlock() == null) {
                GTFluidMaterial fluidMaterial = new GTFluidMaterial(GTUtility.getMapColor(definition.getColor()),
                        material.hasFlag(MaterialFlags.STICKY));

                BlockFluidBase fluidBlock = new MaterialFluidBlock(fluid, fluidMaterial, material);
                fluidBlock.setRegistryName(fluidName);
                MetaBlocks.FLUID_BLOCKS.add(fluidBlock);
            }

            fluidToMaterialMappings.put(fluid.getName(), material);
        }
    }

    @Nonnull
    public static List<String> createMaterialFluidTooltip(@Nonnull Material material, @Nonnull Fluid fluid) {
        List<String> tooltip = createFluidTooltip(fluid);
        if (!material.getChemicalFormula().isEmpty()) {
            tooltip.add(0, TextFormatting.YELLOW + material.getChemicalFormula());
        }

        return tooltip;
    }

    @Nonnull
    public static List<String> createFluidTooltip(@Nonnull Fluid fluid) {
        List<String> tooltip = new ArrayList<>();
        final int temperature = fluid.getTemperature();
        tooltip.add(LocalizationUtils.format("gregtech.fluid.temperature", temperature));
        if (fluid instanceof IAdvancedFluid) {
            IAdvancedFluid advanced = (IAdvancedFluid) fluid;
            tooltip.add(I18n.format(advanced.getState().getTooltipTranslationKey()));
            advanced.getTags().forEach(data -> data.appendTooltips(tooltip));
        } else {
            if (fluid.isGaseous()) tooltip.add(I18n.format(FluidState.GAS.getTooltipTranslationKey()));
            else tooltip.add(I18n.format(FluidState.LIQUID.getTooltipTranslationKey()));
        }

        if (GTUtility.isTemperatureCryogenic(temperature)) {
            tooltip.add(I18n.format("gregtech.fluid.temperature.cryogenic"));
        }
        return tooltip;
    }

    @Nullable
    public static Material getMaterialFromFluid(@Nonnull Fluid fluid) {
        Material material = fluidToMaterialMappings.get(fluid.getName());
        if (material.hasFluid()) return material;
        return null;
    }
}
