package gregtech.api.util;

import gregtech.api.GTValues;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public enum Mods {

    AdvancedRocketry(Names.ADVANCED_ROCKETRY),
    AppliedEnergistics2(Names.APPLIED_ENERGISTICS2),
    Baubles(Names.BAUBLES),
    BinnieCore(Names.BINNIE_CORE),
    BiomesOPlenty(Names.BIOMES_O_PLENTY),
    BuildCraftCore(Names.BUILD_CRAFT_CORE),
    Chisel(Names.CHISEL),
    CoFHCore(Names.COFH_CORE),
    CTM(Names.CONNECTED_TEXTURES_MOD),
    CubicChunks(Names.CUBIC_CHUNKS),
    CraftTweaker(Names.CRAFT_TWEAKER),
    EnderCore(Names.ENDER_CORE),
    EnderIO(Names.ENDER_IO),
    ExtraBees(Names.EXTRA_BEES),
    ExtraTrees(Names.EXTRA_TREES),
    ExtraUtilities2(Names.EXTRA_UTILITIES2),
    Forestry(Names.FORESTRY),
    GalacticraftCore(Names.GALACTICRAFT_CORE),
    Genetics(Names.GENETICS),
    GregTech(Names.GREGTECH),
    GregTechFoodOption(Names.GREGTECH_FOOD_OPTION),
    GroovyScript(Names.GROOVY_SCRIPT),
    GTCE2OC(Names.GTCE_2_OC),
    HWYLA(Names.HWYLA),
    ImmersiveEngineering(Names.IMMERSIVE_ENGINEERING),
    IndustrialCraft2(Names.INDUSTRIAL_CRAFT2),
    InventoryTweaks(Names.INVENTORY_TWEAKS),
    JourneyMap(Names.JOURNEY_MAP),
    JustEnoughItems(Names.JUST_ENOUGH_ITEMS),
    MagicBees(Names.MAGIC_BEES),
    Nothirium(Names.NOTHIRIUM),
    NuclearCraft(Names.NUCLEAR_CRAFT, v -> !v.contains("2o")),
    NuclearCraftOverhauled(Names.NUCLEAR_CRAFT, v -> v.contains("2o")),
    OpenComputers(Names.OPEN_COMPUTERS),
    ProjectRedCore(Names.PROJECT_RED_CORE),
    Railcraft(Names.RAILCRAFT),
    RefinedStorage(Names.REFINED_STORAGE),
    TechReborn(Names.TECH_REBORN),
    TheOneProbe(Names.THE_ONE_PROBE),
    TinkersConstruct(Names.TINKERS_CONSTRUCT),
    TOPAddons(Names.TOP_ADDONS),
    VoxelMap(Names.VOXEL_MAP),
    XaerosMinimap(Names.XAEROS_MINIMAP),

    // Special Optifine handler, but consolidated here for simplicity
    Optifine(null) {

        @Override
        public boolean isModLoaded() {
            if (this.modLoaded == null) {
                try {
                    Class<?> c = Class.forName("net.optifine.shaders.Shaders");
                    Field f = c.getDeclaredField("shaderPackLoaded");
                    f.setAccessible(true);
                    this.modLoaded = f.getBoolean(null);
                } catch (Exception ignored) {
                    this.modLoaded = false;
                }
            }
            return this.modLoaded;
        }
    };

    public static class Names {

        public static final String ADVANCED_ROCKETRY = "advancedrocketry";
        public static final String APPLIED_ENERGISTICS2 = "appliedenergistics2";
        public static final String BAUBLES = "baubles";
        public static final String BINNIE_CORE = "binniecore";
        public static final String BIOMES_O_PLENTY = "biomesoplenty";
        public static final String BUILD_CRAFT_CORE = "buildcraftcore";
        public static final String CHISEL = "chisel";
        public static final String COFH_CORE = "cofhcore";
        public static final String CONNECTED_TEXTURES_MOD = "ctm";
        public static final String CUBIC_CHUNKS = "cubicchunks";
        public static final String CRAFT_TWEAKER = "crafttweaker";
        public static final String ENDER_CORE = "endercore";
        public static final String ENDER_IO = "enderio";
        public static final String EXTRA_BEES = "extrabees";
        public static final String EXTRA_TREES = "extratrees";
        public static final String EXTRA_UTILITIES2 = "extrautils2";
        public static final String FORESTRY = "forestry";
        public static final String GALACTICRAFT_CORE = "galacticraftcore";
        public static final String GENETICS = "genetics";
        public static final String GREGTECH = GTValues.MODID;
        public static final String GREGTECH_FOOD_OPTION = "gregtechfoodoption";
        public static final String GROOVY_SCRIPT = "groovyscript";
        public static final String GTCE_2_OC = "gtce2oc";
        public static final String HWYLA = "hwyla";
        public static final String IMMERSIVE_ENGINEERING = "immersiveengineering";
        public static final String INDUSTRIAL_CRAFT2 = "ic2";
        public static final String INVENTORY_TWEAKS = "inventorytweaks";
        public static final String JOURNEY_MAP = "journeymap";
        public static final String JUST_ENOUGH_ITEMS = "jei";
        public static final String MAGIC_BEES = "magicbees";
        public static final String NOTHIRIUM = "nothirium";
        public static final String NUCLEAR_CRAFT = "nuclearcraft";
        public static final String OPEN_COMPUTERS = "opencomputers";
        public static final String PROJECT_RED_CORE = "projred-core";
        public static final String RAILCRAFT = "railcraft";
        public static final String REFINED_STORAGE = "refinedstorage";
        public static final String TECH_REBORN = "techreborn";
        public static final String THE_ONE_PROBE = "theoneprobe";
        public static final String TINKERS_CONSTRUCT = "tconstruct";
        public static final String TOP_ADDONS = "topaddons";
        public static final String VOXEL_MAP = "voxelmap";
        public static final String XAEROS_MINIMAP = "xaerominimap";
    }

    private final String ID;
    private final Function<String, Boolean> versionTester;
    protected Boolean modLoaded;

    Mods(String ID) {
        this.ID = ID;
        this.versionTester = null;
    }

    /**
     * @param versionTester A function to test if a specific version is loaded.
     *                      Used in cases like NC vs NCO, where the mod id is the same
     *                      so the version has to be parsed to test which is loaded.
     */
    Mods(String ID, Function<String, Boolean> versionTester) {
        this.ID = ID;
        this.versionTester = versionTester;
    }

    public boolean isModLoaded() {
        if (this.modLoaded == null) {
            this.modLoaded = Loader.isModLoaded(this.ID);
            if (this.modLoaded && this.versionTester != null) {
                ModContainer container = Loader.instance().getIndexedModList().get(this.ID);
                if (!this.versionTester.apply(container.getVersion())) {
                    this.modLoaded = false;
                }
            }
        }
        return this.modLoaded;
    }

    /**
     * Throw an exception if this mod is found to be loaded.
     * <strong>This must be called in or after
     * {@link net.minecraftforge.fml.common.event.FMLPreInitializationEvent}!</strong>
     */
    public void throwIncompatibilityIfLoaded(String... customMessages) {
        if (isModLoaded()) {
            String modName = TextFormatting.BOLD + ID + TextFormatting.RESET;
            List<String> messages = new ArrayList<>();
            messages.add(modName + " mod detected, this mod is incompatible with GregTech CE Unofficial.");
            messages.addAll(Arrays.asList(customMessages));
            if (FMLLaunchHandler.side() == Side.SERVER) {
                throw new RuntimeException(String.join(",", messages));
            } else {
                throwClientIncompatibility(messages);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private static void throwClientIncompatibility(List<String> messages) {
        throw new ModIncompatibilityException(messages);
    }

    public ItemStack getItem(@NotNull String name) {
        return getItem(name, 0, 1, null);
    }

    @NotNull
    public ItemStack getItem(@NotNull String name, int meta) {
        return getItem(name, meta, 1, null);
    }

    @NotNull
    public ItemStack getItem(@NotNull String name, int meta, int amount) {
        return getItem(name, meta, amount, null);
    }

    @NotNull
    public ItemStack getItem(@NotNull String name, int meta, int amount, @Nullable String nbt) {
        if (isModLoaded()) {
            return ItemStack.EMPTY;
        }
        return GameRegistry.makeItemStack(ID + ":" + name, meta, amount, nbt);
    }
}
