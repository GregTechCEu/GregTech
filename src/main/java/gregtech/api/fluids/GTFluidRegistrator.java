package gregtech.api.fluids;

import gregtech.api.GregTechAPI;
import gregtech.api.fluids.block.GTFluidMaterial;
import gregtech.api.fluids.block.MaterialFluidBlock;
import gregtech.api.fluids.definition.FluidDefinition;
import gregtech.api.fluids.definition.MaterialFluidDefinition;
import gregtech.api.fluids.fluid.IAdvancedFluid;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Handles registration and creation of all GregTech Fluids
 */
public final class GTFluidRegistrator {

    private static final Collection<ResourceLocation> fluidSprites = new ObjectOpenHashSet<>();
    private static final Map<String, Set<String>> alternativeFluidNames = new Object2ObjectOpenHashMap<>();

    private GTFluidRegistrator() {/**/}

    public static void registerSprites(@Nonnull TextureMap textureMap) {
        for (ResourceLocation spriteLocation : fluidSprites) {
            textureMap.registerSprite(spriteLocation);
        }
    }

    /**
     * Internal use only. Initializes the registrator.
     */
    public static void init() {
        overrideMaterialFluid(Materials.Water, FluidTypes.LIQUID, FluidRegistry.WATER);
        overrideMaterialFluid(Materials.Lava, FluidTypes.LIQUID, FluidRegistry.LAVA);

        MinecraftForge.EVENT_BUS.post(new MaterialFluidOverrideEvent());

        addAlternativeNames();

        MinecraftForge.EVENT_BUS.post(new AlternativeFluidNameEvent());

        registerMaterialFluids();
    }

    /**
     * Override a material's fluid with another
     *
     * @param material the material owning the fluid to override
     * @param type     the type corresponding to the fluid
     * @param fluid    the fluid to use
     */
    public static void overrideMaterialFluid(@Nonnull Material material, @Nonnull FluidType type, @Nonnull Fluid fluid) {
        material.getProperty(PropertyKey.FLUID).setFluid(type, fluid);
        FluidTooltipUtil.registerTooltip(fluid, createMaterialFluidTooltip(material, fluid));
    }

    private static void addAlternativeNames() {
        addAlternativeFluidName(Materials.Ethanol, FluidTypes.LIQUID, "bio.ethanol");
        addAlternativeFluidName(Materials.SeedOil, FluidTypes.LIQUID, "seed.oil");
        addAlternativeFluidName(Materials.Ice, FluidTypes.LIQUID, "fluid.ice");
        addAlternativeFluidName(Materials.Diesel, FluidTypes.LIQUID, "fuel");
    }

    /**
     * Add an alternative name for a fluid, to use if found instead of the default
     *
     * @param material        the material whose fluid to get
     * @param type            the type for the fluid
     * @param alternativeName the alternative name
     */
    public static void addAlternativeFluidName(@Nonnull Material material, @Nonnull FluidType type, @Nonnull String alternativeName) {
        addAlternativeFluidName(type.getFluidNameForMaterial(material), alternativeName);
    }

    /**
     * Add an alternative name for a fluid, to use if found instead of the default
     *
     * @param registeredName  the default name of the fluid
     * @param alternativeName the alternative name
     */
    public static void addAlternativeFluidName(@Nonnull String registeredName, @Nonnull String alternativeName) {
        Set<String> set = alternativeFluidNames.get(registeredName);
        if (set == null) {
            set = new ObjectOpenHashSet<>();
            alternativeFluidNames.put(registeredName, set);
        }
        set.add(alternativeName);
    }

    /**
     * Remove all alternative names for a fluid
     *
     * @param registeredName the fluid's registered name
     */
    @SuppressWarnings("unused")
    public static void removeAlternativeFluidName(@Nonnull String registeredName) {
        alternativeFluidNames.remove(registeredName);
    }

    /**
     * Remove an alternative name for a fluid
     *
     * @param registeredName  the fluid's registered name
     * @param alternativeName the name to remove
     */
    @SuppressWarnings("unused")
    public static void removeAlternativeFluidName(@Nonnull String registeredName, @Nonnull String alternativeName) {
        Set<String> set = alternativeFluidNames.get(registeredName);
        if (set != null) set.remove(alternativeName);
    }

    /**
     * Registers a fluid texture for use
     *
     * @param textureLocation the location of the texture to use
     */
    public static void registerFluidTexture(@Nonnull ResourceLocation textureLocation) {
        fluidSprites.add(textureLocation);
    }

    /**
     * @param fluidName the name of the fluid
     * @return the fluid associated with the name
     */
    @Nullable
    public static Fluid getExistingFluid(@Nonnull String fluidName) {
        // if the fluid already exists
        Fluid fluid = FluidRegistry.getFluid(fluidName);
        if (fluid != null) return fluid;

        // check if fluid is registered from elsewhere under an alternative name, if not already found
        if (alternativeFluidNames.containsKey(fluidName)) {
            for (String altName : alternativeFluidNames.get(fluidName)) {
                // if the fluid already exists
                fluid = FluidRegistry.getFluid(altName);
                if (fluid != null) return fluid;
            }
        }
        return null;
    }

