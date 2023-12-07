package gregtech.api;

import gregtech.api.advancement.IAdvancementManager;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.command.ICommandManager;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.event.HighTierEvent;
import gregtech.api.gui.UIFactory;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IBatteryData;
import gregtech.api.modules.IModuleManager;
import gregtech.api.network.INetworkHandler;
import gregtech.api.sound.ISoundManager;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.registry.IMaterialRegistryManager;
import gregtech.api.unification.material.registry.MarkerMaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import gregtech.api.util.BaseCreativeTab;
import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.GTLog;
import gregtech.api.util.IBlockOre;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockWarningSign;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import gregtech.common.metatileentities.MetaTileEntities;

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
    private static boolean highTier;
    private static boolean highTierInitialized;

    public static final GTControlledRegistry<ResourceLocation, MetaTileEntity> MTE_REGISTRY = new GTControlledRegistry<>(
            Short.MAX_VALUE);
    public static final GTControlledRegistry<ResourceLocation, UIFactory> UI_FACTORY_REGISTRY = new GTControlledRegistry<>(
            Short.MAX_VALUE);
    public static final GTControlledRegistry<ResourceLocation, CoverDefinition> COVER_REGISTRY = new GTControlledRegistry<>(
            Integer.MAX_VALUE);

    public static BlockMachine MACHINE;
    public static final Map<Material, Map<StoneType, IBlockOre>> oreBlockTable = new HashMap<>();
    public static final Object2ObjectMap<IBlockState, IHeatingCoilBlockStats> HEATING_COILS = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectMap<IBlockState, IBatteryData> PSS_BATTERIES = new Object2ObjectOpenHashMap<>();

    public static final BaseCreativeTab TAB_GREGTECH = new BaseCreativeTab(GTValues.MODID + ".main",
            () -> MetaItems.LOGO.getStackForm(), true);
    public static final BaseCreativeTab TAB_GREGTECH_MACHINES = new BaseCreativeTab(GTValues.MODID + ".machines",
            () -> MetaTileEntities.ELECTRIC_BLAST_FURNACE.getStackForm(), true);
    public static final BaseCreativeTab TAB_GREGTECH_CABLES = new BaseCreativeTab(GTValues.MODID + ".cables",
            () -> OreDictUnifier.get(OrePrefix.cableGtDouble, Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_PIPES = new BaseCreativeTab(GTValues.MODID + ".pipes",
            () -> OreDictUnifier.get(OrePrefix.pipeNormalFluid, Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_TOOLS = new BaseCreativeTab(GTValues.MODID + ".tools",
            () -> ToolItems.HARD_HAMMER.get(Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_MATERIALS = new BaseCreativeTab(GTValues.MODID + ".materials",
            () -> OreDictUnifier.get(OrePrefix.ingot, Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_ORES = new BaseCreativeTab(GTValues.MODID + ".ores",
            () -> OreDictUnifier.get(OrePrefix.ore, Materials.Aluminium), true);
    public static final BaseCreativeTab TAB_GREGTECH_DECORATIONS = new BaseCreativeTab(GTValues.MODID + ".decorations",
            () -> MetaBlocks.WARNING_SIGN.getItemVariant(BlockWarningSign.SignType.YELLOW_STRIPES), true);

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
