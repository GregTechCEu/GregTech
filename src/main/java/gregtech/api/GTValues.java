package gregtech.api;

import gregtech.api.util.XSTR;
import gregtech.common.ConfigHolder;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.time.LocalDate;
import java.util.Random;
import java.util.function.Supplier;

import static net.minecraft.util.text.TextFormatting.*;

/**
 * Made for static imports, this Class is just a Helper.
 */
public class GTValues {

    /**
     * <p/>
     * This is worth exactly one normal Item.
     * This Constant can be divided by many commonly used Numbers such as
     * 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 15, 16, 18, 20, 21, 24, ... 64 or 81
     * without loosing precision and is for that reason used as Unit of Amount.
     * But it is also small enough to be multiplied with larger Numbers.
     * <p/>
     * This is used to determine the amount of Material contained inside a prefixed Ore.
     * For example Nugget = M / 9 as it contains out of 1/9 of an Ingot.
     */
    public static final long M = 3628800;

    /**
     * Renamed from "FLUID_MATERIAL_UNIT" to just "L"
     * <p/>
     * Fluid per Material Unit (Prime Factors: 3 * 3 * 2 * 2 * 2 * 2)
     */
    public static final int L = 144;

    /**
     * The Item WildCard Tag. Even shorter than the "-1" of the past
     */
    public static final short W = OreDictionary.WILDCARD_VALUE;

    public static final Random RNG = new XSTR();

    /** Current time on the Client. Will always be zero on the server. */
    public static long CLIENT_TIME = 0;

    /**
     * The Voltage Tiers. Use this Array instead of the old named Voltage Variables
     */
    public static final long[] V = { 8, 32, 128, 512, 2048, 8192, 32768, 131072, 524288, 2097152, 8388608, 33554432,
            134217728, 536870912, Integer.MAX_VALUE };

    /**
     * The Voltage Tiers divided by 2.
     */
    public static final int[] VH = { 4, 16, 64, 256, 1024, 4096, 16384, 65536, 262144, 1048576, 4194304, 16777216,
            67108864, 268435456, 1073741824 };

    /**
     * The Voltage Tiers adjusted for cable loss. Use this for recipe EU/t to avoid full-amp recipes
     */
    public static final int[] VA = { 7, 30, 120, 480, 1920, 7680, 30720, 122880, 491520, 1966080, 7864320, 31457280,
            125829120, 503316480, 2013265920 };

    /**
     * The Voltage Tiers adjusted for cable loss, divided by 2.
     */
    public static final int[] VHA = { 7, 16, 60, 240, 960, 3840, 15360, 61440, 245760, 983040, 3932160, 15728640,
            62914560, 251658240, 1006632960 };

    public static final int ULV = 0;
    public static final int LV = 1;
    public static final int MV = 2;
    public static final int HV = 3;
    public static final int EV = 4;
    public static final int IV = 5;
    public static final int LuV = 6;
    public static final int ZPM = 7;
    public static final int UV = 8;

    public static final int UHV = 9;
    public static final int UEV = 10;
    public static final int UIV = 11;
    public static final int UXV = 12;
    public static final int OpV = 13;
    public static final int MAX = 14;

    /**
     * The short names for the voltages, used for registration primarily
     */
    public static final String[] VN = new String[] { "ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "UHV",
            "UEV", "UIV", "UXV", "OpV", "MAX" };

    /**
     * The short names for the voltages, formatted for text
     */
    public static final String[] VNF = new String[] {
            DARK_GRAY + "ULV", GRAY + "LV", AQUA + "MV",
            GOLD + "HV", DARK_PURPLE + "EV", DARK_BLUE + "IV",
            LIGHT_PURPLE + "LuV", RED + "ZPM", DARK_AQUA + "UV",
            DARK_RED + "UHV", GREEN + "UEV", DARK_GREEN + "UIV",
            YELLOW + "UXV", BLUE + "OpV", RED.toString() + BOLD + "MAX" };

    /**
     * Color values for the voltages
     */
    public static final int[] VC = new int[] { 0xC80000, 0xDCDCDC, 0xFF6400, 0xFFFF1E, 0x808080, 0xF0F0F5, 0xE99797,
            0x7EC3C4, 0x7EB07E, 0xBF74C0, 0x0B5CFE, 0x914E91, 0x488748, 0x8C0000, 0x2828F5 };

    /**
     * The long names for the voltages
     */
    public static final String[] VOLTAGE_NAMES = new String[] { "Ultra Low Voltage", "Low Voltage", "Medium Voltage",
            "High Voltage", "Extreme Voltage", "Insane Voltage", "Ludicrous Voltage", "ZPM Voltage", "Ultimate Voltage",
            "Ultra High Voltage", "Ultra Excessive Voltage", "Ultra Immense Voltage", "Ultra Extreme Voltage",
            "Overpowered Voltage", "Maximum Voltage" };

    /**
     * ModID strings, since they are quite common parameters
     */
    public static final String MODID = "gregtech",
            MODID_FR = "forestry",
            MODID_CT = "crafttweaker",
            MODID_TOP = "theoneprobe",
            MODID_CTM = "ctm",
            MODID_CC = "cubicchunks",
            MODID_AR = "advancedrocketry",
            MODID_ECORE = "endercore",
            MODID_EIO = "enderio",
            MODID_BC = "buildcraftcore",
            MODID_COFH = "cofhcore",
            MODID_APPENG = "appliedenergistics2",
            MODID_JEI = "jei",
            MODID_GROOVYSCRIPT = "groovyscript",
            MODID_NC = "nuclearcraft",
            MODID_IE = "immersiveengineering",
            MODID_OC = "opencomputers",
            MODID_JOURNEYMAP = "journeymap",
            MODID_VOXELMAP = "voxelmap",
            MODID_XAERO_MINIMAP = "xaerominimap",
            MODID_HWYLA = "hwyla",
            MODID_BAUBLES = "baubles",
            MODID_TOP_ADDONS = "topaddons",
            MODID_IC2 = "ic2",
            MODID_GTFO = "gregtechfoodoption",
            MODID_BINNIE = "binniecore",
            MODID_XU2 = "extrautils2",
            MODID_TR = "techreborn",
            MODID_MB = "magicbees",
            MODID_EB = "extrabees",
            MODID_ET = "extratrees",
            MODID_GENETICS = "genetics",
            MODID_BOP = "biomesoplenty",
            MODID_TCON = "tconstruct";

    private static Boolean isClient;

    public static boolean isClientSide() {
        if (isClient == null) isClient = FMLCommonHandler.instance().getSide().isClient();
        return isClient;
    }

    private static Boolean isDeobf;

    public static boolean isDeobfEnvironment() {
        if (isDeobf == null) isDeobf = FMLLaunchHandler.isDeobfuscatedEnvironment();
        return isDeobf;
    }

    /**
     * Default fallback value used for Map keys.
     * Currently only used in {@link gregtech.loaders.recipe.CraftingComponent}.
     */
    public static final int FALLBACK = -1;

    public static Supplier<Boolean> FOOLS = () -> {
        String[] yearMonthDay = LocalDate.now().toString().split("-");
        return ConfigHolder.misc.specialEvents && yearMonthDay[1].equals("04") && yearMonthDay[2].equals("01");
    };

    public static Supplier<Boolean> XMAS = () -> {
        String[] yearMonthDay = LocalDate.now().toString().split("-");
        return ConfigHolder.misc.specialEvents && yearMonthDay[1].equals("12") &&
                (yearMonthDay[2].equals("24") || yearMonthDay[2].equals("25"));
    };
}