    /**
     * Create a standard Fluid compatible with the GT Fluid System from a definition.
     *
     * @param registryName the registry name for the fluid
     * @param definition   the definition of the fluid
     * @return the already existing fluid if it exists, otherwise a new fluid
     */
    @SuppressWarnings("unused")
    @Nonnull
    public static Fluid createFluid(@Nonnull String registryName, @Nonnull FluidDefinition definition) {
        // if the fluid already exists, exit
        Fluid existing = getExistingFluid(registryName);
        if (existing != null) return existing;

        registerFluidTexture(definition.getStill());
        registerFluidTexture(definition.getFlowing());

        Fluid fluid = definition.constructFluid(registryName);
        FluidTooltipUtil.registerTooltip(fluid, createFluidTooltip(fluid));

        return fluid;
    }

    private static void registerMaterialFluids() {
        for (Material material : GregTechAPI.MATERIAL_REGISTRY) {
            FluidProperty fluidProperty = material.getProperty(PropertyKey.FLUID);
            if (fluidProperty == null) continue;
            registerMaterialFluid(material, fluidProperty);
        }
    }

    /**
     * Register and create all of a material's fluids
     *
     * @param material the material owning the fluid
     * @param property the FluidProperty of the material
     */
    public static void registerMaterialFluid(@Nonnull Material material, @Nonnull FluidProperty property) {
        for (MaterialFluidDefinition definition : property.getDefinitions()) {
            String fluidName = definition.getType().getFluidNameForMaterial(material);

            // if the fluid already exists, don't make a new one from GT
            Fluid fluid = getExistingFluid(fluidName);
            if (fluid == null) {
                // construct the fluid
                fluid = definition.constructFluid(material, fluidName);

                // register the fluid's textures
                // must be done after material fluids are created
                registerFluidTexture(definition.getStill());
                registerFluidTexture(definition.getFlowing());
            }

            // store the fluid in the property
            property.setFluid(definition.getType(), fluid);

            // add buckets for each fluid
            // this also registers it if not already registered
            FluidRegistry.addBucketForFluid(fluid);

            // create the tooltip for each fluid
            FluidTooltipUtil.registerTooltip(fluid, createMaterialFluidTooltip(material, fluid));

            // create fluid blocks for fluids which have them
            if (definition.hasBlock() && fluid.getBlock() == null) {
                GTFluidMaterial fluidMaterial = new GTFluidMaterial(GTUtility.getMapColor(definition.getColor()),
                        material.hasFlag(MaterialFlags.STICKY));

                BlockFluidBase fluidBlock = new MaterialFluidBlock(fluid, fluidMaterial, material);
                fluidBlock.setRegistryName("fluid." + fluidName);
                MetaBlocks.FLUID_BLOCKS.add(fluidBlock);
            }
        }
    }

    /**
     * Create a fluid tooltip for a material
     * @param material the material whose tooltip to create
     * @param fluid the fluid to add the tooltip to
     * @return the tooltip
     */
    @Nonnull
    public static List<String> createMaterialFluidTooltip(@Nonnull Material material, @Nonnull Fluid fluid) {
        List<String> tooltip = createFluidTooltip(fluid);
        if (!material.getChemicalFormula().isEmpty()) {
            tooltip.add(0, TextFormatting.YELLOW + material.getChemicalFormula());
        }

        return tooltip;
    }

    /**
     * Create a fluid tooltip
     * @param fluid the fluid to create the tooltip for
     * @return the tooltip
     */
    @Nonnull
    public static List<String> createFluidTooltip(@Nonnull Fluid fluid) {
        List<String> tooltip = new ArrayList<>();
        final int temperature = fluid.getTemperature();
        tooltip.add(LocalizationUtils.format("gregtech.fluid.temperature", temperature));
        if (fluid instanceof IAdvancedFluid) {
            IAdvancedFluid advanced = (IAdvancedFluid) fluid;
            tooltip.add(I18n.format(advanced.getState().getTooltipTranslationKey()));
            advanced.getTags().forEach(tag -> tag.appendTooltips(tooltip));
        } else {
            if (fluid.isGaseous()) tooltip.add(I18n.format(FluidState.GAS.getTooltipTranslationKey()));
            else tooltip.add(I18n.format(FluidState.LIQUID.getTooltipTranslationKey()));
        }

        if (GTUtility.isTemperatureCryogenic(temperature)) {
            tooltip.add(I18n.format("gregtech.fluid.temperature.cryogenic"));
        }
        return tooltip;
    }

    /**
     * Called during the fluid registration phase, where fluids for materials can be overridden with another.
     * <p>
     * Use {@link GTFluidRegistrator#overrideMaterialFluid(Material, FluidType, Fluid)} to add overrides.
     */
    public static class MaterialFluidOverrideEvent extends Event {/**/}

    /**
     * Called during the fluid registration phase where alternative registry names for fluids can be added or removed.
     * <p>
     * Use any of the following to achieve this:
     * <ul>
     * <li>{@link GTFluidRegistrator#addAlternativeFluidName(String, String)}</li>
     * <li>{@link GTFluidRegistrator#addAlternativeFluidName(Material, FluidType, String)}</li>
     * <li>{@link GTFluidRegistrator#removeAlternativeFluidName(String)}</li>
     * <li>{@link GTFluidRegistrator#removeAlternativeFluidName(String, String)}</li>
     * </ul>
     */
    public static class AlternativeFluidNameEvent extends Event {/**/}
}
