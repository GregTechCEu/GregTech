package gregtech.api;

import gregtech.api.advancement.IAdvancementManager;
import gregtech.api.block.ICleanroomFilter;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.block.coil.CoilManager;
import gregtech.api.command.ICommandManager;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.event.HighTierEvent;
import gregtech.api.gui.UIFactory;
import gregtech.api.metatileentity.multiblock.IBatteryData;
import gregtech.api.metatileentity.registry.MTEManager;
import gregtech.api.modules.IModuleManager;
import gregtech.api.network.INetworkHandler;
import gregtech.api.recipes.properties.RecipePropertyRegistry;
import gregtech.api.sound.ISoundManager;
import gregtech.api.unification.RecyclingManager;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.registry.IMaterialRegistryManager;
import gregtech.api.unification.material.registry.MarkerMaterialRegistry;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.GTLog;
import gregtech.api.util.IBlockOre;
import gregtech.common.ConfigHolder;
import gregtech.datafix.migration.lib.MigrationAPI;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;

public class GregTechAPI {

    /** Will always be available */
    public static Object instance;
    /** Will be available at the Construction stage */
    public static IModuleManager moduleManager;
    /** Will be available at the Pre-Initialization stage */
    public static INetworkHandler networkHandler;
    /** Will be available at the Server-Starting stage */
    public static ICommandManager commandManager;
    /** Will be available at the Pre-Initialization stage */
    public static IAdvancementManager advancementManager;
    /** Will be available at the Pre-Initialization stage */
    public static ISoundManager soundManager;
    /** Will be available at the Construction stage */
    public static IMaterialRegistryManager materialManager;
    /** Will be available at the Pre-Initialization stage */
    public static MarkerMaterialRegistry markerMaterialRegistry;
    /** Will be available at the Pre-Initialization stage */
    public static MTEManager mteManager;
    /** Will be available at the Pre-Initialization stage */
    public static CoilManager coilManager;
    /** GT's data migrations API */
    public static final MigrationAPI MIGRATIONS = new MigrationAPI();
    public static final RecipePropertyRegistry RECIPE_PROPERTIES = new RecipePropertyRegistry();
    /**
     * Manager for Item Recycling Data
     */
    public static final RecyclingManager RECYCLING_MANAGER = new RecyclingManager();

    /** Will be available at the Pre-Initialization stage */
    private static boolean highTier;
    private static boolean highTierInitialized;

    @Deprecated
    public static final GTControlledRegistry<ResourceLocation, UIFactory> UI_FACTORY_REGISTRY = new GTControlledRegistry<>(
            Short.MAX_VALUE);
    public static final GTControlledRegistry<ResourceLocation, CoverDefinition> COVER_REGISTRY = new GTControlledRegistry<>(
            Integer.MAX_VALUE);

    public static final Map<Material, Map<StoneType, IBlockOre>> oreBlockTable = new HashMap<>();
    public static final Object2ObjectMap<IBlockState, IHeatingCoilBlockStats> HEATING_COILS = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectMap<IBlockState, IBatteryData> PSS_BATTERIES = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectMap<IBlockState, ICleanroomFilter> CLEANROOM_FILTERS = new Object2ObjectOpenHashMap<>();

    /** Will be available at the Pre-Initialization stage */
    public static boolean isHighTier() {
        return highTier;
    }

    /**
     * Initializes High-Tier. Internal use only, do not attempt to call this.
     */
    static void initializeHighTier() {
        if (highTierInitialized) throw new IllegalStateException("High-Tier is already initialized.");
        HighTierEvent highTierEvent = new HighTierEvent();
        MinecraftForge.EVENT_BUS.post(highTierEvent);

        highTier = ConfigHolder.machines.highTierContent || highTierEvent.isHighTier();
        highTierInitialized = true;

        if (GregTechAPI.isHighTier()) GTLog.logger.info("High-Tier is Enabled.");
        else GTLog.logger.info("High-Tier is Disabled.");
    }

    public static class RegisterEvent<V> extends GenericEvent<V> {

        private final GTControlledRegistry<ResourceLocation, V> registry;

        public RegisterEvent(GTControlledRegistry<ResourceLocation, V> registry, Class<V> clazz) {
            super(clazz);
            this.registry = registry;
        }

        public void register(int id, ResourceLocation key, V value) {
            if (registry != null) registry.register(id, key, value);
        }

        public void register(int id, String key, V value) {
            if (registry != null) registry.register(id,
                    new ResourceLocation(Loader.instance().activeModContainer().getModId(), key), value);
        }
    }
}
