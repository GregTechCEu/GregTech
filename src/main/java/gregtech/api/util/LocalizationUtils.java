package gregtech.api.util;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class LocalizationUtils {

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("/n");

    /**
     * This function calls {@link net.minecraft.client.resources.I18n#format(String, Object...)} when called on client
     * or {@link net.minecraft.util.text.translation.I18n#translateToLocalFormatted(String, Object...)} when called on
     * server.
     * <ul>
     * <li>It is intended that translations should be done using {@link net.minecraft.client.resources.I18n} on the
     * client.</li>
     * <li>For setting up translations on the server you should use
     * {@link net.minecraft.util.text.TextComponentTranslation}.</li>
     * <li>{@code LocalizationUtils} is only for cases where some kind of translation is required on the server and
     * there is no client/player in context.</li>
     * <li>{@code LocalizationUtils} is "best effort" and will probably only work properly with {@code en-us}.</li>
     * </ul>
     *
     * @param key the localization key passed to the underlying hasKey function
     * @return a boolean indicating if the given localization key has localisations
     */
    public static String format(String key, Object... format) {
        if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
            return net.minecraft.util.text.translation.I18n.translateToLocalFormatted(key, format);
        } else {
            return net.minecraft.client.resources.I18n.format(key, format);
        }
    }

    /**
     * This function calls {@link net.minecraft.client.resources.I18n#hasKey(String)} when called on client
     * or {@link net.minecraft.util.text.translation.I18n#canTranslate(String)} when called on server.
     * <ul>
     * <li>It is intended that translations should be done using {@link net.minecraft.client.resources.I18n} on the
     * client.</li>
     * <li>For setting up translations on the server you should use
     * {@link net.minecraft.util.text.TextComponentTranslation}.</li>
     * <li>{@code LocalizationUtils} is only for cases where some kind of translation is required on the server and
     * there is no client/player in context.</li>
     * <li>{@code LocalizationUtils} is "best effort" and will probably only work properly with {@code en-us}.</li>
     * </ul>
     *
     * @param key the localization key passed to the underlying hasKey function
     * @return a boolean indicating if the given localization key has localisations
     */
    public static boolean hasKey(String key) {
        if (FMLCommonHandler.instance().getSide() == Side.SERVER) {
            return net.minecraft.util.text.translation.I18n.canTranslate(key);
        } else {
            return net.minecraft.client.resources.I18n.hasKey(key);
        }
    }

    /**
     * Returns translated text corresponding to given key {@code key} on current locale,
     * split with text {@code '\n'}.
     *
     * @param key  localization key
     * @param args substitutions
     * @return translated text split with text {@code '\n'}
     */
    @NotNull
    public static String[] formatLines(String key, Object... args) {
        return NEW_LINE_PATTERN.split(format(key, args));
    }
}
