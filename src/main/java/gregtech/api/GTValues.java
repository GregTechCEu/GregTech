package gregtech.api;

import gregtech.GregTechVersion;
import gregtech.api.util.XSTR;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
     * Version String for use in addon mods' @Mod(dependencies = "...") block.
     */
    public static final String MOD_VERSION_DEP = "required-after:gregtech@[" + GregTechVersion.VERSION + ",);";

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

    /**
     * The Voltage Tiers. Use this Array instead of the old named Voltage Variables
     */
    public static final long[] V = new long[]{8, 32, 128, 512, 2048, 8192, 32768, 131072, 524288, 2097152, 8388608, 33554432, 134217728, 536870912, Integer.MAX_VALUE};


    /**
     * The Voltage Tiers adjusted for cable loss. Use this for recipe EU/t to avoid full-amp recipes
     */
    public static final int[] VA = new int[]{7, 30, 120, 480, 1920, 7680, 30720, 122880, 491520, 1966080, 7864320, 31457280, 125829120, 503316480, 2013265920};

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
    public static final String[] VN = new String[]{"ULV", "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "UHV", "UEV", "UIV", "UXV", "OpV", "MAX"};

    /**
     * The short names for the voltages, formatted for text
     */
    public static final String[] VNF = new String[]{
            DARK_GRAY + "ULV", GRAY + "LV", AQUA + "MV",
            GOLD + "HV", DARK_PURPLE + "EV", DARK_BLUE + "IV",
            LIGHT_PURPLE + "LuV", WHITE + "ZPM", DARK_AQUA + "UV",
            DARK_RED + "UHV", GREEN + "UEV", DARK_GREEN + "UIV",
            YELLOW + "UXV", BLUE + "OpV", RED + "MAX"};

    /**
     * Color values for the voltages
     */
    public static final int[] VC = new int[]{0xB4B4B4, 0xDCDCDC, 0xFF6400, 0xFFFF1E, 0x808080, 0xF0F0F5, 0xDCDCF5, 0xC8C8F5, 0xB4B4F5, 0xA0A0F5, 0x8C8CF5, 0x7878F5, 0x6464F5, 0x5050F5, 0x2828F5};

    /**
     * The long names for the voltages
     */
    public static final String[] VOLTAGE_NAMES = new String[]{"Ultra Low Voltage", "Low Voltage", "Medium Voltage", "High Voltage", "Extreme Voltage", "Insane Voltage", "Ludicrous Voltage", "ZPM Voltage", "Ultimate Voltage",
            "Ultra High Voltage", "Ultra Excessive Voltage", "Ultra Immense Voltage", "Ultra Extreme Voltage", "Overpowered Voltage", "Maximum Voltage"};

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
            MODID_EIO = "enderio",
            MODID_BC = "buildcraftcore",
            MODID_COFH = "cofhcore",
            MODID_APPENG = "appliedenergistics2",
            MODID_JEI = "jei",
            MODID_GROOVYSCRIPT = "groovyscript";

    private static Boolean isClient;

    public static boolean isClientSide() {
        if (isClient == null) isClient = FMLCommonHandler.instance().getSide().isClient();
        return isClient;
    }

    /**
     * Default fallback value used for Map keys.
     * Currently only used in {@link gregtech.loaders.recipe.CraftingComponent}.
     */
    public static final int FALLBACK = -1;

    /**
     * Used to tell if any high-tier machine (UHV+) was registered.
     */
    public static boolean HT = false;

    public static Supplier<Boolean> FOOLS = () -> {
        String[] yearMonthDay = LocalDate.now().toString().split("-");
        return yearMonthDay[1].equals("04") && yearMonthDay[2].equals("01");
    };
}
